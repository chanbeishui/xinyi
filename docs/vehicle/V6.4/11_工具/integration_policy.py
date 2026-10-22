"""Executable design reference; no DB/network/KMS. Not the production payment service."""
from __future__ import annotations
from dataclasses import dataclass,field
from copy import deepcopy
import hashlib,json,re
class PolicyError(ValueError): pass

def digest(value:dict)->str:
    return hashlib.sha256(json.dumps(value,ensure_ascii=False,sort_keys=True,separators=(',',':')).encode()).hexdigest()

def validate(kind:str, config:dict, secrets:dict|None=None, env:str='LIVE')->list[str]:
    s=secrets or {}; errors=[]
    def required(*keys):
        for k in keys:
            if config.get(k) in (None,'',[]): errors.append(k)
    def secret(k,length=None):
        value=s.get(k)
        if not isinstance(value,str) or not value or set(value)=={'*'}:errors.append(k)
        elif length and len(value.encode('utf-8'))!=length:errors.append(k)
    if kind=='PAYMENT_POLICY':
        if not isinstance(config.get('offlineEnabled'),bool):errors.append('offlineEnabled')
        if not isinstance(config.get('wechatEnabled'),bool):errors.append('wechatEnabled')
        if config.get('wechatEnabled') and not config.get('merchantReady'):errors.append('wechatMerchant')
        if config.get('offlineEnabled'):
            required('offlineReceiverName','offlineInstructions','offlineMethods','offlineContactName','offlineContactPhone')
            methods=config.get('offlineMethods',[])
            if any(x not in ['CASH','BANK_TRANSFER'] for x in methods):errors.append('offlineMethods')
            if 'BANK_TRANSFER'in methods:
                required('offlineAccountName','offlineBankName');secret('offlineBankAccount')
            if config.get('offlineCollectionMode')!='PREPAID_MANUAL_VERIFIED':errors.append('offlineCollectionMode')
    elif kind=='WECHAT_PAY':
        required('merchantName','mchid','appId','merchantCertificateSerial')
        secret('merchantPrivateKey');secret('apiV3Key',32)
        if config.get('verificationMode')=='WECHATPAY_PUBLIC_KEY':required('wechatPayPublicKeyId','wechatPayPublicKeyPem')
        elif config.get('verificationMode')=='PLATFORM_CERT':required('platformCertificates')
        else:errors.append('verificationMode')
        if not config.get('bindingAcknowledged'):errors.append('bindingAcknowledged')
        if config.get('appId')!=config.get('loginAppId'):errors.append('appIdMismatch')
    elif kind=='TENCENT_SMS':
        required('secretId','region','smsSdkAppId','signName');secret('secretKey')
        if config.get('apiVersion')!='2021-01-11':errors.append('apiVersion')
    elif kind=='ALIYUN_SMS':
        required('accessKeyId','region','signName');secret('accessKeySecret')
        if config.get('apiVersion')!='2017-05-25':errors.append('apiVersion')
    elif kind=='WECHAT_APP':required('appId');secret('appSecret')
    elif kind=='SUBSCRIBE':
        required('templateId','appId','pageRoute','parameters')
        route=config.get('pageRoute','')
        if route.startswith(('http:','https:','//')) or '..' in route or re.search(r'(phone|token|secret)=',route,re.I):errors.append('pageRoute')
        if env=='LIVE'and config.get('miniProgramState')!='formal':errors.append('miniProgramState')
        if config.get('subscriptionType','ONCE')=='LONG'and not config.get('qualificationEvidence'):errors.append('qualificationEvidence')
        keys=[p.get('key')for p in config.get('parameters',[])]
        if len(keys)!=len(set(keys)) or any(not re.fullmatch('[A-Za-z]+[0-9]+',k or '')for k in keys):errors.append('parameters')
    else:errors.append('kind')
    return errors

@dataclass
class VersionedConfig:
    active:dict=field(default_factory=dict)
    draft:dict=field(default_factory=dict)
    version:int=0
    checked_hash:str|None=None
    history:list=field(default_factory=list)
    def save(self,value:dict,expected:int):
        if expected!=self.version:raise PolicyError('VERSION_CONFLICT')
        self.draft=deepcopy(value);self.version+=1;self.checked_hash=None
    def check(self,errors:list):
        if errors:raise PolicyError('CONFIG_INVALID:'+','.join(errors))
        self.checked_hash=digest(self.draft)
    def publish(self,expected:int,ack_no_methods:bool=False):
        if expected!=self.version:raise PolicyError('VERSION_CONFLICT')
        if self.checked_hash!=digest(self.draft):raise PolicyError('CHECK_STALE')
        if 'wechatEnabled'in self.draft and not self.draft.get('wechatEnabled')and not self.draft.get('offlineEnabled')and not ack_no_methods:raise PolicyError('PAUSE_ACK_REQUIRED')
        self.active=deepcopy(self.draft);self.version+=1;self.history.append(deepcopy(self.active))

@dataclass
class Payment:
    amount:int
    method:str='UNSELECTED'
    status:str='PENDING'
    merchant:str|None=None
    paid:int=0
    capacity:str='HELD'
    proof:dict|None=None
    def choose(self,method:str,enabled:dict,merchant:str|None=None):
        if self.method not in ('UNSELECTED',method):raise PolicyError('METHOD_LOCKED')
        if not enabled.get(method,False):raise PolicyError('METHOD_DISABLED')
        if method=='WECHAT'and not merchant:raise PolicyError('MERCHANT_REQUIRED')
        self.method=method;self.merchant=merchant
    def register(self,amount:int,operator:str,reference:str):
        if self.method!='OFFLINE':raise PolicyError('WRONG_METHOD')
        if self.proof and self.proof['status']!='REJECTED':raise PolicyError('DUPLICATE_PROOF')
        if amount!=self.amount or amount<=0:raise PolicyError('AMOUNT_MISMATCH')
        if not reference:raise PolicyError('REFERENCE_REQUIRED')
        self.proof={'amount':amount,'operator':operator,'reference':reference,'status':'SUBMITTED'};self.status='REVIEW_PENDING'
    def verify(self,operator:str,has_capacity:bool=True):
        if not self.proof or self.proof['status']!='SUBMITTED':raise PolicyError('STATE_CONFLICT')
        if operator==self.proof['operator']:raise PolicyError('SELF_REVIEW')
        self.proof['status']='VERIFIED';self.paid=self.amount;self.status='SUCCESS'
        if self.capacity=='RELEASED':self.capacity='REACQUIRED'if has_capacity else'MANUAL_CONFLICT'
    def expire(self):
        if self.status=='SUCCESS':return
        self.capacity='RELEASED'
        if self.status=='PENDING':self.status='CLOSED'
    def historical_action(self,action:str,current_switch:bool):
        if action not in ('QUERY','REFUND','CALLBACK'):raise PolicyError('NOT_HISTORICAL')
        return self.method,self.merchant

def map_sms(provider:str,bindings:list[dict],values:dict):
    if not bindings: return []if provider=='TENCENT_SMS'else'{}'
    result={}
    for i,b in enumerate(bindings):
        key=b['parameterKey'];value=values.get(b['variable'])
        if value is None:raise PolicyError('MISSING_VARIABLE')
        if len(str(value))>b.get('maxLength',500):raise PolicyError('VARIABLE_TOO_LONG')
        if key in result:raise PolicyError('DUPLICATE_PARAMETER')
        if provider=='TENCENT_SMS'and key!=str(i+1):raise PolicyError('POSITIONAL_MISMATCH')
        result[key]=str(value)
    return list(result.values())if provider=='TENCENT_SMS'else json.dumps(result,ensure_ascii=False,separators=(',',':'))

def may_retry(status:str,enabled:bool=True,expired:bool=False)->bool:
    return status=='FAILED_NOT_SENT'and enabled and not expired

def can_subscribe_send(actor:int,owner:int,reported_ids:set,actual_id:str,enabled:bool,event:bool)->bool:
    # This is local eligibility only; the vendor remains the final authority on quota/consent.
    return actor==owner and actual_id in reported_ids and enabled and event

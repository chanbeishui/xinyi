"""Pure-reference and actual OpenAPI checks, not backend integration evidence."""
from pathlib import Path
import unittest,json,yaml,copy
from integration_policy import *
R=Path(__file__).resolve().parents[1]
class PolicyTests(unittest.TestCase):
 def test_draft_not_effective(self):
  v=VersionedConfig(active={'a':1});v.save({'a':2},0);self.assertEqual(v.active,{'a':1})
 def test_publish_check_required(self):
  v=VersionedConfig();v.save({'a':1},0)
  with self.assertRaisesRegex(PolicyError,'CHECK_STALE'):v.publish(1)
 def test_publish_version(self):
  v=VersionedConfig();v.save({'a':1},0);v.check([])
  with self.assertRaisesRegex(PolicyError,'VERSION_CONFLICT'):v.publish(0)
 def test_edit_invalidates_check(self):
  v=VersionedConfig();v.save({'a':1},0);v.check([]);v.save({'a':2},1)
  with self.assertRaisesRegex(PolicyError,'CHECK_STALE'):v.publish(2)
 def test_publish_copies(self):
  v=VersionedConfig();v.save({'a':1},0);v.check([]);v.publish(1);v.draft['a']=2;self.assertEqual(v.active['a'],1)
 def test_dual_close_ack(self):
  v=VersionedConfig();v.save(dict(wechatEnabled=False,offlineEnabled=False),0);v.check([])
  with self.assertRaisesRegex(PolicyError,'PAUSE_ACK'):v.publish(1)
  v.publish(1,True);self.assertFalse(v.active['wechatEnabled'])
 def test_offline_independent(self):
  c=dict(wechatEnabled=False,offlineEnabled=True,offlineReceiverName='DEMO',offlineInstructions='DEMO',offlineCollectionMode='PREPAID_MANUAL_VERIFIED',offlineMethods=['CASH'],offlineContactName='DEMO',offlineContactPhone='000-TEST');self.assertEqual(validate('PAYMENT_POLICY',c),[])
 def test_wechat_requires_merchant(self):self.assertIn('wechatMerchant',validate('PAYMENT_POLICY',dict(offlineEnabled=False,wechatEnabled=True)))
 def test_both_independent(self):self.assertEqual(validate('PAYMENT_POLICY',dict(offlineEnabled=True,wechatEnabled=True,merchantReady=True,offlineReceiverName='D',offlineInstructions='D',offlineCollectionMode='PREPAID_MANUAL_VERIFIED',offlineMethods=['CASH'],offlineContactName='DEMO',offlineContactPhone='000-TEST')),[])
 def wx(self):return dict(merchantName='DEMO',mchid='1900000001',appId='APP',loginAppId='APP',merchantCertificateSerial='SERIAL',verificationMode='WECHATPAY_PUBLIC_KEY',wechatPayPublicKeyId='PUB_ID',wechatPayPublicKeyPem='DEMO',bindingAcknowledged=True)
 def test_wechat_fields(self):self.assertEqual(validate('WECHAT_PAY',self.wx(),dict(merchantPrivateKey='DEMO_NOT_REAL',apiV3Key='x'*32)),[])
 def test_key_bytes_not_chars(self):self.assertIn('apiV3Key',validate('WECHAT_PAY',self.wx(),dict(merchantPrivateKey='D',apiV3Key='汉'*32)))
 def test_mask_not_secret(self):self.assertIn('apiV3Key',validate('WECHAT_PAY',self.wx(),dict(merchantPrivateKey='D',apiV3Key='*'*32)))
 def test_app_mismatch(self):c=self.wx();c['loginAppId']='OTHER';self.assertIn('appIdMismatch',validate('WECHAT_PAY',c,dict(merchantPrivateKey='D',apiV3Key='x'*32)))
 def test_platform_cert_conditional(self):c=self.wx();c['verificationMode']='PLATFORM_CERT';self.assertIn('platformCertificates',validate('WECHAT_PAY',c,dict(merchantPrivateKey='D',apiV3Key='x'*32)))
 def test_tencent_fields(self):c=dict(secretId='D',region='ap-guangzhou',smsSdkAppId='1400000',signName='DEMO',apiVersion='2021-01-11');self.assertEqual(validate('TENCENT_SMS',c,dict(secretKey='DEMO')),[])
 def test_aliyun_fields(self):c=dict(accessKeyId='D',region='cn-hangzhou',signName='DEMO',apiVersion='2017-05-25');self.assertEqual(validate('ALIYUN_SMS',c,dict(accessKeySecret='DEMO')),[])
 def test_sms_parameter_order(self):self.assertEqual(map_sms('TENCENT_SMS',[dict(parameterKey='1',variable='code'),dict(parameterKey='2',variable='ttlMinutes')],dict(code='123456',ttlMinutes=5)),['123456','5'])
 def test_sms_named(self):self.assertEqual(json.loads(map_sms('ALIYUN_SMS',[dict(parameterKey='number',variable='code')],dict(code='123456'))),{'number':'123456'})
 def test_missing_variable(self):
  with self.assertRaisesRegex(PolicyError,'MISSING_VARIABLE'):map_sms('TENCENT_SMS',[dict(parameterKey='1',variable='code')],{})
 def test_wrong_sequence(self):
  with self.assertRaisesRegex(PolicyError,'POSITIONAL'):map_sms('TENCENT_SMS',[dict(parameterKey='2',variable='code')],{'code':'D'})
 def test_unknown_no_retry(self):self.assertFalse(may_retry('UNKNOWN'))
 def test_accepted_no_retry(self):self.assertFalse(may_retry('ACCEPTED'))
 def test_failed_retry(self):self.assertTrue(may_retry('FAILED_NOT_SENT'))
 def test_disabled_no_retry(self):self.assertFalse(may_retry('FAILED_NOT_SENT',False))
 def test_expired_no_retry(self):self.assertFalse(may_retry('FAILED_NOT_SENT',True,True))
 def test_offline_not_paid(self):p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});self.assertEqual(p.paid,0);self.assertEqual(p.status,'PENDING')
 def test_methods_locked(self):
  p=Payment(34000);p.choose('WECHAT',{'WECHAT':True},'MERCHANT')
  with self.assertRaisesRegex(PolicyError,'METHOD_LOCKED'):p.choose('OFFLINE',{'OFFLINE':True})
 def test_switch_disabled(self):
  with self.assertRaisesRegex(PolicyError,'METHOD_DISABLED'):Payment(34000).choose('WECHAT',{'WECHAT':False},'MERCHANT')
 def test_proof_not_paid(self):p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});p.register(34000,'A','REF');self.assertEqual(p.status,'REVIEW_PENDING');self.assertEqual(p.paid,0)
 def test_self_review(self):
  p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});p.register(34000,'A','REF')
  with self.assertRaisesRegex(PolicyError,'SELF_REVIEW'):p.verify('A')
 def test_independent_review(self):p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});p.register(34000,'A','REF');p.verify('B');self.assertEqual(p.paid,34000)
 def test_duplicate_proof(self):
  p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});p.register(34000,'A','REF')
  with self.assertRaisesRegex(PolicyError,'DUPLICATE'):p.register(34000,'A','REF2')
 def test_partial_no_success(self):
  p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True})
  with self.assertRaisesRegex(PolicyError,'AMOUNT_MISMATCH'):p.register(10000,'A','REF')
 def test_review_timeout_not_false_unpaid(self):p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});p.register(34000,'A','R');p.expire();self.assertEqual(p.status,'REVIEW_PENDING');self.assertEqual(p.capacity,'RELEASED')
 def test_late_money_capacity_conflict(self):p=Payment(34000);p.choose('OFFLINE',{'OFFLINE':True});p.register(34000,'A','R');p.expire();p.verify('B',False);self.assertEqual(p.paid,34000);self.assertEqual(p.capacity,'MANUAL_CONFLICT')
 def test_historic_disabled(self):p=Payment(1);p.choose('WECHAT',{'WECHAT':True},'ORIGINAL');self.assertEqual(p.historical_action('REFUND',False),('WECHAT','ORIGINAL'))
 def test_subscribe_declined(self):self.assertFalse(can_subscribe_send(1,1,set(),'T',True,True))
 def test_subscribe_wrong_account(self):self.assertFalse(can_subscribe_send(2,1,{'T'},'T',True,True))
 def test_new_template_no_inherit(self):self.assertFalse(can_subscribe_send(1,1,{'OLD'},'NEW',True,True))
 def test_subscribe_real_event_needed(self):self.assertFalse(can_subscribe_send(1,1,{'T'},'T',True,False))
 def test_template_unsafe_page(self):c=dict(templateId='T',appId='A',pageRoute='https://evil.invalid/?phone=1',parameters=[{'key':'thing1'}],miniProgramState='formal');self.assertIn('pageRoute',validate('SUBSCRIBE',c))
 def test_template_live_env(self):c=dict(templateId='T',appId='A',pageRoute='pages/order',parameters=[{'key':'thing1'}],miniProgramState='developer');self.assertIn('miniProgramState',validate('SUBSCRIBE',c))
 def test_long_not_automatic(self):c=dict(templateId='T',appId='A',pageRoute='pages/order',parameters=[{'key':'thing1'}],miniProgramState='formal',subscriptionType='LONG');self.assertIn('qualificationEvidence',validate('SUBSCRIBE',c))
class ContractTests(unittest.TestCase):
 @classmethod
 def setUpClass(cls):cls.a=yaml.safe_load((R/'04_接口/openapi_V6.4.yaml').read_text());cls.m=json.loads((R/'05_数据设计/data_model_V6.4.json').read_text())
 def test_payment_status(self):self.assertIn('REVIEW_PENDING',self.a['components']['schemas']['PaymentStatus']['enum'])
 def test_no_plain_secret_in_read(self):
  S=self.a['components']['schemas'];seen=set();keys=set()
  def walk(x):
   if isinstance(x,dict):
    if '$ref'in x:
     k=x['$ref'].split('/')[-1]
     if k not in seen:seen.add(k);walk(S[k])
    keys.update(x.get('properties',{}))
    for k,v in x.items():
     if k!='$ref':walk(v)
   elif isinstance(x,list):
    for v in x:walk(v)
  walk(S['IntegrationProfileView']);self.assertFalse(keys & {'value','privateKeyPem','appSecretValue','apiV3KeyValue','secretKeyValue'})
 def test_publish_is_separate(self):self.assertIn('/api/admin/integrations/profiles/{profileId}/publish',self.a['paths']);self.assertIn('/api/admin/integrations/profiles/{profileId}/draft',self.a['paths'])
 def test_subscription_producer(self):self.assertIn('/api/common/subscriptions/prompts',self.a['paths'])
 def test_offline_review_distinct(self):p=self.a['paths']['/api/admin/offline-payments/{proofId}/verify']['post'];self.assertTrue(p['x-authorization']['notSelfReview'])
 def test_tables_have_pk(self):self.assertTrue(all(t['primaryKey']for t in self.m))
 def test_no_seeded_real_secret(self):sql=(R/'05_数据设计/001_vi业务表_初始化_V6.4.sql').read_text();self.assertNotIn('INSERT INTO',sql.upper())
 def test_config_history_immutable(self):w=json.loads((R/'05_数据设计/生成列与写入白名单.json').read_text());self.assertTrue(all(not x['updateColumns']for x in w if x['table']in ['vi_integration_revision','vi_integration_publication','vi_message_template_revision']))
if __name__=='__main__':
 suite=unittest.defaultTestLoader.loadTestsFromModule(__import__(__name__));result=unittest.TextTestRunner(verbosity=1).run(suite)
 report={'kind':'PURE_REFERENCE_AND_OPENAPI_TEST','testsRun':result.testsRun,'failures':len(result.failures),'errors':len(result.errors),'scope':'reference algorithms and actual files only; no MySQL, no xinyi, no vendors','result':'PASS'if result.wasSuccessful()else'FAIL'}
 (R/'09_验证记录/V6.4_配置策略与契约测试.json').write_text(json.dumps(report,ensure_ascii=False,indent=2))
 raise SystemExit(0 if result.wasSuccessful()else 1)

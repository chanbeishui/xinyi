import copy
from pathlib import Path
import pytest,yaml,jsonschema
B=Path(__file__).resolve().parents[2];S=yaml.safe_load((B/'04_接口/openapi_V6.4.yaml').read_text())
def convert(x):
 if isinstance(x,list):return[convert(v)for v in x]
 if not isinstance(x,dict):return x
 d={k:convert(v)for k,v in x.items()if k not in ['nullable','example','discriminator']}
 return {'anyOf':[d,{'type':'null'}]}if x.get('nullable')else d
C=convert(S)
def good():return{'slotId':'12','stationId':'15','inspectionTypeCode':'TEST_INSPECTION','vehicleDraft':{'plateNo':'粤A12345','vehicleTypeCode':'TEST_CAR'},'addressDraft':{'address':'测试路1号','location':{'lng':110.9,'lat':22.3,'coordinateSystem':'GCJ02'}}}
def validate(x):jsonschema.Draft7Validator(C['components']['schemas']['QuoteRequest'],resolver=jsonschema.RefResolver.from_schema(C)).validate(x)
@pytest.mark.parametrize('vehicle,address',[(False,False),(False,True),(True,False),(True,True)])
def test_four_valid_sources(vehicle,address):
 x=good()
 if vehicle:x.pop('vehicleDraft');x['vehicleId']='100'
 if address:x.pop('addressDraft');x['addressId']='200'
 validate(x)
@pytest.mark.parametrize('kind',['no_vehicle','no_address','both_vehicle','both_address','null_vehicle','null_address','no_sources'])
def test_bad_source_rejected(kind):
 x=good()
 if kind=='no_vehicle':x.pop('vehicleDraft')
 elif kind=='no_address':x.pop('addressDraft')
 elif kind=='both_vehicle':x['vehicleId']='123'
 elif kind=='both_address':x['addressId']='124'
 elif kind=='null_vehicle':x['vehicleDraft']=None
 elif kind=='null_address':x['addressDraft']=None
 elif kind=='no_sources':x={'slotId':'12','inspectionTypeCode':'TEST_INSPECTION'}
 with pytest.raises(jsonschema.ValidationError):validate(x)
def test_new_address_id_not_required():
 x=good();validate(x);assert 'addressId'not in x['addressDraft']and'version'not in x['addressDraft']
def test_confirm_returns_public():assert 'DriverStatementPublic' in str(S['paths']['/api/app/driver/statements/{statementId}/confirm']['post']['responses']['200'])
def test_terms_must_be_true():assert S['components']['schemas']['OrderCreate']['properties']['acceptedTerms']['enum']==[True]

@pytest.mark.parametrize('replacement,valid', [({}, True), ({'replacesRuleId':'12','replacesExpectedVersion':3}, True), ({'replacesRuleId':'12'}, False), ({'replacesExpectedVersion':3}, False)])
def test_price_replacement_requires_paired_identity_and_version(replacement,valid):
 x={'expectedVersion':2,'reason':'核实替换版本',**replacement}
 errors=list(jsonschema.Draft7Validator(C['components']['schemas']['PricePublish']).iter_errors(x))
 assert (not errors)==valid

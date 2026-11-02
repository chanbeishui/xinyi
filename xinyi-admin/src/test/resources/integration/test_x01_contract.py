from conftest import Client
from test_integration_config import new_profile,save

def test_all_twelve_success_responses_against_main_contract(base,run):
    c,p=new_profile(base,run)
    c.call('GET','/api/admin/integrations/profiles?pageNum=1&pageSize=100')
    p=save(c,p,{'apiVersion':'2021-01-11','enabled':False})
    path='/api/admin/integrations/profiles/'+p['profileId']
    c.call('GET',path)
    check=c.call('POST',path+'/checks',{'revisionId':p['draftRevisionId'],'expectedVersion':p['version'],'level':'LOCAL'})['data']
    pub=c.call('POST',path+'/publish',{'revisionId':p['draftRevisionId'],'expectedVersion':p['version'],'contentHash':p['draft']['contentHash'],'checkId':check['checkId'],'reason':'契约验证发布'})['data']
    p=c.call('GET',path)['data']
    check=c.call('POST',path+'/checks',{'revisionId':pub['revisionId'],'expectedVersion':p['version'],'level':'LOCAL'})['data']
    c.call('POST',path+'/rollback',{'targetRevisionId':pub['revisionId'],'expectedVersion':p['version'],'checkId':check['checkId'],'reason':'契约验证回滚'})
    c.call('GET',path+'/publications');c.call('GET',path+'/revisions')
    r=c.call('POST','/api/admin/integrations/test-recipients',{'name':'契约测试','kind':'PHONE','phone':'13900008888','ownerDeptId':run['ownerDeptId'],'environment':'TEST','reason':'隔离授权号码'})['data']
    c.call('GET','/api/admin/integrations/test-recipients')
    c.call('POST','/api/admin/integrations/test-recipients/'+r['recipientId']+'/revoke',{'expectedVersion':r['version'],'reason':'结束测试授权'})

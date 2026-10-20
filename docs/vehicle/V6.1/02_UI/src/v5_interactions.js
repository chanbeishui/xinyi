(function(U){'use strict';const prev=U.action, go=id=>U.go(id), val=k=>String(U.get(k,'')).trim();
function need(keys){for(const k of keys)if(!U.get(k)){U.toast('请完成必填项或核对选项');return false;}return true;}
U.action=async function(action,payload='',element=null){const z=U.v5();
 if(action==='training-demo'){go('D39');return;}
 if(action==='appeal'){go('D43');return;}
 if(action==='v5-open-face'){go('D46');return;}
 if(action==='v5-training-read'){z.trainingRead=true;U.toast('已记录演示学习状态');U.testing.render('D40');return;}
 if(action==='v5-attempt-start'){if(!z.trainingRead){U.toast('请先完成学习');return;}z.attempt={id:'ATTEMPT-DEMO-'+Date.now(),submitted:false};z.passed=false;go('D41');return;}
 if(action==='v5-attempt-submit'){if(!z.attempt||z.attempt.submitted){U.toast('该尝试不可重复提交');return;}if(!need(['v5.answer']))return;z.attempt.submitted=true;z.passed=val('v5.answer')==='RETURN';if(z.passed)U.store.saved.training=true;go('D42');return;}
 if(action==='v5-appeal-submit'){if(z.appeal){go('D44');return;}if(!need(['v5.appealReason']))return;z.appeal={id:'APPEAL-DEMO-001',reason:val('v5.appealReason')};go('D44');return;}
 if(action==='v5-appeal-approve'||action==='v5-appeal-reject'){if(z.appealDecision){U.toast('本申诉已处理');return;}if(!need(['v5.appealOpinion']))return;z.appealDecision=action==='v5-appeal-approve'?'APPROVE':'REJECT';z.appealOpinion=val('v5.appealOpinion');U.toast('演示处理已保存');U.testing.render('B45');return;}
 if(action==='v5-face-start'){z.faceStatus='PENDING';U.toast('已创建演示会话，未调用供应商');U.testing.render('D46');return;}
 if(action==='v5-face-query'){if(z.faceStatus!=='PENDING')return;z.faceStatus='DEMO_PASSED';U.store.saved.face=true;U.toast('这是模拟结果，不是实际人脸核验');U.testing.render('D46');return;}
 if(action==='v5-bind-request'){if(!need(['v5.bindReason']))return;z.bindingRequest={requestCode:'BIND-DEMO-001',reason:val('v5.bindReason')};U.testing.render('U02');return;}
 if(action==='v5-bind-issue'){if(!need(['v5.sysUser','v5.issueReason','v5.identityChecked']))return;if(z.bindingConsumed){U.toast('已消费的请求不能再签发');return;}z.bindingRequest=z.bindingRequest||{requestCode:'BIND-DEMO-001',reason:'演示申请'};z.bindingIssued=true;U.toast('仅签发凭证，尚未绑定成功');U.testing.render('B36');return;}
 if(action==='v5-bind-revoke'){z.bindingIssued=false;U.toast('未用凭证已撤销');U.testing.render('B36');return;}
 if(action==='v5-bind-consume'){if(!z.bindingRequest||!z.bindingIssued||z.bindingConsumed||val('v5.credential')!=='DEMO-ONLY-123456'){U.toast('凭证无效、未签发、已撤销或已消费');return;}z.bindingConsumed=true;U.store.binding='BOUND';U.toast('本人已确认演示关联，站点权限仍单独配置');U.testing.render('U02');return;}
 if(action==='v5-course-save'||action==='v5-course-publish'){if(!need(['v5.courseTitle','v5.mediaFileId','v5.duration','v5.passScore','v5.examSeconds','v5.prompt','v5.optionA','v5.optionB','v5.correct']))return;let score=Number(val('v5.passScore')),duration=Number(val('v5.duration'));if(score<0||score>100||!Number.isFinite(score)||duration<1){U.toast('请检查分数和必学时长');return;}z.coursePublished=action==='v5-course-publish';U.toast(z.coursePublished?'演示课程版本已发布':'演示课程草稿已保存');go('B43');return;}
 return prev(action,payload,element);
};
U.flows.training={name:'培训与考试记录',desc:'课程学习→创建尝试→答题→服务端结果（本地模拟）',ids:['D04','D39','D40','D41','D42'],seed:'SCHEDULED'};
U.flows.employeeCredential={name:'员工绑定凭证',desc:'本人创建请求→后台核实签发→本人消费→站点授权',ids:['U02','B36','U02','B26'],seed:'SCHEDULED'};
U.flows.penaltyAppeal={name:'接单限制与申诉',desc:'本人记录→详情→申诉→后台处理→本人查询',ids:['D43','D44','D45','B45','D44'],seed:'SCHEDULED'};
})(window.UI);

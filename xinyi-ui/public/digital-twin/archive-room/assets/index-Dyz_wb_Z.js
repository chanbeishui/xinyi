(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const l of document.querySelectorAll('link[rel="modulepreload"]'))s(l);new MutationObserver(l=>{for(const c of l)if(c.type==="childList")for(const f of c.addedNodes)f.tagName==="LINK"&&f.rel==="modulepreload"&&s(f)}).observe(document,{childList:!0,subtree:!0});function i(l){const c={};return l.integrity&&(c.integrity=l.integrity),l.referrerPolicy&&(c.referrerPolicy=l.referrerPolicy),l.crossOrigin==="use-credentials"?c.credentials="include":l.crossOrigin==="anonymous"?c.credentials="omit":c.credentials="same-origin",c}function s(l){if(l.ep)return;l.ep=!0;const c=i(l);fetch(l.href,c)}})();function aM(r){return r&&r.__esModule&&Object.prototype.hasOwnProperty.call(r,"default")?r.default:r}var Fh={exports:{}},$o={};/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var P_;function sM(){if(P_)return $o;P_=1;var r=Symbol.for("react.transitional.element"),t=Symbol.for("react.fragment");function i(s,l,c){var f=null;if(c!==void 0&&(f=""+c),l.key!==void 0&&(f=""+l.key),"key"in l){c={};for(var p in l)p!=="key"&&(c[p]=l[p])}else c=l;return l=c.ref,{$$typeof:r,type:s,key:f,ref:l!==void 0?l:null,props:c}}return $o.Fragment=t,$o.jsx=i,$o.jsxs=i,$o}var z_;function rM(){return z_||(z_=1,Fh.exports=sM()),Fh.exports}var N=rM(),Hh={exports:{}},ce={};/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var B_;function oM(){if(B_)return ce;B_=1;var r=Symbol.for("react.transitional.element"),t=Symbol.for("react.portal"),i=Symbol.for("react.fragment"),s=Symbol.for("react.strict_mode"),l=Symbol.for("react.profiler"),c=Symbol.for("react.consumer"),f=Symbol.for("react.context"),p=Symbol.for("react.forward_ref"),m=Symbol.for("react.suspense"),d=Symbol.for("react.memo"),x=Symbol.for("react.lazy"),v=Symbol.for("react.activity"),g=Symbol.iterator;function S(L){return L===null||typeof L!="object"?null:(L=g&&L[g]||L["@@iterator"],typeof L=="function"?L:null)}var b={isMounted:function(){return!1},enqueueForceUpdate:function(){},enqueueReplaceState:function(){},enqueueSetState:function(){}},w=Object.assign,E={};function _(L,W,yt){this.props=L,this.context=W,this.refs=E,this.updater=yt||b}_.prototype.isReactComponent={},_.prototype.setState=function(L,W){if(typeof L!="object"&&typeof L!="function"&&L!=null)throw Error("takes an object of state variables to update or a function which returns an object of state variables.");this.updater.enqueueSetState(this,L,W,"setState")},_.prototype.forceUpdate=function(L){this.updater.enqueueForceUpdate(this,L,"forceUpdate")};function z(){}z.prototype=_.prototype;function F(L,W,yt){this.props=L,this.context=W,this.refs=E,this.updater=yt||b}var C=F.prototype=new z;C.constructor=F,w(C,_.prototype),C.isPureReactComponent=!0;var B=Array.isArray;function O(){}var U={H:null,A:null,T:null,S:null},T=Object.prototype.hasOwnProperty;function P(L,W,yt){var At=yt.ref;return{$$typeof:r,type:L,key:W,ref:At!==void 0?At:null,props:yt}}function $(L,W){return P(L.type,W,L.props)}function Y(L){return typeof L=="object"&&L!==null&&L.$$typeof===r}function J(L){var W={"=":"=0",":":"=2"};return"$"+L.replace(/[=:]/g,function(yt){return W[yt]})}var dt=/\/+/g;function gt(L,W){return typeof L=="object"&&L!==null&&L.key!=null?J(""+L.key):W.toString(36)}function et(L){switch(L.status){case"fulfilled":return L.value;case"rejected":throw L.reason;default:switch(typeof L.status=="string"?L.then(O,O):(L.status="pending",L.then(function(W){L.status==="pending"&&(L.status="fulfilled",L.value=W)},function(W){L.status==="pending"&&(L.status="rejected",L.reason=W)})),L.status){case"fulfilled":return L.value;case"rejected":throw L.reason}}throw L}function I(L,W,yt,At,wt){var nt=typeof L;(nt==="undefined"||nt==="boolean")&&(L=null);var vt=!1;if(L===null)vt=!0;else switch(nt){case"bigint":case"string":case"number":vt=!0;break;case"object":switch(L.$$typeof){case r:case t:vt=!0;break;case x:return vt=L._init,I(vt(L._payload),W,yt,At,wt)}}if(vt)return wt=wt(L),vt=At===""?"."+gt(L,0):At,B(wt)?(yt="",vt!=null&&(yt=vt.replace(dt,"$&/")+"/"),I(wt,W,yt,"",function(Jt){return Jt})):wt!=null&&(Y(wt)&&(wt=$(wt,yt+(wt.key==null||L&&L.key===wt.key?"":(""+wt.key).replace(dt,"$&/")+"/")+vt)),W.push(wt)),1;vt=0;var St=At===""?".":At+":";if(B(L))for(var Ut=0;Ut<L.length;Ut++)At=L[Ut],nt=St+gt(At,Ut),vt+=I(At,W,yt,nt,wt);else if(Ut=S(L),typeof Ut=="function")for(L=Ut.call(L),Ut=0;!(At=L.next()).done;)At=At.value,nt=St+gt(At,Ut++),vt+=I(At,W,yt,nt,wt);else if(nt==="object"){if(typeof L.then=="function")return I(et(L),W,yt,At,wt);throw W=String(L),Error("Objects are not valid as a React child (found: "+(W==="[object Object]"?"object with keys {"+Object.keys(L).join(", ")+"}":W)+"). If you meant to render a collection of children, use an array instead.")}return vt}function H(L,W,yt){if(L==null)return L;var At=[],wt=0;return I(L,At,"","",function(nt){return W.call(yt,nt,wt++)}),At}function q(L){if(L._status===-1){var W=L._result;W=W(),W.then(function(yt){(L._status===0||L._status===-1)&&(L._status=1,L._result=yt)},function(yt){(L._status===0||L._status===-1)&&(L._status=2,L._result=yt)}),L._status===-1&&(L._status=0,L._result=W)}if(L._status===1)return L._result.default;throw L._result}var ht=typeof reportError=="function"?reportError:function(L){if(typeof window=="object"&&typeof window.ErrorEvent=="function"){var W=new window.ErrorEvent("error",{bubbles:!0,cancelable:!0,message:typeof L=="object"&&L!==null&&typeof L.message=="string"?String(L.message):String(L),error:L});if(!window.dispatchEvent(W))return}else if(typeof process=="object"&&typeof process.emit=="function"){process.emit("uncaughtException",L);return}console.error(L)},Et={map:H,forEach:function(L,W,yt){H(L,function(){W.apply(this,arguments)},yt)},count:function(L){var W=0;return H(L,function(){W++}),W},toArray:function(L){return H(L,function(W){return W})||[]},only:function(L){if(!Y(L))throw Error("React.Children.only expected to receive a single React element child.");return L}};return ce.Activity=v,ce.Children=Et,ce.Component=_,ce.Fragment=i,ce.Profiler=l,ce.PureComponent=F,ce.StrictMode=s,ce.Suspense=m,ce.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE=U,ce.__COMPILER_RUNTIME={__proto__:null,c:function(L){return U.H.useMemoCache(L)}},ce.cache=function(L){return function(){return L.apply(null,arguments)}},ce.cacheSignal=function(){return null},ce.cloneElement=function(L,W,yt){if(L==null)throw Error("The argument must be a React element, but you passed "+L+".");var At=w({},L.props),wt=L.key;if(W!=null)for(nt in W.key!==void 0&&(wt=""+W.key),W)!T.call(W,nt)||nt==="key"||nt==="__self"||nt==="__source"||nt==="ref"&&W.ref===void 0||(At[nt]=W[nt]);var nt=arguments.length-2;if(nt===1)At.children=yt;else if(1<nt){for(var vt=Array(nt),St=0;St<nt;St++)vt[St]=arguments[St+2];At.children=vt}return P(L.type,wt,At)},ce.createContext=function(L){return L={$$typeof:f,_currentValue:L,_currentValue2:L,_threadCount:0,Provider:null,Consumer:null},L.Provider=L,L.Consumer={$$typeof:c,_context:L},L},ce.createElement=function(L,W,yt){var At,wt={},nt=null;if(W!=null)for(At in W.key!==void 0&&(nt=""+W.key),W)T.call(W,At)&&At!=="key"&&At!=="__self"&&At!=="__source"&&(wt[At]=W[At]);var vt=arguments.length-2;if(vt===1)wt.children=yt;else if(1<vt){for(var St=Array(vt),Ut=0;Ut<vt;Ut++)St[Ut]=arguments[Ut+2];wt.children=St}if(L&&L.defaultProps)for(At in vt=L.defaultProps,vt)wt[At]===void 0&&(wt[At]=vt[At]);return P(L,nt,wt)},ce.createRef=function(){return{current:null}},ce.forwardRef=function(L){return{$$typeof:p,render:L}},ce.isValidElement=Y,ce.lazy=function(L){return{$$typeof:x,_payload:{_status:-1,_result:L},_init:q}},ce.memo=function(L,W){return{$$typeof:d,type:L,compare:W===void 0?null:W}},ce.startTransition=function(L){var W=U.T,yt={};U.T=yt;try{var At=L(),wt=U.S;wt!==null&&wt(yt,At),typeof At=="object"&&At!==null&&typeof At.then=="function"&&At.then(O,ht)}catch(nt){ht(nt)}finally{W!==null&&yt.types!==null&&(W.types=yt.types),U.T=W}},ce.unstable_useCacheRefresh=function(){return U.H.useCacheRefresh()},ce.use=function(L){return U.H.use(L)},ce.useActionState=function(L,W,yt){return U.H.useActionState(L,W,yt)},ce.useCallback=function(L,W){return U.H.useCallback(L,W)},ce.useContext=function(L){return U.H.useContext(L)},ce.useDebugValue=function(){},ce.useDeferredValue=function(L,W){return U.H.useDeferredValue(L,W)},ce.useEffect=function(L,W){return U.H.useEffect(L,W)},ce.useEffectEvent=function(L){return U.H.useEffectEvent(L)},ce.useId=function(){return U.H.useId()},ce.useImperativeHandle=function(L,W,yt){return U.H.useImperativeHandle(L,W,yt)},ce.useInsertionEffect=function(L,W){return U.H.useInsertionEffect(L,W)},ce.useLayoutEffect=function(L,W){return U.H.useLayoutEffect(L,W)},ce.useMemo=function(L,W){return U.H.useMemo(L,W)},ce.useOptimistic=function(L,W){return U.H.useOptimistic(L,W)},ce.useReducer=function(L,W,yt){return U.H.useReducer(L,W,yt)},ce.useRef=function(L){return U.H.useRef(L)},ce.useState=function(L){return U.H.useState(L)},ce.useSyncExternalStore=function(L,W,yt){return U.H.useSyncExternalStore(L,W,yt)},ce.useTransition=function(){return U.H.useTransition()},ce.version="19.2.0",ce}var I_;function Rp(){return I_||(I_=1,Hh.exports=oM()),Hh.exports}var Yt=Rp();const lM=aM(Yt);var Gh={exports:{}},tl={},Vh={exports:{}},kh={};/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var F_;function cM(){return F_||(F_=1,(function(r){function t(I,H){var q=I.length;I.push(H);t:for(;0<q;){var ht=q-1>>>1,Et=I[ht];if(0<l(Et,H))I[ht]=H,I[q]=Et,q=ht;else break t}}function i(I){return I.length===0?null:I[0]}function s(I){if(I.length===0)return null;var H=I[0],q=I.pop();if(q!==H){I[0]=q;t:for(var ht=0,Et=I.length,L=Et>>>1;ht<L;){var W=2*(ht+1)-1,yt=I[W],At=W+1,wt=I[At];if(0>l(yt,q))At<Et&&0>l(wt,yt)?(I[ht]=wt,I[At]=q,ht=At):(I[ht]=yt,I[W]=q,ht=W);else if(At<Et&&0>l(wt,q))I[ht]=wt,I[At]=q,ht=At;else break t}}return H}function l(I,H){var q=I.sortIndex-H.sortIndex;return q!==0?q:I.id-H.id}if(r.unstable_now=void 0,typeof performance=="object"&&typeof performance.now=="function"){var c=performance;r.unstable_now=function(){return c.now()}}else{var f=Date,p=f.now();r.unstable_now=function(){return f.now()-p}}var m=[],d=[],x=1,v=null,g=3,S=!1,b=!1,w=!1,E=!1,_=typeof setTimeout=="function"?setTimeout:null,z=typeof clearTimeout=="function"?clearTimeout:null,F=typeof setImmediate<"u"?setImmediate:null;function C(I){for(var H=i(d);H!==null;){if(H.callback===null)s(d);else if(H.startTime<=I)s(d),H.sortIndex=H.expirationTime,t(m,H);else break;H=i(d)}}function B(I){if(w=!1,C(I),!b)if(i(m)!==null)b=!0,O||(O=!0,J());else{var H=i(d);H!==null&&et(B,H.startTime-I)}}var O=!1,U=-1,T=5,P=-1;function $(){return E?!0:!(r.unstable_now()-P<T)}function Y(){if(E=!1,O){var I=r.unstable_now();P=I;var H=!0;try{t:{b=!1,w&&(w=!1,z(U),U=-1),S=!0;var q=g;try{e:{for(C(I),v=i(m);v!==null&&!(v.expirationTime>I&&$());){var ht=v.callback;if(typeof ht=="function"){v.callback=null,g=v.priorityLevel;var Et=ht(v.expirationTime<=I);if(I=r.unstable_now(),typeof Et=="function"){v.callback=Et,C(I),H=!0;break e}v===i(m)&&s(m),C(I)}else s(m);v=i(m)}if(v!==null)H=!0;else{var L=i(d);L!==null&&et(B,L.startTime-I),H=!1}}break t}finally{v=null,g=q,S=!1}H=void 0}}finally{H?J():O=!1}}}var J;if(typeof F=="function")J=function(){F(Y)};else if(typeof MessageChannel<"u"){var dt=new MessageChannel,gt=dt.port2;dt.port1.onmessage=Y,J=function(){gt.postMessage(null)}}else J=function(){_(Y,0)};function et(I,H){U=_(function(){I(r.unstable_now())},H)}r.unstable_IdlePriority=5,r.unstable_ImmediatePriority=1,r.unstable_LowPriority=4,r.unstable_NormalPriority=3,r.unstable_Profiling=null,r.unstable_UserBlockingPriority=2,r.unstable_cancelCallback=function(I){I.callback=null},r.unstable_forceFrameRate=function(I){0>I||125<I?console.error("forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"):T=0<I?Math.floor(1e3/I):5},r.unstable_getCurrentPriorityLevel=function(){return g},r.unstable_next=function(I){switch(g){case 1:case 2:case 3:var H=3;break;default:H=g}var q=g;g=H;try{return I()}finally{g=q}},r.unstable_requestPaint=function(){E=!0},r.unstable_runWithPriority=function(I,H){switch(I){case 1:case 2:case 3:case 4:case 5:break;default:I=3}var q=g;g=I;try{return H()}finally{g=q}},r.unstable_scheduleCallback=function(I,H,q){var ht=r.unstable_now();switch(typeof q=="object"&&q!==null?(q=q.delay,q=typeof q=="number"&&0<q?ht+q:ht):q=ht,I){case 1:var Et=-1;break;case 2:Et=250;break;case 5:Et=1073741823;break;case 4:Et=1e4;break;default:Et=5e3}return Et=q+Et,I={id:x++,callback:H,priorityLevel:I,startTime:q,expirationTime:Et,sortIndex:-1},q>ht?(I.sortIndex=q,t(d,I),i(m)===null&&I===i(d)&&(w?(z(U),U=-1):w=!0,et(B,q-ht))):(I.sortIndex=Et,t(m,I),b||S||(b=!0,O||(O=!0,J()))),I},r.unstable_shouldYield=$,r.unstable_wrapCallback=function(I){var H=g;return function(){var q=g;g=H;try{return I.apply(this,arguments)}finally{g=q}}}})(kh)),kh}var H_;function uM(){return H_||(H_=1,Vh.exports=cM()),Vh.exports}var Xh={exports:{}},In={};/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var G_;function fM(){if(G_)return In;G_=1;var r=Rp();function t(m){var d="https://react.dev/errors/"+m;if(1<arguments.length){d+="?args[]="+encodeURIComponent(arguments[1]);for(var x=2;x<arguments.length;x++)d+="&args[]="+encodeURIComponent(arguments[x])}return"Minified React error #"+m+"; visit "+d+" for the full message or use the non-minified dev environment for full errors and additional helpful warnings."}function i(){}var s={d:{f:i,r:function(){throw Error(t(522))},D:i,C:i,L:i,m:i,X:i,S:i,M:i},p:0,findDOMNode:null},l=Symbol.for("react.portal");function c(m,d,x){var v=3<arguments.length&&arguments[3]!==void 0?arguments[3]:null;return{$$typeof:l,key:v==null?null:""+v,children:m,containerInfo:d,implementation:x}}var f=r.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;function p(m,d){if(m==="font")return"";if(typeof d=="string")return d==="use-credentials"?d:""}return In.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE=s,In.createPortal=function(m,d){var x=2<arguments.length&&arguments[2]!==void 0?arguments[2]:null;if(!d||d.nodeType!==1&&d.nodeType!==9&&d.nodeType!==11)throw Error(t(299));return c(m,d,null,x)},In.flushSync=function(m){var d=f.T,x=s.p;try{if(f.T=null,s.p=2,m)return m()}finally{f.T=d,s.p=x,s.d.f()}},In.preconnect=function(m,d){typeof m=="string"&&(d?(d=d.crossOrigin,d=typeof d=="string"?d==="use-credentials"?d:"":void 0):d=null,s.d.C(m,d))},In.prefetchDNS=function(m){typeof m=="string"&&s.d.D(m)},In.preinit=function(m,d){if(typeof m=="string"&&d&&typeof d.as=="string"){var x=d.as,v=p(x,d.crossOrigin),g=typeof d.integrity=="string"?d.integrity:void 0,S=typeof d.fetchPriority=="string"?d.fetchPriority:void 0;x==="style"?s.d.S(m,typeof d.precedence=="string"?d.precedence:void 0,{crossOrigin:v,integrity:g,fetchPriority:S}):x==="script"&&s.d.X(m,{crossOrigin:v,integrity:g,fetchPriority:S,nonce:typeof d.nonce=="string"?d.nonce:void 0})}},In.preinitModule=function(m,d){if(typeof m=="string")if(typeof d=="object"&&d!==null){if(d.as==null||d.as==="script"){var x=p(d.as,d.crossOrigin);s.d.M(m,{crossOrigin:x,integrity:typeof d.integrity=="string"?d.integrity:void 0,nonce:typeof d.nonce=="string"?d.nonce:void 0})}}else d==null&&s.d.M(m)},In.preload=function(m,d){if(typeof m=="string"&&typeof d=="object"&&d!==null&&typeof d.as=="string"){var x=d.as,v=p(x,d.crossOrigin);s.d.L(m,x,{crossOrigin:v,integrity:typeof d.integrity=="string"?d.integrity:void 0,nonce:typeof d.nonce=="string"?d.nonce:void 0,type:typeof d.type=="string"?d.type:void 0,fetchPriority:typeof d.fetchPriority=="string"?d.fetchPriority:void 0,referrerPolicy:typeof d.referrerPolicy=="string"?d.referrerPolicy:void 0,imageSrcSet:typeof d.imageSrcSet=="string"?d.imageSrcSet:void 0,imageSizes:typeof d.imageSizes=="string"?d.imageSizes:void 0,media:typeof d.media=="string"?d.media:void 0})}},In.preloadModule=function(m,d){if(typeof m=="string")if(d){var x=p(d.as,d.crossOrigin);s.d.m(m,{as:typeof d.as=="string"&&d.as!=="script"?d.as:void 0,crossOrigin:x,integrity:typeof d.integrity=="string"?d.integrity:void 0})}else s.d.m(m)},In.requestFormReset=function(m){s.d.r(m)},In.unstable_batchedUpdates=function(m,d){return m(d)},In.useFormState=function(m,d,x){return f.H.useFormState(m,d,x)},In.useFormStatus=function(){return f.H.useHostTransitionStatus()},In.version="19.2.0",In}var V_;function hM(){if(V_)return Xh.exports;V_=1;function r(){if(!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__>"u"||typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE!="function"))try{__REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(r)}catch(t){console.error(t)}}return r(),Xh.exports=fM(),Xh.exports}/**
 * @license React
 * react-dom-client.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var k_;function dM(){if(k_)return tl;k_=1;var r=uM(),t=Rp(),i=hM();function s(e){var n="https://react.dev/errors/"+e;if(1<arguments.length){n+="?args[]="+encodeURIComponent(arguments[1]);for(var a=2;a<arguments.length;a++)n+="&args[]="+encodeURIComponent(arguments[a])}return"Minified React error #"+e+"; visit "+n+" for the full message or use the non-minified dev environment for full errors and additional helpful warnings."}function l(e){return!(!e||e.nodeType!==1&&e.nodeType!==9&&e.nodeType!==11)}function c(e){var n=e,a=e;if(e.alternate)for(;n.return;)n=n.return;else{e=n;do n=e,(n.flags&4098)!==0&&(a=n.return),e=n.return;while(e)}return n.tag===3?a:null}function f(e){if(e.tag===13){var n=e.memoizedState;if(n===null&&(e=e.alternate,e!==null&&(n=e.memoizedState)),n!==null)return n.dehydrated}return null}function p(e){if(e.tag===31){var n=e.memoizedState;if(n===null&&(e=e.alternate,e!==null&&(n=e.memoizedState)),n!==null)return n.dehydrated}return null}function m(e){if(c(e)!==e)throw Error(s(188))}function d(e){var n=e.alternate;if(!n){if(n=c(e),n===null)throw Error(s(188));return n!==e?null:e}for(var a=e,o=n;;){var u=a.return;if(u===null)break;var h=u.alternate;if(h===null){if(o=u.return,o!==null){a=o;continue}break}if(u.child===h.child){for(h=u.child;h;){if(h===a)return m(u),e;if(h===o)return m(u),n;h=h.sibling}throw Error(s(188))}if(a.return!==o.return)a=u,o=h;else{for(var y=!1,R=u.child;R;){if(R===a){y=!0,a=u,o=h;break}if(R===o){y=!0,o=u,a=h;break}R=R.sibling}if(!y){for(R=h.child;R;){if(R===a){y=!0,a=h,o=u;break}if(R===o){y=!0,o=h,a=u;break}R=R.sibling}if(!y)throw Error(s(189))}}if(a.alternate!==o)throw Error(s(190))}if(a.tag!==3)throw Error(s(188));return a.stateNode.current===a?e:n}function x(e){var n=e.tag;if(n===5||n===26||n===27||n===6)return e;for(e=e.child;e!==null;){if(n=x(e),n!==null)return n;e=e.sibling}return null}var v=Object.assign,g=Symbol.for("react.element"),S=Symbol.for("react.transitional.element"),b=Symbol.for("react.portal"),w=Symbol.for("react.fragment"),E=Symbol.for("react.strict_mode"),_=Symbol.for("react.profiler"),z=Symbol.for("react.consumer"),F=Symbol.for("react.context"),C=Symbol.for("react.forward_ref"),B=Symbol.for("react.suspense"),O=Symbol.for("react.suspense_list"),U=Symbol.for("react.memo"),T=Symbol.for("react.lazy"),P=Symbol.for("react.activity"),$=Symbol.for("react.memo_cache_sentinel"),Y=Symbol.iterator;function J(e){return e===null||typeof e!="object"?null:(e=Y&&e[Y]||e["@@iterator"],typeof e=="function"?e:null)}var dt=Symbol.for("react.client.reference");function gt(e){if(e==null)return null;if(typeof e=="function")return e.$$typeof===dt?null:e.displayName||e.name||null;if(typeof e=="string")return e;switch(e){case w:return"Fragment";case _:return"Profiler";case E:return"StrictMode";case B:return"Suspense";case O:return"SuspenseList";case P:return"Activity"}if(typeof e=="object")switch(e.$$typeof){case b:return"Portal";case F:return e.displayName||"Context";case z:return(e._context.displayName||"Context")+".Consumer";case C:var n=e.render;return e=e.displayName,e||(e=n.displayName||n.name||"",e=e!==""?"ForwardRef("+e+")":"ForwardRef"),e;case U:return n=e.displayName||null,n!==null?n:gt(e.type)||"Memo";case T:n=e._payload,e=e._init;try{return gt(e(n))}catch{}}return null}var et=Array.isArray,I=t.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,H=i.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,q={pending:!1,data:null,method:null,action:null},ht=[],Et=-1;function L(e){return{current:e}}function W(e){0>Et||(e.current=ht[Et],ht[Et]=null,Et--)}function yt(e,n){Et++,ht[Et]=e.current,e.current=n}var At=L(null),wt=L(null),nt=L(null),vt=L(null);function St(e,n){switch(yt(nt,n),yt(wt,e),yt(At,null),n.nodeType){case 9:case 11:e=(e=n.documentElement)&&(e=e.namespaceURI)?a_(e):0;break;default:if(e=n.tagName,n=n.namespaceURI)n=a_(n),e=s_(n,e);else switch(e){case"svg":e=1;break;case"math":e=2;break;default:e=0}}W(At),yt(At,e)}function Ut(){W(At),W(wt),W(nt)}function Jt(e){e.memoizedState!==null&&yt(vt,e);var n=At.current,a=s_(n,e.type);n!==a&&(yt(wt,e),yt(At,a))}function Kt(e){wt.current===e&&(W(At),W(wt)),vt.current===e&&(W(vt),Zo._currentValue=q)}var Ce,le;function _e(e){if(Ce===void 0)try{throw Error()}catch(a){var n=a.stack.trim().match(/\n( *(at )?)/);Ce=n&&n[1]||"",le=-1<a.stack.indexOf(`
    at`)?" (<anonymous>)":-1<a.stack.indexOf("@")?"@unknown:0:0":""}return`
`+Ce+e+le}var ve=!1;function de(e,n){if(!e||ve)return"";ve=!0;var a=Error.prepareStackTrace;Error.prepareStackTrace=void 0;try{var o={DetermineComponentFrameRoot:function(){try{if(n){var Mt=function(){throw Error()};if(Object.defineProperty(Mt.prototype,"props",{set:function(){throw Error()}}),typeof Reflect=="object"&&Reflect.construct){try{Reflect.construct(Mt,[])}catch(ft){var lt=ft}Reflect.construct(e,[],Mt)}else{try{Mt.call()}catch(ft){lt=ft}e.call(Mt.prototype)}}else{try{throw Error()}catch(ft){lt=ft}(Mt=e())&&typeof Mt.catch=="function"&&Mt.catch(function(){})}}catch(ft){if(ft&&lt&&typeof ft.stack=="string")return[ft.stack,lt.stack]}return[null,null]}};o.DetermineComponentFrameRoot.displayName="DetermineComponentFrameRoot";var u=Object.getOwnPropertyDescriptor(o.DetermineComponentFrameRoot,"name");u&&u.configurable&&Object.defineProperty(o.DetermineComponentFrameRoot,"name",{value:"DetermineComponentFrameRoot"});var h=o.DetermineComponentFrameRoot(),y=h[0],R=h[1];if(y&&R){var G=y.split(`
`),at=R.split(`
`);for(u=o=0;o<G.length&&!G[o].includes("DetermineComponentFrameRoot");)o++;for(;u<at.length&&!at[u].includes("DetermineComponentFrameRoot");)u++;if(o===G.length||u===at.length)for(o=G.length-1,u=at.length-1;1<=o&&0<=u&&G[o]!==at[u];)u--;for(;1<=o&&0<=u;o--,u--)if(G[o]!==at[u]){if(o!==1||u!==1)do if(o--,u--,0>u||G[o]!==at[u]){var mt=`
`+G[o].replace(" at new "," at ");return e.displayName&&mt.includes("<anonymous>")&&(mt=mt.replace("<anonymous>",e.displayName)),mt}while(1<=o&&0<=u);break}}}finally{ve=!1,Error.prepareStackTrace=a}return(a=e?e.displayName||e.name:"")?_e(a):""}function Ke(e,n){switch(e.tag){case 26:case 27:case 5:return _e(e.type);case 16:return _e("Lazy");case 13:return e.child!==n&&n!==null?_e("Suspense Fallback"):_e("Suspense");case 19:return _e("SuspenseList");case 0:case 15:return de(e.type,!1);case 11:return de(e.type.render,!1);case 1:return de(e.type,!0);case 31:return _e("Activity");default:return""}}function Qe(e){try{var n="",a=null;do n+=Ke(e,a),a=e,e=e.return;while(e);return n}catch(o){return`
Error generating stack: `+o.message+`
`+o.stack}}var Je=Object.prototype.hasOwnProperty,nn=r.unstable_scheduleCallback,ze=r.unstable_cancelCallback,$e=r.unstable_shouldYield,K=r.unstable_requestPaint,Le=r.unstable_now,Lt=r.unstable_getCurrentPriorityLevel,D=r.unstable_ImmediatePriority,M=r.unstable_UserBlockingPriority,X=r.unstable_NormalPriority,V=r.unstable_LowPriority,ct=r.unstable_IdlePriority,bt=r.log,Ct=r.unstable_setDisableYieldValue,ut=null,pt=null;function Rt(e){if(typeof bt=="function"&&Ct(e),pt&&typeof pt.setStrictMode=="function")try{pt.setStrictMode(ut,e)}catch{}}var Ft=Math.clz32?Math.clz32:$t,Ot=Math.log,Pt=Math.LN2;function $t(e){return e>>>=0,e===0?32:31-(Ot(e)/Pt|0)|0}var te=256,re=262144,Z=4194304;function Dt(e){var n=e&42;if(n!==0)return n;switch(e&-e){case 1:return 1;case 2:return 2;case 4:return 4;case 8:return 8;case 16:return 16;case 32:return 32;case 64:return 64;case 128:return 128;case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:return e&261888;case 262144:case 524288:case 1048576:case 2097152:return e&3932160;case 4194304:case 8388608:case 16777216:case 33554432:return e&62914560;case 67108864:return 67108864;case 134217728:return 134217728;case 268435456:return 268435456;case 536870912:return 536870912;case 1073741824:return 0;default:return e}}function _t(e,n,a){var o=e.pendingLanes;if(o===0)return 0;var u=0,h=e.suspendedLanes,y=e.pingedLanes;e=e.warmLanes;var R=o&134217727;return R!==0?(o=R&~h,o!==0?u=Dt(o):(y&=R,y!==0?u=Dt(y):a||(a=R&~e,a!==0&&(u=Dt(a))))):(R=o&~h,R!==0?u=Dt(R):y!==0?u=Dt(y):a||(a=o&~e,a!==0&&(u=Dt(a)))),u===0?0:n!==0&&n!==u&&(n&h)===0&&(h=u&-u,a=n&-n,h>=a||h===32&&(a&4194048)!==0)?n:u}function Nt(e,n){return(e.pendingLanes&~(e.suspendedLanes&~e.pingedLanes)&n)===0}function Ht(e,n){switch(e){case 1:case 2:case 4:case 8:case 64:return n+250;case 16:case 32:case 128:case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:case 262144:case 524288:case 1048576:case 2097152:return n+5e3;case 4194304:case 8388608:case 16777216:case 33554432:return-1;case 67108864:case 134217728:case 268435456:case 536870912:case 1073741824:return-1;default:return-1}}function Tt(){var e=Z;return Z<<=1,(Z&62914560)===0&&(Z=4194304),e}function jt(e){for(var n=[],a=0;31>a;a++)n.push(e);return n}function kt(e,n){e.pendingLanes|=n,n!==268435456&&(e.suspendedLanes=0,e.pingedLanes=0,e.warmLanes=0)}function an(e,n,a,o,u,h){var y=e.pendingLanes;e.pendingLanes=a,e.suspendedLanes=0,e.pingedLanes=0,e.warmLanes=0,e.expiredLanes&=a,e.entangledLanes&=a,e.errorRecoveryDisabledLanes&=a,e.shellSuspendCounter=0;var R=e.entanglements,G=e.expirationTimes,at=e.hiddenUpdates;for(a=y&~a;0<a;){var mt=31-Ft(a),Mt=1<<mt;R[mt]=0,G[mt]=-1;var lt=at[mt];if(lt!==null)for(at[mt]=null,mt=0;mt<lt.length;mt++){var ft=lt[mt];ft!==null&&(ft.lane&=-536870913)}a&=~Mt}o!==0&&Be(e,o,0),h!==0&&u===0&&e.tag!==0&&(e.suspendedLanes|=h&~(y&~n))}function Be(e,n,a){e.pendingLanes|=n,e.suspendedLanes&=~n;var o=31-Ft(n);e.entangledLanes|=n,e.entanglements[o]=e.entanglements[o]|1073741824|a&261930}function ei(e,n){var a=e.entangledLanes|=n;for(e=e.entanglements;a;){var o=31-Ft(a),u=1<<o;u&n|e[o]&n&&(e[o]|=n),a&=~u}}function ni(e,n){var a=n&-n;return a=(a&42)!==0?1:lo(a),(a&(e.suspendedLanes|n))!==0?0:a}function lo(e){switch(e){case 2:e=1;break;case 8:e=4;break;case 32:e=16;break;case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:case 262144:case 524288:case 1048576:case 2097152:case 4194304:case 8388608:case 16777216:case 33554432:e=128;break;case 268435456:e=134217728;break;default:e=0}return e}function co(e){return e&=-e,2<e?8<e?(e&134217727)!==0?32:268435456:8:2}function uo(){var e=H.p;return e!==0?e:(e=window.event,e===void 0?32:w_(e.type))}function $s(e,n){var a=H.p;try{return H.p=e,n()}finally{H.p=a}}var Hi=Math.random().toString(36).slice(2),mn="__reactFiber$"+Hi,Un="__reactProps$"+Hi,Yn="__reactContainer$"+Hi,_s="__reactEvents$"+Hi,El="__reactListeners$"+Hi,bl="__reactHandles$"+Hi,vs="__reactResources$"+Hi,Ua="__reactMarker$"+Hi;function Na(e){delete e[mn],delete e[Un],delete e[_s],delete e[El],delete e[bl]}function ta(e){var n=e[mn];if(n)return n;for(var a=e.parentNode;a;){if(n=a[Yn]||a[mn]){if(a=n.alternate,n.child!==null||a!==null&&a.child!==null)for(e=h_(e);e!==null;){if(a=e[mn])return a;e=h_(e)}return n}e=a,a=e.parentNode}return null}function ea(e){if(e=e[mn]||e[Yn]){var n=e.tag;if(n===5||n===6||n===13||n===31||n===26||n===27||n===3)return e}return null}function xs(e){var n=e.tag;if(n===5||n===26||n===27||n===6)return e.stateNode;throw Error(s(33))}function La(e){var n=e[vs];return n||(n=e[vs]={hoistableStyles:new Map,hoistableScripts:new Map}),n}function gn(e){e[Ua]=!0}var Tl=new Set,A={};function Q(e,n){ot(e,n),ot(e+"Capture",n)}function ot(e,n){for(A[e]=n,e=0;e<n.length;e++)Tl.add(n[e])}var st=RegExp("^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"),rt={},Bt={};function Vt(e){return Je.call(Bt,e)?!0:Je.call(rt,e)?!1:st.test(e)?Bt[e]=!0:(rt[e]=!0,!1)}function zt(e,n,a){if(Vt(n))if(a===null)e.removeAttribute(n);else{switch(typeof a){case"undefined":case"function":case"symbol":e.removeAttribute(n);return;case"boolean":var o=n.toLowerCase().slice(0,5);if(o!=="data-"&&o!=="aria-"){e.removeAttribute(n);return}}e.setAttribute(n,""+a)}}function Wt(e,n,a){if(a===null)e.removeAttribute(n);else{switch(typeof a){case"undefined":case"function":case"symbol":case"boolean":e.removeAttribute(n);return}e.setAttribute(n,""+a)}}function Xt(e,n,a,o){if(o===null)e.removeAttribute(a);else{switch(typeof o){case"undefined":case"function":case"symbol":case"boolean":e.removeAttribute(a);return}e.setAttributeNS(n,a,""+o)}}function ee(e){switch(typeof e){case"bigint":case"boolean":case"number":case"string":case"undefined":return e;case"object":return e;default:return""}}function fe(e){var n=e.type;return(e=e.nodeName)&&e.toLowerCase()==="input"&&(n==="checkbox"||n==="radio")}function Qt(e,n,a){var o=Object.getOwnPropertyDescriptor(e.constructor.prototype,n);if(!e.hasOwnProperty(n)&&typeof o<"u"&&typeof o.get=="function"&&typeof o.set=="function"){var u=o.get,h=o.set;return Object.defineProperty(e,n,{configurable:!0,get:function(){return u.call(this)},set:function(y){a=""+y,h.call(this,y)}}),Object.defineProperty(e,n,{enumerable:o.enumerable}),{getValue:function(){return a},setValue:function(y){a=""+y},stopTracking:function(){e._valueTracker=null,delete e[n]}}}}function De(e){if(!e._valueTracker){var n=fe(e)?"checked":"value";e._valueTracker=Qt(e,n,""+e[n])}}function sn(e){if(!e)return!1;var n=e._valueTracker;if(!n)return!0;var a=n.getValue(),o="";return e&&(o=fe(e)?e.checked?"true":"false":e.value),e=o,e!==a?(n.setValue(e),!0):!1}function je(e){if(e=e||(typeof document<"u"?document:void 0),typeof e>"u")return null;try{return e.activeElement||e.body}catch{return e.body}}var Ie=/[\n"\\]/g;function Fe(e){return e.replace(Ie,function(n){return"\\"+n.charCodeAt(0).toString(16)+" "})}function Gt(e,n,a,o,u,h,y,R){e.name="",y!=null&&typeof y!="function"&&typeof y!="symbol"&&typeof y!="boolean"?e.type=y:e.removeAttribute("type"),n!=null?y==="number"?(n===0&&e.value===""||e.value!=n)&&(e.value=""+ee(n)):e.value!==""+ee(n)&&(e.value=""+ee(n)):y!=="submit"&&y!=="reset"||e.removeAttribute("value"),n!=null?xe(e,y,ee(n)):a!=null?xe(e,y,ee(a)):o!=null&&e.removeAttribute("value"),u==null&&h!=null&&(e.defaultChecked=!!h),u!=null&&(e.checked=u&&typeof u!="function"&&typeof u!="symbol"),R!=null&&typeof R!="function"&&typeof R!="symbol"&&typeof R!="boolean"?e.name=""+ee(R):e.removeAttribute("name")}function Bn(e,n,a,o,u,h,y,R){if(h!=null&&typeof h!="function"&&typeof h!="symbol"&&typeof h!="boolean"&&(e.type=h),n!=null||a!=null){if(!(h!=="submit"&&h!=="reset"||n!=null)){De(e);return}a=a!=null?""+ee(a):"",n=n!=null?""+ee(n):a,R||n===e.value||(e.value=n),e.defaultValue=n}o=o??u,o=typeof o!="function"&&typeof o!="symbol"&&!!o,e.checked=R?e.checked:!!o,e.defaultChecked=!!o,y!=null&&typeof y!="function"&&typeof y!="symbol"&&typeof y!="boolean"&&(e.name=y),De(e)}function xe(e,n,a){n==="number"&&je(e.ownerDocument)===e||e.defaultValue===""+a||(e.defaultValue=""+a)}function bn(e,n,a,o){if(e=e.options,n){n={};for(var u=0;u<a.length;u++)n["$"+a[u]]=!0;for(a=0;a<e.length;a++)u=n.hasOwnProperty("$"+e[a].value),e[a].selected!==u&&(e[a].selected=u),u&&o&&(e[a].defaultSelected=!0)}else{for(a=""+ee(a),n=null,u=0;u<e.length;u++){if(e[u].value===a){e[u].selected=!0,o&&(e[u].defaultSelected=!0);return}n!==null||e[u].disabled||(n=e[u])}n!==null&&(n.selected=!0)}}function ii(e,n,a){if(n!=null&&(n=""+ee(n),n!==e.value&&(e.value=n),a==null)){e.defaultValue!==n&&(e.defaultValue=n);return}e.defaultValue=a!=null?""+ee(a):""}function Ci(e,n,a,o){if(n==null){if(o!=null){if(a!=null)throw Error(s(92));if(et(o)){if(1<o.length)throw Error(s(93));o=o[0]}a=o}a==null&&(a=""),n=a}a=ee(n),e.defaultValue=a,o=e.textContent,o===a&&o!==""&&o!==null&&(e.value=o),De(e)}function ai(e,n){if(n){var a=e.firstChild;if(a&&a===e.lastChild&&a.nodeType===3){a.nodeValue=n;return}}e.textContent=n}var He=new Set("animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(" "));function rn(e,n,a){var o=n.indexOf("--")===0;a==null||typeof a=="boolean"||a===""?o?e.setProperty(n,""):n==="float"?e.cssFloat="":e[n]="":o?e.setProperty(n,a):typeof a!="number"||a===0||He.has(n)?n==="float"?e.cssFloat=a:e[n]=(""+a).trim():e[n]=a+"px"}function Di(e,n,a){if(n!=null&&typeof n!="object")throw Error(s(62));if(e=e.style,a!=null){for(var o in a)!a.hasOwnProperty(o)||n!=null&&n.hasOwnProperty(o)||(o.indexOf("--")===0?e.setProperty(o,""):o==="float"?e.cssFloat="":e[o]="");for(var u in n)o=n[u],n.hasOwnProperty(u)&&a[u]!==o&&rn(e,u,o)}else for(var h in n)n.hasOwnProperty(h)&&rn(e,h,n[h])}function Pe(e){if(e.indexOf("-")===-1)return!1;switch(e){case"annotation-xml":case"color-profile":case"font-face":case"font-face-src":case"font-face-uri":case"font-face-format":case"font-face-name":case"missing-glyph":return!1;default:return!0}}var Gi=new Map([["acceptCharset","accept-charset"],["htmlFor","for"],["httpEquiv","http-equiv"],["crossOrigin","crossorigin"],["accentHeight","accent-height"],["alignmentBaseline","alignment-baseline"],["arabicForm","arabic-form"],["baselineShift","baseline-shift"],["capHeight","cap-height"],["clipPath","clip-path"],["clipRule","clip-rule"],["colorInterpolation","color-interpolation"],["colorInterpolationFilters","color-interpolation-filters"],["colorProfile","color-profile"],["colorRendering","color-rendering"],["dominantBaseline","dominant-baseline"],["enableBackground","enable-background"],["fillOpacity","fill-opacity"],["fillRule","fill-rule"],["floodColor","flood-color"],["floodOpacity","flood-opacity"],["fontFamily","font-family"],["fontSize","font-size"],["fontSizeAdjust","font-size-adjust"],["fontStretch","font-stretch"],["fontStyle","font-style"],["fontVariant","font-variant"],["fontWeight","font-weight"],["glyphName","glyph-name"],["glyphOrientationHorizontal","glyph-orientation-horizontal"],["glyphOrientationVertical","glyph-orientation-vertical"],["horizAdvX","horiz-adv-x"],["horizOriginX","horiz-origin-x"],["imageRendering","image-rendering"],["letterSpacing","letter-spacing"],["lightingColor","lighting-color"],["markerEnd","marker-end"],["markerMid","marker-mid"],["markerStart","marker-start"],["overlinePosition","overline-position"],["overlineThickness","overline-thickness"],["paintOrder","paint-order"],["panose-1","panose-1"],["pointerEvents","pointer-events"],["renderingIntent","rendering-intent"],["shapeRendering","shape-rendering"],["stopColor","stop-color"],["stopOpacity","stop-opacity"],["strikethroughPosition","strikethrough-position"],["strikethroughThickness","strikethrough-thickness"],["strokeDasharray","stroke-dasharray"],["strokeDashoffset","stroke-dashoffset"],["strokeLinecap","stroke-linecap"],["strokeLinejoin","stroke-linejoin"],["strokeMiterlimit","stroke-miterlimit"],["strokeOpacity","stroke-opacity"],["strokeWidth","stroke-width"],["textAnchor","text-anchor"],["textDecoration","text-decoration"],["textRendering","text-rendering"],["transformOrigin","transform-origin"],["underlinePosition","underline-position"],["underlineThickness","underline-thickness"],["unicodeBidi","unicode-bidi"],["unicodeRange","unicode-range"],["unitsPerEm","units-per-em"],["vAlphabetic","v-alphabetic"],["vHanging","v-hanging"],["vIdeographic","v-ideographic"],["vMathematical","v-mathematical"],["vectorEffect","vector-effect"],["vertAdvY","vert-adv-y"],["vertOriginX","vert-origin-x"],["vertOriginY","vert-origin-y"],["wordSpacing","word-spacing"],["writingMode","writing-mode"],["xmlnsXlink","xmlns:xlink"],["xHeight","x-height"]]),Oa=/^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;function ys(e){return Oa.test(""+e)?"javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')":e}function na(){}var Pu=null;function zu(e){return e=e.target||e.srcElement||window,e.correspondingUseElement&&(e=e.correspondingUseElement),e.nodeType===3?e.parentNode:e}var tr=null,er=null;function im(e){var n=ea(e);if(n&&(e=n.stateNode)){var a=e[Un]||null;t:switch(e=n.stateNode,n.type){case"input":if(Gt(e,a.value,a.defaultValue,a.defaultValue,a.checked,a.defaultChecked,a.type,a.name),n=a.name,a.type==="radio"&&n!=null){for(a=e;a.parentNode;)a=a.parentNode;for(a=a.querySelectorAll('input[name="'+Fe(""+n)+'"][type="radio"]'),n=0;n<a.length;n++){var o=a[n];if(o!==e&&o.form===e.form){var u=o[Un]||null;if(!u)throw Error(s(90));Gt(o,u.value,u.defaultValue,u.defaultValue,u.checked,u.defaultChecked,u.type,u.name)}}for(n=0;n<a.length;n++)o=a[n],o.form===e.form&&sn(o)}break t;case"textarea":ii(e,a.value,a.defaultValue);break t;case"select":n=a.value,n!=null&&bn(e,!!a.multiple,n,!1)}}}var Bu=!1;function am(e,n,a){if(Bu)return e(n,a);Bu=!0;try{var o=e(n);return o}finally{if(Bu=!1,(tr!==null||er!==null)&&(hc(),tr&&(n=tr,e=er,er=tr=null,im(n),e)))for(n=0;n<e.length;n++)im(e[n])}}function fo(e,n){var a=e.stateNode;if(a===null)return null;var o=a[Un]||null;if(o===null)return null;a=o[n];t:switch(n){case"onClick":case"onClickCapture":case"onDoubleClick":case"onDoubleClickCapture":case"onMouseDown":case"onMouseDownCapture":case"onMouseMove":case"onMouseMoveCapture":case"onMouseUp":case"onMouseUpCapture":case"onMouseEnter":(o=!o.disabled)||(e=e.type,o=!(e==="button"||e==="input"||e==="select"||e==="textarea")),e=!o;break t;default:e=!1}if(e)return null;if(a&&typeof a!="function")throw Error(s(231,n,typeof a));return a}var ia=!(typeof window>"u"||typeof window.document>"u"||typeof window.document.createElement>"u"),Iu=!1;if(ia)try{var ho={};Object.defineProperty(ho,"passive",{get:function(){Iu=!0}}),window.addEventListener("test",ho,ho),window.removeEventListener("test",ho,ho)}catch{Iu=!1}var Pa=null,Fu=null,Al=null;function sm(){if(Al)return Al;var e,n=Fu,a=n.length,o,u="value"in Pa?Pa.value:Pa.textContent,h=u.length;for(e=0;e<a&&n[e]===u[e];e++);var y=a-e;for(o=1;o<=y&&n[a-o]===u[h-o];o++);return Al=u.slice(e,1<o?1-o:void 0)}function Rl(e){var n=e.keyCode;return"charCode"in e?(e=e.charCode,e===0&&n===13&&(e=13)):e=n,e===10&&(e=13),32<=e||e===13?e:0}function wl(){return!0}function rm(){return!1}function jn(e){function n(a,o,u,h,y){this._reactName=a,this._targetInst=u,this.type=o,this.nativeEvent=h,this.target=y,this.currentTarget=null;for(var R in e)e.hasOwnProperty(R)&&(a=e[R],this[R]=a?a(h):h[R]);return this.isDefaultPrevented=(h.defaultPrevented!=null?h.defaultPrevented:h.returnValue===!1)?wl:rm,this.isPropagationStopped=rm,this}return v(n.prototype,{preventDefault:function(){this.defaultPrevented=!0;var a=this.nativeEvent;a&&(a.preventDefault?a.preventDefault():typeof a.returnValue!="unknown"&&(a.returnValue=!1),this.isDefaultPrevented=wl)},stopPropagation:function(){var a=this.nativeEvent;a&&(a.stopPropagation?a.stopPropagation():typeof a.cancelBubble!="unknown"&&(a.cancelBubble=!0),this.isPropagationStopped=wl)},persist:function(){},isPersistent:wl}),n}var Ss={eventPhase:0,bubbles:0,cancelable:0,timeStamp:function(e){return e.timeStamp||Date.now()},defaultPrevented:0,isTrusted:0},Cl=jn(Ss),po=v({},Ss,{view:0,detail:0}),ny=jn(po),Hu,Gu,mo,Dl=v({},po,{screenX:0,screenY:0,clientX:0,clientY:0,pageX:0,pageY:0,ctrlKey:0,shiftKey:0,altKey:0,metaKey:0,getModifierState:ku,button:0,buttons:0,relatedTarget:function(e){return e.relatedTarget===void 0?e.fromElement===e.srcElement?e.toElement:e.fromElement:e.relatedTarget},movementX:function(e){return"movementX"in e?e.movementX:(e!==mo&&(mo&&e.type==="mousemove"?(Hu=e.screenX-mo.screenX,Gu=e.screenY-mo.screenY):Gu=Hu=0,mo=e),Hu)},movementY:function(e){return"movementY"in e?e.movementY:Gu}}),om=jn(Dl),iy=v({},Dl,{dataTransfer:0}),ay=jn(iy),sy=v({},po,{relatedTarget:0}),Vu=jn(sy),ry=v({},Ss,{animationName:0,elapsedTime:0,pseudoElement:0}),oy=jn(ry),ly=v({},Ss,{clipboardData:function(e){return"clipboardData"in e?e.clipboardData:window.clipboardData}}),cy=jn(ly),uy=v({},Ss,{data:0}),lm=jn(uy),fy={Esc:"Escape",Spacebar:" ",Left:"ArrowLeft",Up:"ArrowUp",Right:"ArrowRight",Down:"ArrowDown",Del:"Delete",Win:"OS",Menu:"ContextMenu",Apps:"ContextMenu",Scroll:"ScrollLock",MozPrintableKey:"Unidentified"},hy={8:"Backspace",9:"Tab",12:"Clear",13:"Enter",16:"Shift",17:"Control",18:"Alt",19:"Pause",20:"CapsLock",27:"Escape",32:" ",33:"PageUp",34:"PageDown",35:"End",36:"Home",37:"ArrowLeft",38:"ArrowUp",39:"ArrowRight",40:"ArrowDown",45:"Insert",46:"Delete",112:"F1",113:"F2",114:"F3",115:"F4",116:"F5",117:"F6",118:"F7",119:"F8",120:"F9",121:"F10",122:"F11",123:"F12",144:"NumLock",145:"ScrollLock",224:"Meta"},dy={Alt:"altKey",Control:"ctrlKey",Meta:"metaKey",Shift:"shiftKey"};function py(e){var n=this.nativeEvent;return n.getModifierState?n.getModifierState(e):(e=dy[e])?!!n[e]:!1}function ku(){return py}var my=v({},po,{key:function(e){if(e.key){var n=fy[e.key]||e.key;if(n!=="Unidentified")return n}return e.type==="keypress"?(e=Rl(e),e===13?"Enter":String.fromCharCode(e)):e.type==="keydown"||e.type==="keyup"?hy[e.keyCode]||"Unidentified":""},code:0,location:0,ctrlKey:0,shiftKey:0,altKey:0,metaKey:0,repeat:0,locale:0,getModifierState:ku,charCode:function(e){return e.type==="keypress"?Rl(e):0},keyCode:function(e){return e.type==="keydown"||e.type==="keyup"?e.keyCode:0},which:function(e){return e.type==="keypress"?Rl(e):e.type==="keydown"||e.type==="keyup"?e.keyCode:0}}),gy=jn(my),_y=v({},Dl,{pointerId:0,width:0,height:0,pressure:0,tangentialPressure:0,tiltX:0,tiltY:0,twist:0,pointerType:0,isPrimary:0}),cm=jn(_y),vy=v({},po,{touches:0,targetTouches:0,changedTouches:0,altKey:0,metaKey:0,ctrlKey:0,shiftKey:0,getModifierState:ku}),xy=jn(vy),yy=v({},Ss,{propertyName:0,elapsedTime:0,pseudoElement:0}),Sy=jn(yy),My=v({},Dl,{deltaX:function(e){return"deltaX"in e?e.deltaX:"wheelDeltaX"in e?-e.wheelDeltaX:0},deltaY:function(e){return"deltaY"in e?e.deltaY:"wheelDeltaY"in e?-e.wheelDeltaY:"wheelDelta"in e?-e.wheelDelta:0},deltaZ:0,deltaMode:0}),Ey=jn(My),by=v({},Ss,{newState:0,oldState:0}),Ty=jn(by),Ay=[9,13,27,32],Xu=ia&&"CompositionEvent"in window,go=null;ia&&"documentMode"in document&&(go=document.documentMode);var Ry=ia&&"TextEvent"in window&&!go,um=ia&&(!Xu||go&&8<go&&11>=go),fm=" ",hm=!1;function dm(e,n){switch(e){case"keyup":return Ay.indexOf(n.keyCode)!==-1;case"keydown":return n.keyCode!==229;case"keypress":case"mousedown":case"focusout":return!0;default:return!1}}function pm(e){return e=e.detail,typeof e=="object"&&"data"in e?e.data:null}var nr=!1;function wy(e,n){switch(e){case"compositionend":return pm(n);case"keypress":return n.which!==32?null:(hm=!0,fm);case"textInput":return e=n.data,e===fm&&hm?null:e;default:return null}}function Cy(e,n){if(nr)return e==="compositionend"||!Xu&&dm(e,n)?(e=sm(),Al=Fu=Pa=null,nr=!1,e):null;switch(e){case"paste":return null;case"keypress":if(!(n.ctrlKey||n.altKey||n.metaKey)||n.ctrlKey&&n.altKey){if(n.char&&1<n.char.length)return n.char;if(n.which)return String.fromCharCode(n.which)}return null;case"compositionend":return um&&n.locale!=="ko"?null:n.data;default:return null}}var Dy={color:!0,date:!0,datetime:!0,"datetime-local":!0,email:!0,month:!0,number:!0,password:!0,range:!0,search:!0,tel:!0,text:!0,time:!0,url:!0,week:!0};function mm(e){var n=e&&e.nodeName&&e.nodeName.toLowerCase();return n==="input"?!!Dy[e.type]:n==="textarea"}function gm(e,n,a,o){tr?er?er.push(o):er=[o]:tr=o,n=xc(n,"onChange"),0<n.length&&(a=new Cl("onChange","change",null,a,o),e.push({event:a,listeners:n}))}var _o=null,vo=null;function Uy(e){Jg(e,0)}function Ul(e){var n=xs(e);if(sn(n))return e}function _m(e,n){if(e==="change")return n}var vm=!1;if(ia){var Wu;if(ia){var qu="oninput"in document;if(!qu){var xm=document.createElement("div");xm.setAttribute("oninput","return;"),qu=typeof xm.oninput=="function"}Wu=qu}else Wu=!1;vm=Wu&&(!document.documentMode||9<document.documentMode)}function ym(){_o&&(_o.detachEvent("onpropertychange",Sm),vo=_o=null)}function Sm(e){if(e.propertyName==="value"&&Ul(vo)){var n=[];gm(n,vo,e,zu(e)),am(Uy,n)}}function Ny(e,n,a){e==="focusin"?(ym(),_o=n,vo=a,_o.attachEvent("onpropertychange",Sm)):e==="focusout"&&ym()}function Ly(e){if(e==="selectionchange"||e==="keyup"||e==="keydown")return Ul(vo)}function Oy(e,n){if(e==="click")return Ul(n)}function Py(e,n){if(e==="input"||e==="change")return Ul(n)}function zy(e,n){return e===n&&(e!==0||1/e===1/n)||e!==e&&n!==n}var si=typeof Object.is=="function"?Object.is:zy;function xo(e,n){if(si(e,n))return!0;if(typeof e!="object"||e===null||typeof n!="object"||n===null)return!1;var a=Object.keys(e),o=Object.keys(n);if(a.length!==o.length)return!1;for(o=0;o<a.length;o++){var u=a[o];if(!Je.call(n,u)||!si(e[u],n[u]))return!1}return!0}function Mm(e){for(;e&&e.firstChild;)e=e.firstChild;return e}function Em(e,n){var a=Mm(e);e=0;for(var o;a;){if(a.nodeType===3){if(o=e+a.textContent.length,e<=n&&o>=n)return{node:a,offset:n-e};e=o}t:{for(;a;){if(a.nextSibling){a=a.nextSibling;break t}a=a.parentNode}a=void 0}a=Mm(a)}}function bm(e,n){return e&&n?e===n?!0:e&&e.nodeType===3?!1:n&&n.nodeType===3?bm(e,n.parentNode):"contains"in e?e.contains(n):e.compareDocumentPosition?!!(e.compareDocumentPosition(n)&16):!1:!1}function Tm(e){e=e!=null&&e.ownerDocument!=null&&e.ownerDocument.defaultView!=null?e.ownerDocument.defaultView:window;for(var n=je(e.document);n instanceof e.HTMLIFrameElement;){try{var a=typeof n.contentWindow.location.href=="string"}catch{a=!1}if(a)e=n.contentWindow;else break;n=je(e.document)}return n}function Yu(e){var n=e&&e.nodeName&&e.nodeName.toLowerCase();return n&&(n==="input"&&(e.type==="text"||e.type==="search"||e.type==="tel"||e.type==="url"||e.type==="password")||n==="textarea"||e.contentEditable==="true")}var By=ia&&"documentMode"in document&&11>=document.documentMode,ir=null,ju=null,yo=null,Zu=!1;function Am(e,n,a){var o=a.window===a?a.document:a.nodeType===9?a:a.ownerDocument;Zu||ir==null||ir!==je(o)||(o=ir,"selectionStart"in o&&Yu(o)?o={start:o.selectionStart,end:o.selectionEnd}:(o=(o.ownerDocument&&o.ownerDocument.defaultView||window).getSelection(),o={anchorNode:o.anchorNode,anchorOffset:o.anchorOffset,focusNode:o.focusNode,focusOffset:o.focusOffset}),yo&&xo(yo,o)||(yo=o,o=xc(ju,"onSelect"),0<o.length&&(n=new Cl("onSelect","select",null,n,a),e.push({event:n,listeners:o}),n.target=ir)))}function Ms(e,n){var a={};return a[e.toLowerCase()]=n.toLowerCase(),a["Webkit"+e]="webkit"+n,a["Moz"+e]="moz"+n,a}var ar={animationend:Ms("Animation","AnimationEnd"),animationiteration:Ms("Animation","AnimationIteration"),animationstart:Ms("Animation","AnimationStart"),transitionrun:Ms("Transition","TransitionRun"),transitionstart:Ms("Transition","TransitionStart"),transitioncancel:Ms("Transition","TransitionCancel"),transitionend:Ms("Transition","TransitionEnd")},Ku={},Rm={};ia&&(Rm=document.createElement("div").style,"AnimationEvent"in window||(delete ar.animationend.animation,delete ar.animationiteration.animation,delete ar.animationstart.animation),"TransitionEvent"in window||delete ar.transitionend.transition);function Es(e){if(Ku[e])return Ku[e];if(!ar[e])return e;var n=ar[e],a;for(a in n)if(n.hasOwnProperty(a)&&a in Rm)return Ku[e]=n[a];return e}var wm=Es("animationend"),Cm=Es("animationiteration"),Dm=Es("animationstart"),Iy=Es("transitionrun"),Fy=Es("transitionstart"),Hy=Es("transitioncancel"),Um=Es("transitionend"),Nm=new Map,Qu="abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(" ");Qu.push("scrollEnd");function Ui(e,n){Nm.set(e,n),Q(n,[e])}var Nl=typeof reportError=="function"?reportError:function(e){if(typeof window=="object"&&typeof window.ErrorEvent=="function"){var n=new window.ErrorEvent("error",{bubbles:!0,cancelable:!0,message:typeof e=="object"&&e!==null&&typeof e.message=="string"?String(e.message):String(e),error:e});if(!window.dispatchEvent(n))return}else if(typeof process=="object"&&typeof process.emit=="function"){process.emit("uncaughtException",e);return}console.error(e)},xi=[],sr=0,Ju=0;function Ll(){for(var e=sr,n=Ju=sr=0;n<e;){var a=xi[n];xi[n++]=null;var o=xi[n];xi[n++]=null;var u=xi[n];xi[n++]=null;var h=xi[n];if(xi[n++]=null,o!==null&&u!==null){var y=o.pending;y===null?u.next=u:(u.next=y.next,y.next=u),o.pending=u}h!==0&&Lm(a,u,h)}}function Ol(e,n,a,o){xi[sr++]=e,xi[sr++]=n,xi[sr++]=a,xi[sr++]=o,Ju|=o,e.lanes|=o,e=e.alternate,e!==null&&(e.lanes|=o)}function $u(e,n,a,o){return Ol(e,n,a,o),Pl(e)}function bs(e,n){return Ol(e,null,null,n),Pl(e)}function Lm(e,n,a){e.lanes|=a;var o=e.alternate;o!==null&&(o.lanes|=a);for(var u=!1,h=e.return;h!==null;)h.childLanes|=a,o=h.alternate,o!==null&&(o.childLanes|=a),h.tag===22&&(e=h.stateNode,e===null||e._visibility&1||(u=!0)),e=h,h=h.return;return e.tag===3?(h=e.stateNode,u&&n!==null&&(u=31-Ft(a),e=h.hiddenUpdates,o=e[u],o===null?e[u]=[n]:o.push(n),n.lane=a|536870912),h):null}function Pl(e){if(50<Vo)throw Vo=0,ch=null,Error(s(185));for(var n=e.return;n!==null;)e=n,n=e.return;return e.tag===3?e.stateNode:null}var rr={};function Gy(e,n,a,o){this.tag=e,this.key=a,this.sibling=this.child=this.return=this.stateNode=this.type=this.elementType=null,this.index=0,this.refCleanup=this.ref=null,this.pendingProps=n,this.dependencies=this.memoizedState=this.updateQueue=this.memoizedProps=null,this.mode=o,this.subtreeFlags=this.flags=0,this.deletions=null,this.childLanes=this.lanes=0,this.alternate=null}function ri(e,n,a,o){return new Gy(e,n,a,o)}function tf(e){return e=e.prototype,!(!e||!e.isReactComponent)}function aa(e,n){var a=e.alternate;return a===null?(a=ri(e.tag,n,e.key,e.mode),a.elementType=e.elementType,a.type=e.type,a.stateNode=e.stateNode,a.alternate=e,e.alternate=a):(a.pendingProps=n,a.type=e.type,a.flags=0,a.subtreeFlags=0,a.deletions=null),a.flags=e.flags&65011712,a.childLanes=e.childLanes,a.lanes=e.lanes,a.child=e.child,a.memoizedProps=e.memoizedProps,a.memoizedState=e.memoizedState,a.updateQueue=e.updateQueue,n=e.dependencies,a.dependencies=n===null?null:{lanes:n.lanes,firstContext:n.firstContext},a.sibling=e.sibling,a.index=e.index,a.ref=e.ref,a.refCleanup=e.refCleanup,a}function Om(e,n){e.flags&=65011714;var a=e.alternate;return a===null?(e.childLanes=0,e.lanes=n,e.child=null,e.subtreeFlags=0,e.memoizedProps=null,e.memoizedState=null,e.updateQueue=null,e.dependencies=null,e.stateNode=null):(e.childLanes=a.childLanes,e.lanes=a.lanes,e.child=a.child,e.subtreeFlags=0,e.deletions=null,e.memoizedProps=a.memoizedProps,e.memoizedState=a.memoizedState,e.updateQueue=a.updateQueue,e.type=a.type,n=a.dependencies,e.dependencies=n===null?null:{lanes:n.lanes,firstContext:n.firstContext}),e}function zl(e,n,a,o,u,h){var y=0;if(o=e,typeof e=="function")tf(e)&&(y=1);else if(typeof e=="string")y=qS(e,a,At.current)?26:e==="html"||e==="head"||e==="body"?27:5;else t:switch(e){case P:return e=ri(31,a,n,u),e.elementType=P,e.lanes=h,e;case w:return Ts(a.children,u,h,n);case E:y=8,u|=24;break;case _:return e=ri(12,a,n,u|2),e.elementType=_,e.lanes=h,e;case B:return e=ri(13,a,n,u),e.elementType=B,e.lanes=h,e;case O:return e=ri(19,a,n,u),e.elementType=O,e.lanes=h,e;default:if(typeof e=="object"&&e!==null)switch(e.$$typeof){case F:y=10;break t;case z:y=9;break t;case C:y=11;break t;case U:y=14;break t;case T:y=16,o=null;break t}y=29,a=Error(s(130,e===null?"null":typeof e,"")),o=null}return n=ri(y,a,n,u),n.elementType=e,n.type=o,n.lanes=h,n}function Ts(e,n,a,o){return e=ri(7,e,o,n),e.lanes=a,e}function ef(e,n,a){return e=ri(6,e,null,n),e.lanes=a,e}function Pm(e){var n=ri(18,null,null,0);return n.stateNode=e,n}function nf(e,n,a){return n=ri(4,e.children!==null?e.children:[],e.key,n),n.lanes=a,n.stateNode={containerInfo:e.containerInfo,pendingChildren:null,implementation:e.implementation},n}var zm=new WeakMap;function yi(e,n){if(typeof e=="object"&&e!==null){var a=zm.get(e);return a!==void 0?a:(n={value:e,source:n,stack:Qe(n)},zm.set(e,n),n)}return{value:e,source:n,stack:Qe(n)}}var or=[],lr=0,Bl=null,So=0,Si=[],Mi=0,za=null,Vi=1,ki="";function sa(e,n){or[lr++]=So,or[lr++]=Bl,Bl=e,So=n}function Bm(e,n,a){Si[Mi++]=Vi,Si[Mi++]=ki,Si[Mi++]=za,za=e;var o=Vi;e=ki;var u=32-Ft(o)-1;o&=~(1<<u),a+=1;var h=32-Ft(n)+u;if(30<h){var y=u-u%5;h=(o&(1<<y)-1).toString(32),o>>=y,u-=y,Vi=1<<32-Ft(n)+u|a<<u|o,ki=h+e}else Vi=1<<h|a<<u|o,ki=e}function af(e){e.return!==null&&(sa(e,1),Bm(e,1,0))}function sf(e){for(;e===Bl;)Bl=or[--lr],or[lr]=null,So=or[--lr],or[lr]=null;for(;e===za;)za=Si[--Mi],Si[Mi]=null,ki=Si[--Mi],Si[Mi]=null,Vi=Si[--Mi],Si[Mi]=null}function Im(e,n){Si[Mi++]=Vi,Si[Mi++]=ki,Si[Mi++]=za,Vi=n.id,ki=n.overflow,za=e}var Nn=null,tn=null,Te=!1,Ba=null,Ei=!1,rf=Error(s(519));function Ia(e){var n=Error(s(418,1<arguments.length&&arguments[1]!==void 0&&arguments[1]?"text":"HTML",""));throw Mo(yi(n,e)),rf}function Fm(e){var n=e.stateNode,a=e.type,o=e.memoizedProps;switch(n[mn]=e,n[Un]=o,a){case"dialog":Se("cancel",n),Se("close",n);break;case"iframe":case"object":case"embed":Se("load",n);break;case"video":case"audio":for(a=0;a<Xo.length;a++)Se(Xo[a],n);break;case"source":Se("error",n);break;case"img":case"image":case"link":Se("error",n),Se("load",n);break;case"details":Se("toggle",n);break;case"input":Se("invalid",n),Bn(n,o.value,o.defaultValue,o.checked,o.defaultChecked,o.type,o.name,!0);break;case"select":Se("invalid",n);break;case"textarea":Se("invalid",n),Ci(n,o.value,o.defaultValue,o.children)}a=o.children,typeof a!="string"&&typeof a!="number"&&typeof a!="bigint"||n.textContent===""+a||o.suppressHydrationWarning===!0||n_(n.textContent,a)?(o.popover!=null&&(Se("beforetoggle",n),Se("toggle",n)),o.onScroll!=null&&Se("scroll",n),o.onScrollEnd!=null&&Se("scrollend",n),o.onClick!=null&&(n.onclick=na),n=!0):n=!1,n||Ia(e,!0)}function Hm(e){for(Nn=e.return;Nn;)switch(Nn.tag){case 5:case 31:case 13:Ei=!1;return;case 27:case 3:Ei=!0;return;default:Nn=Nn.return}}function cr(e){if(e!==Nn)return!1;if(!Te)return Hm(e),Te=!0,!1;var n=e.tag,a;if((a=n!==3&&n!==27)&&((a=n===5)&&(a=e.type,a=!(a!=="form"&&a!=="button")||bh(e.type,e.memoizedProps)),a=!a),a&&tn&&Ia(e),Hm(e),n===13){if(e=e.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(s(317));tn=f_(e)}else if(n===31){if(e=e.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(s(317));tn=f_(e)}else n===27?(n=tn,Ja(e.type)?(e=Ch,Ch=null,tn=e):tn=n):tn=Nn?Ti(e.stateNode.nextSibling):null;return!0}function As(){tn=Nn=null,Te=!1}function of(){var e=Ba;return e!==null&&(Jn===null?Jn=e:Jn.push.apply(Jn,e),Ba=null),e}function Mo(e){Ba===null?Ba=[e]:Ba.push(e)}var lf=L(null),Rs=null,ra=null;function Fa(e,n,a){yt(lf,n._currentValue),n._currentValue=a}function oa(e){e._currentValue=lf.current,W(lf)}function cf(e,n,a){for(;e!==null;){var o=e.alternate;if((e.childLanes&n)!==n?(e.childLanes|=n,o!==null&&(o.childLanes|=n)):o!==null&&(o.childLanes&n)!==n&&(o.childLanes|=n),e===a)break;e=e.return}}function uf(e,n,a,o){var u=e.child;for(u!==null&&(u.return=e);u!==null;){var h=u.dependencies;if(h!==null){var y=u.child;h=h.firstContext;t:for(;h!==null;){var R=h;h=u;for(var G=0;G<n.length;G++)if(R.context===n[G]){h.lanes|=a,R=h.alternate,R!==null&&(R.lanes|=a),cf(h.return,a,e),o||(y=null);break t}h=R.next}}else if(u.tag===18){if(y=u.return,y===null)throw Error(s(341));y.lanes|=a,h=y.alternate,h!==null&&(h.lanes|=a),cf(y,a,e),y=null}else y=u.child;if(y!==null)y.return=u;else for(y=u;y!==null;){if(y===e){y=null;break}if(u=y.sibling,u!==null){u.return=y.return,y=u;break}y=y.return}u=y}}function ur(e,n,a,o){e=null;for(var u=n,h=!1;u!==null;){if(!h){if((u.flags&524288)!==0)h=!0;else if((u.flags&262144)!==0)break}if(u.tag===10){var y=u.alternate;if(y===null)throw Error(s(387));if(y=y.memoizedProps,y!==null){var R=u.type;si(u.pendingProps.value,y.value)||(e!==null?e.push(R):e=[R])}}else if(u===vt.current){if(y=u.alternate,y===null)throw Error(s(387));y.memoizedState.memoizedState!==u.memoizedState.memoizedState&&(e!==null?e.push(Zo):e=[Zo])}u=u.return}e!==null&&uf(n,e,a,o),n.flags|=262144}function Il(e){for(e=e.firstContext;e!==null;){if(!si(e.context._currentValue,e.memoizedValue))return!0;e=e.next}return!1}function ws(e){Rs=e,ra=null,e=e.dependencies,e!==null&&(e.firstContext=null)}function Ln(e){return Gm(Rs,e)}function Fl(e,n){return Rs===null&&ws(e),Gm(e,n)}function Gm(e,n){var a=n._currentValue;if(n={context:n,memoizedValue:a,next:null},ra===null){if(e===null)throw Error(s(308));ra=n,e.dependencies={lanes:0,firstContext:n},e.flags|=524288}else ra=ra.next=n;return a}var Vy=typeof AbortController<"u"?AbortController:function(){var e=[],n=this.signal={aborted:!1,addEventListener:function(a,o){e.push(o)}};this.abort=function(){n.aborted=!0,e.forEach(function(a){return a()})}},ky=r.unstable_scheduleCallback,Xy=r.unstable_NormalPriority,_n={$$typeof:F,Consumer:null,Provider:null,_currentValue:null,_currentValue2:null,_threadCount:0};function ff(){return{controller:new Vy,data:new Map,refCount:0}}function Eo(e){e.refCount--,e.refCount===0&&ky(Xy,function(){e.controller.abort()})}var bo=null,hf=0,fr=0,hr=null;function Wy(e,n){if(bo===null){var a=bo=[];hf=0,fr=mh(),hr={status:"pending",value:void 0,then:function(o){a.push(o)}}}return hf++,n.then(Vm,Vm),n}function Vm(){if(--hf===0&&bo!==null){hr!==null&&(hr.status="fulfilled");var e=bo;bo=null,fr=0,hr=null;for(var n=0;n<e.length;n++)(0,e[n])()}}function qy(e,n){var a=[],o={status:"pending",value:null,reason:null,then:function(u){a.push(u)}};return e.then(function(){o.status="fulfilled",o.value=n;for(var u=0;u<a.length;u++)(0,a[u])(n)},function(u){for(o.status="rejected",o.reason=u,u=0;u<a.length;u++)(0,a[u])(void 0)}),o}var km=I.S;I.S=function(e,n){Ag=Le(),typeof n=="object"&&n!==null&&typeof n.then=="function"&&Wy(e,n),km!==null&&km(e,n)};var Cs=L(null);function df(){var e=Cs.current;return e!==null?e:Ze.pooledCache}function Hl(e,n){n===null?yt(Cs,Cs.current):yt(Cs,n.pool)}function Xm(){var e=df();return e===null?null:{parent:_n._currentValue,pool:e}}var dr=Error(s(460)),pf=Error(s(474)),Gl=Error(s(542)),Vl={then:function(){}};function Wm(e){return e=e.status,e==="fulfilled"||e==="rejected"}function qm(e,n,a){switch(a=e[a],a===void 0?e.push(n):a!==n&&(n.then(na,na),n=a),n.status){case"fulfilled":return n.value;case"rejected":throw e=n.reason,jm(e),e;default:if(typeof n.status=="string")n.then(na,na);else{if(e=Ze,e!==null&&100<e.shellSuspendCounter)throw Error(s(482));e=n,e.status="pending",e.then(function(o){if(n.status==="pending"){var u=n;u.status="fulfilled",u.value=o}},function(o){if(n.status==="pending"){var u=n;u.status="rejected",u.reason=o}})}switch(n.status){case"fulfilled":return n.value;case"rejected":throw e=n.reason,jm(e),e}throw Us=n,dr}}function Ds(e){try{var n=e._init;return n(e._payload)}catch(a){throw a!==null&&typeof a=="object"&&typeof a.then=="function"?(Us=a,dr):a}}var Us=null;function Ym(){if(Us===null)throw Error(s(459));var e=Us;return Us=null,e}function jm(e){if(e===dr||e===Gl)throw Error(s(483))}var pr=null,To=0;function kl(e){var n=To;return To+=1,pr===null&&(pr=[]),qm(pr,e,n)}function Ao(e,n){n=n.props.ref,e.ref=n!==void 0?n:null}function Xl(e,n){throw n.$$typeof===g?Error(s(525)):(e=Object.prototype.toString.call(n),Error(s(31,e==="[object Object]"?"object with keys {"+Object.keys(n).join(", ")+"}":e)))}function Zm(e){function n(tt,j){if(e){var it=tt.deletions;it===null?(tt.deletions=[j],tt.flags|=16):it.push(j)}}function a(tt,j){if(!e)return null;for(;j!==null;)n(tt,j),j=j.sibling;return null}function o(tt){for(var j=new Map;tt!==null;)tt.key!==null?j.set(tt.key,tt):j.set(tt.index,tt),tt=tt.sibling;return j}function u(tt,j){return tt=aa(tt,j),tt.index=0,tt.sibling=null,tt}function h(tt,j,it){return tt.index=it,e?(it=tt.alternate,it!==null?(it=it.index,it<j?(tt.flags|=67108866,j):it):(tt.flags|=67108866,j)):(tt.flags|=1048576,j)}function y(tt){return e&&tt.alternate===null&&(tt.flags|=67108866),tt}function R(tt,j,it,xt){return j===null||j.tag!==6?(j=ef(it,tt.mode,xt),j.return=tt,j):(j=u(j,it),j.return=tt,j)}function G(tt,j,it,xt){var ne=it.type;return ne===w?mt(tt,j,it.props.children,xt,it.key):j!==null&&(j.elementType===ne||typeof ne=="object"&&ne!==null&&ne.$$typeof===T&&Ds(ne)===j.type)?(j=u(j,it.props),Ao(j,it),j.return=tt,j):(j=zl(it.type,it.key,it.props,null,tt.mode,xt),Ao(j,it),j.return=tt,j)}function at(tt,j,it,xt){return j===null||j.tag!==4||j.stateNode.containerInfo!==it.containerInfo||j.stateNode.implementation!==it.implementation?(j=nf(it,tt.mode,xt),j.return=tt,j):(j=u(j,it.children||[]),j.return=tt,j)}function mt(tt,j,it,xt,ne){return j===null||j.tag!==7?(j=Ts(it,tt.mode,xt,ne),j.return=tt,j):(j=u(j,it),j.return=tt,j)}function Mt(tt,j,it){if(typeof j=="string"&&j!==""||typeof j=="number"||typeof j=="bigint")return j=ef(""+j,tt.mode,it),j.return=tt,j;if(typeof j=="object"&&j!==null){switch(j.$$typeof){case S:return it=zl(j.type,j.key,j.props,null,tt.mode,it),Ao(it,j),it.return=tt,it;case b:return j=nf(j,tt.mode,it),j.return=tt,j;case T:return j=Ds(j),Mt(tt,j,it)}if(et(j)||J(j))return j=Ts(j,tt.mode,it,null),j.return=tt,j;if(typeof j.then=="function")return Mt(tt,kl(j),it);if(j.$$typeof===F)return Mt(tt,Fl(tt,j),it);Xl(tt,j)}return null}function lt(tt,j,it,xt){var ne=j!==null?j.key:null;if(typeof it=="string"&&it!==""||typeof it=="number"||typeof it=="bigint")return ne!==null?null:R(tt,j,""+it,xt);if(typeof it=="object"&&it!==null){switch(it.$$typeof){case S:return it.key===ne?G(tt,j,it,xt):null;case b:return it.key===ne?at(tt,j,it,xt):null;case T:return it=Ds(it),lt(tt,j,it,xt)}if(et(it)||J(it))return ne!==null?null:mt(tt,j,it,xt,null);if(typeof it.then=="function")return lt(tt,j,kl(it),xt);if(it.$$typeof===F)return lt(tt,j,Fl(tt,it),xt);Xl(tt,it)}return null}function ft(tt,j,it,xt,ne){if(typeof xt=="string"&&xt!==""||typeof xt=="number"||typeof xt=="bigint")return tt=tt.get(it)||null,R(j,tt,""+xt,ne);if(typeof xt=="object"&&xt!==null){switch(xt.$$typeof){case S:return tt=tt.get(xt.key===null?it:xt.key)||null,G(j,tt,xt,ne);case b:return tt=tt.get(xt.key===null?it:xt.key)||null,at(j,tt,xt,ne);case T:return xt=Ds(xt),ft(tt,j,it,xt,ne)}if(et(xt)||J(xt))return tt=tt.get(it)||null,mt(j,tt,xt,ne,null);if(typeof xt.then=="function")return ft(tt,j,it,kl(xt),ne);if(xt.$$typeof===F)return ft(tt,j,it,Fl(j,xt),ne);Xl(j,xt)}return null}function qt(tt,j,it,xt){for(var ne=null,Ue=null,Zt=j,pe=j=0,be=null;Zt!==null&&pe<it.length;pe++){Zt.index>pe?(be=Zt,Zt=null):be=Zt.sibling;var Ne=lt(tt,Zt,it[pe],xt);if(Ne===null){Zt===null&&(Zt=be);break}e&&Zt&&Ne.alternate===null&&n(tt,Zt),j=h(Ne,j,pe),Ue===null?ne=Ne:Ue.sibling=Ne,Ue=Ne,Zt=be}if(pe===it.length)return a(tt,Zt),Te&&sa(tt,pe),ne;if(Zt===null){for(;pe<it.length;pe++)Zt=Mt(tt,it[pe],xt),Zt!==null&&(j=h(Zt,j,pe),Ue===null?ne=Zt:Ue.sibling=Zt,Ue=Zt);return Te&&sa(tt,pe),ne}for(Zt=o(Zt);pe<it.length;pe++)be=ft(Zt,tt,pe,it[pe],xt),be!==null&&(e&&be.alternate!==null&&Zt.delete(be.key===null?pe:be.key),j=h(be,j,pe),Ue===null?ne=be:Ue.sibling=be,Ue=be);return e&&Zt.forEach(function(is){return n(tt,is)}),Te&&sa(tt,pe),ne}function ae(tt,j,it,xt){if(it==null)throw Error(s(151));for(var ne=null,Ue=null,Zt=j,pe=j=0,be=null,Ne=it.next();Zt!==null&&!Ne.done;pe++,Ne=it.next()){Zt.index>pe?(be=Zt,Zt=null):be=Zt.sibling;var is=lt(tt,Zt,Ne.value,xt);if(is===null){Zt===null&&(Zt=be);break}e&&Zt&&is.alternate===null&&n(tt,Zt),j=h(is,j,pe),Ue===null?ne=is:Ue.sibling=is,Ue=is,Zt=be}if(Ne.done)return a(tt,Zt),Te&&sa(tt,pe),ne;if(Zt===null){for(;!Ne.done;pe++,Ne=it.next())Ne=Mt(tt,Ne.value,xt),Ne!==null&&(j=h(Ne,j,pe),Ue===null?ne=Ne:Ue.sibling=Ne,Ue=Ne);return Te&&sa(tt,pe),ne}for(Zt=o(Zt);!Ne.done;pe++,Ne=it.next())Ne=ft(Zt,tt,pe,Ne.value,xt),Ne!==null&&(e&&Ne.alternate!==null&&Zt.delete(Ne.key===null?pe:Ne.key),j=h(Ne,j,pe),Ue===null?ne=Ne:Ue.sibling=Ne,Ue=Ne);return e&&Zt.forEach(function(iM){return n(tt,iM)}),Te&&sa(tt,pe),ne}function Ye(tt,j,it,xt){if(typeof it=="object"&&it!==null&&it.type===w&&it.key===null&&(it=it.props.children),typeof it=="object"&&it!==null){switch(it.$$typeof){case S:t:{for(var ne=it.key;j!==null;){if(j.key===ne){if(ne=it.type,ne===w){if(j.tag===7){a(tt,j.sibling),xt=u(j,it.props.children),xt.return=tt,tt=xt;break t}}else if(j.elementType===ne||typeof ne=="object"&&ne!==null&&ne.$$typeof===T&&Ds(ne)===j.type){a(tt,j.sibling),xt=u(j,it.props),Ao(xt,it),xt.return=tt,tt=xt;break t}a(tt,j);break}else n(tt,j);j=j.sibling}it.type===w?(xt=Ts(it.props.children,tt.mode,xt,it.key),xt.return=tt,tt=xt):(xt=zl(it.type,it.key,it.props,null,tt.mode,xt),Ao(xt,it),xt.return=tt,tt=xt)}return y(tt);case b:t:{for(ne=it.key;j!==null;){if(j.key===ne)if(j.tag===4&&j.stateNode.containerInfo===it.containerInfo&&j.stateNode.implementation===it.implementation){a(tt,j.sibling),xt=u(j,it.children||[]),xt.return=tt,tt=xt;break t}else{a(tt,j);break}else n(tt,j);j=j.sibling}xt=nf(it,tt.mode,xt),xt.return=tt,tt=xt}return y(tt);case T:return it=Ds(it),Ye(tt,j,it,xt)}if(et(it))return qt(tt,j,it,xt);if(J(it)){if(ne=J(it),typeof ne!="function")throw Error(s(150));return it=ne.call(it),ae(tt,j,it,xt)}if(typeof it.then=="function")return Ye(tt,j,kl(it),xt);if(it.$$typeof===F)return Ye(tt,j,Fl(tt,it),xt);Xl(tt,it)}return typeof it=="string"&&it!==""||typeof it=="number"||typeof it=="bigint"?(it=""+it,j!==null&&j.tag===6?(a(tt,j.sibling),xt=u(j,it),xt.return=tt,tt=xt):(a(tt,j),xt=ef(it,tt.mode,xt),xt.return=tt,tt=xt),y(tt)):a(tt,j)}return function(tt,j,it,xt){try{To=0;var ne=Ye(tt,j,it,xt);return pr=null,ne}catch(Zt){if(Zt===dr||Zt===Gl)throw Zt;var Ue=ri(29,Zt,null,tt.mode);return Ue.lanes=xt,Ue.return=tt,Ue}finally{}}}var Ns=Zm(!0),Km=Zm(!1),Ha=!1;function mf(e){e.updateQueue={baseState:e.memoizedState,firstBaseUpdate:null,lastBaseUpdate:null,shared:{pending:null,lanes:0,hiddenCallbacks:null},callbacks:null}}function gf(e,n){e=e.updateQueue,n.updateQueue===e&&(n.updateQueue={baseState:e.baseState,firstBaseUpdate:e.firstBaseUpdate,lastBaseUpdate:e.lastBaseUpdate,shared:e.shared,callbacks:null})}function Ga(e){return{lane:e,tag:0,payload:null,callback:null,next:null}}function Va(e,n,a){var o=e.updateQueue;if(o===null)return null;if(o=o.shared,(Oe&2)!==0){var u=o.pending;return u===null?n.next=n:(n.next=u.next,u.next=n),o.pending=n,n=Pl(e),Lm(e,null,a),n}return Ol(e,o,n,a),Pl(e)}function Ro(e,n,a){if(n=n.updateQueue,n!==null&&(n=n.shared,(a&4194048)!==0)){var o=n.lanes;o&=e.pendingLanes,a|=o,n.lanes=a,ei(e,a)}}function _f(e,n){var a=e.updateQueue,o=e.alternate;if(o!==null&&(o=o.updateQueue,a===o)){var u=null,h=null;if(a=a.firstBaseUpdate,a!==null){do{var y={lane:a.lane,tag:a.tag,payload:a.payload,callback:null,next:null};h===null?u=h=y:h=h.next=y,a=a.next}while(a!==null);h===null?u=h=n:h=h.next=n}else u=h=n;a={baseState:o.baseState,firstBaseUpdate:u,lastBaseUpdate:h,shared:o.shared,callbacks:o.callbacks},e.updateQueue=a;return}e=a.lastBaseUpdate,e===null?a.firstBaseUpdate=n:e.next=n,a.lastBaseUpdate=n}var vf=!1;function wo(){if(vf){var e=hr;if(e!==null)throw e}}function Co(e,n,a,o){vf=!1;var u=e.updateQueue;Ha=!1;var h=u.firstBaseUpdate,y=u.lastBaseUpdate,R=u.shared.pending;if(R!==null){u.shared.pending=null;var G=R,at=G.next;G.next=null,y===null?h=at:y.next=at,y=G;var mt=e.alternate;mt!==null&&(mt=mt.updateQueue,R=mt.lastBaseUpdate,R!==y&&(R===null?mt.firstBaseUpdate=at:R.next=at,mt.lastBaseUpdate=G))}if(h!==null){var Mt=u.baseState;y=0,mt=at=G=null,R=h;do{var lt=R.lane&-536870913,ft=lt!==R.lane;if(ft?(Ee&lt)===lt:(o&lt)===lt){lt!==0&&lt===fr&&(vf=!0),mt!==null&&(mt=mt.next={lane:0,tag:R.tag,payload:R.payload,callback:null,next:null});t:{var qt=e,ae=R;lt=n;var Ye=a;switch(ae.tag){case 1:if(qt=ae.payload,typeof qt=="function"){Mt=qt.call(Ye,Mt,lt);break t}Mt=qt;break t;case 3:qt.flags=qt.flags&-65537|128;case 0:if(qt=ae.payload,lt=typeof qt=="function"?qt.call(Ye,Mt,lt):qt,lt==null)break t;Mt=v({},Mt,lt);break t;case 2:Ha=!0}}lt=R.callback,lt!==null&&(e.flags|=64,ft&&(e.flags|=8192),ft=u.callbacks,ft===null?u.callbacks=[lt]:ft.push(lt))}else ft={lane:lt,tag:R.tag,payload:R.payload,callback:R.callback,next:null},mt===null?(at=mt=ft,G=Mt):mt=mt.next=ft,y|=lt;if(R=R.next,R===null){if(R=u.shared.pending,R===null)break;ft=R,R=ft.next,ft.next=null,u.lastBaseUpdate=ft,u.shared.pending=null}}while(!0);mt===null&&(G=Mt),u.baseState=G,u.firstBaseUpdate=at,u.lastBaseUpdate=mt,h===null&&(u.shared.lanes=0),Ya|=y,e.lanes=y,e.memoizedState=Mt}}function Qm(e,n){if(typeof e!="function")throw Error(s(191,e));e.call(n)}function Jm(e,n){var a=e.callbacks;if(a!==null)for(e.callbacks=null,e=0;e<a.length;e++)Qm(a[e],n)}var mr=L(null),Wl=L(0);function $m(e,n){e=ga,yt(Wl,e),yt(mr,n),ga=e|n.baseLanes}function xf(){yt(Wl,ga),yt(mr,mr.current)}function yf(){ga=Wl.current,W(mr),W(Wl)}var oi=L(null),bi=null;function ka(e){var n=e.alternate;yt(dn,dn.current&1),yt(oi,e),bi===null&&(n===null||mr.current!==null||n.memoizedState!==null)&&(bi=e)}function Sf(e){yt(dn,dn.current),yt(oi,e),bi===null&&(bi=e)}function t0(e){e.tag===22?(yt(dn,dn.current),yt(oi,e),bi===null&&(bi=e)):Xa()}function Xa(){yt(dn,dn.current),yt(oi,oi.current)}function li(e){W(oi),bi===e&&(bi=null),W(dn)}var dn=L(0);function ql(e){for(var n=e;n!==null;){if(n.tag===13){var a=n.memoizedState;if(a!==null&&(a=a.dehydrated,a===null||Rh(a)||wh(a)))return n}else if(n.tag===19&&(n.memoizedProps.revealOrder==="forwards"||n.memoizedProps.revealOrder==="backwards"||n.memoizedProps.revealOrder==="unstable_legacy-backwards"||n.memoizedProps.revealOrder==="together")){if((n.flags&128)!==0)return n}else if(n.child!==null){n.child.return=n,n=n.child;continue}if(n===e)break;for(;n.sibling===null;){if(n.return===null||n.return===e)return null;n=n.return}n.sibling.return=n.return,n=n.sibling}return null}var la=0,he=null,We=null,vn=null,Yl=!1,gr=!1,Ls=!1,jl=0,Do=0,_r=null,Yy=0;function un(){throw Error(s(321))}function Mf(e,n){if(n===null)return!1;for(var a=0;a<n.length&&a<e.length;a++)if(!si(e[a],n[a]))return!1;return!0}function Ef(e,n,a,o,u,h){return la=h,he=n,n.memoizedState=null,n.updateQueue=null,n.lanes=0,I.H=e===null||e.memoizedState===null?B0:If,Ls=!1,h=a(o,u),Ls=!1,gr&&(h=n0(n,a,o,u)),e0(e),h}function e0(e){I.H=Lo;var n=We!==null&&We.next!==null;if(la=0,vn=We=he=null,Yl=!1,Do=0,_r=null,n)throw Error(s(300));e===null||xn||(e=e.dependencies,e!==null&&Il(e)&&(xn=!0))}function n0(e,n,a,o){he=e;var u=0;do{if(gr&&(_r=null),Do=0,gr=!1,25<=u)throw Error(s(301));if(u+=1,vn=We=null,e.updateQueue!=null){var h=e.updateQueue;h.lastEffect=null,h.events=null,h.stores=null,h.memoCache!=null&&(h.memoCache.index=0)}I.H=I0,h=n(a,o)}while(gr);return h}function jy(){var e=I.H,n=e.useState()[0];return n=typeof n.then=="function"?Uo(n):n,e=e.useState()[0],(We!==null?We.memoizedState:null)!==e&&(he.flags|=1024),n}function bf(){var e=jl!==0;return jl=0,e}function Tf(e,n,a){n.updateQueue=e.updateQueue,n.flags&=-2053,e.lanes&=~a}function Af(e){if(Yl){for(e=e.memoizedState;e!==null;){var n=e.queue;n!==null&&(n.pending=null),e=e.next}Yl=!1}la=0,vn=We=he=null,gr=!1,Do=jl=0,_r=null}function kn(){var e={memoizedState:null,baseState:null,baseQueue:null,queue:null,next:null};return vn===null?he.memoizedState=vn=e:vn=vn.next=e,vn}function pn(){if(We===null){var e=he.alternate;e=e!==null?e.memoizedState:null}else e=We.next;var n=vn===null?he.memoizedState:vn.next;if(n!==null)vn=n,We=e;else{if(e===null)throw he.alternate===null?Error(s(467)):Error(s(310));We=e,e={memoizedState:We.memoizedState,baseState:We.baseState,baseQueue:We.baseQueue,queue:We.queue,next:null},vn===null?he.memoizedState=vn=e:vn=vn.next=e}return vn}function Zl(){return{lastEffect:null,events:null,stores:null,memoCache:null}}function Uo(e){var n=Do;return Do+=1,_r===null&&(_r=[]),e=qm(_r,e,n),n=he,(vn===null?n.memoizedState:vn.next)===null&&(n=n.alternate,I.H=n===null||n.memoizedState===null?B0:If),e}function Kl(e){if(e!==null&&typeof e=="object"){if(typeof e.then=="function")return Uo(e);if(e.$$typeof===F)return Ln(e)}throw Error(s(438,String(e)))}function Rf(e){var n=null,a=he.updateQueue;if(a!==null&&(n=a.memoCache),n==null){var o=he.alternate;o!==null&&(o=o.updateQueue,o!==null&&(o=o.memoCache,o!=null&&(n={data:o.data.map(function(u){return u.slice()}),index:0})))}if(n==null&&(n={data:[],index:0}),a===null&&(a=Zl(),he.updateQueue=a),a.memoCache=n,a=n.data[n.index],a===void 0)for(a=n.data[n.index]=Array(e),o=0;o<e;o++)a[o]=$;return n.index++,a}function ca(e,n){return typeof n=="function"?n(e):n}function Ql(e){var n=pn();return wf(n,We,e)}function wf(e,n,a){var o=e.queue;if(o===null)throw Error(s(311));o.lastRenderedReducer=a;var u=e.baseQueue,h=o.pending;if(h!==null){if(u!==null){var y=u.next;u.next=h.next,h.next=y}n.baseQueue=u=h,o.pending=null}if(h=e.baseState,u===null)e.memoizedState=h;else{n=u.next;var R=y=null,G=null,at=n,mt=!1;do{var Mt=at.lane&-536870913;if(Mt!==at.lane?(Ee&Mt)===Mt:(la&Mt)===Mt){var lt=at.revertLane;if(lt===0)G!==null&&(G=G.next={lane:0,revertLane:0,gesture:null,action:at.action,hasEagerState:at.hasEagerState,eagerState:at.eagerState,next:null}),Mt===fr&&(mt=!0);else if((la&lt)===lt){at=at.next,lt===fr&&(mt=!0);continue}else Mt={lane:0,revertLane:at.revertLane,gesture:null,action:at.action,hasEagerState:at.hasEagerState,eagerState:at.eagerState,next:null},G===null?(R=G=Mt,y=h):G=G.next=Mt,he.lanes|=lt,Ya|=lt;Mt=at.action,Ls&&a(h,Mt),h=at.hasEagerState?at.eagerState:a(h,Mt)}else lt={lane:Mt,revertLane:at.revertLane,gesture:at.gesture,action:at.action,hasEagerState:at.hasEagerState,eagerState:at.eagerState,next:null},G===null?(R=G=lt,y=h):G=G.next=lt,he.lanes|=Mt,Ya|=Mt;at=at.next}while(at!==null&&at!==n);if(G===null?y=h:G.next=R,!si(h,e.memoizedState)&&(xn=!0,mt&&(a=hr,a!==null)))throw a;e.memoizedState=h,e.baseState=y,e.baseQueue=G,o.lastRenderedState=h}return u===null&&(o.lanes=0),[e.memoizedState,o.dispatch]}function Cf(e){var n=pn(),a=n.queue;if(a===null)throw Error(s(311));a.lastRenderedReducer=e;var o=a.dispatch,u=a.pending,h=n.memoizedState;if(u!==null){a.pending=null;var y=u=u.next;do h=e(h,y.action),y=y.next;while(y!==u);si(h,n.memoizedState)||(xn=!0),n.memoizedState=h,n.baseQueue===null&&(n.baseState=h),a.lastRenderedState=h}return[h,o]}function i0(e,n,a){var o=he,u=pn(),h=Te;if(h){if(a===void 0)throw Error(s(407));a=a()}else a=n();var y=!si((We||u).memoizedState,a);if(y&&(u.memoizedState=a,xn=!0),u=u.queue,Nf(r0.bind(null,o,u,e),[e]),u.getSnapshot!==n||y||vn!==null&&vn.memoizedState.tag&1){if(o.flags|=2048,vr(9,{destroy:void 0},s0.bind(null,o,u,a,n),null),Ze===null)throw Error(s(349));h||(la&127)!==0||a0(o,n,a)}return a}function a0(e,n,a){e.flags|=16384,e={getSnapshot:n,value:a},n=he.updateQueue,n===null?(n=Zl(),he.updateQueue=n,n.stores=[e]):(a=n.stores,a===null?n.stores=[e]:a.push(e))}function s0(e,n,a,o){n.value=a,n.getSnapshot=o,o0(n)&&l0(e)}function r0(e,n,a){return a(function(){o0(n)&&l0(e)})}function o0(e){var n=e.getSnapshot;e=e.value;try{var a=n();return!si(e,a)}catch{return!0}}function l0(e){var n=bs(e,2);n!==null&&$n(n,e,2)}function Df(e){var n=kn();if(typeof e=="function"){var a=e;if(e=a(),Ls){Rt(!0);try{a()}finally{Rt(!1)}}}return n.memoizedState=n.baseState=e,n.queue={pending:null,lanes:0,dispatch:null,lastRenderedReducer:ca,lastRenderedState:e},n}function c0(e,n,a,o){return e.baseState=a,wf(e,We,typeof o=="function"?o:ca)}function Zy(e,n,a,o,u){if(tc(e))throw Error(s(485));if(e=n.action,e!==null){var h={payload:u,action:e,next:null,isTransition:!0,status:"pending",value:null,reason:null,listeners:[],then:function(y){h.listeners.push(y)}};I.T!==null?a(!0):h.isTransition=!1,o(h),a=n.pending,a===null?(h.next=n.pending=h,u0(n,h)):(h.next=a.next,n.pending=a.next=h)}}function u0(e,n){var a=n.action,o=n.payload,u=e.state;if(n.isTransition){var h=I.T,y={};I.T=y;try{var R=a(u,o),G=I.S;G!==null&&G(y,R),f0(e,n,R)}catch(at){Uf(e,n,at)}finally{h!==null&&y.types!==null&&(h.types=y.types),I.T=h}}else try{h=a(u,o),f0(e,n,h)}catch(at){Uf(e,n,at)}}function f0(e,n,a){a!==null&&typeof a=="object"&&typeof a.then=="function"?a.then(function(o){h0(e,n,o)},function(o){return Uf(e,n,o)}):h0(e,n,a)}function h0(e,n,a){n.status="fulfilled",n.value=a,d0(n),e.state=a,n=e.pending,n!==null&&(a=n.next,a===n?e.pending=null:(a=a.next,n.next=a,u0(e,a)))}function Uf(e,n,a){var o=e.pending;if(e.pending=null,o!==null){o=o.next;do n.status="rejected",n.reason=a,d0(n),n=n.next;while(n!==o)}e.action=null}function d0(e){e=e.listeners;for(var n=0;n<e.length;n++)(0,e[n])()}function p0(e,n){return n}function m0(e,n){if(Te){var a=Ze.formState;if(a!==null){t:{var o=he;if(Te){if(tn){e:{for(var u=tn,h=Ei;u.nodeType!==8;){if(!h){u=null;break e}if(u=Ti(u.nextSibling),u===null){u=null;break e}}h=u.data,u=h==="F!"||h==="F"?u:null}if(u){tn=Ti(u.nextSibling),o=u.data==="F!";break t}}Ia(o)}o=!1}o&&(n=a[0])}}return a=kn(),a.memoizedState=a.baseState=n,o={pending:null,lanes:0,dispatch:null,lastRenderedReducer:p0,lastRenderedState:n},a.queue=o,a=O0.bind(null,he,o),o.dispatch=a,o=Df(!1),h=Bf.bind(null,he,!1,o.queue),o=kn(),u={state:n,dispatch:null,action:e,pending:null},o.queue=u,a=Zy.bind(null,he,u,h,a),u.dispatch=a,o.memoizedState=e,[n,a,!1]}function g0(e){var n=pn();return _0(n,We,e)}function _0(e,n,a){if(n=wf(e,n,p0)[0],e=Ql(ca)[0],typeof n=="object"&&n!==null&&typeof n.then=="function")try{var o=Uo(n)}catch(y){throw y===dr?Gl:y}else o=n;n=pn();var u=n.queue,h=u.dispatch;return a!==n.memoizedState&&(he.flags|=2048,vr(9,{destroy:void 0},Ky.bind(null,u,a),null)),[o,h,e]}function Ky(e,n){e.action=n}function v0(e){var n=pn(),a=We;if(a!==null)return _0(n,a,e);pn(),n=n.memoizedState,a=pn();var o=a.queue.dispatch;return a.memoizedState=e,[n,o,!1]}function vr(e,n,a,o){return e={tag:e,create:a,deps:o,inst:n,next:null},n=he.updateQueue,n===null&&(n=Zl(),he.updateQueue=n),a=n.lastEffect,a===null?n.lastEffect=e.next=e:(o=a.next,a.next=e,e.next=o,n.lastEffect=e),e}function x0(){return pn().memoizedState}function Jl(e,n,a,o){var u=kn();he.flags|=e,u.memoizedState=vr(1|n,{destroy:void 0},a,o===void 0?null:o)}function $l(e,n,a,o){var u=pn();o=o===void 0?null:o;var h=u.memoizedState.inst;We!==null&&o!==null&&Mf(o,We.memoizedState.deps)?u.memoizedState=vr(n,h,a,o):(he.flags|=e,u.memoizedState=vr(1|n,h,a,o))}function y0(e,n){Jl(8390656,8,e,n)}function Nf(e,n){$l(2048,8,e,n)}function Qy(e){he.flags|=4;var n=he.updateQueue;if(n===null)n=Zl(),he.updateQueue=n,n.events=[e];else{var a=n.events;a===null?n.events=[e]:a.push(e)}}function S0(e){var n=pn().memoizedState;return Qy({ref:n,nextImpl:e}),function(){if((Oe&2)!==0)throw Error(s(440));return n.impl.apply(void 0,arguments)}}function M0(e,n){return $l(4,2,e,n)}function E0(e,n){return $l(4,4,e,n)}function b0(e,n){if(typeof n=="function"){e=e();var a=n(e);return function(){typeof a=="function"?a():n(null)}}if(n!=null)return e=e(),n.current=e,function(){n.current=null}}function T0(e,n,a){a=a!=null?a.concat([e]):null,$l(4,4,b0.bind(null,n,e),a)}function Lf(){}function A0(e,n){var a=pn();n=n===void 0?null:n;var o=a.memoizedState;return n!==null&&Mf(n,o[1])?o[0]:(a.memoizedState=[e,n],e)}function R0(e,n){var a=pn();n=n===void 0?null:n;var o=a.memoizedState;if(n!==null&&Mf(n,o[1]))return o[0];if(o=e(),Ls){Rt(!0);try{e()}finally{Rt(!1)}}return a.memoizedState=[o,n],o}function Of(e,n,a){return a===void 0||(la&1073741824)!==0&&(Ee&261930)===0?e.memoizedState=n:(e.memoizedState=a,e=wg(),he.lanes|=e,Ya|=e,a)}function w0(e,n,a,o){return si(a,n)?a:mr.current!==null?(e=Of(e,a,o),si(e,n)||(xn=!0),e):(la&42)===0||(la&1073741824)!==0&&(Ee&261930)===0?(xn=!0,e.memoizedState=a):(e=wg(),he.lanes|=e,Ya|=e,n)}function C0(e,n,a,o,u){var h=H.p;H.p=h!==0&&8>h?h:8;var y=I.T,R={};I.T=R,Bf(e,!1,n,a);try{var G=u(),at=I.S;if(at!==null&&at(R,G),G!==null&&typeof G=="object"&&typeof G.then=="function"){var mt=qy(G,o);No(e,n,mt,fi(e))}else No(e,n,o,fi(e))}catch(Mt){No(e,n,{then:function(){},status:"rejected",reason:Mt},fi())}finally{H.p=h,y!==null&&R.types!==null&&(y.types=R.types),I.T=y}}function Jy(){}function Pf(e,n,a,o){if(e.tag!==5)throw Error(s(476));var u=D0(e).queue;C0(e,u,n,q,a===null?Jy:function(){return U0(e),a(o)})}function D0(e){var n=e.memoizedState;if(n!==null)return n;n={memoizedState:q,baseState:q,baseQueue:null,queue:{pending:null,lanes:0,dispatch:null,lastRenderedReducer:ca,lastRenderedState:q},next:null};var a={};return n.next={memoizedState:a,baseState:a,baseQueue:null,queue:{pending:null,lanes:0,dispatch:null,lastRenderedReducer:ca,lastRenderedState:a},next:null},e.memoizedState=n,e=e.alternate,e!==null&&(e.memoizedState=n),n}function U0(e){var n=D0(e);n.next===null&&(n=e.alternate.memoizedState),No(e,n.next.queue,{},fi())}function zf(){return Ln(Zo)}function N0(){return pn().memoizedState}function L0(){return pn().memoizedState}function $y(e){for(var n=e.return;n!==null;){switch(n.tag){case 24:case 3:var a=fi();e=Ga(a);var o=Va(n,e,a);o!==null&&($n(o,n,a),Ro(o,n,a)),n={cache:ff()},e.payload=n;return}n=n.return}}function tS(e,n,a){var o=fi();a={lane:o,revertLane:0,gesture:null,action:a,hasEagerState:!1,eagerState:null,next:null},tc(e)?P0(n,a):(a=$u(e,n,a,o),a!==null&&($n(a,e,o),z0(a,n,o)))}function O0(e,n,a){var o=fi();No(e,n,a,o)}function No(e,n,a,o){var u={lane:o,revertLane:0,gesture:null,action:a,hasEagerState:!1,eagerState:null,next:null};if(tc(e))P0(n,u);else{var h=e.alternate;if(e.lanes===0&&(h===null||h.lanes===0)&&(h=n.lastRenderedReducer,h!==null))try{var y=n.lastRenderedState,R=h(y,a);if(u.hasEagerState=!0,u.eagerState=R,si(R,y))return Ol(e,n,u,0),Ze===null&&Ll(),!1}catch{}finally{}if(a=$u(e,n,u,o),a!==null)return $n(a,e,o),z0(a,n,o),!0}return!1}function Bf(e,n,a,o){if(o={lane:2,revertLane:mh(),gesture:null,action:o,hasEagerState:!1,eagerState:null,next:null},tc(e)){if(n)throw Error(s(479))}else n=$u(e,a,o,2),n!==null&&$n(n,e,2)}function tc(e){var n=e.alternate;return e===he||n!==null&&n===he}function P0(e,n){gr=Yl=!0;var a=e.pending;a===null?n.next=n:(n.next=a.next,a.next=n),e.pending=n}function z0(e,n,a){if((a&4194048)!==0){var o=n.lanes;o&=e.pendingLanes,a|=o,n.lanes=a,ei(e,a)}}var Lo={readContext:Ln,use:Kl,useCallback:un,useContext:un,useEffect:un,useImperativeHandle:un,useLayoutEffect:un,useInsertionEffect:un,useMemo:un,useReducer:un,useRef:un,useState:un,useDebugValue:un,useDeferredValue:un,useTransition:un,useSyncExternalStore:un,useId:un,useHostTransitionStatus:un,useFormState:un,useActionState:un,useOptimistic:un,useMemoCache:un,useCacheRefresh:un};Lo.useEffectEvent=un;var B0={readContext:Ln,use:Kl,useCallback:function(e,n){return kn().memoizedState=[e,n===void 0?null:n],e},useContext:Ln,useEffect:y0,useImperativeHandle:function(e,n,a){a=a!=null?a.concat([e]):null,Jl(4194308,4,b0.bind(null,n,e),a)},useLayoutEffect:function(e,n){return Jl(4194308,4,e,n)},useInsertionEffect:function(e,n){Jl(4,2,e,n)},useMemo:function(e,n){var a=kn();n=n===void 0?null:n;var o=e();if(Ls){Rt(!0);try{e()}finally{Rt(!1)}}return a.memoizedState=[o,n],o},useReducer:function(e,n,a){var o=kn();if(a!==void 0){var u=a(n);if(Ls){Rt(!0);try{a(n)}finally{Rt(!1)}}}else u=n;return o.memoizedState=o.baseState=u,e={pending:null,lanes:0,dispatch:null,lastRenderedReducer:e,lastRenderedState:u},o.queue=e,e=e.dispatch=tS.bind(null,he,e),[o.memoizedState,e]},useRef:function(e){var n=kn();return e={current:e},n.memoizedState=e},useState:function(e){e=Df(e);var n=e.queue,a=O0.bind(null,he,n);return n.dispatch=a,[e.memoizedState,a]},useDebugValue:Lf,useDeferredValue:function(e,n){var a=kn();return Of(a,e,n)},useTransition:function(){var e=Df(!1);return e=C0.bind(null,he,e.queue,!0,!1),kn().memoizedState=e,[!1,e]},useSyncExternalStore:function(e,n,a){var o=he,u=kn();if(Te){if(a===void 0)throw Error(s(407));a=a()}else{if(a=n(),Ze===null)throw Error(s(349));(Ee&127)!==0||a0(o,n,a)}u.memoizedState=a;var h={value:a,getSnapshot:n};return u.queue=h,y0(r0.bind(null,o,h,e),[e]),o.flags|=2048,vr(9,{destroy:void 0},s0.bind(null,o,h,a,n),null),a},useId:function(){var e=kn(),n=Ze.identifierPrefix;if(Te){var a=ki,o=Vi;a=(o&~(1<<32-Ft(o)-1)).toString(32)+a,n="_"+n+"R_"+a,a=jl++,0<a&&(n+="H"+a.toString(32)),n+="_"}else a=Yy++,n="_"+n+"r_"+a.toString(32)+"_";return e.memoizedState=n},useHostTransitionStatus:zf,useFormState:m0,useActionState:m0,useOptimistic:function(e){var n=kn();n.memoizedState=n.baseState=e;var a={pending:null,lanes:0,dispatch:null,lastRenderedReducer:null,lastRenderedState:null};return n.queue=a,n=Bf.bind(null,he,!0,a),a.dispatch=n,[e,n]},useMemoCache:Rf,useCacheRefresh:function(){return kn().memoizedState=$y.bind(null,he)},useEffectEvent:function(e){var n=kn(),a={impl:e};return n.memoizedState=a,function(){if((Oe&2)!==0)throw Error(s(440));return a.impl.apply(void 0,arguments)}}},If={readContext:Ln,use:Kl,useCallback:A0,useContext:Ln,useEffect:Nf,useImperativeHandle:T0,useInsertionEffect:M0,useLayoutEffect:E0,useMemo:R0,useReducer:Ql,useRef:x0,useState:function(){return Ql(ca)},useDebugValue:Lf,useDeferredValue:function(e,n){var a=pn();return w0(a,We.memoizedState,e,n)},useTransition:function(){var e=Ql(ca)[0],n=pn().memoizedState;return[typeof e=="boolean"?e:Uo(e),n]},useSyncExternalStore:i0,useId:N0,useHostTransitionStatus:zf,useFormState:g0,useActionState:g0,useOptimistic:function(e,n){var a=pn();return c0(a,We,e,n)},useMemoCache:Rf,useCacheRefresh:L0};If.useEffectEvent=S0;var I0={readContext:Ln,use:Kl,useCallback:A0,useContext:Ln,useEffect:Nf,useImperativeHandle:T0,useInsertionEffect:M0,useLayoutEffect:E0,useMemo:R0,useReducer:Cf,useRef:x0,useState:function(){return Cf(ca)},useDebugValue:Lf,useDeferredValue:function(e,n){var a=pn();return We===null?Of(a,e,n):w0(a,We.memoizedState,e,n)},useTransition:function(){var e=Cf(ca)[0],n=pn().memoizedState;return[typeof e=="boolean"?e:Uo(e),n]},useSyncExternalStore:i0,useId:N0,useHostTransitionStatus:zf,useFormState:v0,useActionState:v0,useOptimistic:function(e,n){var a=pn();return We!==null?c0(a,We,e,n):(a.baseState=e,[e,a.queue.dispatch])},useMemoCache:Rf,useCacheRefresh:L0};I0.useEffectEvent=S0;function Ff(e,n,a,o){n=e.memoizedState,a=a(o,n),a=a==null?n:v({},n,a),e.memoizedState=a,e.lanes===0&&(e.updateQueue.baseState=a)}var Hf={enqueueSetState:function(e,n,a){e=e._reactInternals;var o=fi(),u=Ga(o);u.payload=n,a!=null&&(u.callback=a),n=Va(e,u,o),n!==null&&($n(n,e,o),Ro(n,e,o))},enqueueReplaceState:function(e,n,a){e=e._reactInternals;var o=fi(),u=Ga(o);u.tag=1,u.payload=n,a!=null&&(u.callback=a),n=Va(e,u,o),n!==null&&($n(n,e,o),Ro(n,e,o))},enqueueForceUpdate:function(e,n){e=e._reactInternals;var a=fi(),o=Ga(a);o.tag=2,n!=null&&(o.callback=n),n=Va(e,o,a),n!==null&&($n(n,e,a),Ro(n,e,a))}};function F0(e,n,a,o,u,h,y){return e=e.stateNode,typeof e.shouldComponentUpdate=="function"?e.shouldComponentUpdate(o,h,y):n.prototype&&n.prototype.isPureReactComponent?!xo(a,o)||!xo(u,h):!0}function H0(e,n,a,o){e=n.state,typeof n.componentWillReceiveProps=="function"&&n.componentWillReceiveProps(a,o),typeof n.UNSAFE_componentWillReceiveProps=="function"&&n.UNSAFE_componentWillReceiveProps(a,o),n.state!==e&&Hf.enqueueReplaceState(n,n.state,null)}function Os(e,n){var a=n;if("ref"in n){a={};for(var o in n)o!=="ref"&&(a[o]=n[o])}if(e=e.defaultProps){a===n&&(a=v({},a));for(var u in e)a[u]===void 0&&(a[u]=e[u])}return a}function G0(e){Nl(e)}function V0(e){console.error(e)}function k0(e){Nl(e)}function ec(e,n){try{var a=e.onUncaughtError;a(n.value,{componentStack:n.stack})}catch(o){setTimeout(function(){throw o})}}function X0(e,n,a){try{var o=e.onCaughtError;o(a.value,{componentStack:a.stack,errorBoundary:n.tag===1?n.stateNode:null})}catch(u){setTimeout(function(){throw u})}}function Gf(e,n,a){return a=Ga(a),a.tag=3,a.payload={element:null},a.callback=function(){ec(e,n)},a}function W0(e){return e=Ga(e),e.tag=3,e}function q0(e,n,a,o){var u=a.type.getDerivedStateFromError;if(typeof u=="function"){var h=o.value;e.payload=function(){return u(h)},e.callback=function(){X0(n,a,o)}}var y=a.stateNode;y!==null&&typeof y.componentDidCatch=="function"&&(e.callback=function(){X0(n,a,o),typeof u!="function"&&(ja===null?ja=new Set([this]):ja.add(this));var R=o.stack;this.componentDidCatch(o.value,{componentStack:R!==null?R:""})})}function eS(e,n,a,o,u){if(a.flags|=32768,o!==null&&typeof o=="object"&&typeof o.then=="function"){if(n=a.alternate,n!==null&&ur(n,a,u,!0),a=oi.current,a!==null){switch(a.tag){case 31:case 13:return bi===null?dc():a.alternate===null&&fn===0&&(fn=3),a.flags&=-257,a.flags|=65536,a.lanes=u,o===Vl?a.flags|=16384:(n=a.updateQueue,n===null?a.updateQueue=new Set([o]):n.add(o),hh(e,o,u)),!1;case 22:return a.flags|=65536,o===Vl?a.flags|=16384:(n=a.updateQueue,n===null?(n={transitions:null,markerInstances:null,retryQueue:new Set([o])},a.updateQueue=n):(a=n.retryQueue,a===null?n.retryQueue=new Set([o]):a.add(o)),hh(e,o,u)),!1}throw Error(s(435,a.tag))}return hh(e,o,u),dc(),!1}if(Te)return n=oi.current,n!==null?((n.flags&65536)===0&&(n.flags|=256),n.flags|=65536,n.lanes=u,o!==rf&&(e=Error(s(422),{cause:o}),Mo(yi(e,a)))):(o!==rf&&(n=Error(s(423),{cause:o}),Mo(yi(n,a))),e=e.current.alternate,e.flags|=65536,u&=-u,e.lanes|=u,o=yi(o,a),u=Gf(e.stateNode,o,u),_f(e,u),fn!==4&&(fn=2)),!1;var h=Error(s(520),{cause:o});if(h=yi(h,a),Go===null?Go=[h]:Go.push(h),fn!==4&&(fn=2),n===null)return!0;o=yi(o,a),a=n;do{switch(a.tag){case 3:return a.flags|=65536,e=u&-u,a.lanes|=e,e=Gf(a.stateNode,o,e),_f(a,e),!1;case 1:if(n=a.type,h=a.stateNode,(a.flags&128)===0&&(typeof n.getDerivedStateFromError=="function"||h!==null&&typeof h.componentDidCatch=="function"&&(ja===null||!ja.has(h))))return a.flags|=65536,u&=-u,a.lanes|=u,u=W0(u),q0(u,e,a,o),_f(a,u),!1}a=a.return}while(a!==null);return!1}var Vf=Error(s(461)),xn=!1;function On(e,n,a,o){n.child=e===null?Km(n,null,a,o):Ns(n,e.child,a,o)}function Y0(e,n,a,o,u){a=a.render;var h=n.ref;if("ref"in o){var y={};for(var R in o)R!=="ref"&&(y[R]=o[R])}else y=o;return ws(n),o=Ef(e,n,a,y,h,u),R=bf(),e!==null&&!xn?(Tf(e,n,u),ua(e,n,u)):(Te&&R&&af(n),n.flags|=1,On(e,n,o,u),n.child)}function j0(e,n,a,o,u){if(e===null){var h=a.type;return typeof h=="function"&&!tf(h)&&h.defaultProps===void 0&&a.compare===null?(n.tag=15,n.type=h,Z0(e,n,h,o,u)):(e=zl(a.type,null,o,n,n.mode,u),e.ref=n.ref,e.return=n,n.child=e)}if(h=e.child,!Kf(e,u)){var y=h.memoizedProps;if(a=a.compare,a=a!==null?a:xo,a(y,o)&&e.ref===n.ref)return ua(e,n,u)}return n.flags|=1,e=aa(h,o),e.ref=n.ref,e.return=n,n.child=e}function Z0(e,n,a,o,u){if(e!==null){var h=e.memoizedProps;if(xo(h,o)&&e.ref===n.ref)if(xn=!1,n.pendingProps=o=h,Kf(e,u))(e.flags&131072)!==0&&(xn=!0);else return n.lanes=e.lanes,ua(e,n,u)}return kf(e,n,a,o,u)}function K0(e,n,a,o){var u=o.children,h=e!==null?e.memoizedState:null;if(e===null&&n.stateNode===null&&(n.stateNode={_visibility:1,_pendingMarkers:null,_retryCache:null,_transitions:null}),o.mode==="hidden"){if((n.flags&128)!==0){if(h=h!==null?h.baseLanes|a:a,e!==null){for(o=n.child=e.child,u=0;o!==null;)u=u|o.lanes|o.childLanes,o=o.sibling;o=u&~h}else o=0,n.child=null;return Q0(e,n,h,a,o)}if((a&536870912)!==0)n.memoizedState={baseLanes:0,cachePool:null},e!==null&&Hl(n,h!==null?h.cachePool:null),h!==null?$m(n,h):xf(),t0(n);else return o=n.lanes=536870912,Q0(e,n,h!==null?h.baseLanes|a:a,a,o)}else h!==null?(Hl(n,h.cachePool),$m(n,h),Xa(),n.memoizedState=null):(e!==null&&Hl(n,null),xf(),Xa());return On(e,n,u,a),n.child}function Oo(e,n){return e!==null&&e.tag===22||n.stateNode!==null||(n.stateNode={_visibility:1,_pendingMarkers:null,_retryCache:null,_transitions:null}),n.sibling}function Q0(e,n,a,o,u){var h=df();return h=h===null?null:{parent:_n._currentValue,pool:h},n.memoizedState={baseLanes:a,cachePool:h},e!==null&&Hl(n,null),xf(),t0(n),e!==null&&ur(e,n,o,!0),n.childLanes=u,null}function nc(e,n){return n=ac({mode:n.mode,children:n.children},e.mode),n.ref=e.ref,e.child=n,n.return=e,n}function J0(e,n,a){return Ns(n,e.child,null,a),e=nc(n,n.pendingProps),e.flags|=2,li(n),n.memoizedState=null,e}function nS(e,n,a){var o=n.pendingProps,u=(n.flags&128)!==0;if(n.flags&=-129,e===null){if(Te){if(o.mode==="hidden")return e=nc(n,o),n.lanes=536870912,Oo(null,e);if(Sf(n),(e=tn)?(e=u_(e,Ei),e=e!==null&&e.data==="&"?e:null,e!==null&&(n.memoizedState={dehydrated:e,treeContext:za!==null?{id:Vi,overflow:ki}:null,retryLane:536870912,hydrationErrors:null},a=Pm(e),a.return=n,n.child=a,Nn=n,tn=null)):e=null,e===null)throw Ia(n);return n.lanes=536870912,null}return nc(n,o)}var h=e.memoizedState;if(h!==null){var y=h.dehydrated;if(Sf(n),u)if(n.flags&256)n.flags&=-257,n=J0(e,n,a);else if(n.memoizedState!==null)n.child=e.child,n.flags|=128,n=null;else throw Error(s(558));else if(xn||ur(e,n,a,!1),u=(a&e.childLanes)!==0,xn||u){if(o=Ze,o!==null&&(y=ni(o,a),y!==0&&y!==h.retryLane))throw h.retryLane=y,bs(e,y),$n(o,e,y),Vf;dc(),n=J0(e,n,a)}else e=h.treeContext,tn=Ti(y.nextSibling),Nn=n,Te=!0,Ba=null,Ei=!1,e!==null&&Im(n,e),n=nc(n,o),n.flags|=4096;return n}return e=aa(e.child,{mode:o.mode,children:o.children}),e.ref=n.ref,n.child=e,e.return=n,e}function ic(e,n){var a=n.ref;if(a===null)e!==null&&e.ref!==null&&(n.flags|=4194816);else{if(typeof a!="function"&&typeof a!="object")throw Error(s(284));(e===null||e.ref!==a)&&(n.flags|=4194816)}}function kf(e,n,a,o,u){return ws(n),a=Ef(e,n,a,o,void 0,u),o=bf(),e!==null&&!xn?(Tf(e,n,u),ua(e,n,u)):(Te&&o&&af(n),n.flags|=1,On(e,n,a,u),n.child)}function $0(e,n,a,o,u,h){return ws(n),n.updateQueue=null,a=n0(n,o,a,u),e0(e),o=bf(),e!==null&&!xn?(Tf(e,n,h),ua(e,n,h)):(Te&&o&&af(n),n.flags|=1,On(e,n,a,h),n.child)}function tg(e,n,a,o,u){if(ws(n),n.stateNode===null){var h=rr,y=a.contextType;typeof y=="object"&&y!==null&&(h=Ln(y)),h=new a(o,h),n.memoizedState=h.state!==null&&h.state!==void 0?h.state:null,h.updater=Hf,n.stateNode=h,h._reactInternals=n,h=n.stateNode,h.props=o,h.state=n.memoizedState,h.refs={},mf(n),y=a.contextType,h.context=typeof y=="object"&&y!==null?Ln(y):rr,h.state=n.memoizedState,y=a.getDerivedStateFromProps,typeof y=="function"&&(Ff(n,a,y,o),h.state=n.memoizedState),typeof a.getDerivedStateFromProps=="function"||typeof h.getSnapshotBeforeUpdate=="function"||typeof h.UNSAFE_componentWillMount!="function"&&typeof h.componentWillMount!="function"||(y=h.state,typeof h.componentWillMount=="function"&&h.componentWillMount(),typeof h.UNSAFE_componentWillMount=="function"&&h.UNSAFE_componentWillMount(),y!==h.state&&Hf.enqueueReplaceState(h,h.state,null),Co(n,o,h,u),wo(),h.state=n.memoizedState),typeof h.componentDidMount=="function"&&(n.flags|=4194308),o=!0}else if(e===null){h=n.stateNode;var R=n.memoizedProps,G=Os(a,R);h.props=G;var at=h.context,mt=a.contextType;y=rr,typeof mt=="object"&&mt!==null&&(y=Ln(mt));var Mt=a.getDerivedStateFromProps;mt=typeof Mt=="function"||typeof h.getSnapshotBeforeUpdate=="function",R=n.pendingProps!==R,mt||typeof h.UNSAFE_componentWillReceiveProps!="function"&&typeof h.componentWillReceiveProps!="function"||(R||at!==y)&&H0(n,h,o,y),Ha=!1;var lt=n.memoizedState;h.state=lt,Co(n,o,h,u),wo(),at=n.memoizedState,R||lt!==at||Ha?(typeof Mt=="function"&&(Ff(n,a,Mt,o),at=n.memoizedState),(G=Ha||F0(n,a,G,o,lt,at,y))?(mt||typeof h.UNSAFE_componentWillMount!="function"&&typeof h.componentWillMount!="function"||(typeof h.componentWillMount=="function"&&h.componentWillMount(),typeof h.UNSAFE_componentWillMount=="function"&&h.UNSAFE_componentWillMount()),typeof h.componentDidMount=="function"&&(n.flags|=4194308)):(typeof h.componentDidMount=="function"&&(n.flags|=4194308),n.memoizedProps=o,n.memoizedState=at),h.props=o,h.state=at,h.context=y,o=G):(typeof h.componentDidMount=="function"&&(n.flags|=4194308),o=!1)}else{h=n.stateNode,gf(e,n),y=n.memoizedProps,mt=Os(a,y),h.props=mt,Mt=n.pendingProps,lt=h.context,at=a.contextType,G=rr,typeof at=="object"&&at!==null&&(G=Ln(at)),R=a.getDerivedStateFromProps,(at=typeof R=="function"||typeof h.getSnapshotBeforeUpdate=="function")||typeof h.UNSAFE_componentWillReceiveProps!="function"&&typeof h.componentWillReceiveProps!="function"||(y!==Mt||lt!==G)&&H0(n,h,o,G),Ha=!1,lt=n.memoizedState,h.state=lt,Co(n,o,h,u),wo();var ft=n.memoizedState;y!==Mt||lt!==ft||Ha||e!==null&&e.dependencies!==null&&Il(e.dependencies)?(typeof R=="function"&&(Ff(n,a,R,o),ft=n.memoizedState),(mt=Ha||F0(n,a,mt,o,lt,ft,G)||e!==null&&e.dependencies!==null&&Il(e.dependencies))?(at||typeof h.UNSAFE_componentWillUpdate!="function"&&typeof h.componentWillUpdate!="function"||(typeof h.componentWillUpdate=="function"&&h.componentWillUpdate(o,ft,G),typeof h.UNSAFE_componentWillUpdate=="function"&&h.UNSAFE_componentWillUpdate(o,ft,G)),typeof h.componentDidUpdate=="function"&&(n.flags|=4),typeof h.getSnapshotBeforeUpdate=="function"&&(n.flags|=1024)):(typeof h.componentDidUpdate!="function"||y===e.memoizedProps&&lt===e.memoizedState||(n.flags|=4),typeof h.getSnapshotBeforeUpdate!="function"||y===e.memoizedProps&&lt===e.memoizedState||(n.flags|=1024),n.memoizedProps=o,n.memoizedState=ft),h.props=o,h.state=ft,h.context=G,o=mt):(typeof h.componentDidUpdate!="function"||y===e.memoizedProps&&lt===e.memoizedState||(n.flags|=4),typeof h.getSnapshotBeforeUpdate!="function"||y===e.memoizedProps&&lt===e.memoizedState||(n.flags|=1024),o=!1)}return h=o,ic(e,n),o=(n.flags&128)!==0,h||o?(h=n.stateNode,a=o&&typeof a.getDerivedStateFromError!="function"?null:h.render(),n.flags|=1,e!==null&&o?(n.child=Ns(n,e.child,null,u),n.child=Ns(n,null,a,u)):On(e,n,a,u),n.memoizedState=h.state,e=n.child):e=ua(e,n,u),e}function eg(e,n,a,o){return As(),n.flags|=256,On(e,n,a,o),n.child}var Xf={dehydrated:null,treeContext:null,retryLane:0,hydrationErrors:null};function Wf(e){return{baseLanes:e,cachePool:Xm()}}function qf(e,n,a){return e=e!==null?e.childLanes&~a:0,n&&(e|=ui),e}function ng(e,n,a){var o=n.pendingProps,u=!1,h=(n.flags&128)!==0,y;if((y=h)||(y=e!==null&&e.memoizedState===null?!1:(dn.current&2)!==0),y&&(u=!0,n.flags&=-129),y=(n.flags&32)!==0,n.flags&=-33,e===null){if(Te){if(u?ka(n):Xa(),(e=tn)?(e=u_(e,Ei),e=e!==null&&e.data!=="&"?e:null,e!==null&&(n.memoizedState={dehydrated:e,treeContext:za!==null?{id:Vi,overflow:ki}:null,retryLane:536870912,hydrationErrors:null},a=Pm(e),a.return=n,n.child=a,Nn=n,tn=null)):e=null,e===null)throw Ia(n);return wh(e)?n.lanes=32:n.lanes=536870912,null}var R=o.children;return o=o.fallback,u?(Xa(),u=n.mode,R=ac({mode:"hidden",children:R},u),o=Ts(o,u,a,null),R.return=n,o.return=n,R.sibling=o,n.child=R,o=n.child,o.memoizedState=Wf(a),o.childLanes=qf(e,y,a),n.memoizedState=Xf,Oo(null,o)):(ka(n),Yf(n,R))}var G=e.memoizedState;if(G!==null&&(R=G.dehydrated,R!==null)){if(h)n.flags&256?(ka(n),n.flags&=-257,n=jf(e,n,a)):n.memoizedState!==null?(Xa(),n.child=e.child,n.flags|=128,n=null):(Xa(),R=o.fallback,u=n.mode,o=ac({mode:"visible",children:o.children},u),R=Ts(R,u,a,null),R.flags|=2,o.return=n,R.return=n,o.sibling=R,n.child=o,Ns(n,e.child,null,a),o=n.child,o.memoizedState=Wf(a),o.childLanes=qf(e,y,a),n.memoizedState=Xf,n=Oo(null,o));else if(ka(n),wh(R)){if(y=R.nextSibling&&R.nextSibling.dataset,y)var at=y.dgst;y=at,o=Error(s(419)),o.stack="",o.digest=y,Mo({value:o,source:null,stack:null}),n=jf(e,n,a)}else if(xn||ur(e,n,a,!1),y=(a&e.childLanes)!==0,xn||y){if(y=Ze,y!==null&&(o=ni(y,a),o!==0&&o!==G.retryLane))throw G.retryLane=o,bs(e,o),$n(y,e,o),Vf;Rh(R)||dc(),n=jf(e,n,a)}else Rh(R)?(n.flags|=192,n.child=e.child,n=null):(e=G.treeContext,tn=Ti(R.nextSibling),Nn=n,Te=!0,Ba=null,Ei=!1,e!==null&&Im(n,e),n=Yf(n,o.children),n.flags|=4096);return n}return u?(Xa(),R=o.fallback,u=n.mode,G=e.child,at=G.sibling,o=aa(G,{mode:"hidden",children:o.children}),o.subtreeFlags=G.subtreeFlags&65011712,at!==null?R=aa(at,R):(R=Ts(R,u,a,null),R.flags|=2),R.return=n,o.return=n,o.sibling=R,n.child=o,Oo(null,o),o=n.child,R=e.child.memoizedState,R===null?R=Wf(a):(u=R.cachePool,u!==null?(G=_n._currentValue,u=u.parent!==G?{parent:G,pool:G}:u):u=Xm(),R={baseLanes:R.baseLanes|a,cachePool:u}),o.memoizedState=R,o.childLanes=qf(e,y,a),n.memoizedState=Xf,Oo(e.child,o)):(ka(n),a=e.child,e=a.sibling,a=aa(a,{mode:"visible",children:o.children}),a.return=n,a.sibling=null,e!==null&&(y=n.deletions,y===null?(n.deletions=[e],n.flags|=16):y.push(e)),n.child=a,n.memoizedState=null,a)}function Yf(e,n){return n=ac({mode:"visible",children:n},e.mode),n.return=e,e.child=n}function ac(e,n){return e=ri(22,e,null,n),e.lanes=0,e}function jf(e,n,a){return Ns(n,e.child,null,a),e=Yf(n,n.pendingProps.children),e.flags|=2,n.memoizedState=null,e}function ig(e,n,a){e.lanes|=n;var o=e.alternate;o!==null&&(o.lanes|=n),cf(e.return,n,a)}function Zf(e,n,a,o,u,h){var y=e.memoizedState;y===null?e.memoizedState={isBackwards:n,rendering:null,renderingStartTime:0,last:o,tail:a,tailMode:u,treeForkCount:h}:(y.isBackwards=n,y.rendering=null,y.renderingStartTime=0,y.last=o,y.tail=a,y.tailMode=u,y.treeForkCount=h)}function ag(e,n,a){var o=n.pendingProps,u=o.revealOrder,h=o.tail;o=o.children;var y=dn.current,R=(y&2)!==0;if(R?(y=y&1|2,n.flags|=128):y&=1,yt(dn,y),On(e,n,o,a),o=Te?So:0,!R&&e!==null&&(e.flags&128)!==0)t:for(e=n.child;e!==null;){if(e.tag===13)e.memoizedState!==null&&ig(e,a,n);else if(e.tag===19)ig(e,a,n);else if(e.child!==null){e.child.return=e,e=e.child;continue}if(e===n)break t;for(;e.sibling===null;){if(e.return===null||e.return===n)break t;e=e.return}e.sibling.return=e.return,e=e.sibling}switch(u){case"forwards":for(a=n.child,u=null;a!==null;)e=a.alternate,e!==null&&ql(e)===null&&(u=a),a=a.sibling;a=u,a===null?(u=n.child,n.child=null):(u=a.sibling,a.sibling=null),Zf(n,!1,u,a,h,o);break;case"backwards":case"unstable_legacy-backwards":for(a=null,u=n.child,n.child=null;u!==null;){if(e=u.alternate,e!==null&&ql(e)===null){n.child=u;break}e=u.sibling,u.sibling=a,a=u,u=e}Zf(n,!0,a,null,h,o);break;case"together":Zf(n,!1,null,null,void 0,o);break;default:n.memoizedState=null}return n.child}function ua(e,n,a){if(e!==null&&(n.dependencies=e.dependencies),Ya|=n.lanes,(a&n.childLanes)===0)if(e!==null){if(ur(e,n,a,!1),(a&n.childLanes)===0)return null}else return null;if(e!==null&&n.child!==e.child)throw Error(s(153));if(n.child!==null){for(e=n.child,a=aa(e,e.pendingProps),n.child=a,a.return=n;e.sibling!==null;)e=e.sibling,a=a.sibling=aa(e,e.pendingProps),a.return=n;a.sibling=null}return n.child}function Kf(e,n){return(e.lanes&n)!==0?!0:(e=e.dependencies,!!(e!==null&&Il(e)))}function iS(e,n,a){switch(n.tag){case 3:St(n,n.stateNode.containerInfo),Fa(n,_n,e.memoizedState.cache),As();break;case 27:case 5:Jt(n);break;case 4:St(n,n.stateNode.containerInfo);break;case 10:Fa(n,n.type,n.memoizedProps.value);break;case 31:if(n.memoizedState!==null)return n.flags|=128,Sf(n),null;break;case 13:var o=n.memoizedState;if(o!==null)return o.dehydrated!==null?(ka(n),n.flags|=128,null):(a&n.child.childLanes)!==0?ng(e,n,a):(ka(n),e=ua(e,n,a),e!==null?e.sibling:null);ka(n);break;case 19:var u=(e.flags&128)!==0;if(o=(a&n.childLanes)!==0,o||(ur(e,n,a,!1),o=(a&n.childLanes)!==0),u){if(o)return ag(e,n,a);n.flags|=128}if(u=n.memoizedState,u!==null&&(u.rendering=null,u.tail=null,u.lastEffect=null),yt(dn,dn.current),o)break;return null;case 22:return n.lanes=0,K0(e,n,a,n.pendingProps);case 24:Fa(n,_n,e.memoizedState.cache)}return ua(e,n,a)}function sg(e,n,a){if(e!==null)if(e.memoizedProps!==n.pendingProps)xn=!0;else{if(!Kf(e,a)&&(n.flags&128)===0)return xn=!1,iS(e,n,a);xn=(e.flags&131072)!==0}else xn=!1,Te&&(n.flags&1048576)!==0&&Bm(n,So,n.index);switch(n.lanes=0,n.tag){case 16:t:{var o=n.pendingProps;if(e=Ds(n.elementType),n.type=e,typeof e=="function")tf(e)?(o=Os(e,o),n.tag=1,n=tg(null,n,e,o,a)):(n.tag=0,n=kf(null,n,e,o,a));else{if(e!=null){var u=e.$$typeof;if(u===C){n.tag=11,n=Y0(null,n,e,o,a);break t}else if(u===U){n.tag=14,n=j0(null,n,e,o,a);break t}}throw n=gt(e)||e,Error(s(306,n,""))}}return n;case 0:return kf(e,n,n.type,n.pendingProps,a);case 1:return o=n.type,u=Os(o,n.pendingProps),tg(e,n,o,u,a);case 3:t:{if(St(n,n.stateNode.containerInfo),e===null)throw Error(s(387));o=n.pendingProps;var h=n.memoizedState;u=h.element,gf(e,n),Co(n,o,null,a);var y=n.memoizedState;if(o=y.cache,Fa(n,_n,o),o!==h.cache&&uf(n,[_n],a,!0),wo(),o=y.element,h.isDehydrated)if(h={element:o,isDehydrated:!1,cache:y.cache},n.updateQueue.baseState=h,n.memoizedState=h,n.flags&256){n=eg(e,n,o,a);break t}else if(o!==u){u=yi(Error(s(424)),n),Mo(u),n=eg(e,n,o,a);break t}else{switch(e=n.stateNode.containerInfo,e.nodeType){case 9:e=e.body;break;default:e=e.nodeName==="HTML"?e.ownerDocument.body:e}for(tn=Ti(e.firstChild),Nn=n,Te=!0,Ba=null,Ei=!0,a=Km(n,null,o,a),n.child=a;a;)a.flags=a.flags&-3|4096,a=a.sibling}else{if(As(),o===u){n=ua(e,n,a);break t}On(e,n,o,a)}n=n.child}return n;case 26:return ic(e,n),e===null?(a=g_(n.type,null,n.pendingProps,null))?n.memoizedState=a:Te||(a=n.type,e=n.pendingProps,o=yc(nt.current).createElement(a),o[mn]=n,o[Un]=e,Pn(o,a,e),gn(o),n.stateNode=o):n.memoizedState=g_(n.type,e.memoizedProps,n.pendingProps,e.memoizedState),null;case 27:return Jt(n),e===null&&Te&&(o=n.stateNode=d_(n.type,n.pendingProps,nt.current),Nn=n,Ei=!0,u=tn,Ja(n.type)?(Ch=u,tn=Ti(o.firstChild)):tn=u),On(e,n,n.pendingProps.children,a),ic(e,n),e===null&&(n.flags|=4194304),n.child;case 5:return e===null&&Te&&((u=o=tn)&&(o=LS(o,n.type,n.pendingProps,Ei),o!==null?(n.stateNode=o,Nn=n,tn=Ti(o.firstChild),Ei=!1,u=!0):u=!1),u||Ia(n)),Jt(n),u=n.type,h=n.pendingProps,y=e!==null?e.memoizedProps:null,o=h.children,bh(u,h)?o=null:y!==null&&bh(u,y)&&(n.flags|=32),n.memoizedState!==null&&(u=Ef(e,n,jy,null,null,a),Zo._currentValue=u),ic(e,n),On(e,n,o,a),n.child;case 6:return e===null&&Te&&((e=a=tn)&&(a=OS(a,n.pendingProps,Ei),a!==null?(n.stateNode=a,Nn=n,tn=null,e=!0):e=!1),e||Ia(n)),null;case 13:return ng(e,n,a);case 4:return St(n,n.stateNode.containerInfo),o=n.pendingProps,e===null?n.child=Ns(n,null,o,a):On(e,n,o,a),n.child;case 11:return Y0(e,n,n.type,n.pendingProps,a);case 7:return On(e,n,n.pendingProps,a),n.child;case 8:return On(e,n,n.pendingProps.children,a),n.child;case 12:return On(e,n,n.pendingProps.children,a),n.child;case 10:return o=n.pendingProps,Fa(n,n.type,o.value),On(e,n,o.children,a),n.child;case 9:return u=n.type._context,o=n.pendingProps.children,ws(n),u=Ln(u),o=o(u),n.flags|=1,On(e,n,o,a),n.child;case 14:return j0(e,n,n.type,n.pendingProps,a);case 15:return Z0(e,n,n.type,n.pendingProps,a);case 19:return ag(e,n,a);case 31:return nS(e,n,a);case 22:return K0(e,n,a,n.pendingProps);case 24:return ws(n),o=Ln(_n),e===null?(u=df(),u===null&&(u=Ze,h=ff(),u.pooledCache=h,h.refCount++,h!==null&&(u.pooledCacheLanes|=a),u=h),n.memoizedState={parent:o,cache:u},mf(n),Fa(n,_n,u)):((e.lanes&a)!==0&&(gf(e,n),Co(n,null,null,a),wo()),u=e.memoizedState,h=n.memoizedState,u.parent!==o?(u={parent:o,cache:o},n.memoizedState=u,n.lanes===0&&(n.memoizedState=n.updateQueue.baseState=u),Fa(n,_n,o)):(o=h.cache,Fa(n,_n,o),o!==u.cache&&uf(n,[_n],a,!0))),On(e,n,n.pendingProps.children,a),n.child;case 29:throw n.pendingProps}throw Error(s(156,n.tag))}function fa(e){e.flags|=4}function Qf(e,n,a,o,u){if((n=(e.mode&32)!==0)&&(n=!1),n){if(e.flags|=16777216,(u&335544128)===u)if(e.stateNode.complete)e.flags|=8192;else if(Ng())e.flags|=8192;else throw Us=Vl,pf}else e.flags&=-16777217}function rg(e,n){if(n.type!=="stylesheet"||(n.state.loading&4)!==0)e.flags&=-16777217;else if(e.flags|=16777216,!S_(n))if(Ng())e.flags|=8192;else throw Us=Vl,pf}function sc(e,n){n!==null&&(e.flags|=4),e.flags&16384&&(n=e.tag!==22?Tt():536870912,e.lanes|=n,Mr|=n)}function Po(e,n){if(!Te)switch(e.tailMode){case"hidden":n=e.tail;for(var a=null;n!==null;)n.alternate!==null&&(a=n),n=n.sibling;a===null?e.tail=null:a.sibling=null;break;case"collapsed":a=e.tail;for(var o=null;a!==null;)a.alternate!==null&&(o=a),a=a.sibling;o===null?n||e.tail===null?e.tail=null:e.tail.sibling=null:o.sibling=null}}function en(e){var n=e.alternate!==null&&e.alternate.child===e.child,a=0,o=0;if(n)for(var u=e.child;u!==null;)a|=u.lanes|u.childLanes,o|=u.subtreeFlags&65011712,o|=u.flags&65011712,u.return=e,u=u.sibling;else for(u=e.child;u!==null;)a|=u.lanes|u.childLanes,o|=u.subtreeFlags,o|=u.flags,u.return=e,u=u.sibling;return e.subtreeFlags|=o,e.childLanes=a,n}function aS(e,n,a){var o=n.pendingProps;switch(sf(n),n.tag){case 16:case 15:case 0:case 11:case 7:case 8:case 12:case 9:case 14:return en(n),null;case 1:return en(n),null;case 3:return a=n.stateNode,o=null,e!==null&&(o=e.memoizedState.cache),n.memoizedState.cache!==o&&(n.flags|=2048),oa(_n),Ut(),a.pendingContext&&(a.context=a.pendingContext,a.pendingContext=null),(e===null||e.child===null)&&(cr(n)?fa(n):e===null||e.memoizedState.isDehydrated&&(n.flags&256)===0||(n.flags|=1024,of())),en(n),null;case 26:var u=n.type,h=n.memoizedState;return e===null?(fa(n),h!==null?(en(n),rg(n,h)):(en(n),Qf(n,u,null,o,a))):h?h!==e.memoizedState?(fa(n),en(n),rg(n,h)):(en(n),n.flags&=-16777217):(e=e.memoizedProps,e!==o&&fa(n),en(n),Qf(n,u,e,o,a)),null;case 27:if(Kt(n),a=nt.current,u=n.type,e!==null&&n.stateNode!=null)e.memoizedProps!==o&&fa(n);else{if(!o){if(n.stateNode===null)throw Error(s(166));return en(n),null}e=At.current,cr(n)?Fm(n):(e=d_(u,o,a),n.stateNode=e,fa(n))}return en(n),null;case 5:if(Kt(n),u=n.type,e!==null&&n.stateNode!=null)e.memoizedProps!==o&&fa(n);else{if(!o){if(n.stateNode===null)throw Error(s(166));return en(n),null}if(h=At.current,cr(n))Fm(n);else{var y=yc(nt.current);switch(h){case 1:h=y.createElementNS("http://www.w3.org/2000/svg",u);break;case 2:h=y.createElementNS("http://www.w3.org/1998/Math/MathML",u);break;default:switch(u){case"svg":h=y.createElementNS("http://www.w3.org/2000/svg",u);break;case"math":h=y.createElementNS("http://www.w3.org/1998/Math/MathML",u);break;case"script":h=y.createElement("div"),h.innerHTML="<script><\/script>",h=h.removeChild(h.firstChild);break;case"select":h=typeof o.is=="string"?y.createElement("select",{is:o.is}):y.createElement("select"),o.multiple?h.multiple=!0:o.size&&(h.size=o.size);break;default:h=typeof o.is=="string"?y.createElement(u,{is:o.is}):y.createElement(u)}}h[mn]=n,h[Un]=o;t:for(y=n.child;y!==null;){if(y.tag===5||y.tag===6)h.appendChild(y.stateNode);else if(y.tag!==4&&y.tag!==27&&y.child!==null){y.child.return=y,y=y.child;continue}if(y===n)break t;for(;y.sibling===null;){if(y.return===null||y.return===n)break t;y=y.return}y.sibling.return=y.return,y=y.sibling}n.stateNode=h;t:switch(Pn(h,u,o),u){case"button":case"input":case"select":case"textarea":o=!!o.autoFocus;break t;case"img":o=!0;break t;default:o=!1}o&&fa(n)}}return en(n),Qf(n,n.type,e===null?null:e.memoizedProps,n.pendingProps,a),null;case 6:if(e&&n.stateNode!=null)e.memoizedProps!==o&&fa(n);else{if(typeof o!="string"&&n.stateNode===null)throw Error(s(166));if(e=nt.current,cr(n)){if(e=n.stateNode,a=n.memoizedProps,o=null,u=Nn,u!==null)switch(u.tag){case 27:case 5:o=u.memoizedProps}e[mn]=n,e=!!(e.nodeValue===a||o!==null&&o.suppressHydrationWarning===!0||n_(e.nodeValue,a)),e||Ia(n,!0)}else e=yc(e).createTextNode(o),e[mn]=n,n.stateNode=e}return en(n),null;case 31:if(a=n.memoizedState,e===null||e.memoizedState!==null){if(o=cr(n),a!==null){if(e===null){if(!o)throw Error(s(318));if(e=n.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(s(557));e[mn]=n}else As(),(n.flags&128)===0&&(n.memoizedState=null),n.flags|=4;en(n),e=!1}else a=of(),e!==null&&e.memoizedState!==null&&(e.memoizedState.hydrationErrors=a),e=!0;if(!e)return n.flags&256?(li(n),n):(li(n),null);if((n.flags&128)!==0)throw Error(s(558))}return en(n),null;case 13:if(o=n.memoizedState,e===null||e.memoizedState!==null&&e.memoizedState.dehydrated!==null){if(u=cr(n),o!==null&&o.dehydrated!==null){if(e===null){if(!u)throw Error(s(318));if(u=n.memoizedState,u=u!==null?u.dehydrated:null,!u)throw Error(s(317));u[mn]=n}else As(),(n.flags&128)===0&&(n.memoizedState=null),n.flags|=4;en(n),u=!1}else u=of(),e!==null&&e.memoizedState!==null&&(e.memoizedState.hydrationErrors=u),u=!0;if(!u)return n.flags&256?(li(n),n):(li(n),null)}return li(n),(n.flags&128)!==0?(n.lanes=a,n):(a=o!==null,e=e!==null&&e.memoizedState!==null,a&&(o=n.child,u=null,o.alternate!==null&&o.alternate.memoizedState!==null&&o.alternate.memoizedState.cachePool!==null&&(u=o.alternate.memoizedState.cachePool.pool),h=null,o.memoizedState!==null&&o.memoizedState.cachePool!==null&&(h=o.memoizedState.cachePool.pool),h!==u&&(o.flags|=2048)),a!==e&&a&&(n.child.flags|=8192),sc(n,n.updateQueue),en(n),null);case 4:return Ut(),e===null&&xh(n.stateNode.containerInfo),en(n),null;case 10:return oa(n.type),en(n),null;case 19:if(W(dn),o=n.memoizedState,o===null)return en(n),null;if(u=(n.flags&128)!==0,h=o.rendering,h===null)if(u)Po(o,!1);else{if(fn!==0||e!==null&&(e.flags&128)!==0)for(e=n.child;e!==null;){if(h=ql(e),h!==null){for(n.flags|=128,Po(o,!1),e=h.updateQueue,n.updateQueue=e,sc(n,e),n.subtreeFlags=0,e=a,a=n.child;a!==null;)Om(a,e),a=a.sibling;return yt(dn,dn.current&1|2),Te&&sa(n,o.treeForkCount),n.child}e=e.sibling}o.tail!==null&&Le()>uc&&(n.flags|=128,u=!0,Po(o,!1),n.lanes=4194304)}else{if(!u)if(e=ql(h),e!==null){if(n.flags|=128,u=!0,e=e.updateQueue,n.updateQueue=e,sc(n,e),Po(o,!0),o.tail===null&&o.tailMode==="hidden"&&!h.alternate&&!Te)return en(n),null}else 2*Le()-o.renderingStartTime>uc&&a!==536870912&&(n.flags|=128,u=!0,Po(o,!1),n.lanes=4194304);o.isBackwards?(h.sibling=n.child,n.child=h):(e=o.last,e!==null?e.sibling=h:n.child=h,o.last=h)}return o.tail!==null?(e=o.tail,o.rendering=e,o.tail=e.sibling,o.renderingStartTime=Le(),e.sibling=null,a=dn.current,yt(dn,u?a&1|2:a&1),Te&&sa(n,o.treeForkCount),e):(en(n),null);case 22:case 23:return li(n),yf(),o=n.memoizedState!==null,e!==null?e.memoizedState!==null!==o&&(n.flags|=8192):o&&(n.flags|=8192),o?(a&536870912)!==0&&(n.flags&128)===0&&(en(n),n.subtreeFlags&6&&(n.flags|=8192)):en(n),a=n.updateQueue,a!==null&&sc(n,a.retryQueue),a=null,e!==null&&e.memoizedState!==null&&e.memoizedState.cachePool!==null&&(a=e.memoizedState.cachePool.pool),o=null,n.memoizedState!==null&&n.memoizedState.cachePool!==null&&(o=n.memoizedState.cachePool.pool),o!==a&&(n.flags|=2048),e!==null&&W(Cs),null;case 24:return a=null,e!==null&&(a=e.memoizedState.cache),n.memoizedState.cache!==a&&(n.flags|=2048),oa(_n),en(n),null;case 25:return null;case 30:return null}throw Error(s(156,n.tag))}function sS(e,n){switch(sf(n),n.tag){case 1:return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 3:return oa(_n),Ut(),e=n.flags,(e&65536)!==0&&(e&128)===0?(n.flags=e&-65537|128,n):null;case 26:case 27:case 5:return Kt(n),null;case 31:if(n.memoizedState!==null){if(li(n),n.alternate===null)throw Error(s(340));As()}return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 13:if(li(n),e=n.memoizedState,e!==null&&e.dehydrated!==null){if(n.alternate===null)throw Error(s(340));As()}return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 19:return W(dn),null;case 4:return Ut(),null;case 10:return oa(n.type),null;case 22:case 23:return li(n),yf(),e!==null&&W(Cs),e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 24:return oa(_n),null;case 25:return null;default:return null}}function og(e,n){switch(sf(n),n.tag){case 3:oa(_n),Ut();break;case 26:case 27:case 5:Kt(n);break;case 4:Ut();break;case 31:n.memoizedState!==null&&li(n);break;case 13:li(n);break;case 19:W(dn);break;case 10:oa(n.type);break;case 22:case 23:li(n),yf(),e!==null&&W(Cs);break;case 24:oa(_n)}}function zo(e,n){try{var a=n.updateQueue,o=a!==null?a.lastEffect:null;if(o!==null){var u=o.next;a=u;do{if((a.tag&e)===e){o=void 0;var h=a.create,y=a.inst;o=h(),y.destroy=o}a=a.next}while(a!==u)}}catch(R){Ve(n,n.return,R)}}function Wa(e,n,a){try{var o=n.updateQueue,u=o!==null?o.lastEffect:null;if(u!==null){var h=u.next;o=h;do{if((o.tag&e)===e){var y=o.inst,R=y.destroy;if(R!==void 0){y.destroy=void 0,u=n;var G=a,at=R;try{at()}catch(mt){Ve(u,G,mt)}}}o=o.next}while(o!==h)}}catch(mt){Ve(n,n.return,mt)}}function lg(e){var n=e.updateQueue;if(n!==null){var a=e.stateNode;try{Jm(n,a)}catch(o){Ve(e,e.return,o)}}}function cg(e,n,a){a.props=Os(e.type,e.memoizedProps),a.state=e.memoizedState;try{a.componentWillUnmount()}catch(o){Ve(e,n,o)}}function Bo(e,n){try{var a=e.ref;if(a!==null){switch(e.tag){case 26:case 27:case 5:var o=e.stateNode;break;case 30:o=e.stateNode;break;default:o=e.stateNode}typeof a=="function"?e.refCleanup=a(o):a.current=o}}catch(u){Ve(e,n,u)}}function Xi(e,n){var a=e.ref,o=e.refCleanup;if(a!==null)if(typeof o=="function")try{o()}catch(u){Ve(e,n,u)}finally{e.refCleanup=null,e=e.alternate,e!=null&&(e.refCleanup=null)}else if(typeof a=="function")try{a(null)}catch(u){Ve(e,n,u)}else a.current=null}function ug(e){var n=e.type,a=e.memoizedProps,o=e.stateNode;try{t:switch(n){case"button":case"input":case"select":case"textarea":a.autoFocus&&o.focus();break t;case"img":a.src?o.src=a.src:a.srcSet&&(o.srcset=a.srcSet)}}catch(u){Ve(e,e.return,u)}}function Jf(e,n,a){try{var o=e.stateNode;RS(o,e.type,a,n),o[Un]=n}catch(u){Ve(e,e.return,u)}}function fg(e){return e.tag===5||e.tag===3||e.tag===26||e.tag===27&&Ja(e.type)||e.tag===4}function $f(e){t:for(;;){for(;e.sibling===null;){if(e.return===null||fg(e.return))return null;e=e.return}for(e.sibling.return=e.return,e=e.sibling;e.tag!==5&&e.tag!==6&&e.tag!==18;){if(e.tag===27&&Ja(e.type)||e.flags&2||e.child===null||e.tag===4)continue t;e.child.return=e,e=e.child}if(!(e.flags&2))return e.stateNode}}function th(e,n,a){var o=e.tag;if(o===5||o===6)e=e.stateNode,n?(a.nodeType===9?a.body:a.nodeName==="HTML"?a.ownerDocument.body:a).insertBefore(e,n):(n=a.nodeType===9?a.body:a.nodeName==="HTML"?a.ownerDocument.body:a,n.appendChild(e),a=a._reactRootContainer,a!=null||n.onclick!==null||(n.onclick=na));else if(o!==4&&(o===27&&Ja(e.type)&&(a=e.stateNode,n=null),e=e.child,e!==null))for(th(e,n,a),e=e.sibling;e!==null;)th(e,n,a),e=e.sibling}function rc(e,n,a){var o=e.tag;if(o===5||o===6)e=e.stateNode,n?a.insertBefore(e,n):a.appendChild(e);else if(o!==4&&(o===27&&Ja(e.type)&&(a=e.stateNode),e=e.child,e!==null))for(rc(e,n,a),e=e.sibling;e!==null;)rc(e,n,a),e=e.sibling}function hg(e){var n=e.stateNode,a=e.memoizedProps;try{for(var o=e.type,u=n.attributes;u.length;)n.removeAttributeNode(u[0]);Pn(n,o,a),n[mn]=e,n[Un]=a}catch(h){Ve(e,e.return,h)}}var ha=!1,yn=!1,eh=!1,dg=typeof WeakSet=="function"?WeakSet:Set,wn=null;function rS(e,n){if(e=e.containerInfo,Mh=Rc,e=Tm(e),Yu(e)){if("selectionStart"in e)var a={start:e.selectionStart,end:e.selectionEnd};else t:{a=(a=e.ownerDocument)&&a.defaultView||window;var o=a.getSelection&&a.getSelection();if(o&&o.rangeCount!==0){a=o.anchorNode;var u=o.anchorOffset,h=o.focusNode;o=o.focusOffset;try{a.nodeType,h.nodeType}catch{a=null;break t}var y=0,R=-1,G=-1,at=0,mt=0,Mt=e,lt=null;e:for(;;){for(var ft;Mt!==a||u!==0&&Mt.nodeType!==3||(R=y+u),Mt!==h||o!==0&&Mt.nodeType!==3||(G=y+o),Mt.nodeType===3&&(y+=Mt.nodeValue.length),(ft=Mt.firstChild)!==null;)lt=Mt,Mt=ft;for(;;){if(Mt===e)break e;if(lt===a&&++at===u&&(R=y),lt===h&&++mt===o&&(G=y),(ft=Mt.nextSibling)!==null)break;Mt=lt,lt=Mt.parentNode}Mt=ft}a=R===-1||G===-1?null:{start:R,end:G}}else a=null}a=a||{start:0,end:0}}else a=null;for(Eh={focusedElem:e,selectionRange:a},Rc=!1,wn=n;wn!==null;)if(n=wn,e=n.child,(n.subtreeFlags&1028)!==0&&e!==null)e.return=n,wn=e;else for(;wn!==null;){switch(n=wn,h=n.alternate,e=n.flags,n.tag){case 0:if((e&4)!==0&&(e=n.updateQueue,e=e!==null?e.events:null,e!==null))for(a=0;a<e.length;a++)u=e[a],u.ref.impl=u.nextImpl;break;case 11:case 15:break;case 1:if((e&1024)!==0&&h!==null){e=void 0,a=n,u=h.memoizedProps,h=h.memoizedState,o=a.stateNode;try{var qt=Os(a.type,u);e=o.getSnapshotBeforeUpdate(qt,h),o.__reactInternalSnapshotBeforeUpdate=e}catch(ae){Ve(a,a.return,ae)}}break;case 3:if((e&1024)!==0){if(e=n.stateNode.containerInfo,a=e.nodeType,a===9)Ah(e);else if(a===1)switch(e.nodeName){case"HEAD":case"HTML":case"BODY":Ah(e);break;default:e.textContent=""}}break;case 5:case 26:case 27:case 6:case 4:case 17:break;default:if((e&1024)!==0)throw Error(s(163))}if(e=n.sibling,e!==null){e.return=n.return,wn=e;break}wn=n.return}}function pg(e,n,a){var o=a.flags;switch(a.tag){case 0:case 11:case 15:pa(e,a),o&4&&zo(5,a);break;case 1:if(pa(e,a),o&4)if(e=a.stateNode,n===null)try{e.componentDidMount()}catch(y){Ve(a,a.return,y)}else{var u=Os(a.type,n.memoizedProps);n=n.memoizedState;try{e.componentDidUpdate(u,n,e.__reactInternalSnapshotBeforeUpdate)}catch(y){Ve(a,a.return,y)}}o&64&&lg(a),o&512&&Bo(a,a.return);break;case 3:if(pa(e,a),o&64&&(e=a.updateQueue,e!==null)){if(n=null,a.child!==null)switch(a.child.tag){case 27:case 5:n=a.child.stateNode;break;case 1:n=a.child.stateNode}try{Jm(e,n)}catch(y){Ve(a,a.return,y)}}break;case 27:n===null&&o&4&&hg(a);case 26:case 5:pa(e,a),n===null&&o&4&&ug(a),o&512&&Bo(a,a.return);break;case 12:pa(e,a);break;case 31:pa(e,a),o&4&&_g(e,a);break;case 13:pa(e,a),o&4&&vg(e,a),o&64&&(e=a.memoizedState,e!==null&&(e=e.dehydrated,e!==null&&(a=mS.bind(null,a),PS(e,a))));break;case 22:if(o=a.memoizedState!==null||ha,!o){n=n!==null&&n.memoizedState!==null||yn,u=ha;var h=yn;ha=o,(yn=n)&&!h?ma(e,a,(a.subtreeFlags&8772)!==0):pa(e,a),ha=u,yn=h}break;case 30:break;default:pa(e,a)}}function mg(e){var n=e.alternate;n!==null&&(e.alternate=null,mg(n)),e.child=null,e.deletions=null,e.sibling=null,e.tag===5&&(n=e.stateNode,n!==null&&Na(n)),e.stateNode=null,e.return=null,e.dependencies=null,e.memoizedProps=null,e.memoizedState=null,e.pendingProps=null,e.stateNode=null,e.updateQueue=null}var on=null,Zn=!1;function da(e,n,a){for(a=a.child;a!==null;)gg(e,n,a),a=a.sibling}function gg(e,n,a){if(pt&&typeof pt.onCommitFiberUnmount=="function")try{pt.onCommitFiberUnmount(ut,a)}catch{}switch(a.tag){case 26:yn||Xi(a,n),da(e,n,a),a.memoizedState?a.memoizedState.count--:a.stateNode&&(a=a.stateNode,a.parentNode.removeChild(a));break;case 27:yn||Xi(a,n);var o=on,u=Zn;Ja(a.type)&&(on=a.stateNode,Zn=!1),da(e,n,a),qo(a.stateNode),on=o,Zn=u;break;case 5:yn||Xi(a,n);case 6:if(o=on,u=Zn,on=null,da(e,n,a),on=o,Zn=u,on!==null)if(Zn)try{(on.nodeType===9?on.body:on.nodeName==="HTML"?on.ownerDocument.body:on).removeChild(a.stateNode)}catch(h){Ve(a,n,h)}else try{on.removeChild(a.stateNode)}catch(h){Ve(a,n,h)}break;case 18:on!==null&&(Zn?(e=on,l_(e.nodeType===9?e.body:e.nodeName==="HTML"?e.ownerDocument.body:e,a.stateNode),Dr(e)):l_(on,a.stateNode));break;case 4:o=on,u=Zn,on=a.stateNode.containerInfo,Zn=!0,da(e,n,a),on=o,Zn=u;break;case 0:case 11:case 14:case 15:Wa(2,a,n),yn||Wa(4,a,n),da(e,n,a);break;case 1:yn||(Xi(a,n),o=a.stateNode,typeof o.componentWillUnmount=="function"&&cg(a,n,o)),da(e,n,a);break;case 21:da(e,n,a);break;case 22:yn=(o=yn)||a.memoizedState!==null,da(e,n,a),yn=o;break;default:da(e,n,a)}}function _g(e,n){if(n.memoizedState===null&&(e=n.alternate,e!==null&&(e=e.memoizedState,e!==null))){e=e.dehydrated;try{Dr(e)}catch(a){Ve(n,n.return,a)}}}function vg(e,n){if(n.memoizedState===null&&(e=n.alternate,e!==null&&(e=e.memoizedState,e!==null&&(e=e.dehydrated,e!==null))))try{Dr(e)}catch(a){Ve(n,n.return,a)}}function oS(e){switch(e.tag){case 31:case 13:case 19:var n=e.stateNode;return n===null&&(n=e.stateNode=new dg),n;case 22:return e=e.stateNode,n=e._retryCache,n===null&&(n=e._retryCache=new dg),n;default:throw Error(s(435,e.tag))}}function oc(e,n){var a=oS(e);n.forEach(function(o){if(!a.has(o)){a.add(o);var u=gS.bind(null,e,o);o.then(u,u)}})}function Kn(e,n){var a=n.deletions;if(a!==null)for(var o=0;o<a.length;o++){var u=a[o],h=e,y=n,R=y;t:for(;R!==null;){switch(R.tag){case 27:if(Ja(R.type)){on=R.stateNode,Zn=!1;break t}break;case 5:on=R.stateNode,Zn=!1;break t;case 3:case 4:on=R.stateNode.containerInfo,Zn=!0;break t}R=R.return}if(on===null)throw Error(s(160));gg(h,y,u),on=null,Zn=!1,h=u.alternate,h!==null&&(h.return=null),u.return=null}if(n.subtreeFlags&13886)for(n=n.child;n!==null;)xg(n,e),n=n.sibling}var Ni=null;function xg(e,n){var a=e.alternate,o=e.flags;switch(e.tag){case 0:case 11:case 14:case 15:Kn(n,e),Qn(e),o&4&&(Wa(3,e,e.return),zo(3,e),Wa(5,e,e.return));break;case 1:Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),o&64&&ha&&(e=e.updateQueue,e!==null&&(o=e.callbacks,o!==null&&(a=e.shared.hiddenCallbacks,e.shared.hiddenCallbacks=a===null?o:a.concat(o))));break;case 26:var u=Ni;if(Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),o&4){var h=a!==null?a.memoizedState:null;if(o=e.memoizedState,a===null)if(o===null)if(e.stateNode===null){t:{o=e.type,a=e.memoizedProps,u=u.ownerDocument||u;e:switch(o){case"title":h=u.getElementsByTagName("title")[0],(!h||h[Ua]||h[mn]||h.namespaceURI==="http://www.w3.org/2000/svg"||h.hasAttribute("itemprop"))&&(h=u.createElement(o),u.head.insertBefore(h,u.querySelector("head > title"))),Pn(h,o,a),h[mn]=e,gn(h),o=h;break t;case"link":var y=x_("link","href",u).get(o+(a.href||""));if(y){for(var R=0;R<y.length;R++)if(h=y[R],h.getAttribute("href")===(a.href==null||a.href===""?null:a.href)&&h.getAttribute("rel")===(a.rel==null?null:a.rel)&&h.getAttribute("title")===(a.title==null?null:a.title)&&h.getAttribute("crossorigin")===(a.crossOrigin==null?null:a.crossOrigin)){y.splice(R,1);break e}}h=u.createElement(o),Pn(h,o,a),u.head.appendChild(h);break;case"meta":if(y=x_("meta","content",u).get(o+(a.content||""))){for(R=0;R<y.length;R++)if(h=y[R],h.getAttribute("content")===(a.content==null?null:""+a.content)&&h.getAttribute("name")===(a.name==null?null:a.name)&&h.getAttribute("property")===(a.property==null?null:a.property)&&h.getAttribute("http-equiv")===(a.httpEquiv==null?null:a.httpEquiv)&&h.getAttribute("charset")===(a.charSet==null?null:a.charSet)){y.splice(R,1);break e}}h=u.createElement(o),Pn(h,o,a),u.head.appendChild(h);break;default:throw Error(s(468,o))}h[mn]=e,gn(h),o=h}e.stateNode=o}else y_(u,e.type,e.stateNode);else e.stateNode=v_(u,o,e.memoizedProps);else h!==o?(h===null?a.stateNode!==null&&(a=a.stateNode,a.parentNode.removeChild(a)):h.count--,o===null?y_(u,e.type,e.stateNode):v_(u,o,e.memoizedProps)):o===null&&e.stateNode!==null&&Jf(e,e.memoizedProps,a.memoizedProps)}break;case 27:Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),a!==null&&o&4&&Jf(e,e.memoizedProps,a.memoizedProps);break;case 5:if(Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),e.flags&32){u=e.stateNode;try{ai(u,"")}catch(qt){Ve(e,e.return,qt)}}o&4&&e.stateNode!=null&&(u=e.memoizedProps,Jf(e,u,a!==null?a.memoizedProps:u)),o&1024&&(eh=!0);break;case 6:if(Kn(n,e),Qn(e),o&4){if(e.stateNode===null)throw Error(s(162));o=e.memoizedProps,a=e.stateNode;try{a.nodeValue=o}catch(qt){Ve(e,e.return,qt)}}break;case 3:if(Ec=null,u=Ni,Ni=Sc(n.containerInfo),Kn(n,e),Ni=u,Qn(e),o&4&&a!==null&&a.memoizedState.isDehydrated)try{Dr(n.containerInfo)}catch(qt){Ve(e,e.return,qt)}eh&&(eh=!1,yg(e));break;case 4:o=Ni,Ni=Sc(e.stateNode.containerInfo),Kn(n,e),Qn(e),Ni=o;break;case 12:Kn(n,e),Qn(e);break;case 31:Kn(n,e),Qn(e),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,oc(e,o)));break;case 13:Kn(n,e),Qn(e),e.child.flags&8192&&e.memoizedState!==null!=(a!==null&&a.memoizedState!==null)&&(cc=Le()),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,oc(e,o)));break;case 22:u=e.memoizedState!==null;var G=a!==null&&a.memoizedState!==null,at=ha,mt=yn;if(ha=at||u,yn=mt||G,Kn(n,e),yn=mt,ha=at,Qn(e),o&8192)t:for(n=e.stateNode,n._visibility=u?n._visibility&-2:n._visibility|1,u&&(a===null||G||ha||yn||Ps(e)),a=null,n=e;;){if(n.tag===5||n.tag===26){if(a===null){G=a=n;try{if(h=G.stateNode,u)y=h.style,typeof y.setProperty=="function"?y.setProperty("display","none","important"):y.display="none";else{R=G.stateNode;var Mt=G.memoizedProps.style,lt=Mt!=null&&Mt.hasOwnProperty("display")?Mt.display:null;R.style.display=lt==null||typeof lt=="boolean"?"":(""+lt).trim()}}catch(qt){Ve(G,G.return,qt)}}}else if(n.tag===6){if(a===null){G=n;try{G.stateNode.nodeValue=u?"":G.memoizedProps}catch(qt){Ve(G,G.return,qt)}}}else if(n.tag===18){if(a===null){G=n;try{var ft=G.stateNode;u?c_(ft,!0):c_(G.stateNode,!1)}catch(qt){Ve(G,G.return,qt)}}}else if((n.tag!==22&&n.tag!==23||n.memoizedState===null||n===e)&&n.child!==null){n.child.return=n,n=n.child;continue}if(n===e)break t;for(;n.sibling===null;){if(n.return===null||n.return===e)break t;a===n&&(a=null),n=n.return}a===n&&(a=null),n.sibling.return=n.return,n=n.sibling}o&4&&(o=e.updateQueue,o!==null&&(a=o.retryQueue,a!==null&&(o.retryQueue=null,oc(e,a))));break;case 19:Kn(n,e),Qn(e),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,oc(e,o)));break;case 30:break;case 21:break;default:Kn(n,e),Qn(e)}}function Qn(e){var n=e.flags;if(n&2){try{for(var a,o=e.return;o!==null;){if(fg(o)){a=o;break}o=o.return}if(a==null)throw Error(s(160));switch(a.tag){case 27:var u=a.stateNode,h=$f(e);rc(e,h,u);break;case 5:var y=a.stateNode;a.flags&32&&(ai(y,""),a.flags&=-33);var R=$f(e);rc(e,R,y);break;case 3:case 4:var G=a.stateNode.containerInfo,at=$f(e);th(e,at,G);break;default:throw Error(s(161))}}catch(mt){Ve(e,e.return,mt)}e.flags&=-3}n&4096&&(e.flags&=-4097)}function yg(e){if(e.subtreeFlags&1024)for(e=e.child;e!==null;){var n=e;yg(n),n.tag===5&&n.flags&1024&&n.stateNode.reset(),e=e.sibling}}function pa(e,n){if(n.subtreeFlags&8772)for(n=n.child;n!==null;)pg(e,n.alternate,n),n=n.sibling}function Ps(e){for(e=e.child;e!==null;){var n=e;switch(n.tag){case 0:case 11:case 14:case 15:Wa(4,n,n.return),Ps(n);break;case 1:Xi(n,n.return);var a=n.stateNode;typeof a.componentWillUnmount=="function"&&cg(n,n.return,a),Ps(n);break;case 27:qo(n.stateNode);case 26:case 5:Xi(n,n.return),Ps(n);break;case 22:n.memoizedState===null&&Ps(n);break;case 30:Ps(n);break;default:Ps(n)}e=e.sibling}}function ma(e,n,a){for(a=a&&(n.subtreeFlags&8772)!==0,n=n.child;n!==null;){var o=n.alternate,u=e,h=n,y=h.flags;switch(h.tag){case 0:case 11:case 15:ma(u,h,a),zo(4,h);break;case 1:if(ma(u,h,a),o=h,u=o.stateNode,typeof u.componentDidMount=="function")try{u.componentDidMount()}catch(at){Ve(o,o.return,at)}if(o=h,u=o.updateQueue,u!==null){var R=o.stateNode;try{var G=u.shared.hiddenCallbacks;if(G!==null)for(u.shared.hiddenCallbacks=null,u=0;u<G.length;u++)Qm(G[u],R)}catch(at){Ve(o,o.return,at)}}a&&y&64&&lg(h),Bo(h,h.return);break;case 27:hg(h);case 26:case 5:ma(u,h,a),a&&o===null&&y&4&&ug(h),Bo(h,h.return);break;case 12:ma(u,h,a);break;case 31:ma(u,h,a),a&&y&4&&_g(u,h);break;case 13:ma(u,h,a),a&&y&4&&vg(u,h);break;case 22:h.memoizedState===null&&ma(u,h,a),Bo(h,h.return);break;case 30:break;default:ma(u,h,a)}n=n.sibling}}function nh(e,n){var a=null;e!==null&&e.memoizedState!==null&&e.memoizedState.cachePool!==null&&(a=e.memoizedState.cachePool.pool),e=null,n.memoizedState!==null&&n.memoizedState.cachePool!==null&&(e=n.memoizedState.cachePool.pool),e!==a&&(e!=null&&e.refCount++,a!=null&&Eo(a))}function ih(e,n){e=null,n.alternate!==null&&(e=n.alternate.memoizedState.cache),n=n.memoizedState.cache,n!==e&&(n.refCount++,e!=null&&Eo(e))}function Li(e,n,a,o){if(n.subtreeFlags&10256)for(n=n.child;n!==null;)Sg(e,n,a,o),n=n.sibling}function Sg(e,n,a,o){var u=n.flags;switch(n.tag){case 0:case 11:case 15:Li(e,n,a,o),u&2048&&zo(9,n);break;case 1:Li(e,n,a,o);break;case 3:Li(e,n,a,o),u&2048&&(e=null,n.alternate!==null&&(e=n.alternate.memoizedState.cache),n=n.memoizedState.cache,n!==e&&(n.refCount++,e!=null&&Eo(e)));break;case 12:if(u&2048){Li(e,n,a,o),e=n.stateNode;try{var h=n.memoizedProps,y=h.id,R=h.onPostCommit;typeof R=="function"&&R(y,n.alternate===null?"mount":"update",e.passiveEffectDuration,-0)}catch(G){Ve(n,n.return,G)}}else Li(e,n,a,o);break;case 31:Li(e,n,a,o);break;case 13:Li(e,n,a,o);break;case 23:break;case 22:h=n.stateNode,y=n.alternate,n.memoizedState!==null?h._visibility&2?Li(e,n,a,o):Io(e,n):h._visibility&2?Li(e,n,a,o):(h._visibility|=2,xr(e,n,a,o,(n.subtreeFlags&10256)!==0||!1)),u&2048&&nh(y,n);break;case 24:Li(e,n,a,o),u&2048&&ih(n.alternate,n);break;default:Li(e,n,a,o)}}function xr(e,n,a,o,u){for(u=u&&((n.subtreeFlags&10256)!==0||!1),n=n.child;n!==null;){var h=e,y=n,R=a,G=o,at=y.flags;switch(y.tag){case 0:case 11:case 15:xr(h,y,R,G,u),zo(8,y);break;case 23:break;case 22:var mt=y.stateNode;y.memoizedState!==null?mt._visibility&2?xr(h,y,R,G,u):Io(h,y):(mt._visibility|=2,xr(h,y,R,G,u)),u&&at&2048&&nh(y.alternate,y);break;case 24:xr(h,y,R,G,u),u&&at&2048&&ih(y.alternate,y);break;default:xr(h,y,R,G,u)}n=n.sibling}}function Io(e,n){if(n.subtreeFlags&10256)for(n=n.child;n!==null;){var a=e,o=n,u=o.flags;switch(o.tag){case 22:Io(a,o),u&2048&&nh(o.alternate,o);break;case 24:Io(a,o),u&2048&&ih(o.alternate,o);break;default:Io(a,o)}n=n.sibling}}var Fo=8192;function yr(e,n,a){if(e.subtreeFlags&Fo)for(e=e.child;e!==null;)Mg(e,n,a),e=e.sibling}function Mg(e,n,a){switch(e.tag){case 26:yr(e,n,a),e.flags&Fo&&e.memoizedState!==null&&YS(a,Ni,e.memoizedState,e.memoizedProps);break;case 5:yr(e,n,a);break;case 3:case 4:var o=Ni;Ni=Sc(e.stateNode.containerInfo),yr(e,n,a),Ni=o;break;case 22:e.memoizedState===null&&(o=e.alternate,o!==null&&o.memoizedState!==null?(o=Fo,Fo=16777216,yr(e,n,a),Fo=o):yr(e,n,a));break;default:yr(e,n,a)}}function Eg(e){var n=e.alternate;if(n!==null&&(e=n.child,e!==null)){n.child=null;do n=e.sibling,e.sibling=null,e=n;while(e!==null)}}function Ho(e){var n=e.deletions;if((e.flags&16)!==0){if(n!==null)for(var a=0;a<n.length;a++){var o=n[a];wn=o,Tg(o,e)}Eg(e)}if(e.subtreeFlags&10256)for(e=e.child;e!==null;)bg(e),e=e.sibling}function bg(e){switch(e.tag){case 0:case 11:case 15:Ho(e),e.flags&2048&&Wa(9,e,e.return);break;case 3:Ho(e);break;case 12:Ho(e);break;case 22:var n=e.stateNode;e.memoizedState!==null&&n._visibility&2&&(e.return===null||e.return.tag!==13)?(n._visibility&=-3,lc(e)):Ho(e);break;default:Ho(e)}}function lc(e){var n=e.deletions;if((e.flags&16)!==0){if(n!==null)for(var a=0;a<n.length;a++){var o=n[a];wn=o,Tg(o,e)}Eg(e)}for(e=e.child;e!==null;){switch(n=e,n.tag){case 0:case 11:case 15:Wa(8,n,n.return),lc(n);break;case 22:a=n.stateNode,a._visibility&2&&(a._visibility&=-3,lc(n));break;default:lc(n)}e=e.sibling}}function Tg(e,n){for(;wn!==null;){var a=wn;switch(a.tag){case 0:case 11:case 15:Wa(8,a,n);break;case 23:case 22:if(a.memoizedState!==null&&a.memoizedState.cachePool!==null){var o=a.memoizedState.cachePool.pool;o!=null&&o.refCount++}break;case 24:Eo(a.memoizedState.cache)}if(o=a.child,o!==null)o.return=a,wn=o;else t:for(a=e;wn!==null;){o=wn;var u=o.sibling,h=o.return;if(mg(o),o===a){wn=null;break t}if(u!==null){u.return=h,wn=u;break t}wn=h}}}var lS={getCacheForType:function(e){var n=Ln(_n),a=n.data.get(e);return a===void 0&&(a=e(),n.data.set(e,a)),a},cacheSignal:function(){return Ln(_n).controller.signal}},cS=typeof WeakMap=="function"?WeakMap:Map,Oe=0,Ze=null,ye=null,Ee=0,Ge=0,ci=null,qa=!1,Sr=!1,ah=!1,ga=0,fn=0,Ya=0,zs=0,sh=0,ui=0,Mr=0,Go=null,Jn=null,rh=!1,cc=0,Ag=0,uc=1/0,fc=null,ja=null,Tn=0,Za=null,Er=null,_a=0,oh=0,lh=null,Rg=null,Vo=0,ch=null;function fi(){return(Oe&2)!==0&&Ee!==0?Ee&-Ee:I.T!==null?mh():uo()}function wg(){if(ui===0)if((Ee&536870912)===0||Te){var e=re;re<<=1,(re&3932160)===0&&(re=262144),ui=e}else ui=536870912;return e=oi.current,e!==null&&(e.flags|=32),ui}function $n(e,n,a){(e===Ze&&(Ge===2||Ge===9)||e.cancelPendingCommit!==null)&&(br(e,0),Ka(e,Ee,ui,!1)),kt(e,a),((Oe&2)===0||e!==Ze)&&(e===Ze&&((Oe&2)===0&&(zs|=a),fn===4&&Ka(e,Ee,ui,!1)),Wi(e))}function Cg(e,n,a){if((Oe&6)!==0)throw Error(s(327));var o=!a&&(n&127)===0&&(n&e.expiredLanes)===0||Nt(e,n),u=o?hS(e,n):fh(e,n,!0),h=o;do{if(u===0){Sr&&!o&&Ka(e,n,0,!1);break}else{if(a=e.current.alternate,h&&!uS(a)){u=fh(e,n,!1),h=!1;continue}if(u===2){if(h=n,e.errorRecoveryDisabledLanes&h)var y=0;else y=e.pendingLanes&-536870913,y=y!==0?y:y&536870912?536870912:0;if(y!==0){n=y;t:{var R=e;u=Go;var G=R.current.memoizedState.isDehydrated;if(G&&(br(R,y).flags|=256),y=fh(R,y,!1),y!==2){if(ah&&!G){R.errorRecoveryDisabledLanes|=h,zs|=h,u=4;break t}h=Jn,Jn=u,h!==null&&(Jn===null?Jn=h:Jn.push.apply(Jn,h))}u=y}if(h=!1,u!==2)continue}}if(u===1){br(e,0),Ka(e,n,0,!0);break}t:{switch(o=e,h=u,h){case 0:case 1:throw Error(s(345));case 4:if((n&4194048)!==n)break;case 6:Ka(o,n,ui,!qa);break t;case 2:Jn=null;break;case 3:case 5:break;default:throw Error(s(329))}if((n&62914560)===n&&(u=cc+300-Le(),10<u)){if(Ka(o,n,ui,!qa),_t(o,0,!0)!==0)break t;_a=n,o.timeoutHandle=r_(Dg.bind(null,o,a,Jn,fc,rh,n,ui,zs,Mr,qa,h,"Throttled",-0,0),u);break t}Dg(o,a,Jn,fc,rh,n,ui,zs,Mr,qa,h,null,-0,0)}}break}while(!0);Wi(e)}function Dg(e,n,a,o,u,h,y,R,G,at,mt,Mt,lt,ft){if(e.timeoutHandle=-1,Mt=n.subtreeFlags,Mt&8192||(Mt&16785408)===16785408){Mt={stylesheets:null,count:0,imgCount:0,imgBytes:0,suspenseyImages:[],waitingForImages:!0,waitingForViewTransition:!1,unsuspend:na},Mg(n,h,Mt);var qt=(h&62914560)===h?cc-Le():(h&4194048)===h?Ag-Le():0;if(qt=jS(Mt,qt),qt!==null){_a=h,e.cancelPendingCommit=qt(Ig.bind(null,e,n,h,a,o,u,y,R,G,mt,Mt,null,lt,ft)),Ka(e,h,y,!at);return}}Ig(e,n,h,a,o,u,y,R,G)}function uS(e){for(var n=e;;){var a=n.tag;if((a===0||a===11||a===15)&&n.flags&16384&&(a=n.updateQueue,a!==null&&(a=a.stores,a!==null)))for(var o=0;o<a.length;o++){var u=a[o],h=u.getSnapshot;u=u.value;try{if(!si(h(),u))return!1}catch{return!1}}if(a=n.child,n.subtreeFlags&16384&&a!==null)a.return=n,n=a;else{if(n===e)break;for(;n.sibling===null;){if(n.return===null||n.return===e)return!0;n=n.return}n.sibling.return=n.return,n=n.sibling}}return!0}function Ka(e,n,a,o){n&=~sh,n&=~zs,e.suspendedLanes|=n,e.pingedLanes&=~n,o&&(e.warmLanes|=n),o=e.expirationTimes;for(var u=n;0<u;){var h=31-Ft(u),y=1<<h;o[h]=-1,u&=~y}a!==0&&Be(e,a,n)}function hc(){return(Oe&6)===0?(ko(0),!1):!0}function uh(){if(ye!==null){if(Ge===0)var e=ye.return;else e=ye,ra=Rs=null,Af(e),pr=null,To=0,e=ye;for(;e!==null;)og(e.alternate,e),e=e.return;ye=null}}function br(e,n){var a=e.timeoutHandle;a!==-1&&(e.timeoutHandle=-1,DS(a)),a=e.cancelPendingCommit,a!==null&&(e.cancelPendingCommit=null,a()),_a=0,uh(),Ze=e,ye=a=aa(e.current,null),Ee=n,Ge=0,ci=null,qa=!1,Sr=Nt(e,n),ah=!1,Mr=ui=sh=zs=Ya=fn=0,Jn=Go=null,rh=!1,(n&8)!==0&&(n|=n&32);var o=e.entangledLanes;if(o!==0)for(e=e.entanglements,o&=n;0<o;){var u=31-Ft(o),h=1<<u;n|=e[u],o&=~h}return ga=n,Ll(),a}function Ug(e,n){he=null,I.H=Lo,n===dr||n===Gl?(n=Ym(),Ge=3):n===pf?(n=Ym(),Ge=4):Ge=n===Vf?8:n!==null&&typeof n=="object"&&typeof n.then=="function"?6:1,ci=n,ye===null&&(fn=1,ec(e,yi(n,e.current)))}function Ng(){var e=oi.current;return e===null?!0:(Ee&4194048)===Ee?bi===null:(Ee&62914560)===Ee||(Ee&536870912)!==0?e===bi:!1}function Lg(){var e=I.H;return I.H=Lo,e===null?Lo:e}function Og(){var e=I.A;return I.A=lS,e}function dc(){fn=4,qa||(Ee&4194048)!==Ee&&oi.current!==null||(Sr=!0),(Ya&134217727)===0&&(zs&134217727)===0||Ze===null||Ka(Ze,Ee,ui,!1)}function fh(e,n,a){var o=Oe;Oe|=2;var u=Lg(),h=Og();(Ze!==e||Ee!==n)&&(fc=null,br(e,n)),n=!1;var y=fn;t:do try{if(Ge!==0&&ye!==null){var R=ye,G=ci;switch(Ge){case 8:uh(),y=6;break t;case 3:case 2:case 9:case 6:oi.current===null&&(n=!0);var at=Ge;if(Ge=0,ci=null,Tr(e,R,G,at),a&&Sr){y=0;break t}break;default:at=Ge,Ge=0,ci=null,Tr(e,R,G,at)}}fS(),y=fn;break}catch(mt){Ug(e,mt)}while(!0);return n&&e.shellSuspendCounter++,ra=Rs=null,Oe=o,I.H=u,I.A=h,ye===null&&(Ze=null,Ee=0,Ll()),y}function fS(){for(;ye!==null;)Pg(ye)}function hS(e,n){var a=Oe;Oe|=2;var o=Lg(),u=Og();Ze!==e||Ee!==n?(fc=null,uc=Le()+500,br(e,n)):Sr=Nt(e,n);t:do try{if(Ge!==0&&ye!==null){n=ye;var h=ci;e:switch(Ge){case 1:Ge=0,ci=null,Tr(e,n,h,1);break;case 2:case 9:if(Wm(h)){Ge=0,ci=null,zg(n);break}n=function(){Ge!==2&&Ge!==9||Ze!==e||(Ge=7),Wi(e)},h.then(n,n);break t;case 3:Ge=7;break t;case 4:Ge=5;break t;case 7:Wm(h)?(Ge=0,ci=null,zg(n)):(Ge=0,ci=null,Tr(e,n,h,7));break;case 5:var y=null;switch(ye.tag){case 26:y=ye.memoizedState;case 5:case 27:var R=ye;if(y?S_(y):R.stateNode.complete){Ge=0,ci=null;var G=R.sibling;if(G!==null)ye=G;else{var at=R.return;at!==null?(ye=at,pc(at)):ye=null}break e}}Ge=0,ci=null,Tr(e,n,h,5);break;case 6:Ge=0,ci=null,Tr(e,n,h,6);break;case 8:uh(),fn=6;break t;default:throw Error(s(462))}}dS();break}catch(mt){Ug(e,mt)}while(!0);return ra=Rs=null,I.H=o,I.A=u,Oe=a,ye!==null?0:(Ze=null,Ee=0,Ll(),fn)}function dS(){for(;ye!==null&&!$e();)Pg(ye)}function Pg(e){var n=sg(e.alternate,e,ga);e.memoizedProps=e.pendingProps,n===null?pc(e):ye=n}function zg(e){var n=e,a=n.alternate;switch(n.tag){case 15:case 0:n=$0(a,n,n.pendingProps,n.type,void 0,Ee);break;case 11:n=$0(a,n,n.pendingProps,n.type.render,n.ref,Ee);break;case 5:Af(n);default:og(a,n),n=ye=Om(n,ga),n=sg(a,n,ga)}e.memoizedProps=e.pendingProps,n===null?pc(e):ye=n}function Tr(e,n,a,o){ra=Rs=null,Af(n),pr=null,To=0;var u=n.return;try{if(eS(e,u,n,a,Ee)){fn=1,ec(e,yi(a,e.current)),ye=null;return}}catch(h){if(u!==null)throw ye=u,h;fn=1,ec(e,yi(a,e.current)),ye=null;return}n.flags&32768?(Te||o===1?e=!0:Sr||(Ee&536870912)!==0?e=!1:(qa=e=!0,(o===2||o===9||o===3||o===6)&&(o=oi.current,o!==null&&o.tag===13&&(o.flags|=16384))),Bg(n,e)):pc(n)}function pc(e){var n=e;do{if((n.flags&32768)!==0){Bg(n,qa);return}e=n.return;var a=aS(n.alternate,n,ga);if(a!==null){ye=a;return}if(n=n.sibling,n!==null){ye=n;return}ye=n=e}while(n!==null);fn===0&&(fn=5)}function Bg(e,n){do{var a=sS(e.alternate,e);if(a!==null){a.flags&=32767,ye=a;return}if(a=e.return,a!==null&&(a.flags|=32768,a.subtreeFlags=0,a.deletions=null),!n&&(e=e.sibling,e!==null)){ye=e;return}ye=e=a}while(e!==null);fn=6,ye=null}function Ig(e,n,a,o,u,h,y,R,G){e.cancelPendingCommit=null;do mc();while(Tn!==0);if((Oe&6)!==0)throw Error(s(327));if(n!==null){if(n===e.current)throw Error(s(177));if(h=n.lanes|n.childLanes,h|=Ju,an(e,a,h,y,R,G),e===Ze&&(ye=Ze=null,Ee=0),Er=n,Za=e,_a=a,oh=h,lh=u,Rg=o,(n.subtreeFlags&10256)!==0||(n.flags&10256)!==0?(e.callbackNode=null,e.callbackPriority=0,_S(X,function(){return kg(),null})):(e.callbackNode=null,e.callbackPriority=0),o=(n.flags&13878)!==0,(n.subtreeFlags&13878)!==0||o){o=I.T,I.T=null,u=H.p,H.p=2,y=Oe,Oe|=4;try{rS(e,n,a)}finally{Oe=y,H.p=u,I.T=o}}Tn=1,Fg(),Hg(),Gg()}}function Fg(){if(Tn===1){Tn=0;var e=Za,n=Er,a=(n.flags&13878)!==0;if((n.subtreeFlags&13878)!==0||a){a=I.T,I.T=null;var o=H.p;H.p=2;var u=Oe;Oe|=4;try{xg(n,e);var h=Eh,y=Tm(e.containerInfo),R=h.focusedElem,G=h.selectionRange;if(y!==R&&R&&R.ownerDocument&&bm(R.ownerDocument.documentElement,R)){if(G!==null&&Yu(R)){var at=G.start,mt=G.end;if(mt===void 0&&(mt=at),"selectionStart"in R)R.selectionStart=at,R.selectionEnd=Math.min(mt,R.value.length);else{var Mt=R.ownerDocument||document,lt=Mt&&Mt.defaultView||window;if(lt.getSelection){var ft=lt.getSelection(),qt=R.textContent.length,ae=Math.min(G.start,qt),Ye=G.end===void 0?ae:Math.min(G.end,qt);!ft.extend&&ae>Ye&&(y=Ye,Ye=ae,ae=y);var tt=Em(R,ae),j=Em(R,Ye);if(tt&&j&&(ft.rangeCount!==1||ft.anchorNode!==tt.node||ft.anchorOffset!==tt.offset||ft.focusNode!==j.node||ft.focusOffset!==j.offset)){var it=Mt.createRange();it.setStart(tt.node,tt.offset),ft.removeAllRanges(),ae>Ye?(ft.addRange(it),ft.extend(j.node,j.offset)):(it.setEnd(j.node,j.offset),ft.addRange(it))}}}}for(Mt=[],ft=R;ft=ft.parentNode;)ft.nodeType===1&&Mt.push({element:ft,left:ft.scrollLeft,top:ft.scrollTop});for(typeof R.focus=="function"&&R.focus(),R=0;R<Mt.length;R++){var xt=Mt[R];xt.element.scrollLeft=xt.left,xt.element.scrollTop=xt.top}}Rc=!!Mh,Eh=Mh=null}finally{Oe=u,H.p=o,I.T=a}}e.current=n,Tn=2}}function Hg(){if(Tn===2){Tn=0;var e=Za,n=Er,a=(n.flags&8772)!==0;if((n.subtreeFlags&8772)!==0||a){a=I.T,I.T=null;var o=H.p;H.p=2;var u=Oe;Oe|=4;try{pg(e,n.alternate,n)}finally{Oe=u,H.p=o,I.T=a}}Tn=3}}function Gg(){if(Tn===4||Tn===3){Tn=0,K();var e=Za,n=Er,a=_a,o=Rg;(n.subtreeFlags&10256)!==0||(n.flags&10256)!==0?Tn=5:(Tn=0,Er=Za=null,Vg(e,e.pendingLanes));var u=e.pendingLanes;if(u===0&&(ja=null),co(a),n=n.stateNode,pt&&typeof pt.onCommitFiberRoot=="function")try{pt.onCommitFiberRoot(ut,n,void 0,(n.current.flags&128)===128)}catch{}if(o!==null){n=I.T,u=H.p,H.p=2,I.T=null;try{for(var h=e.onRecoverableError,y=0;y<o.length;y++){var R=o[y];h(R.value,{componentStack:R.stack})}}finally{I.T=n,H.p=u}}(_a&3)!==0&&mc(),Wi(e),u=e.pendingLanes,(a&261930)!==0&&(u&42)!==0?e===ch?Vo++:(Vo=0,ch=e):Vo=0,ko(0)}}function Vg(e,n){(e.pooledCacheLanes&=n)===0&&(n=e.pooledCache,n!=null&&(e.pooledCache=null,Eo(n)))}function mc(){return Fg(),Hg(),Gg(),kg()}function kg(){if(Tn!==5)return!1;var e=Za,n=oh;oh=0;var a=co(_a),o=I.T,u=H.p;try{H.p=32>a?32:a,I.T=null,a=lh,lh=null;var h=Za,y=_a;if(Tn=0,Er=Za=null,_a=0,(Oe&6)!==0)throw Error(s(331));var R=Oe;if(Oe|=4,bg(h.current),Sg(h,h.current,y,a),Oe=R,ko(0,!1),pt&&typeof pt.onPostCommitFiberRoot=="function")try{pt.onPostCommitFiberRoot(ut,h)}catch{}return!0}finally{H.p=u,I.T=o,Vg(e,n)}}function Xg(e,n,a){n=yi(a,n),n=Gf(e.stateNode,n,2),e=Va(e,n,2),e!==null&&(kt(e,2),Wi(e))}function Ve(e,n,a){if(e.tag===3)Xg(e,e,a);else for(;n!==null;){if(n.tag===3){Xg(n,e,a);break}else if(n.tag===1){var o=n.stateNode;if(typeof n.type.getDerivedStateFromError=="function"||typeof o.componentDidCatch=="function"&&(ja===null||!ja.has(o))){e=yi(a,e),a=W0(2),o=Va(n,a,2),o!==null&&(q0(a,o,n,e),kt(o,2),Wi(o));break}}n=n.return}}function hh(e,n,a){var o=e.pingCache;if(o===null){o=e.pingCache=new cS;var u=new Set;o.set(n,u)}else u=o.get(n),u===void 0&&(u=new Set,o.set(n,u));u.has(a)||(ah=!0,u.add(a),e=pS.bind(null,e,n,a),n.then(e,e))}function pS(e,n,a){var o=e.pingCache;o!==null&&o.delete(n),e.pingedLanes|=e.suspendedLanes&a,e.warmLanes&=~a,Ze===e&&(Ee&a)===a&&(fn===4||fn===3&&(Ee&62914560)===Ee&&300>Le()-cc?(Oe&2)===0&&br(e,0):sh|=a,Mr===Ee&&(Mr=0)),Wi(e)}function Wg(e,n){n===0&&(n=Tt()),e=bs(e,n),e!==null&&(kt(e,n),Wi(e))}function mS(e){var n=e.memoizedState,a=0;n!==null&&(a=n.retryLane),Wg(e,a)}function gS(e,n){var a=0;switch(e.tag){case 31:case 13:var o=e.stateNode,u=e.memoizedState;u!==null&&(a=u.retryLane);break;case 19:o=e.stateNode;break;case 22:o=e.stateNode._retryCache;break;default:throw Error(s(314))}o!==null&&o.delete(n),Wg(e,a)}function _S(e,n){return nn(e,n)}var gc=null,Ar=null,dh=!1,_c=!1,ph=!1,Qa=0;function Wi(e){e!==Ar&&e.next===null&&(Ar===null?gc=Ar=e:Ar=Ar.next=e),_c=!0,dh||(dh=!0,xS())}function ko(e,n){if(!ph&&_c){ph=!0;do for(var a=!1,o=gc;o!==null;){if(e!==0){var u=o.pendingLanes;if(u===0)var h=0;else{var y=o.suspendedLanes,R=o.pingedLanes;h=(1<<31-Ft(42|e)+1)-1,h&=u&~(y&~R),h=h&201326741?h&201326741|1:h?h|2:0}h!==0&&(a=!0,Zg(o,h))}else h=Ee,h=_t(o,o===Ze?h:0,o.cancelPendingCommit!==null||o.timeoutHandle!==-1),(h&3)===0||Nt(o,h)||(a=!0,Zg(o,h));o=o.next}while(a);ph=!1}}function vS(){qg()}function qg(){_c=dh=!1;var e=0;Qa!==0&&CS()&&(e=Qa);for(var n=Le(),a=null,o=gc;o!==null;){var u=o.next,h=Yg(o,n);h===0?(o.next=null,a===null?gc=u:a.next=u,u===null&&(Ar=a)):(a=o,(e!==0||(h&3)!==0)&&(_c=!0)),o=u}Tn!==0&&Tn!==5||ko(e),Qa!==0&&(Qa=0)}function Yg(e,n){for(var a=e.suspendedLanes,o=e.pingedLanes,u=e.expirationTimes,h=e.pendingLanes&-62914561;0<h;){var y=31-Ft(h),R=1<<y,G=u[y];G===-1?((R&a)===0||(R&o)!==0)&&(u[y]=Ht(R,n)):G<=n&&(e.expiredLanes|=R),h&=~R}if(n=Ze,a=Ee,a=_t(e,e===n?a:0,e.cancelPendingCommit!==null||e.timeoutHandle!==-1),o=e.callbackNode,a===0||e===n&&(Ge===2||Ge===9)||e.cancelPendingCommit!==null)return o!==null&&o!==null&&ze(o),e.callbackNode=null,e.callbackPriority=0;if((a&3)===0||Nt(e,a)){if(n=a&-a,n===e.callbackPriority)return n;switch(o!==null&&ze(o),co(a)){case 2:case 8:a=M;break;case 32:a=X;break;case 268435456:a=ct;break;default:a=X}return o=jg.bind(null,e),a=nn(a,o),e.callbackPriority=n,e.callbackNode=a,n}return o!==null&&o!==null&&ze(o),e.callbackPriority=2,e.callbackNode=null,2}function jg(e,n){if(Tn!==0&&Tn!==5)return e.callbackNode=null,e.callbackPriority=0,null;var a=e.callbackNode;if(mc()&&e.callbackNode!==a)return null;var o=Ee;return o=_t(e,e===Ze?o:0,e.cancelPendingCommit!==null||e.timeoutHandle!==-1),o===0?null:(Cg(e,o,n),Yg(e,Le()),e.callbackNode!=null&&e.callbackNode===a?jg.bind(null,e):null)}function Zg(e,n){if(mc())return null;Cg(e,n,!0)}function xS(){US(function(){(Oe&6)!==0?nn(D,vS):qg()})}function mh(){if(Qa===0){var e=fr;e===0&&(e=te,te<<=1,(te&261888)===0&&(te=256)),Qa=e}return Qa}function Kg(e){return e==null||typeof e=="symbol"||typeof e=="boolean"?null:typeof e=="function"?e:ys(""+e)}function Qg(e,n){var a=n.ownerDocument.createElement("input");return a.name=n.name,a.value=n.value,e.id&&a.setAttribute("form",e.id),n.parentNode.insertBefore(a,n),e=new FormData(e),a.parentNode.removeChild(a),e}function yS(e,n,a,o,u){if(n==="submit"&&a&&a.stateNode===u){var h=Kg((u[Un]||null).action),y=o.submitter;y&&(n=(n=y[Un]||null)?Kg(n.formAction):y.getAttribute("formAction"),n!==null&&(h=n,y=null));var R=new Cl("action","action",null,o,u);e.push({event:R,listeners:[{instance:null,listener:function(){if(o.defaultPrevented){if(Qa!==0){var G=y?Qg(u,y):new FormData(u);Pf(a,{pending:!0,data:G,method:u.method,action:h},null,G)}}else typeof h=="function"&&(R.preventDefault(),G=y?Qg(u,y):new FormData(u),Pf(a,{pending:!0,data:G,method:u.method,action:h},h,G))},currentTarget:u}]})}}for(var gh=0;gh<Qu.length;gh++){var _h=Qu[gh],SS=_h.toLowerCase(),MS=_h[0].toUpperCase()+_h.slice(1);Ui(SS,"on"+MS)}Ui(wm,"onAnimationEnd"),Ui(Cm,"onAnimationIteration"),Ui(Dm,"onAnimationStart"),Ui("dblclick","onDoubleClick"),Ui("focusin","onFocus"),Ui("focusout","onBlur"),Ui(Iy,"onTransitionRun"),Ui(Fy,"onTransitionStart"),Ui(Hy,"onTransitionCancel"),Ui(Um,"onTransitionEnd"),ot("onMouseEnter",["mouseout","mouseover"]),ot("onMouseLeave",["mouseout","mouseover"]),ot("onPointerEnter",["pointerout","pointerover"]),ot("onPointerLeave",["pointerout","pointerover"]),Q("onChange","change click focusin focusout input keydown keyup selectionchange".split(" ")),Q("onSelect","focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(" ")),Q("onBeforeInput",["compositionend","keypress","textInput","paste"]),Q("onCompositionEnd","compositionend focusout keydown keypress keyup mousedown".split(" ")),Q("onCompositionStart","compositionstart focusout keydown keypress keyup mousedown".split(" ")),Q("onCompositionUpdate","compositionupdate focusout keydown keypress keyup mousedown".split(" "));var Xo="abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(" "),ES=new Set("beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(Xo));function Jg(e,n){n=(n&4)!==0;for(var a=0;a<e.length;a++){var o=e[a],u=o.event;o=o.listeners;t:{var h=void 0;if(n)for(var y=o.length-1;0<=y;y--){var R=o[y],G=R.instance,at=R.currentTarget;if(R=R.listener,G!==h&&u.isPropagationStopped())break t;h=R,u.currentTarget=at;try{h(u)}catch(mt){Nl(mt)}u.currentTarget=null,h=G}else for(y=0;y<o.length;y++){if(R=o[y],G=R.instance,at=R.currentTarget,R=R.listener,G!==h&&u.isPropagationStopped())break t;h=R,u.currentTarget=at;try{h(u)}catch(mt){Nl(mt)}u.currentTarget=null,h=G}}}}function Se(e,n){var a=n[_s];a===void 0&&(a=n[_s]=new Set);var o=e+"__bubble";a.has(o)||($g(n,e,2,!1),a.add(o))}function vh(e,n,a){var o=0;n&&(o|=4),$g(a,e,o,n)}var vc="_reactListening"+Math.random().toString(36).slice(2);function xh(e){if(!e[vc]){e[vc]=!0,Tl.forEach(function(a){a!=="selectionchange"&&(ES.has(a)||vh(a,!1,e),vh(a,!0,e))});var n=e.nodeType===9?e:e.ownerDocument;n===null||n[vc]||(n[vc]=!0,vh("selectionchange",!1,n))}}function $g(e,n,a,o){switch(w_(n)){case 2:var u=QS;break;case 8:u=JS;break;default:u=Oh}a=u.bind(null,n,a,e),u=void 0,!Iu||n!=="touchstart"&&n!=="touchmove"&&n!=="wheel"||(u=!0),o?u!==void 0?e.addEventListener(n,a,{capture:!0,passive:u}):e.addEventListener(n,a,!0):u!==void 0?e.addEventListener(n,a,{passive:u}):e.addEventListener(n,a,!1)}function yh(e,n,a,o,u){var h=o;if((n&1)===0&&(n&2)===0&&o!==null)t:for(;;){if(o===null)return;var y=o.tag;if(y===3||y===4){var R=o.stateNode.containerInfo;if(R===u)break;if(y===4)for(y=o.return;y!==null;){var G=y.tag;if((G===3||G===4)&&y.stateNode.containerInfo===u)return;y=y.return}for(;R!==null;){if(y=ta(R),y===null)return;if(G=y.tag,G===5||G===6||G===26||G===27){o=h=y;continue t}R=R.parentNode}}o=o.return}am(function(){var at=h,mt=zu(a),Mt=[];t:{var lt=Nm.get(e);if(lt!==void 0){var ft=Cl,qt=e;switch(e){case"keypress":if(Rl(a)===0)break t;case"keydown":case"keyup":ft=gy;break;case"focusin":qt="focus",ft=Vu;break;case"focusout":qt="blur",ft=Vu;break;case"beforeblur":case"afterblur":ft=Vu;break;case"click":if(a.button===2)break t;case"auxclick":case"dblclick":case"mousedown":case"mousemove":case"mouseup":case"mouseout":case"mouseover":case"contextmenu":ft=om;break;case"drag":case"dragend":case"dragenter":case"dragexit":case"dragleave":case"dragover":case"dragstart":case"drop":ft=ay;break;case"touchcancel":case"touchend":case"touchmove":case"touchstart":ft=xy;break;case wm:case Cm:case Dm:ft=oy;break;case Um:ft=Sy;break;case"scroll":case"scrollend":ft=ny;break;case"wheel":ft=Ey;break;case"copy":case"cut":case"paste":ft=cy;break;case"gotpointercapture":case"lostpointercapture":case"pointercancel":case"pointerdown":case"pointermove":case"pointerout":case"pointerover":case"pointerup":ft=cm;break;case"toggle":case"beforetoggle":ft=Ty}var ae=(n&4)!==0,Ye=!ae&&(e==="scroll"||e==="scrollend"),tt=ae?lt!==null?lt+"Capture":null:lt;ae=[];for(var j=at,it;j!==null;){var xt=j;if(it=xt.stateNode,xt=xt.tag,xt!==5&&xt!==26&&xt!==27||it===null||tt===null||(xt=fo(j,tt),xt!=null&&ae.push(Wo(j,xt,it))),Ye)break;j=j.return}0<ae.length&&(lt=new ft(lt,qt,null,a,mt),Mt.push({event:lt,listeners:ae}))}}if((n&7)===0){t:{if(lt=e==="mouseover"||e==="pointerover",ft=e==="mouseout"||e==="pointerout",lt&&a!==Pu&&(qt=a.relatedTarget||a.fromElement)&&(ta(qt)||qt[Yn]))break t;if((ft||lt)&&(lt=mt.window===mt?mt:(lt=mt.ownerDocument)?lt.defaultView||lt.parentWindow:window,ft?(qt=a.relatedTarget||a.toElement,ft=at,qt=qt?ta(qt):null,qt!==null&&(Ye=c(qt),ae=qt.tag,qt!==Ye||ae!==5&&ae!==27&&ae!==6)&&(qt=null)):(ft=null,qt=at),ft!==qt)){if(ae=om,xt="onMouseLeave",tt="onMouseEnter",j="mouse",(e==="pointerout"||e==="pointerover")&&(ae=cm,xt="onPointerLeave",tt="onPointerEnter",j="pointer"),Ye=ft==null?lt:xs(ft),it=qt==null?lt:xs(qt),lt=new ae(xt,j+"leave",ft,a,mt),lt.target=Ye,lt.relatedTarget=it,xt=null,ta(mt)===at&&(ae=new ae(tt,j+"enter",qt,a,mt),ae.target=it,ae.relatedTarget=Ye,xt=ae),Ye=xt,ft&&qt)e:{for(ae=bS,tt=ft,j=qt,it=0,xt=tt;xt;xt=ae(xt))it++;xt=0;for(var ne=j;ne;ne=ae(ne))xt++;for(;0<it-xt;)tt=ae(tt),it--;for(;0<xt-it;)j=ae(j),xt--;for(;it--;){if(tt===j||j!==null&&tt===j.alternate){ae=tt;break e}tt=ae(tt),j=ae(j)}ae=null}else ae=null;ft!==null&&t_(Mt,lt,ft,ae,!1),qt!==null&&Ye!==null&&t_(Mt,Ye,qt,ae,!0)}}t:{if(lt=at?xs(at):window,ft=lt.nodeName&&lt.nodeName.toLowerCase(),ft==="select"||ft==="input"&&lt.type==="file")var Ue=_m;else if(mm(lt))if(vm)Ue=Py;else{Ue=Ly;var Zt=Ny}else ft=lt.nodeName,!ft||ft.toLowerCase()!=="input"||lt.type!=="checkbox"&&lt.type!=="radio"?at&&Pe(at.elementType)&&(Ue=_m):Ue=Oy;if(Ue&&(Ue=Ue(e,at))){gm(Mt,Ue,a,mt);break t}Zt&&Zt(e,lt,at),e==="focusout"&&at&&lt.type==="number"&&at.memoizedProps.value!=null&&xe(lt,"number",lt.value)}switch(Zt=at?xs(at):window,e){case"focusin":(mm(Zt)||Zt.contentEditable==="true")&&(ir=Zt,ju=at,yo=null);break;case"focusout":yo=ju=ir=null;break;case"mousedown":Zu=!0;break;case"contextmenu":case"mouseup":case"dragend":Zu=!1,Am(Mt,a,mt);break;case"selectionchange":if(By)break;case"keydown":case"keyup":Am(Mt,a,mt)}var pe;if(Xu)t:{switch(e){case"compositionstart":var be="onCompositionStart";break t;case"compositionend":be="onCompositionEnd";break t;case"compositionupdate":be="onCompositionUpdate";break t}be=void 0}else nr?dm(e,a)&&(be="onCompositionEnd"):e==="keydown"&&a.keyCode===229&&(be="onCompositionStart");be&&(um&&a.locale!=="ko"&&(nr||be!=="onCompositionStart"?be==="onCompositionEnd"&&nr&&(pe=sm()):(Pa=mt,Fu="value"in Pa?Pa.value:Pa.textContent,nr=!0)),Zt=xc(at,be),0<Zt.length&&(be=new lm(be,e,null,a,mt),Mt.push({event:be,listeners:Zt}),pe?be.data=pe:(pe=pm(a),pe!==null&&(be.data=pe)))),(pe=Ry?wy(e,a):Cy(e,a))&&(be=xc(at,"onBeforeInput"),0<be.length&&(Zt=new lm("onBeforeInput","beforeinput",null,a,mt),Mt.push({event:Zt,listeners:be}),Zt.data=pe)),yS(Mt,e,at,a,mt)}Jg(Mt,n)})}function Wo(e,n,a){return{instance:e,listener:n,currentTarget:a}}function xc(e,n){for(var a=n+"Capture",o=[];e!==null;){var u=e,h=u.stateNode;if(u=u.tag,u!==5&&u!==26&&u!==27||h===null||(u=fo(e,a),u!=null&&o.unshift(Wo(e,u,h)),u=fo(e,n),u!=null&&o.push(Wo(e,u,h))),e.tag===3)return o;e=e.return}return[]}function bS(e){if(e===null)return null;do e=e.return;while(e&&e.tag!==5&&e.tag!==27);return e||null}function t_(e,n,a,o,u){for(var h=n._reactName,y=[];a!==null&&a!==o;){var R=a,G=R.alternate,at=R.stateNode;if(R=R.tag,G!==null&&G===o)break;R!==5&&R!==26&&R!==27||at===null||(G=at,u?(at=fo(a,h),at!=null&&y.unshift(Wo(a,at,G))):u||(at=fo(a,h),at!=null&&y.push(Wo(a,at,G)))),a=a.return}y.length!==0&&e.push({event:n,listeners:y})}var TS=/\r\n?/g,AS=/\u0000|\uFFFD/g;function e_(e){return(typeof e=="string"?e:""+e).replace(TS,`
`).replace(AS,"")}function n_(e,n){return n=e_(n),e_(e)===n}function qe(e,n,a,o,u,h){switch(a){case"children":typeof o=="string"?n==="body"||n==="textarea"&&o===""||ai(e,o):(typeof o=="number"||typeof o=="bigint")&&n!=="body"&&ai(e,""+o);break;case"className":Wt(e,"class",o);break;case"tabIndex":Wt(e,"tabindex",o);break;case"dir":case"role":case"viewBox":case"width":case"height":Wt(e,a,o);break;case"style":Di(e,o,h);break;case"data":if(n!=="object"){Wt(e,"data",o);break}case"src":case"href":if(o===""&&(n!=="a"||a!=="href")){e.removeAttribute(a);break}if(o==null||typeof o=="function"||typeof o=="symbol"||typeof o=="boolean"){e.removeAttribute(a);break}o=ys(""+o),e.setAttribute(a,o);break;case"action":case"formAction":if(typeof o=="function"){e.setAttribute(a,"javascript:throw new Error('A React form was unexpectedly submitted. If you called form.submit() manually, consider using form.requestSubmit() instead. If you\\'re trying to use event.stopPropagation() in a submit event handler, consider also calling event.preventDefault().')");break}else typeof h=="function"&&(a==="formAction"?(n!=="input"&&qe(e,n,"name",u.name,u,null),qe(e,n,"formEncType",u.formEncType,u,null),qe(e,n,"formMethod",u.formMethod,u,null),qe(e,n,"formTarget",u.formTarget,u,null)):(qe(e,n,"encType",u.encType,u,null),qe(e,n,"method",u.method,u,null),qe(e,n,"target",u.target,u,null)));if(o==null||typeof o=="symbol"||typeof o=="boolean"){e.removeAttribute(a);break}o=ys(""+o),e.setAttribute(a,o);break;case"onClick":o!=null&&(e.onclick=na);break;case"onScroll":o!=null&&Se("scroll",e);break;case"onScrollEnd":o!=null&&Se("scrollend",e);break;case"dangerouslySetInnerHTML":if(o!=null){if(typeof o!="object"||!("__html"in o))throw Error(s(61));if(a=o.__html,a!=null){if(u.children!=null)throw Error(s(60));e.innerHTML=a}}break;case"multiple":e.multiple=o&&typeof o!="function"&&typeof o!="symbol";break;case"muted":e.muted=o&&typeof o!="function"&&typeof o!="symbol";break;case"suppressContentEditableWarning":case"suppressHydrationWarning":case"defaultValue":case"defaultChecked":case"innerHTML":case"ref":break;case"autoFocus":break;case"xlinkHref":if(o==null||typeof o=="function"||typeof o=="boolean"||typeof o=="symbol"){e.removeAttribute("xlink:href");break}a=ys(""+o),e.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href",a);break;case"contentEditable":case"spellCheck":case"draggable":case"value":case"autoReverse":case"externalResourcesRequired":case"focusable":case"preserveAlpha":o!=null&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,""+o):e.removeAttribute(a);break;case"inert":case"allowFullScreen":case"async":case"autoPlay":case"controls":case"default":case"defer":case"disabled":case"disablePictureInPicture":case"disableRemotePlayback":case"formNoValidate":case"hidden":case"loop":case"noModule":case"noValidate":case"open":case"playsInline":case"readOnly":case"required":case"reversed":case"scoped":case"seamless":case"itemScope":o&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,""):e.removeAttribute(a);break;case"capture":case"download":o===!0?e.setAttribute(a,""):o!==!1&&o!=null&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,o):e.removeAttribute(a);break;case"cols":case"rows":case"size":case"span":o!=null&&typeof o!="function"&&typeof o!="symbol"&&!isNaN(o)&&1<=o?e.setAttribute(a,o):e.removeAttribute(a);break;case"rowSpan":case"start":o==null||typeof o=="function"||typeof o=="symbol"||isNaN(o)?e.removeAttribute(a):e.setAttribute(a,o);break;case"popover":Se("beforetoggle",e),Se("toggle",e),zt(e,"popover",o);break;case"xlinkActuate":Xt(e,"http://www.w3.org/1999/xlink","xlink:actuate",o);break;case"xlinkArcrole":Xt(e,"http://www.w3.org/1999/xlink","xlink:arcrole",o);break;case"xlinkRole":Xt(e,"http://www.w3.org/1999/xlink","xlink:role",o);break;case"xlinkShow":Xt(e,"http://www.w3.org/1999/xlink","xlink:show",o);break;case"xlinkTitle":Xt(e,"http://www.w3.org/1999/xlink","xlink:title",o);break;case"xlinkType":Xt(e,"http://www.w3.org/1999/xlink","xlink:type",o);break;case"xmlBase":Xt(e,"http://www.w3.org/XML/1998/namespace","xml:base",o);break;case"xmlLang":Xt(e,"http://www.w3.org/XML/1998/namespace","xml:lang",o);break;case"xmlSpace":Xt(e,"http://www.w3.org/XML/1998/namespace","xml:space",o);break;case"is":zt(e,"is",o);break;case"innerText":case"textContent":break;default:(!(2<a.length)||a[0]!=="o"&&a[0]!=="O"||a[1]!=="n"&&a[1]!=="N")&&(a=Gi.get(a)||a,zt(e,a,o))}}function Sh(e,n,a,o,u,h){switch(a){case"style":Di(e,o,h);break;case"dangerouslySetInnerHTML":if(o!=null){if(typeof o!="object"||!("__html"in o))throw Error(s(61));if(a=o.__html,a!=null){if(u.children!=null)throw Error(s(60));e.innerHTML=a}}break;case"children":typeof o=="string"?ai(e,o):(typeof o=="number"||typeof o=="bigint")&&ai(e,""+o);break;case"onScroll":o!=null&&Se("scroll",e);break;case"onScrollEnd":o!=null&&Se("scrollend",e);break;case"onClick":o!=null&&(e.onclick=na);break;case"suppressContentEditableWarning":case"suppressHydrationWarning":case"innerHTML":case"ref":break;case"innerText":case"textContent":break;default:if(!A.hasOwnProperty(a))t:{if(a[0]==="o"&&a[1]==="n"&&(u=a.endsWith("Capture"),n=a.slice(2,u?a.length-7:void 0),h=e[Un]||null,h=h!=null?h[a]:null,typeof h=="function"&&e.removeEventListener(n,h,u),typeof o=="function")){typeof h!="function"&&h!==null&&(a in e?e[a]=null:e.hasAttribute(a)&&e.removeAttribute(a)),e.addEventListener(n,o,u);break t}a in e?e[a]=o:o===!0?e.setAttribute(a,""):zt(e,a,o)}}}function Pn(e,n,a){switch(n){case"div":case"span":case"svg":case"path":case"a":case"g":case"p":case"li":break;case"img":Se("error",e),Se("load",e);var o=!1,u=!1,h;for(h in a)if(a.hasOwnProperty(h)){var y=a[h];if(y!=null)switch(h){case"src":o=!0;break;case"srcSet":u=!0;break;case"children":case"dangerouslySetInnerHTML":throw Error(s(137,n));default:qe(e,n,h,y,a,null)}}u&&qe(e,n,"srcSet",a.srcSet,a,null),o&&qe(e,n,"src",a.src,a,null);return;case"input":Se("invalid",e);var R=h=y=u=null,G=null,at=null;for(o in a)if(a.hasOwnProperty(o)){var mt=a[o];if(mt!=null)switch(o){case"name":u=mt;break;case"type":y=mt;break;case"checked":G=mt;break;case"defaultChecked":at=mt;break;case"value":h=mt;break;case"defaultValue":R=mt;break;case"children":case"dangerouslySetInnerHTML":if(mt!=null)throw Error(s(137,n));break;default:qe(e,n,o,mt,a,null)}}Bn(e,h,R,G,at,y,u,!1);return;case"select":Se("invalid",e),o=y=h=null;for(u in a)if(a.hasOwnProperty(u)&&(R=a[u],R!=null))switch(u){case"value":h=R;break;case"defaultValue":y=R;break;case"multiple":o=R;default:qe(e,n,u,R,a,null)}n=h,a=y,e.multiple=!!o,n!=null?bn(e,!!o,n,!1):a!=null&&bn(e,!!o,a,!0);return;case"textarea":Se("invalid",e),h=u=o=null;for(y in a)if(a.hasOwnProperty(y)&&(R=a[y],R!=null))switch(y){case"value":o=R;break;case"defaultValue":u=R;break;case"children":h=R;break;case"dangerouslySetInnerHTML":if(R!=null)throw Error(s(91));break;default:qe(e,n,y,R,a,null)}Ci(e,o,u,h);return;case"option":for(G in a)if(a.hasOwnProperty(G)&&(o=a[G],o!=null))switch(G){case"selected":e.selected=o&&typeof o!="function"&&typeof o!="symbol";break;default:qe(e,n,G,o,a,null)}return;case"dialog":Se("beforetoggle",e),Se("toggle",e),Se("cancel",e),Se("close",e);break;case"iframe":case"object":Se("load",e);break;case"video":case"audio":for(o=0;o<Xo.length;o++)Se(Xo[o],e);break;case"image":Se("error",e),Se("load",e);break;case"details":Se("toggle",e);break;case"embed":case"source":case"link":Se("error",e),Se("load",e);case"area":case"base":case"br":case"col":case"hr":case"keygen":case"meta":case"param":case"track":case"wbr":case"menuitem":for(at in a)if(a.hasOwnProperty(at)&&(o=a[at],o!=null))switch(at){case"children":case"dangerouslySetInnerHTML":throw Error(s(137,n));default:qe(e,n,at,o,a,null)}return;default:if(Pe(n)){for(mt in a)a.hasOwnProperty(mt)&&(o=a[mt],o!==void 0&&Sh(e,n,mt,o,a,void 0));return}}for(R in a)a.hasOwnProperty(R)&&(o=a[R],o!=null&&qe(e,n,R,o,a,null))}function RS(e,n,a,o){switch(n){case"div":case"span":case"svg":case"path":case"a":case"g":case"p":case"li":break;case"input":var u=null,h=null,y=null,R=null,G=null,at=null,mt=null;for(ft in a){var Mt=a[ft];if(a.hasOwnProperty(ft)&&Mt!=null)switch(ft){case"checked":break;case"value":break;case"defaultValue":G=Mt;default:o.hasOwnProperty(ft)||qe(e,n,ft,null,o,Mt)}}for(var lt in o){var ft=o[lt];if(Mt=a[lt],o.hasOwnProperty(lt)&&(ft!=null||Mt!=null))switch(lt){case"type":h=ft;break;case"name":u=ft;break;case"checked":at=ft;break;case"defaultChecked":mt=ft;break;case"value":y=ft;break;case"defaultValue":R=ft;break;case"children":case"dangerouslySetInnerHTML":if(ft!=null)throw Error(s(137,n));break;default:ft!==Mt&&qe(e,n,lt,ft,o,Mt)}}Gt(e,y,R,G,at,mt,h,u);return;case"select":ft=y=R=lt=null;for(h in a)if(G=a[h],a.hasOwnProperty(h)&&G!=null)switch(h){case"value":break;case"multiple":ft=G;default:o.hasOwnProperty(h)||qe(e,n,h,null,o,G)}for(u in o)if(h=o[u],G=a[u],o.hasOwnProperty(u)&&(h!=null||G!=null))switch(u){case"value":lt=h;break;case"defaultValue":R=h;break;case"multiple":y=h;default:h!==G&&qe(e,n,u,h,o,G)}n=R,a=y,o=ft,lt!=null?bn(e,!!a,lt,!1):!!o!=!!a&&(n!=null?bn(e,!!a,n,!0):bn(e,!!a,a?[]:"",!1));return;case"textarea":ft=lt=null;for(R in a)if(u=a[R],a.hasOwnProperty(R)&&u!=null&&!o.hasOwnProperty(R))switch(R){case"value":break;case"children":break;default:qe(e,n,R,null,o,u)}for(y in o)if(u=o[y],h=a[y],o.hasOwnProperty(y)&&(u!=null||h!=null))switch(y){case"value":lt=u;break;case"defaultValue":ft=u;break;case"children":break;case"dangerouslySetInnerHTML":if(u!=null)throw Error(s(91));break;default:u!==h&&qe(e,n,y,u,o,h)}ii(e,lt,ft);return;case"option":for(var qt in a)if(lt=a[qt],a.hasOwnProperty(qt)&&lt!=null&&!o.hasOwnProperty(qt))switch(qt){case"selected":e.selected=!1;break;default:qe(e,n,qt,null,o,lt)}for(G in o)if(lt=o[G],ft=a[G],o.hasOwnProperty(G)&&lt!==ft&&(lt!=null||ft!=null))switch(G){case"selected":e.selected=lt&&typeof lt!="function"&&typeof lt!="symbol";break;default:qe(e,n,G,lt,o,ft)}return;case"img":case"link":case"area":case"base":case"br":case"col":case"embed":case"hr":case"keygen":case"meta":case"param":case"source":case"track":case"wbr":case"menuitem":for(var ae in a)lt=a[ae],a.hasOwnProperty(ae)&&lt!=null&&!o.hasOwnProperty(ae)&&qe(e,n,ae,null,o,lt);for(at in o)if(lt=o[at],ft=a[at],o.hasOwnProperty(at)&&lt!==ft&&(lt!=null||ft!=null))switch(at){case"children":case"dangerouslySetInnerHTML":if(lt!=null)throw Error(s(137,n));break;default:qe(e,n,at,lt,o,ft)}return;default:if(Pe(n)){for(var Ye in a)lt=a[Ye],a.hasOwnProperty(Ye)&&lt!==void 0&&!o.hasOwnProperty(Ye)&&Sh(e,n,Ye,void 0,o,lt);for(mt in o)lt=o[mt],ft=a[mt],!o.hasOwnProperty(mt)||lt===ft||lt===void 0&&ft===void 0||Sh(e,n,mt,lt,o,ft);return}}for(var tt in a)lt=a[tt],a.hasOwnProperty(tt)&&lt!=null&&!o.hasOwnProperty(tt)&&qe(e,n,tt,null,o,lt);for(Mt in o)lt=o[Mt],ft=a[Mt],!o.hasOwnProperty(Mt)||lt===ft||lt==null&&ft==null||qe(e,n,Mt,lt,o,ft)}function i_(e){switch(e){case"css":case"script":case"font":case"img":case"image":case"input":case"link":return!0;default:return!1}}function wS(){if(typeof performance.getEntriesByType=="function"){for(var e=0,n=0,a=performance.getEntriesByType("resource"),o=0;o<a.length;o++){var u=a[o],h=u.transferSize,y=u.initiatorType,R=u.duration;if(h&&R&&i_(y)){for(y=0,R=u.responseEnd,o+=1;o<a.length;o++){var G=a[o],at=G.startTime;if(at>R)break;var mt=G.transferSize,Mt=G.initiatorType;mt&&i_(Mt)&&(G=G.responseEnd,y+=mt*(G<R?1:(R-at)/(G-at)))}if(--o,n+=8*(h+y)/(u.duration/1e3),e++,10<e)break}}if(0<e)return n/e/1e6}return navigator.connection&&(e=navigator.connection.downlink,typeof e=="number")?e:5}var Mh=null,Eh=null;function yc(e){return e.nodeType===9?e:e.ownerDocument}function a_(e){switch(e){case"http://www.w3.org/2000/svg":return 1;case"http://www.w3.org/1998/Math/MathML":return 2;default:return 0}}function s_(e,n){if(e===0)switch(n){case"svg":return 1;case"math":return 2;default:return 0}return e===1&&n==="foreignObject"?0:e}function bh(e,n){return e==="textarea"||e==="noscript"||typeof n.children=="string"||typeof n.children=="number"||typeof n.children=="bigint"||typeof n.dangerouslySetInnerHTML=="object"&&n.dangerouslySetInnerHTML!==null&&n.dangerouslySetInnerHTML.__html!=null}var Th=null;function CS(){var e=window.event;return e&&e.type==="popstate"?e===Th?!1:(Th=e,!0):(Th=null,!1)}var r_=typeof setTimeout=="function"?setTimeout:void 0,DS=typeof clearTimeout=="function"?clearTimeout:void 0,o_=typeof Promise=="function"?Promise:void 0,US=typeof queueMicrotask=="function"?queueMicrotask:typeof o_<"u"?function(e){return o_.resolve(null).then(e).catch(NS)}:r_;function NS(e){setTimeout(function(){throw e})}function Ja(e){return e==="head"}function l_(e,n){var a=n,o=0;do{var u=a.nextSibling;if(e.removeChild(a),u&&u.nodeType===8)if(a=u.data,a==="/$"||a==="/&"){if(o===0){e.removeChild(u),Dr(n);return}o--}else if(a==="$"||a==="$?"||a==="$~"||a==="$!"||a==="&")o++;else if(a==="html")qo(e.ownerDocument.documentElement);else if(a==="head"){a=e.ownerDocument.head,qo(a);for(var h=a.firstChild;h;){var y=h.nextSibling,R=h.nodeName;h[Ua]||R==="SCRIPT"||R==="STYLE"||R==="LINK"&&h.rel.toLowerCase()==="stylesheet"||a.removeChild(h),h=y}}else a==="body"&&qo(e.ownerDocument.body);a=u}while(a);Dr(n)}function c_(e,n){var a=e;e=0;do{var o=a.nextSibling;if(a.nodeType===1?n?(a._stashedDisplay=a.style.display,a.style.display="none"):(a.style.display=a._stashedDisplay||"",a.getAttribute("style")===""&&a.removeAttribute("style")):a.nodeType===3&&(n?(a._stashedText=a.nodeValue,a.nodeValue=""):a.nodeValue=a._stashedText||""),o&&o.nodeType===8)if(a=o.data,a==="/$"){if(e===0)break;e--}else a!=="$"&&a!=="$?"&&a!=="$~"&&a!=="$!"||e++;a=o}while(a)}function Ah(e){var n=e.firstChild;for(n&&n.nodeType===10&&(n=n.nextSibling);n;){var a=n;switch(n=n.nextSibling,a.nodeName){case"HTML":case"HEAD":case"BODY":Ah(a),Na(a);continue;case"SCRIPT":case"STYLE":continue;case"LINK":if(a.rel.toLowerCase()==="stylesheet")continue}e.removeChild(a)}}function LS(e,n,a,o){for(;e.nodeType===1;){var u=a;if(e.nodeName.toLowerCase()!==n.toLowerCase()){if(!o&&(e.nodeName!=="INPUT"||e.type!=="hidden"))break}else if(o){if(!e[Ua])switch(n){case"meta":if(!e.hasAttribute("itemprop"))break;return e;case"link":if(h=e.getAttribute("rel"),h==="stylesheet"&&e.hasAttribute("data-precedence"))break;if(h!==u.rel||e.getAttribute("href")!==(u.href==null||u.href===""?null:u.href)||e.getAttribute("crossorigin")!==(u.crossOrigin==null?null:u.crossOrigin)||e.getAttribute("title")!==(u.title==null?null:u.title))break;return e;case"style":if(e.hasAttribute("data-precedence"))break;return e;case"script":if(h=e.getAttribute("src"),(h!==(u.src==null?null:u.src)||e.getAttribute("type")!==(u.type==null?null:u.type)||e.getAttribute("crossorigin")!==(u.crossOrigin==null?null:u.crossOrigin))&&h&&e.hasAttribute("async")&&!e.hasAttribute("itemprop"))break;return e;default:return e}}else if(n==="input"&&e.type==="hidden"){var h=u.name==null?null:""+u.name;if(u.type==="hidden"&&e.getAttribute("name")===h)return e}else return e;if(e=Ti(e.nextSibling),e===null)break}return null}function OS(e,n,a){if(n==="")return null;for(;e.nodeType!==3;)if((e.nodeType!==1||e.nodeName!=="INPUT"||e.type!=="hidden")&&!a||(e=Ti(e.nextSibling),e===null))return null;return e}function u_(e,n){for(;e.nodeType!==8;)if((e.nodeType!==1||e.nodeName!=="INPUT"||e.type!=="hidden")&&!n||(e=Ti(e.nextSibling),e===null))return null;return e}function Rh(e){return e.data==="$?"||e.data==="$~"}function wh(e){return e.data==="$!"||e.data==="$?"&&e.ownerDocument.readyState!=="loading"}function PS(e,n){var a=e.ownerDocument;if(e.data==="$~")e._reactRetry=n;else if(e.data!=="$?"||a.readyState!=="loading")n();else{var o=function(){n(),a.removeEventListener("DOMContentLoaded",o)};a.addEventListener("DOMContentLoaded",o),e._reactRetry=o}}function Ti(e){for(;e!=null;e=e.nextSibling){var n=e.nodeType;if(n===1||n===3)break;if(n===8){if(n=e.data,n==="$"||n==="$!"||n==="$?"||n==="$~"||n==="&"||n==="F!"||n==="F")break;if(n==="/$"||n==="/&")return null}}return e}var Ch=null;function f_(e){e=e.nextSibling;for(var n=0;e;){if(e.nodeType===8){var a=e.data;if(a==="/$"||a==="/&"){if(n===0)return Ti(e.nextSibling);n--}else a!=="$"&&a!=="$!"&&a!=="$?"&&a!=="$~"&&a!=="&"||n++}e=e.nextSibling}return null}function h_(e){e=e.previousSibling;for(var n=0;e;){if(e.nodeType===8){var a=e.data;if(a==="$"||a==="$!"||a==="$?"||a==="$~"||a==="&"){if(n===0)return e;n--}else a!=="/$"&&a!=="/&"||n++}e=e.previousSibling}return null}function d_(e,n,a){switch(n=yc(a),e){case"html":if(e=n.documentElement,!e)throw Error(s(452));return e;case"head":if(e=n.head,!e)throw Error(s(453));return e;case"body":if(e=n.body,!e)throw Error(s(454));return e;default:throw Error(s(451))}}function qo(e){for(var n=e.attributes;n.length;)e.removeAttributeNode(n[0]);Na(e)}var Ai=new Map,p_=new Set;function Sc(e){return typeof e.getRootNode=="function"?e.getRootNode():e.nodeType===9?e:e.ownerDocument}var va=H.d;H.d={f:zS,r:BS,D:IS,C:FS,L:HS,m:GS,X:kS,S:VS,M:XS};function zS(){var e=va.f(),n=hc();return e||n}function BS(e){var n=ea(e);n!==null&&n.tag===5&&n.type==="form"?U0(n):va.r(e)}var Rr=typeof document>"u"?null:document;function m_(e,n,a){var o=Rr;if(o&&typeof n=="string"&&n){var u=Fe(n);u='link[rel="'+e+'"][href="'+u+'"]',typeof a=="string"&&(u+='[crossorigin="'+a+'"]'),p_.has(u)||(p_.add(u),e={rel:e,crossOrigin:a,href:n},o.querySelector(u)===null&&(n=o.createElement("link"),Pn(n,"link",e),gn(n),o.head.appendChild(n)))}}function IS(e){va.D(e),m_("dns-prefetch",e,null)}function FS(e,n){va.C(e,n),m_("preconnect",e,n)}function HS(e,n,a){va.L(e,n,a);var o=Rr;if(o&&e&&n){var u='link[rel="preload"][as="'+Fe(n)+'"]';n==="image"&&a&&a.imageSrcSet?(u+='[imagesrcset="'+Fe(a.imageSrcSet)+'"]',typeof a.imageSizes=="string"&&(u+='[imagesizes="'+Fe(a.imageSizes)+'"]')):u+='[href="'+Fe(e)+'"]';var h=u;switch(n){case"style":h=wr(e);break;case"script":h=Cr(e)}Ai.has(h)||(e=v({rel:"preload",href:n==="image"&&a&&a.imageSrcSet?void 0:e,as:n},a),Ai.set(h,e),o.querySelector(u)!==null||n==="style"&&o.querySelector(Yo(h))||n==="script"&&o.querySelector(jo(h))||(n=o.createElement("link"),Pn(n,"link",e),gn(n),o.head.appendChild(n)))}}function GS(e,n){va.m(e,n);var a=Rr;if(a&&e){var o=n&&typeof n.as=="string"?n.as:"script",u='link[rel="modulepreload"][as="'+Fe(o)+'"][href="'+Fe(e)+'"]',h=u;switch(o){case"audioworklet":case"paintworklet":case"serviceworker":case"sharedworker":case"worker":case"script":h=Cr(e)}if(!Ai.has(h)&&(e=v({rel:"modulepreload",href:e},n),Ai.set(h,e),a.querySelector(u)===null)){switch(o){case"audioworklet":case"paintworklet":case"serviceworker":case"sharedworker":case"worker":case"script":if(a.querySelector(jo(h)))return}o=a.createElement("link"),Pn(o,"link",e),gn(o),a.head.appendChild(o)}}}function VS(e,n,a){va.S(e,n,a);var o=Rr;if(o&&e){var u=La(o).hoistableStyles,h=wr(e);n=n||"default";var y=u.get(h);if(!y){var R={loading:0,preload:null};if(y=o.querySelector(Yo(h)))R.loading=5;else{e=v({rel:"stylesheet",href:e,"data-precedence":n},a),(a=Ai.get(h))&&Dh(e,a);var G=y=o.createElement("link");gn(G),Pn(G,"link",e),G._p=new Promise(function(at,mt){G.onload=at,G.onerror=mt}),G.addEventListener("load",function(){R.loading|=1}),G.addEventListener("error",function(){R.loading|=2}),R.loading|=4,Mc(y,n,o)}y={type:"stylesheet",instance:y,count:1,state:R},u.set(h,y)}}}function kS(e,n){va.X(e,n);var a=Rr;if(a&&e){var o=La(a).hoistableScripts,u=Cr(e),h=o.get(u);h||(h=a.querySelector(jo(u)),h||(e=v({src:e,async:!0},n),(n=Ai.get(u))&&Uh(e,n),h=a.createElement("script"),gn(h),Pn(h,"link",e),a.head.appendChild(h)),h={type:"script",instance:h,count:1,state:null},o.set(u,h))}}function XS(e,n){va.M(e,n);var a=Rr;if(a&&e){var o=La(a).hoistableScripts,u=Cr(e),h=o.get(u);h||(h=a.querySelector(jo(u)),h||(e=v({src:e,async:!0,type:"module"},n),(n=Ai.get(u))&&Uh(e,n),h=a.createElement("script"),gn(h),Pn(h,"link",e),a.head.appendChild(h)),h={type:"script",instance:h,count:1,state:null},o.set(u,h))}}function g_(e,n,a,o){var u=(u=nt.current)?Sc(u):null;if(!u)throw Error(s(446));switch(e){case"meta":case"title":return null;case"style":return typeof a.precedence=="string"&&typeof a.href=="string"?(n=wr(a.href),a=La(u).hoistableStyles,o=a.get(n),o||(o={type:"style",instance:null,count:0,state:null},a.set(n,o)),o):{type:"void",instance:null,count:0,state:null};case"link":if(a.rel==="stylesheet"&&typeof a.href=="string"&&typeof a.precedence=="string"){e=wr(a.href);var h=La(u).hoistableStyles,y=h.get(e);if(y||(u=u.ownerDocument||u,y={type:"stylesheet",instance:null,count:0,state:{loading:0,preload:null}},h.set(e,y),(h=u.querySelector(Yo(e)))&&!h._p&&(y.instance=h,y.state.loading=5),Ai.has(e)||(a={rel:"preload",as:"style",href:a.href,crossOrigin:a.crossOrigin,integrity:a.integrity,media:a.media,hrefLang:a.hrefLang,referrerPolicy:a.referrerPolicy},Ai.set(e,a),h||WS(u,e,a,y.state))),n&&o===null)throw Error(s(528,""));return y}if(n&&o!==null)throw Error(s(529,""));return null;case"script":return n=a.async,a=a.src,typeof a=="string"&&n&&typeof n!="function"&&typeof n!="symbol"?(n=Cr(a),a=La(u).hoistableScripts,o=a.get(n),o||(o={type:"script",instance:null,count:0,state:null},a.set(n,o)),o):{type:"void",instance:null,count:0,state:null};default:throw Error(s(444,e))}}function wr(e){return'href="'+Fe(e)+'"'}function Yo(e){return'link[rel="stylesheet"]['+e+"]"}function __(e){return v({},e,{"data-precedence":e.precedence,precedence:null})}function WS(e,n,a,o){e.querySelector('link[rel="preload"][as="style"]['+n+"]")?o.loading=1:(n=e.createElement("link"),o.preload=n,n.addEventListener("load",function(){return o.loading|=1}),n.addEventListener("error",function(){return o.loading|=2}),Pn(n,"link",a),gn(n),e.head.appendChild(n))}function Cr(e){return'[src="'+Fe(e)+'"]'}function jo(e){return"script[async]"+e}function v_(e,n,a){if(n.count++,n.instance===null)switch(n.type){case"style":var o=e.querySelector('style[data-href~="'+Fe(a.href)+'"]');if(o)return n.instance=o,gn(o),o;var u=v({},a,{"data-href":a.href,"data-precedence":a.precedence,href:null,precedence:null});return o=(e.ownerDocument||e).createElement("style"),gn(o),Pn(o,"style",u),Mc(o,a.precedence,e),n.instance=o;case"stylesheet":u=wr(a.href);var h=e.querySelector(Yo(u));if(h)return n.state.loading|=4,n.instance=h,gn(h),h;o=__(a),(u=Ai.get(u))&&Dh(o,u),h=(e.ownerDocument||e).createElement("link"),gn(h);var y=h;return y._p=new Promise(function(R,G){y.onload=R,y.onerror=G}),Pn(h,"link",o),n.state.loading|=4,Mc(h,a.precedence,e),n.instance=h;case"script":return h=Cr(a.src),(u=e.querySelector(jo(h)))?(n.instance=u,gn(u),u):(o=a,(u=Ai.get(h))&&(o=v({},a),Uh(o,u)),e=e.ownerDocument||e,u=e.createElement("script"),gn(u),Pn(u,"link",o),e.head.appendChild(u),n.instance=u);case"void":return null;default:throw Error(s(443,n.type))}else n.type==="stylesheet"&&(n.state.loading&4)===0&&(o=n.instance,n.state.loading|=4,Mc(o,a.precedence,e));return n.instance}function Mc(e,n,a){for(var o=a.querySelectorAll('link[rel="stylesheet"][data-precedence],style[data-precedence]'),u=o.length?o[o.length-1]:null,h=u,y=0;y<o.length;y++){var R=o[y];if(R.dataset.precedence===n)h=R;else if(h!==u)break}h?h.parentNode.insertBefore(e,h.nextSibling):(n=a.nodeType===9?a.head:a,n.insertBefore(e,n.firstChild))}function Dh(e,n){e.crossOrigin==null&&(e.crossOrigin=n.crossOrigin),e.referrerPolicy==null&&(e.referrerPolicy=n.referrerPolicy),e.title==null&&(e.title=n.title)}function Uh(e,n){e.crossOrigin==null&&(e.crossOrigin=n.crossOrigin),e.referrerPolicy==null&&(e.referrerPolicy=n.referrerPolicy),e.integrity==null&&(e.integrity=n.integrity)}var Ec=null;function x_(e,n,a){if(Ec===null){var o=new Map,u=Ec=new Map;u.set(a,o)}else u=Ec,o=u.get(a),o||(o=new Map,u.set(a,o));if(o.has(e))return o;for(o.set(e,null),a=a.getElementsByTagName(e),u=0;u<a.length;u++){var h=a[u];if(!(h[Ua]||h[mn]||e==="link"&&h.getAttribute("rel")==="stylesheet")&&h.namespaceURI!=="http://www.w3.org/2000/svg"){var y=h.getAttribute(n)||"";y=e+y;var R=o.get(y);R?R.push(h):o.set(y,[h])}}return o}function y_(e,n,a){e=e.ownerDocument||e,e.head.insertBefore(a,n==="title"?e.querySelector("head > title"):null)}function qS(e,n,a){if(a===1||n.itemProp!=null)return!1;switch(e){case"meta":case"title":return!0;case"style":if(typeof n.precedence!="string"||typeof n.href!="string"||n.href==="")break;return!0;case"link":if(typeof n.rel!="string"||typeof n.href!="string"||n.href===""||n.onLoad||n.onError)break;switch(n.rel){case"stylesheet":return e=n.disabled,typeof n.precedence=="string"&&e==null;default:return!0}case"script":if(n.async&&typeof n.async!="function"&&typeof n.async!="symbol"&&!n.onLoad&&!n.onError&&n.src&&typeof n.src=="string")return!0}return!1}function S_(e){return!(e.type==="stylesheet"&&(e.state.loading&3)===0)}function YS(e,n,a,o){if(a.type==="stylesheet"&&(typeof o.media!="string"||matchMedia(o.media).matches!==!1)&&(a.state.loading&4)===0){if(a.instance===null){var u=wr(o.href),h=n.querySelector(Yo(u));if(h){n=h._p,n!==null&&typeof n=="object"&&typeof n.then=="function"&&(e.count++,e=bc.bind(e),n.then(e,e)),a.state.loading|=4,a.instance=h,gn(h);return}h=n.ownerDocument||n,o=__(o),(u=Ai.get(u))&&Dh(o,u),h=h.createElement("link"),gn(h);var y=h;y._p=new Promise(function(R,G){y.onload=R,y.onerror=G}),Pn(h,"link",o),a.instance=h}e.stylesheets===null&&(e.stylesheets=new Map),e.stylesheets.set(a,n),(n=a.state.preload)&&(a.state.loading&3)===0&&(e.count++,a=bc.bind(e),n.addEventListener("load",a),n.addEventListener("error",a))}}var Nh=0;function jS(e,n){return e.stylesheets&&e.count===0&&Ac(e,e.stylesheets),0<e.count||0<e.imgCount?function(a){var o=setTimeout(function(){if(e.stylesheets&&Ac(e,e.stylesheets),e.unsuspend){var h=e.unsuspend;e.unsuspend=null,h()}},6e4+n);0<e.imgBytes&&Nh===0&&(Nh=62500*wS());var u=setTimeout(function(){if(e.waitingForImages=!1,e.count===0&&(e.stylesheets&&Ac(e,e.stylesheets),e.unsuspend)){var h=e.unsuspend;e.unsuspend=null,h()}},(e.imgBytes>Nh?50:800)+n);return e.unsuspend=a,function(){e.unsuspend=null,clearTimeout(o),clearTimeout(u)}}:null}function bc(){if(this.count--,this.count===0&&(this.imgCount===0||!this.waitingForImages)){if(this.stylesheets)Ac(this,this.stylesheets);else if(this.unsuspend){var e=this.unsuspend;this.unsuspend=null,e()}}}var Tc=null;function Ac(e,n){e.stylesheets=null,e.unsuspend!==null&&(e.count++,Tc=new Map,n.forEach(ZS,e),Tc=null,bc.call(e))}function ZS(e,n){if(!(n.state.loading&4)){var a=Tc.get(e);if(a)var o=a.get(null);else{a=new Map,Tc.set(e,a);for(var u=e.querySelectorAll("link[data-precedence],style[data-precedence]"),h=0;h<u.length;h++){var y=u[h];(y.nodeName==="LINK"||y.getAttribute("media")!=="not all")&&(a.set(y.dataset.precedence,y),o=y)}o&&a.set(null,o)}u=n.instance,y=u.getAttribute("data-precedence"),h=a.get(y)||o,h===o&&a.set(null,u),a.set(y,u),this.count++,o=bc.bind(this),u.addEventListener("load",o),u.addEventListener("error",o),h?h.parentNode.insertBefore(u,h.nextSibling):(e=e.nodeType===9?e.head:e,e.insertBefore(u,e.firstChild)),n.state.loading|=4}}var Zo={$$typeof:F,Provider:null,Consumer:null,_currentValue:q,_currentValue2:q,_threadCount:0};function KS(e,n,a,o,u,h,y,R,G){this.tag=1,this.containerInfo=e,this.pingCache=this.current=this.pendingChildren=null,this.timeoutHandle=-1,this.callbackNode=this.next=this.pendingContext=this.context=this.cancelPendingCommit=null,this.callbackPriority=0,this.expirationTimes=jt(-1),this.entangledLanes=this.shellSuspendCounter=this.errorRecoveryDisabledLanes=this.expiredLanes=this.warmLanes=this.pingedLanes=this.suspendedLanes=this.pendingLanes=0,this.entanglements=jt(0),this.hiddenUpdates=jt(null),this.identifierPrefix=o,this.onUncaughtError=u,this.onCaughtError=h,this.onRecoverableError=y,this.pooledCache=null,this.pooledCacheLanes=0,this.formState=G,this.incompleteTransitions=new Map}function M_(e,n,a,o,u,h,y,R,G,at,mt,Mt){return e=new KS(e,n,a,y,G,at,mt,Mt,R),n=1,h===!0&&(n|=24),h=ri(3,null,null,n),e.current=h,h.stateNode=e,n=ff(),n.refCount++,e.pooledCache=n,n.refCount++,h.memoizedState={element:o,isDehydrated:a,cache:n},mf(h),e}function E_(e){return e?(e=rr,e):rr}function b_(e,n,a,o,u,h){u=E_(u),o.context===null?o.context=u:o.pendingContext=u,o=Ga(n),o.payload={element:a},h=h===void 0?null:h,h!==null&&(o.callback=h),a=Va(e,o,n),a!==null&&($n(a,e,n),Ro(a,e,n))}function T_(e,n){if(e=e.memoizedState,e!==null&&e.dehydrated!==null){var a=e.retryLane;e.retryLane=a!==0&&a<n?a:n}}function Lh(e,n){T_(e,n),(e=e.alternate)&&T_(e,n)}function A_(e){if(e.tag===13||e.tag===31){var n=bs(e,67108864);n!==null&&$n(n,e,67108864),Lh(e,67108864)}}function R_(e){if(e.tag===13||e.tag===31){var n=fi();n=lo(n);var a=bs(e,n);a!==null&&$n(a,e,n),Lh(e,n)}}var Rc=!0;function QS(e,n,a,o){var u=I.T;I.T=null;var h=H.p;try{H.p=2,Oh(e,n,a,o)}finally{H.p=h,I.T=u}}function JS(e,n,a,o){var u=I.T;I.T=null;var h=H.p;try{H.p=8,Oh(e,n,a,o)}finally{H.p=h,I.T=u}}function Oh(e,n,a,o){if(Rc){var u=Ph(o);if(u===null)yh(e,n,o,wc,a),C_(e,o);else if(tM(u,e,n,a,o))o.stopPropagation();else if(C_(e,o),n&4&&-1<$S.indexOf(e)){for(;u!==null;){var h=ea(u);if(h!==null)switch(h.tag){case 3:if(h=h.stateNode,h.current.memoizedState.isDehydrated){var y=Dt(h.pendingLanes);if(y!==0){var R=h;for(R.pendingLanes|=2,R.entangledLanes|=2;y;){var G=1<<31-Ft(y);R.entanglements[1]|=G,y&=~G}Wi(h),(Oe&6)===0&&(uc=Le()+500,ko(0))}}break;case 31:case 13:R=bs(h,2),R!==null&&$n(R,h,2),hc(),Lh(h,2)}if(h=Ph(o),h===null&&yh(e,n,o,wc,a),h===u)break;u=h}u!==null&&o.stopPropagation()}else yh(e,n,o,null,a)}}function Ph(e){return e=zu(e),zh(e)}var wc=null;function zh(e){if(wc=null,e=ta(e),e!==null){var n=c(e);if(n===null)e=null;else{var a=n.tag;if(a===13){if(e=f(n),e!==null)return e;e=null}else if(a===31){if(e=p(n),e!==null)return e;e=null}else if(a===3){if(n.stateNode.current.memoizedState.isDehydrated)return n.tag===3?n.stateNode.containerInfo:null;e=null}else n!==e&&(e=null)}}return wc=e,null}function w_(e){switch(e){case"beforetoggle":case"cancel":case"click":case"close":case"contextmenu":case"copy":case"cut":case"auxclick":case"dblclick":case"dragend":case"dragstart":case"drop":case"focusin":case"focusout":case"input":case"invalid":case"keydown":case"keypress":case"keyup":case"mousedown":case"mouseup":case"paste":case"pause":case"play":case"pointercancel":case"pointerdown":case"pointerup":case"ratechange":case"reset":case"resize":case"seeked":case"submit":case"toggle":case"touchcancel":case"touchend":case"touchstart":case"volumechange":case"change":case"selectionchange":case"textInput":case"compositionstart":case"compositionend":case"compositionupdate":case"beforeblur":case"afterblur":case"beforeinput":case"blur":case"fullscreenchange":case"focus":case"hashchange":case"popstate":case"select":case"selectstart":return 2;case"drag":case"dragenter":case"dragexit":case"dragleave":case"dragover":case"mousemove":case"mouseout":case"mouseover":case"pointermove":case"pointerout":case"pointerover":case"scroll":case"touchmove":case"wheel":case"mouseenter":case"mouseleave":case"pointerenter":case"pointerleave":return 8;case"message":switch(Lt()){case D:return 2;case M:return 8;case X:case V:return 32;case ct:return 268435456;default:return 32}default:return 32}}var Bh=!1,$a=null,ts=null,es=null,Ko=new Map,Qo=new Map,ns=[],$S="mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(" ");function C_(e,n){switch(e){case"focusin":case"focusout":$a=null;break;case"dragenter":case"dragleave":ts=null;break;case"mouseover":case"mouseout":es=null;break;case"pointerover":case"pointerout":Ko.delete(n.pointerId);break;case"gotpointercapture":case"lostpointercapture":Qo.delete(n.pointerId)}}function Jo(e,n,a,o,u,h){return e===null||e.nativeEvent!==h?(e={blockedOn:n,domEventName:a,eventSystemFlags:o,nativeEvent:h,targetContainers:[u]},n!==null&&(n=ea(n),n!==null&&A_(n)),e):(e.eventSystemFlags|=o,n=e.targetContainers,u!==null&&n.indexOf(u)===-1&&n.push(u),e)}function tM(e,n,a,o,u){switch(n){case"focusin":return $a=Jo($a,e,n,a,o,u),!0;case"dragenter":return ts=Jo(ts,e,n,a,o,u),!0;case"mouseover":return es=Jo(es,e,n,a,o,u),!0;case"pointerover":var h=u.pointerId;return Ko.set(h,Jo(Ko.get(h)||null,e,n,a,o,u)),!0;case"gotpointercapture":return h=u.pointerId,Qo.set(h,Jo(Qo.get(h)||null,e,n,a,o,u)),!0}return!1}function D_(e){var n=ta(e.target);if(n!==null){var a=c(n);if(a!==null){if(n=a.tag,n===13){if(n=f(a),n!==null){e.blockedOn=n,$s(e.priority,function(){R_(a)});return}}else if(n===31){if(n=p(a),n!==null){e.blockedOn=n,$s(e.priority,function(){R_(a)});return}}else if(n===3&&a.stateNode.current.memoizedState.isDehydrated){e.blockedOn=a.tag===3?a.stateNode.containerInfo:null;return}}}e.blockedOn=null}function Cc(e){if(e.blockedOn!==null)return!1;for(var n=e.targetContainers;0<n.length;){var a=Ph(e.nativeEvent);if(a===null){a=e.nativeEvent;var o=new a.constructor(a.type,a);Pu=o,a.target.dispatchEvent(o),Pu=null}else return n=ea(a),n!==null&&A_(n),e.blockedOn=a,!1;n.shift()}return!0}function U_(e,n,a){Cc(e)&&a.delete(n)}function eM(){Bh=!1,$a!==null&&Cc($a)&&($a=null),ts!==null&&Cc(ts)&&(ts=null),es!==null&&Cc(es)&&(es=null),Ko.forEach(U_),Qo.forEach(U_)}function Dc(e,n){e.blockedOn===n&&(e.blockedOn=null,Bh||(Bh=!0,r.unstable_scheduleCallback(r.unstable_NormalPriority,eM)))}var Uc=null;function N_(e){Uc!==e&&(Uc=e,r.unstable_scheduleCallback(r.unstable_NormalPriority,function(){Uc===e&&(Uc=null);for(var n=0;n<e.length;n+=3){var a=e[n],o=e[n+1],u=e[n+2];if(typeof o!="function"){if(zh(o||a)===null)continue;break}var h=ea(a);h!==null&&(e.splice(n,3),n-=3,Pf(h,{pending:!0,data:u,method:a.method,action:o},o,u))}}))}function Dr(e){function n(G){return Dc(G,e)}$a!==null&&Dc($a,e),ts!==null&&Dc(ts,e),es!==null&&Dc(es,e),Ko.forEach(n),Qo.forEach(n);for(var a=0;a<ns.length;a++){var o=ns[a];o.blockedOn===e&&(o.blockedOn=null)}for(;0<ns.length&&(a=ns[0],a.blockedOn===null);)D_(a),a.blockedOn===null&&ns.shift();if(a=(e.ownerDocument||e).$$reactFormReplay,a!=null)for(o=0;o<a.length;o+=3){var u=a[o],h=a[o+1],y=u[Un]||null;if(typeof h=="function")y||N_(a);else if(y){var R=null;if(h&&h.hasAttribute("formAction")){if(u=h,y=h[Un]||null)R=y.formAction;else if(zh(u)!==null)continue}else R=y.action;typeof R=="function"?a[o+1]=R:(a.splice(o,3),o-=3),N_(a)}}}function L_(){function e(h){h.canIntercept&&h.info==="react-transition"&&h.intercept({handler:function(){return new Promise(function(y){return u=y})},focusReset:"manual",scroll:"manual"})}function n(){u!==null&&(u(),u=null),o||setTimeout(a,20)}function a(){if(!o&&!navigation.transition){var h=navigation.currentEntry;h&&h.url!=null&&navigation.navigate(h.url,{state:h.getState(),info:"react-transition",history:"replace"})}}if(typeof navigation=="object"){var o=!1,u=null;return navigation.addEventListener("navigate",e),navigation.addEventListener("navigatesuccess",n),navigation.addEventListener("navigateerror",n),setTimeout(a,100),function(){o=!0,navigation.removeEventListener("navigate",e),navigation.removeEventListener("navigatesuccess",n),navigation.removeEventListener("navigateerror",n),u!==null&&(u(),u=null)}}}function Ih(e){this._internalRoot=e}Nc.prototype.render=Ih.prototype.render=function(e){var n=this._internalRoot;if(n===null)throw Error(s(409));var a=n.current,o=fi();b_(a,o,e,n,null,null)},Nc.prototype.unmount=Ih.prototype.unmount=function(){var e=this._internalRoot;if(e!==null){this._internalRoot=null;var n=e.containerInfo;b_(e.current,2,null,e,null,null),hc(),n[Yn]=null}};function Nc(e){this._internalRoot=e}Nc.prototype.unstable_scheduleHydration=function(e){if(e){var n=uo();e={blockedOn:null,target:e,priority:n};for(var a=0;a<ns.length&&n!==0&&n<ns[a].priority;a++);ns.splice(a,0,e),a===0&&D_(e)}};var O_=t.version;if(O_!=="19.2.0")throw Error(s(527,O_,"19.2.0"));H.findDOMNode=function(e){var n=e._reactInternals;if(n===void 0)throw typeof e.render=="function"?Error(s(188)):(e=Object.keys(e).join(","),Error(s(268,e)));return e=d(n),e=e!==null?x(e):null,e=e===null?null:e.stateNode,e};var nM={bundleType:0,version:"19.2.0",rendererPackageName:"react-dom",currentDispatcherRef:I,reconcilerVersion:"19.2.0"};if(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__<"u"){var Lc=__REACT_DEVTOOLS_GLOBAL_HOOK__;if(!Lc.isDisabled&&Lc.supportsFiber)try{ut=Lc.inject(nM),pt=Lc}catch{}}return tl.createRoot=function(e,n){if(!l(e))throw Error(s(299));var a=!1,o="",u=G0,h=V0,y=k0;return n!=null&&(n.unstable_strictMode===!0&&(a=!0),n.identifierPrefix!==void 0&&(o=n.identifierPrefix),n.onUncaughtError!==void 0&&(u=n.onUncaughtError),n.onCaughtError!==void 0&&(h=n.onCaughtError),n.onRecoverableError!==void 0&&(y=n.onRecoverableError)),n=M_(e,1,!1,null,null,a,o,null,u,h,y,L_),e[Yn]=n.current,xh(e),new Ih(n)},tl.hydrateRoot=function(e,n,a){if(!l(e))throw Error(s(299));var o=!1,u="",h=G0,y=V0,R=k0,G=null;return a!=null&&(a.unstable_strictMode===!0&&(o=!0),a.identifierPrefix!==void 0&&(u=a.identifierPrefix),a.onUncaughtError!==void 0&&(h=a.onUncaughtError),a.onCaughtError!==void 0&&(y=a.onCaughtError),a.onRecoverableError!==void 0&&(R=a.onRecoverableError),a.formState!==void 0&&(G=a.formState)),n=M_(e,1,!0,n,a??null,o,u,G,h,y,R,L_),n.context=E_(null),a=n.current,o=fi(),o=lo(o),u=Ga(o),u.callback=null,Va(a,u,o),a=o,n.current.lanes=a,kt(n,a),Wi(n),e[Yn]=n.current,xh(e),new Nc(n)},tl.version="19.2.0",tl}var X_;function pM(){if(X_)return Gh.exports;X_=1;function r(){if(!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__>"u"||typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE!="function"))try{__REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(r)}catch(t){console.error(t)}}return r(),Gh.exports=dM(),Gh.exports}var mM=pM();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const fx=(...r)=>r.filter((t,i,s)=>!!t&&t.trim()!==""&&s.indexOf(t)===i).join(" ").trim();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const gM=r=>r.replace(/([a-z0-9])([A-Z])/g,"$1-$2").toLowerCase();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const _M=r=>r.replace(/^([A-Z])|[\s-_]+(\w)/g,(t,i,s)=>s?s.toUpperCase():i.toLowerCase());/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const W_=r=>{const t=_M(r);return t.charAt(0).toUpperCase()+t.slice(1)};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */var Wh={xmlns:"http://www.w3.org/2000/svg",width:24,height:24,viewBox:"0 0 24 24",fill:"none",stroke:"currentColor",strokeWidth:2,strokeLinecap:"round",strokeLinejoin:"round"};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const vM=r=>{for(const t in r)if(t.startsWith("aria-")||t==="role"||t==="title")return!0;return!1},xM=Yt.createContext({}),yM=()=>Yt.useContext(xM),SM=Yt.forwardRef(({color:r,size:t,strokeWidth:i,absoluteStrokeWidth:s,className:l="",children:c,iconNode:f,...p},m)=>{const{size:d=24,strokeWidth:x=2,absoluteStrokeWidth:v=!1,color:g="currentColor",className:S=""}=yM()??{},b=s??v?Number(i??x)*24/Number(t??d):i??x;return Yt.createElement("svg",{ref:m,...Wh,width:t??d??Wh.width,height:t??d??Wh.height,stroke:r??g,strokeWidth:b,className:fx("lucide",S,l),...!c&&!vM(p)&&{"aria-hidden":"true"},...p},[...f.map(([w,E])=>Yt.createElement(w,E)),...Array.isArray(c)?c:[c]])});/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const Me=(r,t)=>{const i=Yt.forwardRef(({className:s,...l},c)=>Yt.createElement(SM,{ref:c,iconNode:t,className:fx(`lucide-${gM(W_(r))}`,`lucide-${r}`,s),...l}));return i.displayName=W_(r),i};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const MM=[["path",{d:"M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2",key:"169zse"}]],hx=Me("activity",MM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const EM=[["rect",{width:"20",height:"5",x:"2",y:"3",rx:"1",key:"1wp1u1"}],["path",{d:"M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8",key:"1s80jp"}],["path",{d:"M10 12h4",key:"a56b0p"}]],xu=Me("archive",EM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const bM=[["path",{d:"m12 19-7-7 7-7",key:"1l729n"}],["path",{d:"M19 12H5",key:"x3x0zl"}]],TM=Me("arrow-left",bM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const AM=[["path",{d:"M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z",key:"hh9hay"}],["path",{d:"m3.3 7 8.7 5 8.7-5",key:"g66t2b"}],["path",{d:"M12 22V12",key:"d0xqtd"}]],RM=Me("box",AM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const wM=[["path",{d:"M2.97 12.92A2 2 0 0 0 2 14.63v3.24a2 2 0 0 0 .97 1.71l3 1.8a2 2 0 0 0 2.06 0L12 19v-5.5l-5-3-4.03 2.42Z",key:"lc1i9w"}],["path",{d:"m7 16.5-4.74-2.85",key:"1o9zyk"}],["path",{d:"m7 16.5 5-3",key:"va8pkn"}],["path",{d:"M7 16.5v5.17",key:"jnp8gn"}],["path",{d:"M12 13.5V19l3.97 2.38a2 2 0 0 0 2.06 0l3-1.8a2 2 0 0 0 .97-1.71v-3.24a2 2 0 0 0-.97-1.71L17 10.5l-5 3Z",key:"8zsnat"}],["path",{d:"m17 16.5-5-3",key:"8arw3v"}],["path",{d:"m17 16.5 4.74-2.85",key:"8rfmw"}],["path",{d:"M17 16.5v5.17",key:"k6z78m"}],["path",{d:"M7.97 4.42A2 2 0 0 0 7 6.13v4.37l5 3 5-3V6.13a2 2 0 0 0-.97-1.71l-3-1.8a2 2 0 0 0-2.06 0l-3 1.8Z",key:"1xygjf"}],["path",{d:"M12 8 7.26 5.15",key:"1vbdud"}],["path",{d:"m12 8 4.74-2.85",key:"3rx089"}],["path",{d:"M12 13.5V8",key:"1io7kd"}]],CM=Me("boxes",wM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const DM=[["path",{d:"M8 2v3",key:"1ioesn"}],["path",{d:"M16 2v3",key:"otl347"}],["rect",{x:"3",y:"3",width:"18",height:"18",rx:"2",key:"h1oib"}],["path",{d:"M3 9h18",key:"1pudct"}],["path",{d:"M8 13h.01",key:"1sbv64"}],["path",{d:"M12 13h.01",key:"y0uutt"}],["path",{d:"M16 13h.01",key:"wip0gl"}],["path",{d:"M8 17h.01",key:"p3bg7i"}],["path",{d:"M12 17h.01",key:"p32p05"}],["path",{d:"M16 17h.01",key:"ql8jdd"}]],UM=Me("calendar-days",DM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const NM=[["path",{d:"M20 6 9 17l-5-5",key:"1gmf2c"}]],LM=Me("check",NM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const OM=[["path",{d:"m9 18 6-6-6-6",key:"mthhwq"}]],PM=Me("chevron-right",OM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const zM=[["circle",{cx:"12",cy:"12",r:"10",key:"1mglay"}],["path",{d:"M12 6v6h4",key:"135r8i"}]],BM=Me("clock-3",zM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const IM=[["rect",{width:"14",height:"14",x:"8",y:"8",rx:"2",ry:"2",key:"17jyea"}],["path",{d:"M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2",key:"zix9uf"}]],FM=Me("copy",IM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const HM=[["path",{d:"M12 15V3",key:"m9g1x1"}],["path",{d:"M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",key:"ih7n3h"}],["path",{d:"m7 10 5 5 5-5",key:"brsn70"}]],GM=Me("download",HM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const VM=[["path",{d:"M7 16.3c2.2 0 4-1.83 4-4.05 0-1.16-.57-2.26-1.71-3.19S7.29 6.75 7 5.3c-.29 1.45-1.14 2.84-2.29 3.76S3 11.1 3 12.25c0 2.22 1.8 4.05 4 4.05z",key:"1ptgy4"}],["path",{d:"M12.56 6.6A10.97 10.97 0 0 0 14 3.02c.5 2.5 2 4.9 4 6.5s3 3.5 3 5.5a6.98 6.98 0 0 1-11.91 4.97",key:"1sl1rz"}]],kM=Me("droplets",VM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const XM=[["path",{d:"M15 3h6v6",key:"1q9fwt"}],["path",{d:"M10 14 21 3",key:"gplh6r"}],["path",{d:"M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6",key:"a6xqqp"}]],WM=Me("external-link",XM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const qM=[["path",{d:"M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z",key:"1oefj6"}],["path",{d:"M14 2v5a1 1 0 0 0 1 1h5",key:"wfsgrz"}],["path",{d:"M10 9H8",key:"b1mrlr"}],["path",{d:"M16 13H8",key:"t4e002"}],["path",{d:"M16 17H8",key:"z1uh3a"}]],YM=Me("file-text",qM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const jM=[["path",{d:"m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2",key:"usdka0"}]],dx=Me("folder-open",jM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const ZM=[["path",{d:"m12 14 4-4",key:"9kzdfg"}],["path",{d:"M3.34 19a10 10 0 1 1 17.32 0",key:"19p75a"}]],wp=Me("gauge",ZM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const KM=[["path",{d:"M12 3v18",key:"108xh3"}],["path",{d:"M3 12h18",key:"1i2n21"}],["rect",{x:"3",y:"3",width:"18",height:"18",rx:"2",key:"h1oib"}]],QM=Me("grid-2x2",KM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const JM=[["path",{d:"M12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83z",key:"zw3jo"}],["path",{d:"M2 12a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 12",key:"1wduqc"}],["path",{d:"M2 17a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 17",key:"kqbvx6"}]],$M=Me("layers",JM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const t1=[["path",{d:"M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z",key:"nnexq3"}],["path",{d:"M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12",key:"mt58a7"}]],e1=Me("leaf",t1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const n1=[["line",{x1:"2",x2:"5",y1:"12",y2:"12",key:"bvdh0s"}],["line",{x1:"19",x2:"22",y1:"12",y2:"12",key:"1tbv5k"}],["line",{x1:"12",x2:"12",y1:"2",y2:"5",key:"11lu5j"}],["line",{x1:"12",x2:"12",y1:"19",y2:"22",key:"x3vr5v"}],["circle",{cx:"12",cy:"12",r:"7",key:"fim9np"}],["circle",{cx:"12",cy:"12",r:"3",key:"1v7zrd"}]],i1=Me("locate-fixed",n1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const a1=[["rect",{width:"18",height:"11",x:"3",y:"11",rx:"2",ry:"2",key:"1w4ew1"}],["path",{d:"M7 11V7a5 5 0 0 1 9.9-1",key:"1mm8w8"}]],s1=Me("lock-open",a1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const r1=[["path",{d:"M15 3h6v6",key:"1q9fwt"}],["path",{d:"m21 3-7 7",key:"1l2asr"}],["path",{d:"m3 21 7-7",key:"tjx5ai"}],["path",{d:"M9 21H3v-6",key:"wtvkvv"}]],px=Me("maximize-2",r1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const o1=[["path",{d:"m14 10 7-7",key:"oa77jy"}],["path",{d:"M20 10h-6V4",key:"mjg0md"}],["path",{d:"m3 21 7-7",key:"tjx5ai"}],["path",{d:"M4 14h6v6",key:"rmj7iw"}]],l1=Me("minimize-2",o1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const c1=[["rect",{x:"14",y:"3",width:"5",height:"18",rx:"1",key:"kaeet6"}],["rect",{x:"5",y:"3",width:"5",height:"18",rx:"1",key:"1wsw3u"}]],u1=Me("pause",c1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const f1=[["path",{d:"M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z",key:"10ikf1"}]],h1=Me("play",f1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const d1=[["path",{d:"m15.194 13.707 3.814 1.86-1.86 3.814",key:"16shm9"}],["path",{d:"M16.47214 7.52786 A 5 10 0 1 0 13 21.79796",key:"1245p8"}],["path",{d:"M21.79796 11 A 10 5 0 1 0 19 15.57071",key:"1i40ks"}]],p1=Me("rotate-3d",d1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const m1=[["path",{d:"M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8",key:"1357e3"}],["path",{d:"M3 3v5h5",key:"1xhq8a"}]],g1=Me("rotate-ccw",m1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const _1=[["path",{d:"m21 21-4.34-4.34",key:"14j7rj"}],["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}]],v1=Me("search",_1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const x1=[["path",{d:"M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915",key:"1i5ecw"}],["circle",{cx:"12",cy:"12",r:"3",key:"1v7zrd"}]],y1=Me("settings",x1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const S1=[["path",{d:"M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",key:"oel41y"}],["path",{d:"m9 12 2 2 4-4",key:"dzmm74"}]],M1=Me("shield-check",S1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const E1=[["path",{d:"M14 4v10.54a4 4 0 1 1-4 0V4a2 2 0 0 1 4 0Z",key:"17jzev"}]],b1=Me("thermometer",E1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const T1=[["path",{d:"m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",key:"wmoenq"}],["path",{d:"M12 9v4",key:"juzpu7"}],["path",{d:"M12 17h.01",key:"p32p05"}]],mx=Me("triangle-alert",T1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const A1=[["circle",{cx:"12",cy:"8",r:"5",key:"1hypcn"}],["path",{d:"M20 21a8 8 0 0 0-16 0",key:"rfgkzh"}]],R1=Me("user-round",A1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const w1=[["path",{d:"M12.8 19.6A2 2 0 1 0 14 16H2",key:"148xed"}],["path",{d:"M17.5 8a2.5 2.5 0 1 1 2 4H2",key:"1u4tom"}],["path",{d:"M9.8 4.4A2 2 0 1 1 11 8H2",key:"75valh"}]],C1=Me("wind",w1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const D1=[["path",{d:"M18 6 6 18",key:"1bl5f8"}],["path",{d:"m6 6 12 12",key:"d8bk6v"}]],U1=Me("x",D1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const N1=[["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}],["line",{x1:"21",x2:"16.65",y1:"21",y2:"16.65",key:"13gj7c"}],["line",{x1:"11",x2:"11",y1:"8",y2:"14",key:"1vmskp"}],["line",{x1:"8",x2:"14",y1:"11",y2:"11",key:"durymu"}]],L1=Me("zoom-in",N1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const O1=[["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}],["line",{x1:"21",x2:"16.65",y1:"21",y2:"16.65",key:"13gj7c"}],["line",{x1:"8",x2:"14",y1:"11",y2:"11",key:"durymu"}]],P1=Me("zoom-out",O1);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */const Cp="185",z1=0,q_=1,B1=2,_l=1,I1=2,pl=3,ds=0,ti=1,wi=2,Aa=0,Qr=1,Ys=2,Y_=3,j_=4,F1=5,Vs=100,H1=101,G1=102,V1=103,k1=104,X1=200,W1=201,q1=202,Y1=203,Nd=204,Ld=205,j1=206,Z1=207,K1=208,Q1=209,J1=210,$1=211,tE=212,eE=213,nE=214,Od=0,Pd=1,zd=2,eo=3,Bd=4,Id=5,Fd=6,Hd=7,gx=0,iE=1,aE=2,Ki=0,_x=1,vx=2,xx=3,Dp=4,yx=5,Sx=6,Mx=7,Ex=300,js=301,no=302,qh=303,Yh=304,Du=306,Gd=1e3,Ta=1001,Vd=1002,zn=1003,sE=1004,Oc=1005,Gn=1006,jh=1007,Xs=1008,gi=1009,bx=1010,Tx=1011,xl=1012,Up=1013,Ji=1014,Ii=1015,Ca=1016,Np=1017,Lp=1018,yl=1020,Ax=35902,Rx=35899,wx=1021,Cx=1022,Fi=1023,Da=1026,Ws=1027,Op=1028,Pp=1029,Zs=1030,zp=1031,Bp=1033,pu=33776,mu=33777,gu=33778,_u=33779,kd=35840,Xd=35841,Wd=35842,qd=35843,Yd=36196,jd=37492,Zd=37496,Kd=37488,Qd=37489,yu=37490,Jd=37491,$d=37808,tp=37809,ep=37810,np=37811,ip=37812,ap=37813,sp=37814,rp=37815,op=37816,lp=37817,cp=37818,up=37819,fp=37820,hp=37821,dp=36492,pp=36494,mp=36495,gp=36283,_p=36284,Su=36285,vp=36286,rE=3200,xp=0,oE=1,us="",qn="srgb",Mu="srgb-linear",Eu="linear",ke="srgb",Ur=7680,Z_=519,lE=512,cE=513,uE=514,Ip=515,fE=516,hE=517,Fp=518,dE=519,yp=35044,K_="300 es",Zi=2e3,Sl=2001;function pE(r){for(let t=r.length-1;t>=0;--t)if(r[t]>=65535)return!0;return!1}function bu(r){return document.createElementNS("http://www.w3.org/1999/xhtml",r)}function mE(){const r=bu("canvas");return r.style.display="block",r}const Q_={};function Tu(...r){const t="THREE."+r.shift();console.log(t,...r)}function Dx(r){const t=r[0];if(typeof t=="string"&&t.startsWith("TSL:")){const i=r[1];i&&i.isStackTrace?r[0]+=" "+i.getLocation():r[1]='Stack trace not available. Enable "THREE.Node.captureStackTrace" to capture stack traces.'}return r}function se(...r){r=Dx(r);const t="THREE."+r.shift();{const i=r[0];i&&i.isStackTrace?console.warn(i.getError(t)):console.warn(t,...r)}}function Ae(...r){r=Dx(r);const t="THREE."+r.shift();{const i=r[0];i&&i.isStackTrace?console.error(i.getError(t)):console.error(t,...r)}}function Jr(...r){const t=r.join(" ");t in Q_||(Q_[t]=!0,se(...r))}function gE(r,t,i){return new Promise(function(s,l){function c(){switch(r.clientWaitSync(t,r.SYNC_FLUSH_COMMANDS_BIT,0)){case r.WAIT_FAILED:l();break;case r.TIMEOUT_EXPIRED:setTimeout(c,i);break;default:s()}}setTimeout(c,i)})}const _E={[Od]:Pd,[zd]:Fd,[Bd]:Hd,[eo]:Id,[Pd]:Od,[Fd]:zd,[Hd]:Bd,[Id]:eo};class Ks{addEventListener(t,i){this._listeners===void 0&&(this._listeners={});const s=this._listeners;s[t]===void 0&&(s[t]=[]),s[t].indexOf(i)===-1&&s[t].push(i)}hasEventListener(t,i){const s=this._listeners;return s===void 0?!1:s[t]!==void 0&&s[t].indexOf(i)!==-1}removeEventListener(t,i){const s=this._listeners;if(s===void 0)return;const l=s[t];if(l!==void 0){const c=l.indexOf(i);c!==-1&&l.splice(c,1)}}dispatchEvent(t){const i=this._listeners;if(i===void 0)return;const s=i[t.type];if(s!==void 0){t.target=this;const l=s.slice(0);for(let c=0,f=l.length;c<f;c++)l[c].call(this,t);t.target=null}}}const Fn=["00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f","10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f","20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f","30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f","40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f","50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f","60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f","70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f","80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f","90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f","a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af","b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf","c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf","d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df","e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef","f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"];let J_=1234567;const $r=Math.PI/180,Ml=180/Math.PI;function Ra(){const r=Math.random()*4294967295|0,t=Math.random()*4294967295|0,i=Math.random()*4294967295|0,s=Math.random()*4294967295|0;return(Fn[r&255]+Fn[r>>8&255]+Fn[r>>16&255]+Fn[r>>24&255]+"-"+Fn[t&255]+Fn[t>>8&255]+"-"+Fn[t>>16&15|64]+Fn[t>>24&255]+"-"+Fn[i&63|128]+Fn[i>>8&255]+"-"+Fn[i>>16&255]+Fn[i>>24&255]+Fn[s&255]+Fn[s>>8&255]+Fn[s>>16&255]+Fn[s>>24&255]).toLowerCase()}function ge(r,t,i){return Math.max(t,Math.min(i,r))}function Hp(r,t){return(r%t+t)%t}function vE(r,t,i,s,l){return s+(r-t)*(l-s)/(i-t)}function xE(r,t,i){return r!==t?(i-r)/(t-r):0}function vl(r,t,i){return(1-i)*r+i*t}function yE(r,t,i,s){return vl(r,t,1-Math.exp(-i*s))}function SE(r,t=1){return t-Math.abs(Hp(r,t*2)-t)}function ME(r,t,i){return r<=t?0:r>=i?1:(r=(r-t)/(i-t),r*r*(3-2*r))}function EE(r,t,i){return r<=t?0:r>=i?1:(r=(r-t)/(i-t),r*r*r*(r*(r*6-15)+10))}function bE(r,t){return r+Math.floor(Math.random()*(t-r+1))}function TE(r,t){return r+Math.random()*(t-r)}function AE(r){return r*(.5-Math.random())}function RE(r){r!==void 0&&(J_=r);let t=J_+=1831565813;return t=Math.imul(t^t>>>15,t|1),t^=t+Math.imul(t^t>>>7,t|61),((t^t>>>14)>>>0)/4294967296}function wE(r){return r*$r}function CE(r){return r*Ml}function DE(r){return(r&r-1)===0&&r!==0}function UE(r){return Math.pow(2,Math.ceil(Math.log(r)/Math.LN2))}function NE(r){return Math.pow(2,Math.floor(Math.log(r)/Math.LN2))}function LE(r,t,i,s,l){const c=Math.cos,f=Math.sin,p=c(i/2),m=f(i/2),d=c((t+s)/2),x=f((t+s)/2),v=c((t-s)/2),g=f((t-s)/2),S=c((s-t)/2),b=f((s-t)/2);switch(l){case"XYX":r.set(p*x,m*v,m*g,p*d);break;case"YZY":r.set(m*g,p*x,m*v,p*d);break;case"ZXZ":r.set(m*v,m*g,p*x,p*d);break;case"XZX":r.set(p*x,m*b,m*S,p*d);break;case"YXY":r.set(m*S,p*x,m*b,p*d);break;case"ZYZ":r.set(m*b,m*S,p*x,p*d);break;default:se("MathUtils: .setQuaternionFromProperEuler() encountered an unknown order: "+l)}}function Bi(r,t){switch(t.constructor){case Float32Array:return r;case Uint32Array:return r/4294967295;case Uint16Array:return r/65535;case Uint8Array:return r/255;case Int32Array:return Math.max(r/2147483647,-1);case Int16Array:return Math.max(r/32767,-1);case Int8Array:return Math.max(r/127,-1);default:throw new Error("THREE.MathUtils: Invalid component type.")}}function Xe(r,t){switch(t.constructor){case Float32Array:return r;case Uint32Array:return Math.round(r*4294967295);case Uint16Array:return Math.round(r*65535);case Uint8Array:return Math.round(r*255);case Int32Array:return Math.round(r*2147483647);case Int16Array:return Math.round(r*32767);case Int8Array:return Math.round(r*127);default:throw new Error("THREE.MathUtils: Invalid component type.")}}const ba={DEG2RAD:$r,RAD2DEG:Ml,generateUUID:Ra,clamp:ge,euclideanModulo:Hp,mapLinear:vE,inverseLerp:xE,lerp:vl,damp:yE,pingpong:SE,smoothstep:ME,smootherstep:EE,randInt:bE,randFloat:TE,randFloatSpread:AE,seededRandom:RE,degToRad:wE,radToDeg:CE,isPowerOfTwo:DE,ceilPowerOfTwo:UE,floorPowerOfTwo:NE,setQuaternionFromProperEuler:LE,normalize:Xe,denormalize:Bi},Jp=class Jp{constructor(t=0,i=0){this.x=t,this.y=i}get width(){return this.x}set width(t){this.x=t}get height(){return this.y}set height(t){this.y=t}set(t,i){return this.x=t,this.y=i,this}setScalar(t){return this.x=t,this.y=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;default:throw new Error("THREE.Vector2: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;default:throw new Error("THREE.Vector2: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y)}copy(t){return this.x=t.x,this.y=t.y,this}add(t){return this.x+=t.x,this.y+=t.y,this}addScalar(t){return this.x+=t,this.y+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this}subScalar(t){return this.x-=t,this.y-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this}multiply(t){return this.x*=t.x,this.y*=t.y,this}multiplyScalar(t){return this.x*=t,this.y*=t,this}divide(t){return this.x/=t.x,this.y/=t.y,this}divideScalar(t){return this.multiplyScalar(1/t)}applyMatrix3(t){const i=this.x,s=this.y,l=t.elements;return this.x=l[0]*i+l[3]*s+l[6],this.y=l[1]*i+l[4]*s+l[7],this}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this}clamp(t,i){return this.x=ge(this.x,t.x,i.x),this.y=ge(this.y,t.y,i.y),this}clampScalar(t,i){return this.x=ge(this.x,t,i),this.y=ge(this.y,t,i),this}clampLength(t,i){const s=this.length();return this.divideScalar(s||1).multiplyScalar(ge(s,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this}negate(){return this.x=-this.x,this.y=-this.y,this}dot(t){return this.x*t.x+this.y*t.y}cross(t){return this.x*t.y-this.y*t.x}lengthSq(){return this.x*this.x+this.y*this.y}length(){return Math.sqrt(this.x*this.x+this.y*this.y)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)}normalize(){return this.divideScalar(this.length()||1)}angle(){return Math.atan2(-this.y,-this.x)+Math.PI}angleTo(t){const i=Math.sqrt(this.lengthSq()*t.lengthSq());if(i===0)return Math.PI/2;const s=this.dot(t)/i;return Math.acos(ge(s,-1,1))}distanceTo(t){return Math.sqrt(this.distanceToSquared(t))}distanceToSquared(t){const i=this.x-t.x,s=this.y-t.y;return i*i+s*s}manhattanDistanceTo(t){return Math.abs(this.x-t.x)+Math.abs(this.y-t.y)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this}lerpVectors(t,i,s){return this.x=t.x+(i.x-t.x)*s,this.y=t.y+(i.y-t.y)*s,this}equals(t){return t.x===this.x&&t.y===this.y}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this}rotateAround(t,i){const s=Math.cos(i),l=Math.sin(i),c=this.x-t.x,f=this.y-t.y;return this.x=c*s-f*l+t.x,this.y=c*l+f*s+t.y,this}random(){return this.x=Math.random(),this.y=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y}};Jp.prototype.isVector2=!0;let oe=Jp;class ro{constructor(t=0,i=0,s=0,l=1){this.isQuaternion=!0,this._x=t,this._y=i,this._z=s,this._w=l}static slerpFlat(t,i,s,l,c,f,p){let m=s[l+0],d=s[l+1],x=s[l+2],v=s[l+3],g=c[f+0],S=c[f+1],b=c[f+2],w=c[f+3];if(v!==w||m!==g||d!==S||x!==b){let E=m*g+d*S+x*b+v*w;E<0&&(g=-g,S=-S,b=-b,w=-w,E=-E);let _=1-p;if(E<.9995){const z=Math.acos(E),F=Math.sin(z);_=Math.sin(_*z)/F,p=Math.sin(p*z)/F,m=m*_+g*p,d=d*_+S*p,x=x*_+b*p,v=v*_+w*p}else{m=m*_+g*p,d=d*_+S*p,x=x*_+b*p,v=v*_+w*p;const z=1/Math.sqrt(m*m+d*d+x*x+v*v);m*=z,d*=z,x*=z,v*=z}}t[i]=m,t[i+1]=d,t[i+2]=x,t[i+3]=v}static multiplyQuaternionsFlat(t,i,s,l,c,f){const p=s[l],m=s[l+1],d=s[l+2],x=s[l+3],v=c[f],g=c[f+1],S=c[f+2],b=c[f+3];return t[i]=p*b+x*v+m*S-d*g,t[i+1]=m*b+x*g+d*v-p*S,t[i+2]=d*b+x*S+p*g-m*v,t[i+3]=x*b-p*v-m*g-d*S,t}get x(){return this._x}set x(t){this._x=t,this._onChangeCallback()}get y(){return this._y}set y(t){this._y=t,this._onChangeCallback()}get z(){return this._z}set z(t){this._z=t,this._onChangeCallback()}get w(){return this._w}set w(t){this._w=t,this._onChangeCallback()}set(t,i,s,l){return this._x=t,this._y=i,this._z=s,this._w=l,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._w)}copy(t){return this._x=t.x,this._y=t.y,this._z=t.z,this._w=t.w,this._onChangeCallback(),this}setFromEuler(t,i=!0){const s=t._x,l=t._y,c=t._z,f=t._order,p=Math.cos,m=Math.sin,d=p(s/2),x=p(l/2),v=p(c/2),g=m(s/2),S=m(l/2),b=m(c/2);switch(f){case"XYZ":this._x=g*x*v+d*S*b,this._y=d*S*v-g*x*b,this._z=d*x*b+g*S*v,this._w=d*x*v-g*S*b;break;case"YXZ":this._x=g*x*v+d*S*b,this._y=d*S*v-g*x*b,this._z=d*x*b-g*S*v,this._w=d*x*v+g*S*b;break;case"ZXY":this._x=g*x*v-d*S*b,this._y=d*S*v+g*x*b,this._z=d*x*b+g*S*v,this._w=d*x*v-g*S*b;break;case"ZYX":this._x=g*x*v-d*S*b,this._y=d*S*v+g*x*b,this._z=d*x*b-g*S*v,this._w=d*x*v+g*S*b;break;case"YZX":this._x=g*x*v+d*S*b,this._y=d*S*v+g*x*b,this._z=d*x*b-g*S*v,this._w=d*x*v-g*S*b;break;case"XZY":this._x=g*x*v-d*S*b,this._y=d*S*v-g*x*b,this._z=d*x*b+g*S*v,this._w=d*x*v+g*S*b;break;default:se("Quaternion: .setFromEuler() encountered an unknown order: "+f)}return i===!0&&this._onChangeCallback(),this}setFromAxisAngle(t,i){const s=i/2,l=Math.sin(s);return this._x=t.x*l,this._y=t.y*l,this._z=t.z*l,this._w=Math.cos(s),this._onChangeCallback(),this}setFromRotationMatrix(t){const i=t.elements,s=i[0],l=i[4],c=i[8],f=i[1],p=i[5],m=i[9],d=i[2],x=i[6],v=i[10],g=s+p+v;if(g>0){const S=.5/Math.sqrt(g+1);this._w=.25/S,this._x=(x-m)*S,this._y=(c-d)*S,this._z=(f-l)*S}else if(s>p&&s>v){const S=2*Math.sqrt(1+s-p-v);this._w=(x-m)/S,this._x=.25*S,this._y=(l+f)/S,this._z=(c+d)/S}else if(p>v){const S=2*Math.sqrt(1+p-s-v);this._w=(c-d)/S,this._x=(l+f)/S,this._y=.25*S,this._z=(m+x)/S}else{const S=2*Math.sqrt(1+v-s-p);this._w=(f-l)/S,this._x=(c+d)/S,this._y=(m+x)/S,this._z=.25*S}return this._onChangeCallback(),this}setFromUnitVectors(t,i){let s=t.dot(i)+1;return s<1e-8?(s=0,Math.abs(t.x)>Math.abs(t.z)?(this._x=-t.y,this._y=t.x,this._z=0,this._w=s):(this._x=0,this._y=-t.z,this._z=t.y,this._w=s)):(this._x=t.y*i.z-t.z*i.y,this._y=t.z*i.x-t.x*i.z,this._z=t.x*i.y-t.y*i.x,this._w=s),this.normalize()}angleTo(t){return 2*Math.acos(Math.abs(ge(this.dot(t),-1,1)))}rotateTowards(t,i){const s=this.angleTo(t);if(s===0)return this;const l=Math.min(1,i/s);return this.slerp(t,l),this}identity(){return this.set(0,0,0,1)}invert(){return this.conjugate()}conjugate(){return this._x*=-1,this._y*=-1,this._z*=-1,this._onChangeCallback(),this}dot(t){return this._x*t._x+this._y*t._y+this._z*t._z+this._w*t._w}lengthSq(){return this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w}length(){return Math.sqrt(this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w)}normalize(){let t=this.length();return t===0?(this._x=0,this._y=0,this._z=0,this._w=1):(t=1/t,this._x=this._x*t,this._y=this._y*t,this._z=this._z*t,this._w=this._w*t),this._onChangeCallback(),this}multiply(t){return this.multiplyQuaternions(this,t)}premultiply(t){return this.multiplyQuaternions(t,this)}multiplyQuaternions(t,i){const s=t._x,l=t._y,c=t._z,f=t._w,p=i._x,m=i._y,d=i._z,x=i._w;return this._x=s*x+f*p+l*d-c*m,this._y=l*x+f*m+c*p-s*d,this._z=c*x+f*d+s*m-l*p,this._w=f*x-s*p-l*m-c*d,this._onChangeCallback(),this}slerp(t,i){let s=t._x,l=t._y,c=t._z,f=t._w,p=this.dot(t);p<0&&(s=-s,l=-l,c=-c,f=-f,p=-p);let m=1-i;if(p<.9995){const d=Math.acos(p),x=Math.sin(d);m=Math.sin(m*d)/x,i=Math.sin(i*d)/x,this._x=this._x*m+s*i,this._y=this._y*m+l*i,this._z=this._z*m+c*i,this._w=this._w*m+f*i,this._onChangeCallback()}else this._x=this._x*m+s*i,this._y=this._y*m+l*i,this._z=this._z*m+c*i,this._w=this._w*m+f*i,this.normalize();return this}slerpQuaternions(t,i,s){return this.copy(t).slerp(i,s)}random(){const t=2*Math.PI*Math.random(),i=2*Math.PI*Math.random(),s=Math.random(),l=Math.sqrt(1-s),c=Math.sqrt(s);return this.set(l*Math.sin(t),l*Math.cos(t),c*Math.sin(i),c*Math.cos(i))}equals(t){return t._x===this._x&&t._y===this._y&&t._z===this._z&&t._w===this._w}fromArray(t,i=0){return this._x=t[i],this._y=t[i+1],this._z=t[i+2],this._w=t[i+3],this._onChangeCallback(),this}toArray(t=[],i=0){return t[i]=this._x,t[i+1]=this._y,t[i+2]=this._z,t[i+3]=this._w,t}fromBufferAttribute(t,i){return this._x=t.getX(i),this._y=t.getY(i),this._z=t.getZ(i),this._w=t.getW(i),this._onChangeCallback(),this}toJSON(){return this.toArray()}_onChange(t){return this._onChangeCallback=t,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._w}}const $p=class $p{constructor(t=0,i=0,s=0){this.x=t,this.y=i,this.z=s}set(t,i,s){return s===void 0&&(s=this.z),this.x=t,this.y=i,this.z=s,this}setScalar(t){return this.x=t,this.y=t,this.z=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setZ(t){return this.z=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;case 2:this.z=i;break;default:throw new Error("THREE.Vector3: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:throw new Error("THREE.Vector3: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y,this.z)}copy(t){return this.x=t.x,this.y=t.y,this.z=t.z,this}add(t){return this.x+=t.x,this.y+=t.y,this.z+=t.z,this}addScalar(t){return this.x+=t,this.y+=t,this.z+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this.z=t.z+i.z,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this.z+=t.z*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this.z-=t.z,this}subScalar(t){return this.x-=t,this.y-=t,this.z-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this.z=t.z-i.z,this}multiply(t){return this.x*=t.x,this.y*=t.y,this.z*=t.z,this}multiplyScalar(t){return this.x*=t,this.y*=t,this.z*=t,this}multiplyVectors(t,i){return this.x=t.x*i.x,this.y=t.y*i.y,this.z=t.z*i.z,this}applyEuler(t){return this.applyQuaternion($_.setFromEuler(t))}applyAxisAngle(t,i){return this.applyQuaternion($_.setFromAxisAngle(t,i))}applyMatrix3(t){const i=this.x,s=this.y,l=this.z,c=t.elements;return this.x=c[0]*i+c[3]*s+c[6]*l,this.y=c[1]*i+c[4]*s+c[7]*l,this.z=c[2]*i+c[5]*s+c[8]*l,this}applyNormalMatrix(t){return this.applyMatrix3(t).normalize()}applyMatrix4(t){const i=this.x,s=this.y,l=this.z,c=t.elements,f=1/(c[3]*i+c[7]*s+c[11]*l+c[15]);return this.x=(c[0]*i+c[4]*s+c[8]*l+c[12])*f,this.y=(c[1]*i+c[5]*s+c[9]*l+c[13])*f,this.z=(c[2]*i+c[6]*s+c[10]*l+c[14])*f,this}applyQuaternion(t){const i=this.x,s=this.y,l=this.z,c=t.x,f=t.y,p=t.z,m=t.w,d=2*(f*l-p*s),x=2*(p*i-c*l),v=2*(c*s-f*i);return this.x=i+m*d+f*v-p*x,this.y=s+m*x+p*d-c*v,this.z=l+m*v+c*x-f*d,this}project(t){return this.applyMatrix4(t.matrixWorldInverse).applyMatrix4(t.projectionMatrix)}unproject(t){return this.applyMatrix4(t.projectionMatrixInverse).applyMatrix4(t.matrixWorld)}transformDirection(t){const i=this.x,s=this.y,l=this.z,c=t.elements;return this.x=c[0]*i+c[4]*s+c[8]*l,this.y=c[1]*i+c[5]*s+c[9]*l,this.z=c[2]*i+c[6]*s+c[10]*l,this.normalize()}divide(t){return this.x/=t.x,this.y/=t.y,this.z/=t.z,this}divideScalar(t){return this.multiplyScalar(1/t)}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this.z=Math.min(this.z,t.z),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this.z=Math.max(this.z,t.z),this}clamp(t,i){return this.x=ge(this.x,t.x,i.x),this.y=ge(this.y,t.y,i.y),this.z=ge(this.z,t.z,i.z),this}clampScalar(t,i){return this.x=ge(this.x,t,i),this.y=ge(this.y,t,i),this.z=ge(this.z,t,i),this}clampLength(t,i){const s=this.length();return this.divideScalar(s||1).multiplyScalar(ge(s,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this}dot(t){return this.x*t.x+this.y*t.y+this.z*t.z}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)}normalize(){return this.divideScalar(this.length()||1)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this.z+=(t.z-this.z)*i,this}lerpVectors(t,i,s){return this.x=t.x+(i.x-t.x)*s,this.y=t.y+(i.y-t.y)*s,this.z=t.z+(i.z-t.z)*s,this}cross(t){return this.crossVectors(this,t)}crossVectors(t,i){const s=t.x,l=t.y,c=t.z,f=i.x,p=i.y,m=i.z;return this.x=l*m-c*p,this.y=c*f-s*m,this.z=s*p-l*f,this}projectOnVector(t){const i=t.lengthSq();if(i===0)return this.set(0,0,0);const s=t.dot(this)/i;return this.copy(t).multiplyScalar(s)}projectOnPlane(t){return Zh.copy(this).projectOnVector(t),this.sub(Zh)}reflect(t){return this.sub(Zh.copy(t).multiplyScalar(2*this.dot(t)))}angleTo(t){const i=Math.sqrt(this.lengthSq()*t.lengthSq());if(i===0)return Math.PI/2;const s=this.dot(t)/i;return Math.acos(ge(s,-1,1))}distanceTo(t){return Math.sqrt(this.distanceToSquared(t))}distanceToSquared(t){const i=this.x-t.x,s=this.y-t.y,l=this.z-t.z;return i*i+s*s+l*l}manhattanDistanceTo(t){return Math.abs(this.x-t.x)+Math.abs(this.y-t.y)+Math.abs(this.z-t.z)}setFromSpherical(t){return this.setFromSphericalCoords(t.radius,t.phi,t.theta)}setFromSphericalCoords(t,i,s){const l=Math.sin(i)*t;return this.x=l*Math.sin(s),this.y=Math.cos(i)*t,this.z=l*Math.cos(s),this}setFromCylindrical(t){return this.setFromCylindricalCoords(t.radius,t.theta,t.y)}setFromCylindricalCoords(t,i,s){return this.x=t*Math.sin(i),this.y=s,this.z=t*Math.cos(i),this}setFromMatrixPosition(t){const i=t.elements;return this.x=i[12],this.y=i[13],this.z=i[14],this}setFromMatrixScale(t){const i=this.setFromMatrixColumn(t,0).length(),s=this.setFromMatrixColumn(t,1).length(),l=this.setFromMatrixColumn(t,2).length();return this.x=i,this.y=s,this.z=l,this}setFromMatrixColumn(t,i){return this.fromArray(t.elements,i*4)}setFromMatrix3Column(t,i){return this.fromArray(t.elements,i*3)}setFromEuler(t){return this.x=t._x,this.y=t._y,this.z=t._z,this}setFromColor(t){return this.x=t.r,this.y=t.g,this.z=t.b,this}equals(t){return t.x===this.x&&t.y===this.y&&t.z===this.z}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this.z=t[i+2],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t[i+2]=this.z,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this.z=t.getZ(i),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this}randomDirection(){const t=Math.random()*Math.PI*2,i=Math.random()*2-1,s=Math.sqrt(1-i*i);return this.x=s*Math.cos(t),this.y=i,this.z=s*Math.sin(t),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z}};$p.prototype.isVector3=!0;let k=$p;const Zh=new k,$_=new ro,tm=class tm{constructor(t,i,s,l,c,f,p,m,d){this.elements=[1,0,0,0,1,0,0,0,1],t!==void 0&&this.set(t,i,s,l,c,f,p,m,d)}set(t,i,s,l,c,f,p,m,d){const x=this.elements;return x[0]=t,x[1]=l,x[2]=p,x[3]=i,x[4]=c,x[5]=m,x[6]=s,x[7]=f,x[8]=d,this}identity(){return this.set(1,0,0,0,1,0,0,0,1),this}copy(t){const i=this.elements,s=t.elements;return i[0]=s[0],i[1]=s[1],i[2]=s[2],i[3]=s[3],i[4]=s[4],i[5]=s[5],i[6]=s[6],i[7]=s[7],i[8]=s[8],this}extractBasis(t,i,s){return t.setFromMatrix3Column(this,0),i.setFromMatrix3Column(this,1),s.setFromMatrix3Column(this,2),this}setFromMatrix4(t){const i=t.elements;return this.set(i[0],i[4],i[8],i[1],i[5],i[9],i[2],i[6],i[10]),this}multiply(t){return this.multiplyMatrices(this,t)}premultiply(t){return this.multiplyMatrices(t,this)}multiplyMatrices(t,i){const s=t.elements,l=i.elements,c=this.elements,f=s[0],p=s[3],m=s[6],d=s[1],x=s[4],v=s[7],g=s[2],S=s[5],b=s[8],w=l[0],E=l[3],_=l[6],z=l[1],F=l[4],C=l[7],B=l[2],O=l[5],U=l[8];return c[0]=f*w+p*z+m*B,c[3]=f*E+p*F+m*O,c[6]=f*_+p*C+m*U,c[1]=d*w+x*z+v*B,c[4]=d*E+x*F+v*O,c[7]=d*_+x*C+v*U,c[2]=g*w+S*z+b*B,c[5]=g*E+S*F+b*O,c[8]=g*_+S*C+b*U,this}multiplyScalar(t){const i=this.elements;return i[0]*=t,i[3]*=t,i[6]*=t,i[1]*=t,i[4]*=t,i[7]*=t,i[2]*=t,i[5]*=t,i[8]*=t,this}determinant(){const t=this.elements,i=t[0],s=t[1],l=t[2],c=t[3],f=t[4],p=t[5],m=t[6],d=t[7],x=t[8];return i*f*x-i*p*d-s*c*x+s*p*m+l*c*d-l*f*m}invert(){const t=this.elements,i=t[0],s=t[1],l=t[2],c=t[3],f=t[4],p=t[5],m=t[6],d=t[7],x=t[8],v=x*f-p*d,g=p*m-x*c,S=d*c-f*m,b=i*v+s*g+l*S;if(b===0)return this.set(0,0,0,0,0,0,0,0,0);const w=1/b;return t[0]=v*w,t[1]=(l*d-x*s)*w,t[2]=(p*s-l*f)*w,t[3]=g*w,t[4]=(x*i-l*m)*w,t[5]=(l*c-p*i)*w,t[6]=S*w,t[7]=(s*m-d*i)*w,t[8]=(f*i-s*c)*w,this}transpose(){let t;const i=this.elements;return t=i[1],i[1]=i[3],i[3]=t,t=i[2],i[2]=i[6],i[6]=t,t=i[5],i[5]=i[7],i[7]=t,this}getNormalMatrix(t){return this.setFromMatrix4(t).invert().transpose()}transposeIntoArray(t){const i=this.elements;return t[0]=i[0],t[1]=i[3],t[2]=i[6],t[3]=i[1],t[4]=i[4],t[5]=i[7],t[6]=i[2],t[7]=i[5],t[8]=i[8],this}setUvTransform(t,i,s,l,c,f,p){const m=Math.cos(c),d=Math.sin(c);return this.set(s*m,s*d,-s*(m*f+d*p)+f+t,-l*d,l*m,-l*(-d*f+m*p)+p+i,0,0,1),this}scale(t,i){return Jr("Matrix3: .scale() is deprecated. Use .makeScale() instead."),this.premultiply(Kh.makeScale(t,i)),this}rotate(t){return Jr("Matrix3: .rotate() is deprecated. Use .makeRotation() instead."),this.premultiply(Kh.makeRotation(-t)),this}translate(t,i){return Jr("Matrix3: .translate() is deprecated. Use .makeTranslation() instead."),this.premultiply(Kh.makeTranslation(t,i)),this}makeTranslation(t,i){return t.isVector2?this.set(1,0,t.x,0,1,t.y,0,0,1):this.set(1,0,t,0,1,i,0,0,1),this}makeRotation(t){const i=Math.cos(t),s=Math.sin(t);return this.set(i,-s,0,s,i,0,0,0,1),this}makeScale(t,i){return this.set(t,0,0,0,i,0,0,0,1),this}equals(t){const i=this.elements,s=t.elements;for(let l=0;l<9;l++)if(i[l]!==s[l])return!1;return!0}fromArray(t,i=0){for(let s=0;s<9;s++)this.elements[s]=t[s+i];return this}toArray(t=[],i=0){const s=this.elements;return t[i]=s[0],t[i+1]=s[1],t[i+2]=s[2],t[i+3]=s[3],t[i+4]=s[4],t[i+5]=s[5],t[i+6]=s[6],t[i+7]=s[7],t[i+8]=s[8],t}clone(){return new this.constructor().fromArray(this.elements)}};tm.prototype.isMatrix3=!0;let ue=tm;const Kh=new ue,tv=new ue().set(.4123908,.3575843,.1804808,.212639,.7151687,.0721923,.0193308,.1191948,.9505322),ev=new ue().set(3.2409699,-1.5373832,-.4986108,-.9692436,1.8759675,.0415551,.0556301,-.203977,1.0569715);function OE(){const r={enabled:!0,workingColorSpace:Mu,spaces:{},convert:function(l,c,f){return this.enabled===!1||c===f||!c||!f||(this.spaces[c].transfer===ke&&(l.r=wa(l.r),l.g=wa(l.g),l.b=wa(l.b)),this.spaces[c].primaries!==this.spaces[f].primaries&&(l.applyMatrix3(this.spaces[c].toXYZ),l.applyMatrix3(this.spaces[f].fromXYZ)),this.spaces[f].transfer===ke&&(l.r=to(l.r),l.g=to(l.g),l.b=to(l.b))),l},workingToColorSpace:function(l,c){return this.convert(l,this.workingColorSpace,c)},colorSpaceToWorking:function(l,c){return this.convert(l,c,this.workingColorSpace)},getPrimaries:function(l){return this.spaces[l].primaries},getTransfer:function(l){return l===us?Eu:this.spaces[l].transfer},getToneMappingMode:function(l){return this.spaces[l].outputColorSpaceConfig.toneMappingMode||"standard"},getLuminanceCoefficients:function(l,c=this.workingColorSpace){return l.fromArray(this.spaces[c].luminanceCoefficients)},define:function(l){Object.assign(this.spaces,l)},_getMatrix:function(l,c,f){return l.copy(this.spaces[c].toXYZ).multiply(this.spaces[f].fromXYZ)},_getDrawingBufferColorSpace:function(l){return this.spaces[l].outputColorSpaceConfig.drawingBufferColorSpace},_getUnpackColorSpace:function(l=this.workingColorSpace){return this.spaces[l].workingColorSpaceConfig.unpackColorSpace},fromWorkingColorSpace:function(l,c){return Jr("ColorManagement: .fromWorkingColorSpace() has been renamed to .workingToColorSpace()."),r.workingToColorSpace(l,c)},toWorkingColorSpace:function(l,c){return Jr("ColorManagement: .toWorkingColorSpace() has been renamed to .colorSpaceToWorking()."),r.colorSpaceToWorking(l,c)}},t=[.64,.33,.3,.6,.15,.06],i=[.2126,.7152,.0722],s=[.3127,.329];return r.define({[Mu]:{primaries:t,whitePoint:s,transfer:Eu,toXYZ:tv,fromXYZ:ev,luminanceCoefficients:i,workingColorSpaceConfig:{unpackColorSpace:qn},outputColorSpaceConfig:{drawingBufferColorSpace:qn}},[qn]:{primaries:t,whitePoint:s,transfer:ke,toXYZ:tv,fromXYZ:ev,luminanceCoefficients:i,outputColorSpaceConfig:{drawingBufferColorSpace:qn}}}),r}const Re=OE();function wa(r){return r<.04045?r*.0773993808:Math.pow(r*.9478672986+.0521327014,2.4)}function to(r){return r<.0031308?r*12.92:1.055*Math.pow(r,.41666)-.055}let Nr;class PE{static getDataURL(t,i="image/png"){if(/^data:/i.test(t.src)||typeof HTMLCanvasElement>"u")return t.src;let s;if(t instanceof HTMLCanvasElement)s=t;else{Nr===void 0&&(Nr=bu("canvas")),Nr.width=t.width,Nr.height=t.height;const l=Nr.getContext("2d");t instanceof ImageData?l.putImageData(t,0,0):l.drawImage(t,0,0,t.width,t.height),s=Nr}return s.toDataURL(i)}static sRGBToLinear(t){if(typeof HTMLImageElement<"u"&&t instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&t instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&t instanceof ImageBitmap){const i=bu("canvas");i.width=t.width,i.height=t.height;const s=i.getContext("2d");s.drawImage(t,0,0,t.width,t.height);const l=s.getImageData(0,0,t.width,t.height),c=l.data;for(let f=0;f<c.length;f++)c[f]=wa(c[f]/255)*255;return s.putImageData(l,0,0),i}else if(t.data){const i=t.data.slice(0);for(let s=0;s<i.length;s++)i instanceof Uint8Array||i instanceof Uint8ClampedArray?i[s]=Math.floor(wa(i[s]/255)*255):i[s]=wa(i[s]);return{data:i,width:t.width,height:t.height}}else return se("ImageUtils.sRGBToLinear(): Unsupported image type. No color space conversion applied."),t}}let zE=0;class Gp{constructor(t=null){this.isSource=!0,Object.defineProperty(this,"id",{value:zE++}),this.uuid=Ra(),this.data=t,this.dataReady=!0,this.version=0}getSize(t){const i=this.data;return typeof HTMLVideoElement<"u"&&i instanceof HTMLVideoElement?t.set(i.videoWidth,i.videoHeight,0):typeof VideoFrame<"u"&&i instanceof VideoFrame?t.set(i.displayWidth,i.displayHeight,0):i!==null?t.set(i.width,i.height,i.depth||0):t.set(0,0,0),t}set needsUpdate(t){t===!0&&this.version++}toJSON(t){const i=t===void 0||typeof t=="string";if(!i&&t.images[this.uuid]!==void 0)return t.images[this.uuid];const s={uuid:this.uuid,url:""},l=this.data;if(l!==null){let c;if(Array.isArray(l)){c=[];for(let f=0,p=l.length;f<p;f++)l[f].isDataTexture?c.push(Qh(l[f].image)):c.push(Qh(l[f]))}else c=Qh(l);s.url=c}return i||(t.images[this.uuid]=s),s}}function Qh(r){return typeof HTMLImageElement<"u"&&r instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&r instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&r instanceof ImageBitmap?PE.getDataURL(r):r.data?{data:Array.from(r.data),width:r.width,height:r.height,type:r.data.constructor.name}:(se("Texture: Unable to serialize Texture."),{})}let BE=0;const Jh=new k;class Vn extends Ks{constructor(t=Vn.DEFAULT_IMAGE,i=Vn.DEFAULT_MAPPING,s=Ta,l=Ta,c=Gn,f=Xs,p=Fi,m=gi,d=Vn.DEFAULT_ANISOTROPY,x=us){super(),this.isTexture=!0,Object.defineProperty(this,"id",{value:BE++}),this.uuid=Ra(),this.name="",this.source=new Gp(t),this.mipmaps=[],this.mapping=i,this.channel=0,this.wrapS=s,this.wrapT=l,this.magFilter=c,this.minFilter=f,this.anisotropy=d,this.format=p,this.internalFormat=null,this.type=m,this.offset=new oe(0,0),this.repeat=new oe(1,1),this.center=new oe(0,0),this.rotation=0,this.matrixAutoUpdate=!0,this.matrix=new ue,this.generateMipmaps=!0,this.premultiplyAlpha=!1,this.flipY=!0,this.unpackAlignment=4,this.colorSpace=x,this.userData={},this.updateRanges=[],this.version=0,this.onUpdate=null,this.renderTarget=null,this.isRenderTargetTexture=!1,this.isArrayTexture=!!(t&&t.depth&&t.depth>1),this.pmremVersion=0,this.normalized=!1}get width(){return this.source.getSize(Jh).x}get height(){return this.source.getSize(Jh).y}get depth(){return this.source.getSize(Jh).z}get image(){return this.source.data}set image(t){this.source.data=t}updateMatrix(){this.matrix.setUvTransform(this.offset.x,this.offset.y,this.repeat.x,this.repeat.y,this.rotation,this.center.x,this.center.y)}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}clone(){return new this.constructor().copy(this)}copy(t){return this.name=t.name,this.source=t.source,this.mipmaps=t.mipmaps.slice(0),this.mapping=t.mapping,this.channel=t.channel,this.wrapS=t.wrapS,this.wrapT=t.wrapT,this.magFilter=t.magFilter,this.minFilter=t.minFilter,this.anisotropy=t.anisotropy,this.format=t.format,this.internalFormat=t.internalFormat,this.type=t.type,this.normalized=t.normalized,this.offset.copy(t.offset),this.repeat.copy(t.repeat),this.center.copy(t.center),this.rotation=t.rotation,this.matrixAutoUpdate=t.matrixAutoUpdate,this.matrix.copy(t.matrix),this.generateMipmaps=t.generateMipmaps,this.premultiplyAlpha=t.premultiplyAlpha,this.flipY=t.flipY,this.unpackAlignment=t.unpackAlignment,this.colorSpace=t.colorSpace,this.renderTarget=t.renderTarget,this.isRenderTargetTexture=t.isRenderTargetTexture,this.isArrayTexture=t.isArrayTexture,this.userData=JSON.parse(JSON.stringify(t.userData)),this.needsUpdate=!0,this}setValues(t){for(const i in t){const s=t[i];if(s===void 0){se(`Texture.setValues(): parameter '${i}' has value of undefined.`);continue}const l=this[i];if(l===void 0){se(`Texture.setValues(): property '${i}' does not exist.`);continue}l&&s&&l.isVector2&&s.isVector2||l&&s&&l.isVector3&&s.isVector3||l&&s&&l.isMatrix3&&s.isMatrix3?l.copy(s):this[i]=s}}toJSON(t){const i=t===void 0||typeof t=="string";if(!i&&t.textures[this.uuid]!==void 0)return t.textures[this.uuid];const s={metadata:{version:4.7,type:"Texture",generator:"Texture.toJSON"},uuid:this.uuid,name:this.name,image:this.source.toJSON(t).uuid,mapping:this.mapping,channel:this.channel,repeat:[this.repeat.x,this.repeat.y],offset:[this.offset.x,this.offset.y],center:[this.center.x,this.center.y],rotation:this.rotation,wrap:[this.wrapS,this.wrapT],format:this.format,internalFormat:this.internalFormat,type:this.type,normalized:this.normalized,colorSpace:this.colorSpace,minFilter:this.minFilter,magFilter:this.magFilter,anisotropy:this.anisotropy,flipY:this.flipY,generateMipmaps:this.generateMipmaps,premultiplyAlpha:this.premultiplyAlpha,unpackAlignment:this.unpackAlignment};return Object.keys(this.userData).length>0&&(s.userData=this.userData),i||(t.textures[this.uuid]=s),s}dispose(){this.dispatchEvent({type:"dispose"})}transformUv(t){if(this.mapping!==Ex)return t;if(t.applyMatrix3(this.matrix),t.x<0||t.x>1)switch(this.wrapS){case Gd:t.x=t.x-Math.floor(t.x);break;case Ta:t.x=t.x<0?0:1;break;case Vd:Math.abs(Math.floor(t.x)%2)===1?t.x=Math.ceil(t.x)-t.x:t.x=t.x-Math.floor(t.x);break}if(t.y<0||t.y>1)switch(this.wrapT){case Gd:t.y=t.y-Math.floor(t.y);break;case Ta:t.y=t.y<0?0:1;break;case Vd:Math.abs(Math.floor(t.y)%2)===1?t.y=Math.ceil(t.y)-t.y:t.y=t.y-Math.floor(t.y);break}return this.flipY&&(t.y=1-t.y),t}set needsUpdate(t){t===!0&&(this.version++,this.source.needsUpdate=!0)}set needsPMREMUpdate(t){t===!0&&this.pmremVersion++}}Vn.DEFAULT_IMAGE=null;Vn.DEFAULT_MAPPING=Ex;Vn.DEFAULT_ANISOTROPY=1;const em=class em{constructor(t=0,i=0,s=0,l=1){this.x=t,this.y=i,this.z=s,this.w=l}get width(){return this.z}set width(t){this.z=t}get height(){return this.w}set height(t){this.w=t}set(t,i,s,l){return this.x=t,this.y=i,this.z=s,this.w=l,this}setScalar(t){return this.x=t,this.y=t,this.z=t,this.w=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setZ(t){return this.z=t,this}setW(t){return this.w=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;case 2:this.z=i;break;case 3:this.w=i;break;default:throw new Error("THREE.Vector4: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;case 2:return this.z;case 3:return this.w;default:throw new Error("THREE.Vector4: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y,this.z,this.w)}copy(t){return this.x=t.x,this.y=t.y,this.z=t.z,this.w=t.w!==void 0?t.w:1,this}add(t){return this.x+=t.x,this.y+=t.y,this.z+=t.z,this.w+=t.w,this}addScalar(t){return this.x+=t,this.y+=t,this.z+=t,this.w+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this.z=t.z+i.z,this.w=t.w+i.w,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this.z+=t.z*i,this.w+=t.w*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this.z-=t.z,this.w-=t.w,this}subScalar(t){return this.x-=t,this.y-=t,this.z-=t,this.w-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this.z=t.z-i.z,this.w=t.w-i.w,this}multiply(t){return this.x*=t.x,this.y*=t.y,this.z*=t.z,this.w*=t.w,this}multiplyScalar(t){return this.x*=t,this.y*=t,this.z*=t,this.w*=t,this}applyMatrix4(t){const i=this.x,s=this.y,l=this.z,c=this.w,f=t.elements;return this.x=f[0]*i+f[4]*s+f[8]*l+f[12]*c,this.y=f[1]*i+f[5]*s+f[9]*l+f[13]*c,this.z=f[2]*i+f[6]*s+f[10]*l+f[14]*c,this.w=f[3]*i+f[7]*s+f[11]*l+f[15]*c,this}divide(t){return this.x/=t.x,this.y/=t.y,this.z/=t.z,this.w/=t.w,this}divideScalar(t){return this.multiplyScalar(1/t)}setAxisAngleFromQuaternion(t){this.w=2*Math.acos(t.w);const i=Math.sqrt(1-t.w*t.w);return i<1e-4?(this.x=1,this.y=0,this.z=0):(this.x=t.x/i,this.y=t.y/i,this.z=t.z/i),this}setAxisAngleFromRotationMatrix(t){let i,s,l,c;const m=t.elements,d=m[0],x=m[4],v=m[8],g=m[1],S=m[5],b=m[9],w=m[2],E=m[6],_=m[10];if(Math.abs(x-g)<.01&&Math.abs(v-w)<.01&&Math.abs(b-E)<.01){if(Math.abs(x+g)<.1&&Math.abs(v+w)<.1&&Math.abs(b+E)<.1&&Math.abs(d+S+_-3)<.1)return this.set(1,0,0,0),this;i=Math.PI;const F=(d+1)/2,C=(S+1)/2,B=(_+1)/2,O=(x+g)/4,U=(v+w)/4,T=(b+E)/4;return F>C&&F>B?F<.01?(s=0,l=.707106781,c=.707106781):(s=Math.sqrt(F),l=O/s,c=U/s):C>B?C<.01?(s=.707106781,l=0,c=.707106781):(l=Math.sqrt(C),s=O/l,c=T/l):B<.01?(s=.707106781,l=.707106781,c=0):(c=Math.sqrt(B),s=U/c,l=T/c),this.set(s,l,c,i),this}let z=Math.sqrt((E-b)*(E-b)+(v-w)*(v-w)+(g-x)*(g-x));return Math.abs(z)<.001&&(z=1),this.x=(E-b)/z,this.y=(v-w)/z,this.z=(g-x)/z,this.w=Math.acos((d+S+_-1)/2),this}setFromMatrixPosition(t){const i=t.elements;return this.x=i[12],this.y=i[13],this.z=i[14],this.w=i[15],this}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this.z=Math.min(this.z,t.z),this.w=Math.min(this.w,t.w),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this.z=Math.max(this.z,t.z),this.w=Math.max(this.w,t.w),this}clamp(t,i){return this.x=ge(this.x,t.x,i.x),this.y=ge(this.y,t.y,i.y),this.z=ge(this.z,t.z,i.z),this.w=ge(this.w,t.w,i.w),this}clampScalar(t,i){return this.x=ge(this.x,t,i),this.y=ge(this.y,t,i),this.z=ge(this.z,t,i),this.w=ge(this.w,t,i),this}clampLength(t,i){const s=this.length();return this.divideScalar(s||1).multiplyScalar(ge(s,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this.w=Math.floor(this.w),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this.w=Math.ceil(this.w),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this.w=Math.round(this.w),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this.w=Math.trunc(this.w),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this.w=-this.w,this}dot(t){return this.x*t.x+this.y*t.y+this.z*t.z+this.w*t.w}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)+Math.abs(this.w)}normalize(){return this.divideScalar(this.length()||1)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this.z+=(t.z-this.z)*i,this.w+=(t.w-this.w)*i,this}lerpVectors(t,i,s){return this.x=t.x+(i.x-t.x)*s,this.y=t.y+(i.y-t.y)*s,this.z=t.z+(i.z-t.z)*s,this.w=t.w+(i.w-t.w)*s,this}equals(t){return t.x===this.x&&t.y===this.y&&t.z===this.z&&t.w===this.w}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this.z=t[i+2],this.w=t[i+3],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t[i+2]=this.z,t[i+3]=this.w,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this.z=t.getZ(i),this.w=t.getW(i),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this.w=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z,yield this.w}};em.prototype.isVector4=!0;let cn=em;class IE extends Ks{constructor(t=1,i=1,s={}){super(),s=Object.assign({generateMipmaps:!1,internalFormat:null,minFilter:Gn,depthBuffer:!0,stencilBuffer:!1,resolveDepthBuffer:!0,resolveStencilBuffer:!0,depthTexture:null,samples:0,count:1,depth:1,multiview:!1,useArrayDepthTexture:!1},s),this.isRenderTarget=!0,this.width=t,this.height=i,this.depth=s.depth,this.scissor=new cn(0,0,t,i),this.scissorTest=!1,this.viewport=new cn(0,0,t,i),this.textures=[];const l={width:t,height:i,depth:s.depth},c=new Vn(l),f=s.count;for(let p=0;p<f;p++)this.textures[p]=c.clone(),this.textures[p].isRenderTargetTexture=!0,this.textures[p].renderTarget=this;this._setTextureOptions(s),this.depthBuffer=s.depthBuffer,this.stencilBuffer=s.stencilBuffer,this.resolveDepthBuffer=s.resolveDepthBuffer,this.resolveStencilBuffer=s.resolveStencilBuffer,this._depthTexture=null,this.depthTexture=s.depthTexture,this.samples=s.samples,this.multiview=s.multiview,this.useArrayDepthTexture=s.useArrayDepthTexture}_setTextureOptions(t={}){const i={minFilter:Gn,generateMipmaps:!1,flipY:!1,internalFormat:null};t.mapping!==void 0&&(i.mapping=t.mapping),t.wrapS!==void 0&&(i.wrapS=t.wrapS),t.wrapT!==void 0&&(i.wrapT=t.wrapT),t.wrapR!==void 0&&(i.wrapR=t.wrapR),t.magFilter!==void 0&&(i.magFilter=t.magFilter),t.minFilter!==void 0&&(i.minFilter=t.minFilter),t.format!==void 0&&(i.format=t.format),t.type!==void 0&&(i.type=t.type),t.anisotropy!==void 0&&(i.anisotropy=t.anisotropy),t.colorSpace!==void 0&&(i.colorSpace=t.colorSpace),t.flipY!==void 0&&(i.flipY=t.flipY),t.generateMipmaps!==void 0&&(i.generateMipmaps=t.generateMipmaps),t.internalFormat!==void 0&&(i.internalFormat=t.internalFormat);for(let s=0;s<this.textures.length;s++)this.textures[s].setValues(i)}get texture(){return this.textures[0]}set texture(t){this.textures[0]=t}set depthTexture(t){this._depthTexture!==null&&(this._depthTexture.renderTarget=null),t!==null&&(t.renderTarget=this),this._depthTexture=t}get depthTexture(){return this._depthTexture}setSize(t,i,s=1){if(this.width!==t||this.height!==i||this.depth!==s){this.width=t,this.height=i,this.depth=s;for(let l=0,c=this.textures.length;l<c;l++)this.textures[l].image.width=t,this.textures[l].image.height=i,this.textures[l].image.depth=s,this.textures[l].isData3DTexture!==!0&&(this.textures[l].isArrayTexture=this.textures[l].image.depth>1);this.dispose()}this.viewport.set(0,0,t,i),this.scissor.set(0,0,t,i)}clone(){return new this.constructor().copy(this)}copy(t){this.width=t.width,this.height=t.height,this.depth=t.depth,this.scissor.copy(t.scissor),this.scissorTest=t.scissorTest,this.viewport.copy(t.viewport),this.textures.length=0;for(let i=0,s=t.textures.length;i<s;i++){this.textures[i]=t.textures[i].clone(),this.textures[i].isRenderTargetTexture=!0,this.textures[i].renderTarget=this;const l=Object.assign({},t.textures[i].image);this.textures[i].source=new Gp(l)}return this.depthBuffer=t.depthBuffer,this.stencilBuffer=t.stencilBuffer,this.resolveDepthBuffer=t.resolveDepthBuffer,this.resolveStencilBuffer=t.resolveStencilBuffer,t.depthTexture!==null&&(this.depthTexture=t.depthTexture.clone()),this.samples=t.samples,this.multiview=t.multiview,this.useArrayDepthTexture=t.useArrayDepthTexture,this}dispose(){this.dispatchEvent({type:"dispose"})}}class Qi extends IE{constructor(t=1,i=1,s={}){super(t,i,s),this.isWebGLRenderTarget=!0}}class Ux extends Vn{constructor(t=null,i=1,s=1,l=1){super(null),this.isDataArrayTexture=!0,this.image={data:t,width:i,height:s,depth:l},this.magFilter=zn,this.minFilter=zn,this.wrapR=Ta,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1,this.layerUpdates=new Set}addLayerUpdate(t){this.layerUpdates.add(t)}clearLayerUpdates(){this.layerUpdates.clear()}}class FE extends Vn{constructor(t=null,i=1,s=1,l=1){super(null),this.isData3DTexture=!0,this.image={data:t,width:i,height:s,depth:l},this.magFilter=zn,this.minFilter=zn,this.wrapR=Ta,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}const Cu=class Cu{constructor(t,i,s,l,c,f,p,m,d,x,v,g,S,b,w,E){this.elements=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],t!==void 0&&this.set(t,i,s,l,c,f,p,m,d,x,v,g,S,b,w,E)}set(t,i,s,l,c,f,p,m,d,x,v,g,S,b,w,E){const _=this.elements;return _[0]=t,_[4]=i,_[8]=s,_[12]=l,_[1]=c,_[5]=f,_[9]=p,_[13]=m,_[2]=d,_[6]=x,_[10]=v,_[14]=g,_[3]=S,_[7]=b,_[11]=w,_[15]=E,this}identity(){return this.set(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1),this}clone(){return new Cu().fromArray(this.elements)}copy(t){const i=this.elements,s=t.elements;return i[0]=s[0],i[1]=s[1],i[2]=s[2],i[3]=s[3],i[4]=s[4],i[5]=s[5],i[6]=s[6],i[7]=s[7],i[8]=s[8],i[9]=s[9],i[10]=s[10],i[11]=s[11],i[12]=s[12],i[13]=s[13],i[14]=s[14],i[15]=s[15],this}copyPosition(t){const i=this.elements,s=t.elements;return i[12]=s[12],i[13]=s[13],i[14]=s[14],this}setFromMatrix3(t){const i=t.elements;return this.set(i[0],i[3],i[6],0,i[1],i[4],i[7],0,i[2],i[5],i[8],0,0,0,0,1),this}extractBasis(t,i,s){return this.determinantAffine()===0?(t.set(1,0,0),i.set(0,1,0),s.set(0,0,1),this):(t.setFromMatrixColumn(this,0),i.setFromMatrixColumn(this,1),s.setFromMatrixColumn(this,2),this)}makeBasis(t,i,s){return this.set(t.x,i.x,s.x,0,t.y,i.y,s.y,0,t.z,i.z,s.z,0,0,0,0,1),this}extractRotation(t){if(t.determinantAffine()===0)return this.identity();const i=this.elements,s=t.elements,l=1/Lr.setFromMatrixColumn(t,0).length(),c=1/Lr.setFromMatrixColumn(t,1).length(),f=1/Lr.setFromMatrixColumn(t,2).length();return i[0]=s[0]*l,i[1]=s[1]*l,i[2]=s[2]*l,i[3]=0,i[4]=s[4]*c,i[5]=s[5]*c,i[6]=s[6]*c,i[7]=0,i[8]=s[8]*f,i[9]=s[9]*f,i[10]=s[10]*f,i[11]=0,i[12]=0,i[13]=0,i[14]=0,i[15]=1,this}makeRotationFromEuler(t){const i=this.elements,s=t.x,l=t.y,c=t.z,f=Math.cos(s),p=Math.sin(s),m=Math.cos(l),d=Math.sin(l),x=Math.cos(c),v=Math.sin(c);if(t.order==="XYZ"){const g=f*x,S=f*v,b=p*x,w=p*v;i[0]=m*x,i[4]=-m*v,i[8]=d,i[1]=S+b*d,i[5]=g-w*d,i[9]=-p*m,i[2]=w-g*d,i[6]=b+S*d,i[10]=f*m}else if(t.order==="YXZ"){const g=m*x,S=m*v,b=d*x,w=d*v;i[0]=g+w*p,i[4]=b*p-S,i[8]=f*d,i[1]=f*v,i[5]=f*x,i[9]=-p,i[2]=S*p-b,i[6]=w+g*p,i[10]=f*m}else if(t.order==="ZXY"){const g=m*x,S=m*v,b=d*x,w=d*v;i[0]=g-w*p,i[4]=-f*v,i[8]=b+S*p,i[1]=S+b*p,i[5]=f*x,i[9]=w-g*p,i[2]=-f*d,i[6]=p,i[10]=f*m}else if(t.order==="ZYX"){const g=f*x,S=f*v,b=p*x,w=p*v;i[0]=m*x,i[4]=b*d-S,i[8]=g*d+w,i[1]=m*v,i[5]=w*d+g,i[9]=S*d-b,i[2]=-d,i[6]=p*m,i[10]=f*m}else if(t.order==="YZX"){const g=f*m,S=f*d,b=p*m,w=p*d;i[0]=m*x,i[4]=w-g*v,i[8]=b*v+S,i[1]=v,i[5]=f*x,i[9]=-p*x,i[2]=-d*x,i[6]=S*v+b,i[10]=g-w*v}else if(t.order==="XZY"){const g=f*m,S=f*d,b=p*m,w=p*d;i[0]=m*x,i[4]=-v,i[8]=d*x,i[1]=g*v+w,i[5]=f*x,i[9]=S*v-b,i[2]=b*v-S,i[6]=p*x,i[10]=w*v+g}return i[3]=0,i[7]=0,i[11]=0,i[12]=0,i[13]=0,i[14]=0,i[15]=1,this}makeRotationFromQuaternion(t){return this.compose(HE,t,GE)}lookAt(t,i,s){const l=this.elements;return hi.subVectors(t,i),hi.lengthSq()===0&&(hi.z=1),hi.normalize(),as.crossVectors(s,hi),as.lengthSq()===0&&(Math.abs(s.z)===1?hi.x+=1e-4:hi.z+=1e-4,hi.normalize(),as.crossVectors(s,hi)),as.normalize(),Pc.crossVectors(hi,as),l[0]=as.x,l[4]=Pc.x,l[8]=hi.x,l[1]=as.y,l[5]=Pc.y,l[9]=hi.y,l[2]=as.z,l[6]=Pc.z,l[10]=hi.z,this}multiply(t){return this.multiplyMatrices(this,t)}premultiply(t){return this.multiplyMatrices(t,this)}multiplyMatrices(t,i){const s=t.elements,l=i.elements,c=this.elements,f=s[0],p=s[4],m=s[8],d=s[12],x=s[1],v=s[5],g=s[9],S=s[13],b=s[2],w=s[6],E=s[10],_=s[14],z=s[3],F=s[7],C=s[11],B=s[15],O=l[0],U=l[4],T=l[8],P=l[12],$=l[1],Y=l[5],J=l[9],dt=l[13],gt=l[2],et=l[6],I=l[10],H=l[14],q=l[3],ht=l[7],Et=l[11],L=l[15];return c[0]=f*O+p*$+m*gt+d*q,c[4]=f*U+p*Y+m*et+d*ht,c[8]=f*T+p*J+m*I+d*Et,c[12]=f*P+p*dt+m*H+d*L,c[1]=x*O+v*$+g*gt+S*q,c[5]=x*U+v*Y+g*et+S*ht,c[9]=x*T+v*J+g*I+S*Et,c[13]=x*P+v*dt+g*H+S*L,c[2]=b*O+w*$+E*gt+_*q,c[6]=b*U+w*Y+E*et+_*ht,c[10]=b*T+w*J+E*I+_*Et,c[14]=b*P+w*dt+E*H+_*L,c[3]=z*O+F*$+C*gt+B*q,c[7]=z*U+F*Y+C*et+B*ht,c[11]=z*T+F*J+C*I+B*Et,c[15]=z*P+F*dt+C*H+B*L,this}multiplyScalar(t){const i=this.elements;return i[0]*=t,i[4]*=t,i[8]*=t,i[12]*=t,i[1]*=t,i[5]*=t,i[9]*=t,i[13]*=t,i[2]*=t,i[6]*=t,i[10]*=t,i[14]*=t,i[3]*=t,i[7]*=t,i[11]*=t,i[15]*=t,this}determinant(){const t=this.elements,i=t[0],s=t[4],l=t[8],c=t[12],f=t[1],p=t[5],m=t[9],d=t[13],x=t[2],v=t[6],g=t[10],S=t[14],b=t[3],w=t[7],E=t[11],_=t[15],z=m*S-d*g,F=p*S-d*v,C=p*g-m*v,B=f*S-d*x,O=f*g-m*x,U=f*v-p*x;return i*(w*z-E*F+_*C)-s*(b*z-E*B+_*O)+l*(b*F-w*B+_*U)-c*(b*C-w*O+E*U)}determinantAffine(){const t=this.elements,i=t[0],s=t[4],l=t[8],c=t[1],f=t[5],p=t[9],m=t[2],d=t[6],x=t[10];return i*(f*x-p*d)-s*(c*x-p*m)+l*(c*d-f*m)}transpose(){const t=this.elements;let i;return i=t[1],t[1]=t[4],t[4]=i,i=t[2],t[2]=t[8],t[8]=i,i=t[6],t[6]=t[9],t[9]=i,i=t[3],t[3]=t[12],t[12]=i,i=t[7],t[7]=t[13],t[13]=i,i=t[11],t[11]=t[14],t[14]=i,this}setPosition(t,i,s){const l=this.elements;return t.isVector3?(l[12]=t.x,l[13]=t.y,l[14]=t.z):(l[12]=t,l[13]=i,l[14]=s),this}invert(){const t=this.elements,i=t[0],s=t[1],l=t[2],c=t[3],f=t[4],p=t[5],m=t[6],d=t[7],x=t[8],v=t[9],g=t[10],S=t[11],b=t[12],w=t[13],E=t[14],_=t[15],z=i*p-s*f,F=i*m-l*f,C=i*d-c*f,B=s*m-l*p,O=s*d-c*p,U=l*d-c*m,T=x*w-v*b,P=x*E-g*b,$=x*_-S*b,Y=v*E-g*w,J=v*_-S*w,dt=g*_-S*E,gt=z*dt-F*J+C*Y+B*$-O*P+U*T;if(gt===0)return this.set(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);const et=1/gt;return t[0]=(p*dt-m*J+d*Y)*et,t[1]=(l*J-s*dt-c*Y)*et,t[2]=(w*U-E*O+_*B)*et,t[3]=(g*O-v*U-S*B)*et,t[4]=(m*$-f*dt-d*P)*et,t[5]=(i*dt-l*$+c*P)*et,t[6]=(E*C-b*U-_*F)*et,t[7]=(x*U-g*C+S*F)*et,t[8]=(f*J-p*$+d*T)*et,t[9]=(s*$-i*J-c*T)*et,t[10]=(b*O-w*C+_*z)*et,t[11]=(v*C-x*O-S*z)*et,t[12]=(p*P-f*Y-m*T)*et,t[13]=(i*Y-s*P+l*T)*et,t[14]=(w*F-b*B-E*z)*et,t[15]=(x*B-v*F+g*z)*et,this}scale(t){const i=this.elements,s=t.x,l=t.y,c=t.z;return i[0]*=s,i[4]*=l,i[8]*=c,i[1]*=s,i[5]*=l,i[9]*=c,i[2]*=s,i[6]*=l,i[10]*=c,i[3]*=s,i[7]*=l,i[11]*=c,this}getMaxScaleOnAxis(){const t=this.elements,i=t[0]*t[0]+t[1]*t[1]+t[2]*t[2],s=t[4]*t[4]+t[5]*t[5]+t[6]*t[6],l=t[8]*t[8]+t[9]*t[9]+t[10]*t[10];return Math.sqrt(Math.max(i,s,l))}makeTranslation(t,i,s){return t.isVector3?this.set(1,0,0,t.x,0,1,0,t.y,0,0,1,t.z,0,0,0,1):this.set(1,0,0,t,0,1,0,i,0,0,1,s,0,0,0,1),this}makeRotationX(t){const i=Math.cos(t),s=Math.sin(t);return this.set(1,0,0,0,0,i,-s,0,0,s,i,0,0,0,0,1),this}makeRotationY(t){const i=Math.cos(t),s=Math.sin(t);return this.set(i,0,s,0,0,1,0,0,-s,0,i,0,0,0,0,1),this}makeRotationZ(t){const i=Math.cos(t),s=Math.sin(t);return this.set(i,-s,0,0,s,i,0,0,0,0,1,0,0,0,0,1),this}makeRotationAxis(t,i){const s=Math.cos(i),l=Math.sin(i),c=1-s,f=t.x,p=t.y,m=t.z,d=c*f,x=c*p;return this.set(d*f+s,d*p-l*m,d*m+l*p,0,d*p+l*m,x*p+s,x*m-l*f,0,d*m-l*p,x*m+l*f,c*m*m+s,0,0,0,0,1),this}makeScale(t,i,s){return this.set(t,0,0,0,0,i,0,0,0,0,s,0,0,0,0,1),this}makeShear(t,i,s,l,c,f){return this.set(1,s,c,0,t,1,f,0,i,l,1,0,0,0,0,1),this}compose(t,i,s){const l=this.elements,c=i._x,f=i._y,p=i._z,m=i._w,d=c+c,x=f+f,v=p+p,g=c*d,S=c*x,b=c*v,w=f*x,E=f*v,_=p*v,z=m*d,F=m*x,C=m*v,B=s.x,O=s.y,U=s.z;return l[0]=(1-(w+_))*B,l[1]=(S+C)*B,l[2]=(b-F)*B,l[3]=0,l[4]=(S-C)*O,l[5]=(1-(g+_))*O,l[6]=(E+z)*O,l[7]=0,l[8]=(b+F)*U,l[9]=(E-z)*U,l[10]=(1-(g+w))*U,l[11]=0,l[12]=t.x,l[13]=t.y,l[14]=t.z,l[15]=1,this}decompose(t,i,s){const l=this.elements;t.x=l[12],t.y=l[13],t.z=l[14];const c=this.determinantAffine();if(c===0)return s.set(1,1,1),i.identity(),this;let f=Lr.set(l[0],l[1],l[2]).length();const p=Lr.set(l[4],l[5],l[6]).length(),m=Lr.set(l[8],l[9],l[10]).length();c<0&&(f=-f),Oi.copy(this);const d=1/f,x=1/p,v=1/m;return Oi.elements[0]*=d,Oi.elements[1]*=d,Oi.elements[2]*=d,Oi.elements[4]*=x,Oi.elements[5]*=x,Oi.elements[6]*=x,Oi.elements[8]*=v,Oi.elements[9]*=v,Oi.elements[10]*=v,i.setFromRotationMatrix(Oi),s.x=f,s.y=p,s.z=m,this}makePerspective(t,i,s,l,c,f,p=Zi,m=!1){const d=this.elements,x=2*c/(i-t),v=2*c/(s-l),g=(i+t)/(i-t),S=(s+l)/(s-l);let b,w;if(m)b=c/(f-c),w=f*c/(f-c);else if(p===Zi)b=-(f+c)/(f-c),w=-2*f*c/(f-c);else if(p===Sl)b=-f/(f-c),w=-f*c/(f-c);else throw new Error("THREE.Matrix4.makePerspective(): Invalid coordinate system: "+p);return d[0]=x,d[4]=0,d[8]=g,d[12]=0,d[1]=0,d[5]=v,d[9]=S,d[13]=0,d[2]=0,d[6]=0,d[10]=b,d[14]=w,d[3]=0,d[7]=0,d[11]=-1,d[15]=0,this}makeOrthographic(t,i,s,l,c,f,p=Zi,m=!1){const d=this.elements,x=2/(i-t),v=2/(s-l),g=-(i+t)/(i-t),S=-(s+l)/(s-l);let b,w;if(m)b=1/(f-c),w=f/(f-c);else if(p===Zi)b=-2/(f-c),w=-(f+c)/(f-c);else if(p===Sl)b=-1/(f-c),w=-c/(f-c);else throw new Error("THREE.Matrix4.makeOrthographic(): Invalid coordinate system: "+p);return d[0]=x,d[4]=0,d[8]=0,d[12]=g,d[1]=0,d[5]=v,d[9]=0,d[13]=S,d[2]=0,d[6]=0,d[10]=b,d[14]=w,d[3]=0,d[7]=0,d[11]=0,d[15]=1,this}equals(t){const i=this.elements,s=t.elements;for(let l=0;l<16;l++)if(i[l]!==s[l])return!1;return!0}fromArray(t,i=0){for(let s=0;s<16;s++)this.elements[s]=t[s+i];return this}toArray(t=[],i=0){const s=this.elements;return t[i]=s[0],t[i+1]=s[1],t[i+2]=s[2],t[i+3]=s[3],t[i+4]=s[4],t[i+5]=s[5],t[i+6]=s[6],t[i+7]=s[7],t[i+8]=s[8],t[i+9]=s[9],t[i+10]=s[10],t[i+11]=s[11],t[i+12]=s[12],t[i+13]=s[13],t[i+14]=s[14],t[i+15]=s[15],t}};Cu.prototype.isMatrix4=!0;let we=Cu;const Lr=new k,Oi=new we,HE=new k(0,0,0),GE=new k(1,1,1),as=new k,Pc=new k,hi=new k,nv=new we,iv=new ro;class ps{constructor(t=0,i=0,s=0,l=ps.DEFAULT_ORDER){this.isEuler=!0,this._x=t,this._y=i,this._z=s,this._order=l}get x(){return this._x}set x(t){this._x=t,this._onChangeCallback()}get y(){return this._y}set y(t){this._y=t,this._onChangeCallback()}get z(){return this._z}set z(t){this._z=t,this._onChangeCallback()}get order(){return this._order}set order(t){this._order=t,this._onChangeCallback()}set(t,i,s,l=this._order){return this._x=t,this._y=i,this._z=s,this._order=l,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._order)}copy(t){return this._x=t._x,this._y=t._y,this._z=t._z,this._order=t._order,this._onChangeCallback(),this}setFromRotationMatrix(t,i=this._order,s=!0){const l=t.elements,c=l[0],f=l[4],p=l[8],m=l[1],d=l[5],x=l[9],v=l[2],g=l[6],S=l[10];switch(i){case"XYZ":this._y=Math.asin(ge(p,-1,1)),Math.abs(p)<.9999999?(this._x=Math.atan2(-x,S),this._z=Math.atan2(-f,c)):(this._x=Math.atan2(g,d),this._z=0);break;case"YXZ":this._x=Math.asin(-ge(x,-1,1)),Math.abs(x)<.9999999?(this._y=Math.atan2(p,S),this._z=Math.atan2(m,d)):(this._y=Math.atan2(-v,c),this._z=0);break;case"ZXY":this._x=Math.asin(ge(g,-1,1)),Math.abs(g)<.9999999?(this._y=Math.atan2(-v,S),this._z=Math.atan2(-f,d)):(this._y=0,this._z=Math.atan2(m,c));break;case"ZYX":this._y=Math.asin(-ge(v,-1,1)),Math.abs(v)<.9999999?(this._x=Math.atan2(g,S),this._z=Math.atan2(m,c)):(this._x=0,this._z=Math.atan2(-f,d));break;case"YZX":this._z=Math.asin(ge(m,-1,1)),Math.abs(m)<.9999999?(this._x=Math.atan2(-x,d),this._y=Math.atan2(-v,c)):(this._x=0,this._y=Math.atan2(p,S));break;case"XZY":this._z=Math.asin(-ge(f,-1,1)),Math.abs(f)<.9999999?(this._x=Math.atan2(g,d),this._y=Math.atan2(p,c)):(this._x=Math.atan2(-x,S),this._y=0);break;default:se("Euler: .setFromRotationMatrix() encountered an unknown order: "+i)}return this._order=i,s===!0&&this._onChangeCallback(),this}setFromQuaternion(t,i,s){return nv.makeRotationFromQuaternion(t),this.setFromRotationMatrix(nv,i,s)}setFromVector3(t,i=this._order){return this.set(t.x,t.y,t.z,i)}reorder(t){return iv.setFromEuler(this),this.setFromQuaternion(iv,t)}equals(t){return t._x===this._x&&t._y===this._y&&t._z===this._z&&t._order===this._order}fromArray(t){return this._x=t[0],this._y=t[1],this._z=t[2],t[3]!==void 0&&(this._order=t[3]),this._onChangeCallback(),this}toArray(t=[],i=0){return t[i]=this._x,t[i+1]=this._y,t[i+2]=this._z,t[i+3]=this._order,t}_onChange(t){return this._onChangeCallback=t,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._order}}ps.DEFAULT_ORDER="XYZ";class Vp{constructor(){this.mask=1}set(t){this.mask=(1<<t|0)>>>0}enable(t){this.mask|=1<<t|0}enableAll(){this.mask=-1}toggle(t){this.mask^=1<<t|0}disable(t){this.mask&=~(1<<t|0)}disableAll(){this.mask=0}test(t){return(this.mask&t.mask)!==0}isEnabled(t){return(this.mask&(1<<t|0))!==0}}let VE=0;const av=new k,Or=new ro,xa=new we,zc=new k,el=new k,kE=new k,XE=new ro,sv=new k(1,0,0),rv=new k(0,1,0),ov=new k(0,0,1),lv={type:"added"},WE={type:"removed"},Pr={type:"childadded",child:null},$h={type:"childremoved",child:null};class Mn extends Ks{constructor(){super(),this.isObject3D=!0,Object.defineProperty(this,"id",{value:VE++}),this.uuid=Ra(),this.name="",this.type="Object3D",this.parent=null,this.children=[],this.up=Mn.DEFAULT_UP.clone();const t=new k,i=new ps,s=new ro,l=new k(1,1,1);function c(){s.setFromEuler(i,!1)}function f(){i.setFromQuaternion(s,void 0,!1)}i._onChange(c),s._onChange(f),Object.defineProperties(this,{position:{configurable:!0,enumerable:!0,value:t},rotation:{configurable:!0,enumerable:!0,value:i},quaternion:{configurable:!0,enumerable:!0,value:s},scale:{configurable:!0,enumerable:!0,value:l},modelViewMatrix:{value:new we},normalMatrix:{value:new ue}}),this.matrix=new we,this.matrixWorld=new we,this.matrixAutoUpdate=Mn.DEFAULT_MATRIX_AUTO_UPDATE,this.matrixWorldAutoUpdate=Mn.DEFAULT_MATRIX_WORLD_AUTO_UPDATE,this.matrixWorldNeedsUpdate=!1,this.layers=new Vp,this.visible=!0,this.castShadow=!1,this.receiveShadow=!1,this.frustumCulled=!0,this.renderOrder=0,this.animations=[],this.customDepthMaterial=void 0,this.customDistanceMaterial=void 0,this.static=!1,this.userData={},this.pivot=null}onBeforeShadow(){}onAfterShadow(){}onBeforeRender(){}onAfterRender(){}applyMatrix4(t){this.matrixAutoUpdate&&this.updateMatrix(),this.matrix.premultiply(t),this.matrix.decompose(this.position,this.quaternion,this.scale)}applyQuaternion(t){return this.quaternion.premultiply(t),this}setRotationFromAxisAngle(t,i){this.quaternion.setFromAxisAngle(t,i)}setRotationFromEuler(t){this.quaternion.setFromEuler(t,!0)}setRotationFromMatrix(t){this.quaternion.setFromRotationMatrix(t)}setRotationFromQuaternion(t){this.quaternion.copy(t)}rotateOnAxis(t,i){return Or.setFromAxisAngle(t,i),this.quaternion.multiply(Or),this}rotateOnWorldAxis(t,i){return Or.setFromAxisAngle(t,i),this.quaternion.premultiply(Or),this}rotateX(t){return this.rotateOnAxis(sv,t)}rotateY(t){return this.rotateOnAxis(rv,t)}rotateZ(t){return this.rotateOnAxis(ov,t)}translateOnAxis(t,i){return av.copy(t).applyQuaternion(this.quaternion),this.position.add(av.multiplyScalar(i)),this}translateX(t){return this.translateOnAxis(sv,t)}translateY(t){return this.translateOnAxis(rv,t)}translateZ(t){return this.translateOnAxis(ov,t)}localToWorld(t){return this.updateWorldMatrix(!0,!1),t.applyMatrix4(this.matrixWorld)}worldToLocal(t){return this.updateWorldMatrix(!0,!1),t.applyMatrix4(xa.copy(this.matrixWorld).invert())}lookAt(t,i,s){t.isVector3?zc.copy(t):zc.set(t,i,s);const l=this.parent;this.updateWorldMatrix(!0,!1),el.setFromMatrixPosition(this.matrixWorld),this.isCamera||this.isLight?xa.lookAt(el,zc,this.up):xa.lookAt(zc,el,this.up),this.quaternion.setFromRotationMatrix(xa),l&&(xa.extractRotation(l.matrixWorld),Or.setFromRotationMatrix(xa),this.quaternion.premultiply(Or.invert()))}add(t){if(arguments.length>1){for(let i=0;i<arguments.length;i++)this.add(arguments[i]);return this}return t===this?(Ae("Object3D.add: object can't be added as a child of itself.",t),this):(t&&t.isObject3D?(t.removeFromParent(),t.parent=this,this.children.push(t),t.dispatchEvent(lv),Pr.child=t,this.dispatchEvent(Pr),Pr.child=null):Ae("Object3D.add: object not an instance of THREE.Object3D.",t),this)}remove(t){if(arguments.length>1){for(let s=0;s<arguments.length;s++)this.remove(arguments[s]);return this}const i=this.children.indexOf(t);return i!==-1&&(t.parent=null,this.children.splice(i,1),t.dispatchEvent(WE),$h.child=t,this.dispatchEvent($h),$h.child=null),this}removeFromParent(){const t=this.parent;return t!==null&&t.remove(this),this}clear(){return this.remove(...this.children)}attach(t){return this.updateWorldMatrix(!0,!1),xa.copy(this.matrixWorld).invert(),t.parent!==null&&(t.parent.updateWorldMatrix(!0,!1),xa.multiply(t.parent.matrixWorld)),t.applyMatrix4(xa),t.removeFromParent(),t.parent=this,this.children.push(t),t.updateWorldMatrix(!1,!0),t.dispatchEvent(lv),Pr.child=t,this.dispatchEvent(Pr),Pr.child=null,this}getObjectById(t){return this.getObjectByProperty("id",t)}getObjectByName(t){return this.getObjectByProperty("name",t)}getObjectByProperty(t,i){if(this[t]===i)return this;for(let s=0,l=this.children.length;s<l;s++){const f=this.children[s].getObjectByProperty(t,i);if(f!==void 0)return f}}getObjectsByProperty(t,i,s=[]){this[t]===i&&s.push(this);const l=this.children;for(let c=0,f=l.length;c<f;c++)l[c].getObjectsByProperty(t,i,s);return s}getWorldPosition(t){return this.updateWorldMatrix(!0,!1),t.setFromMatrixPosition(this.matrixWorld)}getWorldQuaternion(t){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(el,t,kE),t}getWorldScale(t){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(el,XE,t),t}getWorldDirection(t){this.updateWorldMatrix(!0,!1);const i=this.matrixWorld.elements;return t.set(i[8],i[9],i[10]).normalize()}raycast(){}traverse(t){t(this);const i=this.children;for(let s=0,l=i.length;s<l;s++)i[s].traverse(t)}traverseVisible(t){if(this.visible===!1)return;t(this);const i=this.children;for(let s=0,l=i.length;s<l;s++)i[s].traverseVisible(t)}traverseAncestors(t){const i=this.parent;i!==null&&(t(i),i.traverseAncestors(t))}updateMatrix(){this.matrix.compose(this.position,this.quaternion,this.scale);const t=this.pivot;if(t!==null){const i=t.x,s=t.y,l=t.z,c=this.matrix.elements;c[12]+=i-c[0]*i-c[4]*s-c[8]*l,c[13]+=s-c[1]*i-c[5]*s-c[9]*l,c[14]+=l-c[2]*i-c[6]*s-c[10]*l}this.matrixWorldNeedsUpdate=!0}updateMatrixWorld(t){this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||t)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,t=!0);const i=this.children;for(let s=0,l=i.length;s<l;s++)i[s].updateMatrixWorld(t)}updateWorldMatrix(t,i,s=!1){const l=this.parent;if(t===!0&&l!==null&&l.updateWorldMatrix(!0,!1),this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||s)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,s=!0),i===!0){const c=this.children;for(let f=0,p=c.length;f<p;f++)c[f].updateWorldMatrix(!1,!0,s)}}toJSON(t){const i=t===void 0||typeof t=="string",s={};i&&(t={geometries:{},materials:{},textures:{},images:{},shapes:{},skeletons:{},animations:{},nodes:{}},s.metadata={version:4.7,type:"Object",generator:"Object3D.toJSON"});const l={};l.uuid=this.uuid,l.type=this.type,this.name!==""&&(l.name=this.name),this.castShadow===!0&&(l.castShadow=!0),this.receiveShadow===!0&&(l.receiveShadow=!0),this.visible===!1&&(l.visible=!1),this.frustumCulled===!1&&(l.frustumCulled=!1),this.renderOrder!==0&&(l.renderOrder=this.renderOrder),this.static!==!1&&(l.static=this.static),Object.keys(this.userData).length>0&&(l.userData=this.userData),l.layers=this.layers.mask,l.matrix=this.matrix.toArray(),l.up=this.up.toArray(),this.pivot!==null&&(l.pivot=this.pivot.toArray()),this.matrixAutoUpdate===!1&&(l.matrixAutoUpdate=!1),this.morphTargetDictionary!==void 0&&(l.morphTargetDictionary=Object.assign({},this.morphTargetDictionary)),this.morphTargetInfluences!==void 0&&(l.morphTargetInfluences=this.morphTargetInfluences.slice()),this.isInstancedMesh&&(l.type="InstancedMesh",l.count=this.count,l.instanceMatrix=this.instanceMatrix.toJSON(),this.instanceColor!==null&&(l.instanceColor=this.instanceColor.toJSON())),this.isBatchedMesh&&(l.type="BatchedMesh",l.perObjectFrustumCulled=this.perObjectFrustumCulled,l.sortObjects=this.sortObjects,l.drawRanges=this._drawRanges,l.reservedRanges=this._reservedRanges,l.geometryInfo=this._geometryInfo.map(p=>({...p,boundingBox:p.boundingBox?p.boundingBox.toJSON():void 0,boundingSphere:p.boundingSphere?p.boundingSphere.toJSON():void 0})),l.instanceInfo=this._instanceInfo.map(p=>({...p})),l.availableInstanceIds=this._availableInstanceIds.slice(),l.availableGeometryIds=this._availableGeometryIds.slice(),l.nextIndexStart=this._nextIndexStart,l.nextVertexStart=this._nextVertexStart,l.geometryCount=this._geometryCount,l.maxInstanceCount=this._maxInstanceCount,l.maxVertexCount=this._maxVertexCount,l.maxIndexCount=this._maxIndexCount,l.geometryInitialized=this._geometryInitialized,l.matricesTexture=this._matricesTexture.toJSON(t),l.indirectTexture=this._indirectTexture.toJSON(t),this._colorsTexture!==null&&(l.colorsTexture=this._colorsTexture.toJSON(t)),this.boundingSphere!==null&&(l.boundingSphere=this.boundingSphere.toJSON()),this.boundingBox!==null&&(l.boundingBox=this.boundingBox.toJSON()));function c(p,m){return p[m.uuid]===void 0&&(p[m.uuid]=m.toJSON(t)),m.uuid}if(this.isScene)this.background&&(this.background.isColor?l.background=this.background.toJSON():this.background.isTexture&&(l.background=this.background.toJSON(t).uuid)),this.environment&&this.environment.isTexture&&this.environment.isRenderTargetTexture!==!0&&(l.environment=this.environment.toJSON(t).uuid);else if(this.isMesh||this.isLine||this.isPoints){l.geometry=c(t.geometries,this.geometry);const p=this.geometry.parameters;if(p!==void 0&&p.shapes!==void 0){const m=p.shapes;if(Array.isArray(m))for(let d=0,x=m.length;d<x;d++){const v=m[d];c(t.shapes,v)}else c(t.shapes,m)}}if(this.isSkinnedMesh&&(l.bindMode=this.bindMode,l.bindMatrix=this.bindMatrix.toArray(),this.skeleton!==void 0&&(c(t.skeletons,this.skeleton),l.skeleton=this.skeleton.uuid)),this.material!==void 0)if(Array.isArray(this.material)){const p=[];for(let m=0,d=this.material.length;m<d;m++)p.push(c(t.materials,this.material[m]));l.material=p}else l.material=c(t.materials,this.material);if(this.children.length>0){l.children=[];for(let p=0;p<this.children.length;p++)l.children.push(this.children[p].toJSON(t).object)}if(this.animations.length>0){l.animations=[];for(let p=0;p<this.animations.length;p++){const m=this.animations[p];l.animations.push(c(t.animations,m))}}if(i){const p=f(t.geometries),m=f(t.materials),d=f(t.textures),x=f(t.images),v=f(t.shapes),g=f(t.skeletons),S=f(t.animations),b=f(t.nodes);p.length>0&&(s.geometries=p),m.length>0&&(s.materials=m),d.length>0&&(s.textures=d),x.length>0&&(s.images=x),v.length>0&&(s.shapes=v),g.length>0&&(s.skeletons=g),S.length>0&&(s.animations=S),b.length>0&&(s.nodes=b)}return s.object=l,s;function f(p){const m=[];for(const d in p){const x=p[d];delete x.metadata,m.push(x)}return m}}clone(t){return new this.constructor().copy(this,t)}copy(t,i=!0){if(this.name=t.name,this.up.copy(t.up),this.position.copy(t.position),this.rotation.order=t.rotation.order,this.quaternion.copy(t.quaternion),this.scale.copy(t.scale),this.pivot=t.pivot!==null?t.pivot.clone():null,this.matrix.copy(t.matrix),this.matrixWorld.copy(t.matrixWorld),this.matrixAutoUpdate=t.matrixAutoUpdate,this.matrixWorldAutoUpdate=t.matrixWorldAutoUpdate,this.matrixWorldNeedsUpdate=t.matrixWorldNeedsUpdate,this.layers.mask=t.layers.mask,this.visible=t.visible,this.castShadow=t.castShadow,this.receiveShadow=t.receiveShadow,this.frustumCulled=t.frustumCulled,this.renderOrder=t.renderOrder,this.static=t.static,this.animations=t.animations.slice(),this.userData=JSON.parse(JSON.stringify(t.userData)),i===!0)for(let s=0;s<t.children.length;s++){const l=t.children[s];this.add(l.clone())}return this}}Mn.DEFAULT_UP=new k(0,1,0);Mn.DEFAULT_MATRIX_AUTO_UPDATE=!0;Mn.DEFAULT_MATRIX_WORLD_AUTO_UPDATE=!0;class qs extends Mn{constructor(){super(),this.isGroup=!0,this.type="Group"}}const qE={type:"move"};class td{constructor(){this._targetRay=null,this._grip=null,this._hand=null}getHandSpace(){return this._hand===null&&(this._hand=new qs,this._hand.matrixAutoUpdate=!1,this._hand.visible=!1,this._hand.joints={},this._hand.inputState={pinching:!1}),this._hand}getTargetRaySpace(){return this._targetRay===null&&(this._targetRay=new qs,this._targetRay.matrixAutoUpdate=!1,this._targetRay.visible=!1,this._targetRay.hasLinearVelocity=!1,this._targetRay.linearVelocity=new k,this._targetRay.hasAngularVelocity=!1,this._targetRay.angularVelocity=new k),this._targetRay}getGripSpace(){return this._grip===null&&(this._grip=new qs,this._grip.matrixAutoUpdate=!1,this._grip.visible=!1,this._grip.hasLinearVelocity=!1,this._grip.linearVelocity=new k,this._grip.hasAngularVelocity=!1,this._grip.angularVelocity=new k,this._grip.eventsEnabled=!1),this._grip}dispatchEvent(t){return this._targetRay!==null&&this._targetRay.dispatchEvent(t),this._grip!==null&&this._grip.dispatchEvent(t),this._hand!==null&&this._hand.dispatchEvent(t),this}connect(t){if(t&&t.hand){const i=this._hand;if(i)for(const s of t.hand.values())this._getHandJoint(i,s)}return this.dispatchEvent({type:"connected",data:t}),this}disconnect(t){return this.dispatchEvent({type:"disconnected",data:t}),this._targetRay!==null&&(this._targetRay.visible=!1),this._grip!==null&&(this._grip.visible=!1),this._hand!==null&&(this._hand.visible=!1),this}update(t,i,s){let l=null,c=null,f=null;const p=this._targetRay,m=this._grip,d=this._hand;if(t&&i.session.visibilityState!=="visible-blurred"){if(d&&t.hand){f=!0;for(const w of t.hand.values()){const E=i.getJointPose(w,s),_=this._getHandJoint(d,w);E!==null&&(_.matrix.fromArray(E.transform.matrix),_.matrix.decompose(_.position,_.rotation,_.scale),_.matrixWorldNeedsUpdate=!0,_.jointRadius=E.radius),_.visible=E!==null}const x=d.joints["index-finger-tip"],v=d.joints["thumb-tip"],g=x.position.distanceTo(v.position),S=.02,b=.005;d.inputState.pinching&&g>S+b?(d.inputState.pinching=!1,this.dispatchEvent({type:"pinchend",handedness:t.handedness,target:this})):!d.inputState.pinching&&g<=S-b&&(d.inputState.pinching=!0,this.dispatchEvent({type:"pinchstart",handedness:t.handedness,target:this}))}else m!==null&&t.gripSpace&&(c=i.getPose(t.gripSpace,s),c!==null&&(m.matrix.fromArray(c.transform.matrix),m.matrix.decompose(m.position,m.rotation,m.scale),m.matrixWorldNeedsUpdate=!0,c.linearVelocity?(m.hasLinearVelocity=!0,m.linearVelocity.copy(c.linearVelocity)):m.hasLinearVelocity=!1,c.angularVelocity?(m.hasAngularVelocity=!0,m.angularVelocity.copy(c.angularVelocity)):m.hasAngularVelocity=!1,m.eventsEnabled&&m.dispatchEvent({type:"gripUpdated",data:t,target:this})));p!==null&&(l=i.getPose(t.targetRaySpace,s),l===null&&c!==null&&(l=c),l!==null&&(p.matrix.fromArray(l.transform.matrix),p.matrix.decompose(p.position,p.rotation,p.scale),p.matrixWorldNeedsUpdate=!0,l.linearVelocity?(p.hasLinearVelocity=!0,p.linearVelocity.copy(l.linearVelocity)):p.hasLinearVelocity=!1,l.angularVelocity?(p.hasAngularVelocity=!0,p.angularVelocity.copy(l.angularVelocity)):p.hasAngularVelocity=!1,this.dispatchEvent(qE)))}return p!==null&&(p.visible=l!==null),m!==null&&(m.visible=c!==null),d!==null&&(d.visible=f!==null),this}_getHandJoint(t,i){if(t.joints[i.jointName]===void 0){const s=new qs;s.matrixAutoUpdate=!1,s.visible=!1,t.joints[i.jointName]=s,t.add(s)}return t.joints[i.jointName]}}const Nx={aliceblue:15792383,antiquewhite:16444375,aqua:65535,aquamarine:8388564,azure:15794175,beige:16119260,bisque:16770244,black:0,blanchedalmond:16772045,blue:255,blueviolet:9055202,brown:10824234,burlywood:14596231,cadetblue:6266528,chartreuse:8388352,chocolate:13789470,coral:16744272,cornflowerblue:6591981,cornsilk:16775388,crimson:14423100,cyan:65535,darkblue:139,darkcyan:35723,darkgoldenrod:12092939,darkgray:11119017,darkgreen:25600,darkgrey:11119017,darkkhaki:12433259,darkmagenta:9109643,darkolivegreen:5597999,darkorange:16747520,darkorchid:10040012,darkred:9109504,darksalmon:15308410,darkseagreen:9419919,darkslateblue:4734347,darkslategray:3100495,darkslategrey:3100495,darkturquoise:52945,darkviolet:9699539,deeppink:16716947,deepskyblue:49151,dimgray:6908265,dimgrey:6908265,dodgerblue:2003199,firebrick:11674146,floralwhite:16775920,forestgreen:2263842,fuchsia:16711935,gainsboro:14474460,ghostwhite:16316671,gold:16766720,goldenrod:14329120,gray:8421504,green:32768,greenyellow:11403055,grey:8421504,honeydew:15794160,hotpink:16738740,indianred:13458524,indigo:4915330,ivory:16777200,khaki:15787660,lavender:15132410,lavenderblush:16773365,lawngreen:8190976,lemonchiffon:16775885,lightblue:11393254,lightcoral:15761536,lightcyan:14745599,lightgoldenrodyellow:16448210,lightgray:13882323,lightgreen:9498256,lightgrey:13882323,lightpink:16758465,lightsalmon:16752762,lightseagreen:2142890,lightskyblue:8900346,lightslategray:7833753,lightslategrey:7833753,lightsteelblue:11584734,lightyellow:16777184,lime:65280,limegreen:3329330,linen:16445670,magenta:16711935,maroon:8388608,mediumaquamarine:6737322,mediumblue:205,mediumorchid:12211667,mediumpurple:9662683,mediumseagreen:3978097,mediumslateblue:8087790,mediumspringgreen:64154,mediumturquoise:4772300,mediumvioletred:13047173,midnightblue:1644912,mintcream:16121850,mistyrose:16770273,moccasin:16770229,navajowhite:16768685,navy:128,oldlace:16643558,olive:8421376,olivedrab:7048739,orange:16753920,orangered:16729344,orchid:14315734,palegoldenrod:15657130,palegreen:10025880,paleturquoise:11529966,palevioletred:14381203,papayawhip:16773077,peachpuff:16767673,peru:13468991,pink:16761035,plum:14524637,powderblue:11591910,purple:8388736,rebeccapurple:6697881,red:16711680,rosybrown:12357519,royalblue:4286945,saddlebrown:9127187,salmon:16416882,sandybrown:16032864,seagreen:3050327,seashell:16774638,sienna:10506797,silver:12632256,skyblue:8900331,slateblue:6970061,slategray:7372944,slategrey:7372944,snow:16775930,springgreen:65407,steelblue:4620980,tan:13808780,teal:32896,thistle:14204888,tomato:16737095,turquoise:4251856,violet:15631086,wheat:16113331,white:16777215,whitesmoke:16119285,yellow:16776960,yellowgreen:10145074},ss={h:0,s:0,l:0},Bc={h:0,s:0,l:0};function ed(r,t,i){return i<0&&(i+=1),i>1&&(i-=1),i<1/6?r+(t-r)*6*i:i<1/2?t:i<2/3?r+(t-r)*6*(2/3-i):r}class ie{constructor(t,i,s){return this.isColor=!0,this.r=1,this.g=1,this.b=1,this.set(t,i,s)}set(t,i,s){if(i===void 0&&s===void 0){const l=t;l&&l.isColor?this.copy(l):typeof l=="number"?this.setHex(l):typeof l=="string"&&this.setStyle(l)}else this.setRGB(t,i,s);return this}setScalar(t){return this.r=t,this.g=t,this.b=t,this}setHex(t,i=qn){return t=Math.floor(t),this.r=(t>>16&255)/255,this.g=(t>>8&255)/255,this.b=(t&255)/255,Re.colorSpaceToWorking(this,i),this}setRGB(t,i,s,l=Re.workingColorSpace){return this.r=t,this.g=i,this.b=s,Re.colorSpaceToWorking(this,l),this}setHSL(t,i,s,l=Re.workingColorSpace){if(t=Hp(t,1),i=ge(i,0,1),s=ge(s,0,1),i===0)this.r=this.g=this.b=s;else{const c=s<=.5?s*(1+i):s+i-s*i,f=2*s-c;this.r=ed(f,c,t+1/3),this.g=ed(f,c,t),this.b=ed(f,c,t-1/3)}return Re.colorSpaceToWorking(this,l),this}setStyle(t,i=qn){function s(c){c!==void 0&&parseFloat(c)<1&&se("Color: Alpha component of "+t+" will be ignored.")}let l;if(l=/^(\w+)\(([^\)]*)\)/.exec(t)){let c;const f=l[1],p=l[2];switch(f){case"rgb":case"rgba":if(c=/^\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(p))return s(c[4]),this.setRGB(Math.min(255,parseInt(c[1],10))/255,Math.min(255,parseInt(c[2],10))/255,Math.min(255,parseInt(c[3],10))/255,i);if(c=/^\s*(\d+)\%\s*,\s*(\d+)\%\s*,\s*(\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(p))return s(c[4]),this.setRGB(Math.min(100,parseInt(c[1],10))/100,Math.min(100,parseInt(c[2],10))/100,Math.min(100,parseInt(c[3],10))/100,i);break;case"hsl":case"hsla":if(c=/^\s*(\d*\.?\d+)\s*,\s*(\d*\.?\d+)\%\s*,\s*(\d*\.?\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(p))return s(c[4]),this.setHSL(parseFloat(c[1])/360,parseFloat(c[2])/100,parseFloat(c[3])/100,i);break;default:se("Color: Unknown color model "+t)}}else if(l=/^\#([A-Fa-f\d]+)$/.exec(t)){const c=l[1],f=c.length;if(f===3)return this.setRGB(parseInt(c.charAt(0),16)/15,parseInt(c.charAt(1),16)/15,parseInt(c.charAt(2),16)/15,i);if(f===6)return this.setHex(parseInt(c,16),i);se("Color: Invalid hex color "+t)}else if(t&&t.length>0)return this.setColorName(t,i);return this}setColorName(t,i=qn){const s=Nx[t.toLowerCase()];return s!==void 0?this.setHex(s,i):se("Color: Unknown color "+t),this}clone(){return new this.constructor(this.r,this.g,this.b)}copy(t){return this.r=t.r,this.g=t.g,this.b=t.b,this}copySRGBToLinear(t){return this.r=wa(t.r),this.g=wa(t.g),this.b=wa(t.b),this}copyLinearToSRGB(t){return this.r=to(t.r),this.g=to(t.g),this.b=to(t.b),this}convertSRGBToLinear(){return this.copySRGBToLinear(this),this}convertLinearToSRGB(){return this.copyLinearToSRGB(this),this}getHex(t=qn){return Re.workingToColorSpace(Hn.copy(this),t),Math.round(ge(Hn.r*255,0,255))*65536+Math.round(ge(Hn.g*255,0,255))*256+Math.round(ge(Hn.b*255,0,255))}getHexString(t=qn){return("000000"+this.getHex(t).toString(16)).slice(-6)}getHSL(t,i=Re.workingColorSpace){Re.workingToColorSpace(Hn.copy(this),i);const s=Hn.r,l=Hn.g,c=Hn.b,f=Math.max(s,l,c),p=Math.min(s,l,c);let m,d;const x=(p+f)/2;if(p===f)m=0,d=0;else{const v=f-p;switch(d=x<=.5?v/(f+p):v/(2-f-p),f){case s:m=(l-c)/v+(l<c?6:0);break;case l:m=(c-s)/v+2;break;case c:m=(s-l)/v+4;break}m/=6}return t.h=m,t.s=d,t.l=x,t}getRGB(t,i=Re.workingColorSpace){return Re.workingToColorSpace(Hn.copy(this),i),t.r=Hn.r,t.g=Hn.g,t.b=Hn.b,t}getStyle(t=qn){Re.workingToColorSpace(Hn.copy(this),t);const i=Hn.r,s=Hn.g,l=Hn.b;return t!==qn?`color(${t} ${i.toFixed(3)} ${s.toFixed(3)} ${l.toFixed(3)})`:`rgb(${Math.round(i*255)},${Math.round(s*255)},${Math.round(l*255)})`}offsetHSL(t,i,s){return this.getHSL(ss),this.setHSL(ss.h+t,ss.s+i,ss.l+s)}add(t){return this.r+=t.r,this.g+=t.g,this.b+=t.b,this}addColors(t,i){return this.r=t.r+i.r,this.g=t.g+i.g,this.b=t.b+i.b,this}addScalar(t){return this.r+=t,this.g+=t,this.b+=t,this}sub(t){return this.r=Math.max(0,this.r-t.r),this.g=Math.max(0,this.g-t.g),this.b=Math.max(0,this.b-t.b),this}multiply(t){return this.r*=t.r,this.g*=t.g,this.b*=t.b,this}multiplyScalar(t){return this.r*=t,this.g*=t,this.b*=t,this}lerp(t,i){return this.r+=(t.r-this.r)*i,this.g+=(t.g-this.g)*i,this.b+=(t.b-this.b)*i,this}lerpColors(t,i,s){return this.r=t.r+(i.r-t.r)*s,this.g=t.g+(i.g-t.g)*s,this.b=t.b+(i.b-t.b)*s,this}lerpHSL(t,i){this.getHSL(ss),t.getHSL(Bc);const s=vl(ss.h,Bc.h,i),l=vl(ss.s,Bc.s,i),c=vl(ss.l,Bc.l,i);return this.setHSL(s,l,c),this}setFromVector3(t){return this.r=t.x,this.g=t.y,this.b=t.z,this}applyMatrix3(t){const i=this.r,s=this.g,l=this.b,c=t.elements;return this.r=c[0]*i+c[3]*s+c[6]*l,this.g=c[1]*i+c[4]*s+c[7]*l,this.b=c[2]*i+c[5]*s+c[8]*l,this}equals(t){return t.r===this.r&&t.g===this.g&&t.b===this.b}fromArray(t,i=0){return this.r=t[i],this.g=t[i+1],this.b=t[i+2],this}toArray(t=[],i=0){return t[i]=this.r,t[i+1]=this.g,t[i+2]=this.b,t}fromBufferAttribute(t,i){return this.r=t.getX(i),this.g=t.getY(i),this.b=t.getZ(i),this}toJSON(){return this.getHex()}*[Symbol.iterator](){yield this.r,yield this.g,yield this.b}}const Hn=new ie;ie.NAMES=Nx;class kp{constructor(t,i=25e-5){this.isFogExp2=!0,this.name="",this.color=new ie(t),this.density=i}clone(){return new kp(this.color,this.density)}toJSON(){return{type:"FogExp2",name:this.name,color:this.color.getHex(),density:this.density}}}class YE extends Mn{constructor(){super(),this.isScene=!0,this.type="Scene",this.background=null,this.environment=null,this.fog=null,this.backgroundBlurriness=0,this.backgroundIntensity=1,this.backgroundRotation=new ps,this.environmentIntensity=1,this.environmentRotation=new ps,this.overrideMaterial=null,typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}copy(t,i){return super.copy(t,i),t.background!==null&&(this.background=t.background.clone()),t.environment!==null&&(this.environment=t.environment.clone()),t.fog!==null&&(this.fog=t.fog.clone()),this.backgroundBlurriness=t.backgroundBlurriness,this.backgroundIntensity=t.backgroundIntensity,this.backgroundRotation.copy(t.backgroundRotation),this.environmentIntensity=t.environmentIntensity,this.environmentRotation.copy(t.environmentRotation),t.overrideMaterial!==null&&(this.overrideMaterial=t.overrideMaterial.clone()),this.matrixAutoUpdate=t.matrixAutoUpdate,this}toJSON(t){const i=super.toJSON(t);return this.fog!==null&&(i.object.fog=this.fog.toJSON()),this.backgroundBlurriness>0&&(i.object.backgroundBlurriness=this.backgroundBlurriness),this.backgroundIntensity!==1&&(i.object.backgroundIntensity=this.backgroundIntensity),i.object.backgroundRotation=this.backgroundRotation.toArray(),this.environmentIntensity!==1&&(i.object.environmentIntensity=this.environmentIntensity),i.object.environmentRotation=this.environmentRotation.toArray(),i}}const Pi=new k,ya=new k,nd=new k,Sa=new k,zr=new k,Br=new k,cv=new k,id=new k,ad=new k,sd=new k,rd=new cn,od=new cn,ld=new cn;class _i{constructor(t=new k,i=new k,s=new k){this.a=t,this.b=i,this.c=s}static getNormal(t,i,s,l){l.subVectors(s,i),Pi.subVectors(t,i),l.cross(Pi);const c=l.lengthSq();return c>0?l.multiplyScalar(1/Math.sqrt(c)):l.set(0,0,0)}static getBarycoord(t,i,s,l,c){Pi.subVectors(l,i),ya.subVectors(s,i),nd.subVectors(t,i);const f=Pi.dot(Pi),p=Pi.dot(ya),m=Pi.dot(nd),d=ya.dot(ya),x=ya.dot(nd),v=f*d-p*p;if(v===0)return c.set(0,0,0),null;const g=1/v,S=(d*m-p*x)*g,b=(f*x-p*m)*g;return c.set(1-S-b,b,S)}static containsPoint(t,i,s,l){return this.getBarycoord(t,i,s,l,Sa)===null?!1:Sa.x>=0&&Sa.y>=0&&Sa.x+Sa.y<=1}static getInterpolation(t,i,s,l,c,f,p,m){return this.getBarycoord(t,i,s,l,Sa)===null?(m.x=0,m.y=0,"z"in m&&(m.z=0),"w"in m&&(m.w=0),null):(m.setScalar(0),m.addScaledVector(c,Sa.x),m.addScaledVector(f,Sa.y),m.addScaledVector(p,Sa.z),m)}static getInterpolatedAttribute(t,i,s,l,c,f){return rd.setScalar(0),od.setScalar(0),ld.setScalar(0),rd.fromBufferAttribute(t,i),od.fromBufferAttribute(t,s),ld.fromBufferAttribute(t,l),f.setScalar(0),f.addScaledVector(rd,c.x),f.addScaledVector(od,c.y),f.addScaledVector(ld,c.z),f}static isFrontFacing(t,i,s,l){return Pi.subVectors(s,i),ya.subVectors(t,i),Pi.cross(ya).dot(l)<0}set(t,i,s){return this.a.copy(t),this.b.copy(i),this.c.copy(s),this}setFromPointsAndIndices(t,i,s,l){return this.a.copy(t[i]),this.b.copy(t[s]),this.c.copy(t[l]),this}setFromAttributeAndIndices(t,i,s,l){return this.a.fromBufferAttribute(t,i),this.b.fromBufferAttribute(t,s),this.c.fromBufferAttribute(t,l),this}clone(){return new this.constructor().copy(this)}copy(t){return this.a.copy(t.a),this.b.copy(t.b),this.c.copy(t.c),this}getArea(){return Pi.subVectors(this.c,this.b),ya.subVectors(this.a,this.b),Pi.cross(ya).length()*.5}getMidpoint(t){return t.addVectors(this.a,this.b).add(this.c).multiplyScalar(1/3)}getNormal(t){return _i.getNormal(this.a,this.b,this.c,t)}getPlane(t){return t.setFromCoplanarPoints(this.a,this.b,this.c)}getBarycoord(t,i){return _i.getBarycoord(t,this.a,this.b,this.c,i)}getInterpolation(t,i,s,l,c){return _i.getInterpolation(t,this.a,this.b,this.c,i,s,l,c)}containsPoint(t){return _i.containsPoint(t,this.a,this.b,this.c)}isFrontFacing(t){return _i.isFrontFacing(this.a,this.b,this.c,t)}intersectsBox(t){return t.intersectsTriangle(this)}closestPointToPoint(t,i){const s=this.a,l=this.b,c=this.c;let f,p;zr.subVectors(l,s),Br.subVectors(c,s),id.subVectors(t,s);const m=zr.dot(id),d=Br.dot(id);if(m<=0&&d<=0)return i.copy(s);ad.subVectors(t,l);const x=zr.dot(ad),v=Br.dot(ad);if(x>=0&&v<=x)return i.copy(l);const g=m*v-x*d;if(g<=0&&m>=0&&x<=0)return f=m/(m-x),i.copy(s).addScaledVector(zr,f);sd.subVectors(t,c);const S=zr.dot(sd),b=Br.dot(sd);if(b>=0&&S<=b)return i.copy(c);const w=S*d-m*b;if(w<=0&&d>=0&&b<=0)return p=d/(d-b),i.copy(s).addScaledVector(Br,p);const E=x*b-S*v;if(E<=0&&v-x>=0&&S-b>=0)return cv.subVectors(c,l),p=(v-x)/(v-x+(S-b)),i.copy(l).addScaledVector(cv,p);const _=1/(E+w+g);return f=w*_,p=g*_,i.copy(s).addScaledVector(zr,f).addScaledVector(Br,p)}equals(t){return t.a.equals(this.a)&&t.b.equals(this.b)&&t.c.equals(this.c)}}class Qs{constructor(t=new k(1/0,1/0,1/0),i=new k(-1/0,-1/0,-1/0)){this.isBox3=!0,this.min=t,this.max=i}set(t,i){return this.min.copy(t),this.max.copy(i),this}setFromArray(t){this.makeEmpty();for(let i=0,s=t.length;i<s;i+=3)this.expandByPoint(zi.fromArray(t,i));return this}setFromBufferAttribute(t){this.makeEmpty();for(let i=0,s=t.count;i<s;i++)this.expandByPoint(zi.fromBufferAttribute(t,i));return this}setFromPoints(t){this.makeEmpty();for(let i=0,s=t.length;i<s;i++)this.expandByPoint(t[i]);return this}setFromCenterAndSize(t,i){const s=zi.copy(i).multiplyScalar(.5);return this.min.copy(t).sub(s),this.max.copy(t).add(s),this}setFromObject(t,i=!1){return this.makeEmpty(),this.expandByObject(t,i)}clone(){return new this.constructor().copy(this)}copy(t){return this.min.copy(t.min),this.max.copy(t.max),this}makeEmpty(){return this.min.x=this.min.y=this.min.z=1/0,this.max.x=this.max.y=this.max.z=-1/0,this}isEmpty(){return this.max.x<this.min.x||this.max.y<this.min.y||this.max.z<this.min.z}getCenter(t){return this.isEmpty()?t.set(0,0,0):t.addVectors(this.min,this.max).multiplyScalar(.5)}getSize(t){return this.isEmpty()?t.set(0,0,0):t.subVectors(this.max,this.min)}expandByPoint(t){return this.min.min(t),this.max.max(t),this}expandByVector(t){return this.min.sub(t),this.max.add(t),this}expandByScalar(t){return this.min.addScalar(-t),this.max.addScalar(t),this}expandByObject(t,i=!1){t.updateWorldMatrix(!1,!1);const s=t.geometry;if(s!==void 0){const c=s.getAttribute("position");if(i===!0&&c!==void 0&&t.isInstancedMesh!==!0)for(let f=0,p=c.count;f<p;f++)t.isMesh===!0?t.getVertexPosition(f,zi):zi.fromBufferAttribute(c,f),zi.applyMatrix4(t.matrixWorld),this.expandByPoint(zi);else t.boundingBox!==void 0?(t.boundingBox===null&&t.computeBoundingBox(),Ic.copy(t.boundingBox)):(s.boundingBox===null&&s.computeBoundingBox(),Ic.copy(s.boundingBox)),Ic.applyMatrix4(t.matrixWorld),this.union(Ic)}const l=t.children;for(let c=0,f=l.length;c<f;c++)this.expandByObject(l[c],i);return this}containsPoint(t){return t.x>=this.min.x&&t.x<=this.max.x&&t.y>=this.min.y&&t.y<=this.max.y&&t.z>=this.min.z&&t.z<=this.max.z}containsBox(t){return this.min.x<=t.min.x&&t.max.x<=this.max.x&&this.min.y<=t.min.y&&t.max.y<=this.max.y&&this.min.z<=t.min.z&&t.max.z<=this.max.z}getParameter(t,i){return i.set((t.x-this.min.x)/(this.max.x-this.min.x),(t.y-this.min.y)/(this.max.y-this.min.y),(t.z-this.min.z)/(this.max.z-this.min.z))}intersectsBox(t){return t.max.x>=this.min.x&&t.min.x<=this.max.x&&t.max.y>=this.min.y&&t.min.y<=this.max.y&&t.max.z>=this.min.z&&t.min.z<=this.max.z}intersectsSphere(t){return this.clampPoint(t.center,zi),zi.distanceToSquared(t.center)<=t.radius*t.radius}intersectsPlane(t){let i,s;return t.normal.x>0?(i=t.normal.x*this.min.x,s=t.normal.x*this.max.x):(i=t.normal.x*this.max.x,s=t.normal.x*this.min.x),t.normal.y>0?(i+=t.normal.y*this.min.y,s+=t.normal.y*this.max.y):(i+=t.normal.y*this.max.y,s+=t.normal.y*this.min.y),t.normal.z>0?(i+=t.normal.z*this.min.z,s+=t.normal.z*this.max.z):(i+=t.normal.z*this.max.z,s+=t.normal.z*this.min.z),i<=-t.constant&&s>=-t.constant}intersectsTriangle(t){if(this.isEmpty())return!1;this.getCenter(nl),Fc.subVectors(this.max,nl),Ir.subVectors(t.a,nl),Fr.subVectors(t.b,nl),Hr.subVectors(t.c,nl),rs.subVectors(Fr,Ir),os.subVectors(Hr,Fr),Bs.subVectors(Ir,Hr);let i=[0,-rs.z,rs.y,0,-os.z,os.y,0,-Bs.z,Bs.y,rs.z,0,-rs.x,os.z,0,-os.x,Bs.z,0,-Bs.x,-rs.y,rs.x,0,-os.y,os.x,0,-Bs.y,Bs.x,0];return!cd(i,Ir,Fr,Hr,Fc)||(i=[1,0,0,0,1,0,0,0,1],!cd(i,Ir,Fr,Hr,Fc))?!1:(Hc.crossVectors(rs,os),i=[Hc.x,Hc.y,Hc.z],cd(i,Ir,Fr,Hr,Fc))}clampPoint(t,i){return i.copy(t).clamp(this.min,this.max)}distanceToPoint(t){return this.clampPoint(t,zi).distanceTo(t)}getBoundingSphere(t){return this.isEmpty()?t.makeEmpty():(this.getCenter(t.center),t.radius=this.getSize(zi).length()*.5),t}intersect(t){return this.min.max(t.min),this.max.min(t.max),this.isEmpty()&&this.makeEmpty(),this}union(t){return this.min.min(t.min),this.max.max(t.max),this}applyMatrix4(t){return this.isEmpty()?this:(Ma[0].set(this.min.x,this.min.y,this.min.z).applyMatrix4(t),Ma[1].set(this.min.x,this.min.y,this.max.z).applyMatrix4(t),Ma[2].set(this.min.x,this.max.y,this.min.z).applyMatrix4(t),Ma[3].set(this.min.x,this.max.y,this.max.z).applyMatrix4(t),Ma[4].set(this.max.x,this.min.y,this.min.z).applyMatrix4(t),Ma[5].set(this.max.x,this.min.y,this.max.z).applyMatrix4(t),Ma[6].set(this.max.x,this.max.y,this.min.z).applyMatrix4(t),Ma[7].set(this.max.x,this.max.y,this.max.z).applyMatrix4(t),this.setFromPoints(Ma),this)}translate(t){return this.min.add(t),this.max.add(t),this}equals(t){return t.min.equals(this.min)&&t.max.equals(this.max)}toJSON(){return{min:this.min.toArray(),max:this.max.toArray()}}fromJSON(t){return this.min.fromArray(t.min),this.max.fromArray(t.max),this}}const Ma=[new k,new k,new k,new k,new k,new k,new k,new k],zi=new k,Ic=new Qs,Ir=new k,Fr=new k,Hr=new k,rs=new k,os=new k,Bs=new k,nl=new k,Fc=new k,Hc=new k,Is=new k;function cd(r,t,i,s,l){for(let c=0,f=r.length-3;c<=f;c+=3){Is.fromArray(r,c);const p=l.x*Math.abs(Is.x)+l.y*Math.abs(Is.y)+l.z*Math.abs(Is.z),m=t.dot(Is),d=i.dot(Is),x=s.dot(Is);if(Math.max(-Math.max(m,d,x),Math.min(m,d,x))>p)return!1}return!0}const Sn=new k,Gc=new oe;let jE=0;class vi extends Ks{constructor(t,i,s=!1){if(super(),Array.isArray(t))throw new TypeError("THREE.BufferAttribute: array should be a Typed Array.");this.isBufferAttribute=!0,Object.defineProperty(this,"id",{value:jE++}),this.name="",this.array=t,this.itemSize=i,this.count=t!==void 0?t.length/i:0,this.normalized=s,this.usage=yp,this.updateRanges=[],this.gpuType=Ii,this.version=0}onUploadCallback(){}set needsUpdate(t){t===!0&&this.version++}setUsage(t){return this.usage=t,this}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}copy(t){return this.name=t.name,this.array=new t.array.constructor(t.array),this.itemSize=t.itemSize,this.count=t.count,this.normalized=t.normalized,this.usage=t.usage,this.gpuType=t.gpuType,this}copyAt(t,i,s){t*=this.itemSize,s*=i.itemSize;for(let l=0,c=this.itemSize;l<c;l++)this.array[t+l]=i.array[s+l];return this}copyArray(t){return this.array.set(t),this}applyMatrix3(t){if(this.itemSize===2)for(let i=0,s=this.count;i<s;i++)Gc.fromBufferAttribute(this,i),Gc.applyMatrix3(t),this.setXY(i,Gc.x,Gc.y);else if(this.itemSize===3)for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.applyMatrix3(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}applyMatrix4(t){for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.applyMatrix4(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}applyNormalMatrix(t){for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.applyNormalMatrix(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}transformDirection(t){for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.transformDirection(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}set(t,i=0){return this.array.set(t,i),this}getComponent(t,i){let s=this.array[t*this.itemSize+i];return this.normalized&&(s=Bi(s,this.array)),s}setComponent(t,i,s){return this.normalized&&(s=Xe(s,this.array)),this.array[t*this.itemSize+i]=s,this}getX(t){let i=this.array[t*this.itemSize];return this.normalized&&(i=Bi(i,this.array)),i}setX(t,i){return this.normalized&&(i=Xe(i,this.array)),this.array[t*this.itemSize]=i,this}getY(t){let i=this.array[t*this.itemSize+1];return this.normalized&&(i=Bi(i,this.array)),i}setY(t,i){return this.normalized&&(i=Xe(i,this.array)),this.array[t*this.itemSize+1]=i,this}getZ(t){let i=this.array[t*this.itemSize+2];return this.normalized&&(i=Bi(i,this.array)),i}setZ(t,i){return this.normalized&&(i=Xe(i,this.array)),this.array[t*this.itemSize+2]=i,this}getW(t){let i=this.array[t*this.itemSize+3];return this.normalized&&(i=Bi(i,this.array)),i}setW(t,i){return this.normalized&&(i=Xe(i,this.array)),this.array[t*this.itemSize+3]=i,this}setXY(t,i,s){return t*=this.itemSize,this.normalized&&(i=Xe(i,this.array),s=Xe(s,this.array)),this.array[t+0]=i,this.array[t+1]=s,this}setXYZ(t,i,s,l){return t*=this.itemSize,this.normalized&&(i=Xe(i,this.array),s=Xe(s,this.array),l=Xe(l,this.array)),this.array[t+0]=i,this.array[t+1]=s,this.array[t+2]=l,this}setXYZW(t,i,s,l,c){return t*=this.itemSize,this.normalized&&(i=Xe(i,this.array),s=Xe(s,this.array),l=Xe(l,this.array),c=Xe(c,this.array)),this.array[t+0]=i,this.array[t+1]=s,this.array[t+2]=l,this.array[t+3]=c,this}onUpload(t){return this.onUploadCallback=t,this}clone(){return new this.constructor(this.array,this.itemSize).copy(this)}toJSON(){const t={itemSize:this.itemSize,type:this.array.constructor.name,array:Array.from(this.array),normalized:this.normalized};return this.name!==""&&(t.name=this.name),this.usage!==yp&&(t.usage=this.usage),t}dispose(){this.dispatchEvent({type:"dispose"})}}class Lx extends vi{constructor(t,i,s){super(new Uint16Array(t),i,s)}}class Ox extends vi{constructor(t,i,s){super(new Uint32Array(t),i,s)}}class En extends vi{constructor(t,i,s){super(new Float32Array(t),i,s)}}const ZE=new Qs,il=new k,ud=new k;class Js{constructor(t=new k,i=-1){this.isSphere=!0,this.center=t,this.radius=i}set(t,i){return this.center.copy(t),this.radius=i,this}setFromPoints(t,i){const s=this.center;i!==void 0?s.copy(i):ZE.setFromPoints(t).getCenter(s);let l=0;for(let c=0,f=t.length;c<f;c++)l=Math.max(l,s.distanceToSquared(t[c]));return this.radius=Math.sqrt(l),this}copy(t){return this.center.copy(t.center),this.radius=t.radius,this}isEmpty(){return this.radius<0}makeEmpty(){return this.center.set(0,0,0),this.radius=-1,this}containsPoint(t){return t.distanceToSquared(this.center)<=this.radius*this.radius}distanceToPoint(t){return t.distanceTo(this.center)-this.radius}intersectsSphere(t){const i=this.radius+t.radius;return t.center.distanceToSquared(this.center)<=i*i}intersectsBox(t){return t.intersectsSphere(this)}intersectsPlane(t){return Math.abs(t.distanceToPoint(this.center))<=this.radius}clampPoint(t,i){const s=this.center.distanceToSquared(t);return i.copy(t),s>this.radius*this.radius&&(i.sub(this.center).normalize(),i.multiplyScalar(this.radius).add(this.center)),i}getBoundingBox(t){return this.isEmpty()?(t.makeEmpty(),t):(t.set(this.center,this.center),t.expandByScalar(this.radius),t)}applyMatrix4(t){return this.center.applyMatrix4(t),this.radius=this.radius*t.getMaxScaleOnAxis(),this}translate(t){return this.center.add(t),this}expandByPoint(t){if(this.isEmpty())return this.center.copy(t),this.radius=0,this;il.subVectors(t,this.center);const i=il.lengthSq();if(i>this.radius*this.radius){const s=Math.sqrt(i),l=(s-this.radius)*.5;this.center.addScaledVector(il,l/s),this.radius+=l}return this}union(t){return t.isEmpty()?this:this.isEmpty()?(this.copy(t),this):(this.center.equals(t.center)===!0?this.radius=Math.max(this.radius,t.radius):(ud.subVectors(t.center,this.center).setLength(t.radius),this.expandByPoint(il.copy(t.center).add(ud)),this.expandByPoint(il.copy(t.center).sub(ud))),this)}equals(t){return t.center.equals(this.center)&&t.radius===this.radius}clone(){return new this.constructor().copy(this)}toJSON(){return{radius:this.radius,center:this.center.toArray()}}fromJSON(t){return this.radius=t.radius,this.center.fromArray(t.center),this}}let KE=0;const Ri=new we,fd=new Mn,Gr=new k,di=new Qs,al=new Qs,Cn=new k;class hn extends Ks{constructor(){super(),this.isBufferGeometry=!0,Object.defineProperty(this,"id",{value:KE++}),this.uuid=Ra(),this.name="",this.type="BufferGeometry",this.index=null,this.indirect=null,this.indirectOffset=0,this.attributes={},this.morphAttributes={},this.morphTargetsRelative=!1,this.groups=[],this.boundingBox=null,this.boundingSphere=null,this.drawRange={start:0,count:1/0},this.userData={},this._transformed=!1}getIndex(){return this.index}setIndex(t){return Array.isArray(t)?this.index=new(pE(t)?Ox:Lx)(t,1):this.index=t,this}setIndirect(t,i=0){return this.indirect=t,this.indirectOffset=i,this}getIndirect(){return this.indirect}getAttribute(t){return this.attributes[t]}setAttribute(t,i){return this.attributes[t]=i,this}deleteAttribute(t){return delete this.attributes[t],this}hasAttribute(t){return this.attributes[t]!==void 0}addGroup(t,i,s=0){this.groups.push({start:t,count:i,materialIndex:s})}clearGroups(){this.groups=[]}setDrawRange(t,i){this.drawRange.start=t,this.drawRange.count=i}applyMatrix4(t){const i=this.attributes.position;i!==void 0&&(i.applyMatrix4(t),i.needsUpdate=!0);const s=this.attributes.normal;if(s!==void 0){const c=new ue().getNormalMatrix(t);s.applyNormalMatrix(c),s.needsUpdate=!0}const l=this.attributes.tangent;return l!==void 0&&(l.transformDirection(t),l.needsUpdate=!0),this.boundingBox!==null&&this.computeBoundingBox(),this.boundingSphere!==null&&this.computeBoundingSphere(),this._transformed=!0,this}applyQuaternion(t){return Ri.makeRotationFromQuaternion(t),this.applyMatrix4(Ri),this}rotateX(t){return Ri.makeRotationX(t),this.applyMatrix4(Ri),this}rotateY(t){return Ri.makeRotationY(t),this.applyMatrix4(Ri),this}rotateZ(t){return Ri.makeRotationZ(t),this.applyMatrix4(Ri),this}translate(t,i,s){return Ri.makeTranslation(t,i,s),this.applyMatrix4(Ri),this}scale(t,i,s){return Ri.makeScale(t,i,s),this.applyMatrix4(Ri),this}lookAt(t){return fd.lookAt(t),fd.updateMatrix(),this.applyMatrix4(fd.matrix),this}center(){return this.computeBoundingBox(),this.boundingBox.getCenter(Gr).negate(),this.translate(Gr.x,Gr.y,Gr.z),this}setFromPoints(t){const i=this.getAttribute("position");if(i===void 0){const s=[];for(let l=0,c=t.length;l<c;l++){const f=t[l];s.push(f.x,f.y,f.z||0)}this.setAttribute("position",new En(s,3))}else{const s=Math.min(t.length,i.count);for(let l=0;l<s;l++){const c=t[l];i.setXYZ(l,c.x,c.y,c.z||0)}t.length>i.count&&se("BufferGeometry: Buffer size too small for points data. Use .dispose() and create a new geometry."),i.needsUpdate=!0}return this}computeBoundingBox(){this.boundingBox===null&&(this.boundingBox=new Qs);const t=this.attributes.position,i=this.morphAttributes.position;if(t&&t.isGLBufferAttribute){Ae("BufferGeometry.computeBoundingBox(): GLBufferAttribute requires a manual bounding box.",this),this.boundingBox.set(new k(-1/0,-1/0,-1/0),new k(1/0,1/0,1/0));return}if(t!==void 0){if(this.boundingBox.setFromBufferAttribute(t),i)for(let s=0,l=i.length;s<l;s++){const c=i[s];di.setFromBufferAttribute(c),this.morphTargetsRelative?(Cn.addVectors(this.boundingBox.min,di.min),this.boundingBox.expandByPoint(Cn),Cn.addVectors(this.boundingBox.max,di.max),this.boundingBox.expandByPoint(Cn)):(this.boundingBox.expandByPoint(di.min),this.boundingBox.expandByPoint(di.max))}}else this.boundingBox.makeEmpty();(isNaN(this.boundingBox.min.x)||isNaN(this.boundingBox.min.y)||isNaN(this.boundingBox.min.z))&&Ae('BufferGeometry.computeBoundingBox(): Computed min/max have NaN values. The "position" attribute is likely to have NaN values.',this)}computeBoundingSphere(){this.boundingSphere===null&&(this.boundingSphere=new Js);const t=this.attributes.position,i=this.morphAttributes.position;if(t&&t.isGLBufferAttribute){Ae("BufferGeometry.computeBoundingSphere(): GLBufferAttribute requires a manual bounding sphere.",this),this.boundingSphere.set(new k,1/0);return}if(t){const s=this.boundingSphere.center;if(di.setFromBufferAttribute(t),i)for(let c=0,f=i.length;c<f;c++){const p=i[c];al.setFromBufferAttribute(p),this.morphTargetsRelative?(Cn.addVectors(di.min,al.min),di.expandByPoint(Cn),Cn.addVectors(di.max,al.max),di.expandByPoint(Cn)):(di.expandByPoint(al.min),di.expandByPoint(al.max))}di.getCenter(s);let l=0;for(let c=0,f=t.count;c<f;c++)Cn.fromBufferAttribute(t,c),l=Math.max(l,s.distanceToSquared(Cn));if(i)for(let c=0,f=i.length;c<f;c++){const p=i[c],m=this.morphTargetsRelative;for(let d=0,x=p.count;d<x;d++)Cn.fromBufferAttribute(p,d),m&&(Gr.fromBufferAttribute(t,d),Cn.add(Gr)),l=Math.max(l,s.distanceToSquared(Cn))}this.boundingSphere.radius=Math.sqrt(l),isNaN(this.boundingSphere.radius)&&Ae('BufferGeometry.computeBoundingSphere(): Computed radius is NaN. The "position" attribute is likely to have NaN values.',this)}}computeTangents(){const t=this.index,i=this.attributes;if(t===null||i.position===void 0||i.normal===void 0||i.uv===void 0){Ae("BufferGeometry: .computeTangents() failed. Missing required attributes (index, position, normal or uv)");return}const s=i.position,l=i.normal,c=i.uv;let f=this.getAttribute("tangent");(f===void 0||f.count!==s.count)&&(f=new vi(new Float32Array(4*s.count),4),this.setAttribute("tangent",f));const p=[],m=[];for(let T=0;T<s.count;T++)p[T]=new k,m[T]=new k;const d=new k,x=new k,v=new k,g=new oe,S=new oe,b=new oe,w=new k,E=new k;function _(T,P,$){d.fromBufferAttribute(s,T),x.fromBufferAttribute(s,P),v.fromBufferAttribute(s,$),g.fromBufferAttribute(c,T),S.fromBufferAttribute(c,P),b.fromBufferAttribute(c,$),x.sub(d),v.sub(d),S.sub(g),b.sub(g);const Y=1/(S.x*b.y-b.x*S.y);isFinite(Y)&&(w.copy(x).multiplyScalar(b.y).addScaledVector(v,-S.y).multiplyScalar(Y),E.copy(v).multiplyScalar(S.x).addScaledVector(x,-b.x).multiplyScalar(Y),p[T].add(w),p[P].add(w),p[$].add(w),m[T].add(E),m[P].add(E),m[$].add(E))}let z=this.groups;z.length===0&&(z=[{start:0,count:t.count}]);for(let T=0,P=z.length;T<P;++T){const $=z[T],Y=$.start,J=$.count;for(let dt=Y,gt=Y+J;dt<gt;dt+=3)_(t.getX(dt+0),t.getX(dt+1),t.getX(dt+2))}const F=new k,C=new k,B=new k,O=new k;function U(T){B.fromBufferAttribute(l,T),O.copy(B);const P=p[T];F.copy(P),F.sub(B.multiplyScalar(B.dot(P))).normalize(),C.crossVectors(O,P);const Y=C.dot(m[T])<0?-1:1;f.setXYZW(T,F.x,F.y,F.z,Y)}for(let T=0,P=z.length;T<P;++T){const $=z[T],Y=$.start,J=$.count;for(let dt=Y,gt=Y+J;dt<gt;dt+=3)U(t.getX(dt+0)),U(t.getX(dt+1)),U(t.getX(dt+2))}this._transformed=!0}computeVertexNormals(){const t=this.index,i=this.getAttribute("position");if(i!==void 0){let s=this.getAttribute("normal");if(s===void 0||s.count!==i.count)s=new vi(new Float32Array(i.count*3),3),this.setAttribute("normal",s);else for(let g=0,S=s.count;g<S;g++)s.setXYZ(g,0,0,0);const l=new k,c=new k,f=new k,p=new k,m=new k,d=new k,x=new k,v=new k;if(t)for(let g=0,S=t.count;g<S;g+=3){const b=t.getX(g+0),w=t.getX(g+1),E=t.getX(g+2);l.fromBufferAttribute(i,b),c.fromBufferAttribute(i,w),f.fromBufferAttribute(i,E),x.subVectors(f,c),v.subVectors(l,c),x.cross(v),p.fromBufferAttribute(s,b),m.fromBufferAttribute(s,w),d.fromBufferAttribute(s,E),p.add(x),m.add(x),d.add(x),s.setXYZ(b,p.x,p.y,p.z),s.setXYZ(w,m.x,m.y,m.z),s.setXYZ(E,d.x,d.y,d.z)}else for(let g=0,S=i.count;g<S;g+=3)l.fromBufferAttribute(i,g+0),c.fromBufferAttribute(i,g+1),f.fromBufferAttribute(i,g+2),x.subVectors(f,c),v.subVectors(l,c),x.cross(v),s.setXYZ(g+0,x.x,x.y,x.z),s.setXYZ(g+1,x.x,x.y,x.z),s.setXYZ(g+2,x.x,x.y,x.z);this.normalizeNormals(),s.needsUpdate=!0}}normalizeNormals(){const t=this.attributes.normal;for(let i=0,s=t.count;i<s;i++)Cn.fromBufferAttribute(t,i),Cn.normalize(),t.setXYZ(i,Cn.x,Cn.y,Cn.z)}toNonIndexed(){function t(p,m){const d=p.array,x=p.itemSize,v=p.normalized,g=new d.constructor(m.length*x);let S=0,b=0;for(let w=0,E=m.length;w<E;w++){p.isInterleavedBufferAttribute?S=m[w]*p.data.stride+p.offset:S=m[w]*x;for(let _=0;_<x;_++)g[b++]=d[S++]}return new vi(g,x,v)}if(this.index===null)return se("BufferGeometry.toNonIndexed(): BufferGeometry is already non-indexed."),this;const i=new hn,s=this.index.array,l=this.attributes;for(const p in l){const m=l[p],d=t(m,s);i.setAttribute(p,d)}const c=this.morphAttributes;for(const p in c){const m=[],d=c[p];for(let x=0,v=d.length;x<v;x++){const g=d[x],S=t(g,s);m.push(S)}i.morphAttributes[p]=m}i.morphTargetsRelative=this.morphTargetsRelative;const f=this.groups;for(let p=0,m=f.length;p<m;p++){const d=f[p];i.addGroup(d.start,d.count,d.materialIndex)}return i}toJSON(){const t={metadata:{version:4.7,type:"BufferGeometry",generator:"BufferGeometry.toJSON"}};if(t.uuid=this.uuid,t.type=this.parameters!==void 0&&this._transformed===!0?"BufferGeometry":this.type,this.name!==""&&(t.name=this.name),Object.keys(this.userData).length>0&&(t.userData=this.userData),this.parameters!==void 0&&this._transformed!==!0){const m=this.parameters;for(const d in m)m[d]!==void 0&&(t[d]=m[d]);return t}t.data={attributes:{}};const i=this.index;i!==null&&(t.data.index={type:i.array.constructor.name,array:Array.prototype.slice.call(i.array)});const s=this.attributes;for(const m in s){const d=s[m];t.data.attributes[m]=d.toJSON(t.data)}const l={};let c=!1;for(const m in this.morphAttributes){const d=this.morphAttributes[m],x=[];for(let v=0,g=d.length;v<g;v++){const S=d[v];x.push(S.toJSON(t.data))}x.length>0&&(l[m]=x,c=!0)}c&&(t.data.morphAttributes=l,t.data.morphTargetsRelative=this.morphTargetsRelative);const f=this.groups;f.length>0&&(t.data.groups=JSON.parse(JSON.stringify(f)));const p=this.boundingSphere;return p!==null&&(t.data.boundingSphere=p.toJSON()),t}clone(){return new this.constructor().copy(this)}copy(t){this.index=null,this.attributes={},this.morphAttributes={},this.groups=[],this.boundingBox=null,this.boundingSphere=null;const i={};this.name=t.name;const s=t.index;s!==null&&this.setIndex(s.clone());const l=t.attributes;for(const d in l){const x=l[d];this.setAttribute(d,x.clone(i))}const c=t.morphAttributes;for(const d in c){const x=[],v=c[d];for(let g=0,S=v.length;g<S;g++)x.push(v[g].clone(i));this.morphAttributes[d]=x}this.morphTargetsRelative=t.morphTargetsRelative;const f=t.groups;for(let d=0,x=f.length;d<x;d++){const v=f[d];this.addGroup(v.start,v.count,v.materialIndex)}const p=t.boundingBox;p!==null&&(this.boundingBox=p.clone());const m=t.boundingSphere;return m!==null&&(this.boundingSphere=m.clone()),this.drawRange.start=t.drawRange.start,this.drawRange.count=t.drawRange.count,this.userData=t.userData,this._transformed=t._transformed,this}dispose(){this.dispatchEvent({type:"dispose"})}}class QE{constructor(t,i){this.isInterleavedBuffer=!0,this.array=t,this.stride=i,this.count=t!==void 0?t.length/i:0,this.usage=yp,this.updateRanges=[],this.version=0,this.uuid=Ra()}onUploadCallback(){}set needsUpdate(t){t===!0&&this.version++}setUsage(t){return this.usage=t,this}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}copy(t){return this.array=new t.array.constructor(t.array),this.count=t.count,this.stride=t.stride,this.usage=t.usage,this}copyAt(t,i,s){t*=this.stride,s*=i.stride;for(let l=0,c=this.stride;l<c;l++)this.array[t+l]=i.array[s+l];return this}set(t,i=0){return this.array.set(t,i),this}clone(t){t.arrayBuffers===void 0&&(t.arrayBuffers={}),this.array.buffer._uuid===void 0&&(this.array.buffer._uuid=Ra()),t.arrayBuffers[this.array.buffer._uuid]===void 0&&(t.arrayBuffers[this.array.buffer._uuid]=this.array.slice(0).buffer);const i=new this.array.constructor(t.arrayBuffers[this.array.buffer._uuid]),s=new this.constructor(i,this.stride);return s.setUsage(this.usage),s}onUpload(t){return this.onUploadCallback=t,this}toJSON(t){return t.arrayBuffers===void 0&&(t.arrayBuffers={}),this.array.buffer._uuid===void 0&&(this.array.buffer._uuid=Ra()),t.arrayBuffers[this.array.buffer._uuid]===void 0&&(t.arrayBuffers[this.array.buffer._uuid]=Array.from(new Uint32Array(this.array.buffer))),{uuid:this.uuid,buffer:this.array.buffer._uuid,type:this.array.constructor.name,stride:this.stride}}}const Xn=new k;class Au{constructor(t,i,s,l=!1){this.isInterleavedBufferAttribute=!0,this.name="",this.data=t,this.itemSize=i,this.offset=s,this.normalized=l}get count(){return this.data.count}get array(){return this.data.array}set needsUpdate(t){this.data.needsUpdate=t}applyMatrix4(t){for(let i=0,s=this.data.count;i<s;i++)Xn.fromBufferAttribute(this,i),Xn.applyMatrix4(t),this.setXYZ(i,Xn.x,Xn.y,Xn.z);return this}applyNormalMatrix(t){for(let i=0,s=this.count;i<s;i++)Xn.fromBufferAttribute(this,i),Xn.applyNormalMatrix(t),this.setXYZ(i,Xn.x,Xn.y,Xn.z);return this}transformDirection(t){for(let i=0,s=this.count;i<s;i++)Xn.fromBufferAttribute(this,i),Xn.transformDirection(t),this.setXYZ(i,Xn.x,Xn.y,Xn.z);return this}getComponent(t,i){let s=this.array[t*this.data.stride+this.offset+i];return this.normalized&&(s=Bi(s,this.array)),s}setComponent(t,i,s){return this.normalized&&(s=Xe(s,this.array)),this.data.array[t*this.data.stride+this.offset+i]=s,this}setX(t,i){return this.normalized&&(i=Xe(i,this.array)),this.data.array[t*this.data.stride+this.offset]=i,this}setY(t,i){return this.normalized&&(i=Xe(i,this.array)),this.data.array[t*this.data.stride+this.offset+1]=i,this}setZ(t,i){return this.normalized&&(i=Xe(i,this.array)),this.data.array[t*this.data.stride+this.offset+2]=i,this}setW(t,i){return this.normalized&&(i=Xe(i,this.array)),this.data.array[t*this.data.stride+this.offset+3]=i,this}getX(t){let i=this.data.array[t*this.data.stride+this.offset];return this.normalized&&(i=Bi(i,this.array)),i}getY(t){let i=this.data.array[t*this.data.stride+this.offset+1];return this.normalized&&(i=Bi(i,this.array)),i}getZ(t){let i=this.data.array[t*this.data.stride+this.offset+2];return this.normalized&&(i=Bi(i,this.array)),i}getW(t){let i=this.data.array[t*this.data.stride+this.offset+3];return this.normalized&&(i=Bi(i,this.array)),i}setXY(t,i,s){return t=t*this.data.stride+this.offset,this.normalized&&(i=Xe(i,this.array),s=Xe(s,this.array)),this.data.array[t+0]=i,this.data.array[t+1]=s,this}setXYZ(t,i,s,l){return t=t*this.data.stride+this.offset,this.normalized&&(i=Xe(i,this.array),s=Xe(s,this.array),l=Xe(l,this.array)),this.data.array[t+0]=i,this.data.array[t+1]=s,this.data.array[t+2]=l,this}setXYZW(t,i,s,l,c){return t=t*this.data.stride+this.offset,this.normalized&&(i=Xe(i,this.array),s=Xe(s,this.array),l=Xe(l,this.array),c=Xe(c,this.array)),this.data.array[t+0]=i,this.data.array[t+1]=s,this.data.array[t+2]=l,this.data.array[t+3]=c,this}clone(t){if(t===void 0){Tu("InterleavedBufferAttribute.clone(): Cloning an interleaved buffer attribute will de-interleave buffer data.");const i=[];for(let s=0;s<this.count;s++){const l=s*this.data.stride+this.offset;for(let c=0;c<this.itemSize;c++)i.push(this.data.array[l+c])}return new vi(new this.array.constructor(i),this.itemSize,this.normalized)}else return t.interleavedBuffers===void 0&&(t.interleavedBuffers={}),t.interleavedBuffers[this.data.uuid]===void 0&&(t.interleavedBuffers[this.data.uuid]=this.data.clone(t)),new Au(t.interleavedBuffers[this.data.uuid],this.itemSize,this.offset,this.normalized)}toJSON(t){if(t===void 0){Tu("InterleavedBufferAttribute.toJSON(): Serializing an interleaved buffer attribute will de-interleave buffer data.");const i=[];for(let s=0;s<this.count;s++){const l=s*this.data.stride+this.offset;for(let c=0;c<this.itemSize;c++)i.push(this.data.array[l+c])}return{itemSize:this.itemSize,type:this.array.constructor.name,array:i,normalized:this.normalized}}else return t.interleavedBuffers===void 0&&(t.interleavedBuffers={}),t.interleavedBuffers[this.data.uuid]===void 0&&(t.interleavedBuffers[this.data.uuid]=this.data.toJSON(t)),{isInterleavedBufferAttribute:!0,itemSize:this.itemSize,data:this.data.uuid,offset:this.offset,normalized:this.normalized}}}let JE=0;class ms extends Ks{constructor(){super(),this.isMaterial=!0,Object.defineProperty(this,"id",{value:JE++}),this.uuid=Ra(),this.name="",this.type="Material",this.blending=Qr,this.side=ds,this.vertexColors=!1,this.opacity=1,this.transparent=!1,this.alphaHash=!1,this.blendSrc=Nd,this.blendDst=Ld,this.blendEquation=Vs,this.blendSrcAlpha=null,this.blendDstAlpha=null,this.blendEquationAlpha=null,this.blendColor=new ie(0,0,0),this.blendAlpha=0,this.depthFunc=eo,this.depthTest=!0,this.depthWrite=!0,this.stencilWriteMask=255,this.stencilFunc=Z_,this.stencilRef=0,this.stencilFuncMask=255,this.stencilFail=Ur,this.stencilZFail=Ur,this.stencilZPass=Ur,this.stencilWrite=!1,this.clippingPlanes=null,this.clipIntersection=!1,this.clipShadows=!1,this.shadowSide=null,this.colorWrite=!0,this.precision=null,this.polygonOffset=!1,this.polygonOffsetFactor=0,this.polygonOffsetUnits=0,this.dithering=!1,this.alphaToCoverage=!1,this.premultipliedAlpha=!1,this.forceSinglePass=!1,this.allowOverride=!0,this.visible=!0,this.toneMapped=!0,this.userData={},this.version=0,this._alphaTest=0}get alphaTest(){return this._alphaTest}set alphaTest(t){this._alphaTest>0!=t>0&&this.version++,this._alphaTest=t}onBeforeRender(){}onBeforeCompile(){}customProgramCacheKey(){return this.onBeforeCompile.toString()}setValues(t){if(t!==void 0)for(const i in t){const s=t[i];if(s===void 0){se(`Material: parameter '${i}' has value of undefined.`);continue}const l=this[i];if(l===void 0){se(`Material: '${i}' is not a property of THREE.${this.type}.`);continue}l&&l.isColor?l.set(s):l&&l.isVector2&&s&&s.isVector2||l&&l.isEuler&&s&&s.isEuler||l&&l.isVector3&&s&&s.isVector3?l.copy(s):this[i]=s}}toJSON(t){const i=t===void 0||typeof t=="string";i&&(t={textures:{},images:{}});const s={metadata:{version:4.7,type:"Material",generator:"Material.toJSON"}};s.uuid=this.uuid,s.type=this.type,this.name!==""&&(s.name=this.name),this.color&&this.color.isColor&&(s.color=this.color.getHex()),this.roughness!==void 0&&(s.roughness=this.roughness),this.metalness!==void 0&&(s.metalness=this.metalness),this.sheen!==void 0&&(s.sheen=this.sheen),this.sheenColor&&this.sheenColor.isColor&&(s.sheenColor=this.sheenColor.getHex()),this.sheenRoughness!==void 0&&(s.sheenRoughness=this.sheenRoughness),this.emissive&&this.emissive.isColor&&(s.emissive=this.emissive.getHex()),this.emissiveIntensity!==void 0&&this.emissiveIntensity!==1&&(s.emissiveIntensity=this.emissiveIntensity),this.specular&&this.specular.isColor&&(s.specular=this.specular.getHex()),this.specularIntensity!==void 0&&(s.specularIntensity=this.specularIntensity),this.specularColor&&this.specularColor.isColor&&(s.specularColor=this.specularColor.getHex()),this.shininess!==void 0&&(s.shininess=this.shininess),this.clearcoat!==void 0&&(s.clearcoat=this.clearcoat),this.clearcoatRoughness!==void 0&&(s.clearcoatRoughness=this.clearcoatRoughness),this.clearcoatMap&&this.clearcoatMap.isTexture&&(s.clearcoatMap=this.clearcoatMap.toJSON(t).uuid),this.clearcoatRoughnessMap&&this.clearcoatRoughnessMap.isTexture&&(s.clearcoatRoughnessMap=this.clearcoatRoughnessMap.toJSON(t).uuid),this.clearcoatNormalMap&&this.clearcoatNormalMap.isTexture&&(s.clearcoatNormalMap=this.clearcoatNormalMap.toJSON(t).uuid,s.clearcoatNormalScale=this.clearcoatNormalScale.toArray()),this.sheenColorMap&&this.sheenColorMap.isTexture&&(s.sheenColorMap=this.sheenColorMap.toJSON(t).uuid),this.sheenRoughnessMap&&this.sheenRoughnessMap.isTexture&&(s.sheenRoughnessMap=this.sheenRoughnessMap.toJSON(t).uuid),this.dispersion!==void 0&&(s.dispersion=this.dispersion),this.iridescence!==void 0&&(s.iridescence=this.iridescence),this.iridescenceIOR!==void 0&&(s.iridescenceIOR=this.iridescenceIOR),this.iridescenceThicknessRange!==void 0&&(s.iridescenceThicknessRange=this.iridescenceThicknessRange),this.iridescenceMap&&this.iridescenceMap.isTexture&&(s.iridescenceMap=this.iridescenceMap.toJSON(t).uuid),this.iridescenceThicknessMap&&this.iridescenceThicknessMap.isTexture&&(s.iridescenceThicknessMap=this.iridescenceThicknessMap.toJSON(t).uuid),this.anisotropy!==void 0&&(s.anisotropy=this.anisotropy),this.anisotropyRotation!==void 0&&(s.anisotropyRotation=this.anisotropyRotation),this.anisotropyMap&&this.anisotropyMap.isTexture&&(s.anisotropyMap=this.anisotropyMap.toJSON(t).uuid),this.map&&this.map.isTexture&&(s.map=this.map.toJSON(t).uuid),this.matcap&&this.matcap.isTexture&&(s.matcap=this.matcap.toJSON(t).uuid),this.alphaMap&&this.alphaMap.isTexture&&(s.alphaMap=this.alphaMap.toJSON(t).uuid),this.lightMap&&this.lightMap.isTexture&&(s.lightMap=this.lightMap.toJSON(t).uuid,s.lightMapIntensity=this.lightMapIntensity),this.aoMap&&this.aoMap.isTexture&&(s.aoMap=this.aoMap.toJSON(t).uuid,s.aoMapIntensity=this.aoMapIntensity),this.bumpMap&&this.bumpMap.isTexture&&(s.bumpMap=this.bumpMap.toJSON(t).uuid,s.bumpScale=this.bumpScale),this.normalMap&&this.normalMap.isTexture&&(s.normalMap=this.normalMap.toJSON(t).uuid,s.normalMapType=this.normalMapType,s.normalScale=this.normalScale.toArray()),this.displacementMap&&this.displacementMap.isTexture&&(s.displacementMap=this.displacementMap.toJSON(t).uuid,s.displacementScale=this.displacementScale,s.displacementBias=this.displacementBias),this.roughnessMap&&this.roughnessMap.isTexture&&(s.roughnessMap=this.roughnessMap.toJSON(t).uuid),this.metalnessMap&&this.metalnessMap.isTexture&&(s.metalnessMap=this.metalnessMap.toJSON(t).uuid),this.emissiveMap&&this.emissiveMap.isTexture&&(s.emissiveMap=this.emissiveMap.toJSON(t).uuid),this.specularMap&&this.specularMap.isTexture&&(s.specularMap=this.specularMap.toJSON(t).uuid),this.specularIntensityMap&&this.specularIntensityMap.isTexture&&(s.specularIntensityMap=this.specularIntensityMap.toJSON(t).uuid),this.specularColorMap&&this.specularColorMap.isTexture&&(s.specularColorMap=this.specularColorMap.toJSON(t).uuid),this.envMap&&this.envMap.isTexture&&(s.envMap=this.envMap.toJSON(t).uuid,this.combine!==void 0&&(s.combine=this.combine)),this.envMapRotation!==void 0&&(s.envMapRotation=this.envMapRotation.toArray()),this.envMapIntensity!==void 0&&(s.envMapIntensity=this.envMapIntensity),this.reflectivity!==void 0&&(s.reflectivity=this.reflectivity),this.refractionRatio!==void 0&&(s.refractionRatio=this.refractionRatio),this.gradientMap&&this.gradientMap.isTexture&&(s.gradientMap=this.gradientMap.toJSON(t).uuid),this.transmission!==void 0&&(s.transmission=this.transmission),this.transmissionMap&&this.transmissionMap.isTexture&&(s.transmissionMap=this.transmissionMap.toJSON(t).uuid),this.thickness!==void 0&&(s.thickness=this.thickness),this.thicknessMap&&this.thicknessMap.isTexture&&(s.thicknessMap=this.thicknessMap.toJSON(t).uuid),this.attenuationDistance!==void 0&&this.attenuationDistance!==1/0&&(s.attenuationDistance=this.attenuationDistance),this.attenuationColor!==void 0&&(s.attenuationColor=this.attenuationColor.getHex()),this.size!==void 0&&(s.size=this.size),this.shadowSide!==null&&(s.shadowSide=this.shadowSide),this.sizeAttenuation!==void 0&&(s.sizeAttenuation=this.sizeAttenuation),this.blending!==Qr&&(s.blending=this.blending),this.side!==ds&&(s.side=this.side),this.vertexColors===!0&&(s.vertexColors=!0),this.opacity<1&&(s.opacity=this.opacity),this.transparent===!0&&(s.transparent=!0),this.blendSrc!==Nd&&(s.blendSrc=this.blendSrc),this.blendDst!==Ld&&(s.blendDst=this.blendDst),this.blendEquation!==Vs&&(s.blendEquation=this.blendEquation),this.blendSrcAlpha!==null&&(s.blendSrcAlpha=this.blendSrcAlpha),this.blendDstAlpha!==null&&(s.blendDstAlpha=this.blendDstAlpha),this.blendEquationAlpha!==null&&(s.blendEquationAlpha=this.blendEquationAlpha),this.blendColor&&this.blendColor.isColor&&(s.blendColor=this.blendColor.getHex()),this.blendAlpha!==0&&(s.blendAlpha=this.blendAlpha),this.depthFunc!==eo&&(s.depthFunc=this.depthFunc),this.depthTest===!1&&(s.depthTest=this.depthTest),this.depthWrite===!1&&(s.depthWrite=this.depthWrite),this.colorWrite===!1&&(s.colorWrite=this.colorWrite),this.stencilWriteMask!==255&&(s.stencilWriteMask=this.stencilWriteMask),this.stencilFunc!==Z_&&(s.stencilFunc=this.stencilFunc),this.stencilRef!==0&&(s.stencilRef=this.stencilRef),this.stencilFuncMask!==255&&(s.stencilFuncMask=this.stencilFuncMask),this.stencilFail!==Ur&&(s.stencilFail=this.stencilFail),this.stencilZFail!==Ur&&(s.stencilZFail=this.stencilZFail),this.stencilZPass!==Ur&&(s.stencilZPass=this.stencilZPass),this.stencilWrite===!0&&(s.stencilWrite=this.stencilWrite),this.rotation!==void 0&&this.rotation!==0&&(s.rotation=this.rotation),this.polygonOffset===!0&&(s.polygonOffset=!0),this.polygonOffsetFactor!==0&&(s.polygonOffsetFactor=this.polygonOffsetFactor),this.polygonOffsetUnits!==0&&(s.polygonOffsetUnits=this.polygonOffsetUnits),this.linewidth!==void 0&&this.linewidth!==1&&(s.linewidth=this.linewidth),this.dashSize!==void 0&&(s.dashSize=this.dashSize),this.gapSize!==void 0&&(s.gapSize=this.gapSize),this.scale!==void 0&&(s.scale=this.scale),this.dithering===!0&&(s.dithering=!0),this.alphaTest>0&&(s.alphaTest=this.alphaTest),this.alphaHash===!0&&(s.alphaHash=!0),this.alphaToCoverage===!0&&(s.alphaToCoverage=!0),this.premultipliedAlpha===!0&&(s.premultipliedAlpha=!0),this.forceSinglePass===!0&&(s.forceSinglePass=!0),this.allowOverride===!1&&(s.allowOverride=!1),this.wireframe===!0&&(s.wireframe=!0),this.wireframeLinewidth>1&&(s.wireframeLinewidth=this.wireframeLinewidth),this.wireframeLinecap!=="round"&&(s.wireframeLinecap=this.wireframeLinecap),this.wireframeLinejoin!=="round"&&(s.wireframeLinejoin=this.wireframeLinejoin),this.flatShading===!0&&(s.flatShading=!0),this.visible===!1&&(s.visible=!1),this.toneMapped===!1&&(s.toneMapped=!1),this.fog===!1&&(s.fog=!1),Object.keys(this.userData).length>0&&(s.userData=this.userData);function l(c){const f=[];for(const p in c){const m=c[p];delete m.metadata,f.push(m)}return f}if(i){const c=l(t.textures),f=l(t.images);c.length>0&&(s.textures=c),f.length>0&&(s.images=f)}return s}fromJSON(t,i){if(t.uuid!==void 0&&(this.uuid=t.uuid),t.name!==void 0&&(this.name=t.name),t.color!==void 0&&this.color!==void 0&&this.color.setHex(t.color),t.roughness!==void 0&&(this.roughness=t.roughness),t.metalness!==void 0&&(this.metalness=t.metalness),t.sheen!==void 0&&(this.sheen=t.sheen),t.sheenColor!==void 0&&(this.sheenColor=new ie().setHex(t.sheenColor)),t.sheenRoughness!==void 0&&(this.sheenRoughness=t.sheenRoughness),t.emissive!==void 0&&this.emissive!==void 0&&this.emissive.setHex(t.emissive),t.specular!==void 0&&this.specular!==void 0&&this.specular.setHex(t.specular),t.specularIntensity!==void 0&&(this.specularIntensity=t.specularIntensity),t.specularColor!==void 0&&this.specularColor!==void 0&&this.specularColor.setHex(t.specularColor),t.shininess!==void 0&&(this.shininess=t.shininess),t.clearcoat!==void 0&&(this.clearcoat=t.clearcoat),t.clearcoatRoughness!==void 0&&(this.clearcoatRoughness=t.clearcoatRoughness),t.dispersion!==void 0&&(this.dispersion=t.dispersion),t.iridescence!==void 0&&(this.iridescence=t.iridescence),t.iridescenceIOR!==void 0&&(this.iridescenceIOR=t.iridescenceIOR),t.iridescenceThicknessRange!==void 0&&(this.iridescenceThicknessRange=t.iridescenceThicknessRange),t.transmission!==void 0&&(this.transmission=t.transmission),t.thickness!==void 0&&(this.thickness=t.thickness),t.attenuationDistance!==void 0&&(this.attenuationDistance=t.attenuationDistance),t.attenuationColor!==void 0&&this.attenuationColor!==void 0&&this.attenuationColor.setHex(t.attenuationColor),t.anisotropy!==void 0&&(this.anisotropy=t.anisotropy),t.anisotropyRotation!==void 0&&(this.anisotropyRotation=t.anisotropyRotation),t.fog!==void 0&&(this.fog=t.fog),t.flatShading!==void 0&&(this.flatShading=t.flatShading),t.blending!==void 0&&(this.blending=t.blending),t.combine!==void 0&&(this.combine=t.combine),t.side!==void 0&&(this.side=t.side),t.shadowSide!==void 0&&(this.shadowSide=t.shadowSide),t.opacity!==void 0&&(this.opacity=t.opacity),t.transparent!==void 0&&(this.transparent=t.transparent),t.alphaTest!==void 0&&(this.alphaTest=t.alphaTest),t.alphaHash!==void 0&&(this.alphaHash=t.alphaHash),t.depthFunc!==void 0&&(this.depthFunc=t.depthFunc),t.depthTest!==void 0&&(this.depthTest=t.depthTest),t.depthWrite!==void 0&&(this.depthWrite=t.depthWrite),t.colorWrite!==void 0&&(this.colorWrite=t.colorWrite),t.blendSrc!==void 0&&(this.blendSrc=t.blendSrc),t.blendDst!==void 0&&(this.blendDst=t.blendDst),t.blendEquation!==void 0&&(this.blendEquation=t.blendEquation),t.blendSrcAlpha!==void 0&&(this.blendSrcAlpha=t.blendSrcAlpha),t.blendDstAlpha!==void 0&&(this.blendDstAlpha=t.blendDstAlpha),t.blendEquationAlpha!==void 0&&(this.blendEquationAlpha=t.blendEquationAlpha),t.blendColor!==void 0&&this.blendColor!==void 0&&this.blendColor.setHex(t.blendColor),t.blendAlpha!==void 0&&(this.blendAlpha=t.blendAlpha),t.stencilWriteMask!==void 0&&(this.stencilWriteMask=t.stencilWriteMask),t.stencilFunc!==void 0&&(this.stencilFunc=t.stencilFunc),t.stencilRef!==void 0&&(this.stencilRef=t.stencilRef),t.stencilFuncMask!==void 0&&(this.stencilFuncMask=t.stencilFuncMask),t.stencilFail!==void 0&&(this.stencilFail=t.stencilFail),t.stencilZFail!==void 0&&(this.stencilZFail=t.stencilZFail),t.stencilZPass!==void 0&&(this.stencilZPass=t.stencilZPass),t.stencilWrite!==void 0&&(this.stencilWrite=t.stencilWrite),t.wireframe!==void 0&&(this.wireframe=t.wireframe),t.wireframeLinewidth!==void 0&&(this.wireframeLinewidth=t.wireframeLinewidth),t.wireframeLinecap!==void 0&&(this.wireframeLinecap=t.wireframeLinecap),t.wireframeLinejoin!==void 0&&(this.wireframeLinejoin=t.wireframeLinejoin),t.rotation!==void 0&&(this.rotation=t.rotation),t.linewidth!==void 0&&(this.linewidth=t.linewidth),t.dashSize!==void 0&&(this.dashSize=t.dashSize),t.gapSize!==void 0&&(this.gapSize=t.gapSize),t.scale!==void 0&&(this.scale=t.scale),t.polygonOffset!==void 0&&(this.polygonOffset=t.polygonOffset),t.polygonOffsetFactor!==void 0&&(this.polygonOffsetFactor=t.polygonOffsetFactor),t.polygonOffsetUnits!==void 0&&(this.polygonOffsetUnits=t.polygonOffsetUnits),t.dithering!==void 0&&(this.dithering=t.dithering),t.alphaToCoverage!==void 0&&(this.alphaToCoverage=t.alphaToCoverage),t.premultipliedAlpha!==void 0&&(this.premultipliedAlpha=t.premultipliedAlpha),t.forceSinglePass!==void 0&&(this.forceSinglePass=t.forceSinglePass),t.allowOverride!==void 0&&(this.allowOverride=t.allowOverride),t.visible!==void 0&&(this.visible=t.visible),t.toneMapped!==void 0&&(this.toneMapped=t.toneMapped),t.userData!==void 0&&(this.userData=t.userData),t.vertexColors!==void 0&&(typeof t.vertexColors=="number"?this.vertexColors=t.vertexColors>0:this.vertexColors=t.vertexColors),t.size!==void 0&&(this.size=t.size),t.sizeAttenuation!==void 0&&(this.sizeAttenuation=t.sizeAttenuation),t.map!==void 0&&(this.map=i[t.map]||null),t.matcap!==void 0&&(this.matcap=i[t.matcap]||null),t.alphaMap!==void 0&&(this.alphaMap=i[t.alphaMap]||null),t.bumpMap!==void 0&&(this.bumpMap=i[t.bumpMap]||null),t.bumpScale!==void 0&&(this.bumpScale=t.bumpScale),t.normalMap!==void 0&&(this.normalMap=i[t.normalMap]||null),t.normalMapType!==void 0&&(this.normalMapType=t.normalMapType),t.normalScale!==void 0){let s=t.normalScale;Array.isArray(s)===!1&&(s=[s,s]),this.normalScale=new oe().fromArray(s)}return t.displacementMap!==void 0&&(this.displacementMap=i[t.displacementMap]||null),t.displacementScale!==void 0&&(this.displacementScale=t.displacementScale),t.displacementBias!==void 0&&(this.displacementBias=t.displacementBias),t.roughnessMap!==void 0&&(this.roughnessMap=i[t.roughnessMap]||null),t.metalnessMap!==void 0&&(this.metalnessMap=i[t.metalnessMap]||null),t.emissiveMap!==void 0&&(this.emissiveMap=i[t.emissiveMap]||null),t.emissiveIntensity!==void 0&&(this.emissiveIntensity=t.emissiveIntensity),t.specularMap!==void 0&&(this.specularMap=i[t.specularMap]||null),t.specularIntensityMap!==void 0&&(this.specularIntensityMap=i[t.specularIntensityMap]||null),t.specularColorMap!==void 0&&(this.specularColorMap=i[t.specularColorMap]||null),t.envMap!==void 0&&(this.envMap=i[t.envMap]||null),t.envMapRotation!==void 0&&this.envMapRotation.fromArray(t.envMapRotation),t.envMapIntensity!==void 0&&(this.envMapIntensity=t.envMapIntensity),t.reflectivity!==void 0&&(this.reflectivity=t.reflectivity),t.refractionRatio!==void 0&&(this.refractionRatio=t.refractionRatio),t.lightMap!==void 0&&(this.lightMap=i[t.lightMap]||null),t.lightMapIntensity!==void 0&&(this.lightMapIntensity=t.lightMapIntensity),t.aoMap!==void 0&&(this.aoMap=i[t.aoMap]||null),t.aoMapIntensity!==void 0&&(this.aoMapIntensity=t.aoMapIntensity),t.gradientMap!==void 0&&(this.gradientMap=i[t.gradientMap]||null),t.clearcoatMap!==void 0&&(this.clearcoatMap=i[t.clearcoatMap]||null),t.clearcoatRoughnessMap!==void 0&&(this.clearcoatRoughnessMap=i[t.clearcoatRoughnessMap]||null),t.clearcoatNormalMap!==void 0&&(this.clearcoatNormalMap=i[t.clearcoatNormalMap]||null),t.clearcoatNormalScale!==void 0&&(this.clearcoatNormalScale=new oe().fromArray(t.clearcoatNormalScale)),t.iridescenceMap!==void 0&&(this.iridescenceMap=i[t.iridescenceMap]||null),t.iridescenceThicknessMap!==void 0&&(this.iridescenceThicknessMap=i[t.iridescenceThicknessMap]||null),t.transmissionMap!==void 0&&(this.transmissionMap=i[t.transmissionMap]||null),t.thicknessMap!==void 0&&(this.thicknessMap=i[t.thicknessMap]||null),t.anisotropyMap!==void 0&&(this.anisotropyMap=i[t.anisotropyMap]||null),t.sheenColorMap!==void 0&&(this.sheenColorMap=i[t.sheenColorMap]||null),t.sheenRoughnessMap!==void 0&&(this.sheenRoughnessMap=i[t.sheenRoughnessMap]||null),this}clone(){return new this.constructor().copy(this)}copy(t){this.name=t.name,this.blending=t.blending,this.side=t.side,this.vertexColors=t.vertexColors,this.opacity=t.opacity,this.transparent=t.transparent,this.blendSrc=t.blendSrc,this.blendDst=t.blendDst,this.blendEquation=t.blendEquation,this.blendSrcAlpha=t.blendSrcAlpha,this.blendDstAlpha=t.blendDstAlpha,this.blendEquationAlpha=t.blendEquationAlpha,this.blendColor.copy(t.blendColor),this.blendAlpha=t.blendAlpha,this.depthFunc=t.depthFunc,this.depthTest=t.depthTest,this.depthWrite=t.depthWrite,this.stencilWriteMask=t.stencilWriteMask,this.stencilFunc=t.stencilFunc,this.stencilRef=t.stencilRef,this.stencilFuncMask=t.stencilFuncMask,this.stencilFail=t.stencilFail,this.stencilZFail=t.stencilZFail,this.stencilZPass=t.stencilZPass,this.stencilWrite=t.stencilWrite;const i=t.clippingPlanes;let s=null;if(i!==null){const l=i.length;s=new Array(l);for(let c=0;c!==l;++c)s[c]=i[c].clone()}return this.clippingPlanes=s,this.clipIntersection=t.clipIntersection,this.clipShadows=t.clipShadows,this.shadowSide=t.shadowSide,this.colorWrite=t.colorWrite,this.precision=t.precision,this.polygonOffset=t.polygonOffset,this.polygonOffsetFactor=t.polygonOffsetFactor,this.polygonOffsetUnits=t.polygonOffsetUnits,this.dithering=t.dithering,this.alphaTest=t.alphaTest,this.alphaHash=t.alphaHash,this.alphaToCoverage=t.alphaToCoverage,this.premultipliedAlpha=t.premultipliedAlpha,this.forceSinglePass=t.forceSinglePass,this.allowOverride=t.allowOverride,this.visible=t.visible,this.toneMapped=t.toneMapped,this.userData=JSON.parse(JSON.stringify(t.userData)),this}dispose(){this.dispatchEvent({type:"dispose"})}set needsUpdate(t){t===!0&&this.version++}}class Xp extends ms{constructor(t){super(),this.isSpriteMaterial=!0,this.type="SpriteMaterial",this.color=new ie(16777215),this.map=null,this.alphaMap=null,this.rotation=0,this.sizeAttenuation=!0,this.transparent=!0,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.alphaMap=t.alphaMap,this.rotation=t.rotation,this.sizeAttenuation=t.sizeAttenuation,this.fog=t.fog,this}}let Vr;const sl=new k,kr=new k,Xr=new k,Wr=new oe,rl=new oe,Px=new we,Vc=new k,ol=new k,kc=new k,uv=new oe,hd=new oe,fv=new oe;class zx extends Mn{constructor(t=new Xp){if(super(),this.isSprite=!0,this.type="Sprite",Vr===void 0){Vr=new hn;const i=new Float32Array([-.5,-.5,0,0,0,.5,-.5,0,1,0,.5,.5,0,1,1,-.5,.5,0,0,1]),s=new QE(i,5);Vr.setIndex([0,1,2,0,2,3]),Vr.setAttribute("position",new Au(s,3,0,!1)),Vr.setAttribute("uv",new Au(s,2,3,!1))}this.geometry=Vr,this.material=t,this.center=new oe(.5,.5),this.count=1}raycast(t,i){t.camera===null&&Ae('Sprite: "Raycaster.camera" needs to be set in order to raycast against sprites.'),kr.setFromMatrixScale(this.matrixWorld),Px.copy(t.camera.matrixWorld),this.modelViewMatrix.multiplyMatrices(t.camera.matrixWorldInverse,this.matrixWorld),Xr.setFromMatrixPosition(this.modelViewMatrix),t.camera.isPerspectiveCamera&&this.material.sizeAttenuation===!1&&kr.multiplyScalar(-Xr.z);const s=this.material.rotation;let l,c;s!==0&&(c=Math.cos(s),l=Math.sin(s));const f=this.center;Xc(Vc.set(-.5,-.5,0),Xr,f,kr,l,c),Xc(ol.set(.5,-.5,0),Xr,f,kr,l,c),Xc(kc.set(.5,.5,0),Xr,f,kr,l,c),uv.set(0,0),hd.set(1,0),fv.set(1,1);let p=t.ray.intersectTriangle(Vc,ol,kc,!1,sl);if(p===null&&(Xc(ol.set(-.5,.5,0),Xr,f,kr,l,c),hd.set(0,1),p=t.ray.intersectTriangle(Vc,kc,ol,!1,sl),p===null))return;const m=t.ray.origin.distanceTo(sl);m<t.near||m>t.far||i.push({distance:m,point:sl.clone(),uv:_i.getInterpolation(sl,Vc,ol,kc,uv,hd,fv,new oe),face:null,object:this})}copy(t,i){return super.copy(t,i),t.center!==void 0&&this.center.copy(t.center),this.material=t.material,this}}function Xc(r,t,i,s,l,c){Wr.subVectors(r,i).addScalar(.5).multiply(s),l!==void 0?(rl.x=c*Wr.x-l*Wr.y,rl.y=l*Wr.x+c*Wr.y):rl.copy(Wr),r.copy(t),r.x+=rl.x,r.y+=rl.y,r.applyMatrix4(Px)}const Ea=new k,dd=new k,Wc=new k,ls=new k,pd=new k,qc=new k,md=new k;class Uu{constructor(t=new k,i=new k(0,0,-1)){this.origin=t,this.direction=i}set(t,i){return this.origin.copy(t),this.direction.copy(i),this}copy(t){return this.origin.copy(t.origin),this.direction.copy(t.direction),this}at(t,i){return i.copy(this.origin).addScaledVector(this.direction,t)}lookAt(t){return this.direction.copy(t).sub(this.origin).normalize(),this}recast(t){return this.origin.copy(this.at(t,Ea)),this}closestPointToPoint(t,i){i.subVectors(t,this.origin);const s=i.dot(this.direction);return s<0?i.copy(this.origin):i.copy(this.origin).addScaledVector(this.direction,s)}distanceToPoint(t){return Math.sqrt(this.distanceSqToPoint(t))}distanceSqToPoint(t){const i=Ea.subVectors(t,this.origin).dot(this.direction);return i<0?this.origin.distanceToSquared(t):(Ea.copy(this.origin).addScaledVector(this.direction,i),Ea.distanceToSquared(t))}distanceSqToSegment(t,i,s,l){dd.copy(t).add(i).multiplyScalar(.5),Wc.copy(i).sub(t).normalize(),ls.copy(this.origin).sub(dd);const c=t.distanceTo(i)*.5,f=-this.direction.dot(Wc),p=ls.dot(this.direction),m=-ls.dot(Wc),d=ls.lengthSq(),x=Math.abs(1-f*f);let v,g,S,b;if(x>0)if(v=f*m-p,g=f*p-m,b=c*x,v>=0)if(g>=-b)if(g<=b){const w=1/x;v*=w,g*=w,S=v*(v+f*g+2*p)+g*(f*v+g+2*m)+d}else g=c,v=Math.max(0,-(f*g+p)),S=-v*v+g*(g+2*m)+d;else g=-c,v=Math.max(0,-(f*g+p)),S=-v*v+g*(g+2*m)+d;else g<=-b?(v=Math.max(0,-(-f*c+p)),g=v>0?-c:Math.min(Math.max(-c,-m),c),S=-v*v+g*(g+2*m)+d):g<=b?(v=0,g=Math.min(Math.max(-c,-m),c),S=g*(g+2*m)+d):(v=Math.max(0,-(f*c+p)),g=v>0?c:Math.min(Math.max(-c,-m),c),S=-v*v+g*(g+2*m)+d);else g=f>0?-c:c,v=Math.max(0,-(f*g+p)),S=-v*v+g*(g+2*m)+d;return s&&s.copy(this.origin).addScaledVector(this.direction,v),l&&l.copy(dd).addScaledVector(Wc,g),S}intersectSphere(t,i){Ea.subVectors(t.center,this.origin);const s=Ea.dot(this.direction),l=Ea.dot(Ea)-s*s,c=t.radius*t.radius;if(l>c)return null;const f=Math.sqrt(c-l),p=s-f,m=s+f;return m<0?null:p<0?this.at(m,i):this.at(p,i)}intersectsSphere(t){return t.radius<0?!1:this.distanceSqToPoint(t.center)<=t.radius*t.radius}distanceToPlane(t){const i=t.normal.dot(this.direction);if(i===0)return t.distanceToPoint(this.origin)===0?0:null;const s=-(this.origin.dot(t.normal)+t.constant)/i;return s>=0?s:null}intersectPlane(t,i){const s=this.distanceToPlane(t);return s===null?null:this.at(s,i)}intersectsPlane(t){const i=t.distanceToPoint(this.origin);return i===0||t.normal.dot(this.direction)*i<0}intersectBox(t,i){let s,l,c,f,p,m;const d=1/this.direction.x,x=1/this.direction.y,v=1/this.direction.z,g=this.origin;return d>=0?(s=(t.min.x-g.x)*d,l=(t.max.x-g.x)*d):(s=(t.max.x-g.x)*d,l=(t.min.x-g.x)*d),x>=0?(c=(t.min.y-g.y)*x,f=(t.max.y-g.y)*x):(c=(t.max.y-g.y)*x,f=(t.min.y-g.y)*x),s>f||c>l||((c>s||isNaN(s))&&(s=c),(f<l||isNaN(l))&&(l=f),v>=0?(p=(t.min.z-g.z)*v,m=(t.max.z-g.z)*v):(p=(t.max.z-g.z)*v,m=(t.min.z-g.z)*v),s>m||p>l)||((p>s||s!==s)&&(s=p),(m<l||l!==l)&&(l=m),l<0)?null:this.at(s>=0?s:l,i)}intersectsBox(t){return this.intersectBox(t,Ea)!==null}intersectTriangle(t,i,s,l,c){pd.subVectors(i,t),qc.subVectors(s,t),md.crossVectors(pd,qc);let f=this.direction.dot(md),p;if(f>0){if(l)return null;p=1}else if(f<0)p=-1,f=-f;else return null;ls.subVectors(this.origin,t);const m=p*this.direction.dot(qc.crossVectors(ls,qc));if(m<0)return null;const d=p*this.direction.dot(pd.cross(ls));if(d<0||m+d>f)return null;const x=-p*ls.dot(md);return x<0?null:this.at(x/f,c)}applyMatrix4(t){return this.origin.applyMatrix4(t),this.direction.transformDirection(t),this}equals(t){return t.origin.equals(this.origin)&&t.direction.equals(this.direction)}clone(){return new this.constructor().copy(this)}}class io extends ms{constructor(t){super(),this.isMeshBasicMaterial=!0,this.type="MeshBasicMaterial",this.color=new ie(16777215),this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.specularMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new ps,this.combine=gx,this.reflectivity=1,this.refractionRatio=.98,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.lightMap=t.lightMap,this.lightMapIntensity=t.lightMapIntensity,this.aoMap=t.aoMap,this.aoMapIntensity=t.aoMapIntensity,this.specularMap=t.specularMap,this.alphaMap=t.alphaMap,this.envMap=t.envMap,this.envMapRotation.copy(t.envMapRotation),this.combine=t.combine,this.reflectivity=t.reflectivity,this.refractionRatio=t.refractionRatio,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.wireframeLinecap=t.wireframeLinecap,this.wireframeLinejoin=t.wireframeLinejoin,this.fog=t.fog,this}}const hv=new we,Fs=new Uu,Yc=new Js,dv=new k,jc=new k,Zc=new k,Kc=new k,gd=new k,Qc=new k,pv=new k,Jc=new k;class ln extends Mn{constructor(t=new hn,i=new io){super(),this.isMesh=!0,this.type="Mesh",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.count=1,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),t.morphTargetInfluences!==void 0&&(this.morphTargetInfluences=t.morphTargetInfluences.slice()),t.morphTargetDictionary!==void 0&&(this.morphTargetDictionary=Object.assign({},t.morphTargetDictionary)),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}updateMorphTargets(){const i=this.geometry.morphAttributes,s=Object.keys(i);if(s.length>0){const l=i[s[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const p=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[p]=c}}}}getVertexPosition(t,i){const s=this.geometry,l=s.attributes.position,c=s.morphAttributes.position,f=s.morphTargetsRelative;i.fromBufferAttribute(l,t);const p=this.morphTargetInfluences;if(c&&p){Qc.set(0,0,0);for(let m=0,d=c.length;m<d;m++){const x=p[m],v=c[m];x!==0&&(gd.fromBufferAttribute(v,t),f?Qc.addScaledVector(gd,x):Qc.addScaledVector(gd.sub(i),x))}i.add(Qc)}return i}raycast(t,i){const s=this.geometry,l=this.material,c=this.matrixWorld;l!==void 0&&(s.boundingSphere===null&&s.computeBoundingSphere(),Yc.copy(s.boundingSphere),Yc.applyMatrix4(c),Fs.copy(t.ray).recast(t.near),!(Yc.containsPoint(Fs.origin)===!1&&(Fs.intersectSphere(Yc,dv)===null||Fs.origin.distanceToSquared(dv)>(t.far-t.near)**2))&&(hv.copy(c).invert(),Fs.copy(t.ray).applyMatrix4(hv),!(s.boundingBox!==null&&Fs.intersectsBox(s.boundingBox)===!1)&&this._computeIntersections(t,i,Fs)))}_computeIntersections(t,i,s){let l;const c=this.geometry,f=this.material,p=c.index,m=c.attributes.position,d=c.attributes.uv,x=c.attributes.uv1,v=c.attributes.normal,g=c.groups,S=c.drawRange;if(p!==null)if(Array.isArray(f))for(let b=0,w=g.length;b<w;b++){const E=g[b],_=f[E.materialIndex],z=Math.max(E.start,S.start),F=Math.min(p.count,Math.min(E.start+E.count,S.start+S.count));for(let C=z,B=F;C<B;C+=3){const O=p.getX(C),U=p.getX(C+1),T=p.getX(C+2);l=$c(this,_,t,s,d,x,v,O,U,T),l&&(l.faceIndex=Math.floor(C/3),l.face.materialIndex=E.materialIndex,i.push(l))}}else{const b=Math.max(0,S.start),w=Math.min(p.count,S.start+S.count);for(let E=b,_=w;E<_;E+=3){const z=p.getX(E),F=p.getX(E+1),C=p.getX(E+2);l=$c(this,f,t,s,d,x,v,z,F,C),l&&(l.faceIndex=Math.floor(E/3),i.push(l))}}else if(m!==void 0)if(Array.isArray(f))for(let b=0,w=g.length;b<w;b++){const E=g[b],_=f[E.materialIndex],z=Math.max(E.start,S.start),F=Math.min(m.count,Math.min(E.start+E.count,S.start+S.count));for(let C=z,B=F;C<B;C+=3){const O=C,U=C+1,T=C+2;l=$c(this,_,t,s,d,x,v,O,U,T),l&&(l.faceIndex=Math.floor(C/3),l.face.materialIndex=E.materialIndex,i.push(l))}}else{const b=Math.max(0,S.start),w=Math.min(m.count,S.start+S.count);for(let E=b,_=w;E<_;E+=3){const z=E,F=E+1,C=E+2;l=$c(this,f,t,s,d,x,v,z,F,C),l&&(l.faceIndex=Math.floor(E/3),i.push(l))}}}}function $E(r,t,i,s,l,c,f,p){let m;if(t.side===ti?m=s.intersectTriangle(f,c,l,!0,p):m=s.intersectTriangle(l,c,f,t.side===ds,p),m===null)return null;Jc.copy(p),Jc.applyMatrix4(r.matrixWorld);const d=i.ray.origin.distanceTo(Jc);return d<i.near||d>i.far?null:{distance:d,point:Jc.clone(),object:r}}function $c(r,t,i,s,l,c,f,p,m,d){r.getVertexPosition(p,jc),r.getVertexPosition(m,Zc),r.getVertexPosition(d,Kc);const x=$E(r,t,i,s,jc,Zc,Kc,pv);if(x){const v=new k;_i.getBarycoord(pv,jc,Zc,Kc,v),l&&(x.uv=_i.getInterpolatedAttribute(l,p,m,d,v,new oe)),c&&(x.uv1=_i.getInterpolatedAttribute(c,p,m,d,v,new oe)),f&&(x.normal=_i.getInterpolatedAttribute(f,p,m,d,v,new k),x.normal.dot(s.direction)>0&&x.normal.multiplyScalar(-1));const g={a:p,b:m,c:d,normal:new k,materialIndex:0};_i.getNormal(jc,Zc,Kc,g.normal),x.face=g,x.barycoord=v}return x}class Bx extends Vn{constructor(t=null,i=1,s=1,l,c,f,p,m,d=zn,x=zn,v,g){super(null,f,p,m,d,x,l,c,v,g),this.isDataTexture=!0,this.image={data:t,width:i,height:s},this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}class mv extends vi{constructor(t,i,s,l=1){super(t,i,s),this.isInstancedBufferAttribute=!0,this.meshPerAttribute=l}copy(t){return super.copy(t),this.meshPerAttribute=t.meshPerAttribute,this}toJSON(){const t=super.toJSON();return t.meshPerAttribute=this.meshPerAttribute,t.isInstancedBufferAttribute=!0,t}}const qr=new we,gv=new we,tu=[],_v=new Qs,tb=new we,ll=new ln,cl=new Js;class ul extends ln{constructor(t,i,s){super(t,i),this.isInstancedMesh=!0,this.instanceMatrix=new mv(new Float32Array(s*16),16),this.instanceColor=null,this.morphTexture=null,this.count=s,this.boundingBox=null,this.boundingSphere=null;for(let l=0;l<s;l++)this.setMatrixAt(l,tb)}computeBoundingBox(){const t=this.geometry,i=this.count;this.boundingBox===null&&(this.boundingBox=new Qs),t.boundingBox===null&&t.computeBoundingBox(),this.boundingBox.makeEmpty();for(let s=0;s<i;s++)this.getMatrixAt(s,qr),_v.copy(t.boundingBox).applyMatrix4(qr),this.boundingBox.union(_v)}computeBoundingSphere(){const t=this.geometry,i=this.count;this.boundingSphere===null&&(this.boundingSphere=new Js),t.boundingSphere===null&&t.computeBoundingSphere(),this.boundingSphere.makeEmpty();for(let s=0;s<i;s++)this.getMatrixAt(s,qr),cl.copy(t.boundingSphere).applyMatrix4(qr),this.boundingSphere.union(cl)}copy(t,i){return super.copy(t,i),this.instanceMatrix.copy(t.instanceMatrix),t.morphTexture!==null&&(this.morphTexture=t.morphTexture.clone()),t.instanceColor!==null&&(this.instanceColor=t.instanceColor.clone()),this.count=t.count,t.boundingBox!==null&&(this.boundingBox=t.boundingBox.clone()),t.boundingSphere!==null&&(this.boundingSphere=t.boundingSphere.clone()),this}getColorAt(t,i){return this.instanceColor===null?i.setRGB(1,1,1):i.fromArray(this.instanceColor.array,t*3)}getMatrixAt(t,i){return i.fromArray(this.instanceMatrix.array,t*16)}getMorphAt(t,i){const s=i.morphTargetInfluences,l=this.morphTexture.source.data.data,c=s.length+1,f=t*c+1;for(let p=0;p<s.length;p++)s[p]=l[f+p]}raycast(t,i){const s=this.matrixWorld,l=this.count;if(ll.geometry=this.geometry,ll.material=this.material,ll.material!==void 0&&(this.boundingSphere===null&&this.computeBoundingSphere(),cl.copy(this.boundingSphere),cl.applyMatrix4(s),t.ray.intersectsSphere(cl)!==!1))for(let c=0;c<l;c++){this.getMatrixAt(c,qr),gv.multiplyMatrices(s,qr),ll.matrixWorld=gv,ll.raycast(t,tu);for(let f=0,p=tu.length;f<p;f++){const m=tu[f];m.instanceId=c,m.object=this,i.push(m)}tu.length=0}}setColorAt(t,i){return this.instanceColor===null&&(this.instanceColor=new mv(new Float32Array(this.instanceMatrix.count*3).fill(1),3)),i.toArray(this.instanceColor.array,t*3),this}setMatrixAt(t,i){return i.toArray(this.instanceMatrix.array,t*16),this}setMorphAt(t,i){const s=i.morphTargetInfluences,l=s.length+1;this.morphTexture===null&&(this.morphTexture=new Bx(new Float32Array(l*this.count),l,this.count,Op,Ii));const c=this.morphTexture.source.data.data;let f=0;for(let d=0;d<s.length;d++)f+=s[d];const p=this.geometry.morphTargetsRelative?1:1-f,m=l*t;return c[m]=p,c.set(s,m+1),this}updateMorphTargets(){}dispose(){this.dispatchEvent({type:"dispose"}),this.morphTexture!==null&&(this.morphTexture.dispose(),this.morphTexture=null)}}const _d=new k,eb=new k,nb=new ue;class Gs{constructor(t=new k(1,0,0),i=0){this.isPlane=!0,this.normal=t,this.constant=i}set(t,i){return this.normal.copy(t),this.constant=i,this}setComponents(t,i,s,l){return this.normal.set(t,i,s),this.constant=l,this}setFromNormalAndCoplanarPoint(t,i){return this.normal.copy(t),this.constant=-i.dot(this.normal),this}setFromCoplanarPoints(t,i,s){const l=_d.subVectors(s,i).cross(eb.subVectors(t,i)).normalize();return this.setFromNormalAndCoplanarPoint(l,t),this}copy(t){return this.normal.copy(t.normal),this.constant=t.constant,this}normalize(){const t=1/this.normal.length();return this.normal.multiplyScalar(t),this.constant*=t,this}negate(){return this.constant*=-1,this.normal.negate(),this}distanceToPoint(t){return this.normal.dot(t)+this.constant}distanceToSphere(t){return this.distanceToPoint(t.center)-t.radius}projectPoint(t,i){return i.copy(t).addScaledVector(this.normal,-this.distanceToPoint(t))}intersectLine(t,i,s=!0){const l=t.delta(_d),c=this.normal.dot(l);if(c===0)return this.distanceToPoint(t.start)===0?i.copy(t.start):null;const f=-(t.start.dot(this.normal)+this.constant)/c;return s===!0&&(f<0||f>1)?null:i.copy(t.start).addScaledVector(l,f)}intersectsLine(t){const i=this.distanceToPoint(t.start),s=this.distanceToPoint(t.end);return i<0&&s>0||s<0&&i>0}intersectsBox(t){return t.intersectsPlane(this)}intersectsSphere(t){return t.intersectsPlane(this)}coplanarPoint(t){return t.copy(this.normal).multiplyScalar(-this.constant)}applyMatrix4(t,i){const s=i||nb.getNormalMatrix(t),l=this.coplanarPoint(_d).applyMatrix4(t),c=this.normal.applyMatrix3(s).normalize();return this.constant=-l.dot(c),this}translate(t){return this.constant-=t.dot(this.normal),this}equals(t){return t.normal.equals(this.normal)&&t.constant===this.constant}clone(){return new this.constructor().copy(this)}}const Hs=new Js,ib=new oe(.5,.5),eu=new k;class Wp{constructor(t=new Gs,i=new Gs,s=new Gs,l=new Gs,c=new Gs,f=new Gs){this.planes=[t,i,s,l,c,f]}set(t,i,s,l,c,f){const p=this.planes;return p[0].copy(t),p[1].copy(i),p[2].copy(s),p[3].copy(l),p[4].copy(c),p[5].copy(f),this}copy(t){const i=this.planes;for(let s=0;s<6;s++)i[s].copy(t.planes[s]);return this}setFromProjectionMatrix(t,i=Zi,s=!1){const l=this.planes,c=t.elements,f=c[0],p=c[1],m=c[2],d=c[3],x=c[4],v=c[5],g=c[6],S=c[7],b=c[8],w=c[9],E=c[10],_=c[11],z=c[12],F=c[13],C=c[14],B=c[15];if(l[0].setComponents(d-f,S-x,_-b,B-z).normalize(),l[1].setComponents(d+f,S+x,_+b,B+z).normalize(),l[2].setComponents(d+p,S+v,_+w,B+F).normalize(),l[3].setComponents(d-p,S-v,_-w,B-F).normalize(),s)l[4].setComponents(m,g,E,C).normalize(),l[5].setComponents(d-m,S-g,_-E,B-C).normalize();else if(l[4].setComponents(d-m,S-g,_-E,B-C).normalize(),i===Zi)l[5].setComponents(d+m,S+g,_+E,B+C).normalize();else if(i===Sl)l[5].setComponents(m,g,E,C).normalize();else throw new Error("THREE.Frustum.setFromProjectionMatrix(): Invalid coordinate system: "+i);return this}intersectsObject(t){if(t.boundingSphere!==void 0)t.boundingSphere===null&&t.computeBoundingSphere(),Hs.copy(t.boundingSphere).applyMatrix4(t.matrixWorld);else{const i=t.geometry;i.boundingSphere===null&&i.computeBoundingSphere(),Hs.copy(i.boundingSphere).applyMatrix4(t.matrixWorld)}return this.intersectsSphere(Hs)}intersectsSprite(t){Hs.center.set(0,0,0);const i=ib.distanceTo(t.center);return Hs.radius=.7071067811865476+i,Hs.applyMatrix4(t.matrixWorld),this.intersectsSphere(Hs)}intersectsSphere(t){const i=this.planes,s=t.center,l=-t.radius;for(let c=0;c<6;c++)if(i[c].distanceToPoint(s)<l)return!1;return!0}intersectsBox(t){const i=this.planes;for(let s=0;s<6;s++){const l=i[s];if(eu.x=l.normal.x>0?t.max.x:t.min.x,eu.y=l.normal.y>0?t.max.y:t.min.y,eu.z=l.normal.z>0?t.max.z:t.min.z,l.distanceToPoint(eu)<0)return!1}return!0}containsPoint(t){const i=this.planes;for(let s=0;s<6;s++)if(i[s].distanceToPoint(t)<0)return!1;return!0}clone(){return new this.constructor().copy(this)}}class hs extends ms{constructor(t){super(),this.isLineBasicMaterial=!0,this.type="LineBasicMaterial",this.color=new ie(16777215),this.map=null,this.linewidth=1,this.linecap="round",this.linejoin="round",this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.linewidth=t.linewidth,this.linecap=t.linecap,this.linejoin=t.linejoin,this.fog=t.fog,this}}const Ru=new k,wu=new k,vv=new we,fl=new Uu,nu=new Js,vd=new k,xv=new k;class Nu extends Mn{constructor(t=new hn,i=new hs){super(),this.isLine=!0,this.type="Line",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}computeLineDistances(){const t=this.geometry;if(t.index===null){const i=t.attributes.position,s=[0];for(let l=1,c=i.count;l<c;l++)Ru.fromBufferAttribute(i,l-1),wu.fromBufferAttribute(i,l),s[l]=s[l-1],s[l]+=Ru.distanceTo(wu);t.setAttribute("lineDistance",new En(s,1))}else se("Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}raycast(t,i){const s=this.geometry,l=this.matrixWorld,c=t.params.Line.threshold,f=s.drawRange;if(s.boundingSphere===null&&s.computeBoundingSphere(),nu.copy(s.boundingSphere),nu.applyMatrix4(l),nu.radius+=c,t.ray.intersectsSphere(nu)===!1)return;vv.copy(l).invert(),fl.copy(t.ray).applyMatrix4(vv);const p=c/((this.scale.x+this.scale.y+this.scale.z)/3),m=p*p,d=this.isLineSegments?2:1,x=s.index,g=s.attributes.position;if(x!==null){const S=Math.max(0,f.start),b=Math.min(x.count,f.start+f.count);for(let w=S,E=b-1;w<E;w+=d){const _=x.getX(w),z=x.getX(w+1),F=iu(this,t,fl,m,_,z,w);F&&i.push(F)}if(this.isLineLoop){const w=x.getX(b-1),E=x.getX(S),_=iu(this,t,fl,m,w,E,b-1);_&&i.push(_)}}else{const S=Math.max(0,f.start),b=Math.min(g.count,f.start+f.count);for(let w=S,E=b-1;w<E;w+=d){const _=iu(this,t,fl,m,w,w+1,w);_&&i.push(_)}if(this.isLineLoop){const w=iu(this,t,fl,m,b-1,S,b-1);w&&i.push(w)}}}updateMorphTargets(){const i=this.geometry.morphAttributes,s=Object.keys(i);if(s.length>0){const l=i[s[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const p=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[p]=c}}}}}function iu(r,t,i,s,l,c,f){const p=r.geometry.attributes.position;if(Ru.fromBufferAttribute(p,l),wu.fromBufferAttribute(p,c),i.distanceSqToSegment(Ru,wu,vd,xv)>s)return;vd.applyMatrix4(r.matrixWorld);const d=t.ray.origin.distanceTo(vd);if(!(d<t.near||d>t.far))return{distance:d,point:xv.clone().applyMatrix4(r.matrixWorld),index:f,face:null,faceIndex:null,barycoord:null,object:r}}const yv=new k,Sv=new k;class Ix extends Nu{constructor(t,i){super(t,i),this.isLineSegments=!0,this.type="LineSegments"}computeLineDistances(){const t=this.geometry;if(t.index===null){const i=t.attributes.position,s=[];for(let l=0,c=i.count;l<c;l+=2)yv.fromBufferAttribute(i,l),Sv.fromBufferAttribute(i,l+1),s[l]=l===0?0:s[l-1],s[l+1]=s[l]+yv.distanceTo(Sv);t.setAttribute("lineDistance",new En(s,1))}else se("LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}}class Sp extends Nu{constructor(t,i){super(t,i),this.isLineLoop=!0,this.type="LineLoop"}}class Fx extends ms{constructor(t){super(),this.isPointsMaterial=!0,this.type="PointsMaterial",this.color=new ie(16777215),this.map=null,this.alphaMap=null,this.size=1,this.sizeAttenuation=!0,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.alphaMap=t.alphaMap,this.size=t.size,this.sizeAttenuation=t.sizeAttenuation,this.fog=t.fog,this}}const Mv=new we,Mp=new Uu,au=new Js,su=new k;class ab extends Mn{constructor(t=new hn,i=new Fx){super(),this.isPoints=!0,this.type="Points",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}raycast(t,i){const s=this.geometry,l=this.matrixWorld,c=t.params.Points.threshold,f=s.drawRange;if(s.boundingSphere===null&&s.computeBoundingSphere(),au.copy(s.boundingSphere),au.applyMatrix4(l),au.radius+=c,t.ray.intersectsSphere(au)===!1)return;Mv.copy(l).invert(),Mp.copy(t.ray).applyMatrix4(Mv);const p=c/((this.scale.x+this.scale.y+this.scale.z)/3),m=p*p,d=s.index,v=s.attributes.position;if(d!==null){const g=Math.max(0,f.start),S=Math.min(d.count,f.start+f.count);for(let b=g,w=S;b<w;b++){const E=d.getX(b);su.fromBufferAttribute(v,E),Ev(su,E,m,l,t,i,this)}}else{const g=Math.max(0,f.start),S=Math.min(v.count,f.start+f.count);for(let b=g,w=S;b<w;b++)su.fromBufferAttribute(v,b),Ev(su,b,m,l,t,i,this)}}updateMorphTargets(){const i=this.geometry.morphAttributes,s=Object.keys(i);if(s.length>0){const l=i[s[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const p=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[p]=c}}}}}function Ev(r,t,i,s,l,c,f){const p=Mp.distanceSqToPoint(r);if(p<i){const m=new k;Mp.closestPointToPoint(r,m),m.applyMatrix4(s);const d=l.ray.origin.distanceTo(m);if(d<l.near||d>l.far)return;c.push({distance:d,distanceToRay:Math.sqrt(p),point:m,index:t,face:null,faceIndex:null,barycoord:null,object:f})}}class Hx extends Vn{constructor(t=[],i=js,s,l,c,f,p,m,d,x){super(t,i,s,l,c,f,p,m,d,x),this.isCubeTexture=!0,this.flipY=!1}get images(){return this.image}set images(t){this.image=t}}class Gx extends Vn{constructor(t,i,s,l,c,f,p,m,d){super(t,i,s,l,c,f,p,m,d),this.isCanvasTexture=!0,this.needsUpdate=!0}}class ao extends Vn{constructor(t,i,s=Ji,l,c,f,p=zn,m=zn,d,x=Da,v=1){if(x!==Da&&x!==Ws)throw new Error("THREE.DepthTexture: format must be either THREE.DepthFormat or THREE.DepthStencilFormat");const g={width:t,height:i,depth:v};super(g,l,c,f,p,m,x,s,d),this.isDepthTexture=!0,this.flipY=!1,this.generateMipmaps=!1,this.compareFunction=null}copy(t){return super.copy(t),this.source=new Gp(Object.assign({},t.image)),this.compareFunction=t.compareFunction,this}toJSON(t){const i=super.toJSON(t);return this.compareFunction!==null&&(i.compareFunction=this.compareFunction),i}}class sb extends ao{constructor(t,i=Ji,s=js,l,c,f=zn,p=zn,m,d=Da){const x={width:t,height:t,depth:1},v=[x,x,x,x,x,x];super(t,t,i,s,l,c,f,p,m,d),this.image=v,this.isCubeDepthTexture=!0,this.isCubeTexture=!0}get images(){return this.image}set images(t){this.image=t}}class Vx extends Vn{constructor(t=null){super(),this.sourceTexture=t,this.isExternalTexture=!0}copy(t){return super.copy(t),this.sourceTexture=t.sourceTexture,this}}class Dn extends hn{constructor(t=1,i=1,s=1,l=1,c=1,f=1){super(),this.type="BoxGeometry",this.parameters={width:t,height:i,depth:s,widthSegments:l,heightSegments:c,depthSegments:f};const p=this;l=Math.floor(l),c=Math.floor(c),f=Math.floor(f);const m=[],d=[],x=[],v=[];let g=0,S=0;b("z","y","x",-1,-1,s,i,t,f,c,0),b("z","y","x",1,-1,s,i,-t,f,c,1),b("x","z","y",1,1,t,s,i,l,f,2),b("x","z","y",1,-1,t,s,-i,l,f,3),b("x","y","z",1,-1,t,i,s,l,c,4),b("x","y","z",-1,-1,t,i,-s,l,c,5),this.setIndex(m),this.setAttribute("position",new En(d,3)),this.setAttribute("normal",new En(x,3)),this.setAttribute("uv",new En(v,2));function b(w,E,_,z,F,C,B,O,U,T,P){const $=C/U,Y=B/T,J=C/2,dt=B/2,gt=O/2,et=U+1,I=T+1;let H=0,q=0;const ht=new k;for(let Et=0;Et<I;Et++){const L=Et*Y-dt;for(let W=0;W<et;W++){const yt=W*$-J;ht[w]=yt*z,ht[E]=L*F,ht[_]=gt,d.push(ht.x,ht.y,ht.z),ht[w]=0,ht[E]=0,ht[_]=O>0?1:-1,x.push(ht.x,ht.y,ht.z),v.push(W/U),v.push(1-Et/T),H+=1}}for(let Et=0;Et<T;Et++)for(let L=0;L<U;L++){const W=g+L+et*Et,yt=g+L+et*(Et+1),At=g+(L+1)+et*(Et+1),wt=g+(L+1)+et*Et;m.push(W,yt,wt),m.push(yt,At,wt),q+=6}p.addGroup(S,q,P),S+=q,g+=H}}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new Dn(t.width,t.height,t.depth,t.widthSegments,t.heightSegments,t.depthSegments)}}class qp extends hn{constructor(t=1,i=32,s=0,l=Math.PI*2){super(),this.type="CircleGeometry",this.parameters={radius:t,segments:i,thetaStart:s,thetaLength:l},i=Math.max(3,i);const c=[],f=[],p=[],m=[],d=new k,x=new oe;f.push(0,0,0),p.push(0,0,1),m.push(.5,.5);for(let v=0,g=3;v<=i;v++,g+=3){const S=s+v/i*l;d.x=t*Math.cos(S),d.y=t*Math.sin(S),f.push(d.x,d.y,d.z),p.push(0,0,1),x.x=(f[g]/t+1)/2,x.y=(f[g+1]/t+1)/2,m.push(x.x,x.y)}for(let v=1;v<=i;v++)c.push(v,v+1,0);this.setIndex(c),this.setAttribute("position",new En(f,3)),this.setAttribute("normal",new En(p,3)),this.setAttribute("uv",new En(m,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new qp(t.radius,t.segments,t.thetaStart,t.thetaLength)}}const ru=new k,ou=new k,xd=new k,lu=new _i;class rb extends hn{constructor(t=null,i=1){if(super(),this.type="EdgesGeometry",this.parameters={geometry:t,thresholdAngle:i},t!==null){const l=Math.pow(10,4),c=Math.cos($r*i),f=t.getIndex(),p=t.getAttribute("position"),m=f?f.count:p.count,d=[0,0,0],x=["a","b","c"],v=new Array(3),g={},S=[];for(let b=0;b<m;b+=3){f?(d[0]=f.getX(b),d[1]=f.getX(b+1),d[2]=f.getX(b+2)):(d[0]=b,d[1]=b+1,d[2]=b+2);const{a:w,b:E,c:_}=lu;if(w.fromBufferAttribute(p,d[0]),E.fromBufferAttribute(p,d[1]),_.fromBufferAttribute(p,d[2]),lu.getNormal(xd),v[0]=`${Math.round(w.x*l)},${Math.round(w.y*l)},${Math.round(w.z*l)}`,v[1]=`${Math.round(E.x*l)},${Math.round(E.y*l)},${Math.round(E.z*l)}`,v[2]=`${Math.round(_.x*l)},${Math.round(_.y*l)},${Math.round(_.z*l)}`,!(v[0]===v[1]||v[1]===v[2]||v[2]===v[0]))for(let z=0;z<3;z++){const F=(z+1)%3,C=v[z],B=v[F],O=lu[x[z]],U=lu[x[F]],T=`${C}_${B}`,P=`${B}_${C}`;P in g&&g[P]?(xd.dot(g[P].normal)<=c&&(S.push(O.x,O.y,O.z),S.push(U.x,U.y,U.z)),g[P]=null):T in g||(g[T]={index0:d[z],index1:d[F],normal:xd.clone()})}}for(const b in g)if(g[b]){const{index0:w,index1:E}=g[b];ru.fromBufferAttribute(p,w),ou.fromBufferAttribute(p,E),S.push(ru.x,ru.y,ru.z),S.push(ou.x,ou.y,ou.z)}this.setAttribute("position",new En(S,3))}}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}}class ob{constructor(){this.type="Curve",this.arcLengthDivisions=200,this.needsUpdate=!1,this.cacheArcLengths=null}getPoint(){se("Curve: .getPoint() not implemented.")}getPointAt(t,i){const s=this.getUtoTmapping(t);return this.getPoint(s,i)}getPoints(t=5){const i=[];for(let s=0;s<=t;s++)i.push(this.getPoint(s/t));return i}getSpacedPoints(t=5){const i=[];for(let s=0;s<=t;s++)i.push(this.getPointAt(s/t));return i}getLength(){const t=this.getLengths();return t[t.length-1]}getLengths(t=this.arcLengthDivisions){if(this.cacheArcLengths&&this.cacheArcLengths.length===t+1&&!this.needsUpdate)return this.cacheArcLengths;this.needsUpdate=!1;const i=[];let s,l=this.getPoint(0),c=0;i.push(0);for(let f=1;f<=t;f++)s=this.getPoint(f/t),c+=s.distanceTo(l),i.push(c),l=s;return this.cacheArcLengths=i,i}updateArcLengths(){this.needsUpdate=!0,this.getLengths()}getUtoTmapping(t,i=null){const s=this.getLengths();let l=0;const c=s.length;let f;i?f=i:f=t*s[c-1];let p=0,m=c-1,d;for(;p<=m;)if(l=Math.floor(p+(m-p)/2),d=s[l]-f,d<0)p=l+1;else if(d>0)m=l-1;else{m=l;break}if(l=m,s[l]===f)return l/(c-1);const x=s[l],g=s[l+1]-x,S=(f-x)/g;return(l+S)/(c-1)}getTangent(t,i){let l=t-1e-4,c=t+1e-4;l<0&&(l=0),c>1&&(c=1);const f=this.getPoint(l),p=this.getPoint(c),m=i||(f.isVector2?new oe:new k);return m.copy(p).sub(f).normalize(),m}getTangentAt(t,i){const s=this.getUtoTmapping(t);return this.getTangent(s,i)}computeFrenetFrames(t,i=!1){const s=new k,l=[],c=[],f=[],p=new k,m=new we;for(let S=0;S<=t;S++){const b=S/t;l[S]=this.getTangentAt(b,new k)}c[0]=new k,f[0]=new k;let d=Number.MAX_VALUE;const x=Math.abs(l[0].x),v=Math.abs(l[0].y),g=Math.abs(l[0].z);x<=d&&(d=x,s.set(1,0,0)),v<=d&&(d=v,s.set(0,1,0)),g<=d&&s.set(0,0,1),p.crossVectors(l[0],s).normalize(),c[0].crossVectors(l[0],p),f[0].crossVectors(l[0],c[0]);for(let S=1;S<=t;S++){if(c[S]=c[S-1].clone(),f[S]=f[S-1].clone(),p.crossVectors(l[S-1],l[S]),p.length()>Number.EPSILON){p.normalize();const b=Math.acos(ge(l[S-1].dot(l[S]),-1,1));c[S].applyMatrix4(m.makeRotationAxis(p,b))}f[S].crossVectors(l[S],c[S])}if(i===!0){let S=Math.acos(ge(c[0].dot(c[t]),-1,1));S/=t,l[0].dot(p.crossVectors(c[0],c[t]))>0&&(S=-S);for(let b=1;b<=t;b++)c[b].applyMatrix4(m.makeRotationAxis(l[b],S*b)),f[b].crossVectors(l[b],c[b])}return{tangents:l,normals:c,binormals:f}}clone(){return new this.constructor().copy(this)}copy(t){return this.arcLengthDivisions=t.arcLengthDivisions,this}toJSON(){const t={metadata:{version:4.7,type:"Curve",generator:"Curve.toJSON"}};return t.arcLengthDivisions=this.arcLengthDivisions,t.type=this.type,t}fromJSON(t){return this.arcLengthDivisions=t.arcLengthDivisions,this}}function Yp(){let r=0,t=0,i=0,s=0;function l(c,f,p,m){r=c,t=p,i=-3*c+3*f-2*p-m,s=2*c-2*f+p+m}return{initCatmullRom:function(c,f,p,m,d){l(f,p,d*(p-c),d*(m-f))},initNonuniformCatmullRom:function(c,f,p,m,d,x,v){let g=(f-c)/d-(p-c)/(d+x)+(p-f)/x,S=(p-f)/x-(m-f)/(x+v)+(m-p)/v;g*=x,S*=x,l(f,p,g,S)},calc:function(c){const f=c*c,p=f*c;return r+t*c+i*f+s*p}}}const bv=new k,Tv=new k,yd=new Yp,Sd=new Yp,Md=new Yp;class lb extends ob{constructor(t=[],i=!1,s="centripetal",l=.5){super(),this.isCatmullRomCurve3=!0,this.type="CatmullRomCurve3",this.points=t,this.closed=i,this.curveType=s,this.tension=l}getPoint(t,i=new k){const s=i,l=this.points,c=l.length,f=(c-(this.closed?0:1))*t;let p=Math.floor(f),m=f-p;this.closed?p+=p>0?0:(Math.floor(Math.abs(p)/c)+1)*c:m===0&&p===c-1&&(p=c-2,m=1);let d,x;this.closed||p>0?d=l[(p-1)%c]:(Tv.subVectors(l[0],l[1]).add(l[0]),d=Tv);const v=l[p%c],g=l[(p+1)%c];if(this.closed||p+2<c?x=l[(p+2)%c]:(bv.subVectors(l[c-1],l[c-2]).add(l[c-1]),x=bv),this.curveType==="centripetal"||this.curveType==="chordal"){const S=this.curveType==="chordal"?.5:.25;let b=Math.pow(d.distanceToSquared(v),S),w=Math.pow(v.distanceToSquared(g),S),E=Math.pow(g.distanceToSquared(x),S);w<1e-4&&(w=1),b<1e-4&&(b=w),E<1e-4&&(E=w),yd.initNonuniformCatmullRom(d.x,v.x,g.x,x.x,b,w,E),Sd.initNonuniformCatmullRom(d.y,v.y,g.y,x.y,b,w,E),Md.initNonuniformCatmullRom(d.z,v.z,g.z,x.z,b,w,E)}else this.curveType==="catmullrom"&&(yd.initCatmullRom(d.x,v.x,g.x,x.x,this.tension),Sd.initCatmullRom(d.y,v.y,g.y,x.y,this.tension),Md.initCatmullRom(d.z,v.z,g.z,x.z,this.tension));return s.set(yd.calc(m),Sd.calc(m),Md.calc(m)),s}copy(t){super.copy(t),this.points=[];for(let i=0,s=t.points.length;i<s;i++){const l=t.points[i];this.points.push(l.clone())}return this.closed=t.closed,this.curveType=t.curveType,this.tension=t.tension,this}toJSON(){const t=super.toJSON();t.points=[];for(let i=0,s=this.points.length;i<s;i++){const l=this.points[i];t.points.push(l.toArray())}return t.closed=this.closed,t.curveType=this.curveType,t.tension=this.tension,t}fromJSON(t){super.fromJSON(t),this.points=[];for(let i=0,s=t.points.length;i<s;i++){const l=t.points[i];this.points.push(new k().fromArray(l))}return this.closed=t.closed,this.curveType=t.curveType,this.tension=t.tension,this}}class gs extends hn{constructor(t=1,i=1,s=1,l=1){super(),this.type="PlaneGeometry",this.parameters={width:t,height:i,widthSegments:s,heightSegments:l};const c=t/2,f=i/2,p=Math.floor(s),m=Math.floor(l),d=p+1,x=m+1,v=t/p,g=i/m,S=[],b=[],w=[],E=[];for(let _=0;_<x;_++){const z=_*g-f;for(let F=0;F<d;F++){const C=F*v-c;b.push(C,-z,0),w.push(0,0,1),E.push(F/p),E.push(1-_/m)}}for(let _=0;_<m;_++)for(let z=0;z<p;z++){const F=z+d*_,C=z+d*(_+1),B=z+1+d*(_+1),O=z+1+d*_;S.push(F,C,O),S.push(C,B,O)}this.setIndex(S),this.setAttribute("position",new En(b,3)),this.setAttribute("normal",new En(w,3)),this.setAttribute("uv",new En(E,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new gs(t.width,t.height,t.widthSegments,t.heightSegments)}}class jp extends hn{constructor(t=1,i=.4,s=12,l=48,c=Math.PI*2,f=0,p=Math.PI*2){super(),this.type="TorusGeometry",this.parameters={radius:t,tube:i,radialSegments:s,tubularSegments:l,arc:c,thetaStart:f,thetaLength:p},s=Math.floor(s),l=Math.floor(l);const m=[],d=[],x=[],v=[],g=new k,S=new k,b=new k;for(let w=0;w<=s;w++){const E=f+w/s*p;for(let _=0;_<=l;_++){const z=_/l*c;S.x=(t+i*Math.cos(E))*Math.cos(z),S.y=(t+i*Math.cos(E))*Math.sin(z),S.z=i*Math.sin(E),d.push(S.x,S.y,S.z),g.x=t*Math.cos(z),g.y=t*Math.sin(z),b.subVectors(S,g).normalize(),x.push(b.x,b.y,b.z),v.push(_/l),v.push(w/s)}}for(let w=1;w<=s;w++)for(let E=1;E<=l;E++){const _=(l+1)*w+E-1,z=(l+1)*(w-1)+E-1,F=(l+1)*(w-1)+E,C=(l+1)*w+E;m.push(_,z,C),m.push(z,F,C)}this.setIndex(m),this.setAttribute("position",new En(d,3)),this.setAttribute("normal",new En(x,3)),this.setAttribute("uv",new En(v,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new jp(t.radius,t.tube,t.radialSegments,t.tubularSegments,t.arc)}}function so(r){const t={};for(const i in r){t[i]={};for(const s in r[i]){const l=r[i][s];if(Av(l))l.isRenderTargetTexture?(se("UniformsUtils: Textures of render targets cannot be cloned via cloneUniforms() or mergeUniforms()."),t[i][s]=null):t[i][s]=l.clone();else if(Array.isArray(l))if(Av(l[0])){const c=[];for(let f=0,p=l.length;f<p;f++)c[f]=l[f].clone();t[i][s]=c}else t[i][s]=l.slice();else t[i][s]=l}}return t}function Wn(r){const t={};for(let i=0;i<r.length;i++){const s=so(r[i]);for(const l in s)t[l]=s[l]}return t}function Av(r){return r&&(r.isColor||r.isMatrix3||r.isMatrix4||r.isVector2||r.isVector3||r.isVector4||r.isTexture||r.isQuaternion)}function cb(r){const t=[];for(let i=0;i<r.length;i++)t.push(r[i].clone());return t}function kx(r){const t=r.getRenderTarget();return t===null?r.outputColorSpace:t.isXRRenderTarget===!0?t.texture.colorSpace:Re.workingColorSpace}const ub={clone:so,merge:Wn};var fb=`void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}`,hb=`void main() {
	gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
}`;class $i extends ms{constructor(t){super(),this.isShaderMaterial=!0,this.type="ShaderMaterial",this.defines={},this.uniforms={},this.uniformsGroups=[],this.vertexShader=fb,this.fragmentShader=hb,this.linewidth=1,this.wireframe=!1,this.wireframeLinewidth=1,this.fog=!1,this.lights=!1,this.clipping=!1,this.forceSinglePass=!0,this.extensions={clipCullDistance:!1,multiDraw:!1},this.defaultAttributeValues={color:[1,1,1],uv:[0,0],uv1:[0,0]},this.index0AttributeName=void 0,this.uniformsNeedUpdate=!1,this.glslVersion=null,t!==void 0&&this.setValues(t)}copy(t){return super.copy(t),this.fragmentShader=t.fragmentShader,this.vertexShader=t.vertexShader,this.uniforms=so(t.uniforms),this.uniformsGroups=cb(t.uniformsGroups),this.defines=Object.assign({},t.defines),this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.fog=t.fog,this.lights=t.lights,this.clipping=t.clipping,this.extensions=Object.assign({},t.extensions),this.glslVersion=t.glslVersion,this.defaultAttributeValues=Object.assign({},t.defaultAttributeValues),this.index0AttributeName=t.index0AttributeName,this.uniformsNeedUpdate=t.uniformsNeedUpdate,this}toJSON(t){const i=super.toJSON(t);i.glslVersion=this.glslVersion,i.uniforms={};for(const l in this.uniforms){const f=this.uniforms[l].value;f&&f.isTexture?i.uniforms[l]={type:"t",value:f.toJSON(t).uuid}:f&&f.isColor?i.uniforms[l]={type:"c",value:f.getHex()}:f&&f.isVector2?i.uniforms[l]={type:"v2",value:f.toArray()}:f&&f.isVector3?i.uniforms[l]={type:"v3",value:f.toArray()}:f&&f.isVector4?i.uniforms[l]={type:"v4",value:f.toArray()}:f&&f.isMatrix3?i.uniforms[l]={type:"m3",value:f.toArray()}:f&&f.isMatrix4?i.uniforms[l]={type:"m4",value:f.toArray()}:i.uniforms[l]={value:f}}Object.keys(this.defines).length>0&&(i.defines=this.defines),i.vertexShader=this.vertexShader,i.fragmentShader=this.fragmentShader,i.lights=this.lights,i.clipping=this.clipping;const s={};for(const l in this.extensions)this.extensions[l]===!0&&(s[l]=!0);return Object.keys(s).length>0&&(i.extensions=s),i}fromJSON(t,i){if(super.fromJSON(t,i),t.uniforms!==void 0)for(const s in t.uniforms){const l=t.uniforms[s];switch(this.uniforms[s]={},l.type){case"t":this.uniforms[s].value=i[l.value]||null;break;case"c":this.uniforms[s].value=new ie().setHex(l.value);break;case"v2":this.uniforms[s].value=new oe().fromArray(l.value);break;case"v3":this.uniforms[s].value=new k().fromArray(l.value);break;case"v4":this.uniforms[s].value=new cn().fromArray(l.value);break;case"m3":this.uniforms[s].value=new ue().fromArray(l.value);break;case"m4":this.uniforms[s].value=new we().fromArray(l.value);break;default:this.uniforms[s].value=l.value}}if(t.defines!==void 0&&(this.defines=t.defines),t.vertexShader!==void 0&&(this.vertexShader=t.vertexShader),t.fragmentShader!==void 0&&(this.fragmentShader=t.fragmentShader),t.glslVersion!==void 0&&(this.glslVersion=t.glslVersion),t.extensions!==void 0)for(const s in t.extensions)this.extensions[s]=t.extensions[s];return t.lights!==void 0&&(this.lights=t.lights),t.clipping!==void 0&&(this.clipping=t.clipping),this}}class db extends $i{constructor(t){super(t),this.isRawShaderMaterial=!0,this.type="RawShaderMaterial"}}class pi extends ms{constructor(t){super(),this.isMeshStandardMaterial=!0,this.type="MeshStandardMaterial",this.defines={STANDARD:""},this.color=new ie(16777215),this.roughness=1,this.metalness=0,this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.emissive=new ie(0),this.emissiveIntensity=1,this.emissiveMap=null,this.bumpMap=null,this.bumpScale=1,this.normalMap=null,this.normalMapType=xp,this.normalScale=new oe(1,1),this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.roughnessMap=null,this.metalnessMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new ps,this.envMapIntensity=1,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.flatShading=!1,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.defines={STANDARD:""},this.color.copy(t.color),this.roughness=t.roughness,this.metalness=t.metalness,this.map=t.map,this.lightMap=t.lightMap,this.lightMapIntensity=t.lightMapIntensity,this.aoMap=t.aoMap,this.aoMapIntensity=t.aoMapIntensity,this.emissive.copy(t.emissive),this.emissiveMap=t.emissiveMap,this.emissiveIntensity=t.emissiveIntensity,this.bumpMap=t.bumpMap,this.bumpScale=t.bumpScale,this.normalMap=t.normalMap,this.normalMapType=t.normalMapType,this.normalScale.copy(t.normalScale),this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this.roughnessMap=t.roughnessMap,this.metalnessMap=t.metalnessMap,this.alphaMap=t.alphaMap,this.envMap=t.envMap,this.envMapRotation.copy(t.envMapRotation),this.envMapIntensity=t.envMapIntensity,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.wireframeLinecap=t.wireframeLinecap,this.wireframeLinejoin=t.wireframeLinejoin,this.flatShading=t.flatShading,this.fog=t.fog,this}}class pb extends pi{constructor(t){super(),this.isMeshPhysicalMaterial=!0,this.defines={STANDARD:"",PHYSICAL:""},this.type="MeshPhysicalMaterial",this.anisotropyRotation=0,this.anisotropyMap=null,this.clearcoatMap=null,this.clearcoatRoughness=0,this.clearcoatRoughnessMap=null,this.clearcoatNormalScale=new oe(1,1),this.clearcoatNormalMap=null,this.ior=1.5,Object.defineProperty(this,"reflectivity",{get:function(){return ge(2.5*(this.ior-1)/(this.ior+1),0,1)},set:function(i){this.ior=(1+.4*i)/(1-.4*i)}}),this.iridescenceMap=null,this.iridescenceIOR=1.3,this.iridescenceThicknessRange=[100,400],this.iridescenceThicknessMap=null,this.sheenColor=new ie(0),this.sheenColorMap=null,this.sheenRoughness=1,this.sheenRoughnessMap=null,this.transmissionMap=null,this.thickness=0,this.thicknessMap=null,this.attenuationDistance=1/0,this.attenuationColor=new ie(1,1,1),this.specularIntensity=1,this.specularIntensityMap=null,this.specularColor=new ie(1,1,1),this.specularColorMap=null,this._anisotropy=0,this._clearcoat=0,this._dispersion=0,this._iridescence=0,this._sheen=0,this._transmission=0,this.setValues(t)}get anisotropy(){return this._anisotropy}set anisotropy(t){this._anisotropy>0!=t>0&&this.version++,this._anisotropy=t}get clearcoat(){return this._clearcoat}set clearcoat(t){this._clearcoat>0!=t>0&&this.version++,this._clearcoat=t}get iridescence(){return this._iridescence}set iridescence(t){this._iridescence>0!=t>0&&this.version++,this._iridescence=t}get dispersion(){return this._dispersion}set dispersion(t){this._dispersion>0!=t>0&&this.version++,this._dispersion=t}get sheen(){return this._sheen}set sheen(t){this._sheen>0!=t>0&&this.version++,this._sheen=t}get transmission(){return this._transmission}set transmission(t){this._transmission>0!=t>0&&this.version++,this._transmission=t}copy(t){return super.copy(t),this.defines={STANDARD:"",PHYSICAL:""},this.anisotropy=t.anisotropy,this.anisotropyRotation=t.anisotropyRotation,this.anisotropyMap=t.anisotropyMap,this.clearcoat=t.clearcoat,this.clearcoatMap=t.clearcoatMap,this.clearcoatRoughness=t.clearcoatRoughness,this.clearcoatRoughnessMap=t.clearcoatRoughnessMap,this.clearcoatNormalMap=t.clearcoatNormalMap,this.clearcoatNormalScale.copy(t.clearcoatNormalScale),this.dispersion=t.dispersion,this.ior=t.ior,this.iridescence=t.iridescence,this.iridescenceMap=t.iridescenceMap,this.iridescenceIOR=t.iridescenceIOR,this.iridescenceThicknessRange=[...t.iridescenceThicknessRange],this.iridescenceThicknessMap=t.iridescenceThicknessMap,this.sheen=t.sheen,this.sheenColor.copy(t.sheenColor),this.sheenColorMap=t.sheenColorMap,this.sheenRoughness=t.sheenRoughness,this.sheenRoughnessMap=t.sheenRoughnessMap,this.transmission=t.transmission,this.transmissionMap=t.transmissionMap,this.thickness=t.thickness,this.thicknessMap=t.thicknessMap,this.attenuationDistance=t.attenuationDistance,this.attenuationColor.copy(t.attenuationColor),this.specularIntensity=t.specularIntensity,this.specularIntensityMap=t.specularIntensityMap,this.specularColor.copy(t.specularColor),this.specularColorMap=t.specularColorMap,this}}class mb extends ms{constructor(t){super(),this.isMeshDepthMaterial=!0,this.type="MeshDepthMaterial",this.depthPacking=rE,this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.wireframe=!1,this.wireframeLinewidth=1,this.setValues(t)}copy(t){return super.copy(t),this.depthPacking=t.depthPacking,this.map=t.map,this.alphaMap=t.alphaMap,this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this}}class gb extends ms{constructor(t){super(),this.isMeshDistanceMaterial=!0,this.type="MeshDistanceMaterial",this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.setValues(t)}copy(t){return super.copy(t),this.map=t.map,this.alphaMap=t.alphaMap,this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this}}class _b extends hs{constructor(t){super(),this.isLineDashedMaterial=!0,this.type="LineDashedMaterial",this.scale=1,this.dashSize=3,this.gapSize=1,this.setValues(t)}copy(t){return super.copy(t),this.scale=t.scale,this.dashSize=t.dashSize,this.gapSize=t.gapSize,this}}class Zp extends Mn{constructor(t,i=1){super(),this.isLight=!0,this.type="Light",this.color=new ie(t),this.intensity=i}dispose(){this.dispatchEvent({type:"dispose"})}copy(t,i){return super.copy(t,i),this.color.copy(t.color),this.intensity=t.intensity,this}toJSON(t){const i=super.toJSON(t);return i.object.color=this.color.getHex(),i.object.intensity=this.intensity,i}}class vb extends Zp{constructor(t,i,s){super(t,s),this.isHemisphereLight=!0,this.type="HemisphereLight",this.position.copy(Mn.DEFAULT_UP),this.updateMatrix(),this.groundColor=new ie(i)}copy(t,i){return super.copy(t,i),this.groundColor.copy(t.groundColor),this}toJSON(t){const i=super.toJSON(t);return i.object.groundColor=this.groundColor.getHex(),i}}const Ed=new we,Rv=new k,wv=new k;class Xx{constructor(t){this.camera=t,this.intensity=1,this.bias=0,this.biasNode=null,this.normalBias=0,this.radius=1,this.blurSamples=8,this.mapSize=new oe(512,512),this.mapType=gi,this.map=null,this.mapPass=null,this.matrix=new we,this.autoUpdate=!0,this.needsUpdate=!1,this._frustum=new Wp,this._frameExtents=new oe(1,1),this._viewportCount=1,this._viewports=[new cn(0,0,1,1)]}getViewportCount(){return this._viewportCount}getFrustum(){return this._frustum}updateMatrices(t){const i=this.camera,s=this.matrix;Rv.setFromMatrixPosition(t.matrixWorld),i.position.copy(Rv),wv.setFromMatrixPosition(t.target.matrixWorld),i.lookAt(wv),i.updateMatrixWorld(),Ed.multiplyMatrices(i.projectionMatrix,i.matrixWorldInverse),this._frustum.setFromProjectionMatrix(Ed,i.coordinateSystem,i.reversedDepth),i.coordinateSystem===Sl||i.reversedDepth?s.set(.5,0,0,.5,0,.5,0,.5,0,0,1,0,0,0,0,1):s.set(.5,0,0,.5,0,.5,0,.5,0,0,.5,.5,0,0,0,1),s.multiply(Ed)}getViewport(t){return this._viewports[t]}getFrameExtents(){return this._frameExtents}dispose(){this.map&&this.map.dispose(),this.mapPass&&this.mapPass.dispose()}copy(t){return this.camera=t.camera.clone(),this.intensity=t.intensity,this.bias=t.bias,this.radius=t.radius,this.autoUpdate=t.autoUpdate,this.needsUpdate=t.needsUpdate,this.normalBias=t.normalBias,this.blurSamples=t.blurSamples,this.mapSize.copy(t.mapSize),this.biasNode=t.biasNode,this}clone(){return new this.constructor().copy(this)}toJSON(){const t={};return this.intensity!==1&&(t.intensity=this.intensity),this.bias!==0&&(t.bias=this.bias),this.normalBias!==0&&(t.normalBias=this.normalBias),this.radius!==1&&(t.radius=this.radius),(this.mapSize.x!==512||this.mapSize.y!==512)&&(t.mapSize=this.mapSize.toArray()),t.camera=this.camera.toJSON(!1).object,delete t.camera.matrix,t}}const cu=new k,uu=new ro,qi=new k;class Wx extends Mn{constructor(){super(),this.isCamera=!0,this.type="Camera",this.matrixWorldInverse=new we,this.projectionMatrix=new we,this.projectionMatrixInverse=new we,this.coordinateSystem=Zi,this._reversedDepth=!1}get reversedDepth(){return this._reversedDepth}copy(t,i){return super.copy(t,i),this.matrixWorldInverse.copy(t.matrixWorldInverse),this.projectionMatrix.copy(t.projectionMatrix),this.projectionMatrixInverse.copy(t.projectionMatrixInverse),this.coordinateSystem=t.coordinateSystem,this}getWorldDirection(t){return super.getWorldDirection(t).negate()}updateMatrixWorld(t){super.updateMatrixWorld(t),this.matrixWorld.decompose(cu,uu,qi),qi.x===1&&qi.y===1&&qi.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(cu,uu,qi.set(1,1,1)).invert()}updateWorldMatrix(t,i,s=!1){super.updateWorldMatrix(t,i,s),this.matrixWorld.decompose(cu,uu,qi),qi.x===1&&qi.y===1&&qi.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(cu,uu,qi.set(1,1,1)).invert()}clone(){return new this.constructor().copy(this)}}const cs=new k,Cv=new oe,Dv=new oe;class mi extends Wx{constructor(t=50,i=1,s=.1,l=2e3){super(),this.isPerspectiveCamera=!0,this.type="PerspectiveCamera",this.fov=t,this.zoom=1,this.near=s,this.far=l,this.focus=10,this.aspect=i,this.view=null,this.filmGauge=35,this.filmOffset=0,this.updateProjectionMatrix()}copy(t,i){return super.copy(t,i),this.fov=t.fov,this.zoom=t.zoom,this.near=t.near,this.far=t.far,this.focus=t.focus,this.aspect=t.aspect,this.view=t.view===null?null:Object.assign({},t.view),this.filmGauge=t.filmGauge,this.filmOffset=t.filmOffset,this}setFocalLength(t){const i=.5*this.getFilmHeight()/t;this.fov=Ml*2*Math.atan(i),this.updateProjectionMatrix()}getFocalLength(){const t=Math.tan($r*.5*this.fov);return .5*this.getFilmHeight()/t}getEffectiveFOV(){return Ml*2*Math.atan(Math.tan($r*.5*this.fov)/this.zoom)}getFilmWidth(){return this.filmGauge*Math.min(this.aspect,1)}getFilmHeight(){return this.filmGauge/Math.max(this.aspect,1)}getViewBounds(t,i,s){cs.set(-1,-1,.5).applyMatrix4(this.projectionMatrixInverse),i.set(cs.x,cs.y).multiplyScalar(-t/cs.z),cs.set(1,1,.5).applyMatrix4(this.projectionMatrixInverse),s.set(cs.x,cs.y).multiplyScalar(-t/cs.z)}getViewSize(t,i){return this.getViewBounds(t,Cv,Dv),i.subVectors(Dv,Cv)}setViewOffset(t,i,s,l,c,f){this.aspect=t/i,this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=t,this.view.fullHeight=i,this.view.offsetX=s,this.view.offsetY=l,this.view.width=c,this.view.height=f,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const t=this.near;let i=t*Math.tan($r*.5*this.fov)/this.zoom,s=2*i,l=this.aspect*s,c=-.5*l;const f=this.view;if(this.view!==null&&this.view.enabled){const m=f.fullWidth,d=f.fullHeight;c+=f.offsetX*l/m,i-=f.offsetY*s/d,l*=f.width/m,s*=f.height/d}const p=this.filmOffset;p!==0&&(c+=t*p/this.getFilmWidth()),this.projectionMatrix.makePerspective(c,c+l,i,i-s,t,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(t){const i=super.toJSON(t);return i.object.fov=this.fov,i.object.zoom=this.zoom,i.object.near=this.near,i.object.far=this.far,i.object.focus=this.focus,i.object.aspect=this.aspect,this.view!==null&&(i.object.view=Object.assign({},this.view)),i.object.filmGauge=this.filmGauge,i.object.filmOffset=this.filmOffset,i}}class xb extends Xx{constructor(){super(new mi(90,1,.5,500)),this.isPointLightShadow=!0}}class qx extends Zp{constructor(t,i,s=0,l=2){super(t,i),this.isPointLight=!0,this.type="PointLight",this.distance=s,this.decay=l,this.shadow=new xb}get power(){return this.intensity*4*Math.PI}set power(t){this.intensity=t/(4*Math.PI)}dispose(){super.dispose(),this.shadow.dispose()}copy(t,i){return super.copy(t,i),this.distance=t.distance,this.decay=t.decay,this.shadow=t.shadow.clone(),this}toJSON(t){const i=super.toJSON(t);return i.object.distance=this.distance,i.object.decay=this.decay,i.object.shadow=this.shadow.toJSON(),i}}class Kp extends Wx{constructor(t=-1,i=1,s=1,l=-1,c=.1,f=2e3){super(),this.isOrthographicCamera=!0,this.type="OrthographicCamera",this.zoom=1,this.view=null,this.left=t,this.right=i,this.top=s,this.bottom=l,this.near=c,this.far=f,this.updateProjectionMatrix()}copy(t,i){return super.copy(t,i),this.left=t.left,this.right=t.right,this.top=t.top,this.bottom=t.bottom,this.near=t.near,this.far=t.far,this.zoom=t.zoom,this.view=t.view===null?null:Object.assign({},t.view),this}setViewOffset(t,i,s,l,c,f){this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=t,this.view.fullHeight=i,this.view.offsetX=s,this.view.offsetY=l,this.view.width=c,this.view.height=f,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const t=(this.right-this.left)/(2*this.zoom),i=(this.top-this.bottom)/(2*this.zoom),s=(this.right+this.left)/2,l=(this.top+this.bottom)/2;let c=s-t,f=s+t,p=l+i,m=l-i;if(this.view!==null&&this.view.enabled){const d=(this.right-this.left)/this.view.fullWidth/this.zoom,x=(this.top-this.bottom)/this.view.fullHeight/this.zoom;c+=d*this.view.offsetX,f=c+d*this.view.width,p-=x*this.view.offsetY,m=p-x*this.view.height}this.projectionMatrix.makeOrthographic(c,f,p,m,this.near,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(t){const i=super.toJSON(t);return i.object.zoom=this.zoom,i.object.left=this.left,i.object.right=this.right,i.object.top=this.top,i.object.bottom=this.bottom,i.object.near=this.near,i.object.far=this.far,this.view!==null&&(i.object.view=Object.assign({},this.view)),i}}class yb extends Xx{constructor(){super(new Kp(-5,5,5,-5,.5,500)),this.isDirectionalLightShadow=!0}}class Sb extends Zp{constructor(t,i){super(t,i),this.isDirectionalLight=!0,this.type="DirectionalLight",this.position.copy(Mn.DEFAULT_UP),this.updateMatrix(),this.target=new Mn,this.shadow=new yb}dispose(){super.dispose(),this.shadow.dispose()}copy(t){return super.copy(t),this.target=t.target.clone(),this.shadow=t.shadow.clone(),this}toJSON(t){const i=super.toJSON(t);return i.object.shadow=this.shadow.toJSON(),i.object.target=this.target.uuid,i}}const Yr=-90,jr=1;class Mb extends Mn{constructor(t,i,s){super(),this.type="CubeCamera",this.renderTarget=s,this.coordinateSystem=null,this.activeMipmapLevel=0;const l=new mi(Yr,jr,t,i);l.layers=this.layers,this.add(l);const c=new mi(Yr,jr,t,i);c.layers=this.layers,this.add(c);const f=new mi(Yr,jr,t,i);f.layers=this.layers,this.add(f);const p=new mi(Yr,jr,t,i);p.layers=this.layers,this.add(p);const m=new mi(Yr,jr,t,i);m.layers=this.layers,this.add(m);const d=new mi(Yr,jr,t,i);d.layers=this.layers,this.add(d)}updateCoordinateSystem(){const t=this.coordinateSystem,i=this.children.concat(),[s,l,c,f,p,m]=i;for(const d of i)this.remove(d);if(t===Zi)s.up.set(0,1,0),s.lookAt(1,0,0),l.up.set(0,1,0),l.lookAt(-1,0,0),c.up.set(0,0,-1),c.lookAt(0,1,0),f.up.set(0,0,1),f.lookAt(0,-1,0),p.up.set(0,1,0),p.lookAt(0,0,1),m.up.set(0,1,0),m.lookAt(0,0,-1);else if(t===Sl)s.up.set(0,-1,0),s.lookAt(-1,0,0),l.up.set(0,-1,0),l.lookAt(1,0,0),c.up.set(0,0,1),c.lookAt(0,1,0),f.up.set(0,0,-1),f.lookAt(0,-1,0),p.up.set(0,-1,0),p.lookAt(0,0,1),m.up.set(0,-1,0),m.lookAt(0,0,-1);else throw new Error("THREE.CubeCamera.updateCoordinateSystem(): Invalid coordinate system: "+t);for(const d of i)this.add(d),d.updateMatrixWorld()}update(t,i){this.parent===null&&this.updateMatrixWorld();const{renderTarget:s,activeMipmapLevel:l}=this;this.coordinateSystem!==t.coordinateSystem&&(this.coordinateSystem=t.coordinateSystem,this.updateCoordinateSystem());const[c,f,p,m,d,x]=this.children,v=t.getRenderTarget(),g=t.getActiveCubeFace(),S=t.getActiveMipmapLevel(),b=t.xr.enabled;t.xr.enabled=!1;const w=s.texture.generateMipmaps;s.texture.generateMipmaps=!1;let E=!1;t.isWebGLRenderer===!0?E=t.state.buffers.depth.getReversed():E=t.reversedDepthBuffer,t.setRenderTarget(s,0,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,c),t.setRenderTarget(s,1,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,f),t.setRenderTarget(s,2,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,p),t.setRenderTarget(s,3,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,m),t.setRenderTarget(s,4,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,d),s.texture.generateMipmaps=w,t.setRenderTarget(s,5,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,x),t.setRenderTarget(v,g,S),t.xr.enabled=b,s.texture.needsPMREMUpdate=!0}}class Eb extends mi{constructor(t=[]){super(),this.isArrayCamera=!0,this.isMultiViewCamera=!1,this.cameras=t}}const Uv=new we;class bb{constructor(t,i,s=0,l=1/0){this.ray=new Uu(t,i),this.near=s,this.far=l,this.camera=null,this.layers=new Vp,this.params={Mesh:{},Line:{threshold:1},LOD:{},Points:{threshold:1},Sprite:{}}}set(t,i){this.ray.set(t,i)}setFromCamera(t,i){i.isPerspectiveCamera?(this.ray.origin.setFromMatrixPosition(i.matrixWorld),this.ray.direction.set(t.x,t.y,.5).unproject(i).sub(this.ray.origin).normalize(),this.camera=i):i.isOrthographicCamera?(this.ray.origin.set(t.x,t.y,i.projectionMatrix.elements[14]).unproject(i),this.ray.direction.set(0,0,-1).transformDirection(i.matrixWorld),this.camera=i):Ae("Raycaster: Unsupported camera type: "+i.type)}setFromXRController(t){return Uv.identity().extractRotation(t.matrixWorld),this.ray.origin.setFromMatrixPosition(t.matrixWorld),this.ray.direction.set(0,0,-1).applyMatrix4(Uv),this}intersectObject(t,i=!0,s=[]){return Ep(t,this,s,i),s.sort(Nv),s}intersectObjects(t,i=!0,s=[]){for(let l=0,c=t.length;l<c;l++)Ep(t[l],this,s,i);return s.sort(Nv),s}}function Nv(r,t){return r.distance-t.distance}function Ep(r,t,i,s){let l=!0;if(r.layers.test(t.layers)&&r.raycast(t,i)===!1&&(l=!1),l===!0&&s===!0){const c=r.children;for(let f=0,p=c.length;f<p;f++)Ep(c[f],t,i,!0)}}const nm=class nm{constructor(t,i,s,l){this.elements=[1,0,0,1],t!==void 0&&this.set(t,i,s,l)}identity(){return this.set(1,0,0,1),this}fromArray(t,i=0){for(let s=0;s<4;s++)this.elements[s]=t[s+i];return this}set(t,i,s,l){const c=this.elements;return c[0]=t,c[2]=i,c[1]=s,c[3]=l,this}};nm.prototype.isMatrix2=!0;let Lv=nm;class Tb extends Ix{constructor(t=10,i=10,s=4473924,l=8947848){s=new ie(s),l=new ie(l);const c=i/2,f=t/i,p=t/2,m=[],d=[];for(let g=0,S=0,b=-p;g<=i;g++,b+=f){m.push(-p,0,b,p,0,b),m.push(b,0,-p,b,0,p);const w=g===c?s:l;w.toArray(d,S),S+=3,w.toArray(d,S),S+=3,w.toArray(d,S),S+=3,w.toArray(d,S),S+=3}const x=new hn;x.setAttribute("position",new En(m,3)),x.setAttribute("color",new En(d,3));const v=new hs({vertexColors:!0,toneMapped:!1});super(x,v),this.type="GridHelper"}dispose(){this.geometry.dispose(),this.material.dispose()}}function Ov(r,t,i,s){const l=Ab(s);switch(i){case wx:return r*t;case Op:return r*t/l.components*l.byteLength;case Pp:return r*t/l.components*l.byteLength;case Zs:return r*t*2/l.components*l.byteLength;case zp:return r*t*2/l.components*l.byteLength;case Cx:return r*t*3/l.components*l.byteLength;case Fi:return r*t*4/l.components*l.byteLength;case Bp:return r*t*4/l.components*l.byteLength;case pu:case mu:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*8;case gu:case _u:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*16;case Xd:case qd:return Math.max(r,16)*Math.max(t,8)/4;case kd:case Wd:return Math.max(r,8)*Math.max(t,8)/2;case Yd:case jd:case Kd:case Qd:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*8;case Zd:case yu:case Jd:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*16;case $d:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*16;case tp:return Math.floor((r+4)/5)*Math.floor((t+3)/4)*16;case ep:return Math.floor((r+4)/5)*Math.floor((t+4)/5)*16;case np:return Math.floor((r+5)/6)*Math.floor((t+4)/5)*16;case ip:return Math.floor((r+5)/6)*Math.floor((t+5)/6)*16;case ap:return Math.floor((r+7)/8)*Math.floor((t+4)/5)*16;case sp:return Math.floor((r+7)/8)*Math.floor((t+5)/6)*16;case rp:return Math.floor((r+7)/8)*Math.floor((t+7)/8)*16;case op:return Math.floor((r+9)/10)*Math.floor((t+4)/5)*16;case lp:return Math.floor((r+9)/10)*Math.floor((t+5)/6)*16;case cp:return Math.floor((r+9)/10)*Math.floor((t+7)/8)*16;case up:return Math.floor((r+9)/10)*Math.floor((t+9)/10)*16;case fp:return Math.floor((r+11)/12)*Math.floor((t+9)/10)*16;case hp:return Math.floor((r+11)/12)*Math.floor((t+11)/12)*16;case dp:case pp:case mp:return Math.ceil(r/4)*Math.ceil(t/4)*16;case gp:case _p:return Math.ceil(r/4)*Math.ceil(t/4)*8;case Su:case vp:return Math.ceil(r/4)*Math.ceil(t/4)*16}throw new Error(`Unable to determine texture byte length for ${i} format.`)}function Ab(r){switch(r){case gi:case bx:return{byteLength:1,components:1};case xl:case Tx:case Ca:return{byteLength:2,components:1};case Np:case Lp:return{byteLength:2,components:4};case Ji:case Up:case Ii:return{byteLength:4,components:1};case Ax:case Rx:return{byteLength:4,components:3}}throw new Error(`THREE.TextureUtils: Unknown texture type ${r}.`)}typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("register",{detail:{revision:Cp}}));typeof window<"u"&&(window.__THREE__?se("WARNING: Multiple instances of Three.js being imported."):window.__THREE__=Cp);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */function Yx(){let r=null,t=!1,i=null,s=null;function l(c,f){i(c,f),s=r.requestAnimationFrame(l)}return{start:function(){t!==!0&&i!==null&&r!==null&&(s=r.requestAnimationFrame(l),t=!0)},stop:function(){r!==null&&r.cancelAnimationFrame(s),t=!1},setAnimationLoop:function(c){i=c},setContext:function(c){r=c}}}function Rb(r){const t=new WeakMap;function i(p,m){const d=p.array,x=p.usage,v=d.byteLength,g=r.createBuffer();r.bindBuffer(m,g),r.bufferData(m,d,x),p.onUploadCallback();let S;if(d instanceof Float32Array)S=r.FLOAT;else if(typeof Float16Array<"u"&&d instanceof Float16Array)S=r.HALF_FLOAT;else if(d instanceof Uint16Array)p.isFloat16BufferAttribute?S=r.HALF_FLOAT:S=r.UNSIGNED_SHORT;else if(d instanceof Int16Array)S=r.SHORT;else if(d instanceof Uint32Array)S=r.UNSIGNED_INT;else if(d instanceof Int32Array)S=r.INT;else if(d instanceof Int8Array)S=r.BYTE;else if(d instanceof Uint8Array)S=r.UNSIGNED_BYTE;else if(d instanceof Uint8ClampedArray)S=r.UNSIGNED_BYTE;else throw new Error("THREE.WebGLAttributes: Unsupported buffer data format: "+d);return{buffer:g,type:S,bytesPerElement:d.BYTES_PER_ELEMENT,version:p.version,size:v}}function s(p,m,d){const x=m.array,v=m.updateRanges;if(r.bindBuffer(d,p),v.length===0)r.bufferSubData(d,0,x);else{v.sort((S,b)=>S.start-b.start);let g=0;for(let S=1;S<v.length;S++){const b=v[g],w=v[S];w.start<=b.start+b.count+1?b.count=Math.max(b.count,w.start+w.count-b.start):(++g,v[g]=w)}v.length=g+1;for(let S=0,b=v.length;S<b;S++){const w=v[S];r.bufferSubData(d,w.start*x.BYTES_PER_ELEMENT,x,w.start,w.count)}m.clearUpdateRanges()}m.onUploadCallback()}function l(p){return p.isInterleavedBufferAttribute&&(p=p.data),t.get(p)}function c(p){p.isInterleavedBufferAttribute&&(p=p.data);const m=t.get(p);m&&(r.deleteBuffer(m.buffer),t.delete(p))}function f(p,m){if(p.isInterleavedBufferAttribute&&(p=p.data),p.isGLBufferAttribute){const x=t.get(p);(!x||x.version<p.version)&&t.set(p,{buffer:p.buffer,type:p.type,bytesPerElement:p.elementSize,version:p.version});return}const d=t.get(p);if(d===void 0)t.set(p,i(p,m));else if(d.version<p.version){if(d.size!==p.array.byteLength)throw new Error("THREE.WebGLAttributes: The size of the buffer attribute's array buffer does not match the original size. Resizing buffer attributes is not supported.");s(d.buffer,p,m),d.version=p.version}}return{get:l,remove:c,update:f}}var wb=`#ifdef USE_ALPHAHASH
	if ( diffuseColor.a < getAlphaHashThreshold( vPosition ) ) discard;
#endif`,Cb=`#ifdef USE_ALPHAHASH
	const float ALPHA_HASH_SCALE = 0.05;
	float hash2D( vec2 value ) {
		return fract( 1.0e4 * sin( 17.0 * value.x + 0.1 * value.y ) * ( 0.1 + abs( sin( 13.0 * value.y + value.x ) ) ) );
	}
	float hash3D( vec3 value ) {
		return hash2D( vec2( hash2D( value.xy ), value.z ) );
	}
	float getAlphaHashThreshold( vec3 position ) {
		float maxDeriv = max(
			length( dFdx( position.xyz ) ),
			length( dFdy( position.xyz ) )
		);
		float pixScale = 1.0 / ( ALPHA_HASH_SCALE * maxDeriv );
		vec2 pixScales = vec2(
			exp2( floor( log2( pixScale ) ) ),
			exp2( ceil( log2( pixScale ) ) )
		);
		vec2 alpha = vec2(
			hash3D( floor( pixScales.x * position.xyz ) ),
			hash3D( floor( pixScales.y * position.xyz ) )
		);
		float lerpFactor = fract( log2( pixScale ) );
		float x = ( 1.0 - lerpFactor ) * alpha.x + lerpFactor * alpha.y;
		float a = min( lerpFactor, 1.0 - lerpFactor );
		vec3 cases = vec3(
			x * x / ( 2.0 * a * ( 1.0 - a ) ),
			( x - 0.5 * a ) / ( 1.0 - a ),
			1.0 - ( ( 1.0 - x ) * ( 1.0 - x ) / ( 2.0 * a * ( 1.0 - a ) ) )
		);
		float threshold = ( x < ( 1.0 - a ) )
			? ( ( x < a ) ? cases.x : cases.y )
			: cases.z;
		return clamp( threshold , 1.0e-6, 1.0 );
	}
#endif`,Db=`#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, vAlphaMapUv ).g;
#endif`,Ub=`#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,Nb=`#ifdef USE_ALPHATEST
	#ifdef ALPHA_TO_COVERAGE
	diffuseColor.a = smoothstep( alphaTest, alphaTest + fwidth( diffuseColor.a ), diffuseColor.a );
	if ( diffuseColor.a == 0.0 ) discard;
	#else
	if ( diffuseColor.a < alphaTest ) discard;
	#endif
#endif`,Lb=`#ifdef USE_ALPHATEST
	uniform float alphaTest;
#endif`,Ob=`#ifdef USE_AOMAP
	float ambientOcclusion = ( texture2D( aoMap, vAoMapUv ).r - 1.0 ) * aoMapIntensity + 1.0;
	reflectedLight.indirectDiffuse *= ambientOcclusion;
	#if defined( USE_CLEARCOAT ) 
		clearcoatSpecularIndirect *= ambientOcclusion;
	#endif
	#if defined( USE_SHEEN ) 
		sheenSpecularIndirect *= ambientOcclusion;
	#endif
	#if defined( USE_ENVMAP ) && defined( STANDARD )
		float dotNV = saturate( dot( geometryNormal, geometryViewDir ) );
		reflectedLight.indirectSpecular *= computeSpecularOcclusion( dotNV, ambientOcclusion, material.roughness );
	#endif
#endif`,Pb=`#ifdef USE_AOMAP
	uniform sampler2D aoMap;
	uniform float aoMapIntensity;
#endif`,zb=`#ifdef USE_BATCHING
	#if ! defined( GL_ANGLE_multi_draw )
	#define gl_DrawID _gl_DrawID
	uniform int _gl_DrawID;
	#endif
	uniform highp sampler2D batchingTexture;
	uniform highp usampler2D batchingIdTexture;
	mat4 getBatchingMatrix( const in float i ) {
		int size = textureSize( batchingTexture, 0 ).x;
		int j = int( i ) * 4;
		int x = j % size;
		int y = j / size;
		vec4 v1 = texelFetch( batchingTexture, ivec2( x, y ), 0 );
		vec4 v2 = texelFetch( batchingTexture, ivec2( x + 1, y ), 0 );
		vec4 v3 = texelFetch( batchingTexture, ivec2( x + 2, y ), 0 );
		vec4 v4 = texelFetch( batchingTexture, ivec2( x + 3, y ), 0 );
		return mat4( v1, v2, v3, v4 );
	}
	float getIndirectIndex( const in int i ) {
		int size = textureSize( batchingIdTexture, 0 ).x;
		int x = i % size;
		int y = i / size;
		return float( texelFetch( batchingIdTexture, ivec2( x, y ), 0 ).r );
	}
#endif
#ifdef USE_BATCHING_COLOR
	uniform sampler2D batchingColorTexture;
	vec4 getBatchingColor( const in float i ) {
		int size = textureSize( batchingColorTexture, 0 ).x;
		int j = int( i );
		int x = j % size;
		int y = j / size;
		return texelFetch( batchingColorTexture, ivec2( x, y ), 0 );
	}
#endif`,Bb=`#ifdef USE_BATCHING
	mat4 batchingMatrix = getBatchingMatrix( getIndirectIndex( gl_DrawID ) );
#endif`,Ib=`vec3 transformed = vec3( position );
#ifdef USE_ALPHAHASH
	vPosition = vec3( position );
#endif`,Fb=`vec3 objectNormal = vec3( normal );
#ifdef USE_TANGENT
	vec3 objectTangent = vec3( tangent.xyz );
#endif`,Hb=`float G_BlinnPhong_Implicit( ) {
	return 0.25;
}
float D_BlinnPhong( const in float shininess, const in float dotNH ) {
	return RECIPROCAL_PI * ( shininess * 0.5 + 1.0 ) * pow( dotNH, shininess );
}
vec3 BRDF_BlinnPhong( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in vec3 specularColor, const in float shininess ) {
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNH = saturate( dot( normal, halfDir ) );
	float dotVH = saturate( dot( viewDir, halfDir ) );
	vec3 F = F_Schlick( specularColor, 1.0, dotVH );
	float G = G_BlinnPhong_Implicit( );
	float D = D_BlinnPhong( shininess, dotNH );
	return F * ( G * D );
} // validated`,Gb=`#ifdef USE_IRIDESCENCE
	const mat3 XYZ_TO_REC709 = mat3(
		 3.2404542, -0.9692660,  0.0556434,
		-1.5371385,  1.8760108, -0.2040259,
		-0.4985314,  0.0415560,  1.0572252
	);
	vec3 Fresnel0ToIor( vec3 fresnel0 ) {
		vec3 sqrtF0 = sqrt( fresnel0 );
		return ( vec3( 1.0 ) + sqrtF0 ) / ( vec3( 1.0 ) - sqrtF0 );
	}
	vec3 IorToFresnel0( vec3 transmittedIor, float incidentIor ) {
		return pow2( ( transmittedIor - vec3( incidentIor ) ) / ( transmittedIor + vec3( incidentIor ) ) );
	}
	float IorToFresnel0( float transmittedIor, float incidentIor ) {
		return pow2( ( transmittedIor - incidentIor ) / ( transmittedIor + incidentIor ));
	}
	vec3 evalSensitivity( float OPD, vec3 shift ) {
		float phase = 2.0 * PI * OPD * 1.0e-9;
		vec3 val = vec3( 5.4856e-13, 4.4201e-13, 5.2481e-13 );
		vec3 pos = vec3( 1.6810e+06, 1.7953e+06, 2.2084e+06 );
		vec3 var = vec3( 4.3278e+09, 9.3046e+09, 6.6121e+09 );
		vec3 xyz = val * sqrt( 2.0 * PI * var ) * cos( pos * phase + shift ) * exp( - pow2( phase ) * var );
		xyz.x += 9.7470e-14 * sqrt( 2.0 * PI * 4.5282e+09 ) * cos( 2.2399e+06 * phase + shift[ 0 ] ) * exp( - 4.5282e+09 * pow2( phase ) );
		xyz /= 1.0685e-7;
		vec3 rgb = XYZ_TO_REC709 * xyz;
		return rgb;
	}
	vec3 evalIridescence( float outsideIOR, float eta2, float cosTheta1, float thinFilmThickness, vec3 baseF0 ) {
		vec3 I;
		float iridescenceIOR = mix( outsideIOR, eta2, smoothstep( 0.0, 0.03, thinFilmThickness ) );
		float sinTheta2Sq = pow2( outsideIOR / iridescenceIOR ) * ( 1.0 - pow2( cosTheta1 ) );
		float cosTheta2Sq = 1.0 - sinTheta2Sq;
		if ( cosTheta2Sq < 0.0 ) {
			return vec3( 1.0 );
		}
		float cosTheta2 = sqrt( cosTheta2Sq );
		float R0 = IorToFresnel0( iridescenceIOR, outsideIOR );
		float R12 = F_Schlick( R0, 1.0, cosTheta1 );
		float T121 = 1.0 - R12;
		float phi12 = 0.0;
		if ( iridescenceIOR < outsideIOR ) phi12 = PI;
		float phi21 = PI - phi12;
		vec3 baseIOR = Fresnel0ToIor( clamp( baseF0, 0.0, 0.9999 ) );		vec3 R1 = IorToFresnel0( baseIOR, iridescenceIOR );
		vec3 R23 = F_Schlick( R1, 1.0, cosTheta2 );
		vec3 phi23 = vec3( 0.0 );
		if ( baseIOR[ 0 ] < iridescenceIOR ) phi23[ 0 ] = PI;
		if ( baseIOR[ 1 ] < iridescenceIOR ) phi23[ 1 ] = PI;
		if ( baseIOR[ 2 ] < iridescenceIOR ) phi23[ 2 ] = PI;
		float OPD = 2.0 * iridescenceIOR * thinFilmThickness * cosTheta2;
		vec3 phi = vec3( phi21 ) + phi23;
		vec3 R123 = clamp( R12 * R23, 1e-5, 0.9999 );
		vec3 r123 = sqrt( R123 );
		vec3 Rs = pow2( T121 ) * R23 / ( vec3( 1.0 ) - R123 );
		vec3 C0 = R12 + Rs;
		I = C0;
		vec3 Cm = Rs - T121;
		for ( int m = 1; m <= 2; ++ m ) {
			Cm *= r123;
			vec3 Sm = 2.0 * evalSensitivity( float( m ) * OPD, float( m ) * phi );
			I += Cm * Sm;
		}
		return max( I, vec3( 0.0 ) );
	}
#endif`,Vb=`#ifdef USE_BUMPMAP
	uniform sampler2D bumpMap;
	uniform float bumpScale;
	vec2 dHdxy_fwd() {
		vec2 dSTdx = dFdx( vBumpMapUv );
		vec2 dSTdy = dFdy( vBumpMapUv );
		float Hll = bumpScale * texture2D( bumpMap, vBumpMapUv ).x;
		float dBx = bumpScale * texture2D( bumpMap, vBumpMapUv + dSTdx ).x - Hll;
		float dBy = bumpScale * texture2D( bumpMap, vBumpMapUv + dSTdy ).x - Hll;
		return vec2( dBx, dBy );
	}
	vec3 perturbNormalArb( vec3 surf_pos, vec3 surf_norm, vec2 dHdxy, float faceDirection ) {
		vec3 vSigmaX = normalize( dFdx( surf_pos.xyz ) );
		vec3 vSigmaY = normalize( dFdy( surf_pos.xyz ) );
		vec3 vN = surf_norm;
		vec3 R1 = cross( vSigmaY, vN );
		vec3 R2 = cross( vN, vSigmaX );
		float fDet = dot( vSigmaX, R1 ) * faceDirection;
		vec3 vGrad = sign( fDet ) * ( dHdxy.x * R1 + dHdxy.y * R2 );
		return normalize( abs( fDet ) * surf_norm - vGrad );
	}
#endif`,kb=`#if NUM_CLIPPING_PLANES > 0
	vec4 plane;
	#ifdef ALPHA_TO_COVERAGE
		float distanceToPlane, distanceGradient;
		float clipOpacity = 1.0;
		#pragma unroll_loop_start
		for ( int i = 0; i < UNION_CLIPPING_PLANES; i ++ ) {
			plane = clippingPlanes[ i ];
			distanceToPlane = - dot( vClipPosition, plane.xyz ) + plane.w;
			distanceGradient = fwidth( distanceToPlane ) / 2.0;
			clipOpacity *= smoothstep( - distanceGradient, distanceGradient, distanceToPlane );
			if ( clipOpacity == 0.0 ) discard;
		}
		#pragma unroll_loop_end
		#if UNION_CLIPPING_PLANES < NUM_CLIPPING_PLANES
			float unionClipOpacity = 1.0;
			#pragma unroll_loop_start
			for ( int i = UNION_CLIPPING_PLANES; i < NUM_CLIPPING_PLANES; i ++ ) {
				plane = clippingPlanes[ i ];
				distanceToPlane = - dot( vClipPosition, plane.xyz ) + plane.w;
				distanceGradient = fwidth( distanceToPlane ) / 2.0;
				unionClipOpacity *= 1.0 - smoothstep( - distanceGradient, distanceGradient, distanceToPlane );
			}
			#pragma unroll_loop_end
			clipOpacity *= 1.0 - unionClipOpacity;
		#endif
		diffuseColor.a *= clipOpacity;
		if ( diffuseColor.a == 0.0 ) discard;
	#else
		#pragma unroll_loop_start
		for ( int i = 0; i < UNION_CLIPPING_PLANES; i ++ ) {
			plane = clippingPlanes[ i ];
			if ( dot( vClipPosition, plane.xyz ) > plane.w ) discard;
		}
		#pragma unroll_loop_end
		#if UNION_CLIPPING_PLANES < NUM_CLIPPING_PLANES
			bool clipped = true;
			#pragma unroll_loop_start
			for ( int i = UNION_CLIPPING_PLANES; i < NUM_CLIPPING_PLANES; i ++ ) {
				plane = clippingPlanes[ i ];
				clipped = ( dot( vClipPosition, plane.xyz ) > plane.w ) && clipped;
			}
			#pragma unroll_loop_end
			if ( clipped ) discard;
		#endif
	#endif
#endif`,Xb=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
	uniform vec4 clippingPlanes[ NUM_CLIPPING_PLANES ];
#endif`,Wb=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
#endif`,qb=`#if NUM_CLIPPING_PLANES > 0
	vClipPosition = - mvPosition.xyz;
#endif`,Yb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	diffuseColor *= vColor;
#endif`,jb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#endif`,Zb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	varying vec4 vColor;
#endif`,Kb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	vColor = vec4( 1.0 );
#endif
#ifdef USE_COLOR_ALPHA
	vColor *= color;
#elif defined( USE_COLOR )
	vColor.rgb *= color;
#endif
#ifdef USE_INSTANCING_COLOR
	vColor.rgb *= instanceColor.rgb;
#endif
#ifdef USE_BATCHING_COLOR
	vColor *= getBatchingColor( getIndirectIndex( gl_DrawID ) );
#endif`,Qb=`#define PI 3.141592653589793
#define PI2 6.283185307179586
#define PI_HALF 1.5707963267948966
#define RECIPROCAL_PI 0.3183098861837907
#define RECIPROCAL_PI2 0.15915494309189535
#define EPSILON 1e-6
#ifndef saturate
#define saturate( a ) clamp( a, 0.0, 1.0 )
#endif
#define whiteComplement( a ) ( 1.0 - saturate( a ) )
float pow2( const in float x ) { return x*x; }
vec3 pow2( const in vec3 x ) { return x*x; }
float pow3( const in float x ) { return x*x*x; }
float pow4( const in float x ) { float x2 = x*x; return x2*x2; }
float max3( const in vec3 v ) { return max( max( v.x, v.y ), v.z ); }
float average( const in vec3 v ) { return dot( v, vec3( 0.3333333 ) ); }
highp float rand( const in vec2 uv ) {
	const highp float a = 12.9898, b = 78.233, c = 43758.5453;
	highp float dt = dot( uv.xy, vec2( a,b ) ), sn = mod( dt, PI );
	return fract( sin( sn ) * c );
}
#ifdef HIGH_PRECISION
	float precisionSafeLength( vec3 v ) { return length( v ); }
#else
	float precisionSafeLength( vec3 v ) {
		float maxComponent = max3( abs( v ) );
		return length( v / maxComponent ) * maxComponent;
	}
#endif
struct IncidentLight {
	vec3 color;
	vec3 direction;
	bool visible;
};
struct ReflectedLight {
	vec3 directDiffuse;
	vec3 directSpecular;
	vec3 indirectDiffuse;
	vec3 indirectSpecular;
};
#ifdef USE_ALPHAHASH
	varying vec3 vPosition;
#endif
vec3 transformDirection( in vec3 dir, in mat4 matrix ) {
	return normalize( ( matrix * vec4( dir, 0.0 ) ).xyz );
}
#define inverseTransformDirection transformDirectionByInverseViewMatrix
vec3 transformNormalByInverseViewMatrix( in vec3 normal, in mat4 viewMatrix ) {
	return normalize( ( vec4( normal, 0.0 ) * viewMatrix ).xyz );
}
vec3 transformDirectionByInverseViewMatrix( in vec3 dir, in mat4 viewMatrix ) {
	return normalize( ( vec4( dir, 0.0 ) * viewMatrix ).xyz );
}
bool isPerspectiveMatrix( mat4 m ) {
	return m[ 2 ][ 3 ] == - 1.0;
}
vec2 equirectUv( in vec3 dir ) {
	float u = atan( dir.z, dir.x ) * RECIPROCAL_PI2 + 0.5;
	float v = asin( clamp( dir.y, - 1.0, 1.0 ) ) * RECIPROCAL_PI + 0.5;
	return vec2( u, v );
}
vec3 BRDF_Lambert( const in vec3 diffuseColor ) {
	return RECIPROCAL_PI * diffuseColor;
}
vec3 F_Schlick( const in vec3 f0, const in float f90, const in float dotVH ) {
	float fresnel = exp2( ( - 5.55473 * dotVH - 6.98316 ) * dotVH );
	return f0 * ( 1.0 - fresnel ) + ( f90 * fresnel );
}
float F_Schlick( const in float f0, const in float f90, const in float dotVH ) {
	float fresnel = exp2( ( - 5.55473 * dotVH - 6.98316 ) * dotVH );
	return f0 * ( 1.0 - fresnel ) + ( f90 * fresnel );
} // validated`,Jb=`#ifdef ENVMAP_TYPE_CUBE_UV
	#define cubeUV_minMipLevel 4.0
	#define cubeUV_minTileSize 16.0
	float getFace( vec3 direction ) {
		vec3 absDirection = abs( direction );
		float face = - 1.0;
		if ( absDirection.x > absDirection.z ) {
			if ( absDirection.x > absDirection.y )
				face = direction.x > 0.0 ? 0.0 : 3.0;
			else
				face = direction.y > 0.0 ? 1.0 : 4.0;
		} else {
			if ( absDirection.z > absDirection.y )
				face = direction.z > 0.0 ? 2.0 : 5.0;
			else
				face = direction.y > 0.0 ? 1.0 : 4.0;
		}
		return face;
	}
	vec2 getUV( vec3 direction, float face ) {
		vec2 uv;
		if ( face == 0.0 ) {
			uv = vec2( direction.z, direction.y ) / abs( direction.x );
		} else if ( face == 1.0 ) {
			uv = vec2( - direction.x, - direction.z ) / abs( direction.y );
		} else if ( face == 2.0 ) {
			uv = vec2( - direction.x, direction.y ) / abs( direction.z );
		} else if ( face == 3.0 ) {
			uv = vec2( - direction.z, direction.y ) / abs( direction.x );
		} else if ( face == 4.0 ) {
			uv = vec2( - direction.x, direction.z ) / abs( direction.y );
		} else {
			uv = vec2( direction.x, direction.y ) / abs( direction.z );
		}
		return 0.5 * ( uv + 1.0 );
	}
	vec3 bilinearCubeUV( sampler2D envMap, vec3 direction, float mipInt ) {
		float face = getFace( direction );
		float filterInt = max( cubeUV_minMipLevel - mipInt, 0.0 );
		mipInt = max( mipInt, cubeUV_minMipLevel );
		float faceSize = exp2( mipInt );
		highp vec2 uv = getUV( direction, face ) * ( faceSize - 2.0 ) + 1.0;
		if ( face > 2.0 ) {
			uv.y += faceSize;
			face -= 3.0;
		}
		uv.x += face * faceSize;
		uv.x += filterInt * 3.0 * cubeUV_minTileSize;
		uv.y += 4.0 * ( exp2( CUBEUV_MAX_MIP ) - faceSize );
		uv.x *= CUBEUV_TEXEL_WIDTH;
		uv.y *= CUBEUV_TEXEL_HEIGHT;
		#ifdef texture2DGradEXT
			return texture2DGradEXT( envMap, uv, vec2( 0.0 ), vec2( 0.0 ) ).rgb;
		#else
			return texture2D( envMap, uv ).rgb;
		#endif
	}
	#define cubeUV_r0 1.0
	#define cubeUV_m0 - 2.0
	#define cubeUV_r1 0.8
	#define cubeUV_m1 - 1.0
	#define cubeUV_r4 0.4
	#define cubeUV_m4 2.0
	#define cubeUV_r5 0.305
	#define cubeUV_m5 3.0
	#define cubeUV_r6 0.21
	#define cubeUV_m6 4.0
	float roughnessToMip( float roughness ) {
		float mip = 0.0;
		if ( roughness >= cubeUV_r1 ) {
			mip = ( cubeUV_r0 - roughness ) * ( cubeUV_m1 - cubeUV_m0 ) / ( cubeUV_r0 - cubeUV_r1 ) + cubeUV_m0;
		} else if ( roughness >= cubeUV_r4 ) {
			mip = ( cubeUV_r1 - roughness ) * ( cubeUV_m4 - cubeUV_m1 ) / ( cubeUV_r1 - cubeUV_r4 ) + cubeUV_m1;
		} else if ( roughness >= cubeUV_r5 ) {
			mip = ( cubeUV_r4 - roughness ) * ( cubeUV_m5 - cubeUV_m4 ) / ( cubeUV_r4 - cubeUV_r5 ) + cubeUV_m4;
		} else if ( roughness >= cubeUV_r6 ) {
			mip = ( cubeUV_r5 - roughness ) * ( cubeUV_m6 - cubeUV_m5 ) / ( cubeUV_r5 - cubeUV_r6 ) + cubeUV_m5;
		} else {
			mip = - 2.0 * log2( 1.16 * roughness );		}
		return mip;
	}
	vec4 textureCubeUV( sampler2D envMap, vec3 sampleDir, float roughness ) {
		float mip = clamp( roughnessToMip( roughness ), cubeUV_m0, CUBEUV_MAX_MIP );
		float mipF = fract( mip );
		float mipInt = floor( mip );
		vec3 color0 = bilinearCubeUV( envMap, sampleDir, mipInt );
		if ( mipF == 0.0 ) {
			return vec4( color0, 1.0 );
		} else {
			vec3 color1 = bilinearCubeUV( envMap, sampleDir, mipInt + 1.0 );
			return vec4( mix( color0, color1, mipF ), 1.0 );
		}
	}
#endif`,$b=`vec3 transformedNormal = objectNormal;
#ifdef USE_TANGENT
	vec3 transformedTangent = objectTangent;
#endif
#ifdef USE_BATCHING
	mat3 bm = mat3( batchingMatrix );
	transformedNormal /= vec3( dot( bm[ 0 ], bm[ 0 ] ), dot( bm[ 1 ], bm[ 1 ] ), dot( bm[ 2 ], bm[ 2 ] ) );
	transformedNormal = bm * transformedNormal;
	#ifdef USE_TANGENT
		transformedTangent = bm * transformedTangent;
	#endif
#endif
#ifdef USE_INSTANCING
	mat3 im = mat3( instanceMatrix );
	transformedNormal /= vec3( dot( im[ 0 ], im[ 0 ] ), dot( im[ 1 ], im[ 1 ] ), dot( im[ 2 ], im[ 2 ] ) );
	transformedNormal = im * transformedNormal;
	#ifdef USE_TANGENT
		transformedTangent = im * transformedTangent;
	#endif
#endif
transformedNormal = normalMatrix * transformedNormal;
#ifdef FLIP_SIDED
	transformedNormal = - transformedNormal;
#endif
#ifdef USE_TANGENT
	transformedTangent = ( modelViewMatrix * vec4( transformedTangent, 0.0 ) ).xyz;
#endif`,tT=`#ifdef USE_DISPLACEMENTMAP
	uniform sampler2D displacementMap;
	uniform float displacementScale;
	uniform float displacementBias;
#endif`,eT=`#ifdef USE_DISPLACEMENTMAP
	transformed += normalize( objectNormal ) * ( texture2D( displacementMap, vDisplacementMapUv ).x * displacementScale + displacementBias );
#endif`,nT=`#ifdef USE_EMISSIVEMAP
	vec4 emissiveColor = texture2D( emissiveMap, vEmissiveMapUv );
	#ifdef DECODE_VIDEO_TEXTURE_EMISSIVE
		emissiveColor = sRGBTransferEOTF( emissiveColor );
	#endif
	totalEmissiveRadiance *= emissiveColor.rgb;
#endif`,iT=`#ifdef USE_EMISSIVEMAP
	uniform sampler2D emissiveMap;
#endif`,aT="gl_FragColor = linearToOutputTexel( gl_FragColor );",sT=`vec4 LinearTransferOETF( in vec4 value ) {
	return value;
}
vec4 sRGBTransferEOTF( in vec4 value ) {
	return vec4( mix( pow( value.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), value.rgb * 0.0773993808, vec3( lessThanEqual( value.rgb, vec3( 0.04045 ) ) ) ), value.a );
}
vec4 sRGBTransferOETF( in vec4 value ) {
	return vec4( mix( pow( value.rgb, vec3( 0.41666 ) ) * 1.055 - vec3( 0.055 ), value.rgb * 12.92, vec3( lessThanEqual( value.rgb, vec3( 0.0031308 ) ) ) ), value.a );
}`,rT=`#ifdef USE_ENVMAP
	#ifdef ENV_WORLDPOS
		vec3 cameraToFrag;
		if ( isOrthographic ) {
			cameraToFrag = normalize( vec3( - viewMatrix[ 0 ][ 2 ], - viewMatrix[ 1 ][ 2 ], - viewMatrix[ 2 ][ 2 ] ) );
		} else {
			cameraToFrag = normalize( vWorldPosition - cameraPosition );
		}
		vec3 worldNormal = transformNormalByInverseViewMatrix( normal, viewMatrix );
		#ifdef ENVMAP_MODE_REFLECTION
			vec3 reflectVec = reflect( cameraToFrag, worldNormal );
		#else
			vec3 reflectVec = refract( cameraToFrag, worldNormal, refractionRatio );
		#endif
	#else
		vec3 reflectVec = vReflect;
	#endif
	#ifdef ENVMAP_TYPE_CUBE
		vec4 envColor = textureCube( envMap, envMapRotation * reflectVec );
		#ifdef ENVMAP_BLENDING_MULTIPLY
			outgoingLight = mix( outgoingLight, outgoingLight * envColor.xyz, specularStrength * reflectivity );
		#elif defined( ENVMAP_BLENDING_MIX )
			outgoingLight = mix( outgoingLight, envColor.xyz, specularStrength * reflectivity );
		#elif defined( ENVMAP_BLENDING_ADD )
			outgoingLight += envColor.xyz * specularStrength * reflectivity;
		#endif
	#endif
#endif`,oT=`#ifdef USE_ENVMAP
	uniform float envMapIntensity;
	uniform mat3 envMapRotation;
	#ifdef ENVMAP_TYPE_CUBE
		uniform samplerCube envMap;
	#else
		uniform sampler2D envMap;
	#endif
#endif`,lT=`#ifdef USE_ENVMAP
	uniform float reflectivity;
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		varying vec3 vWorldPosition;
		uniform float refractionRatio;
	#else
		varying vec3 vReflect;
	#endif
#endif`,cT=`#ifdef USE_ENVMAP
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		
		varying vec3 vWorldPosition;
	#else
		varying vec3 vReflect;
		uniform float refractionRatio;
	#endif
#endif`,uT=`#ifdef USE_ENVMAP
	#ifdef ENV_WORLDPOS
		vWorldPosition = worldPosition.xyz;
	#else
		vec3 cameraToVertex;
		if ( isOrthographic ) {
			cameraToVertex = normalize( vec3( - viewMatrix[ 0 ][ 2 ], - viewMatrix[ 1 ][ 2 ], - viewMatrix[ 2 ][ 2 ] ) );
		} else {
			cameraToVertex = normalize( worldPosition.xyz - cameraPosition );
		}
		vec3 worldNormal = transformNormalByInverseViewMatrix( transformedNormal, viewMatrix );
		#ifdef ENVMAP_MODE_REFLECTION
			vReflect = reflect( cameraToVertex, worldNormal );
		#else
			vReflect = refract( cameraToVertex, worldNormal, refractionRatio );
		#endif
	#endif
#endif`,fT=`#ifdef USE_FOG
	vFogDepth = - mvPosition.z;
#endif`,hT=`#ifdef USE_FOG
	varying float vFogDepth;
#endif`,dT=`#ifdef USE_FOG
	#ifdef FOG_EXP2
		float fogFactor = 1.0 - exp( - fogDensity * fogDensity * vFogDepth * vFogDepth );
	#else
		float fogFactor = smoothstep( fogNear, fogFar, vFogDepth );
	#endif
	gl_FragColor.rgb = mix( gl_FragColor.rgb, fogColor, fogFactor );
#endif`,pT=`#ifdef USE_FOG
	uniform vec3 fogColor;
	varying float vFogDepth;
	#ifdef FOG_EXP2
		uniform float fogDensity;
	#else
		uniform float fogNear;
		uniform float fogFar;
	#endif
#endif`,mT=`#ifdef USE_GRADIENTMAP
	uniform sampler2D gradientMap;
#endif
vec3 getGradientIrradiance( vec3 normal, vec3 lightDirection ) {
	float dotNL = dot( normal, lightDirection );
	vec2 coord = vec2( dotNL * 0.5 + 0.5, 0.0 );
	#ifdef USE_GRADIENTMAP
		return vec3( texture2D( gradientMap, coord ).r );
	#else
		vec2 fw = fwidth( coord ) * 0.5;
		return mix( vec3( 0.7 ), vec3( 1.0 ), smoothstep( 0.7 - fw.x, 0.7 + fw.x, coord.x ) );
	#endif
}`,gT=`#ifdef USE_LIGHTMAP
	uniform sampler2D lightMap;
	uniform float lightMapIntensity;
#endif`,_T=`LambertMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularStrength = specularStrength;`,vT=`varying vec3 vViewPosition;
struct LambertMaterial {
	vec3 diffuseColor;
	float specularStrength;
};
void RE_Direct_Lambert( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in LambertMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Lambert( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in LambertMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_Lambert
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Lambert`,xT=`uniform bool receiveShadow;
uniform vec3 ambientLightColor;
#if defined( USE_LIGHT_PROBES )
	uniform vec3 lightProbe[ 9 ];
#endif
vec3 shGetIrradianceAt( in vec3 normal, in vec3 shCoefficients[ 9 ] ) {
	float x = normal.x, y = normal.y, z = normal.z;
	vec3 result = shCoefficients[ 0 ] * 0.886227;
	result += shCoefficients[ 1 ] * 2.0 * 0.511664 * y;
	result += shCoefficients[ 2 ] * 2.0 * 0.511664 * z;
	result += shCoefficients[ 3 ] * 2.0 * 0.511664 * x;
	result += shCoefficients[ 4 ] * 2.0 * 0.429043 * x * y;
	result += shCoefficients[ 5 ] * 2.0 * 0.429043 * y * z;
	result += shCoefficients[ 6 ] * ( 0.743125 * z * z - 0.247708 );
	result += shCoefficients[ 7 ] * 2.0 * 0.429043 * x * z;
	result += shCoefficients[ 8 ] * 0.429043 * ( x * x - y * y );
	return result;
}
vec3 getLightProbeIrradiance( const in vec3 lightProbe[ 9 ], const in vec3 normal ) {
	vec3 worldNormal = transformNormalByInverseViewMatrix( normal, viewMatrix );
	vec3 irradiance = shGetIrradianceAt( worldNormal, lightProbe );
	return irradiance;
}
vec3 getAmbientLightIrradiance( const in vec3 ambientLightColor ) {
	vec3 irradiance = ambientLightColor;
	return irradiance;
}
float getDistanceAttenuation( const in float lightDistance, const in float cutoffDistance, const in float decayExponent ) {
	float distanceFalloff = 1.0 / max( pow( lightDistance, decayExponent ), 0.01 );
	if ( cutoffDistance > 0.0 ) {
		distanceFalloff *= pow2( saturate( 1.0 - pow4( lightDistance / cutoffDistance ) ) );
	}
	return distanceFalloff;
}
float getSpotAttenuation( const in float coneCosine, const in float penumbraCosine, const in float angleCosine ) {
	return smoothstep( coneCosine, penumbraCosine, angleCosine );
}
#if NUM_DIR_LIGHTS > 0
	struct DirectionalLight {
		vec3 direction;
		vec3 color;
	};
	uniform DirectionalLight directionalLights[ NUM_DIR_LIGHTS ];
	void getDirectionalLightInfo( const in DirectionalLight directionalLight, out IncidentLight light ) {
		light.color = directionalLight.color;
		light.direction = directionalLight.direction;
		light.visible = true;
	}
#endif
#if NUM_POINT_LIGHTS > 0
	struct PointLight {
		vec3 position;
		vec3 color;
		float distance;
		float decay;
	};
	uniform PointLight pointLights[ NUM_POINT_LIGHTS ];
	void getPointLightInfo( const in PointLight pointLight, const in vec3 geometryPosition, out IncidentLight light ) {
		vec3 lVector = pointLight.position - geometryPosition;
		light.direction = normalize( lVector );
		float lightDistance = length( lVector );
		light.color = pointLight.color;
		light.color *= getDistanceAttenuation( lightDistance, pointLight.distance, pointLight.decay );
		light.visible = ( light.color != vec3( 0.0 ) );
	}
#endif
#if NUM_SPOT_LIGHTS > 0
	struct SpotLight {
		vec3 position;
		vec3 direction;
		vec3 color;
		float distance;
		float decay;
		float coneCos;
		float penumbraCos;
	};
	uniform SpotLight spotLights[ NUM_SPOT_LIGHTS ];
	void getSpotLightInfo( const in SpotLight spotLight, const in vec3 geometryPosition, out IncidentLight light ) {
		vec3 lVector = spotLight.position - geometryPosition;
		light.direction = normalize( lVector );
		float angleCos = dot( light.direction, spotLight.direction );
		float spotAttenuation = getSpotAttenuation( spotLight.coneCos, spotLight.penumbraCos, angleCos );
		if ( spotAttenuation > 0.0 ) {
			float lightDistance = length( lVector );
			light.color = spotLight.color * spotAttenuation;
			light.color *= getDistanceAttenuation( lightDistance, spotLight.distance, spotLight.decay );
			light.visible = ( light.color != vec3( 0.0 ) );
		} else {
			light.color = vec3( 0.0 );
			light.visible = false;
		}
	}
#endif
#if NUM_RECT_AREA_LIGHTS > 0
	struct RectAreaLight {
		vec3 color;
		vec3 position;
		vec3 halfWidth;
		vec3 halfHeight;
	};
	uniform sampler2D ltc_1;	uniform sampler2D ltc_2;
	uniform RectAreaLight rectAreaLights[ NUM_RECT_AREA_LIGHTS ];
#endif
#if NUM_HEMI_LIGHTS > 0
	struct HemisphereLight {
		vec3 direction;
		vec3 skyColor;
		vec3 groundColor;
	};
	uniform HemisphereLight hemisphereLights[ NUM_HEMI_LIGHTS ];
	vec3 getHemisphereLightIrradiance( const in HemisphereLight hemiLight, const in vec3 normal ) {
		float dotNL = dot( normal, hemiLight.direction );
		float hemiDiffuseWeight = 0.5 * dotNL + 0.5;
		vec3 irradiance = mix( hemiLight.groundColor, hemiLight.skyColor, hemiDiffuseWeight );
		return irradiance;
	}
#endif
#include <lightprobes_pars_fragment>`,yT=`#ifdef USE_ENVMAP
	vec3 getIBLIrradiance( const in vec3 normal ) {
		#ifdef ENVMAP_TYPE_CUBE_UV
			vec3 worldNormal = transformNormalByInverseViewMatrix( normal, viewMatrix );
			vec4 envMapColor = textureCubeUV( envMap, envMapRotation * worldNormal, 1.0 );
			return PI * envMapColor.rgb * envMapIntensity;
		#else
			return vec3( 0.0 );
		#endif
	}
	vec3 getIBLRadiance( const in vec3 viewDir, const in vec3 normal, const in float roughness ) {
		#ifdef ENVMAP_TYPE_CUBE_UV
			vec3 reflectVec = reflect( - viewDir, normal );
			reflectVec = normalize( mix( reflectVec, normal, pow4( roughness ) ) );
			reflectVec = transformDirectionByInverseViewMatrix( reflectVec, viewMatrix );
			vec4 envMapColor = textureCubeUV( envMap, envMapRotation * reflectVec, roughness );
			return envMapColor.rgb * envMapIntensity;
		#else
			return vec3( 0.0 );
		#endif
	}
	#ifdef USE_ANISOTROPY
		vec3 getIBLAnisotropyRadiance( const in vec3 viewDir, const in vec3 normal, const in float roughness, const in vec3 bitangent, const in float anisotropy ) {
			#ifdef ENVMAP_TYPE_CUBE_UV
				vec3 bentNormal = cross( bitangent, viewDir );
				bentNormal = normalize( cross( bentNormal, bitangent ) );
				bentNormal = normalize( mix( bentNormal, normal, pow2( pow2( 1.0 - anisotropy * ( 1.0 - roughness ) ) ) ) );
				return getIBLRadiance( viewDir, bentNormal, roughness );
			#else
				return vec3( 0.0 );
			#endif
		}
	#endif
#endif`,ST=`ToonMaterial material;
material.diffuseColor = diffuseColor.rgb;`,MT=`varying vec3 vViewPosition;
struct ToonMaterial {
	vec3 diffuseColor;
};
void RE_Direct_Toon( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in ToonMaterial material, inout ReflectedLight reflectedLight ) {
	vec3 irradiance = getGradientIrradiance( geometryNormal, directLight.direction ) * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Toon( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in ToonMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_Toon
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Toon`,ET=`BlinnPhongMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularColor = specular;
material.specularShininess = shininess;
material.specularStrength = specularStrength;`,bT=`varying vec3 vViewPosition;
struct BlinnPhongMaterial {
	vec3 diffuseColor;
	vec3 specularColor;
	float specularShininess;
	float specularStrength;
};
void RE_Direct_BlinnPhong( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in BlinnPhongMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
	reflectedLight.directSpecular += irradiance * BRDF_BlinnPhong( directLight.direction, geometryViewDir, geometryNormal, material.specularColor, material.specularShininess ) * material.specularStrength;
}
void RE_IndirectDiffuse_BlinnPhong( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in BlinnPhongMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_BlinnPhong
#define RE_IndirectDiffuse		RE_IndirectDiffuse_BlinnPhong`,TT=`PhysicalMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.diffuseContribution = diffuseColor.rgb * ( 1.0 - metalnessFactor );
material.metalness = metalnessFactor;
vec3 dxy = max( abs( dFdx( nonPerturbedNormal ) ), abs( dFdy( nonPerturbedNormal ) ) );
float geometryRoughness = max( max( dxy.x, dxy.y ), dxy.z );
material.roughness = max( roughnessFactor, 0.0525 );material.roughness += geometryRoughness;
material.roughness = min( material.roughness, 1.0 );
#ifdef IOR
	material.ior = ior;
	#ifdef USE_SPECULAR
		float specularIntensityFactor = specularIntensity;
		vec3 specularColorFactor = specularColor;
		#ifdef USE_SPECULAR_COLORMAP
			specularColorFactor *= texture2D( specularColorMap, vSpecularColorMapUv ).rgb;
		#endif
		#ifdef USE_SPECULAR_INTENSITYMAP
			specularIntensityFactor *= texture2D( specularIntensityMap, vSpecularIntensityMapUv ).a;
		#endif
		material.specularF90 = mix( specularIntensityFactor, 1.0, metalnessFactor );
	#else
		float specularIntensityFactor = 1.0;
		vec3 specularColorFactor = vec3( 1.0 );
		material.specularF90 = 1.0;
	#endif
	material.specularColor = min( pow2( ( material.ior - 1.0 ) / ( material.ior + 1.0 ) ) * specularColorFactor, vec3( 1.0 ) ) * specularIntensityFactor;
	material.specularColorBlended = mix( material.specularColor, diffuseColor.rgb, metalnessFactor );
#else
	material.specularColor = vec3( 0.04 );
	material.specularColorBlended = mix( material.specularColor, diffuseColor.rgb, metalnessFactor );
	material.specularF90 = 1.0;
#endif
#ifdef USE_CLEARCOAT
	material.clearcoat = clearcoat;
	material.clearcoatRoughness = clearcoatRoughness;
	material.clearcoatF0 = vec3( 0.04 );
	material.clearcoatF90 = 1.0;
	#ifdef USE_CLEARCOATMAP
		material.clearcoat *= texture2D( clearcoatMap, vClearcoatMapUv ).x;
	#endif
	#ifdef USE_CLEARCOAT_ROUGHNESSMAP
		material.clearcoatRoughness *= texture2D( clearcoatRoughnessMap, vClearcoatRoughnessMapUv ).y;
	#endif
	material.clearcoat = saturate( material.clearcoat );	material.clearcoatRoughness = max( material.clearcoatRoughness, 0.0525 );
	material.clearcoatRoughness += geometryRoughness;
	material.clearcoatRoughness = min( material.clearcoatRoughness, 1.0 );
#endif
#ifdef USE_DISPERSION
	material.dispersion = dispersion;
#endif
#ifdef USE_IRIDESCENCE
	material.iridescence = iridescence;
	material.iridescenceIOR = iridescenceIOR;
	#ifdef USE_IRIDESCENCEMAP
		material.iridescence *= texture2D( iridescenceMap, vIridescenceMapUv ).r;
	#endif
	#ifdef USE_IRIDESCENCE_THICKNESSMAP
		material.iridescenceThickness = (iridescenceThicknessMaximum - iridescenceThicknessMinimum) * texture2D( iridescenceThicknessMap, vIridescenceThicknessMapUv ).g + iridescenceThicknessMinimum;
	#else
		material.iridescenceThickness = iridescenceThicknessMaximum;
	#endif
#endif
#ifdef USE_SHEEN
	material.sheenColor = sheenColor;
	#ifdef USE_SHEEN_COLORMAP
		material.sheenColor *= texture2D( sheenColorMap, vSheenColorMapUv ).rgb;
	#endif
	material.sheenRoughness = clamp( sheenRoughness, 0.0001, 1.0 );
	#ifdef USE_SHEEN_ROUGHNESSMAP
		material.sheenRoughness *= texture2D( sheenRoughnessMap, vSheenRoughnessMapUv ).a;
	#endif
#endif
#ifdef USE_ANISOTROPY
	#ifdef USE_ANISOTROPYMAP
		mat2 anisotropyMat = mat2( anisotropyVector.x, anisotropyVector.y, - anisotropyVector.y, anisotropyVector.x );
		vec3 anisotropyPolar = texture2D( anisotropyMap, vAnisotropyMapUv ).rgb;
		vec2 anisotropyV = anisotropyMat * normalize( 2.0 * anisotropyPolar.rg - vec2( 1.0 ) ) * anisotropyPolar.b;
	#else
		vec2 anisotropyV = anisotropyVector;
	#endif
	material.anisotropy = length( anisotropyV );
	if( material.anisotropy == 0.0 ) {
		anisotropyV = vec2( 1.0, 0.0 );
	} else {
		anisotropyV /= material.anisotropy;
		material.anisotropy = saturate( material.anisotropy );
	}
	material.alphaT = mix( pow2( material.roughness ), 1.0, pow2( material.anisotropy ) );
	material.anisotropyT = tbn[ 0 ] * anisotropyV.x + tbn[ 1 ] * anisotropyV.y;
	material.anisotropyB = tbn[ 1 ] * anisotropyV.x - tbn[ 0 ] * anisotropyV.y;
#endif`,AT=`uniform sampler2D dfgLUT;
struct PhysicalMaterial {
	vec3 diffuseColor;
	vec3 diffuseContribution;
	vec3 specularColor;
	vec3 specularColorBlended;
	float roughness;
	float metalness;
	float specularF90;
	float dispersion;
	#ifdef USE_CLEARCOAT
		float clearcoat;
		float clearcoatRoughness;
		vec3 clearcoatF0;
		float clearcoatF90;
	#endif
	#ifdef USE_IRIDESCENCE
		float iridescence;
		float iridescenceIOR;
		float iridescenceThickness;
		vec3 iridescenceFresnel;
		vec3 iridescenceF0;
		vec3 iridescenceFresnelDielectric;
		vec3 iridescenceFresnelMetallic;
	#endif
	#ifdef USE_SHEEN
		vec3 sheenColor;
		float sheenRoughness;
	#endif
	#ifdef IOR
		float ior;
	#endif
	#ifdef USE_TRANSMISSION
		float transmission;
		float transmissionAlpha;
		float thickness;
		float attenuationDistance;
		vec3 attenuationColor;
	#endif
	#ifdef USE_ANISOTROPY
		float anisotropy;
		float alphaT;
		vec3 anisotropyT;
		vec3 anisotropyB;
	#endif
};
vec3 clearcoatSpecularDirect = vec3( 0.0 );
vec3 clearcoatSpecularIndirect = vec3( 0.0 );
vec3 sheenSpecularDirect = vec3( 0.0 );
vec3 sheenSpecularIndirect = vec3(0.0 );
vec3 Schlick_to_F0( const in vec3 f, const in float f90, const in float dotVH ) {
    float x = clamp( 1.0 - dotVH, 0.0, 1.0 );
    float x2 = x * x;
    float x5 = clamp( x * x2 * x2, 0.0, 0.9999 );
    return ( f - vec3( f90 ) * x5 ) / ( 1.0 - x5 );
}
float V_GGX_SmithCorrelated( const in float alpha, const in float dotNL, const in float dotNV ) {
	float a2 = pow2( alpha );
	float gv = dotNL * sqrt( a2 + ( 1.0 - a2 ) * pow2( dotNV ) );
	float gl = dotNV * sqrt( a2 + ( 1.0 - a2 ) * pow2( dotNL ) );
	return 0.5 / max( gv + gl, EPSILON );
}
float D_GGX( const in float alpha, const in float dotNH ) {
	float a2 = pow2( alpha );
	float denom = pow2( dotNH ) * ( a2 - 1.0 ) + 1.0;
	return RECIPROCAL_PI * a2 / pow2( denom );
}
#ifdef USE_ANISOTROPY
	float V_GGX_SmithCorrelated_Anisotropic( const in float alphaT, const in float alphaB, const in float dotTV, const in float dotBV, const in float dotTL, const in float dotBL, const in float dotNV, const in float dotNL ) {
		float gv = dotNL * length( vec3( alphaT * dotTV, alphaB * dotBV, dotNV ) );
		float gl = dotNV * length( vec3( alphaT * dotTL, alphaB * dotBL, dotNL ) );
		return 0.5 / max( gv + gl, EPSILON );
	}
	float D_GGX_Anisotropic( const in float alphaT, const in float alphaB, const in float dotNH, const in float dotTH, const in float dotBH ) {
		float a2 = alphaT * alphaB;
		highp vec3 v = vec3( alphaB * dotTH, alphaT * dotBH, a2 * dotNH );
		highp float v2 = dot( v, v );
		float w2 = a2 / v2;
		return RECIPROCAL_PI * a2 * pow2 ( w2 );
	}
#endif
#ifdef USE_CLEARCOAT
	vec3 BRDF_GGX_Clearcoat( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material) {
		vec3 f0 = material.clearcoatF0;
		float f90 = material.clearcoatF90;
		float roughness = material.clearcoatRoughness;
		float alpha = pow2( roughness );
		vec3 halfDir = normalize( lightDir + viewDir );
		float dotNL = saturate( dot( normal, lightDir ) );
		float dotNV = saturate( dot( normal, viewDir ) );
		float dotNH = saturate( dot( normal, halfDir ) );
		float dotVH = saturate( dot( viewDir, halfDir ) );
		vec3 F = F_Schlick( f0, f90, dotVH );
		float V = V_GGX_SmithCorrelated( alpha, dotNL, dotNV );
		float D = D_GGX( alpha, dotNH );
		return F * ( V * D );
	}
#endif
vec3 BRDF_GGX( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material ) {
	vec3 f0 = material.specularColorBlended;
	float f90 = material.specularF90;
	float roughness = material.roughness;
	float alpha = pow2( roughness );
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	float dotNH = saturate( dot( normal, halfDir ) );
	float dotVH = saturate( dot( viewDir, halfDir ) );
	vec3 F = F_Schlick( f0, f90, dotVH );
	#ifdef USE_IRIDESCENCE
		F = mix( F, material.iridescenceFresnel, material.iridescence );
	#endif
	#ifdef USE_ANISOTROPY
		float dotTL = dot( material.anisotropyT, lightDir );
		float dotTV = dot( material.anisotropyT, viewDir );
		float dotTH = dot( material.anisotropyT, halfDir );
		float dotBL = dot( material.anisotropyB, lightDir );
		float dotBV = dot( material.anisotropyB, viewDir );
		float dotBH = dot( material.anisotropyB, halfDir );
		float V = V_GGX_SmithCorrelated_Anisotropic( material.alphaT, alpha, dotTV, dotBV, dotTL, dotBL, dotNV, dotNL );
		float D = D_GGX_Anisotropic( material.alphaT, alpha, dotNH, dotTH, dotBH );
	#else
		float V = V_GGX_SmithCorrelated( alpha, dotNL, dotNV );
		float D = D_GGX( alpha, dotNH );
	#endif
	return F * ( V * D );
}
vec2 LTC_Uv( const in vec3 N, const in vec3 V, const in float roughness ) {
	const float LUT_SIZE = 64.0;
	const float LUT_SCALE = ( LUT_SIZE - 1.0 ) / LUT_SIZE;
	const float LUT_BIAS = 0.5 / LUT_SIZE;
	float dotNV = saturate( dot( N, V ) );
	vec2 uv = vec2( roughness, sqrt( 1.0 - dotNV ) );
	uv = uv * LUT_SCALE + LUT_BIAS;
	return uv;
}
float LTC_ClippedSphereFormFactor( const in vec3 f ) {
	float l = length( f );
	return max( ( l * l + f.z ) / ( l + 1.0 ), 0.0 );
}
vec3 LTC_EdgeVectorFormFactor( const in vec3 v1, const in vec3 v2 ) {
	float x = dot( v1, v2 );
	float y = abs( x );
	float a = 0.8543985 + ( 0.4965155 + 0.0145206 * y ) * y;
	float b = 3.4175940 + ( 4.1616724 + y ) * y;
	float v = a / b;
	float theta_sintheta = ( x > 0.0 ) ? v : 0.5 * inversesqrt( max( 1.0 - x * x, 1e-7 ) ) - v;
	return cross( v1, v2 ) * theta_sintheta;
}
vec3 LTC_Evaluate( const in vec3 N, const in vec3 V, const in vec3 P, const in mat3 mInv, const in vec3 rectCoords[ 4 ] ) {
	vec3 v1 = rectCoords[ 1 ] - rectCoords[ 0 ];
	vec3 v2 = rectCoords[ 3 ] - rectCoords[ 0 ];
	vec3 lightNormal = cross( v1, v2 );
	if( dot( lightNormal, P - rectCoords[ 0 ] ) < 0.0 ) return vec3( 0.0 );
	vec3 T1, T2;
	T1 = normalize( V - N * dot( V, N ) );
	T2 = - cross( N, T1 );
	mat3 mat = mInv * transpose( mat3( T1, T2, N ) );
	vec3 coords[ 4 ];
	coords[ 0 ] = mat * ( rectCoords[ 0 ] - P );
	coords[ 1 ] = mat * ( rectCoords[ 1 ] - P );
	coords[ 2 ] = mat * ( rectCoords[ 2 ] - P );
	coords[ 3 ] = mat * ( rectCoords[ 3 ] - P );
	coords[ 0 ] = normalize( coords[ 0 ] );
	coords[ 1 ] = normalize( coords[ 1 ] );
	coords[ 2 ] = normalize( coords[ 2 ] );
	coords[ 3 ] = normalize( coords[ 3 ] );
	vec3 vectorFormFactor = vec3( 0.0 );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 0 ], coords[ 1 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 1 ], coords[ 2 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 2 ], coords[ 3 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 3 ], coords[ 0 ] );
	float result = LTC_ClippedSphereFormFactor( vectorFormFactor );
	return vec3( result );
}
#if defined( USE_SHEEN )
float D_Charlie( float roughness, float dotNH ) {
	float alpha = pow2( roughness );
	float invAlpha = 1.0 / alpha;
	float cos2h = dotNH * dotNH;
	float sin2h = max( 1.0 - cos2h, 0.0078125 );
	return ( 2.0 + invAlpha ) * pow( sin2h, invAlpha * 0.5 ) / ( 2.0 * PI );
}
float V_Neubelt( float dotNV, float dotNL ) {
	return saturate( 1.0 / ( 4.0 * ( dotNL + dotNV - dotNL * dotNV ) ) );
}
vec3 BRDF_Sheen( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, vec3 sheenColor, const in float sheenRoughness ) {
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	float dotNH = saturate( dot( normal, halfDir ) );
	float D = D_Charlie( sheenRoughness, dotNH );
	float V = V_Neubelt( dotNV, dotNL );
	return sheenColor * ( D * V );
}
#endif
float IBLSheenBRDF( const in vec3 normal, const in vec3 viewDir, const in float roughness ) {
	float dotNV = saturate( dot( normal, viewDir ) );
	float r2 = roughness * roughness;
	float rInv = 1.0 / ( roughness + 0.1 );
	float a = -1.9362 + 1.0678 * roughness + 0.4573 * r2 - 0.8469 * rInv;
	float b = -0.6014 + 0.5538 * roughness - 0.4670 * r2 - 0.1255 * rInv;
	float DG = exp( a * dotNV + b );
	return saturate( DG );
}
vec3 EnvironmentBRDF( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float roughness ) {
	float dotNV = saturate( dot( normal, viewDir ) );
	vec2 fab = texture2D( dfgLUT, vec2( roughness, dotNV ) ).rg;
	return specularColor * fab.x + specularF90 * fab.y;
}
#ifdef USE_IRIDESCENCE
void computeMultiscatteringIridescence( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float iridescence, const in vec3 iridescenceF0, const in float roughness, inout vec3 singleScatter, inout vec3 multiScatter ) {
#else
void computeMultiscattering( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float roughness, inout vec3 singleScatter, inout vec3 multiScatter ) {
#endif
	float dotNV = saturate( dot( normal, viewDir ) );
	vec2 fab = texture2D( dfgLUT, vec2( roughness, dotNV ) ).rg;
	#ifdef USE_IRIDESCENCE
		vec3 Fr = mix( specularColor, iridescenceF0, iridescence );
	#else
		vec3 Fr = specularColor;
	#endif
	vec3 FssEss = Fr * fab.x + specularF90 * fab.y;
	float Ess = fab.x + fab.y;
	float Ems = 1.0 - Ess;
	vec3 Favg = Fr + ( 1.0 - Fr ) * 0.047619;	vec3 Fms = FssEss * Favg / ( 1.0 - Ems * Favg );
	singleScatter += FssEss;
	multiScatter += Fms * Ems;
}
vec3 BRDF_GGX_Multiscatter( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material ) {
	vec3 singleScatter = BRDF_GGX( lightDir, viewDir, normal, material );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	vec2 dfgV = texture2D( dfgLUT, vec2( material.roughness, dotNV ) ).rg;
	vec2 dfgL = texture2D( dfgLUT, vec2( material.roughness, dotNL ) ).rg;
	vec3 FssEss_V = material.specularColorBlended * dfgV.x + material.specularF90 * dfgV.y;
	vec3 FssEss_L = material.specularColorBlended * dfgL.x + material.specularF90 * dfgL.y;
	float Ess_V = dfgV.x + dfgV.y;
	float Ess_L = dfgL.x + dfgL.y;
	float Ems_V = 1.0 - Ess_V;
	float Ems_L = 1.0 - Ess_L;
	vec3 Favg = material.specularColorBlended + ( 1.0 - material.specularColorBlended ) * 0.047619;
	vec3 Fms = FssEss_V * FssEss_L * Favg / ( 1.0 - Ems_V * Ems_L * Favg + EPSILON );
	float compensationFactor = Ems_V * Ems_L;
	vec3 multiScatter = Fms * compensationFactor;
	return singleScatter + multiScatter;
}
#if NUM_RECT_AREA_LIGHTS > 0
	void RE_Direct_RectArea_Physical( const in RectAreaLight rectAreaLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
		vec3 normal = geometryNormal;
		vec3 viewDir = geometryViewDir;
		vec3 position = geometryPosition;
		vec3 lightPos = rectAreaLight.position;
		vec3 halfWidth = rectAreaLight.halfWidth;
		vec3 halfHeight = rectAreaLight.halfHeight;
		vec3 lightColor = rectAreaLight.color;
		float roughness = material.roughness;
		vec3 rectCoords[ 4 ];
		rectCoords[ 0 ] = lightPos + halfWidth - halfHeight;		rectCoords[ 1 ] = lightPos - halfWidth - halfHeight;
		rectCoords[ 2 ] = lightPos - halfWidth + halfHeight;
		rectCoords[ 3 ] = lightPos + halfWidth + halfHeight;
		vec2 uv = LTC_Uv( normal, viewDir, roughness );
		vec4 t1 = texture2D( ltc_1, uv );
		vec4 t2 = texture2D( ltc_2, uv );
		mat3 mInv = mat3(
			vec3( t1.x, 0, t1.y ),
			vec3(    0, 1,    0 ),
			vec3( t1.z, 0, t1.w )
		);
		vec3 fresnel = ( material.specularColorBlended * t2.x + ( material.specularF90 - material.specularColorBlended ) * t2.y );
		reflectedLight.directSpecular += lightColor * fresnel * LTC_Evaluate( normal, viewDir, position, mInv, rectCoords );
		reflectedLight.directDiffuse += lightColor * material.diffuseContribution * LTC_Evaluate( normal, viewDir, position, mat3( 1.0 ), rectCoords );
		#ifdef USE_CLEARCOAT
			vec3 Ncc = geometryClearcoatNormal;
			vec2 uvClearcoat = LTC_Uv( Ncc, viewDir, material.clearcoatRoughness );
			vec4 t1Clearcoat = texture2D( ltc_1, uvClearcoat );
			vec4 t2Clearcoat = texture2D( ltc_2, uvClearcoat );
			mat3 mInvClearcoat = mat3(
				vec3( t1Clearcoat.x, 0, t1Clearcoat.y ),
				vec3(             0, 1,             0 ),
				vec3( t1Clearcoat.z, 0, t1Clearcoat.w )
			);
			vec3 fresnelClearcoat = material.clearcoatF0 * t2Clearcoat.x + ( material.clearcoatF90 - material.clearcoatF0 ) * t2Clearcoat.y;
			clearcoatSpecularDirect += lightColor * fresnelClearcoat * LTC_Evaluate( Ncc, viewDir, position, mInvClearcoat, rectCoords );
		#endif
	}
#endif
void RE_Direct_Physical( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	#ifdef USE_CLEARCOAT
		float dotNLcc = saturate( dot( geometryClearcoatNormal, directLight.direction ) );
		vec3 ccIrradiance = dotNLcc * directLight.color;
		clearcoatSpecularDirect += ccIrradiance * BRDF_GGX_Clearcoat( directLight.direction, geometryViewDir, geometryClearcoatNormal, material );
	#endif
	#ifdef USE_SHEEN
 
 		sheenSpecularDirect += irradiance * BRDF_Sheen( directLight.direction, geometryViewDir, geometryNormal, material.sheenColor, material.sheenRoughness );
 
 		float sheenAlbedoV = IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
 		float sheenAlbedoL = IBLSheenBRDF( geometryNormal, directLight.direction, material.sheenRoughness );
 
 		float sheenEnergyComp = 1.0 - max3( material.sheenColor ) * max( sheenAlbedoV, sheenAlbedoL );
 
 		irradiance *= sheenEnergyComp;
 
 	#endif
	reflectedLight.directSpecular += irradiance * BRDF_GGX_Multiscatter( directLight.direction, geometryViewDir, geometryNormal, material );
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseContribution );
}
void RE_IndirectDiffuse_Physical( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
	vec3 diffuse = irradiance * BRDF_Lambert( material.diffuseContribution );
	#ifdef USE_SHEEN
		float sheenAlbedo = IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
		float sheenEnergyComp = 1.0 - max3( material.sheenColor ) * sheenAlbedo;
		diffuse *= sheenEnergyComp;
	#endif
	reflectedLight.indirectDiffuse += diffuse;
}
void RE_IndirectSpecular_Physical( const in vec3 radiance, const in vec3 irradiance, const in vec3 clearcoatRadiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight) {
	#ifdef USE_CLEARCOAT
		clearcoatSpecularIndirect += clearcoatRadiance * EnvironmentBRDF( geometryClearcoatNormal, geometryViewDir, material.clearcoatF0, material.clearcoatF90, material.clearcoatRoughness );
	#endif
	#ifdef USE_SHEEN
		sheenSpecularIndirect += irradiance * material.sheenColor * IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness ) * RECIPROCAL_PI;
 	#endif
	vec3 singleScatteringDielectric = vec3( 0.0 );
	vec3 multiScatteringDielectric = vec3( 0.0 );
	vec3 singleScatteringMetallic = vec3( 0.0 );
	vec3 multiScatteringMetallic = vec3( 0.0 );
	#ifdef USE_IRIDESCENCE
		computeMultiscatteringIridescence( geometryNormal, geometryViewDir, material.specularColor, material.specularF90, material.iridescence, material.iridescenceFresnelDielectric, material.roughness, singleScatteringDielectric, multiScatteringDielectric );
		computeMultiscatteringIridescence( geometryNormal, geometryViewDir, material.diffuseColor, material.specularF90, material.iridescence, material.iridescenceFresnelMetallic, material.roughness, singleScatteringMetallic, multiScatteringMetallic );
	#else
		computeMultiscattering( geometryNormal, geometryViewDir, material.specularColor, material.specularF90, material.roughness, singleScatteringDielectric, multiScatteringDielectric );
		computeMultiscattering( geometryNormal, geometryViewDir, material.diffuseColor, material.specularF90, material.roughness, singleScatteringMetallic, multiScatteringMetallic );
	#endif
	vec3 singleScattering = mix( singleScatteringDielectric, singleScatteringMetallic, material.metalness );
	vec3 multiScattering = mix( multiScatteringDielectric, multiScatteringMetallic, material.metalness );
	vec3 totalScatteringDielectric = singleScatteringDielectric + multiScatteringDielectric;
	vec3 diffuse = material.diffuseContribution * ( 1.0 - totalScatteringDielectric );
	vec3 cosineWeightedIrradiance = irradiance * RECIPROCAL_PI;
	vec3 indirectSpecular = radiance * singleScattering;
	indirectSpecular += multiScattering * cosineWeightedIrradiance;
	vec3 indirectDiffuse = diffuse * cosineWeightedIrradiance;
	#ifdef USE_SHEEN
		float sheenAlbedo = IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
		float sheenEnergyComp = 1.0 - max3( material.sheenColor ) * sheenAlbedo;
		indirectSpecular *= sheenEnergyComp;
		indirectDiffuse *= sheenEnergyComp;
	#endif
	reflectedLight.indirectSpecular += indirectSpecular;
	reflectedLight.indirectDiffuse += indirectDiffuse;
}
#define RE_Direct				RE_Direct_Physical
#define RE_Direct_RectArea		RE_Direct_RectArea_Physical
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Physical
#define RE_IndirectSpecular		RE_IndirectSpecular_Physical
float computeSpecularOcclusion( const in float dotNV, const in float ambientOcclusion, const in float roughness ) {
	return saturate( pow( dotNV + ambientOcclusion, exp2( - 16.0 * roughness - 1.0 ) ) - 1.0 + ambientOcclusion );
}`,RT=`
vec3 geometryPosition = - vViewPosition;
vec3 geometryNormal = normal;
vec3 geometryViewDir = ( isOrthographic ) ? vec3( 0, 0, 1 ) : normalize( vViewPosition );
vec3 geometryClearcoatNormal = vec3( 0.0 );
#ifdef USE_CLEARCOAT
	geometryClearcoatNormal = clearcoatNormal;
#endif
#ifdef USE_IRIDESCENCE
	float dotNVi = saturate( dot( normal, geometryViewDir ) );
	if ( material.iridescenceThickness == 0.0 ) {
		material.iridescence = 0.0;
	} else {
		material.iridescence = saturate( material.iridescence );
	}
	if ( material.iridescence > 0.0 ) {
		material.iridescenceFresnelDielectric = evalIridescence( 1.0, material.iridescenceIOR, dotNVi, material.iridescenceThickness, material.specularColor );
		material.iridescenceFresnelMetallic = evalIridescence( 1.0, material.iridescenceIOR, dotNVi, material.iridescenceThickness, material.diffuseColor );
		material.iridescenceFresnel = mix( material.iridescenceFresnelDielectric, material.iridescenceFresnelMetallic, material.metalness );
		material.iridescenceF0 = Schlick_to_F0( material.iridescenceFresnel, 1.0, dotNVi );
	}
#endif
IncidentLight directLight;
#if ( NUM_POINT_LIGHTS > 0 ) && defined( RE_Direct )
	PointLight pointLight;
	#if defined( USE_SHADOWMAP ) && NUM_POINT_LIGHT_SHADOWS > 0
	PointLightShadow pointLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_POINT_LIGHTS; i ++ ) {
		pointLight = pointLights[ i ];
		getPointLightInfo( pointLight, geometryPosition, directLight );
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_POINT_LIGHT_SHADOWS ) && ( defined( SHADOWMAP_TYPE_PCF ) || defined( SHADOWMAP_TYPE_BASIC ) )
		pointLightShadow = pointLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getPointShadow( pointShadowMap[ i ], pointLightShadow.shadowMapSize, pointLightShadow.shadowIntensity, pointLightShadow.shadowBias, pointLightShadow.shadowRadius, vPointShadowCoord[ i ], pointLightShadow.shadowCameraNear, pointLightShadow.shadowCameraFar ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_SPOT_LIGHTS > 0 ) && defined( RE_Direct )
	SpotLight spotLight;
	vec4 spotColor;
	vec3 spotLightCoord;
	bool inSpotLightMap;
	#if defined( USE_SHADOWMAP ) && NUM_SPOT_LIGHT_SHADOWS > 0
	SpotLightShadow spotLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHTS; i ++ ) {
		spotLight = spotLights[ i ];
		getSpotLightInfo( spotLight, geometryPosition, directLight );
		#if ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS )
		#define SPOT_LIGHT_MAP_INDEX UNROLLED_LOOP_INDEX
		#elif ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
		#define SPOT_LIGHT_MAP_INDEX NUM_SPOT_LIGHT_MAPS
		#else
		#define SPOT_LIGHT_MAP_INDEX ( UNROLLED_LOOP_INDEX - NUM_SPOT_LIGHT_SHADOWS + NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS )
		#endif
		#if ( SPOT_LIGHT_MAP_INDEX < NUM_SPOT_LIGHT_MAPS )
			spotLightCoord = vSpotLightCoord[ i ].xyz / vSpotLightCoord[ i ].w;
			inSpotLightMap = all( lessThan( abs( spotLightCoord * 2. - 1. ), vec3( 1.0 ) ) );
			spotColor = texture2D( spotLightMap[ SPOT_LIGHT_MAP_INDEX ], spotLightCoord.xy );
			directLight.color = inSpotLightMap ? directLight.color * spotColor.rgb : directLight.color;
		#endif
		#undef SPOT_LIGHT_MAP_INDEX
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
		spotLightShadow = spotLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getShadow( spotShadowMap[ i ], spotLightShadow.shadowMapSize, spotLightShadow.shadowIntensity, spotLightShadow.shadowBias, spotLightShadow.shadowRadius, vSpotLightCoord[ i ] ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_DIR_LIGHTS > 0 ) && defined( RE_Direct )
	DirectionalLight directionalLight;
	#if defined( USE_SHADOWMAP ) && NUM_DIR_LIGHT_SHADOWS > 0
	DirectionalLightShadow directionalLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_DIR_LIGHTS; i ++ ) {
		directionalLight = directionalLights[ i ];
		getDirectionalLightInfo( directionalLight, directLight );
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_DIR_LIGHT_SHADOWS )
		directionalLightShadow = directionalLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getShadow( directionalShadowMap[ i ], directionalLightShadow.shadowMapSize, directionalLightShadow.shadowIntensity, directionalLightShadow.shadowBias, directionalLightShadow.shadowRadius, vDirectionalShadowCoord[ i ] ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_RECT_AREA_LIGHTS > 0 ) && defined( RE_Direct_RectArea )
	RectAreaLight rectAreaLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_RECT_AREA_LIGHTS; i ++ ) {
		rectAreaLight = rectAreaLights[ i ];
		RE_Direct_RectArea( rectAreaLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if defined( RE_IndirectDiffuse )
	vec3 iblIrradiance = vec3( 0.0 );
	vec3 irradiance = getAmbientLightIrradiance( ambientLightColor );
	#if defined( USE_LIGHT_PROBES )
		irradiance += getLightProbeIrradiance( lightProbe, geometryNormal );
	#endif
	#if ( NUM_HEMI_LIGHTS > 0 )
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_HEMI_LIGHTS; i ++ ) {
			irradiance += getHemisphereLightIrradiance( hemisphereLights[ i ], geometryNormal );
		}
		#pragma unroll_loop_end
	#endif
	#ifdef USE_LIGHT_PROBES_GRID
		vec3 probeWorldPos = ( ( vec4( geometryPosition, 1.0 ) - viewMatrix[ 3 ] ) * viewMatrix ).xyz;
		vec3 probeWorldNormal = transformNormalByInverseViewMatrix( geometryNormal, viewMatrix );
		irradiance += getLightProbeGridIrradiance( probeWorldPos, probeWorldNormal );
	#endif
#endif
#if defined( RE_IndirectSpecular )
	vec3 radiance = vec3( 0.0 );
	vec3 clearcoatRadiance = vec3( 0.0 );
#endif`,wT=`#if defined( RE_IndirectDiffuse )
	#ifdef USE_LIGHTMAP
		vec4 lightMapTexel = texture2D( lightMap, vLightMapUv );
		vec3 lightMapIrradiance = lightMapTexel.rgb * lightMapIntensity;
		irradiance += lightMapIrradiance;
	#endif
	#if defined( USE_ENVMAP ) && defined( ENVMAP_TYPE_CUBE_UV )
		#if defined( STANDARD ) || defined( LAMBERT ) || defined( PHONG )
			iblIrradiance += getIBLIrradiance( geometryNormal );
		#endif
	#endif
#endif
#if defined( USE_ENVMAP ) && defined( RE_IndirectSpecular )
	#ifdef USE_ANISOTROPY
		radiance += getIBLAnisotropyRadiance( geometryViewDir, geometryNormal, material.roughness, material.anisotropyB, material.anisotropy );
	#else
		radiance += getIBLRadiance( geometryViewDir, geometryNormal, material.roughness );
	#endif
	#ifdef USE_CLEARCOAT
		clearcoatRadiance += getIBLRadiance( geometryViewDir, geometryClearcoatNormal, material.clearcoatRoughness );
	#endif
#endif`,CT=`#if defined( RE_IndirectDiffuse )
	#if defined( LAMBERT ) || defined( PHONG )
		irradiance += iblIrradiance;
	#endif
	RE_IndirectDiffuse( irradiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif
#if defined( RE_IndirectSpecular )
	RE_IndirectSpecular( radiance, iblIrradiance, clearcoatRadiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif`,DT=`#ifdef USE_LIGHT_PROBES_GRID
uniform highp sampler3D probesSH;
uniform vec3 probesMin;
uniform vec3 probesMax;
uniform vec3 probesResolution;
vec3 getLightProbeGridIrradiance( vec3 worldPos, vec3 worldNormal ) {
	vec3 res = probesResolution;
	vec3 gridRange = probesMax - probesMin;
	vec3 resMinusOne = res - 1.0;
	vec3 probeSpacing = gridRange / resMinusOne;
	vec3 samplePos = worldPos + worldNormal * probeSpacing * 0.5;
	vec3 uvw = clamp( ( samplePos - probesMin ) / gridRange, 0.0, 1.0 );
	uvw = uvw * resMinusOne / res + 0.5 / res;
	float nz          = res.z;
	float paddedSlices = nz + 2.0;
	float atlasDepth  = 7.0 * paddedSlices;
	float uvZBase     = uvw.z * nz + 1.0;
	vec4 s0 = texture( probesSH, vec3( uvw.xy, ( uvZBase                       ) / atlasDepth ) );
	vec4 s1 = texture( probesSH, vec3( uvw.xy, ( uvZBase +       paddedSlices   ) / atlasDepth ) );
	vec4 s2 = texture( probesSH, vec3( uvw.xy, ( uvZBase + 2.0 * paddedSlices   ) / atlasDepth ) );
	vec4 s3 = texture( probesSH, vec3( uvw.xy, ( uvZBase + 3.0 * paddedSlices   ) / atlasDepth ) );
	vec4 s4 = texture( probesSH, vec3( uvw.xy, ( uvZBase + 4.0 * paddedSlices   ) / atlasDepth ) );
	vec4 s5 = texture( probesSH, vec3( uvw.xy, ( uvZBase + 5.0 * paddedSlices   ) / atlasDepth ) );
	vec4 s6 = texture( probesSH, vec3( uvw.xy, ( uvZBase + 6.0 * paddedSlices   ) / atlasDepth ) );
	vec3 c0 = s0.xyz;
	vec3 c1 = vec3( s0.w, s1.xy );
	vec3 c2 = vec3( s1.zw, s2.x );
	vec3 c3 = s2.yzw;
	vec3 c4 = s3.xyz;
	vec3 c5 = vec3( s3.w, s4.xy );
	vec3 c6 = vec3( s4.zw, s5.x );
	vec3 c7 = s5.yzw;
	vec3 c8 = s6.xyz;
	float x = worldNormal.x, y = worldNormal.y, z = worldNormal.z;
	vec3 result = c0 * 0.886227;
	result += c1 * 2.0 * 0.511664 * y;
	result += c2 * 2.0 * 0.511664 * z;
	result += c3 * 2.0 * 0.511664 * x;
	result += c4 * 2.0 * 0.429043 * x * y;
	result += c5 * 2.0 * 0.429043 * y * z;
	result += c6 * ( 0.743125 * z * z - 0.247708 );
	result += c7 * 2.0 * 0.429043 * x * z;
	result += c8 * 0.429043 * ( x * x - y * y );
	return max( result, vec3( 0.0 ) );
}
#endif`,UT=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	gl_FragDepth = vIsPerspective == 0.0 ? gl_FragCoord.z : log2( vFragDepth ) * logDepthBufFC * 0.5;
#endif`,NT=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	uniform float logDepthBufFC;
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,LT=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,OT=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	vFragDepth = 1.0 + gl_Position.w;
	vIsPerspective = float( isPerspectiveMatrix( projectionMatrix ) );
#endif`,PT=`#ifdef USE_MAP
	vec4 sampledDiffuseColor = texture2D( map, vMapUv );
	#ifdef DECODE_VIDEO_TEXTURE
		sampledDiffuseColor = sRGBTransferEOTF( sampledDiffuseColor );
	#endif
	diffuseColor *= sampledDiffuseColor;
#endif`,zT=`#ifdef USE_MAP
	uniform sampler2D map;
#endif`,BT=`#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
	#if defined( USE_POINTS_UV )
		vec2 uv = vUv;
	#else
		vec2 uv = ( uvTransform * vec3( gl_PointCoord.x, 1.0 - gl_PointCoord.y, 1 ) ).xy;
	#endif
#endif
#ifdef USE_MAP
	diffuseColor *= texture2D( map, uv );
#endif
#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, uv ).g;
#endif`,IT=`#if defined( USE_POINTS_UV )
	varying vec2 vUv;
#else
	#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
		uniform mat3 uvTransform;
	#endif
#endif
#ifdef USE_MAP
	uniform sampler2D map;
#endif
#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,FT=`float metalnessFactor = metalness;
#ifdef USE_METALNESSMAP
	vec4 texelMetalness = texture2D( metalnessMap, vMetalnessMapUv );
	metalnessFactor *= texelMetalness.b;
#endif`,HT=`#ifdef USE_METALNESSMAP
	uniform sampler2D metalnessMap;
#endif`,GT=`#ifdef USE_INSTANCING_MORPH
	float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	float morphTargetBaseInfluence = texelFetch( morphTexture, ivec2( 0, gl_InstanceID ), 0 ).r;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		morphTargetInfluences[i] =  texelFetch( morphTexture, ivec2( i + 1, gl_InstanceID ), 0 ).r;
	}
#endif`,VT=`#if defined( USE_MORPHCOLORS )
	vColor *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		#if defined( USE_COLOR_ALPHA )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ) * morphTargetInfluences[ i ];
		#elif defined( USE_COLOR )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ).rgb * morphTargetInfluences[ i ];
		#endif
	}
#endif`,kT=`#ifdef USE_MORPHNORMALS
	objectNormal *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) objectNormal += getMorph( gl_VertexID, i, 1 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,XT=`#ifdef USE_MORPHTARGETS
	#ifndef USE_INSTANCING_MORPH
		uniform float morphTargetBaseInfluence;
		uniform float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	#endif
	uniform sampler2DArray morphTargetsTexture;
	uniform ivec2 morphTargetsTextureSize;
	vec4 getMorph( const in int vertexIndex, const in int morphTargetIndex, const in int offset ) {
		int texelIndex = vertexIndex * MORPHTARGETS_TEXTURE_STRIDE + offset;
		int y = texelIndex / morphTargetsTextureSize.x;
		int x = texelIndex - y * morphTargetsTextureSize.x;
		ivec3 morphUV = ivec3( x, y, morphTargetIndex );
		return texelFetch( morphTargetsTexture, morphUV, 0 );
	}
#endif`,WT=`#ifdef USE_MORPHTARGETS
	transformed *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) transformed += getMorph( gl_VertexID, i, 0 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,qT=`float faceDirection = gl_FrontFacing ? 1.0 : - 1.0;
#ifdef FLAT_SHADED
	vec3 fdx = dFdx( vViewPosition );
	vec3 fdy = dFdy( vViewPosition );
	vec3 normal = normalize( cross( fdx, fdy ) );
#else
	vec3 normal = normalize( vNormal );
	#ifdef DOUBLE_SIDED
		normal *= faceDirection;
	#endif
#endif
#if defined( USE_NORMALMAP_TANGENTSPACE ) || defined( USE_CLEARCOAT_NORMALMAP ) || defined( USE_ANISOTROPY )
	#ifdef USE_TANGENT
		mat3 tbn = mat3( normalize( vTangent ), normalize( vBitangent ), normal );
	#else
		mat3 tbn = getTangentFrame( - vViewPosition, normal,
		#if defined( USE_NORMALMAP )
			vNormalMapUv
		#elif defined( USE_CLEARCOAT_NORMALMAP )
			vClearcoatNormalMapUv
		#else
			vUv
		#endif
		);
	#endif
	#ifdef DOUBLE_SIDED
		tbn[0] *= faceDirection;
		tbn[1] *= faceDirection;
	#endif
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	#ifdef USE_TANGENT
		mat3 tbn2 = mat3( normalize( vTangent ), normalize( vBitangent ), normal );
	#else
		mat3 tbn2 = getTangentFrame( - vViewPosition, normal, vClearcoatNormalMapUv );
	#endif
	#ifdef DOUBLE_SIDED
		tbn2[0] *= faceDirection;
		tbn2[1] *= faceDirection;
	#endif
#endif
vec3 nonPerturbedNormal = normal;`,YT=`#ifdef USE_NORMALMAP_OBJECTSPACE
	normal = texture2D( normalMap, vNormalMapUv ).xyz * 2.0 - 1.0;
	#ifdef FLIP_SIDED
		normal = - normal;
	#endif
	#ifdef DOUBLE_SIDED
		normal = normal * faceDirection;
	#endif
	normal = normalize( normalMatrix * normal );
#elif defined( USE_NORMALMAP_TANGENTSPACE )
	vec3 mapN = texture2D( normalMap, vNormalMapUv ).xyz * 2.0 - 1.0;
	#if defined( USE_PACKED_NORMALMAP )
		mapN = vec3( mapN.xy, sqrt( saturate( 1.0 - dot( mapN.xy, mapN.xy ) ) ) );
	#endif
	mapN.xy *= normalScale;
	normal = normalize( tbn * mapN );
#elif defined( USE_BUMPMAP )
	normal = perturbNormalArb( - vViewPosition, normal, dHdxy_fwd(), faceDirection );
#endif`,jT=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,ZT=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,KT=`#ifndef FLAT_SHADED
	vNormal = normalize( transformedNormal );
	#ifdef USE_TANGENT
		vTangent = normalize( transformedTangent );
		vBitangent = normalize( cross( vNormal, vTangent ) * tangent.w );
		#ifdef FLIP_SIDED
			vBitangent = - vBitangent;
		#endif
	#endif
#endif`,QT=`#ifdef USE_NORMALMAP
	uniform sampler2D normalMap;
	uniform vec2 normalScale;
#endif
#ifdef USE_NORMALMAP_OBJECTSPACE
	uniform mat3 normalMatrix;
#endif
#if ! defined ( USE_TANGENT ) && ( defined ( USE_NORMALMAP_TANGENTSPACE ) || defined ( USE_CLEARCOAT_NORMALMAP ) || defined( USE_ANISOTROPY ) )
	mat3 getTangentFrame( vec3 eye_pos, vec3 surf_norm, vec2 uv ) {
		vec3 q0 = dFdx( eye_pos.xyz );
		vec3 q1 = dFdy( eye_pos.xyz );
		vec2 st0 = dFdx( uv.st );
		vec2 st1 = dFdy( uv.st );
		vec3 N = surf_norm;
		vec3 q1perp = cross( q1, N );
		vec3 q0perp = cross( N, q0 );
		vec3 T = q1perp * st0.x + q0perp * st1.x;
		vec3 B = q1perp * st0.y + q0perp * st1.y;
		float det = max( dot( T, T ), dot( B, B ) );
		float scale = ( det == 0.0 ) ? 0.0 : inversesqrt( det );
		return mat3( T * scale, B * scale, N );
	}
#endif`,JT=`#ifdef USE_CLEARCOAT
	vec3 clearcoatNormal = nonPerturbedNormal;
#endif`,$T=`#ifdef USE_CLEARCOAT_NORMALMAP
	vec3 clearcoatMapN = texture2D( clearcoatNormalMap, vClearcoatNormalMapUv ).xyz * 2.0 - 1.0;
	clearcoatMapN.xy *= clearcoatNormalScale;
	clearcoatNormal = normalize( tbn2 * clearcoatMapN );
#endif`,tA=`#ifdef USE_CLEARCOATMAP
	uniform sampler2D clearcoatMap;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform sampler2D clearcoatNormalMap;
	uniform vec2 clearcoatNormalScale;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform sampler2D clearcoatRoughnessMap;
#endif`,eA=`#ifdef USE_IRIDESCENCEMAP
	uniform sampler2D iridescenceMap;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform sampler2D iridescenceThicknessMap;
#endif`,nA=`#ifdef OPAQUE
diffuseColor.a = 1.0;
#endif
#ifdef USE_TRANSMISSION
diffuseColor.a *= material.transmissionAlpha;
#endif
gl_FragColor = vec4( outgoingLight, diffuseColor.a );`,iA=`vec3 packNormalToRGB( const in vec3 normal ) {
	return normalize( normal ) * 0.5 + 0.5;
}
vec3 unpackRGBToNormal( const in vec3 rgb ) {
	return 2.0 * rgb.xyz - 1.0;
}
const float PackUpscale = 256. / 255.;const float UnpackDownscale = 255. / 256.;const float ShiftRight8 = 1. / 256.;
const float Inv255 = 1. / 255.;
const vec4 PackFactors = vec4( 1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0 );
const vec2 UnpackFactors2 = vec2( UnpackDownscale, 1.0 / PackFactors.g );
const vec3 UnpackFactors3 = vec3( UnpackDownscale / PackFactors.rg, 1.0 / PackFactors.b );
const vec4 UnpackFactors4 = vec4( UnpackDownscale / PackFactors.rgb, 1.0 / PackFactors.a );
vec4 packDepthToRGBA( const in float v ) {
	if( v <= 0.0 )
		return vec4( 0., 0., 0., 0. );
	if( v >= 1.0 )
		return vec4( 1., 1., 1., 1. );
	float vuf;
	float af = modf( v * PackFactors.a, vuf );
	float bf = modf( vuf * ShiftRight8, vuf );
	float gf = modf( vuf * ShiftRight8, vuf );
	return vec4( vuf * Inv255, gf * PackUpscale, bf * PackUpscale, af );
}
vec3 packDepthToRGB( const in float v ) {
	if( v <= 0.0 )
		return vec3( 0., 0., 0. );
	if( v >= 1.0 )
		return vec3( 1., 1., 1. );
	float vuf;
	float bf = modf( v * PackFactors.b, vuf );
	float gf = modf( vuf * ShiftRight8, vuf );
	return vec3( vuf * Inv255, gf * PackUpscale, bf );
}
vec2 packDepthToRG( const in float v ) {
	if( v <= 0.0 )
		return vec2( 0., 0. );
	if( v >= 1.0 )
		return vec2( 1., 1. );
	float vuf;
	float gf = modf( v * 256., vuf );
	return vec2( vuf * Inv255, gf );
}
float unpackRGBAToDepth( const in vec4 v ) {
	return dot( v, UnpackFactors4 );
}
float unpackRGBToDepth( const in vec3 v ) {
	return dot( v, UnpackFactors3 );
}
float unpackRGToDepth( const in vec2 v ) {
	return v.r * UnpackFactors2.r + v.g * UnpackFactors2.g;
}
vec4 pack2HalfToRGBA( const in vec2 v ) {
	vec4 r = vec4( v.x, fract( v.x * 255.0 ), v.y, fract( v.y * 255.0 ) );
	return vec4( r.x - r.y / 255.0, r.y, r.z - r.w / 255.0, r.w );
}
vec2 unpackRGBATo2Half( const in vec4 v ) {
	return vec2( v.x + ( v.y / 255.0 ), v.z + ( v.w / 255.0 ) );
}
float viewZToOrthographicDepth( const in float viewZ, const in float near, const in float far ) {
	return ( viewZ + near ) / ( near - far );
}
float orthographicDepthToViewZ( const in float depth, const in float near, const in float far ) {
	#ifdef USE_REVERSED_DEPTH_BUFFER
	
		return depth * ( far - near ) - far;
	#else
		return depth * ( near - far ) - near;
	#endif
}
float viewZToPerspectiveDepth( const in float viewZ, const in float near, const in float far ) {
	return ( ( near + viewZ ) * far ) / ( ( far - near ) * viewZ );
}
float perspectiveDepthToViewZ( const in float depth, const in float near, const in float far ) {
	
	#ifdef USE_REVERSED_DEPTH_BUFFER
		return ( near * far ) / ( ( near - far ) * depth - near );
	#else
		return ( near * far ) / ( ( far - near ) * depth - far );
	#endif
}`,aA=`#ifdef PREMULTIPLIED_ALPHA
	gl_FragColor.rgb *= gl_FragColor.a;
#endif`,sA=`vec4 mvPosition = vec4( transformed, 1.0 );
#ifdef USE_BATCHING
	mvPosition = batchingMatrix * mvPosition;
#endif
#ifdef USE_INSTANCING
	mvPosition = instanceMatrix * mvPosition;
#endif
mvPosition = modelViewMatrix * mvPosition;
gl_Position = projectionMatrix * mvPosition;`,rA=`#ifdef DITHERING
	gl_FragColor.rgb = dithering( gl_FragColor.rgb );
#endif`,oA=`#ifdef DITHERING
	vec3 dithering( vec3 color ) {
		float grid_position = rand( gl_FragCoord.xy );
		vec3 dither_shift_RGB = vec3( 0.25 / 255.0, -0.25 / 255.0, 0.25 / 255.0 );
		dither_shift_RGB = mix( 2.0 * dither_shift_RGB, -2.0 * dither_shift_RGB, grid_position );
		return color + dither_shift_RGB;
	}
#endif`,lA=`float roughnessFactor = roughness;
#ifdef USE_ROUGHNESSMAP
	vec4 texelRoughness = texture2D( roughnessMap, vRoughnessMapUv );
	roughnessFactor *= texelRoughness.g;
#endif`,cA=`#ifdef USE_ROUGHNESSMAP
	uniform sampler2D roughnessMap;
#endif`,uA=`#if NUM_SPOT_LIGHT_COORDS > 0
	varying vec4 vSpotLightCoord[ NUM_SPOT_LIGHT_COORDS ];
#endif
#if NUM_SPOT_LIGHT_MAPS > 0
	uniform sampler2D spotLightMap[ NUM_SPOT_LIGHT_MAPS ];
#endif
#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
		#if defined( SHADOWMAP_TYPE_PCF )
			uniform sampler2DShadow directionalShadowMap[ NUM_DIR_LIGHT_SHADOWS ];
		#else
			uniform sampler2D directionalShadowMap[ NUM_DIR_LIGHT_SHADOWS ];
		#endif
		varying vec4 vDirectionalShadowCoord[ NUM_DIR_LIGHT_SHADOWS ];
		struct DirectionalLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform DirectionalLightShadow directionalLightShadows[ NUM_DIR_LIGHT_SHADOWS ];
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
		#if defined( SHADOWMAP_TYPE_PCF )
			uniform sampler2DShadow spotShadowMap[ NUM_SPOT_LIGHT_SHADOWS ];
		#else
			uniform sampler2D spotShadowMap[ NUM_SPOT_LIGHT_SHADOWS ];
		#endif
		struct SpotLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform SpotLightShadow spotLightShadows[ NUM_SPOT_LIGHT_SHADOWS ];
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		#if defined( SHADOWMAP_TYPE_PCF )
			uniform samplerCubeShadow pointShadowMap[ NUM_POINT_LIGHT_SHADOWS ];
		#elif defined( SHADOWMAP_TYPE_BASIC )
			uniform samplerCube pointShadowMap[ NUM_POINT_LIGHT_SHADOWS ];
		#endif
		varying vec4 vPointShadowCoord[ NUM_POINT_LIGHT_SHADOWS ];
		struct PointLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
			float shadowCameraNear;
			float shadowCameraFar;
		};
		uniform PointLightShadow pointLightShadows[ NUM_POINT_LIGHT_SHADOWS ];
	#endif
	#if defined( SHADOWMAP_TYPE_PCF )
		float interleavedGradientNoise( vec2 position ) {
			return fract( 52.9829189 * fract( dot( position, vec2( 0.06711056, 0.00583715 ) ) ) );
		}
		vec2 vogelDiskSample( int sampleIndex, int samplesCount, float phi ) {
			const float goldenAngle = 2.399963229728653;
			float r = sqrt( ( float( sampleIndex ) + 0.5 ) / float( samplesCount ) );
			float theta = float( sampleIndex ) * goldenAngle + phi;
			return vec2( cos( theta ), sin( theta ) ) * r;
		}
	#endif
	#if defined( SHADOWMAP_TYPE_PCF )
		float getShadow( sampler2DShadow shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
			float shadow = 1.0;
			shadowCoord.xyz /= shadowCoord.w;
			shadowCoord.z += shadowBias;
			bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
			bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
			if ( frustumTest ) {
				vec2 texelSize = vec2( 1.0 ) / shadowMapSize;
				float radius = shadowRadius * texelSize.x;
				float phi = interleavedGradientNoise( gl_FragCoord.xy ) * PI2;
				shadow = (
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 0, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 1, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 2, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 3, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 4, 5, phi ) * radius, shadowCoord.z ) )
				) * 0.2;
			}
			return mix( 1.0, shadow, shadowIntensity );
		}
	#elif defined( SHADOWMAP_TYPE_VSM )
		float getShadow( sampler2D shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
			float shadow = 1.0;
			shadowCoord.xyz /= shadowCoord.w;
			#ifdef USE_REVERSED_DEPTH_BUFFER
				shadowCoord.z -= shadowBias;
			#else
				shadowCoord.z += shadowBias;
			#endif
			bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
			bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
			if ( frustumTest ) {
				vec2 distribution = texture2D( shadowMap, shadowCoord.xy ).rg;
				float mean = distribution.x;
				float variance = distribution.y * distribution.y;
				#ifdef USE_REVERSED_DEPTH_BUFFER
					float hard_shadow = step( mean, shadowCoord.z );
				#else
					float hard_shadow = step( shadowCoord.z, mean );
				#endif
				
				if ( hard_shadow == 1.0 ) {
					shadow = 1.0;
				} else {
					variance = max( variance, 0.0000001 );
					float d = shadowCoord.z - mean;
					float p_max = variance / ( variance + d * d );
					p_max = clamp( ( p_max - 0.3 ) / 0.65, 0.0, 1.0 );
					shadow = max( hard_shadow, p_max );
				}
			}
			return mix( 1.0, shadow, shadowIntensity );
		}
	#else
		float getShadow( sampler2D shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
			float shadow = 1.0;
			shadowCoord.xyz /= shadowCoord.w;
			#ifdef USE_REVERSED_DEPTH_BUFFER
				shadowCoord.z -= shadowBias;
			#else
				shadowCoord.z += shadowBias;
			#endif
			bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
			bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
			if ( frustumTest ) {
				float depth = texture2D( shadowMap, shadowCoord.xy ).r;
				#ifdef USE_REVERSED_DEPTH_BUFFER
					shadow = step( depth, shadowCoord.z );
				#else
					shadow = step( shadowCoord.z, depth );
				#endif
			}
			return mix( 1.0, shadow, shadowIntensity );
		}
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
	#if defined( SHADOWMAP_TYPE_PCF )
	float getPointShadow( samplerCubeShadow shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord, float shadowCameraNear, float shadowCameraFar ) {
		float shadow = 1.0;
		vec3 lightToPosition = shadowCoord.xyz;
		vec3 bd3D = normalize( lightToPosition );
		vec3 absVec = abs( lightToPosition );
		float viewSpaceZ = max( max( absVec.x, absVec.y ), absVec.z );
		if ( viewSpaceZ - shadowCameraFar <= 0.0 && viewSpaceZ - shadowCameraNear >= 0.0 ) {
			#ifdef USE_REVERSED_DEPTH_BUFFER
				float dp = ( shadowCameraNear * ( shadowCameraFar - viewSpaceZ ) ) / ( viewSpaceZ * ( shadowCameraFar - shadowCameraNear ) );
				dp -= shadowBias;
			#else
				float dp = ( shadowCameraFar * ( viewSpaceZ - shadowCameraNear ) ) / ( viewSpaceZ * ( shadowCameraFar - shadowCameraNear ) );
				dp += shadowBias;
			#endif
			float texelSize = shadowRadius / shadowMapSize.x;
			vec3 absDir = abs( bd3D );
			vec3 tangent = absDir.x > absDir.z ? vec3( 0.0, 1.0, 0.0 ) : vec3( 1.0, 0.0, 0.0 );
			tangent = normalize( cross( bd3D, tangent ) );
			vec3 bitangent = cross( bd3D, tangent );
			float phi = interleavedGradientNoise( gl_FragCoord.xy ) * PI2;
			vec2 sample0 = vogelDiskSample( 0, 5, phi );
			vec2 sample1 = vogelDiskSample( 1, 5, phi );
			vec2 sample2 = vogelDiskSample( 2, 5, phi );
			vec2 sample3 = vogelDiskSample( 3, 5, phi );
			vec2 sample4 = vogelDiskSample( 4, 5, phi );
			shadow = (
				texture( shadowMap, vec4( bd3D + ( tangent * sample0.x + bitangent * sample0.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample1.x + bitangent * sample1.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample2.x + bitangent * sample2.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample3.x + bitangent * sample3.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample4.x + bitangent * sample4.y ) * texelSize, dp ) )
			) * 0.2;
		}
		return mix( 1.0, shadow, shadowIntensity );
	}
	#elif defined( SHADOWMAP_TYPE_BASIC )
	float getPointShadow( samplerCube shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord, float shadowCameraNear, float shadowCameraFar ) {
		float shadow = 1.0;
		vec3 lightToPosition = shadowCoord.xyz;
		vec3 absVec = abs( lightToPosition );
		float viewSpaceZ = max( max( absVec.x, absVec.y ), absVec.z );
		if ( viewSpaceZ - shadowCameraFar <= 0.0 && viewSpaceZ - shadowCameraNear >= 0.0 ) {
			float dp = ( shadowCameraFar * ( viewSpaceZ - shadowCameraNear ) ) / ( viewSpaceZ * ( shadowCameraFar - shadowCameraNear ) );
			dp += shadowBias;
			vec3 bd3D = normalize( lightToPosition );
			float depth = textureCube( shadowMap, bd3D ).r;
			#ifdef USE_REVERSED_DEPTH_BUFFER
				depth = 1.0 - depth;
			#endif
			shadow = step( dp, depth );
		}
		return mix( 1.0, shadow, shadowIntensity );
	}
	#endif
	#endif
#endif`,fA=`#if NUM_SPOT_LIGHT_COORDS > 0
	uniform mat4 spotLightMatrix[ NUM_SPOT_LIGHT_COORDS ];
	varying vec4 vSpotLightCoord[ NUM_SPOT_LIGHT_COORDS ];
#endif
#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
		uniform mat4 directionalShadowMatrix[ NUM_DIR_LIGHT_SHADOWS ];
		varying vec4 vDirectionalShadowCoord[ NUM_DIR_LIGHT_SHADOWS ];
		struct DirectionalLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform DirectionalLightShadow directionalLightShadows[ NUM_DIR_LIGHT_SHADOWS ];
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
		struct SpotLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform SpotLightShadow spotLightShadows[ NUM_SPOT_LIGHT_SHADOWS ];
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		uniform mat4 pointShadowMatrix[ NUM_POINT_LIGHT_SHADOWS ];
		varying vec4 vPointShadowCoord[ NUM_POINT_LIGHT_SHADOWS ];
		struct PointLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
			float shadowCameraNear;
			float shadowCameraFar;
		};
		uniform PointLightShadow pointLightShadows[ NUM_POINT_LIGHT_SHADOWS ];
	#endif
#endif`,hA=`#if ( defined( USE_SHADOWMAP ) && ( NUM_DIR_LIGHT_SHADOWS > 0 || NUM_POINT_LIGHT_SHADOWS > 0 ) ) || ( NUM_SPOT_LIGHT_COORDS > 0 )
	#ifdef HAS_NORMAL
		vec3 shadowWorldNormal = transformNormalByInverseViewMatrix( transformedNormal, viewMatrix );
	#else
		vec3 shadowWorldNormal = vec3( 0.0 );
	#endif
	vec4 shadowWorldPosition;
#endif
#if defined( USE_SHADOWMAP )
	#if NUM_DIR_LIGHT_SHADOWS > 0
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_DIR_LIGHT_SHADOWS; i ++ ) {
			shadowWorldPosition = worldPosition + vec4( shadowWorldNormal * directionalLightShadows[ i ].shadowNormalBias, 0 );
			vDirectionalShadowCoord[ i ] = directionalShadowMatrix[ i ] * shadowWorldPosition;
		}
		#pragma unroll_loop_end
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_POINT_LIGHT_SHADOWS; i ++ ) {
			shadowWorldPosition = worldPosition + vec4( shadowWorldNormal * pointLightShadows[ i ].shadowNormalBias, 0 );
			vPointShadowCoord[ i ] = pointShadowMatrix[ i ] * shadowWorldPosition;
		}
		#pragma unroll_loop_end
	#endif
#endif
#if NUM_SPOT_LIGHT_COORDS > 0
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHT_COORDS; i ++ ) {
		shadowWorldPosition = worldPosition;
		#if ( defined( USE_SHADOWMAP ) && UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
			shadowWorldPosition.xyz += shadowWorldNormal * spotLightShadows[ i ].shadowNormalBias;
		#endif
		vSpotLightCoord[ i ] = spotLightMatrix[ i ] * shadowWorldPosition;
	}
	#pragma unroll_loop_end
#endif`,dA=`float getShadowMask() {
	float shadow = 1.0;
	#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
	DirectionalLightShadow directionalLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_DIR_LIGHT_SHADOWS; i ++ ) {
		directionalLight = directionalLightShadows[ i ];
		shadow *= receiveShadow ? getShadow( directionalShadowMap[ i ], directionalLight.shadowMapSize, directionalLight.shadowIntensity, directionalLight.shadowBias, directionalLight.shadowRadius, vDirectionalShadowCoord[ i ] ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
	SpotLightShadow spotLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHT_SHADOWS; i ++ ) {
		spotLight = spotLightShadows[ i ];
		shadow *= receiveShadow ? getShadow( spotShadowMap[ i ], spotLight.shadowMapSize, spotLight.shadowIntensity, spotLight.shadowBias, spotLight.shadowRadius, vSpotLightCoord[ i ] ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0 && ( defined( SHADOWMAP_TYPE_PCF ) || defined( SHADOWMAP_TYPE_BASIC ) )
	PointLightShadow pointLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_POINT_LIGHT_SHADOWS; i ++ ) {
		pointLight = pointLightShadows[ i ];
		shadow *= receiveShadow ? getPointShadow( pointShadowMap[ i ], pointLight.shadowMapSize, pointLight.shadowIntensity, pointLight.shadowBias, pointLight.shadowRadius, vPointShadowCoord[ i ], pointLight.shadowCameraNear, pointLight.shadowCameraFar ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#endif
	return shadow;
}`,pA=`#ifdef USE_SKINNING
	mat4 boneMatX = getBoneMatrix( skinIndex.x );
	mat4 boneMatY = getBoneMatrix( skinIndex.y );
	mat4 boneMatZ = getBoneMatrix( skinIndex.z );
	mat4 boneMatW = getBoneMatrix( skinIndex.w );
#endif`,mA=`#ifdef USE_SKINNING
	uniform mat4 bindMatrix;
	uniform mat4 bindMatrixInverse;
	uniform highp sampler2D boneTexture;
	mat4 getBoneMatrix( const in float i ) {
		int size = textureSize( boneTexture, 0 ).x;
		int j = int( i ) * 4;
		int x = j % size;
		int y = j / size;
		vec4 v1 = texelFetch( boneTexture, ivec2( x, y ), 0 );
		vec4 v2 = texelFetch( boneTexture, ivec2( x + 1, y ), 0 );
		vec4 v3 = texelFetch( boneTexture, ivec2( x + 2, y ), 0 );
		vec4 v4 = texelFetch( boneTexture, ivec2( x + 3, y ), 0 );
		return mat4( v1, v2, v3, v4 );
	}
#endif`,gA=`#ifdef USE_SKINNING
	vec4 skinVertex = bindMatrix * vec4( transformed, 1.0 );
	vec4 skinned = vec4( 0.0 );
	skinned += boneMatX * skinVertex * skinWeight.x;
	skinned += boneMatY * skinVertex * skinWeight.y;
	skinned += boneMatZ * skinVertex * skinWeight.z;
	skinned += boneMatW * skinVertex * skinWeight.w;
	transformed = ( bindMatrixInverse * skinned ).xyz;
#endif`,_A=`#ifdef USE_SKINNING
	mat4 skinMatrix = mat4( 0.0 );
	skinMatrix += skinWeight.x * boneMatX;
	skinMatrix += skinWeight.y * boneMatY;
	skinMatrix += skinWeight.z * boneMatZ;
	skinMatrix += skinWeight.w * boneMatW;
	skinMatrix = bindMatrixInverse * skinMatrix * bindMatrix;
	objectNormal = vec4( skinMatrix * vec4( objectNormal, 0.0 ) ).xyz;
	#ifdef USE_TANGENT
		objectTangent = vec4( skinMatrix * vec4( objectTangent, 0.0 ) ).xyz;
	#endif
#endif`,vA=`float specularStrength;
#ifdef USE_SPECULARMAP
	vec4 texelSpecular = texture2D( specularMap, vSpecularMapUv );
	specularStrength = texelSpecular.r;
#else
	specularStrength = 1.0;
#endif`,xA=`#ifdef USE_SPECULARMAP
	uniform sampler2D specularMap;
#endif`,yA=`#if defined( TONE_MAPPING )
	gl_FragColor.rgb = toneMapping( gl_FragColor.rgb );
#endif`,SA=`#ifndef saturate
#define saturate( a ) clamp( a, 0.0, 1.0 )
#endif
uniform float toneMappingExposure;
vec3 LinearToneMapping( vec3 color ) {
	return saturate( toneMappingExposure * color );
}
vec3 ReinhardToneMapping( vec3 color ) {
	color *= toneMappingExposure;
	return saturate( color / ( vec3( 1.0 ) + color ) );
}
vec3 CineonToneMapping( vec3 color ) {
	color *= toneMappingExposure;
	color = max( vec3( 0.0 ), color - 0.004 );
	return pow( ( color * ( 6.2 * color + 0.5 ) ) / ( color * ( 6.2 * color + 1.7 ) + 0.06 ), vec3( 2.2 ) );
}
vec3 RRTAndODTFit( vec3 v ) {
	vec3 a = v * ( v + 0.0245786 ) - 0.000090537;
	vec3 b = v * ( 0.983729 * v + 0.4329510 ) + 0.238081;
	return a / b;
}
vec3 ACESFilmicToneMapping( vec3 color ) {
	const mat3 ACESInputMat = mat3(
		vec3( 0.59719, 0.07600, 0.02840 ),		vec3( 0.35458, 0.90834, 0.13383 ),
		vec3( 0.04823, 0.01566, 0.83777 )
	);
	const mat3 ACESOutputMat = mat3(
		vec3(  1.60475, -0.10208, -0.00327 ),		vec3( -0.53108,  1.10813, -0.07276 ),
		vec3( -0.07367, -0.00605,  1.07602 )
	);
	color *= toneMappingExposure / 0.6;
	color = ACESInputMat * color;
	color = RRTAndODTFit( color );
	color = ACESOutputMat * color;
	return saturate( color );
}
const mat3 LINEAR_REC2020_TO_LINEAR_SRGB = mat3(
	vec3( 1.6605, - 0.1246, - 0.0182 ),
	vec3( - 0.5876, 1.1329, - 0.1006 ),
	vec3( - 0.0728, - 0.0083, 1.1187 )
);
const mat3 LINEAR_SRGB_TO_LINEAR_REC2020 = mat3(
	vec3( 0.6274, 0.0691, 0.0164 ),
	vec3( 0.3293, 0.9195, 0.0880 ),
	vec3( 0.0433, 0.0113, 0.8956 )
);
vec3 agxDefaultContrastApprox( vec3 x ) {
	vec3 x2 = x * x;
	vec3 x4 = x2 * x2;
	return + 15.5 * x4 * x2
		- 40.14 * x4 * x
		+ 31.96 * x4
		- 6.868 * x2 * x
		+ 0.4298 * x2
		+ 0.1191 * x
		- 0.00232;
}
vec3 AgXToneMapping( vec3 color ) {
	const mat3 AgXInsetMatrix = mat3(
		vec3( 0.856627153315983, 0.137318972929847, 0.11189821299995 ),
		vec3( 0.0951212405381588, 0.761241990602591, 0.0767994186031903 ),
		vec3( 0.0482516061458583, 0.101439036467562, 0.811302368396859 )
	);
	const mat3 AgXOutsetMatrix = mat3(
		vec3( 1.1271005818144368, - 0.1413297634984383, - 0.14132976349843826 ),
		vec3( - 0.11060664309660323, 1.157823702216272, - 0.11060664309660294 ),
		vec3( - 0.016493938717834573, - 0.016493938717834257, 1.2519364065950405 )
	);
	const float AgxMinEv = - 12.47393;	const float AgxMaxEv = 4.026069;
	color *= toneMappingExposure;
	color = LINEAR_SRGB_TO_LINEAR_REC2020 * color;
	color = AgXInsetMatrix * color;
	color = max( color, 1e-10 );	color = log2( color );
	color = ( color - AgxMinEv ) / ( AgxMaxEv - AgxMinEv );
	color = clamp( color, 0.0, 1.0 );
	color = agxDefaultContrastApprox( color );
	color = AgXOutsetMatrix * color;
	color = pow( max( vec3( 0.0 ), color ), vec3( 2.2 ) );
	color = LINEAR_REC2020_TO_LINEAR_SRGB * color;
	color = clamp( color, 0.0, 1.0 );
	return color;
}
vec3 NeutralToneMapping( vec3 color ) {
	const float StartCompression = 0.8 - 0.04;
	const float Desaturation = 0.15;
	color *= toneMappingExposure;
	float x = min( color.r, min( color.g, color.b ) );
	float offset = x < 0.08 ? x - 6.25 * x * x : 0.04;
	color -= offset;
	float peak = max( color.r, max( color.g, color.b ) );
	if ( peak < StartCompression ) return color;
	float d = 1. - StartCompression;
	float newPeak = 1. - d * d / ( peak + d - StartCompression );
	color *= newPeak / peak;
	float g = 1. - 1. / ( Desaturation * ( peak - newPeak ) + 1. );
	return mix( color, vec3( newPeak ), g );
}
vec3 CustomToneMapping( vec3 color ) { return color; }`,MA=`#ifdef USE_TRANSMISSION
	material.transmission = transmission;
	material.transmissionAlpha = 1.0;
	material.thickness = thickness;
	material.attenuationDistance = attenuationDistance;
	material.attenuationColor = attenuationColor;
	#ifdef USE_TRANSMISSIONMAP
		material.transmission *= texture2D( transmissionMap, vTransmissionMapUv ).r;
	#endif
	#ifdef USE_THICKNESSMAP
		material.thickness *= texture2D( thicknessMap, vThicknessMapUv ).g;
	#endif
	vec3 pos = vWorldPosition;
	vec3 v = normalize( cameraPosition - pos );
	vec3 n = transformNormalByInverseViewMatrix( normal, viewMatrix );
	vec4 transmitted = getIBLVolumeRefraction(
		n, v, material.roughness, material.diffuseContribution, material.specularColorBlended, material.specularF90,
		pos, modelMatrix, viewMatrix, projectionMatrix, material.dispersion, material.ior, material.thickness,
		material.attenuationColor, material.attenuationDistance );
	material.transmissionAlpha = mix( material.transmissionAlpha, transmitted.a, material.transmission );
	totalDiffuse = mix( totalDiffuse, transmitted.rgb, material.transmission );
#endif`,EA=`#ifdef USE_TRANSMISSION
	uniform float transmission;
	uniform float thickness;
	uniform float attenuationDistance;
	uniform vec3 attenuationColor;
	#ifdef USE_TRANSMISSIONMAP
		uniform sampler2D transmissionMap;
	#endif
	#ifdef USE_THICKNESSMAP
		uniform sampler2D thicknessMap;
	#endif
	uniform vec2 transmissionSamplerSize;
	uniform sampler2D transmissionSamplerMap;
	uniform mat4 modelMatrix;
	uniform mat4 projectionMatrix;
	varying vec3 vWorldPosition;
	float w0( float a ) {
		return ( 1.0 / 6.0 ) * ( a * ( a * ( - a + 3.0 ) - 3.0 ) + 1.0 );
	}
	float w1( float a ) {
		return ( 1.0 / 6.0 ) * ( a *  a * ( 3.0 * a - 6.0 ) + 4.0 );
	}
	float w2( float a ){
		return ( 1.0 / 6.0 ) * ( a * ( a * ( - 3.0 * a + 3.0 ) + 3.0 ) + 1.0 );
	}
	float w3( float a ) {
		return ( 1.0 / 6.0 ) * ( a * a * a );
	}
	float g0( float a ) {
		return w0( a ) + w1( a );
	}
	float g1( float a ) {
		return w2( a ) + w3( a );
	}
	float h0( float a ) {
		return - 1.0 + w1( a ) / ( w0( a ) + w1( a ) );
	}
	float h1( float a ) {
		return 1.0 + w3( a ) / ( w2( a ) + w3( a ) );
	}
	vec4 bicubic( sampler2D tex, vec2 uv, vec4 texelSize, float lod ) {
		uv = uv * texelSize.zw + 0.5;
		vec2 iuv = floor( uv );
		vec2 fuv = fract( uv );
		float g0x = g0( fuv.x );
		float g1x = g1( fuv.x );
		float h0x = h0( fuv.x );
		float h1x = h1( fuv.x );
		float h0y = h0( fuv.y );
		float h1y = h1( fuv.y );
		vec2 p0 = ( vec2( iuv.x + h0x, iuv.y + h0y ) - 0.5 ) * texelSize.xy;
		vec2 p1 = ( vec2( iuv.x + h1x, iuv.y + h0y ) - 0.5 ) * texelSize.xy;
		vec2 p2 = ( vec2( iuv.x + h0x, iuv.y + h1y ) - 0.5 ) * texelSize.xy;
		vec2 p3 = ( vec2( iuv.x + h1x, iuv.y + h1y ) - 0.5 ) * texelSize.xy;
		return g0( fuv.y ) * ( g0x * textureLod( tex, p0, lod ) + g1x * textureLod( tex, p1, lod ) ) +
			g1( fuv.y ) * ( g0x * textureLod( tex, p2, lod ) + g1x * textureLod( tex, p3, lod ) );
	}
	vec4 textureBicubic( sampler2D sampler, vec2 uv, float lod ) {
		vec2 fLodSize = vec2( textureSize( sampler, int( lod ) ) );
		vec2 cLodSize = vec2( textureSize( sampler, int( lod + 1.0 ) ) );
		vec2 fLodSizeInv = 1.0 / fLodSize;
		vec2 cLodSizeInv = 1.0 / cLodSize;
		vec4 fSample = bicubic( sampler, uv, vec4( fLodSizeInv, fLodSize ), floor( lod ) );
		vec4 cSample = bicubic( sampler, uv, vec4( cLodSizeInv, cLodSize ), ceil( lod ) );
		return mix( fSample, cSample, fract( lod ) );
	}
	vec3 getVolumeTransmissionRay( const in vec3 n, const in vec3 v, const in float thickness, const in float ior, const in mat4 modelMatrix ) {
		vec3 refractionVector = refract( - v, normalize( n ), 1.0 / ior );
		vec3 modelScale;
		modelScale.x = length( vec3( modelMatrix[ 0 ].xyz ) );
		modelScale.y = length( vec3( modelMatrix[ 1 ].xyz ) );
		modelScale.z = length( vec3( modelMatrix[ 2 ].xyz ) );
		return normalize( refractionVector ) * thickness * modelScale;
	}
	float applyIorToRoughness( const in float roughness, const in float ior ) {
		return roughness * clamp( ior * 2.0 - 2.0, 0.0, 1.0 );
	}
	vec4 getTransmissionSample( const in vec2 fragCoord, const in float roughness, const in float ior ) {
		float lod = log2( transmissionSamplerSize.x ) * applyIorToRoughness( roughness, ior );
		return textureBicubic( transmissionSamplerMap, fragCoord.xy, lod );
	}
	vec3 volumeAttenuation( const in float transmissionDistance, const in vec3 attenuationColor, const in float attenuationDistance ) {
		if ( isinf( attenuationDistance ) ) {
			return vec3( 1.0 );
		} else {
			vec3 attenuationCoefficient = -log( attenuationColor ) / attenuationDistance;
			vec3 transmittance = exp( - attenuationCoefficient * transmissionDistance );			return transmittance;
		}
	}
	vec4 getIBLVolumeRefraction( const in vec3 n, const in vec3 v, const in float roughness, const in vec3 diffuseColor,
		const in vec3 specularColor, const in float specularF90, const in vec3 position, const in mat4 modelMatrix,
		const in mat4 viewMatrix, const in mat4 projMatrix, const in float dispersion, const in float ior, const in float thickness,
		const in vec3 attenuationColor, const in float attenuationDistance ) {
		vec4 transmittedLight;
		vec3 transmittance;
		#ifdef USE_DISPERSION
			float halfSpread = ( ior - 1.0 ) * 0.025 * dispersion;
			vec3 iors = vec3( ior - halfSpread, ior, ior + halfSpread );
			for ( int i = 0; i < 3; i ++ ) {
				vec3 transmissionRay = getVolumeTransmissionRay( n, v, thickness, iors[ i ], modelMatrix );
				vec3 refractedRayExit = position + transmissionRay;
				vec4 ndcPos = projMatrix * viewMatrix * vec4( refractedRayExit, 1.0 );
				vec2 refractionCoords = ndcPos.xy / ndcPos.w;
				refractionCoords += 1.0;
				refractionCoords /= 2.0;
				vec4 transmissionSample = getTransmissionSample( refractionCoords, roughness, iors[ i ] );
				transmittedLight[ i ] = transmissionSample[ i ];
				transmittedLight.a += transmissionSample.a;
				transmittance[ i ] = diffuseColor[ i ] * volumeAttenuation( length( transmissionRay ), attenuationColor, attenuationDistance )[ i ];
			}
			transmittedLight.a /= 3.0;
		#else
			vec3 transmissionRay = getVolumeTransmissionRay( n, v, thickness, ior, modelMatrix );
			vec3 refractedRayExit = position + transmissionRay;
			vec4 ndcPos = projMatrix * viewMatrix * vec4( refractedRayExit, 1.0 );
			vec2 refractionCoords = ndcPos.xy / ndcPos.w;
			refractionCoords += 1.0;
			refractionCoords /= 2.0;
			transmittedLight = getTransmissionSample( refractionCoords, roughness, ior );
			transmittance = diffuseColor * volumeAttenuation( length( transmissionRay ), attenuationColor, attenuationDistance );
		#endif
		vec3 attenuatedColor = transmittance * transmittedLight.rgb;
		vec3 F = EnvironmentBRDF( n, v, specularColor, specularF90, roughness );
		float transmittanceFactor = ( transmittance.r + transmittance.g + transmittance.b ) / 3.0;
		return vec4( ( 1.0 - F ) * attenuatedColor, 1.0 - ( 1.0 - transmittedLight.a ) * transmittanceFactor );
	}
#endif`,bA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	varying vec2 vUv;
#endif
#ifdef USE_MAP
	varying vec2 vMapUv;
#endif
#ifdef USE_ALPHAMAP
	varying vec2 vAlphaMapUv;
#endif
#ifdef USE_LIGHTMAP
	varying vec2 vLightMapUv;
#endif
#ifdef USE_AOMAP
	varying vec2 vAoMapUv;
#endif
#ifdef USE_BUMPMAP
	varying vec2 vBumpMapUv;
#endif
#ifdef USE_NORMALMAP
	varying vec2 vNormalMapUv;
#endif
#ifdef USE_EMISSIVEMAP
	varying vec2 vEmissiveMapUv;
#endif
#ifdef USE_METALNESSMAP
	varying vec2 vMetalnessMapUv;
#endif
#ifdef USE_ROUGHNESSMAP
	varying vec2 vRoughnessMapUv;
#endif
#ifdef USE_ANISOTROPYMAP
	varying vec2 vAnisotropyMapUv;
#endif
#ifdef USE_CLEARCOATMAP
	varying vec2 vClearcoatMapUv;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	varying vec2 vClearcoatNormalMapUv;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	varying vec2 vClearcoatRoughnessMapUv;
#endif
#ifdef USE_IRIDESCENCEMAP
	varying vec2 vIridescenceMapUv;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	varying vec2 vIridescenceThicknessMapUv;
#endif
#ifdef USE_SHEEN_COLORMAP
	varying vec2 vSheenColorMapUv;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	varying vec2 vSheenRoughnessMapUv;
#endif
#ifdef USE_SPECULARMAP
	varying vec2 vSpecularMapUv;
#endif
#ifdef USE_SPECULAR_COLORMAP
	varying vec2 vSpecularColorMapUv;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	varying vec2 vSpecularIntensityMapUv;
#endif
#ifdef USE_TRANSMISSIONMAP
	uniform mat3 transmissionMapTransform;
	varying vec2 vTransmissionMapUv;
#endif
#ifdef USE_THICKNESSMAP
	uniform mat3 thicknessMapTransform;
	varying vec2 vThicknessMapUv;
#endif`,TA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	varying vec2 vUv;
#endif
#ifdef USE_MAP
	uniform mat3 mapTransform;
	varying vec2 vMapUv;
#endif
#ifdef USE_ALPHAMAP
	uniform mat3 alphaMapTransform;
	varying vec2 vAlphaMapUv;
#endif
#ifdef USE_LIGHTMAP
	uniform mat3 lightMapTransform;
	varying vec2 vLightMapUv;
#endif
#ifdef USE_AOMAP
	uniform mat3 aoMapTransform;
	varying vec2 vAoMapUv;
#endif
#ifdef USE_BUMPMAP
	uniform mat3 bumpMapTransform;
	varying vec2 vBumpMapUv;
#endif
#ifdef USE_NORMALMAP
	uniform mat3 normalMapTransform;
	varying vec2 vNormalMapUv;
#endif
#ifdef USE_DISPLACEMENTMAP
	uniform mat3 displacementMapTransform;
	varying vec2 vDisplacementMapUv;
#endif
#ifdef USE_EMISSIVEMAP
	uniform mat3 emissiveMapTransform;
	varying vec2 vEmissiveMapUv;
#endif
#ifdef USE_METALNESSMAP
	uniform mat3 metalnessMapTransform;
	varying vec2 vMetalnessMapUv;
#endif
#ifdef USE_ROUGHNESSMAP
	uniform mat3 roughnessMapTransform;
	varying vec2 vRoughnessMapUv;
#endif
#ifdef USE_ANISOTROPYMAP
	uniform mat3 anisotropyMapTransform;
	varying vec2 vAnisotropyMapUv;
#endif
#ifdef USE_CLEARCOATMAP
	uniform mat3 clearcoatMapTransform;
	varying vec2 vClearcoatMapUv;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform mat3 clearcoatNormalMapTransform;
	varying vec2 vClearcoatNormalMapUv;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform mat3 clearcoatRoughnessMapTransform;
	varying vec2 vClearcoatRoughnessMapUv;
#endif
#ifdef USE_SHEEN_COLORMAP
	uniform mat3 sheenColorMapTransform;
	varying vec2 vSheenColorMapUv;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	uniform mat3 sheenRoughnessMapTransform;
	varying vec2 vSheenRoughnessMapUv;
#endif
#ifdef USE_IRIDESCENCEMAP
	uniform mat3 iridescenceMapTransform;
	varying vec2 vIridescenceMapUv;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform mat3 iridescenceThicknessMapTransform;
	varying vec2 vIridescenceThicknessMapUv;
#endif
#ifdef USE_SPECULARMAP
	uniform mat3 specularMapTransform;
	varying vec2 vSpecularMapUv;
#endif
#ifdef USE_SPECULAR_COLORMAP
	uniform mat3 specularColorMapTransform;
	varying vec2 vSpecularColorMapUv;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	uniform mat3 specularIntensityMapTransform;
	varying vec2 vSpecularIntensityMapUv;
#endif
#ifdef USE_TRANSMISSIONMAP
	uniform mat3 transmissionMapTransform;
	varying vec2 vTransmissionMapUv;
#endif
#ifdef USE_THICKNESSMAP
	uniform mat3 thicknessMapTransform;
	varying vec2 vThicknessMapUv;
#endif`,AA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	vUv = vec3( uv, 1 ).xy;
#endif
#ifdef USE_MAP
	vMapUv = ( mapTransform * vec3( MAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ALPHAMAP
	vAlphaMapUv = ( alphaMapTransform * vec3( ALPHAMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_LIGHTMAP
	vLightMapUv = ( lightMapTransform * vec3( LIGHTMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_AOMAP
	vAoMapUv = ( aoMapTransform * vec3( AOMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_BUMPMAP
	vBumpMapUv = ( bumpMapTransform * vec3( BUMPMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_NORMALMAP
	vNormalMapUv = ( normalMapTransform * vec3( NORMALMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_DISPLACEMENTMAP
	vDisplacementMapUv = ( displacementMapTransform * vec3( DISPLACEMENTMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_EMISSIVEMAP
	vEmissiveMapUv = ( emissiveMapTransform * vec3( EMISSIVEMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_METALNESSMAP
	vMetalnessMapUv = ( metalnessMapTransform * vec3( METALNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ROUGHNESSMAP
	vRoughnessMapUv = ( roughnessMapTransform * vec3( ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ANISOTROPYMAP
	vAnisotropyMapUv = ( anisotropyMapTransform * vec3( ANISOTROPYMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOATMAP
	vClearcoatMapUv = ( clearcoatMapTransform * vec3( CLEARCOATMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	vClearcoatNormalMapUv = ( clearcoatNormalMapTransform * vec3( CLEARCOAT_NORMALMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	vClearcoatRoughnessMapUv = ( clearcoatRoughnessMapTransform * vec3( CLEARCOAT_ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_IRIDESCENCEMAP
	vIridescenceMapUv = ( iridescenceMapTransform * vec3( IRIDESCENCEMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	vIridescenceThicknessMapUv = ( iridescenceThicknessMapTransform * vec3( IRIDESCENCE_THICKNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SHEEN_COLORMAP
	vSheenColorMapUv = ( sheenColorMapTransform * vec3( SHEEN_COLORMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	vSheenRoughnessMapUv = ( sheenRoughnessMapTransform * vec3( SHEEN_ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULARMAP
	vSpecularMapUv = ( specularMapTransform * vec3( SPECULARMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULAR_COLORMAP
	vSpecularColorMapUv = ( specularColorMapTransform * vec3( SPECULAR_COLORMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	vSpecularIntensityMapUv = ( specularIntensityMapTransform * vec3( SPECULAR_INTENSITYMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_TRANSMISSIONMAP
	vTransmissionMapUv = ( transmissionMapTransform * vec3( TRANSMISSIONMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_THICKNESSMAP
	vThicknessMapUv = ( thicknessMapTransform * vec3( THICKNESSMAP_UV, 1 ) ).xy;
#endif`,RA=`#if defined( USE_ENVMAP ) || defined( DISTANCE ) || defined ( USE_SHADOWMAP ) || defined ( USE_TRANSMISSION ) || NUM_SPOT_LIGHT_COORDS > 0
	vec4 worldPosition = vec4( transformed, 1.0 );
	#ifdef USE_BATCHING
		worldPosition = batchingMatrix * worldPosition;
	#endif
	#ifdef USE_INSTANCING
		worldPosition = instanceMatrix * worldPosition;
	#endif
	worldPosition = modelMatrix * worldPosition;
#endif`;const wA=`varying vec2 vUv;
uniform mat3 uvTransform;
void main() {
	vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	gl_Position = vec4( position.xy, 1.0, 1.0 );
}`,CA=`uniform sampler2D t2D;
uniform float backgroundIntensity;
varying vec2 vUv;
void main() {
	vec4 texColor = texture2D( t2D, vUv );
	#ifdef DECODE_VIDEO_TEXTURE
		texColor = vec4( mix( pow( texColor.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), texColor.rgb * 0.0773993808, vec3( lessThanEqual( texColor.rgb, vec3( 0.04045 ) ) ) ), texColor.w );
	#endif
	texColor.rgb *= backgroundIntensity;
	gl_FragColor = texColor;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,DA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,UA=`#ifdef ENVMAP_TYPE_CUBE
	uniform samplerCube envMap;
#elif defined( ENVMAP_TYPE_CUBE_UV )
	uniform sampler2D envMap;
#endif
uniform float backgroundBlurriness;
uniform float backgroundIntensity;
uniform mat3 backgroundRotation;
varying vec3 vWorldDirection;
#include <cube_uv_reflection_fragment>
void main() {
	#ifdef ENVMAP_TYPE_CUBE
		vec4 texColor = textureCube( envMap, backgroundRotation * vWorldDirection );
	#elif defined( ENVMAP_TYPE_CUBE_UV )
		vec4 texColor = textureCubeUV( envMap, backgroundRotation * vWorldDirection, backgroundBlurriness );
	#else
		vec4 texColor = vec4( 0.0, 0.0, 0.0, 1.0 );
	#endif
	texColor.rgb *= backgroundIntensity;
	gl_FragColor = texColor;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,NA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,LA=`uniform samplerCube tCube;
uniform float tFlip;
uniform float opacity;
varying vec3 vWorldDirection;
void main() {
	vec4 texColor = textureCube( tCube, vec3( tFlip * vWorldDirection.x, vWorldDirection.yz ) );
	gl_FragColor = texColor;
	gl_FragColor.a *= opacity;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,OA=`#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
varying vec2 vHighPrecisionZW;
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <skinbase_vertex>
	#include <morphinstance_vertex>
	#ifdef USE_DISPLACEMENTMAP
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vHighPrecisionZW = gl_Position.zw;
}`,PA=`#if DEPTH_PACKING == 3200
	uniform float opacity;
#endif
#include <common>
#include <packing>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
varying vec2 vHighPrecisionZW;
void main() {
	vec4 diffuseColor = vec4( 1.0 );
	#include <clipping_planes_fragment>
	#if DEPTH_PACKING == 3200
		diffuseColor.a = opacity;
	#endif
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <logdepthbuf_fragment>
	#ifdef USE_REVERSED_DEPTH_BUFFER
		float fragCoordZ = vHighPrecisionZW[ 0 ] / vHighPrecisionZW[ 1 ];
	#else
		float fragCoordZ = 0.5 * vHighPrecisionZW[ 0 ] / vHighPrecisionZW[ 1 ] + 0.5;
	#endif
	#if DEPTH_PACKING == 3200
		gl_FragColor = vec4( vec3( 1.0 - fragCoordZ ), opacity );
	#elif DEPTH_PACKING == 3201
		gl_FragColor = packDepthToRGBA( fragCoordZ );
	#elif DEPTH_PACKING == 3202
		gl_FragColor = vec4( packDepthToRGB( fragCoordZ ), 1.0 );
	#elif DEPTH_PACKING == 3203
		gl_FragColor = vec4( packDepthToRG( fragCoordZ ), 0.0, 1.0 );
	#endif
}`,zA=`#define DISTANCE
varying vec3 vWorldPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <skinbase_vertex>
	#include <morphinstance_vertex>
	#ifdef USE_DISPLACEMENTMAP
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <worldpos_vertex>
	#include <clipping_planes_vertex>
	vWorldPosition = worldPosition.xyz;
}`,BA=`#define DISTANCE
uniform vec3 referencePosition;
uniform float nearDistance;
uniform float farDistance;
varying vec3 vWorldPosition;
#include <common>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( 1.0 );
	#include <clipping_planes_fragment>
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	float dist = length( vWorldPosition - referencePosition );
	dist = ( dist - nearDistance ) / ( farDistance - nearDistance );
	dist = saturate( dist );
	gl_FragColor = vec4( dist, 0.0, 0.0, 1.0 );
}`,IA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
}`,FA=`uniform sampler2D tEquirect;
varying vec3 vWorldDirection;
#include <common>
void main() {
	vec3 direction = normalize( vWorldDirection );
	vec2 sampleUV = equirectUv( direction );
	gl_FragColor = texture2D( tEquirect, sampleUV );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,HA=`uniform float scale;
attribute float lineDistance;
varying float vLineDistance;
#include <common>
#include <uv_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	vLineDistance = scale * lineDistance;
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
}`,GA=`uniform vec3 diffuse;
uniform float opacity;
uniform float dashSize;
uniform float totalSize;
varying float vLineDistance;
#include <common>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	if ( mod( vLineDistance, totalSize ) > dashSize ) {
		discard;
	}
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,VA=`#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#if defined ( USE_ENVMAP ) || defined ( USE_SKINNING )
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinbase_vertex>
		#include <skinnormal_vertex>
		#include <defaultnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <fog_vertex>
}`,kA=`uniform vec3 diffuse;
uniform float opacity;
#ifndef FLAT_SHADED
	varying vec3 vNormal;
#endif
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <fog_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	#ifdef USE_LIGHTMAP
		vec4 lightMapTexel = texture2D( lightMap, vLightMapUv );
		reflectedLight.indirectDiffuse += lightMapTexel.rgb * lightMapIntensity * RECIPROCAL_PI;
	#else
		reflectedLight.indirectDiffuse += vec3( 1.0 );
	#endif
	#include <aomap_fragment>
	reflectedLight.indirectDiffuse *= diffuseColor.rgb;
	vec3 outgoingLight = reflectedLight.indirectDiffuse;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,XA=`#define LAMBERT
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,WA=`#define LAMBERT
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float opacity;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_lambert_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_lambert_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + totalEmissiveRadiance;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,qA=`#define MATCAP
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <color_pars_vertex>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
	vViewPosition = - mvPosition.xyz;
}`,YA=`#define MATCAP
uniform vec3 diffuse;
uniform float opacity;
uniform sampler2D matcap;
varying vec3 vViewPosition;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <normal_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	vec3 viewDir = normalize( vViewPosition );
	vec3 x = normalize( vec3( viewDir.z, 0.0, - viewDir.x ) );
	vec3 y = cross( viewDir, x );
	vec2 uv = vec2( dot( x, normal ), dot( y, normal ) ) * 0.495 + 0.5;
	#ifdef USE_MATCAP
		vec4 matcapColor = texture2D( matcap, uv );
	#else
		vec4 matcapColor = vec4( vec3( mix( 0.2, 0.8, uv.y ) ), 1.0 );
	#endif
	vec3 outgoingLight = diffuseColor.rgb * matcapColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,jA=`#define NORMAL
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	varying vec3 vViewPosition;
#endif
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	vViewPosition = - mvPosition.xyz;
#endif
}`,ZA=`#define NORMAL
uniform float opacity;
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	varying vec3 vViewPosition;
#endif
#include <uv_pars_fragment>
#include <normal_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( 0.0, 0.0, 0.0, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	gl_FragColor = vec4( normalize( normal ) * 0.5 + 0.5, diffuseColor.a );
	#ifdef OPAQUE
		gl_FragColor.a = 1.0;
	#endif
}`,KA=`#define PHONG
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,QA=`#define PHONG
uniform vec3 diffuse;
uniform vec3 emissive;
uniform vec3 specular;
uniform float shininess;
uniform float opacity;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_phong_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_phong_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + reflectedLight.directSpecular + reflectedLight.indirectSpecular + totalEmissiveRadiance;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,JA=`#define STANDARD
varying vec3 vViewPosition;
#ifdef USE_TRANSMISSION
	varying vec3 vWorldPosition;
#endif
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
#ifdef USE_TRANSMISSION
	vWorldPosition = worldPosition.xyz;
#endif
}`,$A=`#define STANDARD
#ifdef PHYSICAL
	#define IOR
	#define USE_SPECULAR
#endif
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;
#ifdef IOR
	uniform float ior;
#endif
#ifdef USE_SPECULAR
	uniform float specularIntensity;
	uniform vec3 specularColor;
	#ifdef USE_SPECULAR_COLORMAP
		uniform sampler2D specularColorMap;
	#endif
	#ifdef USE_SPECULAR_INTENSITYMAP
		uniform sampler2D specularIntensityMap;
	#endif
#endif
#ifdef USE_CLEARCOAT
	uniform float clearcoat;
	uniform float clearcoatRoughness;
#endif
#ifdef USE_DISPERSION
	uniform float dispersion;
#endif
#ifdef USE_IRIDESCENCE
	uniform float iridescence;
	uniform float iridescenceIOR;
	uniform float iridescenceThicknessMinimum;
	uniform float iridescenceThicknessMaximum;
#endif
#ifdef USE_SHEEN
	uniform vec3 sheenColor;
	uniform float sheenRoughness;
	#ifdef USE_SHEEN_COLORMAP
		uniform sampler2D sheenColorMap;
	#endif
	#ifdef USE_SHEEN_ROUGHNESSMAP
		uniform sampler2D sheenRoughnessMap;
	#endif
#endif
#ifdef USE_ANISOTROPY
	uniform vec2 anisotropyVector;
	#ifdef USE_ANISOTROPYMAP
		uniform sampler2D anisotropyMap;
	#endif
#endif
varying vec3 vViewPosition;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <iridescence_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_physical_pars_fragment>
#include <transmission_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <iridescence_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <roughnessmap_fragment>
	#include <metalnessmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <clearcoat_normal_fragment_begin>
	#include <clearcoat_normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_physical_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 totalDiffuse = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse;
	vec3 totalSpecular = reflectedLight.directSpecular + reflectedLight.indirectSpecular;
	#include <transmission_fragment>
	vec3 outgoingLight = totalDiffuse + totalSpecular + totalEmissiveRadiance;
	#ifdef USE_SHEEN
 
		outgoingLight = outgoingLight + sheenSpecularDirect + sheenSpecularIndirect;
 
 	#endif
	#ifdef USE_CLEARCOAT
		float dotNVcc = saturate( dot( geometryClearcoatNormal, geometryViewDir ) );
		vec3 Fcc = F_Schlick( material.clearcoatF0, material.clearcoatF90, dotNVcc );
		outgoingLight = outgoingLight * ( 1.0 - material.clearcoat * Fcc ) + ( clearcoatSpecularDirect + clearcoatSpecularIndirect ) * material.clearcoat;
	#endif
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,t2=`#define TOON
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,e2=`#define TOON
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float opacity;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <gradientmap_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_toon_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_toon_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + totalEmissiveRadiance;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,n2=`uniform float size;
uniform float scale;
#include <common>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
#ifdef USE_POINTS_UV
	varying vec2 vUv;
	uniform mat3 uvTransform;
#endif
void main() {
	#ifdef USE_POINTS_UV
		vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	#endif
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <project_vertex>
	gl_PointSize = size;
	#ifdef USE_SIZEATTENUATION
		bool isPerspective = isPerspectiveMatrix( projectionMatrix );
		if ( isPerspective ) gl_PointSize *= ( scale / - mvPosition.z );
	#endif
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <worldpos_vertex>
	#include <fog_vertex>
}`,i2=`uniform vec3 diffuse;
uniform float opacity;
#include <common>
#include <color_pars_fragment>
#include <map_particle_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_particle_fragment>
	#include <color_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,a2=`#include <common>
#include <batching_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <shadowmap_pars_vertex>
void main() {
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,s2=`uniform vec3 color;
uniform float opacity;
#include <common>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <logdepthbuf_pars_fragment>
#include <shadowmap_pars_fragment>
#include <shadowmask_pars_fragment>
void main() {
	#include <logdepthbuf_fragment>
	gl_FragColor = vec4( color, opacity * ( 1.0 - getShadowMask() ) );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,r2=`uniform float rotation;
uniform vec2 center;
#include <common>
#include <uv_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	vec4 mvPosition = modelViewMatrix[ 3 ];
	vec2 scale = vec2( length( modelMatrix[ 0 ].xyz ), length( modelMatrix[ 1 ].xyz ) );
	#ifndef USE_SIZEATTENUATION
		bool isPerspective = isPerspectiveMatrix( projectionMatrix );
		if ( isPerspective ) scale *= - mvPosition.z;
	#endif
	vec2 alignedPosition = ( position.xy - ( center - vec2( 0.5 ) ) ) * scale;
	vec2 rotatedPosition;
	rotatedPosition.x = cos( rotation ) * alignedPosition.x - sin( rotation ) * alignedPosition.y;
	rotatedPosition.y = sin( rotation ) * alignedPosition.x + cos( rotation ) * alignedPosition.y;
	mvPosition.xy += rotatedPosition;
	gl_Position = projectionMatrix * mvPosition;
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
}`,o2=`uniform vec3 diffuse;
uniform float opacity;
#include <common>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
}`,me={alphahash_fragment:wb,alphahash_pars_fragment:Cb,alphamap_fragment:Db,alphamap_pars_fragment:Ub,alphatest_fragment:Nb,alphatest_pars_fragment:Lb,aomap_fragment:Ob,aomap_pars_fragment:Pb,batching_pars_vertex:zb,batching_vertex:Bb,begin_vertex:Ib,beginnormal_vertex:Fb,bsdfs:Hb,iridescence_fragment:Gb,bumpmap_pars_fragment:Vb,clipping_planes_fragment:kb,clipping_planes_pars_fragment:Xb,clipping_planes_pars_vertex:Wb,clipping_planes_vertex:qb,color_fragment:Yb,color_pars_fragment:jb,color_pars_vertex:Zb,color_vertex:Kb,common:Qb,cube_uv_reflection_fragment:Jb,defaultnormal_vertex:$b,displacementmap_pars_vertex:tT,displacementmap_vertex:eT,emissivemap_fragment:nT,emissivemap_pars_fragment:iT,colorspace_fragment:aT,colorspace_pars_fragment:sT,envmap_fragment:rT,envmap_common_pars_fragment:oT,envmap_pars_fragment:lT,envmap_pars_vertex:cT,envmap_physical_pars_fragment:yT,envmap_vertex:uT,fog_vertex:fT,fog_pars_vertex:hT,fog_fragment:dT,fog_pars_fragment:pT,gradientmap_pars_fragment:mT,lightmap_pars_fragment:gT,lights_lambert_fragment:_T,lights_lambert_pars_fragment:vT,lights_pars_begin:xT,lights_toon_fragment:ST,lights_toon_pars_fragment:MT,lights_phong_fragment:ET,lights_phong_pars_fragment:bT,lights_physical_fragment:TT,lights_physical_pars_fragment:AT,lights_fragment_begin:RT,lights_fragment_maps:wT,lights_fragment_end:CT,lightprobes_pars_fragment:DT,logdepthbuf_fragment:UT,logdepthbuf_pars_fragment:NT,logdepthbuf_pars_vertex:LT,logdepthbuf_vertex:OT,map_fragment:PT,map_pars_fragment:zT,map_particle_fragment:BT,map_particle_pars_fragment:IT,metalnessmap_fragment:FT,metalnessmap_pars_fragment:HT,morphinstance_vertex:GT,morphcolor_vertex:VT,morphnormal_vertex:kT,morphtarget_pars_vertex:XT,morphtarget_vertex:WT,normal_fragment_begin:qT,normal_fragment_maps:YT,normal_pars_fragment:jT,normal_pars_vertex:ZT,normal_vertex:KT,normalmap_pars_fragment:QT,clearcoat_normal_fragment_begin:JT,clearcoat_normal_fragment_maps:$T,clearcoat_pars_fragment:tA,iridescence_pars_fragment:eA,opaque_fragment:nA,packing:iA,premultiplied_alpha_fragment:aA,project_vertex:sA,dithering_fragment:rA,dithering_pars_fragment:oA,roughnessmap_fragment:lA,roughnessmap_pars_fragment:cA,shadowmap_pars_fragment:uA,shadowmap_pars_vertex:fA,shadowmap_vertex:hA,shadowmask_pars_fragment:dA,skinbase_vertex:pA,skinning_pars_vertex:mA,skinning_vertex:gA,skinnormal_vertex:_A,specularmap_fragment:vA,specularmap_pars_fragment:xA,tonemapping_fragment:yA,tonemapping_pars_fragment:SA,transmission_fragment:MA,transmission_pars_fragment:EA,uv_pars_fragment:bA,uv_pars_vertex:TA,uv_vertex:AA,worldpos_vertex:RA,background_vert:wA,background_frag:CA,backgroundCube_vert:DA,backgroundCube_frag:UA,cube_vert:NA,cube_frag:LA,depth_vert:OA,depth_frag:PA,distance_vert:zA,distance_frag:BA,equirect_vert:IA,equirect_frag:FA,linedashed_vert:HA,linedashed_frag:GA,meshbasic_vert:VA,meshbasic_frag:kA,meshlambert_vert:XA,meshlambert_frag:WA,meshmatcap_vert:qA,meshmatcap_frag:YA,meshnormal_vert:jA,meshnormal_frag:ZA,meshphong_vert:KA,meshphong_frag:QA,meshphysical_vert:JA,meshphysical_frag:$A,meshtoon_vert:t2,meshtoon_frag:e2,points_vert:n2,points_frag:i2,shadow_vert:a2,shadow_frag:s2,sprite_vert:r2,sprite_frag:o2},It={common:{diffuse:{value:new ie(16777215)},opacity:{value:1},map:{value:null},mapTransform:{value:new ue},alphaMap:{value:null},alphaMapTransform:{value:new ue},alphaTest:{value:0}},specularmap:{specularMap:{value:null},specularMapTransform:{value:new ue}},envmap:{envMap:{value:null},envMapRotation:{value:new ue},reflectivity:{value:1},ior:{value:1.5},refractionRatio:{value:.98},dfgLUT:{value:null}},aomap:{aoMap:{value:null},aoMapIntensity:{value:1},aoMapTransform:{value:new ue}},lightmap:{lightMap:{value:null},lightMapIntensity:{value:1},lightMapTransform:{value:new ue}},bumpmap:{bumpMap:{value:null},bumpMapTransform:{value:new ue},bumpScale:{value:1}},normalmap:{normalMap:{value:null},normalMapTransform:{value:new ue},normalScale:{value:new oe(1,1)}},displacementmap:{displacementMap:{value:null},displacementMapTransform:{value:new ue},displacementScale:{value:1},displacementBias:{value:0}},emissivemap:{emissiveMap:{value:null},emissiveMapTransform:{value:new ue}},metalnessmap:{metalnessMap:{value:null},metalnessMapTransform:{value:new ue}},roughnessmap:{roughnessMap:{value:null},roughnessMapTransform:{value:new ue}},gradientmap:{gradientMap:{value:null}},fog:{fogDensity:{value:25e-5},fogNear:{value:1},fogFar:{value:2e3},fogColor:{value:new ie(16777215)}},lights:{ambientLightColor:{value:[]},lightProbe:{value:[]},directionalLights:{value:[],properties:{direction:{},color:{}}},directionalLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},directionalShadowMatrix:{value:[]},spotLights:{value:[],properties:{color:{},position:{},direction:{},distance:{},coneCos:{},penumbraCos:{},decay:{}}},spotLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},spotLightMap:{value:[]},spotLightMatrix:{value:[]},pointLights:{value:[],properties:{color:{},position:{},decay:{},distance:{}}},pointLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{},shadowCameraNear:{},shadowCameraFar:{}}},pointShadowMatrix:{value:[]},hemisphereLights:{value:[],properties:{direction:{},skyColor:{},groundColor:{}}},rectAreaLights:{value:[],properties:{color:{},position:{},width:{},height:{}}},ltc_1:{value:null},ltc_2:{value:null},probesSH:{value:null},probesMin:{value:new k},probesMax:{value:new k},probesResolution:{value:new k}},points:{diffuse:{value:new ie(16777215)},opacity:{value:1},size:{value:1},scale:{value:1},map:{value:null},alphaMap:{value:null},alphaMapTransform:{value:new ue},alphaTest:{value:0},uvTransform:{value:new ue}},sprite:{diffuse:{value:new ie(16777215)},opacity:{value:1},center:{value:new oe(.5,.5)},rotation:{value:0},map:{value:null},mapTransform:{value:new ue},alphaMap:{value:null},alphaMapTransform:{value:new ue},alphaTest:{value:0}}},ji={basic:{uniforms:Wn([It.common,It.specularmap,It.envmap,It.aomap,It.lightmap,It.fog]),vertexShader:me.meshbasic_vert,fragmentShader:me.meshbasic_frag},lambert:{uniforms:Wn([It.common,It.specularmap,It.envmap,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.fog,It.lights,{emissive:{value:new ie(0)},envMapIntensity:{value:1}}]),vertexShader:me.meshlambert_vert,fragmentShader:me.meshlambert_frag},phong:{uniforms:Wn([It.common,It.specularmap,It.envmap,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.fog,It.lights,{emissive:{value:new ie(0)},specular:{value:new ie(1118481)},shininess:{value:30},envMapIntensity:{value:1}}]),vertexShader:me.meshphong_vert,fragmentShader:me.meshphong_frag},standard:{uniforms:Wn([It.common,It.envmap,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.roughnessmap,It.metalnessmap,It.fog,It.lights,{emissive:{value:new ie(0)},roughness:{value:1},metalness:{value:0},envMapIntensity:{value:1}}]),vertexShader:me.meshphysical_vert,fragmentShader:me.meshphysical_frag},toon:{uniforms:Wn([It.common,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.gradientmap,It.fog,It.lights,{emissive:{value:new ie(0)}}]),vertexShader:me.meshtoon_vert,fragmentShader:me.meshtoon_frag},matcap:{uniforms:Wn([It.common,It.bumpmap,It.normalmap,It.displacementmap,It.fog,{matcap:{value:null}}]),vertexShader:me.meshmatcap_vert,fragmentShader:me.meshmatcap_frag},points:{uniforms:Wn([It.points,It.fog]),vertexShader:me.points_vert,fragmentShader:me.points_frag},dashed:{uniforms:Wn([It.common,It.fog,{scale:{value:1},dashSize:{value:1},totalSize:{value:2}}]),vertexShader:me.linedashed_vert,fragmentShader:me.linedashed_frag},depth:{uniforms:Wn([It.common,It.displacementmap]),vertexShader:me.depth_vert,fragmentShader:me.depth_frag},normal:{uniforms:Wn([It.common,It.bumpmap,It.normalmap,It.displacementmap,{opacity:{value:1}}]),vertexShader:me.meshnormal_vert,fragmentShader:me.meshnormal_frag},sprite:{uniforms:Wn([It.sprite,It.fog]),vertexShader:me.sprite_vert,fragmentShader:me.sprite_frag},background:{uniforms:{uvTransform:{value:new ue},t2D:{value:null},backgroundIntensity:{value:1}},vertexShader:me.background_vert,fragmentShader:me.background_frag},backgroundCube:{uniforms:{envMap:{value:null},backgroundBlurriness:{value:0},backgroundIntensity:{value:1},backgroundRotation:{value:new ue}},vertexShader:me.backgroundCube_vert,fragmentShader:me.backgroundCube_frag},cube:{uniforms:{tCube:{value:null},tFlip:{value:-1},opacity:{value:1}},vertexShader:me.cube_vert,fragmentShader:me.cube_frag},equirect:{uniforms:{tEquirect:{value:null}},vertexShader:me.equirect_vert,fragmentShader:me.equirect_frag},distance:{uniforms:Wn([It.common,It.displacementmap,{referencePosition:{value:new k},nearDistance:{value:1},farDistance:{value:1e3}}]),vertexShader:me.distance_vert,fragmentShader:me.distance_frag},shadow:{uniforms:Wn([It.lights,It.fog,{color:{value:new ie(0)},opacity:{value:1}}]),vertexShader:me.shadow_vert,fragmentShader:me.shadow_frag}};ji.physical={uniforms:Wn([ji.standard.uniforms,{clearcoat:{value:0},clearcoatMap:{value:null},clearcoatMapTransform:{value:new ue},clearcoatNormalMap:{value:null},clearcoatNormalMapTransform:{value:new ue},clearcoatNormalScale:{value:new oe(1,1)},clearcoatRoughness:{value:0},clearcoatRoughnessMap:{value:null},clearcoatRoughnessMapTransform:{value:new ue},dispersion:{value:0},iridescence:{value:0},iridescenceMap:{value:null},iridescenceMapTransform:{value:new ue},iridescenceIOR:{value:1.3},iridescenceThicknessMinimum:{value:100},iridescenceThicknessMaximum:{value:400},iridescenceThicknessMap:{value:null},iridescenceThicknessMapTransform:{value:new ue},sheen:{value:0},sheenColor:{value:new ie(0)},sheenColorMap:{value:null},sheenColorMapTransform:{value:new ue},sheenRoughness:{value:1},sheenRoughnessMap:{value:null},sheenRoughnessMapTransform:{value:new ue},transmission:{value:0},transmissionMap:{value:null},transmissionMapTransform:{value:new ue},transmissionSamplerSize:{value:new oe},transmissionSamplerMap:{value:null},thickness:{value:0},thicknessMap:{value:null},thicknessMapTransform:{value:new ue},attenuationDistance:{value:0},attenuationColor:{value:new ie(0)},specularColor:{value:new ie(1,1,1)},specularColorMap:{value:null},specularColorMapTransform:{value:new ue},specularIntensity:{value:1},specularIntensityMap:{value:null},specularIntensityMapTransform:{value:new ue},anisotropyVector:{value:new oe},anisotropyMap:{value:null},anisotropyMapTransform:{value:new ue}}]),vertexShader:me.meshphysical_vert,fragmentShader:me.meshphysical_frag};const fu={r:0,b:0,g:0},l2=new we,jx=new ue;jx.set(-1,0,0,0,1,0,0,0,1);function c2(r,t,i,s,l,c){const f=new ie(0);let p=l===!0?0:1,m,d,x=null,v=0,g=null;function S(z){let F=z.isScene===!0?z.background:null;if(F&&F.isTexture){const C=z.backgroundBlurriness>0;F=t.get(F,C)}return F}function b(z){let F=!1;const C=S(z);C===null?E(f,p):C&&C.isColor&&(E(C,1),F=!0);const B=r.xr.getEnvironmentBlendMode();B==="additive"?i.buffers.color.setClear(0,0,0,1,c):B==="alpha-blend"&&i.buffers.color.setClear(0,0,0,0,c),(r.autoClear||F)&&(i.buffers.depth.setTest(!0),i.buffers.depth.setMask(!0),i.buffers.color.setMask(!0),r.clear(r.autoClearColor,r.autoClearDepth,r.autoClearStencil))}function w(z,F){const C=S(F);C&&(C.isCubeTexture||C.mapping===Du)?(d===void 0&&(d=new ln(new Dn(1,1,1),new $i({name:"BackgroundCubeMaterial",uniforms:so(ji.backgroundCube.uniforms),vertexShader:ji.backgroundCube.vertexShader,fragmentShader:ji.backgroundCube.fragmentShader,side:ti,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),d.geometry.deleteAttribute("normal"),d.geometry.deleteAttribute("uv"),d.onBeforeRender=function(B,O,U){this.matrixWorld.copyPosition(U.matrixWorld)},Object.defineProperty(d.material,"envMap",{get:function(){return this.uniforms.envMap.value}}),s.update(d)),d.material.uniforms.envMap.value=C,d.material.uniforms.backgroundBlurriness.value=F.backgroundBlurriness,d.material.uniforms.backgroundIntensity.value=F.backgroundIntensity,d.material.uniforms.backgroundRotation.value.setFromMatrix4(l2.makeRotationFromEuler(F.backgroundRotation)).transpose(),C.isCubeTexture&&C.isRenderTargetTexture===!1&&d.material.uniforms.backgroundRotation.value.premultiply(jx),d.material.toneMapped=Re.getTransfer(C.colorSpace)!==ke,(x!==C||v!==C.version||g!==r.toneMapping)&&(d.material.needsUpdate=!0,x=C,v=C.version,g=r.toneMapping),d.layers.enableAll(),z.unshift(d,d.geometry,d.material,0,0,null)):C&&C.isTexture&&(m===void 0&&(m=new ln(new gs(2,2),new $i({name:"BackgroundMaterial",uniforms:so(ji.background.uniforms),vertexShader:ji.background.vertexShader,fragmentShader:ji.background.fragmentShader,side:ds,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),m.geometry.deleteAttribute("normal"),Object.defineProperty(m.material,"map",{get:function(){return this.uniforms.t2D.value}}),s.update(m)),m.material.uniforms.t2D.value=C,m.material.uniforms.backgroundIntensity.value=F.backgroundIntensity,m.material.toneMapped=Re.getTransfer(C.colorSpace)!==ke,C.matrixAutoUpdate===!0&&C.updateMatrix(),m.material.uniforms.uvTransform.value.copy(C.matrix),(x!==C||v!==C.version||g!==r.toneMapping)&&(m.material.needsUpdate=!0,x=C,v=C.version,g=r.toneMapping),m.layers.enableAll(),z.unshift(m,m.geometry,m.material,0,0,null))}function E(z,F){z.getRGB(fu,kx(r)),i.buffers.color.setClear(fu.r,fu.g,fu.b,F,c)}function _(){d!==void 0&&(d.geometry.dispose(),d.material.dispose(),d=void 0),m!==void 0&&(m.geometry.dispose(),m.material.dispose(),m=void 0)}return{getClearColor:function(){return f},setClearColor:function(z,F=1){f.set(z),p=F,E(f,p)},getClearAlpha:function(){return p},setClearAlpha:function(z){p=z,E(f,p)},render:b,addToRenderList:w,dispose:_}}function u2(r,t){const i=r.getParameter(r.MAX_VERTEX_ATTRIBS),s={},l=g(null);let c=l,f=!1;function p(Y,J,dt,gt,et){let I=!1;const H=v(Y,gt,dt,J);c!==H&&(c=H,d(c.object)),I=S(Y,gt,dt,et),I&&b(Y,gt,dt,et),et!==null&&t.update(et,r.ELEMENT_ARRAY_BUFFER),(I||f)&&(f=!1,C(Y,J,dt,gt),et!==null&&r.bindBuffer(r.ELEMENT_ARRAY_BUFFER,t.get(et).buffer))}function m(){return r.createVertexArray()}function d(Y){return r.bindVertexArray(Y)}function x(Y){return r.deleteVertexArray(Y)}function v(Y,J,dt,gt){const et=gt.wireframe===!0;let I=s[J.id];I===void 0&&(I={},s[J.id]=I);const H=Y.isInstancedMesh===!0?Y.id:0;let q=I[H];q===void 0&&(q={},I[H]=q);let ht=q[dt.id];ht===void 0&&(ht={},q[dt.id]=ht);let Et=ht[et];return Et===void 0&&(Et=g(m()),ht[et]=Et),Et}function g(Y){const J=[],dt=[],gt=[];for(let et=0;et<i;et++)J[et]=0,dt[et]=0,gt[et]=0;return{geometry:null,program:null,wireframe:!1,newAttributes:J,enabledAttributes:dt,attributeDivisors:gt,object:Y,attributes:{},index:null}}function S(Y,J,dt,gt){const et=c.attributes,I=J.attributes;let H=0;const q=dt.getAttributes();for(const ht in q)if(q[ht].location>=0){const L=et[ht];let W=I[ht];if(W===void 0&&(ht==="instanceMatrix"&&Y.instanceMatrix&&(W=Y.instanceMatrix),ht==="instanceColor"&&Y.instanceColor&&(W=Y.instanceColor)),L===void 0||L.attribute!==W||W&&L.data!==W.data)return!0;H++}return c.attributesNum!==H||c.index!==gt}function b(Y,J,dt,gt){const et={},I=J.attributes;let H=0;const q=dt.getAttributes();for(const ht in q)if(q[ht].location>=0){let L=I[ht];L===void 0&&(ht==="instanceMatrix"&&Y.instanceMatrix&&(L=Y.instanceMatrix),ht==="instanceColor"&&Y.instanceColor&&(L=Y.instanceColor));const W={};W.attribute=L,L&&L.data&&(W.data=L.data),et[ht]=W,H++}c.attributes=et,c.attributesNum=H,c.index=gt}function w(){const Y=c.newAttributes;for(let J=0,dt=Y.length;J<dt;J++)Y[J]=0}function E(Y){_(Y,0)}function _(Y,J){const dt=c.newAttributes,gt=c.enabledAttributes,et=c.attributeDivisors;dt[Y]=1,gt[Y]===0&&(r.enableVertexAttribArray(Y),gt[Y]=1),et[Y]!==J&&(r.vertexAttribDivisor(Y,J),et[Y]=J)}function z(){const Y=c.newAttributes,J=c.enabledAttributes;for(let dt=0,gt=J.length;dt<gt;dt++)J[dt]!==Y[dt]&&(r.disableVertexAttribArray(dt),J[dt]=0)}function F(Y,J,dt,gt,et,I,H){H===!0?r.vertexAttribIPointer(Y,J,dt,et,I):r.vertexAttribPointer(Y,J,dt,gt,et,I)}function C(Y,J,dt,gt){w();const et=gt.attributes,I=dt.getAttributes(),H=J.defaultAttributeValues;for(const q in I){const ht=I[q];if(ht.location>=0){let Et=et[q];if(Et===void 0&&(q==="instanceMatrix"&&Y.instanceMatrix&&(Et=Y.instanceMatrix),q==="instanceColor"&&Y.instanceColor&&(Et=Y.instanceColor)),Et!==void 0){const L=Et.normalized,W=Et.itemSize,yt=t.get(Et);if(yt===void 0)continue;const At=yt.buffer,wt=yt.type,nt=yt.bytesPerElement,vt=wt===r.INT||wt===r.UNSIGNED_INT||Et.gpuType===Up;if(Et.isInterleavedBufferAttribute){const St=Et.data,Ut=St.stride,Jt=Et.offset;if(St.isInstancedInterleavedBuffer){for(let Kt=0;Kt<ht.locationSize;Kt++)_(ht.location+Kt,St.meshPerAttribute);Y.isInstancedMesh!==!0&&gt._maxInstanceCount===void 0&&(gt._maxInstanceCount=St.meshPerAttribute*St.count)}else for(let Kt=0;Kt<ht.locationSize;Kt++)E(ht.location+Kt);r.bindBuffer(r.ARRAY_BUFFER,At);for(let Kt=0;Kt<ht.locationSize;Kt++)F(ht.location+Kt,W/ht.locationSize,wt,L,Ut*nt,(Jt+W/ht.locationSize*Kt)*nt,vt)}else{if(Et.isInstancedBufferAttribute){for(let St=0;St<ht.locationSize;St++)_(ht.location+St,Et.meshPerAttribute);Y.isInstancedMesh!==!0&&gt._maxInstanceCount===void 0&&(gt._maxInstanceCount=Et.meshPerAttribute*Et.count)}else for(let St=0;St<ht.locationSize;St++)E(ht.location+St);r.bindBuffer(r.ARRAY_BUFFER,At);for(let St=0;St<ht.locationSize;St++)F(ht.location+St,W/ht.locationSize,wt,L,W*nt,W/ht.locationSize*St*nt,vt)}}else if(H!==void 0){const L=H[q];if(L!==void 0)switch(L.length){case 2:r.vertexAttrib2fv(ht.location,L);break;case 3:r.vertexAttrib3fv(ht.location,L);break;case 4:r.vertexAttrib4fv(ht.location,L);break;default:r.vertexAttrib1fv(ht.location,L)}}}}z()}function B(){P();for(const Y in s){const J=s[Y];for(const dt in J){const gt=J[dt];for(const et in gt){const I=gt[et];for(const H in I)x(I[H].object),delete I[H];delete gt[et]}}delete s[Y]}}function O(Y){if(s[Y.id]===void 0)return;const J=s[Y.id];for(const dt in J){const gt=J[dt];for(const et in gt){const I=gt[et];for(const H in I)x(I[H].object),delete I[H];delete gt[et]}}delete s[Y.id]}function U(Y){for(const J in s){const dt=s[J];for(const gt in dt){const et=dt[gt];if(et[Y.id]===void 0)continue;const I=et[Y.id];for(const H in I)x(I[H].object),delete I[H];delete et[Y.id]}}}function T(Y){for(const J in s){const dt=s[J],gt=Y.isInstancedMesh===!0?Y.id:0,et=dt[gt];if(et!==void 0){for(const I in et){const H=et[I];for(const q in H)x(H[q].object),delete H[q];delete et[I]}delete dt[gt],Object.keys(dt).length===0&&delete s[J]}}}function P(){$(),f=!0,c!==l&&(c=l,d(c.object))}function $(){l.geometry=null,l.program=null,l.wireframe=!1}return{setup:p,reset:P,resetDefaultState:$,dispose:B,releaseStatesOfGeometry:O,releaseStatesOfObject:T,releaseStatesOfProgram:U,initAttributes:w,enableAttribute:E,disableUnusedAttributes:z}}function f2(r,t,i){let s;function l(m){s=m}function c(m,d){r.drawArrays(s,m,d),i.update(d,s,1)}function f(m,d,x){x!==0&&(r.drawArraysInstanced(s,m,d,x),i.update(d,s,x))}function p(m,d,x){if(x===0)return;t.get("WEBGL_multi_draw").multiDrawArraysWEBGL(s,m,0,d,0,x);let g=0;for(let S=0;S<x;S++)g+=d[S];i.update(g,s,1)}this.setMode=l,this.render=c,this.renderInstances=f,this.renderMultiDraw=p}function h2(r,t,i,s){let l;function c(){if(l!==void 0)return l;if(t.has("EXT_texture_filter_anisotropic")===!0){const U=t.get("EXT_texture_filter_anisotropic");l=r.getParameter(U.MAX_TEXTURE_MAX_ANISOTROPY_EXT)}else l=0;return l}function f(U){return!(U!==Fi&&s.convert(U)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_FORMAT))}function p(U){const T=U===Ca&&(t.has("EXT_color_buffer_half_float")||t.has("EXT_color_buffer_float"));return!(U!==gi&&s.convert(U)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_TYPE)&&U!==Ii&&!T)}function m(U){if(U==="highp"){if(r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.HIGH_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.HIGH_FLOAT).precision>0)return"highp";U="mediump"}return U==="mediump"&&r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.MEDIUM_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.MEDIUM_FLOAT).precision>0?"mediump":"lowp"}let d=i.precision!==void 0?i.precision:"highp";const x=m(d);x!==d&&(se("WebGLRenderer:",d,"not supported, using",x,"instead."),d=x);const v=i.logarithmicDepthBuffer===!0,g=i.reversedDepthBuffer===!0&&t.has("EXT_clip_control");i.reversedDepthBuffer===!0&&g===!1&&se("WebGLRenderer: Unable to use reversed depth buffer due to missing EXT_clip_control extension. Fallback to default depth buffer.");const S=r.getParameter(r.MAX_TEXTURE_IMAGE_UNITS),b=r.getParameter(r.MAX_VERTEX_TEXTURE_IMAGE_UNITS),w=r.getParameter(r.MAX_TEXTURE_SIZE),E=r.getParameter(r.MAX_CUBE_MAP_TEXTURE_SIZE),_=r.getParameter(r.MAX_VERTEX_ATTRIBS),z=r.getParameter(r.MAX_VERTEX_UNIFORM_VECTORS),F=r.getParameter(r.MAX_VARYING_VECTORS),C=r.getParameter(r.MAX_FRAGMENT_UNIFORM_VECTORS),B=r.getParameter(r.MAX_SAMPLES),O=r.getParameter(r.SAMPLES);return{isWebGL2:!0,getMaxAnisotropy:c,getMaxPrecision:m,textureFormatReadable:f,textureTypeReadable:p,precision:d,logarithmicDepthBuffer:v,reversedDepthBuffer:g,maxTextures:S,maxVertexTextures:b,maxTextureSize:w,maxCubemapSize:E,maxAttributes:_,maxVertexUniforms:z,maxVaryings:F,maxFragmentUniforms:C,maxSamples:B,samples:O}}function d2(r){const t=this;let i=null,s=0,l=!1,c=!1;const f=new Gs,p=new ue,m={value:null,needsUpdate:!1};this.uniform=m,this.numPlanes=0,this.numIntersection=0,this.init=function(v,g){const S=v.length!==0||g||s!==0||l;return l=g,s=v.length,S},this.beginShadows=function(){c=!0,x(null)},this.endShadows=function(){c=!1},this.setGlobalState=function(v,g){i=x(v,g,0)},this.setState=function(v,g,S){const b=v.clippingPlanes,w=v.clipIntersection,E=v.clipShadows,_=r.get(v);if(!l||b===null||b.length===0||c&&!E)c?x(null):d();else{const z=c?0:s,F=z*4;let C=_.clippingState||null;m.value=C,C=x(b,g,F,S);for(let B=0;B!==F;++B)C[B]=i[B];_.clippingState=C,this.numIntersection=w?this.numPlanes:0,this.numPlanes+=z}};function d(){m.value!==i&&(m.value=i,m.needsUpdate=s>0),t.numPlanes=s,t.numIntersection=0}function x(v,g,S,b){const w=v!==null?v.length:0;let E=null;if(w!==0){if(E=m.value,b!==!0||E===null){const _=S+w*4,z=g.matrixWorldInverse;p.getNormalMatrix(z),(E===null||E.length<_)&&(E=new Float32Array(_));for(let F=0,C=S;F!==w;++F,C+=4)f.copy(v[F]).applyMatrix4(z,p),f.normal.toArray(E,C),E[C+3]=f.constant}m.value=E,m.needsUpdate=!0}return t.numPlanes=w,t.numIntersection=0,E}}const fs=4,Pv=[.125,.215,.35,.446,.526,.582],ks=20,p2=256,hl=new Kp,zv=new ie;let bd=null,Td=0,Ad=0,Rd=!1;const m2=new k;class Bv{constructor(t){this._renderer=t,this._pingPongRenderTarget=null,this._lodMax=0,this._cubeSize=0,this._sizeLods=[],this._sigmas=[],this._lodMeshes=[],this._backgroundBox=null,this._cubemapMaterial=null,this._equirectMaterial=null,this._blurMaterial=null,this._ggxMaterial=null}fromScene(t,i=0,s=.1,l=100,c={}){const{size:f=256,position:p=m2}=c;bd=this._renderer.getRenderTarget(),Td=this._renderer.getActiveCubeFace(),Ad=this._renderer.getActiveMipmapLevel(),Rd=this._renderer.xr.enabled,this._renderer.xr.enabled=!1,this._setSize(f);const m=this._allocateTargets();return m.depthBuffer=!0,this._sceneToCubeUV(t,s,l,m,p),i>0&&this._blur(m,0,0,i),this._applyPMREM(m),this._cleanup(m),m}fromEquirectangular(t,i=null){return this._fromTexture(t,i)}fromCubemap(t,i=null){return this._fromTexture(t,i)}compileCubemapShader(){this._cubemapMaterial===null&&(this._cubemapMaterial=Hv(),this._compileMaterial(this._cubemapMaterial))}compileEquirectangularShader(){this._equirectMaterial===null&&(this._equirectMaterial=Fv(),this._compileMaterial(this._equirectMaterial))}dispose(){this._dispose(),this._cubemapMaterial!==null&&this._cubemapMaterial.dispose(),this._equirectMaterial!==null&&this._equirectMaterial.dispose(),this._backgroundBox!==null&&(this._backgroundBox.geometry.dispose(),this._backgroundBox.material.dispose())}_setSize(t){this._lodMax=Math.floor(Math.log2(t)),this._cubeSize=Math.pow(2,this._lodMax)}_dispose(){this._blurMaterial!==null&&this._blurMaterial.dispose(),this._ggxMaterial!==null&&this._ggxMaterial.dispose(),this._pingPongRenderTarget!==null&&this._pingPongRenderTarget.dispose();for(let t=0;t<this._lodMeshes.length;t++)this._lodMeshes[t].geometry.dispose()}_cleanup(t){this._renderer.setRenderTarget(bd,Td,Ad),this._renderer.xr.enabled=Rd,t.scissorTest=!1,Zr(t,0,0,t.width,t.height)}_fromTexture(t,i){t.mapping===js||t.mapping===no?this._setSize(t.image.length===0?16:t.image[0].width||t.image[0].image.width):this._setSize(t.image.width/4),bd=this._renderer.getRenderTarget(),Td=this._renderer.getActiveCubeFace(),Ad=this._renderer.getActiveMipmapLevel(),Rd=this._renderer.xr.enabled,this._renderer.xr.enabled=!1;const s=i||this._allocateTargets();return this._textureToCubeUV(t,s),this._applyPMREM(s),this._cleanup(s),s}_allocateTargets(){const t=3*Math.max(this._cubeSize,112),i=4*this._cubeSize,s={magFilter:Gn,minFilter:Gn,generateMipmaps:!1,type:Ca,format:Fi,colorSpace:Mu,depthBuffer:!1},l=Iv(t,i,s);if(this._pingPongRenderTarget===null||this._pingPongRenderTarget.width!==t||this._pingPongRenderTarget.height!==i){this._pingPongRenderTarget!==null&&this._dispose(),this._pingPongRenderTarget=Iv(t,i,s);const{_lodMax:c}=this;({lodMeshes:this._lodMeshes,sizeLods:this._sizeLods,sigmas:this._sigmas}=g2(c)),this._blurMaterial=v2(c,t,i),this._ggxMaterial=_2(c,t,i)}return l}_compileMaterial(t){const i=new ln(new hn,t);this._renderer.compile(i,hl)}_sceneToCubeUV(t,i,s,l,c){const m=new mi(90,1,i,s),d=[1,-1,1,1,1,1],x=[1,1,1,-1,-1,-1],v=this._renderer,g=v.autoClear,S=v.toneMapping;v.getClearColor(zv),v.toneMapping=Ki,v.autoClear=!1,v.state.buffers.depth.getReversed()&&(v.setRenderTarget(l),v.clearDepth(),v.setRenderTarget(null)),this._backgroundBox===null&&(this._backgroundBox=new ln(new Dn,new io({name:"PMREM.Background",side:ti,depthWrite:!1,depthTest:!1})));const w=this._backgroundBox,E=w.material;let _=!1;const z=t.background;z?z.isColor&&(E.color.copy(z),t.background=null,_=!0):(E.color.copy(zv),_=!0);for(let F=0;F<6;F++){const C=F%3;C===0?(m.up.set(0,d[F],0),m.position.set(c.x,c.y,c.z),m.lookAt(c.x+x[F],c.y,c.z)):C===1?(m.up.set(0,0,d[F]),m.position.set(c.x,c.y,c.z),m.lookAt(c.x,c.y+x[F],c.z)):(m.up.set(0,d[F],0),m.position.set(c.x,c.y,c.z),m.lookAt(c.x,c.y,c.z+x[F]));const B=this._cubeSize;Zr(l,C*B,F>2?B:0,B,B),v.setRenderTarget(l),_&&v.render(w,m),v.render(t,m)}v.toneMapping=S,v.autoClear=g,t.background=z}_textureToCubeUV(t,i){const s=this._renderer,l=t.mapping===js||t.mapping===no;l?(this._cubemapMaterial===null&&(this._cubemapMaterial=Hv()),this._cubemapMaterial.uniforms.flipEnvMap.value=t.isRenderTargetTexture===!1?-1:1):this._equirectMaterial===null&&(this._equirectMaterial=Fv());const c=l?this._cubemapMaterial:this._equirectMaterial,f=this._lodMeshes[0];f.material=c;const p=c.uniforms;p.envMap.value=t;const m=this._cubeSize;Zr(i,0,0,3*m,2*m),s.setRenderTarget(i),s.render(f,hl)}_applyPMREM(t){const i=this._renderer,s=i.autoClear;i.autoClear=!1;const l=this._lodMeshes.length;for(let c=1;c<l;c++)this._applyGGXFilter(t,c-1,c);i.autoClear=s}_applyGGXFilter(t,i,s){const l=this._renderer,c=this._pingPongRenderTarget,f=this._ggxMaterial,p=this._lodMeshes[s];p.material=f;const m=f.uniforms,d=s/(this._lodMeshes.length-1),x=i/(this._lodMeshes.length-1),v=Math.sqrt(d*d-x*x),g=0+d*1.25,S=v*g,{_lodMax:b}=this,w=this._sizeLods[s],E=3*w*(s>b-fs?s-b+fs:0),_=4*(this._cubeSize-w);m.envMap.value=t.texture,m.roughness.value=S,m.mipInt.value=b-i,Zr(c,E,_,3*w,2*w),l.setRenderTarget(c),l.render(p,hl),m.envMap.value=c.texture,m.roughness.value=0,m.mipInt.value=b-s,Zr(t,E,_,3*w,2*w),l.setRenderTarget(t),l.render(p,hl)}_blur(t,i,s,l,c){const f=this._pingPongRenderTarget;this._halfBlur(t,f,i,s,l,"latitudinal",c),this._halfBlur(f,t,s,s,l,"longitudinal",c)}_halfBlur(t,i,s,l,c,f,p){const m=this._renderer,d=this._blurMaterial;f!=="latitudinal"&&f!=="longitudinal"&&Ae("blur direction must be either latitudinal or longitudinal!");const x=3,v=this._lodMeshes[l];v.material=d;const g=d.uniforms,S=this._sizeLods[s]-1,b=isFinite(c)?Math.PI/(2*S):2*Math.PI/(2*ks-1),w=c/b,E=isFinite(c)?1+Math.floor(x*w):ks;E>ks&&se(`sigmaRadians, ${c}, is too large and will clip, as it requested ${E} samples when the maximum is set to ${ks}`);const _=[];let z=0;for(let U=0;U<ks;++U){const T=U/w,P=Math.exp(-T*T/2);_.push(P),U===0?z+=P:U<E&&(z+=2*P)}for(let U=0;U<_.length;U++)_[U]=_[U]/z;g.envMap.value=t.texture,g.samples.value=E,g.weights.value=_,g.latitudinal.value=f==="latitudinal",p&&(g.poleAxis.value=p);const{_lodMax:F}=this;g.dTheta.value=b,g.mipInt.value=F-s;const C=this._sizeLods[l],B=3*C*(l>F-fs?l-F+fs:0),O=4*(this._cubeSize-C);Zr(i,B,O,3*C,2*C),m.setRenderTarget(i),m.render(v,hl)}}function g2(r){const t=[],i=[],s=[];let l=r;const c=r-fs+1+Pv.length;for(let f=0;f<c;f++){const p=Math.pow(2,l);t.push(p);let m=1/p;f>r-fs?m=Pv[f-r+fs-1]:f===0&&(m=0),i.push(m);const d=1/(p-2),x=-d,v=1+d,g=[x,x,v,x,v,v,x,x,v,v,x,v],S=6,b=6,w=3,E=2,_=1,z=new Float32Array(w*b*S),F=new Float32Array(E*b*S),C=new Float32Array(_*b*S);for(let O=0;O<S;O++){const U=O%3*2/3-1,T=O>2?0:-1,P=[U,T,0,U+2/3,T,0,U+2/3,T+1,0,U,T,0,U+2/3,T+1,0,U,T+1,0];z.set(P,w*b*O),F.set(g,E*b*O);const $=[O,O,O,O,O,O];C.set($,_*b*O)}const B=new hn;B.setAttribute("position",new vi(z,w)),B.setAttribute("uv",new vi(F,E)),B.setAttribute("faceIndex",new vi(C,_)),s.push(new ln(B,null)),l>fs&&l--}return{lodMeshes:s,sizeLods:t,sigmas:i}}function Iv(r,t,i){const s=new Qi(r,t,i);return s.texture.mapping=Du,s.texture.name="PMREM.cubeUv",s.scissorTest=!0,s}function Zr(r,t,i,s,l){r.viewport.set(t,i,s,l),r.scissor.set(t,i,s,l)}function _2(r,t,i){return new $i({name:"PMREMGGXConvolution",defines:{GGX_SAMPLES:p2,CUBEUV_TEXEL_WIDTH:1/t,CUBEUV_TEXEL_HEIGHT:1/i,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},roughness:{value:0},mipInt:{value:0}},vertexShader:Lu(),fragmentShader:`

			precision highp float;
			precision highp int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;
			uniform float roughness;
			uniform float mipInt;

			#define ENVMAP_TYPE_CUBE_UV
			#include <cube_uv_reflection_fragment>

			#define PI 3.14159265359

			// Van der Corput radical inverse
			float radicalInverse_VdC(uint bits) {
				bits = (bits << 16u) | (bits >> 16u);
				bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
				bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
				bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
				bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);
				return float(bits) * 2.3283064365386963e-10; // / 0x100000000
			}

			// Hammersley sequence
			vec2 hammersley(uint i, uint N) {
				return vec2(float(i) / float(N), radicalInverse_VdC(i));
			}

			// GGX VNDF importance sampling (Eric Heitz 2018)
			// "Sampling the GGX Distribution of Visible Normals"
			// https://jcgt.org/published/0007/04/01/
			vec3 importanceSampleGGX_VNDF(vec2 Xi, vec3 V, float roughness) {
				float alpha = roughness * roughness;

				// Section 4.1: Orthonormal basis
				vec3 T1 = vec3(1.0, 0.0, 0.0);
				vec3 T2 = cross(V, T1);

				// Section 4.2: Parameterization of projected area
				float r = sqrt(Xi.x);
				float phi = 2.0 * PI * Xi.y;
				float t1 = r * cos(phi);
				float t2 = r * sin(phi);
				float s = 0.5 * (1.0 + V.z);
				t2 = (1.0 - s) * sqrt(1.0 - t1 * t1) + s * t2;

				// Section 4.3: Reprojection onto hemisphere
				vec3 Nh = t1 * T1 + t2 * T2 + sqrt(max(0.0, 1.0 - t1 * t1 - t2 * t2)) * V;

				// Section 3.4: Transform back to ellipsoid configuration
				return normalize(vec3(alpha * Nh.x, alpha * Nh.y, max(0.0, Nh.z)));
			}

			void main() {
				vec3 N = normalize(vOutputDirection);
				vec3 V = N; // Assume view direction equals normal for pre-filtering

				vec3 prefilteredColor = vec3(0.0);
				float totalWeight = 0.0;

				// For very low roughness, just sample the environment directly
				if (roughness < 0.001) {
					gl_FragColor = vec4(bilinearCubeUV(envMap, N, mipInt), 1.0);
					return;
				}

				// Tangent space basis for VNDF sampling
				vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
				vec3 tangent = normalize(cross(up, N));
				vec3 bitangent = cross(N, tangent);

				for(uint i = 0u; i < uint(GGX_SAMPLES); i++) {
					vec2 Xi = hammersley(i, uint(GGX_SAMPLES));

					// For PMREM, V = N, so in tangent space V is always (0, 0, 1)
					vec3 H_tangent = importanceSampleGGX_VNDF(Xi, vec3(0.0, 0.0, 1.0), roughness);

					// Transform H back to world space
					vec3 H = normalize(tangent * H_tangent.x + bitangent * H_tangent.y + N * H_tangent.z);
					vec3 L = normalize(2.0 * dot(V, H) * H - V);

					float NdotL = max(dot(N, L), 0.0);

					if(NdotL > 0.0) {
						// Sample environment at fixed mip level
						// VNDF importance sampling handles the distribution filtering
						vec3 sampleColor = bilinearCubeUV(envMap, L, mipInt);

						// Weight by NdotL for the split-sum approximation
						// VNDF PDF naturally accounts for the visible microfacet distribution
						prefilteredColor += sampleColor * NdotL;
						totalWeight += NdotL;
					}
				}

				if (totalWeight > 0.0) {
					prefilteredColor = prefilteredColor / totalWeight;
				}

				gl_FragColor = vec4(prefilteredColor, 1.0);
			}
		`,blending:Aa,depthTest:!1,depthWrite:!1})}function v2(r,t,i){const s=new Float32Array(ks),l=new k(0,1,0);return new $i({name:"SphericalGaussianBlur",defines:{n:ks,CUBEUV_TEXEL_WIDTH:1/t,CUBEUV_TEXEL_HEIGHT:1/i,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},samples:{value:1},weights:{value:s},latitudinal:{value:!1},dTheta:{value:0},mipInt:{value:0},poleAxis:{value:l}},vertexShader:Lu(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;
			uniform int samples;
			uniform float weights[ n ];
			uniform bool latitudinal;
			uniform float dTheta;
			uniform float mipInt;
			uniform vec3 poleAxis;

			#define ENVMAP_TYPE_CUBE_UV
			#include <cube_uv_reflection_fragment>

			vec3 getSample( float theta, vec3 axis ) {

				float cosTheta = cos( theta );
				// Rodrigues' axis-angle rotation
				vec3 sampleDirection = vOutputDirection * cosTheta
					+ cross( axis, vOutputDirection ) * sin( theta )
					+ axis * dot( axis, vOutputDirection ) * ( 1.0 - cosTheta );

				return bilinearCubeUV( envMap, sampleDirection, mipInt );

			}

			void main() {

				vec3 axis = latitudinal ? poleAxis : cross( poleAxis, vOutputDirection );

				if ( all( equal( axis, vec3( 0.0 ) ) ) ) {

					axis = vec3( vOutputDirection.z, 0.0, - vOutputDirection.x );

				}

				axis = normalize( axis );

				gl_FragColor = vec4( 0.0, 0.0, 0.0, 1.0 );
				gl_FragColor.rgb += weights[ 0 ] * getSample( 0.0, axis );

				for ( int i = 1; i < n; i++ ) {

					if ( i >= samples ) {

						break;

					}

					float theta = dTheta * float( i );
					gl_FragColor.rgb += weights[ i ] * getSample( -1.0 * theta, axis );
					gl_FragColor.rgb += weights[ i ] * getSample( theta, axis );

				}

			}
		`,blending:Aa,depthTest:!1,depthWrite:!1})}function Fv(){return new $i({name:"EquirectangularToCubeUV",uniforms:{envMap:{value:null}},vertexShader:Lu(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;

			#include <common>

			void main() {

				vec3 outputDirection = normalize( vOutputDirection );
				vec2 uv = equirectUv( outputDirection );

				gl_FragColor = vec4( texture2D ( envMap, uv ).rgb, 1.0 );

			}
		`,blending:Aa,depthTest:!1,depthWrite:!1})}function Hv(){return new $i({name:"CubemapToCubeUV",uniforms:{envMap:{value:null},flipEnvMap:{value:-1}},vertexShader:Lu(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			uniform float flipEnvMap;

			varying vec3 vOutputDirection;

			uniform samplerCube envMap;

			void main() {

				gl_FragColor = textureCube( envMap, vec3( flipEnvMap * vOutputDirection.x, vOutputDirection.yz ) );

			}
		`,blending:Aa,depthTest:!1,depthWrite:!1})}function Lu(){return`

		precision mediump float;
		precision mediump int;

		attribute float faceIndex;

		varying vec3 vOutputDirection;

		// RH coordinate system; PMREM face-indexing convention
		vec3 getDirection( vec2 uv, float face ) {

			uv = 2.0 * uv - 1.0;

			vec3 direction = vec3( uv, 1.0 );

			if ( face == 0.0 ) {

				direction = direction.zyx; // ( 1, v, u ) pos x

			} else if ( face == 1.0 ) {

				direction = direction.xzy;
				direction.xz *= -1.0; // ( -u, 1, -v ) pos y

			} else if ( face == 2.0 ) {

				direction.x *= -1.0; // ( -u, v, 1 ) pos z

			} else if ( face == 3.0 ) {

				direction = direction.zyx;
				direction.xz *= -1.0; // ( -1, v, -u ) neg x

			} else if ( face == 4.0 ) {

				direction = direction.xzy;
				direction.xy *= -1.0; // ( -u, -1, v ) neg y

			} else if ( face == 5.0 ) {

				direction.z *= -1.0; // ( u, v, -1 ) neg z

			}

			return direction;

		}

		void main() {

			vOutputDirection = getDirection( uv, faceIndex );
			gl_Position = vec4( position, 1.0 );

		}
	`}class Zx extends Qi{constructor(t=1,i={}){super(t,t,i),this.isWebGLCubeRenderTarget=!0;const s={width:t,height:t,depth:1},l=[s,s,s,s,s,s];this.texture=new Hx(l),this._setTextureOptions(i),this.texture.isRenderTargetTexture=!0}fromEquirectangularTexture(t,i){this.texture.type=i.type,this.texture.colorSpace=i.colorSpace,this.texture.generateMipmaps=i.generateMipmaps,this.texture.minFilter=i.minFilter,this.texture.magFilter=i.magFilter;const s={uniforms:{tEquirect:{value:null}},vertexShader:`

				varying vec3 vWorldDirection;

				vec3 transformDirection( in vec3 dir, in mat4 matrix ) {

					return normalize( ( matrix * vec4( dir, 0.0 ) ).xyz );

				}

				void main() {

					vWorldDirection = transformDirection( position, modelMatrix );

					#include <begin_vertex>
					#include <project_vertex>

				}
			`,fragmentShader:`

				uniform sampler2D tEquirect;

				varying vec3 vWorldDirection;

				#include <common>

				void main() {

					vec3 direction = normalize( vWorldDirection );

					vec2 sampleUV = equirectUv( direction );

					gl_FragColor = texture2D( tEquirect, sampleUV );

				}
			`},l=new Dn(5,5,5),c=new $i({name:"CubemapFromEquirect",uniforms:so(s.uniforms),vertexShader:s.vertexShader,fragmentShader:s.fragmentShader,side:ti,blending:Aa});c.uniforms.tEquirect.value=i;const f=new ln(l,c),p=i.minFilter;return i.minFilter===Xs&&(i.minFilter=Gn),new Mb(1,10,this).update(t,f),i.minFilter=p,f.geometry.dispose(),f.material.dispose(),this}clear(t,i=!0,s=!0,l=!0){const c=t.getRenderTarget();for(let f=0;f<6;f++)t.setRenderTarget(this,f),t.clear(i,s,l);t.setRenderTarget(c)}}function x2(r){let t=new WeakMap,i=new WeakMap,s=null;function l(g,S=!1){return g==null?null:S?f(g):c(g)}function c(g){if(g&&g.isTexture){const S=g.mapping;if(S===qh||S===Yh)if(t.has(g)){const b=t.get(g).texture;return p(b,g.mapping)}else{const b=g.image;if(b&&b.height>0){const w=new Zx(b.height);return w.fromEquirectangularTexture(r,g),t.set(g,w),g.addEventListener("dispose",d),p(w.texture,g.mapping)}else return null}}return g}function f(g){if(g&&g.isTexture){const S=g.mapping,b=S===qh||S===Yh,w=S===js||S===no;if(b||w){let E=i.get(g);const _=E!==void 0?E.texture.pmremVersion:0;if(g.isRenderTargetTexture&&g.pmremVersion!==_)return s===null&&(s=new Bv(r)),E=b?s.fromEquirectangular(g,E):s.fromCubemap(g,E),E.texture.pmremVersion=g.pmremVersion,i.set(g,E),E.texture;if(E!==void 0)return E.texture;{const z=g.image;return b&&z&&z.height>0||w&&z&&m(z)?(s===null&&(s=new Bv(r)),E=b?s.fromEquirectangular(g):s.fromCubemap(g),E.texture.pmremVersion=g.pmremVersion,i.set(g,E),g.addEventListener("dispose",x),E.texture):null}}}return g}function p(g,S){return S===qh?g.mapping=js:S===Yh&&(g.mapping=no),g}function m(g){let S=0;const b=6;for(let w=0;w<b;w++)g[w]!==void 0&&S++;return S===b}function d(g){const S=g.target;S.removeEventListener("dispose",d);const b=t.get(S);b!==void 0&&(t.delete(S),b.dispose())}function x(g){const S=g.target;S.removeEventListener("dispose",x);const b=i.get(S);b!==void 0&&(i.delete(S),b.dispose())}function v(){t=new WeakMap,i=new WeakMap,s!==null&&(s.dispose(),s=null)}return{get:l,dispose:v}}function y2(r){const t={};function i(s){if(t[s]!==void 0)return t[s];const l=r.getExtension(s);return t[s]=l,l}return{has:function(s){return i(s)!==null},init:function(){i("EXT_color_buffer_float"),i("WEBGL_clip_cull_distance"),i("OES_texture_float_linear"),i("EXT_color_buffer_half_float"),i("WEBGL_multisampled_render_to_texture"),i("WEBGL_render_shared_exponent")},get:function(s){const l=i(s);return l===null&&Jr("WebGLRenderer: "+s+" extension not supported."),l}}}function S2(r,t,i,s){const l={},c=new WeakMap;function f(v){const g=v.target;g.index!==null&&t.remove(g.index);for(const b in g.attributes)t.remove(g.attributes[b]);g.removeEventListener("dispose",f),delete l[g.id];const S=c.get(g);S&&(t.remove(S),c.delete(g)),s.releaseStatesOfGeometry(g),g.isInstancedBufferGeometry===!0&&delete g._maxInstanceCount,i.memory.geometries--}function p(v,g){return l[g.id]===!0||(g.addEventListener("dispose",f),l[g.id]=!0,i.memory.geometries++),g}function m(v){const g=v.attributes;for(const S in g)t.update(g[S],r.ARRAY_BUFFER)}function d(v){const g=[],S=v.index,b=v.attributes.position;let w=0;if(b===void 0)return;if(S!==null){const z=S.array;w=S.version;for(let F=0,C=z.length;F<C;F+=3){const B=z[F+0],O=z[F+1],U=z[F+2];g.push(B,O,O,U,U,B)}}else{const z=b.array;w=b.version;for(let F=0,C=z.length/3-1;F<C;F+=3){const B=F+0,O=F+1,U=F+2;g.push(B,O,O,U,U,B)}}const E=new(b.count>=65535?Ox:Lx)(g,1);E.version=w;const _=c.get(v);_&&t.remove(_),c.set(v,E)}function x(v){const g=c.get(v);if(g){const S=v.index;S!==null&&g.version<S.version&&d(v)}else d(v);return c.get(v)}return{get:p,update:m,getWireframeAttribute:x}}function M2(r,t,i){let s;function l(v){s=v}let c,f;function p(v){c=v.type,f=v.bytesPerElement}function m(v,g){r.drawElements(s,g,c,v*f),i.update(g,s,1)}function d(v,g,S){S!==0&&(r.drawElementsInstanced(s,g,c,v*f,S),i.update(g,s,S))}function x(v,g,S){if(S===0)return;t.get("WEBGL_multi_draw").multiDrawElementsWEBGL(s,g,0,c,v,0,S);let w=0;for(let E=0;E<S;E++)w+=g[E];i.update(w,s,1)}this.setMode=l,this.setIndex=p,this.render=m,this.renderInstances=d,this.renderMultiDraw=x}function E2(r){const t={geometries:0,textures:0},i={frame:0,calls:0,triangles:0,points:0,lines:0};function s(c,f,p){switch(i.calls++,f){case r.TRIANGLES:i.triangles+=p*(c/3);break;case r.LINES:i.lines+=p*(c/2);break;case r.LINE_STRIP:i.lines+=p*(c-1);break;case r.LINE_LOOP:i.lines+=p*c;break;case r.POINTS:i.points+=p*c;break;default:Ae("WebGLInfo: Unknown draw mode:",f);break}}function l(){i.calls=0,i.triangles=0,i.points=0,i.lines=0}return{memory:t,render:i,programs:null,autoReset:!0,reset:l,update:s}}function b2(r,t,i){const s=new WeakMap,l=new cn;function c(f,p,m){const d=f.morphTargetInfluences,x=p.morphAttributes.position||p.morphAttributes.normal||p.morphAttributes.color,v=x!==void 0?x.length:0;let g=s.get(p);if(g===void 0||g.count!==v){let $=function(){T.dispose(),s.delete(p),p.removeEventListener("dispose",$)};var S=$;g!==void 0&&g.texture.dispose();const b=p.morphAttributes.position!==void 0,w=p.morphAttributes.normal!==void 0,E=p.morphAttributes.color!==void 0,_=p.morphAttributes.position||[],z=p.morphAttributes.normal||[],F=p.morphAttributes.color||[];let C=0;b===!0&&(C=1),w===!0&&(C=2),E===!0&&(C=3);let B=p.attributes.position.count*C,O=1;B>t.maxTextureSize&&(O=Math.ceil(B/t.maxTextureSize),B=t.maxTextureSize);const U=new Float32Array(B*O*4*v),T=new Ux(U,B,O,v);T.type=Ii,T.needsUpdate=!0;const P=C*4;for(let Y=0;Y<v;Y++){const J=_[Y],dt=z[Y],gt=F[Y],et=B*O*4*Y;for(let I=0;I<J.count;I++){const H=I*P;b===!0&&(l.fromBufferAttribute(J,I),U[et+H+0]=l.x,U[et+H+1]=l.y,U[et+H+2]=l.z,U[et+H+3]=0),w===!0&&(l.fromBufferAttribute(dt,I),U[et+H+4]=l.x,U[et+H+5]=l.y,U[et+H+6]=l.z,U[et+H+7]=0),E===!0&&(l.fromBufferAttribute(gt,I),U[et+H+8]=l.x,U[et+H+9]=l.y,U[et+H+10]=l.z,U[et+H+11]=gt.itemSize===4?l.w:1)}}g={count:v,texture:T,size:new oe(B,O)},s.set(p,g),p.addEventListener("dispose",$)}if(f.isInstancedMesh===!0&&f.morphTexture!==null)m.getUniforms().setValue(r,"morphTexture",f.morphTexture,i);else{let b=0;for(let E=0;E<d.length;E++)b+=d[E];const w=p.morphTargetsRelative?1:1-b;m.getUniforms().setValue(r,"morphTargetBaseInfluence",w),m.getUniforms().setValue(r,"morphTargetInfluences",d)}m.getUniforms().setValue(r,"morphTargetsTexture",g.texture,i),m.getUniforms().setValue(r,"morphTargetsTextureSize",g.size)}return{update:c}}function T2(r,t,i,s,l){let c=new WeakMap;function f(d){const x=l.render.frame,v=d.geometry,g=t.get(d,v);if(c.get(g)!==x&&(t.update(g),c.set(g,x)),d.isInstancedMesh&&(d.hasEventListener("dispose",m)===!1&&d.addEventListener("dispose",m),c.get(d)!==x&&(i.update(d.instanceMatrix,r.ARRAY_BUFFER),d.instanceColor!==null&&i.update(d.instanceColor,r.ARRAY_BUFFER),c.set(d,x))),d.isSkinnedMesh){const S=d.skeleton;c.get(S)!==x&&(S.update(),c.set(S,x))}return g}function p(){c=new WeakMap}function m(d){const x=d.target;x.removeEventListener("dispose",m),s.releaseStatesOfObject(x),i.remove(x.instanceMatrix),x.instanceColor!==null&&i.remove(x.instanceColor)}return{update:f,dispose:p}}const A2={[_x]:"LINEAR_TONE_MAPPING",[vx]:"REINHARD_TONE_MAPPING",[xx]:"CINEON_TONE_MAPPING",[Dp]:"ACES_FILMIC_TONE_MAPPING",[Sx]:"AGX_TONE_MAPPING",[Mx]:"NEUTRAL_TONE_MAPPING",[yx]:"CUSTOM_TONE_MAPPING"};function R2(r,t,i,s,l,c){const f=new Qi(t,i,{type:r,depthBuffer:l,stencilBuffer:c,samples:s?4:0,depthTexture:l?new ao(t,i):void 0}),p=new Qi(t,i,{type:Ca,depthBuffer:!1,stencilBuffer:!1}),m=new hn;m.setAttribute("position",new En([-1,3,0,-1,-1,0,3,-1,0],3)),m.setAttribute("uv",new En([0,2,0,0,2,0],2));const d=new db({uniforms:{tDiffuse:{value:null}},vertexShader:`
			precision highp float;

			uniform mat4 modelViewMatrix;
			uniform mat4 projectionMatrix;

			attribute vec3 position;
			attribute vec2 uv;

			varying vec2 vUv;

			void main() {
				vUv = uv;
				gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
			}`,fragmentShader:`
			precision highp float;

			uniform sampler2D tDiffuse;

			varying vec2 vUv;

			#include <tonemapping_pars_fragment>
			#include <colorspace_pars_fragment>

			void main() {
				gl_FragColor = texture2D( tDiffuse, vUv );

				#ifdef LINEAR_TONE_MAPPING
					gl_FragColor.rgb = LinearToneMapping( gl_FragColor.rgb );
				#elif defined( REINHARD_TONE_MAPPING )
					gl_FragColor.rgb = ReinhardToneMapping( gl_FragColor.rgb );
				#elif defined( CINEON_TONE_MAPPING )
					gl_FragColor.rgb = CineonToneMapping( gl_FragColor.rgb );
				#elif defined( ACES_FILMIC_TONE_MAPPING )
					gl_FragColor.rgb = ACESFilmicToneMapping( gl_FragColor.rgb );
				#elif defined( AGX_TONE_MAPPING )
					gl_FragColor.rgb = AgXToneMapping( gl_FragColor.rgb );
				#elif defined( NEUTRAL_TONE_MAPPING )
					gl_FragColor.rgb = NeutralToneMapping( gl_FragColor.rgb );
				#elif defined( CUSTOM_TONE_MAPPING )
					gl_FragColor.rgb = CustomToneMapping( gl_FragColor.rgb );
				#endif

				#ifdef SRGB_TRANSFER
					gl_FragColor = sRGBTransferOETF( gl_FragColor );
				#endif
			}`,depthTest:!1,depthWrite:!1}),x=new ln(m,d),v=new Kp(-1,1,1,-1,0,1);let g=null,S=null,b=!1,w,E=null,_=[],z=!1;this.setSize=function(F,C){f.setSize(F,C),p.setSize(F,C);for(let B=0;B<_.length;B++){const O=_[B];O.setSize&&O.setSize(F,C)}},this.setEffects=function(F){_=F,z=_.length>0&&_[0].isRenderPass===!0;const C=f.width,B=f.height;for(let O=0;O<_.length;O++){const U=_[O];U.setSize&&U.setSize(C,B)}},this.begin=function(F,C){if(b||F.toneMapping===Ki&&_.length===0)return!1;if(E=C,C!==null){const B=C.width,O=C.height;(f.width!==B||f.height!==O)&&this.setSize(B,O)}return z===!1&&F.setRenderTarget(f),w=F.toneMapping,F.toneMapping=Ki,!0},this.hasRenderPass=function(){return z},this.end=function(F,C){F.toneMapping=w,b=!0;let B=f,O=p;for(let U=0;U<_.length;U++){const T=_[U];if(T.enabled!==!1&&(T.render(F,O,B,C),T.needsSwap!==!1)){const P=B;B=O,O=P}}if(g!==F.outputColorSpace||S!==F.toneMapping){g=F.outputColorSpace,S=F.toneMapping,d.defines={},Re.getTransfer(g)===ke&&(d.defines.SRGB_TRANSFER="");const U=A2[S];U&&(d.defines[U]=""),d.needsUpdate=!0}d.uniforms.tDiffuse.value=B.texture,F.setRenderTarget(E),F.render(x,v),E=null,b=!1},this.isCompositing=function(){return b},this.dispose=function(){f.depthTexture&&f.depthTexture.dispose(),f.dispose(),p.dispose(),m.dispose(),d.dispose()}}const Kx=new Vn,bp=new ao(1,1),Qx=new Ux,Jx=new FE,$x=new Hx,Gv=[],Vv=[],kv=new Float32Array(16),Xv=new Float32Array(9),Wv=new Float32Array(4);function oo(r,t,i){const s=r[0];if(s<=0||s>0)return r;const l=t*i;let c=Gv[l];if(c===void 0&&(c=new Float32Array(l),Gv[l]=c),t!==0){s.toArray(c,0);for(let f=1,p=0;f!==t;++f)p+=i,r[f].toArray(c,p)}return c}function An(r,t){if(r.length!==t.length)return!1;for(let i=0,s=r.length;i<s;i++)if(r[i]!==t[i])return!1;return!0}function Rn(r,t){for(let i=0,s=t.length;i<s;i++)r[i]=t[i]}function Ou(r,t){let i=Vv[t];i===void 0&&(i=new Int32Array(t),Vv[t]=i);for(let s=0;s!==t;++s)i[s]=r.allocateTextureUnit();return i}function w2(r,t){const i=this.cache;i[0]!==t&&(r.uniform1f(this.addr,t),i[0]=t)}function C2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(r.uniform2f(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(An(i,t))return;r.uniform2fv(this.addr,t),Rn(i,t)}}function D2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(r.uniform3f(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else if(t.r!==void 0)(i[0]!==t.r||i[1]!==t.g||i[2]!==t.b)&&(r.uniform3f(this.addr,t.r,t.g,t.b),i[0]=t.r,i[1]=t.g,i[2]=t.b);else{if(An(i,t))return;r.uniform3fv(this.addr,t),Rn(i,t)}}function U2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(r.uniform4f(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(An(i,t))return;r.uniform4fv(this.addr,t),Rn(i,t)}}function N2(r,t){const i=this.cache,s=t.elements;if(s===void 0){if(An(i,t))return;r.uniformMatrix2fv(this.addr,!1,t),Rn(i,t)}else{if(An(i,s))return;Wv.set(s),r.uniformMatrix2fv(this.addr,!1,Wv),Rn(i,s)}}function L2(r,t){const i=this.cache,s=t.elements;if(s===void 0){if(An(i,t))return;r.uniformMatrix3fv(this.addr,!1,t),Rn(i,t)}else{if(An(i,s))return;Xv.set(s),r.uniformMatrix3fv(this.addr,!1,Xv),Rn(i,s)}}function O2(r,t){const i=this.cache,s=t.elements;if(s===void 0){if(An(i,t))return;r.uniformMatrix4fv(this.addr,!1,t),Rn(i,t)}else{if(An(i,s))return;kv.set(s),r.uniformMatrix4fv(this.addr,!1,kv),Rn(i,s)}}function P2(r,t){const i=this.cache;i[0]!==t&&(r.uniform1i(this.addr,t),i[0]=t)}function z2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(r.uniform2i(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(An(i,t))return;r.uniform2iv(this.addr,t),Rn(i,t)}}function B2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(r.uniform3i(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else{if(An(i,t))return;r.uniform3iv(this.addr,t),Rn(i,t)}}function I2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(r.uniform4i(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(An(i,t))return;r.uniform4iv(this.addr,t),Rn(i,t)}}function F2(r,t){const i=this.cache;i[0]!==t&&(r.uniform1ui(this.addr,t),i[0]=t)}function H2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(r.uniform2ui(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(An(i,t))return;r.uniform2uiv(this.addr,t),Rn(i,t)}}function G2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(r.uniform3ui(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else{if(An(i,t))return;r.uniform3uiv(this.addr,t),Rn(i,t)}}function V2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(r.uniform4ui(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(An(i,t))return;r.uniform4uiv(this.addr,t),Rn(i,t)}}function k2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l);let c;this.type===r.SAMPLER_2D_SHADOW?(bp.compareFunction=i.isReversedDepthBuffer()?Fp:Ip,c=bp):c=Kx,i.setTexture2D(t||c,l)}function X2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l),i.setTexture3D(t||Jx,l)}function W2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l),i.setTextureCube(t||$x,l)}function q2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l),i.setTexture2DArray(t||Qx,l)}function Y2(r){switch(r){case 5126:return w2;case 35664:return C2;case 35665:return D2;case 35666:return U2;case 35674:return N2;case 35675:return L2;case 35676:return O2;case 5124:case 35670:return P2;case 35667:case 35671:return z2;case 35668:case 35672:return B2;case 35669:case 35673:return I2;case 5125:return F2;case 36294:return H2;case 36295:return G2;case 36296:return V2;case 35678:case 36198:case 36298:case 36306:case 35682:return k2;case 35679:case 36299:case 36307:return X2;case 35680:case 36300:case 36308:case 36293:return W2;case 36289:case 36303:case 36311:case 36292:return q2}}function j2(r,t){r.uniform1fv(this.addr,t)}function Z2(r,t){const i=oo(t,this.size,2);r.uniform2fv(this.addr,i)}function K2(r,t){const i=oo(t,this.size,3);r.uniform3fv(this.addr,i)}function Q2(r,t){const i=oo(t,this.size,4);r.uniform4fv(this.addr,i)}function J2(r,t){const i=oo(t,this.size,4);r.uniformMatrix2fv(this.addr,!1,i)}function $2(r,t){const i=oo(t,this.size,9);r.uniformMatrix3fv(this.addr,!1,i)}function t3(r,t){const i=oo(t,this.size,16);r.uniformMatrix4fv(this.addr,!1,i)}function e3(r,t){r.uniform1iv(this.addr,t)}function n3(r,t){r.uniform2iv(this.addr,t)}function i3(r,t){r.uniform3iv(this.addr,t)}function a3(r,t){r.uniform4iv(this.addr,t)}function s3(r,t){r.uniform1uiv(this.addr,t)}function r3(r,t){r.uniform2uiv(this.addr,t)}function o3(r,t){r.uniform3uiv(this.addr,t)}function l3(r,t){r.uniform4uiv(this.addr,t)}function c3(r,t,i){const s=this.cache,l=t.length,c=Ou(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));let f;this.type===r.SAMPLER_2D_SHADOW?f=bp:f=Kx;for(let p=0;p!==l;++p)i.setTexture2D(t[p]||f,c[p])}function u3(r,t,i){const s=this.cache,l=t.length,c=Ou(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));for(let f=0;f!==l;++f)i.setTexture3D(t[f]||Jx,c[f])}function f3(r,t,i){const s=this.cache,l=t.length,c=Ou(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));for(let f=0;f!==l;++f)i.setTextureCube(t[f]||$x,c[f])}function h3(r,t,i){const s=this.cache,l=t.length,c=Ou(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));for(let f=0;f!==l;++f)i.setTexture2DArray(t[f]||Qx,c[f])}function d3(r){switch(r){case 5126:return j2;case 35664:return Z2;case 35665:return K2;case 35666:return Q2;case 35674:return J2;case 35675:return $2;case 35676:return t3;case 5124:case 35670:return e3;case 35667:case 35671:return n3;case 35668:case 35672:return i3;case 35669:case 35673:return a3;case 5125:return s3;case 36294:return r3;case 36295:return o3;case 36296:return l3;case 35678:case 36198:case 36298:case 36306:case 35682:return c3;case 35679:case 36299:case 36307:return u3;case 35680:case 36300:case 36308:case 36293:return f3;case 36289:case 36303:case 36311:case 36292:return h3}}class p3{constructor(t,i,s){this.id=t,this.addr=s,this.cache=[],this.type=i.type,this.setValue=Y2(i.type)}}class m3{constructor(t,i,s){this.id=t,this.addr=s,this.cache=[],this.type=i.type,this.size=i.size,this.setValue=d3(i.type)}}class g3{constructor(t){this.id=t,this.seq=[],this.map={}}setValue(t,i,s){const l=this.seq;for(let c=0,f=l.length;c!==f;++c){const p=l[c];p.setValue(t,i[p.id],s)}}}const wd=/(\w+)(\])?(\[|\.)?/g;function qv(r,t){r.seq.push(t),r.map[t.id]=t}function _3(r,t,i){const s=r.name,l=s.length;for(wd.lastIndex=0;;){const c=wd.exec(s),f=wd.lastIndex;let p=c[1];const m=c[2]==="]",d=c[3];if(m&&(p=p|0),d===void 0||d==="["&&f+2===l){qv(i,d===void 0?new p3(p,r,t):new m3(p,r,t));break}else{let v=i.map[p];v===void 0&&(v=new g3(p),qv(i,v)),i=v}}}class vu{constructor(t,i){this.seq=[],this.map={};const s=t.getProgramParameter(i,t.ACTIVE_UNIFORMS);for(let f=0;f<s;++f){const p=t.getActiveUniform(i,f),m=t.getUniformLocation(i,p.name);_3(p,m,this)}const l=[],c=[];for(const f of this.seq)f.type===t.SAMPLER_2D_SHADOW||f.type===t.SAMPLER_CUBE_SHADOW||f.type===t.SAMPLER_2D_ARRAY_SHADOW?l.push(f):c.push(f);l.length>0&&(this.seq=l.concat(c))}setValue(t,i,s,l){const c=this.map[i];c!==void 0&&c.setValue(t,s,l)}setOptional(t,i,s){const l=i[s];l!==void 0&&this.setValue(t,s,l)}static upload(t,i,s,l){for(let c=0,f=i.length;c!==f;++c){const p=i[c],m=s[p.id];m.needsUpdate!==!1&&p.setValue(t,m.value,l)}}static seqWithValue(t,i){const s=[];for(let l=0,c=t.length;l!==c;++l){const f=t[l];f.id in i&&s.push(f)}return s}}function Yv(r,t,i){const s=r.createShader(t);return r.shaderSource(s,i),r.compileShader(s),s}const v3=37297;let x3=0;function y3(r,t){const i=r.split(`
`),s=[],l=Math.max(t-6,0),c=Math.min(t+6,i.length);for(let f=l;f<c;f++){const p=f+1;s.push(`${p===t?">":" "} ${p}: ${i[f]}`)}return s.join(`
`)}const jv=new ue;function S3(r){Re._getMatrix(jv,Re.workingColorSpace,r);const t=`mat3( ${jv.elements.map(i=>i.toFixed(4))} )`;switch(Re.getTransfer(r)){case Eu:return[t,"LinearTransferOETF"];case ke:return[t,"sRGBTransferOETF"];default:return se("WebGLProgram: Unsupported color space: ",r),[t,"LinearTransferOETF"]}}function Zv(r,t,i){const s=r.getShaderParameter(t,r.COMPILE_STATUS),c=(r.getShaderInfoLog(t)||"").trim();if(s&&c==="")return"";const f=/ERROR: 0:(\d+)/.exec(c);if(f){const p=parseInt(f[1]);return i.toUpperCase()+`

`+c+`

`+y3(r.getShaderSource(t),p)}else return c}function M3(r,t){const i=S3(t);return[`vec4 ${r}( vec4 value ) {`,`	return ${i[1]}( vec4( value.rgb * ${i[0]}, value.a ) );`,"}"].join(`
`)}const E3={[_x]:"Linear",[vx]:"Reinhard",[xx]:"Cineon",[Dp]:"ACESFilmic",[Sx]:"AgX",[Mx]:"Neutral",[yx]:"Custom"};function b3(r,t){const i=E3[t];return i===void 0?(se("WebGLProgram: Unsupported toneMapping:",t),"vec3 "+r+"( vec3 color ) { return LinearToneMapping( color ); }"):"vec3 "+r+"( vec3 color ) { return "+i+"ToneMapping( color ); }"}const hu=new k;function T3(){Re.getLuminanceCoefficients(hu);const r=hu.x.toFixed(4),t=hu.y.toFixed(4),i=hu.z.toFixed(4);return["float luminance( const in vec3 rgb ) {",`	const vec3 weights = vec3( ${r}, ${t}, ${i} );`,"	return dot( weights, rgb );","}"].join(`
`)}function A3(r){return[r.extensionClipCullDistance?"#extension GL_ANGLE_clip_cull_distance : require":"",r.extensionMultiDraw?"#extension GL_ANGLE_multi_draw : require":""].filter(ml).join(`
`)}function R3(r){const t=[];for(const i in r){const s=r[i];s!==!1&&t.push("#define "+i+" "+s)}return t.join(`
`)}function w3(r,t){const i={},s=r.getProgramParameter(t,r.ACTIVE_ATTRIBUTES);for(let l=0;l<s;l++){const c=r.getActiveAttrib(t,l),f=c.name;let p=1;c.type===r.FLOAT_MAT2&&(p=2),c.type===r.FLOAT_MAT3&&(p=3),c.type===r.FLOAT_MAT4&&(p=4),i[f]={type:c.type,location:r.getAttribLocation(t,f),locationSize:p}}return i}function ml(r){return r!==""}function Kv(r,t){const i=t.numSpotLightShadows+t.numSpotLightMaps-t.numSpotLightShadowsWithMaps;return r.replace(/NUM_DIR_LIGHTS/g,t.numDirLights).replace(/NUM_SPOT_LIGHTS/g,t.numSpotLights).replace(/NUM_SPOT_LIGHT_MAPS/g,t.numSpotLightMaps).replace(/NUM_SPOT_LIGHT_COORDS/g,i).replace(/NUM_RECT_AREA_LIGHTS/g,t.numRectAreaLights).replace(/NUM_POINT_LIGHTS/g,t.numPointLights).replace(/NUM_HEMI_LIGHTS/g,t.numHemiLights).replace(/NUM_DIR_LIGHT_SHADOWS/g,t.numDirLightShadows).replace(/NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS/g,t.numSpotLightShadowsWithMaps).replace(/NUM_SPOT_LIGHT_SHADOWS/g,t.numSpotLightShadows).replace(/NUM_POINT_LIGHT_SHADOWS/g,t.numPointLightShadows)}function Qv(r,t){return r.replace(/NUM_CLIPPING_PLANES/g,t.numClippingPlanes).replace(/UNION_CLIPPING_PLANES/g,t.numClippingPlanes-t.numClipIntersection)}const C3=/^[ \t]*#include +<([\w\d./]+)>/gm;function Tp(r){return r.replace(C3,U3)}const D3=new Map;function U3(r,t){let i=me[t];if(i===void 0){const s=D3.get(t);if(s!==void 0)i=me[s],se('WebGLRenderer: Shader chunk "%s" has been deprecated. Use "%s" instead.',t,s);else throw new Error("THREE.WebGLProgram: Can not resolve #include <"+t+">")}return Tp(i)}const N3=/#pragma unroll_loop_start\s+for\s*\(\s*int\s+i\s*=\s*(\d+)\s*;\s*i\s*<\s*(\d+)\s*;\s*i\s*\+\+\s*\)\s*{([\s\S]+?)}\s+#pragma unroll_loop_end/g;function Jv(r){return r.replace(N3,L3)}function L3(r,t,i,s){let l="";for(let c=parseInt(t);c<parseInt(i);c++)l+=s.replace(/\[\s*i\s*\]/g,"[ "+c+" ]").replace(/UNROLLED_LOOP_INDEX/g,c);return l}function $v(r){let t=`precision ${r.precision} float;
	precision ${r.precision} int;
	precision ${r.precision} sampler2D;
	precision ${r.precision} samplerCube;
	precision ${r.precision} sampler3D;
	precision ${r.precision} sampler2DArray;
	precision ${r.precision} sampler2DShadow;
	precision ${r.precision} samplerCubeShadow;
	precision ${r.precision} sampler2DArrayShadow;
	precision ${r.precision} isampler2D;
	precision ${r.precision} isampler3D;
	precision ${r.precision} isamplerCube;
	precision ${r.precision} isampler2DArray;
	precision ${r.precision} usampler2D;
	precision ${r.precision} usampler3D;
	precision ${r.precision} usamplerCube;
	precision ${r.precision} usampler2DArray;
	`;return r.precision==="highp"?t+=`
#define HIGH_PRECISION`:r.precision==="mediump"?t+=`
#define MEDIUM_PRECISION`:r.precision==="lowp"&&(t+=`
#define LOW_PRECISION`),t}const O3={[_l]:"SHADOWMAP_TYPE_PCF",[pl]:"SHADOWMAP_TYPE_VSM"};function P3(r){return O3[r.shadowMapType]||"SHADOWMAP_TYPE_BASIC"}const z3={[js]:"ENVMAP_TYPE_CUBE",[no]:"ENVMAP_TYPE_CUBE",[Du]:"ENVMAP_TYPE_CUBE_UV"};function B3(r){return r.envMap===!1?"ENVMAP_TYPE_CUBE":z3[r.envMapMode]||"ENVMAP_TYPE_CUBE"}const I3={[no]:"ENVMAP_MODE_REFRACTION"};function F3(r){return r.envMap===!1?"ENVMAP_MODE_REFLECTION":I3[r.envMapMode]||"ENVMAP_MODE_REFLECTION"}const H3={[gx]:"ENVMAP_BLENDING_MULTIPLY",[iE]:"ENVMAP_BLENDING_MIX",[aE]:"ENVMAP_BLENDING_ADD"};function G3(r){return r.envMap===!1?"ENVMAP_BLENDING_NONE":H3[r.combine]||"ENVMAP_BLENDING_NONE"}function V3(r){const t=r.envMapCubeUVHeight;if(t===null)return null;const i=Math.log2(t)-2,s=1/t;return{texelWidth:1/(3*Math.max(Math.pow(2,i),112)),texelHeight:s,maxMip:i}}function k3(r,t,i,s){const l=r.getContext(),c=i.defines;let f=i.vertexShader,p=i.fragmentShader;const m=P3(i),d=B3(i),x=F3(i),v=G3(i),g=V3(i),S=A3(i),b=R3(c),w=l.createProgram();let E,_,z=i.glslVersion?"#version "+i.glslVersion+`
`:"";i.isRawShaderMaterial?(E=["#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,b].filter(ml).join(`
`),E.length>0&&(E+=`
`),_=["#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,b].filter(ml).join(`
`),_.length>0&&(_+=`
`)):(E=[$v(i),"#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,b,i.extensionClipCullDistance?"#define USE_CLIP_DISTANCE":"",i.batching?"#define USE_BATCHING":"",i.batchingColor?"#define USE_BATCHING_COLOR":"",i.instancing?"#define USE_INSTANCING":"",i.instancingColor?"#define USE_INSTANCING_COLOR":"",i.instancingMorph?"#define USE_INSTANCING_MORPH":"",i.useFog&&i.fog?"#define USE_FOG":"",i.useFog&&i.fogExp2?"#define FOG_EXP2":"",i.map?"#define USE_MAP":"",i.envMap?"#define USE_ENVMAP":"",i.envMap?"#define "+x:"",i.lightMap?"#define USE_LIGHTMAP":"",i.aoMap?"#define USE_AOMAP":"",i.bumpMap?"#define USE_BUMPMAP":"",i.normalMap?"#define USE_NORMALMAP":"",i.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",i.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",i.displacementMap?"#define USE_DISPLACEMENTMAP":"",i.emissiveMap?"#define USE_EMISSIVEMAP":"",i.anisotropy?"#define USE_ANISOTROPY":"",i.anisotropyMap?"#define USE_ANISOTROPYMAP":"",i.clearcoatMap?"#define USE_CLEARCOATMAP":"",i.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",i.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",i.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",i.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",i.specularMap?"#define USE_SPECULARMAP":"",i.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",i.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",i.roughnessMap?"#define USE_ROUGHNESSMAP":"",i.metalnessMap?"#define USE_METALNESSMAP":"",i.alphaMap?"#define USE_ALPHAMAP":"",i.alphaHash?"#define USE_ALPHAHASH":"",i.transmission?"#define USE_TRANSMISSION":"",i.transmissionMap?"#define USE_TRANSMISSIONMAP":"",i.thicknessMap?"#define USE_THICKNESSMAP":"",i.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",i.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",i.mapUv?"#define MAP_UV "+i.mapUv:"",i.alphaMapUv?"#define ALPHAMAP_UV "+i.alphaMapUv:"",i.lightMapUv?"#define LIGHTMAP_UV "+i.lightMapUv:"",i.aoMapUv?"#define AOMAP_UV "+i.aoMapUv:"",i.emissiveMapUv?"#define EMISSIVEMAP_UV "+i.emissiveMapUv:"",i.bumpMapUv?"#define BUMPMAP_UV "+i.bumpMapUv:"",i.normalMapUv?"#define NORMALMAP_UV "+i.normalMapUv:"",i.displacementMapUv?"#define DISPLACEMENTMAP_UV "+i.displacementMapUv:"",i.metalnessMapUv?"#define METALNESSMAP_UV "+i.metalnessMapUv:"",i.roughnessMapUv?"#define ROUGHNESSMAP_UV "+i.roughnessMapUv:"",i.anisotropyMapUv?"#define ANISOTROPYMAP_UV "+i.anisotropyMapUv:"",i.clearcoatMapUv?"#define CLEARCOATMAP_UV "+i.clearcoatMapUv:"",i.clearcoatNormalMapUv?"#define CLEARCOAT_NORMALMAP_UV "+i.clearcoatNormalMapUv:"",i.clearcoatRoughnessMapUv?"#define CLEARCOAT_ROUGHNESSMAP_UV "+i.clearcoatRoughnessMapUv:"",i.iridescenceMapUv?"#define IRIDESCENCEMAP_UV "+i.iridescenceMapUv:"",i.iridescenceThicknessMapUv?"#define IRIDESCENCE_THICKNESSMAP_UV "+i.iridescenceThicknessMapUv:"",i.sheenColorMapUv?"#define SHEEN_COLORMAP_UV "+i.sheenColorMapUv:"",i.sheenRoughnessMapUv?"#define SHEEN_ROUGHNESSMAP_UV "+i.sheenRoughnessMapUv:"",i.specularMapUv?"#define SPECULARMAP_UV "+i.specularMapUv:"",i.specularColorMapUv?"#define SPECULAR_COLORMAP_UV "+i.specularColorMapUv:"",i.specularIntensityMapUv?"#define SPECULAR_INTENSITYMAP_UV "+i.specularIntensityMapUv:"",i.transmissionMapUv?"#define TRANSMISSIONMAP_UV "+i.transmissionMapUv:"",i.thicknessMapUv?"#define THICKNESSMAP_UV "+i.thicknessMapUv:"",i.vertexTangents&&i.flatShading===!1?"#define USE_TANGENT":"",i.vertexNormals?"#define HAS_NORMAL":"",i.vertexColors?"#define USE_COLOR":"",i.vertexAlphas?"#define USE_COLOR_ALPHA":"",i.vertexUv1s?"#define USE_UV1":"",i.vertexUv2s?"#define USE_UV2":"",i.vertexUv3s?"#define USE_UV3":"",i.pointsUvs?"#define USE_POINTS_UV":"",i.flatShading?"#define FLAT_SHADED":"",i.skinning?"#define USE_SKINNING":"",i.morphTargets?"#define USE_MORPHTARGETS":"",i.morphNormals&&i.flatShading===!1?"#define USE_MORPHNORMALS":"",i.morphColors?"#define USE_MORPHCOLORS":"",i.morphTargetsCount>0?"#define MORPHTARGETS_TEXTURE_STRIDE "+i.morphTextureStride:"",i.morphTargetsCount>0?"#define MORPHTARGETS_COUNT "+i.morphTargetsCount:"",i.doubleSided?"#define DOUBLE_SIDED":"",i.flipSided?"#define FLIP_SIDED":"",i.shadowMapEnabled?"#define USE_SHADOWMAP":"",i.shadowMapEnabled?"#define "+m:"",i.sizeAttenuation?"#define USE_SIZEATTENUATION":"",i.numLightProbes>0?"#define USE_LIGHT_PROBES":"",i.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",i.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 modelMatrix;","uniform mat4 modelViewMatrix;","uniform mat4 projectionMatrix;","uniform mat4 viewMatrix;","uniform mat3 normalMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;","#ifdef USE_INSTANCING","	attribute mat4 instanceMatrix;","#endif","#ifdef USE_INSTANCING_COLOR","	attribute vec3 instanceColor;","#endif","#ifdef USE_INSTANCING_MORPH","	uniform sampler2D morphTexture;","#endif","attribute vec3 position;","attribute vec3 normal;","attribute vec2 uv;","#ifdef USE_UV1","	attribute vec2 uv1;","#endif","#ifdef USE_UV2","	attribute vec2 uv2;","#endif","#ifdef USE_UV3","	attribute vec2 uv3;","#endif","#ifdef USE_TANGENT","	attribute vec4 tangent;","#endif","#if defined( USE_COLOR_ALPHA )","	attribute vec4 color;","#elif defined( USE_COLOR )","	attribute vec3 color;","#endif","#ifdef USE_SKINNING","	attribute vec4 skinIndex;","	attribute vec4 skinWeight;","#endif",`
`].filter(ml).join(`
`),_=[$v(i),"#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,b,i.useFog&&i.fog?"#define USE_FOG":"",i.useFog&&i.fogExp2?"#define FOG_EXP2":"",i.alphaToCoverage?"#define ALPHA_TO_COVERAGE":"",i.map?"#define USE_MAP":"",i.matcap?"#define USE_MATCAP":"",i.envMap?"#define USE_ENVMAP":"",i.envMap?"#define "+d:"",i.envMap?"#define "+x:"",i.envMap?"#define "+v:"",g?"#define CUBEUV_TEXEL_WIDTH "+g.texelWidth:"",g?"#define CUBEUV_TEXEL_HEIGHT "+g.texelHeight:"",g?"#define CUBEUV_MAX_MIP "+g.maxMip+".0":"",i.lightMap?"#define USE_LIGHTMAP":"",i.aoMap?"#define USE_AOMAP":"",i.bumpMap?"#define USE_BUMPMAP":"",i.normalMap?"#define USE_NORMALMAP":"",i.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",i.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",i.packedNormalMap?"#define USE_PACKED_NORMALMAP":"",i.emissiveMap?"#define USE_EMISSIVEMAP":"",i.anisotropy?"#define USE_ANISOTROPY":"",i.anisotropyMap?"#define USE_ANISOTROPYMAP":"",i.clearcoat?"#define USE_CLEARCOAT":"",i.clearcoatMap?"#define USE_CLEARCOATMAP":"",i.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",i.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",i.dispersion?"#define USE_DISPERSION":"",i.iridescence?"#define USE_IRIDESCENCE":"",i.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",i.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",i.specularMap?"#define USE_SPECULARMAP":"",i.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",i.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",i.roughnessMap?"#define USE_ROUGHNESSMAP":"",i.metalnessMap?"#define USE_METALNESSMAP":"",i.alphaMap?"#define USE_ALPHAMAP":"",i.alphaTest?"#define USE_ALPHATEST":"",i.alphaHash?"#define USE_ALPHAHASH":"",i.sheen?"#define USE_SHEEN":"",i.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",i.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",i.transmission?"#define USE_TRANSMISSION":"",i.transmissionMap?"#define USE_TRANSMISSIONMAP":"",i.thicknessMap?"#define USE_THICKNESSMAP":"",i.vertexTangents&&i.flatShading===!1?"#define USE_TANGENT":"",i.vertexColors||i.instancingColor?"#define USE_COLOR":"",i.vertexAlphas||i.batchingColor?"#define USE_COLOR_ALPHA":"",i.vertexUv1s?"#define USE_UV1":"",i.vertexUv2s?"#define USE_UV2":"",i.vertexUv3s?"#define USE_UV3":"",i.pointsUvs?"#define USE_POINTS_UV":"",i.gradientMap?"#define USE_GRADIENTMAP":"",i.flatShading?"#define FLAT_SHADED":"",i.doubleSided?"#define DOUBLE_SIDED":"",i.flipSided?"#define FLIP_SIDED":"",i.shadowMapEnabled?"#define USE_SHADOWMAP":"",i.shadowMapEnabled?"#define "+m:"",i.premultipliedAlpha?"#define PREMULTIPLIED_ALPHA":"",i.numLightProbes>0?"#define USE_LIGHT_PROBES":"",i.numLightProbeGrids>0?"#define USE_LIGHT_PROBES_GRID":"",i.decodeVideoTexture?"#define DECODE_VIDEO_TEXTURE":"",i.decodeVideoTextureEmissive?"#define DECODE_VIDEO_TEXTURE_EMISSIVE":"",i.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",i.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 viewMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;",i.toneMapping!==Ki?"#define TONE_MAPPING":"",i.toneMapping!==Ki?me.tonemapping_pars_fragment:"",i.toneMapping!==Ki?b3("toneMapping",i.toneMapping):"",i.dithering?"#define DITHERING":"",i.opaque?"#define OPAQUE":"",me.colorspace_pars_fragment,M3("linearToOutputTexel",i.outputColorSpace),T3(),i.useDepthPacking?"#define DEPTH_PACKING "+i.depthPacking:"",`
`].filter(ml).join(`
`)),f=Tp(f),f=Kv(f,i),f=Qv(f,i),p=Tp(p),p=Kv(p,i),p=Qv(p,i),f=Jv(f),p=Jv(p),i.isRawShaderMaterial!==!0&&(z=`#version 300 es
`,E=[S,"#define attribute in","#define varying out","#define texture2D texture"].join(`
`)+`
`+E,_=["#define varying in",i.glslVersion===K_?"":"layout(location = 0) out highp vec4 pc_fragColor;",i.glslVersion===K_?"":"#define gl_FragColor pc_fragColor","#define gl_FragDepthEXT gl_FragDepth","#define texture2D texture","#define textureCube texture","#define texture2DProj textureProj","#define texture2DLodEXT textureLod","#define texture2DProjLodEXT textureProjLod","#define textureCubeLodEXT textureLod","#define texture2DGradEXT textureGrad","#define texture2DProjGradEXT textureProjGrad","#define textureCubeGradEXT textureGrad"].join(`
`)+`
`+_);const F=z+E+f,C=z+_+p,B=Yv(l,l.VERTEX_SHADER,F),O=Yv(l,l.FRAGMENT_SHADER,C);l.attachShader(w,B),l.attachShader(w,O),i.index0AttributeName!==void 0?l.bindAttribLocation(w,0,i.index0AttributeName):i.hasPositionAttribute===!0&&l.bindAttribLocation(w,0,"position"),l.linkProgram(w);function U(Y){if(r.debug.checkShaderErrors){const J=l.getProgramInfoLog(w)||"",dt=l.getShaderInfoLog(B)||"",gt=l.getShaderInfoLog(O)||"",et=J.trim(),I=dt.trim(),H=gt.trim();let q=!0,ht=!0;if(l.getProgramParameter(w,l.LINK_STATUS)===!1)if(q=!1,typeof r.debug.onShaderError=="function")r.debug.onShaderError(l,w,B,O);else{const Et=Zv(l,B,"vertex"),L=Zv(l,O,"fragment");Ae("WebGLProgram: Shader Error "+l.getError()+" - VALIDATE_STATUS "+l.getProgramParameter(w,l.VALIDATE_STATUS)+`

Material Name: `+Y.name+`
Material Type: `+Y.type+`

Program Info Log: `+et+`
`+Et+`
`+L)}else et!==""?se("WebGLProgram: Program Info Log:",et):(I===""||H==="")&&(ht=!1);ht&&(Y.diagnostics={runnable:q,programLog:et,vertexShader:{log:I,prefix:E},fragmentShader:{log:H,prefix:_}})}l.deleteShader(B),l.deleteShader(O),T=new vu(l,w),P=w3(l,w)}let T;this.getUniforms=function(){return T===void 0&&U(this),T};let P;this.getAttributes=function(){return P===void 0&&U(this),P};let $=i.rendererExtensionParallelShaderCompile===!1;return this.isReady=function(){return $===!1&&($=l.getProgramParameter(w,v3)),$},this.destroy=function(){s.releaseStatesOfProgram(this),l.deleteProgram(w),this.program=void 0},this.type=i.shaderType,this.name=i.shaderName,this.id=x3++,this.cacheKey=t,this.usedTimes=1,this.program=w,this.vertexShader=B,this.fragmentShader=O,this}let X3=0;class W3{constructor(){this.shaderCache=new Map,this.materialCache=new Map}update(t,i,s){const l=this._getShaderCacheForMaterial(t);return l.has(i)===!1&&(l.add(i),i.usedTimes++),l.has(s)===!1&&(l.add(s),s.usedTimes++),this}remove(t){const i=this.materialCache.get(t);for(const s of i)s.usedTimes--,s.usedTimes===0&&this.shaderCache.delete(s.code);return this.materialCache.delete(t),this}getVertexShaderStage(t){return this._getShaderStage(t.vertexShader)}getFragmentShaderStage(t){return this._getShaderStage(t.fragmentShader)}dispose(){this.shaderCache.clear(),this.materialCache.clear()}_getShaderCacheForMaterial(t){const i=this.materialCache;let s=i.get(t);return s===void 0&&(s=new Set,i.set(t,s)),s}_getShaderStage(t){const i=this.shaderCache;let s=i.get(t);return s===void 0&&(s=new q3(t),i.set(t,s)),s}}class q3{constructor(t){this.id=X3++,this.code=t,this.usedTimes=0}}function Y3(r){return r===Zs||r===yu||r===Su}function j3(r,t,i,s,l,c){const f=new Vp,p=new W3,m=new Set,d=[],x=new Map,v=s.logarithmicDepthBuffer;let g=s.precision;const S={MeshDepthMaterial:"depth",MeshDistanceMaterial:"distance",MeshNormalMaterial:"normal",MeshBasicMaterial:"basic",MeshLambertMaterial:"lambert",MeshPhongMaterial:"phong",MeshToonMaterial:"toon",MeshStandardMaterial:"physical",MeshPhysicalMaterial:"physical",MeshMatcapMaterial:"matcap",LineBasicMaterial:"basic",LineDashedMaterial:"dashed",PointsMaterial:"points",ShadowMaterial:"shadow",SpriteMaterial:"sprite"};function b(T){return m.add(T),T===0?"uv":`uv${T}`}function w(T,P,$,Y,J,dt){const gt=Y.fog,et=J.geometry,I=T.isMeshStandardMaterial||T.isMeshLambertMaterial||T.isMeshPhongMaterial?Y.environment:null,H=T.isMeshStandardMaterial||T.isMeshLambertMaterial&&!T.envMap||T.isMeshPhongMaterial&&!T.envMap,q=t.get(T.envMap||I,H),ht=q&&q.mapping===Du?q.image.height:null,Et=S[T.type];T.precision!==null&&(g=s.getMaxPrecision(T.precision),g!==T.precision&&se("WebGLProgram.getParameters:",T.precision,"not supported, using",g,"instead."));const L=et.morphAttributes.position||et.morphAttributes.normal||et.morphAttributes.color,W=L!==void 0?L.length:0;let yt=0;et.morphAttributes.position!==void 0&&(yt=1),et.morphAttributes.normal!==void 0&&(yt=2),et.morphAttributes.color!==void 0&&(yt=3);let At,wt,nt,vt;if(Et){const kt=ji[Et];At=kt.vertexShader,wt=kt.fragmentShader}else{At=T.vertexShader,wt=T.fragmentShader;const kt=p.getVertexShaderStage(T),an=p.getFragmentShaderStage(T);p.update(T,kt,an),nt=kt.id,vt=an.id}const St=r.getRenderTarget(),Ut=r.state.buffers.depth.getReversed(),Jt=J.isInstancedMesh===!0,Kt=J.isBatchedMesh===!0,Ce=!!T.map,le=!!T.matcap,_e=!!q,ve=!!T.aoMap,de=!!T.lightMap,Ke=!!T.bumpMap&&T.wireframe===!1,Qe=!!T.normalMap,Je=!!T.displacementMap,nn=!!T.emissiveMap,ze=!!T.metalnessMap,$e=!!T.roughnessMap,K=T.anisotropy>0,Le=T.clearcoat>0,Lt=T.dispersion>0,D=T.iridescence>0,M=T.sheen>0,X=T.transmission>0,V=K&&!!T.anisotropyMap,ct=Le&&!!T.clearcoatMap,bt=Le&&!!T.clearcoatNormalMap,Ct=Le&&!!T.clearcoatRoughnessMap,ut=D&&!!T.iridescenceMap,pt=D&&!!T.iridescenceThicknessMap,Rt=M&&!!T.sheenColorMap,Ft=M&&!!T.sheenRoughnessMap,Ot=!!T.specularMap,Pt=!!T.specularColorMap,$t=!!T.specularIntensityMap,te=X&&!!T.transmissionMap,re=X&&!!T.thicknessMap,Z=!!T.gradientMap,Dt=!!T.alphaMap,_t=T.alphaTest>0,Nt=!!T.alphaHash,Ht=!!T.extensions;let Tt=Ki;T.toneMapped&&(St===null||St.isXRRenderTarget===!0)&&(Tt=r.toneMapping);const jt={shaderID:Et,shaderType:T.type,shaderName:T.name,vertexShader:At,fragmentShader:wt,defines:T.defines,customVertexShaderID:nt,customFragmentShaderID:vt,isRawShaderMaterial:T.isRawShaderMaterial===!0,glslVersion:T.glslVersion,precision:g,batching:Kt,batchingColor:Kt&&J._colorsTexture!==null,instancing:Jt,instancingColor:Jt&&J.instanceColor!==null,instancingMorph:Jt&&J.morphTexture!==null,outputColorSpace:St===null?r.outputColorSpace:St.isXRRenderTarget===!0?St.texture.colorSpace:Re.workingColorSpace,alphaToCoverage:!!T.alphaToCoverage,map:Ce,matcap:le,envMap:_e,envMapMode:_e&&q.mapping,envMapCubeUVHeight:ht,aoMap:ve,lightMap:de,bumpMap:Ke,normalMap:Qe,displacementMap:Je,emissiveMap:nn,normalMapObjectSpace:Qe&&T.normalMapType===oE,normalMapTangentSpace:Qe&&T.normalMapType===xp,packedNormalMap:Qe&&T.normalMapType===xp&&Y3(T.normalMap.format),metalnessMap:ze,roughnessMap:$e,anisotropy:K,anisotropyMap:V,clearcoat:Le,clearcoatMap:ct,clearcoatNormalMap:bt,clearcoatRoughnessMap:Ct,dispersion:Lt,iridescence:D,iridescenceMap:ut,iridescenceThicknessMap:pt,sheen:M,sheenColorMap:Rt,sheenRoughnessMap:Ft,specularMap:Ot,specularColorMap:Pt,specularIntensityMap:$t,transmission:X,transmissionMap:te,thicknessMap:re,gradientMap:Z,opaque:T.transparent===!1&&T.blending===Qr&&T.alphaToCoverage===!1,alphaMap:Dt,alphaTest:_t,alphaHash:Nt,combine:T.combine,mapUv:Ce&&b(T.map.channel),aoMapUv:ve&&b(T.aoMap.channel),lightMapUv:de&&b(T.lightMap.channel),bumpMapUv:Ke&&b(T.bumpMap.channel),normalMapUv:Qe&&b(T.normalMap.channel),displacementMapUv:Je&&b(T.displacementMap.channel),emissiveMapUv:nn&&b(T.emissiveMap.channel),metalnessMapUv:ze&&b(T.metalnessMap.channel),roughnessMapUv:$e&&b(T.roughnessMap.channel),anisotropyMapUv:V&&b(T.anisotropyMap.channel),clearcoatMapUv:ct&&b(T.clearcoatMap.channel),clearcoatNormalMapUv:bt&&b(T.clearcoatNormalMap.channel),clearcoatRoughnessMapUv:Ct&&b(T.clearcoatRoughnessMap.channel),iridescenceMapUv:ut&&b(T.iridescenceMap.channel),iridescenceThicknessMapUv:pt&&b(T.iridescenceThicknessMap.channel),sheenColorMapUv:Rt&&b(T.sheenColorMap.channel),sheenRoughnessMapUv:Ft&&b(T.sheenRoughnessMap.channel),specularMapUv:Ot&&b(T.specularMap.channel),specularColorMapUv:Pt&&b(T.specularColorMap.channel),specularIntensityMapUv:$t&&b(T.specularIntensityMap.channel),transmissionMapUv:te&&b(T.transmissionMap.channel),thicknessMapUv:re&&b(T.thicknessMap.channel),alphaMapUv:Dt&&b(T.alphaMap.channel),vertexTangents:!!et.attributes.tangent&&(Qe||K),vertexNormals:!!et.attributes.normal,vertexColors:T.vertexColors,vertexAlphas:T.vertexColors===!0&&!!et.attributes.color&&et.attributes.color.itemSize===4,pointsUvs:J.isPoints===!0&&!!et.attributes.uv&&(Ce||Dt),fog:!!gt,useFog:T.fog===!0,fogExp2:!!gt&&gt.isFogExp2,flatShading:T.wireframe===!1&&(T.flatShading===!0||et.attributes.normal===void 0&&Qe===!1&&(T.isMeshLambertMaterial||T.isMeshPhongMaterial||T.isMeshStandardMaterial||T.isMeshPhysicalMaterial)),sizeAttenuation:T.sizeAttenuation===!0,logarithmicDepthBuffer:v,reversedDepthBuffer:Ut,skinning:J.isSkinnedMesh===!0,hasPositionAttribute:et.attributes.position!==void 0,morphTargets:et.morphAttributes.position!==void 0,morphNormals:et.morphAttributes.normal!==void 0,morphColors:et.morphAttributes.color!==void 0,morphTargetsCount:W,morphTextureStride:yt,numDirLights:P.directional.length,numPointLights:P.point.length,numSpotLights:P.spot.length,numSpotLightMaps:P.spotLightMap.length,numRectAreaLights:P.rectArea.length,numHemiLights:P.hemi.length,numDirLightShadows:P.directionalShadowMap.length,numPointLightShadows:P.pointShadowMap.length,numSpotLightShadows:P.spotShadowMap.length,numSpotLightShadowsWithMaps:P.numSpotLightShadowsWithMaps,numLightProbes:P.numLightProbes,numLightProbeGrids:dt.length,numClippingPlanes:c.numPlanes,numClipIntersection:c.numIntersection,dithering:T.dithering,shadowMapEnabled:r.shadowMap.enabled&&$.length>0,shadowMapType:r.shadowMap.type,toneMapping:Tt,decodeVideoTexture:Ce&&T.map.isVideoTexture===!0&&Re.getTransfer(T.map.colorSpace)===ke,decodeVideoTextureEmissive:nn&&T.emissiveMap.isVideoTexture===!0&&Re.getTransfer(T.emissiveMap.colorSpace)===ke,premultipliedAlpha:T.premultipliedAlpha,doubleSided:T.side===wi,flipSided:T.side===ti,useDepthPacking:T.depthPacking>=0,depthPacking:T.depthPacking||0,index0AttributeName:T.index0AttributeName,extensionClipCullDistance:Ht&&T.extensions.clipCullDistance===!0&&i.has("WEBGL_clip_cull_distance"),extensionMultiDraw:(Ht&&T.extensions.multiDraw===!0||Kt)&&i.has("WEBGL_multi_draw"),rendererExtensionParallelShaderCompile:i.has("KHR_parallel_shader_compile"),customProgramCacheKey:T.customProgramCacheKey()};return jt.vertexUv1s=m.has(1),jt.vertexUv2s=m.has(2),jt.vertexUv3s=m.has(3),m.clear(),jt}function E(T){const P=[];if(T.shaderID?P.push(T.shaderID):(P.push(T.customVertexShaderID),P.push(T.customFragmentShaderID)),T.defines!==void 0)for(const $ in T.defines)P.push($),P.push(T.defines[$]);return T.isRawShaderMaterial===!1&&(_(P,T),z(P,T),P.push(r.outputColorSpace)),P.push(T.customProgramCacheKey),P.join()}function _(T,P){T.push(P.precision),T.push(P.outputColorSpace),T.push(P.envMapMode),T.push(P.envMapCubeUVHeight),T.push(P.mapUv),T.push(P.alphaMapUv),T.push(P.lightMapUv),T.push(P.aoMapUv),T.push(P.bumpMapUv),T.push(P.normalMapUv),T.push(P.displacementMapUv),T.push(P.emissiveMapUv),T.push(P.metalnessMapUv),T.push(P.roughnessMapUv),T.push(P.anisotropyMapUv),T.push(P.clearcoatMapUv),T.push(P.clearcoatNormalMapUv),T.push(P.clearcoatRoughnessMapUv),T.push(P.iridescenceMapUv),T.push(P.iridescenceThicknessMapUv),T.push(P.sheenColorMapUv),T.push(P.sheenRoughnessMapUv),T.push(P.specularMapUv),T.push(P.specularColorMapUv),T.push(P.specularIntensityMapUv),T.push(P.transmissionMapUv),T.push(P.thicknessMapUv),T.push(P.combine),T.push(P.fogExp2),T.push(P.sizeAttenuation),T.push(P.morphTargetsCount),T.push(P.morphAttributeCount),T.push(P.numDirLights),T.push(P.numPointLights),T.push(P.numSpotLights),T.push(P.numSpotLightMaps),T.push(P.numHemiLights),T.push(P.numRectAreaLights),T.push(P.numDirLightShadows),T.push(P.numPointLightShadows),T.push(P.numSpotLightShadows),T.push(P.numSpotLightShadowsWithMaps),T.push(P.numLightProbes),T.push(P.shadowMapType),T.push(P.toneMapping),T.push(P.numClippingPlanes),T.push(P.numClipIntersection),T.push(P.depthPacking)}function z(T,P){f.disableAll(),P.instancing&&f.enable(0),P.instancingColor&&f.enable(1),P.instancingMorph&&f.enable(2),P.matcap&&f.enable(3),P.envMap&&f.enable(4),P.normalMapObjectSpace&&f.enable(5),P.normalMapTangentSpace&&f.enable(6),P.clearcoat&&f.enable(7),P.iridescence&&f.enable(8),P.alphaTest&&f.enable(9),P.vertexColors&&f.enable(10),P.vertexAlphas&&f.enable(11),P.vertexUv1s&&f.enable(12),P.vertexUv2s&&f.enable(13),P.vertexUv3s&&f.enable(14),P.vertexTangents&&f.enable(15),P.anisotropy&&f.enable(16),P.alphaHash&&f.enable(17),P.batching&&f.enable(18),P.dispersion&&f.enable(19),P.batchingColor&&f.enable(20),P.gradientMap&&f.enable(21),P.packedNormalMap&&f.enable(22),P.vertexNormals&&f.enable(23),T.push(f.mask),f.disableAll(),P.fog&&f.enable(0),P.useFog&&f.enable(1),P.flatShading&&f.enable(2),P.logarithmicDepthBuffer&&f.enable(3),P.reversedDepthBuffer&&f.enable(4),P.skinning&&f.enable(5),P.morphTargets&&f.enable(6),P.morphNormals&&f.enable(7),P.morphColors&&f.enable(8),P.premultipliedAlpha&&f.enable(9),P.shadowMapEnabled&&f.enable(10),P.doubleSided&&f.enable(11),P.flipSided&&f.enable(12),P.useDepthPacking&&f.enable(13),P.dithering&&f.enable(14),P.transmission&&f.enable(15),P.sheen&&f.enable(16),P.opaque&&f.enable(17),P.pointsUvs&&f.enable(18),P.decodeVideoTexture&&f.enable(19),P.decodeVideoTextureEmissive&&f.enable(20),P.alphaToCoverage&&f.enable(21),P.numLightProbeGrids>0&&f.enable(22),P.hasPositionAttribute&&f.enable(23),T.push(f.mask)}function F(T){const P=S[T.type];let $;if(P){const Y=ji[P];$=ub.clone(Y.uniforms)}else $=T.uniforms;return $}function C(T,P){let $=x.get(P);return $!==void 0?++$.usedTimes:($=new k3(r,P,T,l),d.push($),x.set(P,$)),$}function B(T){if(--T.usedTimes===0){const P=d.indexOf(T);d[P]=d[d.length-1],d.pop(),x.delete(T.cacheKey),T.destroy()}}function O(T){p.remove(T)}function U(){p.dispose()}return{getParameters:w,getProgramCacheKey:E,getUniforms:F,acquireProgram:C,releaseProgram:B,releaseShaderCache:O,programs:d,dispose:U}}function Z3(){let r=new WeakMap;function t(f){return r.has(f)}function i(f){let p=r.get(f);return p===void 0&&(p={},r.set(f,p)),p}function s(f){r.delete(f)}function l(f,p,m){r.get(f)[p]=m}function c(){r=new WeakMap}return{has:t,get:i,remove:s,update:l,dispose:c}}function K3(r,t){return r.groupOrder!==t.groupOrder?r.groupOrder-t.groupOrder:r.renderOrder!==t.renderOrder?r.renderOrder-t.renderOrder:r.material.id!==t.material.id?r.material.id-t.material.id:r.materialVariant!==t.materialVariant?r.materialVariant-t.materialVariant:r.z!==t.z?r.z-t.z:r.id-t.id}function tx(r,t){return r.groupOrder!==t.groupOrder?r.groupOrder-t.groupOrder:r.renderOrder!==t.renderOrder?r.renderOrder-t.renderOrder:r.z!==t.z?t.z-r.z:r.id-t.id}function ex(){const r=[];let t=0;const i=[],s=[],l=[];function c(){t=0,i.length=0,s.length=0,l.length=0}function f(g){let S=0;return g.isInstancedMesh&&(S+=2),g.isSkinnedMesh&&(S+=1),S}function p(g,S,b,w,E,_){let z=r[t];return z===void 0?(z={id:g.id,object:g,geometry:S,material:b,materialVariant:f(g),groupOrder:w,renderOrder:g.renderOrder,z:E,group:_},r[t]=z):(z.id=g.id,z.object=g,z.geometry=S,z.material=b,z.materialVariant=f(g),z.groupOrder=w,z.renderOrder=g.renderOrder,z.z=E,z.group=_),t++,z}function m(g,S,b,w,E,_){const z=p(g,S,b,w,E,_);b.transmission>0?s.push(z):b.transparent===!0?l.push(z):i.push(z)}function d(g,S,b,w,E,_){const z=p(g,S,b,w,E,_);b.transmission>0?s.unshift(z):b.transparent===!0?l.unshift(z):i.unshift(z)}function x(g,S,b){i.length>1&&i.sort(g||K3),s.length>1&&s.sort(S||tx),l.length>1&&l.sort(S||tx),b&&(i.reverse(),s.reverse(),l.reverse())}function v(){for(let g=t,S=r.length;g<S;g++){const b=r[g];if(b.id===null)break;b.id=null,b.object=null,b.geometry=null,b.material=null,b.group=null}}return{opaque:i,transmissive:s,transparent:l,init:c,push:m,unshift:d,finish:v,sort:x}}function Q3(){let r=new WeakMap;function t(s,l){const c=r.get(s);let f;return c===void 0?(f=new ex,r.set(s,[f])):l>=c.length?(f=new ex,c.push(f)):f=c[l],f}function i(){r=new WeakMap}return{get:t,dispose:i}}function J3(){const r={};return{get:function(t){if(r[t.id]!==void 0)return r[t.id];let i;switch(t.type){case"DirectionalLight":i={direction:new k,color:new ie};break;case"SpotLight":i={position:new k,direction:new k,color:new ie,distance:0,coneCos:0,penumbraCos:0,decay:0};break;case"PointLight":i={position:new k,color:new ie,distance:0,decay:0};break;case"HemisphereLight":i={direction:new k,skyColor:new ie,groundColor:new ie};break;case"RectAreaLight":i={color:new ie,position:new k,halfWidth:new k,halfHeight:new k};break}return r[t.id]=i,i}}}function $3(){const r={};return{get:function(t){if(r[t.id]!==void 0)return r[t.id];let i;switch(t.type){case"DirectionalLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new oe};break;case"SpotLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new oe};break;case"PointLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new oe,shadowCameraNear:1,shadowCameraFar:1e3};break}return r[t.id]=i,i}}}let tR=0;function eR(r,t){return(t.castShadow?2:0)-(r.castShadow?2:0)+(t.map?1:0)-(r.map?1:0)}function nR(r){const t=new J3,i=$3(),s={version:0,hash:{directionalLength:-1,pointLength:-1,spotLength:-1,rectAreaLength:-1,hemiLength:-1,numDirectionalShadows:-1,numPointShadows:-1,numSpotShadows:-1,numSpotMaps:-1,numLightProbes:-1},ambient:[0,0,0],probe:[],directional:[],directionalShadow:[],directionalShadowMap:[],directionalShadowMatrix:[],spot:[],spotLightMap:[],spotShadow:[],spotShadowMap:[],spotLightMatrix:[],rectArea:[],rectAreaLTC1:null,rectAreaLTC2:null,point:[],pointShadow:[],pointShadowMap:[],pointShadowMatrix:[],hemi:[],numSpotLightShadowsWithMaps:0,numLightProbes:0};for(let d=0;d<9;d++)s.probe.push(new k);const l=new k,c=new we,f=new we;function p(d){let x=0,v=0,g=0;for(let P=0;P<9;P++)s.probe[P].set(0,0,0);let S=0,b=0,w=0,E=0,_=0,z=0,F=0,C=0,B=0,O=0,U=0;d.sort(eR);for(let P=0,$=d.length;P<$;P++){const Y=d[P],J=Y.color,dt=Y.intensity,gt=Y.distance;let et=null;if(Y.shadow&&Y.shadow.map&&(Y.shadow.map.texture.format===Zs?et=Y.shadow.map.texture:et=Y.shadow.map.depthTexture||Y.shadow.map.texture),Y.isAmbientLight)x+=J.r*dt,v+=J.g*dt,g+=J.b*dt;else if(Y.isLightProbe){for(let I=0;I<9;I++)s.probe[I].addScaledVector(Y.sh.coefficients[I],dt);U++}else if(Y.isDirectionalLight){const I=t.get(Y);if(I.color.copy(Y.color).multiplyScalar(Y.intensity),Y.castShadow){const H=Y.shadow,q=i.get(Y);q.shadowIntensity=H.intensity,q.shadowBias=H.bias,q.shadowNormalBias=H.normalBias,q.shadowRadius=H.radius,q.shadowMapSize=H.mapSize,s.directionalShadow[S]=q,s.directionalShadowMap[S]=et,s.directionalShadowMatrix[S]=Y.shadow.matrix,z++}s.directional[S]=I,S++}else if(Y.isSpotLight){const I=t.get(Y);I.position.setFromMatrixPosition(Y.matrixWorld),I.color.copy(J).multiplyScalar(dt),I.distance=gt,I.coneCos=Math.cos(Y.angle),I.penumbraCos=Math.cos(Y.angle*(1-Y.penumbra)),I.decay=Y.decay,s.spot[w]=I;const H=Y.shadow;if(Y.map&&(s.spotLightMap[B]=Y.map,B++,H.updateMatrices(Y),Y.castShadow&&O++),s.spotLightMatrix[w]=H.matrix,Y.castShadow){const q=i.get(Y);q.shadowIntensity=H.intensity,q.shadowBias=H.bias,q.shadowNormalBias=H.normalBias,q.shadowRadius=H.radius,q.shadowMapSize=H.mapSize,s.spotShadow[w]=q,s.spotShadowMap[w]=et,C++}w++}else if(Y.isRectAreaLight){const I=t.get(Y);I.color.copy(J).multiplyScalar(dt),I.halfWidth.set(Y.width*.5,0,0),I.halfHeight.set(0,Y.height*.5,0),s.rectArea[E]=I,E++}else if(Y.isPointLight){const I=t.get(Y);if(I.color.copy(Y.color).multiplyScalar(Y.intensity),I.distance=Y.distance,I.decay=Y.decay,Y.castShadow){const H=Y.shadow,q=i.get(Y);q.shadowIntensity=H.intensity,q.shadowBias=H.bias,q.shadowNormalBias=H.normalBias,q.shadowRadius=H.radius,q.shadowMapSize=H.mapSize,q.shadowCameraNear=H.camera.near,q.shadowCameraFar=H.camera.far,s.pointShadow[b]=q,s.pointShadowMap[b]=et,s.pointShadowMatrix[b]=Y.shadow.matrix,F++}s.point[b]=I,b++}else if(Y.isHemisphereLight){const I=t.get(Y);I.skyColor.copy(Y.color).multiplyScalar(dt),I.groundColor.copy(Y.groundColor).multiplyScalar(dt),s.hemi[_]=I,_++}}E>0&&(r.has("OES_texture_float_linear")===!0?(s.rectAreaLTC1=It.LTC_FLOAT_1,s.rectAreaLTC2=It.LTC_FLOAT_2):(s.rectAreaLTC1=It.LTC_HALF_1,s.rectAreaLTC2=It.LTC_HALF_2)),s.ambient[0]=x,s.ambient[1]=v,s.ambient[2]=g;const T=s.hash;(T.directionalLength!==S||T.pointLength!==b||T.spotLength!==w||T.rectAreaLength!==E||T.hemiLength!==_||T.numDirectionalShadows!==z||T.numPointShadows!==F||T.numSpotShadows!==C||T.numSpotMaps!==B||T.numLightProbes!==U)&&(s.directional.length=S,s.spot.length=w,s.rectArea.length=E,s.point.length=b,s.hemi.length=_,s.directionalShadow.length=z,s.directionalShadowMap.length=z,s.pointShadow.length=F,s.pointShadowMap.length=F,s.spotShadow.length=C,s.spotShadowMap.length=C,s.directionalShadowMatrix.length=z,s.pointShadowMatrix.length=F,s.spotLightMatrix.length=C+B-O,s.spotLightMap.length=B,s.numSpotLightShadowsWithMaps=O,s.numLightProbes=U,T.directionalLength=S,T.pointLength=b,T.spotLength=w,T.rectAreaLength=E,T.hemiLength=_,T.numDirectionalShadows=z,T.numPointShadows=F,T.numSpotShadows=C,T.numSpotMaps=B,T.numLightProbes=U,s.version=tR++)}function m(d,x){let v=0,g=0,S=0,b=0,w=0;const E=x.matrixWorldInverse;for(let _=0,z=d.length;_<z;_++){const F=d[_];if(F.isDirectionalLight){const C=s.directional[v];C.direction.setFromMatrixPosition(F.matrixWorld),l.setFromMatrixPosition(F.target.matrixWorld),C.direction.sub(l),C.direction.transformDirection(E),v++}else if(F.isSpotLight){const C=s.spot[S];C.position.setFromMatrixPosition(F.matrixWorld),C.position.applyMatrix4(E),C.direction.setFromMatrixPosition(F.matrixWorld),l.setFromMatrixPosition(F.target.matrixWorld),C.direction.sub(l),C.direction.transformDirection(E),S++}else if(F.isRectAreaLight){const C=s.rectArea[b];C.position.setFromMatrixPosition(F.matrixWorld),C.position.applyMatrix4(E),f.identity(),c.copy(F.matrixWorld),c.premultiply(E),f.extractRotation(c),C.halfWidth.set(F.width*.5,0,0),C.halfHeight.set(0,F.height*.5,0),C.halfWidth.applyMatrix4(f),C.halfHeight.applyMatrix4(f),b++}else if(F.isPointLight){const C=s.point[g];C.position.setFromMatrixPosition(F.matrixWorld),C.position.applyMatrix4(E),g++}else if(F.isHemisphereLight){const C=s.hemi[w];C.direction.setFromMatrixPosition(F.matrixWorld),C.direction.transformDirection(E),w++}}}return{setup:p,setupView:m,state:s}}function nx(r){const t=new nR(r),i=[],s=[],l=[];function c(g){v.camera=g,i.length=0,s.length=0,l.length=0}function f(g){i.push(g)}function p(g){s.push(g)}function m(g){l.push(g)}function d(){t.setup(i)}function x(g){t.setupView(i,g)}const v={lightsArray:i,shadowsArray:s,lightProbeGridArray:l,camera:null,lights:t,transmissionRenderTarget:{},textureUnits:0};return{init:c,state:v,setupLights:d,setupLightsView:x,pushLight:f,pushShadow:p,pushLightProbeGrid:m}}function iR(r){let t=new WeakMap;function i(l,c=0){const f=t.get(l);let p;return f===void 0?(p=new nx(r),t.set(l,[p])):c>=f.length?(p=new nx(r),f.push(p)):p=f[c],p}function s(){t=new WeakMap}return{get:i,dispose:s}}const aR=`void main() {
	gl_Position = vec4( position, 1.0 );
}`,sR=`uniform sampler2D shadow_pass;
uniform vec2 resolution;
uniform float radius;
void main() {
	const float samples = float( VSM_SAMPLES );
	float mean = 0.0;
	float squared_mean = 0.0;
	float uvStride = samples <= 1.0 ? 0.0 : 2.0 / ( samples - 1.0 );
	float uvStart = samples <= 1.0 ? 0.0 : - 1.0;
	for ( float i = 0.0; i < samples; i ++ ) {
		float uvOffset = uvStart + i * uvStride;
		#ifdef HORIZONTAL_PASS
			vec2 distribution = texture2D( shadow_pass, ( gl_FragCoord.xy + vec2( uvOffset, 0.0 ) * radius ) / resolution ).rg;
			mean += distribution.x;
			squared_mean += distribution.y * distribution.y + distribution.x * distribution.x;
		#else
			float depth = texture2D( shadow_pass, ( gl_FragCoord.xy + vec2( 0.0, uvOffset ) * radius ) / resolution ).r;
			mean += depth;
			squared_mean += depth * depth;
		#endif
	}
	mean = mean / samples;
	squared_mean = squared_mean / samples;
	float std_dev = sqrt( max( 0.0, squared_mean - mean * mean ) );
	gl_FragColor = vec4( mean, std_dev, 0.0, 1.0 );
}`,rR=[new k(1,0,0),new k(-1,0,0),new k(0,1,0),new k(0,-1,0),new k(0,0,1),new k(0,0,-1)],oR=[new k(0,-1,0),new k(0,-1,0),new k(0,0,1),new k(0,0,-1),new k(0,-1,0),new k(0,-1,0)],ix=new we,dl=new k,Cd=new k;function lR(r,t,i){let s=new Wp;const l=new oe,c=new oe,f=new cn,p=new mb,m=new gb,d={},x=i.maxTextureSize,v={[ds]:ti,[ti]:ds,[wi]:wi},g=new $i({defines:{VSM_SAMPLES:8},uniforms:{shadow_pass:{value:null},resolution:{value:new oe},radius:{value:4}},vertexShader:aR,fragmentShader:sR}),S=g.clone();S.defines.HORIZONTAL_PASS=1;const b=new hn;b.setAttribute("position",new vi(new Float32Array([-1,-1,.5,3,-1,.5,-1,3,.5]),3));const w=new ln(b,g),E=this;this.enabled=!1,this.autoUpdate=!0,this.needsUpdate=!1,this.type=_l;let _=this.type;this.render=function(O,U,T){if(E.enabled===!1||E.autoUpdate===!1&&E.needsUpdate===!1||O.length===0)return;this.type===I1&&(se("WebGLShadowMap: PCFSoftShadowMap has been deprecated. Using PCFShadowMap instead."),this.type=_l);const P=r.getRenderTarget(),$=r.getActiveCubeFace(),Y=r.getActiveMipmapLevel(),J=r.state;J.setBlending(Aa),J.buffers.depth.getReversed()===!0?J.buffers.color.setClear(0,0,0,0):J.buffers.color.setClear(1,1,1,1),J.buffers.depth.setTest(!0),J.setScissorTest(!1);const dt=_!==this.type;dt&&U.traverse(function(gt){gt.material&&(Array.isArray(gt.material)?gt.material.forEach(et=>et.needsUpdate=!0):gt.material.needsUpdate=!0)});for(let gt=0,et=O.length;gt<et;gt++){const I=O[gt],H=I.shadow;if(H===void 0){se("WebGLShadowMap:",I,"has no shadow.");continue}if(H.autoUpdate===!1&&H.needsUpdate===!1)continue;l.copy(H.mapSize);const q=H.getFrameExtents();l.multiply(q),c.copy(H.mapSize),(l.x>x||l.y>x)&&(l.x>x&&(c.x=Math.floor(x/q.x),l.x=c.x*q.x,H.mapSize.x=c.x),l.y>x&&(c.y=Math.floor(x/q.y),l.y=c.y*q.y,H.mapSize.y=c.y));const ht=r.state.buffers.depth.getReversed();if(H.camera._reversedDepth=ht,H.map===null||dt===!0){if(H.map!==null&&(H.map.depthTexture!==null&&(H.map.depthTexture.dispose(),H.map.depthTexture=null),H.map.dispose()),this.type===pl){if(I.isPointLight){se("WebGLShadowMap: VSM shadow maps are not supported for PointLights. Use PCF or BasicShadowMap instead.");continue}H.map=new Qi(l.x,l.y,{format:Zs,type:Ca,minFilter:Gn,magFilter:Gn,generateMipmaps:!1}),H.map.texture.name=I.name+".shadowMap",H.map.depthTexture=new ao(l.x,l.y,Ii),H.map.depthTexture.name=I.name+".shadowMapDepth",H.map.depthTexture.format=Da,H.map.depthTexture.compareFunction=null,H.map.depthTexture.minFilter=zn,H.map.depthTexture.magFilter=zn}else I.isPointLight?(H.map=new Zx(l.x),H.map.depthTexture=new sb(l.x,Ji)):(H.map=new Qi(l.x,l.y),H.map.depthTexture=new ao(l.x,l.y,Ji)),H.map.depthTexture.name=I.name+".shadowMap",H.map.depthTexture.format=Da,this.type===_l?(H.map.depthTexture.compareFunction=ht?Fp:Ip,H.map.depthTexture.minFilter=Gn,H.map.depthTexture.magFilter=Gn):(H.map.depthTexture.compareFunction=null,H.map.depthTexture.minFilter=zn,H.map.depthTexture.magFilter=zn);H.camera.updateProjectionMatrix()}const Et=H.map.isWebGLCubeRenderTarget?6:1;for(let L=0;L<Et;L++){if(H.map.isWebGLCubeRenderTarget)r.setRenderTarget(H.map,L),r.clear();else{L===0&&(r.setRenderTarget(H.map),r.clear());const W=H.getViewport(L);f.set(c.x*W.x,c.y*W.y,c.x*W.z,c.y*W.w),J.viewport(f)}if(I.isPointLight){const W=H.camera,yt=H.matrix,At=I.distance||W.far;At!==W.far&&(W.far=At,W.updateProjectionMatrix()),dl.setFromMatrixPosition(I.matrixWorld),W.position.copy(dl),Cd.copy(W.position),Cd.add(rR[L]),W.up.copy(oR[L]),W.lookAt(Cd),W.updateMatrixWorld(),yt.makeTranslation(-dl.x,-dl.y,-dl.z),ix.multiplyMatrices(W.projectionMatrix,W.matrixWorldInverse),H._frustum.setFromProjectionMatrix(ix,W.coordinateSystem,W.reversedDepth)}else H.updateMatrices(I);s=H.getFrustum(),C(U,T,H.camera,I,this.type)}H.isPointLightShadow!==!0&&this.type===pl&&z(H,T),H.needsUpdate=!1}_=this.type,E.needsUpdate=!1,r.setRenderTarget(P,$,Y)};function z(O,U){const T=t.update(w);g.defines.VSM_SAMPLES!==O.blurSamples&&(g.defines.VSM_SAMPLES=O.blurSamples,S.defines.VSM_SAMPLES=O.blurSamples,g.needsUpdate=!0,S.needsUpdate=!0),O.mapPass===null&&(O.mapPass=new Qi(l.x,l.y,{format:Zs,type:Ca})),g.uniforms.shadow_pass.value=O.map.depthTexture,g.uniforms.resolution.value=O.mapSize,g.uniforms.radius.value=O.radius,r.setRenderTarget(O.mapPass),r.clear(),r.renderBufferDirect(U,null,T,g,w,null),S.uniforms.shadow_pass.value=O.mapPass.texture,S.uniforms.resolution.value=O.mapSize,S.uniforms.radius.value=O.radius,r.setRenderTarget(O.map),r.clear(),r.renderBufferDirect(U,null,T,S,w,null)}function F(O,U,T,P){let $=null;const Y=T.isPointLight===!0?O.customDistanceMaterial:O.customDepthMaterial;if(Y!==void 0)$=Y;else if($=T.isPointLight===!0?m:p,r.localClippingEnabled&&U.clipShadows===!0&&Array.isArray(U.clippingPlanes)&&U.clippingPlanes.length!==0||U.displacementMap&&U.displacementScale!==0||U.alphaMap&&U.alphaTest>0||U.map&&U.alphaTest>0||U.alphaToCoverage===!0){const J=$.uuid,dt=U.uuid;let gt=d[J];gt===void 0&&(gt={},d[J]=gt);let et=gt[dt];et===void 0&&(et=$.clone(),gt[dt]=et,U.addEventListener("dispose",B)),$=et}if($.visible=U.visible,$.wireframe=U.wireframe,P===pl?$.side=U.shadowSide!==null?U.shadowSide:U.side:$.side=U.shadowSide!==null?U.shadowSide:v[U.side],$.alphaMap=U.alphaMap,$.alphaTest=U.alphaToCoverage===!0?.5:U.alphaTest,$.map=U.map,$.clipShadows=U.clipShadows,$.clippingPlanes=U.clippingPlanes,$.clipIntersection=U.clipIntersection,$.displacementMap=U.displacementMap,$.displacementScale=U.displacementScale,$.displacementBias=U.displacementBias,$.wireframeLinewidth=U.wireframeLinewidth,$.linewidth=U.linewidth,T.isPointLight===!0&&$.isMeshDistanceMaterial===!0){const J=r.properties.get($);J.light=T}return $}function C(O,U,T,P,$){if(O.visible===!1)return;if(O.layers.test(U.layers)&&(O.isMesh||O.isLine||O.isPoints)&&(O.castShadow||O.receiveShadow&&$===pl)&&(!O.frustumCulled||s.intersectsObject(O))){O.modelViewMatrix.multiplyMatrices(T.matrixWorldInverse,O.matrixWorld);const dt=t.update(O),gt=O.material;if(Array.isArray(gt)){const et=dt.groups;for(let I=0,H=et.length;I<H;I++){const q=et[I],ht=gt[q.materialIndex];if(ht&&ht.visible){const Et=F(O,ht,P,$);O.onBeforeShadow(r,O,U,T,dt,Et,q),r.renderBufferDirect(T,null,dt,Et,O,q),O.onAfterShadow(r,O,U,T,dt,Et,q)}}}else if(gt.visible){const et=F(O,gt,P,$);O.onBeforeShadow(r,O,U,T,dt,et,null),r.renderBufferDirect(T,null,dt,et,O,null),O.onAfterShadow(r,O,U,T,dt,et,null)}}const J=O.children;for(let dt=0,gt=J.length;dt<gt;dt++)C(J[dt],U,T,P,$)}function B(O){O.target.removeEventListener("dispose",B);for(const T in d){const P=d[T],$=O.target.uuid;$ in P&&(P[$].dispose(),delete P[$])}}}function cR(r,t){function i(){let Z=!1;const Dt=new cn;let _t=null;const Nt=new cn(0,0,0,0);return{setMask:function(Ht){_t!==Ht&&!Z&&(r.colorMask(Ht,Ht,Ht,Ht),_t=Ht)},setLocked:function(Ht){Z=Ht},setClear:function(Ht,Tt,jt,kt,an){an===!0&&(Ht*=kt,Tt*=kt,jt*=kt),Dt.set(Ht,Tt,jt,kt),Nt.equals(Dt)===!1&&(r.clearColor(Ht,Tt,jt,kt),Nt.copy(Dt))},reset:function(){Z=!1,_t=null,Nt.set(-1,0,0,0)}}}function s(){let Z=!1,Dt=!1,_t=null,Nt=null,Ht=null;return{setReversed:function(Tt){if(Dt!==Tt){const jt=t.get("EXT_clip_control");Tt?jt.clipControlEXT(jt.LOWER_LEFT_EXT,jt.ZERO_TO_ONE_EXT):jt.clipControlEXT(jt.LOWER_LEFT_EXT,jt.NEGATIVE_ONE_TO_ONE_EXT),Dt=Tt;const kt=Ht;Ht=null,this.setClear(kt)}},getReversed:function(){return Dt},setTest:function(Tt){Tt?St(r.DEPTH_TEST):Ut(r.DEPTH_TEST)},setMask:function(Tt){_t!==Tt&&!Z&&(r.depthMask(Tt),_t=Tt)},setFunc:function(Tt){if(Dt&&(Tt=_E[Tt]),Nt!==Tt){switch(Tt){case Od:r.depthFunc(r.NEVER);break;case Pd:r.depthFunc(r.ALWAYS);break;case zd:r.depthFunc(r.LESS);break;case eo:r.depthFunc(r.LEQUAL);break;case Bd:r.depthFunc(r.EQUAL);break;case Id:r.depthFunc(r.GEQUAL);break;case Fd:r.depthFunc(r.GREATER);break;case Hd:r.depthFunc(r.NOTEQUAL);break;default:r.depthFunc(r.LEQUAL)}Nt=Tt}},setLocked:function(Tt){Z=Tt},setClear:function(Tt){Ht!==Tt&&(Ht=Tt,Dt&&(Tt=1-Tt),r.clearDepth(Tt))},reset:function(){Z=!1,_t=null,Nt=null,Ht=null,Dt=!1}}}function l(){let Z=!1,Dt=null,_t=null,Nt=null,Ht=null,Tt=null,jt=null,kt=null,an=null;return{setTest:function(Be){Z||(Be?St(r.STENCIL_TEST):Ut(r.STENCIL_TEST))},setMask:function(Be){Dt!==Be&&!Z&&(r.stencilMask(Be),Dt=Be)},setFunc:function(Be,ei,ni){(_t!==Be||Nt!==ei||Ht!==ni)&&(r.stencilFunc(Be,ei,ni),_t=Be,Nt=ei,Ht=ni)},setOp:function(Be,ei,ni){(Tt!==Be||jt!==ei||kt!==ni)&&(r.stencilOp(Be,ei,ni),Tt=Be,jt=ei,kt=ni)},setLocked:function(Be){Z=Be},setClear:function(Be){an!==Be&&(r.clearStencil(Be),an=Be)},reset:function(){Z=!1,Dt=null,_t=null,Nt=null,Ht=null,Tt=null,jt=null,kt=null,an=null}}}const c=new i,f=new s,p=new l,m=new WeakMap,d=new WeakMap;let x={},v={},g={},S=new WeakMap,b=[],w=null,E=!1,_=null,z=null,F=null,C=null,B=null,O=null,U=null,T=new ie(0,0,0),P=0,$=!1,Y=null,J=null,dt=null,gt=null,et=null;const I=r.getParameter(r.MAX_COMBINED_TEXTURE_IMAGE_UNITS);let H=!1,q=0;const ht=r.getParameter(r.VERSION);ht.indexOf("WebGL")!==-1?(q=parseFloat(/^WebGL (\d)/.exec(ht)[1]),H=q>=1):ht.indexOf("OpenGL ES")!==-1&&(q=parseFloat(/^OpenGL ES (\d)/.exec(ht)[1]),H=q>=2);let Et=null,L={};const W=r.getParameter(r.SCISSOR_BOX),yt=r.getParameter(r.VIEWPORT),At=new cn().fromArray(W),wt=new cn().fromArray(yt);function nt(Z,Dt,_t,Nt){const Ht=new Uint8Array(4),Tt=r.createTexture();r.bindTexture(Z,Tt),r.texParameteri(Z,r.TEXTURE_MIN_FILTER,r.NEAREST),r.texParameteri(Z,r.TEXTURE_MAG_FILTER,r.NEAREST);for(let jt=0;jt<_t;jt++)Z===r.TEXTURE_3D||Z===r.TEXTURE_2D_ARRAY?r.texImage3D(Dt,0,r.RGBA,1,1,Nt,0,r.RGBA,r.UNSIGNED_BYTE,Ht):r.texImage2D(Dt+jt,0,r.RGBA,1,1,0,r.RGBA,r.UNSIGNED_BYTE,Ht);return Tt}const vt={};vt[r.TEXTURE_2D]=nt(r.TEXTURE_2D,r.TEXTURE_2D,1),vt[r.TEXTURE_CUBE_MAP]=nt(r.TEXTURE_CUBE_MAP,r.TEXTURE_CUBE_MAP_POSITIVE_X,6),vt[r.TEXTURE_2D_ARRAY]=nt(r.TEXTURE_2D_ARRAY,r.TEXTURE_2D_ARRAY,1,1),vt[r.TEXTURE_3D]=nt(r.TEXTURE_3D,r.TEXTURE_3D,1,1),c.setClear(0,0,0,1),f.setClear(1),p.setClear(0),St(r.DEPTH_TEST),f.setFunc(eo),Ke(!1),Qe(q_),St(r.CULL_FACE),ve(Aa);function St(Z){x[Z]!==!0&&(r.enable(Z),x[Z]=!0)}function Ut(Z){x[Z]!==!1&&(r.disable(Z),x[Z]=!1)}function Jt(Z,Dt){return g[Z]!==Dt?(r.bindFramebuffer(Z,Dt),g[Z]=Dt,Z===r.DRAW_FRAMEBUFFER&&(g[r.FRAMEBUFFER]=Dt),Z===r.FRAMEBUFFER&&(g[r.DRAW_FRAMEBUFFER]=Dt),!0):!1}function Kt(Z,Dt){let _t=b,Nt=!1;if(Z){_t=S.get(Dt),_t===void 0&&(_t=[],S.set(Dt,_t));const Ht=Z.textures;if(_t.length!==Ht.length||_t[0]!==r.COLOR_ATTACHMENT0){for(let Tt=0,jt=Ht.length;Tt<jt;Tt++)_t[Tt]=r.COLOR_ATTACHMENT0+Tt;_t.length=Ht.length,Nt=!0}}else _t[0]!==r.BACK&&(_t[0]=r.BACK,Nt=!0);Nt&&r.drawBuffers(_t)}function Ce(Z){return w!==Z?(r.useProgram(Z),w=Z,!0):!1}const le={[Vs]:r.FUNC_ADD,[H1]:r.FUNC_SUBTRACT,[G1]:r.FUNC_REVERSE_SUBTRACT};le[V1]=r.MIN,le[k1]=r.MAX;const _e={[X1]:r.ZERO,[W1]:r.ONE,[q1]:r.SRC_COLOR,[Nd]:r.SRC_ALPHA,[J1]:r.SRC_ALPHA_SATURATE,[K1]:r.DST_COLOR,[j1]:r.DST_ALPHA,[Y1]:r.ONE_MINUS_SRC_COLOR,[Ld]:r.ONE_MINUS_SRC_ALPHA,[Q1]:r.ONE_MINUS_DST_COLOR,[Z1]:r.ONE_MINUS_DST_ALPHA,[$1]:r.CONSTANT_COLOR,[tE]:r.ONE_MINUS_CONSTANT_COLOR,[eE]:r.CONSTANT_ALPHA,[nE]:r.ONE_MINUS_CONSTANT_ALPHA};function ve(Z,Dt,_t,Nt,Ht,Tt,jt,kt,an,Be){if(Z===Aa){E===!0&&(Ut(r.BLEND),E=!1);return}if(E===!1&&(St(r.BLEND),E=!0),Z!==F1){if(Z!==_||Be!==$){if((z!==Vs||B!==Vs)&&(r.blendEquation(r.FUNC_ADD),z=Vs,B=Vs),Be)switch(Z){case Qr:r.blendFuncSeparate(r.ONE,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case Ys:r.blendFunc(r.ONE,r.ONE);break;case Y_:r.blendFuncSeparate(r.ZERO,r.ONE_MINUS_SRC_COLOR,r.ZERO,r.ONE);break;case j_:r.blendFuncSeparate(r.DST_COLOR,r.ONE_MINUS_SRC_ALPHA,r.ZERO,r.ONE);break;default:Ae("WebGLState: Invalid blending: ",Z);break}else switch(Z){case Qr:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case Ys:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE,r.ONE,r.ONE);break;case Y_:Ae("WebGLState: SubtractiveBlending requires material.premultipliedAlpha = true");break;case j_:Ae("WebGLState: MultiplyBlending requires material.premultipliedAlpha = true");break;default:Ae("WebGLState: Invalid blending: ",Z);break}F=null,C=null,O=null,U=null,T.set(0,0,0),P=0,_=Z,$=Be}return}Ht=Ht||Dt,Tt=Tt||_t,jt=jt||Nt,(Dt!==z||Ht!==B)&&(r.blendEquationSeparate(le[Dt],le[Ht]),z=Dt,B=Ht),(_t!==F||Nt!==C||Tt!==O||jt!==U)&&(r.blendFuncSeparate(_e[_t],_e[Nt],_e[Tt],_e[jt]),F=_t,C=Nt,O=Tt,U=jt),(kt.equals(T)===!1||an!==P)&&(r.blendColor(kt.r,kt.g,kt.b,an),T.copy(kt),P=an),_=Z,$=!1}function de(Z,Dt){Z.side===wi?Ut(r.CULL_FACE):St(r.CULL_FACE);let _t=Z.side===ti;Dt&&(_t=!_t),Ke(_t),Z.blending===Qr&&Z.transparent===!1?ve(Aa):ve(Z.blending,Z.blendEquation,Z.blendSrc,Z.blendDst,Z.blendEquationAlpha,Z.blendSrcAlpha,Z.blendDstAlpha,Z.blendColor,Z.blendAlpha,Z.premultipliedAlpha),f.setFunc(Z.depthFunc),f.setTest(Z.depthTest),f.setMask(Z.depthWrite),c.setMask(Z.colorWrite);const Nt=Z.stencilWrite;p.setTest(Nt),Nt&&(p.setMask(Z.stencilWriteMask),p.setFunc(Z.stencilFunc,Z.stencilRef,Z.stencilFuncMask),p.setOp(Z.stencilFail,Z.stencilZFail,Z.stencilZPass)),nn(Z.polygonOffset,Z.polygonOffsetFactor,Z.polygonOffsetUnits),Z.alphaToCoverage===!0?St(r.SAMPLE_ALPHA_TO_COVERAGE):Ut(r.SAMPLE_ALPHA_TO_COVERAGE)}function Ke(Z){Y!==Z&&(Z?r.frontFace(r.CW):r.frontFace(r.CCW),Y=Z)}function Qe(Z){Z!==z1?(St(r.CULL_FACE),Z!==J&&(Z===q_?r.cullFace(r.BACK):Z===B1?r.cullFace(r.FRONT):r.cullFace(r.FRONT_AND_BACK))):Ut(r.CULL_FACE),J=Z}function Je(Z){Z!==dt&&(H&&r.lineWidth(Z),dt=Z)}function nn(Z,Dt,_t){Z?(St(r.POLYGON_OFFSET_FILL),(gt!==Dt||et!==_t)&&(gt=Dt,et=_t,f.getReversed()&&(Dt=-Dt),r.polygonOffset(Dt,_t))):Ut(r.POLYGON_OFFSET_FILL)}function ze(Z){Z?St(r.SCISSOR_TEST):Ut(r.SCISSOR_TEST)}function $e(Z){Z===void 0&&(Z=r.TEXTURE0+I-1),Et!==Z&&(r.activeTexture(Z),Et=Z)}function K(Z,Dt,_t){_t===void 0&&(Et===null?_t=r.TEXTURE0+I-1:_t=Et);let Nt=L[_t];Nt===void 0&&(Nt={type:void 0,texture:void 0},L[_t]=Nt),(Nt.type!==Z||Nt.texture!==Dt)&&(Et!==_t&&(r.activeTexture(_t),Et=_t),r.bindTexture(Z,Dt||vt[Z]),Nt.type=Z,Nt.texture=Dt)}function Le(){const Z=L[Et];Z!==void 0&&Z.type!==void 0&&(r.bindTexture(Z.type,null),Z.type=void 0,Z.texture=void 0)}function Lt(){try{r.compressedTexImage2D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function D(){try{r.compressedTexImage3D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function M(){try{r.texSubImage2D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function X(){try{r.texSubImage3D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function V(){try{r.compressedTexSubImage2D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function ct(){try{r.compressedTexSubImage3D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function bt(){try{r.texStorage2D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function Ct(){try{r.texStorage3D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function ut(){try{r.texImage2D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function pt(){try{r.texImage3D(...arguments)}catch(Z){Ae("WebGLState:",Z)}}function Rt(Z){return v[Z]!==void 0?v[Z]:r.getParameter(Z)}function Ft(Z,Dt){v[Z]!==Dt&&(r.pixelStorei(Z,Dt),v[Z]=Dt)}function Ot(Z){At.equals(Z)===!1&&(r.scissor(Z.x,Z.y,Z.z,Z.w),At.copy(Z))}function Pt(Z){wt.equals(Z)===!1&&(r.viewport(Z.x,Z.y,Z.z,Z.w),wt.copy(Z))}function $t(Z,Dt){let _t=d.get(Dt);_t===void 0&&(_t=new WeakMap,d.set(Dt,_t));let Nt=_t.get(Z);Nt===void 0&&(Nt=r.getUniformBlockIndex(Dt,Z.name),_t.set(Z,Nt))}function te(Z,Dt){const Nt=d.get(Dt).get(Z);m.get(Dt)!==Nt&&(r.uniformBlockBinding(Dt,Nt,Z.__bindingPointIndex),m.set(Dt,Nt))}function re(){r.disable(r.BLEND),r.disable(r.CULL_FACE),r.disable(r.DEPTH_TEST),r.disable(r.POLYGON_OFFSET_FILL),r.disable(r.SCISSOR_TEST),r.disable(r.STENCIL_TEST),r.disable(r.SAMPLE_ALPHA_TO_COVERAGE),r.blendEquation(r.FUNC_ADD),r.blendFunc(r.ONE,r.ZERO),r.blendFuncSeparate(r.ONE,r.ZERO,r.ONE,r.ZERO),r.blendColor(0,0,0,0),r.colorMask(!0,!0,!0,!0),r.clearColor(0,0,0,0),r.depthMask(!0),r.depthFunc(r.LESS),f.setReversed(!1),r.clearDepth(1),r.stencilMask(4294967295),r.stencilFunc(r.ALWAYS,0,4294967295),r.stencilOp(r.KEEP,r.KEEP,r.KEEP),r.clearStencil(0),r.cullFace(r.BACK),r.frontFace(r.CCW),r.polygonOffset(0,0),r.activeTexture(r.TEXTURE0),r.bindFramebuffer(r.FRAMEBUFFER,null),r.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),r.bindFramebuffer(r.READ_FRAMEBUFFER,null),r.useProgram(null),r.lineWidth(1),r.scissor(0,0,r.canvas.width,r.canvas.height),r.viewport(0,0,r.canvas.width,r.canvas.height),r.pixelStorei(r.PACK_ALIGNMENT,4),r.pixelStorei(r.UNPACK_ALIGNMENT,4),r.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,!1),r.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,!1),r.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,r.BROWSER_DEFAULT_WEBGL),r.pixelStorei(r.PACK_ROW_LENGTH,0),r.pixelStorei(r.PACK_SKIP_PIXELS,0),r.pixelStorei(r.PACK_SKIP_ROWS,0),r.pixelStorei(r.UNPACK_ROW_LENGTH,0),r.pixelStorei(r.UNPACK_IMAGE_HEIGHT,0),r.pixelStorei(r.UNPACK_SKIP_PIXELS,0),r.pixelStorei(r.UNPACK_SKIP_ROWS,0),r.pixelStorei(r.UNPACK_SKIP_IMAGES,0),x={},v={},Et=null,L={},g={},S=new WeakMap,b=[],w=null,E=!1,_=null,z=null,F=null,C=null,B=null,O=null,U=null,T=new ie(0,0,0),P=0,$=!1,Y=null,J=null,dt=null,gt=null,et=null,At.set(0,0,r.canvas.width,r.canvas.height),wt.set(0,0,r.canvas.width,r.canvas.height),c.reset(),f.reset(),p.reset()}return{buffers:{color:c,depth:f,stencil:p},enable:St,disable:Ut,bindFramebuffer:Jt,drawBuffers:Kt,useProgram:Ce,setBlending:ve,setMaterial:de,setFlipSided:Ke,setCullFace:Qe,setLineWidth:Je,setPolygonOffset:nn,setScissorTest:ze,activeTexture:$e,bindTexture:K,unbindTexture:Le,compressedTexImage2D:Lt,compressedTexImage3D:D,texImage2D:ut,texImage3D:pt,pixelStorei:Ft,getParameter:Rt,updateUBOMapping:$t,uniformBlockBinding:te,texStorage2D:bt,texStorage3D:Ct,texSubImage2D:M,texSubImage3D:X,compressedTexSubImage2D:V,compressedTexSubImage3D:ct,scissor:Ot,viewport:Pt,reset:re}}function uR(r,t,i,s,l,c,f){const p=t.has("WEBGL_multisampled_render_to_texture")?t.get("WEBGL_multisampled_render_to_texture"):null,m=typeof navigator>"u"?!1:/OculusBrowser/g.test(navigator.userAgent),d=new oe,x=new WeakMap,v=new Set;let g;const S=new WeakMap;let b=!1;try{b=typeof OffscreenCanvas<"u"&&new OffscreenCanvas(1,1).getContext("2d")!==null}catch{}function w(D,M){return b?new OffscreenCanvas(D,M):bu("canvas")}function E(D,M,X){let V=1;const ct=Lt(D);if((ct.width>X||ct.height>X)&&(V=X/Math.max(ct.width,ct.height)),V<1)if(typeof HTMLImageElement<"u"&&D instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&D instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&D instanceof ImageBitmap||typeof VideoFrame<"u"&&D instanceof VideoFrame){const bt=Math.floor(V*ct.width),Ct=Math.floor(V*ct.height);g===void 0&&(g=w(bt,Ct));const ut=M?w(bt,Ct):g;return ut.width=bt,ut.height=Ct,ut.getContext("2d").drawImage(D,0,0,bt,Ct),se("WebGLRenderer: Texture has been resized from ("+ct.width+"x"+ct.height+") to ("+bt+"x"+Ct+")."),ut}else return"data"in D&&se("WebGLRenderer: Image in DataTexture is too big ("+ct.width+"x"+ct.height+")."),D;return D}function _(D){return D.generateMipmaps}function z(D){r.generateMipmap(D)}function F(D){return D.isWebGLCubeRenderTarget?r.TEXTURE_CUBE_MAP:D.isWebGL3DRenderTarget?r.TEXTURE_3D:D.isWebGLArrayRenderTarget||D.isCompressedArrayTexture?r.TEXTURE_2D_ARRAY:r.TEXTURE_2D}function C(D,M,X,V,ct,bt=!1){if(D!==null){if(r[D]!==void 0)return r[D];se("WebGLRenderer: Attempt to use non-existing WebGL internal format '"+D+"'")}let Ct;V&&(Ct=t.get("EXT_texture_norm16"),Ct||se("WebGLRenderer: Unable to use normalized textures without EXT_texture_norm16 extension"));let ut=M;if(M===r.RED&&(X===r.FLOAT&&(ut=r.R32F),X===r.HALF_FLOAT&&(ut=r.R16F),X===r.UNSIGNED_BYTE&&(ut=r.R8),X===r.UNSIGNED_SHORT&&Ct&&(ut=Ct.R16_EXT),X===r.SHORT&&Ct&&(ut=Ct.R16_SNORM_EXT)),M===r.RED_INTEGER&&(X===r.UNSIGNED_BYTE&&(ut=r.R8UI),X===r.UNSIGNED_SHORT&&(ut=r.R16UI),X===r.UNSIGNED_INT&&(ut=r.R32UI),X===r.BYTE&&(ut=r.R8I),X===r.SHORT&&(ut=r.R16I),X===r.INT&&(ut=r.R32I)),M===r.RG&&(X===r.FLOAT&&(ut=r.RG32F),X===r.HALF_FLOAT&&(ut=r.RG16F),X===r.UNSIGNED_BYTE&&(ut=r.RG8),X===r.UNSIGNED_SHORT&&Ct&&(ut=Ct.RG16_EXT),X===r.SHORT&&Ct&&(ut=Ct.RG16_SNORM_EXT)),M===r.RG_INTEGER&&(X===r.UNSIGNED_BYTE&&(ut=r.RG8UI),X===r.UNSIGNED_SHORT&&(ut=r.RG16UI),X===r.UNSIGNED_INT&&(ut=r.RG32UI),X===r.BYTE&&(ut=r.RG8I),X===r.SHORT&&(ut=r.RG16I),X===r.INT&&(ut=r.RG32I)),M===r.RGB_INTEGER&&(X===r.UNSIGNED_BYTE&&(ut=r.RGB8UI),X===r.UNSIGNED_SHORT&&(ut=r.RGB16UI),X===r.UNSIGNED_INT&&(ut=r.RGB32UI),X===r.BYTE&&(ut=r.RGB8I),X===r.SHORT&&(ut=r.RGB16I),X===r.INT&&(ut=r.RGB32I)),M===r.RGBA_INTEGER&&(X===r.UNSIGNED_BYTE&&(ut=r.RGBA8UI),X===r.UNSIGNED_SHORT&&(ut=r.RGBA16UI),X===r.UNSIGNED_INT&&(ut=r.RGBA32UI),X===r.BYTE&&(ut=r.RGBA8I),X===r.SHORT&&(ut=r.RGBA16I),X===r.INT&&(ut=r.RGBA32I)),M===r.RGB&&(X===r.UNSIGNED_SHORT&&Ct&&(ut=Ct.RGB16_EXT),X===r.SHORT&&Ct&&(ut=Ct.RGB16_SNORM_EXT),X===r.UNSIGNED_INT_5_9_9_9_REV&&(ut=r.RGB9_E5),X===r.UNSIGNED_INT_10F_11F_11F_REV&&(ut=r.R11F_G11F_B10F)),M===r.RGBA){const pt=bt?Eu:Re.getTransfer(ct);X===r.FLOAT&&(ut=r.RGBA32F),X===r.HALF_FLOAT&&(ut=r.RGBA16F),X===r.UNSIGNED_BYTE&&(ut=pt===ke?r.SRGB8_ALPHA8:r.RGBA8),X===r.UNSIGNED_SHORT&&Ct&&(ut=Ct.RGBA16_EXT),X===r.SHORT&&Ct&&(ut=Ct.RGBA16_SNORM_EXT),X===r.UNSIGNED_SHORT_4_4_4_4&&(ut=r.RGBA4),X===r.UNSIGNED_SHORT_5_5_5_1&&(ut=r.RGB5_A1)}return(ut===r.R16F||ut===r.R32F||ut===r.RG16F||ut===r.RG32F||ut===r.RGBA16F||ut===r.RGBA32F)&&t.get("EXT_color_buffer_float"),ut}function B(D,M){let X;return D?M===null||M===Ji||M===yl?X=r.DEPTH24_STENCIL8:M===Ii?X=r.DEPTH32F_STENCIL8:M===xl&&(X=r.DEPTH24_STENCIL8,se("DepthTexture: 16 bit depth attachment is not supported with stencil. Using 24-bit attachment.")):M===null||M===Ji||M===yl?X=r.DEPTH_COMPONENT24:M===Ii?X=r.DEPTH_COMPONENT32F:M===xl&&(X=r.DEPTH_COMPONENT16),X}function O(D,M){return _(D)===!0||D.isFramebufferTexture&&D.minFilter!==zn&&D.minFilter!==Gn?Math.log2(Math.max(M.width,M.height))+1:D.mipmaps!==void 0&&D.mipmaps.length>0?D.mipmaps.length:D.isCompressedTexture&&Array.isArray(D.image)?M.mipmaps.length:1}function U(D){const M=D.target;M.removeEventListener("dispose",U),P(M),M.isVideoTexture&&x.delete(M),M.isHTMLTexture&&v.delete(M)}function T(D){const M=D.target;M.removeEventListener("dispose",T),Y(M)}function P(D){const M=s.get(D);if(M.__webglInit===void 0)return;const X=D.source,V=S.get(X);if(V){const ct=V[M.__cacheKey];ct.usedTimes--,ct.usedTimes===0&&$(D),Object.keys(V).length===0&&S.delete(X)}s.remove(D)}function $(D){const M=s.get(D);r.deleteTexture(M.__webglTexture);const X=D.source,V=S.get(X);delete V[M.__cacheKey],f.memory.textures--}function Y(D){const M=s.get(D);if(D.depthTexture&&(D.depthTexture.dispose(),s.remove(D.depthTexture)),D.isWebGLCubeRenderTarget)for(let V=0;V<6;V++){if(Array.isArray(M.__webglFramebuffer[V]))for(let ct=0;ct<M.__webglFramebuffer[V].length;ct++)r.deleteFramebuffer(M.__webglFramebuffer[V][ct]);else r.deleteFramebuffer(M.__webglFramebuffer[V]);M.__webglDepthbuffer&&r.deleteRenderbuffer(M.__webglDepthbuffer[V])}else{if(Array.isArray(M.__webglFramebuffer))for(let V=0;V<M.__webglFramebuffer.length;V++)r.deleteFramebuffer(M.__webglFramebuffer[V]);else r.deleteFramebuffer(M.__webglFramebuffer);if(M.__webglDepthbuffer&&r.deleteRenderbuffer(M.__webglDepthbuffer),M.__webglMultisampledFramebuffer&&r.deleteFramebuffer(M.__webglMultisampledFramebuffer),M.__webglColorRenderbuffer)for(let V=0;V<M.__webglColorRenderbuffer.length;V++)M.__webglColorRenderbuffer[V]&&r.deleteRenderbuffer(M.__webglColorRenderbuffer[V]);M.__webglDepthRenderbuffer&&r.deleteRenderbuffer(M.__webglDepthRenderbuffer)}const X=D.textures;for(let V=0,ct=X.length;V<ct;V++){const bt=s.get(X[V]);bt.__webglTexture&&(r.deleteTexture(bt.__webglTexture),f.memory.textures--),s.remove(X[V])}s.remove(D)}let J=0;function dt(){J=0}function gt(){return J}function et(D){J=D}function I(){const D=J;return D>=l.maxTextures&&se("WebGLTextures: Trying to use "+D+" texture units while this GPU supports only "+l.maxTextures),J+=1,D}function H(D){const M=[];return M.push(D.wrapS),M.push(D.wrapT),M.push(D.wrapR||0),M.push(D.magFilter),M.push(D.minFilter),M.push(D.anisotropy),M.push(D.internalFormat),M.push(D.format),M.push(D.type),M.push(D.generateMipmaps),M.push(D.premultiplyAlpha),M.push(D.flipY),M.push(D.unpackAlignment),M.push(D.colorSpace),M.join()}function q(D,M){const X=s.get(D);if(D.isVideoTexture&&K(D),D.isRenderTargetTexture===!1&&D.isExternalTexture!==!0&&D.version>0&&X.__version!==D.version){const V=D.image;if(V===null)se("WebGLRenderer: Texture marked for update but no image data found.");else if(V.complete===!1)se("WebGLRenderer: Texture marked for update but image is incomplete");else{Ut(X,D,M);return}}else D.isExternalTexture&&(X.__webglTexture=D.sourceTexture?D.sourceTexture:null);i.bindTexture(r.TEXTURE_2D,X.__webglTexture,r.TEXTURE0+M)}function ht(D,M){const X=s.get(D);if(D.isRenderTargetTexture===!1&&D.version>0&&X.__version!==D.version){Ut(X,D,M);return}else D.isExternalTexture&&(X.__webglTexture=D.sourceTexture?D.sourceTexture:null);i.bindTexture(r.TEXTURE_2D_ARRAY,X.__webglTexture,r.TEXTURE0+M)}function Et(D,M){const X=s.get(D);if(D.isRenderTargetTexture===!1&&D.version>0&&X.__version!==D.version){Ut(X,D,M);return}i.bindTexture(r.TEXTURE_3D,X.__webglTexture,r.TEXTURE0+M)}function L(D,M){const X=s.get(D);if(D.isCubeDepthTexture!==!0&&D.version>0&&X.__version!==D.version){Jt(X,D,M);return}i.bindTexture(r.TEXTURE_CUBE_MAP,X.__webglTexture,r.TEXTURE0+M)}const W={[Gd]:r.REPEAT,[Ta]:r.CLAMP_TO_EDGE,[Vd]:r.MIRRORED_REPEAT},yt={[zn]:r.NEAREST,[sE]:r.NEAREST_MIPMAP_NEAREST,[Oc]:r.NEAREST_MIPMAP_LINEAR,[Gn]:r.LINEAR,[jh]:r.LINEAR_MIPMAP_NEAREST,[Xs]:r.LINEAR_MIPMAP_LINEAR},At={[lE]:r.NEVER,[dE]:r.ALWAYS,[cE]:r.LESS,[Ip]:r.LEQUAL,[uE]:r.EQUAL,[Fp]:r.GEQUAL,[fE]:r.GREATER,[hE]:r.NOTEQUAL};function wt(D,M){if(M.type===Ii&&t.has("OES_texture_float_linear")===!1&&(M.magFilter===Gn||M.magFilter===jh||M.magFilter===Oc||M.magFilter===Xs||M.minFilter===Gn||M.minFilter===jh||M.minFilter===Oc||M.minFilter===Xs)&&se("WebGLRenderer: Unable to use linear filtering with floating point textures. OES_texture_float_linear not supported on this device."),r.texParameteri(D,r.TEXTURE_WRAP_S,W[M.wrapS]),r.texParameteri(D,r.TEXTURE_WRAP_T,W[M.wrapT]),(D===r.TEXTURE_3D||D===r.TEXTURE_2D_ARRAY)&&r.texParameteri(D,r.TEXTURE_WRAP_R,W[M.wrapR]),r.texParameteri(D,r.TEXTURE_MAG_FILTER,yt[M.magFilter]),r.texParameteri(D,r.TEXTURE_MIN_FILTER,yt[M.minFilter]),M.compareFunction&&(r.texParameteri(D,r.TEXTURE_COMPARE_MODE,r.COMPARE_REF_TO_TEXTURE),r.texParameteri(D,r.TEXTURE_COMPARE_FUNC,At[M.compareFunction])),t.has("EXT_texture_filter_anisotropic")===!0){if(M.magFilter===zn||M.minFilter!==Oc&&M.minFilter!==Xs||M.type===Ii&&t.has("OES_texture_float_linear")===!1)return;if(M.anisotropy>1||s.get(M).__currentAnisotropy){const X=t.get("EXT_texture_filter_anisotropic");r.texParameterf(D,X.TEXTURE_MAX_ANISOTROPY_EXT,Math.min(M.anisotropy,l.getMaxAnisotropy())),s.get(M).__currentAnisotropy=M.anisotropy}}}function nt(D,M){let X=!1;D.__webglInit===void 0&&(D.__webglInit=!0,M.addEventListener("dispose",U));const V=M.source;let ct=S.get(V);ct===void 0&&(ct={},S.set(V,ct));const bt=H(M);if(bt!==D.__cacheKey){ct[bt]===void 0&&(ct[bt]={texture:r.createTexture(),usedTimes:0},f.memory.textures++,X=!0),ct[bt].usedTimes++;const Ct=ct[D.__cacheKey];Ct!==void 0&&(ct[D.__cacheKey].usedTimes--,Ct.usedTimes===0&&$(M)),D.__cacheKey=bt,D.__webglTexture=ct[bt].texture}return X}function vt(D,M,X){return Math.floor(Math.floor(D/X)/M)}function St(D,M,X,V){const bt=D.updateRanges;if(bt.length===0)i.texSubImage2D(r.TEXTURE_2D,0,0,0,M.width,M.height,X,V,M.data);else{bt.sort((Ft,Ot)=>Ft.start-Ot.start);let Ct=0;for(let Ft=1;Ft<bt.length;Ft++){const Ot=bt[Ct],Pt=bt[Ft],$t=Ot.start+Ot.count,te=vt(Pt.start,M.width,4),re=vt(Ot.start,M.width,4);Pt.start<=$t+1&&te===re&&vt(Pt.start+Pt.count-1,M.width,4)===te?Ot.count=Math.max(Ot.count,Pt.start+Pt.count-Ot.start):(++Ct,bt[Ct]=Pt)}bt.length=Ct+1;const ut=i.getParameter(r.UNPACK_ROW_LENGTH),pt=i.getParameter(r.UNPACK_SKIP_PIXELS),Rt=i.getParameter(r.UNPACK_SKIP_ROWS);i.pixelStorei(r.UNPACK_ROW_LENGTH,M.width);for(let Ft=0,Ot=bt.length;Ft<Ot;Ft++){const Pt=bt[Ft],$t=Math.floor(Pt.start/4),te=Math.ceil(Pt.count/4),re=$t%M.width,Z=Math.floor($t/M.width),Dt=te,_t=1;i.pixelStorei(r.UNPACK_SKIP_PIXELS,re),i.pixelStorei(r.UNPACK_SKIP_ROWS,Z),i.texSubImage2D(r.TEXTURE_2D,0,re,Z,Dt,_t,X,V,M.data)}D.clearUpdateRanges(),i.pixelStorei(r.UNPACK_ROW_LENGTH,ut),i.pixelStorei(r.UNPACK_SKIP_PIXELS,pt),i.pixelStorei(r.UNPACK_SKIP_ROWS,Rt)}}function Ut(D,M,X){let V=r.TEXTURE_2D;(M.isDataArrayTexture||M.isCompressedArrayTexture)&&(V=r.TEXTURE_2D_ARRAY),M.isData3DTexture&&(V=r.TEXTURE_3D);const ct=nt(D,M),bt=M.source;i.bindTexture(V,D.__webglTexture,r.TEXTURE0+X);const Ct=s.get(bt);if(bt.version!==Ct.__version||ct===!0){if(i.activeTexture(r.TEXTURE0+X),(typeof ImageBitmap<"u"&&M.image instanceof ImageBitmap)===!1){const _t=Re.getPrimaries(Re.workingColorSpace),Nt=M.colorSpace===us?null:Re.getPrimaries(M.colorSpace),Ht=M.colorSpace===us||_t===Nt?r.NONE:r.BROWSER_DEFAULT_WEBGL;i.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,M.flipY),i.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,M.premultiplyAlpha),i.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,Ht)}i.pixelStorei(r.UNPACK_ALIGNMENT,M.unpackAlignment);let pt=E(M.image,!1,l.maxTextureSize);pt=Le(M,pt);const Rt=c.convert(M.format,M.colorSpace),Ft=c.convert(M.type);let Ot=C(M.internalFormat,Rt,Ft,M.normalized,M.colorSpace,M.isVideoTexture);wt(V,M);let Pt;const $t=M.mipmaps,te=M.isVideoTexture!==!0,re=Ct.__version===void 0||ct===!0,Z=bt.dataReady,Dt=O(M,pt);if(M.isDepthTexture)Ot=B(M.format===Ws,M.type),re&&(te?i.texStorage2D(r.TEXTURE_2D,1,Ot,pt.width,pt.height):i.texImage2D(r.TEXTURE_2D,0,Ot,pt.width,pt.height,0,Rt,Ft,null));else if(M.isDataTexture)if($t.length>0){te&&re&&i.texStorage2D(r.TEXTURE_2D,Dt,Ot,$t[0].width,$t[0].height);for(let _t=0,Nt=$t.length;_t<Nt;_t++)Pt=$t[_t],te?Z&&i.texSubImage2D(r.TEXTURE_2D,_t,0,0,Pt.width,Pt.height,Rt,Ft,Pt.data):i.texImage2D(r.TEXTURE_2D,_t,Ot,Pt.width,Pt.height,0,Rt,Ft,Pt.data);M.generateMipmaps=!1}else te?(re&&i.texStorage2D(r.TEXTURE_2D,Dt,Ot,pt.width,pt.height),Z&&St(M,pt,Rt,Ft)):i.texImage2D(r.TEXTURE_2D,0,Ot,pt.width,pt.height,0,Rt,Ft,pt.data);else if(M.isCompressedTexture)if(M.isCompressedArrayTexture){te&&re&&i.texStorage3D(r.TEXTURE_2D_ARRAY,Dt,Ot,$t[0].width,$t[0].height,pt.depth);for(let _t=0,Nt=$t.length;_t<Nt;_t++)if(Pt=$t[_t],M.format!==Fi)if(Rt!==null)if(te){if(Z)if(M.layerUpdates.size>0){const Ht=Ov(Pt.width,Pt.height,M.format,M.type);for(const Tt of M.layerUpdates){const jt=Pt.data.subarray(Tt*Ht/Pt.data.BYTES_PER_ELEMENT,(Tt+1)*Ht/Pt.data.BYTES_PER_ELEMENT);i.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,_t,0,0,Tt,Pt.width,Pt.height,1,Rt,jt)}M.clearLayerUpdates()}else i.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,_t,0,0,0,Pt.width,Pt.height,pt.depth,Rt,Pt.data)}else i.compressedTexImage3D(r.TEXTURE_2D_ARRAY,_t,Ot,Pt.width,Pt.height,pt.depth,0,Pt.data,0,0);else se("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()");else te?Z&&i.texSubImage3D(r.TEXTURE_2D_ARRAY,_t,0,0,0,Pt.width,Pt.height,pt.depth,Rt,Ft,Pt.data):i.texImage3D(r.TEXTURE_2D_ARRAY,_t,Ot,Pt.width,Pt.height,pt.depth,0,Rt,Ft,Pt.data)}else{te&&re&&i.texStorage2D(r.TEXTURE_2D,Dt,Ot,$t[0].width,$t[0].height);for(let _t=0,Nt=$t.length;_t<Nt;_t++)Pt=$t[_t],M.format!==Fi?Rt!==null?te?Z&&i.compressedTexSubImage2D(r.TEXTURE_2D,_t,0,0,Pt.width,Pt.height,Rt,Pt.data):i.compressedTexImage2D(r.TEXTURE_2D,_t,Ot,Pt.width,Pt.height,0,Pt.data):se("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()"):te?Z&&i.texSubImage2D(r.TEXTURE_2D,_t,0,0,Pt.width,Pt.height,Rt,Ft,Pt.data):i.texImage2D(r.TEXTURE_2D,_t,Ot,Pt.width,Pt.height,0,Rt,Ft,Pt.data)}else if(M.isDataArrayTexture)if(te){if(re&&i.texStorage3D(r.TEXTURE_2D_ARRAY,Dt,Ot,pt.width,pt.height,pt.depth),Z)if(M.layerUpdates.size>0){const _t=Ov(pt.width,pt.height,M.format,M.type);for(const Nt of M.layerUpdates){const Ht=pt.data.subarray(Nt*_t/pt.data.BYTES_PER_ELEMENT,(Nt+1)*_t/pt.data.BYTES_PER_ELEMENT);i.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,Nt,pt.width,pt.height,1,Rt,Ft,Ht)}M.clearLayerUpdates()}else i.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,0,pt.width,pt.height,pt.depth,Rt,Ft,pt.data)}else i.texImage3D(r.TEXTURE_2D_ARRAY,0,Ot,pt.width,pt.height,pt.depth,0,Rt,Ft,pt.data);else if(M.isData3DTexture)te?(re&&i.texStorage3D(r.TEXTURE_3D,Dt,Ot,pt.width,pt.height,pt.depth),Z&&i.texSubImage3D(r.TEXTURE_3D,0,0,0,0,pt.width,pt.height,pt.depth,Rt,Ft,pt.data)):i.texImage3D(r.TEXTURE_3D,0,Ot,pt.width,pt.height,pt.depth,0,Rt,Ft,pt.data);else if(M.isFramebufferTexture){if(re)if(te)i.texStorage2D(r.TEXTURE_2D,Dt,Ot,pt.width,pt.height);else{let _t=pt.width,Nt=pt.height;for(let Ht=0;Ht<Dt;Ht++)i.texImage2D(r.TEXTURE_2D,Ht,Ot,_t,Nt,0,Rt,Ft,null),_t>>=1,Nt>>=1}}else if(M.isHTMLTexture){if("texElementImage2D"in r){const _t=r.canvas;if(_t.hasAttribute("layoutsubtree")||_t.setAttribute("layoutsubtree","true"),pt.parentNode!==_t){_t.appendChild(pt),v.add(M),_t.onpaint=Nt=>{const Ht=Nt.changedElements;for(const Tt of v)Ht.includes(Tt.image)&&(Tt.needsUpdate=!0)},_t.requestPaint();return}if(r.texElementImage2D.length===3)r.texElementImage2D(r.TEXTURE_2D,r.RGBA8,pt);else{const Ht=r.RGBA,Tt=r.RGBA,jt=r.UNSIGNED_BYTE;r.texElementImage2D(r.TEXTURE_2D,0,Ht,Tt,jt,pt)}r.texParameteri(r.TEXTURE_2D,r.TEXTURE_MIN_FILTER,r.LINEAR),r.texParameteri(r.TEXTURE_2D,r.TEXTURE_WRAP_S,r.CLAMP_TO_EDGE),r.texParameteri(r.TEXTURE_2D,r.TEXTURE_WRAP_T,r.CLAMP_TO_EDGE)}}else if($t.length>0){if(te&&re){const _t=Lt($t[0]);i.texStorage2D(r.TEXTURE_2D,Dt,Ot,_t.width,_t.height)}for(let _t=0,Nt=$t.length;_t<Nt;_t++)Pt=$t[_t],te?Z&&i.texSubImage2D(r.TEXTURE_2D,_t,0,0,Rt,Ft,Pt):i.texImage2D(r.TEXTURE_2D,_t,Ot,Rt,Ft,Pt);M.generateMipmaps=!1}else if(te){if(re){const _t=Lt(pt);i.texStorage2D(r.TEXTURE_2D,Dt,Ot,_t.width,_t.height)}Z&&i.texSubImage2D(r.TEXTURE_2D,0,0,0,Rt,Ft,pt)}else i.texImage2D(r.TEXTURE_2D,0,Ot,Rt,Ft,pt);_(M)&&z(V),Ct.__version=bt.version,M.onUpdate&&M.onUpdate(M)}D.__version=M.version}function Jt(D,M,X){if(M.image.length!==6)return;const V=nt(D,M),ct=M.source;i.bindTexture(r.TEXTURE_CUBE_MAP,D.__webglTexture,r.TEXTURE0+X);const bt=s.get(ct);if(ct.version!==bt.__version||V===!0){i.activeTexture(r.TEXTURE0+X);const Ct=Re.getPrimaries(Re.workingColorSpace),ut=M.colorSpace===us?null:Re.getPrimaries(M.colorSpace),pt=M.colorSpace===us||Ct===ut?r.NONE:r.BROWSER_DEFAULT_WEBGL;i.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,M.flipY),i.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,M.premultiplyAlpha),i.pixelStorei(r.UNPACK_ALIGNMENT,M.unpackAlignment),i.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,pt);const Rt=M.isCompressedTexture||M.image[0].isCompressedTexture,Ft=M.image[0]&&M.image[0].isDataTexture,Ot=[];for(let Tt=0;Tt<6;Tt++)!Rt&&!Ft?Ot[Tt]=E(M.image[Tt],!0,l.maxCubemapSize):Ot[Tt]=Ft?M.image[Tt].image:M.image[Tt],Ot[Tt]=Le(M,Ot[Tt]);const Pt=Ot[0],$t=c.convert(M.format,M.colorSpace),te=c.convert(M.type),re=C(M.internalFormat,$t,te,M.normalized,M.colorSpace),Z=M.isVideoTexture!==!0,Dt=bt.__version===void 0||V===!0,_t=ct.dataReady;let Nt=O(M,Pt);wt(r.TEXTURE_CUBE_MAP,M);let Ht;if(Rt){Z&&Dt&&i.texStorage2D(r.TEXTURE_CUBE_MAP,Nt,re,Pt.width,Pt.height);for(let Tt=0;Tt<6;Tt++){Ht=Ot[Tt].mipmaps;for(let jt=0;jt<Ht.length;jt++){const kt=Ht[jt];M.format!==Fi?$t!==null?Z?_t&&i.compressedTexSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,0,0,kt.width,kt.height,$t,kt.data):i.compressedTexImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,re,kt.width,kt.height,0,kt.data):se("WebGLRenderer: Attempt to load unsupported compressed texture format in .setTextureCube()"):Z?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,0,0,kt.width,kt.height,$t,te,kt.data):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,re,kt.width,kt.height,0,$t,te,kt.data)}}}else{if(Ht=M.mipmaps,Z&&Dt){Ht.length>0&&Nt++;const Tt=Lt(Ot[0]);i.texStorage2D(r.TEXTURE_CUBE_MAP,Nt,re,Tt.width,Tt.height)}for(let Tt=0;Tt<6;Tt++)if(Ft){Z?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,0,0,Ot[Tt].width,Ot[Tt].height,$t,te,Ot[Tt].data):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,re,Ot[Tt].width,Ot[Tt].height,0,$t,te,Ot[Tt].data);for(let jt=0;jt<Ht.length;jt++){const an=Ht[jt].image[Tt].image;Z?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,0,0,an.width,an.height,$t,te,an.data):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,re,an.width,an.height,0,$t,te,an.data)}}else{Z?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,0,0,$t,te,Ot[Tt]):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,re,$t,te,Ot[Tt]);for(let jt=0;jt<Ht.length;jt++){const kt=Ht[jt];Z?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,0,0,$t,te,kt.image[Tt]):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,re,$t,te,kt.image[Tt])}}}_(M)&&z(r.TEXTURE_CUBE_MAP),bt.__version=ct.version,M.onUpdate&&M.onUpdate(M)}D.__version=M.version}function Kt(D,M,X,V,ct,bt){const Ct=c.convert(X.format,X.colorSpace),ut=c.convert(X.type),pt=C(X.internalFormat,Ct,ut,X.normalized,X.colorSpace),Rt=s.get(M),Ft=s.get(X);if(Ft.__renderTarget=M,!Rt.__hasExternalTextures){const Ot=Math.max(1,M.width>>bt),Pt=Math.max(1,M.height>>bt);ct===r.TEXTURE_3D||ct===r.TEXTURE_2D_ARRAY?i.texImage3D(ct,bt,pt,Ot,Pt,M.depth,0,Ct,ut,null):i.texImage2D(ct,bt,pt,Ot,Pt,0,Ct,ut,null)}i.bindFramebuffer(r.FRAMEBUFFER,D),$e(M)?p.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,V,ct,Ft.__webglTexture,0,ze(M)):(ct===r.TEXTURE_2D||ct>=r.TEXTURE_CUBE_MAP_POSITIVE_X&&ct<=r.TEXTURE_CUBE_MAP_NEGATIVE_Z)&&r.framebufferTexture2D(r.FRAMEBUFFER,V,ct,Ft.__webglTexture,bt),i.bindFramebuffer(r.FRAMEBUFFER,null)}function Ce(D,M,X){if(r.bindRenderbuffer(r.RENDERBUFFER,D),M.depthBuffer){const V=M.depthTexture,ct=V&&V.isDepthTexture?V.type:null,bt=B(M.stencilBuffer,ct),Ct=M.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;$e(M)?p.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,ze(M),bt,M.width,M.height):X?r.renderbufferStorageMultisample(r.RENDERBUFFER,ze(M),bt,M.width,M.height):r.renderbufferStorage(r.RENDERBUFFER,bt,M.width,M.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,Ct,r.RENDERBUFFER,D)}else{const V=M.textures;for(let ct=0;ct<V.length;ct++){const bt=V[ct],Ct=c.convert(bt.format,bt.colorSpace),ut=c.convert(bt.type),pt=C(bt.internalFormat,Ct,ut,bt.normalized,bt.colorSpace);$e(M)?p.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,ze(M),pt,M.width,M.height):X?r.renderbufferStorageMultisample(r.RENDERBUFFER,ze(M),pt,M.width,M.height):r.renderbufferStorage(r.RENDERBUFFER,pt,M.width,M.height)}}r.bindRenderbuffer(r.RENDERBUFFER,null)}function le(D,M,X){const V=M.isWebGLCubeRenderTarget===!0;if(i.bindFramebuffer(r.FRAMEBUFFER,D),!(M.depthTexture&&M.depthTexture.isDepthTexture))throw new Error("THREE.WebGLTextures: renderTarget.depthTexture must be an instance of THREE.DepthTexture.");const ct=s.get(M.depthTexture);if(ct.__renderTarget=M,(!ct.__webglTexture||M.depthTexture.image.width!==M.width||M.depthTexture.image.height!==M.height)&&(M.depthTexture.image.width=M.width,M.depthTexture.image.height=M.height,M.depthTexture.needsUpdate=!0),V){if(ct.__webglInit===void 0&&(ct.__webglInit=!0,M.depthTexture.addEventListener("dispose",U)),ct.__webglTexture===void 0){ct.__webglTexture=r.createTexture(),i.bindTexture(r.TEXTURE_CUBE_MAP,ct.__webglTexture),wt(r.TEXTURE_CUBE_MAP,M.depthTexture);const Rt=c.convert(M.depthTexture.format),Ft=c.convert(M.depthTexture.type);let Ot;M.depthTexture.format===Da?Ot=r.DEPTH_COMPONENT24:M.depthTexture.format===Ws&&(Ot=r.DEPTH24_STENCIL8);for(let Pt=0;Pt<6;Pt++)r.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Pt,0,Ot,M.width,M.height,0,Rt,Ft,null)}}else q(M.depthTexture,0);const bt=ct.__webglTexture,Ct=ze(M),ut=V?r.TEXTURE_CUBE_MAP_POSITIVE_X+X:r.TEXTURE_2D,pt=M.depthTexture.format===Ws?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;if(M.depthTexture.format===Da)$e(M)?p.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,pt,ut,bt,0,Ct):r.framebufferTexture2D(r.FRAMEBUFFER,pt,ut,bt,0);else if(M.depthTexture.format===Ws)$e(M)?p.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,pt,ut,bt,0,Ct):r.framebufferTexture2D(r.FRAMEBUFFER,pt,ut,bt,0);else throw new Error("THREE.WebGLTextures: Unknown depthTexture format.")}function _e(D){const M=s.get(D),X=D.isWebGLCubeRenderTarget===!0;if(M.__boundDepthTexture!==D.depthTexture){const V=D.depthTexture;if(M.__depthDisposeCallback&&M.__depthDisposeCallback(),V){const ct=()=>{delete M.__boundDepthTexture,delete M.__depthDisposeCallback,V.removeEventListener("dispose",ct)};V.addEventListener("dispose",ct),M.__depthDisposeCallback=ct}M.__boundDepthTexture=V}if(D.depthTexture&&!M.__autoAllocateDepthBuffer)if(X)for(let V=0;V<6;V++)le(M.__webglFramebuffer[V],D,V);else{const V=D.texture.mipmaps;V&&V.length>0?le(M.__webglFramebuffer[0],D,0):le(M.__webglFramebuffer,D,0)}else if(X){M.__webglDepthbuffer=[];for(let V=0;V<6;V++)if(i.bindFramebuffer(r.FRAMEBUFFER,M.__webglFramebuffer[V]),M.__webglDepthbuffer[V]===void 0)M.__webglDepthbuffer[V]=r.createRenderbuffer(),Ce(M.__webglDepthbuffer[V],D,!1);else{const ct=D.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,bt=M.__webglDepthbuffer[V];r.bindRenderbuffer(r.RENDERBUFFER,bt),r.framebufferRenderbuffer(r.FRAMEBUFFER,ct,r.RENDERBUFFER,bt)}}else{const V=D.texture.mipmaps;if(V&&V.length>0?i.bindFramebuffer(r.FRAMEBUFFER,M.__webglFramebuffer[0]):i.bindFramebuffer(r.FRAMEBUFFER,M.__webglFramebuffer),M.__webglDepthbuffer===void 0)M.__webglDepthbuffer=r.createRenderbuffer(),Ce(M.__webglDepthbuffer,D,!1);else{const ct=D.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,bt=M.__webglDepthbuffer;r.bindRenderbuffer(r.RENDERBUFFER,bt),r.framebufferRenderbuffer(r.FRAMEBUFFER,ct,r.RENDERBUFFER,bt)}}i.bindFramebuffer(r.FRAMEBUFFER,null)}function ve(D,M,X){const V=s.get(D);M!==void 0&&Kt(V.__webglFramebuffer,D,D.texture,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,0),X!==void 0&&_e(D)}function de(D){const M=D.texture,X=s.get(D),V=s.get(M);D.addEventListener("dispose",T);const ct=D.textures,bt=D.isWebGLCubeRenderTarget===!0,Ct=ct.length>1;if(Ct||(V.__webglTexture===void 0&&(V.__webglTexture=r.createTexture()),V.__version=M.version,f.memory.textures++),bt){X.__webglFramebuffer=[];for(let ut=0;ut<6;ut++)if(M.mipmaps&&M.mipmaps.length>0){X.__webglFramebuffer[ut]=[];for(let pt=0;pt<M.mipmaps.length;pt++)X.__webglFramebuffer[ut][pt]=r.createFramebuffer()}else X.__webglFramebuffer[ut]=r.createFramebuffer()}else{if(M.mipmaps&&M.mipmaps.length>0){X.__webglFramebuffer=[];for(let ut=0;ut<M.mipmaps.length;ut++)X.__webglFramebuffer[ut]=r.createFramebuffer()}else X.__webglFramebuffer=r.createFramebuffer();if(Ct)for(let ut=0,pt=ct.length;ut<pt;ut++){const Rt=s.get(ct[ut]);Rt.__webglTexture===void 0&&(Rt.__webglTexture=r.createTexture(),f.memory.textures++)}if(D.samples>0&&$e(D)===!1){X.__webglMultisampledFramebuffer=r.createFramebuffer(),X.__webglColorRenderbuffer=[],i.bindFramebuffer(r.FRAMEBUFFER,X.__webglMultisampledFramebuffer);for(let ut=0;ut<ct.length;ut++){const pt=ct[ut];X.__webglColorRenderbuffer[ut]=r.createRenderbuffer(),r.bindRenderbuffer(r.RENDERBUFFER,X.__webglColorRenderbuffer[ut]);const Rt=c.convert(pt.format,pt.colorSpace),Ft=c.convert(pt.type),Ot=C(pt.internalFormat,Rt,Ft,pt.normalized,pt.colorSpace,D.isXRRenderTarget===!0),Pt=ze(D);r.renderbufferStorageMultisample(r.RENDERBUFFER,Pt,Ot,D.width,D.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+ut,r.RENDERBUFFER,X.__webglColorRenderbuffer[ut])}r.bindRenderbuffer(r.RENDERBUFFER,null),D.depthBuffer&&(X.__webglDepthRenderbuffer=r.createRenderbuffer(),Ce(X.__webglDepthRenderbuffer,D,!0)),i.bindFramebuffer(r.FRAMEBUFFER,null)}}if(bt){i.bindTexture(r.TEXTURE_CUBE_MAP,V.__webglTexture),wt(r.TEXTURE_CUBE_MAP,M);for(let ut=0;ut<6;ut++)if(M.mipmaps&&M.mipmaps.length>0)for(let pt=0;pt<M.mipmaps.length;pt++)Kt(X.__webglFramebuffer[ut][pt],D,M,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+ut,pt);else Kt(X.__webglFramebuffer[ut],D,M,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+ut,0);_(M)&&z(r.TEXTURE_CUBE_MAP),i.unbindTexture()}else if(Ct){for(let ut=0,pt=ct.length;ut<pt;ut++){const Rt=ct[ut],Ft=s.get(Rt);let Ot=r.TEXTURE_2D;(D.isWebGL3DRenderTarget||D.isWebGLArrayRenderTarget)&&(Ot=D.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),i.bindTexture(Ot,Ft.__webglTexture),wt(Ot,Rt),Kt(X.__webglFramebuffer,D,Rt,r.COLOR_ATTACHMENT0+ut,Ot,0),_(Rt)&&z(Ot)}i.unbindTexture()}else{let ut=r.TEXTURE_2D;if((D.isWebGL3DRenderTarget||D.isWebGLArrayRenderTarget)&&(ut=D.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),i.bindTexture(ut,V.__webglTexture),wt(ut,M),M.mipmaps&&M.mipmaps.length>0)for(let pt=0;pt<M.mipmaps.length;pt++)Kt(X.__webglFramebuffer[pt],D,M,r.COLOR_ATTACHMENT0,ut,pt);else Kt(X.__webglFramebuffer,D,M,r.COLOR_ATTACHMENT0,ut,0);_(M)&&z(ut),i.unbindTexture()}D.depthBuffer&&_e(D)}function Ke(D){const M=D.textures;for(let X=0,V=M.length;X<V;X++){const ct=M[X];if(_(ct)){const bt=F(D),Ct=s.get(ct).__webglTexture;i.bindTexture(bt,Ct),z(bt),i.unbindTexture()}}}const Qe=[],Je=[];function nn(D){if(D.samples>0){if($e(D)===!1){const M=D.textures,X=D.width,V=D.height;let ct=r.COLOR_BUFFER_BIT;const bt=D.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,Ct=s.get(D),ut=M.length>1;if(ut)for(let Rt=0;Rt<M.length;Rt++)i.bindFramebuffer(r.FRAMEBUFFER,Ct.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+Rt,r.RENDERBUFFER,null),i.bindFramebuffer(r.FRAMEBUFFER,Ct.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+Rt,r.TEXTURE_2D,null,0);i.bindFramebuffer(r.READ_FRAMEBUFFER,Ct.__webglMultisampledFramebuffer);const pt=D.texture.mipmaps;pt&&pt.length>0?i.bindFramebuffer(r.DRAW_FRAMEBUFFER,Ct.__webglFramebuffer[0]):i.bindFramebuffer(r.DRAW_FRAMEBUFFER,Ct.__webglFramebuffer);for(let Rt=0;Rt<M.length;Rt++){if(D.resolveDepthBuffer&&(D.depthBuffer&&(ct|=r.DEPTH_BUFFER_BIT),D.stencilBuffer&&D.resolveStencilBuffer&&(ct|=r.STENCIL_BUFFER_BIT)),ut){r.framebufferRenderbuffer(r.READ_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.RENDERBUFFER,Ct.__webglColorRenderbuffer[Rt]);const Ft=s.get(M[Rt]).__webglTexture;r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,Ft,0)}r.blitFramebuffer(0,0,X,V,0,0,X,V,ct,r.NEAREST),m===!0&&(Qe.length=0,Je.length=0,Qe.push(r.COLOR_ATTACHMENT0+Rt),D.depthBuffer&&D.resolveDepthBuffer===!1&&(Qe.push(bt),Je.push(bt),r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,Je)),r.invalidateFramebuffer(r.READ_FRAMEBUFFER,Qe))}if(i.bindFramebuffer(r.READ_FRAMEBUFFER,null),i.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),ut)for(let Rt=0;Rt<M.length;Rt++){i.bindFramebuffer(r.FRAMEBUFFER,Ct.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+Rt,r.RENDERBUFFER,Ct.__webglColorRenderbuffer[Rt]);const Ft=s.get(M[Rt]).__webglTexture;i.bindFramebuffer(r.FRAMEBUFFER,Ct.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+Rt,r.TEXTURE_2D,Ft,0)}i.bindFramebuffer(r.DRAW_FRAMEBUFFER,Ct.__webglMultisampledFramebuffer)}else if(D.depthBuffer&&D.resolveDepthBuffer===!1&&m){const M=D.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,[M])}}}function ze(D){return Math.min(l.maxSamples,D.samples)}function $e(D){const M=s.get(D);return D.samples>0&&t.has("WEBGL_multisampled_render_to_texture")===!0&&M.__useRenderToTexture!==!1}function K(D){const M=f.render.frame;x.get(D)!==M&&(x.set(D,M),D.update())}function Le(D,M){const X=D.colorSpace,V=D.format,ct=D.type;return D.isCompressedTexture===!0||D.isVideoTexture===!0||X!==Mu&&X!==us&&(Re.getTransfer(X)===ke?(V!==Fi||ct!==gi)&&se("WebGLTextures: sRGB encoded textures have to use RGBAFormat and UnsignedByteType."):Ae("WebGLTextures: Unsupported texture color space:",X)),M}function Lt(D){return typeof HTMLImageElement<"u"&&D instanceof HTMLImageElement?(d.width=D.naturalWidth||D.width,d.height=D.naturalHeight||D.height):typeof VideoFrame<"u"&&D instanceof VideoFrame?(d.width=D.displayWidth,d.height=D.displayHeight):(d.width=D.width,d.height=D.height),d}this.allocateTextureUnit=I,this.resetTextureUnits=dt,this.getTextureUnits=gt,this.setTextureUnits=et,this.setTexture2D=q,this.setTexture2DArray=ht,this.setTexture3D=Et,this.setTextureCube=L,this.rebindTextures=ve,this.setupRenderTarget=de,this.updateRenderTargetMipmap=Ke,this.updateMultisampleRenderTarget=nn,this.setupDepthRenderbuffer=_e,this.setupFrameBufferTexture=Kt,this.useMultisampledRTT=$e,this.isReversedDepthBuffer=function(){return i.buffers.depth.getReversed()}}function fR(r,t){function i(s,l=us){let c;const f=Re.getTransfer(l);if(s===gi)return r.UNSIGNED_BYTE;if(s===Np)return r.UNSIGNED_SHORT_4_4_4_4;if(s===Lp)return r.UNSIGNED_SHORT_5_5_5_1;if(s===Ax)return r.UNSIGNED_INT_5_9_9_9_REV;if(s===Rx)return r.UNSIGNED_INT_10F_11F_11F_REV;if(s===bx)return r.BYTE;if(s===Tx)return r.SHORT;if(s===xl)return r.UNSIGNED_SHORT;if(s===Up)return r.INT;if(s===Ji)return r.UNSIGNED_INT;if(s===Ii)return r.FLOAT;if(s===Ca)return r.HALF_FLOAT;if(s===wx)return r.ALPHA;if(s===Cx)return r.RGB;if(s===Fi)return r.RGBA;if(s===Da)return r.DEPTH_COMPONENT;if(s===Ws)return r.DEPTH_STENCIL;if(s===Op)return r.RED;if(s===Pp)return r.RED_INTEGER;if(s===Zs)return r.RG;if(s===zp)return r.RG_INTEGER;if(s===Bp)return r.RGBA_INTEGER;if(s===pu||s===mu||s===gu||s===_u)if(f===ke)if(c=t.get("WEBGL_compressed_texture_s3tc_srgb"),c!==null){if(s===pu)return c.COMPRESSED_SRGB_S3TC_DXT1_EXT;if(s===mu)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;if(s===gu)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;if(s===_u)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT}else return null;else if(c=t.get("WEBGL_compressed_texture_s3tc"),c!==null){if(s===pu)return c.COMPRESSED_RGB_S3TC_DXT1_EXT;if(s===mu)return c.COMPRESSED_RGBA_S3TC_DXT1_EXT;if(s===gu)return c.COMPRESSED_RGBA_S3TC_DXT3_EXT;if(s===_u)return c.COMPRESSED_RGBA_S3TC_DXT5_EXT}else return null;if(s===kd||s===Xd||s===Wd||s===qd)if(c=t.get("WEBGL_compressed_texture_pvrtc"),c!==null){if(s===kd)return c.COMPRESSED_RGB_PVRTC_4BPPV1_IMG;if(s===Xd)return c.COMPRESSED_RGB_PVRTC_2BPPV1_IMG;if(s===Wd)return c.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;if(s===qd)return c.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG}else return null;if(s===Yd||s===jd||s===Zd||s===Kd||s===Qd||s===yu||s===Jd)if(c=t.get("WEBGL_compressed_texture_etc"),c!==null){if(s===Yd||s===jd)return f===ke?c.COMPRESSED_SRGB8_ETC2:c.COMPRESSED_RGB8_ETC2;if(s===Zd)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:c.COMPRESSED_RGBA8_ETC2_EAC;if(s===Kd)return c.COMPRESSED_R11_EAC;if(s===Qd)return c.COMPRESSED_SIGNED_R11_EAC;if(s===yu)return c.COMPRESSED_RG11_EAC;if(s===Jd)return c.COMPRESSED_SIGNED_RG11_EAC}else return null;if(s===$d||s===tp||s===ep||s===np||s===ip||s===ap||s===sp||s===rp||s===op||s===lp||s===cp||s===up||s===fp||s===hp)if(c=t.get("WEBGL_compressed_texture_astc"),c!==null){if(s===$d)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:c.COMPRESSED_RGBA_ASTC_4x4_KHR;if(s===tp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:c.COMPRESSED_RGBA_ASTC_5x4_KHR;if(s===ep)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:c.COMPRESSED_RGBA_ASTC_5x5_KHR;if(s===np)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:c.COMPRESSED_RGBA_ASTC_6x5_KHR;if(s===ip)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:c.COMPRESSED_RGBA_ASTC_6x6_KHR;if(s===ap)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:c.COMPRESSED_RGBA_ASTC_8x5_KHR;if(s===sp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:c.COMPRESSED_RGBA_ASTC_8x6_KHR;if(s===rp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:c.COMPRESSED_RGBA_ASTC_8x8_KHR;if(s===op)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:c.COMPRESSED_RGBA_ASTC_10x5_KHR;if(s===lp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:c.COMPRESSED_RGBA_ASTC_10x6_KHR;if(s===cp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:c.COMPRESSED_RGBA_ASTC_10x8_KHR;if(s===up)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:c.COMPRESSED_RGBA_ASTC_10x10_KHR;if(s===fp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:c.COMPRESSED_RGBA_ASTC_12x10_KHR;if(s===hp)return f===ke?c.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:c.COMPRESSED_RGBA_ASTC_12x12_KHR}else return null;if(s===dp||s===pp||s===mp)if(c=t.get("EXT_texture_compression_bptc"),c!==null){if(s===dp)return f===ke?c.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT:c.COMPRESSED_RGBA_BPTC_UNORM_EXT;if(s===pp)return c.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT;if(s===mp)return c.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT}else return null;if(s===gp||s===_p||s===Su||s===vp)if(c=t.get("EXT_texture_compression_rgtc"),c!==null){if(s===gp)return c.COMPRESSED_RED_RGTC1_EXT;if(s===_p)return c.COMPRESSED_SIGNED_RED_RGTC1_EXT;if(s===Su)return c.COMPRESSED_RED_GREEN_RGTC2_EXT;if(s===vp)return c.COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT}else return null;return s===yl?r.UNSIGNED_INT_24_8:r[s]!==void 0?r[s]:null}return{convert:i}}const hR=`
void main() {

	gl_Position = vec4( position, 1.0 );

}`,dR=`
uniform sampler2DArray depthColor;
uniform float depthWidth;
uniform float depthHeight;

void main() {

	vec2 coord = vec2( gl_FragCoord.x / depthWidth, gl_FragCoord.y / depthHeight );

	if ( coord.x >= 1.0 ) {

		gl_FragDepth = texture( depthColor, vec3( coord.x - 1.0, coord.y, 1 ) ).r;

	} else {

		gl_FragDepth = texture( depthColor, vec3( coord.x, coord.y, 0 ) ).r;

	}

}`;class pR{constructor(){this.texture=null,this.mesh=null,this.depthNear=0,this.depthFar=0}init(t,i){if(this.texture===null){const s=new Vx(t.texture);(t.depthNear!==i.depthNear||t.depthFar!==i.depthFar)&&(this.depthNear=t.depthNear,this.depthFar=t.depthFar),this.texture=s}}getMesh(t){if(this.texture!==null&&this.mesh===null){const i=t.cameras[0].viewport,s=new $i({vertexShader:hR,fragmentShader:dR,uniforms:{depthColor:{value:this.texture},depthWidth:{value:i.z},depthHeight:{value:i.w}}});this.mesh=new ln(new gs(20,20),s)}return this.mesh}reset(){this.texture=null,this.mesh=null}getDepthTexture(){return this.texture}}class mR extends Ks{constructor(t,i){super();const s=this;let l=null,c=1,f=null,p="local-floor",m=1,d=null,x=null,v=null,g=null,S=null,b=null;const w=typeof XRWebGLBinding<"u",E=new pR,_={},z=i.getContextAttributes();let F=null,C=null;const B=[],O=[],U=new oe;let T=null;const P=new mi;P.viewport=new cn;const $=new mi;$.viewport=new cn;const Y=[P,$],J=new Eb;let dt=null,gt=null;this.cameraAutoUpdate=!0,this.enabled=!1,this.isPresenting=!1,this.getController=function(nt){let vt=B[nt];return vt===void 0&&(vt=new td,B[nt]=vt),vt.getTargetRaySpace()},this.getControllerGrip=function(nt){let vt=B[nt];return vt===void 0&&(vt=new td,B[nt]=vt),vt.getGripSpace()},this.getHand=function(nt){let vt=B[nt];return vt===void 0&&(vt=new td,B[nt]=vt),vt.getHandSpace()};function et(nt){const vt=O.indexOf(nt.inputSource);if(vt===-1)return;const St=B[vt];St!==void 0&&(St.update(nt.inputSource,nt.frame,d||f),St.dispatchEvent({type:nt.type,data:nt.inputSource}))}function I(){l.removeEventListener("select",et),l.removeEventListener("selectstart",et),l.removeEventListener("selectend",et),l.removeEventListener("squeeze",et),l.removeEventListener("squeezestart",et),l.removeEventListener("squeezeend",et),l.removeEventListener("end",I),l.removeEventListener("inputsourceschange",H);for(let nt=0;nt<B.length;nt++){const vt=O[nt];vt!==null&&(O[nt]=null,B[nt].disconnect(vt))}dt=null,gt=null,E.reset();for(const nt in _)delete _[nt];t.setRenderTarget(F),S=null,g=null,v=null,l=null,C=null,wt.stop(),s.isPresenting=!1,t.setPixelRatio(T),t.setSize(U.width,U.height,!1),s.dispatchEvent({type:"sessionend"})}this.setFramebufferScaleFactor=function(nt){c=nt,s.isPresenting===!0&&se("WebXRManager: Cannot change framebuffer scale while presenting.")},this.setReferenceSpaceType=function(nt){p=nt,s.isPresenting===!0&&se("WebXRManager: Cannot change reference space type while presenting.")},this.getReferenceSpace=function(){return d||f},this.setReferenceSpace=function(nt){d=nt},this.getBaseLayer=function(){return g!==null?g:S},this.getBinding=function(){return v===null&&w&&(v=new XRWebGLBinding(l,i)),v},this.getFrame=function(){return b},this.getSession=function(){return l},this.setSession=async function(nt){if(l=nt,l!==null){if(F=t.getRenderTarget(),l.addEventListener("select",et),l.addEventListener("selectstart",et),l.addEventListener("selectend",et),l.addEventListener("squeeze",et),l.addEventListener("squeezestart",et),l.addEventListener("squeezeend",et),l.addEventListener("end",I),l.addEventListener("inputsourceschange",H),z.xrCompatible!==!0&&await i.makeXRCompatible(),T=t.getPixelRatio(),t.getSize(U),w&&"createProjectionLayer"in XRWebGLBinding.prototype){let St=null,Ut=null,Jt=null;z.depth&&(Jt=z.stencil?i.DEPTH24_STENCIL8:i.DEPTH_COMPONENT24,St=z.stencil?Ws:Da,Ut=z.stencil?yl:Ji);const Kt={colorFormat:i.RGBA8,depthFormat:Jt,scaleFactor:c};v=this.getBinding(),g=v.createProjectionLayer(Kt),l.updateRenderState({layers:[g]}),t.setPixelRatio(1),t.setSize(g.textureWidth,g.textureHeight,!1),C=new Qi(g.textureWidth,g.textureHeight,{format:Fi,type:gi,depthTexture:new ao(g.textureWidth,g.textureHeight,Ut,void 0,void 0,void 0,void 0,void 0,void 0,St),stencilBuffer:z.stencil,colorSpace:t.outputColorSpace,samples:z.antialias?4:0,resolveDepthBuffer:g.ignoreDepthValues===!1,resolveStencilBuffer:g.ignoreDepthValues===!1})}else{const St={antialias:z.antialias,alpha:!0,depth:z.depth,stencil:z.stencil,framebufferScaleFactor:c};S=new XRWebGLLayer(l,i,St),l.updateRenderState({baseLayer:S}),t.setPixelRatio(1),t.setSize(S.framebufferWidth,S.framebufferHeight,!1),C=new Qi(S.framebufferWidth,S.framebufferHeight,{format:Fi,type:gi,colorSpace:t.outputColorSpace,stencilBuffer:z.stencil,resolveDepthBuffer:S.ignoreDepthValues===!1,resolveStencilBuffer:S.ignoreDepthValues===!1})}C.isXRRenderTarget=!0,this.setFoveation(m),d=null,f=await l.requestReferenceSpace(p),wt.setContext(l),wt.start(),s.isPresenting=!0,s.dispatchEvent({type:"sessionstart"})}},this.getEnvironmentBlendMode=function(){if(l!==null)return l.environmentBlendMode},this.getDepthTexture=function(){return E.getDepthTexture()};function H(nt){for(let vt=0;vt<nt.removed.length;vt++){const St=nt.removed[vt],Ut=O.indexOf(St);Ut>=0&&(O[Ut]=null,B[Ut].disconnect(St))}for(let vt=0;vt<nt.added.length;vt++){const St=nt.added[vt];let Ut=O.indexOf(St);if(Ut===-1){for(let Kt=0;Kt<B.length;Kt++)if(Kt>=O.length){O.push(St),Ut=Kt;break}else if(O[Kt]===null){O[Kt]=St,Ut=Kt;break}if(Ut===-1)break}const Jt=B[Ut];Jt&&Jt.connect(St)}}const q=new k,ht=new k;function Et(nt,vt,St){q.setFromMatrixPosition(vt.matrixWorld),ht.setFromMatrixPosition(St.matrixWorld);const Ut=q.distanceTo(ht),Jt=vt.projectionMatrix.elements,Kt=St.projectionMatrix.elements,Ce=Jt[14]/(Jt[10]-1),le=Jt[14]/(Jt[10]+1),_e=(Jt[9]+1)/Jt[5],ve=(Jt[9]-1)/Jt[5],de=(Jt[8]-1)/Jt[0],Ke=(Kt[8]+1)/Kt[0],Qe=Ce*de,Je=Ce*Ke,nn=Ut/(-de+Ke),ze=nn*-de;if(vt.matrixWorld.decompose(nt.position,nt.quaternion,nt.scale),nt.translateX(ze),nt.translateZ(nn),nt.matrixWorld.compose(nt.position,nt.quaternion,nt.scale),nt.matrixWorldInverse.copy(nt.matrixWorld).invert(),Jt[10]===-1)nt.projectionMatrix.copy(vt.projectionMatrix),nt.projectionMatrixInverse.copy(vt.projectionMatrixInverse);else{const $e=Ce+nn,K=le+nn,Le=Qe-ze,Lt=Je+(Ut-ze),D=_e*le/K*$e,M=ve*le/K*$e;nt.projectionMatrix.makePerspective(Le,Lt,D,M,$e,K),nt.projectionMatrixInverse.copy(nt.projectionMatrix).invert()}}function L(nt,vt){vt===null?nt.matrixWorld.copy(nt.matrix):nt.matrixWorld.multiplyMatrices(vt.matrixWorld,nt.matrix),nt.matrixWorldInverse.copy(nt.matrixWorld).invert()}this.updateCamera=function(nt){if(l===null)return;let vt=nt.near,St=nt.far;E.texture!==null&&(E.depthNear>0&&(vt=E.depthNear),E.depthFar>0&&(St=E.depthFar)),J.near=$.near=P.near=vt,J.far=$.far=P.far=St,(dt!==J.near||gt!==J.far)&&(l.updateRenderState({depthNear:J.near,depthFar:J.far}),dt=J.near,gt=J.far),J.layers.mask=nt.layers.mask|6,P.layers.mask=J.layers.mask&-5,$.layers.mask=J.layers.mask&-3;const Ut=nt.parent,Jt=J.cameras;L(J,Ut);for(let Kt=0;Kt<Jt.length;Kt++)L(Jt[Kt],Ut);Jt.length===2?Et(J,P,$):J.projectionMatrix.copy(P.projectionMatrix),W(nt,J,Ut)};function W(nt,vt,St){St===null?nt.matrix.copy(vt.matrixWorld):(nt.matrix.copy(St.matrixWorld),nt.matrix.invert(),nt.matrix.multiply(vt.matrixWorld)),nt.matrix.decompose(nt.position,nt.quaternion,nt.scale),nt.updateMatrixWorld(!0),nt.projectionMatrix.copy(vt.projectionMatrix),nt.projectionMatrixInverse.copy(vt.projectionMatrixInverse),nt.isPerspectiveCamera&&(nt.fov=Ml*2*Math.atan(1/nt.projectionMatrix.elements[5]),nt.zoom=1)}this.getCamera=function(){return J},this.getFoveation=function(){if(!(g===null&&S===null))return m},this.setFoveation=function(nt){m=nt,g!==null&&(g.fixedFoveation=nt),S!==null&&S.fixedFoveation!==void 0&&(S.fixedFoveation=nt)},this.hasDepthSensing=function(){return E.texture!==null},this.getDepthSensingMesh=function(){return E.getMesh(J)},this.getCameraTexture=function(nt){return _[nt]};let yt=null;function At(nt,vt){if(x=vt.getViewerPose(d||f),b=vt,x!==null){const St=x.views;S!==null&&(t.setRenderTargetFramebuffer(C,S.framebuffer),t.setRenderTarget(C));let Ut=!1;St.length!==J.cameras.length&&(J.cameras.length=0,Ut=!0);for(let le=0;le<St.length;le++){const _e=St[le];let ve=null;if(S!==null)ve=S.getViewport(_e);else{const Ke=v.getViewSubImage(g,_e);ve=Ke.viewport,le===0&&(t.setRenderTargetTextures(C,Ke.colorTexture,Ke.depthStencilTexture),t.setRenderTarget(C))}let de=Y[le];de===void 0&&(de=new mi,de.layers.enable(le),de.viewport=new cn,Y[le]=de),de.matrix.fromArray(_e.transform.matrix),de.matrix.decompose(de.position,de.quaternion,de.scale),de.projectionMatrix.fromArray(_e.projectionMatrix),de.projectionMatrixInverse.copy(de.projectionMatrix).invert(),de.viewport.set(ve.x,ve.y,ve.width,ve.height),le===0&&(J.matrix.copy(de.matrix),J.matrix.decompose(J.position,J.quaternion,J.scale)),Ut===!0&&J.cameras.push(de)}const Jt=l.enabledFeatures;if(Jt&&Jt.includes("depth-sensing")&&l.depthUsage=="gpu-optimized"&&w){v=s.getBinding();const le=v.getDepthInformation(St[0]);le&&le.isValid&&le.texture&&E.init(le,l.renderState)}if(Jt&&Jt.includes("camera-access")&&w){t.state.unbindTexture(),v=s.getBinding();for(let le=0;le<St.length;le++){const _e=St[le].camera;if(_e){let ve=_[_e];ve||(ve=new Vx,_[_e]=ve);const de=v.getCameraImage(_e);ve.sourceTexture=de}}}}for(let St=0;St<B.length;St++){const Ut=O[St],Jt=B[St];Ut!==null&&Jt!==void 0&&Jt.update(Ut,vt,d||f)}yt&&yt(nt,vt),vt.detectedPlanes&&s.dispatchEvent({type:"planesdetected",data:vt}),b=null}const wt=new Yx;wt.setAnimationLoop(At),this.setAnimationLoop=function(nt){yt=nt},this.dispose=function(){}}}const gR=new we,ty=new ue;ty.set(-1,0,0,0,1,0,0,0,1);function _R(r,t){function i(E,_){E.matrixAutoUpdate===!0&&E.updateMatrix(),_.value.copy(E.matrix)}function s(E,_){_.color.getRGB(E.fogColor.value,kx(r)),_.isFog?(E.fogNear.value=_.near,E.fogFar.value=_.far):_.isFogExp2&&(E.fogDensity.value=_.density)}function l(E,_,z,F,C){_.isNodeMaterial?_.uniformsNeedUpdate=!1:_.isMeshBasicMaterial?c(E,_):_.isMeshLambertMaterial?(c(E,_),_.envMap&&(E.envMapIntensity.value=_.envMapIntensity)):_.isMeshToonMaterial?(c(E,_),v(E,_)):_.isMeshPhongMaterial?(c(E,_),x(E,_),_.envMap&&(E.envMapIntensity.value=_.envMapIntensity)):_.isMeshStandardMaterial?(c(E,_),g(E,_),_.isMeshPhysicalMaterial&&S(E,_,C)):_.isMeshMatcapMaterial?(c(E,_),b(E,_)):_.isMeshDepthMaterial?c(E,_):_.isMeshDistanceMaterial?(c(E,_),w(E,_)):_.isMeshNormalMaterial?c(E,_):_.isLineBasicMaterial?(f(E,_),_.isLineDashedMaterial&&p(E,_)):_.isPointsMaterial?m(E,_,z,F):_.isSpriteMaterial?d(E,_):_.isShadowMaterial?(E.color.value.copy(_.color),E.opacity.value=_.opacity):_.isShaderMaterial&&(_.uniformsNeedUpdate=!1)}function c(E,_){E.opacity.value=_.opacity,_.color&&E.diffuse.value.copy(_.color),_.emissive&&E.emissive.value.copy(_.emissive).multiplyScalar(_.emissiveIntensity),_.map&&(E.map.value=_.map,i(_.map,E.mapTransform)),_.alphaMap&&(E.alphaMap.value=_.alphaMap,i(_.alphaMap,E.alphaMapTransform)),_.bumpMap&&(E.bumpMap.value=_.bumpMap,i(_.bumpMap,E.bumpMapTransform),E.bumpScale.value=_.bumpScale,_.side===ti&&(E.bumpScale.value*=-1)),_.normalMap&&(E.normalMap.value=_.normalMap,i(_.normalMap,E.normalMapTransform),E.normalScale.value.copy(_.normalScale),_.side===ti&&E.normalScale.value.negate()),_.displacementMap&&(E.displacementMap.value=_.displacementMap,i(_.displacementMap,E.displacementMapTransform),E.displacementScale.value=_.displacementScale,E.displacementBias.value=_.displacementBias),_.emissiveMap&&(E.emissiveMap.value=_.emissiveMap,i(_.emissiveMap,E.emissiveMapTransform)),_.specularMap&&(E.specularMap.value=_.specularMap,i(_.specularMap,E.specularMapTransform)),_.alphaTest>0&&(E.alphaTest.value=_.alphaTest);const z=t.get(_),F=z.envMap,C=z.envMapRotation;F&&(E.envMap.value=F,E.envMapRotation.value.setFromMatrix4(gR.makeRotationFromEuler(C)).transpose(),F.isCubeTexture&&F.isRenderTargetTexture===!1&&E.envMapRotation.value.premultiply(ty),E.reflectivity.value=_.reflectivity,E.ior.value=_.ior,E.refractionRatio.value=_.refractionRatio),_.lightMap&&(E.lightMap.value=_.lightMap,E.lightMapIntensity.value=_.lightMapIntensity,i(_.lightMap,E.lightMapTransform)),_.aoMap&&(E.aoMap.value=_.aoMap,E.aoMapIntensity.value=_.aoMapIntensity,i(_.aoMap,E.aoMapTransform))}function f(E,_){E.diffuse.value.copy(_.color),E.opacity.value=_.opacity,_.map&&(E.map.value=_.map,i(_.map,E.mapTransform))}function p(E,_){E.dashSize.value=_.dashSize,E.totalSize.value=_.dashSize+_.gapSize,E.scale.value=_.scale}function m(E,_,z,F){E.diffuse.value.copy(_.color),E.opacity.value=_.opacity,E.size.value=_.size*z,E.scale.value=F*.5,_.map&&(E.map.value=_.map,i(_.map,E.uvTransform)),_.alphaMap&&(E.alphaMap.value=_.alphaMap,i(_.alphaMap,E.alphaMapTransform)),_.alphaTest>0&&(E.alphaTest.value=_.alphaTest)}function d(E,_){E.diffuse.value.copy(_.color),E.opacity.value=_.opacity,E.rotation.value=_.rotation,_.map&&(E.map.value=_.map,i(_.map,E.mapTransform)),_.alphaMap&&(E.alphaMap.value=_.alphaMap,i(_.alphaMap,E.alphaMapTransform)),_.alphaTest>0&&(E.alphaTest.value=_.alphaTest)}function x(E,_){E.specular.value.copy(_.specular),E.shininess.value=Math.max(_.shininess,1e-4)}function v(E,_){_.gradientMap&&(E.gradientMap.value=_.gradientMap)}function g(E,_){E.metalness.value=_.metalness,_.metalnessMap&&(E.metalnessMap.value=_.metalnessMap,i(_.metalnessMap,E.metalnessMapTransform)),E.roughness.value=_.roughness,_.roughnessMap&&(E.roughnessMap.value=_.roughnessMap,i(_.roughnessMap,E.roughnessMapTransform)),_.envMap&&(E.envMapIntensity.value=_.envMapIntensity)}function S(E,_,z){E.ior.value=_.ior,_.sheen>0&&(E.sheenColor.value.copy(_.sheenColor).multiplyScalar(_.sheen),E.sheenRoughness.value=_.sheenRoughness,_.sheenColorMap&&(E.sheenColorMap.value=_.sheenColorMap,i(_.sheenColorMap,E.sheenColorMapTransform)),_.sheenRoughnessMap&&(E.sheenRoughnessMap.value=_.sheenRoughnessMap,i(_.sheenRoughnessMap,E.sheenRoughnessMapTransform))),_.clearcoat>0&&(E.clearcoat.value=_.clearcoat,E.clearcoatRoughness.value=_.clearcoatRoughness,_.clearcoatMap&&(E.clearcoatMap.value=_.clearcoatMap,i(_.clearcoatMap,E.clearcoatMapTransform)),_.clearcoatRoughnessMap&&(E.clearcoatRoughnessMap.value=_.clearcoatRoughnessMap,i(_.clearcoatRoughnessMap,E.clearcoatRoughnessMapTransform)),_.clearcoatNormalMap&&(E.clearcoatNormalMap.value=_.clearcoatNormalMap,i(_.clearcoatNormalMap,E.clearcoatNormalMapTransform),E.clearcoatNormalScale.value.copy(_.clearcoatNormalScale),_.side===ti&&E.clearcoatNormalScale.value.negate())),_.dispersion>0&&(E.dispersion.value=_.dispersion),_.iridescence>0&&(E.iridescence.value=_.iridescence,E.iridescenceIOR.value=_.iridescenceIOR,E.iridescenceThicknessMinimum.value=_.iridescenceThicknessRange[0],E.iridescenceThicknessMaximum.value=_.iridescenceThicknessRange[1],_.iridescenceMap&&(E.iridescenceMap.value=_.iridescenceMap,i(_.iridescenceMap,E.iridescenceMapTransform)),_.iridescenceThicknessMap&&(E.iridescenceThicknessMap.value=_.iridescenceThicknessMap,i(_.iridescenceThicknessMap,E.iridescenceThicknessMapTransform))),_.transmission>0&&(E.transmission.value=_.transmission,E.transmissionSamplerMap.value=z.texture,E.transmissionSamplerSize.value.set(z.width,z.height),_.transmissionMap&&(E.transmissionMap.value=_.transmissionMap,i(_.transmissionMap,E.transmissionMapTransform)),E.thickness.value=_.thickness,_.thicknessMap&&(E.thicknessMap.value=_.thicknessMap,i(_.thicknessMap,E.thicknessMapTransform)),E.attenuationDistance.value=_.attenuationDistance,E.attenuationColor.value.copy(_.attenuationColor)),_.anisotropy>0&&(E.anisotropyVector.value.set(_.anisotropy*Math.cos(_.anisotropyRotation),_.anisotropy*Math.sin(_.anisotropyRotation)),_.anisotropyMap&&(E.anisotropyMap.value=_.anisotropyMap,i(_.anisotropyMap,E.anisotropyMapTransform))),E.specularIntensity.value=_.specularIntensity,E.specularColor.value.copy(_.specularColor),_.specularColorMap&&(E.specularColorMap.value=_.specularColorMap,i(_.specularColorMap,E.specularColorMapTransform)),_.specularIntensityMap&&(E.specularIntensityMap.value=_.specularIntensityMap,i(_.specularIntensityMap,E.specularIntensityMapTransform))}function b(E,_){_.matcap&&(E.matcap.value=_.matcap)}function w(E,_){const z=t.get(_).light;E.referencePosition.value.setFromMatrixPosition(z.matrixWorld),E.nearDistance.value=z.shadow.camera.near,E.farDistance.value=z.shadow.camera.far}return{refreshFogUniforms:s,refreshMaterialUniforms:l}}function vR(r,t,i,s){let l={},c={},f=[];const p=r.getParameter(r.MAX_UNIFORM_BUFFER_BINDINGS);function m(C,B){const O=B.program;s.uniformBlockBinding(C,O)}function d(C,B){let O=l[C.id];O===void 0&&(E(C),O=x(C),l[C.id]=O,C.addEventListener("dispose",z));const U=B.program;s.updateUBOMapping(C,U);const T=t.render.frame;c[C.id]!==T&&(g(C),c[C.id]=T)}function x(C){const B=v();C.__bindingPointIndex=B;const O=r.createBuffer(),U=C.__size,T=C.usage;return r.bindBuffer(r.UNIFORM_BUFFER,O),r.bufferData(r.UNIFORM_BUFFER,U,T),r.bindBuffer(r.UNIFORM_BUFFER,null),r.bindBufferBase(r.UNIFORM_BUFFER,B,O),O}function v(){for(let C=0;C<p;C++)if(f.indexOf(C)===-1)return f.push(C),C;return Ae("WebGLRenderer: Maximum number of simultaneously usable uniforms groups reached."),0}function g(C){const B=l[C.id],O=C.uniforms,U=C.__cache;r.bindBuffer(r.UNIFORM_BUFFER,B);for(let T=0,P=O.length;T<P;T++){const $=O[T];if(Array.isArray($))for(let Y=0,J=$.length;Y<J;Y++)S($[Y],T,Y,U);else S($,T,0,U)}r.bindBuffer(r.UNIFORM_BUFFER,null)}function S(C,B,O,U){if(w(C,B,O,U)===!0){const T=C.__offset,P=C.value;if(Array.isArray(P)){let $=0;for(let Y=0;Y<P.length;Y++){const J=P[Y],dt=_(J);b(J,C.__data,$),typeof J!="number"&&typeof J!="boolean"&&!J.isMatrix3&&!ArrayBuffer.isView(J)&&($+=dt.storage/Float32Array.BYTES_PER_ELEMENT)}}else b(P,C.__data,0);r.bufferSubData(r.UNIFORM_BUFFER,T,C.__data)}}function b(C,B,O){typeof C=="number"||typeof C=="boolean"?B[0]=C:C.isMatrix3?(B[0]=C.elements[0],B[1]=C.elements[1],B[2]=C.elements[2],B[3]=0,B[4]=C.elements[3],B[5]=C.elements[4],B[6]=C.elements[5],B[7]=0,B[8]=C.elements[6],B[9]=C.elements[7],B[10]=C.elements[8],B[11]=0):ArrayBuffer.isView(C)?B.set(new C.constructor(C.buffer,C.byteOffset,B.length)):C.toArray(B,O)}function w(C,B,O,U){const T=C.value,P=B+"_"+O;if(U[P]===void 0)return typeof T=="number"||typeof T=="boolean"?U[P]=T:ArrayBuffer.isView(T)?U[P]=T.slice():U[P]=T.clone(),!0;{const $=U[P];if(typeof T=="number"||typeof T=="boolean"){if($!==T)return U[P]=T,!0}else{if(ArrayBuffer.isView(T))return!0;if($.equals(T)===!1)return $.copy(T),!0}}return!1}function E(C){const B=C.uniforms;let O=0;const U=16;for(let P=0,$=B.length;P<$;P++){const Y=Array.isArray(B[P])?B[P]:[B[P]];for(let J=0,dt=Y.length;J<dt;J++){const gt=Y[J],et=Array.isArray(gt.value)?gt.value:[gt.value];for(let I=0,H=et.length;I<H;I++){const q=et[I],ht=_(q),Et=O%U,L=Et%ht.boundary,W=Et+L;O+=L,W!==0&&U-W<ht.storage&&(O+=U-W),gt.__data=new Float32Array(ht.storage/Float32Array.BYTES_PER_ELEMENT),gt.__offset=O,O+=ht.storage}}}const T=O%U;return T>0&&(O+=U-T),C.__size=O,C.__cache={},this}function _(C){const B={boundary:0,storage:0};return typeof C=="number"||typeof C=="boolean"?(B.boundary=4,B.storage=4):C.isVector2?(B.boundary=8,B.storage=8):C.isVector3||C.isColor?(B.boundary=16,B.storage=12):C.isVector4?(B.boundary=16,B.storage=16):C.isMatrix3?(B.boundary=48,B.storage=48):C.isMatrix4?(B.boundary=64,B.storage=64):C.isTexture?se("WebGLRenderer: Texture samplers can not be part of an uniforms group."):ArrayBuffer.isView(C)?(B.boundary=16,B.storage=C.byteLength):se("WebGLRenderer: Unsupported uniform value type.",C),B}function z(C){const B=C.target;B.removeEventListener("dispose",z);const O=f.indexOf(B.__bindingPointIndex);f.splice(O,1),r.deleteBuffer(l[B.id]),delete l[B.id],delete c[B.id]}function F(){for(const C in l)r.deleteBuffer(l[C]);f=[],l={},c={}}return{bind:m,update:d,dispose:F}}const xR=new Uint16Array([12469,15057,12620,14925,13266,14620,13807,14376,14323,13990,14545,13625,14713,13328,14840,12882,14931,12528,14996,12233,15039,11829,15066,11525,15080,11295,15085,10976,15082,10705,15073,10495,13880,14564,13898,14542,13977,14430,14158,14124,14393,13732,14556,13410,14702,12996,14814,12596,14891,12291,14937,11834,14957,11489,14958,11194,14943,10803,14921,10506,14893,10278,14858,9960,14484,14039,14487,14025,14499,13941,14524,13740,14574,13468,14654,13106,14743,12678,14818,12344,14867,11893,14889,11509,14893,11180,14881,10751,14852,10428,14812,10128,14765,9754,14712,9466,14764,13480,14764,13475,14766,13440,14766,13347,14769,13070,14786,12713,14816,12387,14844,11957,14860,11549,14868,11215,14855,10751,14825,10403,14782,10044,14729,9651,14666,9352,14599,9029,14967,12835,14966,12831,14963,12804,14954,12723,14936,12564,14917,12347,14900,11958,14886,11569,14878,11247,14859,10765,14828,10401,14784,10011,14727,9600,14660,9289,14586,8893,14508,8533,15111,12234,15110,12234,15104,12216,15092,12156,15067,12010,15028,11776,14981,11500,14942,11205,14902,10752,14861,10393,14812,9991,14752,9570,14682,9252,14603,8808,14519,8445,14431,8145,15209,11449,15208,11451,15202,11451,15190,11438,15163,11384,15117,11274,15055,10979,14994,10648,14932,10343,14871,9936,14803,9532,14729,9218,14645,8742,14556,8381,14461,8020,14365,7603,15273,10603,15272,10607,15267,10619,15256,10631,15231,10614,15182,10535,15118,10389,15042,10167,14963,9787,14883,9447,14800,9115,14710,8665,14615,8318,14514,7911,14411,7507,14279,7198,15314,9675,15313,9683,15309,9712,15298,9759,15277,9797,15229,9773,15166,9668,15084,9487,14995,9274,14898,8910,14800,8539,14697,8234,14590,7790,14479,7409,14367,7067,14178,6621,15337,8619,15337,8631,15333,8677,15325,8769,15305,8871,15264,8940,15202,8909,15119,8775,15022,8565,14916,8328,14804,8009,14688,7614,14569,7287,14448,6888,14321,6483,14088,6171,15350,7402,15350,7419,15347,7480,15340,7613,15322,7804,15287,7973,15229,8057,15148,8012,15046,7846,14933,7611,14810,7357,14682,7069,14552,6656,14421,6316,14251,5948,14007,5528,15356,5942,15356,5977,15353,6119,15348,6294,15332,6551,15302,6824,15249,7044,15171,7122,15070,7050,14949,6861,14818,6611,14679,6349,14538,6067,14398,5651,14189,5311,13935,4958,15359,4123,15359,4153,15356,4296,15353,4646,15338,5160,15311,5508,15263,5829,15188,6042,15088,6094,14966,6001,14826,5796,14678,5543,14527,5287,14377,4985,14133,4586,13869,4257,15360,1563,15360,1642,15358,2076,15354,2636,15341,3350,15317,4019,15273,4429,15203,4732,15105,4911,14981,4932,14836,4818,14679,4621,14517,4386,14359,4156,14083,3795,13808,3437,15360,122,15360,137,15358,285,15355,636,15344,1274,15322,2177,15281,2765,15215,3223,15120,3451,14995,3569,14846,3567,14681,3466,14511,3305,14344,3121,14037,2800,13753,2467,15360,0,15360,1,15359,21,15355,89,15346,253,15325,479,15287,796,15225,1148,15133,1492,15008,1749,14856,1882,14685,1886,14506,1783,14324,1608,13996,1398,13702,1183]);let Yi=null;function yR(){return Yi===null&&(Yi=new Bx(xR,16,16,Zs,Ca),Yi.name="DFG_LUT",Yi.minFilter=Gn,Yi.magFilter=Gn,Yi.wrapS=Ta,Yi.wrapT=Ta,Yi.generateMipmaps=!1,Yi.needsUpdate=!0),Yi}class SR{constructor(t={}){const{canvas:i=mE(),context:s=null,depth:l=!0,stencil:c=!1,alpha:f=!1,antialias:p=!1,premultipliedAlpha:m=!0,preserveDrawingBuffer:d=!1,powerPreference:x="default",failIfMajorPerformanceCaveat:v=!1,reversedDepthBuffer:g=!1,outputBufferType:S=gi}=t;this.isWebGLRenderer=!0;let b;if(s!==null){if(typeof WebGLRenderingContext<"u"&&s instanceof WebGLRenderingContext)throw new Error("THREE.WebGLRenderer: WebGL 1 is not supported since r163.");b=s.getContextAttributes().alpha}else b=f;const w=S,E=new Set([Bp,zp,Pp]),_=new Set([gi,Ji,xl,yl,Np,Lp]),z=new Uint32Array(4),F=new Int32Array(4),C=new k;let B=null,O=null;const U=[],T=[];let P=null;this.domElement=i,this.debug={checkShaderErrors:!0,onShaderError:null},this.autoClear=!0,this.autoClearColor=!0,this.autoClearDepth=!0,this.autoClearStencil=!0,this.sortObjects=!0,this.clippingPlanes=[],this.localClippingEnabled=!1,this.toneMapping=Ki,this.toneMappingExposure=1,this.transmissionResolutionScale=1;const $=this;let Y=!1,J=null,dt=null,gt=null,et=null;this._outputColorSpace=qn;let I=0,H=0,q=null,ht=-1,Et=null;const L=new cn,W=new cn;let yt=null;const At=new ie(0);let wt=0,nt=i.width,vt=i.height,St=1,Ut=null,Jt=null;const Kt=new cn(0,0,nt,vt),Ce=new cn(0,0,nt,vt);let le=!1;const _e=new Wp;let ve=!1,de=!1;const Ke=new we,Qe=new k,Je=new cn,nn={background:null,fog:null,environment:null,overrideMaterial:null,isScene:!0};let ze=!1;function $e(){return q===null?St:1}let K=s;function Le(A,Q){return i.getContext(A,Q)}try{const A={alpha:!0,depth:l,stencil:c,antialias:p,premultipliedAlpha:m,preserveDrawingBuffer:d,powerPreference:x,failIfMajorPerformanceCaveat:v};if("setAttribute"in i&&i.setAttribute("data-engine",`three.js r${Cp}`),i.addEventListener("webglcontextlost",an,!1),i.addEventListener("webglcontextrestored",Be,!1),i.addEventListener("webglcontextcreationerror",ei,!1),K===null){const Q="webgl2";if(K=Le(Q,A),K===null)throw Le(Q)?new Error("THREE.WebGLRenderer: Error creating WebGL context with your selected attributes."):new Error("THREE.WebGLRenderer: Error creating WebGL context.")}}catch(A){throw Ae("WebGLRenderer: "+A.message),A}let Lt,D,M,X,V,ct,bt,Ct,ut,pt,Rt,Ft,Ot,Pt,$t,te,re,Z,Dt,_t,Nt,Ht,Tt;function jt(){Lt=new y2(K),Lt.init(),Nt=new fR(K,Lt),D=new h2(K,Lt,t,Nt),M=new cR(K,Lt),D.reversedDepthBuffer&&g&&M.buffers.depth.setReversed(!0),dt=K.createFramebuffer(),gt=K.createFramebuffer(),et=K.createFramebuffer(),X=new E2(K),V=new Z3,ct=new uR(K,Lt,M,V,D,Nt,X),bt=new x2($),Ct=new Rb(K),Ht=new u2(K,Ct),ut=new S2(K,Ct,X,Ht),pt=new T2(K,ut,Ct,Ht,X),Z=new b2(K,D,ct),$t=new d2(V),Rt=new j3($,bt,Lt,D,Ht,$t),Ft=new _R($,V),Ot=new Q3,Pt=new iR(Lt),re=new c2($,bt,M,pt,b,m),te=new lR($,pt,D),Tt=new vR(K,X,D,M),Dt=new f2(K,Lt,X),_t=new M2(K,Lt,X),X.programs=Rt.programs,$.capabilities=D,$.extensions=Lt,$.properties=V,$.renderLists=Ot,$.shadowMap=te,$.state=M,$.info=X}jt(),w!==gi&&(P=new R2(w,i.width,i.height,p,l,c));const kt=new mR($,K);this.xr=kt,this.getContext=function(){return K},this.getContextAttributes=function(){return K.getContextAttributes()},this.forceContextLoss=function(){const A=Lt.get("WEBGL_lose_context");A&&A.loseContext()},this.forceContextRestore=function(){const A=Lt.get("WEBGL_lose_context");A&&A.restoreContext()},this.getPixelRatio=function(){return St},this.setPixelRatio=function(A){A!==void 0&&(St=A,this.setSize(nt,vt,!1))},this.getSize=function(A){return A.set(nt,vt)},this.setSize=function(A,Q,ot=!0){if(kt.isPresenting){se("WebGLRenderer: Can't change size while VR device is presenting.");return}nt=A,vt=Q,i.width=Math.floor(A*St),i.height=Math.floor(Q*St),ot===!0&&(i.style.width=A+"px",i.style.height=Q+"px"),P!==null&&P.setSize(i.width,i.height),this.setViewport(0,0,A,Q)},this.getDrawingBufferSize=function(A){return A.set(nt*St,vt*St).floor()},this.setDrawingBufferSize=function(A,Q,ot){nt=A,vt=Q,St=ot,i.width=Math.floor(A*ot),i.height=Math.floor(Q*ot),this.setViewport(0,0,A,Q)},this.setEffects=function(A){if(w===gi){Ae("WebGLRenderer: setEffects() requires outputBufferType set to HalfFloatType or FloatType.");return}if(A){for(let Q=0;Q<A.length;Q++)if(A[Q].isOutputPass===!0){se("WebGLRenderer: OutputPass is not needed in setEffects(). Tone mapping and color space conversion are applied automatically.");break}}P.setEffects(A||[])},this.getCurrentViewport=function(A){return A.copy(L)},this.getViewport=function(A){return A.copy(Kt)},this.setViewport=function(A,Q,ot,st){A.isVector4?Kt.set(A.x,A.y,A.z,A.w):Kt.set(A,Q,ot,st),M.viewport(L.copy(Kt).multiplyScalar(St).round())},this.getScissor=function(A){return A.copy(Ce)},this.setScissor=function(A,Q,ot,st){A.isVector4?Ce.set(A.x,A.y,A.z,A.w):Ce.set(A,Q,ot,st),M.scissor(W.copy(Ce).multiplyScalar(St).round())},this.getScissorTest=function(){return le},this.setScissorTest=function(A){M.setScissorTest(le=A)},this.setOpaqueSort=function(A){Ut=A},this.setTransparentSort=function(A){Jt=A},this.getClearColor=function(A){return A.copy(re.getClearColor())},this.setClearColor=function(){re.setClearColor(...arguments)},this.getClearAlpha=function(){return re.getClearAlpha()},this.setClearAlpha=function(){re.setClearAlpha(...arguments)},this.clear=function(A=!0,Q=!0,ot=!0){let st=0;if(A){let rt=!1;if(q!==null){const Bt=q.texture.format;rt=E.has(Bt)}if(rt){const Bt=q.texture.type,Vt=_.has(Bt),zt=re.getClearColor(),Wt=re.getClearAlpha(),Xt=zt.r,ee=zt.g,fe=zt.b;Vt?(z[0]=Xt,z[1]=ee,z[2]=fe,z[3]=Wt,K.clearBufferuiv(K.COLOR,0,z)):(F[0]=Xt,F[1]=ee,F[2]=fe,F[3]=Wt,K.clearBufferiv(K.COLOR,0,F))}else st|=K.COLOR_BUFFER_BIT}Q&&(st|=K.DEPTH_BUFFER_BIT,this.state.buffers.depth.setMask(!0)),ot&&(st|=K.STENCIL_BUFFER_BIT,this.state.buffers.stencil.setMask(4294967295)),st!==0&&K.clear(st)},this.clearColor=function(){this.clear(!0,!1,!1)},this.clearDepth=function(){this.clear(!1,!0,!1)},this.clearStencil=function(){this.clear(!1,!1,!0)},this.setNodesHandler=function(A){A.setRenderer(this),J=A},this.dispose=function(){i.removeEventListener("webglcontextlost",an,!1),i.removeEventListener("webglcontextrestored",Be,!1),i.removeEventListener("webglcontextcreationerror",ei,!1),re.dispose(),Ot.dispose(),Pt.dispose(),V.dispose(),bt.dispose(),pt.dispose(),Ht.dispose(),Tt.dispose(),Rt.dispose(),kt.dispose(),kt.removeEventListener("sessionstart",mn),kt.removeEventListener("sessionend",Un),Yn.stop()};function an(A){A.preventDefault(),Tu("WebGLRenderer: Context Lost."),Y=!0}function Be(){Tu("WebGLRenderer: Context Restored."),Y=!1;const A=X.autoReset,Q=te.enabled,ot=te.autoUpdate,st=te.needsUpdate,rt=te.type;jt(),X.autoReset=A,te.enabled=Q,te.autoUpdate=ot,te.needsUpdate=st,te.type=rt}function ei(A){Ae("WebGLRenderer: A WebGL context could not be created. Reason: ",A.statusMessage)}function ni(A){const Q=A.target;Q.removeEventListener("dispose",ni),lo(Q)}function lo(A){co(A),V.remove(A)}function co(A){const Q=V.get(A).programs;Q!==void 0&&(Q.forEach(function(ot){Rt.releaseProgram(ot)}),A.isShaderMaterial&&Rt.releaseShaderCache(A))}this.renderBufferDirect=function(A,Q,ot,st,rt,Bt){Q===null&&(Q=nn);const Vt=rt.isMesh&&rt.matrixWorld.determinantAffine()<0,zt=La(A,Q,ot,st,rt);M.setMaterial(st,Vt);let Wt=ot.index,Xt=1;if(st.wireframe===!0){if(Wt=ut.getWireframeAttribute(ot),Wt===void 0)return;Xt=2}const ee=ot.drawRange,fe=ot.attributes.position;let Qt=ee.start*Xt,De=(ee.start+ee.count)*Xt;Bt!==null&&(Qt=Math.max(Qt,Bt.start*Xt),De=Math.min(De,(Bt.start+Bt.count)*Xt)),Wt!==null?(Qt=Math.max(Qt,0),De=Math.min(De,Wt.count)):fe!=null&&(Qt=Math.max(Qt,0),De=Math.min(De,fe.count));const sn=De-Qt;if(sn<0||sn===1/0)return;Ht.setup(rt,st,zt,ot,Wt);let je,Ie=Dt;if(Wt!==null&&(je=Ct.get(Wt),Ie=_t,Ie.setIndex(je)),rt.isMesh)st.wireframe===!0?(M.setLineWidth(st.wireframeLinewidth*$e()),Ie.setMode(K.LINES)):Ie.setMode(K.TRIANGLES);else if(rt.isLine){let Fe=st.linewidth;Fe===void 0&&(Fe=1),M.setLineWidth(Fe*$e()),rt.isLineSegments?Ie.setMode(K.LINES):rt.isLineLoop?Ie.setMode(K.LINE_LOOP):Ie.setMode(K.LINE_STRIP)}else rt.isPoints?Ie.setMode(K.POINTS):rt.isSprite&&Ie.setMode(K.TRIANGLES);if(rt.isBatchedMesh)if(Lt.get("WEBGL_multi_draw"))Ie.renderMultiDraw(rt._multiDrawStarts,rt._multiDrawCounts,rt._multiDrawCount);else{const Fe=rt._multiDrawStarts,Gt=rt._multiDrawCounts,Bn=rt._multiDrawCount,xe=Wt?Ct.get(Wt).bytesPerElement:1,bn=V.get(st).currentProgram.getUniforms();for(let ii=0;ii<Bn;ii++)bn.setValue(K,"_gl_DrawID",ii),Ie.render(Fe[ii]/xe,Gt[ii])}else if(rt.isInstancedMesh)Ie.renderInstances(Qt,sn,rt.count);else if(ot.isInstancedBufferGeometry){const Fe=ot._maxInstanceCount!==void 0?ot._maxInstanceCount:1/0,Gt=Math.min(ot.instanceCount,Fe);Ie.renderInstances(Qt,sn,Gt)}else Ie.render(Qt,sn)};function uo(A,Q,ot){A.transparent===!0&&A.side===wi&&A.forceSinglePass===!1?(A.side=ti,A.needsUpdate=!0,Na(A,Q,ot),A.side=ds,A.needsUpdate=!0,Na(A,Q,ot),A.side=wi):Na(A,Q,ot)}this.compile=function(A,Q,ot=null){ot===null&&(ot=A),O=Pt.get(ot),O.init(Q),T.push(O),ot.traverseVisible(function(rt){rt.isLight&&rt.layers.test(Q.layers)&&(O.pushLight(rt),rt.castShadow&&O.pushShadow(rt))}),A!==ot&&A.traverseVisible(function(rt){rt.isLight&&rt.layers.test(Q.layers)&&(O.pushLight(rt),rt.castShadow&&O.pushShadow(rt))}),O.setupLights();const st=new Set;return A.traverse(function(rt){if(!(rt.isMesh||rt.isPoints||rt.isLine||rt.isSprite))return;const Bt=rt.material;if(Bt)if(Array.isArray(Bt))for(let Vt=0;Vt<Bt.length;Vt++){const zt=Bt[Vt];uo(zt,ot,rt),st.add(zt)}else uo(Bt,ot,rt),st.add(Bt)}),O=T.pop(),st},this.compileAsync=function(A,Q,ot=null){const st=this.compile(A,Q,ot);return new Promise(rt=>{function Bt(){if(st.forEach(function(Vt){V.get(Vt).currentProgram.isReady()&&st.delete(Vt)}),st.size===0){rt(A);return}setTimeout(Bt,10)}Lt.get("KHR_parallel_shader_compile")!==null?Bt():setTimeout(Bt,10)})};let $s=null;function Hi(A){$s&&$s(A)}function mn(){Yn.stop()}function Un(){Yn.start()}const Yn=new Yx;Yn.setAnimationLoop(Hi),typeof self<"u"&&Yn.setContext(self),this.setAnimationLoop=function(A){$s=A,kt.setAnimationLoop(A),A===null?Yn.stop():Yn.start()},kt.addEventListener("sessionstart",mn),kt.addEventListener("sessionend",Un),this.render=function(A,Q){if(Q!==void 0&&Q.isCamera!==!0){Ae("WebGLRenderer.render: camera is not an instance of THREE.Camera.");return}if(Y===!0)return;J!==null&&J.renderStart(A,Q);const ot=kt.enabled===!0&&kt.isPresenting===!0,st=P!==null&&(q===null||ot)&&P.begin($,q);if(A.matrixWorldAutoUpdate===!0&&A.updateMatrixWorld(),Q.parent===null&&Q.matrixWorldAutoUpdate===!0&&Q.updateMatrixWorld(),kt.enabled===!0&&kt.isPresenting===!0&&(P===null||P.isCompositing()===!1)&&(kt.cameraAutoUpdate===!0&&kt.updateCamera(Q),Q=kt.getCamera()),A.isScene===!0&&A.onBeforeRender($,A,Q,q),O=Pt.get(A,T.length),O.init(Q),O.state.textureUnits=ct.getTextureUnits(),T.push(O),Ke.multiplyMatrices(Q.projectionMatrix,Q.matrixWorldInverse),_e.setFromProjectionMatrix(Ke,Zi,Q.reversedDepth),de=this.localClippingEnabled,ve=$t.init(this.clippingPlanes,de),B=Ot.get(A,U.length),B.init(),U.push(B),kt.enabled===!0&&kt.isPresenting===!0){const Vt=$.xr.getDepthSensingMesh();Vt!==null&&_s(Vt,Q,-1/0,$.sortObjects)}_s(A,Q,0,$.sortObjects),B.finish(),$.sortObjects===!0&&B.sort(Ut,Jt,Q.reversedDepth),ze=kt.enabled===!1||kt.isPresenting===!1||kt.hasDepthSensing()===!1,ze&&re.addToRenderList(B,A),this.info.render.frame++,this.info.autoReset===!0&&this.info.reset(),ve===!0&&$t.beginShadows();const rt=O.state.shadowsArray;if(te.render(rt,A,Q),ve===!0&&$t.endShadows(),(st&&P.hasRenderPass())===!1){const Vt=B.opaque,zt=B.transmissive;if(O.setupLights(),Q.isArrayCamera){const Wt=Q.cameras;if(zt.length>0)for(let Xt=0,ee=Wt.length;Xt<ee;Xt++){const fe=Wt[Xt];bl(Vt,zt,A,fe)}ze&&re.render(A);for(let Xt=0,ee=Wt.length;Xt<ee;Xt++){const fe=Wt[Xt];El(B,A,fe,fe.viewport)}}else zt.length>0&&bl(Vt,zt,A,Q),ze&&re.render(A),El(B,A,Q)}q!==null&&H===0&&(ct.updateMultisampleRenderTarget(q),ct.updateRenderTargetMipmap(q)),st&&P.end($),A.isScene===!0&&A.onAfterRender($,A,Q),Ht.resetDefaultState(),ht=-1,Et=null,T.pop(),T.length>0?(O=T[T.length-1],ct.setTextureUnits(O.state.textureUnits),ve===!0&&$t.setGlobalState($.clippingPlanes,O.state.camera)):O=null,U.pop(),U.length>0?B=U[U.length-1]:B=null,J!==null&&J.renderEnd()};function _s(A,Q,ot,st){if(A.visible===!1)return;if(A.layers.test(Q.layers)){if(A.isGroup)ot=A.renderOrder;else if(A.isLOD)A.autoUpdate===!0&&A.update(Q);else if(A.isLightProbeGrid)O.pushLightProbeGrid(A);else if(A.isLight)O.pushLight(A),A.castShadow&&O.pushShadow(A);else if(A.isSprite){if(!A.frustumCulled||_e.intersectsSprite(A)){st&&Je.setFromMatrixPosition(A.matrixWorld).applyMatrix4(Ke);const Vt=pt.update(A),zt=A.material;zt.visible&&B.push(A,Vt,zt,ot,Je.z,null)}}else if((A.isMesh||A.isLine||A.isPoints)&&(!A.frustumCulled||_e.intersectsObject(A))){const Vt=pt.update(A),zt=A.material;if(st&&(A.boundingSphere!==void 0?(A.boundingSphere===null&&A.computeBoundingSphere(),Je.copy(A.boundingSphere.center)):(Vt.boundingSphere===null&&Vt.computeBoundingSphere(),Je.copy(Vt.boundingSphere.center)),Je.applyMatrix4(A.matrixWorld).applyMatrix4(Ke)),Array.isArray(zt)){const Wt=Vt.groups;for(let Xt=0,ee=Wt.length;Xt<ee;Xt++){const fe=Wt[Xt],Qt=zt[fe.materialIndex];Qt&&Qt.visible&&B.push(A,Vt,Qt,ot,Je.z,fe)}}else zt.visible&&B.push(A,Vt,zt,ot,Je.z,null)}}const Bt=A.children;for(let Vt=0,zt=Bt.length;Vt<zt;Vt++)_s(Bt[Vt],Q,ot,st)}function El(A,Q,ot,st){const{opaque:rt,transmissive:Bt,transparent:Vt}=A;O.setupLightsView(ot),ve===!0&&$t.setGlobalState($.clippingPlanes,ot),st&&M.viewport(L.copy(st)),rt.length>0&&vs(rt,Q,ot),Bt.length>0&&vs(Bt,Q,ot),Vt.length>0&&vs(Vt,Q,ot),M.buffers.depth.setTest(!0),M.buffers.depth.setMask(!0),M.buffers.color.setMask(!0),M.setPolygonOffset(!1)}function bl(A,Q,ot,st){if((ot.isScene===!0?ot.overrideMaterial:null)!==null)return;if(O.state.transmissionRenderTarget[st.id]===void 0){const Qt=Lt.has("EXT_color_buffer_half_float")||Lt.has("EXT_color_buffer_float");O.state.transmissionRenderTarget[st.id]=new Qi(1,1,{generateMipmaps:!0,type:Qt?Ca:gi,minFilter:Xs,samples:Math.max(4,D.samples),stencilBuffer:c,resolveDepthBuffer:!1,resolveStencilBuffer:!1,colorSpace:Re.workingColorSpace})}const Bt=O.state.transmissionRenderTarget[st.id],Vt=st.viewport||L;Bt.setSize(Vt.z*$.transmissionResolutionScale,Vt.w*$.transmissionResolutionScale);const zt=$.getRenderTarget(),Wt=$.getActiveCubeFace(),Xt=$.getActiveMipmapLevel();$.setRenderTarget(Bt),$.getClearColor(At),wt=$.getClearAlpha(),wt<1&&$.setClearColor(16777215,.5),$.clear(),ze&&re.render(ot);const ee=$.toneMapping;$.toneMapping=Ki;const fe=st.viewport;if(st.viewport!==void 0&&(st.viewport=void 0),O.setupLightsView(st),ve===!0&&$t.setGlobalState($.clippingPlanes,st),vs(A,ot,st),ct.updateMultisampleRenderTarget(Bt),ct.updateRenderTargetMipmap(Bt),Lt.has("WEBGL_multisampled_render_to_texture")===!1){let Qt=!1;for(let De=0,sn=Q.length;De<sn;De++){const je=Q[De],{object:Ie,geometry:Fe,material:Gt,group:Bn}=je;if(Gt.side===wi&&Ie.layers.test(st.layers)){const xe=Gt.side;Gt.side=ti,Gt.needsUpdate=!0,Ua(Ie,ot,st,Fe,Gt,Bn),Gt.side=xe,Gt.needsUpdate=!0,Qt=!0}}Qt===!0&&(ct.updateMultisampleRenderTarget(Bt),ct.updateRenderTargetMipmap(Bt))}$.setRenderTarget(zt,Wt,Xt),$.setClearColor(At,wt),fe!==void 0&&(st.viewport=fe),$.toneMapping=ee}function vs(A,Q,ot){const st=Q.isScene===!0?Q.overrideMaterial:null;for(let rt=0,Bt=A.length;rt<Bt;rt++){const Vt=A[rt],{object:zt,geometry:Wt,group:Xt}=Vt;let ee=Vt.material;ee.allowOverride===!0&&st!==null&&(ee=st),zt.layers.test(ot.layers)&&Ua(zt,Q,ot,Wt,ee,Xt)}}function Ua(A,Q,ot,st,rt,Bt){A.onBeforeRender($,Q,ot,st,rt,Bt),A.modelViewMatrix.multiplyMatrices(ot.matrixWorldInverse,A.matrixWorld),A.normalMatrix.getNormalMatrix(A.modelViewMatrix),rt.onBeforeRender($,Q,ot,st,A,Bt),rt.transparent===!0&&rt.side===wi&&rt.forceSinglePass===!1?(rt.side=ti,rt.needsUpdate=!0,$.renderBufferDirect(ot,Q,st,rt,A,Bt),rt.side=ds,rt.needsUpdate=!0,$.renderBufferDirect(ot,Q,st,rt,A,Bt),rt.side=wi):$.renderBufferDirect(ot,Q,st,rt,A,Bt),A.onAfterRender($,Q,ot,st,rt,Bt)}function Na(A,Q,ot){Q.isScene!==!0&&(Q=nn);const st=V.get(A),rt=O.state.lights,Bt=O.state.shadowsArray,Vt=rt.state.version,zt=Rt.getParameters(A,rt.state,Bt,Q,ot,O.state.lightProbeGridArray),Wt=Rt.getProgramCacheKey(zt);let Xt=st.programs;st.environment=A.isMeshStandardMaterial||A.isMeshLambertMaterial||A.isMeshPhongMaterial?Q.environment:null,st.fog=Q.fog;const ee=A.isMeshStandardMaterial||A.isMeshLambertMaterial&&!A.envMap||A.isMeshPhongMaterial&&!A.envMap;st.envMap=bt.get(A.envMap||st.environment,ee),st.envMapRotation=st.environment!==null&&A.envMap===null?Q.environmentRotation:A.envMapRotation,Xt===void 0&&(A.addEventListener("dispose",ni),Xt=new Map,st.programs=Xt);let fe=Xt.get(Wt);if(fe!==void 0){if(st.currentProgram===fe&&st.lightsStateVersion===Vt)return ea(A,zt),fe}else zt.uniforms=Rt.getUniforms(A),J!==null&&A.isNodeMaterial&&J.build(A,ot,zt),A.onBeforeCompile(zt,$),fe=Rt.acquireProgram(zt,Wt),Xt.set(Wt,fe),st.uniforms=zt.uniforms;const Qt=st.uniforms;return(!A.isShaderMaterial&&!A.isRawShaderMaterial||A.clipping===!0)&&(Qt.clippingPlanes=$t.uniform),ea(A,zt),st.needsLights=Tl(A),st.lightsStateVersion=Vt,st.needsLights&&(Qt.ambientLightColor.value=rt.state.ambient,Qt.lightProbe.value=rt.state.probe,Qt.directionalLights.value=rt.state.directional,Qt.directionalLightShadows.value=rt.state.directionalShadow,Qt.spotLights.value=rt.state.spot,Qt.spotLightShadows.value=rt.state.spotShadow,Qt.rectAreaLights.value=rt.state.rectArea,Qt.ltc_1.value=rt.state.rectAreaLTC1,Qt.ltc_2.value=rt.state.rectAreaLTC2,Qt.pointLights.value=rt.state.point,Qt.pointLightShadows.value=rt.state.pointShadow,Qt.hemisphereLights.value=rt.state.hemi,Qt.directionalShadowMatrix.value=rt.state.directionalShadowMatrix,Qt.spotLightMatrix.value=rt.state.spotLightMatrix,Qt.spotLightMap.value=rt.state.spotLightMap,Qt.pointShadowMatrix.value=rt.state.pointShadowMatrix),st.lightProbeGrid=O.state.lightProbeGridArray.length>0,st.currentProgram=fe,st.uniformsList=null,fe}function ta(A){if(A.uniformsList===null){const Q=A.currentProgram.getUniforms();A.uniformsList=vu.seqWithValue(Q.seq,A.uniforms)}return A.uniformsList}function ea(A,Q){const ot=V.get(A);ot.outputColorSpace=Q.outputColorSpace,ot.batching=Q.batching,ot.batchingColor=Q.batchingColor,ot.instancing=Q.instancing,ot.instancingColor=Q.instancingColor,ot.instancingMorph=Q.instancingMorph,ot.skinning=Q.skinning,ot.morphTargets=Q.morphTargets,ot.morphNormals=Q.morphNormals,ot.morphColors=Q.morphColors,ot.morphTargetsCount=Q.morphTargetsCount,ot.numClippingPlanes=Q.numClippingPlanes,ot.numIntersection=Q.numClipIntersection,ot.vertexAlphas=Q.vertexAlphas,ot.vertexTangents=Q.vertexTangents,ot.toneMapping=Q.toneMapping}function xs(A,Q){if(A.length===0)return null;if(A.length===1)return A[0].texture!==null?A[0]:null;C.setFromMatrixPosition(Q.matrixWorld);for(let ot=0,st=A.length;ot<st;ot++){const rt=A[ot];if(rt.texture!==null&&rt.boundingBox.containsPoint(C))return rt}return null}function La(A,Q,ot,st,rt){Q.isScene!==!0&&(Q=nn),ct.resetTextureUnits();const Bt=Q.fog,Vt=st.isMeshStandardMaterial||st.isMeshLambertMaterial||st.isMeshPhongMaterial?Q.environment:null,zt=q===null?$.outputColorSpace:q.isXRRenderTarget===!0?q.texture.colorSpace:Re.workingColorSpace,Wt=st.isMeshStandardMaterial||st.isMeshLambertMaterial&&!st.envMap||st.isMeshPhongMaterial&&!st.envMap,Xt=bt.get(st.envMap||Vt,Wt),ee=st.vertexColors===!0&&!!ot.attributes.color&&ot.attributes.color.itemSize===4,fe=!!ot.attributes.tangent&&(!!st.normalMap||st.anisotropy>0),Qt=!!ot.morphAttributes.position,De=!!ot.morphAttributes.normal,sn=!!ot.morphAttributes.color;let je=Ki;st.toneMapped&&(q===null||q.isXRRenderTarget===!0)&&(je=$.toneMapping);const Ie=ot.morphAttributes.position||ot.morphAttributes.normal||ot.morphAttributes.color,Fe=Ie!==void 0?Ie.length:0,Gt=V.get(st),Bn=O.state.lights;if(ve===!0&&(de===!0||A!==Et)){const Pe=A===Et&&st.id===ht;$t.setState(st,A,Pe)}let xe=!1;st.version===Gt.__version?(Gt.needsLights&&Gt.lightsStateVersion!==Bn.state.version||Gt.outputColorSpace!==zt||rt.isBatchedMesh&&Gt.batching===!1||!rt.isBatchedMesh&&Gt.batching===!0||rt.isBatchedMesh&&Gt.batchingColor===!0&&rt.colorTexture===null||rt.isBatchedMesh&&Gt.batchingColor===!1&&rt.colorTexture!==null||rt.isInstancedMesh&&Gt.instancing===!1||!rt.isInstancedMesh&&Gt.instancing===!0||rt.isSkinnedMesh&&Gt.skinning===!1||!rt.isSkinnedMesh&&Gt.skinning===!0||rt.isInstancedMesh&&Gt.instancingColor===!0&&rt.instanceColor===null||rt.isInstancedMesh&&Gt.instancingColor===!1&&rt.instanceColor!==null||rt.isInstancedMesh&&Gt.instancingMorph===!0&&rt.morphTexture===null||rt.isInstancedMesh&&Gt.instancingMorph===!1&&rt.morphTexture!==null||Gt.envMap!==Xt||st.fog===!0&&Gt.fog!==Bt||Gt.numClippingPlanes!==void 0&&(Gt.numClippingPlanes!==$t.numPlanes||Gt.numIntersection!==$t.numIntersection)||Gt.vertexAlphas!==ee||Gt.vertexTangents!==fe||Gt.morphTargets!==Qt||Gt.morphNormals!==De||Gt.morphColors!==sn||Gt.toneMapping!==je||Gt.morphTargetsCount!==Fe||!!Gt.lightProbeGrid!=O.state.lightProbeGridArray.length>0)&&(xe=!0):(xe=!0,Gt.__version=st.version);let bn=Gt.currentProgram;xe===!0&&(bn=Na(st,Q,rt),J&&st.isNodeMaterial&&J.onUpdateProgram(st,bn,Gt));let ii=!1,Ci=!1,ai=!1;const He=bn.getUniforms(),rn=Gt.uniforms;if(M.useProgram(bn.program)&&(ii=!0,Ci=!0,ai=!0),st.id!==ht&&(ht=st.id,Ci=!0),Gt.needsLights){const Pe=xs(O.state.lightProbeGridArray,rt);Gt.lightProbeGrid!==Pe&&(Gt.lightProbeGrid=Pe,Ci=!0)}if(ii||Et!==A){M.buffers.depth.getReversed()&&A.reversedDepth!==!0&&(A._reversedDepth=!0,A.updateProjectionMatrix()),He.setValue(K,"projectionMatrix",A.projectionMatrix),He.setValue(K,"viewMatrix",A.matrixWorldInverse);const Gi=He.map.cameraPosition;Gi!==void 0&&Gi.setValue(K,Qe.setFromMatrixPosition(A.matrixWorld)),D.logarithmicDepthBuffer&&He.setValue(K,"logDepthBufFC",2/(Math.log(A.far+1)/Math.LN2)),(st.isMeshPhongMaterial||st.isMeshToonMaterial||st.isMeshLambertMaterial||st.isMeshBasicMaterial||st.isMeshStandardMaterial||st.isShaderMaterial)&&He.setValue(K,"isOrthographic",A.isOrthographicCamera===!0),Et!==A&&(Et=A,Ci=!0,ai=!0)}if(Gt.needsLights&&(Bn.state.directionalShadowMap.length>0&&He.setValue(K,"directionalShadowMap",Bn.state.directionalShadowMap,ct),Bn.state.spotShadowMap.length>0&&He.setValue(K,"spotShadowMap",Bn.state.spotShadowMap,ct),Bn.state.pointShadowMap.length>0&&He.setValue(K,"pointShadowMap",Bn.state.pointShadowMap,ct)),rt.isSkinnedMesh){He.setOptional(K,rt,"bindMatrix"),He.setOptional(K,rt,"bindMatrixInverse");const Pe=rt.skeleton;Pe&&(Pe.boneTexture===null&&Pe.computeBoneTexture(),He.setValue(K,"boneTexture",Pe.boneTexture,ct))}rt.isBatchedMesh&&(He.setOptional(K,rt,"batchingTexture"),He.setValue(K,"batchingTexture",rt._matricesTexture,ct),He.setOptional(K,rt,"batchingIdTexture"),He.setValue(K,"batchingIdTexture",rt._indirectTexture,ct),He.setOptional(K,rt,"batchingColorTexture"),rt._colorsTexture!==null&&He.setValue(K,"batchingColorTexture",rt._colorsTexture,ct));const Di=ot.morphAttributes;if((Di.position!==void 0||Di.normal!==void 0||Di.color!==void 0)&&Z.update(rt,ot,bn),(Ci||Gt.receiveShadow!==rt.receiveShadow)&&(Gt.receiveShadow=rt.receiveShadow,He.setValue(K,"receiveShadow",rt.receiveShadow)),(st.isMeshStandardMaterial||st.isMeshLambertMaterial||st.isMeshPhongMaterial)&&st.envMap===null&&Q.environment!==null&&(rn.envMapIntensity.value=Q.environmentIntensity),rn.dfgLUT!==void 0&&(rn.dfgLUT.value=yR()),Ci){if(He.setValue(K,"toneMappingExposure",$.toneMappingExposure),Gt.needsLights&&gn(rn,ai),Bt&&st.fog===!0&&Ft.refreshFogUniforms(rn,Bt),Ft.refreshMaterialUniforms(rn,st,St,vt,O.state.transmissionRenderTarget[A.id]),Gt.needsLights&&Gt.lightProbeGrid){const Pe=Gt.lightProbeGrid;rn.probesSH.value=Pe.texture,rn.probesMin.value.copy(Pe.boundingBox.min),rn.probesMax.value.copy(Pe.boundingBox.max),rn.probesResolution.value.copy(Pe.resolution)}vu.upload(K,ta(Gt),rn,ct)}if(st.isShaderMaterial&&st.uniformsNeedUpdate===!0&&(vu.upload(K,ta(Gt),rn,ct),st.uniformsNeedUpdate=!1),st.isSpriteMaterial&&He.setValue(K,"center",rt.center),He.setValue(K,"modelViewMatrix",rt.modelViewMatrix),He.setValue(K,"normalMatrix",rt.normalMatrix),He.setValue(K,"modelMatrix",rt.matrixWorld),st.uniformsGroups!==void 0){const Pe=st.uniformsGroups;for(let Gi=0,Oa=Pe.length;Gi<Oa;Gi++){const ys=Pe[Gi];Tt.update(ys,bn),Tt.bind(ys,bn)}}return bn}function gn(A,Q){A.ambientLightColor.needsUpdate=Q,A.lightProbe.needsUpdate=Q,A.directionalLights.needsUpdate=Q,A.directionalLightShadows.needsUpdate=Q,A.pointLights.needsUpdate=Q,A.pointLightShadows.needsUpdate=Q,A.spotLights.needsUpdate=Q,A.spotLightShadows.needsUpdate=Q,A.rectAreaLights.needsUpdate=Q,A.hemisphereLights.needsUpdate=Q}function Tl(A){return A.isMeshLambertMaterial||A.isMeshToonMaterial||A.isMeshPhongMaterial||A.isMeshStandardMaterial||A.isShadowMaterial||A.isShaderMaterial&&A.lights===!0}this.getActiveCubeFace=function(){return I},this.getActiveMipmapLevel=function(){return H},this.getRenderTarget=function(){return q},this.setRenderTargetTextures=function(A,Q,ot){const st=V.get(A);st.__autoAllocateDepthBuffer=A.resolveDepthBuffer===!1,st.__autoAllocateDepthBuffer===!1&&(st.__useRenderToTexture=!1),V.get(A.texture).__webglTexture=Q,V.get(A.depthTexture).__webglTexture=st.__autoAllocateDepthBuffer?void 0:ot,st.__hasExternalTextures=!0},this.setRenderTargetFramebuffer=function(A,Q){const ot=V.get(A);ot.__webglFramebuffer=Q,ot.__useDefaultFramebuffer=Q===void 0},this.setRenderTarget=function(A,Q=0,ot=0){q=A,I=Q,H=ot;let st=null,rt=!1,Bt=!1;if(A){const zt=V.get(A);if(zt.__useDefaultFramebuffer!==void 0){M.bindFramebuffer(K.FRAMEBUFFER,zt.__webglFramebuffer),L.copy(A.viewport),W.copy(A.scissor),yt=A.scissorTest,M.viewport(L),M.scissor(W),M.setScissorTest(yt),ht=-1;return}else if(zt.__webglFramebuffer===void 0)ct.setupRenderTarget(A);else if(zt.__hasExternalTextures)ct.rebindTextures(A,V.get(A.texture).__webglTexture,V.get(A.depthTexture).__webglTexture);else if(A.depthBuffer){const ee=A.depthTexture;if(zt.__boundDepthTexture!==ee){if(ee!==null&&V.has(ee)&&(A.width!==ee.image.width||A.height!==ee.image.height))throw new Error("THREE.WebGLRenderer: Attached DepthTexture is initialized to the incorrect size.");ct.setupDepthRenderbuffer(A)}}const Wt=A.texture;(Wt.isData3DTexture||Wt.isDataArrayTexture||Wt.isCompressedArrayTexture)&&(Bt=!0);const Xt=V.get(A).__webglFramebuffer;A.isWebGLCubeRenderTarget?(Array.isArray(Xt[Q])?st=Xt[Q][ot]:st=Xt[Q],rt=!0):A.samples>0&&ct.useMultisampledRTT(A)===!1?st=V.get(A).__webglMultisampledFramebuffer:Array.isArray(Xt)?st=Xt[ot]:st=Xt,L.copy(A.viewport),W.copy(A.scissor),yt=A.scissorTest}else L.copy(Kt).multiplyScalar(St).floor(),W.copy(Ce).multiplyScalar(St).floor(),yt=le;if(ot!==0&&(st=dt),M.bindFramebuffer(K.FRAMEBUFFER,st)&&M.drawBuffers(A,st),M.viewport(L),M.scissor(W),M.setScissorTest(yt),rt){const zt=V.get(A.texture);K.framebufferTexture2D(K.FRAMEBUFFER,K.COLOR_ATTACHMENT0,K.TEXTURE_CUBE_MAP_POSITIVE_X+Q,zt.__webglTexture,ot)}else if(Bt){const zt=Q;for(let Wt=0;Wt<A.textures.length;Wt++){const Xt=V.get(A.textures[Wt]);K.framebufferTextureLayer(K.FRAMEBUFFER,K.COLOR_ATTACHMENT0+Wt,Xt.__webglTexture,ot,zt)}}else if(A!==null&&ot!==0){const zt=V.get(A.texture);K.framebufferTexture2D(K.FRAMEBUFFER,K.COLOR_ATTACHMENT0,K.TEXTURE_2D,zt.__webglTexture,ot)}ht=-1},this.readRenderTargetPixels=function(A,Q,ot,st,rt,Bt,Vt,zt=0){if(!(A&&A.isWebGLRenderTarget)){Ae("WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");return}let Wt=V.get(A).__webglFramebuffer;if(A.isWebGLCubeRenderTarget&&Vt!==void 0&&(Wt=Wt[Vt]),Wt){M.bindFramebuffer(K.FRAMEBUFFER,Wt);try{const Xt=A.textures[zt],ee=Xt.format,fe=Xt.type;if(A.textures.length>1&&K.readBuffer(K.COLOR_ATTACHMENT0+zt),!D.textureFormatReadable(ee)){Ae("WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");return}if(!D.textureTypeReadable(fe)){Ae("WebGLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");return}Q>=0&&Q<=A.width-st&&ot>=0&&ot<=A.height-rt&&K.readPixels(Q,ot,st,rt,Nt.convert(ee),Nt.convert(fe),Bt)}finally{const Xt=q!==null?V.get(q).__webglFramebuffer:null;M.bindFramebuffer(K.FRAMEBUFFER,Xt)}}},this.readRenderTargetPixelsAsync=async function(A,Q,ot,st,rt,Bt,Vt,zt=0){if(!(A&&A.isWebGLRenderTarget))throw new Error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");let Wt=V.get(A).__webglFramebuffer;if(A.isWebGLCubeRenderTarget&&Vt!==void 0&&(Wt=Wt[Vt]),Wt)if(Q>=0&&Q<=A.width-st&&ot>=0&&ot<=A.height-rt){M.bindFramebuffer(K.FRAMEBUFFER,Wt);const Xt=A.textures[zt],ee=Xt.format,fe=Xt.type;if(A.textures.length>1&&K.readBuffer(K.COLOR_ATTACHMENT0+zt),!D.textureFormatReadable(ee))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in RGBA or implementation defined format.");if(!D.textureTypeReadable(fe))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in UnsignedByteType or implementation defined type.");const Qt=K.createBuffer();K.bindBuffer(K.PIXEL_PACK_BUFFER,Qt),K.bufferData(K.PIXEL_PACK_BUFFER,Bt.byteLength,K.STREAM_READ),K.readPixels(Q,ot,st,rt,Nt.convert(ee),Nt.convert(fe),0);const De=q!==null?V.get(q).__webglFramebuffer:null;M.bindFramebuffer(K.FRAMEBUFFER,De);const sn=K.fenceSync(K.SYNC_GPU_COMMANDS_COMPLETE,0);return K.flush(),await gE(K,sn,4),K.bindBuffer(K.PIXEL_PACK_BUFFER,Qt),K.getBufferSubData(K.PIXEL_PACK_BUFFER,0,Bt),K.deleteBuffer(Qt),K.deleteSync(sn),Bt}else throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: requested read bounds are out of range.")},this.copyFramebufferToTexture=function(A,Q=null,ot=0){const st=Math.pow(2,-ot),rt=Math.floor(A.image.width*st),Bt=Math.floor(A.image.height*st),Vt=Q!==null?Q.x:0,zt=Q!==null?Q.y:0;ct.setTexture2D(A,0),K.copyTexSubImage2D(K.TEXTURE_2D,ot,0,0,Vt,zt,rt,Bt),M.unbindTexture()},this.copyTextureToTexture=function(A,Q,ot=null,st=null,rt=0,Bt=0){let Vt,zt,Wt,Xt,ee,fe,Qt,De,sn;const je=A.isCompressedTexture?A.mipmaps[Bt]:A.image;if(ot!==null)Vt=ot.max.x-ot.min.x,zt=ot.max.y-ot.min.y,Wt=ot.isBox3?ot.max.z-ot.min.z:1,Xt=ot.min.x,ee=ot.min.y,fe=ot.isBox3?ot.min.z:0;else{const rn=Math.pow(2,-rt);Vt=Math.floor(je.width*rn),zt=Math.floor(je.height*rn),A.isDataArrayTexture?Wt=je.depth:A.isData3DTexture?Wt=Math.floor(je.depth*rn):Wt=1,Xt=0,ee=0,fe=0}st!==null?(Qt=st.x,De=st.y,sn=st.z):(Qt=0,De=0,sn=0);const Ie=Nt.convert(Q.format),Fe=Nt.convert(Q.type);let Gt;Q.isData3DTexture?(ct.setTexture3D(Q,0),Gt=K.TEXTURE_3D):Q.isDataArrayTexture||Q.isCompressedArrayTexture?(ct.setTexture2DArray(Q,0),Gt=K.TEXTURE_2D_ARRAY):(ct.setTexture2D(Q,0),Gt=K.TEXTURE_2D),M.activeTexture(K.TEXTURE0),M.pixelStorei(K.UNPACK_FLIP_Y_WEBGL,Q.flipY),M.pixelStorei(K.UNPACK_PREMULTIPLY_ALPHA_WEBGL,Q.premultiplyAlpha),M.pixelStorei(K.UNPACK_ALIGNMENT,Q.unpackAlignment);const Bn=M.getParameter(K.UNPACK_ROW_LENGTH),xe=M.getParameter(K.UNPACK_IMAGE_HEIGHT),bn=M.getParameter(K.UNPACK_SKIP_PIXELS),ii=M.getParameter(K.UNPACK_SKIP_ROWS),Ci=M.getParameter(K.UNPACK_SKIP_IMAGES);M.pixelStorei(K.UNPACK_ROW_LENGTH,je.width),M.pixelStorei(K.UNPACK_IMAGE_HEIGHT,je.height),M.pixelStorei(K.UNPACK_SKIP_PIXELS,Xt),M.pixelStorei(K.UNPACK_SKIP_ROWS,ee),M.pixelStorei(K.UNPACK_SKIP_IMAGES,fe);const ai=A.isDataArrayTexture||A.isData3DTexture,He=Q.isDataArrayTexture||Q.isData3DTexture;if(A.isDepthTexture){const rn=V.get(A),Di=V.get(Q),Pe=V.get(rn.__renderTarget),Gi=V.get(Di.__renderTarget);M.bindFramebuffer(K.READ_FRAMEBUFFER,Pe.__webglFramebuffer),M.bindFramebuffer(K.DRAW_FRAMEBUFFER,Gi.__webglFramebuffer);for(let Oa=0;Oa<Wt;Oa++)ai&&(K.framebufferTextureLayer(K.READ_FRAMEBUFFER,K.COLOR_ATTACHMENT0,V.get(A).__webglTexture,rt,fe+Oa),K.framebufferTextureLayer(K.DRAW_FRAMEBUFFER,K.COLOR_ATTACHMENT0,V.get(Q).__webglTexture,Bt,sn+Oa)),K.blitFramebuffer(Xt,ee,Vt,zt,Qt,De,Vt,zt,K.DEPTH_BUFFER_BIT,K.NEAREST);M.bindFramebuffer(K.READ_FRAMEBUFFER,null),M.bindFramebuffer(K.DRAW_FRAMEBUFFER,null)}else if(rt!==0||A.isRenderTargetTexture||V.has(A)){const rn=V.get(A),Di=V.get(Q);M.bindFramebuffer(K.READ_FRAMEBUFFER,gt),M.bindFramebuffer(K.DRAW_FRAMEBUFFER,et);for(let Pe=0;Pe<Wt;Pe++)ai?K.framebufferTextureLayer(K.READ_FRAMEBUFFER,K.COLOR_ATTACHMENT0,rn.__webglTexture,rt,fe+Pe):K.framebufferTexture2D(K.READ_FRAMEBUFFER,K.COLOR_ATTACHMENT0,K.TEXTURE_2D,rn.__webglTexture,rt),He?K.framebufferTextureLayer(K.DRAW_FRAMEBUFFER,K.COLOR_ATTACHMENT0,Di.__webglTexture,Bt,sn+Pe):K.framebufferTexture2D(K.DRAW_FRAMEBUFFER,K.COLOR_ATTACHMENT0,K.TEXTURE_2D,Di.__webglTexture,Bt),rt!==0?K.blitFramebuffer(Xt,ee,Vt,zt,Qt,De,Vt,zt,K.COLOR_BUFFER_BIT,K.NEAREST):He?K.copyTexSubImage3D(Gt,Bt,Qt,De,sn+Pe,Xt,ee,Vt,zt):K.copyTexSubImage2D(Gt,Bt,Qt,De,Xt,ee,Vt,zt);M.bindFramebuffer(K.READ_FRAMEBUFFER,null),M.bindFramebuffer(K.DRAW_FRAMEBUFFER,null)}else He?A.isDataTexture||A.isData3DTexture?K.texSubImage3D(Gt,Bt,Qt,De,sn,Vt,zt,Wt,Ie,Fe,je.data):Q.isCompressedArrayTexture?K.compressedTexSubImage3D(Gt,Bt,Qt,De,sn,Vt,zt,Wt,Ie,je.data):K.texSubImage3D(Gt,Bt,Qt,De,sn,Vt,zt,Wt,Ie,Fe,je):A.isDataTexture?K.texSubImage2D(K.TEXTURE_2D,Bt,Qt,De,Vt,zt,Ie,Fe,je.data):A.isCompressedTexture?K.compressedTexSubImage2D(K.TEXTURE_2D,Bt,Qt,De,je.width,je.height,Ie,je.data):K.texSubImage2D(K.TEXTURE_2D,Bt,Qt,De,Vt,zt,Ie,Fe,je);M.pixelStorei(K.UNPACK_ROW_LENGTH,Bn),M.pixelStorei(K.UNPACK_IMAGE_HEIGHT,xe),M.pixelStorei(K.UNPACK_SKIP_PIXELS,bn),M.pixelStorei(K.UNPACK_SKIP_ROWS,ii),M.pixelStorei(K.UNPACK_SKIP_IMAGES,Ci),Bt===0&&Q.generateMipmaps&&K.generateMipmap(Gt),M.unbindTexture()},this.initRenderTarget=function(A){V.get(A).__webglFramebuffer===void 0&&ct.setupRenderTarget(A)},this.initTexture=function(A){A.isCubeTexture?ct.setTextureCube(A,0):A.isData3DTexture?ct.setTexture3D(A,0):A.isDataArrayTexture||A.isCompressedArrayTexture?ct.setTexture2DArray(A,0):ct.setTexture2D(A,0),M.unbindTexture()},this.resetState=function(){I=0,H=0,q=null,M.reset(),Ht.reset()},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}get coordinateSystem(){return Zi}get outputColorSpace(){return this._outputColorSpace}set outputColorSpace(t){this._outputColorSpace=t;const i=this.getContext();i.drawingBufferColorSpace=Re._getDrawingBufferColorSpace(t),i.unpackColorSpace=Re._getUnpackColorSpace()}}const ax={angle:.14,elevation:12.6,distance:22},Dd=new we,sx=new we,rx=new we,MR=new ie(16777215),ER=new ie(16777215),ox=new ie(16766571),du=new ie;function Qp(r,t,i){const s=t.hoverProgress*.16+t.activeProgress*.055,l=t.hoverProgress*.045+t.activeProgress*.58;Dd.makeRotationX(s),Dd.setPosition(t.x,t.bottom,t.z+l),r.components.forEach(({mesh:f,y:p,z:m})=>{sx.makeTranslation(0,p,m),rx.multiplyMatrices(Dd,sx),f.setMatrixAt(i,rx)});const c=Math.max(t.hoverProgress,t.activeProgress);du.copy(MR).lerp(r.zoneTint,c*.42).lerp(ox,t.activeProgress*.42),r.bodyInstances.setColorAt(i,du),du.copy(ER).lerp(r.zoneColor,c*.5).lerp(ox,t.activeProgress*.3),r.accentInstances.setColorAt(i,du)}function bR(r,t,i){r.records.forEach((s,l)=>{const c=s.file.id===t?1:0,f=s.file.id===i?1:0;s.hoverProgress=ba.lerp(s.hoverProgress,c,c?.18:.13),s.activeProgress=ba.lerp(s.activeProgress,f,f?.115:.16),Qp(r,s,l)}),r.components.forEach(({mesh:s})=>{s.instanceMatrix.needsUpdate=!0}),r.bodyInstances.instanceColor.needsUpdate=!0,r.accentInstances.instanceColor.needsUpdate=!0}function TR(r){r.records.forEach((t,i)=>{t.hoverProgress=0,t.activeProgress=0,Qp(r,t,i)}),r.components.forEach(({mesh:t})=>{t.instanceMatrix.needsUpdate=!0}),r.bodyInstances.instanceColor.needsUpdate=!0,r.accentInstances.instanceColor.needsUpdate=!0}function AR(r,t,i,s){const l=document.createElement("canvas");l.width=512,l.height=256;const c=l.getContext("2d");c.clearRect(0,0,l.width,l.height),c.fillStyle="rgba(5, 12, 20, .82)",c.roundRect(72,32,368,172,18),c.fill(),c.strokeStyle=i,c.lineWidth=4,c.stroke(),c.fillStyle="#ffffff",c.textAlign="center",c.font="700 62px Microsoft YaHei, sans-serif",c.strokeStyle="rgba(255, 255, 255, .95)",c.lineWidth=2,c.strokeText(r,256,112),c.fillText(r,256,112),c.fillStyle="#ffffff",c.font="700 35px Microsoft YaHei, sans-serif",c.lineWidth=1.5,c.strokeText(t,256,166),c.fillText(t,256,166);const f=8,p=7,m=260,d=(m-p*(f-1))/f,x=(l.width-m)/2,v=Math.max(1,Math.round(ba.clamp(s,0,1)*f));for(let w=0;w<f;w+=1){const E=x+w*(d+p);c.globalAlpha=w<v?.56:.16,c.fillStyle=w<v?i:"#7890a4",c.beginPath(),c.roundRect(E,184,d,6,3),c.fill()}c.globalAlpha=1;const g=new Gx(l);g.colorSpace=qn;const S=new Xp({map:g,transparent:!0,depthTest:!1,toneMapped:!1}),b=new zx(S);return b.scale.set(1.08,.54,1),b.position.set(0,.3,.47),b.renderOrder=8,b}function RR(r,t,i){const s=document.createElement("canvas");s.width=1024,s.height=256;const l=s.getContext("2d");l.clearRect(0,0,s.width,s.height),l.fillStyle="rgba(4, 11, 18, .9)",l.roundRect(24,24,976,208,18),l.fill(),l.strokeStyle=r.color,l.lineWidth=5,l.stroke(),l.fillStyle=r.color,l.font="700 72px Microsoft YaHei, sans-serif",l.fillText(`${r.id} 区`,66,117),l.fillStyle="#ffffff",l.font="700 52px Microsoft YaHei, sans-serif",l.strokeStyle="rgba(255, 255, 255, .95)",l.lineWidth=1.75,l.strokeText(r.department,278,108),l.fillText(r.department,278,108),l.fillStyle="#ffffff",l.font="700 35px Microsoft YaHei, sans-serif",l.lineWidth=1.5,l.strokeText(`8 个柜体  ·  ${t}/${i} 盒`,278,174),l.fillText(`8 个柜体  ·  ${t}/${i} 盒`,278,174);const c=new Gx(s);c.colorSpace=qn;const f=new zx(new Xp({map:c,transparent:!0,depthTest:!1,toneMapped:!1}));return f.scale.set(3.25,.81,1),f.renderOrder=9,f}function wR(r){const t={2:[{x:-4.43,z:0,labelPosition:"rear"},{x:4.37,z:0,labelPosition:"rear"}],3:[{x:-4.43,z:-2.18,labelPosition:"rear"},{x:4.37,z:-2.18,labelPosition:"rear"},{x:0,z:2.08,labelPosition:"front"}],4:[{x:-4.43,z:-2.58,labelPosition:"rear"},{x:4.37,z:-2.58,labelPosition:"rear"},{x:-4.43,z:2.18,labelPosition:"front"},{x:4.37,z:2.18,labelPosition:"front"}]},i=t[r.length]||t[4];return Object.fromEntries(r.map((s,l)=>[s.id,i[l]||i.at(-1)]))}function CR(r,t,i,s){const f=[];return t.forEach(p=>{const m=s[p.id],d=new ln(new gs(6.55,3.25),new io({color:p.color,transparent:!0,opacity:.055,depthWrite:!1,side:wi}));d.rotation.x=-Math.PI/2,d.position.set(m.x,.026,m.z),r.add(d);const x=[new k(-6.55/2,0,-3.25/2),new k(6.55/2,0,-3.25/2),new k(6.55/2,0,3.25/2),new k(-6.55/2,0,3.25/2)],v=new Sp(new hn().setFromPoints(x),new hs({color:p.color,transparent:!0,opacity:.82}));v.position.set(m.x,.055,m.z),r.add(v);const g=new hn().setFromPoints([new k(-6.55/2,.02,-3.25/2+.68),new k(-6.55/2,.02,-3.25/2),new k(-6.55/2+.68,.02,-3.25/2),new k(6.55/2-.68,.02,-3.25/2),new k(6.55/2,.02,-3.25/2),new k(6.55/2,.02,-3.25/2+.68),new k(6.55/2,.02,3.25/2-.68),new k(6.55/2,.02,3.25/2),new k(6.55/2-.68,.02,3.25/2),new k(-6.55/2+.68,.02,3.25/2),new k(-6.55/2,.02,3.25/2),new k(-6.55/2,.02,3.25/2-.68)]),S=new Nu(g,new hs({color:p.color,transparent:!0,opacity:1}));S.position.set(m.x,.07,m.z),r.add(S);const b=i.filter(F=>F.zone===p.id),w=b.reduce((F,C)=>F+C.used,0),E=b.reduce((F,C)=>F+C.capacity,0),_=RR(p,w,E),z=m.labelPosition==="rear";z||_.scale.multiplyScalar(.9),_.position.set(m.x-6.55/2+1.65,z?2.5:.36,z?m.z-3.25/2+.08:m.z+3.25/2-.08),r.add(_),f.push({borderMaterial:v.material,bracketMaterial:S.material,labelMaterial:_.material,platformMaterial:d.material})}),f}function DR(r,t){const i=new qs;i.userData={id:r.id,zone:r.zone,interactive:!0};const s=new pi({color:1121065,transparent:!0,opacity:1,depthWrite:!1,metalness:.78,roughness:.4,emissive:new ie(t.color),emissiveIntensity:.04}),l=[];[[-.5625,.94,0,.055,1.8,.88,s],[.5625,.94,0,.055,1.8,.88,s],[0,1.8125,-.07,1.18,.055,.74,s],[0,.0675,-.07,1.18,.055,.74,s],[0,.94,-.4125,1.07,1.69,.055,s]].forEach(([W,yt,At,wt,nt,vt,St])=>{const Ut=new ln(new Dn(wt,nt,vt),St);Ut.position.set(W,yt,At),Ut.userData={id:r.id,interactive:!0},Ut.castShadow=!0,Ut.receiveShadow=!0,i.add(Ut),l.push(Ut)});const c=new Dn(.7,.28,.025),f=new ln(c,s);f.position.set(0,1.56,-.2),f.castShadow=!0,f.receiveShadow=!0,i.add(f),l.push(f);const p=new hs({color:t.color,transparent:!0,opacity:.95}),m=new Dn(1.18,1.8,.88),d=new rb(m);m.dispose();const x=new Ix(d,p);x.position.set(0,.94,0),i.add(x);const v=new pi({color:1121065,transparent:!0,opacity:1,depthWrite:!1,emissive:new ie(t.color),emissiveIntensity:.035,metalness:.78,roughness:.4}),g=new pi({color:11123144,transparent:!0,opacity:1,depthWrite:!1,metalness:.95,roughness:.16}),S=new hs({color:t.color,transparent:!0,opacity:0,depthWrite:!1}),b=[];[{hinge:-.59,center:.295,handle:.515},{hinge:.59,center:-.295,handle:-.515}].forEach(({hinge:W,center:yt,handle:At})=>{const wt=new qs;wt.position.set(W,0,.46);const nt=new Dn(.54,1.46,.035),vt=new ln(nt,v);vt.position.set(yt,.9,.02),vt.userData={id:r.id,interactive:!0},wt.add(vt);const St=new Sp(new hn().setFromPoints([new k(-.27,-.73,0),new k(.27,-.73,0),new k(.27,.73,0),new k(-.27,.73,0)]),S);St.position.set(yt,.9,.039),wt.add(St);const Ut=new Dn(.045,.28,.04),Jt=new ln(Ut,g);Jt.position.set(At,.92,.05),wt.add(Jt),i.add(wt),b.push(wt)});const w=new pi({color:1582648,emissive:528668,emissiveIntensity:.2,transparent:!0,opacity:.94,metalness:.72,roughness:.36}),E=new io({color:t.color,transparent:!0,opacity:.64,blending:Ys,depthWrite:!1});for(let W=0;W<4;W+=1){const yt=new ln(new Dn(1.03,.024,.78),w);yt.position.set(0,.19+W*.31,0),i.add(yt);const At=new ln(new Dn(1.01,.016,.026),E);At.position.set(0,.19+W*.31,.402),i.add(At)}const _=new pi({color:15331057,metalness:.04,roughness:.62}),z=new pi({color:4685771,emissive:1059672,emissiveIntensity:.18,metalness:.08,roughness:.48}),F=new pi({color:5609180,emissive:1193055,emissiveIntensity:.16,roughness:.5}),C=new pi({color:12042954,emissive:6252913,emissiveIntensity:.5,metalness:.96,roughness:.12}),B=new pi({color:16251386,emissive:9410718,emissiveIntensity:.58,metalness:.08,roughness:.4}),O=new qs;O.visible=!1;const U=r.files.length,T=new ul(new Dn(.055,.18,.21),_,U),P=new ul(new Dn(.057,.075,.214),z,U),$=new ul(new Dn(.029,.052,.008),F,U),Y=new ul(new jp(.0115,.004,10,20),C,U),J=new ul(new qp(.008,16),B,U),dt=[{mesh:T,y:.165,z:0},{mesh:P,y:.0375,z:0},{mesh:$,y:.198,z:.109},{mesh:J,y:.038,z:.109},{mesh:Y,y:.038,z:.112}],gt={accentInstances:P,bodyInstances:T,components:dt,records:r.files.map(W=>({activeProgress:0,bottom:.202+(W.shelf-1)*.31,file:W,hoverProgress:0,x:-.4515+(W.box-1)*.0645,z:.255})),wasFocused:!1,zoneColor:new ie(t.color),zoneTint:new ie(t.color).lerp(new ie(16777215),.58)};T.userData={files:r.files,id:r.id,interactive:!0,kind:"archive-box"},gt.records.forEach((W,yt)=>{Qp(gt,W,yt)}),dt.forEach(({mesh:W})=>{W.instanceMatrix.needsUpdate=!0,W.castShadow=!0,W.frustumCulled=!1,O.add(W)}),T.instanceColor.needsUpdate=!0,P.instanceColor.needsUpdate=!0,i.add(O);const et=new io({color:t.color,transparent:!0,opacity:0,depthWrite:!1,side:wi,blending:Ys}),I=new ln(new gs(1.78,1.78),et);I.rotation.x=-Math.PI/2,I.position.set(0,.036,.18),I.renderOrder=2,i.add(I);const H=new hs({color:t.color,transparent:!0,opacity:0,blending:Ys,depthWrite:!1}),q=new Sp(new hn().setFromPoints([new k(-.89,0,-.89),new k(.89,0,-.89),new k(.89,0,.89),new k(-.89,0,.89)]),H);q.position.set(0,.043,.18),q.renderOrder=3,i.add(q);const ht=r.used/r.capacity,Et=AR(r.id,`${r.used}/${r.capacity}`,t.color,ht);Et.position.y=1.02,i.add(Et);const L=new qx(t.color,0,3.4,2);return L.position.set(0,.18,.22),i.add(L),i.userData.parts={archiveBoxes:gt,archiveGroup:O,bodyMaterial:s,doorEdgeMaterial:S,doorMaterial:v,doorPivots:b,edgeMaterial:p,focusPad:I,focusPadBorder:q,focusPadBorderMaterial:H,focusPadMaterial:et,handleMaterial:g,labelMaterial:Et.material,shellPanels:l,shelfLipMaterial:E,shelfMaterial:w,glow:L,utilization:ht,focusProgress:0,visibility:1},i}function UR(r){const t=new ln(new gs(17,11.5),new pi({color:857891,metalness:.58,roughness:.64}));t.rotation.x=-Math.PI/2,t.receiveShadow=!0,r.add(t);const i=new Tb(17,34,1796538,1454414);i.position.y=.015,i.material.transparent=!0,i.material.opacity=.32,r.add(i);const s=new pi({color:1713717,metalness:.75,roughness:.38});[[0,.75,-5.65,17.2,1.5,.18],[-8.55,.75,0,.18,1.5,11.5],[8.55,.75,0,.18,1.5,11.5]].forEach(([f,p,m,d,x,v])=>{const g=new ln(new Dn(d,x,v),s);g.position.set(f,p,m),g.castShadow=!0,r.add(g)});const l=new ln(new Dn(3.4,2.4,.28),new pi({color:2503748,metalness:.8,roughness:.3}));l.position.set(0,1.2,-5.52),r.add(l);const c=new ln(new Dn(2.7,1.95,.08),new pb({color:859953,transparent:!0,opacity:.75,metalness:.55,roughness:.3}));c.position.set(0,1.05,-5.31),r.add(c)}function NR(r){const t=[];return[-.36,0,.36].forEach((i,s)=>{const l=new lb([new k(i,.045,5.6),new k(i,.045,1.4),new k(i*.35,.045,-1.2),new k(i*.2,.045,-5.2)]),c=new hn().setFromPoints(l.getPoints(80)),f=new _b({color:s===1?5431295:2522367,dashSize:.26,gapSize:.21,transparent:!0,opacity:s===1?.78:.36}),p=new Nu(c,f);p.computeLineDistances(),r.add(p),t.push(f)}),t}const LR=Yt.forwardRef(function({activeFileId:t,autoRotate:i,cabinets:s,focusId:l,hoveredFileId:c,onFileHover:f,onFileSelect:p,onHover:m,onSelect:d,selectedId:x,zones:v},g){const S=Yt.useRef(null),b=Yt.useRef(null),w=Yt.useRef(x),E=Yt.useRef(i),_=Yt.useRef(l),z=Yt.useRef(c),F=Yt.useRef(t);return Yt.useEffect(()=>{w.current=x},[x]),Yt.useEffect(()=>{E.current=i},[i]),Yt.useEffect(()=>{_.current=l},[l]),Yt.useEffect(()=>{z.current=c},[c]),Yt.useEffect(()=>{F.current=t},[t]),Yt.useImperativeHandle(g,()=>({zoomIn(){b.current&&(b.current.view.distance=Math.max(16,b.current.view.distance-1.8))},zoomOut(){b.current&&(b.current.view.distance=Math.min(29,b.current.view.distance+1.8))},reset(){b.current&&Object.assign(b.current.view,ax)}})),Yt.useEffect(()=>{const C=S.current;if(!C)return;const B=new YE;B.fog=new kp(462874,.035);const O=new mi(38,1,.1,100),U=new SR({antialias:!0,alpha:!0,powerPreference:"high-performance"});U.setPixelRatio(Math.min(window.devicePixelRatio,1.8)),U.outputColorSpace=qn,U.toneMapping=Dp,U.toneMappingExposure=1.15,U.shadowMap.enabled=!0,U.shadowMap.type=_l,C.appendChild(U.domElement),B.add(new vb(8239615,462872,1.8));const T=new Sb(12179711,3.8);T.position.set(-6,12,8),T.castShadow=!0,T.shadow.mapSize.set(2048,2048),B.add(T);const P=new qx(2068991,16,18,2);P.position.set(0,3.5,2.8),B.add(P),UR(B);const $=wR(v),Y=CR(B,v,s,$),J=NR(B),dt=new Map;s.forEach(Lt=>{const D=v.find(Ct=>Ct.id===Lt.zone),M=Number(Lt.id.slice(-2))-1,X=Math.floor(M/4),V=M%4,ct=$[Lt.zone],bt=DR(Lt,D);bt.position.set(ct.x-2.22+V*1.48,0,ct.z-.625+X*1.25),B.add(bt),dt.set(Lt.id,bt)});const gt=new io({color:3324415,transparent:!0,opacity:.15,side:wi,blending:Ys,depthWrite:!1}),et=new ln(new gs(16.4,.45),gt);et.rotation.x=-Math.PI/2,et.position.set(0,.09,-5.1),B.add(et);const I=150,H=new Float32Array(I*3);for(let Lt=0;Lt<I;Lt+=1)H[Lt*3]=(Math.random()-.5)*15.8,H[Lt*3+1]=.08+Math.random()*.18,H[Lt*3+2]=(Math.random()-.5)*10.2;const q=new hn;q.setAttribute("position",new vi(H,3));const ht=new ab(q,new Fx({color:3712767,size:.045,transparent:!0,opacity:.65,blending:Ys,depthWrite:!1}));B.add(ht);const Et=new bb,L=new oe,W={...ax},yt=new k(0,.45,0),At=new k,wt=new k(0,.45,0);let nt=null,vt=null,St,Ut=!1,Jt=!1,Kt=!1,Ce=1,le={x:0,y:0};const _e=(Lt=!1)=>{const D=_.current?dt.get(_.current):null;if(D){const X=D.position.x<0?1:-1,V=2.15;At.set(D.position.x+X*2.8+V,2.8,D.position.z+6.2),wt.set(D.position.x+V,.91,D.position.z)}else{const X=Math.sqrt(Math.max(W.distance**2-W.elevation**2,36));At.set(Math.sin(W.angle)*X,W.elevation,Math.cos(W.angle)*X),wt.set(0,.45,0)}const M=Lt?1:D?.072:.055;O.position.lerp(At,M),yt.lerp(wt,M),O.fov=ba.lerp(O.fov,D?34:38,Lt?1:.07),O.updateProjectionMatrix(),O.lookAt(yt)},ve=()=>{const Lt=Math.max(C.clientWidth,1),D=Math.max(C.clientHeight,1);U.setSize(Lt,D,!1),O.aspect=Lt/D,O.updateProjectionMatrix(),_e(!Kt),Kt=!0},de=Lt=>{const D=U.domElement.getBoundingClientRect();return L.x=(Lt.clientX-D.left)/D.width*2-1,L.y=-((Lt.clientY-D.top)/D.height)*2+1,Et.setFromCamera(L,O),Et.intersectObjects(B.children,!0).filter(M=>{var X;return(X=M.object.userData)==null?void 0:X.interactive})},Ke=Lt=>{const D=_.current;if(!D)return null;const M=de(Lt).find(V=>V.object.userData.kind==="archive-box"&&V.object.userData.id===D&&Number.isInteger(V.instanceId));if(!M)return null;const X=M.object.userData.files[M.instanceId];return X?{file:X,id:X.id}:null},Qe=Lt=>{if(_.current){const X=Ke(Lt),V=(X==null?void 0:X.id)||null;C.style.cursor=V?"pointer":"default",V!==vt&&(vt=V,f==null||f(V));return}if(Ut){const X=Lt.clientX-le.x,V=Lt.clientY-le.y;Math.abs(X)+Math.abs(V)>2&&(Jt=!0),W.angle-=X*.006,W.elevation=ba.clamp(W.elevation+V*.035,6.8,14),le={x:Lt.clientX,y:Lt.clientY};return}const D=de(Lt)[0],M=(D==null?void 0:D.object.userData.id)||null;M!==nt&&(nt=M,C.style.cursor=M?"pointer":"grab",m==null||m(M))},Je=Lt=>{var D,M;_.current||(Ut=!0,Jt=!1,le={x:Lt.clientX,y:Lt.clientY},C.style.cursor="grabbing",(M=(D=U.domElement).setPointerCapture)==null||M.call(D,Lt.pointerId))},nn=Lt=>{var D,M;if(_.current){Ut=!1;const X=Ke(Lt);C.style.cursor=X?"pointer":"default",X&&(p==null||p(X.file));return}if(Ut=!1,C.style.cursor=nt?"pointer":"grab",(M=(D=U.domElement).releasePointerCapture)==null||M.call(D,Lt.pointerId),!Jt){const X=de(Lt)[0],V=X==null?void 0:X.object.userData.id;V&&(d==null||d(V))}},ze=Lt=>{var D,M;Ut=!1,Jt=!1,nt=null,vt=null,C.style.cursor=_.current?"default":"grab",(M=(D=U.domElement).releasePointerCapture)==null||M.call(D,Lt.pointerId),m==null||m(null),f==null||f(null)},$e=Lt=>{Lt.preventDefault(),!_.current&&(W.distance=ba.clamp(W.distance+Lt.deltaY*.008,16,29))};U.domElement.addEventListener("pointermove",Qe),U.domElement.addEventListener("pointerdown",Je),U.domElement.addEventListener("pointerup",nn),U.domElement.addEventListener("pointerleave",ze),U.domElement.addEventListener("wheel",$e,{passive:!1});const K=new ResizeObserver(ve);K.observe(C),ve(),b.current={view:W,cabinetGroups:dt};const Le=Lt=>{const D=Lt*.001;E.current&&!Ut&&!_.current&&(W.angle+=55e-5),_e(),et.position.z=-5.1+D*1.05%10.2,gt.opacity=.11+Math.sin(D*4.5)*.045,ht.rotation.y=D*.015,J.forEach((M,X)=>{M.dashOffset=-(D*(.25+X*.06))}),Ce=ba.lerp(Ce,_.current?.1:1,.075),Y.forEach(M=>{M.platformMaterial.opacity=.055*Ce,M.borderMaterial.opacity=.82*Ce,M.bracketMaterial.opacity=Ce,M.labelMaterial.opacity=_.current?0:Ce}),dt.forEach((M,X)=>{const V=M.userData.parts,ct=X===w.current,bt=X===_.current,Ct=_.current&&!bt?0:1;M.visible=!_.current||bt,V.visibility=ba.lerp(V.visibility,Ct,.075),V.focusProgress=ba.lerp(V.focusProgress,bt?1:0,bt?.07:.1),V.doorPivots[0].rotation.y=-1.54*V.focusProgress,V.doorPivots[1].rotation.y=1.54*V.focusProgress,V.archiveGroup.visible=bt,bt?(V.archiveBoxes.wasFocused=!0,bR(V.archiveBoxes,z.current,F.current)):V.archiveBoxes.wasFocused&&(TR(V.archiveBoxes),V.archiveBoxes.wasFocused=!1);const ut=Math.max(0,1-V.focusProgress)*V.visibility;V.bodyMaterial.opacity=ut,V.bodyMaterial.emissiveIntensity=ct?bt?.1:.14:.04,V.doorMaterial.opacity=ut,V.handleMaterial.opacity=ut,V.doorEdgeMaterial.opacity=.88*V.focusProgress*V.visibility,V.shellPanels.forEach(Ot=>{Ot.castShadow=ut>.05}),V.shelfMaterial.opacity=.94*V.visibility,V.shelfLipMaterial.opacity=.64*V.visibility,V.labelMaterial.opacity=_.current?0:V.visibility;const pt=.6+Math.sin(D*4+M.position.x)*.35;V.edgeMaterial.opacity=(V.utilization>.93?pt:.95)*V.visibility,ct?(M.scale.setScalar(1),V.glow.intensity=bt?2.4:.65+Math.sin(D*3.8)*.18):(M.scale.setScalar(1),V.glow.intensity=0);const Rt=(Math.sin(D*2.5)+1)/2,Ft=bt?.96+Rt*.08:1;V.focusPadMaterial.opacity=bt?.12+Rt*.13:0,V.focusPadBorderMaterial.opacity=bt?.42+Rt*.48:0,V.focusPad.scale.setScalar(Ft),V.focusPadBorder.scale.setScalar(Ft)}),U.render(B,O),St=requestAnimationFrame(Le)};return St=requestAnimationFrame(Le),()=>{cancelAnimationFrame(St),K.disconnect(),U.domElement.removeEventListener("pointermove",Qe),U.domElement.removeEventListener("pointerdown",Je),U.domElement.removeEventListener("pointerup",nn),U.domElement.removeEventListener("pointerleave",ze),U.domElement.removeEventListener("wheel",$e),B.traverse(Lt=>{var D,M,X,V,ct,bt,Ct;(M=(D=Lt.geometry)==null?void 0:D.dispose)==null||M.call(D),Array.isArray(Lt.material)?Lt.material.forEach(ut=>ut.dispose()):((ct=(V=(X=Lt.material)==null?void 0:X.map)==null?void 0:V.dispose)==null||ct.call(V),(Ct=(bt=Lt.material)==null?void 0:bt.dispose)==null||Ct.call(bt))}),U.dispose(),U.domElement.remove(),b.current=null}},[s,f,p,m,d,v]),N.jsx("div",{className:"archive-scene",ref:S,role:"img","aria-label":`${v.length}分区三维档案柜数字孪生场景`})}),lx=[{title:"中建三局混凝土抗压强度检测报告",category:"建筑工程",retention:"长期"},{title:"中铁建工钢筋拉伸试验报告",category:"建筑工程",retention:"长期"},{title:"城市快速路桩基检测归档资料",category:"市政工程",retention:"永久"},{title:"中建八局水泥安定性检测报告",category:"材料检测",retention:"长期"},{title:"跨江大桥钢结构焊缝检测报告",category:"桥梁工程",retention:"永久"},{title:"轨道交通区间沉降监测月报",category:"监测工程",retention:"长期"},{title:"基坑支护结构位移监测报告",category:"基础工程",retention:"长期"},{title:"水利枢纽闸门荷载试验记录",category:"水利工程",retention:"永久"},{title:"预应力混凝土构件检验批资料",category:"构件检测",retention:"长期"},{title:"工程质量监督抽检汇总报告",category:"质量监督",retention:"永久"}],cx=15,gl=60,ux=[{url:"./demo-pdfs/concrete-strength-test-report.pdf",label:"混凝土抗压强度检测报告"},{url:"./demo-pdfs/rebar-tensile-test-report.pdf",label:"钢筋拉伸试验报告"}],Ud=[{id:"room-1",name:"1号档案室 · 主档案室",zoneIds:["A","B","C","D"],manager:{name:"李一凡",department:"信息技术部",phone:"13013013012"}},{id:"room-2",name:"2号档案室 · 工程档案室",zoneIds:["A","B"],manager:{name:"李一凡",department:"信息技术部",phone:"13013013012"}},{id:"room-3",name:"3号档案室 · 综合档案室",zoneIds:["A","B","C"],manager:{name:"李一凡",department:"信息技术部",phone:"13013013012"}}],Ap=[{id:"A",department:"材料检测部",color:"#2f8cff",soft:"#112b4d"},{id:"B",department:"监测与结构检测部",color:"#ffac21",soft:"#3b2b10"},{id:"C",department:"路桥与水利检测部",color:"#27c6d1",soft:"#10343a"},{id:"D",department:"基础检测部",color:"#9a6bff",soft:"#2b1d48"}],OR={A:[43,50,57,34,41,48,55,32],B:[46,53,30,37,44,51,28,35],C:[49,56,33,40,47,54,31,38],D:[52,29,34,39,48,57,31,40]};function PR(r,t){const i=gl-r,s=new Set;let l=(t*13+7)%gl;for(;s.size<i;)l=(l+17)%gl,s.add(l);return Array.from({length:gl},(c,f)=>f).filter(c=>!s.has(c))}function zR(r,t,i,s){const l=`${i}-${String(t+1).padStart(2,"0")}`;return PR(s,r*8+t).map((f,p)=>{const m=lx[(r*3+t*2+p)%lx.length],d=Math.floor(f/cx)+1,x=f%cx+1,v=i==="A"&&t===0&&p<ux.length?ux[p]:null;return{...m,box:x,cabinetId:l,id:`DA-2026-${i}${String(t+1).padStart(2,"0")}-${String(p+1).padStart(3,"0")}`,location:`第${d}层 · ${String(x).padStart(2,"0")}号盒`,archivedAt:`2026-${String(t%6+1).padStart(2,"0")}-${String(1+(p*3+t*2)%28).padStart(2,"0")}`,pages:64+(t*29+p*47)%236,pdf:v,position:f,shelf:d}})}const BR=Ap.flatMap(r=>OR[r.id].map((t,i)=>{const s=Ap.findIndex(l=>l.id===r.id);return{id:`${r.id}-${String(i+1).padStart(2,"0")}`,zone:r.id,capacity:gl,used:t,files:zR(s,i,r.id,t)}})),IR=[{title:"中建三局混凝土抗压强度检测报告",cabinet:"A-01",category:"建筑工程"},{title:"中铁建工钢筋拉伸试验报告",cabinet:"A-01",category:"建筑工程"}],FR=[{time:"14:31",text:"A-03 柜体使用率超过90%",type:"alert"},{time:"14:28",text:"C-02 柜体使用率超过90%",type:"alert"},{time:"14:24",text:"D-06 柜体使用率超过90%",type:"alert"},{time:"14:20",text:"环境巡检",type:"ok",status:"正常"},{time:"14:18",text:"系统巡检",type:"ok",status:"正常"}];function HR(){const[r,t]=Yt.useState(new Date(2026,6,30,14,32,8));return Yt.useEffect(()=>{const i=window.setInterval(()=>t(s=>new Date(s.getTime()+1e3)),1e3);return()=>window.clearInterval(i)},[]),r}function GR(r,t=900){const[i,s]=Yt.useState(0);return Yt.useEffect(()=>{let l;const c=performance.now(),f=p=>{const m=Math.min((p-c)/t,1),d=1-Math.pow(1-m,3);s(Math.round(r*d)),m<1&&(l=requestAnimationFrame(f))};return l=requestAnimationFrame(f),()=>cancelAnimationFrame(l)},[t,r]),i}async function VR(r){var s;if((s=navigator.clipboard)!=null&&s.writeText){await navigator.clipboard.writeText(r);return}const t=document.createElement("textarea");t.value=r,t.setAttribute("readonly",""),t.style.position="fixed",t.style.opacity="0",document.body.appendChild(t),t.select();const i=document.execCommand("copy");if(t.remove(),!i)throw new Error("Copy failed")}function Kr({icon:r,label:t,value:i,suffix:s,tone:l="blue"}){const c=GR(i);return N.jsxs("div",{className:`metric metric--${l}`,children:[N.jsx("span",{className:"metric__icon",children:N.jsx(r,{size:19,strokeWidth:1.8})}),N.jsxs("span",{className:"metric__copy",children:[N.jsx("span",{children:t}),N.jsxs("strong",{children:[c.toLocaleString(),N.jsx("small",{children:s})]})]})]})}function ey({value:r,color:t,compact:i=!1}){return N.jsx("span",{className:`capacity ${i?"capacity--compact":""}`,children:N.jsx("span",{style:{width:`${r}%`,backgroundColor:t}})})}function kR({cabinets:r,hoveredFileId:t,onFileHover:i,onFileOpen:s,onSelect:l,selectedId:c,zones:f}){const[p,m]=Yt.useState("中建三局"),[d,x]=Yt.useState("中建三局"),v=r.find(_=>_.id===c)||r[0],g=f.find(_=>_.id===v.zone),S=Math.round(v.used/v.capacity*100),b=new Set(r.map(_=>_.id)),w=d.trim()?IR.filter(_=>b.has(_.cabinet)&&_.title.includes(d.trim())):[],E=_=>{_.preventDefault(),x(p)};return N.jsxs("aside",{className:"side-rail side-rail--left","aria-label":"文件检索与柜体详情",children:[N.jsxs("section",{className:"rail-section rail-section--search",children:[N.jsxs("div",{className:"section-heading",children:[N.jsx("span",{children:"文件定位"}),N.jsx(i1,{size:15})]}),N.jsxs("form",{className:"search-form",onSubmit:E,children:[N.jsxs("label",{className:"search-input",children:[N.jsx(v1,{size:15}),N.jsx("input",{"aria-label":"输入文件或项目名称",value:p,onChange:_=>m(_.target.value)})]}),N.jsx("button",{type:"submit",children:"搜索"})]}),N.jsxs("p",{className:"result-count",children:["检索结果（",w.length," 条）"]}),N.jsxs("div",{className:"result-list",children:[w.map(_=>N.jsxs("button",{className:`result-row ${c===_.cabinet?"is-active":""}`,onClick:()=>{const z=r.find(C=>C.id===_.cabinet),F=z==null?void 0:z.files.find(C=>C.title===_.title);F!=null&&F.pdf?s(F):l(_.cabinet)},title:"定位档案盒并查看PDF演示",type:"button",children:[N.jsx("span",{className:"result-row__title",children:_.title}),N.jsxs("span",{className:"result-row__meta",children:[N.jsxs("span",{style:{color:(g==null?void 0:g.color)||"#3b8dff"},children:[N.jsx(xu,{size:12}),_.cabinet]}),N.jsx("span",{children:_.category}),N.jsx("em",{children:"在档"})]}),N.jsx(PM,{size:14})]},_.title)),!w.length&&N.jsx("div",{className:"empty-result",children:"未找到匹配档案，请更换关键词"})]})]}),N.jsxs("section",{className:"rail-section rail-section--detail",children:[N.jsxs("div",{className:"section-heading",children:[N.jsx("span",{children:"柜体详情"}),N.jsx(RM,{size:15})]}),N.jsxs("div",{className:"cabinet-heading",children:[N.jsxs("div",{children:[N.jsx("strong",{style:{color:g.color},children:v.id}),N.jsx("span",{children:g.department})]}),N.jsxs("span",{className:"online-state",children:[N.jsx("i",{}),"在线"]})]}),N.jsxs("div",{className:"detail-stats",children:[N.jsxs("div",{children:[N.jsx("span",{children:"容量"}),N.jsxs("strong",{children:[v.capacity,N.jsx("small",{children:"盒"})]})]}),N.jsxs("div",{children:[N.jsx("span",{children:"已用"}),N.jsxs("strong",{children:[v.used,N.jsx("small",{children:"盒"})]})]}),N.jsxs("div",{children:[N.jsx("span",{children:"使用率"}),N.jsxs("strong",{className:S>=85?"is-alert":"",children:[S,"%"]})]})]}),N.jsx(ey,{value:S,color:S>=85?"#ff5664":g.color}),N.jsxs("div",{className:"file-list-heading",children:[N.jsx("span",{children:"柜内文件"}),N.jsx("small",{children:"最近更新"})]}),N.jsx("div",{className:"file-list",children:v.files.slice(0,2).map(_=>N.jsxs("button",{className:t===_.id?"is-highlighted":"",onBlur:()=>i(null),onClick:()=>s(_),onFocus:()=>i(_.id),onMouseEnter:()=>i(_.id),onMouseLeave:()=>i(null),title:_.pdf?"抽出档案盒并查看PDF":"定位柜内档案盒",type:"button",children:[N.jsx("span",{className:"file-icon",children:N.jsx(dx,{size:15})}),N.jsxs("span",{children:[N.jsx("strong",{children:_.title}),N.jsxs("small",{children:[_.location," · ",_.category]})]}),N.jsx("em",{children:_.pdf?"PDF演示":"目录数据"})]},_.id))})]})]})}function XR({percent:r,used:t,total:i}){return N.jsxs("div",{className:"ring-wrap",children:[N.jsx("div",{className:"utilization-ring",style:{"--progress":`${r*3.6}deg`},children:N.jsxs("div",{children:[N.jsxs("strong",{children:[r,"%"]}),N.jsxs("span",{children:[t.toLocaleString()," / ",i.toLocaleString()]})]})}),N.jsxs("div",{className:"ring-legend",children:[N.jsxs("span",{children:[N.jsx("i",{className:"is-used"})," 已用",N.jsxs("strong",{children:[t.toLocaleString()," 盒"]})]}),N.jsxs("span",{children:[N.jsx("i",{})," 剩余",N.jsxs("strong",{children:[(i-t).toLocaleString()," 盒"]})]})]})]})}function WR(){return N.jsx("div",{className:"environment-wave","aria-label":"环境指标近30分钟趋势",children:Array.from({length:30},(r,t)=>N.jsx("i",{style:{height:`${18+Math.sin(t*.75)*9+t%4*2}px`,animationDelay:`${t*-55}ms`}},t))})}function qR({activities:r,cabinets:t,total:i,used:s,zones:l}){const c=Math.round(s/i*100);return N.jsxs("aside",{className:"side-rail side-rail--right","aria-label":"档案室统计",children:[N.jsxs("section",{className:"rail-section rail-section--usage",children:[N.jsxs("div",{className:"section-heading",children:[N.jsx("span",{children:"总体利用率"}),N.jsx(wp,{size:15})]}),N.jsx(XR,{percent:c,used:s,total:i}),N.jsx("div",{className:"zone-heading",children:"分区利用率"}),N.jsx("div",{className:"zone-bars",children:l.map(f=>{const p=t.filter(v=>v.zone===f.id),m=p.reduce((v,g)=>v+g.used,0),d=p.reduce((v,g)=>v+g.capacity,0),x=Math.round(m/d*100);return N.jsxs("div",{className:"zone-bar",children:[N.jsxs("div",{children:[N.jsxs("span",{style:{color:f.color},children:[f.id,"区 · ",f.department]}),N.jsxs("strong",{children:[x,"%"]})]}),N.jsx(ey,{value:x,color:f.color,compact:!0})]},f.id)})})]}),N.jsxs("section",{className:"rail-section rail-section--environment",children:[N.jsxs("div",{className:"section-heading",children:[N.jsx("span",{children:"环境监测"}),N.jsx(hx,{size:15})]}),N.jsxs("div",{className:"environment-grid",children:[N.jsxs("div",{children:[N.jsx(b1,{size:18}),N.jsxs("span",{children:["温度",N.jsxs("strong",{children:["22.6",N.jsx("small",{children:"°C"})]})]})]}),N.jsxs("div",{children:[N.jsx(kM,{size:18}),N.jsxs("span",{children:["湿度",N.jsxs("strong",{children:["48",N.jsx("small",{children:"%RH"})]})]})]}),N.jsxs("div",{children:[N.jsx(e1,{size:18}),N.jsxs("span",{children:["空气质量",N.jsx("strong",{className:"air-quality",children:"优"})]})]})]}),N.jsx(WR,{})]}),N.jsxs("section",{className:"rail-section rail-section--activity",children:[N.jsxs("div",{className:"section-heading",children:[N.jsx("span",{children:"近期动态"}),N.jsx(BM,{size:15})]}),N.jsx("div",{className:"activity-list",children:r.map(f=>N.jsxs("div",{className:`activity-row activity-row--${f.type}`,children:[N.jsx("i",{}),N.jsx("time",{children:f.time}),N.jsx("span",{children:f.text}),f.status&&N.jsx("em",{children:f.status})]},`${f.time}-${f.text}`))})]})]})}function YR({file:r,onClose:t}){return Yt.useEffect(()=>{const i=s=>{s.key==="Escape"&&t()};return window.addEventListener("keydown",i),()=>window.removeEventListener("keydown",i)},[t]),N.jsx("div",{className:"document-preview-backdrop",onMouseDown:i=>{i.target===i.currentTarget&&t()},role:"presentation",children:N.jsxs("section",{"aria-labelledby":"document-preview-title","aria-modal":"true",className:"document-preview",role:"dialog",children:[N.jsxs("div",{className:"archive-unfold","aria-hidden":"true",children:[N.jsx("span",{className:"archive-unfold__spine"}),N.jsx("span",{className:"archive-unfold__cover"}),N.jsx("span",{className:"archive-unfold__page"})]}),N.jsxs("header",{className:"document-preview__header",children:[N.jsxs("div",{children:[N.jsxs("span",{children:[r.cabinetId," · ",r.location]}),N.jsx("h2",{id:"document-preview-title",children:r.title})]}),N.jsx("button",{"aria-label":"关闭PDF预览",autoFocus:!0,onClick:t,title:"关闭PDF预览",type:"button",children:N.jsx(U1,{size:18})})]}),N.jsxs("div",{className:"document-preview__body",children:[N.jsxs("aside",{className:"document-preview__meta","aria-label":"档案信息",children:[N.jsxs("span",{children:["档案编号",N.jsx("strong",{children:r.id})]}),N.jsxs("span",{children:["档案类别",N.jsx("strong",{children:r.category})]}),N.jsxs("span",{children:["归档日期",N.jsx("strong",{children:r.archivedAt})]}),N.jsxs("span",{children:["保管期限",N.jsx("strong",{children:r.retention})]}),N.jsxs("span",{children:["文件页数",N.jsxs("strong",{children:[r.pages," 页"]})]})]}),N.jsxs("div",{className:"pdf-viewer-shell",children:[N.jsx("iframe",{src:`${r.pdf.url}#toolbar=0&navpanes=0&view=FitH`,title:`${r.title} PDF测试文件`}),N.jsx("span",{className:"pdf-viewer-badge",children:"测试数据"})]})]}),N.jsxs("footer",{className:"document-preview__footer",children:[N.jsxs("span",{children:[N.jsx("i",{})," PDF档案已展开"]}),N.jsxs("div",{children:[N.jsxs("a",{download:!0,href:r.pdf.url,children:[N.jsx(GM,{size:15}),"下载PDF"]}),N.jsxs("a",{href:r.pdf.url,rel:"noreferrer",target:"_blank",children:[N.jsx(WM,{size:15}),"新窗口打开"]})]})]})]})})}function jR({activeFileId:r,cabinet:t,hoveredFileId:i,onClose:s,onFileHover:l,onFileOpen:c,zone:f}){const p=Math.round(t.used/t.capacity*100),m=Yt.useRef(null);return Yt.useEffect(()=>{if(!i||!m.current)return;const d=m.current.querySelector(`[data-file-id="${i}"]`);d==null||d.scrollIntoView({block:"nearest",behavior:"smooth"})},[i]),N.jsxs("section",{className:"cabinet-inspection",style:{"--zone-color":f.color},"aria-label":`${t.id}柜体档案清单`,children:[N.jsx("div",{className:"inspection-accent"}),N.jsxs("div",{className:"inspection-heading",children:[N.jsx("button",{type:"button",onClick:s,title:"返回库区全景","aria-label":"返回库区全景",children:N.jsx(TM,{size:16})}),N.jsxs("div",{children:[N.jsxs("span",{children:["柜体近景 · ",f.id,"区"]}),N.jsx("strong",{children:t.id})]}),N.jsxs("em",{children:[N.jsx(s1,{size:13}),"柜门已解锁"]})]}),N.jsxs("div",{className:"inspection-summary",children:[N.jsxs("div",{children:[N.jsx($M,{size:16}),N.jsx("span",{children:"4 层"})]}),N.jsxs("div",{children:[N.jsx(xu,{size:16}),N.jsxs("span",{children:[t.used," / ",t.capacity," 盒"]})]}),N.jsxs("div",{children:[N.jsx(wp,{size:16}),N.jsxs("span",{children:[p,"%"]})]})]}),N.jsxs("div",{className:"inspection-list-heading",children:[N.jsx("span",{children:"柜内档案"}),N.jsxs("small",{children:["测试数据 · ",t.files.length," 份"]})]}),N.jsx("div",{className:"inspection-files",ref:m,children:t.files.map((d,x)=>N.jsxs("button",{"aria-label":`${d.title}，${d.location}${d.pdf?"，打开PDF演示":""}`,className:[i===d.id?"is-highlighted":"",r===d.id?"is-active":"",d.pdf?"has-pdf":"is-catalog-only"].filter(Boolean).join(" "),"data-file-id":d.id,onBlur:()=>l(null),onClick:()=>{d.pdf&&c(d)},onFocus:()=>l(d.id),onMouseEnter:()=>l(d.id),onMouseLeave:()=>l(null),title:d.pdf?"抽出档案盒并查看PDF":"定位柜内档案盒",type:"button",children:[N.jsx("span",{className:"inspection-file-index",children:String(x+1).padStart(2,"0")}),N.jsxs("span",{className:"inspection-file-copy",children:[N.jsx("strong",{children:d.title}),N.jsxs("small",{children:[N.jsx(YM,{size:11}),d.id]})]}),N.jsxs("span",{className:"inspection-file-meta",children:[N.jsx("span",{children:d.location}),N.jsxs("small",{children:[N.jsx(UM,{size:10}),d.archivedAt]}),N.jsx("em",{children:d.pdf?"PDF演示":"目录数据"})]})]},d.id))}),N.jsxs("div",{className:"inspection-footer",children:[N.jsxs("span",{children:[N.jsx("i",{})," 镜头已锁定当前柜体"]}),N.jsxs("span",{children:[t.files.reduce((d,x)=>d+x.pages,0)," 页"]})]})]})}function ZR({autoRotate:r,fullscreen:t,inspecting:i,onToggleFullscreen:s,onReset:l,sceneRef:c,setAutoRotate:f}){return N.jsxs("div",{className:"scene-toolbar","aria-label":"三维场景控制",children:[N.jsx("button",{className:r?"is-active":"","aria-label":i?"近景模式下暂停旋转":r?"暂停自动旋转":"开启自动旋转",disabled:i,onClick:()=>f(p=>!p),title:i?"近景模式下暂停旋转":r?"暂停自动旋转":"开启自动旋转",type:"button",children:r?N.jsx(u1,{size:15}):N.jsx(h1,{size:15})}),N.jsx("button",{"aria-label":"放大场景",disabled:i,onClick:()=>{var p;return(p=c.current)==null?void 0:p.zoomIn()},title:"放大场景",type:"button",children:N.jsx(L1,{size:15})}),N.jsx("button",{"aria-label":"缩小场景",disabled:i,onClick:()=>{var p;return(p=c.current)==null?void 0:p.zoomOut()},title:"缩小场景",type:"button",children:N.jsx(P1,{size:15})}),N.jsx("button",{"aria-label":"重置视角",onClick:l,title:"重置视角",type:"button",children:N.jsx(g1,{size:15})}),N.jsx("button",{"aria-label":t?"退出档案柜区域全屏":"档案柜区域全屏","aria-pressed":t,className:`scene-toolbar__fullscreen ${t?"is-active":""}`,onClick:s,title:t?"退出档案柜区域全屏":"档案柜区域全屏",type:"button",children:t?N.jsx(l1,{size:15}):N.jsx(px,{size:15})})]})}function KR({activeFileId:r,cabinets:t,focusId:i,hoveredFileId:s,onExitFocus:l,onFileHover:c,onFileOpen:f,onSelect:p,selectedId:m,zones:d}){const x=Yt.useRef(null),[v,g]=Yt.useState(!0),[S,b]=Yt.useState(!1),[w,E]=Yt.useState(null),_=t.find(U=>U.id===i),z=d.find(U=>U.id===(_==null?void 0:_.zone)),F=_==null?void 0:_.files.find(U=>U.id===s);Yt.useEffect(()=>{i&&(g(!1),E(null))},[i]),Yt.useEffect(()=>{const U=()=>{document.fullscreenElement||b(!1)},T=P=>{P.key==="Escape"&&b(!1)};return document.addEventListener("fullscreenchange",U),window.addEventListener("keydown",T),()=>{document.removeEventListener("fullscreenchange",U),window.removeEventListener("keydown",T)}},[]),Yt.useEffect(()=>(document.body.classList.toggle("scene-panel-expanded",S),()=>document.body.classList.remove("scene-panel-expanded")),[S]);const C=Yt.useCallback(U=>{g(!1),p(U)},[p]),B=Yt.useCallback(()=>{var U;l(),(U=x.current)==null||U.reset()},[l]),O=Yt.useCallback(async()=>{var U,T;if(S){if(b(!1),document.fullscreenElement)try{await document.exitFullscreen()}catch{}return}if(b(!0),!document.fullscreenElement&&document.fullscreenEnabled)try{await((T=(U=document.documentElement).requestFullscreen)==null?void 0:T.call(U))}catch{}},[S]);return N.jsxs("main",{className:`scene-panel ${S?"is-panel-expanded":""}`,children:[N.jsxs("div",{className:"scene-panel__header",children:[N.jsxs("div",{children:[N.jsx("span",{children:"数字孪生库区"}),N.jsx("small",{children:i?`正在查看 ${i} · 柜门已打开`:"点击柜体进入近景 · 拖拽调整视角 · 滚轮缩放"})]}),N.jsxs("div",{className:"scene-status",children:[F?N.jsxs("span",{children:["当前指向 ",F.location]}):w&&N.jsxs("span",{children:["当前指向 ",w]}),N.jsxs("span",{children:[N.jsx("i",{})," WebGL实时渲染"]})]}),N.jsx(ZR,{sceneRef:x,autoRotate:v,fullscreen:S,inspecting:!!i,onToggleFullscreen:O,onReset:B,setAutoRotate:g})]}),N.jsxs("div",{className:`scene-stage ${i?"is-inspecting":""}`,children:[N.jsx(LR,{ref:x,autoRotate:v,activeFileId:r,cabinets:t,focusId:i,hoveredFileId:s,onFileHover:c,onFileSelect:f,onHover:E,onSelect:C,selectedId:m,zones:d}),N.jsxs("div",{className:"scene-corners","aria-hidden":"true",children:[N.jsx("i",{}),N.jsx("i",{}),N.jsx("i",{}),N.jsx("i",{})]}),N.jsxs("div",{className:"scan-readout",children:[N.jsx(hx,{size:13}),i?"柜内扫描":"空间扫描",N.jsx("strong",{children:i?"已定位":"运行中"})]}),_&&N.jsx(jR,{activeFileId:r,cabinet:_,hoveredFileId:s,onClose:B,onFileHover:c,onFileOpen:f,zone:z}),N.jsxs("div",{className:"scene-legend",children:[d.map(U=>N.jsxs("span",{children:[N.jsx("i",{style:{backgroundColor:U.color}}),U.id,"区 · ",U.department]},U.id)),N.jsxs("span",{children:[N.jsx(mx,{size:13})," 高占用柜体"]})]})]})]})}function QR(){const[r,t]=Yt.useState("room-1"),[i,s]=Yt.useState("A-01"),[l,c]=Yt.useState(null),[f,p]=Yt.useState(null),[m,d]=Yt.useState(null),[x,v]=Yt.useState(null),[g,S]=Yt.useState("idle"),b=Yt.useRef(null),w=Yt.useRef([]),E=HR(),_=Yt.useMemo(()=>Ud.find(q=>q.id===r)||Ud[0],[r]),z=Yt.useMemo(()=>Ap.filter(q=>_.zoneIds.includes(q.id)),[_]),F=Yt.useMemo(()=>BR.filter(q=>_.zoneIds.includes(q.zone)),[_]),C=Yt.useMemo(()=>FR.filter(q=>q.type!=="alert"||_.zoneIds.includes(q.text.charAt(0))),[_]),B=F.reduce((q,ht)=>q+ht.capacity,0),O=F.reduce((q,ht)=>q+ht.used,0),U=Math.round(O/B*100),T=F.filter(q=>q.used/q.capacity>.93).length,P=Yt.useMemo(()=>new Intl.DateTimeFormat("zh-CN",{year:"numeric",month:"long",day:"numeric",weekday:"long"}).format(E),[E]),$=async()=>{var q,ht,Et;document.fullscreenElement?await((Et=document.exitFullscreen)==null?void 0:Et.call(document)):await((ht=(q=document.documentElement).requestFullscreen)==null?void 0:ht.call(q))},Y=Yt.useCallback(async()=>{window.clearTimeout(b.current);try{await VR(_.manager.phone),S("copied")}catch{S("failed")}b.current=window.setTimeout(()=>S("idle"),1800)},[_.manager.phone]),J=Yt.useCallback(()=>{w.current.forEach(q=>window.clearTimeout(q)),w.current=[]},[]);Yt.useEffect(()=>()=>{window.clearTimeout(b.current),J()},[J]);const dt=Yt.useCallback(q=>{J(),d(null),v(null),p(null),s(q),c(q)},[J]),gt=Yt.useCallback(()=>{J(),d(null),v(null),p(null),c(null)},[J]),et=Yt.useCallback(q=>{p(q)},[]),I=Yt.useCallback(q=>{if(p(q.id),!q.pdf)return;J();const ht=l===q.cabinetId?80:760;s(q.cabinetId),c(q.cabinetId),d(null),v(null);const Et=window.setTimeout(()=>d(q),ht),L=window.setTimeout(()=>v(q),ht+620);w.current=[Et,L]},[J,l]),H=Yt.useCallback(()=>{J(),v(null);const q=window.setTimeout(()=>{d(null),p(null)},220);w.current=[q]},[J]);return N.jsxs("div",{className:"dashboard-shell",children:[N.jsx("div",{className:"ambient-grid","aria-hidden":"true"}),N.jsxs("header",{className:"command-header",children:[N.jsxs("div",{className:"brand-block",children:[N.jsx("div",{className:"brand-mark",children:N.jsx(xu,{size:25})}),N.jsxs("div",{children:[N.jsx("h1",{children:"档案室管理大屏"}),N.jsxs("p",{children:[_.name," · 信息总部"]})]})]}),N.jsxs("div",{className:"top-metrics",children:[N.jsx(Kr,{icon:QM,label:"分区",value:z.length,suffix:"个"}),N.jsx(Kr,{icon:CM,label:"柜体",value:F.length,suffix:"个",tone:"cyan"}),N.jsx(Kr,{icon:xu,label:"总容量",value:B,suffix:"盒",tone:"cyan"}),N.jsx(Kr,{icon:dx,label:"已用",value:O,suffix:"盒",tone:"amber"}),N.jsx(Kr,{icon:wp,label:"利用率",value:U,suffix:"%",tone:"amber"}),N.jsx(Kr,{icon:mx,label:"异常",value:T,suffix:"",tone:"coral"})]}),N.jsxs("div",{className:"system-tools",children:[N.jsxs("div",{className:"system-state",children:[N.jsxs("span",{children:[N.jsx("i",{})," 系统在线"]}),N.jsx("strong",{children:E.toLocaleTimeString("zh-CN",{hour12:!1})}),N.jsx("small",{children:P})]}),N.jsx("button",{type:"button",title:"进入全屏","aria-label":"进入全屏",onClick:$,children:N.jsx(px,{size:17})}),N.jsx("button",{type:"button",title:"系统设置","aria-label":"系统设置",children:N.jsx(y1,{size:17})})]})]}),N.jsxs("nav",{className:"room-nav","aria-label":"档案室切换",children:[N.jsx("span",{children:"切换档案室"}),Ud.map(q=>N.jsx("button",{className:r===q.id?"is-active":"",onClick:()=>{J(),t(q.id),s(`${q.zoneIds[0]}-01`),c(null),p(null),d(null),v(null)},type:"button",children:q.name},q.id)),N.jsxs("div",{className:"manager-info","aria-label":"档案室管理员信息",children:[N.jsxs("span",{className:"manager-info__role",children:[N.jsx(R1,{size:13}),"档案室管理员"]}),N.jsx("strong",{children:_.manager.name}),N.jsx("span",{children:_.manager.department}),N.jsxs("button",{"aria-label":`复制管理员电话 ${_.manager.phone}`,className:`manager-info__copy ${g==="idle"?"":`is-${g}`}`,onClick:Y,title:g==="copied"?"电话已复制":g==="failed"?"复制失败":"复制管理员电话",type:"button",children:[g==="copied"?N.jsx(LM,{size:12}):N.jsx(FM,{size:12}),N.jsx("span",{"aria-live":"polite",children:g==="copied"?"已复制":g==="failed"?"复制失败":_.manager.phone})]})]}),N.jsxs("div",{className:"security-note",children:[N.jsx(M1,{size:14}),"今日巡检已完成"]})]}),N.jsxs("div",{className:"dashboard-grid",children:[N.jsx(kR,{cabinets:F,hoveredFileId:f,onFileHover:et,onFileOpen:I,selectedId:i,onSelect:dt,zones:z}),N.jsx(KR,{activeFileId:(m==null?void 0:m.id)||null,cabinets:F,focusId:l,hoveredFileId:f,onExitFocus:gt,onFileHover:et,onFileOpen:I,onSelect:dt,selectedId:i,zones:z},r),N.jsx(qR,{activities:C,cabinets:F,used:O,total:B,zones:z})]}),N.jsxs("footer",{className:"status-footer",children:[N.jsxs("span",{children:[N.jsx("i",{})," 档案库环境稳定"]}),N.jsx("span",{children:"数据更新时间 14:32:08"}),N.jsxs("span",{children:[N.jsx(C1,{size:12})," 温湿度传感器 8 / 8 在线"]}),N.jsxs("span",{children:[N.jsx(p1,{size:12})," 数字孪生引擎 v3.2"]})]}),x?N.jsx(YR,{file:x,onClose:H}):null]})}mM.createRoot(document.getElementById("root")).render(N.jsx(lM.StrictMode,{children:N.jsx(QR,{})}));

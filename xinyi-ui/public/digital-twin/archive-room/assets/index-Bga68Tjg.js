(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const l of document.querySelectorAll('link[rel="modulepreload"]'))s(l);new MutationObserver(l=>{for(const c of l)if(c.type==="childList")for(const f of c.addedNodes)f.tagName==="LINK"&&f.rel==="modulepreload"&&s(f)}).observe(document,{childList:!0,subtree:!0});function i(l){const c={};return l.integrity&&(c.integrity=l.integrity),l.referrerPolicy&&(c.referrerPolicy=l.referrerPolicy),l.crossOrigin==="use-credentials"?c.credentials="include":l.crossOrigin==="anonymous"?c.credentials="omit":c.credentials="same-origin",c}function s(l){if(l.ep)return;l.ep=!0;const c=i(l);fetch(l.href,c)}})();function ZS(r){return r&&r.__esModule&&Object.prototype.hasOwnProperty.call(r,"default")?r.default:r}var Bh={exports:{}},$o={};/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var U_;function jS(){if(U_)return $o;U_=1;var r=Symbol.for("react.transitional.element"),t=Symbol.for("react.fragment");function i(s,l,c){var f=null;if(c!==void 0&&(f=""+c),l.key!==void 0&&(f=""+l.key),"key"in l){c={};for(var p in l)p!=="key"&&(c[p]=l[p])}else c=l;return l=c.ref,{$$typeof:r,type:s,key:f,ref:l!==void 0?l:null,props:c}}return $o.Fragment=t,$o.jsx=i,$o.jsxs=i,$o}var N_;function KS(){return N_||(N_=1,Bh.exports=jS()),Bh.exports}var z=KS(),Ih={exports:{}},he={};/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var L_;function QS(){if(L_)return he;L_=1;var r=Symbol.for("react.transitional.element"),t=Symbol.for("react.portal"),i=Symbol.for("react.fragment"),s=Symbol.for("react.strict_mode"),l=Symbol.for("react.profiler"),c=Symbol.for("react.consumer"),f=Symbol.for("react.context"),p=Symbol.for("react.forward_ref"),m=Symbol.for("react.suspense"),d=Symbol.for("react.memo"),_=Symbol.for("react.lazy"),v=Symbol.for("react.activity"),g=Symbol.iterator;function S(N){return N===null||typeof N!="object"?null:(N=g&&N[g]||N["@@iterator"],typeof N=="function"?N:null)}var E={isMounted:function(){return!1},enqueueForceUpdate:function(){},enqueueReplaceState:function(){},enqueueSetState:function(){}},A=Object.assign,M={};function y(N,W,vt){this.props=N,this.context=W,this.refs=M,this.updater=vt||E}y.prototype.isReactComponent={},y.prototype.setState=function(N,W){if(typeof N!="object"&&typeof N!="function"&&N!=null)throw Error("takes an object of state variables to update or a function which returns an object of state variables.");this.updater.enqueueSetState(this,N,W,"setState")},y.prototype.forceUpdate=function(N){this.updater.enqueueForceUpdate(this,N,"forceUpdate")};function B(){}B.prototype=y.prototype;function F(N,W,vt){this.props=N,this.context=W,this.refs=M,this.updater=vt||E}var w=F.prototype=new B;w.constructor=F,A(w,y.prototype),w.isPureReactComponent=!0;var I=Array.isArray;function O(){}var D={H:null,A:null,T:null,S:null},T=Object.prototype.hasOwnProperty;function L(N,W,vt){var Tt=vt.ref;return{$$typeof:r,type:N,key:W,ref:Tt!==void 0?Tt:null,props:vt}}function j(N,W){return L(N.type,W,N.props)}function k(N){return typeof N=="object"&&N!==null&&N.$$typeof===r}function J(N){var W={"=":"=0",":":"=2"};return"$"+N.replace(/[=:]/g,function(vt){return W[vt]})}var ut=/\/+/g;function gt(N,W){return typeof N=="object"&&N!==null&&N.key!=null?J(""+N.key):W.toString(36)}function $(N){switch(N.status){case"fulfilled":return N.value;case"rejected":throw N.reason;default:switch(typeof N.status=="string"?N.then(O,O):(N.status="pending",N.then(function(W){N.status==="pending"&&(N.status="fulfilled",N.value=W)},function(W){N.status==="pending"&&(N.status="rejected",N.reason=W)})),N.status){case"fulfilled":return N.value;case"rejected":throw N.reason}}throw N}function P(N,W,vt,Tt,At){var tt=typeof N;(tt==="undefined"||tt==="boolean")&&(N=null);var yt=!1;if(N===null)yt=!0;else switch(tt){case"bigint":case"string":case"number":yt=!0;break;case"object":switch(N.$$typeof){case r:case t:yt=!0;break;case _:return yt=N._init,P(yt(N._payload),W,vt,Tt,At)}}if(yt)return At=At(N),yt=Tt===""?"."+gt(N,0):Tt,I(At)?(vt="",yt!=null&&(vt=yt.replace(ut,"$&/")+"/"),P(At,W,vt,"",function(ae){return ae})):At!=null&&(k(At)&&(At=j(At,vt+(At.key==null||N&&N.key===At.key?"":(""+At.key).replace(ut,"$&/")+"/")+yt)),W.push(At)),1;yt=0;var Mt=Tt===""?".":Tt+":";if(I(N))for(var Nt=0;Nt<N.length;Nt++)Tt=N[Nt],tt=Mt+gt(Tt,Nt),yt+=P(Tt,W,vt,tt,At);else if(Nt=S(N),typeof Nt=="function")for(N=Nt.call(N),Nt=0;!(Tt=N.next()).done;)Tt=Tt.value,tt=Mt+gt(Tt,Nt++),yt+=P(Tt,W,vt,tt,At);else if(tt==="object"){if(typeof N.then=="function")return P($(N),W,vt,Tt,At);throw W=String(N),Error("Objects are not valid as a React child (found: "+(W==="[object Object]"?"object with keys {"+Object.keys(N).join(", ")+"}":W)+"). If you meant to render a collection of children, use an array instead.")}return yt}function H(N,W,vt){if(N==null)return N;var Tt=[],At=0;return P(N,Tt,"","",function(tt){return W.call(vt,tt,At++)}),Tt}function et(N){if(N._status===-1){var W=N._result;W=W(),W.then(function(vt){(N._status===0||N._status===-1)&&(N._status=1,N._result=vt)},function(vt){(N._status===0||N._status===-1)&&(N._status=2,N._result=vt)}),N._status===-1&&(N._status=0,N._result=W)}if(N._status===1)return N._result.default;throw N._result}var pt=typeof reportError=="function"?reportError:function(N){if(typeof window=="object"&&typeof window.ErrorEvent=="function"){var W=new window.ErrorEvent("error",{bubbles:!0,cancelable:!0,message:typeof N=="object"&&N!==null&&typeof N.message=="string"?String(N.message):String(N),error:N});if(!window.dispatchEvent(W))return}else if(typeof process=="object"&&typeof process.emit=="function"){process.emit("uncaughtException",N);return}console.error(N)},Et={map:H,forEach:function(N,W,vt){H(N,function(){W.apply(this,arguments)},vt)},count:function(N){var W=0;return H(N,function(){W++}),W},toArray:function(N){return H(N,function(W){return W})||[]},only:function(N){if(!k(N))throw Error("React.Children.only expected to receive a single React element child.");return N}};return he.Activity=v,he.Children=Et,he.Component=y,he.Fragment=i,he.Profiler=l,he.PureComponent=F,he.StrictMode=s,he.Suspense=m,he.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE=D,he.__COMPILER_RUNTIME={__proto__:null,c:function(N){return D.H.useMemoCache(N)}},he.cache=function(N){return function(){return N.apply(null,arguments)}},he.cacheSignal=function(){return null},he.cloneElement=function(N,W,vt){if(N==null)throw Error("The argument must be a React element, but you passed "+N+".");var Tt=A({},N.props),At=N.key;if(W!=null)for(tt in W.key!==void 0&&(At=""+W.key),W)!T.call(W,tt)||tt==="key"||tt==="__self"||tt==="__source"||tt==="ref"&&W.ref===void 0||(Tt[tt]=W[tt]);var tt=arguments.length-2;if(tt===1)Tt.children=vt;else if(1<tt){for(var yt=Array(tt),Mt=0;Mt<tt;Mt++)yt[Mt]=arguments[Mt+2];Tt.children=yt}return L(N.type,At,Tt)},he.createContext=function(N){return N={$$typeof:f,_currentValue:N,_currentValue2:N,_threadCount:0,Provider:null,Consumer:null},N.Provider=N,N.Consumer={$$typeof:c,_context:N},N},he.createElement=function(N,W,vt){var Tt,At={},tt=null;if(W!=null)for(Tt in W.key!==void 0&&(tt=""+W.key),W)T.call(W,Tt)&&Tt!=="key"&&Tt!=="__self"&&Tt!=="__source"&&(At[Tt]=W[Tt]);var yt=arguments.length-2;if(yt===1)At.children=vt;else if(1<yt){for(var Mt=Array(yt),Nt=0;Nt<yt;Nt++)Mt[Nt]=arguments[Nt+2];At.children=Mt}if(N&&N.defaultProps)for(Tt in yt=N.defaultProps,yt)At[Tt]===void 0&&(At[Tt]=yt[Tt]);return L(N,tt,At)},he.createRef=function(){return{current:null}},he.forwardRef=function(N){return{$$typeof:p,render:N}},he.isValidElement=k,he.lazy=function(N){return{$$typeof:_,_payload:{_status:-1,_result:N},_init:et}},he.memo=function(N,W){return{$$typeof:d,type:N,compare:W===void 0?null:W}},he.startTransition=function(N){var W=D.T,vt={};D.T=vt;try{var Tt=N(),At=D.S;At!==null&&At(vt,Tt),typeof Tt=="object"&&Tt!==null&&typeof Tt.then=="function"&&Tt.then(O,pt)}catch(tt){pt(tt)}finally{W!==null&&vt.types!==null&&(W.types=vt.types),D.T=W}},he.unstable_useCacheRefresh=function(){return D.H.useCacheRefresh()},he.use=function(N){return D.H.use(N)},he.useActionState=function(N,W,vt){return D.H.useActionState(N,W,vt)},he.useCallback=function(N,W){return D.H.useCallback(N,W)},he.useContext=function(N){return D.H.useContext(N)},he.useDebugValue=function(){},he.useDeferredValue=function(N,W){return D.H.useDeferredValue(N,W)},he.useEffect=function(N,W){return D.H.useEffect(N,W)},he.useEffectEvent=function(N){return D.H.useEffectEvent(N)},he.useId=function(){return D.H.useId()},he.useImperativeHandle=function(N,W,vt){return D.H.useImperativeHandle(N,W,vt)},he.useInsertionEffect=function(N,W){return D.H.useInsertionEffect(N,W)},he.useLayoutEffect=function(N,W){return D.H.useLayoutEffect(N,W)},he.useMemo=function(N,W){return D.H.useMemo(N,W)},he.useOptimistic=function(N,W){return D.H.useOptimistic(N,W)},he.useReducer=function(N,W,vt){return D.H.useReducer(N,W,vt)},he.useRef=function(N){return D.H.useRef(N)},he.useState=function(N){return D.H.useState(N)},he.useSyncExternalStore=function(N,W,vt){return D.H.useSyncExternalStore(N,W,vt)},he.useTransition=function(){return D.H.useTransition()},he.version="19.2.0",he}var O_;function bp(){return O_||(O_=1,Ih.exports=QS()),Ih.exports}var ce=bp();const JS=ZS(ce);var Fh={exports:{}},tl={},Hh={exports:{}},Gh={};/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var P_;function $S(){return P_||(P_=1,(function(r){function t(P,H){var et=P.length;P.push(H);t:for(;0<et;){var pt=et-1>>>1,Et=P[pt];if(0<l(Et,H))P[pt]=H,P[et]=Et,et=pt;else break t}}function i(P){return P.length===0?null:P[0]}function s(P){if(P.length===0)return null;var H=P[0],et=P.pop();if(et!==H){P[0]=et;t:for(var pt=0,Et=P.length,N=Et>>>1;pt<N;){var W=2*(pt+1)-1,vt=P[W],Tt=W+1,At=P[Tt];if(0>l(vt,et))Tt<Et&&0>l(At,vt)?(P[pt]=At,P[Tt]=et,pt=Tt):(P[pt]=vt,P[W]=et,pt=W);else if(Tt<Et&&0>l(At,et))P[pt]=At,P[Tt]=et,pt=Tt;else break t}}return H}function l(P,H){var et=P.sortIndex-H.sortIndex;return et!==0?et:P.id-H.id}if(r.unstable_now=void 0,typeof performance=="object"&&typeof performance.now=="function"){var c=performance;r.unstable_now=function(){return c.now()}}else{var f=Date,p=f.now();r.unstable_now=function(){return f.now()-p}}var m=[],d=[],_=1,v=null,g=3,S=!1,E=!1,A=!1,M=!1,y=typeof setTimeout=="function"?setTimeout:null,B=typeof clearTimeout=="function"?clearTimeout:null,F=typeof setImmediate<"u"?setImmediate:null;function w(P){for(var H=i(d);H!==null;){if(H.callback===null)s(d);else if(H.startTime<=P)s(d),H.sortIndex=H.expirationTime,t(m,H);else break;H=i(d)}}function I(P){if(A=!1,w(P),!E)if(i(m)!==null)E=!0,O||(O=!0,J());else{var H=i(d);H!==null&&$(I,H.startTime-P)}}var O=!1,D=-1,T=5,L=-1;function j(){return M?!0:!(r.unstable_now()-L<T)}function k(){if(M=!1,O){var P=r.unstable_now();L=P;var H=!0;try{t:{E=!1,A&&(A=!1,B(D),D=-1),S=!0;var et=g;try{e:{for(w(P),v=i(m);v!==null&&!(v.expirationTime>P&&j());){var pt=v.callback;if(typeof pt=="function"){v.callback=null,g=v.priorityLevel;var Et=pt(v.expirationTime<=P);if(P=r.unstable_now(),typeof Et=="function"){v.callback=Et,w(P),H=!0;break e}v===i(m)&&s(m),w(P)}else s(m);v=i(m)}if(v!==null)H=!0;else{var N=i(d);N!==null&&$(I,N.startTime-P),H=!1}}break t}finally{v=null,g=et,S=!1}H=void 0}}finally{H?J():O=!1}}}var J;if(typeof F=="function")J=function(){F(k)};else if(typeof MessageChannel<"u"){var ut=new MessageChannel,gt=ut.port2;ut.port1.onmessage=k,J=function(){gt.postMessage(null)}}else J=function(){y(k,0)};function $(P,H){D=y(function(){P(r.unstable_now())},H)}r.unstable_IdlePriority=5,r.unstable_ImmediatePriority=1,r.unstable_LowPriority=4,r.unstable_NormalPriority=3,r.unstable_Profiling=null,r.unstable_UserBlockingPriority=2,r.unstable_cancelCallback=function(P){P.callback=null},r.unstable_forceFrameRate=function(P){0>P||125<P?console.error("forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"):T=0<P?Math.floor(1e3/P):5},r.unstable_getCurrentPriorityLevel=function(){return g},r.unstable_next=function(P){switch(g){case 1:case 2:case 3:var H=3;break;default:H=g}var et=g;g=H;try{return P()}finally{g=et}},r.unstable_requestPaint=function(){M=!0},r.unstable_runWithPriority=function(P,H){switch(P){case 1:case 2:case 3:case 4:case 5:break;default:P=3}var et=g;g=P;try{return H()}finally{g=et}},r.unstable_scheduleCallback=function(P,H,et){var pt=r.unstable_now();switch(typeof et=="object"&&et!==null?(et=et.delay,et=typeof et=="number"&&0<et?pt+et:pt):et=pt,P){case 1:var Et=-1;break;case 2:Et=250;break;case 5:Et=1073741823;break;case 4:Et=1e4;break;default:Et=5e3}return Et=et+Et,P={id:_++,callback:H,priorityLevel:P,startTime:et,expirationTime:Et,sortIndex:-1},et>pt?(P.sortIndex=et,t(d,P),i(m)===null&&P===i(d)&&(A?(B(D),D=-1):A=!0,$(I,et-pt))):(P.sortIndex=Et,t(m,P),E||S||(E=!0,O||(O=!0,J()))),P},r.unstable_shouldYield=j,r.unstable_wrapCallback=function(P){var H=g;return function(){var et=g;g=H;try{return P.apply(this,arguments)}finally{g=et}}}})(Gh)),Gh}var z_;function tM(){return z_||(z_=1,Hh.exports=$S()),Hh.exports}var Vh={exports:{}},In={};/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var B_;function eM(){if(B_)return In;B_=1;var r=bp();function t(m){var d="https://react.dev/errors/"+m;if(1<arguments.length){d+="?args[]="+encodeURIComponent(arguments[1]);for(var _=2;_<arguments.length;_++)d+="&args[]="+encodeURIComponent(arguments[_])}return"Minified React error #"+m+"; visit "+d+" for the full message or use the non-minified dev environment for full errors and additional helpful warnings."}function i(){}var s={d:{f:i,r:function(){throw Error(t(522))},D:i,C:i,L:i,m:i,X:i,S:i,M:i},p:0,findDOMNode:null},l=Symbol.for("react.portal");function c(m,d,_){var v=3<arguments.length&&arguments[3]!==void 0?arguments[3]:null;return{$$typeof:l,key:v==null?null:""+v,children:m,containerInfo:d,implementation:_}}var f=r.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;function p(m,d){if(m==="font")return"";if(typeof d=="string")return d==="use-credentials"?d:""}return In.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE=s,In.createPortal=function(m,d){var _=2<arguments.length&&arguments[2]!==void 0?arguments[2]:null;if(!d||d.nodeType!==1&&d.nodeType!==9&&d.nodeType!==11)throw Error(t(299));return c(m,d,null,_)},In.flushSync=function(m){var d=f.T,_=s.p;try{if(f.T=null,s.p=2,m)return m()}finally{f.T=d,s.p=_,s.d.f()}},In.preconnect=function(m,d){typeof m=="string"&&(d?(d=d.crossOrigin,d=typeof d=="string"?d==="use-credentials"?d:"":void 0):d=null,s.d.C(m,d))},In.prefetchDNS=function(m){typeof m=="string"&&s.d.D(m)},In.preinit=function(m,d){if(typeof m=="string"&&d&&typeof d.as=="string"){var _=d.as,v=p(_,d.crossOrigin),g=typeof d.integrity=="string"?d.integrity:void 0,S=typeof d.fetchPriority=="string"?d.fetchPriority:void 0;_==="style"?s.d.S(m,typeof d.precedence=="string"?d.precedence:void 0,{crossOrigin:v,integrity:g,fetchPriority:S}):_==="script"&&s.d.X(m,{crossOrigin:v,integrity:g,fetchPriority:S,nonce:typeof d.nonce=="string"?d.nonce:void 0})}},In.preinitModule=function(m,d){if(typeof m=="string")if(typeof d=="object"&&d!==null){if(d.as==null||d.as==="script"){var _=p(d.as,d.crossOrigin);s.d.M(m,{crossOrigin:_,integrity:typeof d.integrity=="string"?d.integrity:void 0,nonce:typeof d.nonce=="string"?d.nonce:void 0})}}else d==null&&s.d.M(m)},In.preload=function(m,d){if(typeof m=="string"&&typeof d=="object"&&d!==null&&typeof d.as=="string"){var _=d.as,v=p(_,d.crossOrigin);s.d.L(m,_,{crossOrigin:v,integrity:typeof d.integrity=="string"?d.integrity:void 0,nonce:typeof d.nonce=="string"?d.nonce:void 0,type:typeof d.type=="string"?d.type:void 0,fetchPriority:typeof d.fetchPriority=="string"?d.fetchPriority:void 0,referrerPolicy:typeof d.referrerPolicy=="string"?d.referrerPolicy:void 0,imageSrcSet:typeof d.imageSrcSet=="string"?d.imageSrcSet:void 0,imageSizes:typeof d.imageSizes=="string"?d.imageSizes:void 0,media:typeof d.media=="string"?d.media:void 0})}},In.preloadModule=function(m,d){if(typeof m=="string")if(d){var _=p(d.as,d.crossOrigin);s.d.m(m,{as:typeof d.as=="string"&&d.as!=="script"?d.as:void 0,crossOrigin:_,integrity:typeof d.integrity=="string"?d.integrity:void 0})}else s.d.m(m)},In.requestFormReset=function(m){s.d.r(m)},In.unstable_batchedUpdates=function(m,d){return m(d)},In.useFormState=function(m,d,_){return f.H.useFormState(m,d,_)},In.useFormStatus=function(){return f.H.useHostTransitionStatus()},In.version="19.2.0",In}var I_;function nM(){if(I_)return Vh.exports;I_=1;function r(){if(!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__>"u"||typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE!="function"))try{__REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(r)}catch(t){console.error(t)}}return r(),Vh.exports=eM(),Vh.exports}/**
 * @license React
 * react-dom-client.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var F_;function iM(){if(F_)return tl;F_=1;var r=tM(),t=bp(),i=nM();function s(e){var n="https://react.dev/errors/"+e;if(1<arguments.length){n+="?args[]="+encodeURIComponent(arguments[1]);for(var a=2;a<arguments.length;a++)n+="&args[]="+encodeURIComponent(arguments[a])}return"Minified React error #"+e+"; visit "+n+" for the full message or use the non-minified dev environment for full errors and additional helpful warnings."}function l(e){return!(!e||e.nodeType!==1&&e.nodeType!==9&&e.nodeType!==11)}function c(e){var n=e,a=e;if(e.alternate)for(;n.return;)n=n.return;else{e=n;do n=e,(n.flags&4098)!==0&&(a=n.return),e=n.return;while(e)}return n.tag===3?a:null}function f(e){if(e.tag===13){var n=e.memoizedState;if(n===null&&(e=e.alternate,e!==null&&(n=e.memoizedState)),n!==null)return n.dehydrated}return null}function p(e){if(e.tag===31){var n=e.memoizedState;if(n===null&&(e=e.alternate,e!==null&&(n=e.memoizedState)),n!==null)return n.dehydrated}return null}function m(e){if(c(e)!==e)throw Error(s(188))}function d(e){var n=e.alternate;if(!n){if(n=c(e),n===null)throw Error(s(188));return n!==e?null:e}for(var a=e,o=n;;){var u=a.return;if(u===null)break;var h=u.alternate;if(h===null){if(o=u.return,o!==null){a=o;continue}break}if(u.child===h.child){for(h=u.child;h;){if(h===a)return m(u),e;if(h===o)return m(u),n;h=h.sibling}throw Error(s(188))}if(a.return!==o.return)a=u,o=h;else{for(var x=!1,C=u.child;C;){if(C===a){x=!0,a=u,o=h;break}if(C===o){x=!0,o=u,a=h;break}C=C.sibling}if(!x){for(C=h.child;C;){if(C===a){x=!0,a=h,o=u;break}if(C===o){x=!0,o=h,a=u;break}C=C.sibling}if(!x)throw Error(s(189))}}if(a.alternate!==o)throw Error(s(190))}if(a.tag!==3)throw Error(s(188));return a.stateNode.current===a?e:n}function _(e){var n=e.tag;if(n===5||n===26||n===27||n===6)return e;for(e=e.child;e!==null;){if(n=_(e),n!==null)return n;e=e.sibling}return null}var v=Object.assign,g=Symbol.for("react.element"),S=Symbol.for("react.transitional.element"),E=Symbol.for("react.portal"),A=Symbol.for("react.fragment"),M=Symbol.for("react.strict_mode"),y=Symbol.for("react.profiler"),B=Symbol.for("react.consumer"),F=Symbol.for("react.context"),w=Symbol.for("react.forward_ref"),I=Symbol.for("react.suspense"),O=Symbol.for("react.suspense_list"),D=Symbol.for("react.memo"),T=Symbol.for("react.lazy"),L=Symbol.for("react.activity"),j=Symbol.for("react.memo_cache_sentinel"),k=Symbol.iterator;function J(e){return e===null||typeof e!="object"?null:(e=k&&e[k]||e["@@iterator"],typeof e=="function"?e:null)}var ut=Symbol.for("react.client.reference");function gt(e){if(e==null)return null;if(typeof e=="function")return e.$$typeof===ut?null:e.displayName||e.name||null;if(typeof e=="string")return e;switch(e){case A:return"Fragment";case y:return"Profiler";case M:return"StrictMode";case I:return"Suspense";case O:return"SuspenseList";case L:return"Activity"}if(typeof e=="object")switch(e.$$typeof){case E:return"Portal";case F:return e.displayName||"Context";case B:return(e._context.displayName||"Context")+".Consumer";case w:var n=e.render;return e=e.displayName,e||(e=n.displayName||n.name||"",e=e!==""?"ForwardRef("+e+")":"ForwardRef"),e;case D:return n=e.displayName||null,n!==null?n:gt(e.type)||"Memo";case T:n=e._payload,e=e._init;try{return gt(e(n))}catch{}}return null}var $=Array.isArray,P=t.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,H=i.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,et={pending:!1,data:null,method:null,action:null},pt=[],Et=-1;function N(e){return{current:e}}function W(e){0>Et||(e.current=pt[Et],pt[Et]=null,Et--)}function vt(e,n){Et++,pt[Et]=e.current,e.current=n}var Tt=N(null),At=N(null),tt=N(null),yt=N(null);function Mt(e,n){switch(vt(tt,n),vt(At,e),vt(Tt,null),n.nodeType){case 9:case 11:e=(e=n.documentElement)&&(e=e.namespaceURI)?t_(e):0;break;default:if(e=n.tagName,n=n.namespaceURI)n=t_(n),e=e_(n,e);else switch(e){case"svg":e=1;break;case"math":e=2;break;default:e=0}}W(Tt),vt(Tt,e)}function Nt(){W(Tt),W(At),W(tt)}function ae(e){e.memoizedState!==null&&vt(yt,e);var n=Tt.current,a=e_(n,e.type);n!==a&&(vt(At,e),vt(Tt,a))}function Qt(e){At.current===e&&(W(Tt),W(At)),yt.current===e&&(W(yt),jo._currentValue=et)}var He,pe;function Se(e){if(He===void 0)try{throw Error()}catch(a){var n=a.stack.trim().match(/\n( *(at )?)/);He=n&&n[1]||"",pe=-1<a.stack.indexOf(`
    at`)?" (<anonymous>)":-1<a.stack.indexOf("@")?"@unknown:0:0":""}return`
`+He+e+pe}var Me=!1;function Ut(e,n){if(!e||Me)return"";Me=!0;var a=Error.prepareStackTrace;Error.prepareStackTrace=void 0;try{var o={DetermineComponentFrameRoot:function(){try{if(n){var St=function(){throw Error()};if(Object.defineProperty(St.prototype,"props",{set:function(){throw Error()}}),typeof Reflect=="object"&&Reflect.construct){try{Reflect.construct(St,[])}catch(ct){var lt=ct}Reflect.construct(e,[],St)}else{try{St.call()}catch(ct){lt=ct}e.call(St.prototype)}}else{try{throw Error()}catch(ct){lt=ct}(St=e())&&typeof St.catch=="function"&&St.catch(function(){})}}catch(ct){if(ct&&lt&&typeof ct.stack=="string")return[ct.stack,lt.stack]}return[null,null]}};o.DetermineComponentFrameRoot.displayName="DetermineComponentFrameRoot";var u=Object.getOwnPropertyDescriptor(o.DetermineComponentFrameRoot,"name");u&&u.configurable&&Object.defineProperty(o.DetermineComponentFrameRoot,"name",{value:"DetermineComponentFrameRoot"});var h=o.DetermineComponentFrameRoot(),x=h[0],C=h[1];if(x&&C){var G=x.split(`
`),it=C.split(`
`);for(u=o=0;o<G.length&&!G[o].includes("DetermineComponentFrameRoot");)o++;for(;u<it.length&&!it[u].includes("DetermineComponentFrameRoot");)u++;if(o===G.length||u===it.length)for(o=G.length-1,u=it.length-1;1<=o&&0<=u&&G[o]!==it[u];)u--;for(;1<=o&&0<=u;o--,u--)if(G[o]!==it[u]){if(o!==1||u!==1)do if(o--,u--,0>u||G[o]!==it[u]){var mt=`
`+G[o].replace(" at new "," at ");return e.displayName&&mt.includes("<anonymous>")&&(mt=mt.replace("<anonymous>",e.displayName)),mt}while(1<=o&&0<=u);break}}}finally{Me=!1,Error.prepareStackTrace=a}return(a=e?e.displayName||e.name:"")?Se(a):""}function Zt(e,n){switch(e.tag){case 26:case 27:case 5:return Se(e.type);case 16:return Se("Lazy");case 13:return e.child!==n&&n!==null?Se("Suspense Fallback"):Se("Suspense");case 19:return Se("SuspenseList");case 0:case 15:return Ut(e.type,!1);case 11:return Ut(e.type.render,!1);case 1:return Ut(e.type,!0);case 31:return Se("Activity");default:return""}}function $t(e){try{var n="",a=null;do n+=Zt(e,a),a=e,e=e.return;while(e);return n}catch(o){return`
Error generating stack: `+o.message+`
`+o.stack}}var oe=Object.prototype.hasOwnProperty,Vt=r.unstable_scheduleCallback,Ce=r.unstable_cancelCallback,me=r.unstable_shouldYield,Y=r.unstable_requestPaint,we=r.unstable_now,Le=r.unstable_getCurrentPriorityLevel,U=r.unstable_ImmediatePriority,b=r.unstable_UserBlockingPriority,Q=r.unstable_NormalPriority,rt=r.unstable_LowPriority,ht=r.unstable_IdlePriority,Rt=r.log,Lt=r.unstable_setDisableYieldValue,ft=null,dt=null;function wt(e){if(typeof Rt=="function"&&Lt(e),dt&&typeof dt.setStrictMode=="function")try{dt.setStrictMode(ft,e)}catch{}}var Ht=Math.clz32?Math.clz32:te,zt=Math.log,Ot=Math.LN2;function te(e){return e>>>=0,e===0?32:31-(zt(e)/Ot|0)|0}var ee=256,le=262144,q=4194304;function Ct(e){var n=e&42;if(n!==0)return n;switch(e&-e){case 1:return 1;case 2:return 2;case 4:return 4;case 8:return 8;case 16:return 16;case 32:return 32;case 64:return 64;case 128:return 128;case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:return e&261888;case 262144:case 524288:case 1048576:case 2097152:return e&3932160;case 4194304:case 8388608:case 16777216:case 33554432:return e&62914560;case 67108864:return 67108864;case 134217728:return 134217728;case 268435456:return 268435456;case 536870912:return 536870912;case 1073741824:return 0;default:return e}}function _t(e,n,a){var o=e.pendingLanes;if(o===0)return 0;var u=0,h=e.suspendedLanes,x=e.pingedLanes;e=e.warmLanes;var C=o&134217727;return C!==0?(o=C&~h,o!==0?u=Ct(o):(x&=C,x!==0?u=Ct(x):a||(a=C&~e,a!==0&&(u=Ct(a))))):(C=o&~h,C!==0?u=Ct(C):x!==0?u=Ct(x):a||(a=o&~e,a!==0&&(u=Ct(a)))),u===0?0:n!==0&&n!==u&&(n&h)===0&&(h=u&-u,a=n&-n,h>=a||h===32&&(a&4194048)!==0)?n:u}function Dt(e,n){return(e.pendingLanes&~(e.suspendedLanes&~e.pingedLanes)&n)===0}function Ft(e,n){switch(e){case 1:case 2:case 4:case 8:case 64:return n+250;case 16:case 32:case 128:case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:case 262144:case 524288:case 1048576:case 2097152:return n+5e3;case 4194304:case 8388608:case 16777216:case 33554432:return-1;case 67108864:case 134217728:case 268435456:case 536870912:case 1073741824:return-1;default:return-1}}function bt(){var e=q;return q<<=1,(q&62914560)===0&&(q=4194304),e}function jt(e){for(var n=[],a=0;31>a;a++)n.push(e);return n}function Xt(e,n){e.pendingLanes|=n,n!==268435456&&(e.suspendedLanes=0,e.pingedLanes=0,e.warmLanes=0)}function an(e,n,a,o,u,h){var x=e.pendingLanes;e.pendingLanes=a,e.suspendedLanes=0,e.pingedLanes=0,e.warmLanes=0,e.expiredLanes&=a,e.entangledLanes&=a,e.errorRecoveryDisabledLanes&=a,e.shellSuspendCounter=0;var C=e.entanglements,G=e.expirationTimes,it=e.hiddenUpdates;for(a=x&~a;0<a;){var mt=31-Ht(a),St=1<<mt;C[mt]=0,G[mt]=-1;var lt=it[mt];if(lt!==null)for(it[mt]=null,mt=0;mt<lt.length;mt++){var ct=lt[mt];ct!==null&&(ct.lane&=-536870913)}a&=~St}o!==0&&Ve(e,o,0),h!==0&&u===0&&e.tag!==0&&(e.suspendedLanes|=h&~(x&~n))}function Ve(e,n,a){e.pendingLanes|=n,e.suspendedLanes&=~n;var o=31-Ht(n);e.entangledLanes|=n,e.entanglements[o]=e.entanglements[o]|1073741824|a&261930}function ei(e,n){var a=e.entangledLanes|=n;for(e=e.entanglements;a;){var o=31-Ht(a),u=1<<o;u&n|e[o]&n&&(e[o]|=n),a&=~u}}function ni(e,n){var a=n&-n;return a=(a&42)!==0?1:lo(a),(a&(e.suspendedLanes|n))!==0?0:a}function lo(e){switch(e){case 2:e=1;break;case 8:e=4;break;case 32:e=16;break;case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:case 262144:case 524288:case 1048576:case 2097152:case 4194304:case 8388608:case 16777216:case 33554432:e=128;break;case 268435456:e=134217728;break;default:e=0}return e}function co(e){return e&=-e,2<e?8<e?(e&134217727)!==0?32:268435456:8:2}function uo(){var e=H.p;return e!==0?e:(e=window.event,e===void 0?32:b_(e.type))}function $s(e,n){var a=H.p;try{return H.p=e,n()}finally{H.p=a}}var Hi=Math.random().toString(36).slice(2),mn="__reactFiber$"+Hi,Un="__reactProps$"+Hi,Yn="__reactContainer$"+Hi,gs="__reactEvents$"+Hi,Ml="__reactListeners$"+Hi,El="__reactHandles$"+Hi,_s="__reactResources$"+Hi,Da="__reactMarker$"+Hi;function Ua(e){delete e[mn],delete e[Un],delete e[gs],delete e[Ml],delete e[El]}function ta(e){var n=e[mn];if(n)return n;for(var a=e.parentNode;a;){if(n=a[Yn]||a[mn]){if(a=n.alternate,n.child!==null||a!==null&&a.child!==null)for(e=l_(e);e!==null;){if(a=e[mn])return a;e=l_(e)}return n}e=a,a=e.parentNode}return null}function ea(e){if(e=e[mn]||e[Yn]){var n=e.tag;if(n===5||n===6||n===13||n===31||n===26||n===27||n===3)return e}return null}function vs(e){var n=e.tag;if(n===5||n===26||n===27||n===6)return e.stateNode;throw Error(s(33))}function Na(e){var n=e[_s];return n||(n=e[_s]={hoistableStyles:new Map,hoistableScripts:new Map}),n}function gn(e){e[Da]=!0}var bl=new Set,R={};function Z(e,n){ot(e,n),ot(e+"Capture",n)}function ot(e,n){for(R[e]=n,e=0;e<n.length;e++)bl.add(n[e])}var at=RegExp("^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"),st={},Bt={};function kt(e){return oe.call(Bt,e)?!0:oe.call(st,e)?!1:at.test(e)?Bt[e]=!0:(st[e]=!0,!1)}function Pt(e,n,a){if(kt(n))if(a===null)e.removeAttribute(n);else{switch(typeof a){case"undefined":case"function":case"symbol":e.removeAttribute(n);return;case"boolean":var o=n.toLowerCase().slice(0,5);if(o!=="data-"&&o!=="aria-"){e.removeAttribute(n);return}}e.setAttribute(n,""+a)}}function qt(e,n,a){if(a===null)e.removeAttribute(n);else{switch(typeof a){case"undefined":case"function":case"symbol":case"boolean":e.removeAttribute(n);return}e.setAttribute(n,""+a)}}function Wt(e,n,a,o){if(o===null)e.removeAttribute(a);else{switch(typeof o){case"undefined":case"function":case"symbol":case"boolean":e.removeAttribute(a);return}e.setAttributeNS(n,a,""+o)}}function ne(e){switch(typeof e){case"bigint":case"boolean":case"number":case"string":case"undefined":return e;case"object":return e;default:return""}}function ge(e){var n=e.type;return(e=e.nodeName)&&e.toLowerCase()==="input"&&(n==="checkbox"||n==="radio")}function Jt(e,n,a){var o=Object.getOwnPropertyDescriptor(e.constructor.prototype,n);if(!e.hasOwnProperty(n)&&typeof o<"u"&&typeof o.get=="function"&&typeof o.set=="function"){var u=o.get,h=o.set;return Object.defineProperty(e,n,{configurable:!0,get:function(){return u.call(this)},set:function(x){a=""+x,h.call(this,x)}}),Object.defineProperty(e,n,{enumerable:o.enumerable}),{getValue:function(){return a},setValue:function(x){a=""+x},stopTracking:function(){e._valueTracker=null,delete e[n]}}}}function Pe(e){if(!e._valueTracker){var n=ge(e)?"checked":"value";e._valueTracker=Jt(e,n,""+e[n])}}function sn(e){if(!e)return!1;var n=e._valueTracker;if(!n)return!0;var a=n.getValue(),o="";return e&&(o=ge(e)?e.checked?"true":"false":e.value),e=o,e!==a?(n.setValue(e),!0):!1}function $e(e){if(e=e||(typeof document<"u"?document:void 0),typeof e>"u")return null;try{return e.activeElement||e.body}catch{return e.body}}var ke=/[\n"\\]/g;function Xe(e){return e.replace(ke,function(n){return"\\"+n.charCodeAt(0).toString(16)+" "})}function Gt(e,n,a,o,u,h,x,C){e.name="",x!=null&&typeof x!="function"&&typeof x!="symbol"&&typeof x!="boolean"?e.type=x:e.removeAttribute("type"),n!=null?x==="number"?(n===0&&e.value===""||e.value!=n)&&(e.value=""+ne(n)):e.value!==""+ne(n)&&(e.value=""+ne(n)):x!=="submit"&&x!=="reset"||e.removeAttribute("value"),n!=null?Ee(e,x,ne(n)):a!=null?Ee(e,x,ne(a)):o!=null&&e.removeAttribute("value"),u==null&&h!=null&&(e.defaultChecked=!!h),u!=null&&(e.checked=u&&typeof u!="function"&&typeof u!="symbol"),C!=null&&typeof C!="function"&&typeof C!="symbol"&&typeof C!="boolean"?e.name=""+ne(C):e.removeAttribute("name")}function Bn(e,n,a,o,u,h,x,C){if(h!=null&&typeof h!="function"&&typeof h!="symbol"&&typeof h!="boolean"&&(e.type=h),n!=null||a!=null){if(!(h!=="submit"&&h!=="reset"||n!=null)){Pe(e);return}a=a!=null?""+ne(a):"",n=n!=null?""+ne(n):a,C||n===e.value||(e.value=n),e.defaultValue=n}o=o??u,o=typeof o!="function"&&typeof o!="symbol"&&!!o,e.checked=C?e.checked:!!o,e.defaultChecked=!!o,x!=null&&typeof x!="function"&&typeof x!="symbol"&&typeof x!="boolean"&&(e.name=x),Pe(e)}function Ee(e,n,a){n==="number"&&$e(e.ownerDocument)===e||e.defaultValue===""+a||(e.defaultValue=""+a)}function bn(e,n,a,o){if(e=e.options,n){n={};for(var u=0;u<a.length;u++)n["$"+a[u]]=!0;for(a=0;a<e.length;a++)u=n.hasOwnProperty("$"+e[a].value),e[a].selected!==u&&(e[a].selected=u),u&&o&&(e[a].defaultSelected=!0)}else{for(a=""+ne(a),n=null,u=0;u<e.length;u++){if(e[u].value===a){e[u].selected=!0,o&&(e[u].defaultSelected=!0);return}n!==null||e[u].disabled||(n=e[u])}n!==null&&(n.selected=!0)}}function ii(e,n,a){if(n!=null&&(n=""+ne(n),n!==e.value&&(e.value=n),a==null)){e.defaultValue!==n&&(e.defaultValue=n);return}e.defaultValue=a!=null?""+ne(a):""}function wi(e,n,a,o){if(n==null){if(o!=null){if(a!=null)throw Error(s(92));if($(o)){if(1<o.length)throw Error(s(93));o=o[0]}a=o}a==null&&(a=""),n=a}a=ne(n),e.defaultValue=a,o=e.textContent,o===a&&o!==""&&o!==null&&(e.value=o),Pe(e)}function ai(e,n){if(n){var a=e.firstChild;if(a&&a===e.lastChild&&a.nodeType===3){a.nodeValue=n;return}}e.textContent=n}var We=new Set("animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(" "));function rn(e,n,a){var o=n.indexOf("--")===0;a==null||typeof a=="boolean"||a===""?o?e.setProperty(n,""):n==="float"?e.cssFloat="":e[n]="":o?e.setProperty(n,a):typeof a!="number"||a===0||We.has(n)?n==="float"?e.cssFloat=a:e[n]=(""+a).trim():e[n]=a+"px"}function Di(e,n,a){if(n!=null&&typeof n!="object")throw Error(s(62));if(e=e.style,a!=null){for(var o in a)!a.hasOwnProperty(o)||n!=null&&n.hasOwnProperty(o)||(o.indexOf("--")===0?e.setProperty(o,""):o==="float"?e.cssFloat="":e[o]="");for(var u in n)o=n[u],n.hasOwnProperty(u)&&a[u]!==o&&rn(e,u,o)}else for(var h in n)n.hasOwnProperty(h)&&rn(e,h,n[h])}function Ge(e){if(e.indexOf("-")===-1)return!1;switch(e){case"annotation-xml":case"color-profile":case"font-face":case"font-face-src":case"font-face-uri":case"font-face-format":case"font-face-name":case"missing-glyph":return!1;default:return!0}}var Gi=new Map([["acceptCharset","accept-charset"],["htmlFor","for"],["httpEquiv","http-equiv"],["crossOrigin","crossorigin"],["accentHeight","accent-height"],["alignmentBaseline","alignment-baseline"],["arabicForm","arabic-form"],["baselineShift","baseline-shift"],["capHeight","cap-height"],["clipPath","clip-path"],["clipRule","clip-rule"],["colorInterpolation","color-interpolation"],["colorInterpolationFilters","color-interpolation-filters"],["colorProfile","color-profile"],["colorRendering","color-rendering"],["dominantBaseline","dominant-baseline"],["enableBackground","enable-background"],["fillOpacity","fill-opacity"],["fillRule","fill-rule"],["floodColor","flood-color"],["floodOpacity","flood-opacity"],["fontFamily","font-family"],["fontSize","font-size"],["fontSizeAdjust","font-size-adjust"],["fontStretch","font-stretch"],["fontStyle","font-style"],["fontVariant","font-variant"],["fontWeight","font-weight"],["glyphName","glyph-name"],["glyphOrientationHorizontal","glyph-orientation-horizontal"],["glyphOrientationVertical","glyph-orientation-vertical"],["horizAdvX","horiz-adv-x"],["horizOriginX","horiz-origin-x"],["imageRendering","image-rendering"],["letterSpacing","letter-spacing"],["lightingColor","lighting-color"],["markerEnd","marker-end"],["markerMid","marker-mid"],["markerStart","marker-start"],["overlinePosition","overline-position"],["overlineThickness","overline-thickness"],["paintOrder","paint-order"],["panose-1","panose-1"],["pointerEvents","pointer-events"],["renderingIntent","rendering-intent"],["shapeRendering","shape-rendering"],["stopColor","stop-color"],["stopOpacity","stop-opacity"],["strikethroughPosition","strikethrough-position"],["strikethroughThickness","strikethrough-thickness"],["strokeDasharray","stroke-dasharray"],["strokeDashoffset","stroke-dashoffset"],["strokeLinecap","stroke-linecap"],["strokeLinejoin","stroke-linejoin"],["strokeMiterlimit","stroke-miterlimit"],["strokeOpacity","stroke-opacity"],["strokeWidth","stroke-width"],["textAnchor","text-anchor"],["textDecoration","text-decoration"],["textRendering","text-rendering"],["transformOrigin","transform-origin"],["underlinePosition","underline-position"],["underlineThickness","underline-thickness"],["unicodeBidi","unicode-bidi"],["unicodeRange","unicode-range"],["unitsPerEm","units-per-em"],["vAlphabetic","v-alphabetic"],["vHanging","v-hanging"],["vIdeographic","v-ideographic"],["vMathematical","v-mathematical"],["vectorEffect","vector-effect"],["vertAdvY","vert-adv-y"],["vertOriginX","vert-origin-x"],["vertOriginY","vert-origin-y"],["wordSpacing","word-spacing"],["writingMode","writing-mode"],["xmlnsXlink","xmlns:xlink"],["xHeight","x-height"]]),La=/^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;function xs(e){return La.test(""+e)?"javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')":e}function na(){}var Lu=null;function Ou(e){return e=e.target||e.srcElement||window,e.correspondingUseElement&&(e=e.correspondingUseElement),e.nodeType===3?e.parentNode:e}var tr=null,er=null;function $p(e){var n=ea(e);if(n&&(e=n.stateNode)){var a=e[Un]||null;t:switch(e=n.stateNode,n.type){case"input":if(Gt(e,a.value,a.defaultValue,a.defaultValue,a.checked,a.defaultChecked,a.type,a.name),n=a.name,a.type==="radio"&&n!=null){for(a=e;a.parentNode;)a=a.parentNode;for(a=a.querySelectorAll('input[name="'+Xe(""+n)+'"][type="radio"]'),n=0;n<a.length;n++){var o=a[n];if(o!==e&&o.form===e.form){var u=o[Un]||null;if(!u)throw Error(s(90));Gt(o,u.value,u.defaultValue,u.defaultValue,u.checked,u.defaultChecked,u.type,u.name)}}for(n=0;n<a.length;n++)o=a[n],o.form===e.form&&sn(o)}break t;case"textarea":ii(e,a.value,a.defaultValue);break t;case"select":n=a.value,n!=null&&bn(e,!!a.multiple,n,!1)}}}var Pu=!1;function tm(e,n,a){if(Pu)return e(n,a);Pu=!0;try{var o=e(n);return o}finally{if(Pu=!1,(tr!==null||er!==null)&&(fc(),tr&&(n=tr,e=er,er=tr=null,$p(n),e)))for(n=0;n<e.length;n++)$p(e[n])}}function fo(e,n){var a=e.stateNode;if(a===null)return null;var o=a[Un]||null;if(o===null)return null;a=o[n];t:switch(n){case"onClick":case"onClickCapture":case"onDoubleClick":case"onDoubleClickCapture":case"onMouseDown":case"onMouseDownCapture":case"onMouseMove":case"onMouseMoveCapture":case"onMouseUp":case"onMouseUpCapture":case"onMouseEnter":(o=!o.disabled)||(e=e.type,o=!(e==="button"||e==="input"||e==="select"||e==="textarea")),e=!o;break t;default:e=!1}if(e)return null;if(a&&typeof a!="function")throw Error(s(231,n,typeof a));return a}var ia=!(typeof window>"u"||typeof window.document>"u"||typeof window.document.createElement>"u"),zu=!1;if(ia)try{var ho={};Object.defineProperty(ho,"passive",{get:function(){zu=!0}}),window.addEventListener("test",ho,ho),window.removeEventListener("test",ho,ho)}catch{zu=!1}var Oa=null,Bu=null,Tl=null;function em(){if(Tl)return Tl;var e,n=Bu,a=n.length,o,u="value"in Oa?Oa.value:Oa.textContent,h=u.length;for(e=0;e<a&&n[e]===u[e];e++);var x=a-e;for(o=1;o<=x&&n[a-o]===u[h-o];o++);return Tl=u.slice(e,1<o?1-o:void 0)}function Al(e){var n=e.keyCode;return"charCode"in e?(e=e.charCode,e===0&&n===13&&(e=13)):e=n,e===10&&(e=13),32<=e||e===13?e:0}function Rl(){return!0}function nm(){return!1}function Zn(e){function n(a,o,u,h,x){this._reactName=a,this._targetInst=u,this.type=o,this.nativeEvent=h,this.target=x,this.currentTarget=null;for(var C in e)e.hasOwnProperty(C)&&(a=e[C],this[C]=a?a(h):h[C]);return this.isDefaultPrevented=(h.defaultPrevented!=null?h.defaultPrevented:h.returnValue===!1)?Rl:nm,this.isPropagationStopped=nm,this}return v(n.prototype,{preventDefault:function(){this.defaultPrevented=!0;var a=this.nativeEvent;a&&(a.preventDefault?a.preventDefault():typeof a.returnValue!="unknown"&&(a.returnValue=!1),this.isDefaultPrevented=Rl)},stopPropagation:function(){var a=this.nativeEvent;a&&(a.stopPropagation?a.stopPropagation():typeof a.cancelBubble!="unknown"&&(a.cancelBubble=!0),this.isPropagationStopped=Rl)},persist:function(){},isPersistent:Rl}),n}var ys={eventPhase:0,bubbles:0,cancelable:0,timeStamp:function(e){return e.timeStamp||Date.now()},defaultPrevented:0,isTrusted:0},Cl=Zn(ys),po=v({},ys,{view:0,detail:0}),qx=Zn(po),Iu,Fu,mo,wl=v({},po,{screenX:0,screenY:0,clientX:0,clientY:0,pageX:0,pageY:0,ctrlKey:0,shiftKey:0,altKey:0,metaKey:0,getModifierState:Gu,button:0,buttons:0,relatedTarget:function(e){return e.relatedTarget===void 0?e.fromElement===e.srcElement?e.toElement:e.fromElement:e.relatedTarget},movementX:function(e){return"movementX"in e?e.movementX:(e!==mo&&(mo&&e.type==="mousemove"?(Iu=e.screenX-mo.screenX,Fu=e.screenY-mo.screenY):Fu=Iu=0,mo=e),Iu)},movementY:function(e){return"movementY"in e?e.movementY:Fu}}),im=Zn(wl),Yx=v({},wl,{dataTransfer:0}),Zx=Zn(Yx),jx=v({},po,{relatedTarget:0}),Hu=Zn(jx),Kx=v({},ys,{animationName:0,elapsedTime:0,pseudoElement:0}),Qx=Zn(Kx),Jx=v({},ys,{clipboardData:function(e){return"clipboardData"in e?e.clipboardData:window.clipboardData}}),$x=Zn(Jx),ty=v({},ys,{data:0}),am=Zn(ty),ey={Esc:"Escape",Spacebar:" ",Left:"ArrowLeft",Up:"ArrowUp",Right:"ArrowRight",Down:"ArrowDown",Del:"Delete",Win:"OS",Menu:"ContextMenu",Apps:"ContextMenu",Scroll:"ScrollLock",MozPrintableKey:"Unidentified"},ny={8:"Backspace",9:"Tab",12:"Clear",13:"Enter",16:"Shift",17:"Control",18:"Alt",19:"Pause",20:"CapsLock",27:"Escape",32:" ",33:"PageUp",34:"PageDown",35:"End",36:"Home",37:"ArrowLeft",38:"ArrowUp",39:"ArrowRight",40:"ArrowDown",45:"Insert",46:"Delete",112:"F1",113:"F2",114:"F3",115:"F4",116:"F5",117:"F6",118:"F7",119:"F8",120:"F9",121:"F10",122:"F11",123:"F12",144:"NumLock",145:"ScrollLock",224:"Meta"},iy={Alt:"altKey",Control:"ctrlKey",Meta:"metaKey",Shift:"shiftKey"};function ay(e){var n=this.nativeEvent;return n.getModifierState?n.getModifierState(e):(e=iy[e])?!!n[e]:!1}function Gu(){return ay}var sy=v({},po,{key:function(e){if(e.key){var n=ey[e.key]||e.key;if(n!=="Unidentified")return n}return e.type==="keypress"?(e=Al(e),e===13?"Enter":String.fromCharCode(e)):e.type==="keydown"||e.type==="keyup"?ny[e.keyCode]||"Unidentified":""},code:0,location:0,ctrlKey:0,shiftKey:0,altKey:0,metaKey:0,repeat:0,locale:0,getModifierState:Gu,charCode:function(e){return e.type==="keypress"?Al(e):0},keyCode:function(e){return e.type==="keydown"||e.type==="keyup"?e.keyCode:0},which:function(e){return e.type==="keypress"?Al(e):e.type==="keydown"||e.type==="keyup"?e.keyCode:0}}),ry=Zn(sy),oy=v({},wl,{pointerId:0,width:0,height:0,pressure:0,tangentialPressure:0,tiltX:0,tiltY:0,twist:0,pointerType:0,isPrimary:0}),sm=Zn(oy),ly=v({},po,{touches:0,targetTouches:0,changedTouches:0,altKey:0,metaKey:0,ctrlKey:0,shiftKey:0,getModifierState:Gu}),cy=Zn(ly),uy=v({},ys,{propertyName:0,elapsedTime:0,pseudoElement:0}),fy=Zn(uy),hy=v({},wl,{deltaX:function(e){return"deltaX"in e?e.deltaX:"wheelDeltaX"in e?-e.wheelDeltaX:0},deltaY:function(e){return"deltaY"in e?e.deltaY:"wheelDeltaY"in e?-e.wheelDeltaY:"wheelDelta"in e?-e.wheelDelta:0},deltaZ:0,deltaMode:0}),dy=Zn(hy),py=v({},ys,{newState:0,oldState:0}),my=Zn(py),gy=[9,13,27,32],Vu=ia&&"CompositionEvent"in window,go=null;ia&&"documentMode"in document&&(go=document.documentMode);var _y=ia&&"TextEvent"in window&&!go,rm=ia&&(!Vu||go&&8<go&&11>=go),om=" ",lm=!1;function cm(e,n){switch(e){case"keyup":return gy.indexOf(n.keyCode)!==-1;case"keydown":return n.keyCode!==229;case"keypress":case"mousedown":case"focusout":return!0;default:return!1}}function um(e){return e=e.detail,typeof e=="object"&&"data"in e?e.data:null}var nr=!1;function vy(e,n){switch(e){case"compositionend":return um(n);case"keypress":return n.which!==32?null:(lm=!0,om);case"textInput":return e=n.data,e===om&&lm?null:e;default:return null}}function xy(e,n){if(nr)return e==="compositionend"||!Vu&&cm(e,n)?(e=em(),Tl=Bu=Oa=null,nr=!1,e):null;switch(e){case"paste":return null;case"keypress":if(!(n.ctrlKey||n.altKey||n.metaKey)||n.ctrlKey&&n.altKey){if(n.char&&1<n.char.length)return n.char;if(n.which)return String.fromCharCode(n.which)}return null;case"compositionend":return rm&&n.locale!=="ko"?null:n.data;default:return null}}var yy={color:!0,date:!0,datetime:!0,"datetime-local":!0,email:!0,month:!0,number:!0,password:!0,range:!0,search:!0,tel:!0,text:!0,time:!0,url:!0,week:!0};function fm(e){var n=e&&e.nodeName&&e.nodeName.toLowerCase();return n==="input"?!!yy[e.type]:n==="textarea"}function hm(e,n,a,o){tr?er?er.push(o):er=[o]:tr=o,n=vc(n,"onChange"),0<n.length&&(a=new Cl("onChange","change",null,a,o),e.push({event:a,listeners:n}))}var _o=null,vo=null;function Sy(e){Zg(e,0)}function Dl(e){var n=vs(e);if(sn(n))return e}function dm(e,n){if(e==="change")return n}var pm=!1;if(ia){var ku;if(ia){var Xu="oninput"in document;if(!Xu){var mm=document.createElement("div");mm.setAttribute("oninput","return;"),Xu=typeof mm.oninput=="function"}ku=Xu}else ku=!1;pm=ku&&(!document.documentMode||9<document.documentMode)}function gm(){_o&&(_o.detachEvent("onpropertychange",_m),vo=_o=null)}function _m(e){if(e.propertyName==="value"&&Dl(vo)){var n=[];hm(n,vo,e,Ou(e)),tm(Sy,n)}}function My(e,n,a){e==="focusin"?(gm(),_o=n,vo=a,_o.attachEvent("onpropertychange",_m)):e==="focusout"&&gm()}function Ey(e){if(e==="selectionchange"||e==="keyup"||e==="keydown")return Dl(vo)}function by(e,n){if(e==="click")return Dl(n)}function Ty(e,n){if(e==="input"||e==="change")return Dl(n)}function Ay(e,n){return e===n&&(e!==0||1/e===1/n)||e!==e&&n!==n}var si=typeof Object.is=="function"?Object.is:Ay;function xo(e,n){if(si(e,n))return!0;if(typeof e!="object"||e===null||typeof n!="object"||n===null)return!1;var a=Object.keys(e),o=Object.keys(n);if(a.length!==o.length)return!1;for(o=0;o<a.length;o++){var u=a[o];if(!oe.call(n,u)||!si(e[u],n[u]))return!1}return!0}function vm(e){for(;e&&e.firstChild;)e=e.firstChild;return e}function xm(e,n){var a=vm(e);e=0;for(var o;a;){if(a.nodeType===3){if(o=e+a.textContent.length,e<=n&&o>=n)return{node:a,offset:n-e};e=o}t:{for(;a;){if(a.nextSibling){a=a.nextSibling;break t}a=a.parentNode}a=void 0}a=vm(a)}}function ym(e,n){return e&&n?e===n?!0:e&&e.nodeType===3?!1:n&&n.nodeType===3?ym(e,n.parentNode):"contains"in e?e.contains(n):e.compareDocumentPosition?!!(e.compareDocumentPosition(n)&16):!1:!1}function Sm(e){e=e!=null&&e.ownerDocument!=null&&e.ownerDocument.defaultView!=null?e.ownerDocument.defaultView:window;for(var n=$e(e.document);n instanceof e.HTMLIFrameElement;){try{var a=typeof n.contentWindow.location.href=="string"}catch{a=!1}if(a)e=n.contentWindow;else break;n=$e(e.document)}return n}function Wu(e){var n=e&&e.nodeName&&e.nodeName.toLowerCase();return n&&(n==="input"&&(e.type==="text"||e.type==="search"||e.type==="tel"||e.type==="url"||e.type==="password")||n==="textarea"||e.contentEditable==="true")}var Ry=ia&&"documentMode"in document&&11>=document.documentMode,ir=null,qu=null,yo=null,Yu=!1;function Mm(e,n,a){var o=a.window===a?a.document:a.nodeType===9?a:a.ownerDocument;Yu||ir==null||ir!==$e(o)||(o=ir,"selectionStart"in o&&Wu(o)?o={start:o.selectionStart,end:o.selectionEnd}:(o=(o.ownerDocument&&o.ownerDocument.defaultView||window).getSelection(),o={anchorNode:o.anchorNode,anchorOffset:o.anchorOffset,focusNode:o.focusNode,focusOffset:o.focusOffset}),yo&&xo(yo,o)||(yo=o,o=vc(qu,"onSelect"),0<o.length&&(n=new Cl("onSelect","select",null,n,a),e.push({event:n,listeners:o}),n.target=ir)))}function Ss(e,n){var a={};return a[e.toLowerCase()]=n.toLowerCase(),a["Webkit"+e]="webkit"+n,a["Moz"+e]="moz"+n,a}var ar={animationend:Ss("Animation","AnimationEnd"),animationiteration:Ss("Animation","AnimationIteration"),animationstart:Ss("Animation","AnimationStart"),transitionrun:Ss("Transition","TransitionRun"),transitionstart:Ss("Transition","TransitionStart"),transitioncancel:Ss("Transition","TransitionCancel"),transitionend:Ss("Transition","TransitionEnd")},Zu={},Em={};ia&&(Em=document.createElement("div").style,"AnimationEvent"in window||(delete ar.animationend.animation,delete ar.animationiteration.animation,delete ar.animationstart.animation),"TransitionEvent"in window||delete ar.transitionend.transition);function Ms(e){if(Zu[e])return Zu[e];if(!ar[e])return e;var n=ar[e],a;for(a in n)if(n.hasOwnProperty(a)&&a in Em)return Zu[e]=n[a];return e}var bm=Ms("animationend"),Tm=Ms("animationiteration"),Am=Ms("animationstart"),Cy=Ms("transitionrun"),wy=Ms("transitionstart"),Dy=Ms("transitioncancel"),Rm=Ms("transitionend"),Cm=new Map,ju="abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(" ");ju.push("scrollEnd");function Ui(e,n){Cm.set(e,n),Z(n,[e])}var Ul=typeof reportError=="function"?reportError:function(e){if(typeof window=="object"&&typeof window.ErrorEvent=="function"){var n=new window.ErrorEvent("error",{bubbles:!0,cancelable:!0,message:typeof e=="object"&&e!==null&&typeof e.message=="string"?String(e.message):String(e),error:e});if(!window.dispatchEvent(n))return}else if(typeof process=="object"&&typeof process.emit=="function"){process.emit("uncaughtException",e);return}console.error(e)},xi=[],sr=0,Ku=0;function Nl(){for(var e=sr,n=Ku=sr=0;n<e;){var a=xi[n];xi[n++]=null;var o=xi[n];xi[n++]=null;var u=xi[n];xi[n++]=null;var h=xi[n];if(xi[n++]=null,o!==null&&u!==null){var x=o.pending;x===null?u.next=u:(u.next=x.next,x.next=u),o.pending=u}h!==0&&wm(a,u,h)}}function Ll(e,n,a,o){xi[sr++]=e,xi[sr++]=n,xi[sr++]=a,xi[sr++]=o,Ku|=o,e.lanes|=o,e=e.alternate,e!==null&&(e.lanes|=o)}function Qu(e,n,a,o){return Ll(e,n,a,o),Ol(e)}function Es(e,n){return Ll(e,null,null,n),Ol(e)}function wm(e,n,a){e.lanes|=a;var o=e.alternate;o!==null&&(o.lanes|=a);for(var u=!1,h=e.return;h!==null;)h.childLanes|=a,o=h.alternate,o!==null&&(o.childLanes|=a),h.tag===22&&(e=h.stateNode,e===null||e._visibility&1||(u=!0)),e=h,h=h.return;return e.tag===3?(h=e.stateNode,u&&n!==null&&(u=31-Ht(a),e=h.hiddenUpdates,o=e[u],o===null?e[u]=[n]:o.push(n),n.lane=a|536870912),h):null}function Ol(e){if(50<Vo)throw Vo=0,oh=null,Error(s(185));for(var n=e.return;n!==null;)e=n,n=e.return;return e.tag===3?e.stateNode:null}var rr={};function Uy(e,n,a,o){this.tag=e,this.key=a,this.sibling=this.child=this.return=this.stateNode=this.type=this.elementType=null,this.index=0,this.refCleanup=this.ref=null,this.pendingProps=n,this.dependencies=this.memoizedState=this.updateQueue=this.memoizedProps=null,this.mode=o,this.subtreeFlags=this.flags=0,this.deletions=null,this.childLanes=this.lanes=0,this.alternate=null}function ri(e,n,a,o){return new Uy(e,n,a,o)}function Ju(e){return e=e.prototype,!(!e||!e.isReactComponent)}function aa(e,n){var a=e.alternate;return a===null?(a=ri(e.tag,n,e.key,e.mode),a.elementType=e.elementType,a.type=e.type,a.stateNode=e.stateNode,a.alternate=e,e.alternate=a):(a.pendingProps=n,a.type=e.type,a.flags=0,a.subtreeFlags=0,a.deletions=null),a.flags=e.flags&65011712,a.childLanes=e.childLanes,a.lanes=e.lanes,a.child=e.child,a.memoizedProps=e.memoizedProps,a.memoizedState=e.memoizedState,a.updateQueue=e.updateQueue,n=e.dependencies,a.dependencies=n===null?null:{lanes:n.lanes,firstContext:n.firstContext},a.sibling=e.sibling,a.index=e.index,a.ref=e.ref,a.refCleanup=e.refCleanup,a}function Dm(e,n){e.flags&=65011714;var a=e.alternate;return a===null?(e.childLanes=0,e.lanes=n,e.child=null,e.subtreeFlags=0,e.memoizedProps=null,e.memoizedState=null,e.updateQueue=null,e.dependencies=null,e.stateNode=null):(e.childLanes=a.childLanes,e.lanes=a.lanes,e.child=a.child,e.subtreeFlags=0,e.deletions=null,e.memoizedProps=a.memoizedProps,e.memoizedState=a.memoizedState,e.updateQueue=a.updateQueue,e.type=a.type,n=a.dependencies,e.dependencies=n===null?null:{lanes:n.lanes,firstContext:n.firstContext}),e}function Pl(e,n,a,o,u,h){var x=0;if(o=e,typeof e=="function")Ju(e)&&(x=1);else if(typeof e=="string")x=zS(e,a,Tt.current)?26:e==="html"||e==="head"||e==="body"?27:5;else t:switch(e){case L:return e=ri(31,a,n,u),e.elementType=L,e.lanes=h,e;case A:return bs(a.children,u,h,n);case M:x=8,u|=24;break;case y:return e=ri(12,a,n,u|2),e.elementType=y,e.lanes=h,e;case I:return e=ri(13,a,n,u),e.elementType=I,e.lanes=h,e;case O:return e=ri(19,a,n,u),e.elementType=O,e.lanes=h,e;default:if(typeof e=="object"&&e!==null)switch(e.$$typeof){case F:x=10;break t;case B:x=9;break t;case w:x=11;break t;case D:x=14;break t;case T:x=16,o=null;break t}x=29,a=Error(s(130,e===null?"null":typeof e,"")),o=null}return n=ri(x,a,n,u),n.elementType=e,n.type=o,n.lanes=h,n}function bs(e,n,a,o){return e=ri(7,e,o,n),e.lanes=a,e}function $u(e,n,a){return e=ri(6,e,null,n),e.lanes=a,e}function Um(e){var n=ri(18,null,null,0);return n.stateNode=e,n}function tf(e,n,a){return n=ri(4,e.children!==null?e.children:[],e.key,n),n.lanes=a,n.stateNode={containerInfo:e.containerInfo,pendingChildren:null,implementation:e.implementation},n}var Nm=new WeakMap;function yi(e,n){if(typeof e=="object"&&e!==null){var a=Nm.get(e);return a!==void 0?a:(n={value:e,source:n,stack:$t(n)},Nm.set(e,n),n)}return{value:e,source:n,stack:$t(n)}}var or=[],lr=0,zl=null,So=0,Si=[],Mi=0,Pa=null,Vi=1,ki="";function sa(e,n){or[lr++]=So,or[lr++]=zl,zl=e,So=n}function Lm(e,n,a){Si[Mi++]=Vi,Si[Mi++]=ki,Si[Mi++]=Pa,Pa=e;var o=Vi;e=ki;var u=32-Ht(o)-1;o&=~(1<<u),a+=1;var h=32-Ht(n)+u;if(30<h){var x=u-u%5;h=(o&(1<<x)-1).toString(32),o>>=x,u-=x,Vi=1<<32-Ht(n)+u|a<<u|o,ki=h+e}else Vi=1<<h|a<<u|o,ki=e}function ef(e){e.return!==null&&(sa(e,1),Lm(e,1,0))}function nf(e){for(;e===zl;)zl=or[--lr],or[lr]=null,So=or[--lr],or[lr]=null;for(;e===Pa;)Pa=Si[--Mi],Si[Mi]=null,ki=Si[--Mi],Si[Mi]=null,Vi=Si[--Mi],Si[Mi]=null}function Om(e,n){Si[Mi++]=Vi,Si[Mi++]=ki,Si[Mi++]=Pa,Vi=n.id,ki=n.overflow,Pa=e}var Nn=null,en=null,De=!1,za=null,Ei=!1,af=Error(s(519));function Ba(e){var n=Error(s(418,1<arguments.length&&arguments[1]!==void 0&&arguments[1]?"text":"HTML",""));throw Mo(yi(n,e)),af}function Pm(e){var n=e.stateNode,a=e.type,o=e.memoizedProps;switch(n[mn]=e,n[Un]=o,a){case"dialog":Te("cancel",n),Te("close",n);break;case"iframe":case"object":case"embed":Te("load",n);break;case"video":case"audio":for(a=0;a<Xo.length;a++)Te(Xo[a],n);break;case"source":Te("error",n);break;case"img":case"image":case"link":Te("error",n),Te("load",n);break;case"details":Te("toggle",n);break;case"input":Te("invalid",n),Bn(n,o.value,o.defaultValue,o.checked,o.defaultChecked,o.type,o.name,!0);break;case"select":Te("invalid",n);break;case"textarea":Te("invalid",n),wi(n,o.value,o.defaultValue,o.children)}a=o.children,typeof a!="string"&&typeof a!="number"&&typeof a!="bigint"||n.textContent===""+a||o.suppressHydrationWarning===!0||Jg(n.textContent,a)?(o.popover!=null&&(Te("beforetoggle",n),Te("toggle",n)),o.onScroll!=null&&Te("scroll",n),o.onScrollEnd!=null&&Te("scrollend",n),o.onClick!=null&&(n.onclick=na),n=!0):n=!1,n||Ba(e,!0)}function zm(e){for(Nn=e.return;Nn;)switch(Nn.tag){case 5:case 31:case 13:Ei=!1;return;case 27:case 3:Ei=!0;return;default:Nn=Nn.return}}function cr(e){if(e!==Nn)return!1;if(!De)return zm(e),De=!0,!1;var n=e.tag,a;if((a=n!==3&&n!==27)&&((a=n===5)&&(a=e.type,a=!(a!=="form"&&a!=="button")||Mh(e.type,e.memoizedProps)),a=!a),a&&en&&Ba(e),zm(e),n===13){if(e=e.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(s(317));en=o_(e)}else if(n===31){if(e=e.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(s(317));en=o_(e)}else n===27?(n=en,Qa(e.type)?(e=Rh,Rh=null,en=e):en=n):en=Nn?Ti(e.stateNode.nextSibling):null;return!0}function Ts(){en=Nn=null,De=!1}function sf(){var e=za;return e!==null&&(Jn===null?Jn=e:Jn.push.apply(Jn,e),za=null),e}function Mo(e){za===null?za=[e]:za.push(e)}var rf=N(null),As=null,ra=null;function Ia(e,n,a){vt(rf,n._currentValue),n._currentValue=a}function oa(e){e._currentValue=rf.current,W(rf)}function of(e,n,a){for(;e!==null;){var o=e.alternate;if((e.childLanes&n)!==n?(e.childLanes|=n,o!==null&&(o.childLanes|=n)):o!==null&&(o.childLanes&n)!==n&&(o.childLanes|=n),e===a)break;e=e.return}}function lf(e,n,a,o){var u=e.child;for(u!==null&&(u.return=e);u!==null;){var h=u.dependencies;if(h!==null){var x=u.child;h=h.firstContext;t:for(;h!==null;){var C=h;h=u;for(var G=0;G<n.length;G++)if(C.context===n[G]){h.lanes|=a,C=h.alternate,C!==null&&(C.lanes|=a),of(h.return,a,e),o||(x=null);break t}h=C.next}}else if(u.tag===18){if(x=u.return,x===null)throw Error(s(341));x.lanes|=a,h=x.alternate,h!==null&&(h.lanes|=a),of(x,a,e),x=null}else x=u.child;if(x!==null)x.return=u;else for(x=u;x!==null;){if(x===e){x=null;break}if(u=x.sibling,u!==null){u.return=x.return,x=u;break}x=x.return}u=x}}function ur(e,n,a,o){e=null;for(var u=n,h=!1;u!==null;){if(!h){if((u.flags&524288)!==0)h=!0;else if((u.flags&262144)!==0)break}if(u.tag===10){var x=u.alternate;if(x===null)throw Error(s(387));if(x=x.memoizedProps,x!==null){var C=u.type;si(u.pendingProps.value,x.value)||(e!==null?e.push(C):e=[C])}}else if(u===yt.current){if(x=u.alternate,x===null)throw Error(s(387));x.memoizedState.memoizedState!==u.memoizedState.memoizedState&&(e!==null?e.push(jo):e=[jo])}u=u.return}e!==null&&lf(n,e,a,o),n.flags|=262144}function Bl(e){for(e=e.firstContext;e!==null;){if(!si(e.context._currentValue,e.memoizedValue))return!0;e=e.next}return!1}function Rs(e){As=e,ra=null,e=e.dependencies,e!==null&&(e.firstContext=null)}function Ln(e){return Bm(As,e)}function Il(e,n){return As===null&&Rs(e),Bm(e,n)}function Bm(e,n){var a=n._currentValue;if(n={context:n,memoizedValue:a,next:null},ra===null){if(e===null)throw Error(s(308));ra=n,e.dependencies={lanes:0,firstContext:n},e.flags|=524288}else ra=ra.next=n;return a}var Ny=typeof AbortController<"u"?AbortController:function(){var e=[],n=this.signal={aborted:!1,addEventListener:function(a,o){e.push(o)}};this.abort=function(){n.aborted=!0,e.forEach(function(a){return a()})}},Ly=r.unstable_scheduleCallback,Oy=r.unstable_NormalPriority,_n={$$typeof:F,Consumer:null,Provider:null,_currentValue:null,_currentValue2:null,_threadCount:0};function cf(){return{controller:new Ny,data:new Map,refCount:0}}function Eo(e){e.refCount--,e.refCount===0&&Ly(Oy,function(){e.controller.abort()})}var bo=null,uf=0,fr=0,hr=null;function Py(e,n){if(bo===null){var a=bo=[];uf=0,fr=dh(),hr={status:"pending",value:void 0,then:function(o){a.push(o)}}}return uf++,n.then(Im,Im),n}function Im(){if(--uf===0&&bo!==null){hr!==null&&(hr.status="fulfilled");var e=bo;bo=null,fr=0,hr=null;for(var n=0;n<e.length;n++)(0,e[n])()}}function zy(e,n){var a=[],o={status:"pending",value:null,reason:null,then:function(u){a.push(u)}};return e.then(function(){o.status="fulfilled",o.value=n;for(var u=0;u<a.length;u++)(0,a[u])(n)},function(u){for(o.status="rejected",o.reason=u,u=0;u<a.length;u++)(0,a[u])(void 0)}),o}var Fm=P.S;P.S=function(e,n){Mg=we(),typeof n=="object"&&n!==null&&typeof n.then=="function"&&Py(e,n),Fm!==null&&Fm(e,n)};var Cs=N(null);function ff(){var e=Cs.current;return e!==null?e:tn.pooledCache}function Fl(e,n){n===null?vt(Cs,Cs.current):vt(Cs,n.pool)}function Hm(){var e=ff();return e===null?null:{parent:_n._currentValue,pool:e}}var dr=Error(s(460)),hf=Error(s(474)),Hl=Error(s(542)),Gl={then:function(){}};function Gm(e){return e=e.status,e==="fulfilled"||e==="rejected"}function Vm(e,n,a){switch(a=e[a],a===void 0?e.push(n):a!==n&&(n.then(na,na),n=a),n.status){case"fulfilled":return n.value;case"rejected":throw e=n.reason,Xm(e),e;default:if(typeof n.status=="string")n.then(na,na);else{if(e=tn,e!==null&&100<e.shellSuspendCounter)throw Error(s(482));e=n,e.status="pending",e.then(function(o){if(n.status==="pending"){var u=n;u.status="fulfilled",u.value=o}},function(o){if(n.status==="pending"){var u=n;u.status="rejected",u.reason=o}})}switch(n.status){case"fulfilled":return n.value;case"rejected":throw e=n.reason,Xm(e),e}throw Ds=n,dr}}function ws(e){try{var n=e._init;return n(e._payload)}catch(a){throw a!==null&&typeof a=="object"&&typeof a.then=="function"?(Ds=a,dr):a}}var Ds=null;function km(){if(Ds===null)throw Error(s(459));var e=Ds;return Ds=null,e}function Xm(e){if(e===dr||e===Hl)throw Error(s(483))}var pr=null,To=0;function Vl(e){var n=To;return To+=1,pr===null&&(pr=[]),Vm(pr,e,n)}function Ao(e,n){n=n.props.ref,e.ref=n!==void 0?n:null}function kl(e,n){throw n.$$typeof===g?Error(s(525)):(e=Object.prototype.toString.call(n),Error(s(31,e==="[object Object]"?"object with keys {"+Object.keys(n).join(", ")+"}":e)))}function Wm(e){function n(K,X){if(e){var nt=K.deletions;nt===null?(K.deletions=[X],K.flags|=16):nt.push(X)}}function a(K,X){if(!e)return null;for(;X!==null;)n(K,X),X=X.sibling;return null}function o(K){for(var X=new Map;K!==null;)K.key!==null?X.set(K.key,K):X.set(K.index,K),K=K.sibling;return X}function u(K,X){return K=aa(K,X),K.index=0,K.sibling=null,K}function h(K,X,nt){return K.index=nt,e?(nt=K.alternate,nt!==null?(nt=nt.index,nt<X?(K.flags|=67108866,X):nt):(K.flags|=67108866,X)):(K.flags|=1048576,X)}function x(K){return e&&K.alternate===null&&(K.flags|=67108866),K}function C(K,X,nt,xt){return X===null||X.tag!==6?(X=$u(nt,K.mode,xt),X.return=K,X):(X=u(X,nt),X.return=K,X)}function G(K,X,nt,xt){var ie=nt.type;return ie===A?mt(K,X,nt.props.children,xt,nt.key):X!==null&&(X.elementType===ie||typeof ie=="object"&&ie!==null&&ie.$$typeof===T&&ws(ie)===X.type)?(X=u(X,nt.props),Ao(X,nt),X.return=K,X):(X=Pl(nt.type,nt.key,nt.props,null,K.mode,xt),Ao(X,nt),X.return=K,X)}function it(K,X,nt,xt){return X===null||X.tag!==4||X.stateNode.containerInfo!==nt.containerInfo||X.stateNode.implementation!==nt.implementation?(X=tf(nt,K.mode,xt),X.return=K,X):(X=u(X,nt.children||[]),X.return=K,X)}function mt(K,X,nt,xt,ie){return X===null||X.tag!==7?(X=bs(nt,K.mode,xt,ie),X.return=K,X):(X=u(X,nt),X.return=K,X)}function St(K,X,nt){if(typeof X=="string"&&X!==""||typeof X=="number"||typeof X=="bigint")return X=$u(""+X,K.mode,nt),X.return=K,X;if(typeof X=="object"&&X!==null){switch(X.$$typeof){case S:return nt=Pl(X.type,X.key,X.props,null,K.mode,nt),Ao(nt,X),nt.return=K,nt;case E:return X=tf(X,K.mode,nt),X.return=K,X;case T:return X=ws(X),St(K,X,nt)}if($(X)||J(X))return X=bs(X,K.mode,nt,null),X.return=K,X;if(typeof X.then=="function")return St(K,Vl(X),nt);if(X.$$typeof===F)return St(K,Il(K,X),nt);kl(K,X)}return null}function lt(K,X,nt,xt){var ie=X!==null?X.key:null;if(typeof nt=="string"&&nt!==""||typeof nt=="number"||typeof nt=="bigint")return ie!==null?null:C(K,X,""+nt,xt);if(typeof nt=="object"&&nt!==null){switch(nt.$$typeof){case S:return nt.key===ie?G(K,X,nt,xt):null;case E:return nt.key===ie?it(K,X,nt,xt):null;case T:return nt=ws(nt),lt(K,X,nt,xt)}if($(nt)||J(nt))return ie!==null?null:mt(K,X,nt,xt,null);if(typeof nt.then=="function")return lt(K,X,Vl(nt),xt);if(nt.$$typeof===F)return lt(K,X,Il(K,nt),xt);kl(K,nt)}return null}function ct(K,X,nt,xt,ie){if(typeof xt=="string"&&xt!==""||typeof xt=="number"||typeof xt=="bigint")return K=K.get(nt)||null,C(X,K,""+xt,ie);if(typeof xt=="object"&&xt!==null){switch(xt.$$typeof){case S:return K=K.get(xt.key===null?nt:xt.key)||null,G(X,K,xt,ie);case E:return K=K.get(xt.key===null?nt:xt.key)||null,it(X,K,xt,ie);case T:return xt=ws(xt),ct(K,X,nt,xt,ie)}if($(xt)||J(xt))return K=K.get(nt)||null,mt(X,K,xt,ie,null);if(typeof xt.then=="function")return ct(K,X,nt,Vl(xt),ie);if(xt.$$typeof===F)return ct(K,X,nt,Il(X,xt),ie);kl(X,xt)}return null}function Yt(K,X,nt,xt){for(var ie=null,ze=null,Kt=X,ve=X=0,Re=null;Kt!==null&&ve<nt.length;ve++){Kt.index>ve?(Re=Kt,Kt=null):Re=Kt.sibling;var Be=lt(K,Kt,nt[ve],xt);if(Be===null){Kt===null&&(Kt=Re);break}e&&Kt&&Be.alternate===null&&n(K,Kt),X=h(Be,X,ve),ze===null?ie=Be:ze.sibling=Be,ze=Be,Kt=Re}if(ve===nt.length)return a(K,Kt),De&&sa(K,ve),ie;if(Kt===null){for(;ve<nt.length;ve++)Kt=St(K,nt[ve],xt),Kt!==null&&(X=h(Kt,X,ve),ze===null?ie=Kt:ze.sibling=Kt,ze=Kt);return De&&sa(K,ve),ie}for(Kt=o(Kt);ve<nt.length;ve++)Re=ct(Kt,K,ve,nt[ve],xt),Re!==null&&(e&&Re.alternate!==null&&Kt.delete(Re.key===null?ve:Re.key),X=h(Re,X,ve),ze===null?ie=Re:ze.sibling=Re,ze=Re);return e&&Kt.forEach(function(ns){return n(K,ns)}),De&&sa(K,ve),ie}function se(K,X,nt,xt){if(nt==null)throw Error(s(151));for(var ie=null,ze=null,Kt=X,ve=X=0,Re=null,Be=nt.next();Kt!==null&&!Be.done;ve++,Be=nt.next()){Kt.index>ve?(Re=Kt,Kt=null):Re=Kt.sibling;var ns=lt(K,Kt,Be.value,xt);if(ns===null){Kt===null&&(Kt=Re);break}e&&Kt&&ns.alternate===null&&n(K,Kt),X=h(ns,X,ve),ze===null?ie=ns:ze.sibling=ns,ze=ns,Kt=Re}if(Be.done)return a(K,Kt),De&&sa(K,ve),ie;if(Kt===null){for(;!Be.done;ve++,Be=nt.next())Be=St(K,Be.value,xt),Be!==null&&(X=h(Be,X,ve),ze===null?ie=Be:ze.sibling=Be,ze=Be);return De&&sa(K,ve),ie}for(Kt=o(Kt);!Be.done;ve++,Be=nt.next())Be=ct(Kt,K,ve,Be.value,xt),Be!==null&&(e&&Be.alternate!==null&&Kt.delete(Be.key===null?ve:Be.key),X=h(Be,X,ve),ze===null?ie=Be:ze.sibling=Be,ze=Be);return e&&Kt.forEach(function(YS){return n(K,YS)}),De&&sa(K,ve),ie}function Je(K,X,nt,xt){if(typeof nt=="object"&&nt!==null&&nt.type===A&&nt.key===null&&(nt=nt.props.children),typeof nt=="object"&&nt!==null){switch(nt.$$typeof){case S:t:{for(var ie=nt.key;X!==null;){if(X.key===ie){if(ie=nt.type,ie===A){if(X.tag===7){a(K,X.sibling),xt=u(X,nt.props.children),xt.return=K,K=xt;break t}}else if(X.elementType===ie||typeof ie=="object"&&ie!==null&&ie.$$typeof===T&&ws(ie)===X.type){a(K,X.sibling),xt=u(X,nt.props),Ao(xt,nt),xt.return=K,K=xt;break t}a(K,X);break}else n(K,X);X=X.sibling}nt.type===A?(xt=bs(nt.props.children,K.mode,xt,nt.key),xt.return=K,K=xt):(xt=Pl(nt.type,nt.key,nt.props,null,K.mode,xt),Ao(xt,nt),xt.return=K,K=xt)}return x(K);case E:t:{for(ie=nt.key;X!==null;){if(X.key===ie)if(X.tag===4&&X.stateNode.containerInfo===nt.containerInfo&&X.stateNode.implementation===nt.implementation){a(K,X.sibling),xt=u(X,nt.children||[]),xt.return=K,K=xt;break t}else{a(K,X);break}else n(K,X);X=X.sibling}xt=tf(nt,K.mode,xt),xt.return=K,K=xt}return x(K);case T:return nt=ws(nt),Je(K,X,nt,xt)}if($(nt))return Yt(K,X,nt,xt);if(J(nt)){if(ie=J(nt),typeof ie!="function")throw Error(s(150));return nt=ie.call(nt),se(K,X,nt,xt)}if(typeof nt.then=="function")return Je(K,X,Vl(nt),xt);if(nt.$$typeof===F)return Je(K,X,Il(K,nt),xt);kl(K,nt)}return typeof nt=="string"&&nt!==""||typeof nt=="number"||typeof nt=="bigint"?(nt=""+nt,X!==null&&X.tag===6?(a(K,X.sibling),xt=u(X,nt),xt.return=K,K=xt):(a(K,X),xt=$u(nt,K.mode,xt),xt.return=K,K=xt),x(K)):a(K,X)}return function(K,X,nt,xt){try{To=0;var ie=Je(K,X,nt,xt);return pr=null,ie}catch(Kt){if(Kt===dr||Kt===Hl)throw Kt;var ze=ri(29,Kt,null,K.mode);return ze.lanes=xt,ze.return=K,ze}finally{}}}var Us=Wm(!0),qm=Wm(!1),Fa=!1;function df(e){e.updateQueue={baseState:e.memoizedState,firstBaseUpdate:null,lastBaseUpdate:null,shared:{pending:null,lanes:0,hiddenCallbacks:null},callbacks:null}}function pf(e,n){e=e.updateQueue,n.updateQueue===e&&(n.updateQueue={baseState:e.baseState,firstBaseUpdate:e.firstBaseUpdate,lastBaseUpdate:e.lastBaseUpdate,shared:e.shared,callbacks:null})}function Ha(e){return{lane:e,tag:0,payload:null,callback:null,next:null}}function Ga(e,n,a){var o=e.updateQueue;if(o===null)return null;if(o=o.shared,(Ie&2)!==0){var u=o.pending;return u===null?n.next=n:(n.next=u.next,u.next=n),o.pending=n,n=Ol(e),wm(e,null,a),n}return Ll(e,o,n,a),Ol(e)}function Ro(e,n,a){if(n=n.updateQueue,n!==null&&(n=n.shared,(a&4194048)!==0)){var o=n.lanes;o&=e.pendingLanes,a|=o,n.lanes=a,ei(e,a)}}function mf(e,n){var a=e.updateQueue,o=e.alternate;if(o!==null&&(o=o.updateQueue,a===o)){var u=null,h=null;if(a=a.firstBaseUpdate,a!==null){do{var x={lane:a.lane,tag:a.tag,payload:a.payload,callback:null,next:null};h===null?u=h=x:h=h.next=x,a=a.next}while(a!==null);h===null?u=h=n:h=h.next=n}else u=h=n;a={baseState:o.baseState,firstBaseUpdate:u,lastBaseUpdate:h,shared:o.shared,callbacks:o.callbacks},e.updateQueue=a;return}e=a.lastBaseUpdate,e===null?a.firstBaseUpdate=n:e.next=n,a.lastBaseUpdate=n}var gf=!1;function Co(){if(gf){var e=hr;if(e!==null)throw e}}function wo(e,n,a,o){gf=!1;var u=e.updateQueue;Fa=!1;var h=u.firstBaseUpdate,x=u.lastBaseUpdate,C=u.shared.pending;if(C!==null){u.shared.pending=null;var G=C,it=G.next;G.next=null,x===null?h=it:x.next=it,x=G;var mt=e.alternate;mt!==null&&(mt=mt.updateQueue,C=mt.lastBaseUpdate,C!==x&&(C===null?mt.firstBaseUpdate=it:C.next=it,mt.lastBaseUpdate=G))}if(h!==null){var St=u.baseState;x=0,mt=it=G=null,C=h;do{var lt=C.lane&-536870913,ct=lt!==C.lane;if(ct?(Ae&lt)===lt:(o&lt)===lt){lt!==0&&lt===fr&&(gf=!0),mt!==null&&(mt=mt.next={lane:0,tag:C.tag,payload:C.payload,callback:null,next:null});t:{var Yt=e,se=C;lt=n;var Je=a;switch(se.tag){case 1:if(Yt=se.payload,typeof Yt=="function"){St=Yt.call(Je,St,lt);break t}St=Yt;break t;case 3:Yt.flags=Yt.flags&-65537|128;case 0:if(Yt=se.payload,lt=typeof Yt=="function"?Yt.call(Je,St,lt):Yt,lt==null)break t;St=v({},St,lt);break t;case 2:Fa=!0}}lt=C.callback,lt!==null&&(e.flags|=64,ct&&(e.flags|=8192),ct=u.callbacks,ct===null?u.callbacks=[lt]:ct.push(lt))}else ct={lane:lt,tag:C.tag,payload:C.payload,callback:C.callback,next:null},mt===null?(it=mt=ct,G=St):mt=mt.next=ct,x|=lt;if(C=C.next,C===null){if(C=u.shared.pending,C===null)break;ct=C,C=ct.next,ct.next=null,u.lastBaseUpdate=ct,u.shared.pending=null}}while(!0);mt===null&&(G=St),u.baseState=G,u.firstBaseUpdate=it,u.lastBaseUpdate=mt,h===null&&(u.shared.lanes=0),qa|=x,e.lanes=x,e.memoizedState=St}}function Ym(e,n){if(typeof e!="function")throw Error(s(191,e));e.call(n)}function Zm(e,n){var a=e.callbacks;if(a!==null)for(e.callbacks=null,e=0;e<a.length;e++)Ym(a[e],n)}var mr=N(null),Xl=N(0);function jm(e,n){e=ga,vt(Xl,e),vt(mr,n),ga=e|n.baseLanes}function _f(){vt(Xl,ga),vt(mr,mr.current)}function vf(){ga=Xl.current,W(mr),W(Xl)}var oi=N(null),bi=null;function Va(e){var n=e.alternate;vt(dn,dn.current&1),vt(oi,e),bi===null&&(n===null||mr.current!==null||n.memoizedState!==null)&&(bi=e)}function xf(e){vt(dn,dn.current),vt(oi,e),bi===null&&(bi=e)}function Km(e){e.tag===22?(vt(dn,dn.current),vt(oi,e),bi===null&&(bi=e)):ka()}function ka(){vt(dn,dn.current),vt(oi,oi.current)}function li(e){W(oi),bi===e&&(bi=null),W(dn)}var dn=N(0);function Wl(e){for(var n=e;n!==null;){if(n.tag===13){var a=n.memoizedState;if(a!==null&&(a=a.dehydrated,a===null||Th(a)||Ah(a)))return n}else if(n.tag===19&&(n.memoizedProps.revealOrder==="forwards"||n.memoizedProps.revealOrder==="backwards"||n.memoizedProps.revealOrder==="unstable_legacy-backwards"||n.memoizedProps.revealOrder==="together")){if((n.flags&128)!==0)return n}else if(n.child!==null){n.child.return=n,n=n.child;continue}if(n===e)break;for(;n.sibling===null;){if(n.return===null||n.return===e)return null;n=n.return}n.sibling.return=n.return,n=n.sibling}return null}var la=0,_e=null,Ke=null,vn=null,ql=!1,gr=!1,Ns=!1,Yl=0,Do=0,_r=null,By=0;function un(){throw Error(s(321))}function yf(e,n){if(n===null)return!1;for(var a=0;a<n.length&&a<e.length;a++)if(!si(e[a],n[a]))return!1;return!0}function Sf(e,n,a,o,u,h){return la=h,_e=n,n.memoizedState=null,n.updateQueue=null,n.lanes=0,P.H=e===null||e.memoizedState===null?L0:zf,Ns=!1,h=a(o,u),Ns=!1,gr&&(h=Jm(n,a,o,u)),Qm(e),h}function Qm(e){P.H=Lo;var n=Ke!==null&&Ke.next!==null;if(la=0,vn=Ke=_e=null,ql=!1,Do=0,_r=null,n)throw Error(s(300));e===null||xn||(e=e.dependencies,e!==null&&Bl(e)&&(xn=!0))}function Jm(e,n,a,o){_e=e;var u=0;do{if(gr&&(_r=null),Do=0,gr=!1,25<=u)throw Error(s(301));if(u+=1,vn=Ke=null,e.updateQueue!=null){var h=e.updateQueue;h.lastEffect=null,h.events=null,h.stores=null,h.memoCache!=null&&(h.memoCache.index=0)}P.H=O0,h=n(a,o)}while(gr);return h}function Iy(){var e=P.H,n=e.useState()[0];return n=typeof n.then=="function"?Uo(n):n,e=e.useState()[0],(Ke!==null?Ke.memoizedState:null)!==e&&(_e.flags|=1024),n}function Mf(){var e=Yl!==0;return Yl=0,e}function Ef(e,n,a){n.updateQueue=e.updateQueue,n.flags&=-2053,e.lanes&=~a}function bf(e){if(ql){for(e=e.memoizedState;e!==null;){var n=e.queue;n!==null&&(n.pending=null),e=e.next}ql=!1}la=0,vn=Ke=_e=null,gr=!1,Do=Yl=0,_r=null}function kn(){var e={memoizedState:null,baseState:null,baseQueue:null,queue:null,next:null};return vn===null?_e.memoizedState=vn=e:vn=vn.next=e,vn}function pn(){if(Ke===null){var e=_e.alternate;e=e!==null?e.memoizedState:null}else e=Ke.next;var n=vn===null?_e.memoizedState:vn.next;if(n!==null)vn=n,Ke=e;else{if(e===null)throw _e.alternate===null?Error(s(467)):Error(s(310));Ke=e,e={memoizedState:Ke.memoizedState,baseState:Ke.baseState,baseQueue:Ke.baseQueue,queue:Ke.queue,next:null},vn===null?_e.memoizedState=vn=e:vn=vn.next=e}return vn}function Zl(){return{lastEffect:null,events:null,stores:null,memoCache:null}}function Uo(e){var n=Do;return Do+=1,_r===null&&(_r=[]),e=Vm(_r,e,n),n=_e,(vn===null?n.memoizedState:vn.next)===null&&(n=n.alternate,P.H=n===null||n.memoizedState===null?L0:zf),e}function jl(e){if(e!==null&&typeof e=="object"){if(typeof e.then=="function")return Uo(e);if(e.$$typeof===F)return Ln(e)}throw Error(s(438,String(e)))}function Tf(e){var n=null,a=_e.updateQueue;if(a!==null&&(n=a.memoCache),n==null){var o=_e.alternate;o!==null&&(o=o.updateQueue,o!==null&&(o=o.memoCache,o!=null&&(n={data:o.data.map(function(u){return u.slice()}),index:0})))}if(n==null&&(n={data:[],index:0}),a===null&&(a=Zl(),_e.updateQueue=a),a.memoCache=n,a=n.data[n.index],a===void 0)for(a=n.data[n.index]=Array(e),o=0;o<e;o++)a[o]=j;return n.index++,a}function ca(e,n){return typeof n=="function"?n(e):n}function Kl(e){var n=pn();return Af(n,Ke,e)}function Af(e,n,a){var o=e.queue;if(o===null)throw Error(s(311));o.lastRenderedReducer=a;var u=e.baseQueue,h=o.pending;if(h!==null){if(u!==null){var x=u.next;u.next=h.next,h.next=x}n.baseQueue=u=h,o.pending=null}if(h=e.baseState,u===null)e.memoizedState=h;else{n=u.next;var C=x=null,G=null,it=n,mt=!1;do{var St=it.lane&-536870913;if(St!==it.lane?(Ae&St)===St:(la&St)===St){var lt=it.revertLane;if(lt===0)G!==null&&(G=G.next={lane:0,revertLane:0,gesture:null,action:it.action,hasEagerState:it.hasEagerState,eagerState:it.eagerState,next:null}),St===fr&&(mt=!0);else if((la&lt)===lt){it=it.next,lt===fr&&(mt=!0);continue}else St={lane:0,revertLane:it.revertLane,gesture:null,action:it.action,hasEagerState:it.hasEagerState,eagerState:it.eagerState,next:null},G===null?(C=G=St,x=h):G=G.next=St,_e.lanes|=lt,qa|=lt;St=it.action,Ns&&a(h,St),h=it.hasEagerState?it.eagerState:a(h,St)}else lt={lane:St,revertLane:it.revertLane,gesture:it.gesture,action:it.action,hasEagerState:it.hasEagerState,eagerState:it.eagerState,next:null},G===null?(C=G=lt,x=h):G=G.next=lt,_e.lanes|=St,qa|=St;it=it.next}while(it!==null&&it!==n);if(G===null?x=h:G.next=C,!si(h,e.memoizedState)&&(xn=!0,mt&&(a=hr,a!==null)))throw a;e.memoizedState=h,e.baseState=x,e.baseQueue=G,o.lastRenderedState=h}return u===null&&(o.lanes=0),[e.memoizedState,o.dispatch]}function Rf(e){var n=pn(),a=n.queue;if(a===null)throw Error(s(311));a.lastRenderedReducer=e;var o=a.dispatch,u=a.pending,h=n.memoizedState;if(u!==null){a.pending=null;var x=u=u.next;do h=e(h,x.action),x=x.next;while(x!==u);si(h,n.memoizedState)||(xn=!0),n.memoizedState=h,n.baseQueue===null&&(n.baseState=h),a.lastRenderedState=h}return[h,o]}function $m(e,n,a){var o=_e,u=pn(),h=De;if(h){if(a===void 0)throw Error(s(407));a=a()}else a=n();var x=!si((Ke||u).memoizedState,a);if(x&&(u.memoizedState=a,xn=!0),u=u.queue,Df(n0.bind(null,o,u,e),[e]),u.getSnapshot!==n||x||vn!==null&&vn.memoizedState.tag&1){if(o.flags|=2048,vr(9,{destroy:void 0},e0.bind(null,o,u,a,n),null),tn===null)throw Error(s(349));h||(la&127)!==0||t0(o,n,a)}return a}function t0(e,n,a){e.flags|=16384,e={getSnapshot:n,value:a},n=_e.updateQueue,n===null?(n=Zl(),_e.updateQueue=n,n.stores=[e]):(a=n.stores,a===null?n.stores=[e]:a.push(e))}function e0(e,n,a,o){n.value=a,n.getSnapshot=o,i0(n)&&a0(e)}function n0(e,n,a){return a(function(){i0(n)&&a0(e)})}function i0(e){var n=e.getSnapshot;e=e.value;try{var a=n();return!si(e,a)}catch{return!0}}function a0(e){var n=Es(e,2);n!==null&&$n(n,e,2)}function Cf(e){var n=kn();if(typeof e=="function"){var a=e;if(e=a(),Ns){wt(!0);try{a()}finally{wt(!1)}}}return n.memoizedState=n.baseState=e,n.queue={pending:null,lanes:0,dispatch:null,lastRenderedReducer:ca,lastRenderedState:e},n}function s0(e,n,a,o){return e.baseState=a,Af(e,Ke,typeof o=="function"?o:ca)}function Fy(e,n,a,o,u){if($l(e))throw Error(s(485));if(e=n.action,e!==null){var h={payload:u,action:e,next:null,isTransition:!0,status:"pending",value:null,reason:null,listeners:[],then:function(x){h.listeners.push(x)}};P.T!==null?a(!0):h.isTransition=!1,o(h),a=n.pending,a===null?(h.next=n.pending=h,r0(n,h)):(h.next=a.next,n.pending=a.next=h)}}function r0(e,n){var a=n.action,o=n.payload,u=e.state;if(n.isTransition){var h=P.T,x={};P.T=x;try{var C=a(u,o),G=P.S;G!==null&&G(x,C),o0(e,n,C)}catch(it){wf(e,n,it)}finally{h!==null&&x.types!==null&&(h.types=x.types),P.T=h}}else try{h=a(u,o),o0(e,n,h)}catch(it){wf(e,n,it)}}function o0(e,n,a){a!==null&&typeof a=="object"&&typeof a.then=="function"?a.then(function(o){l0(e,n,o)},function(o){return wf(e,n,o)}):l0(e,n,a)}function l0(e,n,a){n.status="fulfilled",n.value=a,c0(n),e.state=a,n=e.pending,n!==null&&(a=n.next,a===n?e.pending=null:(a=a.next,n.next=a,r0(e,a)))}function wf(e,n,a){var o=e.pending;if(e.pending=null,o!==null){o=o.next;do n.status="rejected",n.reason=a,c0(n),n=n.next;while(n!==o)}e.action=null}function c0(e){e=e.listeners;for(var n=0;n<e.length;n++)(0,e[n])()}function u0(e,n){return n}function f0(e,n){if(De){var a=tn.formState;if(a!==null){t:{var o=_e;if(De){if(en){e:{for(var u=en,h=Ei;u.nodeType!==8;){if(!h){u=null;break e}if(u=Ti(u.nextSibling),u===null){u=null;break e}}h=u.data,u=h==="F!"||h==="F"?u:null}if(u){en=Ti(u.nextSibling),o=u.data==="F!";break t}}Ba(o)}o=!1}o&&(n=a[0])}}return a=kn(),a.memoizedState=a.baseState=n,o={pending:null,lanes:0,dispatch:null,lastRenderedReducer:u0,lastRenderedState:n},a.queue=o,a=D0.bind(null,_e,o),o.dispatch=a,o=Cf(!1),h=Pf.bind(null,_e,!1,o.queue),o=kn(),u={state:n,dispatch:null,action:e,pending:null},o.queue=u,a=Fy.bind(null,_e,u,h,a),u.dispatch=a,o.memoizedState=e,[n,a,!1]}function h0(e){var n=pn();return d0(n,Ke,e)}function d0(e,n,a){if(n=Af(e,n,u0)[0],e=Kl(ca)[0],typeof n=="object"&&n!==null&&typeof n.then=="function")try{var o=Uo(n)}catch(x){throw x===dr?Hl:x}else o=n;n=pn();var u=n.queue,h=u.dispatch;return a!==n.memoizedState&&(_e.flags|=2048,vr(9,{destroy:void 0},Hy.bind(null,u,a),null)),[o,h,e]}function Hy(e,n){e.action=n}function p0(e){var n=pn(),a=Ke;if(a!==null)return d0(n,a,e);pn(),n=n.memoizedState,a=pn();var o=a.queue.dispatch;return a.memoizedState=e,[n,o,!1]}function vr(e,n,a,o){return e={tag:e,create:a,deps:o,inst:n,next:null},n=_e.updateQueue,n===null&&(n=Zl(),_e.updateQueue=n),a=n.lastEffect,a===null?n.lastEffect=e.next=e:(o=a.next,a.next=e,e.next=o,n.lastEffect=e),e}function m0(){return pn().memoizedState}function Ql(e,n,a,o){var u=kn();_e.flags|=e,u.memoizedState=vr(1|n,{destroy:void 0},a,o===void 0?null:o)}function Jl(e,n,a,o){var u=pn();o=o===void 0?null:o;var h=u.memoizedState.inst;Ke!==null&&o!==null&&yf(o,Ke.memoizedState.deps)?u.memoizedState=vr(n,h,a,o):(_e.flags|=e,u.memoizedState=vr(1|n,h,a,o))}function g0(e,n){Ql(8390656,8,e,n)}function Df(e,n){Jl(2048,8,e,n)}function Gy(e){_e.flags|=4;var n=_e.updateQueue;if(n===null)n=Zl(),_e.updateQueue=n,n.events=[e];else{var a=n.events;a===null?n.events=[e]:a.push(e)}}function _0(e){var n=pn().memoizedState;return Gy({ref:n,nextImpl:e}),function(){if((Ie&2)!==0)throw Error(s(440));return n.impl.apply(void 0,arguments)}}function v0(e,n){return Jl(4,2,e,n)}function x0(e,n){return Jl(4,4,e,n)}function y0(e,n){if(typeof n=="function"){e=e();var a=n(e);return function(){typeof a=="function"?a():n(null)}}if(n!=null)return e=e(),n.current=e,function(){n.current=null}}function S0(e,n,a){a=a!=null?a.concat([e]):null,Jl(4,4,y0.bind(null,n,e),a)}function Uf(){}function M0(e,n){var a=pn();n=n===void 0?null:n;var o=a.memoizedState;return n!==null&&yf(n,o[1])?o[0]:(a.memoizedState=[e,n],e)}function E0(e,n){var a=pn();n=n===void 0?null:n;var o=a.memoizedState;if(n!==null&&yf(n,o[1]))return o[0];if(o=e(),Ns){wt(!0);try{e()}finally{wt(!1)}}return a.memoizedState=[o,n],o}function Nf(e,n,a){return a===void 0||(la&1073741824)!==0&&(Ae&261930)===0?e.memoizedState=n:(e.memoizedState=a,e=bg(),_e.lanes|=e,qa|=e,a)}function b0(e,n,a,o){return si(a,n)?a:mr.current!==null?(e=Nf(e,a,o),si(e,n)||(xn=!0),e):(la&42)===0||(la&1073741824)!==0&&(Ae&261930)===0?(xn=!0,e.memoizedState=a):(e=bg(),_e.lanes|=e,qa|=e,n)}function T0(e,n,a,o,u){var h=H.p;H.p=h!==0&&8>h?h:8;var x=P.T,C={};P.T=C,Pf(e,!1,n,a);try{var G=u(),it=P.S;if(it!==null&&it(C,G),G!==null&&typeof G=="object"&&typeof G.then=="function"){var mt=zy(G,o);No(e,n,mt,fi(e))}else No(e,n,o,fi(e))}catch(St){No(e,n,{then:function(){},status:"rejected",reason:St},fi())}finally{H.p=h,x!==null&&C.types!==null&&(x.types=C.types),P.T=x}}function Vy(){}function Lf(e,n,a,o){if(e.tag!==5)throw Error(s(476));var u=A0(e).queue;T0(e,u,n,et,a===null?Vy:function(){return R0(e),a(o)})}function A0(e){var n=e.memoizedState;if(n!==null)return n;n={memoizedState:et,baseState:et,baseQueue:null,queue:{pending:null,lanes:0,dispatch:null,lastRenderedReducer:ca,lastRenderedState:et},next:null};var a={};return n.next={memoizedState:a,baseState:a,baseQueue:null,queue:{pending:null,lanes:0,dispatch:null,lastRenderedReducer:ca,lastRenderedState:a},next:null},e.memoizedState=n,e=e.alternate,e!==null&&(e.memoizedState=n),n}function R0(e){var n=A0(e);n.next===null&&(n=e.alternate.memoizedState),No(e,n.next.queue,{},fi())}function Of(){return Ln(jo)}function C0(){return pn().memoizedState}function w0(){return pn().memoizedState}function ky(e){for(var n=e.return;n!==null;){switch(n.tag){case 24:case 3:var a=fi();e=Ha(a);var o=Ga(n,e,a);o!==null&&($n(o,n,a),Ro(o,n,a)),n={cache:cf()},e.payload=n;return}n=n.return}}function Xy(e,n,a){var o=fi();a={lane:o,revertLane:0,gesture:null,action:a,hasEagerState:!1,eagerState:null,next:null},$l(e)?U0(n,a):(a=Qu(e,n,a,o),a!==null&&($n(a,e,o),N0(a,n,o)))}function D0(e,n,a){var o=fi();No(e,n,a,o)}function No(e,n,a,o){var u={lane:o,revertLane:0,gesture:null,action:a,hasEagerState:!1,eagerState:null,next:null};if($l(e))U0(n,u);else{var h=e.alternate;if(e.lanes===0&&(h===null||h.lanes===0)&&(h=n.lastRenderedReducer,h!==null))try{var x=n.lastRenderedState,C=h(x,a);if(u.hasEagerState=!0,u.eagerState=C,si(C,x))return Ll(e,n,u,0),tn===null&&Nl(),!1}catch{}finally{}if(a=Qu(e,n,u,o),a!==null)return $n(a,e,o),N0(a,n,o),!0}return!1}function Pf(e,n,a,o){if(o={lane:2,revertLane:dh(),gesture:null,action:o,hasEagerState:!1,eagerState:null,next:null},$l(e)){if(n)throw Error(s(479))}else n=Qu(e,a,o,2),n!==null&&$n(n,e,2)}function $l(e){var n=e.alternate;return e===_e||n!==null&&n===_e}function U0(e,n){gr=ql=!0;var a=e.pending;a===null?n.next=n:(n.next=a.next,a.next=n),e.pending=n}function N0(e,n,a){if((a&4194048)!==0){var o=n.lanes;o&=e.pendingLanes,a|=o,n.lanes=a,ei(e,a)}}var Lo={readContext:Ln,use:jl,useCallback:un,useContext:un,useEffect:un,useImperativeHandle:un,useLayoutEffect:un,useInsertionEffect:un,useMemo:un,useReducer:un,useRef:un,useState:un,useDebugValue:un,useDeferredValue:un,useTransition:un,useSyncExternalStore:un,useId:un,useHostTransitionStatus:un,useFormState:un,useActionState:un,useOptimistic:un,useMemoCache:un,useCacheRefresh:un};Lo.useEffectEvent=un;var L0={readContext:Ln,use:jl,useCallback:function(e,n){return kn().memoizedState=[e,n===void 0?null:n],e},useContext:Ln,useEffect:g0,useImperativeHandle:function(e,n,a){a=a!=null?a.concat([e]):null,Ql(4194308,4,y0.bind(null,n,e),a)},useLayoutEffect:function(e,n){return Ql(4194308,4,e,n)},useInsertionEffect:function(e,n){Ql(4,2,e,n)},useMemo:function(e,n){var a=kn();n=n===void 0?null:n;var o=e();if(Ns){wt(!0);try{e()}finally{wt(!1)}}return a.memoizedState=[o,n],o},useReducer:function(e,n,a){var o=kn();if(a!==void 0){var u=a(n);if(Ns){wt(!0);try{a(n)}finally{wt(!1)}}}else u=n;return o.memoizedState=o.baseState=u,e={pending:null,lanes:0,dispatch:null,lastRenderedReducer:e,lastRenderedState:u},o.queue=e,e=e.dispatch=Xy.bind(null,_e,e),[o.memoizedState,e]},useRef:function(e){var n=kn();return e={current:e},n.memoizedState=e},useState:function(e){e=Cf(e);var n=e.queue,a=D0.bind(null,_e,n);return n.dispatch=a,[e.memoizedState,a]},useDebugValue:Uf,useDeferredValue:function(e,n){var a=kn();return Nf(a,e,n)},useTransition:function(){var e=Cf(!1);return e=T0.bind(null,_e,e.queue,!0,!1),kn().memoizedState=e,[!1,e]},useSyncExternalStore:function(e,n,a){var o=_e,u=kn();if(De){if(a===void 0)throw Error(s(407));a=a()}else{if(a=n(),tn===null)throw Error(s(349));(Ae&127)!==0||t0(o,n,a)}u.memoizedState=a;var h={value:a,getSnapshot:n};return u.queue=h,g0(n0.bind(null,o,h,e),[e]),o.flags|=2048,vr(9,{destroy:void 0},e0.bind(null,o,h,a,n),null),a},useId:function(){var e=kn(),n=tn.identifierPrefix;if(De){var a=ki,o=Vi;a=(o&~(1<<32-Ht(o)-1)).toString(32)+a,n="_"+n+"R_"+a,a=Yl++,0<a&&(n+="H"+a.toString(32)),n+="_"}else a=By++,n="_"+n+"r_"+a.toString(32)+"_";return e.memoizedState=n},useHostTransitionStatus:Of,useFormState:f0,useActionState:f0,useOptimistic:function(e){var n=kn();n.memoizedState=n.baseState=e;var a={pending:null,lanes:0,dispatch:null,lastRenderedReducer:null,lastRenderedState:null};return n.queue=a,n=Pf.bind(null,_e,!0,a),a.dispatch=n,[e,n]},useMemoCache:Tf,useCacheRefresh:function(){return kn().memoizedState=ky.bind(null,_e)},useEffectEvent:function(e){var n=kn(),a={impl:e};return n.memoizedState=a,function(){if((Ie&2)!==0)throw Error(s(440));return a.impl.apply(void 0,arguments)}}},zf={readContext:Ln,use:jl,useCallback:M0,useContext:Ln,useEffect:Df,useImperativeHandle:S0,useInsertionEffect:v0,useLayoutEffect:x0,useMemo:E0,useReducer:Kl,useRef:m0,useState:function(){return Kl(ca)},useDebugValue:Uf,useDeferredValue:function(e,n){var a=pn();return b0(a,Ke.memoizedState,e,n)},useTransition:function(){var e=Kl(ca)[0],n=pn().memoizedState;return[typeof e=="boolean"?e:Uo(e),n]},useSyncExternalStore:$m,useId:C0,useHostTransitionStatus:Of,useFormState:h0,useActionState:h0,useOptimistic:function(e,n){var a=pn();return s0(a,Ke,e,n)},useMemoCache:Tf,useCacheRefresh:w0};zf.useEffectEvent=_0;var O0={readContext:Ln,use:jl,useCallback:M0,useContext:Ln,useEffect:Df,useImperativeHandle:S0,useInsertionEffect:v0,useLayoutEffect:x0,useMemo:E0,useReducer:Rf,useRef:m0,useState:function(){return Rf(ca)},useDebugValue:Uf,useDeferredValue:function(e,n){var a=pn();return Ke===null?Nf(a,e,n):b0(a,Ke.memoizedState,e,n)},useTransition:function(){var e=Rf(ca)[0],n=pn().memoizedState;return[typeof e=="boolean"?e:Uo(e),n]},useSyncExternalStore:$m,useId:C0,useHostTransitionStatus:Of,useFormState:p0,useActionState:p0,useOptimistic:function(e,n){var a=pn();return Ke!==null?s0(a,Ke,e,n):(a.baseState=e,[e,a.queue.dispatch])},useMemoCache:Tf,useCacheRefresh:w0};O0.useEffectEvent=_0;function Bf(e,n,a,o){n=e.memoizedState,a=a(o,n),a=a==null?n:v({},n,a),e.memoizedState=a,e.lanes===0&&(e.updateQueue.baseState=a)}var If={enqueueSetState:function(e,n,a){e=e._reactInternals;var o=fi(),u=Ha(o);u.payload=n,a!=null&&(u.callback=a),n=Ga(e,u,o),n!==null&&($n(n,e,o),Ro(n,e,o))},enqueueReplaceState:function(e,n,a){e=e._reactInternals;var o=fi(),u=Ha(o);u.tag=1,u.payload=n,a!=null&&(u.callback=a),n=Ga(e,u,o),n!==null&&($n(n,e,o),Ro(n,e,o))},enqueueForceUpdate:function(e,n){e=e._reactInternals;var a=fi(),o=Ha(a);o.tag=2,n!=null&&(o.callback=n),n=Ga(e,o,a),n!==null&&($n(n,e,a),Ro(n,e,a))}};function P0(e,n,a,o,u,h,x){return e=e.stateNode,typeof e.shouldComponentUpdate=="function"?e.shouldComponentUpdate(o,h,x):n.prototype&&n.prototype.isPureReactComponent?!xo(a,o)||!xo(u,h):!0}function z0(e,n,a,o){e=n.state,typeof n.componentWillReceiveProps=="function"&&n.componentWillReceiveProps(a,o),typeof n.UNSAFE_componentWillReceiveProps=="function"&&n.UNSAFE_componentWillReceiveProps(a,o),n.state!==e&&If.enqueueReplaceState(n,n.state,null)}function Ls(e,n){var a=n;if("ref"in n){a={};for(var o in n)o!=="ref"&&(a[o]=n[o])}if(e=e.defaultProps){a===n&&(a=v({},a));for(var u in e)a[u]===void 0&&(a[u]=e[u])}return a}function B0(e){Ul(e)}function I0(e){console.error(e)}function F0(e){Ul(e)}function tc(e,n){try{var a=e.onUncaughtError;a(n.value,{componentStack:n.stack})}catch(o){setTimeout(function(){throw o})}}function H0(e,n,a){try{var o=e.onCaughtError;o(a.value,{componentStack:a.stack,errorBoundary:n.tag===1?n.stateNode:null})}catch(u){setTimeout(function(){throw u})}}function Ff(e,n,a){return a=Ha(a),a.tag=3,a.payload={element:null},a.callback=function(){tc(e,n)},a}function G0(e){return e=Ha(e),e.tag=3,e}function V0(e,n,a,o){var u=a.type.getDerivedStateFromError;if(typeof u=="function"){var h=o.value;e.payload=function(){return u(h)},e.callback=function(){H0(n,a,o)}}var x=a.stateNode;x!==null&&typeof x.componentDidCatch=="function"&&(e.callback=function(){H0(n,a,o),typeof u!="function"&&(Ya===null?Ya=new Set([this]):Ya.add(this));var C=o.stack;this.componentDidCatch(o.value,{componentStack:C!==null?C:""})})}function Wy(e,n,a,o,u){if(a.flags|=32768,o!==null&&typeof o=="object"&&typeof o.then=="function"){if(n=a.alternate,n!==null&&ur(n,a,u,!0),a=oi.current,a!==null){switch(a.tag){case 31:case 13:return bi===null?hc():a.alternate===null&&fn===0&&(fn=3),a.flags&=-257,a.flags|=65536,a.lanes=u,o===Gl?a.flags|=16384:(n=a.updateQueue,n===null?a.updateQueue=new Set([o]):n.add(o),uh(e,o,u)),!1;case 22:return a.flags|=65536,o===Gl?a.flags|=16384:(n=a.updateQueue,n===null?(n={transitions:null,markerInstances:null,retryQueue:new Set([o])},a.updateQueue=n):(a=n.retryQueue,a===null?n.retryQueue=new Set([o]):a.add(o)),uh(e,o,u)),!1}throw Error(s(435,a.tag))}return uh(e,o,u),hc(),!1}if(De)return n=oi.current,n!==null?((n.flags&65536)===0&&(n.flags|=256),n.flags|=65536,n.lanes=u,o!==af&&(e=Error(s(422),{cause:o}),Mo(yi(e,a)))):(o!==af&&(n=Error(s(423),{cause:o}),Mo(yi(n,a))),e=e.current.alternate,e.flags|=65536,u&=-u,e.lanes|=u,o=yi(o,a),u=Ff(e.stateNode,o,u),mf(e,u),fn!==4&&(fn=2)),!1;var h=Error(s(520),{cause:o});if(h=yi(h,a),Go===null?Go=[h]:Go.push(h),fn!==4&&(fn=2),n===null)return!0;o=yi(o,a),a=n;do{switch(a.tag){case 3:return a.flags|=65536,e=u&-u,a.lanes|=e,e=Ff(a.stateNode,o,e),mf(a,e),!1;case 1:if(n=a.type,h=a.stateNode,(a.flags&128)===0&&(typeof n.getDerivedStateFromError=="function"||h!==null&&typeof h.componentDidCatch=="function"&&(Ya===null||!Ya.has(h))))return a.flags|=65536,u&=-u,a.lanes|=u,u=G0(u),V0(u,e,a,o),mf(a,u),!1}a=a.return}while(a!==null);return!1}var Hf=Error(s(461)),xn=!1;function On(e,n,a,o){n.child=e===null?qm(n,null,a,o):Us(n,e.child,a,o)}function k0(e,n,a,o,u){a=a.render;var h=n.ref;if("ref"in o){var x={};for(var C in o)C!=="ref"&&(x[C]=o[C])}else x=o;return Rs(n),o=Sf(e,n,a,x,h,u),C=Mf(),e!==null&&!xn?(Ef(e,n,u),ua(e,n,u)):(De&&C&&ef(n),n.flags|=1,On(e,n,o,u),n.child)}function X0(e,n,a,o,u){if(e===null){var h=a.type;return typeof h=="function"&&!Ju(h)&&h.defaultProps===void 0&&a.compare===null?(n.tag=15,n.type=h,W0(e,n,h,o,u)):(e=Pl(a.type,null,o,n,n.mode,u),e.ref=n.ref,e.return=n,n.child=e)}if(h=e.child,!Zf(e,u)){var x=h.memoizedProps;if(a=a.compare,a=a!==null?a:xo,a(x,o)&&e.ref===n.ref)return ua(e,n,u)}return n.flags|=1,e=aa(h,o),e.ref=n.ref,e.return=n,n.child=e}function W0(e,n,a,o,u){if(e!==null){var h=e.memoizedProps;if(xo(h,o)&&e.ref===n.ref)if(xn=!1,n.pendingProps=o=h,Zf(e,u))(e.flags&131072)!==0&&(xn=!0);else return n.lanes=e.lanes,ua(e,n,u)}return Gf(e,n,a,o,u)}function q0(e,n,a,o){var u=o.children,h=e!==null?e.memoizedState:null;if(e===null&&n.stateNode===null&&(n.stateNode={_visibility:1,_pendingMarkers:null,_retryCache:null,_transitions:null}),o.mode==="hidden"){if((n.flags&128)!==0){if(h=h!==null?h.baseLanes|a:a,e!==null){for(o=n.child=e.child,u=0;o!==null;)u=u|o.lanes|o.childLanes,o=o.sibling;o=u&~h}else o=0,n.child=null;return Y0(e,n,h,a,o)}if((a&536870912)!==0)n.memoizedState={baseLanes:0,cachePool:null},e!==null&&Fl(n,h!==null?h.cachePool:null),h!==null?jm(n,h):_f(),Km(n);else return o=n.lanes=536870912,Y0(e,n,h!==null?h.baseLanes|a:a,a,o)}else h!==null?(Fl(n,h.cachePool),jm(n,h),ka(),n.memoizedState=null):(e!==null&&Fl(n,null),_f(),ka());return On(e,n,u,a),n.child}function Oo(e,n){return e!==null&&e.tag===22||n.stateNode!==null||(n.stateNode={_visibility:1,_pendingMarkers:null,_retryCache:null,_transitions:null}),n.sibling}function Y0(e,n,a,o,u){var h=ff();return h=h===null?null:{parent:_n._currentValue,pool:h},n.memoizedState={baseLanes:a,cachePool:h},e!==null&&Fl(n,null),_f(),Km(n),e!==null&&ur(e,n,o,!0),n.childLanes=u,null}function ec(e,n){return n=ic({mode:n.mode,children:n.children},e.mode),n.ref=e.ref,e.child=n,n.return=e,n}function Z0(e,n,a){return Us(n,e.child,null,a),e=ec(n,n.pendingProps),e.flags|=2,li(n),n.memoizedState=null,e}function qy(e,n,a){var o=n.pendingProps,u=(n.flags&128)!==0;if(n.flags&=-129,e===null){if(De){if(o.mode==="hidden")return e=ec(n,o),n.lanes=536870912,Oo(null,e);if(xf(n),(e=en)?(e=r_(e,Ei),e=e!==null&&e.data==="&"?e:null,e!==null&&(n.memoizedState={dehydrated:e,treeContext:Pa!==null?{id:Vi,overflow:ki}:null,retryLane:536870912,hydrationErrors:null},a=Um(e),a.return=n,n.child=a,Nn=n,en=null)):e=null,e===null)throw Ba(n);return n.lanes=536870912,null}return ec(n,o)}var h=e.memoizedState;if(h!==null){var x=h.dehydrated;if(xf(n),u)if(n.flags&256)n.flags&=-257,n=Z0(e,n,a);else if(n.memoizedState!==null)n.child=e.child,n.flags|=128,n=null;else throw Error(s(558));else if(xn||ur(e,n,a,!1),u=(a&e.childLanes)!==0,xn||u){if(o=tn,o!==null&&(x=ni(o,a),x!==0&&x!==h.retryLane))throw h.retryLane=x,Es(e,x),$n(o,e,x),Hf;hc(),n=Z0(e,n,a)}else e=h.treeContext,en=Ti(x.nextSibling),Nn=n,De=!0,za=null,Ei=!1,e!==null&&Om(n,e),n=ec(n,o),n.flags|=4096;return n}return e=aa(e.child,{mode:o.mode,children:o.children}),e.ref=n.ref,n.child=e,e.return=n,e}function nc(e,n){var a=n.ref;if(a===null)e!==null&&e.ref!==null&&(n.flags|=4194816);else{if(typeof a!="function"&&typeof a!="object")throw Error(s(284));(e===null||e.ref!==a)&&(n.flags|=4194816)}}function Gf(e,n,a,o,u){return Rs(n),a=Sf(e,n,a,o,void 0,u),o=Mf(),e!==null&&!xn?(Ef(e,n,u),ua(e,n,u)):(De&&o&&ef(n),n.flags|=1,On(e,n,a,u),n.child)}function j0(e,n,a,o,u,h){return Rs(n),n.updateQueue=null,a=Jm(n,o,a,u),Qm(e),o=Mf(),e!==null&&!xn?(Ef(e,n,h),ua(e,n,h)):(De&&o&&ef(n),n.flags|=1,On(e,n,a,h),n.child)}function K0(e,n,a,o,u){if(Rs(n),n.stateNode===null){var h=rr,x=a.contextType;typeof x=="object"&&x!==null&&(h=Ln(x)),h=new a(o,h),n.memoizedState=h.state!==null&&h.state!==void 0?h.state:null,h.updater=If,n.stateNode=h,h._reactInternals=n,h=n.stateNode,h.props=o,h.state=n.memoizedState,h.refs={},df(n),x=a.contextType,h.context=typeof x=="object"&&x!==null?Ln(x):rr,h.state=n.memoizedState,x=a.getDerivedStateFromProps,typeof x=="function"&&(Bf(n,a,x,o),h.state=n.memoizedState),typeof a.getDerivedStateFromProps=="function"||typeof h.getSnapshotBeforeUpdate=="function"||typeof h.UNSAFE_componentWillMount!="function"&&typeof h.componentWillMount!="function"||(x=h.state,typeof h.componentWillMount=="function"&&h.componentWillMount(),typeof h.UNSAFE_componentWillMount=="function"&&h.UNSAFE_componentWillMount(),x!==h.state&&If.enqueueReplaceState(h,h.state,null),wo(n,o,h,u),Co(),h.state=n.memoizedState),typeof h.componentDidMount=="function"&&(n.flags|=4194308),o=!0}else if(e===null){h=n.stateNode;var C=n.memoizedProps,G=Ls(a,C);h.props=G;var it=h.context,mt=a.contextType;x=rr,typeof mt=="object"&&mt!==null&&(x=Ln(mt));var St=a.getDerivedStateFromProps;mt=typeof St=="function"||typeof h.getSnapshotBeforeUpdate=="function",C=n.pendingProps!==C,mt||typeof h.UNSAFE_componentWillReceiveProps!="function"&&typeof h.componentWillReceiveProps!="function"||(C||it!==x)&&z0(n,h,o,x),Fa=!1;var lt=n.memoizedState;h.state=lt,wo(n,o,h,u),Co(),it=n.memoizedState,C||lt!==it||Fa?(typeof St=="function"&&(Bf(n,a,St,o),it=n.memoizedState),(G=Fa||P0(n,a,G,o,lt,it,x))?(mt||typeof h.UNSAFE_componentWillMount!="function"&&typeof h.componentWillMount!="function"||(typeof h.componentWillMount=="function"&&h.componentWillMount(),typeof h.UNSAFE_componentWillMount=="function"&&h.UNSAFE_componentWillMount()),typeof h.componentDidMount=="function"&&(n.flags|=4194308)):(typeof h.componentDidMount=="function"&&(n.flags|=4194308),n.memoizedProps=o,n.memoizedState=it),h.props=o,h.state=it,h.context=x,o=G):(typeof h.componentDidMount=="function"&&(n.flags|=4194308),o=!1)}else{h=n.stateNode,pf(e,n),x=n.memoizedProps,mt=Ls(a,x),h.props=mt,St=n.pendingProps,lt=h.context,it=a.contextType,G=rr,typeof it=="object"&&it!==null&&(G=Ln(it)),C=a.getDerivedStateFromProps,(it=typeof C=="function"||typeof h.getSnapshotBeforeUpdate=="function")||typeof h.UNSAFE_componentWillReceiveProps!="function"&&typeof h.componentWillReceiveProps!="function"||(x!==St||lt!==G)&&z0(n,h,o,G),Fa=!1,lt=n.memoizedState,h.state=lt,wo(n,o,h,u),Co();var ct=n.memoizedState;x!==St||lt!==ct||Fa||e!==null&&e.dependencies!==null&&Bl(e.dependencies)?(typeof C=="function"&&(Bf(n,a,C,o),ct=n.memoizedState),(mt=Fa||P0(n,a,mt,o,lt,ct,G)||e!==null&&e.dependencies!==null&&Bl(e.dependencies))?(it||typeof h.UNSAFE_componentWillUpdate!="function"&&typeof h.componentWillUpdate!="function"||(typeof h.componentWillUpdate=="function"&&h.componentWillUpdate(o,ct,G),typeof h.UNSAFE_componentWillUpdate=="function"&&h.UNSAFE_componentWillUpdate(o,ct,G)),typeof h.componentDidUpdate=="function"&&(n.flags|=4),typeof h.getSnapshotBeforeUpdate=="function"&&(n.flags|=1024)):(typeof h.componentDidUpdate!="function"||x===e.memoizedProps&&lt===e.memoizedState||(n.flags|=4),typeof h.getSnapshotBeforeUpdate!="function"||x===e.memoizedProps&&lt===e.memoizedState||(n.flags|=1024),n.memoizedProps=o,n.memoizedState=ct),h.props=o,h.state=ct,h.context=G,o=mt):(typeof h.componentDidUpdate!="function"||x===e.memoizedProps&&lt===e.memoizedState||(n.flags|=4),typeof h.getSnapshotBeforeUpdate!="function"||x===e.memoizedProps&&lt===e.memoizedState||(n.flags|=1024),o=!1)}return h=o,nc(e,n),o=(n.flags&128)!==0,h||o?(h=n.stateNode,a=o&&typeof a.getDerivedStateFromError!="function"?null:h.render(),n.flags|=1,e!==null&&o?(n.child=Us(n,e.child,null,u),n.child=Us(n,null,a,u)):On(e,n,a,u),n.memoizedState=h.state,e=n.child):e=ua(e,n,u),e}function Q0(e,n,a,o){return Ts(),n.flags|=256,On(e,n,a,o),n.child}var Vf={dehydrated:null,treeContext:null,retryLane:0,hydrationErrors:null};function kf(e){return{baseLanes:e,cachePool:Hm()}}function Xf(e,n,a){return e=e!==null?e.childLanes&~a:0,n&&(e|=ui),e}function J0(e,n,a){var o=n.pendingProps,u=!1,h=(n.flags&128)!==0,x;if((x=h)||(x=e!==null&&e.memoizedState===null?!1:(dn.current&2)!==0),x&&(u=!0,n.flags&=-129),x=(n.flags&32)!==0,n.flags&=-33,e===null){if(De){if(u?Va(n):ka(),(e=en)?(e=r_(e,Ei),e=e!==null&&e.data!=="&"?e:null,e!==null&&(n.memoizedState={dehydrated:e,treeContext:Pa!==null?{id:Vi,overflow:ki}:null,retryLane:536870912,hydrationErrors:null},a=Um(e),a.return=n,n.child=a,Nn=n,en=null)):e=null,e===null)throw Ba(n);return Ah(e)?n.lanes=32:n.lanes=536870912,null}var C=o.children;return o=o.fallback,u?(ka(),u=n.mode,C=ic({mode:"hidden",children:C},u),o=bs(o,u,a,null),C.return=n,o.return=n,C.sibling=o,n.child=C,o=n.child,o.memoizedState=kf(a),o.childLanes=Xf(e,x,a),n.memoizedState=Vf,Oo(null,o)):(Va(n),Wf(n,C))}var G=e.memoizedState;if(G!==null&&(C=G.dehydrated,C!==null)){if(h)n.flags&256?(Va(n),n.flags&=-257,n=qf(e,n,a)):n.memoizedState!==null?(ka(),n.child=e.child,n.flags|=128,n=null):(ka(),C=o.fallback,u=n.mode,o=ic({mode:"visible",children:o.children},u),C=bs(C,u,a,null),C.flags|=2,o.return=n,C.return=n,o.sibling=C,n.child=o,Us(n,e.child,null,a),o=n.child,o.memoizedState=kf(a),o.childLanes=Xf(e,x,a),n.memoizedState=Vf,n=Oo(null,o));else if(Va(n),Ah(C)){if(x=C.nextSibling&&C.nextSibling.dataset,x)var it=x.dgst;x=it,o=Error(s(419)),o.stack="",o.digest=x,Mo({value:o,source:null,stack:null}),n=qf(e,n,a)}else if(xn||ur(e,n,a,!1),x=(a&e.childLanes)!==0,xn||x){if(x=tn,x!==null&&(o=ni(x,a),o!==0&&o!==G.retryLane))throw G.retryLane=o,Es(e,o),$n(x,e,o),Hf;Th(C)||hc(),n=qf(e,n,a)}else Th(C)?(n.flags|=192,n.child=e.child,n=null):(e=G.treeContext,en=Ti(C.nextSibling),Nn=n,De=!0,za=null,Ei=!1,e!==null&&Om(n,e),n=Wf(n,o.children),n.flags|=4096);return n}return u?(ka(),C=o.fallback,u=n.mode,G=e.child,it=G.sibling,o=aa(G,{mode:"hidden",children:o.children}),o.subtreeFlags=G.subtreeFlags&65011712,it!==null?C=aa(it,C):(C=bs(C,u,a,null),C.flags|=2),C.return=n,o.return=n,o.sibling=C,n.child=o,Oo(null,o),o=n.child,C=e.child.memoizedState,C===null?C=kf(a):(u=C.cachePool,u!==null?(G=_n._currentValue,u=u.parent!==G?{parent:G,pool:G}:u):u=Hm(),C={baseLanes:C.baseLanes|a,cachePool:u}),o.memoizedState=C,o.childLanes=Xf(e,x,a),n.memoizedState=Vf,Oo(e.child,o)):(Va(n),a=e.child,e=a.sibling,a=aa(a,{mode:"visible",children:o.children}),a.return=n,a.sibling=null,e!==null&&(x=n.deletions,x===null?(n.deletions=[e],n.flags|=16):x.push(e)),n.child=a,n.memoizedState=null,a)}function Wf(e,n){return n=ic({mode:"visible",children:n},e.mode),n.return=e,e.child=n}function ic(e,n){return e=ri(22,e,null,n),e.lanes=0,e}function qf(e,n,a){return Us(n,e.child,null,a),e=Wf(n,n.pendingProps.children),e.flags|=2,n.memoizedState=null,e}function $0(e,n,a){e.lanes|=n;var o=e.alternate;o!==null&&(o.lanes|=n),of(e.return,n,a)}function Yf(e,n,a,o,u,h){var x=e.memoizedState;x===null?e.memoizedState={isBackwards:n,rendering:null,renderingStartTime:0,last:o,tail:a,tailMode:u,treeForkCount:h}:(x.isBackwards=n,x.rendering=null,x.renderingStartTime=0,x.last=o,x.tail=a,x.tailMode=u,x.treeForkCount=h)}function tg(e,n,a){var o=n.pendingProps,u=o.revealOrder,h=o.tail;o=o.children;var x=dn.current,C=(x&2)!==0;if(C?(x=x&1|2,n.flags|=128):x&=1,vt(dn,x),On(e,n,o,a),o=De?So:0,!C&&e!==null&&(e.flags&128)!==0)t:for(e=n.child;e!==null;){if(e.tag===13)e.memoizedState!==null&&$0(e,a,n);else if(e.tag===19)$0(e,a,n);else if(e.child!==null){e.child.return=e,e=e.child;continue}if(e===n)break t;for(;e.sibling===null;){if(e.return===null||e.return===n)break t;e=e.return}e.sibling.return=e.return,e=e.sibling}switch(u){case"forwards":for(a=n.child,u=null;a!==null;)e=a.alternate,e!==null&&Wl(e)===null&&(u=a),a=a.sibling;a=u,a===null?(u=n.child,n.child=null):(u=a.sibling,a.sibling=null),Yf(n,!1,u,a,h,o);break;case"backwards":case"unstable_legacy-backwards":for(a=null,u=n.child,n.child=null;u!==null;){if(e=u.alternate,e!==null&&Wl(e)===null){n.child=u;break}e=u.sibling,u.sibling=a,a=u,u=e}Yf(n,!0,a,null,h,o);break;case"together":Yf(n,!1,null,null,void 0,o);break;default:n.memoizedState=null}return n.child}function ua(e,n,a){if(e!==null&&(n.dependencies=e.dependencies),qa|=n.lanes,(a&n.childLanes)===0)if(e!==null){if(ur(e,n,a,!1),(a&n.childLanes)===0)return null}else return null;if(e!==null&&n.child!==e.child)throw Error(s(153));if(n.child!==null){for(e=n.child,a=aa(e,e.pendingProps),n.child=a,a.return=n;e.sibling!==null;)e=e.sibling,a=a.sibling=aa(e,e.pendingProps),a.return=n;a.sibling=null}return n.child}function Zf(e,n){return(e.lanes&n)!==0?!0:(e=e.dependencies,!!(e!==null&&Bl(e)))}function Yy(e,n,a){switch(n.tag){case 3:Mt(n,n.stateNode.containerInfo),Ia(n,_n,e.memoizedState.cache),Ts();break;case 27:case 5:ae(n);break;case 4:Mt(n,n.stateNode.containerInfo);break;case 10:Ia(n,n.type,n.memoizedProps.value);break;case 31:if(n.memoizedState!==null)return n.flags|=128,xf(n),null;break;case 13:var o=n.memoizedState;if(o!==null)return o.dehydrated!==null?(Va(n),n.flags|=128,null):(a&n.child.childLanes)!==0?J0(e,n,a):(Va(n),e=ua(e,n,a),e!==null?e.sibling:null);Va(n);break;case 19:var u=(e.flags&128)!==0;if(o=(a&n.childLanes)!==0,o||(ur(e,n,a,!1),o=(a&n.childLanes)!==0),u){if(o)return tg(e,n,a);n.flags|=128}if(u=n.memoizedState,u!==null&&(u.rendering=null,u.tail=null,u.lastEffect=null),vt(dn,dn.current),o)break;return null;case 22:return n.lanes=0,q0(e,n,a,n.pendingProps);case 24:Ia(n,_n,e.memoizedState.cache)}return ua(e,n,a)}function eg(e,n,a){if(e!==null)if(e.memoizedProps!==n.pendingProps)xn=!0;else{if(!Zf(e,a)&&(n.flags&128)===0)return xn=!1,Yy(e,n,a);xn=(e.flags&131072)!==0}else xn=!1,De&&(n.flags&1048576)!==0&&Lm(n,So,n.index);switch(n.lanes=0,n.tag){case 16:t:{var o=n.pendingProps;if(e=ws(n.elementType),n.type=e,typeof e=="function")Ju(e)?(o=Ls(e,o),n.tag=1,n=K0(null,n,e,o,a)):(n.tag=0,n=Gf(null,n,e,o,a));else{if(e!=null){var u=e.$$typeof;if(u===w){n.tag=11,n=k0(null,n,e,o,a);break t}else if(u===D){n.tag=14,n=X0(null,n,e,o,a);break t}}throw n=gt(e)||e,Error(s(306,n,""))}}return n;case 0:return Gf(e,n,n.type,n.pendingProps,a);case 1:return o=n.type,u=Ls(o,n.pendingProps),K0(e,n,o,u,a);case 3:t:{if(Mt(n,n.stateNode.containerInfo),e===null)throw Error(s(387));o=n.pendingProps;var h=n.memoizedState;u=h.element,pf(e,n),wo(n,o,null,a);var x=n.memoizedState;if(o=x.cache,Ia(n,_n,o),o!==h.cache&&lf(n,[_n],a,!0),Co(),o=x.element,h.isDehydrated)if(h={element:o,isDehydrated:!1,cache:x.cache},n.updateQueue.baseState=h,n.memoizedState=h,n.flags&256){n=Q0(e,n,o,a);break t}else if(o!==u){u=yi(Error(s(424)),n),Mo(u),n=Q0(e,n,o,a);break t}else{switch(e=n.stateNode.containerInfo,e.nodeType){case 9:e=e.body;break;default:e=e.nodeName==="HTML"?e.ownerDocument.body:e}for(en=Ti(e.firstChild),Nn=n,De=!0,za=null,Ei=!0,a=qm(n,null,o,a),n.child=a;a;)a.flags=a.flags&-3|4096,a=a.sibling}else{if(Ts(),o===u){n=ua(e,n,a);break t}On(e,n,o,a)}n=n.child}return n;case 26:return nc(e,n),e===null?(a=h_(n.type,null,n.pendingProps,null))?n.memoizedState=a:De||(a=n.type,e=n.pendingProps,o=xc(tt.current).createElement(a),o[mn]=n,o[Un]=e,Pn(o,a,e),gn(o),n.stateNode=o):n.memoizedState=h_(n.type,e.memoizedProps,n.pendingProps,e.memoizedState),null;case 27:return ae(n),e===null&&De&&(o=n.stateNode=c_(n.type,n.pendingProps,tt.current),Nn=n,Ei=!0,u=en,Qa(n.type)?(Rh=u,en=Ti(o.firstChild)):en=u),On(e,n,n.pendingProps.children,a),nc(e,n),e===null&&(n.flags|=4194304),n.child;case 5:return e===null&&De&&((u=o=en)&&(o=ES(o,n.type,n.pendingProps,Ei),o!==null?(n.stateNode=o,Nn=n,en=Ti(o.firstChild),Ei=!1,u=!0):u=!1),u||Ba(n)),ae(n),u=n.type,h=n.pendingProps,x=e!==null?e.memoizedProps:null,o=h.children,Mh(u,h)?o=null:x!==null&&Mh(u,x)&&(n.flags|=32),n.memoizedState!==null&&(u=Sf(e,n,Iy,null,null,a),jo._currentValue=u),nc(e,n),On(e,n,o,a),n.child;case 6:return e===null&&De&&((e=a=en)&&(a=bS(a,n.pendingProps,Ei),a!==null?(n.stateNode=a,Nn=n,en=null,e=!0):e=!1),e||Ba(n)),null;case 13:return J0(e,n,a);case 4:return Mt(n,n.stateNode.containerInfo),o=n.pendingProps,e===null?n.child=Us(n,null,o,a):On(e,n,o,a),n.child;case 11:return k0(e,n,n.type,n.pendingProps,a);case 7:return On(e,n,n.pendingProps,a),n.child;case 8:return On(e,n,n.pendingProps.children,a),n.child;case 12:return On(e,n,n.pendingProps.children,a),n.child;case 10:return o=n.pendingProps,Ia(n,n.type,o.value),On(e,n,o.children,a),n.child;case 9:return u=n.type._context,o=n.pendingProps.children,Rs(n),u=Ln(u),o=o(u),n.flags|=1,On(e,n,o,a),n.child;case 14:return X0(e,n,n.type,n.pendingProps,a);case 15:return W0(e,n,n.type,n.pendingProps,a);case 19:return tg(e,n,a);case 31:return qy(e,n,a);case 22:return q0(e,n,a,n.pendingProps);case 24:return Rs(n),o=Ln(_n),e===null?(u=ff(),u===null&&(u=tn,h=cf(),u.pooledCache=h,h.refCount++,h!==null&&(u.pooledCacheLanes|=a),u=h),n.memoizedState={parent:o,cache:u},df(n),Ia(n,_n,u)):((e.lanes&a)!==0&&(pf(e,n),wo(n,null,null,a),Co()),u=e.memoizedState,h=n.memoizedState,u.parent!==o?(u={parent:o,cache:o},n.memoizedState=u,n.lanes===0&&(n.memoizedState=n.updateQueue.baseState=u),Ia(n,_n,o)):(o=h.cache,Ia(n,_n,o),o!==u.cache&&lf(n,[_n],a,!0))),On(e,n,n.pendingProps.children,a),n.child;case 29:throw n.pendingProps}throw Error(s(156,n.tag))}function fa(e){e.flags|=4}function jf(e,n,a,o,u){if((n=(e.mode&32)!==0)&&(n=!1),n){if(e.flags|=16777216,(u&335544128)===u)if(e.stateNode.complete)e.flags|=8192;else if(Cg())e.flags|=8192;else throw Ds=Gl,hf}else e.flags&=-16777217}function ng(e,n){if(n.type!=="stylesheet"||(n.state.loading&4)!==0)e.flags&=-16777217;else if(e.flags|=16777216,!__(n))if(Cg())e.flags|=8192;else throw Ds=Gl,hf}function ac(e,n){n!==null&&(e.flags|=4),e.flags&16384&&(n=e.tag!==22?bt():536870912,e.lanes|=n,Mr|=n)}function Po(e,n){if(!De)switch(e.tailMode){case"hidden":n=e.tail;for(var a=null;n!==null;)n.alternate!==null&&(a=n),n=n.sibling;a===null?e.tail=null:a.sibling=null;break;case"collapsed":a=e.tail;for(var o=null;a!==null;)a.alternate!==null&&(o=a),a=a.sibling;o===null?n||e.tail===null?e.tail=null:e.tail.sibling=null:o.sibling=null}}function nn(e){var n=e.alternate!==null&&e.alternate.child===e.child,a=0,o=0;if(n)for(var u=e.child;u!==null;)a|=u.lanes|u.childLanes,o|=u.subtreeFlags&65011712,o|=u.flags&65011712,u.return=e,u=u.sibling;else for(u=e.child;u!==null;)a|=u.lanes|u.childLanes,o|=u.subtreeFlags,o|=u.flags,u.return=e,u=u.sibling;return e.subtreeFlags|=o,e.childLanes=a,n}function Zy(e,n,a){var o=n.pendingProps;switch(nf(n),n.tag){case 16:case 15:case 0:case 11:case 7:case 8:case 12:case 9:case 14:return nn(n),null;case 1:return nn(n),null;case 3:return a=n.stateNode,o=null,e!==null&&(o=e.memoizedState.cache),n.memoizedState.cache!==o&&(n.flags|=2048),oa(_n),Nt(),a.pendingContext&&(a.context=a.pendingContext,a.pendingContext=null),(e===null||e.child===null)&&(cr(n)?fa(n):e===null||e.memoizedState.isDehydrated&&(n.flags&256)===0||(n.flags|=1024,sf())),nn(n),null;case 26:var u=n.type,h=n.memoizedState;return e===null?(fa(n),h!==null?(nn(n),ng(n,h)):(nn(n),jf(n,u,null,o,a))):h?h!==e.memoizedState?(fa(n),nn(n),ng(n,h)):(nn(n),n.flags&=-16777217):(e=e.memoizedProps,e!==o&&fa(n),nn(n),jf(n,u,e,o,a)),null;case 27:if(Qt(n),a=tt.current,u=n.type,e!==null&&n.stateNode!=null)e.memoizedProps!==o&&fa(n);else{if(!o){if(n.stateNode===null)throw Error(s(166));return nn(n),null}e=Tt.current,cr(n)?Pm(n):(e=c_(u,o,a),n.stateNode=e,fa(n))}return nn(n),null;case 5:if(Qt(n),u=n.type,e!==null&&n.stateNode!=null)e.memoizedProps!==o&&fa(n);else{if(!o){if(n.stateNode===null)throw Error(s(166));return nn(n),null}if(h=Tt.current,cr(n))Pm(n);else{var x=xc(tt.current);switch(h){case 1:h=x.createElementNS("http://www.w3.org/2000/svg",u);break;case 2:h=x.createElementNS("http://www.w3.org/1998/Math/MathML",u);break;default:switch(u){case"svg":h=x.createElementNS("http://www.w3.org/2000/svg",u);break;case"math":h=x.createElementNS("http://www.w3.org/1998/Math/MathML",u);break;case"script":h=x.createElement("div"),h.innerHTML="<script><\/script>",h=h.removeChild(h.firstChild);break;case"select":h=typeof o.is=="string"?x.createElement("select",{is:o.is}):x.createElement("select"),o.multiple?h.multiple=!0:o.size&&(h.size=o.size);break;default:h=typeof o.is=="string"?x.createElement(u,{is:o.is}):x.createElement(u)}}h[mn]=n,h[Un]=o;t:for(x=n.child;x!==null;){if(x.tag===5||x.tag===6)h.appendChild(x.stateNode);else if(x.tag!==4&&x.tag!==27&&x.child!==null){x.child.return=x,x=x.child;continue}if(x===n)break t;for(;x.sibling===null;){if(x.return===null||x.return===n)break t;x=x.return}x.sibling.return=x.return,x=x.sibling}n.stateNode=h;t:switch(Pn(h,u,o),u){case"button":case"input":case"select":case"textarea":o=!!o.autoFocus;break t;case"img":o=!0;break t;default:o=!1}o&&fa(n)}}return nn(n),jf(n,n.type,e===null?null:e.memoizedProps,n.pendingProps,a),null;case 6:if(e&&n.stateNode!=null)e.memoizedProps!==o&&fa(n);else{if(typeof o!="string"&&n.stateNode===null)throw Error(s(166));if(e=tt.current,cr(n)){if(e=n.stateNode,a=n.memoizedProps,o=null,u=Nn,u!==null)switch(u.tag){case 27:case 5:o=u.memoizedProps}e[mn]=n,e=!!(e.nodeValue===a||o!==null&&o.suppressHydrationWarning===!0||Jg(e.nodeValue,a)),e||Ba(n,!0)}else e=xc(e).createTextNode(o),e[mn]=n,n.stateNode=e}return nn(n),null;case 31:if(a=n.memoizedState,e===null||e.memoizedState!==null){if(o=cr(n),a!==null){if(e===null){if(!o)throw Error(s(318));if(e=n.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(s(557));e[mn]=n}else Ts(),(n.flags&128)===0&&(n.memoizedState=null),n.flags|=4;nn(n),e=!1}else a=sf(),e!==null&&e.memoizedState!==null&&(e.memoizedState.hydrationErrors=a),e=!0;if(!e)return n.flags&256?(li(n),n):(li(n),null);if((n.flags&128)!==0)throw Error(s(558))}return nn(n),null;case 13:if(o=n.memoizedState,e===null||e.memoizedState!==null&&e.memoizedState.dehydrated!==null){if(u=cr(n),o!==null&&o.dehydrated!==null){if(e===null){if(!u)throw Error(s(318));if(u=n.memoizedState,u=u!==null?u.dehydrated:null,!u)throw Error(s(317));u[mn]=n}else Ts(),(n.flags&128)===0&&(n.memoizedState=null),n.flags|=4;nn(n),u=!1}else u=sf(),e!==null&&e.memoizedState!==null&&(e.memoizedState.hydrationErrors=u),u=!0;if(!u)return n.flags&256?(li(n),n):(li(n),null)}return li(n),(n.flags&128)!==0?(n.lanes=a,n):(a=o!==null,e=e!==null&&e.memoizedState!==null,a&&(o=n.child,u=null,o.alternate!==null&&o.alternate.memoizedState!==null&&o.alternate.memoizedState.cachePool!==null&&(u=o.alternate.memoizedState.cachePool.pool),h=null,o.memoizedState!==null&&o.memoizedState.cachePool!==null&&(h=o.memoizedState.cachePool.pool),h!==u&&(o.flags|=2048)),a!==e&&a&&(n.child.flags|=8192),ac(n,n.updateQueue),nn(n),null);case 4:return Nt(),e===null&&_h(n.stateNode.containerInfo),nn(n),null;case 10:return oa(n.type),nn(n),null;case 19:if(W(dn),o=n.memoizedState,o===null)return nn(n),null;if(u=(n.flags&128)!==0,h=o.rendering,h===null)if(u)Po(o,!1);else{if(fn!==0||e!==null&&(e.flags&128)!==0)for(e=n.child;e!==null;){if(h=Wl(e),h!==null){for(n.flags|=128,Po(o,!1),e=h.updateQueue,n.updateQueue=e,ac(n,e),n.subtreeFlags=0,e=a,a=n.child;a!==null;)Dm(a,e),a=a.sibling;return vt(dn,dn.current&1|2),De&&sa(n,o.treeForkCount),n.child}e=e.sibling}o.tail!==null&&we()>cc&&(n.flags|=128,u=!0,Po(o,!1),n.lanes=4194304)}else{if(!u)if(e=Wl(h),e!==null){if(n.flags|=128,u=!0,e=e.updateQueue,n.updateQueue=e,ac(n,e),Po(o,!0),o.tail===null&&o.tailMode==="hidden"&&!h.alternate&&!De)return nn(n),null}else 2*we()-o.renderingStartTime>cc&&a!==536870912&&(n.flags|=128,u=!0,Po(o,!1),n.lanes=4194304);o.isBackwards?(h.sibling=n.child,n.child=h):(e=o.last,e!==null?e.sibling=h:n.child=h,o.last=h)}return o.tail!==null?(e=o.tail,o.rendering=e,o.tail=e.sibling,o.renderingStartTime=we(),e.sibling=null,a=dn.current,vt(dn,u?a&1|2:a&1),De&&sa(n,o.treeForkCount),e):(nn(n),null);case 22:case 23:return li(n),vf(),o=n.memoizedState!==null,e!==null?e.memoizedState!==null!==o&&(n.flags|=8192):o&&(n.flags|=8192),o?(a&536870912)!==0&&(n.flags&128)===0&&(nn(n),n.subtreeFlags&6&&(n.flags|=8192)):nn(n),a=n.updateQueue,a!==null&&ac(n,a.retryQueue),a=null,e!==null&&e.memoizedState!==null&&e.memoizedState.cachePool!==null&&(a=e.memoizedState.cachePool.pool),o=null,n.memoizedState!==null&&n.memoizedState.cachePool!==null&&(o=n.memoizedState.cachePool.pool),o!==a&&(n.flags|=2048),e!==null&&W(Cs),null;case 24:return a=null,e!==null&&(a=e.memoizedState.cache),n.memoizedState.cache!==a&&(n.flags|=2048),oa(_n),nn(n),null;case 25:return null;case 30:return null}throw Error(s(156,n.tag))}function jy(e,n){switch(nf(n),n.tag){case 1:return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 3:return oa(_n),Nt(),e=n.flags,(e&65536)!==0&&(e&128)===0?(n.flags=e&-65537|128,n):null;case 26:case 27:case 5:return Qt(n),null;case 31:if(n.memoizedState!==null){if(li(n),n.alternate===null)throw Error(s(340));Ts()}return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 13:if(li(n),e=n.memoizedState,e!==null&&e.dehydrated!==null){if(n.alternate===null)throw Error(s(340));Ts()}return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 19:return W(dn),null;case 4:return Nt(),null;case 10:return oa(n.type),null;case 22:case 23:return li(n),vf(),e!==null&&W(Cs),e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 24:return oa(_n),null;case 25:return null;default:return null}}function ig(e,n){switch(nf(n),n.tag){case 3:oa(_n),Nt();break;case 26:case 27:case 5:Qt(n);break;case 4:Nt();break;case 31:n.memoizedState!==null&&li(n);break;case 13:li(n);break;case 19:W(dn);break;case 10:oa(n.type);break;case 22:case 23:li(n),vf(),e!==null&&W(Cs);break;case 24:oa(_n)}}function zo(e,n){try{var a=n.updateQueue,o=a!==null?a.lastEffect:null;if(o!==null){var u=o.next;a=u;do{if((a.tag&e)===e){o=void 0;var h=a.create,x=a.inst;o=h(),x.destroy=o}a=a.next}while(a!==u)}}catch(C){Ye(n,n.return,C)}}function Xa(e,n,a){try{var o=n.updateQueue,u=o!==null?o.lastEffect:null;if(u!==null){var h=u.next;o=h;do{if((o.tag&e)===e){var x=o.inst,C=x.destroy;if(C!==void 0){x.destroy=void 0,u=n;var G=a,it=C;try{it()}catch(mt){Ye(u,G,mt)}}}o=o.next}while(o!==h)}}catch(mt){Ye(n,n.return,mt)}}function ag(e){var n=e.updateQueue;if(n!==null){var a=e.stateNode;try{Zm(n,a)}catch(o){Ye(e,e.return,o)}}}function sg(e,n,a){a.props=Ls(e.type,e.memoizedProps),a.state=e.memoizedState;try{a.componentWillUnmount()}catch(o){Ye(e,n,o)}}function Bo(e,n){try{var a=e.ref;if(a!==null){switch(e.tag){case 26:case 27:case 5:var o=e.stateNode;break;case 30:o=e.stateNode;break;default:o=e.stateNode}typeof a=="function"?e.refCleanup=a(o):a.current=o}}catch(u){Ye(e,n,u)}}function Xi(e,n){var a=e.ref,o=e.refCleanup;if(a!==null)if(typeof o=="function")try{o()}catch(u){Ye(e,n,u)}finally{e.refCleanup=null,e=e.alternate,e!=null&&(e.refCleanup=null)}else if(typeof a=="function")try{a(null)}catch(u){Ye(e,n,u)}else a.current=null}function rg(e){var n=e.type,a=e.memoizedProps,o=e.stateNode;try{t:switch(n){case"button":case"input":case"select":case"textarea":a.autoFocus&&o.focus();break t;case"img":a.src?o.src=a.src:a.srcSet&&(o.srcset=a.srcSet)}}catch(u){Ye(e,e.return,u)}}function Kf(e,n,a){try{var o=e.stateNode;_S(o,e.type,a,n),o[Un]=n}catch(u){Ye(e,e.return,u)}}function og(e){return e.tag===5||e.tag===3||e.tag===26||e.tag===27&&Qa(e.type)||e.tag===4}function Qf(e){t:for(;;){for(;e.sibling===null;){if(e.return===null||og(e.return))return null;e=e.return}for(e.sibling.return=e.return,e=e.sibling;e.tag!==5&&e.tag!==6&&e.tag!==18;){if(e.tag===27&&Qa(e.type)||e.flags&2||e.child===null||e.tag===4)continue t;e.child.return=e,e=e.child}if(!(e.flags&2))return e.stateNode}}function Jf(e,n,a){var o=e.tag;if(o===5||o===6)e=e.stateNode,n?(a.nodeType===9?a.body:a.nodeName==="HTML"?a.ownerDocument.body:a).insertBefore(e,n):(n=a.nodeType===9?a.body:a.nodeName==="HTML"?a.ownerDocument.body:a,n.appendChild(e),a=a._reactRootContainer,a!=null||n.onclick!==null||(n.onclick=na));else if(o!==4&&(o===27&&Qa(e.type)&&(a=e.stateNode,n=null),e=e.child,e!==null))for(Jf(e,n,a),e=e.sibling;e!==null;)Jf(e,n,a),e=e.sibling}function sc(e,n,a){var o=e.tag;if(o===5||o===6)e=e.stateNode,n?a.insertBefore(e,n):a.appendChild(e);else if(o!==4&&(o===27&&Qa(e.type)&&(a=e.stateNode),e=e.child,e!==null))for(sc(e,n,a),e=e.sibling;e!==null;)sc(e,n,a),e=e.sibling}function lg(e){var n=e.stateNode,a=e.memoizedProps;try{for(var o=e.type,u=n.attributes;u.length;)n.removeAttributeNode(u[0]);Pn(n,o,a),n[mn]=e,n[Un]=a}catch(h){Ye(e,e.return,h)}}var ha=!1,yn=!1,$f=!1,cg=typeof WeakSet=="function"?WeakSet:Set,Cn=null;function Ky(e,n){if(e=e.containerInfo,yh=Ac,e=Sm(e),Wu(e)){if("selectionStart"in e)var a={start:e.selectionStart,end:e.selectionEnd};else t:{a=(a=e.ownerDocument)&&a.defaultView||window;var o=a.getSelection&&a.getSelection();if(o&&o.rangeCount!==0){a=o.anchorNode;var u=o.anchorOffset,h=o.focusNode;o=o.focusOffset;try{a.nodeType,h.nodeType}catch{a=null;break t}var x=0,C=-1,G=-1,it=0,mt=0,St=e,lt=null;e:for(;;){for(var ct;St!==a||u!==0&&St.nodeType!==3||(C=x+u),St!==h||o!==0&&St.nodeType!==3||(G=x+o),St.nodeType===3&&(x+=St.nodeValue.length),(ct=St.firstChild)!==null;)lt=St,St=ct;for(;;){if(St===e)break e;if(lt===a&&++it===u&&(C=x),lt===h&&++mt===o&&(G=x),(ct=St.nextSibling)!==null)break;St=lt,lt=St.parentNode}St=ct}a=C===-1||G===-1?null:{start:C,end:G}}else a=null}a=a||{start:0,end:0}}else a=null;for(Sh={focusedElem:e,selectionRange:a},Ac=!1,Cn=n;Cn!==null;)if(n=Cn,e=n.child,(n.subtreeFlags&1028)!==0&&e!==null)e.return=n,Cn=e;else for(;Cn!==null;){switch(n=Cn,h=n.alternate,e=n.flags,n.tag){case 0:if((e&4)!==0&&(e=n.updateQueue,e=e!==null?e.events:null,e!==null))for(a=0;a<e.length;a++)u=e[a],u.ref.impl=u.nextImpl;break;case 11:case 15:break;case 1:if((e&1024)!==0&&h!==null){e=void 0,a=n,u=h.memoizedProps,h=h.memoizedState,o=a.stateNode;try{var Yt=Ls(a.type,u);e=o.getSnapshotBeforeUpdate(Yt,h),o.__reactInternalSnapshotBeforeUpdate=e}catch(se){Ye(a,a.return,se)}}break;case 3:if((e&1024)!==0){if(e=n.stateNode.containerInfo,a=e.nodeType,a===9)bh(e);else if(a===1)switch(e.nodeName){case"HEAD":case"HTML":case"BODY":bh(e);break;default:e.textContent=""}}break;case 5:case 26:case 27:case 6:case 4:case 17:break;default:if((e&1024)!==0)throw Error(s(163))}if(e=n.sibling,e!==null){e.return=n.return,Cn=e;break}Cn=n.return}}function ug(e,n,a){var o=a.flags;switch(a.tag){case 0:case 11:case 15:pa(e,a),o&4&&zo(5,a);break;case 1:if(pa(e,a),o&4)if(e=a.stateNode,n===null)try{e.componentDidMount()}catch(x){Ye(a,a.return,x)}else{var u=Ls(a.type,n.memoizedProps);n=n.memoizedState;try{e.componentDidUpdate(u,n,e.__reactInternalSnapshotBeforeUpdate)}catch(x){Ye(a,a.return,x)}}o&64&&ag(a),o&512&&Bo(a,a.return);break;case 3:if(pa(e,a),o&64&&(e=a.updateQueue,e!==null)){if(n=null,a.child!==null)switch(a.child.tag){case 27:case 5:n=a.child.stateNode;break;case 1:n=a.child.stateNode}try{Zm(e,n)}catch(x){Ye(a,a.return,x)}}break;case 27:n===null&&o&4&&lg(a);case 26:case 5:pa(e,a),n===null&&o&4&&rg(a),o&512&&Bo(a,a.return);break;case 12:pa(e,a);break;case 31:pa(e,a),o&4&&dg(e,a);break;case 13:pa(e,a),o&4&&pg(e,a),o&64&&(e=a.memoizedState,e!==null&&(e=e.dehydrated,e!==null&&(a=sS.bind(null,a),TS(e,a))));break;case 22:if(o=a.memoizedState!==null||ha,!o){n=n!==null&&n.memoizedState!==null||yn,u=ha;var h=yn;ha=o,(yn=n)&&!h?ma(e,a,(a.subtreeFlags&8772)!==0):pa(e,a),ha=u,yn=h}break;case 30:break;default:pa(e,a)}}function fg(e){var n=e.alternate;n!==null&&(e.alternate=null,fg(n)),e.child=null,e.deletions=null,e.sibling=null,e.tag===5&&(n=e.stateNode,n!==null&&Ua(n)),e.stateNode=null,e.return=null,e.dependencies=null,e.memoizedProps=null,e.memoizedState=null,e.pendingProps=null,e.stateNode=null,e.updateQueue=null}var on=null,jn=!1;function da(e,n,a){for(a=a.child;a!==null;)hg(e,n,a),a=a.sibling}function hg(e,n,a){if(dt&&typeof dt.onCommitFiberUnmount=="function")try{dt.onCommitFiberUnmount(ft,a)}catch{}switch(a.tag){case 26:yn||Xi(a,n),da(e,n,a),a.memoizedState?a.memoizedState.count--:a.stateNode&&(a=a.stateNode,a.parentNode.removeChild(a));break;case 27:yn||Xi(a,n);var o=on,u=jn;Qa(a.type)&&(on=a.stateNode,jn=!1),da(e,n,a),qo(a.stateNode),on=o,jn=u;break;case 5:yn||Xi(a,n);case 6:if(o=on,u=jn,on=null,da(e,n,a),on=o,jn=u,on!==null)if(jn)try{(on.nodeType===9?on.body:on.nodeName==="HTML"?on.ownerDocument.body:on).removeChild(a.stateNode)}catch(h){Ye(a,n,h)}else try{on.removeChild(a.stateNode)}catch(h){Ye(a,n,h)}break;case 18:on!==null&&(jn?(e=on,a_(e.nodeType===9?e.body:e.nodeName==="HTML"?e.ownerDocument.body:e,a.stateNode),Dr(e)):a_(on,a.stateNode));break;case 4:o=on,u=jn,on=a.stateNode.containerInfo,jn=!0,da(e,n,a),on=o,jn=u;break;case 0:case 11:case 14:case 15:Xa(2,a,n),yn||Xa(4,a,n),da(e,n,a);break;case 1:yn||(Xi(a,n),o=a.stateNode,typeof o.componentWillUnmount=="function"&&sg(a,n,o)),da(e,n,a);break;case 21:da(e,n,a);break;case 22:yn=(o=yn)||a.memoizedState!==null,da(e,n,a),yn=o;break;default:da(e,n,a)}}function dg(e,n){if(n.memoizedState===null&&(e=n.alternate,e!==null&&(e=e.memoizedState,e!==null))){e=e.dehydrated;try{Dr(e)}catch(a){Ye(n,n.return,a)}}}function pg(e,n){if(n.memoizedState===null&&(e=n.alternate,e!==null&&(e=e.memoizedState,e!==null&&(e=e.dehydrated,e!==null))))try{Dr(e)}catch(a){Ye(n,n.return,a)}}function Qy(e){switch(e.tag){case 31:case 13:case 19:var n=e.stateNode;return n===null&&(n=e.stateNode=new cg),n;case 22:return e=e.stateNode,n=e._retryCache,n===null&&(n=e._retryCache=new cg),n;default:throw Error(s(435,e.tag))}}function rc(e,n){var a=Qy(e);n.forEach(function(o){if(!a.has(o)){a.add(o);var u=rS.bind(null,e,o);o.then(u,u)}})}function Kn(e,n){var a=n.deletions;if(a!==null)for(var o=0;o<a.length;o++){var u=a[o],h=e,x=n,C=x;t:for(;C!==null;){switch(C.tag){case 27:if(Qa(C.type)){on=C.stateNode,jn=!1;break t}break;case 5:on=C.stateNode,jn=!1;break t;case 3:case 4:on=C.stateNode.containerInfo,jn=!0;break t}C=C.return}if(on===null)throw Error(s(160));hg(h,x,u),on=null,jn=!1,h=u.alternate,h!==null&&(h.return=null),u.return=null}if(n.subtreeFlags&13886)for(n=n.child;n!==null;)mg(n,e),n=n.sibling}var Ni=null;function mg(e,n){var a=e.alternate,o=e.flags;switch(e.tag){case 0:case 11:case 14:case 15:Kn(n,e),Qn(e),o&4&&(Xa(3,e,e.return),zo(3,e),Xa(5,e,e.return));break;case 1:Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),o&64&&ha&&(e=e.updateQueue,e!==null&&(o=e.callbacks,o!==null&&(a=e.shared.hiddenCallbacks,e.shared.hiddenCallbacks=a===null?o:a.concat(o))));break;case 26:var u=Ni;if(Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),o&4){var h=a!==null?a.memoizedState:null;if(o=e.memoizedState,a===null)if(o===null)if(e.stateNode===null){t:{o=e.type,a=e.memoizedProps,u=u.ownerDocument||u;e:switch(o){case"title":h=u.getElementsByTagName("title")[0],(!h||h[Da]||h[mn]||h.namespaceURI==="http://www.w3.org/2000/svg"||h.hasAttribute("itemprop"))&&(h=u.createElement(o),u.head.insertBefore(h,u.querySelector("head > title"))),Pn(h,o,a),h[mn]=e,gn(h),o=h;break t;case"link":var x=m_("link","href",u).get(o+(a.href||""));if(x){for(var C=0;C<x.length;C++)if(h=x[C],h.getAttribute("href")===(a.href==null||a.href===""?null:a.href)&&h.getAttribute("rel")===(a.rel==null?null:a.rel)&&h.getAttribute("title")===(a.title==null?null:a.title)&&h.getAttribute("crossorigin")===(a.crossOrigin==null?null:a.crossOrigin)){x.splice(C,1);break e}}h=u.createElement(o),Pn(h,o,a),u.head.appendChild(h);break;case"meta":if(x=m_("meta","content",u).get(o+(a.content||""))){for(C=0;C<x.length;C++)if(h=x[C],h.getAttribute("content")===(a.content==null?null:""+a.content)&&h.getAttribute("name")===(a.name==null?null:a.name)&&h.getAttribute("property")===(a.property==null?null:a.property)&&h.getAttribute("http-equiv")===(a.httpEquiv==null?null:a.httpEquiv)&&h.getAttribute("charset")===(a.charSet==null?null:a.charSet)){x.splice(C,1);break e}}h=u.createElement(o),Pn(h,o,a),u.head.appendChild(h);break;default:throw Error(s(468,o))}h[mn]=e,gn(h),o=h}e.stateNode=o}else g_(u,e.type,e.stateNode);else e.stateNode=p_(u,o,e.memoizedProps);else h!==o?(h===null?a.stateNode!==null&&(a=a.stateNode,a.parentNode.removeChild(a)):h.count--,o===null?g_(u,e.type,e.stateNode):p_(u,o,e.memoizedProps)):o===null&&e.stateNode!==null&&Kf(e,e.memoizedProps,a.memoizedProps)}break;case 27:Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),a!==null&&o&4&&Kf(e,e.memoizedProps,a.memoizedProps);break;case 5:if(Kn(n,e),Qn(e),o&512&&(yn||a===null||Xi(a,a.return)),e.flags&32){u=e.stateNode;try{ai(u,"")}catch(Yt){Ye(e,e.return,Yt)}}o&4&&e.stateNode!=null&&(u=e.memoizedProps,Kf(e,u,a!==null?a.memoizedProps:u)),o&1024&&($f=!0);break;case 6:if(Kn(n,e),Qn(e),o&4){if(e.stateNode===null)throw Error(s(162));o=e.memoizedProps,a=e.stateNode;try{a.nodeValue=o}catch(Yt){Ye(e,e.return,Yt)}}break;case 3:if(Mc=null,u=Ni,Ni=yc(n.containerInfo),Kn(n,e),Ni=u,Qn(e),o&4&&a!==null&&a.memoizedState.isDehydrated)try{Dr(n.containerInfo)}catch(Yt){Ye(e,e.return,Yt)}$f&&($f=!1,gg(e));break;case 4:o=Ni,Ni=yc(e.stateNode.containerInfo),Kn(n,e),Qn(e),Ni=o;break;case 12:Kn(n,e),Qn(e);break;case 31:Kn(n,e),Qn(e),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,rc(e,o)));break;case 13:Kn(n,e),Qn(e),e.child.flags&8192&&e.memoizedState!==null!=(a!==null&&a.memoizedState!==null)&&(lc=we()),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,rc(e,o)));break;case 22:u=e.memoizedState!==null;var G=a!==null&&a.memoizedState!==null,it=ha,mt=yn;if(ha=it||u,yn=mt||G,Kn(n,e),yn=mt,ha=it,Qn(e),o&8192)t:for(n=e.stateNode,n._visibility=u?n._visibility&-2:n._visibility|1,u&&(a===null||G||ha||yn||Os(e)),a=null,n=e;;){if(n.tag===5||n.tag===26){if(a===null){G=a=n;try{if(h=G.stateNode,u)x=h.style,typeof x.setProperty=="function"?x.setProperty("display","none","important"):x.display="none";else{C=G.stateNode;var St=G.memoizedProps.style,lt=St!=null&&St.hasOwnProperty("display")?St.display:null;C.style.display=lt==null||typeof lt=="boolean"?"":(""+lt).trim()}}catch(Yt){Ye(G,G.return,Yt)}}}else if(n.tag===6){if(a===null){G=n;try{G.stateNode.nodeValue=u?"":G.memoizedProps}catch(Yt){Ye(G,G.return,Yt)}}}else if(n.tag===18){if(a===null){G=n;try{var ct=G.stateNode;u?s_(ct,!0):s_(G.stateNode,!1)}catch(Yt){Ye(G,G.return,Yt)}}}else if((n.tag!==22&&n.tag!==23||n.memoizedState===null||n===e)&&n.child!==null){n.child.return=n,n=n.child;continue}if(n===e)break t;for(;n.sibling===null;){if(n.return===null||n.return===e)break t;a===n&&(a=null),n=n.return}a===n&&(a=null),n.sibling.return=n.return,n=n.sibling}o&4&&(o=e.updateQueue,o!==null&&(a=o.retryQueue,a!==null&&(o.retryQueue=null,rc(e,a))));break;case 19:Kn(n,e),Qn(e),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,rc(e,o)));break;case 30:break;case 21:break;default:Kn(n,e),Qn(e)}}function Qn(e){var n=e.flags;if(n&2){try{for(var a,o=e.return;o!==null;){if(og(o)){a=o;break}o=o.return}if(a==null)throw Error(s(160));switch(a.tag){case 27:var u=a.stateNode,h=Qf(e);sc(e,h,u);break;case 5:var x=a.stateNode;a.flags&32&&(ai(x,""),a.flags&=-33);var C=Qf(e);sc(e,C,x);break;case 3:case 4:var G=a.stateNode.containerInfo,it=Qf(e);Jf(e,it,G);break;default:throw Error(s(161))}}catch(mt){Ye(e,e.return,mt)}e.flags&=-3}n&4096&&(e.flags&=-4097)}function gg(e){if(e.subtreeFlags&1024)for(e=e.child;e!==null;){var n=e;gg(n),n.tag===5&&n.flags&1024&&n.stateNode.reset(),e=e.sibling}}function pa(e,n){if(n.subtreeFlags&8772)for(n=n.child;n!==null;)ug(e,n.alternate,n),n=n.sibling}function Os(e){for(e=e.child;e!==null;){var n=e;switch(n.tag){case 0:case 11:case 14:case 15:Xa(4,n,n.return),Os(n);break;case 1:Xi(n,n.return);var a=n.stateNode;typeof a.componentWillUnmount=="function"&&sg(n,n.return,a),Os(n);break;case 27:qo(n.stateNode);case 26:case 5:Xi(n,n.return),Os(n);break;case 22:n.memoizedState===null&&Os(n);break;case 30:Os(n);break;default:Os(n)}e=e.sibling}}function ma(e,n,a){for(a=a&&(n.subtreeFlags&8772)!==0,n=n.child;n!==null;){var o=n.alternate,u=e,h=n,x=h.flags;switch(h.tag){case 0:case 11:case 15:ma(u,h,a),zo(4,h);break;case 1:if(ma(u,h,a),o=h,u=o.stateNode,typeof u.componentDidMount=="function")try{u.componentDidMount()}catch(it){Ye(o,o.return,it)}if(o=h,u=o.updateQueue,u!==null){var C=o.stateNode;try{var G=u.shared.hiddenCallbacks;if(G!==null)for(u.shared.hiddenCallbacks=null,u=0;u<G.length;u++)Ym(G[u],C)}catch(it){Ye(o,o.return,it)}}a&&x&64&&ag(h),Bo(h,h.return);break;case 27:lg(h);case 26:case 5:ma(u,h,a),a&&o===null&&x&4&&rg(h),Bo(h,h.return);break;case 12:ma(u,h,a);break;case 31:ma(u,h,a),a&&x&4&&dg(u,h);break;case 13:ma(u,h,a),a&&x&4&&pg(u,h);break;case 22:h.memoizedState===null&&ma(u,h,a),Bo(h,h.return);break;case 30:break;default:ma(u,h,a)}n=n.sibling}}function th(e,n){var a=null;e!==null&&e.memoizedState!==null&&e.memoizedState.cachePool!==null&&(a=e.memoizedState.cachePool.pool),e=null,n.memoizedState!==null&&n.memoizedState.cachePool!==null&&(e=n.memoizedState.cachePool.pool),e!==a&&(e!=null&&e.refCount++,a!=null&&Eo(a))}function eh(e,n){e=null,n.alternate!==null&&(e=n.alternate.memoizedState.cache),n=n.memoizedState.cache,n!==e&&(n.refCount++,e!=null&&Eo(e))}function Li(e,n,a,o){if(n.subtreeFlags&10256)for(n=n.child;n!==null;)_g(e,n,a,o),n=n.sibling}function _g(e,n,a,o){var u=n.flags;switch(n.tag){case 0:case 11:case 15:Li(e,n,a,o),u&2048&&zo(9,n);break;case 1:Li(e,n,a,o);break;case 3:Li(e,n,a,o),u&2048&&(e=null,n.alternate!==null&&(e=n.alternate.memoizedState.cache),n=n.memoizedState.cache,n!==e&&(n.refCount++,e!=null&&Eo(e)));break;case 12:if(u&2048){Li(e,n,a,o),e=n.stateNode;try{var h=n.memoizedProps,x=h.id,C=h.onPostCommit;typeof C=="function"&&C(x,n.alternate===null?"mount":"update",e.passiveEffectDuration,-0)}catch(G){Ye(n,n.return,G)}}else Li(e,n,a,o);break;case 31:Li(e,n,a,o);break;case 13:Li(e,n,a,o);break;case 23:break;case 22:h=n.stateNode,x=n.alternate,n.memoizedState!==null?h._visibility&2?Li(e,n,a,o):Io(e,n):h._visibility&2?Li(e,n,a,o):(h._visibility|=2,xr(e,n,a,o,(n.subtreeFlags&10256)!==0||!1)),u&2048&&th(x,n);break;case 24:Li(e,n,a,o),u&2048&&eh(n.alternate,n);break;default:Li(e,n,a,o)}}function xr(e,n,a,o,u){for(u=u&&((n.subtreeFlags&10256)!==0||!1),n=n.child;n!==null;){var h=e,x=n,C=a,G=o,it=x.flags;switch(x.tag){case 0:case 11:case 15:xr(h,x,C,G,u),zo(8,x);break;case 23:break;case 22:var mt=x.stateNode;x.memoizedState!==null?mt._visibility&2?xr(h,x,C,G,u):Io(h,x):(mt._visibility|=2,xr(h,x,C,G,u)),u&&it&2048&&th(x.alternate,x);break;case 24:xr(h,x,C,G,u),u&&it&2048&&eh(x.alternate,x);break;default:xr(h,x,C,G,u)}n=n.sibling}}function Io(e,n){if(n.subtreeFlags&10256)for(n=n.child;n!==null;){var a=e,o=n,u=o.flags;switch(o.tag){case 22:Io(a,o),u&2048&&th(o.alternate,o);break;case 24:Io(a,o),u&2048&&eh(o.alternate,o);break;default:Io(a,o)}n=n.sibling}}var Fo=8192;function yr(e,n,a){if(e.subtreeFlags&Fo)for(e=e.child;e!==null;)vg(e,n,a),e=e.sibling}function vg(e,n,a){switch(e.tag){case 26:yr(e,n,a),e.flags&Fo&&e.memoizedState!==null&&BS(a,Ni,e.memoizedState,e.memoizedProps);break;case 5:yr(e,n,a);break;case 3:case 4:var o=Ni;Ni=yc(e.stateNode.containerInfo),yr(e,n,a),Ni=o;break;case 22:e.memoizedState===null&&(o=e.alternate,o!==null&&o.memoizedState!==null?(o=Fo,Fo=16777216,yr(e,n,a),Fo=o):yr(e,n,a));break;default:yr(e,n,a)}}function xg(e){var n=e.alternate;if(n!==null&&(e=n.child,e!==null)){n.child=null;do n=e.sibling,e.sibling=null,e=n;while(e!==null)}}function Ho(e){var n=e.deletions;if((e.flags&16)!==0){if(n!==null)for(var a=0;a<n.length;a++){var o=n[a];Cn=o,Sg(o,e)}xg(e)}if(e.subtreeFlags&10256)for(e=e.child;e!==null;)yg(e),e=e.sibling}function yg(e){switch(e.tag){case 0:case 11:case 15:Ho(e),e.flags&2048&&Xa(9,e,e.return);break;case 3:Ho(e);break;case 12:Ho(e);break;case 22:var n=e.stateNode;e.memoizedState!==null&&n._visibility&2&&(e.return===null||e.return.tag!==13)?(n._visibility&=-3,oc(e)):Ho(e);break;default:Ho(e)}}function oc(e){var n=e.deletions;if((e.flags&16)!==0){if(n!==null)for(var a=0;a<n.length;a++){var o=n[a];Cn=o,Sg(o,e)}xg(e)}for(e=e.child;e!==null;){switch(n=e,n.tag){case 0:case 11:case 15:Xa(8,n,n.return),oc(n);break;case 22:a=n.stateNode,a._visibility&2&&(a._visibility&=-3,oc(n));break;default:oc(n)}e=e.sibling}}function Sg(e,n){for(;Cn!==null;){var a=Cn;switch(a.tag){case 0:case 11:case 15:Xa(8,a,n);break;case 23:case 22:if(a.memoizedState!==null&&a.memoizedState.cachePool!==null){var o=a.memoizedState.cachePool.pool;o!=null&&o.refCount++}break;case 24:Eo(a.memoizedState.cache)}if(o=a.child,o!==null)o.return=a,Cn=o;else t:for(a=e;Cn!==null;){o=Cn;var u=o.sibling,h=o.return;if(fg(o),o===a){Cn=null;break t}if(u!==null){u.return=h,Cn=u;break t}Cn=h}}}var Jy={getCacheForType:function(e){var n=Ln(_n),a=n.data.get(e);return a===void 0&&(a=e(),n.data.set(e,a)),a},cacheSignal:function(){return Ln(_n).controller.signal}},$y=typeof WeakMap=="function"?WeakMap:Map,Ie=0,tn=null,be=null,Ae=0,qe=0,ci=null,Wa=!1,Sr=!1,nh=!1,ga=0,fn=0,qa=0,Ps=0,ih=0,ui=0,Mr=0,Go=null,Jn=null,ah=!1,lc=0,Mg=0,cc=1/0,uc=null,Ya=null,Tn=0,Za=null,Er=null,_a=0,sh=0,rh=null,Eg=null,Vo=0,oh=null;function fi(){return(Ie&2)!==0&&Ae!==0?Ae&-Ae:P.T!==null?dh():uo()}function bg(){if(ui===0)if((Ae&536870912)===0||De){var e=le;le<<=1,(le&3932160)===0&&(le=262144),ui=e}else ui=536870912;return e=oi.current,e!==null&&(e.flags|=32),ui}function $n(e,n,a){(e===tn&&(qe===2||qe===9)||e.cancelPendingCommit!==null)&&(br(e,0),ja(e,Ae,ui,!1)),Xt(e,a),((Ie&2)===0||e!==tn)&&(e===tn&&((Ie&2)===0&&(Ps|=a),fn===4&&ja(e,Ae,ui,!1)),Wi(e))}function Tg(e,n,a){if((Ie&6)!==0)throw Error(s(327));var o=!a&&(n&127)===0&&(n&e.expiredLanes)===0||Dt(e,n),u=o?nS(e,n):ch(e,n,!0),h=o;do{if(u===0){Sr&&!o&&ja(e,n,0,!1);break}else{if(a=e.current.alternate,h&&!tS(a)){u=ch(e,n,!1),h=!1;continue}if(u===2){if(h=n,e.errorRecoveryDisabledLanes&h)var x=0;else x=e.pendingLanes&-536870913,x=x!==0?x:x&536870912?536870912:0;if(x!==0){n=x;t:{var C=e;u=Go;var G=C.current.memoizedState.isDehydrated;if(G&&(br(C,x).flags|=256),x=ch(C,x,!1),x!==2){if(nh&&!G){C.errorRecoveryDisabledLanes|=h,Ps|=h,u=4;break t}h=Jn,Jn=u,h!==null&&(Jn===null?Jn=h:Jn.push.apply(Jn,h))}u=x}if(h=!1,u!==2)continue}}if(u===1){br(e,0),ja(e,n,0,!0);break}t:{switch(o=e,h=u,h){case 0:case 1:throw Error(s(345));case 4:if((n&4194048)!==n)break;case 6:ja(o,n,ui,!Wa);break t;case 2:Jn=null;break;case 3:case 5:break;default:throw Error(s(329))}if((n&62914560)===n&&(u=lc+300-we(),10<u)){if(ja(o,n,ui,!Wa),_t(o,0,!0)!==0)break t;_a=n,o.timeoutHandle=n_(Ag.bind(null,o,a,Jn,uc,ah,n,ui,Ps,Mr,Wa,h,"Throttled",-0,0),u);break t}Ag(o,a,Jn,uc,ah,n,ui,Ps,Mr,Wa,h,null,-0,0)}}break}while(!0);Wi(e)}function Ag(e,n,a,o,u,h,x,C,G,it,mt,St,lt,ct){if(e.timeoutHandle=-1,St=n.subtreeFlags,St&8192||(St&16785408)===16785408){St={stylesheets:null,count:0,imgCount:0,imgBytes:0,suspenseyImages:[],waitingForImages:!0,waitingForViewTransition:!1,unsuspend:na},vg(n,h,St);var Yt=(h&62914560)===h?lc-we():(h&4194048)===h?Mg-we():0;if(Yt=IS(St,Yt),Yt!==null){_a=h,e.cancelPendingCommit=Yt(Og.bind(null,e,n,h,a,o,u,x,C,G,mt,St,null,lt,ct)),ja(e,h,x,!it);return}}Og(e,n,h,a,o,u,x,C,G)}function tS(e){for(var n=e;;){var a=n.tag;if((a===0||a===11||a===15)&&n.flags&16384&&(a=n.updateQueue,a!==null&&(a=a.stores,a!==null)))for(var o=0;o<a.length;o++){var u=a[o],h=u.getSnapshot;u=u.value;try{if(!si(h(),u))return!1}catch{return!1}}if(a=n.child,n.subtreeFlags&16384&&a!==null)a.return=n,n=a;else{if(n===e)break;for(;n.sibling===null;){if(n.return===null||n.return===e)return!0;n=n.return}n.sibling.return=n.return,n=n.sibling}}return!0}function ja(e,n,a,o){n&=~ih,n&=~Ps,e.suspendedLanes|=n,e.pingedLanes&=~n,o&&(e.warmLanes|=n),o=e.expirationTimes;for(var u=n;0<u;){var h=31-Ht(u),x=1<<h;o[h]=-1,u&=~x}a!==0&&Ve(e,a,n)}function fc(){return(Ie&6)===0?(ko(0),!1):!0}function lh(){if(be!==null){if(qe===0)var e=be.return;else e=be,ra=As=null,bf(e),pr=null,To=0,e=be;for(;e!==null;)ig(e.alternate,e),e=e.return;be=null}}function br(e,n){var a=e.timeoutHandle;a!==-1&&(e.timeoutHandle=-1,yS(a)),a=e.cancelPendingCommit,a!==null&&(e.cancelPendingCommit=null,a()),_a=0,lh(),tn=e,be=a=aa(e.current,null),Ae=n,qe=0,ci=null,Wa=!1,Sr=Dt(e,n),nh=!1,Mr=ui=ih=Ps=qa=fn=0,Jn=Go=null,ah=!1,(n&8)!==0&&(n|=n&32);var o=e.entangledLanes;if(o!==0)for(e=e.entanglements,o&=n;0<o;){var u=31-Ht(o),h=1<<u;n|=e[u],o&=~h}return ga=n,Nl(),a}function Rg(e,n){_e=null,P.H=Lo,n===dr||n===Hl?(n=km(),qe=3):n===hf?(n=km(),qe=4):qe=n===Hf?8:n!==null&&typeof n=="object"&&typeof n.then=="function"?6:1,ci=n,be===null&&(fn=1,tc(e,yi(n,e.current)))}function Cg(){var e=oi.current;return e===null?!0:(Ae&4194048)===Ae?bi===null:(Ae&62914560)===Ae||(Ae&536870912)!==0?e===bi:!1}function wg(){var e=P.H;return P.H=Lo,e===null?Lo:e}function Dg(){var e=P.A;return P.A=Jy,e}function hc(){fn=4,Wa||(Ae&4194048)!==Ae&&oi.current!==null||(Sr=!0),(qa&134217727)===0&&(Ps&134217727)===0||tn===null||ja(tn,Ae,ui,!1)}function ch(e,n,a){var o=Ie;Ie|=2;var u=wg(),h=Dg();(tn!==e||Ae!==n)&&(uc=null,br(e,n)),n=!1;var x=fn;t:do try{if(qe!==0&&be!==null){var C=be,G=ci;switch(qe){case 8:lh(),x=6;break t;case 3:case 2:case 9:case 6:oi.current===null&&(n=!0);var it=qe;if(qe=0,ci=null,Tr(e,C,G,it),a&&Sr){x=0;break t}break;default:it=qe,qe=0,ci=null,Tr(e,C,G,it)}}eS(),x=fn;break}catch(mt){Rg(e,mt)}while(!0);return n&&e.shellSuspendCounter++,ra=As=null,Ie=o,P.H=u,P.A=h,be===null&&(tn=null,Ae=0,Nl()),x}function eS(){for(;be!==null;)Ug(be)}function nS(e,n){var a=Ie;Ie|=2;var o=wg(),u=Dg();tn!==e||Ae!==n?(uc=null,cc=we()+500,br(e,n)):Sr=Dt(e,n);t:do try{if(qe!==0&&be!==null){n=be;var h=ci;e:switch(qe){case 1:qe=0,ci=null,Tr(e,n,h,1);break;case 2:case 9:if(Gm(h)){qe=0,ci=null,Ng(n);break}n=function(){qe!==2&&qe!==9||tn!==e||(qe=7),Wi(e)},h.then(n,n);break t;case 3:qe=7;break t;case 4:qe=5;break t;case 7:Gm(h)?(qe=0,ci=null,Ng(n)):(qe=0,ci=null,Tr(e,n,h,7));break;case 5:var x=null;switch(be.tag){case 26:x=be.memoizedState;case 5:case 27:var C=be;if(x?__(x):C.stateNode.complete){qe=0,ci=null;var G=C.sibling;if(G!==null)be=G;else{var it=C.return;it!==null?(be=it,dc(it)):be=null}break e}}qe=0,ci=null,Tr(e,n,h,5);break;case 6:qe=0,ci=null,Tr(e,n,h,6);break;case 8:lh(),fn=6;break t;default:throw Error(s(462))}}iS();break}catch(mt){Rg(e,mt)}while(!0);return ra=As=null,P.H=o,P.A=u,Ie=a,be!==null?0:(tn=null,Ae=0,Nl(),fn)}function iS(){for(;be!==null&&!me();)Ug(be)}function Ug(e){var n=eg(e.alternate,e,ga);e.memoizedProps=e.pendingProps,n===null?dc(e):be=n}function Ng(e){var n=e,a=n.alternate;switch(n.tag){case 15:case 0:n=j0(a,n,n.pendingProps,n.type,void 0,Ae);break;case 11:n=j0(a,n,n.pendingProps,n.type.render,n.ref,Ae);break;case 5:bf(n);default:ig(a,n),n=be=Dm(n,ga),n=eg(a,n,ga)}e.memoizedProps=e.pendingProps,n===null?dc(e):be=n}function Tr(e,n,a,o){ra=As=null,bf(n),pr=null,To=0;var u=n.return;try{if(Wy(e,u,n,a,Ae)){fn=1,tc(e,yi(a,e.current)),be=null;return}}catch(h){if(u!==null)throw be=u,h;fn=1,tc(e,yi(a,e.current)),be=null;return}n.flags&32768?(De||o===1?e=!0:Sr||(Ae&536870912)!==0?e=!1:(Wa=e=!0,(o===2||o===9||o===3||o===6)&&(o=oi.current,o!==null&&o.tag===13&&(o.flags|=16384))),Lg(n,e)):dc(n)}function dc(e){var n=e;do{if((n.flags&32768)!==0){Lg(n,Wa);return}e=n.return;var a=Zy(n.alternate,n,ga);if(a!==null){be=a;return}if(n=n.sibling,n!==null){be=n;return}be=n=e}while(n!==null);fn===0&&(fn=5)}function Lg(e,n){do{var a=jy(e.alternate,e);if(a!==null){a.flags&=32767,be=a;return}if(a=e.return,a!==null&&(a.flags|=32768,a.subtreeFlags=0,a.deletions=null),!n&&(e=e.sibling,e!==null)){be=e;return}be=e=a}while(e!==null);fn=6,be=null}function Og(e,n,a,o,u,h,x,C,G){e.cancelPendingCommit=null;do pc();while(Tn!==0);if((Ie&6)!==0)throw Error(s(327));if(n!==null){if(n===e.current)throw Error(s(177));if(h=n.lanes|n.childLanes,h|=Ku,an(e,a,h,x,C,G),e===tn&&(be=tn=null,Ae=0),Er=n,Za=e,_a=a,sh=h,rh=u,Eg=o,(n.subtreeFlags&10256)!==0||(n.flags&10256)!==0?(e.callbackNode=null,e.callbackPriority=0,oS(Q,function(){return Fg(),null})):(e.callbackNode=null,e.callbackPriority=0),o=(n.flags&13878)!==0,(n.subtreeFlags&13878)!==0||o){o=P.T,P.T=null,u=H.p,H.p=2,x=Ie,Ie|=4;try{Ky(e,n,a)}finally{Ie=x,H.p=u,P.T=o}}Tn=1,Pg(),zg(),Bg()}}function Pg(){if(Tn===1){Tn=0;var e=Za,n=Er,a=(n.flags&13878)!==0;if((n.subtreeFlags&13878)!==0||a){a=P.T,P.T=null;var o=H.p;H.p=2;var u=Ie;Ie|=4;try{mg(n,e);var h=Sh,x=Sm(e.containerInfo),C=h.focusedElem,G=h.selectionRange;if(x!==C&&C&&C.ownerDocument&&ym(C.ownerDocument.documentElement,C)){if(G!==null&&Wu(C)){var it=G.start,mt=G.end;if(mt===void 0&&(mt=it),"selectionStart"in C)C.selectionStart=it,C.selectionEnd=Math.min(mt,C.value.length);else{var St=C.ownerDocument||document,lt=St&&St.defaultView||window;if(lt.getSelection){var ct=lt.getSelection(),Yt=C.textContent.length,se=Math.min(G.start,Yt),Je=G.end===void 0?se:Math.min(G.end,Yt);!ct.extend&&se>Je&&(x=Je,Je=se,se=x);var K=xm(C,se),X=xm(C,Je);if(K&&X&&(ct.rangeCount!==1||ct.anchorNode!==K.node||ct.anchorOffset!==K.offset||ct.focusNode!==X.node||ct.focusOffset!==X.offset)){var nt=St.createRange();nt.setStart(K.node,K.offset),ct.removeAllRanges(),se>Je?(ct.addRange(nt),ct.extend(X.node,X.offset)):(nt.setEnd(X.node,X.offset),ct.addRange(nt))}}}}for(St=[],ct=C;ct=ct.parentNode;)ct.nodeType===1&&St.push({element:ct,left:ct.scrollLeft,top:ct.scrollTop});for(typeof C.focus=="function"&&C.focus(),C=0;C<St.length;C++){var xt=St[C];xt.element.scrollLeft=xt.left,xt.element.scrollTop=xt.top}}Ac=!!yh,Sh=yh=null}finally{Ie=u,H.p=o,P.T=a}}e.current=n,Tn=2}}function zg(){if(Tn===2){Tn=0;var e=Za,n=Er,a=(n.flags&8772)!==0;if((n.subtreeFlags&8772)!==0||a){a=P.T,P.T=null;var o=H.p;H.p=2;var u=Ie;Ie|=4;try{ug(e,n.alternate,n)}finally{Ie=u,H.p=o,P.T=a}}Tn=3}}function Bg(){if(Tn===4||Tn===3){Tn=0,Y();var e=Za,n=Er,a=_a,o=Eg;(n.subtreeFlags&10256)!==0||(n.flags&10256)!==0?Tn=5:(Tn=0,Er=Za=null,Ig(e,e.pendingLanes));var u=e.pendingLanes;if(u===0&&(Ya=null),co(a),n=n.stateNode,dt&&typeof dt.onCommitFiberRoot=="function")try{dt.onCommitFiberRoot(ft,n,void 0,(n.current.flags&128)===128)}catch{}if(o!==null){n=P.T,u=H.p,H.p=2,P.T=null;try{for(var h=e.onRecoverableError,x=0;x<o.length;x++){var C=o[x];h(C.value,{componentStack:C.stack})}}finally{P.T=n,H.p=u}}(_a&3)!==0&&pc(),Wi(e),u=e.pendingLanes,(a&261930)!==0&&(u&42)!==0?e===oh?Vo++:(Vo=0,oh=e):Vo=0,ko(0)}}function Ig(e,n){(e.pooledCacheLanes&=n)===0&&(n=e.pooledCache,n!=null&&(e.pooledCache=null,Eo(n)))}function pc(){return Pg(),zg(),Bg(),Fg()}function Fg(){if(Tn!==5)return!1;var e=Za,n=sh;sh=0;var a=co(_a),o=P.T,u=H.p;try{H.p=32>a?32:a,P.T=null,a=rh,rh=null;var h=Za,x=_a;if(Tn=0,Er=Za=null,_a=0,(Ie&6)!==0)throw Error(s(331));var C=Ie;if(Ie|=4,yg(h.current),_g(h,h.current,x,a),Ie=C,ko(0,!1),dt&&typeof dt.onPostCommitFiberRoot=="function")try{dt.onPostCommitFiberRoot(ft,h)}catch{}return!0}finally{H.p=u,P.T=o,Ig(e,n)}}function Hg(e,n,a){n=yi(a,n),n=Ff(e.stateNode,n,2),e=Ga(e,n,2),e!==null&&(Xt(e,2),Wi(e))}function Ye(e,n,a){if(e.tag===3)Hg(e,e,a);else for(;n!==null;){if(n.tag===3){Hg(n,e,a);break}else if(n.tag===1){var o=n.stateNode;if(typeof n.type.getDerivedStateFromError=="function"||typeof o.componentDidCatch=="function"&&(Ya===null||!Ya.has(o))){e=yi(a,e),a=G0(2),o=Ga(n,a,2),o!==null&&(V0(a,o,n,e),Xt(o,2),Wi(o));break}}n=n.return}}function uh(e,n,a){var o=e.pingCache;if(o===null){o=e.pingCache=new $y;var u=new Set;o.set(n,u)}else u=o.get(n),u===void 0&&(u=new Set,o.set(n,u));u.has(a)||(nh=!0,u.add(a),e=aS.bind(null,e,n,a),n.then(e,e))}function aS(e,n,a){var o=e.pingCache;o!==null&&o.delete(n),e.pingedLanes|=e.suspendedLanes&a,e.warmLanes&=~a,tn===e&&(Ae&a)===a&&(fn===4||fn===3&&(Ae&62914560)===Ae&&300>we()-lc?(Ie&2)===0&&br(e,0):ih|=a,Mr===Ae&&(Mr=0)),Wi(e)}function Gg(e,n){n===0&&(n=bt()),e=Es(e,n),e!==null&&(Xt(e,n),Wi(e))}function sS(e){var n=e.memoizedState,a=0;n!==null&&(a=n.retryLane),Gg(e,a)}function rS(e,n){var a=0;switch(e.tag){case 31:case 13:var o=e.stateNode,u=e.memoizedState;u!==null&&(a=u.retryLane);break;case 19:o=e.stateNode;break;case 22:o=e.stateNode._retryCache;break;default:throw Error(s(314))}o!==null&&o.delete(n),Gg(e,a)}function oS(e,n){return Vt(e,n)}var mc=null,Ar=null,fh=!1,gc=!1,hh=!1,Ka=0;function Wi(e){e!==Ar&&e.next===null&&(Ar===null?mc=Ar=e:Ar=Ar.next=e),gc=!0,fh||(fh=!0,cS())}function ko(e,n){if(!hh&&gc){hh=!0;do for(var a=!1,o=mc;o!==null;){if(e!==0){var u=o.pendingLanes;if(u===0)var h=0;else{var x=o.suspendedLanes,C=o.pingedLanes;h=(1<<31-Ht(42|e)+1)-1,h&=u&~(x&~C),h=h&201326741?h&201326741|1:h?h|2:0}h!==0&&(a=!0,Wg(o,h))}else h=Ae,h=_t(o,o===tn?h:0,o.cancelPendingCommit!==null||o.timeoutHandle!==-1),(h&3)===0||Dt(o,h)||(a=!0,Wg(o,h));o=o.next}while(a);hh=!1}}function lS(){Vg()}function Vg(){gc=fh=!1;var e=0;Ka!==0&&xS()&&(e=Ka);for(var n=we(),a=null,o=mc;o!==null;){var u=o.next,h=kg(o,n);h===0?(o.next=null,a===null?mc=u:a.next=u,u===null&&(Ar=a)):(a=o,(e!==0||(h&3)!==0)&&(gc=!0)),o=u}Tn!==0&&Tn!==5||ko(e),Ka!==0&&(Ka=0)}function kg(e,n){for(var a=e.suspendedLanes,o=e.pingedLanes,u=e.expirationTimes,h=e.pendingLanes&-62914561;0<h;){var x=31-Ht(h),C=1<<x,G=u[x];G===-1?((C&a)===0||(C&o)!==0)&&(u[x]=Ft(C,n)):G<=n&&(e.expiredLanes|=C),h&=~C}if(n=tn,a=Ae,a=_t(e,e===n?a:0,e.cancelPendingCommit!==null||e.timeoutHandle!==-1),o=e.callbackNode,a===0||e===n&&(qe===2||qe===9)||e.cancelPendingCommit!==null)return o!==null&&o!==null&&Ce(o),e.callbackNode=null,e.callbackPriority=0;if((a&3)===0||Dt(e,a)){if(n=a&-a,n===e.callbackPriority)return n;switch(o!==null&&Ce(o),co(a)){case 2:case 8:a=b;break;case 32:a=Q;break;case 268435456:a=ht;break;default:a=Q}return o=Xg.bind(null,e),a=Vt(a,o),e.callbackPriority=n,e.callbackNode=a,n}return o!==null&&o!==null&&Ce(o),e.callbackPriority=2,e.callbackNode=null,2}function Xg(e,n){if(Tn!==0&&Tn!==5)return e.callbackNode=null,e.callbackPriority=0,null;var a=e.callbackNode;if(pc()&&e.callbackNode!==a)return null;var o=Ae;return o=_t(e,e===tn?o:0,e.cancelPendingCommit!==null||e.timeoutHandle!==-1),o===0?null:(Tg(e,o,n),kg(e,we()),e.callbackNode!=null&&e.callbackNode===a?Xg.bind(null,e):null)}function Wg(e,n){if(pc())return null;Tg(e,n,!0)}function cS(){SS(function(){(Ie&6)!==0?Vt(U,lS):Vg()})}function dh(){if(Ka===0){var e=fr;e===0&&(e=ee,ee<<=1,(ee&261888)===0&&(ee=256)),Ka=e}return Ka}function qg(e){return e==null||typeof e=="symbol"||typeof e=="boolean"?null:typeof e=="function"?e:xs(""+e)}function Yg(e,n){var a=n.ownerDocument.createElement("input");return a.name=n.name,a.value=n.value,e.id&&a.setAttribute("form",e.id),n.parentNode.insertBefore(a,n),e=new FormData(e),a.parentNode.removeChild(a),e}function uS(e,n,a,o,u){if(n==="submit"&&a&&a.stateNode===u){var h=qg((u[Un]||null).action),x=o.submitter;x&&(n=(n=x[Un]||null)?qg(n.formAction):x.getAttribute("formAction"),n!==null&&(h=n,x=null));var C=new Cl("action","action",null,o,u);e.push({event:C,listeners:[{instance:null,listener:function(){if(o.defaultPrevented){if(Ka!==0){var G=x?Yg(u,x):new FormData(u);Lf(a,{pending:!0,data:G,method:u.method,action:h},null,G)}}else typeof h=="function"&&(C.preventDefault(),G=x?Yg(u,x):new FormData(u),Lf(a,{pending:!0,data:G,method:u.method,action:h},h,G))},currentTarget:u}]})}}for(var ph=0;ph<ju.length;ph++){var mh=ju[ph],fS=mh.toLowerCase(),hS=mh[0].toUpperCase()+mh.slice(1);Ui(fS,"on"+hS)}Ui(bm,"onAnimationEnd"),Ui(Tm,"onAnimationIteration"),Ui(Am,"onAnimationStart"),Ui("dblclick","onDoubleClick"),Ui("focusin","onFocus"),Ui("focusout","onBlur"),Ui(Cy,"onTransitionRun"),Ui(wy,"onTransitionStart"),Ui(Dy,"onTransitionCancel"),Ui(Rm,"onTransitionEnd"),ot("onMouseEnter",["mouseout","mouseover"]),ot("onMouseLeave",["mouseout","mouseover"]),ot("onPointerEnter",["pointerout","pointerover"]),ot("onPointerLeave",["pointerout","pointerover"]),Z("onChange","change click focusin focusout input keydown keyup selectionchange".split(" ")),Z("onSelect","focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(" ")),Z("onBeforeInput",["compositionend","keypress","textInput","paste"]),Z("onCompositionEnd","compositionend focusout keydown keypress keyup mousedown".split(" ")),Z("onCompositionStart","compositionstart focusout keydown keypress keyup mousedown".split(" ")),Z("onCompositionUpdate","compositionupdate focusout keydown keypress keyup mousedown".split(" "));var Xo="abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(" "),dS=new Set("beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(Xo));function Zg(e,n){n=(n&4)!==0;for(var a=0;a<e.length;a++){var o=e[a],u=o.event;o=o.listeners;t:{var h=void 0;if(n)for(var x=o.length-1;0<=x;x--){var C=o[x],G=C.instance,it=C.currentTarget;if(C=C.listener,G!==h&&u.isPropagationStopped())break t;h=C,u.currentTarget=it;try{h(u)}catch(mt){Ul(mt)}u.currentTarget=null,h=G}else for(x=0;x<o.length;x++){if(C=o[x],G=C.instance,it=C.currentTarget,C=C.listener,G!==h&&u.isPropagationStopped())break t;h=C,u.currentTarget=it;try{h(u)}catch(mt){Ul(mt)}u.currentTarget=null,h=G}}}}function Te(e,n){var a=n[gs];a===void 0&&(a=n[gs]=new Set);var o=e+"__bubble";a.has(o)||(jg(n,e,2,!1),a.add(o))}function gh(e,n,a){var o=0;n&&(o|=4),jg(a,e,o,n)}var _c="_reactListening"+Math.random().toString(36).slice(2);function _h(e){if(!e[_c]){e[_c]=!0,bl.forEach(function(a){a!=="selectionchange"&&(dS.has(a)||gh(a,!1,e),gh(a,!0,e))});var n=e.nodeType===9?e:e.ownerDocument;n===null||n[_c]||(n[_c]=!0,gh("selectionchange",!1,n))}}function jg(e,n,a,o){switch(b_(n)){case 2:var u=GS;break;case 8:u=VS;break;default:u=Nh}a=u.bind(null,n,a,e),u=void 0,!zu||n!=="touchstart"&&n!=="touchmove"&&n!=="wheel"||(u=!0),o?u!==void 0?e.addEventListener(n,a,{capture:!0,passive:u}):e.addEventListener(n,a,!0):u!==void 0?e.addEventListener(n,a,{passive:u}):e.addEventListener(n,a,!1)}function vh(e,n,a,o,u){var h=o;if((n&1)===0&&(n&2)===0&&o!==null)t:for(;;){if(o===null)return;var x=o.tag;if(x===3||x===4){var C=o.stateNode.containerInfo;if(C===u)break;if(x===4)for(x=o.return;x!==null;){var G=x.tag;if((G===3||G===4)&&x.stateNode.containerInfo===u)return;x=x.return}for(;C!==null;){if(x=ta(C),x===null)return;if(G=x.tag,G===5||G===6||G===26||G===27){o=h=x;continue t}C=C.parentNode}}o=o.return}tm(function(){var it=h,mt=Ou(a),St=[];t:{var lt=Cm.get(e);if(lt!==void 0){var ct=Cl,Yt=e;switch(e){case"keypress":if(Al(a)===0)break t;case"keydown":case"keyup":ct=ry;break;case"focusin":Yt="focus",ct=Hu;break;case"focusout":Yt="blur",ct=Hu;break;case"beforeblur":case"afterblur":ct=Hu;break;case"click":if(a.button===2)break t;case"auxclick":case"dblclick":case"mousedown":case"mousemove":case"mouseup":case"mouseout":case"mouseover":case"contextmenu":ct=im;break;case"drag":case"dragend":case"dragenter":case"dragexit":case"dragleave":case"dragover":case"dragstart":case"drop":ct=Zx;break;case"touchcancel":case"touchend":case"touchmove":case"touchstart":ct=cy;break;case bm:case Tm:case Am:ct=Qx;break;case Rm:ct=fy;break;case"scroll":case"scrollend":ct=qx;break;case"wheel":ct=dy;break;case"copy":case"cut":case"paste":ct=$x;break;case"gotpointercapture":case"lostpointercapture":case"pointercancel":case"pointerdown":case"pointermove":case"pointerout":case"pointerover":case"pointerup":ct=sm;break;case"toggle":case"beforetoggle":ct=my}var se=(n&4)!==0,Je=!se&&(e==="scroll"||e==="scrollend"),K=se?lt!==null?lt+"Capture":null:lt;se=[];for(var X=it,nt;X!==null;){var xt=X;if(nt=xt.stateNode,xt=xt.tag,xt!==5&&xt!==26&&xt!==27||nt===null||K===null||(xt=fo(X,K),xt!=null&&se.push(Wo(X,xt,nt))),Je)break;X=X.return}0<se.length&&(lt=new ct(lt,Yt,null,a,mt),St.push({event:lt,listeners:se}))}}if((n&7)===0){t:{if(lt=e==="mouseover"||e==="pointerover",ct=e==="mouseout"||e==="pointerout",lt&&a!==Lu&&(Yt=a.relatedTarget||a.fromElement)&&(ta(Yt)||Yt[Yn]))break t;if((ct||lt)&&(lt=mt.window===mt?mt:(lt=mt.ownerDocument)?lt.defaultView||lt.parentWindow:window,ct?(Yt=a.relatedTarget||a.toElement,ct=it,Yt=Yt?ta(Yt):null,Yt!==null&&(Je=c(Yt),se=Yt.tag,Yt!==Je||se!==5&&se!==27&&se!==6)&&(Yt=null)):(ct=null,Yt=it),ct!==Yt)){if(se=im,xt="onMouseLeave",K="onMouseEnter",X="mouse",(e==="pointerout"||e==="pointerover")&&(se=sm,xt="onPointerLeave",K="onPointerEnter",X="pointer"),Je=ct==null?lt:vs(ct),nt=Yt==null?lt:vs(Yt),lt=new se(xt,X+"leave",ct,a,mt),lt.target=Je,lt.relatedTarget=nt,xt=null,ta(mt)===it&&(se=new se(K,X+"enter",Yt,a,mt),se.target=nt,se.relatedTarget=Je,xt=se),Je=xt,ct&&Yt)e:{for(se=pS,K=ct,X=Yt,nt=0,xt=K;xt;xt=se(xt))nt++;xt=0;for(var ie=X;ie;ie=se(ie))xt++;for(;0<nt-xt;)K=se(K),nt--;for(;0<xt-nt;)X=se(X),xt--;for(;nt--;){if(K===X||X!==null&&K===X.alternate){se=K;break e}K=se(K),X=se(X)}se=null}else se=null;ct!==null&&Kg(St,lt,ct,se,!1),Yt!==null&&Je!==null&&Kg(St,Je,Yt,se,!0)}}t:{if(lt=it?vs(it):window,ct=lt.nodeName&&lt.nodeName.toLowerCase(),ct==="select"||ct==="input"&&lt.type==="file")var ze=dm;else if(fm(lt))if(pm)ze=Ty;else{ze=Ey;var Kt=My}else ct=lt.nodeName,!ct||ct.toLowerCase()!=="input"||lt.type!=="checkbox"&&lt.type!=="radio"?it&&Ge(it.elementType)&&(ze=dm):ze=by;if(ze&&(ze=ze(e,it))){hm(St,ze,a,mt);break t}Kt&&Kt(e,lt,it),e==="focusout"&&it&&lt.type==="number"&&it.memoizedProps.value!=null&&Ee(lt,"number",lt.value)}switch(Kt=it?vs(it):window,e){case"focusin":(fm(Kt)||Kt.contentEditable==="true")&&(ir=Kt,qu=it,yo=null);break;case"focusout":yo=qu=ir=null;break;case"mousedown":Yu=!0;break;case"contextmenu":case"mouseup":case"dragend":Yu=!1,Mm(St,a,mt);break;case"selectionchange":if(Ry)break;case"keydown":case"keyup":Mm(St,a,mt)}var ve;if(Vu)t:{switch(e){case"compositionstart":var Re="onCompositionStart";break t;case"compositionend":Re="onCompositionEnd";break t;case"compositionupdate":Re="onCompositionUpdate";break t}Re=void 0}else nr?cm(e,a)&&(Re="onCompositionEnd"):e==="keydown"&&a.keyCode===229&&(Re="onCompositionStart");Re&&(rm&&a.locale!=="ko"&&(nr||Re!=="onCompositionStart"?Re==="onCompositionEnd"&&nr&&(ve=em()):(Oa=mt,Bu="value"in Oa?Oa.value:Oa.textContent,nr=!0)),Kt=vc(it,Re),0<Kt.length&&(Re=new am(Re,e,null,a,mt),St.push({event:Re,listeners:Kt}),ve?Re.data=ve:(ve=um(a),ve!==null&&(Re.data=ve)))),(ve=_y?vy(e,a):xy(e,a))&&(Re=vc(it,"onBeforeInput"),0<Re.length&&(Kt=new am("onBeforeInput","beforeinput",null,a,mt),St.push({event:Kt,listeners:Re}),Kt.data=ve)),uS(St,e,it,a,mt)}Zg(St,n)})}function Wo(e,n,a){return{instance:e,listener:n,currentTarget:a}}function vc(e,n){for(var a=n+"Capture",o=[];e!==null;){var u=e,h=u.stateNode;if(u=u.tag,u!==5&&u!==26&&u!==27||h===null||(u=fo(e,a),u!=null&&o.unshift(Wo(e,u,h)),u=fo(e,n),u!=null&&o.push(Wo(e,u,h))),e.tag===3)return o;e=e.return}return[]}function pS(e){if(e===null)return null;do e=e.return;while(e&&e.tag!==5&&e.tag!==27);return e||null}function Kg(e,n,a,o,u){for(var h=n._reactName,x=[];a!==null&&a!==o;){var C=a,G=C.alternate,it=C.stateNode;if(C=C.tag,G!==null&&G===o)break;C!==5&&C!==26&&C!==27||it===null||(G=it,u?(it=fo(a,h),it!=null&&x.unshift(Wo(a,it,G))):u||(it=fo(a,h),it!=null&&x.push(Wo(a,it,G)))),a=a.return}x.length!==0&&e.push({event:n,listeners:x})}var mS=/\r\n?/g,gS=/\u0000|\uFFFD/g;function Qg(e){return(typeof e=="string"?e:""+e).replace(mS,`
`).replace(gS,"")}function Jg(e,n){return n=Qg(n),Qg(e)===n}function Qe(e,n,a,o,u,h){switch(a){case"children":typeof o=="string"?n==="body"||n==="textarea"&&o===""||ai(e,o):(typeof o=="number"||typeof o=="bigint")&&n!=="body"&&ai(e,""+o);break;case"className":qt(e,"class",o);break;case"tabIndex":qt(e,"tabindex",o);break;case"dir":case"role":case"viewBox":case"width":case"height":qt(e,a,o);break;case"style":Di(e,o,h);break;case"data":if(n!=="object"){qt(e,"data",o);break}case"src":case"href":if(o===""&&(n!=="a"||a!=="href")){e.removeAttribute(a);break}if(o==null||typeof o=="function"||typeof o=="symbol"||typeof o=="boolean"){e.removeAttribute(a);break}o=xs(""+o),e.setAttribute(a,o);break;case"action":case"formAction":if(typeof o=="function"){e.setAttribute(a,"javascript:throw new Error('A React form was unexpectedly submitted. If you called form.submit() manually, consider using form.requestSubmit() instead. If you\\'re trying to use event.stopPropagation() in a submit event handler, consider also calling event.preventDefault().')");break}else typeof h=="function"&&(a==="formAction"?(n!=="input"&&Qe(e,n,"name",u.name,u,null),Qe(e,n,"formEncType",u.formEncType,u,null),Qe(e,n,"formMethod",u.formMethod,u,null),Qe(e,n,"formTarget",u.formTarget,u,null)):(Qe(e,n,"encType",u.encType,u,null),Qe(e,n,"method",u.method,u,null),Qe(e,n,"target",u.target,u,null)));if(o==null||typeof o=="symbol"||typeof o=="boolean"){e.removeAttribute(a);break}o=xs(""+o),e.setAttribute(a,o);break;case"onClick":o!=null&&(e.onclick=na);break;case"onScroll":o!=null&&Te("scroll",e);break;case"onScrollEnd":o!=null&&Te("scrollend",e);break;case"dangerouslySetInnerHTML":if(o!=null){if(typeof o!="object"||!("__html"in o))throw Error(s(61));if(a=o.__html,a!=null){if(u.children!=null)throw Error(s(60));e.innerHTML=a}}break;case"multiple":e.multiple=o&&typeof o!="function"&&typeof o!="symbol";break;case"muted":e.muted=o&&typeof o!="function"&&typeof o!="symbol";break;case"suppressContentEditableWarning":case"suppressHydrationWarning":case"defaultValue":case"defaultChecked":case"innerHTML":case"ref":break;case"autoFocus":break;case"xlinkHref":if(o==null||typeof o=="function"||typeof o=="boolean"||typeof o=="symbol"){e.removeAttribute("xlink:href");break}a=xs(""+o),e.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href",a);break;case"contentEditable":case"spellCheck":case"draggable":case"value":case"autoReverse":case"externalResourcesRequired":case"focusable":case"preserveAlpha":o!=null&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,""+o):e.removeAttribute(a);break;case"inert":case"allowFullScreen":case"async":case"autoPlay":case"controls":case"default":case"defer":case"disabled":case"disablePictureInPicture":case"disableRemotePlayback":case"formNoValidate":case"hidden":case"loop":case"noModule":case"noValidate":case"open":case"playsInline":case"readOnly":case"required":case"reversed":case"scoped":case"seamless":case"itemScope":o&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,""):e.removeAttribute(a);break;case"capture":case"download":o===!0?e.setAttribute(a,""):o!==!1&&o!=null&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,o):e.removeAttribute(a);break;case"cols":case"rows":case"size":case"span":o!=null&&typeof o!="function"&&typeof o!="symbol"&&!isNaN(o)&&1<=o?e.setAttribute(a,o):e.removeAttribute(a);break;case"rowSpan":case"start":o==null||typeof o=="function"||typeof o=="symbol"||isNaN(o)?e.removeAttribute(a):e.setAttribute(a,o);break;case"popover":Te("beforetoggle",e),Te("toggle",e),Pt(e,"popover",o);break;case"xlinkActuate":Wt(e,"http://www.w3.org/1999/xlink","xlink:actuate",o);break;case"xlinkArcrole":Wt(e,"http://www.w3.org/1999/xlink","xlink:arcrole",o);break;case"xlinkRole":Wt(e,"http://www.w3.org/1999/xlink","xlink:role",o);break;case"xlinkShow":Wt(e,"http://www.w3.org/1999/xlink","xlink:show",o);break;case"xlinkTitle":Wt(e,"http://www.w3.org/1999/xlink","xlink:title",o);break;case"xlinkType":Wt(e,"http://www.w3.org/1999/xlink","xlink:type",o);break;case"xmlBase":Wt(e,"http://www.w3.org/XML/1998/namespace","xml:base",o);break;case"xmlLang":Wt(e,"http://www.w3.org/XML/1998/namespace","xml:lang",o);break;case"xmlSpace":Wt(e,"http://www.w3.org/XML/1998/namespace","xml:space",o);break;case"is":Pt(e,"is",o);break;case"innerText":case"textContent":break;default:(!(2<a.length)||a[0]!=="o"&&a[0]!=="O"||a[1]!=="n"&&a[1]!=="N")&&(a=Gi.get(a)||a,Pt(e,a,o))}}function xh(e,n,a,o,u,h){switch(a){case"style":Di(e,o,h);break;case"dangerouslySetInnerHTML":if(o!=null){if(typeof o!="object"||!("__html"in o))throw Error(s(61));if(a=o.__html,a!=null){if(u.children!=null)throw Error(s(60));e.innerHTML=a}}break;case"children":typeof o=="string"?ai(e,o):(typeof o=="number"||typeof o=="bigint")&&ai(e,""+o);break;case"onScroll":o!=null&&Te("scroll",e);break;case"onScrollEnd":o!=null&&Te("scrollend",e);break;case"onClick":o!=null&&(e.onclick=na);break;case"suppressContentEditableWarning":case"suppressHydrationWarning":case"innerHTML":case"ref":break;case"innerText":case"textContent":break;default:if(!R.hasOwnProperty(a))t:{if(a[0]==="o"&&a[1]==="n"&&(u=a.endsWith("Capture"),n=a.slice(2,u?a.length-7:void 0),h=e[Un]||null,h=h!=null?h[a]:null,typeof h=="function"&&e.removeEventListener(n,h,u),typeof o=="function")){typeof h!="function"&&h!==null&&(a in e?e[a]=null:e.hasAttribute(a)&&e.removeAttribute(a)),e.addEventListener(n,o,u);break t}a in e?e[a]=o:o===!0?e.setAttribute(a,""):Pt(e,a,o)}}}function Pn(e,n,a){switch(n){case"div":case"span":case"svg":case"path":case"a":case"g":case"p":case"li":break;case"img":Te("error",e),Te("load",e);var o=!1,u=!1,h;for(h in a)if(a.hasOwnProperty(h)){var x=a[h];if(x!=null)switch(h){case"src":o=!0;break;case"srcSet":u=!0;break;case"children":case"dangerouslySetInnerHTML":throw Error(s(137,n));default:Qe(e,n,h,x,a,null)}}u&&Qe(e,n,"srcSet",a.srcSet,a,null),o&&Qe(e,n,"src",a.src,a,null);return;case"input":Te("invalid",e);var C=h=x=u=null,G=null,it=null;for(o in a)if(a.hasOwnProperty(o)){var mt=a[o];if(mt!=null)switch(o){case"name":u=mt;break;case"type":x=mt;break;case"checked":G=mt;break;case"defaultChecked":it=mt;break;case"value":h=mt;break;case"defaultValue":C=mt;break;case"children":case"dangerouslySetInnerHTML":if(mt!=null)throw Error(s(137,n));break;default:Qe(e,n,o,mt,a,null)}}Bn(e,h,C,G,it,x,u,!1);return;case"select":Te("invalid",e),o=x=h=null;for(u in a)if(a.hasOwnProperty(u)&&(C=a[u],C!=null))switch(u){case"value":h=C;break;case"defaultValue":x=C;break;case"multiple":o=C;default:Qe(e,n,u,C,a,null)}n=h,a=x,e.multiple=!!o,n!=null?bn(e,!!o,n,!1):a!=null&&bn(e,!!o,a,!0);return;case"textarea":Te("invalid",e),h=u=o=null;for(x in a)if(a.hasOwnProperty(x)&&(C=a[x],C!=null))switch(x){case"value":o=C;break;case"defaultValue":u=C;break;case"children":h=C;break;case"dangerouslySetInnerHTML":if(C!=null)throw Error(s(91));break;default:Qe(e,n,x,C,a,null)}wi(e,o,u,h);return;case"option":for(G in a)if(a.hasOwnProperty(G)&&(o=a[G],o!=null))switch(G){case"selected":e.selected=o&&typeof o!="function"&&typeof o!="symbol";break;default:Qe(e,n,G,o,a,null)}return;case"dialog":Te("beforetoggle",e),Te("toggle",e),Te("cancel",e),Te("close",e);break;case"iframe":case"object":Te("load",e);break;case"video":case"audio":for(o=0;o<Xo.length;o++)Te(Xo[o],e);break;case"image":Te("error",e),Te("load",e);break;case"details":Te("toggle",e);break;case"embed":case"source":case"link":Te("error",e),Te("load",e);case"area":case"base":case"br":case"col":case"hr":case"keygen":case"meta":case"param":case"track":case"wbr":case"menuitem":for(it in a)if(a.hasOwnProperty(it)&&(o=a[it],o!=null))switch(it){case"children":case"dangerouslySetInnerHTML":throw Error(s(137,n));default:Qe(e,n,it,o,a,null)}return;default:if(Ge(n)){for(mt in a)a.hasOwnProperty(mt)&&(o=a[mt],o!==void 0&&xh(e,n,mt,o,a,void 0));return}}for(C in a)a.hasOwnProperty(C)&&(o=a[C],o!=null&&Qe(e,n,C,o,a,null))}function _S(e,n,a,o){switch(n){case"div":case"span":case"svg":case"path":case"a":case"g":case"p":case"li":break;case"input":var u=null,h=null,x=null,C=null,G=null,it=null,mt=null;for(ct in a){var St=a[ct];if(a.hasOwnProperty(ct)&&St!=null)switch(ct){case"checked":break;case"value":break;case"defaultValue":G=St;default:o.hasOwnProperty(ct)||Qe(e,n,ct,null,o,St)}}for(var lt in o){var ct=o[lt];if(St=a[lt],o.hasOwnProperty(lt)&&(ct!=null||St!=null))switch(lt){case"type":h=ct;break;case"name":u=ct;break;case"checked":it=ct;break;case"defaultChecked":mt=ct;break;case"value":x=ct;break;case"defaultValue":C=ct;break;case"children":case"dangerouslySetInnerHTML":if(ct!=null)throw Error(s(137,n));break;default:ct!==St&&Qe(e,n,lt,ct,o,St)}}Gt(e,x,C,G,it,mt,h,u);return;case"select":ct=x=C=lt=null;for(h in a)if(G=a[h],a.hasOwnProperty(h)&&G!=null)switch(h){case"value":break;case"multiple":ct=G;default:o.hasOwnProperty(h)||Qe(e,n,h,null,o,G)}for(u in o)if(h=o[u],G=a[u],o.hasOwnProperty(u)&&(h!=null||G!=null))switch(u){case"value":lt=h;break;case"defaultValue":C=h;break;case"multiple":x=h;default:h!==G&&Qe(e,n,u,h,o,G)}n=C,a=x,o=ct,lt!=null?bn(e,!!a,lt,!1):!!o!=!!a&&(n!=null?bn(e,!!a,n,!0):bn(e,!!a,a?[]:"",!1));return;case"textarea":ct=lt=null;for(C in a)if(u=a[C],a.hasOwnProperty(C)&&u!=null&&!o.hasOwnProperty(C))switch(C){case"value":break;case"children":break;default:Qe(e,n,C,null,o,u)}for(x in o)if(u=o[x],h=a[x],o.hasOwnProperty(x)&&(u!=null||h!=null))switch(x){case"value":lt=u;break;case"defaultValue":ct=u;break;case"children":break;case"dangerouslySetInnerHTML":if(u!=null)throw Error(s(91));break;default:u!==h&&Qe(e,n,x,u,o,h)}ii(e,lt,ct);return;case"option":for(var Yt in a)if(lt=a[Yt],a.hasOwnProperty(Yt)&&lt!=null&&!o.hasOwnProperty(Yt))switch(Yt){case"selected":e.selected=!1;break;default:Qe(e,n,Yt,null,o,lt)}for(G in o)if(lt=o[G],ct=a[G],o.hasOwnProperty(G)&&lt!==ct&&(lt!=null||ct!=null))switch(G){case"selected":e.selected=lt&&typeof lt!="function"&&typeof lt!="symbol";break;default:Qe(e,n,G,lt,o,ct)}return;case"img":case"link":case"area":case"base":case"br":case"col":case"embed":case"hr":case"keygen":case"meta":case"param":case"source":case"track":case"wbr":case"menuitem":for(var se in a)lt=a[se],a.hasOwnProperty(se)&&lt!=null&&!o.hasOwnProperty(se)&&Qe(e,n,se,null,o,lt);for(it in o)if(lt=o[it],ct=a[it],o.hasOwnProperty(it)&&lt!==ct&&(lt!=null||ct!=null))switch(it){case"children":case"dangerouslySetInnerHTML":if(lt!=null)throw Error(s(137,n));break;default:Qe(e,n,it,lt,o,ct)}return;default:if(Ge(n)){for(var Je in a)lt=a[Je],a.hasOwnProperty(Je)&&lt!==void 0&&!o.hasOwnProperty(Je)&&xh(e,n,Je,void 0,o,lt);for(mt in o)lt=o[mt],ct=a[mt],!o.hasOwnProperty(mt)||lt===ct||lt===void 0&&ct===void 0||xh(e,n,mt,lt,o,ct);return}}for(var K in a)lt=a[K],a.hasOwnProperty(K)&&lt!=null&&!o.hasOwnProperty(K)&&Qe(e,n,K,null,o,lt);for(St in o)lt=o[St],ct=a[St],!o.hasOwnProperty(St)||lt===ct||lt==null&&ct==null||Qe(e,n,St,lt,o,ct)}function $g(e){switch(e){case"css":case"script":case"font":case"img":case"image":case"input":case"link":return!0;default:return!1}}function vS(){if(typeof performance.getEntriesByType=="function"){for(var e=0,n=0,a=performance.getEntriesByType("resource"),o=0;o<a.length;o++){var u=a[o],h=u.transferSize,x=u.initiatorType,C=u.duration;if(h&&C&&$g(x)){for(x=0,C=u.responseEnd,o+=1;o<a.length;o++){var G=a[o],it=G.startTime;if(it>C)break;var mt=G.transferSize,St=G.initiatorType;mt&&$g(St)&&(G=G.responseEnd,x+=mt*(G<C?1:(C-it)/(G-it)))}if(--o,n+=8*(h+x)/(u.duration/1e3),e++,10<e)break}}if(0<e)return n/e/1e6}return navigator.connection&&(e=navigator.connection.downlink,typeof e=="number")?e:5}var yh=null,Sh=null;function xc(e){return e.nodeType===9?e:e.ownerDocument}function t_(e){switch(e){case"http://www.w3.org/2000/svg":return 1;case"http://www.w3.org/1998/Math/MathML":return 2;default:return 0}}function e_(e,n){if(e===0)switch(n){case"svg":return 1;case"math":return 2;default:return 0}return e===1&&n==="foreignObject"?0:e}function Mh(e,n){return e==="textarea"||e==="noscript"||typeof n.children=="string"||typeof n.children=="number"||typeof n.children=="bigint"||typeof n.dangerouslySetInnerHTML=="object"&&n.dangerouslySetInnerHTML!==null&&n.dangerouslySetInnerHTML.__html!=null}var Eh=null;function xS(){var e=window.event;return e&&e.type==="popstate"?e===Eh?!1:(Eh=e,!0):(Eh=null,!1)}var n_=typeof setTimeout=="function"?setTimeout:void 0,yS=typeof clearTimeout=="function"?clearTimeout:void 0,i_=typeof Promise=="function"?Promise:void 0,SS=typeof queueMicrotask=="function"?queueMicrotask:typeof i_<"u"?function(e){return i_.resolve(null).then(e).catch(MS)}:n_;function MS(e){setTimeout(function(){throw e})}function Qa(e){return e==="head"}function a_(e,n){var a=n,o=0;do{var u=a.nextSibling;if(e.removeChild(a),u&&u.nodeType===8)if(a=u.data,a==="/$"||a==="/&"){if(o===0){e.removeChild(u),Dr(n);return}o--}else if(a==="$"||a==="$?"||a==="$~"||a==="$!"||a==="&")o++;else if(a==="html")qo(e.ownerDocument.documentElement);else if(a==="head"){a=e.ownerDocument.head,qo(a);for(var h=a.firstChild;h;){var x=h.nextSibling,C=h.nodeName;h[Da]||C==="SCRIPT"||C==="STYLE"||C==="LINK"&&h.rel.toLowerCase()==="stylesheet"||a.removeChild(h),h=x}}else a==="body"&&qo(e.ownerDocument.body);a=u}while(a);Dr(n)}function s_(e,n){var a=e;e=0;do{var o=a.nextSibling;if(a.nodeType===1?n?(a._stashedDisplay=a.style.display,a.style.display="none"):(a.style.display=a._stashedDisplay||"",a.getAttribute("style")===""&&a.removeAttribute("style")):a.nodeType===3&&(n?(a._stashedText=a.nodeValue,a.nodeValue=""):a.nodeValue=a._stashedText||""),o&&o.nodeType===8)if(a=o.data,a==="/$"){if(e===0)break;e--}else a!=="$"&&a!=="$?"&&a!=="$~"&&a!=="$!"||e++;a=o}while(a)}function bh(e){var n=e.firstChild;for(n&&n.nodeType===10&&(n=n.nextSibling);n;){var a=n;switch(n=n.nextSibling,a.nodeName){case"HTML":case"HEAD":case"BODY":bh(a),Ua(a);continue;case"SCRIPT":case"STYLE":continue;case"LINK":if(a.rel.toLowerCase()==="stylesheet")continue}e.removeChild(a)}}function ES(e,n,a,o){for(;e.nodeType===1;){var u=a;if(e.nodeName.toLowerCase()!==n.toLowerCase()){if(!o&&(e.nodeName!=="INPUT"||e.type!=="hidden"))break}else if(o){if(!e[Da])switch(n){case"meta":if(!e.hasAttribute("itemprop"))break;return e;case"link":if(h=e.getAttribute("rel"),h==="stylesheet"&&e.hasAttribute("data-precedence"))break;if(h!==u.rel||e.getAttribute("href")!==(u.href==null||u.href===""?null:u.href)||e.getAttribute("crossorigin")!==(u.crossOrigin==null?null:u.crossOrigin)||e.getAttribute("title")!==(u.title==null?null:u.title))break;return e;case"style":if(e.hasAttribute("data-precedence"))break;return e;case"script":if(h=e.getAttribute("src"),(h!==(u.src==null?null:u.src)||e.getAttribute("type")!==(u.type==null?null:u.type)||e.getAttribute("crossorigin")!==(u.crossOrigin==null?null:u.crossOrigin))&&h&&e.hasAttribute("async")&&!e.hasAttribute("itemprop"))break;return e;default:return e}}else if(n==="input"&&e.type==="hidden"){var h=u.name==null?null:""+u.name;if(u.type==="hidden"&&e.getAttribute("name")===h)return e}else return e;if(e=Ti(e.nextSibling),e===null)break}return null}function bS(e,n,a){if(n==="")return null;for(;e.nodeType!==3;)if((e.nodeType!==1||e.nodeName!=="INPUT"||e.type!=="hidden")&&!a||(e=Ti(e.nextSibling),e===null))return null;return e}function r_(e,n){for(;e.nodeType!==8;)if((e.nodeType!==1||e.nodeName!=="INPUT"||e.type!=="hidden")&&!n||(e=Ti(e.nextSibling),e===null))return null;return e}function Th(e){return e.data==="$?"||e.data==="$~"}function Ah(e){return e.data==="$!"||e.data==="$?"&&e.ownerDocument.readyState!=="loading"}function TS(e,n){var a=e.ownerDocument;if(e.data==="$~")e._reactRetry=n;else if(e.data!=="$?"||a.readyState!=="loading")n();else{var o=function(){n(),a.removeEventListener("DOMContentLoaded",o)};a.addEventListener("DOMContentLoaded",o),e._reactRetry=o}}function Ti(e){for(;e!=null;e=e.nextSibling){var n=e.nodeType;if(n===1||n===3)break;if(n===8){if(n=e.data,n==="$"||n==="$!"||n==="$?"||n==="$~"||n==="&"||n==="F!"||n==="F")break;if(n==="/$"||n==="/&")return null}}return e}var Rh=null;function o_(e){e=e.nextSibling;for(var n=0;e;){if(e.nodeType===8){var a=e.data;if(a==="/$"||a==="/&"){if(n===0)return Ti(e.nextSibling);n--}else a!=="$"&&a!=="$!"&&a!=="$?"&&a!=="$~"&&a!=="&"||n++}e=e.nextSibling}return null}function l_(e){e=e.previousSibling;for(var n=0;e;){if(e.nodeType===8){var a=e.data;if(a==="$"||a==="$!"||a==="$?"||a==="$~"||a==="&"){if(n===0)return e;n--}else a!=="/$"&&a!=="/&"||n++}e=e.previousSibling}return null}function c_(e,n,a){switch(n=xc(a),e){case"html":if(e=n.documentElement,!e)throw Error(s(452));return e;case"head":if(e=n.head,!e)throw Error(s(453));return e;case"body":if(e=n.body,!e)throw Error(s(454));return e;default:throw Error(s(451))}}function qo(e){for(var n=e.attributes;n.length;)e.removeAttributeNode(n[0]);Ua(e)}var Ai=new Map,u_=new Set;function yc(e){return typeof e.getRootNode=="function"?e.getRootNode():e.nodeType===9?e:e.ownerDocument}var va=H.d;H.d={f:AS,r:RS,D:CS,C:wS,L:DS,m:US,X:LS,S:NS,M:OS};function AS(){var e=va.f(),n=fc();return e||n}function RS(e){var n=ea(e);n!==null&&n.tag===5&&n.type==="form"?R0(n):va.r(e)}var Rr=typeof document>"u"?null:document;function f_(e,n,a){var o=Rr;if(o&&typeof n=="string"&&n){var u=Xe(n);u='link[rel="'+e+'"][href="'+u+'"]',typeof a=="string"&&(u+='[crossorigin="'+a+'"]'),u_.has(u)||(u_.add(u),e={rel:e,crossOrigin:a,href:n},o.querySelector(u)===null&&(n=o.createElement("link"),Pn(n,"link",e),gn(n),o.head.appendChild(n)))}}function CS(e){va.D(e),f_("dns-prefetch",e,null)}function wS(e,n){va.C(e,n),f_("preconnect",e,n)}function DS(e,n,a){va.L(e,n,a);var o=Rr;if(o&&e&&n){var u='link[rel="preload"][as="'+Xe(n)+'"]';n==="image"&&a&&a.imageSrcSet?(u+='[imagesrcset="'+Xe(a.imageSrcSet)+'"]',typeof a.imageSizes=="string"&&(u+='[imagesizes="'+Xe(a.imageSizes)+'"]')):u+='[href="'+Xe(e)+'"]';var h=u;switch(n){case"style":h=Cr(e);break;case"script":h=wr(e)}Ai.has(h)||(e=v({rel:"preload",href:n==="image"&&a&&a.imageSrcSet?void 0:e,as:n},a),Ai.set(h,e),o.querySelector(u)!==null||n==="style"&&o.querySelector(Yo(h))||n==="script"&&o.querySelector(Zo(h))||(n=o.createElement("link"),Pn(n,"link",e),gn(n),o.head.appendChild(n)))}}function US(e,n){va.m(e,n);var a=Rr;if(a&&e){var o=n&&typeof n.as=="string"?n.as:"script",u='link[rel="modulepreload"][as="'+Xe(o)+'"][href="'+Xe(e)+'"]',h=u;switch(o){case"audioworklet":case"paintworklet":case"serviceworker":case"sharedworker":case"worker":case"script":h=wr(e)}if(!Ai.has(h)&&(e=v({rel:"modulepreload",href:e},n),Ai.set(h,e),a.querySelector(u)===null)){switch(o){case"audioworklet":case"paintworklet":case"serviceworker":case"sharedworker":case"worker":case"script":if(a.querySelector(Zo(h)))return}o=a.createElement("link"),Pn(o,"link",e),gn(o),a.head.appendChild(o)}}}function NS(e,n,a){va.S(e,n,a);var o=Rr;if(o&&e){var u=Na(o).hoistableStyles,h=Cr(e);n=n||"default";var x=u.get(h);if(!x){var C={loading:0,preload:null};if(x=o.querySelector(Yo(h)))C.loading=5;else{e=v({rel:"stylesheet",href:e,"data-precedence":n},a),(a=Ai.get(h))&&Ch(e,a);var G=x=o.createElement("link");gn(G),Pn(G,"link",e),G._p=new Promise(function(it,mt){G.onload=it,G.onerror=mt}),G.addEventListener("load",function(){C.loading|=1}),G.addEventListener("error",function(){C.loading|=2}),C.loading|=4,Sc(x,n,o)}x={type:"stylesheet",instance:x,count:1,state:C},u.set(h,x)}}}function LS(e,n){va.X(e,n);var a=Rr;if(a&&e){var o=Na(a).hoistableScripts,u=wr(e),h=o.get(u);h||(h=a.querySelector(Zo(u)),h||(e=v({src:e,async:!0},n),(n=Ai.get(u))&&wh(e,n),h=a.createElement("script"),gn(h),Pn(h,"link",e),a.head.appendChild(h)),h={type:"script",instance:h,count:1,state:null},o.set(u,h))}}function OS(e,n){va.M(e,n);var a=Rr;if(a&&e){var o=Na(a).hoistableScripts,u=wr(e),h=o.get(u);h||(h=a.querySelector(Zo(u)),h||(e=v({src:e,async:!0,type:"module"},n),(n=Ai.get(u))&&wh(e,n),h=a.createElement("script"),gn(h),Pn(h,"link",e),a.head.appendChild(h)),h={type:"script",instance:h,count:1,state:null},o.set(u,h))}}function h_(e,n,a,o){var u=(u=tt.current)?yc(u):null;if(!u)throw Error(s(446));switch(e){case"meta":case"title":return null;case"style":return typeof a.precedence=="string"&&typeof a.href=="string"?(n=Cr(a.href),a=Na(u).hoistableStyles,o=a.get(n),o||(o={type:"style",instance:null,count:0,state:null},a.set(n,o)),o):{type:"void",instance:null,count:0,state:null};case"link":if(a.rel==="stylesheet"&&typeof a.href=="string"&&typeof a.precedence=="string"){e=Cr(a.href);var h=Na(u).hoistableStyles,x=h.get(e);if(x||(u=u.ownerDocument||u,x={type:"stylesheet",instance:null,count:0,state:{loading:0,preload:null}},h.set(e,x),(h=u.querySelector(Yo(e)))&&!h._p&&(x.instance=h,x.state.loading=5),Ai.has(e)||(a={rel:"preload",as:"style",href:a.href,crossOrigin:a.crossOrigin,integrity:a.integrity,media:a.media,hrefLang:a.hrefLang,referrerPolicy:a.referrerPolicy},Ai.set(e,a),h||PS(u,e,a,x.state))),n&&o===null)throw Error(s(528,""));return x}if(n&&o!==null)throw Error(s(529,""));return null;case"script":return n=a.async,a=a.src,typeof a=="string"&&n&&typeof n!="function"&&typeof n!="symbol"?(n=wr(a),a=Na(u).hoistableScripts,o=a.get(n),o||(o={type:"script",instance:null,count:0,state:null},a.set(n,o)),o):{type:"void",instance:null,count:0,state:null};default:throw Error(s(444,e))}}function Cr(e){return'href="'+Xe(e)+'"'}function Yo(e){return'link[rel="stylesheet"]['+e+"]"}function d_(e){return v({},e,{"data-precedence":e.precedence,precedence:null})}function PS(e,n,a,o){e.querySelector('link[rel="preload"][as="style"]['+n+"]")?o.loading=1:(n=e.createElement("link"),o.preload=n,n.addEventListener("load",function(){return o.loading|=1}),n.addEventListener("error",function(){return o.loading|=2}),Pn(n,"link",a),gn(n),e.head.appendChild(n))}function wr(e){return'[src="'+Xe(e)+'"]'}function Zo(e){return"script[async]"+e}function p_(e,n,a){if(n.count++,n.instance===null)switch(n.type){case"style":var o=e.querySelector('style[data-href~="'+Xe(a.href)+'"]');if(o)return n.instance=o,gn(o),o;var u=v({},a,{"data-href":a.href,"data-precedence":a.precedence,href:null,precedence:null});return o=(e.ownerDocument||e).createElement("style"),gn(o),Pn(o,"style",u),Sc(o,a.precedence,e),n.instance=o;case"stylesheet":u=Cr(a.href);var h=e.querySelector(Yo(u));if(h)return n.state.loading|=4,n.instance=h,gn(h),h;o=d_(a),(u=Ai.get(u))&&Ch(o,u),h=(e.ownerDocument||e).createElement("link"),gn(h);var x=h;return x._p=new Promise(function(C,G){x.onload=C,x.onerror=G}),Pn(h,"link",o),n.state.loading|=4,Sc(h,a.precedence,e),n.instance=h;case"script":return h=wr(a.src),(u=e.querySelector(Zo(h)))?(n.instance=u,gn(u),u):(o=a,(u=Ai.get(h))&&(o=v({},a),wh(o,u)),e=e.ownerDocument||e,u=e.createElement("script"),gn(u),Pn(u,"link",o),e.head.appendChild(u),n.instance=u);case"void":return null;default:throw Error(s(443,n.type))}else n.type==="stylesheet"&&(n.state.loading&4)===0&&(o=n.instance,n.state.loading|=4,Sc(o,a.precedence,e));return n.instance}function Sc(e,n,a){for(var o=a.querySelectorAll('link[rel="stylesheet"][data-precedence],style[data-precedence]'),u=o.length?o[o.length-1]:null,h=u,x=0;x<o.length;x++){var C=o[x];if(C.dataset.precedence===n)h=C;else if(h!==u)break}h?h.parentNode.insertBefore(e,h.nextSibling):(n=a.nodeType===9?a.head:a,n.insertBefore(e,n.firstChild))}function Ch(e,n){e.crossOrigin==null&&(e.crossOrigin=n.crossOrigin),e.referrerPolicy==null&&(e.referrerPolicy=n.referrerPolicy),e.title==null&&(e.title=n.title)}function wh(e,n){e.crossOrigin==null&&(e.crossOrigin=n.crossOrigin),e.referrerPolicy==null&&(e.referrerPolicy=n.referrerPolicy),e.integrity==null&&(e.integrity=n.integrity)}var Mc=null;function m_(e,n,a){if(Mc===null){var o=new Map,u=Mc=new Map;u.set(a,o)}else u=Mc,o=u.get(a),o||(o=new Map,u.set(a,o));if(o.has(e))return o;for(o.set(e,null),a=a.getElementsByTagName(e),u=0;u<a.length;u++){var h=a[u];if(!(h[Da]||h[mn]||e==="link"&&h.getAttribute("rel")==="stylesheet")&&h.namespaceURI!=="http://www.w3.org/2000/svg"){var x=h.getAttribute(n)||"";x=e+x;var C=o.get(x);C?C.push(h):o.set(x,[h])}}return o}function g_(e,n,a){e=e.ownerDocument||e,e.head.insertBefore(a,n==="title"?e.querySelector("head > title"):null)}function zS(e,n,a){if(a===1||n.itemProp!=null)return!1;switch(e){case"meta":case"title":return!0;case"style":if(typeof n.precedence!="string"||typeof n.href!="string"||n.href==="")break;return!0;case"link":if(typeof n.rel!="string"||typeof n.href!="string"||n.href===""||n.onLoad||n.onError)break;switch(n.rel){case"stylesheet":return e=n.disabled,typeof n.precedence=="string"&&e==null;default:return!0}case"script":if(n.async&&typeof n.async!="function"&&typeof n.async!="symbol"&&!n.onLoad&&!n.onError&&n.src&&typeof n.src=="string")return!0}return!1}function __(e){return!(e.type==="stylesheet"&&(e.state.loading&3)===0)}function BS(e,n,a,o){if(a.type==="stylesheet"&&(typeof o.media!="string"||matchMedia(o.media).matches!==!1)&&(a.state.loading&4)===0){if(a.instance===null){var u=Cr(o.href),h=n.querySelector(Yo(u));if(h){n=h._p,n!==null&&typeof n=="object"&&typeof n.then=="function"&&(e.count++,e=Ec.bind(e),n.then(e,e)),a.state.loading|=4,a.instance=h,gn(h);return}h=n.ownerDocument||n,o=d_(o),(u=Ai.get(u))&&Ch(o,u),h=h.createElement("link"),gn(h);var x=h;x._p=new Promise(function(C,G){x.onload=C,x.onerror=G}),Pn(h,"link",o),a.instance=h}e.stylesheets===null&&(e.stylesheets=new Map),e.stylesheets.set(a,n),(n=a.state.preload)&&(a.state.loading&3)===0&&(e.count++,a=Ec.bind(e),n.addEventListener("load",a),n.addEventListener("error",a))}}var Dh=0;function IS(e,n){return e.stylesheets&&e.count===0&&Tc(e,e.stylesheets),0<e.count||0<e.imgCount?function(a){var o=setTimeout(function(){if(e.stylesheets&&Tc(e,e.stylesheets),e.unsuspend){var h=e.unsuspend;e.unsuspend=null,h()}},6e4+n);0<e.imgBytes&&Dh===0&&(Dh=62500*vS());var u=setTimeout(function(){if(e.waitingForImages=!1,e.count===0&&(e.stylesheets&&Tc(e,e.stylesheets),e.unsuspend)){var h=e.unsuspend;e.unsuspend=null,h()}},(e.imgBytes>Dh?50:800)+n);return e.unsuspend=a,function(){e.unsuspend=null,clearTimeout(o),clearTimeout(u)}}:null}function Ec(){if(this.count--,this.count===0&&(this.imgCount===0||!this.waitingForImages)){if(this.stylesheets)Tc(this,this.stylesheets);else if(this.unsuspend){var e=this.unsuspend;this.unsuspend=null,e()}}}var bc=null;function Tc(e,n){e.stylesheets=null,e.unsuspend!==null&&(e.count++,bc=new Map,n.forEach(FS,e),bc=null,Ec.call(e))}function FS(e,n){if(!(n.state.loading&4)){var a=bc.get(e);if(a)var o=a.get(null);else{a=new Map,bc.set(e,a);for(var u=e.querySelectorAll("link[data-precedence],style[data-precedence]"),h=0;h<u.length;h++){var x=u[h];(x.nodeName==="LINK"||x.getAttribute("media")!=="not all")&&(a.set(x.dataset.precedence,x),o=x)}o&&a.set(null,o)}u=n.instance,x=u.getAttribute("data-precedence"),h=a.get(x)||o,h===o&&a.set(null,u),a.set(x,u),this.count++,o=Ec.bind(this),u.addEventListener("load",o),u.addEventListener("error",o),h?h.parentNode.insertBefore(u,h.nextSibling):(e=e.nodeType===9?e.head:e,e.insertBefore(u,e.firstChild)),n.state.loading|=4}}var jo={$$typeof:F,Provider:null,Consumer:null,_currentValue:et,_currentValue2:et,_threadCount:0};function HS(e,n,a,o,u,h,x,C,G){this.tag=1,this.containerInfo=e,this.pingCache=this.current=this.pendingChildren=null,this.timeoutHandle=-1,this.callbackNode=this.next=this.pendingContext=this.context=this.cancelPendingCommit=null,this.callbackPriority=0,this.expirationTimes=jt(-1),this.entangledLanes=this.shellSuspendCounter=this.errorRecoveryDisabledLanes=this.expiredLanes=this.warmLanes=this.pingedLanes=this.suspendedLanes=this.pendingLanes=0,this.entanglements=jt(0),this.hiddenUpdates=jt(null),this.identifierPrefix=o,this.onUncaughtError=u,this.onCaughtError=h,this.onRecoverableError=x,this.pooledCache=null,this.pooledCacheLanes=0,this.formState=G,this.incompleteTransitions=new Map}function v_(e,n,a,o,u,h,x,C,G,it,mt,St){return e=new HS(e,n,a,x,G,it,mt,St,C),n=1,h===!0&&(n|=24),h=ri(3,null,null,n),e.current=h,h.stateNode=e,n=cf(),n.refCount++,e.pooledCache=n,n.refCount++,h.memoizedState={element:o,isDehydrated:a,cache:n},df(h),e}function x_(e){return e?(e=rr,e):rr}function y_(e,n,a,o,u,h){u=x_(u),o.context===null?o.context=u:o.pendingContext=u,o=Ha(n),o.payload={element:a},h=h===void 0?null:h,h!==null&&(o.callback=h),a=Ga(e,o,n),a!==null&&($n(a,e,n),Ro(a,e,n))}function S_(e,n){if(e=e.memoizedState,e!==null&&e.dehydrated!==null){var a=e.retryLane;e.retryLane=a!==0&&a<n?a:n}}function Uh(e,n){S_(e,n),(e=e.alternate)&&S_(e,n)}function M_(e){if(e.tag===13||e.tag===31){var n=Es(e,67108864);n!==null&&$n(n,e,67108864),Uh(e,67108864)}}function E_(e){if(e.tag===13||e.tag===31){var n=fi();n=lo(n);var a=Es(e,n);a!==null&&$n(a,e,n),Uh(e,n)}}var Ac=!0;function GS(e,n,a,o){var u=P.T;P.T=null;var h=H.p;try{H.p=2,Nh(e,n,a,o)}finally{H.p=h,P.T=u}}function VS(e,n,a,o){var u=P.T;P.T=null;var h=H.p;try{H.p=8,Nh(e,n,a,o)}finally{H.p=h,P.T=u}}function Nh(e,n,a,o){if(Ac){var u=Lh(o);if(u===null)vh(e,n,o,Rc,a),T_(e,o);else if(XS(u,e,n,a,o))o.stopPropagation();else if(T_(e,o),n&4&&-1<kS.indexOf(e)){for(;u!==null;){var h=ea(u);if(h!==null)switch(h.tag){case 3:if(h=h.stateNode,h.current.memoizedState.isDehydrated){var x=Ct(h.pendingLanes);if(x!==0){var C=h;for(C.pendingLanes|=2,C.entangledLanes|=2;x;){var G=1<<31-Ht(x);C.entanglements[1]|=G,x&=~G}Wi(h),(Ie&6)===0&&(cc=we()+500,ko(0))}}break;case 31:case 13:C=Es(h,2),C!==null&&$n(C,h,2),fc(),Uh(h,2)}if(h=Lh(o),h===null&&vh(e,n,o,Rc,a),h===u)break;u=h}u!==null&&o.stopPropagation()}else vh(e,n,o,null,a)}}function Lh(e){return e=Ou(e),Oh(e)}var Rc=null;function Oh(e){if(Rc=null,e=ta(e),e!==null){var n=c(e);if(n===null)e=null;else{var a=n.tag;if(a===13){if(e=f(n),e!==null)return e;e=null}else if(a===31){if(e=p(n),e!==null)return e;e=null}else if(a===3){if(n.stateNode.current.memoizedState.isDehydrated)return n.tag===3?n.stateNode.containerInfo:null;e=null}else n!==e&&(e=null)}}return Rc=e,null}function b_(e){switch(e){case"beforetoggle":case"cancel":case"click":case"close":case"contextmenu":case"copy":case"cut":case"auxclick":case"dblclick":case"dragend":case"dragstart":case"drop":case"focusin":case"focusout":case"input":case"invalid":case"keydown":case"keypress":case"keyup":case"mousedown":case"mouseup":case"paste":case"pause":case"play":case"pointercancel":case"pointerdown":case"pointerup":case"ratechange":case"reset":case"resize":case"seeked":case"submit":case"toggle":case"touchcancel":case"touchend":case"touchstart":case"volumechange":case"change":case"selectionchange":case"textInput":case"compositionstart":case"compositionend":case"compositionupdate":case"beforeblur":case"afterblur":case"beforeinput":case"blur":case"fullscreenchange":case"focus":case"hashchange":case"popstate":case"select":case"selectstart":return 2;case"drag":case"dragenter":case"dragexit":case"dragleave":case"dragover":case"mousemove":case"mouseout":case"mouseover":case"pointermove":case"pointerout":case"pointerover":case"scroll":case"touchmove":case"wheel":case"mouseenter":case"mouseleave":case"pointerenter":case"pointerleave":return 8;case"message":switch(Le()){case U:return 2;case b:return 8;case Q:case rt:return 32;case ht:return 268435456;default:return 32}default:return 32}}var Ph=!1,Ja=null,$a=null,ts=null,Ko=new Map,Qo=new Map,es=[],kS="mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(" ");function T_(e,n){switch(e){case"focusin":case"focusout":Ja=null;break;case"dragenter":case"dragleave":$a=null;break;case"mouseover":case"mouseout":ts=null;break;case"pointerover":case"pointerout":Ko.delete(n.pointerId);break;case"gotpointercapture":case"lostpointercapture":Qo.delete(n.pointerId)}}function Jo(e,n,a,o,u,h){return e===null||e.nativeEvent!==h?(e={blockedOn:n,domEventName:a,eventSystemFlags:o,nativeEvent:h,targetContainers:[u]},n!==null&&(n=ea(n),n!==null&&M_(n)),e):(e.eventSystemFlags|=o,n=e.targetContainers,u!==null&&n.indexOf(u)===-1&&n.push(u),e)}function XS(e,n,a,o,u){switch(n){case"focusin":return Ja=Jo(Ja,e,n,a,o,u),!0;case"dragenter":return $a=Jo($a,e,n,a,o,u),!0;case"mouseover":return ts=Jo(ts,e,n,a,o,u),!0;case"pointerover":var h=u.pointerId;return Ko.set(h,Jo(Ko.get(h)||null,e,n,a,o,u)),!0;case"gotpointercapture":return h=u.pointerId,Qo.set(h,Jo(Qo.get(h)||null,e,n,a,o,u)),!0}return!1}function A_(e){var n=ta(e.target);if(n!==null){var a=c(n);if(a!==null){if(n=a.tag,n===13){if(n=f(a),n!==null){e.blockedOn=n,$s(e.priority,function(){E_(a)});return}}else if(n===31){if(n=p(a),n!==null){e.blockedOn=n,$s(e.priority,function(){E_(a)});return}}else if(n===3&&a.stateNode.current.memoizedState.isDehydrated){e.blockedOn=a.tag===3?a.stateNode.containerInfo:null;return}}}e.blockedOn=null}function Cc(e){if(e.blockedOn!==null)return!1;for(var n=e.targetContainers;0<n.length;){var a=Lh(e.nativeEvent);if(a===null){a=e.nativeEvent;var o=new a.constructor(a.type,a);Lu=o,a.target.dispatchEvent(o),Lu=null}else return n=ea(a),n!==null&&M_(n),e.blockedOn=a,!1;n.shift()}return!0}function R_(e,n,a){Cc(e)&&a.delete(n)}function WS(){Ph=!1,Ja!==null&&Cc(Ja)&&(Ja=null),$a!==null&&Cc($a)&&($a=null),ts!==null&&Cc(ts)&&(ts=null),Ko.forEach(R_),Qo.forEach(R_)}function wc(e,n){e.blockedOn===n&&(e.blockedOn=null,Ph||(Ph=!0,r.unstable_scheduleCallback(r.unstable_NormalPriority,WS)))}var Dc=null;function C_(e){Dc!==e&&(Dc=e,r.unstable_scheduleCallback(r.unstable_NormalPriority,function(){Dc===e&&(Dc=null);for(var n=0;n<e.length;n+=3){var a=e[n],o=e[n+1],u=e[n+2];if(typeof o!="function"){if(Oh(o||a)===null)continue;break}var h=ea(a);h!==null&&(e.splice(n,3),n-=3,Lf(h,{pending:!0,data:u,method:a.method,action:o},o,u))}}))}function Dr(e){function n(G){return wc(G,e)}Ja!==null&&wc(Ja,e),$a!==null&&wc($a,e),ts!==null&&wc(ts,e),Ko.forEach(n),Qo.forEach(n);for(var a=0;a<es.length;a++){var o=es[a];o.blockedOn===e&&(o.blockedOn=null)}for(;0<es.length&&(a=es[0],a.blockedOn===null);)A_(a),a.blockedOn===null&&es.shift();if(a=(e.ownerDocument||e).$$reactFormReplay,a!=null)for(o=0;o<a.length;o+=3){var u=a[o],h=a[o+1],x=u[Un]||null;if(typeof h=="function")x||C_(a);else if(x){var C=null;if(h&&h.hasAttribute("formAction")){if(u=h,x=h[Un]||null)C=x.formAction;else if(Oh(u)!==null)continue}else C=x.action;typeof C=="function"?a[o+1]=C:(a.splice(o,3),o-=3),C_(a)}}}function w_(){function e(h){h.canIntercept&&h.info==="react-transition"&&h.intercept({handler:function(){return new Promise(function(x){return u=x})},focusReset:"manual",scroll:"manual"})}function n(){u!==null&&(u(),u=null),o||setTimeout(a,20)}function a(){if(!o&&!navigation.transition){var h=navigation.currentEntry;h&&h.url!=null&&navigation.navigate(h.url,{state:h.getState(),info:"react-transition",history:"replace"})}}if(typeof navigation=="object"){var o=!1,u=null;return navigation.addEventListener("navigate",e),navigation.addEventListener("navigatesuccess",n),navigation.addEventListener("navigateerror",n),setTimeout(a,100),function(){o=!0,navigation.removeEventListener("navigate",e),navigation.removeEventListener("navigatesuccess",n),navigation.removeEventListener("navigateerror",n),u!==null&&(u(),u=null)}}}function zh(e){this._internalRoot=e}Uc.prototype.render=zh.prototype.render=function(e){var n=this._internalRoot;if(n===null)throw Error(s(409));var a=n.current,o=fi();y_(a,o,e,n,null,null)},Uc.prototype.unmount=zh.prototype.unmount=function(){var e=this._internalRoot;if(e!==null){this._internalRoot=null;var n=e.containerInfo;y_(e.current,2,null,e,null,null),fc(),n[Yn]=null}};function Uc(e){this._internalRoot=e}Uc.prototype.unstable_scheduleHydration=function(e){if(e){var n=uo();e={blockedOn:null,target:e,priority:n};for(var a=0;a<es.length&&n!==0&&n<es[a].priority;a++);es.splice(a,0,e),a===0&&A_(e)}};var D_=t.version;if(D_!=="19.2.0")throw Error(s(527,D_,"19.2.0"));H.findDOMNode=function(e){var n=e._reactInternals;if(n===void 0)throw typeof e.render=="function"?Error(s(188)):(e=Object.keys(e).join(","),Error(s(268,e)));return e=d(n),e=e!==null?_(e):null,e=e===null?null:e.stateNode,e};var qS={bundleType:0,version:"19.2.0",rendererPackageName:"react-dom",currentDispatcherRef:P,reconcilerVersion:"19.2.0"};if(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__<"u"){var Nc=__REACT_DEVTOOLS_GLOBAL_HOOK__;if(!Nc.isDisabled&&Nc.supportsFiber)try{ft=Nc.inject(qS),dt=Nc}catch{}}return tl.createRoot=function(e,n){if(!l(e))throw Error(s(299));var a=!1,o="",u=B0,h=I0,x=F0;return n!=null&&(n.unstable_strictMode===!0&&(a=!0),n.identifierPrefix!==void 0&&(o=n.identifierPrefix),n.onUncaughtError!==void 0&&(u=n.onUncaughtError),n.onCaughtError!==void 0&&(h=n.onCaughtError),n.onRecoverableError!==void 0&&(x=n.onRecoverableError)),n=v_(e,1,!1,null,null,a,o,null,u,h,x,w_),e[Yn]=n.current,_h(e),new zh(n)},tl.hydrateRoot=function(e,n,a){if(!l(e))throw Error(s(299));var o=!1,u="",h=B0,x=I0,C=F0,G=null;return a!=null&&(a.unstable_strictMode===!0&&(o=!0),a.identifierPrefix!==void 0&&(u=a.identifierPrefix),a.onUncaughtError!==void 0&&(h=a.onUncaughtError),a.onCaughtError!==void 0&&(x=a.onCaughtError),a.onRecoverableError!==void 0&&(C=a.onRecoverableError),a.formState!==void 0&&(G=a.formState)),n=v_(e,1,!0,n,a??null,o,u,G,h,x,C,w_),n.context=x_(null),a=n.current,o=fi(),o=lo(o),u=Ha(o),u.callback=null,Ga(a,u,o),a=o,n.current.lanes=a,Xt(n,a),Wi(n),e[Yn]=n.current,_h(e),new Uc(n)},tl.version="19.2.0",tl}var H_;function aM(){if(H_)return Fh.exports;H_=1;function r(){if(!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__>"u"||typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE!="function"))try{__REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(r)}catch(t){console.error(t)}}return r(),Fh.exports=iM(),Fh.exports}var sM=aM();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const nx=(...r)=>r.filter((t,i,s)=>!!t&&t.trim()!==""&&s.indexOf(t)===i).join(" ").trim();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const rM=r=>r.replace(/([a-z0-9])([A-Z])/g,"$1-$2").toLowerCase();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const oM=r=>r.replace(/^([A-Z])|[\s-_]+(\w)/g,(t,i,s)=>s?s.toUpperCase():i.toLowerCase());/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const G_=r=>{const t=oM(r);return t.charAt(0).toUpperCase()+t.slice(1)};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */var kh={xmlns:"http://www.w3.org/2000/svg",width:24,height:24,viewBox:"0 0 24 24",fill:"none",stroke:"currentColor",strokeWidth:2,strokeLinecap:"round",strokeLinejoin:"round"};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const lM=r=>{for(const t in r)if(t.startsWith("aria-")||t==="role"||t==="title")return!0;return!1},cM=ce.createContext({}),uM=()=>ce.useContext(cM),fM=ce.forwardRef(({color:r,size:t,strokeWidth:i,absoluteStrokeWidth:s,className:l="",children:c,iconNode:f,...p},m)=>{const{size:d=24,strokeWidth:_=2,absoluteStrokeWidth:v=!1,color:g="currentColor",className:S=""}=uM()??{},E=s??v?Number(i??_)*24/Number(t??d):i??_;return ce.createElement("svg",{ref:m,...kh,width:t??d??kh.width,height:t??d??kh.height,stroke:r??g,strokeWidth:E,className:nx("lucide",S,l),...!c&&!lM(p)&&{"aria-hidden":"true"},...p},[...f.map(([A,M])=>ce.createElement(A,M)),...Array.isArray(c)?c:[c]])});/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const Oe=(r,t)=>{const i=ce.forwardRef(({className:s,...l},c)=>ce.createElement(fM,{ref:c,iconNode:t,className:nx(`lucide-${rM(G_(r))}`,`lucide-${r}`,s),...l}));return i.displayName=G_(r),i};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const hM=[["path",{d:"M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2",key:"169zse"}]],ix=Oe("activity",hM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const dM=[["rect",{width:"20",height:"5",x:"2",y:"3",rx:"1",key:"1wp1u1"}],["path",{d:"M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8",key:"1s80jp"}],["path",{d:"M10 12h4",key:"a56b0p"}]],_u=Oe("archive",dM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const pM=[["path",{d:"m12 19-7-7 7-7",key:"1l729n"}],["path",{d:"M19 12H5",key:"x3x0zl"}]],mM=Oe("arrow-left",pM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const gM=[["path",{d:"M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z",key:"hh9hay"}],["path",{d:"m3.3 7 8.7 5 8.7-5",key:"g66t2b"}],["path",{d:"M12 22V12",key:"d0xqtd"}]],_M=Oe("box",gM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const vM=[["path",{d:"M2.97 12.92A2 2 0 0 0 2 14.63v3.24a2 2 0 0 0 .97 1.71l3 1.8a2 2 0 0 0 2.06 0L12 19v-5.5l-5-3-4.03 2.42Z",key:"lc1i9w"}],["path",{d:"m7 16.5-4.74-2.85",key:"1o9zyk"}],["path",{d:"m7 16.5 5-3",key:"va8pkn"}],["path",{d:"M7 16.5v5.17",key:"jnp8gn"}],["path",{d:"M12 13.5V19l3.97 2.38a2 2 0 0 0 2.06 0l3-1.8a2 2 0 0 0 .97-1.71v-3.24a2 2 0 0 0-.97-1.71L17 10.5l-5 3Z",key:"8zsnat"}],["path",{d:"m17 16.5-5-3",key:"8arw3v"}],["path",{d:"m17 16.5 4.74-2.85",key:"8rfmw"}],["path",{d:"M17 16.5v5.17",key:"k6z78m"}],["path",{d:"M7.97 4.42A2 2 0 0 0 7 6.13v4.37l5 3 5-3V6.13a2 2 0 0 0-.97-1.71l-3-1.8a2 2 0 0 0-2.06 0l-3 1.8Z",key:"1xygjf"}],["path",{d:"M12 8 7.26 5.15",key:"1vbdud"}],["path",{d:"m12 8 4.74-2.85",key:"3rx089"}],["path",{d:"M12 13.5V8",key:"1io7kd"}]],xM=Oe("boxes",vM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const yM=[["path",{d:"M8 2v3",key:"1ioesn"}],["path",{d:"M16 2v3",key:"otl347"}],["rect",{x:"3",y:"3",width:"18",height:"18",rx:"2",key:"h1oib"}],["path",{d:"M3 9h18",key:"1pudct"}],["path",{d:"M8 13h.01",key:"1sbv64"}],["path",{d:"M12 13h.01",key:"y0uutt"}],["path",{d:"M16 13h.01",key:"wip0gl"}],["path",{d:"M8 17h.01",key:"p3bg7i"}],["path",{d:"M12 17h.01",key:"p32p05"}],["path",{d:"M16 17h.01",key:"ql8jdd"}]],SM=Oe("calendar-days",yM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const MM=[["path",{d:"M20 6 9 17l-5-5",key:"1gmf2c"}]],EM=Oe("check",MM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const bM=[["path",{d:"m9 18 6-6-6-6",key:"mthhwq"}]],TM=Oe("chevron-right",bM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const AM=[["circle",{cx:"12",cy:"12",r:"10",key:"1mglay"}],["path",{d:"M12 6v6h4",key:"135r8i"}]],RM=Oe("clock-3",AM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const CM=[["rect",{width:"14",height:"14",x:"8",y:"8",rx:"2",ry:"2",key:"17jyea"}],["path",{d:"M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2",key:"zix9uf"}]],wM=Oe("copy",CM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const DM=[["path",{d:"M7 16.3c2.2 0 4-1.83 4-4.05 0-1.16-.57-2.26-1.71-3.19S7.29 6.75 7 5.3c-.29 1.45-1.14 2.84-2.29 3.76S3 11.1 3 12.25c0 2.22 1.8 4.05 4 4.05z",key:"1ptgy4"}],["path",{d:"M12.56 6.6A10.97 10.97 0 0 0 14 3.02c.5 2.5 2 4.9 4 6.5s3 3.5 3 5.5a6.98 6.98 0 0 1-11.91 4.97",key:"1sl1rz"}]],UM=Oe("droplets",DM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const NM=[["path",{d:"M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z",key:"1oefj6"}],["path",{d:"M14 2v5a1 1 0 0 0 1 1h5",key:"wfsgrz"}],["path",{d:"M10 9H8",key:"b1mrlr"}],["path",{d:"M16 13H8",key:"t4e002"}],["path",{d:"M16 17H8",key:"z1uh3a"}]],LM=Oe("file-text",NM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const OM=[["path",{d:"m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2",key:"usdka0"}]],ax=Oe("folder-open",OM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const PM=[["path",{d:"m12 14 4-4",key:"9kzdfg"}],["path",{d:"M3.34 19a10 10 0 1 1 17.32 0",key:"19p75a"}]],Tp=Oe("gauge",PM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const zM=[["path",{d:"M12 3v18",key:"108xh3"}],["path",{d:"M3 12h18",key:"1i2n21"}],["rect",{x:"3",y:"3",width:"18",height:"18",rx:"2",key:"h1oib"}]],BM=Oe("grid-2x2",zM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const IM=[["path",{d:"M12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83z",key:"zw3jo"}],["path",{d:"M2 12a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 12",key:"1wduqc"}],["path",{d:"M2 17a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 17",key:"kqbvx6"}]],FM=Oe("layers",IM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const HM=[["path",{d:"M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z",key:"nnexq3"}],["path",{d:"M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12",key:"mt58a7"}]],GM=Oe("leaf",HM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const VM=[["line",{x1:"2",x2:"5",y1:"12",y2:"12",key:"bvdh0s"}],["line",{x1:"19",x2:"22",y1:"12",y2:"12",key:"1tbv5k"}],["line",{x1:"12",x2:"12",y1:"2",y2:"5",key:"11lu5j"}],["line",{x1:"12",x2:"12",y1:"19",y2:"22",key:"x3vr5v"}],["circle",{cx:"12",cy:"12",r:"7",key:"fim9np"}],["circle",{cx:"12",cy:"12",r:"3",key:"1v7zrd"}]],kM=Oe("locate-fixed",VM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const XM=[["rect",{width:"18",height:"11",x:"3",y:"11",rx:"2",ry:"2",key:"1w4ew1"}],["path",{d:"M7 11V7a5 5 0 0 1 9.9-1",key:"1mm8w8"}]],WM=Oe("lock-open",XM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const qM=[["path",{d:"M15 3h6v6",key:"1q9fwt"}],["path",{d:"m21 3-7 7",key:"1l2asr"}],["path",{d:"m3 21 7-7",key:"tjx5ai"}],["path",{d:"M9 21H3v-6",key:"wtvkvv"}]],YM=Oe("maximize-2",qM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const ZM=[["rect",{x:"14",y:"3",width:"5",height:"18",rx:"1",key:"kaeet6"}],["rect",{x:"5",y:"3",width:"5",height:"18",rx:"1",key:"1wsw3u"}]],jM=Oe("pause",ZM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const KM=[["path",{d:"M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z",key:"10ikf1"}]],QM=Oe("play",KM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const JM=[["path",{d:"m15.194 13.707 3.814 1.86-1.86 3.814",key:"16shm9"}],["path",{d:"M16.47214 7.52786 A 5 10 0 1 0 13 21.79796",key:"1245p8"}],["path",{d:"M21.79796 11 A 10 5 0 1 0 19 15.57071",key:"1i40ks"}]],$M=Oe("rotate-3d",JM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const t1=[["path",{d:"M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8",key:"1357e3"}],["path",{d:"M3 3v5h5",key:"1xhq8a"}]],e1=Oe("rotate-ccw",t1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const n1=[["path",{d:"m21 21-4.34-4.34",key:"14j7rj"}],["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}]],i1=Oe("search",n1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const a1=[["path",{d:"M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915",key:"1i5ecw"}],["circle",{cx:"12",cy:"12",r:"3",key:"1v7zrd"}]],s1=Oe("settings",a1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const r1=[["path",{d:"M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",key:"oel41y"}],["path",{d:"m9 12 2 2 4-4",key:"dzmm74"}]],o1=Oe("shield-check",r1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const l1=[["path",{d:"M14 4v10.54a4 4 0 1 1-4 0V4a2 2 0 0 1 4 0Z",key:"17jzev"}]],c1=Oe("thermometer",l1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const u1=[["path",{d:"m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",key:"wmoenq"}],["path",{d:"M12 9v4",key:"juzpu7"}],["path",{d:"M12 17h.01",key:"p32p05"}]],sx=Oe("triangle-alert",u1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const f1=[["circle",{cx:"12",cy:"8",r:"5",key:"1hypcn"}],["path",{d:"M20 21a8 8 0 0 0-16 0",key:"rfgkzh"}]],h1=Oe("user-round",f1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const d1=[["path",{d:"M12.8 19.6A2 2 0 1 0 14 16H2",key:"148xed"}],["path",{d:"M17.5 8a2.5 2.5 0 1 1 2 4H2",key:"1u4tom"}],["path",{d:"M9.8 4.4A2 2 0 1 1 11 8H2",key:"75valh"}]],p1=Oe("wind",d1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const m1=[["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}],["line",{x1:"21",x2:"16.65",y1:"21",y2:"16.65",key:"13gj7c"}],["line",{x1:"11",x2:"11",y1:"8",y2:"14",key:"1vmskp"}],["line",{x1:"8",x2:"14",y1:"11",y2:"11",key:"durymu"}]],g1=Oe("zoom-in",m1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const _1=[["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}],["line",{x1:"21",x2:"16.65",y1:"21",y2:"16.65",key:"13gj7c"}],["line",{x1:"8",x2:"14",y1:"11",y2:"11",key:"durymu"}]],v1=Oe("zoom-out",_1);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */const Ap="185",x1=0,V_=1,y1=2,gl=1,S1=2,pl=3,hs=0,ti=1,Ci=2,Ta=0,Qr=1,Ys=2,k_=3,X_=4,M1=5,Vs=100,E1=101,b1=102,T1=103,A1=104,R1=200,C1=201,w1=202,D1=203,wd=204,Dd=205,U1=206,N1=207,L1=208,O1=209,P1=210,z1=211,B1=212,I1=213,F1=214,Ud=0,Nd=1,Ld=2,eo=3,Od=4,Pd=5,zd=6,Bd=7,rx=0,H1=1,G1=2,Ki=0,ox=1,lx=2,cx=3,Rp=4,ux=5,fx=6,hx=7,dx=300,Zs=301,no=302,Xh=303,Wh=304,Cu=306,Id=1e3,ba=1001,Fd=1002,zn=1003,V1=1004,Lc=1005,Gn=1006,qh=1007,Xs=1008,gi=1009,px=1010,mx=1011,vl=1012,Cp=1013,Ji=1014,Ii=1015,Ca=1016,wp=1017,Dp=1018,xl=1020,gx=35902,_x=35899,vx=1021,xx=1022,Fi=1023,wa=1026,Ws=1027,Up=1028,Np=1029,js=1030,Lp=1031,Op=1033,hu=33776,du=33777,pu=33778,mu=33779,Hd=35840,Gd=35841,Vd=35842,kd=35843,Xd=36196,Wd=37492,qd=37496,Yd=37488,Zd=37489,vu=37490,jd=37491,Kd=37808,Qd=37809,Jd=37810,$d=37811,tp=37812,ep=37813,np=37814,ip=37815,ap=37816,sp=37817,rp=37818,op=37819,lp=37820,cp=37821,up=36492,fp=36494,hp=36495,dp=36283,pp=36284,xu=36285,mp=36286,k1=3200,gp=0,X1=1,cs="",qn="srgb",yu="srgb-linear",Su="linear",Ze="srgb",Ur=7680,W_=519,W1=512,q1=513,Y1=514,Pp=515,Z1=516,j1=517,zp=518,K1=519,_p=35044,q_="300 es",ji=2e3,yl=2001;function Q1(r){for(let t=r.length-1;t>=0;--t)if(r[t]>=65535)return!0;return!1}function Mu(r){return document.createElementNS("http://www.w3.org/1999/xhtml",r)}function J1(){const r=Mu("canvas");return r.style.display="block",r}const Y_={};function Eu(...r){const t="THREE."+r.shift();console.log(t,...r)}function yx(r){const t=r[0];if(typeof t=="string"&&t.startsWith("TSL:")){const i=r[1];i&&i.isStackTrace?r[0]+=" "+i.getLocation():r[1]='Stack trace not available. Enable "THREE.Node.captureStackTrace" to capture stack traces.'}return r}function re(...r){r=yx(r);const t="THREE."+r.shift();{const i=r[0];i&&i.isStackTrace?console.warn(i.getError(t)):console.warn(t,...r)}}function Ue(...r){r=yx(r);const t="THREE."+r.shift();{const i=r[0];i&&i.isStackTrace?console.error(i.getError(t)):console.error(t,...r)}}function Jr(...r){const t=r.join(" ");t in Y_||(Y_[t]=!0,re(...r))}function $1(r,t,i){return new Promise(function(s,l){function c(){switch(r.clientWaitSync(t,r.SYNC_FLUSH_COMMANDS_BIT,0)){case r.WAIT_FAILED:l();break;case r.TIMEOUT_EXPIRED:setTimeout(c,i);break;default:s()}}setTimeout(c,i)})}const tE={[Ud]:Nd,[Ld]:zd,[Od]:Bd,[eo]:Pd,[Nd]:Ud,[zd]:Ld,[Bd]:Od,[Pd]:eo};class Ks{addEventListener(t,i){this._listeners===void 0&&(this._listeners={});const s=this._listeners;s[t]===void 0&&(s[t]=[]),s[t].indexOf(i)===-1&&s[t].push(i)}hasEventListener(t,i){const s=this._listeners;return s===void 0?!1:s[t]!==void 0&&s[t].indexOf(i)!==-1}removeEventListener(t,i){const s=this._listeners;if(s===void 0)return;const l=s[t];if(l!==void 0){const c=l.indexOf(i);c!==-1&&l.splice(c,1)}}dispatchEvent(t){const i=this._listeners;if(i===void 0)return;const s=i[t.type];if(s!==void 0){t.target=this;const l=s.slice(0);for(let c=0,f=l.length;c<f;c++)l[c].call(this,t);t.target=null}}}const Fn=["00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f","10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f","20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f","30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f","40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f","50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f","60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f","70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f","80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f","90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f","a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af","b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf","c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf","d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df","e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef","f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"];let Z_=1234567;const $r=Math.PI/180,Sl=180/Math.PI;function Aa(){const r=Math.random()*4294967295|0,t=Math.random()*4294967295|0,i=Math.random()*4294967295|0,s=Math.random()*4294967295|0;return(Fn[r&255]+Fn[r>>8&255]+Fn[r>>16&255]+Fn[r>>24&255]+"-"+Fn[t&255]+Fn[t>>8&255]+"-"+Fn[t>>16&15|64]+Fn[t>>24&255]+"-"+Fn[i&63|128]+Fn[i>>8&255]+"-"+Fn[i>>16&255]+Fn[i>>24&255]+Fn[s&255]+Fn[s>>8&255]+Fn[s>>16&255]+Fn[s>>24&255]).toLowerCase()}function ye(r,t,i){return Math.max(t,Math.min(i,r))}function Bp(r,t){return(r%t+t)%t}function eE(r,t,i,s,l){return s+(r-t)*(l-s)/(i-t)}function nE(r,t,i){return r!==t?(i-r)/(t-r):0}function _l(r,t,i){return(1-i)*r+i*t}function iE(r,t,i,s){return _l(r,t,1-Math.exp(-i*s))}function aE(r,t=1){return t-Math.abs(Bp(r,t*2)-t)}function sE(r,t,i){return r<=t?0:r>=i?1:(r=(r-t)/(i-t),r*r*(3-2*r))}function rE(r,t,i){return r<=t?0:r>=i?1:(r=(r-t)/(i-t),r*r*r*(r*(r*6-15)+10))}function oE(r,t){return r+Math.floor(Math.random()*(t-r+1))}function lE(r,t){return r+Math.random()*(t-r)}function cE(r){return r*(.5-Math.random())}function uE(r){r!==void 0&&(Z_=r);let t=Z_+=1831565813;return t=Math.imul(t^t>>>15,t|1),t^=t+Math.imul(t^t>>>7,t|61),((t^t>>>14)>>>0)/4294967296}function fE(r){return r*$r}function hE(r){return r*Sl}function dE(r){return(r&r-1)===0&&r!==0}function pE(r){return Math.pow(2,Math.ceil(Math.log(r)/Math.LN2))}function mE(r){return Math.pow(2,Math.floor(Math.log(r)/Math.LN2))}function gE(r,t,i,s,l){const c=Math.cos,f=Math.sin,p=c(i/2),m=f(i/2),d=c((t+s)/2),_=f((t+s)/2),v=c((t-s)/2),g=f((t-s)/2),S=c((s-t)/2),E=f((s-t)/2);switch(l){case"XYX":r.set(p*_,m*v,m*g,p*d);break;case"YZY":r.set(m*g,p*_,m*v,p*d);break;case"ZXZ":r.set(m*v,m*g,p*_,p*d);break;case"XZX":r.set(p*_,m*E,m*S,p*d);break;case"YXY":r.set(m*S,p*_,m*E,p*d);break;case"ZYZ":r.set(m*E,m*S,p*_,p*d);break;default:re("MathUtils: .setQuaternionFromProperEuler() encountered an unknown order: "+l)}}function Bi(r,t){switch(t.constructor){case Float32Array:return r;case Uint32Array:return r/4294967295;case Uint16Array:return r/65535;case Uint8Array:return r/255;case Int32Array:return Math.max(r/2147483647,-1);case Int16Array:return Math.max(r/32767,-1);case Int8Array:return Math.max(r/127,-1);default:throw new Error("THREE.MathUtils: Invalid component type.")}}function je(r,t){switch(t.constructor){case Float32Array:return r;case Uint32Array:return Math.round(r*4294967295);case Uint16Array:return Math.round(r*65535);case Uint8Array:return Math.round(r*255);case Int32Array:return Math.round(r*2147483647);case Int16Array:return Math.round(r*32767);case Int8Array:return Math.round(r*127);default:throw new Error("THREE.MathUtils: Invalid component type.")}}const Hs={DEG2RAD:$r,RAD2DEG:Sl,generateUUID:Aa,clamp:ye,euclideanModulo:Bp,mapLinear:eE,inverseLerp:nE,lerp:_l,damp:iE,pingpong:aE,smoothstep:sE,smootherstep:rE,randInt:oE,randFloat:lE,randFloatSpread:cE,seededRandom:uE,degToRad:fE,radToDeg:hE,isPowerOfTwo:dE,ceilPowerOfTwo:pE,floorPowerOfTwo:mE,setQuaternionFromProperEuler:gE,normalize:je,denormalize:Bi},Zp=class Zp{constructor(t=0,i=0){this.x=t,this.y=i}get width(){return this.x}set width(t){this.x=t}get height(){return this.y}set height(t){this.y=t}set(t,i){return this.x=t,this.y=i,this}setScalar(t){return this.x=t,this.y=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;default:throw new Error("THREE.Vector2: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;default:throw new Error("THREE.Vector2: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y)}copy(t){return this.x=t.x,this.y=t.y,this}add(t){return this.x+=t.x,this.y+=t.y,this}addScalar(t){return this.x+=t,this.y+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this}subScalar(t){return this.x-=t,this.y-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this}multiply(t){return this.x*=t.x,this.y*=t.y,this}multiplyScalar(t){return this.x*=t,this.y*=t,this}divide(t){return this.x/=t.x,this.y/=t.y,this}divideScalar(t){return this.multiplyScalar(1/t)}applyMatrix3(t){const i=this.x,s=this.y,l=t.elements;return this.x=l[0]*i+l[3]*s+l[6],this.y=l[1]*i+l[4]*s+l[7],this}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this}clamp(t,i){return this.x=ye(this.x,t.x,i.x),this.y=ye(this.y,t.y,i.y),this}clampScalar(t,i){return this.x=ye(this.x,t,i),this.y=ye(this.y,t,i),this}clampLength(t,i){const s=this.length();return this.divideScalar(s||1).multiplyScalar(ye(s,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this}negate(){return this.x=-this.x,this.y=-this.y,this}dot(t){return this.x*t.x+this.y*t.y}cross(t){return this.x*t.y-this.y*t.x}lengthSq(){return this.x*this.x+this.y*this.y}length(){return Math.sqrt(this.x*this.x+this.y*this.y)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)}normalize(){return this.divideScalar(this.length()||1)}angle(){return Math.atan2(-this.y,-this.x)+Math.PI}angleTo(t){const i=Math.sqrt(this.lengthSq()*t.lengthSq());if(i===0)return Math.PI/2;const s=this.dot(t)/i;return Math.acos(ye(s,-1,1))}distanceTo(t){return Math.sqrt(this.distanceToSquared(t))}distanceToSquared(t){const i=this.x-t.x,s=this.y-t.y;return i*i+s*s}manhattanDistanceTo(t){return Math.abs(this.x-t.x)+Math.abs(this.y-t.y)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this}lerpVectors(t,i,s){return this.x=t.x+(i.x-t.x)*s,this.y=t.y+(i.y-t.y)*s,this}equals(t){return t.x===this.x&&t.y===this.y}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this}rotateAround(t,i){const s=Math.cos(i),l=Math.sin(i),c=this.x-t.x,f=this.y-t.y;return this.x=c*s-f*l+t.x,this.y=c*l+f*s+t.y,this}random(){return this.x=Math.random(),this.y=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y}};Zp.prototype.isVector2=!0;let fe=Zp;class ro{constructor(t=0,i=0,s=0,l=1){this.isQuaternion=!0,this._x=t,this._y=i,this._z=s,this._w=l}static slerpFlat(t,i,s,l,c,f,p){let m=s[l+0],d=s[l+1],_=s[l+2],v=s[l+3],g=c[f+0],S=c[f+1],E=c[f+2],A=c[f+3];if(v!==A||m!==g||d!==S||_!==E){let M=m*g+d*S+_*E+v*A;M<0&&(g=-g,S=-S,E=-E,A=-A,M=-M);let y=1-p;if(M<.9995){const B=Math.acos(M),F=Math.sin(B);y=Math.sin(y*B)/F,p=Math.sin(p*B)/F,m=m*y+g*p,d=d*y+S*p,_=_*y+E*p,v=v*y+A*p}else{m=m*y+g*p,d=d*y+S*p,_=_*y+E*p,v=v*y+A*p;const B=1/Math.sqrt(m*m+d*d+_*_+v*v);m*=B,d*=B,_*=B,v*=B}}t[i]=m,t[i+1]=d,t[i+2]=_,t[i+3]=v}static multiplyQuaternionsFlat(t,i,s,l,c,f){const p=s[l],m=s[l+1],d=s[l+2],_=s[l+3],v=c[f],g=c[f+1],S=c[f+2],E=c[f+3];return t[i]=p*E+_*v+m*S-d*g,t[i+1]=m*E+_*g+d*v-p*S,t[i+2]=d*E+_*S+p*g-m*v,t[i+3]=_*E-p*v-m*g-d*S,t}get x(){return this._x}set x(t){this._x=t,this._onChangeCallback()}get y(){return this._y}set y(t){this._y=t,this._onChangeCallback()}get z(){return this._z}set z(t){this._z=t,this._onChangeCallback()}get w(){return this._w}set w(t){this._w=t,this._onChangeCallback()}set(t,i,s,l){return this._x=t,this._y=i,this._z=s,this._w=l,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._w)}copy(t){return this._x=t.x,this._y=t.y,this._z=t.z,this._w=t.w,this._onChangeCallback(),this}setFromEuler(t,i=!0){const s=t._x,l=t._y,c=t._z,f=t._order,p=Math.cos,m=Math.sin,d=p(s/2),_=p(l/2),v=p(c/2),g=m(s/2),S=m(l/2),E=m(c/2);switch(f){case"XYZ":this._x=g*_*v+d*S*E,this._y=d*S*v-g*_*E,this._z=d*_*E+g*S*v,this._w=d*_*v-g*S*E;break;case"YXZ":this._x=g*_*v+d*S*E,this._y=d*S*v-g*_*E,this._z=d*_*E-g*S*v,this._w=d*_*v+g*S*E;break;case"ZXY":this._x=g*_*v-d*S*E,this._y=d*S*v+g*_*E,this._z=d*_*E+g*S*v,this._w=d*_*v-g*S*E;break;case"ZYX":this._x=g*_*v-d*S*E,this._y=d*S*v+g*_*E,this._z=d*_*E-g*S*v,this._w=d*_*v+g*S*E;break;case"YZX":this._x=g*_*v+d*S*E,this._y=d*S*v+g*_*E,this._z=d*_*E-g*S*v,this._w=d*_*v-g*S*E;break;case"XZY":this._x=g*_*v-d*S*E,this._y=d*S*v-g*_*E,this._z=d*_*E+g*S*v,this._w=d*_*v+g*S*E;break;default:re("Quaternion: .setFromEuler() encountered an unknown order: "+f)}return i===!0&&this._onChangeCallback(),this}setFromAxisAngle(t,i){const s=i/2,l=Math.sin(s);return this._x=t.x*l,this._y=t.y*l,this._z=t.z*l,this._w=Math.cos(s),this._onChangeCallback(),this}setFromRotationMatrix(t){const i=t.elements,s=i[0],l=i[4],c=i[8],f=i[1],p=i[5],m=i[9],d=i[2],_=i[6],v=i[10],g=s+p+v;if(g>0){const S=.5/Math.sqrt(g+1);this._w=.25/S,this._x=(_-m)*S,this._y=(c-d)*S,this._z=(f-l)*S}else if(s>p&&s>v){const S=2*Math.sqrt(1+s-p-v);this._w=(_-m)/S,this._x=.25*S,this._y=(l+f)/S,this._z=(c+d)/S}else if(p>v){const S=2*Math.sqrt(1+p-s-v);this._w=(c-d)/S,this._x=(l+f)/S,this._y=.25*S,this._z=(m+_)/S}else{const S=2*Math.sqrt(1+v-s-p);this._w=(f-l)/S,this._x=(c+d)/S,this._y=(m+_)/S,this._z=.25*S}return this._onChangeCallback(),this}setFromUnitVectors(t,i){let s=t.dot(i)+1;return s<1e-8?(s=0,Math.abs(t.x)>Math.abs(t.z)?(this._x=-t.y,this._y=t.x,this._z=0,this._w=s):(this._x=0,this._y=-t.z,this._z=t.y,this._w=s)):(this._x=t.y*i.z-t.z*i.y,this._y=t.z*i.x-t.x*i.z,this._z=t.x*i.y-t.y*i.x,this._w=s),this.normalize()}angleTo(t){return 2*Math.acos(Math.abs(ye(this.dot(t),-1,1)))}rotateTowards(t,i){const s=this.angleTo(t);if(s===0)return this;const l=Math.min(1,i/s);return this.slerp(t,l),this}identity(){return this.set(0,0,0,1)}invert(){return this.conjugate()}conjugate(){return this._x*=-1,this._y*=-1,this._z*=-1,this._onChangeCallback(),this}dot(t){return this._x*t._x+this._y*t._y+this._z*t._z+this._w*t._w}lengthSq(){return this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w}length(){return Math.sqrt(this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w)}normalize(){let t=this.length();return t===0?(this._x=0,this._y=0,this._z=0,this._w=1):(t=1/t,this._x=this._x*t,this._y=this._y*t,this._z=this._z*t,this._w=this._w*t),this._onChangeCallback(),this}multiply(t){return this.multiplyQuaternions(this,t)}premultiply(t){return this.multiplyQuaternions(t,this)}multiplyQuaternions(t,i){const s=t._x,l=t._y,c=t._z,f=t._w,p=i._x,m=i._y,d=i._z,_=i._w;return this._x=s*_+f*p+l*d-c*m,this._y=l*_+f*m+c*p-s*d,this._z=c*_+f*d+s*m-l*p,this._w=f*_-s*p-l*m-c*d,this._onChangeCallback(),this}slerp(t,i){let s=t._x,l=t._y,c=t._z,f=t._w,p=this.dot(t);p<0&&(s=-s,l=-l,c=-c,f=-f,p=-p);let m=1-i;if(p<.9995){const d=Math.acos(p),_=Math.sin(d);m=Math.sin(m*d)/_,i=Math.sin(i*d)/_,this._x=this._x*m+s*i,this._y=this._y*m+l*i,this._z=this._z*m+c*i,this._w=this._w*m+f*i,this._onChangeCallback()}else this._x=this._x*m+s*i,this._y=this._y*m+l*i,this._z=this._z*m+c*i,this._w=this._w*m+f*i,this.normalize();return this}slerpQuaternions(t,i,s){return this.copy(t).slerp(i,s)}random(){const t=2*Math.PI*Math.random(),i=2*Math.PI*Math.random(),s=Math.random(),l=Math.sqrt(1-s),c=Math.sqrt(s);return this.set(l*Math.sin(t),l*Math.cos(t),c*Math.sin(i),c*Math.cos(i))}equals(t){return t._x===this._x&&t._y===this._y&&t._z===this._z&&t._w===this._w}fromArray(t,i=0){return this._x=t[i],this._y=t[i+1],this._z=t[i+2],this._w=t[i+3],this._onChangeCallback(),this}toArray(t=[],i=0){return t[i]=this._x,t[i+1]=this._y,t[i+2]=this._z,t[i+3]=this._w,t}fromBufferAttribute(t,i){return this._x=t.getX(i),this._y=t.getY(i),this._z=t.getZ(i),this._w=t.getW(i),this._onChangeCallback(),this}toJSON(){return this.toArray()}_onChange(t){return this._onChangeCallback=t,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._w}}const jp=class jp{constructor(t=0,i=0,s=0){this.x=t,this.y=i,this.z=s}set(t,i,s){return s===void 0&&(s=this.z),this.x=t,this.y=i,this.z=s,this}setScalar(t){return this.x=t,this.y=t,this.z=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setZ(t){return this.z=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;case 2:this.z=i;break;default:throw new Error("THREE.Vector3: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:throw new Error("THREE.Vector3: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y,this.z)}copy(t){return this.x=t.x,this.y=t.y,this.z=t.z,this}add(t){return this.x+=t.x,this.y+=t.y,this.z+=t.z,this}addScalar(t){return this.x+=t,this.y+=t,this.z+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this.z=t.z+i.z,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this.z+=t.z*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this.z-=t.z,this}subScalar(t){return this.x-=t,this.y-=t,this.z-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this.z=t.z-i.z,this}multiply(t){return this.x*=t.x,this.y*=t.y,this.z*=t.z,this}multiplyScalar(t){return this.x*=t,this.y*=t,this.z*=t,this}multiplyVectors(t,i){return this.x=t.x*i.x,this.y=t.y*i.y,this.z=t.z*i.z,this}applyEuler(t){return this.applyQuaternion(j_.setFromEuler(t))}applyAxisAngle(t,i){return this.applyQuaternion(j_.setFromAxisAngle(t,i))}applyMatrix3(t){const i=this.x,s=this.y,l=this.z,c=t.elements;return this.x=c[0]*i+c[3]*s+c[6]*l,this.y=c[1]*i+c[4]*s+c[7]*l,this.z=c[2]*i+c[5]*s+c[8]*l,this}applyNormalMatrix(t){return this.applyMatrix3(t).normalize()}applyMatrix4(t){const i=this.x,s=this.y,l=this.z,c=t.elements,f=1/(c[3]*i+c[7]*s+c[11]*l+c[15]);return this.x=(c[0]*i+c[4]*s+c[8]*l+c[12])*f,this.y=(c[1]*i+c[5]*s+c[9]*l+c[13])*f,this.z=(c[2]*i+c[6]*s+c[10]*l+c[14])*f,this}applyQuaternion(t){const i=this.x,s=this.y,l=this.z,c=t.x,f=t.y,p=t.z,m=t.w,d=2*(f*l-p*s),_=2*(p*i-c*l),v=2*(c*s-f*i);return this.x=i+m*d+f*v-p*_,this.y=s+m*_+p*d-c*v,this.z=l+m*v+c*_-f*d,this}project(t){return this.applyMatrix4(t.matrixWorldInverse).applyMatrix4(t.projectionMatrix)}unproject(t){return this.applyMatrix4(t.projectionMatrixInverse).applyMatrix4(t.matrixWorld)}transformDirection(t){const i=this.x,s=this.y,l=this.z,c=t.elements;return this.x=c[0]*i+c[4]*s+c[8]*l,this.y=c[1]*i+c[5]*s+c[9]*l,this.z=c[2]*i+c[6]*s+c[10]*l,this.normalize()}divide(t){return this.x/=t.x,this.y/=t.y,this.z/=t.z,this}divideScalar(t){return this.multiplyScalar(1/t)}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this.z=Math.min(this.z,t.z),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this.z=Math.max(this.z,t.z),this}clamp(t,i){return this.x=ye(this.x,t.x,i.x),this.y=ye(this.y,t.y,i.y),this.z=ye(this.z,t.z,i.z),this}clampScalar(t,i){return this.x=ye(this.x,t,i),this.y=ye(this.y,t,i),this.z=ye(this.z,t,i),this}clampLength(t,i){const s=this.length();return this.divideScalar(s||1).multiplyScalar(ye(s,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this}dot(t){return this.x*t.x+this.y*t.y+this.z*t.z}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)}normalize(){return this.divideScalar(this.length()||1)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this.z+=(t.z-this.z)*i,this}lerpVectors(t,i,s){return this.x=t.x+(i.x-t.x)*s,this.y=t.y+(i.y-t.y)*s,this.z=t.z+(i.z-t.z)*s,this}cross(t){return this.crossVectors(this,t)}crossVectors(t,i){const s=t.x,l=t.y,c=t.z,f=i.x,p=i.y,m=i.z;return this.x=l*m-c*p,this.y=c*f-s*m,this.z=s*p-l*f,this}projectOnVector(t){const i=t.lengthSq();if(i===0)return this.set(0,0,0);const s=t.dot(this)/i;return this.copy(t).multiplyScalar(s)}projectOnPlane(t){return Yh.copy(this).projectOnVector(t),this.sub(Yh)}reflect(t){return this.sub(Yh.copy(t).multiplyScalar(2*this.dot(t)))}angleTo(t){const i=Math.sqrt(this.lengthSq()*t.lengthSq());if(i===0)return Math.PI/2;const s=this.dot(t)/i;return Math.acos(ye(s,-1,1))}distanceTo(t){return Math.sqrt(this.distanceToSquared(t))}distanceToSquared(t){const i=this.x-t.x,s=this.y-t.y,l=this.z-t.z;return i*i+s*s+l*l}manhattanDistanceTo(t){return Math.abs(this.x-t.x)+Math.abs(this.y-t.y)+Math.abs(this.z-t.z)}setFromSpherical(t){return this.setFromSphericalCoords(t.radius,t.phi,t.theta)}setFromSphericalCoords(t,i,s){const l=Math.sin(i)*t;return this.x=l*Math.sin(s),this.y=Math.cos(i)*t,this.z=l*Math.cos(s),this}setFromCylindrical(t){return this.setFromCylindricalCoords(t.radius,t.theta,t.y)}setFromCylindricalCoords(t,i,s){return this.x=t*Math.sin(i),this.y=s,this.z=t*Math.cos(i),this}setFromMatrixPosition(t){const i=t.elements;return this.x=i[12],this.y=i[13],this.z=i[14],this}setFromMatrixScale(t){const i=this.setFromMatrixColumn(t,0).length(),s=this.setFromMatrixColumn(t,1).length(),l=this.setFromMatrixColumn(t,2).length();return this.x=i,this.y=s,this.z=l,this}setFromMatrixColumn(t,i){return this.fromArray(t.elements,i*4)}setFromMatrix3Column(t,i){return this.fromArray(t.elements,i*3)}setFromEuler(t){return this.x=t._x,this.y=t._y,this.z=t._z,this}setFromColor(t){return this.x=t.r,this.y=t.g,this.z=t.b,this}equals(t){return t.x===this.x&&t.y===this.y&&t.z===this.z}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this.z=t[i+2],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t[i+2]=this.z,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this.z=t.getZ(i),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this}randomDirection(){const t=Math.random()*Math.PI*2,i=Math.random()*2-1,s=Math.sqrt(1-i*i);return this.x=s*Math.cos(t),this.y=i,this.z=s*Math.sin(t),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z}};jp.prototype.isVector3=!0;let V=jp;const Yh=new V,j_=new ro,Kp=class Kp{constructor(t,i,s,l,c,f,p,m,d){this.elements=[1,0,0,0,1,0,0,0,1],t!==void 0&&this.set(t,i,s,l,c,f,p,m,d)}set(t,i,s,l,c,f,p,m,d){const _=this.elements;return _[0]=t,_[1]=l,_[2]=p,_[3]=i,_[4]=c,_[5]=m,_[6]=s,_[7]=f,_[8]=d,this}identity(){return this.set(1,0,0,0,1,0,0,0,1),this}copy(t){const i=this.elements,s=t.elements;return i[0]=s[0],i[1]=s[1],i[2]=s[2],i[3]=s[3],i[4]=s[4],i[5]=s[5],i[6]=s[6],i[7]=s[7],i[8]=s[8],this}extractBasis(t,i,s){return t.setFromMatrix3Column(this,0),i.setFromMatrix3Column(this,1),s.setFromMatrix3Column(this,2),this}setFromMatrix4(t){const i=t.elements;return this.set(i[0],i[4],i[8],i[1],i[5],i[9],i[2],i[6],i[10]),this}multiply(t){return this.multiplyMatrices(this,t)}premultiply(t){return this.multiplyMatrices(t,this)}multiplyMatrices(t,i){const s=t.elements,l=i.elements,c=this.elements,f=s[0],p=s[3],m=s[6],d=s[1],_=s[4],v=s[7],g=s[2],S=s[5],E=s[8],A=l[0],M=l[3],y=l[6],B=l[1],F=l[4],w=l[7],I=l[2],O=l[5],D=l[8];return c[0]=f*A+p*B+m*I,c[3]=f*M+p*F+m*O,c[6]=f*y+p*w+m*D,c[1]=d*A+_*B+v*I,c[4]=d*M+_*F+v*O,c[7]=d*y+_*w+v*D,c[2]=g*A+S*B+E*I,c[5]=g*M+S*F+E*O,c[8]=g*y+S*w+E*D,this}multiplyScalar(t){const i=this.elements;return i[0]*=t,i[3]*=t,i[6]*=t,i[1]*=t,i[4]*=t,i[7]*=t,i[2]*=t,i[5]*=t,i[8]*=t,this}determinant(){const t=this.elements,i=t[0],s=t[1],l=t[2],c=t[3],f=t[4],p=t[5],m=t[6],d=t[7],_=t[8];return i*f*_-i*p*d-s*c*_+s*p*m+l*c*d-l*f*m}invert(){const t=this.elements,i=t[0],s=t[1],l=t[2],c=t[3],f=t[4],p=t[5],m=t[6],d=t[7],_=t[8],v=_*f-p*d,g=p*m-_*c,S=d*c-f*m,E=i*v+s*g+l*S;if(E===0)return this.set(0,0,0,0,0,0,0,0,0);const A=1/E;return t[0]=v*A,t[1]=(l*d-_*s)*A,t[2]=(p*s-l*f)*A,t[3]=g*A,t[4]=(_*i-l*m)*A,t[5]=(l*c-p*i)*A,t[6]=S*A,t[7]=(s*m-d*i)*A,t[8]=(f*i-s*c)*A,this}transpose(){let t;const i=this.elements;return t=i[1],i[1]=i[3],i[3]=t,t=i[2],i[2]=i[6],i[6]=t,t=i[5],i[5]=i[7],i[7]=t,this}getNormalMatrix(t){return this.setFromMatrix4(t).invert().transpose()}transposeIntoArray(t){const i=this.elements;return t[0]=i[0],t[1]=i[3],t[2]=i[6],t[3]=i[1],t[4]=i[4],t[5]=i[7],t[6]=i[2],t[7]=i[5],t[8]=i[8],this}setUvTransform(t,i,s,l,c,f,p){const m=Math.cos(c),d=Math.sin(c);return this.set(s*m,s*d,-s*(m*f+d*p)+f+t,-l*d,l*m,-l*(-d*f+m*p)+p+i,0,0,1),this}scale(t,i){return Jr("Matrix3: .scale() is deprecated. Use .makeScale() instead."),this.premultiply(Zh.makeScale(t,i)),this}rotate(t){return Jr("Matrix3: .rotate() is deprecated. Use .makeRotation() instead."),this.premultiply(Zh.makeRotation(-t)),this}translate(t,i){return Jr("Matrix3: .translate() is deprecated. Use .makeTranslation() instead."),this.premultiply(Zh.makeTranslation(t,i)),this}makeTranslation(t,i){return t.isVector2?this.set(1,0,t.x,0,1,t.y,0,0,1):this.set(1,0,t,0,1,i,0,0,1),this}makeRotation(t){const i=Math.cos(t),s=Math.sin(t);return this.set(i,-s,0,s,i,0,0,0,1),this}makeScale(t,i){return this.set(t,0,0,0,i,0,0,0,1),this}equals(t){const i=this.elements,s=t.elements;for(let l=0;l<9;l++)if(i[l]!==s[l])return!1;return!0}fromArray(t,i=0){for(let s=0;s<9;s++)this.elements[s]=t[s+i];return this}toArray(t=[],i=0){const s=this.elements;return t[i]=s[0],t[i+1]=s[1],t[i+2]=s[2],t[i+3]=s[3],t[i+4]=s[4],t[i+5]=s[5],t[i+6]=s[6],t[i+7]=s[7],t[i+8]=s[8],t}clone(){return new this.constructor().fromArray(this.elements)}};Kp.prototype.isMatrix3=!0;let de=Kp;const Zh=new de,K_=new de().set(.4123908,.3575843,.1804808,.212639,.7151687,.0721923,.0193308,.1191948,.9505322),Q_=new de().set(3.2409699,-1.5373832,-.4986108,-.9692436,1.8759675,.0415551,.0556301,-.203977,1.0569715);function _E(){const r={enabled:!0,workingColorSpace:yu,spaces:{},convert:function(l,c,f){return this.enabled===!1||c===f||!c||!f||(this.spaces[c].transfer===Ze&&(l.r=Ra(l.r),l.g=Ra(l.g),l.b=Ra(l.b)),this.spaces[c].primaries!==this.spaces[f].primaries&&(l.applyMatrix3(this.spaces[c].toXYZ),l.applyMatrix3(this.spaces[f].fromXYZ)),this.spaces[f].transfer===Ze&&(l.r=to(l.r),l.g=to(l.g),l.b=to(l.b))),l},workingToColorSpace:function(l,c){return this.convert(l,this.workingColorSpace,c)},colorSpaceToWorking:function(l,c){return this.convert(l,c,this.workingColorSpace)},getPrimaries:function(l){return this.spaces[l].primaries},getTransfer:function(l){return l===cs?Su:this.spaces[l].transfer},getToneMappingMode:function(l){return this.spaces[l].outputColorSpaceConfig.toneMappingMode||"standard"},getLuminanceCoefficients:function(l,c=this.workingColorSpace){return l.fromArray(this.spaces[c].luminanceCoefficients)},define:function(l){Object.assign(this.spaces,l)},_getMatrix:function(l,c,f){return l.copy(this.spaces[c].toXYZ).multiply(this.spaces[f].fromXYZ)},_getDrawingBufferColorSpace:function(l){return this.spaces[l].outputColorSpaceConfig.drawingBufferColorSpace},_getUnpackColorSpace:function(l=this.workingColorSpace){return this.spaces[l].workingColorSpaceConfig.unpackColorSpace},fromWorkingColorSpace:function(l,c){return Jr("ColorManagement: .fromWorkingColorSpace() has been renamed to .workingToColorSpace()."),r.workingToColorSpace(l,c)},toWorkingColorSpace:function(l,c){return Jr("ColorManagement: .toWorkingColorSpace() has been renamed to .colorSpaceToWorking()."),r.colorSpaceToWorking(l,c)}},t=[.64,.33,.3,.6,.15,.06],i=[.2126,.7152,.0722],s=[.3127,.329];return r.define({[yu]:{primaries:t,whitePoint:s,transfer:Su,toXYZ:K_,fromXYZ:Q_,luminanceCoefficients:i,workingColorSpaceConfig:{unpackColorSpace:qn},outputColorSpaceConfig:{drawingBufferColorSpace:qn}},[qn]:{primaries:t,whitePoint:s,transfer:Ze,toXYZ:K_,fromXYZ:Q_,luminanceCoefficients:i,outputColorSpaceConfig:{drawingBufferColorSpace:qn}}}),r}const Ne=_E();function Ra(r){return r<.04045?r*.0773993808:Math.pow(r*.9478672986+.0521327014,2.4)}function to(r){return r<.0031308?r*12.92:1.055*Math.pow(r,.41666)-.055}let Nr;class vE{static getDataURL(t,i="image/png"){if(/^data:/i.test(t.src)||typeof HTMLCanvasElement>"u")return t.src;let s;if(t instanceof HTMLCanvasElement)s=t;else{Nr===void 0&&(Nr=Mu("canvas")),Nr.width=t.width,Nr.height=t.height;const l=Nr.getContext("2d");t instanceof ImageData?l.putImageData(t,0,0):l.drawImage(t,0,0,t.width,t.height),s=Nr}return s.toDataURL(i)}static sRGBToLinear(t){if(typeof HTMLImageElement<"u"&&t instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&t instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&t instanceof ImageBitmap){const i=Mu("canvas");i.width=t.width,i.height=t.height;const s=i.getContext("2d");s.drawImage(t,0,0,t.width,t.height);const l=s.getImageData(0,0,t.width,t.height),c=l.data;for(let f=0;f<c.length;f++)c[f]=Ra(c[f]/255)*255;return s.putImageData(l,0,0),i}else if(t.data){const i=t.data.slice(0);for(let s=0;s<i.length;s++)i instanceof Uint8Array||i instanceof Uint8ClampedArray?i[s]=Math.floor(Ra(i[s]/255)*255):i[s]=Ra(i[s]);return{data:i,width:t.width,height:t.height}}else return re("ImageUtils.sRGBToLinear(): Unsupported image type. No color space conversion applied."),t}}let xE=0;class Ip{constructor(t=null){this.isSource=!0,Object.defineProperty(this,"id",{value:xE++}),this.uuid=Aa(),this.data=t,this.dataReady=!0,this.version=0}getSize(t){const i=this.data;return typeof HTMLVideoElement<"u"&&i instanceof HTMLVideoElement?t.set(i.videoWidth,i.videoHeight,0):typeof VideoFrame<"u"&&i instanceof VideoFrame?t.set(i.displayWidth,i.displayHeight,0):i!==null?t.set(i.width,i.height,i.depth||0):t.set(0,0,0),t}set needsUpdate(t){t===!0&&this.version++}toJSON(t){const i=t===void 0||typeof t=="string";if(!i&&t.images[this.uuid]!==void 0)return t.images[this.uuid];const s={uuid:this.uuid,url:""},l=this.data;if(l!==null){let c;if(Array.isArray(l)){c=[];for(let f=0,p=l.length;f<p;f++)l[f].isDataTexture?c.push(jh(l[f].image)):c.push(jh(l[f]))}else c=jh(l);s.url=c}return i||(t.images[this.uuid]=s),s}}function jh(r){return typeof HTMLImageElement<"u"&&r instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&r instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&r instanceof ImageBitmap?vE.getDataURL(r):r.data?{data:Array.from(r.data),width:r.width,height:r.height,type:r.data.constructor.name}:(re("Texture: Unable to serialize Texture."),{})}let yE=0;const Kh=new V;class Vn extends Ks{constructor(t=Vn.DEFAULT_IMAGE,i=Vn.DEFAULT_MAPPING,s=ba,l=ba,c=Gn,f=Xs,p=Fi,m=gi,d=Vn.DEFAULT_ANISOTROPY,_=cs){super(),this.isTexture=!0,Object.defineProperty(this,"id",{value:yE++}),this.uuid=Aa(),this.name="",this.source=new Ip(t),this.mipmaps=[],this.mapping=i,this.channel=0,this.wrapS=s,this.wrapT=l,this.magFilter=c,this.minFilter=f,this.anisotropy=d,this.format=p,this.internalFormat=null,this.type=m,this.offset=new fe(0,0),this.repeat=new fe(1,1),this.center=new fe(0,0),this.rotation=0,this.matrixAutoUpdate=!0,this.matrix=new de,this.generateMipmaps=!0,this.premultiplyAlpha=!1,this.flipY=!0,this.unpackAlignment=4,this.colorSpace=_,this.userData={},this.updateRanges=[],this.version=0,this.onUpdate=null,this.renderTarget=null,this.isRenderTargetTexture=!1,this.isArrayTexture=!!(t&&t.depth&&t.depth>1),this.pmremVersion=0,this.normalized=!1}get width(){return this.source.getSize(Kh).x}get height(){return this.source.getSize(Kh).y}get depth(){return this.source.getSize(Kh).z}get image(){return this.source.data}set image(t){this.source.data=t}updateMatrix(){this.matrix.setUvTransform(this.offset.x,this.offset.y,this.repeat.x,this.repeat.y,this.rotation,this.center.x,this.center.y)}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}clone(){return new this.constructor().copy(this)}copy(t){return this.name=t.name,this.source=t.source,this.mipmaps=t.mipmaps.slice(0),this.mapping=t.mapping,this.channel=t.channel,this.wrapS=t.wrapS,this.wrapT=t.wrapT,this.magFilter=t.magFilter,this.minFilter=t.minFilter,this.anisotropy=t.anisotropy,this.format=t.format,this.internalFormat=t.internalFormat,this.type=t.type,this.normalized=t.normalized,this.offset.copy(t.offset),this.repeat.copy(t.repeat),this.center.copy(t.center),this.rotation=t.rotation,this.matrixAutoUpdate=t.matrixAutoUpdate,this.matrix.copy(t.matrix),this.generateMipmaps=t.generateMipmaps,this.premultiplyAlpha=t.premultiplyAlpha,this.flipY=t.flipY,this.unpackAlignment=t.unpackAlignment,this.colorSpace=t.colorSpace,this.renderTarget=t.renderTarget,this.isRenderTargetTexture=t.isRenderTargetTexture,this.isArrayTexture=t.isArrayTexture,this.userData=JSON.parse(JSON.stringify(t.userData)),this.needsUpdate=!0,this}setValues(t){for(const i in t){const s=t[i];if(s===void 0){re(`Texture.setValues(): parameter '${i}' has value of undefined.`);continue}const l=this[i];if(l===void 0){re(`Texture.setValues(): property '${i}' does not exist.`);continue}l&&s&&l.isVector2&&s.isVector2||l&&s&&l.isVector3&&s.isVector3||l&&s&&l.isMatrix3&&s.isMatrix3?l.copy(s):this[i]=s}}toJSON(t){const i=t===void 0||typeof t=="string";if(!i&&t.textures[this.uuid]!==void 0)return t.textures[this.uuid];const s={metadata:{version:4.7,type:"Texture",generator:"Texture.toJSON"},uuid:this.uuid,name:this.name,image:this.source.toJSON(t).uuid,mapping:this.mapping,channel:this.channel,repeat:[this.repeat.x,this.repeat.y],offset:[this.offset.x,this.offset.y],center:[this.center.x,this.center.y],rotation:this.rotation,wrap:[this.wrapS,this.wrapT],format:this.format,internalFormat:this.internalFormat,type:this.type,normalized:this.normalized,colorSpace:this.colorSpace,minFilter:this.minFilter,magFilter:this.magFilter,anisotropy:this.anisotropy,flipY:this.flipY,generateMipmaps:this.generateMipmaps,premultiplyAlpha:this.premultiplyAlpha,unpackAlignment:this.unpackAlignment};return Object.keys(this.userData).length>0&&(s.userData=this.userData),i||(t.textures[this.uuid]=s),s}dispose(){this.dispatchEvent({type:"dispose"})}transformUv(t){if(this.mapping!==dx)return t;if(t.applyMatrix3(this.matrix),t.x<0||t.x>1)switch(this.wrapS){case Id:t.x=t.x-Math.floor(t.x);break;case ba:t.x=t.x<0?0:1;break;case Fd:Math.abs(Math.floor(t.x)%2)===1?t.x=Math.ceil(t.x)-t.x:t.x=t.x-Math.floor(t.x);break}if(t.y<0||t.y>1)switch(this.wrapT){case Id:t.y=t.y-Math.floor(t.y);break;case ba:t.y=t.y<0?0:1;break;case Fd:Math.abs(Math.floor(t.y)%2)===1?t.y=Math.ceil(t.y)-t.y:t.y=t.y-Math.floor(t.y);break}return this.flipY&&(t.y=1-t.y),t}set needsUpdate(t){t===!0&&(this.version++,this.source.needsUpdate=!0)}set needsPMREMUpdate(t){t===!0&&this.pmremVersion++}}Vn.DEFAULT_IMAGE=null;Vn.DEFAULT_MAPPING=dx;Vn.DEFAULT_ANISOTROPY=1;const Qp=class Qp{constructor(t=0,i=0,s=0,l=1){this.x=t,this.y=i,this.z=s,this.w=l}get width(){return this.z}set width(t){this.z=t}get height(){return this.w}set height(t){this.w=t}set(t,i,s,l){return this.x=t,this.y=i,this.z=s,this.w=l,this}setScalar(t){return this.x=t,this.y=t,this.z=t,this.w=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setZ(t){return this.z=t,this}setW(t){return this.w=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;case 2:this.z=i;break;case 3:this.w=i;break;default:throw new Error("THREE.Vector4: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;case 2:return this.z;case 3:return this.w;default:throw new Error("THREE.Vector4: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y,this.z,this.w)}copy(t){return this.x=t.x,this.y=t.y,this.z=t.z,this.w=t.w!==void 0?t.w:1,this}add(t){return this.x+=t.x,this.y+=t.y,this.z+=t.z,this.w+=t.w,this}addScalar(t){return this.x+=t,this.y+=t,this.z+=t,this.w+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this.z=t.z+i.z,this.w=t.w+i.w,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this.z+=t.z*i,this.w+=t.w*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this.z-=t.z,this.w-=t.w,this}subScalar(t){return this.x-=t,this.y-=t,this.z-=t,this.w-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this.z=t.z-i.z,this.w=t.w-i.w,this}multiply(t){return this.x*=t.x,this.y*=t.y,this.z*=t.z,this.w*=t.w,this}multiplyScalar(t){return this.x*=t,this.y*=t,this.z*=t,this.w*=t,this}applyMatrix4(t){const i=this.x,s=this.y,l=this.z,c=this.w,f=t.elements;return this.x=f[0]*i+f[4]*s+f[8]*l+f[12]*c,this.y=f[1]*i+f[5]*s+f[9]*l+f[13]*c,this.z=f[2]*i+f[6]*s+f[10]*l+f[14]*c,this.w=f[3]*i+f[7]*s+f[11]*l+f[15]*c,this}divide(t){return this.x/=t.x,this.y/=t.y,this.z/=t.z,this.w/=t.w,this}divideScalar(t){return this.multiplyScalar(1/t)}setAxisAngleFromQuaternion(t){this.w=2*Math.acos(t.w);const i=Math.sqrt(1-t.w*t.w);return i<1e-4?(this.x=1,this.y=0,this.z=0):(this.x=t.x/i,this.y=t.y/i,this.z=t.z/i),this}setAxisAngleFromRotationMatrix(t){let i,s,l,c;const m=t.elements,d=m[0],_=m[4],v=m[8],g=m[1],S=m[5],E=m[9],A=m[2],M=m[6],y=m[10];if(Math.abs(_-g)<.01&&Math.abs(v-A)<.01&&Math.abs(E-M)<.01){if(Math.abs(_+g)<.1&&Math.abs(v+A)<.1&&Math.abs(E+M)<.1&&Math.abs(d+S+y-3)<.1)return this.set(1,0,0,0),this;i=Math.PI;const F=(d+1)/2,w=(S+1)/2,I=(y+1)/2,O=(_+g)/4,D=(v+A)/4,T=(E+M)/4;return F>w&&F>I?F<.01?(s=0,l=.707106781,c=.707106781):(s=Math.sqrt(F),l=O/s,c=D/s):w>I?w<.01?(s=.707106781,l=0,c=.707106781):(l=Math.sqrt(w),s=O/l,c=T/l):I<.01?(s=.707106781,l=.707106781,c=0):(c=Math.sqrt(I),s=D/c,l=T/c),this.set(s,l,c,i),this}let B=Math.sqrt((M-E)*(M-E)+(v-A)*(v-A)+(g-_)*(g-_));return Math.abs(B)<.001&&(B=1),this.x=(M-E)/B,this.y=(v-A)/B,this.z=(g-_)/B,this.w=Math.acos((d+S+y-1)/2),this}setFromMatrixPosition(t){const i=t.elements;return this.x=i[12],this.y=i[13],this.z=i[14],this.w=i[15],this}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this.z=Math.min(this.z,t.z),this.w=Math.min(this.w,t.w),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this.z=Math.max(this.z,t.z),this.w=Math.max(this.w,t.w),this}clamp(t,i){return this.x=ye(this.x,t.x,i.x),this.y=ye(this.y,t.y,i.y),this.z=ye(this.z,t.z,i.z),this.w=ye(this.w,t.w,i.w),this}clampScalar(t,i){return this.x=ye(this.x,t,i),this.y=ye(this.y,t,i),this.z=ye(this.z,t,i),this.w=ye(this.w,t,i),this}clampLength(t,i){const s=this.length();return this.divideScalar(s||1).multiplyScalar(ye(s,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this.w=Math.floor(this.w),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this.w=Math.ceil(this.w),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this.w=Math.round(this.w),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this.w=Math.trunc(this.w),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this.w=-this.w,this}dot(t){return this.x*t.x+this.y*t.y+this.z*t.z+this.w*t.w}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)+Math.abs(this.w)}normalize(){return this.divideScalar(this.length()||1)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this.z+=(t.z-this.z)*i,this.w+=(t.w-this.w)*i,this}lerpVectors(t,i,s){return this.x=t.x+(i.x-t.x)*s,this.y=t.y+(i.y-t.y)*s,this.z=t.z+(i.z-t.z)*s,this.w=t.w+(i.w-t.w)*s,this}equals(t){return t.x===this.x&&t.y===this.y&&t.z===this.z&&t.w===this.w}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this.z=t[i+2],this.w=t[i+3],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t[i+2]=this.z,t[i+3]=this.w,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this.z=t.getZ(i),this.w=t.getW(i),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this.w=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z,yield this.w}};Qp.prototype.isVector4=!0;let cn=Qp;class SE extends Ks{constructor(t=1,i=1,s={}){super(),s=Object.assign({generateMipmaps:!1,internalFormat:null,minFilter:Gn,depthBuffer:!0,stencilBuffer:!1,resolveDepthBuffer:!0,resolveStencilBuffer:!0,depthTexture:null,samples:0,count:1,depth:1,multiview:!1,useArrayDepthTexture:!1},s),this.isRenderTarget=!0,this.width=t,this.height=i,this.depth=s.depth,this.scissor=new cn(0,0,t,i),this.scissorTest=!1,this.viewport=new cn(0,0,t,i),this.textures=[];const l={width:t,height:i,depth:s.depth},c=new Vn(l),f=s.count;for(let p=0;p<f;p++)this.textures[p]=c.clone(),this.textures[p].isRenderTargetTexture=!0,this.textures[p].renderTarget=this;this._setTextureOptions(s),this.depthBuffer=s.depthBuffer,this.stencilBuffer=s.stencilBuffer,this.resolveDepthBuffer=s.resolveDepthBuffer,this.resolveStencilBuffer=s.resolveStencilBuffer,this._depthTexture=null,this.depthTexture=s.depthTexture,this.samples=s.samples,this.multiview=s.multiview,this.useArrayDepthTexture=s.useArrayDepthTexture}_setTextureOptions(t={}){const i={minFilter:Gn,generateMipmaps:!1,flipY:!1,internalFormat:null};t.mapping!==void 0&&(i.mapping=t.mapping),t.wrapS!==void 0&&(i.wrapS=t.wrapS),t.wrapT!==void 0&&(i.wrapT=t.wrapT),t.wrapR!==void 0&&(i.wrapR=t.wrapR),t.magFilter!==void 0&&(i.magFilter=t.magFilter),t.minFilter!==void 0&&(i.minFilter=t.minFilter),t.format!==void 0&&(i.format=t.format),t.type!==void 0&&(i.type=t.type),t.anisotropy!==void 0&&(i.anisotropy=t.anisotropy),t.colorSpace!==void 0&&(i.colorSpace=t.colorSpace),t.flipY!==void 0&&(i.flipY=t.flipY),t.generateMipmaps!==void 0&&(i.generateMipmaps=t.generateMipmaps),t.internalFormat!==void 0&&(i.internalFormat=t.internalFormat);for(let s=0;s<this.textures.length;s++)this.textures[s].setValues(i)}get texture(){return this.textures[0]}set texture(t){this.textures[0]=t}set depthTexture(t){this._depthTexture!==null&&(this._depthTexture.renderTarget=null),t!==null&&(t.renderTarget=this),this._depthTexture=t}get depthTexture(){return this._depthTexture}setSize(t,i,s=1){if(this.width!==t||this.height!==i||this.depth!==s){this.width=t,this.height=i,this.depth=s;for(let l=0,c=this.textures.length;l<c;l++)this.textures[l].image.width=t,this.textures[l].image.height=i,this.textures[l].image.depth=s,this.textures[l].isData3DTexture!==!0&&(this.textures[l].isArrayTexture=this.textures[l].image.depth>1);this.dispose()}this.viewport.set(0,0,t,i),this.scissor.set(0,0,t,i)}clone(){return new this.constructor().copy(this)}copy(t){this.width=t.width,this.height=t.height,this.depth=t.depth,this.scissor.copy(t.scissor),this.scissorTest=t.scissorTest,this.viewport.copy(t.viewport),this.textures.length=0;for(let i=0,s=t.textures.length;i<s;i++){this.textures[i]=t.textures[i].clone(),this.textures[i].isRenderTargetTexture=!0,this.textures[i].renderTarget=this;const l=Object.assign({},t.textures[i].image);this.textures[i].source=new Ip(l)}return this.depthBuffer=t.depthBuffer,this.stencilBuffer=t.stencilBuffer,this.resolveDepthBuffer=t.resolveDepthBuffer,this.resolveStencilBuffer=t.resolveStencilBuffer,t.depthTexture!==null&&(this.depthTexture=t.depthTexture.clone()),this.samples=t.samples,this.multiview=t.multiview,this.useArrayDepthTexture=t.useArrayDepthTexture,this}dispose(){this.dispatchEvent({type:"dispose"})}}class Qi extends SE{constructor(t=1,i=1,s={}){super(t,i,s),this.isWebGLRenderTarget=!0}}class Sx extends Vn{constructor(t=null,i=1,s=1,l=1){super(null),this.isDataArrayTexture=!0,this.image={data:t,width:i,height:s,depth:l},this.magFilter=zn,this.minFilter=zn,this.wrapR=ba,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1,this.layerUpdates=new Set}addLayerUpdate(t){this.layerUpdates.add(t)}clearLayerUpdates(){this.layerUpdates.clear()}}class ME extends Vn{constructor(t=null,i=1,s=1,l=1){super(null),this.isData3DTexture=!0,this.image={data:t,width:i,height:s,depth:l},this.magFilter=zn,this.minFilter=zn,this.wrapR=ba,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}const Ru=class Ru{constructor(t,i,s,l,c,f,p,m,d,_,v,g,S,E,A,M){this.elements=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],t!==void 0&&this.set(t,i,s,l,c,f,p,m,d,_,v,g,S,E,A,M)}set(t,i,s,l,c,f,p,m,d,_,v,g,S,E,A,M){const y=this.elements;return y[0]=t,y[4]=i,y[8]=s,y[12]=l,y[1]=c,y[5]=f,y[9]=p,y[13]=m,y[2]=d,y[6]=_,y[10]=v,y[14]=g,y[3]=S,y[7]=E,y[11]=A,y[15]=M,this}identity(){return this.set(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1),this}clone(){return new Ru().fromArray(this.elements)}copy(t){const i=this.elements,s=t.elements;return i[0]=s[0],i[1]=s[1],i[2]=s[2],i[3]=s[3],i[4]=s[4],i[5]=s[5],i[6]=s[6],i[7]=s[7],i[8]=s[8],i[9]=s[9],i[10]=s[10],i[11]=s[11],i[12]=s[12],i[13]=s[13],i[14]=s[14],i[15]=s[15],this}copyPosition(t){const i=this.elements,s=t.elements;return i[12]=s[12],i[13]=s[13],i[14]=s[14],this}setFromMatrix3(t){const i=t.elements;return this.set(i[0],i[3],i[6],0,i[1],i[4],i[7],0,i[2],i[5],i[8],0,0,0,0,1),this}extractBasis(t,i,s){return this.determinantAffine()===0?(t.set(1,0,0),i.set(0,1,0),s.set(0,0,1),this):(t.setFromMatrixColumn(this,0),i.setFromMatrixColumn(this,1),s.setFromMatrixColumn(this,2),this)}makeBasis(t,i,s){return this.set(t.x,i.x,s.x,0,t.y,i.y,s.y,0,t.z,i.z,s.z,0,0,0,0,1),this}extractRotation(t){if(t.determinantAffine()===0)return this.identity();const i=this.elements,s=t.elements,l=1/Lr.setFromMatrixColumn(t,0).length(),c=1/Lr.setFromMatrixColumn(t,1).length(),f=1/Lr.setFromMatrixColumn(t,2).length();return i[0]=s[0]*l,i[1]=s[1]*l,i[2]=s[2]*l,i[3]=0,i[4]=s[4]*c,i[5]=s[5]*c,i[6]=s[6]*c,i[7]=0,i[8]=s[8]*f,i[9]=s[9]*f,i[10]=s[10]*f,i[11]=0,i[12]=0,i[13]=0,i[14]=0,i[15]=1,this}makeRotationFromEuler(t){const i=this.elements,s=t.x,l=t.y,c=t.z,f=Math.cos(s),p=Math.sin(s),m=Math.cos(l),d=Math.sin(l),_=Math.cos(c),v=Math.sin(c);if(t.order==="XYZ"){const g=f*_,S=f*v,E=p*_,A=p*v;i[0]=m*_,i[4]=-m*v,i[8]=d,i[1]=S+E*d,i[5]=g-A*d,i[9]=-p*m,i[2]=A-g*d,i[6]=E+S*d,i[10]=f*m}else if(t.order==="YXZ"){const g=m*_,S=m*v,E=d*_,A=d*v;i[0]=g+A*p,i[4]=E*p-S,i[8]=f*d,i[1]=f*v,i[5]=f*_,i[9]=-p,i[2]=S*p-E,i[6]=A+g*p,i[10]=f*m}else if(t.order==="ZXY"){const g=m*_,S=m*v,E=d*_,A=d*v;i[0]=g-A*p,i[4]=-f*v,i[8]=E+S*p,i[1]=S+E*p,i[5]=f*_,i[9]=A-g*p,i[2]=-f*d,i[6]=p,i[10]=f*m}else if(t.order==="ZYX"){const g=f*_,S=f*v,E=p*_,A=p*v;i[0]=m*_,i[4]=E*d-S,i[8]=g*d+A,i[1]=m*v,i[5]=A*d+g,i[9]=S*d-E,i[2]=-d,i[6]=p*m,i[10]=f*m}else if(t.order==="YZX"){const g=f*m,S=f*d,E=p*m,A=p*d;i[0]=m*_,i[4]=A-g*v,i[8]=E*v+S,i[1]=v,i[5]=f*_,i[9]=-p*_,i[2]=-d*_,i[6]=S*v+E,i[10]=g-A*v}else if(t.order==="XZY"){const g=f*m,S=f*d,E=p*m,A=p*d;i[0]=m*_,i[4]=-v,i[8]=d*_,i[1]=g*v+A,i[5]=f*_,i[9]=S*v-E,i[2]=E*v-S,i[6]=p*_,i[10]=A*v+g}return i[3]=0,i[7]=0,i[11]=0,i[12]=0,i[13]=0,i[14]=0,i[15]=1,this}makeRotationFromQuaternion(t){return this.compose(EE,t,bE)}lookAt(t,i,s){const l=this.elements;return hi.subVectors(t,i),hi.lengthSq()===0&&(hi.z=1),hi.normalize(),is.crossVectors(s,hi),is.lengthSq()===0&&(Math.abs(s.z)===1?hi.x+=1e-4:hi.z+=1e-4,hi.normalize(),is.crossVectors(s,hi)),is.normalize(),Oc.crossVectors(hi,is),l[0]=is.x,l[4]=Oc.x,l[8]=hi.x,l[1]=is.y,l[5]=Oc.y,l[9]=hi.y,l[2]=is.z,l[6]=Oc.z,l[10]=hi.z,this}multiply(t){return this.multiplyMatrices(this,t)}premultiply(t){return this.multiplyMatrices(t,this)}multiplyMatrices(t,i){const s=t.elements,l=i.elements,c=this.elements,f=s[0],p=s[4],m=s[8],d=s[12],_=s[1],v=s[5],g=s[9],S=s[13],E=s[2],A=s[6],M=s[10],y=s[14],B=s[3],F=s[7],w=s[11],I=s[15],O=l[0],D=l[4],T=l[8],L=l[12],j=l[1],k=l[5],J=l[9],ut=l[13],gt=l[2],$=l[6],P=l[10],H=l[14],et=l[3],pt=l[7],Et=l[11],N=l[15];return c[0]=f*O+p*j+m*gt+d*et,c[4]=f*D+p*k+m*$+d*pt,c[8]=f*T+p*J+m*P+d*Et,c[12]=f*L+p*ut+m*H+d*N,c[1]=_*O+v*j+g*gt+S*et,c[5]=_*D+v*k+g*$+S*pt,c[9]=_*T+v*J+g*P+S*Et,c[13]=_*L+v*ut+g*H+S*N,c[2]=E*O+A*j+M*gt+y*et,c[6]=E*D+A*k+M*$+y*pt,c[10]=E*T+A*J+M*P+y*Et,c[14]=E*L+A*ut+M*H+y*N,c[3]=B*O+F*j+w*gt+I*et,c[7]=B*D+F*k+w*$+I*pt,c[11]=B*T+F*J+w*P+I*Et,c[15]=B*L+F*ut+w*H+I*N,this}multiplyScalar(t){const i=this.elements;return i[0]*=t,i[4]*=t,i[8]*=t,i[12]*=t,i[1]*=t,i[5]*=t,i[9]*=t,i[13]*=t,i[2]*=t,i[6]*=t,i[10]*=t,i[14]*=t,i[3]*=t,i[7]*=t,i[11]*=t,i[15]*=t,this}determinant(){const t=this.elements,i=t[0],s=t[4],l=t[8],c=t[12],f=t[1],p=t[5],m=t[9],d=t[13],_=t[2],v=t[6],g=t[10],S=t[14],E=t[3],A=t[7],M=t[11],y=t[15],B=m*S-d*g,F=p*S-d*v,w=p*g-m*v,I=f*S-d*_,O=f*g-m*_,D=f*v-p*_;return i*(A*B-M*F+y*w)-s*(E*B-M*I+y*O)+l*(E*F-A*I+y*D)-c*(E*w-A*O+M*D)}determinantAffine(){const t=this.elements,i=t[0],s=t[4],l=t[8],c=t[1],f=t[5],p=t[9],m=t[2],d=t[6],_=t[10];return i*(f*_-p*d)-s*(c*_-p*m)+l*(c*d-f*m)}transpose(){const t=this.elements;let i;return i=t[1],t[1]=t[4],t[4]=i,i=t[2],t[2]=t[8],t[8]=i,i=t[6],t[6]=t[9],t[9]=i,i=t[3],t[3]=t[12],t[12]=i,i=t[7],t[7]=t[13],t[13]=i,i=t[11],t[11]=t[14],t[14]=i,this}setPosition(t,i,s){const l=this.elements;return t.isVector3?(l[12]=t.x,l[13]=t.y,l[14]=t.z):(l[12]=t,l[13]=i,l[14]=s),this}invert(){const t=this.elements,i=t[0],s=t[1],l=t[2],c=t[3],f=t[4],p=t[5],m=t[6],d=t[7],_=t[8],v=t[9],g=t[10],S=t[11],E=t[12],A=t[13],M=t[14],y=t[15],B=i*p-s*f,F=i*m-l*f,w=i*d-c*f,I=s*m-l*p,O=s*d-c*p,D=l*d-c*m,T=_*A-v*E,L=_*M-g*E,j=_*y-S*E,k=v*M-g*A,J=v*y-S*A,ut=g*y-S*M,gt=B*ut-F*J+w*k+I*j-O*L+D*T;if(gt===0)return this.set(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);const $=1/gt;return t[0]=(p*ut-m*J+d*k)*$,t[1]=(l*J-s*ut-c*k)*$,t[2]=(A*D-M*O+y*I)*$,t[3]=(g*O-v*D-S*I)*$,t[4]=(m*j-f*ut-d*L)*$,t[5]=(i*ut-l*j+c*L)*$,t[6]=(M*w-E*D-y*F)*$,t[7]=(_*D-g*w+S*F)*$,t[8]=(f*J-p*j+d*T)*$,t[9]=(s*j-i*J-c*T)*$,t[10]=(E*O-A*w+y*B)*$,t[11]=(v*w-_*O-S*B)*$,t[12]=(p*L-f*k-m*T)*$,t[13]=(i*k-s*L+l*T)*$,t[14]=(A*F-E*I-M*B)*$,t[15]=(_*I-v*F+g*B)*$,this}scale(t){const i=this.elements,s=t.x,l=t.y,c=t.z;return i[0]*=s,i[4]*=l,i[8]*=c,i[1]*=s,i[5]*=l,i[9]*=c,i[2]*=s,i[6]*=l,i[10]*=c,i[3]*=s,i[7]*=l,i[11]*=c,this}getMaxScaleOnAxis(){const t=this.elements,i=t[0]*t[0]+t[1]*t[1]+t[2]*t[2],s=t[4]*t[4]+t[5]*t[5]+t[6]*t[6],l=t[8]*t[8]+t[9]*t[9]+t[10]*t[10];return Math.sqrt(Math.max(i,s,l))}makeTranslation(t,i,s){return t.isVector3?this.set(1,0,0,t.x,0,1,0,t.y,0,0,1,t.z,0,0,0,1):this.set(1,0,0,t,0,1,0,i,0,0,1,s,0,0,0,1),this}makeRotationX(t){const i=Math.cos(t),s=Math.sin(t);return this.set(1,0,0,0,0,i,-s,0,0,s,i,0,0,0,0,1),this}makeRotationY(t){const i=Math.cos(t),s=Math.sin(t);return this.set(i,0,s,0,0,1,0,0,-s,0,i,0,0,0,0,1),this}makeRotationZ(t){const i=Math.cos(t),s=Math.sin(t);return this.set(i,-s,0,0,s,i,0,0,0,0,1,0,0,0,0,1),this}makeRotationAxis(t,i){const s=Math.cos(i),l=Math.sin(i),c=1-s,f=t.x,p=t.y,m=t.z,d=c*f,_=c*p;return this.set(d*f+s,d*p-l*m,d*m+l*p,0,d*p+l*m,_*p+s,_*m-l*f,0,d*m-l*p,_*m+l*f,c*m*m+s,0,0,0,0,1),this}makeScale(t,i,s){return this.set(t,0,0,0,0,i,0,0,0,0,s,0,0,0,0,1),this}makeShear(t,i,s,l,c,f){return this.set(1,s,c,0,t,1,f,0,i,l,1,0,0,0,0,1),this}compose(t,i,s){const l=this.elements,c=i._x,f=i._y,p=i._z,m=i._w,d=c+c,_=f+f,v=p+p,g=c*d,S=c*_,E=c*v,A=f*_,M=f*v,y=p*v,B=m*d,F=m*_,w=m*v,I=s.x,O=s.y,D=s.z;return l[0]=(1-(A+y))*I,l[1]=(S+w)*I,l[2]=(E-F)*I,l[3]=0,l[4]=(S-w)*O,l[5]=(1-(g+y))*O,l[6]=(M+B)*O,l[7]=0,l[8]=(E+F)*D,l[9]=(M-B)*D,l[10]=(1-(g+A))*D,l[11]=0,l[12]=t.x,l[13]=t.y,l[14]=t.z,l[15]=1,this}decompose(t,i,s){const l=this.elements;t.x=l[12],t.y=l[13],t.z=l[14];const c=this.determinantAffine();if(c===0)return s.set(1,1,1),i.identity(),this;let f=Lr.set(l[0],l[1],l[2]).length();const p=Lr.set(l[4],l[5],l[6]).length(),m=Lr.set(l[8],l[9],l[10]).length();c<0&&(f=-f),Oi.copy(this);const d=1/f,_=1/p,v=1/m;return Oi.elements[0]*=d,Oi.elements[1]*=d,Oi.elements[2]*=d,Oi.elements[4]*=_,Oi.elements[5]*=_,Oi.elements[6]*=_,Oi.elements[8]*=v,Oi.elements[9]*=v,Oi.elements[10]*=v,i.setFromRotationMatrix(Oi),s.x=f,s.y=p,s.z=m,this}makePerspective(t,i,s,l,c,f,p=ji,m=!1){const d=this.elements,_=2*c/(i-t),v=2*c/(s-l),g=(i+t)/(i-t),S=(s+l)/(s-l);let E,A;if(m)E=c/(f-c),A=f*c/(f-c);else if(p===ji)E=-(f+c)/(f-c),A=-2*f*c/(f-c);else if(p===yl)E=-f/(f-c),A=-f*c/(f-c);else throw new Error("THREE.Matrix4.makePerspective(): Invalid coordinate system: "+p);return d[0]=_,d[4]=0,d[8]=g,d[12]=0,d[1]=0,d[5]=v,d[9]=S,d[13]=0,d[2]=0,d[6]=0,d[10]=E,d[14]=A,d[3]=0,d[7]=0,d[11]=-1,d[15]=0,this}makeOrthographic(t,i,s,l,c,f,p=ji,m=!1){const d=this.elements,_=2/(i-t),v=2/(s-l),g=-(i+t)/(i-t),S=-(s+l)/(s-l);let E,A;if(m)E=1/(f-c),A=f/(f-c);else if(p===ji)E=-2/(f-c),A=-(f+c)/(f-c);else if(p===yl)E=-1/(f-c),A=-c/(f-c);else throw new Error("THREE.Matrix4.makeOrthographic(): Invalid coordinate system: "+p);return d[0]=_,d[4]=0,d[8]=0,d[12]=g,d[1]=0,d[5]=v,d[9]=0,d[13]=S,d[2]=0,d[6]=0,d[10]=E,d[14]=A,d[3]=0,d[7]=0,d[11]=0,d[15]=1,this}equals(t){const i=this.elements,s=t.elements;for(let l=0;l<16;l++)if(i[l]!==s[l])return!1;return!0}fromArray(t,i=0){for(let s=0;s<16;s++)this.elements[s]=t[s+i];return this}toArray(t=[],i=0){const s=this.elements;return t[i]=s[0],t[i+1]=s[1],t[i+2]=s[2],t[i+3]=s[3],t[i+4]=s[4],t[i+5]=s[5],t[i+6]=s[6],t[i+7]=s[7],t[i+8]=s[8],t[i+9]=s[9],t[i+10]=s[10],t[i+11]=s[11],t[i+12]=s[12],t[i+13]=s[13],t[i+14]=s[14],t[i+15]=s[15],t}};Ru.prototype.isMatrix4=!0;let Fe=Ru;const Lr=new V,Oi=new Fe,EE=new V(0,0,0),bE=new V(1,1,1),is=new V,Oc=new V,hi=new V,J_=new Fe,$_=new ro;class ds{constructor(t=0,i=0,s=0,l=ds.DEFAULT_ORDER){this.isEuler=!0,this._x=t,this._y=i,this._z=s,this._order=l}get x(){return this._x}set x(t){this._x=t,this._onChangeCallback()}get y(){return this._y}set y(t){this._y=t,this._onChangeCallback()}get z(){return this._z}set z(t){this._z=t,this._onChangeCallback()}get order(){return this._order}set order(t){this._order=t,this._onChangeCallback()}set(t,i,s,l=this._order){return this._x=t,this._y=i,this._z=s,this._order=l,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._order)}copy(t){return this._x=t._x,this._y=t._y,this._z=t._z,this._order=t._order,this._onChangeCallback(),this}setFromRotationMatrix(t,i=this._order,s=!0){const l=t.elements,c=l[0],f=l[4],p=l[8],m=l[1],d=l[5],_=l[9],v=l[2],g=l[6],S=l[10];switch(i){case"XYZ":this._y=Math.asin(ye(p,-1,1)),Math.abs(p)<.9999999?(this._x=Math.atan2(-_,S),this._z=Math.atan2(-f,c)):(this._x=Math.atan2(g,d),this._z=0);break;case"YXZ":this._x=Math.asin(-ye(_,-1,1)),Math.abs(_)<.9999999?(this._y=Math.atan2(p,S),this._z=Math.atan2(m,d)):(this._y=Math.atan2(-v,c),this._z=0);break;case"ZXY":this._x=Math.asin(ye(g,-1,1)),Math.abs(g)<.9999999?(this._y=Math.atan2(-v,S),this._z=Math.atan2(-f,d)):(this._y=0,this._z=Math.atan2(m,c));break;case"ZYX":this._y=Math.asin(-ye(v,-1,1)),Math.abs(v)<.9999999?(this._x=Math.atan2(g,S),this._z=Math.atan2(m,c)):(this._x=0,this._z=Math.atan2(-f,d));break;case"YZX":this._z=Math.asin(ye(m,-1,1)),Math.abs(m)<.9999999?(this._x=Math.atan2(-_,d),this._y=Math.atan2(-v,c)):(this._x=0,this._y=Math.atan2(p,S));break;case"XZY":this._z=Math.asin(-ye(f,-1,1)),Math.abs(f)<.9999999?(this._x=Math.atan2(g,d),this._y=Math.atan2(p,c)):(this._x=Math.atan2(-_,S),this._y=0);break;default:re("Euler: .setFromRotationMatrix() encountered an unknown order: "+i)}return this._order=i,s===!0&&this._onChangeCallback(),this}setFromQuaternion(t,i,s){return J_.makeRotationFromQuaternion(t),this.setFromRotationMatrix(J_,i,s)}setFromVector3(t,i=this._order){return this.set(t.x,t.y,t.z,i)}reorder(t){return $_.setFromEuler(this),this.setFromQuaternion($_,t)}equals(t){return t._x===this._x&&t._y===this._y&&t._z===this._z&&t._order===this._order}fromArray(t){return this._x=t[0],this._y=t[1],this._z=t[2],t[3]!==void 0&&(this._order=t[3]),this._onChangeCallback(),this}toArray(t=[],i=0){return t[i]=this._x,t[i+1]=this._y,t[i+2]=this._z,t[i+3]=this._order,t}_onChange(t){return this._onChangeCallback=t,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._order}}ds.DEFAULT_ORDER="XYZ";class Fp{constructor(){this.mask=1}set(t){this.mask=(1<<t|0)>>>0}enable(t){this.mask|=1<<t|0}enableAll(){this.mask=-1}toggle(t){this.mask^=1<<t|0}disable(t){this.mask&=~(1<<t|0)}disableAll(){this.mask=0}test(t){return(this.mask&t.mask)!==0}isEnabled(t){return(this.mask&(1<<t|0))!==0}}let TE=0;const tv=new V,Or=new ro,xa=new Fe,Pc=new V,el=new V,AE=new V,RE=new ro,ev=new V(1,0,0),nv=new V(0,1,0),iv=new V(0,0,1),av={type:"added"},CE={type:"removed"},Pr={type:"childadded",child:null},Qh={type:"childremoved",child:null};class Mn extends Ks{constructor(){super(),this.isObject3D=!0,Object.defineProperty(this,"id",{value:TE++}),this.uuid=Aa(),this.name="",this.type="Object3D",this.parent=null,this.children=[],this.up=Mn.DEFAULT_UP.clone();const t=new V,i=new ds,s=new ro,l=new V(1,1,1);function c(){s.setFromEuler(i,!1)}function f(){i.setFromQuaternion(s,void 0,!1)}i._onChange(c),s._onChange(f),Object.defineProperties(this,{position:{configurable:!0,enumerable:!0,value:t},rotation:{configurable:!0,enumerable:!0,value:i},quaternion:{configurable:!0,enumerable:!0,value:s},scale:{configurable:!0,enumerable:!0,value:l},modelViewMatrix:{value:new Fe},normalMatrix:{value:new de}}),this.matrix=new Fe,this.matrixWorld=new Fe,this.matrixAutoUpdate=Mn.DEFAULT_MATRIX_AUTO_UPDATE,this.matrixWorldAutoUpdate=Mn.DEFAULT_MATRIX_WORLD_AUTO_UPDATE,this.matrixWorldNeedsUpdate=!1,this.layers=new Fp,this.visible=!0,this.castShadow=!1,this.receiveShadow=!1,this.frustumCulled=!0,this.renderOrder=0,this.animations=[],this.customDepthMaterial=void 0,this.customDistanceMaterial=void 0,this.static=!1,this.userData={},this.pivot=null}onBeforeShadow(){}onAfterShadow(){}onBeforeRender(){}onAfterRender(){}applyMatrix4(t){this.matrixAutoUpdate&&this.updateMatrix(),this.matrix.premultiply(t),this.matrix.decompose(this.position,this.quaternion,this.scale)}applyQuaternion(t){return this.quaternion.premultiply(t),this}setRotationFromAxisAngle(t,i){this.quaternion.setFromAxisAngle(t,i)}setRotationFromEuler(t){this.quaternion.setFromEuler(t,!0)}setRotationFromMatrix(t){this.quaternion.setFromRotationMatrix(t)}setRotationFromQuaternion(t){this.quaternion.copy(t)}rotateOnAxis(t,i){return Or.setFromAxisAngle(t,i),this.quaternion.multiply(Or),this}rotateOnWorldAxis(t,i){return Or.setFromAxisAngle(t,i),this.quaternion.premultiply(Or),this}rotateX(t){return this.rotateOnAxis(ev,t)}rotateY(t){return this.rotateOnAxis(nv,t)}rotateZ(t){return this.rotateOnAxis(iv,t)}translateOnAxis(t,i){return tv.copy(t).applyQuaternion(this.quaternion),this.position.add(tv.multiplyScalar(i)),this}translateX(t){return this.translateOnAxis(ev,t)}translateY(t){return this.translateOnAxis(nv,t)}translateZ(t){return this.translateOnAxis(iv,t)}localToWorld(t){return this.updateWorldMatrix(!0,!1),t.applyMatrix4(this.matrixWorld)}worldToLocal(t){return this.updateWorldMatrix(!0,!1),t.applyMatrix4(xa.copy(this.matrixWorld).invert())}lookAt(t,i,s){t.isVector3?Pc.copy(t):Pc.set(t,i,s);const l=this.parent;this.updateWorldMatrix(!0,!1),el.setFromMatrixPosition(this.matrixWorld),this.isCamera||this.isLight?xa.lookAt(el,Pc,this.up):xa.lookAt(Pc,el,this.up),this.quaternion.setFromRotationMatrix(xa),l&&(xa.extractRotation(l.matrixWorld),Or.setFromRotationMatrix(xa),this.quaternion.premultiply(Or.invert()))}add(t){if(arguments.length>1){for(let i=0;i<arguments.length;i++)this.add(arguments[i]);return this}return t===this?(Ue("Object3D.add: object can't be added as a child of itself.",t),this):(t&&t.isObject3D?(t.removeFromParent(),t.parent=this,this.children.push(t),t.dispatchEvent(av),Pr.child=t,this.dispatchEvent(Pr),Pr.child=null):Ue("Object3D.add: object not an instance of THREE.Object3D.",t),this)}remove(t){if(arguments.length>1){for(let s=0;s<arguments.length;s++)this.remove(arguments[s]);return this}const i=this.children.indexOf(t);return i!==-1&&(t.parent=null,this.children.splice(i,1),t.dispatchEvent(CE),Qh.child=t,this.dispatchEvent(Qh),Qh.child=null),this}removeFromParent(){const t=this.parent;return t!==null&&t.remove(this),this}clear(){return this.remove(...this.children)}attach(t){return this.updateWorldMatrix(!0,!1),xa.copy(this.matrixWorld).invert(),t.parent!==null&&(t.parent.updateWorldMatrix(!0,!1),xa.multiply(t.parent.matrixWorld)),t.applyMatrix4(xa),t.removeFromParent(),t.parent=this,this.children.push(t),t.updateWorldMatrix(!1,!0),t.dispatchEvent(av),Pr.child=t,this.dispatchEvent(Pr),Pr.child=null,this}getObjectById(t){return this.getObjectByProperty("id",t)}getObjectByName(t){return this.getObjectByProperty("name",t)}getObjectByProperty(t,i){if(this[t]===i)return this;for(let s=0,l=this.children.length;s<l;s++){const f=this.children[s].getObjectByProperty(t,i);if(f!==void 0)return f}}getObjectsByProperty(t,i,s=[]){this[t]===i&&s.push(this);const l=this.children;for(let c=0,f=l.length;c<f;c++)l[c].getObjectsByProperty(t,i,s);return s}getWorldPosition(t){return this.updateWorldMatrix(!0,!1),t.setFromMatrixPosition(this.matrixWorld)}getWorldQuaternion(t){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(el,t,AE),t}getWorldScale(t){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(el,RE,t),t}getWorldDirection(t){this.updateWorldMatrix(!0,!1);const i=this.matrixWorld.elements;return t.set(i[8],i[9],i[10]).normalize()}raycast(){}traverse(t){t(this);const i=this.children;for(let s=0,l=i.length;s<l;s++)i[s].traverse(t)}traverseVisible(t){if(this.visible===!1)return;t(this);const i=this.children;for(let s=0,l=i.length;s<l;s++)i[s].traverseVisible(t)}traverseAncestors(t){const i=this.parent;i!==null&&(t(i),i.traverseAncestors(t))}updateMatrix(){this.matrix.compose(this.position,this.quaternion,this.scale);const t=this.pivot;if(t!==null){const i=t.x,s=t.y,l=t.z,c=this.matrix.elements;c[12]+=i-c[0]*i-c[4]*s-c[8]*l,c[13]+=s-c[1]*i-c[5]*s-c[9]*l,c[14]+=l-c[2]*i-c[6]*s-c[10]*l}this.matrixWorldNeedsUpdate=!0}updateMatrixWorld(t){this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||t)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,t=!0);const i=this.children;for(let s=0,l=i.length;s<l;s++)i[s].updateMatrixWorld(t)}updateWorldMatrix(t,i,s=!1){const l=this.parent;if(t===!0&&l!==null&&l.updateWorldMatrix(!0,!1),this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||s)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,s=!0),i===!0){const c=this.children;for(let f=0,p=c.length;f<p;f++)c[f].updateWorldMatrix(!1,!0,s)}}toJSON(t){const i=t===void 0||typeof t=="string",s={};i&&(t={geometries:{},materials:{},textures:{},images:{},shapes:{},skeletons:{},animations:{},nodes:{}},s.metadata={version:4.7,type:"Object",generator:"Object3D.toJSON"});const l={};l.uuid=this.uuid,l.type=this.type,this.name!==""&&(l.name=this.name),this.castShadow===!0&&(l.castShadow=!0),this.receiveShadow===!0&&(l.receiveShadow=!0),this.visible===!1&&(l.visible=!1),this.frustumCulled===!1&&(l.frustumCulled=!1),this.renderOrder!==0&&(l.renderOrder=this.renderOrder),this.static!==!1&&(l.static=this.static),Object.keys(this.userData).length>0&&(l.userData=this.userData),l.layers=this.layers.mask,l.matrix=this.matrix.toArray(),l.up=this.up.toArray(),this.pivot!==null&&(l.pivot=this.pivot.toArray()),this.matrixAutoUpdate===!1&&(l.matrixAutoUpdate=!1),this.morphTargetDictionary!==void 0&&(l.morphTargetDictionary=Object.assign({},this.morphTargetDictionary)),this.morphTargetInfluences!==void 0&&(l.morphTargetInfluences=this.morphTargetInfluences.slice()),this.isInstancedMesh&&(l.type="InstancedMesh",l.count=this.count,l.instanceMatrix=this.instanceMatrix.toJSON(),this.instanceColor!==null&&(l.instanceColor=this.instanceColor.toJSON())),this.isBatchedMesh&&(l.type="BatchedMesh",l.perObjectFrustumCulled=this.perObjectFrustumCulled,l.sortObjects=this.sortObjects,l.drawRanges=this._drawRanges,l.reservedRanges=this._reservedRanges,l.geometryInfo=this._geometryInfo.map(p=>({...p,boundingBox:p.boundingBox?p.boundingBox.toJSON():void 0,boundingSphere:p.boundingSphere?p.boundingSphere.toJSON():void 0})),l.instanceInfo=this._instanceInfo.map(p=>({...p})),l.availableInstanceIds=this._availableInstanceIds.slice(),l.availableGeometryIds=this._availableGeometryIds.slice(),l.nextIndexStart=this._nextIndexStart,l.nextVertexStart=this._nextVertexStart,l.geometryCount=this._geometryCount,l.maxInstanceCount=this._maxInstanceCount,l.maxVertexCount=this._maxVertexCount,l.maxIndexCount=this._maxIndexCount,l.geometryInitialized=this._geometryInitialized,l.matricesTexture=this._matricesTexture.toJSON(t),l.indirectTexture=this._indirectTexture.toJSON(t),this._colorsTexture!==null&&(l.colorsTexture=this._colorsTexture.toJSON(t)),this.boundingSphere!==null&&(l.boundingSphere=this.boundingSphere.toJSON()),this.boundingBox!==null&&(l.boundingBox=this.boundingBox.toJSON()));function c(p,m){return p[m.uuid]===void 0&&(p[m.uuid]=m.toJSON(t)),m.uuid}if(this.isScene)this.background&&(this.background.isColor?l.background=this.background.toJSON():this.background.isTexture&&(l.background=this.background.toJSON(t).uuid)),this.environment&&this.environment.isTexture&&this.environment.isRenderTargetTexture!==!0&&(l.environment=this.environment.toJSON(t).uuid);else if(this.isMesh||this.isLine||this.isPoints){l.geometry=c(t.geometries,this.geometry);const p=this.geometry.parameters;if(p!==void 0&&p.shapes!==void 0){const m=p.shapes;if(Array.isArray(m))for(let d=0,_=m.length;d<_;d++){const v=m[d];c(t.shapes,v)}else c(t.shapes,m)}}if(this.isSkinnedMesh&&(l.bindMode=this.bindMode,l.bindMatrix=this.bindMatrix.toArray(),this.skeleton!==void 0&&(c(t.skeletons,this.skeleton),l.skeleton=this.skeleton.uuid)),this.material!==void 0)if(Array.isArray(this.material)){const p=[];for(let m=0,d=this.material.length;m<d;m++)p.push(c(t.materials,this.material[m]));l.material=p}else l.material=c(t.materials,this.material);if(this.children.length>0){l.children=[];for(let p=0;p<this.children.length;p++)l.children.push(this.children[p].toJSON(t).object)}if(this.animations.length>0){l.animations=[];for(let p=0;p<this.animations.length;p++){const m=this.animations[p];l.animations.push(c(t.animations,m))}}if(i){const p=f(t.geometries),m=f(t.materials),d=f(t.textures),_=f(t.images),v=f(t.shapes),g=f(t.skeletons),S=f(t.animations),E=f(t.nodes);p.length>0&&(s.geometries=p),m.length>0&&(s.materials=m),d.length>0&&(s.textures=d),_.length>0&&(s.images=_),v.length>0&&(s.shapes=v),g.length>0&&(s.skeletons=g),S.length>0&&(s.animations=S),E.length>0&&(s.nodes=E)}return s.object=l,s;function f(p){const m=[];for(const d in p){const _=p[d];delete _.metadata,m.push(_)}return m}}clone(t){return new this.constructor().copy(this,t)}copy(t,i=!0){if(this.name=t.name,this.up.copy(t.up),this.position.copy(t.position),this.rotation.order=t.rotation.order,this.quaternion.copy(t.quaternion),this.scale.copy(t.scale),this.pivot=t.pivot!==null?t.pivot.clone():null,this.matrix.copy(t.matrix),this.matrixWorld.copy(t.matrixWorld),this.matrixAutoUpdate=t.matrixAutoUpdate,this.matrixWorldAutoUpdate=t.matrixWorldAutoUpdate,this.matrixWorldNeedsUpdate=t.matrixWorldNeedsUpdate,this.layers.mask=t.layers.mask,this.visible=t.visible,this.castShadow=t.castShadow,this.receiveShadow=t.receiveShadow,this.frustumCulled=t.frustumCulled,this.renderOrder=t.renderOrder,this.static=t.static,this.animations=t.animations.slice(),this.userData=JSON.parse(JSON.stringify(t.userData)),i===!0)for(let s=0;s<t.children.length;s++){const l=t.children[s];this.add(l.clone())}return this}}Mn.DEFAULT_UP=new V(0,1,0);Mn.DEFAULT_MATRIX_AUTO_UPDATE=!0;Mn.DEFAULT_MATRIX_WORLD_AUTO_UPDATE=!0;class qs extends Mn{constructor(){super(),this.isGroup=!0,this.type="Group"}}const wE={type:"move"};class Jh{constructor(){this._targetRay=null,this._grip=null,this._hand=null}getHandSpace(){return this._hand===null&&(this._hand=new qs,this._hand.matrixAutoUpdate=!1,this._hand.visible=!1,this._hand.joints={},this._hand.inputState={pinching:!1}),this._hand}getTargetRaySpace(){return this._targetRay===null&&(this._targetRay=new qs,this._targetRay.matrixAutoUpdate=!1,this._targetRay.visible=!1,this._targetRay.hasLinearVelocity=!1,this._targetRay.linearVelocity=new V,this._targetRay.hasAngularVelocity=!1,this._targetRay.angularVelocity=new V),this._targetRay}getGripSpace(){return this._grip===null&&(this._grip=new qs,this._grip.matrixAutoUpdate=!1,this._grip.visible=!1,this._grip.hasLinearVelocity=!1,this._grip.linearVelocity=new V,this._grip.hasAngularVelocity=!1,this._grip.angularVelocity=new V,this._grip.eventsEnabled=!1),this._grip}dispatchEvent(t){return this._targetRay!==null&&this._targetRay.dispatchEvent(t),this._grip!==null&&this._grip.dispatchEvent(t),this._hand!==null&&this._hand.dispatchEvent(t),this}connect(t){if(t&&t.hand){const i=this._hand;if(i)for(const s of t.hand.values())this._getHandJoint(i,s)}return this.dispatchEvent({type:"connected",data:t}),this}disconnect(t){return this.dispatchEvent({type:"disconnected",data:t}),this._targetRay!==null&&(this._targetRay.visible=!1),this._grip!==null&&(this._grip.visible=!1),this._hand!==null&&(this._hand.visible=!1),this}update(t,i,s){let l=null,c=null,f=null;const p=this._targetRay,m=this._grip,d=this._hand;if(t&&i.session.visibilityState!=="visible-blurred"){if(d&&t.hand){f=!0;for(const A of t.hand.values()){const M=i.getJointPose(A,s),y=this._getHandJoint(d,A);M!==null&&(y.matrix.fromArray(M.transform.matrix),y.matrix.decompose(y.position,y.rotation,y.scale),y.matrixWorldNeedsUpdate=!0,y.jointRadius=M.radius),y.visible=M!==null}const _=d.joints["index-finger-tip"],v=d.joints["thumb-tip"],g=_.position.distanceTo(v.position),S=.02,E=.005;d.inputState.pinching&&g>S+E?(d.inputState.pinching=!1,this.dispatchEvent({type:"pinchend",handedness:t.handedness,target:this})):!d.inputState.pinching&&g<=S-E&&(d.inputState.pinching=!0,this.dispatchEvent({type:"pinchstart",handedness:t.handedness,target:this}))}else m!==null&&t.gripSpace&&(c=i.getPose(t.gripSpace,s),c!==null&&(m.matrix.fromArray(c.transform.matrix),m.matrix.decompose(m.position,m.rotation,m.scale),m.matrixWorldNeedsUpdate=!0,c.linearVelocity?(m.hasLinearVelocity=!0,m.linearVelocity.copy(c.linearVelocity)):m.hasLinearVelocity=!1,c.angularVelocity?(m.hasAngularVelocity=!0,m.angularVelocity.copy(c.angularVelocity)):m.hasAngularVelocity=!1,m.eventsEnabled&&m.dispatchEvent({type:"gripUpdated",data:t,target:this})));p!==null&&(l=i.getPose(t.targetRaySpace,s),l===null&&c!==null&&(l=c),l!==null&&(p.matrix.fromArray(l.transform.matrix),p.matrix.decompose(p.position,p.rotation,p.scale),p.matrixWorldNeedsUpdate=!0,l.linearVelocity?(p.hasLinearVelocity=!0,p.linearVelocity.copy(l.linearVelocity)):p.hasLinearVelocity=!1,l.angularVelocity?(p.hasAngularVelocity=!0,p.angularVelocity.copy(l.angularVelocity)):p.hasAngularVelocity=!1,this.dispatchEvent(wE)))}return p!==null&&(p.visible=l!==null),m!==null&&(m.visible=c!==null),d!==null&&(d.visible=f!==null),this}_getHandJoint(t,i){if(t.joints[i.jointName]===void 0){const s=new qs;s.matrixAutoUpdate=!1,s.visible=!1,t.joints[i.jointName]=s,t.add(s)}return t.joints[i.jointName]}}const Mx={aliceblue:15792383,antiquewhite:16444375,aqua:65535,aquamarine:8388564,azure:15794175,beige:16119260,bisque:16770244,black:0,blanchedalmond:16772045,blue:255,blueviolet:9055202,brown:10824234,burlywood:14596231,cadetblue:6266528,chartreuse:8388352,chocolate:13789470,coral:16744272,cornflowerblue:6591981,cornsilk:16775388,crimson:14423100,cyan:65535,darkblue:139,darkcyan:35723,darkgoldenrod:12092939,darkgray:11119017,darkgreen:25600,darkgrey:11119017,darkkhaki:12433259,darkmagenta:9109643,darkolivegreen:5597999,darkorange:16747520,darkorchid:10040012,darkred:9109504,darksalmon:15308410,darkseagreen:9419919,darkslateblue:4734347,darkslategray:3100495,darkslategrey:3100495,darkturquoise:52945,darkviolet:9699539,deeppink:16716947,deepskyblue:49151,dimgray:6908265,dimgrey:6908265,dodgerblue:2003199,firebrick:11674146,floralwhite:16775920,forestgreen:2263842,fuchsia:16711935,gainsboro:14474460,ghostwhite:16316671,gold:16766720,goldenrod:14329120,gray:8421504,green:32768,greenyellow:11403055,grey:8421504,honeydew:15794160,hotpink:16738740,indianred:13458524,indigo:4915330,ivory:16777200,khaki:15787660,lavender:15132410,lavenderblush:16773365,lawngreen:8190976,lemonchiffon:16775885,lightblue:11393254,lightcoral:15761536,lightcyan:14745599,lightgoldenrodyellow:16448210,lightgray:13882323,lightgreen:9498256,lightgrey:13882323,lightpink:16758465,lightsalmon:16752762,lightseagreen:2142890,lightskyblue:8900346,lightslategray:7833753,lightslategrey:7833753,lightsteelblue:11584734,lightyellow:16777184,lime:65280,limegreen:3329330,linen:16445670,magenta:16711935,maroon:8388608,mediumaquamarine:6737322,mediumblue:205,mediumorchid:12211667,mediumpurple:9662683,mediumseagreen:3978097,mediumslateblue:8087790,mediumspringgreen:64154,mediumturquoise:4772300,mediumvioletred:13047173,midnightblue:1644912,mintcream:16121850,mistyrose:16770273,moccasin:16770229,navajowhite:16768685,navy:128,oldlace:16643558,olive:8421376,olivedrab:7048739,orange:16753920,orangered:16729344,orchid:14315734,palegoldenrod:15657130,palegreen:10025880,paleturquoise:11529966,palevioletred:14381203,papayawhip:16773077,peachpuff:16767673,peru:13468991,pink:16761035,plum:14524637,powderblue:11591910,purple:8388736,rebeccapurple:6697881,red:16711680,rosybrown:12357519,royalblue:4286945,saddlebrown:9127187,salmon:16416882,sandybrown:16032864,seagreen:3050327,seashell:16774638,sienna:10506797,silver:12632256,skyblue:8900331,slateblue:6970061,slategray:7372944,slategrey:7372944,snow:16775930,springgreen:65407,steelblue:4620980,tan:13808780,teal:32896,thistle:14204888,tomato:16737095,turquoise:4251856,violet:15631086,wheat:16113331,white:16777215,whitesmoke:16119285,yellow:16776960,yellowgreen:10145074},as={h:0,s:0,l:0},zc={h:0,s:0,l:0};function $h(r,t,i){return i<0&&(i+=1),i>1&&(i-=1),i<1/6?r+(t-r)*6*i:i<1/2?t:i<2/3?r+(t-r)*6*(2/3-i):r}class ue{constructor(t,i,s){return this.isColor=!0,this.r=1,this.g=1,this.b=1,this.set(t,i,s)}set(t,i,s){if(i===void 0&&s===void 0){const l=t;l&&l.isColor?this.copy(l):typeof l=="number"?this.setHex(l):typeof l=="string"&&this.setStyle(l)}else this.setRGB(t,i,s);return this}setScalar(t){return this.r=t,this.g=t,this.b=t,this}setHex(t,i=qn){return t=Math.floor(t),this.r=(t>>16&255)/255,this.g=(t>>8&255)/255,this.b=(t&255)/255,Ne.colorSpaceToWorking(this,i),this}setRGB(t,i,s,l=Ne.workingColorSpace){return this.r=t,this.g=i,this.b=s,Ne.colorSpaceToWorking(this,l),this}setHSL(t,i,s,l=Ne.workingColorSpace){if(t=Bp(t,1),i=ye(i,0,1),s=ye(s,0,1),i===0)this.r=this.g=this.b=s;else{const c=s<=.5?s*(1+i):s+i-s*i,f=2*s-c;this.r=$h(f,c,t+1/3),this.g=$h(f,c,t),this.b=$h(f,c,t-1/3)}return Ne.colorSpaceToWorking(this,l),this}setStyle(t,i=qn){function s(c){c!==void 0&&parseFloat(c)<1&&re("Color: Alpha component of "+t+" will be ignored.")}let l;if(l=/^(\w+)\(([^\)]*)\)/.exec(t)){let c;const f=l[1],p=l[2];switch(f){case"rgb":case"rgba":if(c=/^\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(p))return s(c[4]),this.setRGB(Math.min(255,parseInt(c[1],10))/255,Math.min(255,parseInt(c[2],10))/255,Math.min(255,parseInt(c[3],10))/255,i);if(c=/^\s*(\d+)\%\s*,\s*(\d+)\%\s*,\s*(\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(p))return s(c[4]),this.setRGB(Math.min(100,parseInt(c[1],10))/100,Math.min(100,parseInt(c[2],10))/100,Math.min(100,parseInt(c[3],10))/100,i);break;case"hsl":case"hsla":if(c=/^\s*(\d*\.?\d+)\s*,\s*(\d*\.?\d+)\%\s*,\s*(\d*\.?\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(p))return s(c[4]),this.setHSL(parseFloat(c[1])/360,parseFloat(c[2])/100,parseFloat(c[3])/100,i);break;default:re("Color: Unknown color model "+t)}}else if(l=/^\#([A-Fa-f\d]+)$/.exec(t)){const c=l[1],f=c.length;if(f===3)return this.setRGB(parseInt(c.charAt(0),16)/15,parseInt(c.charAt(1),16)/15,parseInt(c.charAt(2),16)/15,i);if(f===6)return this.setHex(parseInt(c,16),i);re("Color: Invalid hex color "+t)}else if(t&&t.length>0)return this.setColorName(t,i);return this}setColorName(t,i=qn){const s=Mx[t.toLowerCase()];return s!==void 0?this.setHex(s,i):re("Color: Unknown color "+t),this}clone(){return new this.constructor(this.r,this.g,this.b)}copy(t){return this.r=t.r,this.g=t.g,this.b=t.b,this}copySRGBToLinear(t){return this.r=Ra(t.r),this.g=Ra(t.g),this.b=Ra(t.b),this}copyLinearToSRGB(t){return this.r=to(t.r),this.g=to(t.g),this.b=to(t.b),this}convertSRGBToLinear(){return this.copySRGBToLinear(this),this}convertLinearToSRGB(){return this.copyLinearToSRGB(this),this}getHex(t=qn){return Ne.workingToColorSpace(Hn.copy(this),t),Math.round(ye(Hn.r*255,0,255))*65536+Math.round(ye(Hn.g*255,0,255))*256+Math.round(ye(Hn.b*255,0,255))}getHexString(t=qn){return("000000"+this.getHex(t).toString(16)).slice(-6)}getHSL(t,i=Ne.workingColorSpace){Ne.workingToColorSpace(Hn.copy(this),i);const s=Hn.r,l=Hn.g,c=Hn.b,f=Math.max(s,l,c),p=Math.min(s,l,c);let m,d;const _=(p+f)/2;if(p===f)m=0,d=0;else{const v=f-p;switch(d=_<=.5?v/(f+p):v/(2-f-p),f){case s:m=(l-c)/v+(l<c?6:0);break;case l:m=(c-s)/v+2;break;case c:m=(s-l)/v+4;break}m/=6}return t.h=m,t.s=d,t.l=_,t}getRGB(t,i=Ne.workingColorSpace){return Ne.workingToColorSpace(Hn.copy(this),i),t.r=Hn.r,t.g=Hn.g,t.b=Hn.b,t}getStyle(t=qn){Ne.workingToColorSpace(Hn.copy(this),t);const i=Hn.r,s=Hn.g,l=Hn.b;return t!==qn?`color(${t} ${i.toFixed(3)} ${s.toFixed(3)} ${l.toFixed(3)})`:`rgb(${Math.round(i*255)},${Math.round(s*255)},${Math.round(l*255)})`}offsetHSL(t,i,s){return this.getHSL(as),this.setHSL(as.h+t,as.s+i,as.l+s)}add(t){return this.r+=t.r,this.g+=t.g,this.b+=t.b,this}addColors(t,i){return this.r=t.r+i.r,this.g=t.g+i.g,this.b=t.b+i.b,this}addScalar(t){return this.r+=t,this.g+=t,this.b+=t,this}sub(t){return this.r=Math.max(0,this.r-t.r),this.g=Math.max(0,this.g-t.g),this.b=Math.max(0,this.b-t.b),this}multiply(t){return this.r*=t.r,this.g*=t.g,this.b*=t.b,this}multiplyScalar(t){return this.r*=t,this.g*=t,this.b*=t,this}lerp(t,i){return this.r+=(t.r-this.r)*i,this.g+=(t.g-this.g)*i,this.b+=(t.b-this.b)*i,this}lerpColors(t,i,s){return this.r=t.r+(i.r-t.r)*s,this.g=t.g+(i.g-t.g)*s,this.b=t.b+(i.b-t.b)*s,this}lerpHSL(t,i){this.getHSL(as),t.getHSL(zc);const s=_l(as.h,zc.h,i),l=_l(as.s,zc.s,i),c=_l(as.l,zc.l,i);return this.setHSL(s,l,c),this}setFromVector3(t){return this.r=t.x,this.g=t.y,this.b=t.z,this}applyMatrix3(t){const i=this.r,s=this.g,l=this.b,c=t.elements;return this.r=c[0]*i+c[3]*s+c[6]*l,this.g=c[1]*i+c[4]*s+c[7]*l,this.b=c[2]*i+c[5]*s+c[8]*l,this}equals(t){return t.r===this.r&&t.g===this.g&&t.b===this.b}fromArray(t,i=0){return this.r=t[i],this.g=t[i+1],this.b=t[i+2],this}toArray(t=[],i=0){return t[i]=this.r,t[i+1]=this.g,t[i+2]=this.b,t}fromBufferAttribute(t,i){return this.r=t.getX(i),this.g=t.getY(i),this.b=t.getZ(i),this}toJSON(){return this.getHex()}*[Symbol.iterator](){yield this.r,yield this.g,yield this.b}}const Hn=new ue;ue.NAMES=Mx;class Hp{constructor(t,i=25e-5){this.isFogExp2=!0,this.name="",this.color=new ue(t),this.density=i}clone(){return new Hp(this.color,this.density)}toJSON(){return{type:"FogExp2",name:this.name,color:this.color.getHex(),density:this.density}}}class DE extends Mn{constructor(){super(),this.isScene=!0,this.type="Scene",this.background=null,this.environment=null,this.fog=null,this.backgroundBlurriness=0,this.backgroundIntensity=1,this.backgroundRotation=new ds,this.environmentIntensity=1,this.environmentRotation=new ds,this.overrideMaterial=null,typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}copy(t,i){return super.copy(t,i),t.background!==null&&(this.background=t.background.clone()),t.environment!==null&&(this.environment=t.environment.clone()),t.fog!==null&&(this.fog=t.fog.clone()),this.backgroundBlurriness=t.backgroundBlurriness,this.backgroundIntensity=t.backgroundIntensity,this.backgroundRotation.copy(t.backgroundRotation),this.environmentIntensity=t.environmentIntensity,this.environmentRotation.copy(t.environmentRotation),t.overrideMaterial!==null&&(this.overrideMaterial=t.overrideMaterial.clone()),this.matrixAutoUpdate=t.matrixAutoUpdate,this}toJSON(t){const i=super.toJSON(t);return this.fog!==null&&(i.object.fog=this.fog.toJSON()),this.backgroundBlurriness>0&&(i.object.backgroundBlurriness=this.backgroundBlurriness),this.backgroundIntensity!==1&&(i.object.backgroundIntensity=this.backgroundIntensity),i.object.backgroundRotation=this.backgroundRotation.toArray(),this.environmentIntensity!==1&&(i.object.environmentIntensity=this.environmentIntensity),i.object.environmentRotation=this.environmentRotation.toArray(),i}}const Pi=new V,ya=new V,td=new V,Sa=new V,zr=new V,Br=new V,sv=new V,ed=new V,nd=new V,id=new V,ad=new cn,sd=new cn,rd=new cn;class _i{constructor(t=new V,i=new V,s=new V){this.a=t,this.b=i,this.c=s}static getNormal(t,i,s,l){l.subVectors(s,i),Pi.subVectors(t,i),l.cross(Pi);const c=l.lengthSq();return c>0?l.multiplyScalar(1/Math.sqrt(c)):l.set(0,0,0)}static getBarycoord(t,i,s,l,c){Pi.subVectors(l,i),ya.subVectors(s,i),td.subVectors(t,i);const f=Pi.dot(Pi),p=Pi.dot(ya),m=Pi.dot(td),d=ya.dot(ya),_=ya.dot(td),v=f*d-p*p;if(v===0)return c.set(0,0,0),null;const g=1/v,S=(d*m-p*_)*g,E=(f*_-p*m)*g;return c.set(1-S-E,E,S)}static containsPoint(t,i,s,l){return this.getBarycoord(t,i,s,l,Sa)===null?!1:Sa.x>=0&&Sa.y>=0&&Sa.x+Sa.y<=1}static getInterpolation(t,i,s,l,c,f,p,m){return this.getBarycoord(t,i,s,l,Sa)===null?(m.x=0,m.y=0,"z"in m&&(m.z=0),"w"in m&&(m.w=0),null):(m.setScalar(0),m.addScaledVector(c,Sa.x),m.addScaledVector(f,Sa.y),m.addScaledVector(p,Sa.z),m)}static getInterpolatedAttribute(t,i,s,l,c,f){return ad.setScalar(0),sd.setScalar(0),rd.setScalar(0),ad.fromBufferAttribute(t,i),sd.fromBufferAttribute(t,s),rd.fromBufferAttribute(t,l),f.setScalar(0),f.addScaledVector(ad,c.x),f.addScaledVector(sd,c.y),f.addScaledVector(rd,c.z),f}static isFrontFacing(t,i,s,l){return Pi.subVectors(s,i),ya.subVectors(t,i),Pi.cross(ya).dot(l)<0}set(t,i,s){return this.a.copy(t),this.b.copy(i),this.c.copy(s),this}setFromPointsAndIndices(t,i,s,l){return this.a.copy(t[i]),this.b.copy(t[s]),this.c.copy(t[l]),this}setFromAttributeAndIndices(t,i,s,l){return this.a.fromBufferAttribute(t,i),this.b.fromBufferAttribute(t,s),this.c.fromBufferAttribute(t,l),this}clone(){return new this.constructor().copy(this)}copy(t){return this.a.copy(t.a),this.b.copy(t.b),this.c.copy(t.c),this}getArea(){return Pi.subVectors(this.c,this.b),ya.subVectors(this.a,this.b),Pi.cross(ya).length()*.5}getMidpoint(t){return t.addVectors(this.a,this.b).add(this.c).multiplyScalar(1/3)}getNormal(t){return _i.getNormal(this.a,this.b,this.c,t)}getPlane(t){return t.setFromCoplanarPoints(this.a,this.b,this.c)}getBarycoord(t,i){return _i.getBarycoord(t,this.a,this.b,this.c,i)}getInterpolation(t,i,s,l,c){return _i.getInterpolation(t,this.a,this.b,this.c,i,s,l,c)}containsPoint(t){return _i.containsPoint(t,this.a,this.b,this.c)}isFrontFacing(t){return _i.isFrontFacing(this.a,this.b,this.c,t)}intersectsBox(t){return t.intersectsTriangle(this)}closestPointToPoint(t,i){const s=this.a,l=this.b,c=this.c;let f,p;zr.subVectors(l,s),Br.subVectors(c,s),ed.subVectors(t,s);const m=zr.dot(ed),d=Br.dot(ed);if(m<=0&&d<=0)return i.copy(s);nd.subVectors(t,l);const _=zr.dot(nd),v=Br.dot(nd);if(_>=0&&v<=_)return i.copy(l);const g=m*v-_*d;if(g<=0&&m>=0&&_<=0)return f=m/(m-_),i.copy(s).addScaledVector(zr,f);id.subVectors(t,c);const S=zr.dot(id),E=Br.dot(id);if(E>=0&&S<=E)return i.copy(c);const A=S*d-m*E;if(A<=0&&d>=0&&E<=0)return p=d/(d-E),i.copy(s).addScaledVector(Br,p);const M=_*E-S*v;if(M<=0&&v-_>=0&&S-E>=0)return sv.subVectors(c,l),p=(v-_)/(v-_+(S-E)),i.copy(l).addScaledVector(sv,p);const y=1/(M+A+g);return f=A*y,p=g*y,i.copy(s).addScaledVector(zr,f).addScaledVector(Br,p)}equals(t){return t.a.equals(this.a)&&t.b.equals(this.b)&&t.c.equals(this.c)}}class Qs{constructor(t=new V(1/0,1/0,1/0),i=new V(-1/0,-1/0,-1/0)){this.isBox3=!0,this.min=t,this.max=i}set(t,i){return this.min.copy(t),this.max.copy(i),this}setFromArray(t){this.makeEmpty();for(let i=0,s=t.length;i<s;i+=3)this.expandByPoint(zi.fromArray(t,i));return this}setFromBufferAttribute(t){this.makeEmpty();for(let i=0,s=t.count;i<s;i++)this.expandByPoint(zi.fromBufferAttribute(t,i));return this}setFromPoints(t){this.makeEmpty();for(let i=0,s=t.length;i<s;i++)this.expandByPoint(t[i]);return this}setFromCenterAndSize(t,i){const s=zi.copy(i).multiplyScalar(.5);return this.min.copy(t).sub(s),this.max.copy(t).add(s),this}setFromObject(t,i=!1){return this.makeEmpty(),this.expandByObject(t,i)}clone(){return new this.constructor().copy(this)}copy(t){return this.min.copy(t.min),this.max.copy(t.max),this}makeEmpty(){return this.min.x=this.min.y=this.min.z=1/0,this.max.x=this.max.y=this.max.z=-1/0,this}isEmpty(){return this.max.x<this.min.x||this.max.y<this.min.y||this.max.z<this.min.z}getCenter(t){return this.isEmpty()?t.set(0,0,0):t.addVectors(this.min,this.max).multiplyScalar(.5)}getSize(t){return this.isEmpty()?t.set(0,0,0):t.subVectors(this.max,this.min)}expandByPoint(t){return this.min.min(t),this.max.max(t),this}expandByVector(t){return this.min.sub(t),this.max.add(t),this}expandByScalar(t){return this.min.addScalar(-t),this.max.addScalar(t),this}expandByObject(t,i=!1){t.updateWorldMatrix(!1,!1);const s=t.geometry;if(s!==void 0){const c=s.getAttribute("position");if(i===!0&&c!==void 0&&t.isInstancedMesh!==!0)for(let f=0,p=c.count;f<p;f++)t.isMesh===!0?t.getVertexPosition(f,zi):zi.fromBufferAttribute(c,f),zi.applyMatrix4(t.matrixWorld),this.expandByPoint(zi);else t.boundingBox!==void 0?(t.boundingBox===null&&t.computeBoundingBox(),Bc.copy(t.boundingBox)):(s.boundingBox===null&&s.computeBoundingBox(),Bc.copy(s.boundingBox)),Bc.applyMatrix4(t.matrixWorld),this.union(Bc)}const l=t.children;for(let c=0,f=l.length;c<f;c++)this.expandByObject(l[c],i);return this}containsPoint(t){return t.x>=this.min.x&&t.x<=this.max.x&&t.y>=this.min.y&&t.y<=this.max.y&&t.z>=this.min.z&&t.z<=this.max.z}containsBox(t){return this.min.x<=t.min.x&&t.max.x<=this.max.x&&this.min.y<=t.min.y&&t.max.y<=this.max.y&&this.min.z<=t.min.z&&t.max.z<=this.max.z}getParameter(t,i){return i.set((t.x-this.min.x)/(this.max.x-this.min.x),(t.y-this.min.y)/(this.max.y-this.min.y),(t.z-this.min.z)/(this.max.z-this.min.z))}intersectsBox(t){return t.max.x>=this.min.x&&t.min.x<=this.max.x&&t.max.y>=this.min.y&&t.min.y<=this.max.y&&t.max.z>=this.min.z&&t.min.z<=this.max.z}intersectsSphere(t){return this.clampPoint(t.center,zi),zi.distanceToSquared(t.center)<=t.radius*t.radius}intersectsPlane(t){let i,s;return t.normal.x>0?(i=t.normal.x*this.min.x,s=t.normal.x*this.max.x):(i=t.normal.x*this.max.x,s=t.normal.x*this.min.x),t.normal.y>0?(i+=t.normal.y*this.min.y,s+=t.normal.y*this.max.y):(i+=t.normal.y*this.max.y,s+=t.normal.y*this.min.y),t.normal.z>0?(i+=t.normal.z*this.min.z,s+=t.normal.z*this.max.z):(i+=t.normal.z*this.max.z,s+=t.normal.z*this.min.z),i<=-t.constant&&s>=-t.constant}intersectsTriangle(t){if(this.isEmpty())return!1;this.getCenter(nl),Ic.subVectors(this.max,nl),Ir.subVectors(t.a,nl),Fr.subVectors(t.b,nl),Hr.subVectors(t.c,nl),ss.subVectors(Fr,Ir),rs.subVectors(Hr,Fr),zs.subVectors(Ir,Hr);let i=[0,-ss.z,ss.y,0,-rs.z,rs.y,0,-zs.z,zs.y,ss.z,0,-ss.x,rs.z,0,-rs.x,zs.z,0,-zs.x,-ss.y,ss.x,0,-rs.y,rs.x,0,-zs.y,zs.x,0];return!od(i,Ir,Fr,Hr,Ic)||(i=[1,0,0,0,1,0,0,0,1],!od(i,Ir,Fr,Hr,Ic))?!1:(Fc.crossVectors(ss,rs),i=[Fc.x,Fc.y,Fc.z],od(i,Ir,Fr,Hr,Ic))}clampPoint(t,i){return i.copy(t).clamp(this.min,this.max)}distanceToPoint(t){return this.clampPoint(t,zi).distanceTo(t)}getBoundingSphere(t){return this.isEmpty()?t.makeEmpty():(this.getCenter(t.center),t.radius=this.getSize(zi).length()*.5),t}intersect(t){return this.min.max(t.min),this.max.min(t.max),this.isEmpty()&&this.makeEmpty(),this}union(t){return this.min.min(t.min),this.max.max(t.max),this}applyMatrix4(t){return this.isEmpty()?this:(Ma[0].set(this.min.x,this.min.y,this.min.z).applyMatrix4(t),Ma[1].set(this.min.x,this.min.y,this.max.z).applyMatrix4(t),Ma[2].set(this.min.x,this.max.y,this.min.z).applyMatrix4(t),Ma[3].set(this.min.x,this.max.y,this.max.z).applyMatrix4(t),Ma[4].set(this.max.x,this.min.y,this.min.z).applyMatrix4(t),Ma[5].set(this.max.x,this.min.y,this.max.z).applyMatrix4(t),Ma[6].set(this.max.x,this.max.y,this.min.z).applyMatrix4(t),Ma[7].set(this.max.x,this.max.y,this.max.z).applyMatrix4(t),this.setFromPoints(Ma),this)}translate(t){return this.min.add(t),this.max.add(t),this}equals(t){return t.min.equals(this.min)&&t.max.equals(this.max)}toJSON(){return{min:this.min.toArray(),max:this.max.toArray()}}fromJSON(t){return this.min.fromArray(t.min),this.max.fromArray(t.max),this}}const Ma=[new V,new V,new V,new V,new V,new V,new V,new V],zi=new V,Bc=new Qs,Ir=new V,Fr=new V,Hr=new V,ss=new V,rs=new V,zs=new V,nl=new V,Ic=new V,Fc=new V,Bs=new V;function od(r,t,i,s,l){for(let c=0,f=r.length-3;c<=f;c+=3){Bs.fromArray(r,c);const p=l.x*Math.abs(Bs.x)+l.y*Math.abs(Bs.y)+l.z*Math.abs(Bs.z),m=t.dot(Bs),d=i.dot(Bs),_=s.dot(Bs);if(Math.max(-Math.max(m,d,_),Math.min(m,d,_))>p)return!1}return!0}const Sn=new V,Hc=new fe;let UE=0;class vi extends Ks{constructor(t,i,s=!1){if(super(),Array.isArray(t))throw new TypeError("THREE.BufferAttribute: array should be a Typed Array.");this.isBufferAttribute=!0,Object.defineProperty(this,"id",{value:UE++}),this.name="",this.array=t,this.itemSize=i,this.count=t!==void 0?t.length/i:0,this.normalized=s,this.usage=_p,this.updateRanges=[],this.gpuType=Ii,this.version=0}onUploadCallback(){}set needsUpdate(t){t===!0&&this.version++}setUsage(t){return this.usage=t,this}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}copy(t){return this.name=t.name,this.array=new t.array.constructor(t.array),this.itemSize=t.itemSize,this.count=t.count,this.normalized=t.normalized,this.usage=t.usage,this.gpuType=t.gpuType,this}copyAt(t,i,s){t*=this.itemSize,s*=i.itemSize;for(let l=0,c=this.itemSize;l<c;l++)this.array[t+l]=i.array[s+l];return this}copyArray(t){return this.array.set(t),this}applyMatrix3(t){if(this.itemSize===2)for(let i=0,s=this.count;i<s;i++)Hc.fromBufferAttribute(this,i),Hc.applyMatrix3(t),this.setXY(i,Hc.x,Hc.y);else if(this.itemSize===3)for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.applyMatrix3(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}applyMatrix4(t){for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.applyMatrix4(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}applyNormalMatrix(t){for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.applyNormalMatrix(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}transformDirection(t){for(let i=0,s=this.count;i<s;i++)Sn.fromBufferAttribute(this,i),Sn.transformDirection(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}set(t,i=0){return this.array.set(t,i),this}getComponent(t,i){let s=this.array[t*this.itemSize+i];return this.normalized&&(s=Bi(s,this.array)),s}setComponent(t,i,s){return this.normalized&&(s=je(s,this.array)),this.array[t*this.itemSize+i]=s,this}getX(t){let i=this.array[t*this.itemSize];return this.normalized&&(i=Bi(i,this.array)),i}setX(t,i){return this.normalized&&(i=je(i,this.array)),this.array[t*this.itemSize]=i,this}getY(t){let i=this.array[t*this.itemSize+1];return this.normalized&&(i=Bi(i,this.array)),i}setY(t,i){return this.normalized&&(i=je(i,this.array)),this.array[t*this.itemSize+1]=i,this}getZ(t){let i=this.array[t*this.itemSize+2];return this.normalized&&(i=Bi(i,this.array)),i}setZ(t,i){return this.normalized&&(i=je(i,this.array)),this.array[t*this.itemSize+2]=i,this}getW(t){let i=this.array[t*this.itemSize+3];return this.normalized&&(i=Bi(i,this.array)),i}setW(t,i){return this.normalized&&(i=je(i,this.array)),this.array[t*this.itemSize+3]=i,this}setXY(t,i,s){return t*=this.itemSize,this.normalized&&(i=je(i,this.array),s=je(s,this.array)),this.array[t+0]=i,this.array[t+1]=s,this}setXYZ(t,i,s,l){return t*=this.itemSize,this.normalized&&(i=je(i,this.array),s=je(s,this.array),l=je(l,this.array)),this.array[t+0]=i,this.array[t+1]=s,this.array[t+2]=l,this}setXYZW(t,i,s,l,c){return t*=this.itemSize,this.normalized&&(i=je(i,this.array),s=je(s,this.array),l=je(l,this.array),c=je(c,this.array)),this.array[t+0]=i,this.array[t+1]=s,this.array[t+2]=l,this.array[t+3]=c,this}onUpload(t){return this.onUploadCallback=t,this}clone(){return new this.constructor(this.array,this.itemSize).copy(this)}toJSON(){const t={itemSize:this.itemSize,type:this.array.constructor.name,array:Array.from(this.array),normalized:this.normalized};return this.name!==""&&(t.name=this.name),this.usage!==_p&&(t.usage=this.usage),t}dispose(){this.dispatchEvent({type:"dispose"})}}class Ex extends vi{constructor(t,i,s){super(new Uint16Array(t),i,s)}}class bx extends vi{constructor(t,i,s){super(new Uint32Array(t),i,s)}}class En extends vi{constructor(t,i,s){super(new Float32Array(t),i,s)}}const NE=new Qs,il=new V,ld=new V;class Js{constructor(t=new V,i=-1){this.isSphere=!0,this.center=t,this.radius=i}set(t,i){return this.center.copy(t),this.radius=i,this}setFromPoints(t,i){const s=this.center;i!==void 0?s.copy(i):NE.setFromPoints(t).getCenter(s);let l=0;for(let c=0,f=t.length;c<f;c++)l=Math.max(l,s.distanceToSquared(t[c]));return this.radius=Math.sqrt(l),this}copy(t){return this.center.copy(t.center),this.radius=t.radius,this}isEmpty(){return this.radius<0}makeEmpty(){return this.center.set(0,0,0),this.radius=-1,this}containsPoint(t){return t.distanceToSquared(this.center)<=this.radius*this.radius}distanceToPoint(t){return t.distanceTo(this.center)-this.radius}intersectsSphere(t){const i=this.radius+t.radius;return t.center.distanceToSquared(this.center)<=i*i}intersectsBox(t){return t.intersectsSphere(this)}intersectsPlane(t){return Math.abs(t.distanceToPoint(this.center))<=this.radius}clampPoint(t,i){const s=this.center.distanceToSquared(t);return i.copy(t),s>this.radius*this.radius&&(i.sub(this.center).normalize(),i.multiplyScalar(this.radius).add(this.center)),i}getBoundingBox(t){return this.isEmpty()?(t.makeEmpty(),t):(t.set(this.center,this.center),t.expandByScalar(this.radius),t)}applyMatrix4(t){return this.center.applyMatrix4(t),this.radius=this.radius*t.getMaxScaleOnAxis(),this}translate(t){return this.center.add(t),this}expandByPoint(t){if(this.isEmpty())return this.center.copy(t),this.radius=0,this;il.subVectors(t,this.center);const i=il.lengthSq();if(i>this.radius*this.radius){const s=Math.sqrt(i),l=(s-this.radius)*.5;this.center.addScaledVector(il,l/s),this.radius+=l}return this}union(t){return t.isEmpty()?this:this.isEmpty()?(this.copy(t),this):(this.center.equals(t.center)===!0?this.radius=Math.max(this.radius,t.radius):(ld.subVectors(t.center,this.center).setLength(t.radius),this.expandByPoint(il.copy(t.center).add(ld)),this.expandByPoint(il.copy(t.center).sub(ld))),this)}equals(t){return t.center.equals(this.center)&&t.radius===this.radius}clone(){return new this.constructor().copy(this)}toJSON(){return{radius:this.radius,center:this.center.toArray()}}fromJSON(t){return this.radius=t.radius,this.center.fromArray(t.center),this}}let LE=0;const Ri=new Fe,cd=new Mn,Gr=new V,di=new Qs,al=new Qs,wn=new V;class hn extends Ks{constructor(){super(),this.isBufferGeometry=!0,Object.defineProperty(this,"id",{value:LE++}),this.uuid=Aa(),this.name="",this.type="BufferGeometry",this.index=null,this.indirect=null,this.indirectOffset=0,this.attributes={},this.morphAttributes={},this.morphTargetsRelative=!1,this.groups=[],this.boundingBox=null,this.boundingSphere=null,this.drawRange={start:0,count:1/0},this.userData={},this._transformed=!1}getIndex(){return this.index}setIndex(t){return Array.isArray(t)?this.index=new(Q1(t)?bx:Ex)(t,1):this.index=t,this}setIndirect(t,i=0){return this.indirect=t,this.indirectOffset=i,this}getIndirect(){return this.indirect}getAttribute(t){return this.attributes[t]}setAttribute(t,i){return this.attributes[t]=i,this}deleteAttribute(t){return delete this.attributes[t],this}hasAttribute(t){return this.attributes[t]!==void 0}addGroup(t,i,s=0){this.groups.push({start:t,count:i,materialIndex:s})}clearGroups(){this.groups=[]}setDrawRange(t,i){this.drawRange.start=t,this.drawRange.count=i}applyMatrix4(t){const i=this.attributes.position;i!==void 0&&(i.applyMatrix4(t),i.needsUpdate=!0);const s=this.attributes.normal;if(s!==void 0){const c=new de().getNormalMatrix(t);s.applyNormalMatrix(c),s.needsUpdate=!0}const l=this.attributes.tangent;return l!==void 0&&(l.transformDirection(t),l.needsUpdate=!0),this.boundingBox!==null&&this.computeBoundingBox(),this.boundingSphere!==null&&this.computeBoundingSphere(),this._transformed=!0,this}applyQuaternion(t){return Ri.makeRotationFromQuaternion(t),this.applyMatrix4(Ri),this}rotateX(t){return Ri.makeRotationX(t),this.applyMatrix4(Ri),this}rotateY(t){return Ri.makeRotationY(t),this.applyMatrix4(Ri),this}rotateZ(t){return Ri.makeRotationZ(t),this.applyMatrix4(Ri),this}translate(t,i,s){return Ri.makeTranslation(t,i,s),this.applyMatrix4(Ri),this}scale(t,i,s){return Ri.makeScale(t,i,s),this.applyMatrix4(Ri),this}lookAt(t){return cd.lookAt(t),cd.updateMatrix(),this.applyMatrix4(cd.matrix),this}center(){return this.computeBoundingBox(),this.boundingBox.getCenter(Gr).negate(),this.translate(Gr.x,Gr.y,Gr.z),this}setFromPoints(t){const i=this.getAttribute("position");if(i===void 0){const s=[];for(let l=0,c=t.length;l<c;l++){const f=t[l];s.push(f.x,f.y,f.z||0)}this.setAttribute("position",new En(s,3))}else{const s=Math.min(t.length,i.count);for(let l=0;l<s;l++){const c=t[l];i.setXYZ(l,c.x,c.y,c.z||0)}t.length>i.count&&re("BufferGeometry: Buffer size too small for points data. Use .dispose() and create a new geometry."),i.needsUpdate=!0}return this}computeBoundingBox(){this.boundingBox===null&&(this.boundingBox=new Qs);const t=this.attributes.position,i=this.morphAttributes.position;if(t&&t.isGLBufferAttribute){Ue("BufferGeometry.computeBoundingBox(): GLBufferAttribute requires a manual bounding box.",this),this.boundingBox.set(new V(-1/0,-1/0,-1/0),new V(1/0,1/0,1/0));return}if(t!==void 0){if(this.boundingBox.setFromBufferAttribute(t),i)for(let s=0,l=i.length;s<l;s++){const c=i[s];di.setFromBufferAttribute(c),this.morphTargetsRelative?(wn.addVectors(this.boundingBox.min,di.min),this.boundingBox.expandByPoint(wn),wn.addVectors(this.boundingBox.max,di.max),this.boundingBox.expandByPoint(wn)):(this.boundingBox.expandByPoint(di.min),this.boundingBox.expandByPoint(di.max))}}else this.boundingBox.makeEmpty();(isNaN(this.boundingBox.min.x)||isNaN(this.boundingBox.min.y)||isNaN(this.boundingBox.min.z))&&Ue('BufferGeometry.computeBoundingBox(): Computed min/max have NaN values. The "position" attribute is likely to have NaN values.',this)}computeBoundingSphere(){this.boundingSphere===null&&(this.boundingSphere=new Js);const t=this.attributes.position,i=this.morphAttributes.position;if(t&&t.isGLBufferAttribute){Ue("BufferGeometry.computeBoundingSphere(): GLBufferAttribute requires a manual bounding sphere.",this),this.boundingSphere.set(new V,1/0);return}if(t){const s=this.boundingSphere.center;if(di.setFromBufferAttribute(t),i)for(let c=0,f=i.length;c<f;c++){const p=i[c];al.setFromBufferAttribute(p),this.morphTargetsRelative?(wn.addVectors(di.min,al.min),di.expandByPoint(wn),wn.addVectors(di.max,al.max),di.expandByPoint(wn)):(di.expandByPoint(al.min),di.expandByPoint(al.max))}di.getCenter(s);let l=0;for(let c=0,f=t.count;c<f;c++)wn.fromBufferAttribute(t,c),l=Math.max(l,s.distanceToSquared(wn));if(i)for(let c=0,f=i.length;c<f;c++){const p=i[c],m=this.morphTargetsRelative;for(let d=0,_=p.count;d<_;d++)wn.fromBufferAttribute(p,d),m&&(Gr.fromBufferAttribute(t,d),wn.add(Gr)),l=Math.max(l,s.distanceToSquared(wn))}this.boundingSphere.radius=Math.sqrt(l),isNaN(this.boundingSphere.radius)&&Ue('BufferGeometry.computeBoundingSphere(): Computed radius is NaN. The "position" attribute is likely to have NaN values.',this)}}computeTangents(){const t=this.index,i=this.attributes;if(t===null||i.position===void 0||i.normal===void 0||i.uv===void 0){Ue("BufferGeometry: .computeTangents() failed. Missing required attributes (index, position, normal or uv)");return}const s=i.position,l=i.normal,c=i.uv;let f=this.getAttribute("tangent");(f===void 0||f.count!==s.count)&&(f=new vi(new Float32Array(4*s.count),4),this.setAttribute("tangent",f));const p=[],m=[];for(let T=0;T<s.count;T++)p[T]=new V,m[T]=new V;const d=new V,_=new V,v=new V,g=new fe,S=new fe,E=new fe,A=new V,M=new V;function y(T,L,j){d.fromBufferAttribute(s,T),_.fromBufferAttribute(s,L),v.fromBufferAttribute(s,j),g.fromBufferAttribute(c,T),S.fromBufferAttribute(c,L),E.fromBufferAttribute(c,j),_.sub(d),v.sub(d),S.sub(g),E.sub(g);const k=1/(S.x*E.y-E.x*S.y);isFinite(k)&&(A.copy(_).multiplyScalar(E.y).addScaledVector(v,-S.y).multiplyScalar(k),M.copy(v).multiplyScalar(S.x).addScaledVector(_,-E.x).multiplyScalar(k),p[T].add(A),p[L].add(A),p[j].add(A),m[T].add(M),m[L].add(M),m[j].add(M))}let B=this.groups;B.length===0&&(B=[{start:0,count:t.count}]);for(let T=0,L=B.length;T<L;++T){const j=B[T],k=j.start,J=j.count;for(let ut=k,gt=k+J;ut<gt;ut+=3)y(t.getX(ut+0),t.getX(ut+1),t.getX(ut+2))}const F=new V,w=new V,I=new V,O=new V;function D(T){I.fromBufferAttribute(l,T),O.copy(I);const L=p[T];F.copy(L),F.sub(I.multiplyScalar(I.dot(L))).normalize(),w.crossVectors(O,L);const k=w.dot(m[T])<0?-1:1;f.setXYZW(T,F.x,F.y,F.z,k)}for(let T=0,L=B.length;T<L;++T){const j=B[T],k=j.start,J=j.count;for(let ut=k,gt=k+J;ut<gt;ut+=3)D(t.getX(ut+0)),D(t.getX(ut+1)),D(t.getX(ut+2))}this._transformed=!0}computeVertexNormals(){const t=this.index,i=this.getAttribute("position");if(i!==void 0){let s=this.getAttribute("normal");if(s===void 0||s.count!==i.count)s=new vi(new Float32Array(i.count*3),3),this.setAttribute("normal",s);else for(let g=0,S=s.count;g<S;g++)s.setXYZ(g,0,0,0);const l=new V,c=new V,f=new V,p=new V,m=new V,d=new V,_=new V,v=new V;if(t)for(let g=0,S=t.count;g<S;g+=3){const E=t.getX(g+0),A=t.getX(g+1),M=t.getX(g+2);l.fromBufferAttribute(i,E),c.fromBufferAttribute(i,A),f.fromBufferAttribute(i,M),_.subVectors(f,c),v.subVectors(l,c),_.cross(v),p.fromBufferAttribute(s,E),m.fromBufferAttribute(s,A),d.fromBufferAttribute(s,M),p.add(_),m.add(_),d.add(_),s.setXYZ(E,p.x,p.y,p.z),s.setXYZ(A,m.x,m.y,m.z),s.setXYZ(M,d.x,d.y,d.z)}else for(let g=0,S=i.count;g<S;g+=3)l.fromBufferAttribute(i,g+0),c.fromBufferAttribute(i,g+1),f.fromBufferAttribute(i,g+2),_.subVectors(f,c),v.subVectors(l,c),_.cross(v),s.setXYZ(g+0,_.x,_.y,_.z),s.setXYZ(g+1,_.x,_.y,_.z),s.setXYZ(g+2,_.x,_.y,_.z);this.normalizeNormals(),s.needsUpdate=!0}}normalizeNormals(){const t=this.attributes.normal;for(let i=0,s=t.count;i<s;i++)wn.fromBufferAttribute(t,i),wn.normalize(),t.setXYZ(i,wn.x,wn.y,wn.z)}toNonIndexed(){function t(p,m){const d=p.array,_=p.itemSize,v=p.normalized,g=new d.constructor(m.length*_);let S=0,E=0;for(let A=0,M=m.length;A<M;A++){p.isInterleavedBufferAttribute?S=m[A]*p.data.stride+p.offset:S=m[A]*_;for(let y=0;y<_;y++)g[E++]=d[S++]}return new vi(g,_,v)}if(this.index===null)return re("BufferGeometry.toNonIndexed(): BufferGeometry is already non-indexed."),this;const i=new hn,s=this.index.array,l=this.attributes;for(const p in l){const m=l[p],d=t(m,s);i.setAttribute(p,d)}const c=this.morphAttributes;for(const p in c){const m=[],d=c[p];for(let _=0,v=d.length;_<v;_++){const g=d[_],S=t(g,s);m.push(S)}i.morphAttributes[p]=m}i.morphTargetsRelative=this.morphTargetsRelative;const f=this.groups;for(let p=0,m=f.length;p<m;p++){const d=f[p];i.addGroup(d.start,d.count,d.materialIndex)}return i}toJSON(){const t={metadata:{version:4.7,type:"BufferGeometry",generator:"BufferGeometry.toJSON"}};if(t.uuid=this.uuid,t.type=this.parameters!==void 0&&this._transformed===!0?"BufferGeometry":this.type,this.name!==""&&(t.name=this.name),Object.keys(this.userData).length>0&&(t.userData=this.userData),this.parameters!==void 0&&this._transformed!==!0){const m=this.parameters;for(const d in m)m[d]!==void 0&&(t[d]=m[d]);return t}t.data={attributes:{}};const i=this.index;i!==null&&(t.data.index={type:i.array.constructor.name,array:Array.prototype.slice.call(i.array)});const s=this.attributes;for(const m in s){const d=s[m];t.data.attributes[m]=d.toJSON(t.data)}const l={};let c=!1;for(const m in this.morphAttributes){const d=this.morphAttributes[m],_=[];for(let v=0,g=d.length;v<g;v++){const S=d[v];_.push(S.toJSON(t.data))}_.length>0&&(l[m]=_,c=!0)}c&&(t.data.morphAttributes=l,t.data.morphTargetsRelative=this.morphTargetsRelative);const f=this.groups;f.length>0&&(t.data.groups=JSON.parse(JSON.stringify(f)));const p=this.boundingSphere;return p!==null&&(t.data.boundingSphere=p.toJSON()),t}clone(){return new this.constructor().copy(this)}copy(t){this.index=null,this.attributes={},this.morphAttributes={},this.groups=[],this.boundingBox=null,this.boundingSphere=null;const i={};this.name=t.name;const s=t.index;s!==null&&this.setIndex(s.clone());const l=t.attributes;for(const d in l){const _=l[d];this.setAttribute(d,_.clone(i))}const c=t.morphAttributes;for(const d in c){const _=[],v=c[d];for(let g=0,S=v.length;g<S;g++)_.push(v[g].clone(i));this.morphAttributes[d]=_}this.morphTargetsRelative=t.morphTargetsRelative;const f=t.groups;for(let d=0,_=f.length;d<_;d++){const v=f[d];this.addGroup(v.start,v.count,v.materialIndex)}const p=t.boundingBox;p!==null&&(this.boundingBox=p.clone());const m=t.boundingSphere;return m!==null&&(this.boundingSphere=m.clone()),this.drawRange.start=t.drawRange.start,this.drawRange.count=t.drawRange.count,this.userData=t.userData,this._transformed=t._transformed,this}dispose(){this.dispatchEvent({type:"dispose"})}}class OE{constructor(t,i){this.isInterleavedBuffer=!0,this.array=t,this.stride=i,this.count=t!==void 0?t.length/i:0,this.usage=_p,this.updateRanges=[],this.version=0,this.uuid=Aa()}onUploadCallback(){}set needsUpdate(t){t===!0&&this.version++}setUsage(t){return this.usage=t,this}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}copy(t){return this.array=new t.array.constructor(t.array),this.count=t.count,this.stride=t.stride,this.usage=t.usage,this}copyAt(t,i,s){t*=this.stride,s*=i.stride;for(let l=0,c=this.stride;l<c;l++)this.array[t+l]=i.array[s+l];return this}set(t,i=0){return this.array.set(t,i),this}clone(t){t.arrayBuffers===void 0&&(t.arrayBuffers={}),this.array.buffer._uuid===void 0&&(this.array.buffer._uuid=Aa()),t.arrayBuffers[this.array.buffer._uuid]===void 0&&(t.arrayBuffers[this.array.buffer._uuid]=this.array.slice(0).buffer);const i=new this.array.constructor(t.arrayBuffers[this.array.buffer._uuid]),s=new this.constructor(i,this.stride);return s.setUsage(this.usage),s}onUpload(t){return this.onUploadCallback=t,this}toJSON(t){return t.arrayBuffers===void 0&&(t.arrayBuffers={}),this.array.buffer._uuid===void 0&&(this.array.buffer._uuid=Aa()),t.arrayBuffers[this.array.buffer._uuid]===void 0&&(t.arrayBuffers[this.array.buffer._uuid]=Array.from(new Uint32Array(this.array.buffer))),{uuid:this.uuid,buffer:this.array.buffer._uuid,type:this.array.constructor.name,stride:this.stride}}}const Xn=new V;class bu{constructor(t,i,s,l=!1){this.isInterleavedBufferAttribute=!0,this.name="",this.data=t,this.itemSize=i,this.offset=s,this.normalized=l}get count(){return this.data.count}get array(){return this.data.array}set needsUpdate(t){this.data.needsUpdate=t}applyMatrix4(t){for(let i=0,s=this.data.count;i<s;i++)Xn.fromBufferAttribute(this,i),Xn.applyMatrix4(t),this.setXYZ(i,Xn.x,Xn.y,Xn.z);return this}applyNormalMatrix(t){for(let i=0,s=this.count;i<s;i++)Xn.fromBufferAttribute(this,i),Xn.applyNormalMatrix(t),this.setXYZ(i,Xn.x,Xn.y,Xn.z);return this}transformDirection(t){for(let i=0,s=this.count;i<s;i++)Xn.fromBufferAttribute(this,i),Xn.transformDirection(t),this.setXYZ(i,Xn.x,Xn.y,Xn.z);return this}getComponent(t,i){let s=this.array[t*this.data.stride+this.offset+i];return this.normalized&&(s=Bi(s,this.array)),s}setComponent(t,i,s){return this.normalized&&(s=je(s,this.array)),this.data.array[t*this.data.stride+this.offset+i]=s,this}setX(t,i){return this.normalized&&(i=je(i,this.array)),this.data.array[t*this.data.stride+this.offset]=i,this}setY(t,i){return this.normalized&&(i=je(i,this.array)),this.data.array[t*this.data.stride+this.offset+1]=i,this}setZ(t,i){return this.normalized&&(i=je(i,this.array)),this.data.array[t*this.data.stride+this.offset+2]=i,this}setW(t,i){return this.normalized&&(i=je(i,this.array)),this.data.array[t*this.data.stride+this.offset+3]=i,this}getX(t){let i=this.data.array[t*this.data.stride+this.offset];return this.normalized&&(i=Bi(i,this.array)),i}getY(t){let i=this.data.array[t*this.data.stride+this.offset+1];return this.normalized&&(i=Bi(i,this.array)),i}getZ(t){let i=this.data.array[t*this.data.stride+this.offset+2];return this.normalized&&(i=Bi(i,this.array)),i}getW(t){let i=this.data.array[t*this.data.stride+this.offset+3];return this.normalized&&(i=Bi(i,this.array)),i}setXY(t,i,s){return t=t*this.data.stride+this.offset,this.normalized&&(i=je(i,this.array),s=je(s,this.array)),this.data.array[t+0]=i,this.data.array[t+1]=s,this}setXYZ(t,i,s,l){return t=t*this.data.stride+this.offset,this.normalized&&(i=je(i,this.array),s=je(s,this.array),l=je(l,this.array)),this.data.array[t+0]=i,this.data.array[t+1]=s,this.data.array[t+2]=l,this}setXYZW(t,i,s,l,c){return t=t*this.data.stride+this.offset,this.normalized&&(i=je(i,this.array),s=je(s,this.array),l=je(l,this.array),c=je(c,this.array)),this.data.array[t+0]=i,this.data.array[t+1]=s,this.data.array[t+2]=l,this.data.array[t+3]=c,this}clone(t){if(t===void 0){Eu("InterleavedBufferAttribute.clone(): Cloning an interleaved buffer attribute will de-interleave buffer data.");const i=[];for(let s=0;s<this.count;s++){const l=s*this.data.stride+this.offset;for(let c=0;c<this.itemSize;c++)i.push(this.data.array[l+c])}return new vi(new this.array.constructor(i),this.itemSize,this.normalized)}else return t.interleavedBuffers===void 0&&(t.interleavedBuffers={}),t.interleavedBuffers[this.data.uuid]===void 0&&(t.interleavedBuffers[this.data.uuid]=this.data.clone(t)),new bu(t.interleavedBuffers[this.data.uuid],this.itemSize,this.offset,this.normalized)}toJSON(t){if(t===void 0){Eu("InterleavedBufferAttribute.toJSON(): Serializing an interleaved buffer attribute will de-interleave buffer data.");const i=[];for(let s=0;s<this.count;s++){const l=s*this.data.stride+this.offset;for(let c=0;c<this.itemSize;c++)i.push(this.data.array[l+c])}return{itemSize:this.itemSize,type:this.array.constructor.name,array:i,normalized:this.normalized}}else return t.interleavedBuffers===void 0&&(t.interleavedBuffers={}),t.interleavedBuffers[this.data.uuid]===void 0&&(t.interleavedBuffers[this.data.uuid]=this.data.toJSON(t)),{isInterleavedBufferAttribute:!0,itemSize:this.itemSize,data:this.data.uuid,offset:this.offset,normalized:this.normalized}}}let PE=0;class ps extends Ks{constructor(){super(),this.isMaterial=!0,Object.defineProperty(this,"id",{value:PE++}),this.uuid=Aa(),this.name="",this.type="Material",this.blending=Qr,this.side=hs,this.vertexColors=!1,this.opacity=1,this.transparent=!1,this.alphaHash=!1,this.blendSrc=wd,this.blendDst=Dd,this.blendEquation=Vs,this.blendSrcAlpha=null,this.blendDstAlpha=null,this.blendEquationAlpha=null,this.blendColor=new ue(0,0,0),this.blendAlpha=0,this.depthFunc=eo,this.depthTest=!0,this.depthWrite=!0,this.stencilWriteMask=255,this.stencilFunc=W_,this.stencilRef=0,this.stencilFuncMask=255,this.stencilFail=Ur,this.stencilZFail=Ur,this.stencilZPass=Ur,this.stencilWrite=!1,this.clippingPlanes=null,this.clipIntersection=!1,this.clipShadows=!1,this.shadowSide=null,this.colorWrite=!0,this.precision=null,this.polygonOffset=!1,this.polygonOffsetFactor=0,this.polygonOffsetUnits=0,this.dithering=!1,this.alphaToCoverage=!1,this.premultipliedAlpha=!1,this.forceSinglePass=!1,this.allowOverride=!0,this.visible=!0,this.toneMapped=!0,this.userData={},this.version=0,this._alphaTest=0}get alphaTest(){return this._alphaTest}set alphaTest(t){this._alphaTest>0!=t>0&&this.version++,this._alphaTest=t}onBeforeRender(){}onBeforeCompile(){}customProgramCacheKey(){return this.onBeforeCompile.toString()}setValues(t){if(t!==void 0)for(const i in t){const s=t[i];if(s===void 0){re(`Material: parameter '${i}' has value of undefined.`);continue}const l=this[i];if(l===void 0){re(`Material: '${i}' is not a property of THREE.${this.type}.`);continue}l&&l.isColor?l.set(s):l&&l.isVector2&&s&&s.isVector2||l&&l.isEuler&&s&&s.isEuler||l&&l.isVector3&&s&&s.isVector3?l.copy(s):this[i]=s}}toJSON(t){const i=t===void 0||typeof t=="string";i&&(t={textures:{},images:{}});const s={metadata:{version:4.7,type:"Material",generator:"Material.toJSON"}};s.uuid=this.uuid,s.type=this.type,this.name!==""&&(s.name=this.name),this.color&&this.color.isColor&&(s.color=this.color.getHex()),this.roughness!==void 0&&(s.roughness=this.roughness),this.metalness!==void 0&&(s.metalness=this.metalness),this.sheen!==void 0&&(s.sheen=this.sheen),this.sheenColor&&this.sheenColor.isColor&&(s.sheenColor=this.sheenColor.getHex()),this.sheenRoughness!==void 0&&(s.sheenRoughness=this.sheenRoughness),this.emissive&&this.emissive.isColor&&(s.emissive=this.emissive.getHex()),this.emissiveIntensity!==void 0&&this.emissiveIntensity!==1&&(s.emissiveIntensity=this.emissiveIntensity),this.specular&&this.specular.isColor&&(s.specular=this.specular.getHex()),this.specularIntensity!==void 0&&(s.specularIntensity=this.specularIntensity),this.specularColor&&this.specularColor.isColor&&(s.specularColor=this.specularColor.getHex()),this.shininess!==void 0&&(s.shininess=this.shininess),this.clearcoat!==void 0&&(s.clearcoat=this.clearcoat),this.clearcoatRoughness!==void 0&&(s.clearcoatRoughness=this.clearcoatRoughness),this.clearcoatMap&&this.clearcoatMap.isTexture&&(s.clearcoatMap=this.clearcoatMap.toJSON(t).uuid),this.clearcoatRoughnessMap&&this.clearcoatRoughnessMap.isTexture&&(s.clearcoatRoughnessMap=this.clearcoatRoughnessMap.toJSON(t).uuid),this.clearcoatNormalMap&&this.clearcoatNormalMap.isTexture&&(s.clearcoatNormalMap=this.clearcoatNormalMap.toJSON(t).uuid,s.clearcoatNormalScale=this.clearcoatNormalScale.toArray()),this.sheenColorMap&&this.sheenColorMap.isTexture&&(s.sheenColorMap=this.sheenColorMap.toJSON(t).uuid),this.sheenRoughnessMap&&this.sheenRoughnessMap.isTexture&&(s.sheenRoughnessMap=this.sheenRoughnessMap.toJSON(t).uuid),this.dispersion!==void 0&&(s.dispersion=this.dispersion),this.iridescence!==void 0&&(s.iridescence=this.iridescence),this.iridescenceIOR!==void 0&&(s.iridescenceIOR=this.iridescenceIOR),this.iridescenceThicknessRange!==void 0&&(s.iridescenceThicknessRange=this.iridescenceThicknessRange),this.iridescenceMap&&this.iridescenceMap.isTexture&&(s.iridescenceMap=this.iridescenceMap.toJSON(t).uuid),this.iridescenceThicknessMap&&this.iridescenceThicknessMap.isTexture&&(s.iridescenceThicknessMap=this.iridescenceThicknessMap.toJSON(t).uuid),this.anisotropy!==void 0&&(s.anisotropy=this.anisotropy),this.anisotropyRotation!==void 0&&(s.anisotropyRotation=this.anisotropyRotation),this.anisotropyMap&&this.anisotropyMap.isTexture&&(s.anisotropyMap=this.anisotropyMap.toJSON(t).uuid),this.map&&this.map.isTexture&&(s.map=this.map.toJSON(t).uuid),this.matcap&&this.matcap.isTexture&&(s.matcap=this.matcap.toJSON(t).uuid),this.alphaMap&&this.alphaMap.isTexture&&(s.alphaMap=this.alphaMap.toJSON(t).uuid),this.lightMap&&this.lightMap.isTexture&&(s.lightMap=this.lightMap.toJSON(t).uuid,s.lightMapIntensity=this.lightMapIntensity),this.aoMap&&this.aoMap.isTexture&&(s.aoMap=this.aoMap.toJSON(t).uuid,s.aoMapIntensity=this.aoMapIntensity),this.bumpMap&&this.bumpMap.isTexture&&(s.bumpMap=this.bumpMap.toJSON(t).uuid,s.bumpScale=this.bumpScale),this.normalMap&&this.normalMap.isTexture&&(s.normalMap=this.normalMap.toJSON(t).uuid,s.normalMapType=this.normalMapType,s.normalScale=this.normalScale.toArray()),this.displacementMap&&this.displacementMap.isTexture&&(s.displacementMap=this.displacementMap.toJSON(t).uuid,s.displacementScale=this.displacementScale,s.displacementBias=this.displacementBias),this.roughnessMap&&this.roughnessMap.isTexture&&(s.roughnessMap=this.roughnessMap.toJSON(t).uuid),this.metalnessMap&&this.metalnessMap.isTexture&&(s.metalnessMap=this.metalnessMap.toJSON(t).uuid),this.emissiveMap&&this.emissiveMap.isTexture&&(s.emissiveMap=this.emissiveMap.toJSON(t).uuid),this.specularMap&&this.specularMap.isTexture&&(s.specularMap=this.specularMap.toJSON(t).uuid),this.specularIntensityMap&&this.specularIntensityMap.isTexture&&(s.specularIntensityMap=this.specularIntensityMap.toJSON(t).uuid),this.specularColorMap&&this.specularColorMap.isTexture&&(s.specularColorMap=this.specularColorMap.toJSON(t).uuid),this.envMap&&this.envMap.isTexture&&(s.envMap=this.envMap.toJSON(t).uuid,this.combine!==void 0&&(s.combine=this.combine)),this.envMapRotation!==void 0&&(s.envMapRotation=this.envMapRotation.toArray()),this.envMapIntensity!==void 0&&(s.envMapIntensity=this.envMapIntensity),this.reflectivity!==void 0&&(s.reflectivity=this.reflectivity),this.refractionRatio!==void 0&&(s.refractionRatio=this.refractionRatio),this.gradientMap&&this.gradientMap.isTexture&&(s.gradientMap=this.gradientMap.toJSON(t).uuid),this.transmission!==void 0&&(s.transmission=this.transmission),this.transmissionMap&&this.transmissionMap.isTexture&&(s.transmissionMap=this.transmissionMap.toJSON(t).uuid),this.thickness!==void 0&&(s.thickness=this.thickness),this.thicknessMap&&this.thicknessMap.isTexture&&(s.thicknessMap=this.thicknessMap.toJSON(t).uuid),this.attenuationDistance!==void 0&&this.attenuationDistance!==1/0&&(s.attenuationDistance=this.attenuationDistance),this.attenuationColor!==void 0&&(s.attenuationColor=this.attenuationColor.getHex()),this.size!==void 0&&(s.size=this.size),this.shadowSide!==null&&(s.shadowSide=this.shadowSide),this.sizeAttenuation!==void 0&&(s.sizeAttenuation=this.sizeAttenuation),this.blending!==Qr&&(s.blending=this.blending),this.side!==hs&&(s.side=this.side),this.vertexColors===!0&&(s.vertexColors=!0),this.opacity<1&&(s.opacity=this.opacity),this.transparent===!0&&(s.transparent=!0),this.blendSrc!==wd&&(s.blendSrc=this.blendSrc),this.blendDst!==Dd&&(s.blendDst=this.blendDst),this.blendEquation!==Vs&&(s.blendEquation=this.blendEquation),this.blendSrcAlpha!==null&&(s.blendSrcAlpha=this.blendSrcAlpha),this.blendDstAlpha!==null&&(s.blendDstAlpha=this.blendDstAlpha),this.blendEquationAlpha!==null&&(s.blendEquationAlpha=this.blendEquationAlpha),this.blendColor&&this.blendColor.isColor&&(s.blendColor=this.blendColor.getHex()),this.blendAlpha!==0&&(s.blendAlpha=this.blendAlpha),this.depthFunc!==eo&&(s.depthFunc=this.depthFunc),this.depthTest===!1&&(s.depthTest=this.depthTest),this.depthWrite===!1&&(s.depthWrite=this.depthWrite),this.colorWrite===!1&&(s.colorWrite=this.colorWrite),this.stencilWriteMask!==255&&(s.stencilWriteMask=this.stencilWriteMask),this.stencilFunc!==W_&&(s.stencilFunc=this.stencilFunc),this.stencilRef!==0&&(s.stencilRef=this.stencilRef),this.stencilFuncMask!==255&&(s.stencilFuncMask=this.stencilFuncMask),this.stencilFail!==Ur&&(s.stencilFail=this.stencilFail),this.stencilZFail!==Ur&&(s.stencilZFail=this.stencilZFail),this.stencilZPass!==Ur&&(s.stencilZPass=this.stencilZPass),this.stencilWrite===!0&&(s.stencilWrite=this.stencilWrite),this.rotation!==void 0&&this.rotation!==0&&(s.rotation=this.rotation),this.polygonOffset===!0&&(s.polygonOffset=!0),this.polygonOffsetFactor!==0&&(s.polygonOffsetFactor=this.polygonOffsetFactor),this.polygonOffsetUnits!==0&&(s.polygonOffsetUnits=this.polygonOffsetUnits),this.linewidth!==void 0&&this.linewidth!==1&&(s.linewidth=this.linewidth),this.dashSize!==void 0&&(s.dashSize=this.dashSize),this.gapSize!==void 0&&(s.gapSize=this.gapSize),this.scale!==void 0&&(s.scale=this.scale),this.dithering===!0&&(s.dithering=!0),this.alphaTest>0&&(s.alphaTest=this.alphaTest),this.alphaHash===!0&&(s.alphaHash=!0),this.alphaToCoverage===!0&&(s.alphaToCoverage=!0),this.premultipliedAlpha===!0&&(s.premultipliedAlpha=!0),this.forceSinglePass===!0&&(s.forceSinglePass=!0),this.allowOverride===!1&&(s.allowOverride=!1),this.wireframe===!0&&(s.wireframe=!0),this.wireframeLinewidth>1&&(s.wireframeLinewidth=this.wireframeLinewidth),this.wireframeLinecap!=="round"&&(s.wireframeLinecap=this.wireframeLinecap),this.wireframeLinejoin!=="round"&&(s.wireframeLinejoin=this.wireframeLinejoin),this.flatShading===!0&&(s.flatShading=!0),this.visible===!1&&(s.visible=!1),this.toneMapped===!1&&(s.toneMapped=!1),this.fog===!1&&(s.fog=!1),Object.keys(this.userData).length>0&&(s.userData=this.userData);function l(c){const f=[];for(const p in c){const m=c[p];delete m.metadata,f.push(m)}return f}if(i){const c=l(t.textures),f=l(t.images);c.length>0&&(s.textures=c),f.length>0&&(s.images=f)}return s}fromJSON(t,i){if(t.uuid!==void 0&&(this.uuid=t.uuid),t.name!==void 0&&(this.name=t.name),t.color!==void 0&&this.color!==void 0&&this.color.setHex(t.color),t.roughness!==void 0&&(this.roughness=t.roughness),t.metalness!==void 0&&(this.metalness=t.metalness),t.sheen!==void 0&&(this.sheen=t.sheen),t.sheenColor!==void 0&&(this.sheenColor=new ue().setHex(t.sheenColor)),t.sheenRoughness!==void 0&&(this.sheenRoughness=t.sheenRoughness),t.emissive!==void 0&&this.emissive!==void 0&&this.emissive.setHex(t.emissive),t.specular!==void 0&&this.specular!==void 0&&this.specular.setHex(t.specular),t.specularIntensity!==void 0&&(this.specularIntensity=t.specularIntensity),t.specularColor!==void 0&&this.specularColor!==void 0&&this.specularColor.setHex(t.specularColor),t.shininess!==void 0&&(this.shininess=t.shininess),t.clearcoat!==void 0&&(this.clearcoat=t.clearcoat),t.clearcoatRoughness!==void 0&&(this.clearcoatRoughness=t.clearcoatRoughness),t.dispersion!==void 0&&(this.dispersion=t.dispersion),t.iridescence!==void 0&&(this.iridescence=t.iridescence),t.iridescenceIOR!==void 0&&(this.iridescenceIOR=t.iridescenceIOR),t.iridescenceThicknessRange!==void 0&&(this.iridescenceThicknessRange=t.iridescenceThicknessRange),t.transmission!==void 0&&(this.transmission=t.transmission),t.thickness!==void 0&&(this.thickness=t.thickness),t.attenuationDistance!==void 0&&(this.attenuationDistance=t.attenuationDistance),t.attenuationColor!==void 0&&this.attenuationColor!==void 0&&this.attenuationColor.setHex(t.attenuationColor),t.anisotropy!==void 0&&(this.anisotropy=t.anisotropy),t.anisotropyRotation!==void 0&&(this.anisotropyRotation=t.anisotropyRotation),t.fog!==void 0&&(this.fog=t.fog),t.flatShading!==void 0&&(this.flatShading=t.flatShading),t.blending!==void 0&&(this.blending=t.blending),t.combine!==void 0&&(this.combine=t.combine),t.side!==void 0&&(this.side=t.side),t.shadowSide!==void 0&&(this.shadowSide=t.shadowSide),t.opacity!==void 0&&(this.opacity=t.opacity),t.transparent!==void 0&&(this.transparent=t.transparent),t.alphaTest!==void 0&&(this.alphaTest=t.alphaTest),t.alphaHash!==void 0&&(this.alphaHash=t.alphaHash),t.depthFunc!==void 0&&(this.depthFunc=t.depthFunc),t.depthTest!==void 0&&(this.depthTest=t.depthTest),t.depthWrite!==void 0&&(this.depthWrite=t.depthWrite),t.colorWrite!==void 0&&(this.colorWrite=t.colorWrite),t.blendSrc!==void 0&&(this.blendSrc=t.blendSrc),t.blendDst!==void 0&&(this.blendDst=t.blendDst),t.blendEquation!==void 0&&(this.blendEquation=t.blendEquation),t.blendSrcAlpha!==void 0&&(this.blendSrcAlpha=t.blendSrcAlpha),t.blendDstAlpha!==void 0&&(this.blendDstAlpha=t.blendDstAlpha),t.blendEquationAlpha!==void 0&&(this.blendEquationAlpha=t.blendEquationAlpha),t.blendColor!==void 0&&this.blendColor!==void 0&&this.blendColor.setHex(t.blendColor),t.blendAlpha!==void 0&&(this.blendAlpha=t.blendAlpha),t.stencilWriteMask!==void 0&&(this.stencilWriteMask=t.stencilWriteMask),t.stencilFunc!==void 0&&(this.stencilFunc=t.stencilFunc),t.stencilRef!==void 0&&(this.stencilRef=t.stencilRef),t.stencilFuncMask!==void 0&&(this.stencilFuncMask=t.stencilFuncMask),t.stencilFail!==void 0&&(this.stencilFail=t.stencilFail),t.stencilZFail!==void 0&&(this.stencilZFail=t.stencilZFail),t.stencilZPass!==void 0&&(this.stencilZPass=t.stencilZPass),t.stencilWrite!==void 0&&(this.stencilWrite=t.stencilWrite),t.wireframe!==void 0&&(this.wireframe=t.wireframe),t.wireframeLinewidth!==void 0&&(this.wireframeLinewidth=t.wireframeLinewidth),t.wireframeLinecap!==void 0&&(this.wireframeLinecap=t.wireframeLinecap),t.wireframeLinejoin!==void 0&&(this.wireframeLinejoin=t.wireframeLinejoin),t.rotation!==void 0&&(this.rotation=t.rotation),t.linewidth!==void 0&&(this.linewidth=t.linewidth),t.dashSize!==void 0&&(this.dashSize=t.dashSize),t.gapSize!==void 0&&(this.gapSize=t.gapSize),t.scale!==void 0&&(this.scale=t.scale),t.polygonOffset!==void 0&&(this.polygonOffset=t.polygonOffset),t.polygonOffsetFactor!==void 0&&(this.polygonOffsetFactor=t.polygonOffsetFactor),t.polygonOffsetUnits!==void 0&&(this.polygonOffsetUnits=t.polygonOffsetUnits),t.dithering!==void 0&&(this.dithering=t.dithering),t.alphaToCoverage!==void 0&&(this.alphaToCoverage=t.alphaToCoverage),t.premultipliedAlpha!==void 0&&(this.premultipliedAlpha=t.premultipliedAlpha),t.forceSinglePass!==void 0&&(this.forceSinglePass=t.forceSinglePass),t.allowOverride!==void 0&&(this.allowOverride=t.allowOverride),t.visible!==void 0&&(this.visible=t.visible),t.toneMapped!==void 0&&(this.toneMapped=t.toneMapped),t.userData!==void 0&&(this.userData=t.userData),t.vertexColors!==void 0&&(typeof t.vertexColors=="number"?this.vertexColors=t.vertexColors>0:this.vertexColors=t.vertexColors),t.size!==void 0&&(this.size=t.size),t.sizeAttenuation!==void 0&&(this.sizeAttenuation=t.sizeAttenuation),t.map!==void 0&&(this.map=i[t.map]||null),t.matcap!==void 0&&(this.matcap=i[t.matcap]||null),t.alphaMap!==void 0&&(this.alphaMap=i[t.alphaMap]||null),t.bumpMap!==void 0&&(this.bumpMap=i[t.bumpMap]||null),t.bumpScale!==void 0&&(this.bumpScale=t.bumpScale),t.normalMap!==void 0&&(this.normalMap=i[t.normalMap]||null),t.normalMapType!==void 0&&(this.normalMapType=t.normalMapType),t.normalScale!==void 0){let s=t.normalScale;Array.isArray(s)===!1&&(s=[s,s]),this.normalScale=new fe().fromArray(s)}return t.displacementMap!==void 0&&(this.displacementMap=i[t.displacementMap]||null),t.displacementScale!==void 0&&(this.displacementScale=t.displacementScale),t.displacementBias!==void 0&&(this.displacementBias=t.displacementBias),t.roughnessMap!==void 0&&(this.roughnessMap=i[t.roughnessMap]||null),t.metalnessMap!==void 0&&(this.metalnessMap=i[t.metalnessMap]||null),t.emissiveMap!==void 0&&(this.emissiveMap=i[t.emissiveMap]||null),t.emissiveIntensity!==void 0&&(this.emissiveIntensity=t.emissiveIntensity),t.specularMap!==void 0&&(this.specularMap=i[t.specularMap]||null),t.specularIntensityMap!==void 0&&(this.specularIntensityMap=i[t.specularIntensityMap]||null),t.specularColorMap!==void 0&&(this.specularColorMap=i[t.specularColorMap]||null),t.envMap!==void 0&&(this.envMap=i[t.envMap]||null),t.envMapRotation!==void 0&&this.envMapRotation.fromArray(t.envMapRotation),t.envMapIntensity!==void 0&&(this.envMapIntensity=t.envMapIntensity),t.reflectivity!==void 0&&(this.reflectivity=t.reflectivity),t.refractionRatio!==void 0&&(this.refractionRatio=t.refractionRatio),t.lightMap!==void 0&&(this.lightMap=i[t.lightMap]||null),t.lightMapIntensity!==void 0&&(this.lightMapIntensity=t.lightMapIntensity),t.aoMap!==void 0&&(this.aoMap=i[t.aoMap]||null),t.aoMapIntensity!==void 0&&(this.aoMapIntensity=t.aoMapIntensity),t.gradientMap!==void 0&&(this.gradientMap=i[t.gradientMap]||null),t.clearcoatMap!==void 0&&(this.clearcoatMap=i[t.clearcoatMap]||null),t.clearcoatRoughnessMap!==void 0&&(this.clearcoatRoughnessMap=i[t.clearcoatRoughnessMap]||null),t.clearcoatNormalMap!==void 0&&(this.clearcoatNormalMap=i[t.clearcoatNormalMap]||null),t.clearcoatNormalScale!==void 0&&(this.clearcoatNormalScale=new fe().fromArray(t.clearcoatNormalScale)),t.iridescenceMap!==void 0&&(this.iridescenceMap=i[t.iridescenceMap]||null),t.iridescenceThicknessMap!==void 0&&(this.iridescenceThicknessMap=i[t.iridescenceThicknessMap]||null),t.transmissionMap!==void 0&&(this.transmissionMap=i[t.transmissionMap]||null),t.thicknessMap!==void 0&&(this.thicknessMap=i[t.thicknessMap]||null),t.anisotropyMap!==void 0&&(this.anisotropyMap=i[t.anisotropyMap]||null),t.sheenColorMap!==void 0&&(this.sheenColorMap=i[t.sheenColorMap]||null),t.sheenRoughnessMap!==void 0&&(this.sheenRoughnessMap=i[t.sheenRoughnessMap]||null),this}clone(){return new this.constructor().copy(this)}copy(t){this.name=t.name,this.blending=t.blending,this.side=t.side,this.vertexColors=t.vertexColors,this.opacity=t.opacity,this.transparent=t.transparent,this.blendSrc=t.blendSrc,this.blendDst=t.blendDst,this.blendEquation=t.blendEquation,this.blendSrcAlpha=t.blendSrcAlpha,this.blendDstAlpha=t.blendDstAlpha,this.blendEquationAlpha=t.blendEquationAlpha,this.blendColor.copy(t.blendColor),this.blendAlpha=t.blendAlpha,this.depthFunc=t.depthFunc,this.depthTest=t.depthTest,this.depthWrite=t.depthWrite,this.stencilWriteMask=t.stencilWriteMask,this.stencilFunc=t.stencilFunc,this.stencilRef=t.stencilRef,this.stencilFuncMask=t.stencilFuncMask,this.stencilFail=t.stencilFail,this.stencilZFail=t.stencilZFail,this.stencilZPass=t.stencilZPass,this.stencilWrite=t.stencilWrite;const i=t.clippingPlanes;let s=null;if(i!==null){const l=i.length;s=new Array(l);for(let c=0;c!==l;++c)s[c]=i[c].clone()}return this.clippingPlanes=s,this.clipIntersection=t.clipIntersection,this.clipShadows=t.clipShadows,this.shadowSide=t.shadowSide,this.colorWrite=t.colorWrite,this.precision=t.precision,this.polygonOffset=t.polygonOffset,this.polygonOffsetFactor=t.polygonOffsetFactor,this.polygonOffsetUnits=t.polygonOffsetUnits,this.dithering=t.dithering,this.alphaTest=t.alphaTest,this.alphaHash=t.alphaHash,this.alphaToCoverage=t.alphaToCoverage,this.premultipliedAlpha=t.premultipliedAlpha,this.forceSinglePass=t.forceSinglePass,this.allowOverride=t.allowOverride,this.visible=t.visible,this.toneMapped=t.toneMapped,this.userData=JSON.parse(JSON.stringify(t.userData)),this}dispose(){this.dispatchEvent({type:"dispose"})}set needsUpdate(t){t===!0&&this.version++}}class Gp extends ps{constructor(t){super(),this.isSpriteMaterial=!0,this.type="SpriteMaterial",this.color=new ue(16777215),this.map=null,this.alphaMap=null,this.rotation=0,this.sizeAttenuation=!0,this.transparent=!0,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.alphaMap=t.alphaMap,this.rotation=t.rotation,this.sizeAttenuation=t.sizeAttenuation,this.fog=t.fog,this}}let Vr;const sl=new V,kr=new V,Xr=new V,Wr=new fe,rl=new fe,Tx=new Fe,Gc=new V,ol=new V,Vc=new V,rv=new fe,ud=new fe,ov=new fe;class Ax extends Mn{constructor(t=new Gp){if(super(),this.isSprite=!0,this.type="Sprite",Vr===void 0){Vr=new hn;const i=new Float32Array([-.5,-.5,0,0,0,.5,-.5,0,1,0,.5,.5,0,1,1,-.5,.5,0,0,1]),s=new OE(i,5);Vr.setIndex([0,1,2,0,2,3]),Vr.setAttribute("position",new bu(s,3,0,!1)),Vr.setAttribute("uv",new bu(s,2,3,!1))}this.geometry=Vr,this.material=t,this.center=new fe(.5,.5),this.count=1}raycast(t,i){t.camera===null&&Ue('Sprite: "Raycaster.camera" needs to be set in order to raycast against sprites.'),kr.setFromMatrixScale(this.matrixWorld),Tx.copy(t.camera.matrixWorld),this.modelViewMatrix.multiplyMatrices(t.camera.matrixWorldInverse,this.matrixWorld),Xr.setFromMatrixPosition(this.modelViewMatrix),t.camera.isPerspectiveCamera&&this.material.sizeAttenuation===!1&&kr.multiplyScalar(-Xr.z);const s=this.material.rotation;let l,c;s!==0&&(c=Math.cos(s),l=Math.sin(s));const f=this.center;kc(Gc.set(-.5,-.5,0),Xr,f,kr,l,c),kc(ol.set(.5,-.5,0),Xr,f,kr,l,c),kc(Vc.set(.5,.5,0),Xr,f,kr,l,c),rv.set(0,0),ud.set(1,0),ov.set(1,1);let p=t.ray.intersectTriangle(Gc,ol,Vc,!1,sl);if(p===null&&(kc(ol.set(-.5,.5,0),Xr,f,kr,l,c),ud.set(0,1),p=t.ray.intersectTriangle(Gc,Vc,ol,!1,sl),p===null))return;const m=t.ray.origin.distanceTo(sl);m<t.near||m>t.far||i.push({distance:m,point:sl.clone(),uv:_i.getInterpolation(sl,Gc,ol,Vc,rv,ud,ov,new fe),face:null,object:this})}copy(t,i){return super.copy(t,i),t.center!==void 0&&this.center.copy(t.center),this.material=t.material,this}}function kc(r,t,i,s,l,c){Wr.subVectors(r,i).addScalar(.5).multiply(s),l!==void 0?(rl.x=c*Wr.x-l*Wr.y,rl.y=l*Wr.x+c*Wr.y):rl.copy(Wr),r.copy(t),r.x+=rl.x,r.y+=rl.y,r.applyMatrix4(Tx)}const Ea=new V,fd=new V,Xc=new V,os=new V,hd=new V,Wc=new V,dd=new V;class wu{constructor(t=new V,i=new V(0,0,-1)){this.origin=t,this.direction=i}set(t,i){return this.origin.copy(t),this.direction.copy(i),this}copy(t){return this.origin.copy(t.origin),this.direction.copy(t.direction),this}at(t,i){return i.copy(this.origin).addScaledVector(this.direction,t)}lookAt(t){return this.direction.copy(t).sub(this.origin).normalize(),this}recast(t){return this.origin.copy(this.at(t,Ea)),this}closestPointToPoint(t,i){i.subVectors(t,this.origin);const s=i.dot(this.direction);return s<0?i.copy(this.origin):i.copy(this.origin).addScaledVector(this.direction,s)}distanceToPoint(t){return Math.sqrt(this.distanceSqToPoint(t))}distanceSqToPoint(t){const i=Ea.subVectors(t,this.origin).dot(this.direction);return i<0?this.origin.distanceToSquared(t):(Ea.copy(this.origin).addScaledVector(this.direction,i),Ea.distanceToSquared(t))}distanceSqToSegment(t,i,s,l){fd.copy(t).add(i).multiplyScalar(.5),Xc.copy(i).sub(t).normalize(),os.copy(this.origin).sub(fd);const c=t.distanceTo(i)*.5,f=-this.direction.dot(Xc),p=os.dot(this.direction),m=-os.dot(Xc),d=os.lengthSq(),_=Math.abs(1-f*f);let v,g,S,E;if(_>0)if(v=f*m-p,g=f*p-m,E=c*_,v>=0)if(g>=-E)if(g<=E){const A=1/_;v*=A,g*=A,S=v*(v+f*g+2*p)+g*(f*v+g+2*m)+d}else g=c,v=Math.max(0,-(f*g+p)),S=-v*v+g*(g+2*m)+d;else g=-c,v=Math.max(0,-(f*g+p)),S=-v*v+g*(g+2*m)+d;else g<=-E?(v=Math.max(0,-(-f*c+p)),g=v>0?-c:Math.min(Math.max(-c,-m),c),S=-v*v+g*(g+2*m)+d):g<=E?(v=0,g=Math.min(Math.max(-c,-m),c),S=g*(g+2*m)+d):(v=Math.max(0,-(f*c+p)),g=v>0?c:Math.min(Math.max(-c,-m),c),S=-v*v+g*(g+2*m)+d);else g=f>0?-c:c,v=Math.max(0,-(f*g+p)),S=-v*v+g*(g+2*m)+d;return s&&s.copy(this.origin).addScaledVector(this.direction,v),l&&l.copy(fd).addScaledVector(Xc,g),S}intersectSphere(t,i){Ea.subVectors(t.center,this.origin);const s=Ea.dot(this.direction),l=Ea.dot(Ea)-s*s,c=t.radius*t.radius;if(l>c)return null;const f=Math.sqrt(c-l),p=s-f,m=s+f;return m<0?null:p<0?this.at(m,i):this.at(p,i)}intersectsSphere(t){return t.radius<0?!1:this.distanceSqToPoint(t.center)<=t.radius*t.radius}distanceToPlane(t){const i=t.normal.dot(this.direction);if(i===0)return t.distanceToPoint(this.origin)===0?0:null;const s=-(this.origin.dot(t.normal)+t.constant)/i;return s>=0?s:null}intersectPlane(t,i){const s=this.distanceToPlane(t);return s===null?null:this.at(s,i)}intersectsPlane(t){const i=t.distanceToPoint(this.origin);return i===0||t.normal.dot(this.direction)*i<0}intersectBox(t,i){let s,l,c,f,p,m;const d=1/this.direction.x,_=1/this.direction.y,v=1/this.direction.z,g=this.origin;return d>=0?(s=(t.min.x-g.x)*d,l=(t.max.x-g.x)*d):(s=(t.max.x-g.x)*d,l=(t.min.x-g.x)*d),_>=0?(c=(t.min.y-g.y)*_,f=(t.max.y-g.y)*_):(c=(t.max.y-g.y)*_,f=(t.min.y-g.y)*_),s>f||c>l||((c>s||isNaN(s))&&(s=c),(f<l||isNaN(l))&&(l=f),v>=0?(p=(t.min.z-g.z)*v,m=(t.max.z-g.z)*v):(p=(t.max.z-g.z)*v,m=(t.min.z-g.z)*v),s>m||p>l)||((p>s||s!==s)&&(s=p),(m<l||l!==l)&&(l=m),l<0)?null:this.at(s>=0?s:l,i)}intersectsBox(t){return this.intersectBox(t,Ea)!==null}intersectTriangle(t,i,s,l,c){hd.subVectors(i,t),Wc.subVectors(s,t),dd.crossVectors(hd,Wc);let f=this.direction.dot(dd),p;if(f>0){if(l)return null;p=1}else if(f<0)p=-1,f=-f;else return null;os.subVectors(this.origin,t);const m=p*this.direction.dot(Wc.crossVectors(os,Wc));if(m<0)return null;const d=p*this.direction.dot(hd.cross(os));if(d<0||m+d>f)return null;const _=-p*os.dot(dd);return _<0?null:this.at(_/f,c)}applyMatrix4(t){return this.origin.applyMatrix4(t),this.direction.transformDirection(t),this}equals(t){return t.origin.equals(this.origin)&&t.direction.equals(this.direction)}clone(){return new this.constructor().copy(this)}}class io extends ps{constructor(t){super(),this.isMeshBasicMaterial=!0,this.type="MeshBasicMaterial",this.color=new ue(16777215),this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.specularMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new ds,this.combine=rx,this.reflectivity=1,this.refractionRatio=.98,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.lightMap=t.lightMap,this.lightMapIntensity=t.lightMapIntensity,this.aoMap=t.aoMap,this.aoMapIntensity=t.aoMapIntensity,this.specularMap=t.specularMap,this.alphaMap=t.alphaMap,this.envMap=t.envMap,this.envMapRotation.copy(t.envMapRotation),this.combine=t.combine,this.reflectivity=t.reflectivity,this.refractionRatio=t.refractionRatio,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.wireframeLinecap=t.wireframeLinecap,this.wireframeLinejoin=t.wireframeLinejoin,this.fog=t.fog,this}}const lv=new Fe,Is=new wu,qc=new Js,cv=new V,Yc=new V,Zc=new V,jc=new V,pd=new V,Kc=new V,uv=new V,Qc=new V;class ln extends Mn{constructor(t=new hn,i=new io){super(),this.isMesh=!0,this.type="Mesh",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.count=1,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),t.morphTargetInfluences!==void 0&&(this.morphTargetInfluences=t.morphTargetInfluences.slice()),t.morphTargetDictionary!==void 0&&(this.morphTargetDictionary=Object.assign({},t.morphTargetDictionary)),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}updateMorphTargets(){const i=this.geometry.morphAttributes,s=Object.keys(i);if(s.length>0){const l=i[s[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const p=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[p]=c}}}}getVertexPosition(t,i){const s=this.geometry,l=s.attributes.position,c=s.morphAttributes.position,f=s.morphTargetsRelative;i.fromBufferAttribute(l,t);const p=this.morphTargetInfluences;if(c&&p){Kc.set(0,0,0);for(let m=0,d=c.length;m<d;m++){const _=p[m],v=c[m];_!==0&&(pd.fromBufferAttribute(v,t),f?Kc.addScaledVector(pd,_):Kc.addScaledVector(pd.sub(i),_))}i.add(Kc)}return i}raycast(t,i){const s=this.geometry,l=this.material,c=this.matrixWorld;l!==void 0&&(s.boundingSphere===null&&s.computeBoundingSphere(),qc.copy(s.boundingSphere),qc.applyMatrix4(c),Is.copy(t.ray).recast(t.near),!(qc.containsPoint(Is.origin)===!1&&(Is.intersectSphere(qc,cv)===null||Is.origin.distanceToSquared(cv)>(t.far-t.near)**2))&&(lv.copy(c).invert(),Is.copy(t.ray).applyMatrix4(lv),!(s.boundingBox!==null&&Is.intersectsBox(s.boundingBox)===!1)&&this._computeIntersections(t,i,Is)))}_computeIntersections(t,i,s){let l;const c=this.geometry,f=this.material,p=c.index,m=c.attributes.position,d=c.attributes.uv,_=c.attributes.uv1,v=c.attributes.normal,g=c.groups,S=c.drawRange;if(p!==null)if(Array.isArray(f))for(let E=0,A=g.length;E<A;E++){const M=g[E],y=f[M.materialIndex],B=Math.max(M.start,S.start),F=Math.min(p.count,Math.min(M.start+M.count,S.start+S.count));for(let w=B,I=F;w<I;w+=3){const O=p.getX(w),D=p.getX(w+1),T=p.getX(w+2);l=Jc(this,y,t,s,d,_,v,O,D,T),l&&(l.faceIndex=Math.floor(w/3),l.face.materialIndex=M.materialIndex,i.push(l))}}else{const E=Math.max(0,S.start),A=Math.min(p.count,S.start+S.count);for(let M=E,y=A;M<y;M+=3){const B=p.getX(M),F=p.getX(M+1),w=p.getX(M+2);l=Jc(this,f,t,s,d,_,v,B,F,w),l&&(l.faceIndex=Math.floor(M/3),i.push(l))}}else if(m!==void 0)if(Array.isArray(f))for(let E=0,A=g.length;E<A;E++){const M=g[E],y=f[M.materialIndex],B=Math.max(M.start,S.start),F=Math.min(m.count,Math.min(M.start+M.count,S.start+S.count));for(let w=B,I=F;w<I;w+=3){const O=w,D=w+1,T=w+2;l=Jc(this,y,t,s,d,_,v,O,D,T),l&&(l.faceIndex=Math.floor(w/3),l.face.materialIndex=M.materialIndex,i.push(l))}}else{const E=Math.max(0,S.start),A=Math.min(m.count,S.start+S.count);for(let M=E,y=A;M<y;M+=3){const B=M,F=M+1,w=M+2;l=Jc(this,f,t,s,d,_,v,B,F,w),l&&(l.faceIndex=Math.floor(M/3),i.push(l))}}}}function zE(r,t,i,s,l,c,f,p){let m;if(t.side===ti?m=s.intersectTriangle(f,c,l,!0,p):m=s.intersectTriangle(l,c,f,t.side===hs,p),m===null)return null;Qc.copy(p),Qc.applyMatrix4(r.matrixWorld);const d=i.ray.origin.distanceTo(Qc);return d<i.near||d>i.far?null:{distance:d,point:Qc.clone(),object:r}}function Jc(r,t,i,s,l,c,f,p,m,d){r.getVertexPosition(p,Yc),r.getVertexPosition(m,Zc),r.getVertexPosition(d,jc);const _=zE(r,t,i,s,Yc,Zc,jc,uv);if(_){const v=new V;_i.getBarycoord(uv,Yc,Zc,jc,v),l&&(_.uv=_i.getInterpolatedAttribute(l,p,m,d,v,new fe)),c&&(_.uv1=_i.getInterpolatedAttribute(c,p,m,d,v,new fe)),f&&(_.normal=_i.getInterpolatedAttribute(f,p,m,d,v,new V),_.normal.dot(s.direction)>0&&_.normal.multiplyScalar(-1));const g={a:p,b:m,c:d,normal:new V,materialIndex:0};_i.getNormal(Yc,Zc,jc,g.normal),_.face=g,_.barycoord=v}return _}class Rx extends Vn{constructor(t=null,i=1,s=1,l,c,f,p,m,d=zn,_=zn,v,g){super(null,f,p,m,d,_,l,c,v,g),this.isDataTexture=!0,this.image={data:t,width:i,height:s},this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}class fv extends vi{constructor(t,i,s,l=1){super(t,i,s),this.isInstancedBufferAttribute=!0,this.meshPerAttribute=l}copy(t){return super.copy(t),this.meshPerAttribute=t.meshPerAttribute,this}toJSON(){const t=super.toJSON();return t.meshPerAttribute=this.meshPerAttribute,t.isInstancedBufferAttribute=!0,t}}const qr=new Fe,hv=new Fe,$c=[],dv=new Qs,BE=new Fe,ll=new ln,cl=new Js;class ul extends ln{constructor(t,i,s){super(t,i),this.isInstancedMesh=!0,this.instanceMatrix=new fv(new Float32Array(s*16),16),this.instanceColor=null,this.morphTexture=null,this.count=s,this.boundingBox=null,this.boundingSphere=null;for(let l=0;l<s;l++)this.setMatrixAt(l,BE)}computeBoundingBox(){const t=this.geometry,i=this.count;this.boundingBox===null&&(this.boundingBox=new Qs),t.boundingBox===null&&t.computeBoundingBox(),this.boundingBox.makeEmpty();for(let s=0;s<i;s++)this.getMatrixAt(s,qr),dv.copy(t.boundingBox).applyMatrix4(qr),this.boundingBox.union(dv)}computeBoundingSphere(){const t=this.geometry,i=this.count;this.boundingSphere===null&&(this.boundingSphere=new Js),t.boundingSphere===null&&t.computeBoundingSphere(),this.boundingSphere.makeEmpty();for(let s=0;s<i;s++)this.getMatrixAt(s,qr),cl.copy(t.boundingSphere).applyMatrix4(qr),this.boundingSphere.union(cl)}copy(t,i){return super.copy(t,i),this.instanceMatrix.copy(t.instanceMatrix),t.morphTexture!==null&&(this.morphTexture=t.morphTexture.clone()),t.instanceColor!==null&&(this.instanceColor=t.instanceColor.clone()),this.count=t.count,t.boundingBox!==null&&(this.boundingBox=t.boundingBox.clone()),t.boundingSphere!==null&&(this.boundingSphere=t.boundingSphere.clone()),this}getColorAt(t,i){return this.instanceColor===null?i.setRGB(1,1,1):i.fromArray(this.instanceColor.array,t*3)}getMatrixAt(t,i){return i.fromArray(this.instanceMatrix.array,t*16)}getMorphAt(t,i){const s=i.morphTargetInfluences,l=this.morphTexture.source.data.data,c=s.length+1,f=t*c+1;for(let p=0;p<s.length;p++)s[p]=l[f+p]}raycast(t,i){const s=this.matrixWorld,l=this.count;if(ll.geometry=this.geometry,ll.material=this.material,ll.material!==void 0&&(this.boundingSphere===null&&this.computeBoundingSphere(),cl.copy(this.boundingSphere),cl.applyMatrix4(s),t.ray.intersectsSphere(cl)!==!1))for(let c=0;c<l;c++){this.getMatrixAt(c,qr),hv.multiplyMatrices(s,qr),ll.matrixWorld=hv,ll.raycast(t,$c);for(let f=0,p=$c.length;f<p;f++){const m=$c[f];m.instanceId=c,m.object=this,i.push(m)}$c.length=0}}setColorAt(t,i){return this.instanceColor===null&&(this.instanceColor=new fv(new Float32Array(this.instanceMatrix.count*3).fill(1),3)),i.toArray(this.instanceColor.array,t*3),this}setMatrixAt(t,i){return i.toArray(this.instanceMatrix.array,t*16),this}setMorphAt(t,i){const s=i.morphTargetInfluences,l=s.length+1;this.morphTexture===null&&(this.morphTexture=new Rx(new Float32Array(l*this.count),l,this.count,Up,Ii));const c=this.morphTexture.source.data.data;let f=0;for(let d=0;d<s.length;d++)f+=s[d];const p=this.geometry.morphTargetsRelative?1:1-f,m=l*t;return c[m]=p,c.set(s,m+1),this}updateMorphTargets(){}dispose(){this.dispatchEvent({type:"dispose"}),this.morphTexture!==null&&(this.morphTexture.dispose(),this.morphTexture=null)}}const md=new V,IE=new V,FE=new de;class Gs{constructor(t=new V(1,0,0),i=0){this.isPlane=!0,this.normal=t,this.constant=i}set(t,i){return this.normal.copy(t),this.constant=i,this}setComponents(t,i,s,l){return this.normal.set(t,i,s),this.constant=l,this}setFromNormalAndCoplanarPoint(t,i){return this.normal.copy(t),this.constant=-i.dot(this.normal),this}setFromCoplanarPoints(t,i,s){const l=md.subVectors(s,i).cross(IE.subVectors(t,i)).normalize();return this.setFromNormalAndCoplanarPoint(l,t),this}copy(t){return this.normal.copy(t.normal),this.constant=t.constant,this}normalize(){const t=1/this.normal.length();return this.normal.multiplyScalar(t),this.constant*=t,this}negate(){return this.constant*=-1,this.normal.negate(),this}distanceToPoint(t){return this.normal.dot(t)+this.constant}distanceToSphere(t){return this.distanceToPoint(t.center)-t.radius}projectPoint(t,i){return i.copy(t).addScaledVector(this.normal,-this.distanceToPoint(t))}intersectLine(t,i,s=!0){const l=t.delta(md),c=this.normal.dot(l);if(c===0)return this.distanceToPoint(t.start)===0?i.copy(t.start):null;const f=-(t.start.dot(this.normal)+this.constant)/c;return s===!0&&(f<0||f>1)?null:i.copy(t.start).addScaledVector(l,f)}intersectsLine(t){const i=this.distanceToPoint(t.start),s=this.distanceToPoint(t.end);return i<0&&s>0||s<0&&i>0}intersectsBox(t){return t.intersectsPlane(this)}intersectsSphere(t){return t.intersectsPlane(this)}coplanarPoint(t){return t.copy(this.normal).multiplyScalar(-this.constant)}applyMatrix4(t,i){const s=i||FE.getNormalMatrix(t),l=this.coplanarPoint(md).applyMatrix4(t),c=this.normal.applyMatrix3(s).normalize();return this.constant=-l.dot(c),this}translate(t){return this.constant-=t.dot(this.normal),this}equals(t){return t.normal.equals(this.normal)&&t.constant===this.constant}clone(){return new this.constructor().copy(this)}}const Fs=new Js,HE=new fe(.5,.5),tu=new V;class Vp{constructor(t=new Gs,i=new Gs,s=new Gs,l=new Gs,c=new Gs,f=new Gs){this.planes=[t,i,s,l,c,f]}set(t,i,s,l,c,f){const p=this.planes;return p[0].copy(t),p[1].copy(i),p[2].copy(s),p[3].copy(l),p[4].copy(c),p[5].copy(f),this}copy(t){const i=this.planes;for(let s=0;s<6;s++)i[s].copy(t.planes[s]);return this}setFromProjectionMatrix(t,i=ji,s=!1){const l=this.planes,c=t.elements,f=c[0],p=c[1],m=c[2],d=c[3],_=c[4],v=c[5],g=c[6],S=c[7],E=c[8],A=c[9],M=c[10],y=c[11],B=c[12],F=c[13],w=c[14],I=c[15];if(l[0].setComponents(d-f,S-_,y-E,I-B).normalize(),l[1].setComponents(d+f,S+_,y+E,I+B).normalize(),l[2].setComponents(d+p,S+v,y+A,I+F).normalize(),l[3].setComponents(d-p,S-v,y-A,I-F).normalize(),s)l[4].setComponents(m,g,M,w).normalize(),l[5].setComponents(d-m,S-g,y-M,I-w).normalize();else if(l[4].setComponents(d-m,S-g,y-M,I-w).normalize(),i===ji)l[5].setComponents(d+m,S+g,y+M,I+w).normalize();else if(i===yl)l[5].setComponents(m,g,M,w).normalize();else throw new Error("THREE.Frustum.setFromProjectionMatrix(): Invalid coordinate system: "+i);return this}intersectsObject(t){if(t.boundingSphere!==void 0)t.boundingSphere===null&&t.computeBoundingSphere(),Fs.copy(t.boundingSphere).applyMatrix4(t.matrixWorld);else{const i=t.geometry;i.boundingSphere===null&&i.computeBoundingSphere(),Fs.copy(i.boundingSphere).applyMatrix4(t.matrixWorld)}return this.intersectsSphere(Fs)}intersectsSprite(t){Fs.center.set(0,0,0);const i=HE.distanceTo(t.center);return Fs.radius=.7071067811865476+i,Fs.applyMatrix4(t.matrixWorld),this.intersectsSphere(Fs)}intersectsSphere(t){const i=this.planes,s=t.center,l=-t.radius;for(let c=0;c<6;c++)if(i[c].distanceToPoint(s)<l)return!1;return!0}intersectsBox(t){const i=this.planes;for(let s=0;s<6;s++){const l=i[s];if(tu.x=l.normal.x>0?t.max.x:t.min.x,tu.y=l.normal.y>0?t.max.y:t.min.y,tu.z=l.normal.z>0?t.max.z:t.min.z,l.distanceToPoint(tu)<0)return!1}return!0}containsPoint(t){const i=this.planes;for(let s=0;s<6;s++)if(i[s].distanceToPoint(t)<0)return!1;return!0}clone(){return new this.constructor().copy(this)}}class fs extends ps{constructor(t){super(),this.isLineBasicMaterial=!0,this.type="LineBasicMaterial",this.color=new ue(16777215),this.map=null,this.linewidth=1,this.linecap="round",this.linejoin="round",this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.linewidth=t.linewidth,this.linecap=t.linecap,this.linejoin=t.linejoin,this.fog=t.fog,this}}const Tu=new V,Au=new V,pv=new Fe,fl=new wu,eu=new Js,gd=new V,mv=new V;class Du extends Mn{constructor(t=new hn,i=new fs){super(),this.isLine=!0,this.type="Line",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}computeLineDistances(){const t=this.geometry;if(t.index===null){const i=t.attributes.position,s=[0];for(let l=1,c=i.count;l<c;l++)Tu.fromBufferAttribute(i,l-1),Au.fromBufferAttribute(i,l),s[l]=s[l-1],s[l]+=Tu.distanceTo(Au);t.setAttribute("lineDistance",new En(s,1))}else re("Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}raycast(t,i){const s=this.geometry,l=this.matrixWorld,c=t.params.Line.threshold,f=s.drawRange;if(s.boundingSphere===null&&s.computeBoundingSphere(),eu.copy(s.boundingSphere),eu.applyMatrix4(l),eu.radius+=c,t.ray.intersectsSphere(eu)===!1)return;pv.copy(l).invert(),fl.copy(t.ray).applyMatrix4(pv);const p=c/((this.scale.x+this.scale.y+this.scale.z)/3),m=p*p,d=this.isLineSegments?2:1,_=s.index,g=s.attributes.position;if(_!==null){const S=Math.max(0,f.start),E=Math.min(_.count,f.start+f.count);for(let A=S,M=E-1;A<M;A+=d){const y=_.getX(A),B=_.getX(A+1),F=nu(this,t,fl,m,y,B,A);F&&i.push(F)}if(this.isLineLoop){const A=_.getX(E-1),M=_.getX(S),y=nu(this,t,fl,m,A,M,E-1);y&&i.push(y)}}else{const S=Math.max(0,f.start),E=Math.min(g.count,f.start+f.count);for(let A=S,M=E-1;A<M;A+=d){const y=nu(this,t,fl,m,A,A+1,A);y&&i.push(y)}if(this.isLineLoop){const A=nu(this,t,fl,m,E-1,S,E-1);A&&i.push(A)}}}updateMorphTargets(){const i=this.geometry.morphAttributes,s=Object.keys(i);if(s.length>0){const l=i[s[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const p=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[p]=c}}}}}function nu(r,t,i,s,l,c,f){const p=r.geometry.attributes.position;if(Tu.fromBufferAttribute(p,l),Au.fromBufferAttribute(p,c),i.distanceSqToSegment(Tu,Au,gd,mv)>s)return;gd.applyMatrix4(r.matrixWorld);const d=t.ray.origin.distanceTo(gd);if(!(d<t.near||d>t.far))return{distance:d,point:mv.clone().applyMatrix4(r.matrixWorld),index:f,face:null,faceIndex:null,barycoord:null,object:r}}const gv=new V,_v=new V;class Cx extends Du{constructor(t,i){super(t,i),this.isLineSegments=!0,this.type="LineSegments"}computeLineDistances(){const t=this.geometry;if(t.index===null){const i=t.attributes.position,s=[];for(let l=0,c=i.count;l<c;l+=2)gv.fromBufferAttribute(i,l),_v.fromBufferAttribute(i,l+1),s[l]=l===0?0:s[l-1],s[l+1]=s[l]+gv.distanceTo(_v);t.setAttribute("lineDistance",new En(s,1))}else re("LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}}class vp extends Du{constructor(t,i){super(t,i),this.isLineLoop=!0,this.type="LineLoop"}}class wx extends ps{constructor(t){super(),this.isPointsMaterial=!0,this.type="PointsMaterial",this.color=new ue(16777215),this.map=null,this.alphaMap=null,this.size=1,this.sizeAttenuation=!0,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.alphaMap=t.alphaMap,this.size=t.size,this.sizeAttenuation=t.sizeAttenuation,this.fog=t.fog,this}}const vv=new Fe,xp=new wu,iu=new Js,au=new V;class GE extends Mn{constructor(t=new hn,i=new wx){super(),this.isPoints=!0,this.type="Points",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}raycast(t,i){const s=this.geometry,l=this.matrixWorld,c=t.params.Points.threshold,f=s.drawRange;if(s.boundingSphere===null&&s.computeBoundingSphere(),iu.copy(s.boundingSphere),iu.applyMatrix4(l),iu.radius+=c,t.ray.intersectsSphere(iu)===!1)return;vv.copy(l).invert(),xp.copy(t.ray).applyMatrix4(vv);const p=c/((this.scale.x+this.scale.y+this.scale.z)/3),m=p*p,d=s.index,v=s.attributes.position;if(d!==null){const g=Math.max(0,f.start),S=Math.min(d.count,f.start+f.count);for(let E=g,A=S;E<A;E++){const M=d.getX(E);au.fromBufferAttribute(v,M),xv(au,M,m,l,t,i,this)}}else{const g=Math.max(0,f.start),S=Math.min(v.count,f.start+f.count);for(let E=g,A=S;E<A;E++)au.fromBufferAttribute(v,E),xv(au,E,m,l,t,i,this)}}updateMorphTargets(){const i=this.geometry.morphAttributes,s=Object.keys(i);if(s.length>0){const l=i[s[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const p=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[p]=c}}}}}function xv(r,t,i,s,l,c,f){const p=xp.distanceSqToPoint(r);if(p<i){const m=new V;xp.closestPointToPoint(r,m),m.applyMatrix4(s);const d=l.ray.origin.distanceTo(m);if(d<l.near||d>l.far)return;c.push({distance:d,distanceToRay:Math.sqrt(p),point:m,index:t,face:null,faceIndex:null,barycoord:null,object:f})}}class Dx extends Vn{constructor(t=[],i=Zs,s,l,c,f,p,m,d,_){super(t,i,s,l,c,f,p,m,d,_),this.isCubeTexture=!0,this.flipY=!1}get images(){return this.image}set images(t){this.image=t}}class Ux extends Vn{constructor(t,i,s,l,c,f,p,m,d){super(t,i,s,l,c,f,p,m,d),this.isCanvasTexture=!0,this.needsUpdate=!0}}class ao extends Vn{constructor(t,i,s=Ji,l,c,f,p=zn,m=zn,d,_=wa,v=1){if(_!==wa&&_!==Ws)throw new Error("THREE.DepthTexture: format must be either THREE.DepthFormat or THREE.DepthStencilFormat");const g={width:t,height:i,depth:v};super(g,l,c,f,p,m,_,s,d),this.isDepthTexture=!0,this.flipY=!1,this.generateMipmaps=!1,this.compareFunction=null}copy(t){return super.copy(t),this.source=new Ip(Object.assign({},t.image)),this.compareFunction=t.compareFunction,this}toJSON(t){const i=super.toJSON(t);return this.compareFunction!==null&&(i.compareFunction=this.compareFunction),i}}class VE extends ao{constructor(t,i=Ji,s=Zs,l,c,f=zn,p=zn,m,d=wa){const _={width:t,height:t,depth:1},v=[_,_,_,_,_,_];super(t,t,i,s,l,c,f,p,m,d),this.image=v,this.isCubeDepthTexture=!0,this.isCubeTexture=!0}get images(){return this.image}set images(t){this.image=t}}class Nx extends Vn{constructor(t=null){super(),this.sourceTexture=t,this.isExternalTexture=!0}copy(t){return super.copy(t),this.sourceTexture=t.sourceTexture,this}}class Dn extends hn{constructor(t=1,i=1,s=1,l=1,c=1,f=1){super(),this.type="BoxGeometry",this.parameters={width:t,height:i,depth:s,widthSegments:l,heightSegments:c,depthSegments:f};const p=this;l=Math.floor(l),c=Math.floor(c),f=Math.floor(f);const m=[],d=[],_=[],v=[];let g=0,S=0;E("z","y","x",-1,-1,s,i,t,f,c,0),E("z","y","x",1,-1,s,i,-t,f,c,1),E("x","z","y",1,1,t,s,i,l,f,2),E("x","z","y",1,-1,t,s,-i,l,f,3),E("x","y","z",1,-1,t,i,s,l,c,4),E("x","y","z",-1,-1,t,i,-s,l,c,5),this.setIndex(m),this.setAttribute("position",new En(d,3)),this.setAttribute("normal",new En(_,3)),this.setAttribute("uv",new En(v,2));function E(A,M,y,B,F,w,I,O,D,T,L){const j=w/D,k=I/T,J=w/2,ut=I/2,gt=O/2,$=D+1,P=T+1;let H=0,et=0;const pt=new V;for(let Et=0;Et<P;Et++){const N=Et*k-ut;for(let W=0;W<$;W++){const vt=W*j-J;pt[A]=vt*B,pt[M]=N*F,pt[y]=gt,d.push(pt.x,pt.y,pt.z),pt[A]=0,pt[M]=0,pt[y]=O>0?1:-1,_.push(pt.x,pt.y,pt.z),v.push(W/D),v.push(1-Et/T),H+=1}}for(let Et=0;Et<T;Et++)for(let N=0;N<D;N++){const W=g+N+$*Et,vt=g+N+$*(Et+1),Tt=g+(N+1)+$*(Et+1),At=g+(N+1)+$*Et;m.push(W,vt,At),m.push(vt,Tt,At),et+=6}p.addGroup(S,et,L),S+=et,g+=H}}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new Dn(t.width,t.height,t.depth,t.widthSegments,t.heightSegments,t.depthSegments)}}class kp extends hn{constructor(t=1,i=32,s=0,l=Math.PI*2){super(),this.type="CircleGeometry",this.parameters={radius:t,segments:i,thetaStart:s,thetaLength:l},i=Math.max(3,i);const c=[],f=[],p=[],m=[],d=new V,_=new fe;f.push(0,0,0),p.push(0,0,1),m.push(.5,.5);for(let v=0,g=3;v<=i;v++,g+=3){const S=s+v/i*l;d.x=t*Math.cos(S),d.y=t*Math.sin(S),f.push(d.x,d.y,d.z),p.push(0,0,1),_.x=(f[g]/t+1)/2,_.y=(f[g+1]/t+1)/2,m.push(_.x,_.y)}for(let v=1;v<=i;v++)c.push(v,v+1,0);this.setIndex(c),this.setAttribute("position",new En(f,3)),this.setAttribute("normal",new En(p,3)),this.setAttribute("uv",new En(m,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new kp(t.radius,t.segments,t.thetaStart,t.thetaLength)}}const su=new V,ru=new V,_d=new V,ou=new _i;class kE extends hn{constructor(t=null,i=1){if(super(),this.type="EdgesGeometry",this.parameters={geometry:t,thresholdAngle:i},t!==null){const l=Math.pow(10,4),c=Math.cos($r*i),f=t.getIndex(),p=t.getAttribute("position"),m=f?f.count:p.count,d=[0,0,0],_=["a","b","c"],v=new Array(3),g={},S=[];for(let E=0;E<m;E+=3){f?(d[0]=f.getX(E),d[1]=f.getX(E+1),d[2]=f.getX(E+2)):(d[0]=E,d[1]=E+1,d[2]=E+2);const{a:A,b:M,c:y}=ou;if(A.fromBufferAttribute(p,d[0]),M.fromBufferAttribute(p,d[1]),y.fromBufferAttribute(p,d[2]),ou.getNormal(_d),v[0]=`${Math.round(A.x*l)},${Math.round(A.y*l)},${Math.round(A.z*l)}`,v[1]=`${Math.round(M.x*l)},${Math.round(M.y*l)},${Math.round(M.z*l)}`,v[2]=`${Math.round(y.x*l)},${Math.round(y.y*l)},${Math.round(y.z*l)}`,!(v[0]===v[1]||v[1]===v[2]||v[2]===v[0]))for(let B=0;B<3;B++){const F=(B+1)%3,w=v[B],I=v[F],O=ou[_[B]],D=ou[_[F]],T=`${w}_${I}`,L=`${I}_${w}`;L in g&&g[L]?(_d.dot(g[L].normal)<=c&&(S.push(O.x,O.y,O.z),S.push(D.x,D.y,D.z)),g[L]=null):T in g||(g[T]={index0:d[B],index1:d[F],normal:_d.clone()})}}for(const E in g)if(g[E]){const{index0:A,index1:M}=g[E];su.fromBufferAttribute(p,A),ru.fromBufferAttribute(p,M),S.push(su.x,su.y,su.z),S.push(ru.x,ru.y,ru.z)}this.setAttribute("position",new En(S,3))}}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}}class XE{constructor(){this.type="Curve",this.arcLengthDivisions=200,this.needsUpdate=!1,this.cacheArcLengths=null}getPoint(){re("Curve: .getPoint() not implemented.")}getPointAt(t,i){const s=this.getUtoTmapping(t);return this.getPoint(s,i)}getPoints(t=5){const i=[];for(let s=0;s<=t;s++)i.push(this.getPoint(s/t));return i}getSpacedPoints(t=5){const i=[];for(let s=0;s<=t;s++)i.push(this.getPointAt(s/t));return i}getLength(){const t=this.getLengths();return t[t.length-1]}getLengths(t=this.arcLengthDivisions){if(this.cacheArcLengths&&this.cacheArcLengths.length===t+1&&!this.needsUpdate)return this.cacheArcLengths;this.needsUpdate=!1;const i=[];let s,l=this.getPoint(0),c=0;i.push(0);for(let f=1;f<=t;f++)s=this.getPoint(f/t),c+=s.distanceTo(l),i.push(c),l=s;return this.cacheArcLengths=i,i}updateArcLengths(){this.needsUpdate=!0,this.getLengths()}getUtoTmapping(t,i=null){const s=this.getLengths();let l=0;const c=s.length;let f;i?f=i:f=t*s[c-1];let p=0,m=c-1,d;for(;p<=m;)if(l=Math.floor(p+(m-p)/2),d=s[l]-f,d<0)p=l+1;else if(d>0)m=l-1;else{m=l;break}if(l=m,s[l]===f)return l/(c-1);const _=s[l],g=s[l+1]-_,S=(f-_)/g;return(l+S)/(c-1)}getTangent(t,i){let l=t-1e-4,c=t+1e-4;l<0&&(l=0),c>1&&(c=1);const f=this.getPoint(l),p=this.getPoint(c),m=i||(f.isVector2?new fe:new V);return m.copy(p).sub(f).normalize(),m}getTangentAt(t,i){const s=this.getUtoTmapping(t);return this.getTangent(s,i)}computeFrenetFrames(t,i=!1){const s=new V,l=[],c=[],f=[],p=new V,m=new Fe;for(let S=0;S<=t;S++){const E=S/t;l[S]=this.getTangentAt(E,new V)}c[0]=new V,f[0]=new V;let d=Number.MAX_VALUE;const _=Math.abs(l[0].x),v=Math.abs(l[0].y),g=Math.abs(l[0].z);_<=d&&(d=_,s.set(1,0,0)),v<=d&&(d=v,s.set(0,1,0)),g<=d&&s.set(0,0,1),p.crossVectors(l[0],s).normalize(),c[0].crossVectors(l[0],p),f[0].crossVectors(l[0],c[0]);for(let S=1;S<=t;S++){if(c[S]=c[S-1].clone(),f[S]=f[S-1].clone(),p.crossVectors(l[S-1],l[S]),p.length()>Number.EPSILON){p.normalize();const E=Math.acos(ye(l[S-1].dot(l[S]),-1,1));c[S].applyMatrix4(m.makeRotationAxis(p,E))}f[S].crossVectors(l[S],c[S])}if(i===!0){let S=Math.acos(ye(c[0].dot(c[t]),-1,1));S/=t,l[0].dot(p.crossVectors(c[0],c[t]))>0&&(S=-S);for(let E=1;E<=t;E++)c[E].applyMatrix4(m.makeRotationAxis(l[E],S*E)),f[E].crossVectors(l[E],c[E])}return{tangents:l,normals:c,binormals:f}}clone(){return new this.constructor().copy(this)}copy(t){return this.arcLengthDivisions=t.arcLengthDivisions,this}toJSON(){const t={metadata:{version:4.7,type:"Curve",generator:"Curve.toJSON"}};return t.arcLengthDivisions=this.arcLengthDivisions,t.type=this.type,t}fromJSON(t){return this.arcLengthDivisions=t.arcLengthDivisions,this}}function Xp(){let r=0,t=0,i=0,s=0;function l(c,f,p,m){r=c,t=p,i=-3*c+3*f-2*p-m,s=2*c-2*f+p+m}return{initCatmullRom:function(c,f,p,m,d){l(f,p,d*(p-c),d*(m-f))},initNonuniformCatmullRom:function(c,f,p,m,d,_,v){let g=(f-c)/d-(p-c)/(d+_)+(p-f)/_,S=(p-f)/_-(m-f)/(_+v)+(m-p)/v;g*=_,S*=_,l(f,p,g,S)},calc:function(c){const f=c*c,p=f*c;return r+t*c+i*f+s*p}}}const yv=new V,Sv=new V,vd=new Xp,xd=new Xp,yd=new Xp;class WE extends XE{constructor(t=[],i=!1,s="centripetal",l=.5){super(),this.isCatmullRomCurve3=!0,this.type="CatmullRomCurve3",this.points=t,this.closed=i,this.curveType=s,this.tension=l}getPoint(t,i=new V){const s=i,l=this.points,c=l.length,f=(c-(this.closed?0:1))*t;let p=Math.floor(f),m=f-p;this.closed?p+=p>0?0:(Math.floor(Math.abs(p)/c)+1)*c:m===0&&p===c-1&&(p=c-2,m=1);let d,_;this.closed||p>0?d=l[(p-1)%c]:(Sv.subVectors(l[0],l[1]).add(l[0]),d=Sv);const v=l[p%c],g=l[(p+1)%c];if(this.closed||p+2<c?_=l[(p+2)%c]:(yv.subVectors(l[c-1],l[c-2]).add(l[c-1]),_=yv),this.curveType==="centripetal"||this.curveType==="chordal"){const S=this.curveType==="chordal"?.5:.25;let E=Math.pow(d.distanceToSquared(v),S),A=Math.pow(v.distanceToSquared(g),S),M=Math.pow(g.distanceToSquared(_),S);A<1e-4&&(A=1),E<1e-4&&(E=A),M<1e-4&&(M=A),vd.initNonuniformCatmullRom(d.x,v.x,g.x,_.x,E,A,M),xd.initNonuniformCatmullRom(d.y,v.y,g.y,_.y,E,A,M),yd.initNonuniformCatmullRom(d.z,v.z,g.z,_.z,E,A,M)}else this.curveType==="catmullrom"&&(vd.initCatmullRom(d.x,v.x,g.x,_.x,this.tension),xd.initCatmullRom(d.y,v.y,g.y,_.y,this.tension),yd.initCatmullRom(d.z,v.z,g.z,_.z,this.tension));return s.set(vd.calc(m),xd.calc(m),yd.calc(m)),s}copy(t){super.copy(t),this.points=[];for(let i=0,s=t.points.length;i<s;i++){const l=t.points[i];this.points.push(l.clone())}return this.closed=t.closed,this.curveType=t.curveType,this.tension=t.tension,this}toJSON(){const t=super.toJSON();t.points=[];for(let i=0,s=this.points.length;i<s;i++){const l=this.points[i];t.points.push(l.toArray())}return t.closed=this.closed,t.curveType=this.curveType,t.tension=this.tension,t}fromJSON(t){super.fromJSON(t),this.points=[];for(let i=0,s=t.points.length;i<s;i++){const l=t.points[i];this.points.push(new V().fromArray(l))}return this.closed=t.closed,this.curveType=t.curveType,this.tension=t.tension,this}}class ms extends hn{constructor(t=1,i=1,s=1,l=1){super(),this.type="PlaneGeometry",this.parameters={width:t,height:i,widthSegments:s,heightSegments:l};const c=t/2,f=i/2,p=Math.floor(s),m=Math.floor(l),d=p+1,_=m+1,v=t/p,g=i/m,S=[],E=[],A=[],M=[];for(let y=0;y<_;y++){const B=y*g-f;for(let F=0;F<d;F++){const w=F*v-c;E.push(w,-B,0),A.push(0,0,1),M.push(F/p),M.push(1-y/m)}}for(let y=0;y<m;y++)for(let B=0;B<p;B++){const F=B+d*y,w=B+d*(y+1),I=B+1+d*(y+1),O=B+1+d*y;S.push(F,w,O),S.push(w,I,O)}this.setIndex(S),this.setAttribute("position",new En(E,3)),this.setAttribute("normal",new En(A,3)),this.setAttribute("uv",new En(M,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new ms(t.width,t.height,t.widthSegments,t.heightSegments)}}class Wp extends hn{constructor(t=1,i=.4,s=12,l=48,c=Math.PI*2,f=0,p=Math.PI*2){super(),this.type="TorusGeometry",this.parameters={radius:t,tube:i,radialSegments:s,tubularSegments:l,arc:c,thetaStart:f,thetaLength:p},s=Math.floor(s),l=Math.floor(l);const m=[],d=[],_=[],v=[],g=new V,S=new V,E=new V;for(let A=0;A<=s;A++){const M=f+A/s*p;for(let y=0;y<=l;y++){const B=y/l*c;S.x=(t+i*Math.cos(M))*Math.cos(B),S.y=(t+i*Math.cos(M))*Math.sin(B),S.z=i*Math.sin(M),d.push(S.x,S.y,S.z),g.x=t*Math.cos(B),g.y=t*Math.sin(B),E.subVectors(S,g).normalize(),_.push(E.x,E.y,E.z),v.push(y/l),v.push(A/s)}}for(let A=1;A<=s;A++)for(let M=1;M<=l;M++){const y=(l+1)*A+M-1,B=(l+1)*(A-1)+M-1,F=(l+1)*(A-1)+M,w=(l+1)*A+M;m.push(y,B,w),m.push(B,F,w)}this.setIndex(m),this.setAttribute("position",new En(d,3)),this.setAttribute("normal",new En(_,3)),this.setAttribute("uv",new En(v,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new Wp(t.radius,t.tube,t.radialSegments,t.tubularSegments,t.arc)}}function so(r){const t={};for(const i in r){t[i]={};for(const s in r[i]){const l=r[i][s];if(Mv(l))l.isRenderTargetTexture?(re("UniformsUtils: Textures of render targets cannot be cloned via cloneUniforms() or mergeUniforms()."),t[i][s]=null):t[i][s]=l.clone();else if(Array.isArray(l))if(Mv(l[0])){const c=[];for(let f=0,p=l.length;f<p;f++)c[f]=l[f].clone();t[i][s]=c}else t[i][s]=l.slice();else t[i][s]=l}}return t}function Wn(r){const t={};for(let i=0;i<r.length;i++){const s=so(r[i]);for(const l in s)t[l]=s[l]}return t}function Mv(r){return r&&(r.isColor||r.isMatrix3||r.isMatrix4||r.isVector2||r.isVector3||r.isVector4||r.isTexture||r.isQuaternion)}function qE(r){const t=[];for(let i=0;i<r.length;i++)t.push(r[i].clone());return t}function Lx(r){const t=r.getRenderTarget();return t===null?r.outputColorSpace:t.isXRRenderTarget===!0?t.texture.colorSpace:Ne.workingColorSpace}const YE={clone:so,merge:Wn};var ZE=`void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}`,jE=`void main() {
	gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
}`;class $i extends ps{constructor(t){super(),this.isShaderMaterial=!0,this.type="ShaderMaterial",this.defines={},this.uniforms={},this.uniformsGroups=[],this.vertexShader=ZE,this.fragmentShader=jE,this.linewidth=1,this.wireframe=!1,this.wireframeLinewidth=1,this.fog=!1,this.lights=!1,this.clipping=!1,this.forceSinglePass=!0,this.extensions={clipCullDistance:!1,multiDraw:!1},this.defaultAttributeValues={color:[1,1,1],uv:[0,0],uv1:[0,0]},this.index0AttributeName=void 0,this.uniformsNeedUpdate=!1,this.glslVersion=null,t!==void 0&&this.setValues(t)}copy(t){return super.copy(t),this.fragmentShader=t.fragmentShader,this.vertexShader=t.vertexShader,this.uniforms=so(t.uniforms),this.uniformsGroups=qE(t.uniformsGroups),this.defines=Object.assign({},t.defines),this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.fog=t.fog,this.lights=t.lights,this.clipping=t.clipping,this.extensions=Object.assign({},t.extensions),this.glslVersion=t.glslVersion,this.defaultAttributeValues=Object.assign({},t.defaultAttributeValues),this.index0AttributeName=t.index0AttributeName,this.uniformsNeedUpdate=t.uniformsNeedUpdate,this}toJSON(t){const i=super.toJSON(t);i.glslVersion=this.glslVersion,i.uniforms={};for(const l in this.uniforms){const f=this.uniforms[l].value;f&&f.isTexture?i.uniforms[l]={type:"t",value:f.toJSON(t).uuid}:f&&f.isColor?i.uniforms[l]={type:"c",value:f.getHex()}:f&&f.isVector2?i.uniforms[l]={type:"v2",value:f.toArray()}:f&&f.isVector3?i.uniforms[l]={type:"v3",value:f.toArray()}:f&&f.isVector4?i.uniforms[l]={type:"v4",value:f.toArray()}:f&&f.isMatrix3?i.uniforms[l]={type:"m3",value:f.toArray()}:f&&f.isMatrix4?i.uniforms[l]={type:"m4",value:f.toArray()}:i.uniforms[l]={value:f}}Object.keys(this.defines).length>0&&(i.defines=this.defines),i.vertexShader=this.vertexShader,i.fragmentShader=this.fragmentShader,i.lights=this.lights,i.clipping=this.clipping;const s={};for(const l in this.extensions)this.extensions[l]===!0&&(s[l]=!0);return Object.keys(s).length>0&&(i.extensions=s),i}fromJSON(t,i){if(super.fromJSON(t,i),t.uniforms!==void 0)for(const s in t.uniforms){const l=t.uniforms[s];switch(this.uniforms[s]={},l.type){case"t":this.uniforms[s].value=i[l.value]||null;break;case"c":this.uniforms[s].value=new ue().setHex(l.value);break;case"v2":this.uniforms[s].value=new fe().fromArray(l.value);break;case"v3":this.uniforms[s].value=new V().fromArray(l.value);break;case"v4":this.uniforms[s].value=new cn().fromArray(l.value);break;case"m3":this.uniforms[s].value=new de().fromArray(l.value);break;case"m4":this.uniforms[s].value=new Fe().fromArray(l.value);break;default:this.uniforms[s].value=l.value}}if(t.defines!==void 0&&(this.defines=t.defines),t.vertexShader!==void 0&&(this.vertexShader=t.vertexShader),t.fragmentShader!==void 0&&(this.fragmentShader=t.fragmentShader),t.glslVersion!==void 0&&(this.glslVersion=t.glslVersion),t.extensions!==void 0)for(const s in t.extensions)this.extensions[s]=t.extensions[s];return t.lights!==void 0&&(this.lights=t.lights),t.clipping!==void 0&&(this.clipping=t.clipping),this}}class KE extends $i{constructor(t){super(t),this.isRawShaderMaterial=!0,this.type="RawShaderMaterial"}}class pi extends ps{constructor(t){super(),this.isMeshStandardMaterial=!0,this.type="MeshStandardMaterial",this.defines={STANDARD:""},this.color=new ue(16777215),this.roughness=1,this.metalness=0,this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.emissive=new ue(0),this.emissiveIntensity=1,this.emissiveMap=null,this.bumpMap=null,this.bumpScale=1,this.normalMap=null,this.normalMapType=gp,this.normalScale=new fe(1,1),this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.roughnessMap=null,this.metalnessMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new ds,this.envMapIntensity=1,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.flatShading=!1,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.defines={STANDARD:""},this.color.copy(t.color),this.roughness=t.roughness,this.metalness=t.metalness,this.map=t.map,this.lightMap=t.lightMap,this.lightMapIntensity=t.lightMapIntensity,this.aoMap=t.aoMap,this.aoMapIntensity=t.aoMapIntensity,this.emissive.copy(t.emissive),this.emissiveMap=t.emissiveMap,this.emissiveIntensity=t.emissiveIntensity,this.bumpMap=t.bumpMap,this.bumpScale=t.bumpScale,this.normalMap=t.normalMap,this.normalMapType=t.normalMapType,this.normalScale.copy(t.normalScale),this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this.roughnessMap=t.roughnessMap,this.metalnessMap=t.metalnessMap,this.alphaMap=t.alphaMap,this.envMap=t.envMap,this.envMapRotation.copy(t.envMapRotation),this.envMapIntensity=t.envMapIntensity,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.wireframeLinecap=t.wireframeLinecap,this.wireframeLinejoin=t.wireframeLinejoin,this.flatShading=t.flatShading,this.fog=t.fog,this}}class QE extends pi{constructor(t){super(),this.isMeshPhysicalMaterial=!0,this.defines={STANDARD:"",PHYSICAL:""},this.type="MeshPhysicalMaterial",this.anisotropyRotation=0,this.anisotropyMap=null,this.clearcoatMap=null,this.clearcoatRoughness=0,this.clearcoatRoughnessMap=null,this.clearcoatNormalScale=new fe(1,1),this.clearcoatNormalMap=null,this.ior=1.5,Object.defineProperty(this,"reflectivity",{get:function(){return ye(2.5*(this.ior-1)/(this.ior+1),0,1)},set:function(i){this.ior=(1+.4*i)/(1-.4*i)}}),this.iridescenceMap=null,this.iridescenceIOR=1.3,this.iridescenceThicknessRange=[100,400],this.iridescenceThicknessMap=null,this.sheenColor=new ue(0),this.sheenColorMap=null,this.sheenRoughness=1,this.sheenRoughnessMap=null,this.transmissionMap=null,this.thickness=0,this.thicknessMap=null,this.attenuationDistance=1/0,this.attenuationColor=new ue(1,1,1),this.specularIntensity=1,this.specularIntensityMap=null,this.specularColor=new ue(1,1,1),this.specularColorMap=null,this._anisotropy=0,this._clearcoat=0,this._dispersion=0,this._iridescence=0,this._sheen=0,this._transmission=0,this.setValues(t)}get anisotropy(){return this._anisotropy}set anisotropy(t){this._anisotropy>0!=t>0&&this.version++,this._anisotropy=t}get clearcoat(){return this._clearcoat}set clearcoat(t){this._clearcoat>0!=t>0&&this.version++,this._clearcoat=t}get iridescence(){return this._iridescence}set iridescence(t){this._iridescence>0!=t>0&&this.version++,this._iridescence=t}get dispersion(){return this._dispersion}set dispersion(t){this._dispersion>0!=t>0&&this.version++,this._dispersion=t}get sheen(){return this._sheen}set sheen(t){this._sheen>0!=t>0&&this.version++,this._sheen=t}get transmission(){return this._transmission}set transmission(t){this._transmission>0!=t>0&&this.version++,this._transmission=t}copy(t){return super.copy(t),this.defines={STANDARD:"",PHYSICAL:""},this.anisotropy=t.anisotropy,this.anisotropyRotation=t.anisotropyRotation,this.anisotropyMap=t.anisotropyMap,this.clearcoat=t.clearcoat,this.clearcoatMap=t.clearcoatMap,this.clearcoatRoughness=t.clearcoatRoughness,this.clearcoatRoughnessMap=t.clearcoatRoughnessMap,this.clearcoatNormalMap=t.clearcoatNormalMap,this.clearcoatNormalScale.copy(t.clearcoatNormalScale),this.dispersion=t.dispersion,this.ior=t.ior,this.iridescence=t.iridescence,this.iridescenceMap=t.iridescenceMap,this.iridescenceIOR=t.iridescenceIOR,this.iridescenceThicknessRange=[...t.iridescenceThicknessRange],this.iridescenceThicknessMap=t.iridescenceThicknessMap,this.sheen=t.sheen,this.sheenColor.copy(t.sheenColor),this.sheenColorMap=t.sheenColorMap,this.sheenRoughness=t.sheenRoughness,this.sheenRoughnessMap=t.sheenRoughnessMap,this.transmission=t.transmission,this.transmissionMap=t.transmissionMap,this.thickness=t.thickness,this.thicknessMap=t.thicknessMap,this.attenuationDistance=t.attenuationDistance,this.attenuationColor.copy(t.attenuationColor),this.specularIntensity=t.specularIntensity,this.specularIntensityMap=t.specularIntensityMap,this.specularColor.copy(t.specularColor),this.specularColorMap=t.specularColorMap,this}}class JE extends ps{constructor(t){super(),this.isMeshDepthMaterial=!0,this.type="MeshDepthMaterial",this.depthPacking=k1,this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.wireframe=!1,this.wireframeLinewidth=1,this.setValues(t)}copy(t){return super.copy(t),this.depthPacking=t.depthPacking,this.map=t.map,this.alphaMap=t.alphaMap,this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this}}class $E extends ps{constructor(t){super(),this.isMeshDistanceMaterial=!0,this.type="MeshDistanceMaterial",this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.setValues(t)}copy(t){return super.copy(t),this.map=t.map,this.alphaMap=t.alphaMap,this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this}}class tb extends fs{constructor(t){super(),this.isLineDashedMaterial=!0,this.type="LineDashedMaterial",this.scale=1,this.dashSize=3,this.gapSize=1,this.setValues(t)}copy(t){return super.copy(t),this.scale=t.scale,this.dashSize=t.dashSize,this.gapSize=t.gapSize,this}}class qp extends Mn{constructor(t,i=1){super(),this.isLight=!0,this.type="Light",this.color=new ue(t),this.intensity=i}dispose(){this.dispatchEvent({type:"dispose"})}copy(t,i){return super.copy(t,i),this.color.copy(t.color),this.intensity=t.intensity,this}toJSON(t){const i=super.toJSON(t);return i.object.color=this.color.getHex(),i.object.intensity=this.intensity,i}}class eb extends qp{constructor(t,i,s){super(t,s),this.isHemisphereLight=!0,this.type="HemisphereLight",this.position.copy(Mn.DEFAULT_UP),this.updateMatrix(),this.groundColor=new ue(i)}copy(t,i){return super.copy(t,i),this.groundColor.copy(t.groundColor),this}toJSON(t){const i=super.toJSON(t);return i.object.groundColor=this.groundColor.getHex(),i}}const Sd=new Fe,Ev=new V,bv=new V;class Ox{constructor(t){this.camera=t,this.intensity=1,this.bias=0,this.biasNode=null,this.normalBias=0,this.radius=1,this.blurSamples=8,this.mapSize=new fe(512,512),this.mapType=gi,this.map=null,this.mapPass=null,this.matrix=new Fe,this.autoUpdate=!0,this.needsUpdate=!1,this._frustum=new Vp,this._frameExtents=new fe(1,1),this._viewportCount=1,this._viewports=[new cn(0,0,1,1)]}getViewportCount(){return this._viewportCount}getFrustum(){return this._frustum}updateMatrices(t){const i=this.camera,s=this.matrix;Ev.setFromMatrixPosition(t.matrixWorld),i.position.copy(Ev),bv.setFromMatrixPosition(t.target.matrixWorld),i.lookAt(bv),i.updateMatrixWorld(),Sd.multiplyMatrices(i.projectionMatrix,i.matrixWorldInverse),this._frustum.setFromProjectionMatrix(Sd,i.coordinateSystem,i.reversedDepth),i.coordinateSystem===yl||i.reversedDepth?s.set(.5,0,0,.5,0,.5,0,.5,0,0,1,0,0,0,0,1):s.set(.5,0,0,.5,0,.5,0,.5,0,0,.5,.5,0,0,0,1),s.multiply(Sd)}getViewport(t){return this._viewports[t]}getFrameExtents(){return this._frameExtents}dispose(){this.map&&this.map.dispose(),this.mapPass&&this.mapPass.dispose()}copy(t){return this.camera=t.camera.clone(),this.intensity=t.intensity,this.bias=t.bias,this.radius=t.radius,this.autoUpdate=t.autoUpdate,this.needsUpdate=t.needsUpdate,this.normalBias=t.normalBias,this.blurSamples=t.blurSamples,this.mapSize.copy(t.mapSize),this.biasNode=t.biasNode,this}clone(){return new this.constructor().copy(this)}toJSON(){const t={};return this.intensity!==1&&(t.intensity=this.intensity),this.bias!==0&&(t.bias=this.bias),this.normalBias!==0&&(t.normalBias=this.normalBias),this.radius!==1&&(t.radius=this.radius),(this.mapSize.x!==512||this.mapSize.y!==512)&&(t.mapSize=this.mapSize.toArray()),t.camera=this.camera.toJSON(!1).object,delete t.camera.matrix,t}}const lu=new V,cu=new ro,qi=new V;class Px extends Mn{constructor(){super(),this.isCamera=!0,this.type="Camera",this.matrixWorldInverse=new Fe,this.projectionMatrix=new Fe,this.projectionMatrixInverse=new Fe,this.coordinateSystem=ji,this._reversedDepth=!1}get reversedDepth(){return this._reversedDepth}copy(t,i){return super.copy(t,i),this.matrixWorldInverse.copy(t.matrixWorldInverse),this.projectionMatrix.copy(t.projectionMatrix),this.projectionMatrixInverse.copy(t.projectionMatrixInverse),this.coordinateSystem=t.coordinateSystem,this}getWorldDirection(t){return super.getWorldDirection(t).negate()}updateMatrixWorld(t){super.updateMatrixWorld(t),this.matrixWorld.decompose(lu,cu,qi),qi.x===1&&qi.y===1&&qi.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(lu,cu,qi.set(1,1,1)).invert()}updateWorldMatrix(t,i,s=!1){super.updateWorldMatrix(t,i,s),this.matrixWorld.decompose(lu,cu,qi),qi.x===1&&qi.y===1&&qi.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(lu,cu,qi.set(1,1,1)).invert()}clone(){return new this.constructor().copy(this)}}const ls=new V,Tv=new fe,Av=new fe;class mi extends Px{constructor(t=50,i=1,s=.1,l=2e3){super(),this.isPerspectiveCamera=!0,this.type="PerspectiveCamera",this.fov=t,this.zoom=1,this.near=s,this.far=l,this.focus=10,this.aspect=i,this.view=null,this.filmGauge=35,this.filmOffset=0,this.updateProjectionMatrix()}copy(t,i){return super.copy(t,i),this.fov=t.fov,this.zoom=t.zoom,this.near=t.near,this.far=t.far,this.focus=t.focus,this.aspect=t.aspect,this.view=t.view===null?null:Object.assign({},t.view),this.filmGauge=t.filmGauge,this.filmOffset=t.filmOffset,this}setFocalLength(t){const i=.5*this.getFilmHeight()/t;this.fov=Sl*2*Math.atan(i),this.updateProjectionMatrix()}getFocalLength(){const t=Math.tan($r*.5*this.fov);return .5*this.getFilmHeight()/t}getEffectiveFOV(){return Sl*2*Math.atan(Math.tan($r*.5*this.fov)/this.zoom)}getFilmWidth(){return this.filmGauge*Math.min(this.aspect,1)}getFilmHeight(){return this.filmGauge/Math.max(this.aspect,1)}getViewBounds(t,i,s){ls.set(-1,-1,.5).applyMatrix4(this.projectionMatrixInverse),i.set(ls.x,ls.y).multiplyScalar(-t/ls.z),ls.set(1,1,.5).applyMatrix4(this.projectionMatrixInverse),s.set(ls.x,ls.y).multiplyScalar(-t/ls.z)}getViewSize(t,i){return this.getViewBounds(t,Tv,Av),i.subVectors(Av,Tv)}setViewOffset(t,i,s,l,c,f){this.aspect=t/i,this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=t,this.view.fullHeight=i,this.view.offsetX=s,this.view.offsetY=l,this.view.width=c,this.view.height=f,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const t=this.near;let i=t*Math.tan($r*.5*this.fov)/this.zoom,s=2*i,l=this.aspect*s,c=-.5*l;const f=this.view;if(this.view!==null&&this.view.enabled){const m=f.fullWidth,d=f.fullHeight;c+=f.offsetX*l/m,i-=f.offsetY*s/d,l*=f.width/m,s*=f.height/d}const p=this.filmOffset;p!==0&&(c+=t*p/this.getFilmWidth()),this.projectionMatrix.makePerspective(c,c+l,i,i-s,t,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(t){const i=super.toJSON(t);return i.object.fov=this.fov,i.object.zoom=this.zoom,i.object.near=this.near,i.object.far=this.far,i.object.focus=this.focus,i.object.aspect=this.aspect,this.view!==null&&(i.object.view=Object.assign({},this.view)),i.object.filmGauge=this.filmGauge,i.object.filmOffset=this.filmOffset,i}}class nb extends Ox{constructor(){super(new mi(90,1,.5,500)),this.isPointLightShadow=!0}}class zx extends qp{constructor(t,i,s=0,l=2){super(t,i),this.isPointLight=!0,this.type="PointLight",this.distance=s,this.decay=l,this.shadow=new nb}get power(){return this.intensity*4*Math.PI}set power(t){this.intensity=t/(4*Math.PI)}dispose(){super.dispose(),this.shadow.dispose()}copy(t,i){return super.copy(t,i),this.distance=t.distance,this.decay=t.decay,this.shadow=t.shadow.clone(),this}toJSON(t){const i=super.toJSON(t);return i.object.distance=this.distance,i.object.decay=this.decay,i.object.shadow=this.shadow.toJSON(),i}}class Yp extends Px{constructor(t=-1,i=1,s=1,l=-1,c=.1,f=2e3){super(),this.isOrthographicCamera=!0,this.type="OrthographicCamera",this.zoom=1,this.view=null,this.left=t,this.right=i,this.top=s,this.bottom=l,this.near=c,this.far=f,this.updateProjectionMatrix()}copy(t,i){return super.copy(t,i),this.left=t.left,this.right=t.right,this.top=t.top,this.bottom=t.bottom,this.near=t.near,this.far=t.far,this.zoom=t.zoom,this.view=t.view===null?null:Object.assign({},t.view),this}setViewOffset(t,i,s,l,c,f){this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=t,this.view.fullHeight=i,this.view.offsetX=s,this.view.offsetY=l,this.view.width=c,this.view.height=f,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const t=(this.right-this.left)/(2*this.zoom),i=(this.top-this.bottom)/(2*this.zoom),s=(this.right+this.left)/2,l=(this.top+this.bottom)/2;let c=s-t,f=s+t,p=l+i,m=l-i;if(this.view!==null&&this.view.enabled){const d=(this.right-this.left)/this.view.fullWidth/this.zoom,_=(this.top-this.bottom)/this.view.fullHeight/this.zoom;c+=d*this.view.offsetX,f=c+d*this.view.width,p-=_*this.view.offsetY,m=p-_*this.view.height}this.projectionMatrix.makeOrthographic(c,f,p,m,this.near,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(t){const i=super.toJSON(t);return i.object.zoom=this.zoom,i.object.left=this.left,i.object.right=this.right,i.object.top=this.top,i.object.bottom=this.bottom,i.object.near=this.near,i.object.far=this.far,this.view!==null&&(i.object.view=Object.assign({},this.view)),i}}class ib extends Ox{constructor(){super(new Yp(-5,5,5,-5,.5,500)),this.isDirectionalLightShadow=!0}}class ab extends qp{constructor(t,i){super(t,i),this.isDirectionalLight=!0,this.type="DirectionalLight",this.position.copy(Mn.DEFAULT_UP),this.updateMatrix(),this.target=new Mn,this.shadow=new ib}dispose(){super.dispose(),this.shadow.dispose()}copy(t){return super.copy(t),this.target=t.target.clone(),this.shadow=t.shadow.clone(),this}toJSON(t){const i=super.toJSON(t);return i.object.shadow=this.shadow.toJSON(),i.object.target=this.target.uuid,i}}const Yr=-90,Zr=1;class sb extends Mn{constructor(t,i,s){super(),this.type="CubeCamera",this.renderTarget=s,this.coordinateSystem=null,this.activeMipmapLevel=0;const l=new mi(Yr,Zr,t,i);l.layers=this.layers,this.add(l);const c=new mi(Yr,Zr,t,i);c.layers=this.layers,this.add(c);const f=new mi(Yr,Zr,t,i);f.layers=this.layers,this.add(f);const p=new mi(Yr,Zr,t,i);p.layers=this.layers,this.add(p);const m=new mi(Yr,Zr,t,i);m.layers=this.layers,this.add(m);const d=new mi(Yr,Zr,t,i);d.layers=this.layers,this.add(d)}updateCoordinateSystem(){const t=this.coordinateSystem,i=this.children.concat(),[s,l,c,f,p,m]=i;for(const d of i)this.remove(d);if(t===ji)s.up.set(0,1,0),s.lookAt(1,0,0),l.up.set(0,1,0),l.lookAt(-1,0,0),c.up.set(0,0,-1),c.lookAt(0,1,0),f.up.set(0,0,1),f.lookAt(0,-1,0),p.up.set(0,1,0),p.lookAt(0,0,1),m.up.set(0,1,0),m.lookAt(0,0,-1);else if(t===yl)s.up.set(0,-1,0),s.lookAt(-1,0,0),l.up.set(0,-1,0),l.lookAt(1,0,0),c.up.set(0,0,1),c.lookAt(0,1,0),f.up.set(0,0,-1),f.lookAt(0,-1,0),p.up.set(0,-1,0),p.lookAt(0,0,1),m.up.set(0,-1,0),m.lookAt(0,0,-1);else throw new Error("THREE.CubeCamera.updateCoordinateSystem(): Invalid coordinate system: "+t);for(const d of i)this.add(d),d.updateMatrixWorld()}update(t,i){this.parent===null&&this.updateMatrixWorld();const{renderTarget:s,activeMipmapLevel:l}=this;this.coordinateSystem!==t.coordinateSystem&&(this.coordinateSystem=t.coordinateSystem,this.updateCoordinateSystem());const[c,f,p,m,d,_]=this.children,v=t.getRenderTarget(),g=t.getActiveCubeFace(),S=t.getActiveMipmapLevel(),E=t.xr.enabled;t.xr.enabled=!1;const A=s.texture.generateMipmaps;s.texture.generateMipmaps=!1;let M=!1;t.isWebGLRenderer===!0?M=t.state.buffers.depth.getReversed():M=t.reversedDepthBuffer,t.setRenderTarget(s,0,l),M&&t.autoClear===!1&&t.clearDepth(),t.render(i,c),t.setRenderTarget(s,1,l),M&&t.autoClear===!1&&t.clearDepth(),t.render(i,f),t.setRenderTarget(s,2,l),M&&t.autoClear===!1&&t.clearDepth(),t.render(i,p),t.setRenderTarget(s,3,l),M&&t.autoClear===!1&&t.clearDepth(),t.render(i,m),t.setRenderTarget(s,4,l),M&&t.autoClear===!1&&t.clearDepth(),t.render(i,d),s.texture.generateMipmaps=A,t.setRenderTarget(s,5,l),M&&t.autoClear===!1&&t.clearDepth(),t.render(i,_),t.setRenderTarget(v,g,S),t.xr.enabled=E,s.texture.needsPMREMUpdate=!0}}class rb extends mi{constructor(t=[]){super(),this.isArrayCamera=!0,this.isMultiViewCamera=!1,this.cameras=t}}const Rv=new Fe;class ob{constructor(t,i,s=0,l=1/0){this.ray=new wu(t,i),this.near=s,this.far=l,this.camera=null,this.layers=new Fp,this.params={Mesh:{},Line:{threshold:1},LOD:{},Points:{threshold:1},Sprite:{}}}set(t,i){this.ray.set(t,i)}setFromCamera(t,i){i.isPerspectiveCamera?(this.ray.origin.setFromMatrixPosition(i.matrixWorld),this.ray.direction.set(t.x,t.y,.5).unproject(i).sub(this.ray.origin).normalize(),this.camera=i):i.isOrthographicCamera?(this.ray.origin.set(t.x,t.y,i.projectionMatrix.elements[14]).unproject(i),this.ray.direction.set(0,0,-1).transformDirection(i.matrixWorld),this.camera=i):Ue("Raycaster: Unsupported camera type: "+i.type)}setFromXRController(t){return Rv.identity().extractRotation(t.matrixWorld),this.ray.origin.setFromMatrixPosition(t.matrixWorld),this.ray.direction.set(0,0,-1).applyMatrix4(Rv),this}intersectObject(t,i=!0,s=[]){return yp(t,this,s,i),s.sort(Cv),s}intersectObjects(t,i=!0,s=[]){for(let l=0,c=t.length;l<c;l++)yp(t[l],this,s,i);return s.sort(Cv),s}}function Cv(r,t){return r.distance-t.distance}function yp(r,t,i,s){let l=!0;if(r.layers.test(t.layers)&&r.raycast(t,i)===!1&&(l=!1),l===!0&&s===!0){const c=r.children;for(let f=0,p=c.length;f<p;f++)yp(c[f],t,i,!0)}}const Jp=class Jp{constructor(t,i,s,l){this.elements=[1,0,0,1],t!==void 0&&this.set(t,i,s,l)}identity(){return this.set(1,0,0,1),this}fromArray(t,i=0){for(let s=0;s<4;s++)this.elements[s]=t[s+i];return this}set(t,i,s,l){const c=this.elements;return c[0]=t,c[2]=i,c[1]=s,c[3]=l,this}};Jp.prototype.isMatrix2=!0;let wv=Jp;class lb extends Cx{constructor(t=10,i=10,s=4473924,l=8947848){s=new ue(s),l=new ue(l);const c=i/2,f=t/i,p=t/2,m=[],d=[];for(let g=0,S=0,E=-p;g<=i;g++,E+=f){m.push(-p,0,E,p,0,E),m.push(E,0,-p,E,0,p);const A=g===c?s:l;A.toArray(d,S),S+=3,A.toArray(d,S),S+=3,A.toArray(d,S),S+=3,A.toArray(d,S),S+=3}const _=new hn;_.setAttribute("position",new En(m,3)),_.setAttribute("color",new En(d,3));const v=new fs({vertexColors:!0,toneMapped:!1});super(_,v),this.type="GridHelper"}dispose(){this.geometry.dispose(),this.material.dispose()}}function Dv(r,t,i,s){const l=cb(s);switch(i){case vx:return r*t;case Up:return r*t/l.components*l.byteLength;case Np:return r*t/l.components*l.byteLength;case js:return r*t*2/l.components*l.byteLength;case Lp:return r*t*2/l.components*l.byteLength;case xx:return r*t*3/l.components*l.byteLength;case Fi:return r*t*4/l.components*l.byteLength;case Op:return r*t*4/l.components*l.byteLength;case hu:case du:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*8;case pu:case mu:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*16;case Gd:case kd:return Math.max(r,16)*Math.max(t,8)/4;case Hd:case Vd:return Math.max(r,8)*Math.max(t,8)/2;case Xd:case Wd:case Yd:case Zd:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*8;case qd:case vu:case jd:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*16;case Kd:return Math.floor((r+3)/4)*Math.floor((t+3)/4)*16;case Qd:return Math.floor((r+4)/5)*Math.floor((t+3)/4)*16;case Jd:return Math.floor((r+4)/5)*Math.floor((t+4)/5)*16;case $d:return Math.floor((r+5)/6)*Math.floor((t+4)/5)*16;case tp:return Math.floor((r+5)/6)*Math.floor((t+5)/6)*16;case ep:return Math.floor((r+7)/8)*Math.floor((t+4)/5)*16;case np:return Math.floor((r+7)/8)*Math.floor((t+5)/6)*16;case ip:return Math.floor((r+7)/8)*Math.floor((t+7)/8)*16;case ap:return Math.floor((r+9)/10)*Math.floor((t+4)/5)*16;case sp:return Math.floor((r+9)/10)*Math.floor((t+5)/6)*16;case rp:return Math.floor((r+9)/10)*Math.floor((t+7)/8)*16;case op:return Math.floor((r+9)/10)*Math.floor((t+9)/10)*16;case lp:return Math.floor((r+11)/12)*Math.floor((t+9)/10)*16;case cp:return Math.floor((r+11)/12)*Math.floor((t+11)/12)*16;case up:case fp:case hp:return Math.ceil(r/4)*Math.ceil(t/4)*16;case dp:case pp:return Math.ceil(r/4)*Math.ceil(t/4)*8;case xu:case mp:return Math.ceil(r/4)*Math.ceil(t/4)*16}throw new Error(`Unable to determine texture byte length for ${i} format.`)}function cb(r){switch(r){case gi:case px:return{byteLength:1,components:1};case vl:case mx:case Ca:return{byteLength:2,components:1};case wp:case Dp:return{byteLength:2,components:4};case Ji:case Cp:case Ii:return{byteLength:4,components:1};case gx:case _x:return{byteLength:4,components:3}}throw new Error(`THREE.TextureUtils: Unknown texture type ${r}.`)}typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("register",{detail:{revision:Ap}}));typeof window<"u"&&(window.__THREE__?re("WARNING: Multiple instances of Three.js being imported."):window.__THREE__=Ap);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */function Bx(){let r=null,t=!1,i=null,s=null;function l(c,f){i(c,f),s=r.requestAnimationFrame(l)}return{start:function(){t!==!0&&i!==null&&r!==null&&(s=r.requestAnimationFrame(l),t=!0)},stop:function(){r!==null&&r.cancelAnimationFrame(s),t=!1},setAnimationLoop:function(c){i=c},setContext:function(c){r=c}}}function ub(r){const t=new WeakMap;function i(p,m){const d=p.array,_=p.usage,v=d.byteLength,g=r.createBuffer();r.bindBuffer(m,g),r.bufferData(m,d,_),p.onUploadCallback();let S;if(d instanceof Float32Array)S=r.FLOAT;else if(typeof Float16Array<"u"&&d instanceof Float16Array)S=r.HALF_FLOAT;else if(d instanceof Uint16Array)p.isFloat16BufferAttribute?S=r.HALF_FLOAT:S=r.UNSIGNED_SHORT;else if(d instanceof Int16Array)S=r.SHORT;else if(d instanceof Uint32Array)S=r.UNSIGNED_INT;else if(d instanceof Int32Array)S=r.INT;else if(d instanceof Int8Array)S=r.BYTE;else if(d instanceof Uint8Array)S=r.UNSIGNED_BYTE;else if(d instanceof Uint8ClampedArray)S=r.UNSIGNED_BYTE;else throw new Error("THREE.WebGLAttributes: Unsupported buffer data format: "+d);return{buffer:g,type:S,bytesPerElement:d.BYTES_PER_ELEMENT,version:p.version,size:v}}function s(p,m,d){const _=m.array,v=m.updateRanges;if(r.bindBuffer(d,p),v.length===0)r.bufferSubData(d,0,_);else{v.sort((S,E)=>S.start-E.start);let g=0;for(let S=1;S<v.length;S++){const E=v[g],A=v[S];A.start<=E.start+E.count+1?E.count=Math.max(E.count,A.start+A.count-E.start):(++g,v[g]=A)}v.length=g+1;for(let S=0,E=v.length;S<E;S++){const A=v[S];r.bufferSubData(d,A.start*_.BYTES_PER_ELEMENT,_,A.start,A.count)}m.clearUpdateRanges()}m.onUploadCallback()}function l(p){return p.isInterleavedBufferAttribute&&(p=p.data),t.get(p)}function c(p){p.isInterleavedBufferAttribute&&(p=p.data);const m=t.get(p);m&&(r.deleteBuffer(m.buffer),t.delete(p))}function f(p,m){if(p.isInterleavedBufferAttribute&&(p=p.data),p.isGLBufferAttribute){const _=t.get(p);(!_||_.version<p.version)&&t.set(p,{buffer:p.buffer,type:p.type,bytesPerElement:p.elementSize,version:p.version});return}const d=t.get(p);if(d===void 0)t.set(p,i(p,m));else if(d.version<p.version){if(d.size!==p.array.byteLength)throw new Error("THREE.WebGLAttributes: The size of the buffer attribute's array buffer does not match the original size. Resizing buffer attributes is not supported.");s(d.buffer,p,m),d.version=p.version}}return{get:l,remove:c,update:f}}var fb=`#ifdef USE_ALPHAHASH
	if ( diffuseColor.a < getAlphaHashThreshold( vPosition ) ) discard;
#endif`,hb=`#ifdef USE_ALPHAHASH
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
#endif`,db=`#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, vAlphaMapUv ).g;
#endif`,pb=`#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,mb=`#ifdef USE_ALPHATEST
	#ifdef ALPHA_TO_COVERAGE
	diffuseColor.a = smoothstep( alphaTest, alphaTest + fwidth( diffuseColor.a ), diffuseColor.a );
	if ( diffuseColor.a == 0.0 ) discard;
	#else
	if ( diffuseColor.a < alphaTest ) discard;
	#endif
#endif`,gb=`#ifdef USE_ALPHATEST
	uniform float alphaTest;
#endif`,_b=`#ifdef USE_AOMAP
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
#endif`,vb=`#ifdef USE_AOMAP
	uniform sampler2D aoMap;
	uniform float aoMapIntensity;
#endif`,xb=`#ifdef USE_BATCHING
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
#endif`,yb=`#ifdef USE_BATCHING
	mat4 batchingMatrix = getBatchingMatrix( getIndirectIndex( gl_DrawID ) );
#endif`,Sb=`vec3 transformed = vec3( position );
#ifdef USE_ALPHAHASH
	vPosition = vec3( position );
#endif`,Mb=`vec3 objectNormal = vec3( normal );
#ifdef USE_TANGENT
	vec3 objectTangent = vec3( tangent.xyz );
#endif`,Eb=`float G_BlinnPhong_Implicit( ) {
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
} // validated`,bb=`#ifdef USE_IRIDESCENCE
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
#endif`,Tb=`#ifdef USE_BUMPMAP
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
#endif`,Ab=`#if NUM_CLIPPING_PLANES > 0
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
#endif`,Rb=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
	uniform vec4 clippingPlanes[ NUM_CLIPPING_PLANES ];
#endif`,Cb=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
#endif`,wb=`#if NUM_CLIPPING_PLANES > 0
	vClipPosition = - mvPosition.xyz;
#endif`,Db=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	diffuseColor *= vColor;
#endif`,Ub=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#endif`,Nb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	varying vec4 vColor;
#endif`,Lb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
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
#endif`,Ob=`#define PI 3.141592653589793
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
} // validated`,Pb=`#ifdef ENVMAP_TYPE_CUBE_UV
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
#endif`,zb=`vec3 transformedNormal = objectNormal;
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
#endif`,Bb=`#ifdef USE_DISPLACEMENTMAP
	uniform sampler2D displacementMap;
	uniform float displacementScale;
	uniform float displacementBias;
#endif`,Ib=`#ifdef USE_DISPLACEMENTMAP
	transformed += normalize( objectNormal ) * ( texture2D( displacementMap, vDisplacementMapUv ).x * displacementScale + displacementBias );
#endif`,Fb=`#ifdef USE_EMISSIVEMAP
	vec4 emissiveColor = texture2D( emissiveMap, vEmissiveMapUv );
	#ifdef DECODE_VIDEO_TEXTURE_EMISSIVE
		emissiveColor = sRGBTransferEOTF( emissiveColor );
	#endif
	totalEmissiveRadiance *= emissiveColor.rgb;
#endif`,Hb=`#ifdef USE_EMISSIVEMAP
	uniform sampler2D emissiveMap;
#endif`,Gb="gl_FragColor = linearToOutputTexel( gl_FragColor );",Vb=`vec4 LinearTransferOETF( in vec4 value ) {
	return value;
}
vec4 sRGBTransferEOTF( in vec4 value ) {
	return vec4( mix( pow( value.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), value.rgb * 0.0773993808, vec3( lessThanEqual( value.rgb, vec3( 0.04045 ) ) ) ), value.a );
}
vec4 sRGBTransferOETF( in vec4 value ) {
	return vec4( mix( pow( value.rgb, vec3( 0.41666 ) ) * 1.055 - vec3( 0.055 ), value.rgb * 12.92, vec3( lessThanEqual( value.rgb, vec3( 0.0031308 ) ) ) ), value.a );
}`,kb=`#ifdef USE_ENVMAP
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
#endif`,Xb=`#ifdef USE_ENVMAP
	uniform float envMapIntensity;
	uniform mat3 envMapRotation;
	#ifdef ENVMAP_TYPE_CUBE
		uniform samplerCube envMap;
	#else
		uniform sampler2D envMap;
	#endif
#endif`,Wb=`#ifdef USE_ENVMAP
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
#endif`,qb=`#ifdef USE_ENVMAP
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		
		varying vec3 vWorldPosition;
	#else
		varying vec3 vReflect;
		uniform float refractionRatio;
	#endif
#endif`,Yb=`#ifdef USE_ENVMAP
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
#endif`,Zb=`#ifdef USE_FOG
	vFogDepth = - mvPosition.z;
#endif`,jb=`#ifdef USE_FOG
	varying float vFogDepth;
#endif`,Kb=`#ifdef USE_FOG
	#ifdef FOG_EXP2
		float fogFactor = 1.0 - exp( - fogDensity * fogDensity * vFogDepth * vFogDepth );
	#else
		float fogFactor = smoothstep( fogNear, fogFar, vFogDepth );
	#endif
	gl_FragColor.rgb = mix( gl_FragColor.rgb, fogColor, fogFactor );
#endif`,Qb=`#ifdef USE_FOG
	uniform vec3 fogColor;
	varying float vFogDepth;
	#ifdef FOG_EXP2
		uniform float fogDensity;
	#else
		uniform float fogNear;
		uniform float fogFar;
	#endif
#endif`,Jb=`#ifdef USE_GRADIENTMAP
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
}`,$b=`#ifdef USE_LIGHTMAP
	uniform sampler2D lightMap;
	uniform float lightMapIntensity;
#endif`,tT=`LambertMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularStrength = specularStrength;`,eT=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Lambert`,nT=`uniform bool receiveShadow;
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
#include <lightprobes_pars_fragment>`,iT=`#ifdef USE_ENVMAP
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
#endif`,aT=`ToonMaterial material;
material.diffuseColor = diffuseColor.rgb;`,sT=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Toon`,rT=`BlinnPhongMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularColor = specular;
material.specularShininess = shininess;
material.specularStrength = specularStrength;`,oT=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_BlinnPhong`,lT=`PhysicalMaterial material;
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
#endif`,cT=`uniform sampler2D dfgLUT;
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
}`,uT=`
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
#endif`,fT=`#if defined( RE_IndirectDiffuse )
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
#endif`,hT=`#if defined( RE_IndirectDiffuse )
	#if defined( LAMBERT ) || defined( PHONG )
		irradiance += iblIrradiance;
	#endif
	RE_IndirectDiffuse( irradiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif
#if defined( RE_IndirectSpecular )
	RE_IndirectSpecular( radiance, iblIrradiance, clearcoatRadiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif`,dT=`#ifdef USE_LIGHT_PROBES_GRID
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
#endif`,pT=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	gl_FragDepth = vIsPerspective == 0.0 ? gl_FragCoord.z : log2( vFragDepth ) * logDepthBufFC * 0.5;
#endif`,mT=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	uniform float logDepthBufFC;
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,gT=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,_T=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	vFragDepth = 1.0 + gl_Position.w;
	vIsPerspective = float( isPerspectiveMatrix( projectionMatrix ) );
#endif`,vT=`#ifdef USE_MAP
	vec4 sampledDiffuseColor = texture2D( map, vMapUv );
	#ifdef DECODE_VIDEO_TEXTURE
		sampledDiffuseColor = sRGBTransferEOTF( sampledDiffuseColor );
	#endif
	diffuseColor *= sampledDiffuseColor;
#endif`,xT=`#ifdef USE_MAP
	uniform sampler2D map;
#endif`,yT=`#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
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
#endif`,ST=`#if defined( USE_POINTS_UV )
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
#endif`,MT=`float metalnessFactor = metalness;
#ifdef USE_METALNESSMAP
	vec4 texelMetalness = texture2D( metalnessMap, vMetalnessMapUv );
	metalnessFactor *= texelMetalness.b;
#endif`,ET=`#ifdef USE_METALNESSMAP
	uniform sampler2D metalnessMap;
#endif`,bT=`#ifdef USE_INSTANCING_MORPH
	float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	float morphTargetBaseInfluence = texelFetch( morphTexture, ivec2( 0, gl_InstanceID ), 0 ).r;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		morphTargetInfluences[i] =  texelFetch( morphTexture, ivec2( i + 1, gl_InstanceID ), 0 ).r;
	}
#endif`,TT=`#if defined( USE_MORPHCOLORS )
	vColor *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		#if defined( USE_COLOR_ALPHA )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ) * morphTargetInfluences[ i ];
		#elif defined( USE_COLOR )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ).rgb * morphTargetInfluences[ i ];
		#endif
	}
#endif`,AT=`#ifdef USE_MORPHNORMALS
	objectNormal *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) objectNormal += getMorph( gl_VertexID, i, 1 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,RT=`#ifdef USE_MORPHTARGETS
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
#endif`,CT=`#ifdef USE_MORPHTARGETS
	transformed *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) transformed += getMorph( gl_VertexID, i, 0 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,wT=`float faceDirection = gl_FrontFacing ? 1.0 : - 1.0;
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
vec3 nonPerturbedNormal = normal;`,DT=`#ifdef USE_NORMALMAP_OBJECTSPACE
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
#endif`,UT=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,NT=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,LT=`#ifndef FLAT_SHADED
	vNormal = normalize( transformedNormal );
	#ifdef USE_TANGENT
		vTangent = normalize( transformedTangent );
		vBitangent = normalize( cross( vNormal, vTangent ) * tangent.w );
		#ifdef FLIP_SIDED
			vBitangent = - vBitangent;
		#endif
	#endif
#endif`,OT=`#ifdef USE_NORMALMAP
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
#endif`,PT=`#ifdef USE_CLEARCOAT
	vec3 clearcoatNormal = nonPerturbedNormal;
#endif`,zT=`#ifdef USE_CLEARCOAT_NORMALMAP
	vec3 clearcoatMapN = texture2D( clearcoatNormalMap, vClearcoatNormalMapUv ).xyz * 2.0 - 1.0;
	clearcoatMapN.xy *= clearcoatNormalScale;
	clearcoatNormal = normalize( tbn2 * clearcoatMapN );
#endif`,BT=`#ifdef USE_CLEARCOATMAP
	uniform sampler2D clearcoatMap;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform sampler2D clearcoatNormalMap;
	uniform vec2 clearcoatNormalScale;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform sampler2D clearcoatRoughnessMap;
#endif`,IT=`#ifdef USE_IRIDESCENCEMAP
	uniform sampler2D iridescenceMap;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform sampler2D iridescenceThicknessMap;
#endif`,FT=`#ifdef OPAQUE
diffuseColor.a = 1.0;
#endif
#ifdef USE_TRANSMISSION
diffuseColor.a *= material.transmissionAlpha;
#endif
gl_FragColor = vec4( outgoingLight, diffuseColor.a );`,HT=`vec3 packNormalToRGB( const in vec3 normal ) {
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
}`,GT=`#ifdef PREMULTIPLIED_ALPHA
	gl_FragColor.rgb *= gl_FragColor.a;
#endif`,VT=`vec4 mvPosition = vec4( transformed, 1.0 );
#ifdef USE_BATCHING
	mvPosition = batchingMatrix * mvPosition;
#endif
#ifdef USE_INSTANCING
	mvPosition = instanceMatrix * mvPosition;
#endif
mvPosition = modelViewMatrix * mvPosition;
gl_Position = projectionMatrix * mvPosition;`,kT=`#ifdef DITHERING
	gl_FragColor.rgb = dithering( gl_FragColor.rgb );
#endif`,XT=`#ifdef DITHERING
	vec3 dithering( vec3 color ) {
		float grid_position = rand( gl_FragCoord.xy );
		vec3 dither_shift_RGB = vec3( 0.25 / 255.0, -0.25 / 255.0, 0.25 / 255.0 );
		dither_shift_RGB = mix( 2.0 * dither_shift_RGB, -2.0 * dither_shift_RGB, grid_position );
		return color + dither_shift_RGB;
	}
#endif`,WT=`float roughnessFactor = roughness;
#ifdef USE_ROUGHNESSMAP
	vec4 texelRoughness = texture2D( roughnessMap, vRoughnessMapUv );
	roughnessFactor *= texelRoughness.g;
#endif`,qT=`#ifdef USE_ROUGHNESSMAP
	uniform sampler2D roughnessMap;
#endif`,YT=`#if NUM_SPOT_LIGHT_COORDS > 0
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
#endif`,ZT=`#if NUM_SPOT_LIGHT_COORDS > 0
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
#endif`,jT=`#if ( defined( USE_SHADOWMAP ) && ( NUM_DIR_LIGHT_SHADOWS > 0 || NUM_POINT_LIGHT_SHADOWS > 0 ) ) || ( NUM_SPOT_LIGHT_COORDS > 0 )
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
#endif`,KT=`float getShadowMask() {
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
}`,QT=`#ifdef USE_SKINNING
	mat4 boneMatX = getBoneMatrix( skinIndex.x );
	mat4 boneMatY = getBoneMatrix( skinIndex.y );
	mat4 boneMatZ = getBoneMatrix( skinIndex.z );
	mat4 boneMatW = getBoneMatrix( skinIndex.w );
#endif`,JT=`#ifdef USE_SKINNING
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
#endif`,$T=`#ifdef USE_SKINNING
	vec4 skinVertex = bindMatrix * vec4( transformed, 1.0 );
	vec4 skinned = vec4( 0.0 );
	skinned += boneMatX * skinVertex * skinWeight.x;
	skinned += boneMatY * skinVertex * skinWeight.y;
	skinned += boneMatZ * skinVertex * skinWeight.z;
	skinned += boneMatW * skinVertex * skinWeight.w;
	transformed = ( bindMatrixInverse * skinned ).xyz;
#endif`,tA=`#ifdef USE_SKINNING
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
#endif`,eA=`float specularStrength;
#ifdef USE_SPECULARMAP
	vec4 texelSpecular = texture2D( specularMap, vSpecularMapUv );
	specularStrength = texelSpecular.r;
#else
	specularStrength = 1.0;
#endif`,nA=`#ifdef USE_SPECULARMAP
	uniform sampler2D specularMap;
#endif`,iA=`#if defined( TONE_MAPPING )
	gl_FragColor.rgb = toneMapping( gl_FragColor.rgb );
#endif`,aA=`#ifndef saturate
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
vec3 CustomToneMapping( vec3 color ) { return color; }`,sA=`#ifdef USE_TRANSMISSION
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
#endif`,rA=`#ifdef USE_TRANSMISSION
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
#endif`,oA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,lA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,cA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,uA=`#if defined( USE_ENVMAP ) || defined( DISTANCE ) || defined ( USE_SHADOWMAP ) || defined ( USE_TRANSMISSION ) || NUM_SPOT_LIGHT_COORDS > 0
	vec4 worldPosition = vec4( transformed, 1.0 );
	#ifdef USE_BATCHING
		worldPosition = batchingMatrix * worldPosition;
	#endif
	#ifdef USE_INSTANCING
		worldPosition = instanceMatrix * worldPosition;
	#endif
	worldPosition = modelMatrix * worldPosition;
#endif`;const fA=`varying vec2 vUv;
uniform mat3 uvTransform;
void main() {
	vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	gl_Position = vec4( position.xy, 1.0, 1.0 );
}`,hA=`uniform sampler2D t2D;
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
}`,dA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,pA=`#ifdef ENVMAP_TYPE_CUBE
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
}`,mA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,gA=`uniform samplerCube tCube;
uniform float tFlip;
uniform float opacity;
varying vec3 vWorldDirection;
void main() {
	vec4 texColor = textureCube( tCube, vec3( tFlip * vWorldDirection.x, vWorldDirection.yz ) );
	gl_FragColor = texColor;
	gl_FragColor.a *= opacity;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,_A=`#include <common>
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
}`,vA=`#if DEPTH_PACKING == 3200
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
}`,xA=`#define DISTANCE
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
}`,yA=`#define DISTANCE
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
}`,SA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
}`,MA=`uniform sampler2D tEquirect;
varying vec3 vWorldDirection;
#include <common>
void main() {
	vec3 direction = normalize( vWorldDirection );
	vec2 sampleUV = equirectUv( direction );
	gl_FragColor = texture2D( tEquirect, sampleUV );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,EA=`uniform float scale;
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
}`,bA=`uniform vec3 diffuse;
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
}`,TA=`#include <common>
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
}`,AA=`uniform vec3 diffuse;
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
}`,RA=`#define LAMBERT
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
}`,CA=`#define LAMBERT
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
}`,wA=`#define MATCAP
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
}`,DA=`#define MATCAP
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
}`,UA=`#define NORMAL
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
}`,NA=`#define NORMAL
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
}`,LA=`#define PHONG
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
}`,OA=`#define PHONG
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
}`,PA=`#define STANDARD
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
}`,zA=`#define STANDARD
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
}`,BA=`#define TOON
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
}`,IA=`#define TOON
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
}`,FA=`uniform float size;
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
}`,HA=`uniform vec3 diffuse;
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
}`,GA=`#include <common>
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
}`,VA=`uniform vec3 color;
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
}`,kA=`uniform float rotation;
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
}`,XA=`uniform vec3 diffuse;
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
}`,xe={alphahash_fragment:fb,alphahash_pars_fragment:hb,alphamap_fragment:db,alphamap_pars_fragment:pb,alphatest_fragment:mb,alphatest_pars_fragment:gb,aomap_fragment:_b,aomap_pars_fragment:vb,batching_pars_vertex:xb,batching_vertex:yb,begin_vertex:Sb,beginnormal_vertex:Mb,bsdfs:Eb,iridescence_fragment:bb,bumpmap_pars_fragment:Tb,clipping_planes_fragment:Ab,clipping_planes_pars_fragment:Rb,clipping_planes_pars_vertex:Cb,clipping_planes_vertex:wb,color_fragment:Db,color_pars_fragment:Ub,color_pars_vertex:Nb,color_vertex:Lb,common:Ob,cube_uv_reflection_fragment:Pb,defaultnormal_vertex:zb,displacementmap_pars_vertex:Bb,displacementmap_vertex:Ib,emissivemap_fragment:Fb,emissivemap_pars_fragment:Hb,colorspace_fragment:Gb,colorspace_pars_fragment:Vb,envmap_fragment:kb,envmap_common_pars_fragment:Xb,envmap_pars_fragment:Wb,envmap_pars_vertex:qb,envmap_physical_pars_fragment:iT,envmap_vertex:Yb,fog_vertex:Zb,fog_pars_vertex:jb,fog_fragment:Kb,fog_pars_fragment:Qb,gradientmap_pars_fragment:Jb,lightmap_pars_fragment:$b,lights_lambert_fragment:tT,lights_lambert_pars_fragment:eT,lights_pars_begin:nT,lights_toon_fragment:aT,lights_toon_pars_fragment:sT,lights_phong_fragment:rT,lights_phong_pars_fragment:oT,lights_physical_fragment:lT,lights_physical_pars_fragment:cT,lights_fragment_begin:uT,lights_fragment_maps:fT,lights_fragment_end:hT,lightprobes_pars_fragment:dT,logdepthbuf_fragment:pT,logdepthbuf_pars_fragment:mT,logdepthbuf_pars_vertex:gT,logdepthbuf_vertex:_T,map_fragment:vT,map_pars_fragment:xT,map_particle_fragment:yT,map_particle_pars_fragment:ST,metalnessmap_fragment:MT,metalnessmap_pars_fragment:ET,morphinstance_vertex:bT,morphcolor_vertex:TT,morphnormal_vertex:AT,morphtarget_pars_vertex:RT,morphtarget_vertex:CT,normal_fragment_begin:wT,normal_fragment_maps:DT,normal_pars_fragment:UT,normal_pars_vertex:NT,normal_vertex:LT,normalmap_pars_fragment:OT,clearcoat_normal_fragment_begin:PT,clearcoat_normal_fragment_maps:zT,clearcoat_pars_fragment:BT,iridescence_pars_fragment:IT,opaque_fragment:FT,packing:HT,premultiplied_alpha_fragment:GT,project_vertex:VT,dithering_fragment:kT,dithering_pars_fragment:XT,roughnessmap_fragment:WT,roughnessmap_pars_fragment:qT,shadowmap_pars_fragment:YT,shadowmap_pars_vertex:ZT,shadowmap_vertex:jT,shadowmask_pars_fragment:KT,skinbase_vertex:QT,skinning_pars_vertex:JT,skinning_vertex:$T,skinnormal_vertex:tA,specularmap_fragment:eA,specularmap_pars_fragment:nA,tonemapping_fragment:iA,tonemapping_pars_fragment:aA,transmission_fragment:sA,transmission_pars_fragment:rA,uv_pars_fragment:oA,uv_pars_vertex:lA,uv_vertex:cA,worldpos_vertex:uA,background_vert:fA,background_frag:hA,backgroundCube_vert:dA,backgroundCube_frag:pA,cube_vert:mA,cube_frag:gA,depth_vert:_A,depth_frag:vA,distance_vert:xA,distance_frag:yA,equirect_vert:SA,equirect_frag:MA,linedashed_vert:EA,linedashed_frag:bA,meshbasic_vert:TA,meshbasic_frag:AA,meshlambert_vert:RA,meshlambert_frag:CA,meshmatcap_vert:wA,meshmatcap_frag:DA,meshnormal_vert:UA,meshnormal_frag:NA,meshphong_vert:LA,meshphong_frag:OA,meshphysical_vert:PA,meshphysical_frag:zA,meshtoon_vert:BA,meshtoon_frag:IA,points_vert:FA,points_frag:HA,shadow_vert:GA,shadow_frag:VA,sprite_vert:kA,sprite_frag:XA},It={common:{diffuse:{value:new ue(16777215)},opacity:{value:1},map:{value:null},mapTransform:{value:new de},alphaMap:{value:null},alphaMapTransform:{value:new de},alphaTest:{value:0}},specularmap:{specularMap:{value:null},specularMapTransform:{value:new de}},envmap:{envMap:{value:null},envMapRotation:{value:new de},reflectivity:{value:1},ior:{value:1.5},refractionRatio:{value:.98},dfgLUT:{value:null}},aomap:{aoMap:{value:null},aoMapIntensity:{value:1},aoMapTransform:{value:new de}},lightmap:{lightMap:{value:null},lightMapIntensity:{value:1},lightMapTransform:{value:new de}},bumpmap:{bumpMap:{value:null},bumpMapTransform:{value:new de},bumpScale:{value:1}},normalmap:{normalMap:{value:null},normalMapTransform:{value:new de},normalScale:{value:new fe(1,1)}},displacementmap:{displacementMap:{value:null},displacementMapTransform:{value:new de},displacementScale:{value:1},displacementBias:{value:0}},emissivemap:{emissiveMap:{value:null},emissiveMapTransform:{value:new de}},metalnessmap:{metalnessMap:{value:null},metalnessMapTransform:{value:new de}},roughnessmap:{roughnessMap:{value:null},roughnessMapTransform:{value:new de}},gradientmap:{gradientMap:{value:null}},fog:{fogDensity:{value:25e-5},fogNear:{value:1},fogFar:{value:2e3},fogColor:{value:new ue(16777215)}},lights:{ambientLightColor:{value:[]},lightProbe:{value:[]},directionalLights:{value:[],properties:{direction:{},color:{}}},directionalLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},directionalShadowMatrix:{value:[]},spotLights:{value:[],properties:{color:{},position:{},direction:{},distance:{},coneCos:{},penumbraCos:{},decay:{}}},spotLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},spotLightMap:{value:[]},spotLightMatrix:{value:[]},pointLights:{value:[],properties:{color:{},position:{},decay:{},distance:{}}},pointLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{},shadowCameraNear:{},shadowCameraFar:{}}},pointShadowMatrix:{value:[]},hemisphereLights:{value:[],properties:{direction:{},skyColor:{},groundColor:{}}},rectAreaLights:{value:[],properties:{color:{},position:{},width:{},height:{}}},ltc_1:{value:null},ltc_2:{value:null},probesSH:{value:null},probesMin:{value:new V},probesMax:{value:new V},probesResolution:{value:new V}},points:{diffuse:{value:new ue(16777215)},opacity:{value:1},size:{value:1},scale:{value:1},map:{value:null},alphaMap:{value:null},alphaMapTransform:{value:new de},alphaTest:{value:0},uvTransform:{value:new de}},sprite:{diffuse:{value:new ue(16777215)},opacity:{value:1},center:{value:new fe(.5,.5)},rotation:{value:0},map:{value:null},mapTransform:{value:new de},alphaMap:{value:null},alphaMapTransform:{value:new de},alphaTest:{value:0}}},Zi={basic:{uniforms:Wn([It.common,It.specularmap,It.envmap,It.aomap,It.lightmap,It.fog]),vertexShader:xe.meshbasic_vert,fragmentShader:xe.meshbasic_frag},lambert:{uniforms:Wn([It.common,It.specularmap,It.envmap,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.fog,It.lights,{emissive:{value:new ue(0)},envMapIntensity:{value:1}}]),vertexShader:xe.meshlambert_vert,fragmentShader:xe.meshlambert_frag},phong:{uniforms:Wn([It.common,It.specularmap,It.envmap,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.fog,It.lights,{emissive:{value:new ue(0)},specular:{value:new ue(1118481)},shininess:{value:30},envMapIntensity:{value:1}}]),vertexShader:xe.meshphong_vert,fragmentShader:xe.meshphong_frag},standard:{uniforms:Wn([It.common,It.envmap,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.roughnessmap,It.metalnessmap,It.fog,It.lights,{emissive:{value:new ue(0)},roughness:{value:1},metalness:{value:0},envMapIntensity:{value:1}}]),vertexShader:xe.meshphysical_vert,fragmentShader:xe.meshphysical_frag},toon:{uniforms:Wn([It.common,It.aomap,It.lightmap,It.emissivemap,It.bumpmap,It.normalmap,It.displacementmap,It.gradientmap,It.fog,It.lights,{emissive:{value:new ue(0)}}]),vertexShader:xe.meshtoon_vert,fragmentShader:xe.meshtoon_frag},matcap:{uniforms:Wn([It.common,It.bumpmap,It.normalmap,It.displacementmap,It.fog,{matcap:{value:null}}]),vertexShader:xe.meshmatcap_vert,fragmentShader:xe.meshmatcap_frag},points:{uniforms:Wn([It.points,It.fog]),vertexShader:xe.points_vert,fragmentShader:xe.points_frag},dashed:{uniforms:Wn([It.common,It.fog,{scale:{value:1},dashSize:{value:1},totalSize:{value:2}}]),vertexShader:xe.linedashed_vert,fragmentShader:xe.linedashed_frag},depth:{uniforms:Wn([It.common,It.displacementmap]),vertexShader:xe.depth_vert,fragmentShader:xe.depth_frag},normal:{uniforms:Wn([It.common,It.bumpmap,It.normalmap,It.displacementmap,{opacity:{value:1}}]),vertexShader:xe.meshnormal_vert,fragmentShader:xe.meshnormal_frag},sprite:{uniforms:Wn([It.sprite,It.fog]),vertexShader:xe.sprite_vert,fragmentShader:xe.sprite_frag},background:{uniforms:{uvTransform:{value:new de},t2D:{value:null},backgroundIntensity:{value:1}},vertexShader:xe.background_vert,fragmentShader:xe.background_frag},backgroundCube:{uniforms:{envMap:{value:null},backgroundBlurriness:{value:0},backgroundIntensity:{value:1},backgroundRotation:{value:new de}},vertexShader:xe.backgroundCube_vert,fragmentShader:xe.backgroundCube_frag},cube:{uniforms:{tCube:{value:null},tFlip:{value:-1},opacity:{value:1}},vertexShader:xe.cube_vert,fragmentShader:xe.cube_frag},equirect:{uniforms:{tEquirect:{value:null}},vertexShader:xe.equirect_vert,fragmentShader:xe.equirect_frag},distance:{uniforms:Wn([It.common,It.displacementmap,{referencePosition:{value:new V},nearDistance:{value:1},farDistance:{value:1e3}}]),vertexShader:xe.distance_vert,fragmentShader:xe.distance_frag},shadow:{uniforms:Wn([It.lights,It.fog,{color:{value:new ue(0)},opacity:{value:1}}]),vertexShader:xe.shadow_vert,fragmentShader:xe.shadow_frag}};Zi.physical={uniforms:Wn([Zi.standard.uniforms,{clearcoat:{value:0},clearcoatMap:{value:null},clearcoatMapTransform:{value:new de},clearcoatNormalMap:{value:null},clearcoatNormalMapTransform:{value:new de},clearcoatNormalScale:{value:new fe(1,1)},clearcoatRoughness:{value:0},clearcoatRoughnessMap:{value:null},clearcoatRoughnessMapTransform:{value:new de},dispersion:{value:0},iridescence:{value:0},iridescenceMap:{value:null},iridescenceMapTransform:{value:new de},iridescenceIOR:{value:1.3},iridescenceThicknessMinimum:{value:100},iridescenceThicknessMaximum:{value:400},iridescenceThicknessMap:{value:null},iridescenceThicknessMapTransform:{value:new de},sheen:{value:0},sheenColor:{value:new ue(0)},sheenColorMap:{value:null},sheenColorMapTransform:{value:new de},sheenRoughness:{value:1},sheenRoughnessMap:{value:null},sheenRoughnessMapTransform:{value:new de},transmission:{value:0},transmissionMap:{value:null},transmissionMapTransform:{value:new de},transmissionSamplerSize:{value:new fe},transmissionSamplerMap:{value:null},thickness:{value:0},thicknessMap:{value:null},thicknessMapTransform:{value:new de},attenuationDistance:{value:0},attenuationColor:{value:new ue(0)},specularColor:{value:new ue(1,1,1)},specularColorMap:{value:null},specularColorMapTransform:{value:new de},specularIntensity:{value:1},specularIntensityMap:{value:null},specularIntensityMapTransform:{value:new de},anisotropyVector:{value:new fe},anisotropyMap:{value:null},anisotropyMapTransform:{value:new de}}]),vertexShader:xe.meshphysical_vert,fragmentShader:xe.meshphysical_frag};const uu={r:0,b:0,g:0},WA=new Fe,Ix=new de;Ix.set(-1,0,0,0,1,0,0,0,1);function qA(r,t,i,s,l,c){const f=new ue(0);let p=l===!0?0:1,m,d,_=null,v=0,g=null;function S(B){let F=B.isScene===!0?B.background:null;if(F&&F.isTexture){const w=B.backgroundBlurriness>0;F=t.get(F,w)}return F}function E(B){let F=!1;const w=S(B);w===null?M(f,p):w&&w.isColor&&(M(w,1),F=!0);const I=r.xr.getEnvironmentBlendMode();I==="additive"?i.buffers.color.setClear(0,0,0,1,c):I==="alpha-blend"&&i.buffers.color.setClear(0,0,0,0,c),(r.autoClear||F)&&(i.buffers.depth.setTest(!0),i.buffers.depth.setMask(!0),i.buffers.color.setMask(!0),r.clear(r.autoClearColor,r.autoClearDepth,r.autoClearStencil))}function A(B,F){const w=S(F);w&&(w.isCubeTexture||w.mapping===Cu)?(d===void 0&&(d=new ln(new Dn(1,1,1),new $i({name:"BackgroundCubeMaterial",uniforms:so(Zi.backgroundCube.uniforms),vertexShader:Zi.backgroundCube.vertexShader,fragmentShader:Zi.backgroundCube.fragmentShader,side:ti,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),d.geometry.deleteAttribute("normal"),d.geometry.deleteAttribute("uv"),d.onBeforeRender=function(I,O,D){this.matrixWorld.copyPosition(D.matrixWorld)},Object.defineProperty(d.material,"envMap",{get:function(){return this.uniforms.envMap.value}}),s.update(d)),d.material.uniforms.envMap.value=w,d.material.uniforms.backgroundBlurriness.value=F.backgroundBlurriness,d.material.uniforms.backgroundIntensity.value=F.backgroundIntensity,d.material.uniforms.backgroundRotation.value.setFromMatrix4(WA.makeRotationFromEuler(F.backgroundRotation)).transpose(),w.isCubeTexture&&w.isRenderTargetTexture===!1&&d.material.uniforms.backgroundRotation.value.premultiply(Ix),d.material.toneMapped=Ne.getTransfer(w.colorSpace)!==Ze,(_!==w||v!==w.version||g!==r.toneMapping)&&(d.material.needsUpdate=!0,_=w,v=w.version,g=r.toneMapping),d.layers.enableAll(),B.unshift(d,d.geometry,d.material,0,0,null)):w&&w.isTexture&&(m===void 0&&(m=new ln(new ms(2,2),new $i({name:"BackgroundMaterial",uniforms:so(Zi.background.uniforms),vertexShader:Zi.background.vertexShader,fragmentShader:Zi.background.fragmentShader,side:hs,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),m.geometry.deleteAttribute("normal"),Object.defineProperty(m.material,"map",{get:function(){return this.uniforms.t2D.value}}),s.update(m)),m.material.uniforms.t2D.value=w,m.material.uniforms.backgroundIntensity.value=F.backgroundIntensity,m.material.toneMapped=Ne.getTransfer(w.colorSpace)!==Ze,w.matrixAutoUpdate===!0&&w.updateMatrix(),m.material.uniforms.uvTransform.value.copy(w.matrix),(_!==w||v!==w.version||g!==r.toneMapping)&&(m.material.needsUpdate=!0,_=w,v=w.version,g=r.toneMapping),m.layers.enableAll(),B.unshift(m,m.geometry,m.material,0,0,null))}function M(B,F){B.getRGB(uu,Lx(r)),i.buffers.color.setClear(uu.r,uu.g,uu.b,F,c)}function y(){d!==void 0&&(d.geometry.dispose(),d.material.dispose(),d=void 0),m!==void 0&&(m.geometry.dispose(),m.material.dispose(),m=void 0)}return{getClearColor:function(){return f},setClearColor:function(B,F=1){f.set(B),p=F,M(f,p)},getClearAlpha:function(){return p},setClearAlpha:function(B){p=B,M(f,p)},render:E,addToRenderList:A,dispose:y}}function YA(r,t){const i=r.getParameter(r.MAX_VERTEX_ATTRIBS),s={},l=g(null);let c=l,f=!1;function p(k,J,ut,gt,$){let P=!1;const H=v(k,gt,ut,J);c!==H&&(c=H,d(c.object)),P=S(k,gt,ut,$),P&&E(k,gt,ut,$),$!==null&&t.update($,r.ELEMENT_ARRAY_BUFFER),(P||f)&&(f=!1,w(k,J,ut,gt),$!==null&&r.bindBuffer(r.ELEMENT_ARRAY_BUFFER,t.get($).buffer))}function m(){return r.createVertexArray()}function d(k){return r.bindVertexArray(k)}function _(k){return r.deleteVertexArray(k)}function v(k,J,ut,gt){const $=gt.wireframe===!0;let P=s[J.id];P===void 0&&(P={},s[J.id]=P);const H=k.isInstancedMesh===!0?k.id:0;let et=P[H];et===void 0&&(et={},P[H]=et);let pt=et[ut.id];pt===void 0&&(pt={},et[ut.id]=pt);let Et=pt[$];return Et===void 0&&(Et=g(m()),pt[$]=Et),Et}function g(k){const J=[],ut=[],gt=[];for(let $=0;$<i;$++)J[$]=0,ut[$]=0,gt[$]=0;return{geometry:null,program:null,wireframe:!1,newAttributes:J,enabledAttributes:ut,attributeDivisors:gt,object:k,attributes:{},index:null}}function S(k,J,ut,gt){const $=c.attributes,P=J.attributes;let H=0;const et=ut.getAttributes();for(const pt in et)if(et[pt].location>=0){const N=$[pt];let W=P[pt];if(W===void 0&&(pt==="instanceMatrix"&&k.instanceMatrix&&(W=k.instanceMatrix),pt==="instanceColor"&&k.instanceColor&&(W=k.instanceColor)),N===void 0||N.attribute!==W||W&&N.data!==W.data)return!0;H++}return c.attributesNum!==H||c.index!==gt}function E(k,J,ut,gt){const $={},P=J.attributes;let H=0;const et=ut.getAttributes();for(const pt in et)if(et[pt].location>=0){let N=P[pt];N===void 0&&(pt==="instanceMatrix"&&k.instanceMatrix&&(N=k.instanceMatrix),pt==="instanceColor"&&k.instanceColor&&(N=k.instanceColor));const W={};W.attribute=N,N&&N.data&&(W.data=N.data),$[pt]=W,H++}c.attributes=$,c.attributesNum=H,c.index=gt}function A(){const k=c.newAttributes;for(let J=0,ut=k.length;J<ut;J++)k[J]=0}function M(k){y(k,0)}function y(k,J){const ut=c.newAttributes,gt=c.enabledAttributes,$=c.attributeDivisors;ut[k]=1,gt[k]===0&&(r.enableVertexAttribArray(k),gt[k]=1),$[k]!==J&&(r.vertexAttribDivisor(k,J),$[k]=J)}function B(){const k=c.newAttributes,J=c.enabledAttributes;for(let ut=0,gt=J.length;ut<gt;ut++)J[ut]!==k[ut]&&(r.disableVertexAttribArray(ut),J[ut]=0)}function F(k,J,ut,gt,$,P,H){H===!0?r.vertexAttribIPointer(k,J,ut,$,P):r.vertexAttribPointer(k,J,ut,gt,$,P)}function w(k,J,ut,gt){A();const $=gt.attributes,P=ut.getAttributes(),H=J.defaultAttributeValues;for(const et in P){const pt=P[et];if(pt.location>=0){let Et=$[et];if(Et===void 0&&(et==="instanceMatrix"&&k.instanceMatrix&&(Et=k.instanceMatrix),et==="instanceColor"&&k.instanceColor&&(Et=k.instanceColor)),Et!==void 0){const N=Et.normalized,W=Et.itemSize,vt=t.get(Et);if(vt===void 0)continue;const Tt=vt.buffer,At=vt.type,tt=vt.bytesPerElement,yt=At===r.INT||At===r.UNSIGNED_INT||Et.gpuType===Cp;if(Et.isInterleavedBufferAttribute){const Mt=Et.data,Nt=Mt.stride,ae=Et.offset;if(Mt.isInstancedInterleavedBuffer){for(let Qt=0;Qt<pt.locationSize;Qt++)y(pt.location+Qt,Mt.meshPerAttribute);k.isInstancedMesh!==!0&&gt._maxInstanceCount===void 0&&(gt._maxInstanceCount=Mt.meshPerAttribute*Mt.count)}else for(let Qt=0;Qt<pt.locationSize;Qt++)M(pt.location+Qt);r.bindBuffer(r.ARRAY_BUFFER,Tt);for(let Qt=0;Qt<pt.locationSize;Qt++)F(pt.location+Qt,W/pt.locationSize,At,N,Nt*tt,(ae+W/pt.locationSize*Qt)*tt,yt)}else{if(Et.isInstancedBufferAttribute){for(let Mt=0;Mt<pt.locationSize;Mt++)y(pt.location+Mt,Et.meshPerAttribute);k.isInstancedMesh!==!0&&gt._maxInstanceCount===void 0&&(gt._maxInstanceCount=Et.meshPerAttribute*Et.count)}else for(let Mt=0;Mt<pt.locationSize;Mt++)M(pt.location+Mt);r.bindBuffer(r.ARRAY_BUFFER,Tt);for(let Mt=0;Mt<pt.locationSize;Mt++)F(pt.location+Mt,W/pt.locationSize,At,N,W*tt,W/pt.locationSize*Mt*tt,yt)}}else if(H!==void 0){const N=H[et];if(N!==void 0)switch(N.length){case 2:r.vertexAttrib2fv(pt.location,N);break;case 3:r.vertexAttrib3fv(pt.location,N);break;case 4:r.vertexAttrib4fv(pt.location,N);break;default:r.vertexAttrib1fv(pt.location,N)}}}}B()}function I(){L();for(const k in s){const J=s[k];for(const ut in J){const gt=J[ut];for(const $ in gt){const P=gt[$];for(const H in P)_(P[H].object),delete P[H];delete gt[$]}}delete s[k]}}function O(k){if(s[k.id]===void 0)return;const J=s[k.id];for(const ut in J){const gt=J[ut];for(const $ in gt){const P=gt[$];for(const H in P)_(P[H].object),delete P[H];delete gt[$]}}delete s[k.id]}function D(k){for(const J in s){const ut=s[J];for(const gt in ut){const $=ut[gt];if($[k.id]===void 0)continue;const P=$[k.id];for(const H in P)_(P[H].object),delete P[H];delete $[k.id]}}}function T(k){for(const J in s){const ut=s[J],gt=k.isInstancedMesh===!0?k.id:0,$=ut[gt];if($!==void 0){for(const P in $){const H=$[P];for(const et in H)_(H[et].object),delete H[et];delete $[P]}delete ut[gt],Object.keys(ut).length===0&&delete s[J]}}}function L(){j(),f=!0,c!==l&&(c=l,d(c.object))}function j(){l.geometry=null,l.program=null,l.wireframe=!1}return{setup:p,reset:L,resetDefaultState:j,dispose:I,releaseStatesOfGeometry:O,releaseStatesOfObject:T,releaseStatesOfProgram:D,initAttributes:A,enableAttribute:M,disableUnusedAttributes:B}}function ZA(r,t,i){let s;function l(m){s=m}function c(m,d){r.drawArrays(s,m,d),i.update(d,s,1)}function f(m,d,_){_!==0&&(r.drawArraysInstanced(s,m,d,_),i.update(d,s,_))}function p(m,d,_){if(_===0)return;t.get("WEBGL_multi_draw").multiDrawArraysWEBGL(s,m,0,d,0,_);let g=0;for(let S=0;S<_;S++)g+=d[S];i.update(g,s,1)}this.setMode=l,this.render=c,this.renderInstances=f,this.renderMultiDraw=p}function jA(r,t,i,s){let l;function c(){if(l!==void 0)return l;if(t.has("EXT_texture_filter_anisotropic")===!0){const D=t.get("EXT_texture_filter_anisotropic");l=r.getParameter(D.MAX_TEXTURE_MAX_ANISOTROPY_EXT)}else l=0;return l}function f(D){return!(D!==Fi&&s.convert(D)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_FORMAT))}function p(D){const T=D===Ca&&(t.has("EXT_color_buffer_half_float")||t.has("EXT_color_buffer_float"));return!(D!==gi&&s.convert(D)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_TYPE)&&D!==Ii&&!T)}function m(D){if(D==="highp"){if(r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.HIGH_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.HIGH_FLOAT).precision>0)return"highp";D="mediump"}return D==="mediump"&&r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.MEDIUM_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.MEDIUM_FLOAT).precision>0?"mediump":"lowp"}let d=i.precision!==void 0?i.precision:"highp";const _=m(d);_!==d&&(re("WebGLRenderer:",d,"not supported, using",_,"instead."),d=_);const v=i.logarithmicDepthBuffer===!0,g=i.reversedDepthBuffer===!0&&t.has("EXT_clip_control");i.reversedDepthBuffer===!0&&g===!1&&re("WebGLRenderer: Unable to use reversed depth buffer due to missing EXT_clip_control extension. Fallback to default depth buffer.");const S=r.getParameter(r.MAX_TEXTURE_IMAGE_UNITS),E=r.getParameter(r.MAX_VERTEX_TEXTURE_IMAGE_UNITS),A=r.getParameter(r.MAX_TEXTURE_SIZE),M=r.getParameter(r.MAX_CUBE_MAP_TEXTURE_SIZE),y=r.getParameter(r.MAX_VERTEX_ATTRIBS),B=r.getParameter(r.MAX_VERTEX_UNIFORM_VECTORS),F=r.getParameter(r.MAX_VARYING_VECTORS),w=r.getParameter(r.MAX_FRAGMENT_UNIFORM_VECTORS),I=r.getParameter(r.MAX_SAMPLES),O=r.getParameter(r.SAMPLES);return{isWebGL2:!0,getMaxAnisotropy:c,getMaxPrecision:m,textureFormatReadable:f,textureTypeReadable:p,precision:d,logarithmicDepthBuffer:v,reversedDepthBuffer:g,maxTextures:S,maxVertexTextures:E,maxTextureSize:A,maxCubemapSize:M,maxAttributes:y,maxVertexUniforms:B,maxVaryings:F,maxFragmentUniforms:w,maxSamples:I,samples:O}}function KA(r){const t=this;let i=null,s=0,l=!1,c=!1;const f=new Gs,p=new de,m={value:null,needsUpdate:!1};this.uniform=m,this.numPlanes=0,this.numIntersection=0,this.init=function(v,g){const S=v.length!==0||g||s!==0||l;return l=g,s=v.length,S},this.beginShadows=function(){c=!0,_(null)},this.endShadows=function(){c=!1},this.setGlobalState=function(v,g){i=_(v,g,0)},this.setState=function(v,g,S){const E=v.clippingPlanes,A=v.clipIntersection,M=v.clipShadows,y=r.get(v);if(!l||E===null||E.length===0||c&&!M)c?_(null):d();else{const B=c?0:s,F=B*4;let w=y.clippingState||null;m.value=w,w=_(E,g,F,S);for(let I=0;I!==F;++I)w[I]=i[I];y.clippingState=w,this.numIntersection=A?this.numPlanes:0,this.numPlanes+=B}};function d(){m.value!==i&&(m.value=i,m.needsUpdate=s>0),t.numPlanes=s,t.numIntersection=0}function _(v,g,S,E){const A=v!==null?v.length:0;let M=null;if(A!==0){if(M=m.value,E!==!0||M===null){const y=S+A*4,B=g.matrixWorldInverse;p.getNormalMatrix(B),(M===null||M.length<y)&&(M=new Float32Array(y));for(let F=0,w=S;F!==A;++F,w+=4)f.copy(v[F]).applyMatrix4(B,p),f.normal.toArray(M,w),M[w+3]=f.constant}m.value=M,m.needsUpdate=!0}return t.numPlanes=A,t.numIntersection=0,M}}const us=4,Uv=[.125,.215,.35,.446,.526,.582],ks=20,QA=256,hl=new Yp,Nv=new ue;let Md=null,Ed=0,bd=0,Td=!1;const JA=new V;class Lv{constructor(t){this._renderer=t,this._pingPongRenderTarget=null,this._lodMax=0,this._cubeSize=0,this._sizeLods=[],this._sigmas=[],this._lodMeshes=[],this._backgroundBox=null,this._cubemapMaterial=null,this._equirectMaterial=null,this._blurMaterial=null,this._ggxMaterial=null}fromScene(t,i=0,s=.1,l=100,c={}){const{size:f=256,position:p=JA}=c;Md=this._renderer.getRenderTarget(),Ed=this._renderer.getActiveCubeFace(),bd=this._renderer.getActiveMipmapLevel(),Td=this._renderer.xr.enabled,this._renderer.xr.enabled=!1,this._setSize(f);const m=this._allocateTargets();return m.depthBuffer=!0,this._sceneToCubeUV(t,s,l,m,p),i>0&&this._blur(m,0,0,i),this._applyPMREM(m),this._cleanup(m),m}fromEquirectangular(t,i=null){return this._fromTexture(t,i)}fromCubemap(t,i=null){return this._fromTexture(t,i)}compileCubemapShader(){this._cubemapMaterial===null&&(this._cubemapMaterial=zv(),this._compileMaterial(this._cubemapMaterial))}compileEquirectangularShader(){this._equirectMaterial===null&&(this._equirectMaterial=Pv(),this._compileMaterial(this._equirectMaterial))}dispose(){this._dispose(),this._cubemapMaterial!==null&&this._cubemapMaterial.dispose(),this._equirectMaterial!==null&&this._equirectMaterial.dispose(),this._backgroundBox!==null&&(this._backgroundBox.geometry.dispose(),this._backgroundBox.material.dispose())}_setSize(t){this._lodMax=Math.floor(Math.log2(t)),this._cubeSize=Math.pow(2,this._lodMax)}_dispose(){this._blurMaterial!==null&&this._blurMaterial.dispose(),this._ggxMaterial!==null&&this._ggxMaterial.dispose(),this._pingPongRenderTarget!==null&&this._pingPongRenderTarget.dispose();for(let t=0;t<this._lodMeshes.length;t++)this._lodMeshes[t].geometry.dispose()}_cleanup(t){this._renderer.setRenderTarget(Md,Ed,bd),this._renderer.xr.enabled=Td,t.scissorTest=!1,jr(t,0,0,t.width,t.height)}_fromTexture(t,i){t.mapping===Zs||t.mapping===no?this._setSize(t.image.length===0?16:t.image[0].width||t.image[0].image.width):this._setSize(t.image.width/4),Md=this._renderer.getRenderTarget(),Ed=this._renderer.getActiveCubeFace(),bd=this._renderer.getActiveMipmapLevel(),Td=this._renderer.xr.enabled,this._renderer.xr.enabled=!1;const s=i||this._allocateTargets();return this._textureToCubeUV(t,s),this._applyPMREM(s),this._cleanup(s),s}_allocateTargets(){const t=3*Math.max(this._cubeSize,112),i=4*this._cubeSize,s={magFilter:Gn,minFilter:Gn,generateMipmaps:!1,type:Ca,format:Fi,colorSpace:yu,depthBuffer:!1},l=Ov(t,i,s);if(this._pingPongRenderTarget===null||this._pingPongRenderTarget.width!==t||this._pingPongRenderTarget.height!==i){this._pingPongRenderTarget!==null&&this._dispose(),this._pingPongRenderTarget=Ov(t,i,s);const{_lodMax:c}=this;({lodMeshes:this._lodMeshes,sizeLods:this._sizeLods,sigmas:this._sigmas}=$A(c)),this._blurMaterial=e2(c,t,i),this._ggxMaterial=t2(c,t,i)}return l}_compileMaterial(t){const i=new ln(new hn,t);this._renderer.compile(i,hl)}_sceneToCubeUV(t,i,s,l,c){const m=new mi(90,1,i,s),d=[1,-1,1,1,1,1],_=[1,1,1,-1,-1,-1],v=this._renderer,g=v.autoClear,S=v.toneMapping;v.getClearColor(Nv),v.toneMapping=Ki,v.autoClear=!1,v.state.buffers.depth.getReversed()&&(v.setRenderTarget(l),v.clearDepth(),v.setRenderTarget(null)),this._backgroundBox===null&&(this._backgroundBox=new ln(new Dn,new io({name:"PMREM.Background",side:ti,depthWrite:!1,depthTest:!1})));const A=this._backgroundBox,M=A.material;let y=!1;const B=t.background;B?B.isColor&&(M.color.copy(B),t.background=null,y=!0):(M.color.copy(Nv),y=!0);for(let F=0;F<6;F++){const w=F%3;w===0?(m.up.set(0,d[F],0),m.position.set(c.x,c.y,c.z),m.lookAt(c.x+_[F],c.y,c.z)):w===1?(m.up.set(0,0,d[F]),m.position.set(c.x,c.y,c.z),m.lookAt(c.x,c.y+_[F],c.z)):(m.up.set(0,d[F],0),m.position.set(c.x,c.y,c.z),m.lookAt(c.x,c.y,c.z+_[F]));const I=this._cubeSize;jr(l,w*I,F>2?I:0,I,I),v.setRenderTarget(l),y&&v.render(A,m),v.render(t,m)}v.toneMapping=S,v.autoClear=g,t.background=B}_textureToCubeUV(t,i){const s=this._renderer,l=t.mapping===Zs||t.mapping===no;l?(this._cubemapMaterial===null&&(this._cubemapMaterial=zv()),this._cubemapMaterial.uniforms.flipEnvMap.value=t.isRenderTargetTexture===!1?-1:1):this._equirectMaterial===null&&(this._equirectMaterial=Pv());const c=l?this._cubemapMaterial:this._equirectMaterial,f=this._lodMeshes[0];f.material=c;const p=c.uniforms;p.envMap.value=t;const m=this._cubeSize;jr(i,0,0,3*m,2*m),s.setRenderTarget(i),s.render(f,hl)}_applyPMREM(t){const i=this._renderer,s=i.autoClear;i.autoClear=!1;const l=this._lodMeshes.length;for(let c=1;c<l;c++)this._applyGGXFilter(t,c-1,c);i.autoClear=s}_applyGGXFilter(t,i,s){const l=this._renderer,c=this._pingPongRenderTarget,f=this._ggxMaterial,p=this._lodMeshes[s];p.material=f;const m=f.uniforms,d=s/(this._lodMeshes.length-1),_=i/(this._lodMeshes.length-1),v=Math.sqrt(d*d-_*_),g=0+d*1.25,S=v*g,{_lodMax:E}=this,A=this._sizeLods[s],M=3*A*(s>E-us?s-E+us:0),y=4*(this._cubeSize-A);m.envMap.value=t.texture,m.roughness.value=S,m.mipInt.value=E-i,jr(c,M,y,3*A,2*A),l.setRenderTarget(c),l.render(p,hl),m.envMap.value=c.texture,m.roughness.value=0,m.mipInt.value=E-s,jr(t,M,y,3*A,2*A),l.setRenderTarget(t),l.render(p,hl)}_blur(t,i,s,l,c){const f=this._pingPongRenderTarget;this._halfBlur(t,f,i,s,l,"latitudinal",c),this._halfBlur(f,t,s,s,l,"longitudinal",c)}_halfBlur(t,i,s,l,c,f,p){const m=this._renderer,d=this._blurMaterial;f!=="latitudinal"&&f!=="longitudinal"&&Ue("blur direction must be either latitudinal or longitudinal!");const _=3,v=this._lodMeshes[l];v.material=d;const g=d.uniforms,S=this._sizeLods[s]-1,E=isFinite(c)?Math.PI/(2*S):2*Math.PI/(2*ks-1),A=c/E,M=isFinite(c)?1+Math.floor(_*A):ks;M>ks&&re(`sigmaRadians, ${c}, is too large and will clip, as it requested ${M} samples when the maximum is set to ${ks}`);const y=[];let B=0;for(let D=0;D<ks;++D){const T=D/A,L=Math.exp(-T*T/2);y.push(L),D===0?B+=L:D<M&&(B+=2*L)}for(let D=0;D<y.length;D++)y[D]=y[D]/B;g.envMap.value=t.texture,g.samples.value=M,g.weights.value=y,g.latitudinal.value=f==="latitudinal",p&&(g.poleAxis.value=p);const{_lodMax:F}=this;g.dTheta.value=E,g.mipInt.value=F-s;const w=this._sizeLods[l],I=3*w*(l>F-us?l-F+us:0),O=4*(this._cubeSize-w);jr(i,I,O,3*w,2*w),m.setRenderTarget(i),m.render(v,hl)}}function $A(r){const t=[],i=[],s=[];let l=r;const c=r-us+1+Uv.length;for(let f=0;f<c;f++){const p=Math.pow(2,l);t.push(p);let m=1/p;f>r-us?m=Uv[f-r+us-1]:f===0&&(m=0),i.push(m);const d=1/(p-2),_=-d,v=1+d,g=[_,_,v,_,v,v,_,_,v,v,_,v],S=6,E=6,A=3,M=2,y=1,B=new Float32Array(A*E*S),F=new Float32Array(M*E*S),w=new Float32Array(y*E*S);for(let O=0;O<S;O++){const D=O%3*2/3-1,T=O>2?0:-1,L=[D,T,0,D+2/3,T,0,D+2/3,T+1,0,D,T,0,D+2/3,T+1,0,D,T+1,0];B.set(L,A*E*O),F.set(g,M*E*O);const j=[O,O,O,O,O,O];w.set(j,y*E*O)}const I=new hn;I.setAttribute("position",new vi(B,A)),I.setAttribute("uv",new vi(F,M)),I.setAttribute("faceIndex",new vi(w,y)),s.push(new ln(I,null)),l>us&&l--}return{lodMeshes:s,sizeLods:t,sigmas:i}}function Ov(r,t,i){const s=new Qi(r,t,i);return s.texture.mapping=Cu,s.texture.name="PMREM.cubeUv",s.scissorTest=!0,s}function jr(r,t,i,s,l){r.viewport.set(t,i,s,l),r.scissor.set(t,i,s,l)}function t2(r,t,i){return new $i({name:"PMREMGGXConvolution",defines:{GGX_SAMPLES:QA,CUBEUV_TEXEL_WIDTH:1/t,CUBEUV_TEXEL_HEIGHT:1/i,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},roughness:{value:0},mipInt:{value:0}},vertexShader:Uu(),fragmentShader:`

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
		`,blending:Ta,depthTest:!1,depthWrite:!1})}function e2(r,t,i){const s=new Float32Array(ks),l=new V(0,1,0);return new $i({name:"SphericalGaussianBlur",defines:{n:ks,CUBEUV_TEXEL_WIDTH:1/t,CUBEUV_TEXEL_HEIGHT:1/i,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},samples:{value:1},weights:{value:s},latitudinal:{value:!1},dTheta:{value:0},mipInt:{value:0},poleAxis:{value:l}},vertexShader:Uu(),fragmentShader:`

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
		`,blending:Ta,depthTest:!1,depthWrite:!1})}function Pv(){return new $i({name:"EquirectangularToCubeUV",uniforms:{envMap:{value:null}},vertexShader:Uu(),fragmentShader:`

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
		`,blending:Ta,depthTest:!1,depthWrite:!1})}function zv(){return new $i({name:"CubemapToCubeUV",uniforms:{envMap:{value:null},flipEnvMap:{value:-1}},vertexShader:Uu(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			uniform float flipEnvMap;

			varying vec3 vOutputDirection;

			uniform samplerCube envMap;

			void main() {

				gl_FragColor = textureCube( envMap, vec3( flipEnvMap * vOutputDirection.x, vOutputDirection.yz ) );

			}
		`,blending:Ta,depthTest:!1,depthWrite:!1})}function Uu(){return`

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
	`}class Fx extends Qi{constructor(t=1,i={}){super(t,t,i),this.isWebGLCubeRenderTarget=!0;const s={width:t,height:t,depth:1},l=[s,s,s,s,s,s];this.texture=new Dx(l),this._setTextureOptions(i),this.texture.isRenderTargetTexture=!0}fromEquirectangularTexture(t,i){this.texture.type=i.type,this.texture.colorSpace=i.colorSpace,this.texture.generateMipmaps=i.generateMipmaps,this.texture.minFilter=i.minFilter,this.texture.magFilter=i.magFilter;const s={uniforms:{tEquirect:{value:null}},vertexShader:`

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
			`},l=new Dn(5,5,5),c=new $i({name:"CubemapFromEquirect",uniforms:so(s.uniforms),vertexShader:s.vertexShader,fragmentShader:s.fragmentShader,side:ti,blending:Ta});c.uniforms.tEquirect.value=i;const f=new ln(l,c),p=i.minFilter;return i.minFilter===Xs&&(i.minFilter=Gn),new sb(1,10,this).update(t,f),i.minFilter=p,f.geometry.dispose(),f.material.dispose(),this}clear(t,i=!0,s=!0,l=!0){const c=t.getRenderTarget();for(let f=0;f<6;f++)t.setRenderTarget(this,f),t.clear(i,s,l);t.setRenderTarget(c)}}function n2(r){let t=new WeakMap,i=new WeakMap,s=null;function l(g,S=!1){return g==null?null:S?f(g):c(g)}function c(g){if(g&&g.isTexture){const S=g.mapping;if(S===Xh||S===Wh)if(t.has(g)){const E=t.get(g).texture;return p(E,g.mapping)}else{const E=g.image;if(E&&E.height>0){const A=new Fx(E.height);return A.fromEquirectangularTexture(r,g),t.set(g,A),g.addEventListener("dispose",d),p(A.texture,g.mapping)}else return null}}return g}function f(g){if(g&&g.isTexture){const S=g.mapping,E=S===Xh||S===Wh,A=S===Zs||S===no;if(E||A){let M=i.get(g);const y=M!==void 0?M.texture.pmremVersion:0;if(g.isRenderTargetTexture&&g.pmremVersion!==y)return s===null&&(s=new Lv(r)),M=E?s.fromEquirectangular(g,M):s.fromCubemap(g,M),M.texture.pmremVersion=g.pmremVersion,i.set(g,M),M.texture;if(M!==void 0)return M.texture;{const B=g.image;return E&&B&&B.height>0||A&&B&&m(B)?(s===null&&(s=new Lv(r)),M=E?s.fromEquirectangular(g):s.fromCubemap(g),M.texture.pmremVersion=g.pmremVersion,i.set(g,M),g.addEventListener("dispose",_),M.texture):null}}}return g}function p(g,S){return S===Xh?g.mapping=Zs:S===Wh&&(g.mapping=no),g}function m(g){let S=0;const E=6;for(let A=0;A<E;A++)g[A]!==void 0&&S++;return S===E}function d(g){const S=g.target;S.removeEventListener("dispose",d);const E=t.get(S);E!==void 0&&(t.delete(S),E.dispose())}function _(g){const S=g.target;S.removeEventListener("dispose",_);const E=i.get(S);E!==void 0&&(i.delete(S),E.dispose())}function v(){t=new WeakMap,i=new WeakMap,s!==null&&(s.dispose(),s=null)}return{get:l,dispose:v}}function i2(r){const t={};function i(s){if(t[s]!==void 0)return t[s];const l=r.getExtension(s);return t[s]=l,l}return{has:function(s){return i(s)!==null},init:function(){i("EXT_color_buffer_float"),i("WEBGL_clip_cull_distance"),i("OES_texture_float_linear"),i("EXT_color_buffer_half_float"),i("WEBGL_multisampled_render_to_texture"),i("WEBGL_render_shared_exponent")},get:function(s){const l=i(s);return l===null&&Jr("WebGLRenderer: "+s+" extension not supported."),l}}}function a2(r,t,i,s){const l={},c=new WeakMap;function f(v){const g=v.target;g.index!==null&&t.remove(g.index);for(const E in g.attributes)t.remove(g.attributes[E]);g.removeEventListener("dispose",f),delete l[g.id];const S=c.get(g);S&&(t.remove(S),c.delete(g)),s.releaseStatesOfGeometry(g),g.isInstancedBufferGeometry===!0&&delete g._maxInstanceCount,i.memory.geometries--}function p(v,g){return l[g.id]===!0||(g.addEventListener("dispose",f),l[g.id]=!0,i.memory.geometries++),g}function m(v){const g=v.attributes;for(const S in g)t.update(g[S],r.ARRAY_BUFFER)}function d(v){const g=[],S=v.index,E=v.attributes.position;let A=0;if(E===void 0)return;if(S!==null){const B=S.array;A=S.version;for(let F=0,w=B.length;F<w;F+=3){const I=B[F+0],O=B[F+1],D=B[F+2];g.push(I,O,O,D,D,I)}}else{const B=E.array;A=E.version;for(let F=0,w=B.length/3-1;F<w;F+=3){const I=F+0,O=F+1,D=F+2;g.push(I,O,O,D,D,I)}}const M=new(E.count>=65535?bx:Ex)(g,1);M.version=A;const y=c.get(v);y&&t.remove(y),c.set(v,M)}function _(v){const g=c.get(v);if(g){const S=v.index;S!==null&&g.version<S.version&&d(v)}else d(v);return c.get(v)}return{get:p,update:m,getWireframeAttribute:_}}function s2(r,t,i){let s;function l(v){s=v}let c,f;function p(v){c=v.type,f=v.bytesPerElement}function m(v,g){r.drawElements(s,g,c,v*f),i.update(g,s,1)}function d(v,g,S){S!==0&&(r.drawElementsInstanced(s,g,c,v*f,S),i.update(g,s,S))}function _(v,g,S){if(S===0)return;t.get("WEBGL_multi_draw").multiDrawElementsWEBGL(s,g,0,c,v,0,S);let A=0;for(let M=0;M<S;M++)A+=g[M];i.update(A,s,1)}this.setMode=l,this.setIndex=p,this.render=m,this.renderInstances=d,this.renderMultiDraw=_}function r2(r){const t={geometries:0,textures:0},i={frame:0,calls:0,triangles:0,points:0,lines:0};function s(c,f,p){switch(i.calls++,f){case r.TRIANGLES:i.triangles+=p*(c/3);break;case r.LINES:i.lines+=p*(c/2);break;case r.LINE_STRIP:i.lines+=p*(c-1);break;case r.LINE_LOOP:i.lines+=p*c;break;case r.POINTS:i.points+=p*c;break;default:Ue("WebGLInfo: Unknown draw mode:",f);break}}function l(){i.calls=0,i.triangles=0,i.points=0,i.lines=0}return{memory:t,render:i,programs:null,autoReset:!0,reset:l,update:s}}function o2(r,t,i){const s=new WeakMap,l=new cn;function c(f,p,m){const d=f.morphTargetInfluences,_=p.morphAttributes.position||p.morphAttributes.normal||p.morphAttributes.color,v=_!==void 0?_.length:0;let g=s.get(p);if(g===void 0||g.count!==v){let j=function(){T.dispose(),s.delete(p),p.removeEventListener("dispose",j)};var S=j;g!==void 0&&g.texture.dispose();const E=p.morphAttributes.position!==void 0,A=p.morphAttributes.normal!==void 0,M=p.morphAttributes.color!==void 0,y=p.morphAttributes.position||[],B=p.morphAttributes.normal||[],F=p.morphAttributes.color||[];let w=0;E===!0&&(w=1),A===!0&&(w=2),M===!0&&(w=3);let I=p.attributes.position.count*w,O=1;I>t.maxTextureSize&&(O=Math.ceil(I/t.maxTextureSize),I=t.maxTextureSize);const D=new Float32Array(I*O*4*v),T=new Sx(D,I,O,v);T.type=Ii,T.needsUpdate=!0;const L=w*4;for(let k=0;k<v;k++){const J=y[k],ut=B[k],gt=F[k],$=I*O*4*k;for(let P=0;P<J.count;P++){const H=P*L;E===!0&&(l.fromBufferAttribute(J,P),D[$+H+0]=l.x,D[$+H+1]=l.y,D[$+H+2]=l.z,D[$+H+3]=0),A===!0&&(l.fromBufferAttribute(ut,P),D[$+H+4]=l.x,D[$+H+5]=l.y,D[$+H+6]=l.z,D[$+H+7]=0),M===!0&&(l.fromBufferAttribute(gt,P),D[$+H+8]=l.x,D[$+H+9]=l.y,D[$+H+10]=l.z,D[$+H+11]=gt.itemSize===4?l.w:1)}}g={count:v,texture:T,size:new fe(I,O)},s.set(p,g),p.addEventListener("dispose",j)}if(f.isInstancedMesh===!0&&f.morphTexture!==null)m.getUniforms().setValue(r,"morphTexture",f.morphTexture,i);else{let E=0;for(let M=0;M<d.length;M++)E+=d[M];const A=p.morphTargetsRelative?1:1-E;m.getUniforms().setValue(r,"morphTargetBaseInfluence",A),m.getUniforms().setValue(r,"morphTargetInfluences",d)}m.getUniforms().setValue(r,"morphTargetsTexture",g.texture,i),m.getUniforms().setValue(r,"morphTargetsTextureSize",g.size)}return{update:c}}function l2(r,t,i,s,l){let c=new WeakMap;function f(d){const _=l.render.frame,v=d.geometry,g=t.get(d,v);if(c.get(g)!==_&&(t.update(g),c.set(g,_)),d.isInstancedMesh&&(d.hasEventListener("dispose",m)===!1&&d.addEventListener("dispose",m),c.get(d)!==_&&(i.update(d.instanceMatrix,r.ARRAY_BUFFER),d.instanceColor!==null&&i.update(d.instanceColor,r.ARRAY_BUFFER),c.set(d,_))),d.isSkinnedMesh){const S=d.skeleton;c.get(S)!==_&&(S.update(),c.set(S,_))}return g}function p(){c=new WeakMap}function m(d){const _=d.target;_.removeEventListener("dispose",m),s.releaseStatesOfObject(_),i.remove(_.instanceMatrix),_.instanceColor!==null&&i.remove(_.instanceColor)}return{update:f,dispose:p}}const c2={[ox]:"LINEAR_TONE_MAPPING",[lx]:"REINHARD_TONE_MAPPING",[cx]:"CINEON_TONE_MAPPING",[Rp]:"ACES_FILMIC_TONE_MAPPING",[fx]:"AGX_TONE_MAPPING",[hx]:"NEUTRAL_TONE_MAPPING",[ux]:"CUSTOM_TONE_MAPPING"};function u2(r,t,i,s,l,c){const f=new Qi(t,i,{type:r,depthBuffer:l,stencilBuffer:c,samples:s?4:0,depthTexture:l?new ao(t,i):void 0}),p=new Qi(t,i,{type:Ca,depthBuffer:!1,stencilBuffer:!1}),m=new hn;m.setAttribute("position",new En([-1,3,0,-1,-1,0,3,-1,0],3)),m.setAttribute("uv",new En([0,2,0,0,2,0],2));const d=new KE({uniforms:{tDiffuse:{value:null}},vertexShader:`
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
			}`,depthTest:!1,depthWrite:!1}),_=new ln(m,d),v=new Yp(-1,1,1,-1,0,1);let g=null,S=null,E=!1,A,M=null,y=[],B=!1;this.setSize=function(F,w){f.setSize(F,w),p.setSize(F,w);for(let I=0;I<y.length;I++){const O=y[I];O.setSize&&O.setSize(F,w)}},this.setEffects=function(F){y=F,B=y.length>0&&y[0].isRenderPass===!0;const w=f.width,I=f.height;for(let O=0;O<y.length;O++){const D=y[O];D.setSize&&D.setSize(w,I)}},this.begin=function(F,w){if(E||F.toneMapping===Ki&&y.length===0)return!1;if(M=w,w!==null){const I=w.width,O=w.height;(f.width!==I||f.height!==O)&&this.setSize(I,O)}return B===!1&&F.setRenderTarget(f),A=F.toneMapping,F.toneMapping=Ki,!0},this.hasRenderPass=function(){return B},this.end=function(F,w){F.toneMapping=A,E=!0;let I=f,O=p;for(let D=0;D<y.length;D++){const T=y[D];if(T.enabled!==!1&&(T.render(F,O,I,w),T.needsSwap!==!1)){const L=I;I=O,O=L}}if(g!==F.outputColorSpace||S!==F.toneMapping){g=F.outputColorSpace,S=F.toneMapping,d.defines={},Ne.getTransfer(g)===Ze&&(d.defines.SRGB_TRANSFER="");const D=c2[S];D&&(d.defines[D]=""),d.needsUpdate=!0}d.uniforms.tDiffuse.value=I.texture,F.setRenderTarget(M),F.render(_,v),M=null,E=!1},this.isCompositing=function(){return E},this.dispose=function(){f.depthTexture&&f.depthTexture.dispose(),f.dispose(),p.dispose(),m.dispose(),d.dispose()}}const Hx=new Vn,Sp=new ao(1,1),Gx=new Sx,Vx=new ME,kx=new Dx,Bv=[],Iv=[],Fv=new Float32Array(16),Hv=new Float32Array(9),Gv=new Float32Array(4);function oo(r,t,i){const s=r[0];if(s<=0||s>0)return r;const l=t*i;let c=Bv[l];if(c===void 0&&(c=new Float32Array(l),Bv[l]=c),t!==0){s.toArray(c,0);for(let f=1,p=0;f!==t;++f)p+=i,r[f].toArray(c,p)}return c}function An(r,t){if(r.length!==t.length)return!1;for(let i=0,s=r.length;i<s;i++)if(r[i]!==t[i])return!1;return!0}function Rn(r,t){for(let i=0,s=t.length;i<s;i++)r[i]=t[i]}function Nu(r,t){let i=Iv[t];i===void 0&&(i=new Int32Array(t),Iv[t]=i);for(let s=0;s!==t;++s)i[s]=r.allocateTextureUnit();return i}function f2(r,t){const i=this.cache;i[0]!==t&&(r.uniform1f(this.addr,t),i[0]=t)}function h2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(r.uniform2f(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(An(i,t))return;r.uniform2fv(this.addr,t),Rn(i,t)}}function d2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(r.uniform3f(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else if(t.r!==void 0)(i[0]!==t.r||i[1]!==t.g||i[2]!==t.b)&&(r.uniform3f(this.addr,t.r,t.g,t.b),i[0]=t.r,i[1]=t.g,i[2]=t.b);else{if(An(i,t))return;r.uniform3fv(this.addr,t),Rn(i,t)}}function p2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(r.uniform4f(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(An(i,t))return;r.uniform4fv(this.addr,t),Rn(i,t)}}function m2(r,t){const i=this.cache,s=t.elements;if(s===void 0){if(An(i,t))return;r.uniformMatrix2fv(this.addr,!1,t),Rn(i,t)}else{if(An(i,s))return;Gv.set(s),r.uniformMatrix2fv(this.addr,!1,Gv),Rn(i,s)}}function g2(r,t){const i=this.cache,s=t.elements;if(s===void 0){if(An(i,t))return;r.uniformMatrix3fv(this.addr,!1,t),Rn(i,t)}else{if(An(i,s))return;Hv.set(s),r.uniformMatrix3fv(this.addr,!1,Hv),Rn(i,s)}}function _2(r,t){const i=this.cache,s=t.elements;if(s===void 0){if(An(i,t))return;r.uniformMatrix4fv(this.addr,!1,t),Rn(i,t)}else{if(An(i,s))return;Fv.set(s),r.uniformMatrix4fv(this.addr,!1,Fv),Rn(i,s)}}function v2(r,t){const i=this.cache;i[0]!==t&&(r.uniform1i(this.addr,t),i[0]=t)}function x2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(r.uniform2i(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(An(i,t))return;r.uniform2iv(this.addr,t),Rn(i,t)}}function y2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(r.uniform3i(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else{if(An(i,t))return;r.uniform3iv(this.addr,t),Rn(i,t)}}function S2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(r.uniform4i(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(An(i,t))return;r.uniform4iv(this.addr,t),Rn(i,t)}}function M2(r,t){const i=this.cache;i[0]!==t&&(r.uniform1ui(this.addr,t),i[0]=t)}function E2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(r.uniform2ui(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(An(i,t))return;r.uniform2uiv(this.addr,t),Rn(i,t)}}function b2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(r.uniform3ui(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else{if(An(i,t))return;r.uniform3uiv(this.addr,t),Rn(i,t)}}function T2(r,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(r.uniform4ui(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(An(i,t))return;r.uniform4uiv(this.addr,t),Rn(i,t)}}function A2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l);let c;this.type===r.SAMPLER_2D_SHADOW?(Sp.compareFunction=i.isReversedDepthBuffer()?zp:Pp,c=Sp):c=Hx,i.setTexture2D(t||c,l)}function R2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l),i.setTexture3D(t||Vx,l)}function C2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l),i.setTextureCube(t||kx,l)}function w2(r,t,i){const s=this.cache,l=i.allocateTextureUnit();s[0]!==l&&(r.uniform1i(this.addr,l),s[0]=l),i.setTexture2DArray(t||Gx,l)}function D2(r){switch(r){case 5126:return f2;case 35664:return h2;case 35665:return d2;case 35666:return p2;case 35674:return m2;case 35675:return g2;case 35676:return _2;case 5124:case 35670:return v2;case 35667:case 35671:return x2;case 35668:case 35672:return y2;case 35669:case 35673:return S2;case 5125:return M2;case 36294:return E2;case 36295:return b2;case 36296:return T2;case 35678:case 36198:case 36298:case 36306:case 35682:return A2;case 35679:case 36299:case 36307:return R2;case 35680:case 36300:case 36308:case 36293:return C2;case 36289:case 36303:case 36311:case 36292:return w2}}function U2(r,t){r.uniform1fv(this.addr,t)}function N2(r,t){const i=oo(t,this.size,2);r.uniform2fv(this.addr,i)}function L2(r,t){const i=oo(t,this.size,3);r.uniform3fv(this.addr,i)}function O2(r,t){const i=oo(t,this.size,4);r.uniform4fv(this.addr,i)}function P2(r,t){const i=oo(t,this.size,4);r.uniformMatrix2fv(this.addr,!1,i)}function z2(r,t){const i=oo(t,this.size,9);r.uniformMatrix3fv(this.addr,!1,i)}function B2(r,t){const i=oo(t,this.size,16);r.uniformMatrix4fv(this.addr,!1,i)}function I2(r,t){r.uniform1iv(this.addr,t)}function F2(r,t){r.uniform2iv(this.addr,t)}function H2(r,t){r.uniform3iv(this.addr,t)}function G2(r,t){r.uniform4iv(this.addr,t)}function V2(r,t){r.uniform1uiv(this.addr,t)}function k2(r,t){r.uniform2uiv(this.addr,t)}function X2(r,t){r.uniform3uiv(this.addr,t)}function W2(r,t){r.uniform4uiv(this.addr,t)}function q2(r,t,i){const s=this.cache,l=t.length,c=Nu(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));let f;this.type===r.SAMPLER_2D_SHADOW?f=Sp:f=Hx;for(let p=0;p!==l;++p)i.setTexture2D(t[p]||f,c[p])}function Y2(r,t,i){const s=this.cache,l=t.length,c=Nu(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));for(let f=0;f!==l;++f)i.setTexture3D(t[f]||Vx,c[f])}function Z2(r,t,i){const s=this.cache,l=t.length,c=Nu(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));for(let f=0;f!==l;++f)i.setTextureCube(t[f]||kx,c[f])}function j2(r,t,i){const s=this.cache,l=t.length,c=Nu(i,l);An(s,c)||(r.uniform1iv(this.addr,c),Rn(s,c));for(let f=0;f!==l;++f)i.setTexture2DArray(t[f]||Gx,c[f])}function K2(r){switch(r){case 5126:return U2;case 35664:return N2;case 35665:return L2;case 35666:return O2;case 35674:return P2;case 35675:return z2;case 35676:return B2;case 5124:case 35670:return I2;case 35667:case 35671:return F2;case 35668:case 35672:return H2;case 35669:case 35673:return G2;case 5125:return V2;case 36294:return k2;case 36295:return X2;case 36296:return W2;case 35678:case 36198:case 36298:case 36306:case 35682:return q2;case 35679:case 36299:case 36307:return Y2;case 35680:case 36300:case 36308:case 36293:return Z2;case 36289:case 36303:case 36311:case 36292:return j2}}class Q2{constructor(t,i,s){this.id=t,this.addr=s,this.cache=[],this.type=i.type,this.setValue=D2(i.type)}}class J2{constructor(t,i,s){this.id=t,this.addr=s,this.cache=[],this.type=i.type,this.size=i.size,this.setValue=K2(i.type)}}class $2{constructor(t){this.id=t,this.seq=[],this.map={}}setValue(t,i,s){const l=this.seq;for(let c=0,f=l.length;c!==f;++c){const p=l[c];p.setValue(t,i[p.id],s)}}}const Ad=/(\w+)(\])?(\[|\.)?/g;function Vv(r,t){r.seq.push(t),r.map[t.id]=t}function t3(r,t,i){const s=r.name,l=s.length;for(Ad.lastIndex=0;;){const c=Ad.exec(s),f=Ad.lastIndex;let p=c[1];const m=c[2]==="]",d=c[3];if(m&&(p=p|0),d===void 0||d==="["&&f+2===l){Vv(i,d===void 0?new Q2(p,r,t):new J2(p,r,t));break}else{let v=i.map[p];v===void 0&&(v=new $2(p),Vv(i,v)),i=v}}}class gu{constructor(t,i){this.seq=[],this.map={};const s=t.getProgramParameter(i,t.ACTIVE_UNIFORMS);for(let f=0;f<s;++f){const p=t.getActiveUniform(i,f),m=t.getUniformLocation(i,p.name);t3(p,m,this)}const l=[],c=[];for(const f of this.seq)f.type===t.SAMPLER_2D_SHADOW||f.type===t.SAMPLER_CUBE_SHADOW||f.type===t.SAMPLER_2D_ARRAY_SHADOW?l.push(f):c.push(f);l.length>0&&(this.seq=l.concat(c))}setValue(t,i,s,l){const c=this.map[i];c!==void 0&&c.setValue(t,s,l)}setOptional(t,i,s){const l=i[s];l!==void 0&&this.setValue(t,s,l)}static upload(t,i,s,l){for(let c=0,f=i.length;c!==f;++c){const p=i[c],m=s[p.id];m.needsUpdate!==!1&&p.setValue(t,m.value,l)}}static seqWithValue(t,i){const s=[];for(let l=0,c=t.length;l!==c;++l){const f=t[l];f.id in i&&s.push(f)}return s}}function kv(r,t,i){const s=r.createShader(t);return r.shaderSource(s,i),r.compileShader(s),s}const e3=37297;let n3=0;function i3(r,t){const i=r.split(`
`),s=[],l=Math.max(t-6,0),c=Math.min(t+6,i.length);for(let f=l;f<c;f++){const p=f+1;s.push(`${p===t?">":" "} ${p}: ${i[f]}`)}return s.join(`
`)}const Xv=new de;function a3(r){Ne._getMatrix(Xv,Ne.workingColorSpace,r);const t=`mat3( ${Xv.elements.map(i=>i.toFixed(4))} )`;switch(Ne.getTransfer(r)){case Su:return[t,"LinearTransferOETF"];case Ze:return[t,"sRGBTransferOETF"];default:return re("WebGLProgram: Unsupported color space: ",r),[t,"LinearTransferOETF"]}}function Wv(r,t,i){const s=r.getShaderParameter(t,r.COMPILE_STATUS),c=(r.getShaderInfoLog(t)||"").trim();if(s&&c==="")return"";const f=/ERROR: 0:(\d+)/.exec(c);if(f){const p=parseInt(f[1]);return i.toUpperCase()+`

`+c+`

`+i3(r.getShaderSource(t),p)}else return c}function s3(r,t){const i=a3(t);return[`vec4 ${r}( vec4 value ) {`,`	return ${i[1]}( vec4( value.rgb * ${i[0]}, value.a ) );`,"}"].join(`
`)}const r3={[ox]:"Linear",[lx]:"Reinhard",[cx]:"Cineon",[Rp]:"ACESFilmic",[fx]:"AgX",[hx]:"Neutral",[ux]:"Custom"};function o3(r,t){const i=r3[t];return i===void 0?(re("WebGLProgram: Unsupported toneMapping:",t),"vec3 "+r+"( vec3 color ) { return LinearToneMapping( color ); }"):"vec3 "+r+"( vec3 color ) { return "+i+"ToneMapping( color ); }"}const fu=new V;function l3(){Ne.getLuminanceCoefficients(fu);const r=fu.x.toFixed(4),t=fu.y.toFixed(4),i=fu.z.toFixed(4);return["float luminance( const in vec3 rgb ) {",`	const vec3 weights = vec3( ${r}, ${t}, ${i} );`,"	return dot( weights, rgb );","}"].join(`
`)}function c3(r){return[r.extensionClipCullDistance?"#extension GL_ANGLE_clip_cull_distance : require":"",r.extensionMultiDraw?"#extension GL_ANGLE_multi_draw : require":""].filter(ml).join(`
`)}function u3(r){const t=[];for(const i in r){const s=r[i];s!==!1&&t.push("#define "+i+" "+s)}return t.join(`
`)}function f3(r,t){const i={},s=r.getProgramParameter(t,r.ACTIVE_ATTRIBUTES);for(let l=0;l<s;l++){const c=r.getActiveAttrib(t,l),f=c.name;let p=1;c.type===r.FLOAT_MAT2&&(p=2),c.type===r.FLOAT_MAT3&&(p=3),c.type===r.FLOAT_MAT4&&(p=4),i[f]={type:c.type,location:r.getAttribLocation(t,f),locationSize:p}}return i}function ml(r){return r!==""}function qv(r,t){const i=t.numSpotLightShadows+t.numSpotLightMaps-t.numSpotLightShadowsWithMaps;return r.replace(/NUM_DIR_LIGHTS/g,t.numDirLights).replace(/NUM_SPOT_LIGHTS/g,t.numSpotLights).replace(/NUM_SPOT_LIGHT_MAPS/g,t.numSpotLightMaps).replace(/NUM_SPOT_LIGHT_COORDS/g,i).replace(/NUM_RECT_AREA_LIGHTS/g,t.numRectAreaLights).replace(/NUM_POINT_LIGHTS/g,t.numPointLights).replace(/NUM_HEMI_LIGHTS/g,t.numHemiLights).replace(/NUM_DIR_LIGHT_SHADOWS/g,t.numDirLightShadows).replace(/NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS/g,t.numSpotLightShadowsWithMaps).replace(/NUM_SPOT_LIGHT_SHADOWS/g,t.numSpotLightShadows).replace(/NUM_POINT_LIGHT_SHADOWS/g,t.numPointLightShadows)}function Yv(r,t){return r.replace(/NUM_CLIPPING_PLANES/g,t.numClippingPlanes).replace(/UNION_CLIPPING_PLANES/g,t.numClippingPlanes-t.numClipIntersection)}const h3=/^[ \t]*#include +<([\w\d./]+)>/gm;function Mp(r){return r.replace(h3,p3)}const d3=new Map;function p3(r,t){let i=xe[t];if(i===void 0){const s=d3.get(t);if(s!==void 0)i=xe[s],re('WebGLRenderer: Shader chunk "%s" has been deprecated. Use "%s" instead.',t,s);else throw new Error("THREE.WebGLProgram: Can not resolve #include <"+t+">")}return Mp(i)}const m3=/#pragma unroll_loop_start\s+for\s*\(\s*int\s+i\s*=\s*(\d+)\s*;\s*i\s*<\s*(\d+)\s*;\s*i\s*\+\+\s*\)\s*{([\s\S]+?)}\s+#pragma unroll_loop_end/g;function Zv(r){return r.replace(m3,g3)}function g3(r,t,i,s){let l="";for(let c=parseInt(t);c<parseInt(i);c++)l+=s.replace(/\[\s*i\s*\]/g,"[ "+c+" ]").replace(/UNROLLED_LOOP_INDEX/g,c);return l}function jv(r){let t=`precision ${r.precision} float;
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
#define LOW_PRECISION`),t}const _3={[gl]:"SHADOWMAP_TYPE_PCF",[pl]:"SHADOWMAP_TYPE_VSM"};function v3(r){return _3[r.shadowMapType]||"SHADOWMAP_TYPE_BASIC"}const x3={[Zs]:"ENVMAP_TYPE_CUBE",[no]:"ENVMAP_TYPE_CUBE",[Cu]:"ENVMAP_TYPE_CUBE_UV"};function y3(r){return r.envMap===!1?"ENVMAP_TYPE_CUBE":x3[r.envMapMode]||"ENVMAP_TYPE_CUBE"}const S3={[no]:"ENVMAP_MODE_REFRACTION"};function M3(r){return r.envMap===!1?"ENVMAP_MODE_REFLECTION":S3[r.envMapMode]||"ENVMAP_MODE_REFLECTION"}const E3={[rx]:"ENVMAP_BLENDING_MULTIPLY",[H1]:"ENVMAP_BLENDING_MIX",[G1]:"ENVMAP_BLENDING_ADD"};function b3(r){return r.envMap===!1?"ENVMAP_BLENDING_NONE":E3[r.combine]||"ENVMAP_BLENDING_NONE"}function T3(r){const t=r.envMapCubeUVHeight;if(t===null)return null;const i=Math.log2(t)-2,s=1/t;return{texelWidth:1/(3*Math.max(Math.pow(2,i),112)),texelHeight:s,maxMip:i}}function A3(r,t,i,s){const l=r.getContext(),c=i.defines;let f=i.vertexShader,p=i.fragmentShader;const m=v3(i),d=y3(i),_=M3(i),v=b3(i),g=T3(i),S=c3(i),E=u3(c),A=l.createProgram();let M,y,B=i.glslVersion?"#version "+i.glslVersion+`
`:"";i.isRawShaderMaterial?(M=["#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,E].filter(ml).join(`
`),M.length>0&&(M+=`
`),y=["#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,E].filter(ml).join(`
`),y.length>0&&(y+=`
`)):(M=[jv(i),"#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,E,i.extensionClipCullDistance?"#define USE_CLIP_DISTANCE":"",i.batching?"#define USE_BATCHING":"",i.batchingColor?"#define USE_BATCHING_COLOR":"",i.instancing?"#define USE_INSTANCING":"",i.instancingColor?"#define USE_INSTANCING_COLOR":"",i.instancingMorph?"#define USE_INSTANCING_MORPH":"",i.useFog&&i.fog?"#define USE_FOG":"",i.useFog&&i.fogExp2?"#define FOG_EXP2":"",i.map?"#define USE_MAP":"",i.envMap?"#define USE_ENVMAP":"",i.envMap?"#define "+_:"",i.lightMap?"#define USE_LIGHTMAP":"",i.aoMap?"#define USE_AOMAP":"",i.bumpMap?"#define USE_BUMPMAP":"",i.normalMap?"#define USE_NORMALMAP":"",i.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",i.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",i.displacementMap?"#define USE_DISPLACEMENTMAP":"",i.emissiveMap?"#define USE_EMISSIVEMAP":"",i.anisotropy?"#define USE_ANISOTROPY":"",i.anisotropyMap?"#define USE_ANISOTROPYMAP":"",i.clearcoatMap?"#define USE_CLEARCOATMAP":"",i.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",i.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",i.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",i.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",i.specularMap?"#define USE_SPECULARMAP":"",i.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",i.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",i.roughnessMap?"#define USE_ROUGHNESSMAP":"",i.metalnessMap?"#define USE_METALNESSMAP":"",i.alphaMap?"#define USE_ALPHAMAP":"",i.alphaHash?"#define USE_ALPHAHASH":"",i.transmission?"#define USE_TRANSMISSION":"",i.transmissionMap?"#define USE_TRANSMISSIONMAP":"",i.thicknessMap?"#define USE_THICKNESSMAP":"",i.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",i.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",i.mapUv?"#define MAP_UV "+i.mapUv:"",i.alphaMapUv?"#define ALPHAMAP_UV "+i.alphaMapUv:"",i.lightMapUv?"#define LIGHTMAP_UV "+i.lightMapUv:"",i.aoMapUv?"#define AOMAP_UV "+i.aoMapUv:"",i.emissiveMapUv?"#define EMISSIVEMAP_UV "+i.emissiveMapUv:"",i.bumpMapUv?"#define BUMPMAP_UV "+i.bumpMapUv:"",i.normalMapUv?"#define NORMALMAP_UV "+i.normalMapUv:"",i.displacementMapUv?"#define DISPLACEMENTMAP_UV "+i.displacementMapUv:"",i.metalnessMapUv?"#define METALNESSMAP_UV "+i.metalnessMapUv:"",i.roughnessMapUv?"#define ROUGHNESSMAP_UV "+i.roughnessMapUv:"",i.anisotropyMapUv?"#define ANISOTROPYMAP_UV "+i.anisotropyMapUv:"",i.clearcoatMapUv?"#define CLEARCOATMAP_UV "+i.clearcoatMapUv:"",i.clearcoatNormalMapUv?"#define CLEARCOAT_NORMALMAP_UV "+i.clearcoatNormalMapUv:"",i.clearcoatRoughnessMapUv?"#define CLEARCOAT_ROUGHNESSMAP_UV "+i.clearcoatRoughnessMapUv:"",i.iridescenceMapUv?"#define IRIDESCENCEMAP_UV "+i.iridescenceMapUv:"",i.iridescenceThicknessMapUv?"#define IRIDESCENCE_THICKNESSMAP_UV "+i.iridescenceThicknessMapUv:"",i.sheenColorMapUv?"#define SHEEN_COLORMAP_UV "+i.sheenColorMapUv:"",i.sheenRoughnessMapUv?"#define SHEEN_ROUGHNESSMAP_UV "+i.sheenRoughnessMapUv:"",i.specularMapUv?"#define SPECULARMAP_UV "+i.specularMapUv:"",i.specularColorMapUv?"#define SPECULAR_COLORMAP_UV "+i.specularColorMapUv:"",i.specularIntensityMapUv?"#define SPECULAR_INTENSITYMAP_UV "+i.specularIntensityMapUv:"",i.transmissionMapUv?"#define TRANSMISSIONMAP_UV "+i.transmissionMapUv:"",i.thicknessMapUv?"#define THICKNESSMAP_UV "+i.thicknessMapUv:"",i.vertexTangents&&i.flatShading===!1?"#define USE_TANGENT":"",i.vertexNormals?"#define HAS_NORMAL":"",i.vertexColors?"#define USE_COLOR":"",i.vertexAlphas?"#define USE_COLOR_ALPHA":"",i.vertexUv1s?"#define USE_UV1":"",i.vertexUv2s?"#define USE_UV2":"",i.vertexUv3s?"#define USE_UV3":"",i.pointsUvs?"#define USE_POINTS_UV":"",i.flatShading?"#define FLAT_SHADED":"",i.skinning?"#define USE_SKINNING":"",i.morphTargets?"#define USE_MORPHTARGETS":"",i.morphNormals&&i.flatShading===!1?"#define USE_MORPHNORMALS":"",i.morphColors?"#define USE_MORPHCOLORS":"",i.morphTargetsCount>0?"#define MORPHTARGETS_TEXTURE_STRIDE "+i.morphTextureStride:"",i.morphTargetsCount>0?"#define MORPHTARGETS_COUNT "+i.morphTargetsCount:"",i.doubleSided?"#define DOUBLE_SIDED":"",i.flipSided?"#define FLIP_SIDED":"",i.shadowMapEnabled?"#define USE_SHADOWMAP":"",i.shadowMapEnabled?"#define "+m:"",i.sizeAttenuation?"#define USE_SIZEATTENUATION":"",i.numLightProbes>0?"#define USE_LIGHT_PROBES":"",i.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",i.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 modelMatrix;","uniform mat4 modelViewMatrix;","uniform mat4 projectionMatrix;","uniform mat4 viewMatrix;","uniform mat3 normalMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;","#ifdef USE_INSTANCING","	attribute mat4 instanceMatrix;","#endif","#ifdef USE_INSTANCING_COLOR","	attribute vec3 instanceColor;","#endif","#ifdef USE_INSTANCING_MORPH","	uniform sampler2D morphTexture;","#endif","attribute vec3 position;","attribute vec3 normal;","attribute vec2 uv;","#ifdef USE_UV1","	attribute vec2 uv1;","#endif","#ifdef USE_UV2","	attribute vec2 uv2;","#endif","#ifdef USE_UV3","	attribute vec2 uv3;","#endif","#ifdef USE_TANGENT","	attribute vec4 tangent;","#endif","#if defined( USE_COLOR_ALPHA )","	attribute vec4 color;","#elif defined( USE_COLOR )","	attribute vec3 color;","#endif","#ifdef USE_SKINNING","	attribute vec4 skinIndex;","	attribute vec4 skinWeight;","#endif",`
`].filter(ml).join(`
`),y=[jv(i),"#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,E,i.useFog&&i.fog?"#define USE_FOG":"",i.useFog&&i.fogExp2?"#define FOG_EXP2":"",i.alphaToCoverage?"#define ALPHA_TO_COVERAGE":"",i.map?"#define USE_MAP":"",i.matcap?"#define USE_MATCAP":"",i.envMap?"#define USE_ENVMAP":"",i.envMap?"#define "+d:"",i.envMap?"#define "+_:"",i.envMap?"#define "+v:"",g?"#define CUBEUV_TEXEL_WIDTH "+g.texelWidth:"",g?"#define CUBEUV_TEXEL_HEIGHT "+g.texelHeight:"",g?"#define CUBEUV_MAX_MIP "+g.maxMip+".0":"",i.lightMap?"#define USE_LIGHTMAP":"",i.aoMap?"#define USE_AOMAP":"",i.bumpMap?"#define USE_BUMPMAP":"",i.normalMap?"#define USE_NORMALMAP":"",i.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",i.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",i.packedNormalMap?"#define USE_PACKED_NORMALMAP":"",i.emissiveMap?"#define USE_EMISSIVEMAP":"",i.anisotropy?"#define USE_ANISOTROPY":"",i.anisotropyMap?"#define USE_ANISOTROPYMAP":"",i.clearcoat?"#define USE_CLEARCOAT":"",i.clearcoatMap?"#define USE_CLEARCOATMAP":"",i.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",i.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",i.dispersion?"#define USE_DISPERSION":"",i.iridescence?"#define USE_IRIDESCENCE":"",i.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",i.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",i.specularMap?"#define USE_SPECULARMAP":"",i.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",i.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",i.roughnessMap?"#define USE_ROUGHNESSMAP":"",i.metalnessMap?"#define USE_METALNESSMAP":"",i.alphaMap?"#define USE_ALPHAMAP":"",i.alphaTest?"#define USE_ALPHATEST":"",i.alphaHash?"#define USE_ALPHAHASH":"",i.sheen?"#define USE_SHEEN":"",i.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",i.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",i.transmission?"#define USE_TRANSMISSION":"",i.transmissionMap?"#define USE_TRANSMISSIONMAP":"",i.thicknessMap?"#define USE_THICKNESSMAP":"",i.vertexTangents&&i.flatShading===!1?"#define USE_TANGENT":"",i.vertexColors||i.instancingColor?"#define USE_COLOR":"",i.vertexAlphas||i.batchingColor?"#define USE_COLOR_ALPHA":"",i.vertexUv1s?"#define USE_UV1":"",i.vertexUv2s?"#define USE_UV2":"",i.vertexUv3s?"#define USE_UV3":"",i.pointsUvs?"#define USE_POINTS_UV":"",i.gradientMap?"#define USE_GRADIENTMAP":"",i.flatShading?"#define FLAT_SHADED":"",i.doubleSided?"#define DOUBLE_SIDED":"",i.flipSided?"#define FLIP_SIDED":"",i.shadowMapEnabled?"#define USE_SHADOWMAP":"",i.shadowMapEnabled?"#define "+m:"",i.premultipliedAlpha?"#define PREMULTIPLIED_ALPHA":"",i.numLightProbes>0?"#define USE_LIGHT_PROBES":"",i.numLightProbeGrids>0?"#define USE_LIGHT_PROBES_GRID":"",i.decodeVideoTexture?"#define DECODE_VIDEO_TEXTURE":"",i.decodeVideoTextureEmissive?"#define DECODE_VIDEO_TEXTURE_EMISSIVE":"",i.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",i.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 viewMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;",i.toneMapping!==Ki?"#define TONE_MAPPING":"",i.toneMapping!==Ki?xe.tonemapping_pars_fragment:"",i.toneMapping!==Ki?o3("toneMapping",i.toneMapping):"",i.dithering?"#define DITHERING":"",i.opaque?"#define OPAQUE":"",xe.colorspace_pars_fragment,s3("linearToOutputTexel",i.outputColorSpace),l3(),i.useDepthPacking?"#define DEPTH_PACKING "+i.depthPacking:"",`
`].filter(ml).join(`
`)),f=Mp(f),f=qv(f,i),f=Yv(f,i),p=Mp(p),p=qv(p,i),p=Yv(p,i),f=Zv(f),p=Zv(p),i.isRawShaderMaterial!==!0&&(B=`#version 300 es
`,M=[S,"#define attribute in","#define varying out","#define texture2D texture"].join(`
`)+`
`+M,y=["#define varying in",i.glslVersion===q_?"":"layout(location = 0) out highp vec4 pc_fragColor;",i.glslVersion===q_?"":"#define gl_FragColor pc_fragColor","#define gl_FragDepthEXT gl_FragDepth","#define texture2D texture","#define textureCube texture","#define texture2DProj textureProj","#define texture2DLodEXT textureLod","#define texture2DProjLodEXT textureProjLod","#define textureCubeLodEXT textureLod","#define texture2DGradEXT textureGrad","#define texture2DProjGradEXT textureProjGrad","#define textureCubeGradEXT textureGrad"].join(`
`)+`
`+y);const F=B+M+f,w=B+y+p,I=kv(l,l.VERTEX_SHADER,F),O=kv(l,l.FRAGMENT_SHADER,w);l.attachShader(A,I),l.attachShader(A,O),i.index0AttributeName!==void 0?l.bindAttribLocation(A,0,i.index0AttributeName):i.hasPositionAttribute===!0&&l.bindAttribLocation(A,0,"position"),l.linkProgram(A);function D(k){if(r.debug.checkShaderErrors){const J=l.getProgramInfoLog(A)||"",ut=l.getShaderInfoLog(I)||"",gt=l.getShaderInfoLog(O)||"",$=J.trim(),P=ut.trim(),H=gt.trim();let et=!0,pt=!0;if(l.getProgramParameter(A,l.LINK_STATUS)===!1)if(et=!1,typeof r.debug.onShaderError=="function")r.debug.onShaderError(l,A,I,O);else{const Et=Wv(l,I,"vertex"),N=Wv(l,O,"fragment");Ue("WebGLProgram: Shader Error "+l.getError()+" - VALIDATE_STATUS "+l.getProgramParameter(A,l.VALIDATE_STATUS)+`

Material Name: `+k.name+`
Material Type: `+k.type+`

Program Info Log: `+$+`
`+Et+`
`+N)}else $!==""?re("WebGLProgram: Program Info Log:",$):(P===""||H==="")&&(pt=!1);pt&&(k.diagnostics={runnable:et,programLog:$,vertexShader:{log:P,prefix:M},fragmentShader:{log:H,prefix:y}})}l.deleteShader(I),l.deleteShader(O),T=new gu(l,A),L=f3(l,A)}let T;this.getUniforms=function(){return T===void 0&&D(this),T};let L;this.getAttributes=function(){return L===void 0&&D(this),L};let j=i.rendererExtensionParallelShaderCompile===!1;return this.isReady=function(){return j===!1&&(j=l.getProgramParameter(A,e3)),j},this.destroy=function(){s.releaseStatesOfProgram(this),l.deleteProgram(A),this.program=void 0},this.type=i.shaderType,this.name=i.shaderName,this.id=n3++,this.cacheKey=t,this.usedTimes=1,this.program=A,this.vertexShader=I,this.fragmentShader=O,this}let R3=0;class C3{constructor(){this.shaderCache=new Map,this.materialCache=new Map}update(t,i,s){const l=this._getShaderCacheForMaterial(t);return l.has(i)===!1&&(l.add(i),i.usedTimes++),l.has(s)===!1&&(l.add(s),s.usedTimes++),this}remove(t){const i=this.materialCache.get(t);for(const s of i)s.usedTimes--,s.usedTimes===0&&this.shaderCache.delete(s.code);return this.materialCache.delete(t),this}getVertexShaderStage(t){return this._getShaderStage(t.vertexShader)}getFragmentShaderStage(t){return this._getShaderStage(t.fragmentShader)}dispose(){this.shaderCache.clear(),this.materialCache.clear()}_getShaderCacheForMaterial(t){const i=this.materialCache;let s=i.get(t);return s===void 0&&(s=new Set,i.set(t,s)),s}_getShaderStage(t){const i=this.shaderCache;let s=i.get(t);return s===void 0&&(s=new w3(t),i.set(t,s)),s}}class w3{constructor(t){this.id=R3++,this.code=t,this.usedTimes=0}}function D3(r){return r===js||r===vu||r===xu}function U3(r,t,i,s,l,c){const f=new Fp,p=new C3,m=new Set,d=[],_=new Map,v=s.logarithmicDepthBuffer;let g=s.precision;const S={MeshDepthMaterial:"depth",MeshDistanceMaterial:"distance",MeshNormalMaterial:"normal",MeshBasicMaterial:"basic",MeshLambertMaterial:"lambert",MeshPhongMaterial:"phong",MeshToonMaterial:"toon",MeshStandardMaterial:"physical",MeshPhysicalMaterial:"physical",MeshMatcapMaterial:"matcap",LineBasicMaterial:"basic",LineDashedMaterial:"dashed",PointsMaterial:"points",ShadowMaterial:"shadow",SpriteMaterial:"sprite"};function E(T){return m.add(T),T===0?"uv":`uv${T}`}function A(T,L,j,k,J,ut){const gt=k.fog,$=J.geometry,P=T.isMeshStandardMaterial||T.isMeshLambertMaterial||T.isMeshPhongMaterial?k.environment:null,H=T.isMeshStandardMaterial||T.isMeshLambertMaterial&&!T.envMap||T.isMeshPhongMaterial&&!T.envMap,et=t.get(T.envMap||P,H),pt=et&&et.mapping===Cu?et.image.height:null,Et=S[T.type];T.precision!==null&&(g=s.getMaxPrecision(T.precision),g!==T.precision&&re("WebGLProgram.getParameters:",T.precision,"not supported, using",g,"instead."));const N=$.morphAttributes.position||$.morphAttributes.normal||$.morphAttributes.color,W=N!==void 0?N.length:0;let vt=0;$.morphAttributes.position!==void 0&&(vt=1),$.morphAttributes.normal!==void 0&&(vt=2),$.morphAttributes.color!==void 0&&(vt=3);let Tt,At,tt,yt;if(Et){const Xt=Zi[Et];Tt=Xt.vertexShader,At=Xt.fragmentShader}else{Tt=T.vertexShader,At=T.fragmentShader;const Xt=p.getVertexShaderStage(T),an=p.getFragmentShaderStage(T);p.update(T,Xt,an),tt=Xt.id,yt=an.id}const Mt=r.getRenderTarget(),Nt=r.state.buffers.depth.getReversed(),ae=J.isInstancedMesh===!0,Qt=J.isBatchedMesh===!0,He=!!T.map,pe=!!T.matcap,Se=!!et,Me=!!T.aoMap,Ut=!!T.lightMap,Zt=!!T.bumpMap&&T.wireframe===!1,$t=!!T.normalMap,oe=!!T.displacementMap,Vt=!!T.emissiveMap,Ce=!!T.metalnessMap,me=!!T.roughnessMap,Y=T.anisotropy>0,we=T.clearcoat>0,Le=T.dispersion>0,U=T.iridescence>0,b=T.sheen>0,Q=T.transmission>0,rt=Y&&!!T.anisotropyMap,ht=we&&!!T.clearcoatMap,Rt=we&&!!T.clearcoatNormalMap,Lt=we&&!!T.clearcoatRoughnessMap,ft=U&&!!T.iridescenceMap,dt=U&&!!T.iridescenceThicknessMap,wt=b&&!!T.sheenColorMap,Ht=b&&!!T.sheenRoughnessMap,zt=!!T.specularMap,Ot=!!T.specularColorMap,te=!!T.specularIntensityMap,ee=Q&&!!T.transmissionMap,le=Q&&!!T.thicknessMap,q=!!T.gradientMap,Ct=!!T.alphaMap,_t=T.alphaTest>0,Dt=!!T.alphaHash,Ft=!!T.extensions;let bt=Ki;T.toneMapped&&(Mt===null||Mt.isXRRenderTarget===!0)&&(bt=r.toneMapping);const jt={shaderID:Et,shaderType:T.type,shaderName:T.name,vertexShader:Tt,fragmentShader:At,defines:T.defines,customVertexShaderID:tt,customFragmentShaderID:yt,isRawShaderMaterial:T.isRawShaderMaterial===!0,glslVersion:T.glslVersion,precision:g,batching:Qt,batchingColor:Qt&&J._colorsTexture!==null,instancing:ae,instancingColor:ae&&J.instanceColor!==null,instancingMorph:ae&&J.morphTexture!==null,outputColorSpace:Mt===null?r.outputColorSpace:Mt.isXRRenderTarget===!0?Mt.texture.colorSpace:Ne.workingColorSpace,alphaToCoverage:!!T.alphaToCoverage,map:He,matcap:pe,envMap:Se,envMapMode:Se&&et.mapping,envMapCubeUVHeight:pt,aoMap:Me,lightMap:Ut,bumpMap:Zt,normalMap:$t,displacementMap:oe,emissiveMap:Vt,normalMapObjectSpace:$t&&T.normalMapType===X1,normalMapTangentSpace:$t&&T.normalMapType===gp,packedNormalMap:$t&&T.normalMapType===gp&&D3(T.normalMap.format),metalnessMap:Ce,roughnessMap:me,anisotropy:Y,anisotropyMap:rt,clearcoat:we,clearcoatMap:ht,clearcoatNormalMap:Rt,clearcoatRoughnessMap:Lt,dispersion:Le,iridescence:U,iridescenceMap:ft,iridescenceThicknessMap:dt,sheen:b,sheenColorMap:wt,sheenRoughnessMap:Ht,specularMap:zt,specularColorMap:Ot,specularIntensityMap:te,transmission:Q,transmissionMap:ee,thicknessMap:le,gradientMap:q,opaque:T.transparent===!1&&T.blending===Qr&&T.alphaToCoverage===!1,alphaMap:Ct,alphaTest:_t,alphaHash:Dt,combine:T.combine,mapUv:He&&E(T.map.channel),aoMapUv:Me&&E(T.aoMap.channel),lightMapUv:Ut&&E(T.lightMap.channel),bumpMapUv:Zt&&E(T.bumpMap.channel),normalMapUv:$t&&E(T.normalMap.channel),displacementMapUv:oe&&E(T.displacementMap.channel),emissiveMapUv:Vt&&E(T.emissiveMap.channel),metalnessMapUv:Ce&&E(T.metalnessMap.channel),roughnessMapUv:me&&E(T.roughnessMap.channel),anisotropyMapUv:rt&&E(T.anisotropyMap.channel),clearcoatMapUv:ht&&E(T.clearcoatMap.channel),clearcoatNormalMapUv:Rt&&E(T.clearcoatNormalMap.channel),clearcoatRoughnessMapUv:Lt&&E(T.clearcoatRoughnessMap.channel),iridescenceMapUv:ft&&E(T.iridescenceMap.channel),iridescenceThicknessMapUv:dt&&E(T.iridescenceThicknessMap.channel),sheenColorMapUv:wt&&E(T.sheenColorMap.channel),sheenRoughnessMapUv:Ht&&E(T.sheenRoughnessMap.channel),specularMapUv:zt&&E(T.specularMap.channel),specularColorMapUv:Ot&&E(T.specularColorMap.channel),specularIntensityMapUv:te&&E(T.specularIntensityMap.channel),transmissionMapUv:ee&&E(T.transmissionMap.channel),thicknessMapUv:le&&E(T.thicknessMap.channel),alphaMapUv:Ct&&E(T.alphaMap.channel),vertexTangents:!!$.attributes.tangent&&($t||Y),vertexNormals:!!$.attributes.normal,vertexColors:T.vertexColors,vertexAlphas:T.vertexColors===!0&&!!$.attributes.color&&$.attributes.color.itemSize===4,pointsUvs:J.isPoints===!0&&!!$.attributes.uv&&(He||Ct),fog:!!gt,useFog:T.fog===!0,fogExp2:!!gt&&gt.isFogExp2,flatShading:T.wireframe===!1&&(T.flatShading===!0||$.attributes.normal===void 0&&$t===!1&&(T.isMeshLambertMaterial||T.isMeshPhongMaterial||T.isMeshStandardMaterial||T.isMeshPhysicalMaterial)),sizeAttenuation:T.sizeAttenuation===!0,logarithmicDepthBuffer:v,reversedDepthBuffer:Nt,skinning:J.isSkinnedMesh===!0,hasPositionAttribute:$.attributes.position!==void 0,morphTargets:$.morphAttributes.position!==void 0,morphNormals:$.morphAttributes.normal!==void 0,morphColors:$.morphAttributes.color!==void 0,morphTargetsCount:W,morphTextureStride:vt,numDirLights:L.directional.length,numPointLights:L.point.length,numSpotLights:L.spot.length,numSpotLightMaps:L.spotLightMap.length,numRectAreaLights:L.rectArea.length,numHemiLights:L.hemi.length,numDirLightShadows:L.directionalShadowMap.length,numPointLightShadows:L.pointShadowMap.length,numSpotLightShadows:L.spotShadowMap.length,numSpotLightShadowsWithMaps:L.numSpotLightShadowsWithMaps,numLightProbes:L.numLightProbes,numLightProbeGrids:ut.length,numClippingPlanes:c.numPlanes,numClipIntersection:c.numIntersection,dithering:T.dithering,shadowMapEnabled:r.shadowMap.enabled&&j.length>0,shadowMapType:r.shadowMap.type,toneMapping:bt,decodeVideoTexture:He&&T.map.isVideoTexture===!0&&Ne.getTransfer(T.map.colorSpace)===Ze,decodeVideoTextureEmissive:Vt&&T.emissiveMap.isVideoTexture===!0&&Ne.getTransfer(T.emissiveMap.colorSpace)===Ze,premultipliedAlpha:T.premultipliedAlpha,doubleSided:T.side===Ci,flipSided:T.side===ti,useDepthPacking:T.depthPacking>=0,depthPacking:T.depthPacking||0,index0AttributeName:T.index0AttributeName,extensionClipCullDistance:Ft&&T.extensions.clipCullDistance===!0&&i.has("WEBGL_clip_cull_distance"),extensionMultiDraw:(Ft&&T.extensions.multiDraw===!0||Qt)&&i.has("WEBGL_multi_draw"),rendererExtensionParallelShaderCompile:i.has("KHR_parallel_shader_compile"),customProgramCacheKey:T.customProgramCacheKey()};return jt.vertexUv1s=m.has(1),jt.vertexUv2s=m.has(2),jt.vertexUv3s=m.has(3),m.clear(),jt}function M(T){const L=[];if(T.shaderID?L.push(T.shaderID):(L.push(T.customVertexShaderID),L.push(T.customFragmentShaderID)),T.defines!==void 0)for(const j in T.defines)L.push(j),L.push(T.defines[j]);return T.isRawShaderMaterial===!1&&(y(L,T),B(L,T),L.push(r.outputColorSpace)),L.push(T.customProgramCacheKey),L.join()}function y(T,L){T.push(L.precision),T.push(L.outputColorSpace),T.push(L.envMapMode),T.push(L.envMapCubeUVHeight),T.push(L.mapUv),T.push(L.alphaMapUv),T.push(L.lightMapUv),T.push(L.aoMapUv),T.push(L.bumpMapUv),T.push(L.normalMapUv),T.push(L.displacementMapUv),T.push(L.emissiveMapUv),T.push(L.metalnessMapUv),T.push(L.roughnessMapUv),T.push(L.anisotropyMapUv),T.push(L.clearcoatMapUv),T.push(L.clearcoatNormalMapUv),T.push(L.clearcoatRoughnessMapUv),T.push(L.iridescenceMapUv),T.push(L.iridescenceThicknessMapUv),T.push(L.sheenColorMapUv),T.push(L.sheenRoughnessMapUv),T.push(L.specularMapUv),T.push(L.specularColorMapUv),T.push(L.specularIntensityMapUv),T.push(L.transmissionMapUv),T.push(L.thicknessMapUv),T.push(L.combine),T.push(L.fogExp2),T.push(L.sizeAttenuation),T.push(L.morphTargetsCount),T.push(L.morphAttributeCount),T.push(L.numDirLights),T.push(L.numPointLights),T.push(L.numSpotLights),T.push(L.numSpotLightMaps),T.push(L.numHemiLights),T.push(L.numRectAreaLights),T.push(L.numDirLightShadows),T.push(L.numPointLightShadows),T.push(L.numSpotLightShadows),T.push(L.numSpotLightShadowsWithMaps),T.push(L.numLightProbes),T.push(L.shadowMapType),T.push(L.toneMapping),T.push(L.numClippingPlanes),T.push(L.numClipIntersection),T.push(L.depthPacking)}function B(T,L){f.disableAll(),L.instancing&&f.enable(0),L.instancingColor&&f.enable(1),L.instancingMorph&&f.enable(2),L.matcap&&f.enable(3),L.envMap&&f.enable(4),L.normalMapObjectSpace&&f.enable(5),L.normalMapTangentSpace&&f.enable(6),L.clearcoat&&f.enable(7),L.iridescence&&f.enable(8),L.alphaTest&&f.enable(9),L.vertexColors&&f.enable(10),L.vertexAlphas&&f.enable(11),L.vertexUv1s&&f.enable(12),L.vertexUv2s&&f.enable(13),L.vertexUv3s&&f.enable(14),L.vertexTangents&&f.enable(15),L.anisotropy&&f.enable(16),L.alphaHash&&f.enable(17),L.batching&&f.enable(18),L.dispersion&&f.enable(19),L.batchingColor&&f.enable(20),L.gradientMap&&f.enable(21),L.packedNormalMap&&f.enable(22),L.vertexNormals&&f.enable(23),T.push(f.mask),f.disableAll(),L.fog&&f.enable(0),L.useFog&&f.enable(1),L.flatShading&&f.enable(2),L.logarithmicDepthBuffer&&f.enable(3),L.reversedDepthBuffer&&f.enable(4),L.skinning&&f.enable(5),L.morphTargets&&f.enable(6),L.morphNormals&&f.enable(7),L.morphColors&&f.enable(8),L.premultipliedAlpha&&f.enable(9),L.shadowMapEnabled&&f.enable(10),L.doubleSided&&f.enable(11),L.flipSided&&f.enable(12),L.useDepthPacking&&f.enable(13),L.dithering&&f.enable(14),L.transmission&&f.enable(15),L.sheen&&f.enable(16),L.opaque&&f.enable(17),L.pointsUvs&&f.enable(18),L.decodeVideoTexture&&f.enable(19),L.decodeVideoTextureEmissive&&f.enable(20),L.alphaToCoverage&&f.enable(21),L.numLightProbeGrids>0&&f.enable(22),L.hasPositionAttribute&&f.enable(23),T.push(f.mask)}function F(T){const L=S[T.type];let j;if(L){const k=Zi[L];j=YE.clone(k.uniforms)}else j=T.uniforms;return j}function w(T,L){let j=_.get(L);return j!==void 0?++j.usedTimes:(j=new A3(r,L,T,l),d.push(j),_.set(L,j)),j}function I(T){if(--T.usedTimes===0){const L=d.indexOf(T);d[L]=d[d.length-1],d.pop(),_.delete(T.cacheKey),T.destroy()}}function O(T){p.remove(T)}function D(){p.dispose()}return{getParameters:A,getProgramCacheKey:M,getUniforms:F,acquireProgram:w,releaseProgram:I,releaseShaderCache:O,programs:d,dispose:D}}function N3(){let r=new WeakMap;function t(f){return r.has(f)}function i(f){let p=r.get(f);return p===void 0&&(p={},r.set(f,p)),p}function s(f){r.delete(f)}function l(f,p,m){r.get(f)[p]=m}function c(){r=new WeakMap}return{has:t,get:i,remove:s,update:l,dispose:c}}function L3(r,t){return r.groupOrder!==t.groupOrder?r.groupOrder-t.groupOrder:r.renderOrder!==t.renderOrder?r.renderOrder-t.renderOrder:r.material.id!==t.material.id?r.material.id-t.material.id:r.materialVariant!==t.materialVariant?r.materialVariant-t.materialVariant:r.z!==t.z?r.z-t.z:r.id-t.id}function Kv(r,t){return r.groupOrder!==t.groupOrder?r.groupOrder-t.groupOrder:r.renderOrder!==t.renderOrder?r.renderOrder-t.renderOrder:r.z!==t.z?t.z-r.z:r.id-t.id}function Qv(){const r=[];let t=0;const i=[],s=[],l=[];function c(){t=0,i.length=0,s.length=0,l.length=0}function f(g){let S=0;return g.isInstancedMesh&&(S+=2),g.isSkinnedMesh&&(S+=1),S}function p(g,S,E,A,M,y){let B=r[t];return B===void 0?(B={id:g.id,object:g,geometry:S,material:E,materialVariant:f(g),groupOrder:A,renderOrder:g.renderOrder,z:M,group:y},r[t]=B):(B.id=g.id,B.object=g,B.geometry=S,B.material=E,B.materialVariant=f(g),B.groupOrder=A,B.renderOrder=g.renderOrder,B.z=M,B.group=y),t++,B}function m(g,S,E,A,M,y){const B=p(g,S,E,A,M,y);E.transmission>0?s.push(B):E.transparent===!0?l.push(B):i.push(B)}function d(g,S,E,A,M,y){const B=p(g,S,E,A,M,y);E.transmission>0?s.unshift(B):E.transparent===!0?l.unshift(B):i.unshift(B)}function _(g,S,E){i.length>1&&i.sort(g||L3),s.length>1&&s.sort(S||Kv),l.length>1&&l.sort(S||Kv),E&&(i.reverse(),s.reverse(),l.reverse())}function v(){for(let g=t,S=r.length;g<S;g++){const E=r[g];if(E.id===null)break;E.id=null,E.object=null,E.geometry=null,E.material=null,E.group=null}}return{opaque:i,transmissive:s,transparent:l,init:c,push:m,unshift:d,finish:v,sort:_}}function O3(){let r=new WeakMap;function t(s,l){const c=r.get(s);let f;return c===void 0?(f=new Qv,r.set(s,[f])):l>=c.length?(f=new Qv,c.push(f)):f=c[l],f}function i(){r=new WeakMap}return{get:t,dispose:i}}function P3(){const r={};return{get:function(t){if(r[t.id]!==void 0)return r[t.id];let i;switch(t.type){case"DirectionalLight":i={direction:new V,color:new ue};break;case"SpotLight":i={position:new V,direction:new V,color:new ue,distance:0,coneCos:0,penumbraCos:0,decay:0};break;case"PointLight":i={position:new V,color:new ue,distance:0,decay:0};break;case"HemisphereLight":i={direction:new V,skyColor:new ue,groundColor:new ue};break;case"RectAreaLight":i={color:new ue,position:new V,halfWidth:new V,halfHeight:new V};break}return r[t.id]=i,i}}}function z3(){const r={};return{get:function(t){if(r[t.id]!==void 0)return r[t.id];let i;switch(t.type){case"DirectionalLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new fe};break;case"SpotLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new fe};break;case"PointLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new fe,shadowCameraNear:1,shadowCameraFar:1e3};break}return r[t.id]=i,i}}}let B3=0;function I3(r,t){return(t.castShadow?2:0)-(r.castShadow?2:0)+(t.map?1:0)-(r.map?1:0)}function F3(r){const t=new P3,i=z3(),s={version:0,hash:{directionalLength:-1,pointLength:-1,spotLength:-1,rectAreaLength:-1,hemiLength:-1,numDirectionalShadows:-1,numPointShadows:-1,numSpotShadows:-1,numSpotMaps:-1,numLightProbes:-1},ambient:[0,0,0],probe:[],directional:[],directionalShadow:[],directionalShadowMap:[],directionalShadowMatrix:[],spot:[],spotLightMap:[],spotShadow:[],spotShadowMap:[],spotLightMatrix:[],rectArea:[],rectAreaLTC1:null,rectAreaLTC2:null,point:[],pointShadow:[],pointShadowMap:[],pointShadowMatrix:[],hemi:[],numSpotLightShadowsWithMaps:0,numLightProbes:0};for(let d=0;d<9;d++)s.probe.push(new V);const l=new V,c=new Fe,f=new Fe;function p(d){let _=0,v=0,g=0;for(let L=0;L<9;L++)s.probe[L].set(0,0,0);let S=0,E=0,A=0,M=0,y=0,B=0,F=0,w=0,I=0,O=0,D=0;d.sort(I3);for(let L=0,j=d.length;L<j;L++){const k=d[L],J=k.color,ut=k.intensity,gt=k.distance;let $=null;if(k.shadow&&k.shadow.map&&(k.shadow.map.texture.format===js?$=k.shadow.map.texture:$=k.shadow.map.depthTexture||k.shadow.map.texture),k.isAmbientLight)_+=J.r*ut,v+=J.g*ut,g+=J.b*ut;else if(k.isLightProbe){for(let P=0;P<9;P++)s.probe[P].addScaledVector(k.sh.coefficients[P],ut);D++}else if(k.isDirectionalLight){const P=t.get(k);if(P.color.copy(k.color).multiplyScalar(k.intensity),k.castShadow){const H=k.shadow,et=i.get(k);et.shadowIntensity=H.intensity,et.shadowBias=H.bias,et.shadowNormalBias=H.normalBias,et.shadowRadius=H.radius,et.shadowMapSize=H.mapSize,s.directionalShadow[S]=et,s.directionalShadowMap[S]=$,s.directionalShadowMatrix[S]=k.shadow.matrix,B++}s.directional[S]=P,S++}else if(k.isSpotLight){const P=t.get(k);P.position.setFromMatrixPosition(k.matrixWorld),P.color.copy(J).multiplyScalar(ut),P.distance=gt,P.coneCos=Math.cos(k.angle),P.penumbraCos=Math.cos(k.angle*(1-k.penumbra)),P.decay=k.decay,s.spot[A]=P;const H=k.shadow;if(k.map&&(s.spotLightMap[I]=k.map,I++,H.updateMatrices(k),k.castShadow&&O++),s.spotLightMatrix[A]=H.matrix,k.castShadow){const et=i.get(k);et.shadowIntensity=H.intensity,et.shadowBias=H.bias,et.shadowNormalBias=H.normalBias,et.shadowRadius=H.radius,et.shadowMapSize=H.mapSize,s.spotShadow[A]=et,s.spotShadowMap[A]=$,w++}A++}else if(k.isRectAreaLight){const P=t.get(k);P.color.copy(J).multiplyScalar(ut),P.halfWidth.set(k.width*.5,0,0),P.halfHeight.set(0,k.height*.5,0),s.rectArea[M]=P,M++}else if(k.isPointLight){const P=t.get(k);if(P.color.copy(k.color).multiplyScalar(k.intensity),P.distance=k.distance,P.decay=k.decay,k.castShadow){const H=k.shadow,et=i.get(k);et.shadowIntensity=H.intensity,et.shadowBias=H.bias,et.shadowNormalBias=H.normalBias,et.shadowRadius=H.radius,et.shadowMapSize=H.mapSize,et.shadowCameraNear=H.camera.near,et.shadowCameraFar=H.camera.far,s.pointShadow[E]=et,s.pointShadowMap[E]=$,s.pointShadowMatrix[E]=k.shadow.matrix,F++}s.point[E]=P,E++}else if(k.isHemisphereLight){const P=t.get(k);P.skyColor.copy(k.color).multiplyScalar(ut),P.groundColor.copy(k.groundColor).multiplyScalar(ut),s.hemi[y]=P,y++}}M>0&&(r.has("OES_texture_float_linear")===!0?(s.rectAreaLTC1=It.LTC_FLOAT_1,s.rectAreaLTC2=It.LTC_FLOAT_2):(s.rectAreaLTC1=It.LTC_HALF_1,s.rectAreaLTC2=It.LTC_HALF_2)),s.ambient[0]=_,s.ambient[1]=v,s.ambient[2]=g;const T=s.hash;(T.directionalLength!==S||T.pointLength!==E||T.spotLength!==A||T.rectAreaLength!==M||T.hemiLength!==y||T.numDirectionalShadows!==B||T.numPointShadows!==F||T.numSpotShadows!==w||T.numSpotMaps!==I||T.numLightProbes!==D)&&(s.directional.length=S,s.spot.length=A,s.rectArea.length=M,s.point.length=E,s.hemi.length=y,s.directionalShadow.length=B,s.directionalShadowMap.length=B,s.pointShadow.length=F,s.pointShadowMap.length=F,s.spotShadow.length=w,s.spotShadowMap.length=w,s.directionalShadowMatrix.length=B,s.pointShadowMatrix.length=F,s.spotLightMatrix.length=w+I-O,s.spotLightMap.length=I,s.numSpotLightShadowsWithMaps=O,s.numLightProbes=D,T.directionalLength=S,T.pointLength=E,T.spotLength=A,T.rectAreaLength=M,T.hemiLength=y,T.numDirectionalShadows=B,T.numPointShadows=F,T.numSpotShadows=w,T.numSpotMaps=I,T.numLightProbes=D,s.version=B3++)}function m(d,_){let v=0,g=0,S=0,E=0,A=0;const M=_.matrixWorldInverse;for(let y=0,B=d.length;y<B;y++){const F=d[y];if(F.isDirectionalLight){const w=s.directional[v];w.direction.setFromMatrixPosition(F.matrixWorld),l.setFromMatrixPosition(F.target.matrixWorld),w.direction.sub(l),w.direction.transformDirection(M),v++}else if(F.isSpotLight){const w=s.spot[S];w.position.setFromMatrixPosition(F.matrixWorld),w.position.applyMatrix4(M),w.direction.setFromMatrixPosition(F.matrixWorld),l.setFromMatrixPosition(F.target.matrixWorld),w.direction.sub(l),w.direction.transformDirection(M),S++}else if(F.isRectAreaLight){const w=s.rectArea[E];w.position.setFromMatrixPosition(F.matrixWorld),w.position.applyMatrix4(M),f.identity(),c.copy(F.matrixWorld),c.premultiply(M),f.extractRotation(c),w.halfWidth.set(F.width*.5,0,0),w.halfHeight.set(0,F.height*.5,0),w.halfWidth.applyMatrix4(f),w.halfHeight.applyMatrix4(f),E++}else if(F.isPointLight){const w=s.point[g];w.position.setFromMatrixPosition(F.matrixWorld),w.position.applyMatrix4(M),g++}else if(F.isHemisphereLight){const w=s.hemi[A];w.direction.setFromMatrixPosition(F.matrixWorld),w.direction.transformDirection(M),A++}}}return{setup:p,setupView:m,state:s}}function Jv(r){const t=new F3(r),i=[],s=[],l=[];function c(g){v.camera=g,i.length=0,s.length=0,l.length=0}function f(g){i.push(g)}function p(g){s.push(g)}function m(g){l.push(g)}function d(){t.setup(i)}function _(g){t.setupView(i,g)}const v={lightsArray:i,shadowsArray:s,lightProbeGridArray:l,camera:null,lights:t,transmissionRenderTarget:{},textureUnits:0};return{init:c,state:v,setupLights:d,setupLightsView:_,pushLight:f,pushShadow:p,pushLightProbeGrid:m}}function H3(r){let t=new WeakMap;function i(l,c=0){const f=t.get(l);let p;return f===void 0?(p=new Jv(r),t.set(l,[p])):c>=f.length?(p=new Jv(r),f.push(p)):p=f[c],p}function s(){t=new WeakMap}return{get:i,dispose:s}}const G3=`void main() {
	gl_Position = vec4( position, 1.0 );
}`,V3=`uniform sampler2D shadow_pass;
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
}`,k3=[new V(1,0,0),new V(-1,0,0),new V(0,1,0),new V(0,-1,0),new V(0,0,1),new V(0,0,-1)],X3=[new V(0,-1,0),new V(0,-1,0),new V(0,0,1),new V(0,0,-1),new V(0,-1,0),new V(0,-1,0)],$v=new Fe,dl=new V,Rd=new V;function W3(r,t,i){let s=new Vp;const l=new fe,c=new fe,f=new cn,p=new JE,m=new $E,d={},_=i.maxTextureSize,v={[hs]:ti,[ti]:hs,[Ci]:Ci},g=new $i({defines:{VSM_SAMPLES:8},uniforms:{shadow_pass:{value:null},resolution:{value:new fe},radius:{value:4}},vertexShader:G3,fragmentShader:V3}),S=g.clone();S.defines.HORIZONTAL_PASS=1;const E=new hn;E.setAttribute("position",new vi(new Float32Array([-1,-1,.5,3,-1,.5,-1,3,.5]),3));const A=new ln(E,g),M=this;this.enabled=!1,this.autoUpdate=!0,this.needsUpdate=!1,this.type=gl;let y=this.type;this.render=function(O,D,T){if(M.enabled===!1||M.autoUpdate===!1&&M.needsUpdate===!1||O.length===0)return;this.type===S1&&(re("WebGLShadowMap: PCFSoftShadowMap has been deprecated. Using PCFShadowMap instead."),this.type=gl);const L=r.getRenderTarget(),j=r.getActiveCubeFace(),k=r.getActiveMipmapLevel(),J=r.state;J.setBlending(Ta),J.buffers.depth.getReversed()===!0?J.buffers.color.setClear(0,0,0,0):J.buffers.color.setClear(1,1,1,1),J.buffers.depth.setTest(!0),J.setScissorTest(!1);const ut=y!==this.type;ut&&D.traverse(function(gt){gt.material&&(Array.isArray(gt.material)?gt.material.forEach($=>$.needsUpdate=!0):gt.material.needsUpdate=!0)});for(let gt=0,$=O.length;gt<$;gt++){const P=O[gt],H=P.shadow;if(H===void 0){re("WebGLShadowMap:",P,"has no shadow.");continue}if(H.autoUpdate===!1&&H.needsUpdate===!1)continue;l.copy(H.mapSize);const et=H.getFrameExtents();l.multiply(et),c.copy(H.mapSize),(l.x>_||l.y>_)&&(l.x>_&&(c.x=Math.floor(_/et.x),l.x=c.x*et.x,H.mapSize.x=c.x),l.y>_&&(c.y=Math.floor(_/et.y),l.y=c.y*et.y,H.mapSize.y=c.y));const pt=r.state.buffers.depth.getReversed();if(H.camera._reversedDepth=pt,H.map===null||ut===!0){if(H.map!==null&&(H.map.depthTexture!==null&&(H.map.depthTexture.dispose(),H.map.depthTexture=null),H.map.dispose()),this.type===pl){if(P.isPointLight){re("WebGLShadowMap: VSM shadow maps are not supported for PointLights. Use PCF or BasicShadowMap instead.");continue}H.map=new Qi(l.x,l.y,{format:js,type:Ca,minFilter:Gn,magFilter:Gn,generateMipmaps:!1}),H.map.texture.name=P.name+".shadowMap",H.map.depthTexture=new ao(l.x,l.y,Ii),H.map.depthTexture.name=P.name+".shadowMapDepth",H.map.depthTexture.format=wa,H.map.depthTexture.compareFunction=null,H.map.depthTexture.minFilter=zn,H.map.depthTexture.magFilter=zn}else P.isPointLight?(H.map=new Fx(l.x),H.map.depthTexture=new VE(l.x,Ji)):(H.map=new Qi(l.x,l.y),H.map.depthTexture=new ao(l.x,l.y,Ji)),H.map.depthTexture.name=P.name+".shadowMap",H.map.depthTexture.format=wa,this.type===gl?(H.map.depthTexture.compareFunction=pt?zp:Pp,H.map.depthTexture.minFilter=Gn,H.map.depthTexture.magFilter=Gn):(H.map.depthTexture.compareFunction=null,H.map.depthTexture.minFilter=zn,H.map.depthTexture.magFilter=zn);H.camera.updateProjectionMatrix()}const Et=H.map.isWebGLCubeRenderTarget?6:1;for(let N=0;N<Et;N++){if(H.map.isWebGLCubeRenderTarget)r.setRenderTarget(H.map,N),r.clear();else{N===0&&(r.setRenderTarget(H.map),r.clear());const W=H.getViewport(N);f.set(c.x*W.x,c.y*W.y,c.x*W.z,c.y*W.w),J.viewport(f)}if(P.isPointLight){const W=H.camera,vt=H.matrix,Tt=P.distance||W.far;Tt!==W.far&&(W.far=Tt,W.updateProjectionMatrix()),dl.setFromMatrixPosition(P.matrixWorld),W.position.copy(dl),Rd.copy(W.position),Rd.add(k3[N]),W.up.copy(X3[N]),W.lookAt(Rd),W.updateMatrixWorld(),vt.makeTranslation(-dl.x,-dl.y,-dl.z),$v.multiplyMatrices(W.projectionMatrix,W.matrixWorldInverse),H._frustum.setFromProjectionMatrix($v,W.coordinateSystem,W.reversedDepth)}else H.updateMatrices(P);s=H.getFrustum(),w(D,T,H.camera,P,this.type)}H.isPointLightShadow!==!0&&this.type===pl&&B(H,T),H.needsUpdate=!1}y=this.type,M.needsUpdate=!1,r.setRenderTarget(L,j,k)};function B(O,D){const T=t.update(A);g.defines.VSM_SAMPLES!==O.blurSamples&&(g.defines.VSM_SAMPLES=O.blurSamples,S.defines.VSM_SAMPLES=O.blurSamples,g.needsUpdate=!0,S.needsUpdate=!0),O.mapPass===null&&(O.mapPass=new Qi(l.x,l.y,{format:js,type:Ca})),g.uniforms.shadow_pass.value=O.map.depthTexture,g.uniforms.resolution.value=O.mapSize,g.uniforms.radius.value=O.radius,r.setRenderTarget(O.mapPass),r.clear(),r.renderBufferDirect(D,null,T,g,A,null),S.uniforms.shadow_pass.value=O.mapPass.texture,S.uniforms.resolution.value=O.mapSize,S.uniforms.radius.value=O.radius,r.setRenderTarget(O.map),r.clear(),r.renderBufferDirect(D,null,T,S,A,null)}function F(O,D,T,L){let j=null;const k=T.isPointLight===!0?O.customDistanceMaterial:O.customDepthMaterial;if(k!==void 0)j=k;else if(j=T.isPointLight===!0?m:p,r.localClippingEnabled&&D.clipShadows===!0&&Array.isArray(D.clippingPlanes)&&D.clippingPlanes.length!==0||D.displacementMap&&D.displacementScale!==0||D.alphaMap&&D.alphaTest>0||D.map&&D.alphaTest>0||D.alphaToCoverage===!0){const J=j.uuid,ut=D.uuid;let gt=d[J];gt===void 0&&(gt={},d[J]=gt);let $=gt[ut];$===void 0&&($=j.clone(),gt[ut]=$,D.addEventListener("dispose",I)),j=$}if(j.visible=D.visible,j.wireframe=D.wireframe,L===pl?j.side=D.shadowSide!==null?D.shadowSide:D.side:j.side=D.shadowSide!==null?D.shadowSide:v[D.side],j.alphaMap=D.alphaMap,j.alphaTest=D.alphaToCoverage===!0?.5:D.alphaTest,j.map=D.map,j.clipShadows=D.clipShadows,j.clippingPlanes=D.clippingPlanes,j.clipIntersection=D.clipIntersection,j.displacementMap=D.displacementMap,j.displacementScale=D.displacementScale,j.displacementBias=D.displacementBias,j.wireframeLinewidth=D.wireframeLinewidth,j.linewidth=D.linewidth,T.isPointLight===!0&&j.isMeshDistanceMaterial===!0){const J=r.properties.get(j);J.light=T}return j}function w(O,D,T,L,j){if(O.visible===!1)return;if(O.layers.test(D.layers)&&(O.isMesh||O.isLine||O.isPoints)&&(O.castShadow||O.receiveShadow&&j===pl)&&(!O.frustumCulled||s.intersectsObject(O))){O.modelViewMatrix.multiplyMatrices(T.matrixWorldInverse,O.matrixWorld);const ut=t.update(O),gt=O.material;if(Array.isArray(gt)){const $=ut.groups;for(let P=0,H=$.length;P<H;P++){const et=$[P],pt=gt[et.materialIndex];if(pt&&pt.visible){const Et=F(O,pt,L,j);O.onBeforeShadow(r,O,D,T,ut,Et,et),r.renderBufferDirect(T,null,ut,Et,O,et),O.onAfterShadow(r,O,D,T,ut,Et,et)}}}else if(gt.visible){const $=F(O,gt,L,j);O.onBeforeShadow(r,O,D,T,ut,$,null),r.renderBufferDirect(T,null,ut,$,O,null),O.onAfterShadow(r,O,D,T,ut,$,null)}}const J=O.children;for(let ut=0,gt=J.length;ut<gt;ut++)w(J[ut],D,T,L,j)}function I(O){O.target.removeEventListener("dispose",I);for(const T in d){const L=d[T],j=O.target.uuid;j in L&&(L[j].dispose(),delete L[j])}}}function q3(r,t){function i(){let q=!1;const Ct=new cn;let _t=null;const Dt=new cn(0,0,0,0);return{setMask:function(Ft){_t!==Ft&&!q&&(r.colorMask(Ft,Ft,Ft,Ft),_t=Ft)},setLocked:function(Ft){q=Ft},setClear:function(Ft,bt,jt,Xt,an){an===!0&&(Ft*=Xt,bt*=Xt,jt*=Xt),Ct.set(Ft,bt,jt,Xt),Dt.equals(Ct)===!1&&(r.clearColor(Ft,bt,jt,Xt),Dt.copy(Ct))},reset:function(){q=!1,_t=null,Dt.set(-1,0,0,0)}}}function s(){let q=!1,Ct=!1,_t=null,Dt=null,Ft=null;return{setReversed:function(bt){if(Ct!==bt){const jt=t.get("EXT_clip_control");bt?jt.clipControlEXT(jt.LOWER_LEFT_EXT,jt.ZERO_TO_ONE_EXT):jt.clipControlEXT(jt.LOWER_LEFT_EXT,jt.NEGATIVE_ONE_TO_ONE_EXT),Ct=bt;const Xt=Ft;Ft=null,this.setClear(Xt)}},getReversed:function(){return Ct},setTest:function(bt){bt?Mt(r.DEPTH_TEST):Nt(r.DEPTH_TEST)},setMask:function(bt){_t!==bt&&!q&&(r.depthMask(bt),_t=bt)},setFunc:function(bt){if(Ct&&(bt=tE[bt]),Dt!==bt){switch(bt){case Ud:r.depthFunc(r.NEVER);break;case Nd:r.depthFunc(r.ALWAYS);break;case Ld:r.depthFunc(r.LESS);break;case eo:r.depthFunc(r.LEQUAL);break;case Od:r.depthFunc(r.EQUAL);break;case Pd:r.depthFunc(r.GEQUAL);break;case zd:r.depthFunc(r.GREATER);break;case Bd:r.depthFunc(r.NOTEQUAL);break;default:r.depthFunc(r.LEQUAL)}Dt=bt}},setLocked:function(bt){q=bt},setClear:function(bt){Ft!==bt&&(Ft=bt,Ct&&(bt=1-bt),r.clearDepth(bt))},reset:function(){q=!1,_t=null,Dt=null,Ft=null,Ct=!1}}}function l(){let q=!1,Ct=null,_t=null,Dt=null,Ft=null,bt=null,jt=null,Xt=null,an=null;return{setTest:function(Ve){q||(Ve?Mt(r.STENCIL_TEST):Nt(r.STENCIL_TEST))},setMask:function(Ve){Ct!==Ve&&!q&&(r.stencilMask(Ve),Ct=Ve)},setFunc:function(Ve,ei,ni){(_t!==Ve||Dt!==ei||Ft!==ni)&&(r.stencilFunc(Ve,ei,ni),_t=Ve,Dt=ei,Ft=ni)},setOp:function(Ve,ei,ni){(bt!==Ve||jt!==ei||Xt!==ni)&&(r.stencilOp(Ve,ei,ni),bt=Ve,jt=ei,Xt=ni)},setLocked:function(Ve){q=Ve},setClear:function(Ve){an!==Ve&&(r.clearStencil(Ve),an=Ve)},reset:function(){q=!1,Ct=null,_t=null,Dt=null,Ft=null,bt=null,jt=null,Xt=null,an=null}}}const c=new i,f=new s,p=new l,m=new WeakMap,d=new WeakMap;let _={},v={},g={},S=new WeakMap,E=[],A=null,M=!1,y=null,B=null,F=null,w=null,I=null,O=null,D=null,T=new ue(0,0,0),L=0,j=!1,k=null,J=null,ut=null,gt=null,$=null;const P=r.getParameter(r.MAX_COMBINED_TEXTURE_IMAGE_UNITS);let H=!1,et=0;const pt=r.getParameter(r.VERSION);pt.indexOf("WebGL")!==-1?(et=parseFloat(/^WebGL (\d)/.exec(pt)[1]),H=et>=1):pt.indexOf("OpenGL ES")!==-1&&(et=parseFloat(/^OpenGL ES (\d)/.exec(pt)[1]),H=et>=2);let Et=null,N={};const W=r.getParameter(r.SCISSOR_BOX),vt=r.getParameter(r.VIEWPORT),Tt=new cn().fromArray(W),At=new cn().fromArray(vt);function tt(q,Ct,_t,Dt){const Ft=new Uint8Array(4),bt=r.createTexture();r.bindTexture(q,bt),r.texParameteri(q,r.TEXTURE_MIN_FILTER,r.NEAREST),r.texParameteri(q,r.TEXTURE_MAG_FILTER,r.NEAREST);for(let jt=0;jt<_t;jt++)q===r.TEXTURE_3D||q===r.TEXTURE_2D_ARRAY?r.texImage3D(Ct,0,r.RGBA,1,1,Dt,0,r.RGBA,r.UNSIGNED_BYTE,Ft):r.texImage2D(Ct+jt,0,r.RGBA,1,1,0,r.RGBA,r.UNSIGNED_BYTE,Ft);return bt}const yt={};yt[r.TEXTURE_2D]=tt(r.TEXTURE_2D,r.TEXTURE_2D,1),yt[r.TEXTURE_CUBE_MAP]=tt(r.TEXTURE_CUBE_MAP,r.TEXTURE_CUBE_MAP_POSITIVE_X,6),yt[r.TEXTURE_2D_ARRAY]=tt(r.TEXTURE_2D_ARRAY,r.TEXTURE_2D_ARRAY,1,1),yt[r.TEXTURE_3D]=tt(r.TEXTURE_3D,r.TEXTURE_3D,1,1),c.setClear(0,0,0,1),f.setClear(1),p.setClear(0),Mt(r.DEPTH_TEST),f.setFunc(eo),Zt(!1),$t(V_),Mt(r.CULL_FACE),Me(Ta);function Mt(q){_[q]!==!0&&(r.enable(q),_[q]=!0)}function Nt(q){_[q]!==!1&&(r.disable(q),_[q]=!1)}function ae(q,Ct){return g[q]!==Ct?(r.bindFramebuffer(q,Ct),g[q]=Ct,q===r.DRAW_FRAMEBUFFER&&(g[r.FRAMEBUFFER]=Ct),q===r.FRAMEBUFFER&&(g[r.DRAW_FRAMEBUFFER]=Ct),!0):!1}function Qt(q,Ct){let _t=E,Dt=!1;if(q){_t=S.get(Ct),_t===void 0&&(_t=[],S.set(Ct,_t));const Ft=q.textures;if(_t.length!==Ft.length||_t[0]!==r.COLOR_ATTACHMENT0){for(let bt=0,jt=Ft.length;bt<jt;bt++)_t[bt]=r.COLOR_ATTACHMENT0+bt;_t.length=Ft.length,Dt=!0}}else _t[0]!==r.BACK&&(_t[0]=r.BACK,Dt=!0);Dt&&r.drawBuffers(_t)}function He(q){return A!==q?(r.useProgram(q),A=q,!0):!1}const pe={[Vs]:r.FUNC_ADD,[E1]:r.FUNC_SUBTRACT,[b1]:r.FUNC_REVERSE_SUBTRACT};pe[T1]=r.MIN,pe[A1]=r.MAX;const Se={[R1]:r.ZERO,[C1]:r.ONE,[w1]:r.SRC_COLOR,[wd]:r.SRC_ALPHA,[P1]:r.SRC_ALPHA_SATURATE,[L1]:r.DST_COLOR,[U1]:r.DST_ALPHA,[D1]:r.ONE_MINUS_SRC_COLOR,[Dd]:r.ONE_MINUS_SRC_ALPHA,[O1]:r.ONE_MINUS_DST_COLOR,[N1]:r.ONE_MINUS_DST_ALPHA,[z1]:r.CONSTANT_COLOR,[B1]:r.ONE_MINUS_CONSTANT_COLOR,[I1]:r.CONSTANT_ALPHA,[F1]:r.ONE_MINUS_CONSTANT_ALPHA};function Me(q,Ct,_t,Dt,Ft,bt,jt,Xt,an,Ve){if(q===Ta){M===!0&&(Nt(r.BLEND),M=!1);return}if(M===!1&&(Mt(r.BLEND),M=!0),q!==M1){if(q!==y||Ve!==j){if((B!==Vs||I!==Vs)&&(r.blendEquation(r.FUNC_ADD),B=Vs,I=Vs),Ve)switch(q){case Qr:r.blendFuncSeparate(r.ONE,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case Ys:r.blendFunc(r.ONE,r.ONE);break;case k_:r.blendFuncSeparate(r.ZERO,r.ONE_MINUS_SRC_COLOR,r.ZERO,r.ONE);break;case X_:r.blendFuncSeparate(r.DST_COLOR,r.ONE_MINUS_SRC_ALPHA,r.ZERO,r.ONE);break;default:Ue("WebGLState: Invalid blending: ",q);break}else switch(q){case Qr:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case Ys:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE,r.ONE,r.ONE);break;case k_:Ue("WebGLState: SubtractiveBlending requires material.premultipliedAlpha = true");break;case X_:Ue("WebGLState: MultiplyBlending requires material.premultipliedAlpha = true");break;default:Ue("WebGLState: Invalid blending: ",q);break}F=null,w=null,O=null,D=null,T.set(0,0,0),L=0,y=q,j=Ve}return}Ft=Ft||Ct,bt=bt||_t,jt=jt||Dt,(Ct!==B||Ft!==I)&&(r.blendEquationSeparate(pe[Ct],pe[Ft]),B=Ct,I=Ft),(_t!==F||Dt!==w||bt!==O||jt!==D)&&(r.blendFuncSeparate(Se[_t],Se[Dt],Se[bt],Se[jt]),F=_t,w=Dt,O=bt,D=jt),(Xt.equals(T)===!1||an!==L)&&(r.blendColor(Xt.r,Xt.g,Xt.b,an),T.copy(Xt),L=an),y=q,j=!1}function Ut(q,Ct){q.side===Ci?Nt(r.CULL_FACE):Mt(r.CULL_FACE);let _t=q.side===ti;Ct&&(_t=!_t),Zt(_t),q.blending===Qr&&q.transparent===!1?Me(Ta):Me(q.blending,q.blendEquation,q.blendSrc,q.blendDst,q.blendEquationAlpha,q.blendSrcAlpha,q.blendDstAlpha,q.blendColor,q.blendAlpha,q.premultipliedAlpha),f.setFunc(q.depthFunc),f.setTest(q.depthTest),f.setMask(q.depthWrite),c.setMask(q.colorWrite);const Dt=q.stencilWrite;p.setTest(Dt),Dt&&(p.setMask(q.stencilWriteMask),p.setFunc(q.stencilFunc,q.stencilRef,q.stencilFuncMask),p.setOp(q.stencilFail,q.stencilZFail,q.stencilZPass)),Vt(q.polygonOffset,q.polygonOffsetFactor,q.polygonOffsetUnits),q.alphaToCoverage===!0?Mt(r.SAMPLE_ALPHA_TO_COVERAGE):Nt(r.SAMPLE_ALPHA_TO_COVERAGE)}function Zt(q){k!==q&&(q?r.frontFace(r.CW):r.frontFace(r.CCW),k=q)}function $t(q){q!==x1?(Mt(r.CULL_FACE),q!==J&&(q===V_?r.cullFace(r.BACK):q===y1?r.cullFace(r.FRONT):r.cullFace(r.FRONT_AND_BACK))):Nt(r.CULL_FACE),J=q}function oe(q){q!==ut&&(H&&r.lineWidth(q),ut=q)}function Vt(q,Ct,_t){q?(Mt(r.POLYGON_OFFSET_FILL),(gt!==Ct||$!==_t)&&(gt=Ct,$=_t,f.getReversed()&&(Ct=-Ct),r.polygonOffset(Ct,_t))):Nt(r.POLYGON_OFFSET_FILL)}function Ce(q){q?Mt(r.SCISSOR_TEST):Nt(r.SCISSOR_TEST)}function me(q){q===void 0&&(q=r.TEXTURE0+P-1),Et!==q&&(r.activeTexture(q),Et=q)}function Y(q,Ct,_t){_t===void 0&&(Et===null?_t=r.TEXTURE0+P-1:_t=Et);let Dt=N[_t];Dt===void 0&&(Dt={type:void 0,texture:void 0},N[_t]=Dt),(Dt.type!==q||Dt.texture!==Ct)&&(Et!==_t&&(r.activeTexture(_t),Et=_t),r.bindTexture(q,Ct||yt[q]),Dt.type=q,Dt.texture=Ct)}function we(){const q=N[Et];q!==void 0&&q.type!==void 0&&(r.bindTexture(q.type,null),q.type=void 0,q.texture=void 0)}function Le(){try{r.compressedTexImage2D(...arguments)}catch(q){Ue("WebGLState:",q)}}function U(){try{r.compressedTexImage3D(...arguments)}catch(q){Ue("WebGLState:",q)}}function b(){try{r.texSubImage2D(...arguments)}catch(q){Ue("WebGLState:",q)}}function Q(){try{r.texSubImage3D(...arguments)}catch(q){Ue("WebGLState:",q)}}function rt(){try{r.compressedTexSubImage2D(...arguments)}catch(q){Ue("WebGLState:",q)}}function ht(){try{r.compressedTexSubImage3D(...arguments)}catch(q){Ue("WebGLState:",q)}}function Rt(){try{r.texStorage2D(...arguments)}catch(q){Ue("WebGLState:",q)}}function Lt(){try{r.texStorage3D(...arguments)}catch(q){Ue("WebGLState:",q)}}function ft(){try{r.texImage2D(...arguments)}catch(q){Ue("WebGLState:",q)}}function dt(){try{r.texImage3D(...arguments)}catch(q){Ue("WebGLState:",q)}}function wt(q){return v[q]!==void 0?v[q]:r.getParameter(q)}function Ht(q,Ct){v[q]!==Ct&&(r.pixelStorei(q,Ct),v[q]=Ct)}function zt(q){Tt.equals(q)===!1&&(r.scissor(q.x,q.y,q.z,q.w),Tt.copy(q))}function Ot(q){At.equals(q)===!1&&(r.viewport(q.x,q.y,q.z,q.w),At.copy(q))}function te(q,Ct){let _t=d.get(Ct);_t===void 0&&(_t=new WeakMap,d.set(Ct,_t));let Dt=_t.get(q);Dt===void 0&&(Dt=r.getUniformBlockIndex(Ct,q.name),_t.set(q,Dt))}function ee(q,Ct){const Dt=d.get(Ct).get(q);m.get(Ct)!==Dt&&(r.uniformBlockBinding(Ct,Dt,q.__bindingPointIndex),m.set(Ct,Dt))}function le(){r.disable(r.BLEND),r.disable(r.CULL_FACE),r.disable(r.DEPTH_TEST),r.disable(r.POLYGON_OFFSET_FILL),r.disable(r.SCISSOR_TEST),r.disable(r.STENCIL_TEST),r.disable(r.SAMPLE_ALPHA_TO_COVERAGE),r.blendEquation(r.FUNC_ADD),r.blendFunc(r.ONE,r.ZERO),r.blendFuncSeparate(r.ONE,r.ZERO,r.ONE,r.ZERO),r.blendColor(0,0,0,0),r.colorMask(!0,!0,!0,!0),r.clearColor(0,0,0,0),r.depthMask(!0),r.depthFunc(r.LESS),f.setReversed(!1),r.clearDepth(1),r.stencilMask(4294967295),r.stencilFunc(r.ALWAYS,0,4294967295),r.stencilOp(r.KEEP,r.KEEP,r.KEEP),r.clearStencil(0),r.cullFace(r.BACK),r.frontFace(r.CCW),r.polygonOffset(0,0),r.activeTexture(r.TEXTURE0),r.bindFramebuffer(r.FRAMEBUFFER,null),r.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),r.bindFramebuffer(r.READ_FRAMEBUFFER,null),r.useProgram(null),r.lineWidth(1),r.scissor(0,0,r.canvas.width,r.canvas.height),r.viewport(0,0,r.canvas.width,r.canvas.height),r.pixelStorei(r.PACK_ALIGNMENT,4),r.pixelStorei(r.UNPACK_ALIGNMENT,4),r.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,!1),r.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,!1),r.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,r.BROWSER_DEFAULT_WEBGL),r.pixelStorei(r.PACK_ROW_LENGTH,0),r.pixelStorei(r.PACK_SKIP_PIXELS,0),r.pixelStorei(r.PACK_SKIP_ROWS,0),r.pixelStorei(r.UNPACK_ROW_LENGTH,0),r.pixelStorei(r.UNPACK_IMAGE_HEIGHT,0),r.pixelStorei(r.UNPACK_SKIP_PIXELS,0),r.pixelStorei(r.UNPACK_SKIP_ROWS,0),r.pixelStorei(r.UNPACK_SKIP_IMAGES,0),_={},v={},Et=null,N={},g={},S=new WeakMap,E=[],A=null,M=!1,y=null,B=null,F=null,w=null,I=null,O=null,D=null,T=new ue(0,0,0),L=0,j=!1,k=null,J=null,ut=null,gt=null,$=null,Tt.set(0,0,r.canvas.width,r.canvas.height),At.set(0,0,r.canvas.width,r.canvas.height),c.reset(),f.reset(),p.reset()}return{buffers:{color:c,depth:f,stencil:p},enable:Mt,disable:Nt,bindFramebuffer:ae,drawBuffers:Qt,useProgram:He,setBlending:Me,setMaterial:Ut,setFlipSided:Zt,setCullFace:$t,setLineWidth:oe,setPolygonOffset:Vt,setScissorTest:Ce,activeTexture:me,bindTexture:Y,unbindTexture:we,compressedTexImage2D:Le,compressedTexImage3D:U,texImage2D:ft,texImage3D:dt,pixelStorei:Ht,getParameter:wt,updateUBOMapping:te,uniformBlockBinding:ee,texStorage2D:Rt,texStorage3D:Lt,texSubImage2D:b,texSubImage3D:Q,compressedTexSubImage2D:rt,compressedTexSubImage3D:ht,scissor:zt,viewport:Ot,reset:le}}function Y3(r,t,i,s,l,c,f){const p=t.has("WEBGL_multisampled_render_to_texture")?t.get("WEBGL_multisampled_render_to_texture"):null,m=typeof navigator>"u"?!1:/OculusBrowser/g.test(navigator.userAgent),d=new fe,_=new WeakMap,v=new Set;let g;const S=new WeakMap;let E=!1;try{E=typeof OffscreenCanvas<"u"&&new OffscreenCanvas(1,1).getContext("2d")!==null}catch{}function A(U,b){return E?new OffscreenCanvas(U,b):Mu("canvas")}function M(U,b,Q){let rt=1;const ht=Le(U);if((ht.width>Q||ht.height>Q)&&(rt=Q/Math.max(ht.width,ht.height)),rt<1)if(typeof HTMLImageElement<"u"&&U instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&U instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&U instanceof ImageBitmap||typeof VideoFrame<"u"&&U instanceof VideoFrame){const Rt=Math.floor(rt*ht.width),Lt=Math.floor(rt*ht.height);g===void 0&&(g=A(Rt,Lt));const ft=b?A(Rt,Lt):g;return ft.width=Rt,ft.height=Lt,ft.getContext("2d").drawImage(U,0,0,Rt,Lt),re("WebGLRenderer: Texture has been resized from ("+ht.width+"x"+ht.height+") to ("+Rt+"x"+Lt+")."),ft}else return"data"in U&&re("WebGLRenderer: Image in DataTexture is too big ("+ht.width+"x"+ht.height+")."),U;return U}function y(U){return U.generateMipmaps}function B(U){r.generateMipmap(U)}function F(U){return U.isWebGLCubeRenderTarget?r.TEXTURE_CUBE_MAP:U.isWebGL3DRenderTarget?r.TEXTURE_3D:U.isWebGLArrayRenderTarget||U.isCompressedArrayTexture?r.TEXTURE_2D_ARRAY:r.TEXTURE_2D}function w(U,b,Q,rt,ht,Rt=!1){if(U!==null){if(r[U]!==void 0)return r[U];re("WebGLRenderer: Attempt to use non-existing WebGL internal format '"+U+"'")}let Lt;rt&&(Lt=t.get("EXT_texture_norm16"),Lt||re("WebGLRenderer: Unable to use normalized textures without EXT_texture_norm16 extension"));let ft=b;if(b===r.RED&&(Q===r.FLOAT&&(ft=r.R32F),Q===r.HALF_FLOAT&&(ft=r.R16F),Q===r.UNSIGNED_BYTE&&(ft=r.R8),Q===r.UNSIGNED_SHORT&&Lt&&(ft=Lt.R16_EXT),Q===r.SHORT&&Lt&&(ft=Lt.R16_SNORM_EXT)),b===r.RED_INTEGER&&(Q===r.UNSIGNED_BYTE&&(ft=r.R8UI),Q===r.UNSIGNED_SHORT&&(ft=r.R16UI),Q===r.UNSIGNED_INT&&(ft=r.R32UI),Q===r.BYTE&&(ft=r.R8I),Q===r.SHORT&&(ft=r.R16I),Q===r.INT&&(ft=r.R32I)),b===r.RG&&(Q===r.FLOAT&&(ft=r.RG32F),Q===r.HALF_FLOAT&&(ft=r.RG16F),Q===r.UNSIGNED_BYTE&&(ft=r.RG8),Q===r.UNSIGNED_SHORT&&Lt&&(ft=Lt.RG16_EXT),Q===r.SHORT&&Lt&&(ft=Lt.RG16_SNORM_EXT)),b===r.RG_INTEGER&&(Q===r.UNSIGNED_BYTE&&(ft=r.RG8UI),Q===r.UNSIGNED_SHORT&&(ft=r.RG16UI),Q===r.UNSIGNED_INT&&(ft=r.RG32UI),Q===r.BYTE&&(ft=r.RG8I),Q===r.SHORT&&(ft=r.RG16I),Q===r.INT&&(ft=r.RG32I)),b===r.RGB_INTEGER&&(Q===r.UNSIGNED_BYTE&&(ft=r.RGB8UI),Q===r.UNSIGNED_SHORT&&(ft=r.RGB16UI),Q===r.UNSIGNED_INT&&(ft=r.RGB32UI),Q===r.BYTE&&(ft=r.RGB8I),Q===r.SHORT&&(ft=r.RGB16I),Q===r.INT&&(ft=r.RGB32I)),b===r.RGBA_INTEGER&&(Q===r.UNSIGNED_BYTE&&(ft=r.RGBA8UI),Q===r.UNSIGNED_SHORT&&(ft=r.RGBA16UI),Q===r.UNSIGNED_INT&&(ft=r.RGBA32UI),Q===r.BYTE&&(ft=r.RGBA8I),Q===r.SHORT&&(ft=r.RGBA16I),Q===r.INT&&(ft=r.RGBA32I)),b===r.RGB&&(Q===r.UNSIGNED_SHORT&&Lt&&(ft=Lt.RGB16_EXT),Q===r.SHORT&&Lt&&(ft=Lt.RGB16_SNORM_EXT),Q===r.UNSIGNED_INT_5_9_9_9_REV&&(ft=r.RGB9_E5),Q===r.UNSIGNED_INT_10F_11F_11F_REV&&(ft=r.R11F_G11F_B10F)),b===r.RGBA){const dt=Rt?Su:Ne.getTransfer(ht);Q===r.FLOAT&&(ft=r.RGBA32F),Q===r.HALF_FLOAT&&(ft=r.RGBA16F),Q===r.UNSIGNED_BYTE&&(ft=dt===Ze?r.SRGB8_ALPHA8:r.RGBA8),Q===r.UNSIGNED_SHORT&&Lt&&(ft=Lt.RGBA16_EXT),Q===r.SHORT&&Lt&&(ft=Lt.RGBA16_SNORM_EXT),Q===r.UNSIGNED_SHORT_4_4_4_4&&(ft=r.RGBA4),Q===r.UNSIGNED_SHORT_5_5_5_1&&(ft=r.RGB5_A1)}return(ft===r.R16F||ft===r.R32F||ft===r.RG16F||ft===r.RG32F||ft===r.RGBA16F||ft===r.RGBA32F)&&t.get("EXT_color_buffer_float"),ft}function I(U,b){let Q;return U?b===null||b===Ji||b===xl?Q=r.DEPTH24_STENCIL8:b===Ii?Q=r.DEPTH32F_STENCIL8:b===vl&&(Q=r.DEPTH24_STENCIL8,re("DepthTexture: 16 bit depth attachment is not supported with stencil. Using 24-bit attachment.")):b===null||b===Ji||b===xl?Q=r.DEPTH_COMPONENT24:b===Ii?Q=r.DEPTH_COMPONENT32F:b===vl&&(Q=r.DEPTH_COMPONENT16),Q}function O(U,b){return y(U)===!0||U.isFramebufferTexture&&U.minFilter!==zn&&U.minFilter!==Gn?Math.log2(Math.max(b.width,b.height))+1:U.mipmaps!==void 0&&U.mipmaps.length>0?U.mipmaps.length:U.isCompressedTexture&&Array.isArray(U.image)?b.mipmaps.length:1}function D(U){const b=U.target;b.removeEventListener("dispose",D),L(b),b.isVideoTexture&&_.delete(b),b.isHTMLTexture&&v.delete(b)}function T(U){const b=U.target;b.removeEventListener("dispose",T),k(b)}function L(U){const b=s.get(U);if(b.__webglInit===void 0)return;const Q=U.source,rt=S.get(Q);if(rt){const ht=rt[b.__cacheKey];ht.usedTimes--,ht.usedTimes===0&&j(U),Object.keys(rt).length===0&&S.delete(Q)}s.remove(U)}function j(U){const b=s.get(U);r.deleteTexture(b.__webglTexture);const Q=U.source,rt=S.get(Q);delete rt[b.__cacheKey],f.memory.textures--}function k(U){const b=s.get(U);if(U.depthTexture&&(U.depthTexture.dispose(),s.remove(U.depthTexture)),U.isWebGLCubeRenderTarget)for(let rt=0;rt<6;rt++){if(Array.isArray(b.__webglFramebuffer[rt]))for(let ht=0;ht<b.__webglFramebuffer[rt].length;ht++)r.deleteFramebuffer(b.__webglFramebuffer[rt][ht]);else r.deleteFramebuffer(b.__webglFramebuffer[rt]);b.__webglDepthbuffer&&r.deleteRenderbuffer(b.__webglDepthbuffer[rt])}else{if(Array.isArray(b.__webglFramebuffer))for(let rt=0;rt<b.__webglFramebuffer.length;rt++)r.deleteFramebuffer(b.__webglFramebuffer[rt]);else r.deleteFramebuffer(b.__webglFramebuffer);if(b.__webglDepthbuffer&&r.deleteRenderbuffer(b.__webglDepthbuffer),b.__webglMultisampledFramebuffer&&r.deleteFramebuffer(b.__webglMultisampledFramebuffer),b.__webglColorRenderbuffer)for(let rt=0;rt<b.__webglColorRenderbuffer.length;rt++)b.__webglColorRenderbuffer[rt]&&r.deleteRenderbuffer(b.__webglColorRenderbuffer[rt]);b.__webglDepthRenderbuffer&&r.deleteRenderbuffer(b.__webglDepthRenderbuffer)}const Q=U.textures;for(let rt=0,ht=Q.length;rt<ht;rt++){const Rt=s.get(Q[rt]);Rt.__webglTexture&&(r.deleteTexture(Rt.__webglTexture),f.memory.textures--),s.remove(Q[rt])}s.remove(U)}let J=0;function ut(){J=0}function gt(){return J}function $(U){J=U}function P(){const U=J;return U>=l.maxTextures&&re("WebGLTextures: Trying to use "+U+" texture units while this GPU supports only "+l.maxTextures),J+=1,U}function H(U){const b=[];return b.push(U.wrapS),b.push(U.wrapT),b.push(U.wrapR||0),b.push(U.magFilter),b.push(U.minFilter),b.push(U.anisotropy),b.push(U.internalFormat),b.push(U.format),b.push(U.type),b.push(U.generateMipmaps),b.push(U.premultiplyAlpha),b.push(U.flipY),b.push(U.unpackAlignment),b.push(U.colorSpace),b.join()}function et(U,b){const Q=s.get(U);if(U.isVideoTexture&&Y(U),U.isRenderTargetTexture===!1&&U.isExternalTexture!==!0&&U.version>0&&Q.__version!==U.version){const rt=U.image;if(rt===null)re("WebGLRenderer: Texture marked for update but no image data found.");else if(rt.complete===!1)re("WebGLRenderer: Texture marked for update but image is incomplete");else{Nt(Q,U,b);return}}else U.isExternalTexture&&(Q.__webglTexture=U.sourceTexture?U.sourceTexture:null);i.bindTexture(r.TEXTURE_2D,Q.__webglTexture,r.TEXTURE0+b)}function pt(U,b){const Q=s.get(U);if(U.isRenderTargetTexture===!1&&U.version>0&&Q.__version!==U.version){Nt(Q,U,b);return}else U.isExternalTexture&&(Q.__webglTexture=U.sourceTexture?U.sourceTexture:null);i.bindTexture(r.TEXTURE_2D_ARRAY,Q.__webglTexture,r.TEXTURE0+b)}function Et(U,b){const Q=s.get(U);if(U.isRenderTargetTexture===!1&&U.version>0&&Q.__version!==U.version){Nt(Q,U,b);return}i.bindTexture(r.TEXTURE_3D,Q.__webglTexture,r.TEXTURE0+b)}function N(U,b){const Q=s.get(U);if(U.isCubeDepthTexture!==!0&&U.version>0&&Q.__version!==U.version){ae(Q,U,b);return}i.bindTexture(r.TEXTURE_CUBE_MAP,Q.__webglTexture,r.TEXTURE0+b)}const W={[Id]:r.REPEAT,[ba]:r.CLAMP_TO_EDGE,[Fd]:r.MIRRORED_REPEAT},vt={[zn]:r.NEAREST,[V1]:r.NEAREST_MIPMAP_NEAREST,[Lc]:r.NEAREST_MIPMAP_LINEAR,[Gn]:r.LINEAR,[qh]:r.LINEAR_MIPMAP_NEAREST,[Xs]:r.LINEAR_MIPMAP_LINEAR},Tt={[W1]:r.NEVER,[K1]:r.ALWAYS,[q1]:r.LESS,[Pp]:r.LEQUAL,[Y1]:r.EQUAL,[zp]:r.GEQUAL,[Z1]:r.GREATER,[j1]:r.NOTEQUAL};function At(U,b){if(b.type===Ii&&t.has("OES_texture_float_linear")===!1&&(b.magFilter===Gn||b.magFilter===qh||b.magFilter===Lc||b.magFilter===Xs||b.minFilter===Gn||b.minFilter===qh||b.minFilter===Lc||b.minFilter===Xs)&&re("WebGLRenderer: Unable to use linear filtering with floating point textures. OES_texture_float_linear not supported on this device."),r.texParameteri(U,r.TEXTURE_WRAP_S,W[b.wrapS]),r.texParameteri(U,r.TEXTURE_WRAP_T,W[b.wrapT]),(U===r.TEXTURE_3D||U===r.TEXTURE_2D_ARRAY)&&r.texParameteri(U,r.TEXTURE_WRAP_R,W[b.wrapR]),r.texParameteri(U,r.TEXTURE_MAG_FILTER,vt[b.magFilter]),r.texParameteri(U,r.TEXTURE_MIN_FILTER,vt[b.minFilter]),b.compareFunction&&(r.texParameteri(U,r.TEXTURE_COMPARE_MODE,r.COMPARE_REF_TO_TEXTURE),r.texParameteri(U,r.TEXTURE_COMPARE_FUNC,Tt[b.compareFunction])),t.has("EXT_texture_filter_anisotropic")===!0){if(b.magFilter===zn||b.minFilter!==Lc&&b.minFilter!==Xs||b.type===Ii&&t.has("OES_texture_float_linear")===!1)return;if(b.anisotropy>1||s.get(b).__currentAnisotropy){const Q=t.get("EXT_texture_filter_anisotropic");r.texParameterf(U,Q.TEXTURE_MAX_ANISOTROPY_EXT,Math.min(b.anisotropy,l.getMaxAnisotropy())),s.get(b).__currentAnisotropy=b.anisotropy}}}function tt(U,b){let Q=!1;U.__webglInit===void 0&&(U.__webglInit=!0,b.addEventListener("dispose",D));const rt=b.source;let ht=S.get(rt);ht===void 0&&(ht={},S.set(rt,ht));const Rt=H(b);if(Rt!==U.__cacheKey){ht[Rt]===void 0&&(ht[Rt]={texture:r.createTexture(),usedTimes:0},f.memory.textures++,Q=!0),ht[Rt].usedTimes++;const Lt=ht[U.__cacheKey];Lt!==void 0&&(ht[U.__cacheKey].usedTimes--,Lt.usedTimes===0&&j(b)),U.__cacheKey=Rt,U.__webglTexture=ht[Rt].texture}return Q}function yt(U,b,Q){return Math.floor(Math.floor(U/Q)/b)}function Mt(U,b,Q,rt){const Rt=U.updateRanges;if(Rt.length===0)i.texSubImage2D(r.TEXTURE_2D,0,0,0,b.width,b.height,Q,rt,b.data);else{Rt.sort((Ht,zt)=>Ht.start-zt.start);let Lt=0;for(let Ht=1;Ht<Rt.length;Ht++){const zt=Rt[Lt],Ot=Rt[Ht],te=zt.start+zt.count,ee=yt(Ot.start,b.width,4),le=yt(zt.start,b.width,4);Ot.start<=te+1&&ee===le&&yt(Ot.start+Ot.count-1,b.width,4)===ee?zt.count=Math.max(zt.count,Ot.start+Ot.count-zt.start):(++Lt,Rt[Lt]=Ot)}Rt.length=Lt+1;const ft=i.getParameter(r.UNPACK_ROW_LENGTH),dt=i.getParameter(r.UNPACK_SKIP_PIXELS),wt=i.getParameter(r.UNPACK_SKIP_ROWS);i.pixelStorei(r.UNPACK_ROW_LENGTH,b.width);for(let Ht=0,zt=Rt.length;Ht<zt;Ht++){const Ot=Rt[Ht],te=Math.floor(Ot.start/4),ee=Math.ceil(Ot.count/4),le=te%b.width,q=Math.floor(te/b.width),Ct=ee,_t=1;i.pixelStorei(r.UNPACK_SKIP_PIXELS,le),i.pixelStorei(r.UNPACK_SKIP_ROWS,q),i.texSubImage2D(r.TEXTURE_2D,0,le,q,Ct,_t,Q,rt,b.data)}U.clearUpdateRanges(),i.pixelStorei(r.UNPACK_ROW_LENGTH,ft),i.pixelStorei(r.UNPACK_SKIP_PIXELS,dt),i.pixelStorei(r.UNPACK_SKIP_ROWS,wt)}}function Nt(U,b,Q){let rt=r.TEXTURE_2D;(b.isDataArrayTexture||b.isCompressedArrayTexture)&&(rt=r.TEXTURE_2D_ARRAY),b.isData3DTexture&&(rt=r.TEXTURE_3D);const ht=tt(U,b),Rt=b.source;i.bindTexture(rt,U.__webglTexture,r.TEXTURE0+Q);const Lt=s.get(Rt);if(Rt.version!==Lt.__version||ht===!0){if(i.activeTexture(r.TEXTURE0+Q),(typeof ImageBitmap<"u"&&b.image instanceof ImageBitmap)===!1){const _t=Ne.getPrimaries(Ne.workingColorSpace),Dt=b.colorSpace===cs?null:Ne.getPrimaries(b.colorSpace),Ft=b.colorSpace===cs||_t===Dt?r.NONE:r.BROWSER_DEFAULT_WEBGL;i.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,b.flipY),i.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,b.premultiplyAlpha),i.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,Ft)}i.pixelStorei(r.UNPACK_ALIGNMENT,b.unpackAlignment);let dt=M(b.image,!1,l.maxTextureSize);dt=we(b,dt);const wt=c.convert(b.format,b.colorSpace),Ht=c.convert(b.type);let zt=w(b.internalFormat,wt,Ht,b.normalized,b.colorSpace,b.isVideoTexture);At(rt,b);let Ot;const te=b.mipmaps,ee=b.isVideoTexture!==!0,le=Lt.__version===void 0||ht===!0,q=Rt.dataReady,Ct=O(b,dt);if(b.isDepthTexture)zt=I(b.format===Ws,b.type),le&&(ee?i.texStorage2D(r.TEXTURE_2D,1,zt,dt.width,dt.height):i.texImage2D(r.TEXTURE_2D,0,zt,dt.width,dt.height,0,wt,Ht,null));else if(b.isDataTexture)if(te.length>0){ee&&le&&i.texStorage2D(r.TEXTURE_2D,Ct,zt,te[0].width,te[0].height);for(let _t=0,Dt=te.length;_t<Dt;_t++)Ot=te[_t],ee?q&&i.texSubImage2D(r.TEXTURE_2D,_t,0,0,Ot.width,Ot.height,wt,Ht,Ot.data):i.texImage2D(r.TEXTURE_2D,_t,zt,Ot.width,Ot.height,0,wt,Ht,Ot.data);b.generateMipmaps=!1}else ee?(le&&i.texStorage2D(r.TEXTURE_2D,Ct,zt,dt.width,dt.height),q&&Mt(b,dt,wt,Ht)):i.texImage2D(r.TEXTURE_2D,0,zt,dt.width,dt.height,0,wt,Ht,dt.data);else if(b.isCompressedTexture)if(b.isCompressedArrayTexture){ee&&le&&i.texStorage3D(r.TEXTURE_2D_ARRAY,Ct,zt,te[0].width,te[0].height,dt.depth);for(let _t=0,Dt=te.length;_t<Dt;_t++)if(Ot=te[_t],b.format!==Fi)if(wt!==null)if(ee){if(q)if(b.layerUpdates.size>0){const Ft=Dv(Ot.width,Ot.height,b.format,b.type);for(const bt of b.layerUpdates){const jt=Ot.data.subarray(bt*Ft/Ot.data.BYTES_PER_ELEMENT,(bt+1)*Ft/Ot.data.BYTES_PER_ELEMENT);i.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,_t,0,0,bt,Ot.width,Ot.height,1,wt,jt)}b.clearLayerUpdates()}else i.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,_t,0,0,0,Ot.width,Ot.height,dt.depth,wt,Ot.data)}else i.compressedTexImage3D(r.TEXTURE_2D_ARRAY,_t,zt,Ot.width,Ot.height,dt.depth,0,Ot.data,0,0);else re("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()");else ee?q&&i.texSubImage3D(r.TEXTURE_2D_ARRAY,_t,0,0,0,Ot.width,Ot.height,dt.depth,wt,Ht,Ot.data):i.texImage3D(r.TEXTURE_2D_ARRAY,_t,zt,Ot.width,Ot.height,dt.depth,0,wt,Ht,Ot.data)}else{ee&&le&&i.texStorage2D(r.TEXTURE_2D,Ct,zt,te[0].width,te[0].height);for(let _t=0,Dt=te.length;_t<Dt;_t++)Ot=te[_t],b.format!==Fi?wt!==null?ee?q&&i.compressedTexSubImage2D(r.TEXTURE_2D,_t,0,0,Ot.width,Ot.height,wt,Ot.data):i.compressedTexImage2D(r.TEXTURE_2D,_t,zt,Ot.width,Ot.height,0,Ot.data):re("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()"):ee?q&&i.texSubImage2D(r.TEXTURE_2D,_t,0,0,Ot.width,Ot.height,wt,Ht,Ot.data):i.texImage2D(r.TEXTURE_2D,_t,zt,Ot.width,Ot.height,0,wt,Ht,Ot.data)}else if(b.isDataArrayTexture)if(ee){if(le&&i.texStorage3D(r.TEXTURE_2D_ARRAY,Ct,zt,dt.width,dt.height,dt.depth),q)if(b.layerUpdates.size>0){const _t=Dv(dt.width,dt.height,b.format,b.type);for(const Dt of b.layerUpdates){const Ft=dt.data.subarray(Dt*_t/dt.data.BYTES_PER_ELEMENT,(Dt+1)*_t/dt.data.BYTES_PER_ELEMENT);i.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,Dt,dt.width,dt.height,1,wt,Ht,Ft)}b.clearLayerUpdates()}else i.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,0,dt.width,dt.height,dt.depth,wt,Ht,dt.data)}else i.texImage3D(r.TEXTURE_2D_ARRAY,0,zt,dt.width,dt.height,dt.depth,0,wt,Ht,dt.data);else if(b.isData3DTexture)ee?(le&&i.texStorage3D(r.TEXTURE_3D,Ct,zt,dt.width,dt.height,dt.depth),q&&i.texSubImage3D(r.TEXTURE_3D,0,0,0,0,dt.width,dt.height,dt.depth,wt,Ht,dt.data)):i.texImage3D(r.TEXTURE_3D,0,zt,dt.width,dt.height,dt.depth,0,wt,Ht,dt.data);else if(b.isFramebufferTexture){if(le)if(ee)i.texStorage2D(r.TEXTURE_2D,Ct,zt,dt.width,dt.height);else{let _t=dt.width,Dt=dt.height;for(let Ft=0;Ft<Ct;Ft++)i.texImage2D(r.TEXTURE_2D,Ft,zt,_t,Dt,0,wt,Ht,null),_t>>=1,Dt>>=1}}else if(b.isHTMLTexture){if("texElementImage2D"in r){const _t=r.canvas;if(_t.hasAttribute("layoutsubtree")||_t.setAttribute("layoutsubtree","true"),dt.parentNode!==_t){_t.appendChild(dt),v.add(b),_t.onpaint=Dt=>{const Ft=Dt.changedElements;for(const bt of v)Ft.includes(bt.image)&&(bt.needsUpdate=!0)},_t.requestPaint();return}if(r.texElementImage2D.length===3)r.texElementImage2D(r.TEXTURE_2D,r.RGBA8,dt);else{const Ft=r.RGBA,bt=r.RGBA,jt=r.UNSIGNED_BYTE;r.texElementImage2D(r.TEXTURE_2D,0,Ft,bt,jt,dt)}r.texParameteri(r.TEXTURE_2D,r.TEXTURE_MIN_FILTER,r.LINEAR),r.texParameteri(r.TEXTURE_2D,r.TEXTURE_WRAP_S,r.CLAMP_TO_EDGE),r.texParameteri(r.TEXTURE_2D,r.TEXTURE_WRAP_T,r.CLAMP_TO_EDGE)}}else if(te.length>0){if(ee&&le){const _t=Le(te[0]);i.texStorage2D(r.TEXTURE_2D,Ct,zt,_t.width,_t.height)}for(let _t=0,Dt=te.length;_t<Dt;_t++)Ot=te[_t],ee?q&&i.texSubImage2D(r.TEXTURE_2D,_t,0,0,wt,Ht,Ot):i.texImage2D(r.TEXTURE_2D,_t,zt,wt,Ht,Ot);b.generateMipmaps=!1}else if(ee){if(le){const _t=Le(dt);i.texStorage2D(r.TEXTURE_2D,Ct,zt,_t.width,_t.height)}q&&i.texSubImage2D(r.TEXTURE_2D,0,0,0,wt,Ht,dt)}else i.texImage2D(r.TEXTURE_2D,0,zt,wt,Ht,dt);y(b)&&B(rt),Lt.__version=Rt.version,b.onUpdate&&b.onUpdate(b)}U.__version=b.version}function ae(U,b,Q){if(b.image.length!==6)return;const rt=tt(U,b),ht=b.source;i.bindTexture(r.TEXTURE_CUBE_MAP,U.__webglTexture,r.TEXTURE0+Q);const Rt=s.get(ht);if(ht.version!==Rt.__version||rt===!0){i.activeTexture(r.TEXTURE0+Q);const Lt=Ne.getPrimaries(Ne.workingColorSpace),ft=b.colorSpace===cs?null:Ne.getPrimaries(b.colorSpace),dt=b.colorSpace===cs||Lt===ft?r.NONE:r.BROWSER_DEFAULT_WEBGL;i.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,b.flipY),i.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,b.premultiplyAlpha),i.pixelStorei(r.UNPACK_ALIGNMENT,b.unpackAlignment),i.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,dt);const wt=b.isCompressedTexture||b.image[0].isCompressedTexture,Ht=b.image[0]&&b.image[0].isDataTexture,zt=[];for(let bt=0;bt<6;bt++)!wt&&!Ht?zt[bt]=M(b.image[bt],!0,l.maxCubemapSize):zt[bt]=Ht?b.image[bt].image:b.image[bt],zt[bt]=we(b,zt[bt]);const Ot=zt[0],te=c.convert(b.format,b.colorSpace),ee=c.convert(b.type),le=w(b.internalFormat,te,ee,b.normalized,b.colorSpace),q=b.isVideoTexture!==!0,Ct=Rt.__version===void 0||rt===!0,_t=ht.dataReady;let Dt=O(b,Ot);At(r.TEXTURE_CUBE_MAP,b);let Ft;if(wt){q&&Ct&&i.texStorage2D(r.TEXTURE_CUBE_MAP,Dt,le,Ot.width,Ot.height);for(let bt=0;bt<6;bt++){Ft=zt[bt].mipmaps;for(let jt=0;jt<Ft.length;jt++){const Xt=Ft[jt];b.format!==Fi?te!==null?q?_t&&i.compressedTexSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt,0,0,Xt.width,Xt.height,te,Xt.data):i.compressedTexImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt,le,Xt.width,Xt.height,0,Xt.data):re("WebGLRenderer: Attempt to load unsupported compressed texture format in .setTextureCube()"):q?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt,0,0,Xt.width,Xt.height,te,ee,Xt.data):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt,le,Xt.width,Xt.height,0,te,ee,Xt.data)}}}else{if(Ft=b.mipmaps,q&&Ct){Ft.length>0&&Dt++;const bt=Le(zt[0]);i.texStorage2D(r.TEXTURE_CUBE_MAP,Dt,le,bt.width,bt.height)}for(let bt=0;bt<6;bt++)if(Ht){q?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,0,0,0,zt[bt].width,zt[bt].height,te,ee,zt[bt].data):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,0,le,zt[bt].width,zt[bt].height,0,te,ee,zt[bt].data);for(let jt=0;jt<Ft.length;jt++){const an=Ft[jt].image[bt].image;q?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt+1,0,0,an.width,an.height,te,ee,an.data):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt+1,le,an.width,an.height,0,te,ee,an.data)}}else{q?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,0,0,0,te,ee,zt[bt]):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,0,le,te,ee,zt[bt]);for(let jt=0;jt<Ft.length;jt++){const Xt=Ft[jt];q?_t&&i.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt+1,0,0,te,ee,Xt.image[bt]):i.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+bt,jt+1,le,te,ee,Xt.image[bt])}}}y(b)&&B(r.TEXTURE_CUBE_MAP),Rt.__version=ht.version,b.onUpdate&&b.onUpdate(b)}U.__version=b.version}function Qt(U,b,Q,rt,ht,Rt){const Lt=c.convert(Q.format,Q.colorSpace),ft=c.convert(Q.type),dt=w(Q.internalFormat,Lt,ft,Q.normalized,Q.colorSpace),wt=s.get(b),Ht=s.get(Q);if(Ht.__renderTarget=b,!wt.__hasExternalTextures){const zt=Math.max(1,b.width>>Rt),Ot=Math.max(1,b.height>>Rt);ht===r.TEXTURE_3D||ht===r.TEXTURE_2D_ARRAY?i.texImage3D(ht,Rt,dt,zt,Ot,b.depth,0,Lt,ft,null):i.texImage2D(ht,Rt,dt,zt,Ot,0,Lt,ft,null)}i.bindFramebuffer(r.FRAMEBUFFER,U),me(b)?p.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,rt,ht,Ht.__webglTexture,0,Ce(b)):(ht===r.TEXTURE_2D||ht>=r.TEXTURE_CUBE_MAP_POSITIVE_X&&ht<=r.TEXTURE_CUBE_MAP_NEGATIVE_Z)&&r.framebufferTexture2D(r.FRAMEBUFFER,rt,ht,Ht.__webglTexture,Rt),i.bindFramebuffer(r.FRAMEBUFFER,null)}function He(U,b,Q){if(r.bindRenderbuffer(r.RENDERBUFFER,U),b.depthBuffer){const rt=b.depthTexture,ht=rt&&rt.isDepthTexture?rt.type:null,Rt=I(b.stencilBuffer,ht),Lt=b.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;me(b)?p.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,Ce(b),Rt,b.width,b.height):Q?r.renderbufferStorageMultisample(r.RENDERBUFFER,Ce(b),Rt,b.width,b.height):r.renderbufferStorage(r.RENDERBUFFER,Rt,b.width,b.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,Lt,r.RENDERBUFFER,U)}else{const rt=b.textures;for(let ht=0;ht<rt.length;ht++){const Rt=rt[ht],Lt=c.convert(Rt.format,Rt.colorSpace),ft=c.convert(Rt.type),dt=w(Rt.internalFormat,Lt,ft,Rt.normalized,Rt.colorSpace);me(b)?p.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,Ce(b),dt,b.width,b.height):Q?r.renderbufferStorageMultisample(r.RENDERBUFFER,Ce(b),dt,b.width,b.height):r.renderbufferStorage(r.RENDERBUFFER,dt,b.width,b.height)}}r.bindRenderbuffer(r.RENDERBUFFER,null)}function pe(U,b,Q){const rt=b.isWebGLCubeRenderTarget===!0;if(i.bindFramebuffer(r.FRAMEBUFFER,U),!(b.depthTexture&&b.depthTexture.isDepthTexture))throw new Error("THREE.WebGLTextures: renderTarget.depthTexture must be an instance of THREE.DepthTexture.");const ht=s.get(b.depthTexture);if(ht.__renderTarget=b,(!ht.__webglTexture||b.depthTexture.image.width!==b.width||b.depthTexture.image.height!==b.height)&&(b.depthTexture.image.width=b.width,b.depthTexture.image.height=b.height,b.depthTexture.needsUpdate=!0),rt){if(ht.__webglInit===void 0&&(ht.__webglInit=!0,b.depthTexture.addEventListener("dispose",D)),ht.__webglTexture===void 0){ht.__webglTexture=r.createTexture(),i.bindTexture(r.TEXTURE_CUBE_MAP,ht.__webglTexture),At(r.TEXTURE_CUBE_MAP,b.depthTexture);const wt=c.convert(b.depthTexture.format),Ht=c.convert(b.depthTexture.type);let zt;b.depthTexture.format===wa?zt=r.DEPTH_COMPONENT24:b.depthTexture.format===Ws&&(zt=r.DEPTH24_STENCIL8);for(let Ot=0;Ot<6;Ot++)r.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Ot,0,zt,b.width,b.height,0,wt,Ht,null)}}else et(b.depthTexture,0);const Rt=ht.__webglTexture,Lt=Ce(b),ft=rt?r.TEXTURE_CUBE_MAP_POSITIVE_X+Q:r.TEXTURE_2D,dt=b.depthTexture.format===Ws?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;if(b.depthTexture.format===wa)me(b)?p.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,dt,ft,Rt,0,Lt):r.framebufferTexture2D(r.FRAMEBUFFER,dt,ft,Rt,0);else if(b.depthTexture.format===Ws)me(b)?p.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,dt,ft,Rt,0,Lt):r.framebufferTexture2D(r.FRAMEBUFFER,dt,ft,Rt,0);else throw new Error("THREE.WebGLTextures: Unknown depthTexture format.")}function Se(U){const b=s.get(U),Q=U.isWebGLCubeRenderTarget===!0;if(b.__boundDepthTexture!==U.depthTexture){const rt=U.depthTexture;if(b.__depthDisposeCallback&&b.__depthDisposeCallback(),rt){const ht=()=>{delete b.__boundDepthTexture,delete b.__depthDisposeCallback,rt.removeEventListener("dispose",ht)};rt.addEventListener("dispose",ht),b.__depthDisposeCallback=ht}b.__boundDepthTexture=rt}if(U.depthTexture&&!b.__autoAllocateDepthBuffer)if(Q)for(let rt=0;rt<6;rt++)pe(b.__webglFramebuffer[rt],U,rt);else{const rt=U.texture.mipmaps;rt&&rt.length>0?pe(b.__webglFramebuffer[0],U,0):pe(b.__webglFramebuffer,U,0)}else if(Q){b.__webglDepthbuffer=[];for(let rt=0;rt<6;rt++)if(i.bindFramebuffer(r.FRAMEBUFFER,b.__webglFramebuffer[rt]),b.__webglDepthbuffer[rt]===void 0)b.__webglDepthbuffer[rt]=r.createRenderbuffer(),He(b.__webglDepthbuffer[rt],U,!1);else{const ht=U.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,Rt=b.__webglDepthbuffer[rt];r.bindRenderbuffer(r.RENDERBUFFER,Rt),r.framebufferRenderbuffer(r.FRAMEBUFFER,ht,r.RENDERBUFFER,Rt)}}else{const rt=U.texture.mipmaps;if(rt&&rt.length>0?i.bindFramebuffer(r.FRAMEBUFFER,b.__webglFramebuffer[0]):i.bindFramebuffer(r.FRAMEBUFFER,b.__webglFramebuffer),b.__webglDepthbuffer===void 0)b.__webglDepthbuffer=r.createRenderbuffer(),He(b.__webglDepthbuffer,U,!1);else{const ht=U.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,Rt=b.__webglDepthbuffer;r.bindRenderbuffer(r.RENDERBUFFER,Rt),r.framebufferRenderbuffer(r.FRAMEBUFFER,ht,r.RENDERBUFFER,Rt)}}i.bindFramebuffer(r.FRAMEBUFFER,null)}function Me(U,b,Q){const rt=s.get(U);b!==void 0&&Qt(rt.__webglFramebuffer,U,U.texture,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,0),Q!==void 0&&Se(U)}function Ut(U){const b=U.texture,Q=s.get(U),rt=s.get(b);U.addEventListener("dispose",T);const ht=U.textures,Rt=U.isWebGLCubeRenderTarget===!0,Lt=ht.length>1;if(Lt||(rt.__webglTexture===void 0&&(rt.__webglTexture=r.createTexture()),rt.__version=b.version,f.memory.textures++),Rt){Q.__webglFramebuffer=[];for(let ft=0;ft<6;ft++)if(b.mipmaps&&b.mipmaps.length>0){Q.__webglFramebuffer[ft]=[];for(let dt=0;dt<b.mipmaps.length;dt++)Q.__webglFramebuffer[ft][dt]=r.createFramebuffer()}else Q.__webglFramebuffer[ft]=r.createFramebuffer()}else{if(b.mipmaps&&b.mipmaps.length>0){Q.__webglFramebuffer=[];for(let ft=0;ft<b.mipmaps.length;ft++)Q.__webglFramebuffer[ft]=r.createFramebuffer()}else Q.__webglFramebuffer=r.createFramebuffer();if(Lt)for(let ft=0,dt=ht.length;ft<dt;ft++){const wt=s.get(ht[ft]);wt.__webglTexture===void 0&&(wt.__webglTexture=r.createTexture(),f.memory.textures++)}if(U.samples>0&&me(U)===!1){Q.__webglMultisampledFramebuffer=r.createFramebuffer(),Q.__webglColorRenderbuffer=[],i.bindFramebuffer(r.FRAMEBUFFER,Q.__webglMultisampledFramebuffer);for(let ft=0;ft<ht.length;ft++){const dt=ht[ft];Q.__webglColorRenderbuffer[ft]=r.createRenderbuffer(),r.bindRenderbuffer(r.RENDERBUFFER,Q.__webglColorRenderbuffer[ft]);const wt=c.convert(dt.format,dt.colorSpace),Ht=c.convert(dt.type),zt=w(dt.internalFormat,wt,Ht,dt.normalized,dt.colorSpace,U.isXRRenderTarget===!0),Ot=Ce(U);r.renderbufferStorageMultisample(r.RENDERBUFFER,Ot,zt,U.width,U.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+ft,r.RENDERBUFFER,Q.__webglColorRenderbuffer[ft])}r.bindRenderbuffer(r.RENDERBUFFER,null),U.depthBuffer&&(Q.__webglDepthRenderbuffer=r.createRenderbuffer(),He(Q.__webglDepthRenderbuffer,U,!0)),i.bindFramebuffer(r.FRAMEBUFFER,null)}}if(Rt){i.bindTexture(r.TEXTURE_CUBE_MAP,rt.__webglTexture),At(r.TEXTURE_CUBE_MAP,b);for(let ft=0;ft<6;ft++)if(b.mipmaps&&b.mipmaps.length>0)for(let dt=0;dt<b.mipmaps.length;dt++)Qt(Q.__webglFramebuffer[ft][dt],U,b,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+ft,dt);else Qt(Q.__webglFramebuffer[ft],U,b,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+ft,0);y(b)&&B(r.TEXTURE_CUBE_MAP),i.unbindTexture()}else if(Lt){for(let ft=0,dt=ht.length;ft<dt;ft++){const wt=ht[ft],Ht=s.get(wt);let zt=r.TEXTURE_2D;(U.isWebGL3DRenderTarget||U.isWebGLArrayRenderTarget)&&(zt=U.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),i.bindTexture(zt,Ht.__webglTexture),At(zt,wt),Qt(Q.__webglFramebuffer,U,wt,r.COLOR_ATTACHMENT0+ft,zt,0),y(wt)&&B(zt)}i.unbindTexture()}else{let ft=r.TEXTURE_2D;if((U.isWebGL3DRenderTarget||U.isWebGLArrayRenderTarget)&&(ft=U.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),i.bindTexture(ft,rt.__webglTexture),At(ft,b),b.mipmaps&&b.mipmaps.length>0)for(let dt=0;dt<b.mipmaps.length;dt++)Qt(Q.__webglFramebuffer[dt],U,b,r.COLOR_ATTACHMENT0,ft,dt);else Qt(Q.__webglFramebuffer,U,b,r.COLOR_ATTACHMENT0,ft,0);y(b)&&B(ft),i.unbindTexture()}U.depthBuffer&&Se(U)}function Zt(U){const b=U.textures;for(let Q=0,rt=b.length;Q<rt;Q++){const ht=b[Q];if(y(ht)){const Rt=F(U),Lt=s.get(ht).__webglTexture;i.bindTexture(Rt,Lt),B(Rt),i.unbindTexture()}}}const $t=[],oe=[];function Vt(U){if(U.samples>0){if(me(U)===!1){const b=U.textures,Q=U.width,rt=U.height;let ht=r.COLOR_BUFFER_BIT;const Rt=U.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,Lt=s.get(U),ft=b.length>1;if(ft)for(let wt=0;wt<b.length;wt++)i.bindFramebuffer(r.FRAMEBUFFER,Lt.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+wt,r.RENDERBUFFER,null),i.bindFramebuffer(r.FRAMEBUFFER,Lt.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+wt,r.TEXTURE_2D,null,0);i.bindFramebuffer(r.READ_FRAMEBUFFER,Lt.__webglMultisampledFramebuffer);const dt=U.texture.mipmaps;dt&&dt.length>0?i.bindFramebuffer(r.DRAW_FRAMEBUFFER,Lt.__webglFramebuffer[0]):i.bindFramebuffer(r.DRAW_FRAMEBUFFER,Lt.__webglFramebuffer);for(let wt=0;wt<b.length;wt++){if(U.resolveDepthBuffer&&(U.depthBuffer&&(ht|=r.DEPTH_BUFFER_BIT),U.stencilBuffer&&U.resolveStencilBuffer&&(ht|=r.STENCIL_BUFFER_BIT)),ft){r.framebufferRenderbuffer(r.READ_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.RENDERBUFFER,Lt.__webglColorRenderbuffer[wt]);const Ht=s.get(b[wt]).__webglTexture;r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,Ht,0)}r.blitFramebuffer(0,0,Q,rt,0,0,Q,rt,ht,r.NEAREST),m===!0&&($t.length=0,oe.length=0,$t.push(r.COLOR_ATTACHMENT0+wt),U.depthBuffer&&U.resolveDepthBuffer===!1&&($t.push(Rt),oe.push(Rt),r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,oe)),r.invalidateFramebuffer(r.READ_FRAMEBUFFER,$t))}if(i.bindFramebuffer(r.READ_FRAMEBUFFER,null),i.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),ft)for(let wt=0;wt<b.length;wt++){i.bindFramebuffer(r.FRAMEBUFFER,Lt.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+wt,r.RENDERBUFFER,Lt.__webglColorRenderbuffer[wt]);const Ht=s.get(b[wt]).__webglTexture;i.bindFramebuffer(r.FRAMEBUFFER,Lt.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+wt,r.TEXTURE_2D,Ht,0)}i.bindFramebuffer(r.DRAW_FRAMEBUFFER,Lt.__webglMultisampledFramebuffer)}else if(U.depthBuffer&&U.resolveDepthBuffer===!1&&m){const b=U.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,[b])}}}function Ce(U){return Math.min(l.maxSamples,U.samples)}function me(U){const b=s.get(U);return U.samples>0&&t.has("WEBGL_multisampled_render_to_texture")===!0&&b.__useRenderToTexture!==!1}function Y(U){const b=f.render.frame;_.get(U)!==b&&(_.set(U,b),U.update())}function we(U,b){const Q=U.colorSpace,rt=U.format,ht=U.type;return U.isCompressedTexture===!0||U.isVideoTexture===!0||Q!==yu&&Q!==cs&&(Ne.getTransfer(Q)===Ze?(rt!==Fi||ht!==gi)&&re("WebGLTextures: sRGB encoded textures have to use RGBAFormat and UnsignedByteType."):Ue("WebGLTextures: Unsupported texture color space:",Q)),b}function Le(U){return typeof HTMLImageElement<"u"&&U instanceof HTMLImageElement?(d.width=U.naturalWidth||U.width,d.height=U.naturalHeight||U.height):typeof VideoFrame<"u"&&U instanceof VideoFrame?(d.width=U.displayWidth,d.height=U.displayHeight):(d.width=U.width,d.height=U.height),d}this.allocateTextureUnit=P,this.resetTextureUnits=ut,this.getTextureUnits=gt,this.setTextureUnits=$,this.setTexture2D=et,this.setTexture2DArray=pt,this.setTexture3D=Et,this.setTextureCube=N,this.rebindTextures=Me,this.setupRenderTarget=Ut,this.updateRenderTargetMipmap=Zt,this.updateMultisampleRenderTarget=Vt,this.setupDepthRenderbuffer=Se,this.setupFrameBufferTexture=Qt,this.useMultisampledRTT=me,this.isReversedDepthBuffer=function(){return i.buffers.depth.getReversed()}}function Z3(r,t){function i(s,l=cs){let c;const f=Ne.getTransfer(l);if(s===gi)return r.UNSIGNED_BYTE;if(s===wp)return r.UNSIGNED_SHORT_4_4_4_4;if(s===Dp)return r.UNSIGNED_SHORT_5_5_5_1;if(s===gx)return r.UNSIGNED_INT_5_9_9_9_REV;if(s===_x)return r.UNSIGNED_INT_10F_11F_11F_REV;if(s===px)return r.BYTE;if(s===mx)return r.SHORT;if(s===vl)return r.UNSIGNED_SHORT;if(s===Cp)return r.INT;if(s===Ji)return r.UNSIGNED_INT;if(s===Ii)return r.FLOAT;if(s===Ca)return r.HALF_FLOAT;if(s===vx)return r.ALPHA;if(s===xx)return r.RGB;if(s===Fi)return r.RGBA;if(s===wa)return r.DEPTH_COMPONENT;if(s===Ws)return r.DEPTH_STENCIL;if(s===Up)return r.RED;if(s===Np)return r.RED_INTEGER;if(s===js)return r.RG;if(s===Lp)return r.RG_INTEGER;if(s===Op)return r.RGBA_INTEGER;if(s===hu||s===du||s===pu||s===mu)if(f===Ze)if(c=t.get("WEBGL_compressed_texture_s3tc_srgb"),c!==null){if(s===hu)return c.COMPRESSED_SRGB_S3TC_DXT1_EXT;if(s===du)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;if(s===pu)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;if(s===mu)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT}else return null;else if(c=t.get("WEBGL_compressed_texture_s3tc"),c!==null){if(s===hu)return c.COMPRESSED_RGB_S3TC_DXT1_EXT;if(s===du)return c.COMPRESSED_RGBA_S3TC_DXT1_EXT;if(s===pu)return c.COMPRESSED_RGBA_S3TC_DXT3_EXT;if(s===mu)return c.COMPRESSED_RGBA_S3TC_DXT5_EXT}else return null;if(s===Hd||s===Gd||s===Vd||s===kd)if(c=t.get("WEBGL_compressed_texture_pvrtc"),c!==null){if(s===Hd)return c.COMPRESSED_RGB_PVRTC_4BPPV1_IMG;if(s===Gd)return c.COMPRESSED_RGB_PVRTC_2BPPV1_IMG;if(s===Vd)return c.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;if(s===kd)return c.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG}else return null;if(s===Xd||s===Wd||s===qd||s===Yd||s===Zd||s===vu||s===jd)if(c=t.get("WEBGL_compressed_texture_etc"),c!==null){if(s===Xd||s===Wd)return f===Ze?c.COMPRESSED_SRGB8_ETC2:c.COMPRESSED_RGB8_ETC2;if(s===qd)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:c.COMPRESSED_RGBA8_ETC2_EAC;if(s===Yd)return c.COMPRESSED_R11_EAC;if(s===Zd)return c.COMPRESSED_SIGNED_R11_EAC;if(s===vu)return c.COMPRESSED_RG11_EAC;if(s===jd)return c.COMPRESSED_SIGNED_RG11_EAC}else return null;if(s===Kd||s===Qd||s===Jd||s===$d||s===tp||s===ep||s===np||s===ip||s===ap||s===sp||s===rp||s===op||s===lp||s===cp)if(c=t.get("WEBGL_compressed_texture_astc"),c!==null){if(s===Kd)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:c.COMPRESSED_RGBA_ASTC_4x4_KHR;if(s===Qd)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:c.COMPRESSED_RGBA_ASTC_5x4_KHR;if(s===Jd)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:c.COMPRESSED_RGBA_ASTC_5x5_KHR;if(s===$d)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:c.COMPRESSED_RGBA_ASTC_6x5_KHR;if(s===tp)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:c.COMPRESSED_RGBA_ASTC_6x6_KHR;if(s===ep)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:c.COMPRESSED_RGBA_ASTC_8x5_KHR;if(s===np)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:c.COMPRESSED_RGBA_ASTC_8x6_KHR;if(s===ip)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:c.COMPRESSED_RGBA_ASTC_8x8_KHR;if(s===ap)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:c.COMPRESSED_RGBA_ASTC_10x5_KHR;if(s===sp)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:c.COMPRESSED_RGBA_ASTC_10x6_KHR;if(s===rp)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:c.COMPRESSED_RGBA_ASTC_10x8_KHR;if(s===op)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:c.COMPRESSED_RGBA_ASTC_10x10_KHR;if(s===lp)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:c.COMPRESSED_RGBA_ASTC_12x10_KHR;if(s===cp)return f===Ze?c.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:c.COMPRESSED_RGBA_ASTC_12x12_KHR}else return null;if(s===up||s===fp||s===hp)if(c=t.get("EXT_texture_compression_bptc"),c!==null){if(s===up)return f===Ze?c.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT:c.COMPRESSED_RGBA_BPTC_UNORM_EXT;if(s===fp)return c.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT;if(s===hp)return c.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT}else return null;if(s===dp||s===pp||s===xu||s===mp)if(c=t.get("EXT_texture_compression_rgtc"),c!==null){if(s===dp)return c.COMPRESSED_RED_RGTC1_EXT;if(s===pp)return c.COMPRESSED_SIGNED_RED_RGTC1_EXT;if(s===xu)return c.COMPRESSED_RED_GREEN_RGTC2_EXT;if(s===mp)return c.COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT}else return null;return s===xl?r.UNSIGNED_INT_24_8:r[s]!==void 0?r[s]:null}return{convert:i}}const j3=`
void main() {

	gl_Position = vec4( position, 1.0 );

}`,K3=`
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

}`;class Q3{constructor(){this.texture=null,this.mesh=null,this.depthNear=0,this.depthFar=0}init(t,i){if(this.texture===null){const s=new Nx(t.texture);(t.depthNear!==i.depthNear||t.depthFar!==i.depthFar)&&(this.depthNear=t.depthNear,this.depthFar=t.depthFar),this.texture=s}}getMesh(t){if(this.texture!==null&&this.mesh===null){const i=t.cameras[0].viewport,s=new $i({vertexShader:j3,fragmentShader:K3,uniforms:{depthColor:{value:this.texture},depthWidth:{value:i.z},depthHeight:{value:i.w}}});this.mesh=new ln(new ms(20,20),s)}return this.mesh}reset(){this.texture=null,this.mesh=null}getDepthTexture(){return this.texture}}class J3 extends Ks{constructor(t,i){super();const s=this;let l=null,c=1,f=null,p="local-floor",m=1,d=null,_=null,v=null,g=null,S=null,E=null;const A=typeof XRWebGLBinding<"u",M=new Q3,y={},B=i.getContextAttributes();let F=null,w=null;const I=[],O=[],D=new fe;let T=null;const L=new mi;L.viewport=new cn;const j=new mi;j.viewport=new cn;const k=[L,j],J=new rb;let ut=null,gt=null;this.cameraAutoUpdate=!0,this.enabled=!1,this.isPresenting=!1,this.getController=function(tt){let yt=I[tt];return yt===void 0&&(yt=new Jh,I[tt]=yt),yt.getTargetRaySpace()},this.getControllerGrip=function(tt){let yt=I[tt];return yt===void 0&&(yt=new Jh,I[tt]=yt),yt.getGripSpace()},this.getHand=function(tt){let yt=I[tt];return yt===void 0&&(yt=new Jh,I[tt]=yt),yt.getHandSpace()};function $(tt){const yt=O.indexOf(tt.inputSource);if(yt===-1)return;const Mt=I[yt];Mt!==void 0&&(Mt.update(tt.inputSource,tt.frame,d||f),Mt.dispatchEvent({type:tt.type,data:tt.inputSource}))}function P(){l.removeEventListener("select",$),l.removeEventListener("selectstart",$),l.removeEventListener("selectend",$),l.removeEventListener("squeeze",$),l.removeEventListener("squeezestart",$),l.removeEventListener("squeezeend",$),l.removeEventListener("end",P),l.removeEventListener("inputsourceschange",H);for(let tt=0;tt<I.length;tt++){const yt=O[tt];yt!==null&&(O[tt]=null,I[tt].disconnect(yt))}ut=null,gt=null,M.reset();for(const tt in y)delete y[tt];t.setRenderTarget(F),S=null,g=null,v=null,l=null,w=null,At.stop(),s.isPresenting=!1,t.setPixelRatio(T),t.setSize(D.width,D.height,!1),s.dispatchEvent({type:"sessionend"})}this.setFramebufferScaleFactor=function(tt){c=tt,s.isPresenting===!0&&re("WebXRManager: Cannot change framebuffer scale while presenting.")},this.setReferenceSpaceType=function(tt){p=tt,s.isPresenting===!0&&re("WebXRManager: Cannot change reference space type while presenting.")},this.getReferenceSpace=function(){return d||f},this.setReferenceSpace=function(tt){d=tt},this.getBaseLayer=function(){return g!==null?g:S},this.getBinding=function(){return v===null&&A&&(v=new XRWebGLBinding(l,i)),v},this.getFrame=function(){return E},this.getSession=function(){return l},this.setSession=async function(tt){if(l=tt,l!==null){if(F=t.getRenderTarget(),l.addEventListener("select",$),l.addEventListener("selectstart",$),l.addEventListener("selectend",$),l.addEventListener("squeeze",$),l.addEventListener("squeezestart",$),l.addEventListener("squeezeend",$),l.addEventListener("end",P),l.addEventListener("inputsourceschange",H),B.xrCompatible!==!0&&await i.makeXRCompatible(),T=t.getPixelRatio(),t.getSize(D),A&&"createProjectionLayer"in XRWebGLBinding.prototype){let Mt=null,Nt=null,ae=null;B.depth&&(ae=B.stencil?i.DEPTH24_STENCIL8:i.DEPTH_COMPONENT24,Mt=B.stencil?Ws:wa,Nt=B.stencil?xl:Ji);const Qt={colorFormat:i.RGBA8,depthFormat:ae,scaleFactor:c};v=this.getBinding(),g=v.createProjectionLayer(Qt),l.updateRenderState({layers:[g]}),t.setPixelRatio(1),t.setSize(g.textureWidth,g.textureHeight,!1),w=new Qi(g.textureWidth,g.textureHeight,{format:Fi,type:gi,depthTexture:new ao(g.textureWidth,g.textureHeight,Nt,void 0,void 0,void 0,void 0,void 0,void 0,Mt),stencilBuffer:B.stencil,colorSpace:t.outputColorSpace,samples:B.antialias?4:0,resolveDepthBuffer:g.ignoreDepthValues===!1,resolveStencilBuffer:g.ignoreDepthValues===!1})}else{const Mt={antialias:B.antialias,alpha:!0,depth:B.depth,stencil:B.stencil,framebufferScaleFactor:c};S=new XRWebGLLayer(l,i,Mt),l.updateRenderState({baseLayer:S}),t.setPixelRatio(1),t.setSize(S.framebufferWidth,S.framebufferHeight,!1),w=new Qi(S.framebufferWidth,S.framebufferHeight,{format:Fi,type:gi,colorSpace:t.outputColorSpace,stencilBuffer:B.stencil,resolveDepthBuffer:S.ignoreDepthValues===!1,resolveStencilBuffer:S.ignoreDepthValues===!1})}w.isXRRenderTarget=!0,this.setFoveation(m),d=null,f=await l.requestReferenceSpace(p),At.setContext(l),At.start(),s.isPresenting=!0,s.dispatchEvent({type:"sessionstart"})}},this.getEnvironmentBlendMode=function(){if(l!==null)return l.environmentBlendMode},this.getDepthTexture=function(){return M.getDepthTexture()};function H(tt){for(let yt=0;yt<tt.removed.length;yt++){const Mt=tt.removed[yt],Nt=O.indexOf(Mt);Nt>=0&&(O[Nt]=null,I[Nt].disconnect(Mt))}for(let yt=0;yt<tt.added.length;yt++){const Mt=tt.added[yt];let Nt=O.indexOf(Mt);if(Nt===-1){for(let Qt=0;Qt<I.length;Qt++)if(Qt>=O.length){O.push(Mt),Nt=Qt;break}else if(O[Qt]===null){O[Qt]=Mt,Nt=Qt;break}if(Nt===-1)break}const ae=I[Nt];ae&&ae.connect(Mt)}}const et=new V,pt=new V;function Et(tt,yt,Mt){et.setFromMatrixPosition(yt.matrixWorld),pt.setFromMatrixPosition(Mt.matrixWorld);const Nt=et.distanceTo(pt),ae=yt.projectionMatrix.elements,Qt=Mt.projectionMatrix.elements,He=ae[14]/(ae[10]-1),pe=ae[14]/(ae[10]+1),Se=(ae[9]+1)/ae[5],Me=(ae[9]-1)/ae[5],Ut=(ae[8]-1)/ae[0],Zt=(Qt[8]+1)/Qt[0],$t=He*Ut,oe=He*Zt,Vt=Nt/(-Ut+Zt),Ce=Vt*-Ut;if(yt.matrixWorld.decompose(tt.position,tt.quaternion,tt.scale),tt.translateX(Ce),tt.translateZ(Vt),tt.matrixWorld.compose(tt.position,tt.quaternion,tt.scale),tt.matrixWorldInverse.copy(tt.matrixWorld).invert(),ae[10]===-1)tt.projectionMatrix.copy(yt.projectionMatrix),tt.projectionMatrixInverse.copy(yt.projectionMatrixInverse);else{const me=He+Vt,Y=pe+Vt,we=$t-Ce,Le=oe+(Nt-Ce),U=Se*pe/Y*me,b=Me*pe/Y*me;tt.projectionMatrix.makePerspective(we,Le,U,b,me,Y),tt.projectionMatrixInverse.copy(tt.projectionMatrix).invert()}}function N(tt,yt){yt===null?tt.matrixWorld.copy(tt.matrix):tt.matrixWorld.multiplyMatrices(yt.matrixWorld,tt.matrix),tt.matrixWorldInverse.copy(tt.matrixWorld).invert()}this.updateCamera=function(tt){if(l===null)return;let yt=tt.near,Mt=tt.far;M.texture!==null&&(M.depthNear>0&&(yt=M.depthNear),M.depthFar>0&&(Mt=M.depthFar)),J.near=j.near=L.near=yt,J.far=j.far=L.far=Mt,(ut!==J.near||gt!==J.far)&&(l.updateRenderState({depthNear:J.near,depthFar:J.far}),ut=J.near,gt=J.far),J.layers.mask=tt.layers.mask|6,L.layers.mask=J.layers.mask&-5,j.layers.mask=J.layers.mask&-3;const Nt=tt.parent,ae=J.cameras;N(J,Nt);for(let Qt=0;Qt<ae.length;Qt++)N(ae[Qt],Nt);ae.length===2?Et(J,L,j):J.projectionMatrix.copy(L.projectionMatrix),W(tt,J,Nt)};function W(tt,yt,Mt){Mt===null?tt.matrix.copy(yt.matrixWorld):(tt.matrix.copy(Mt.matrixWorld),tt.matrix.invert(),tt.matrix.multiply(yt.matrixWorld)),tt.matrix.decompose(tt.position,tt.quaternion,tt.scale),tt.updateMatrixWorld(!0),tt.projectionMatrix.copy(yt.projectionMatrix),tt.projectionMatrixInverse.copy(yt.projectionMatrixInverse),tt.isPerspectiveCamera&&(tt.fov=Sl*2*Math.atan(1/tt.projectionMatrix.elements[5]),tt.zoom=1)}this.getCamera=function(){return J},this.getFoveation=function(){if(!(g===null&&S===null))return m},this.setFoveation=function(tt){m=tt,g!==null&&(g.fixedFoveation=tt),S!==null&&S.fixedFoveation!==void 0&&(S.fixedFoveation=tt)},this.hasDepthSensing=function(){return M.texture!==null},this.getDepthSensingMesh=function(){return M.getMesh(J)},this.getCameraTexture=function(tt){return y[tt]};let vt=null;function Tt(tt,yt){if(_=yt.getViewerPose(d||f),E=yt,_!==null){const Mt=_.views;S!==null&&(t.setRenderTargetFramebuffer(w,S.framebuffer),t.setRenderTarget(w));let Nt=!1;Mt.length!==J.cameras.length&&(J.cameras.length=0,Nt=!0);for(let pe=0;pe<Mt.length;pe++){const Se=Mt[pe];let Me=null;if(S!==null)Me=S.getViewport(Se);else{const Zt=v.getViewSubImage(g,Se);Me=Zt.viewport,pe===0&&(t.setRenderTargetTextures(w,Zt.colorTexture,Zt.depthStencilTexture),t.setRenderTarget(w))}let Ut=k[pe];Ut===void 0&&(Ut=new mi,Ut.layers.enable(pe),Ut.viewport=new cn,k[pe]=Ut),Ut.matrix.fromArray(Se.transform.matrix),Ut.matrix.decompose(Ut.position,Ut.quaternion,Ut.scale),Ut.projectionMatrix.fromArray(Se.projectionMatrix),Ut.projectionMatrixInverse.copy(Ut.projectionMatrix).invert(),Ut.viewport.set(Me.x,Me.y,Me.width,Me.height),pe===0&&(J.matrix.copy(Ut.matrix),J.matrix.decompose(J.position,J.quaternion,J.scale)),Nt===!0&&J.cameras.push(Ut)}const ae=l.enabledFeatures;if(ae&&ae.includes("depth-sensing")&&l.depthUsage=="gpu-optimized"&&A){v=s.getBinding();const pe=v.getDepthInformation(Mt[0]);pe&&pe.isValid&&pe.texture&&M.init(pe,l.renderState)}if(ae&&ae.includes("camera-access")&&A){t.state.unbindTexture(),v=s.getBinding();for(let pe=0;pe<Mt.length;pe++){const Se=Mt[pe].camera;if(Se){let Me=y[Se];Me||(Me=new Nx,y[Se]=Me);const Ut=v.getCameraImage(Se);Me.sourceTexture=Ut}}}}for(let Mt=0;Mt<I.length;Mt++){const Nt=O[Mt],ae=I[Mt];Nt!==null&&ae!==void 0&&ae.update(Nt,yt,d||f)}vt&&vt(tt,yt),yt.detectedPlanes&&s.dispatchEvent({type:"planesdetected",data:yt}),E=null}const At=new Bx;At.setAnimationLoop(Tt),this.setAnimationLoop=function(tt){vt=tt},this.dispose=function(){}}}const $3=new Fe,Xx=new de;Xx.set(-1,0,0,0,1,0,0,0,1);function tR(r,t){function i(M,y){M.matrixAutoUpdate===!0&&M.updateMatrix(),y.value.copy(M.matrix)}function s(M,y){y.color.getRGB(M.fogColor.value,Lx(r)),y.isFog?(M.fogNear.value=y.near,M.fogFar.value=y.far):y.isFogExp2&&(M.fogDensity.value=y.density)}function l(M,y,B,F,w){y.isNodeMaterial?y.uniformsNeedUpdate=!1:y.isMeshBasicMaterial?c(M,y):y.isMeshLambertMaterial?(c(M,y),y.envMap&&(M.envMapIntensity.value=y.envMapIntensity)):y.isMeshToonMaterial?(c(M,y),v(M,y)):y.isMeshPhongMaterial?(c(M,y),_(M,y),y.envMap&&(M.envMapIntensity.value=y.envMapIntensity)):y.isMeshStandardMaterial?(c(M,y),g(M,y),y.isMeshPhysicalMaterial&&S(M,y,w)):y.isMeshMatcapMaterial?(c(M,y),E(M,y)):y.isMeshDepthMaterial?c(M,y):y.isMeshDistanceMaterial?(c(M,y),A(M,y)):y.isMeshNormalMaterial?c(M,y):y.isLineBasicMaterial?(f(M,y),y.isLineDashedMaterial&&p(M,y)):y.isPointsMaterial?m(M,y,B,F):y.isSpriteMaterial?d(M,y):y.isShadowMaterial?(M.color.value.copy(y.color),M.opacity.value=y.opacity):y.isShaderMaterial&&(y.uniformsNeedUpdate=!1)}function c(M,y){M.opacity.value=y.opacity,y.color&&M.diffuse.value.copy(y.color),y.emissive&&M.emissive.value.copy(y.emissive).multiplyScalar(y.emissiveIntensity),y.map&&(M.map.value=y.map,i(y.map,M.mapTransform)),y.alphaMap&&(M.alphaMap.value=y.alphaMap,i(y.alphaMap,M.alphaMapTransform)),y.bumpMap&&(M.bumpMap.value=y.bumpMap,i(y.bumpMap,M.bumpMapTransform),M.bumpScale.value=y.bumpScale,y.side===ti&&(M.bumpScale.value*=-1)),y.normalMap&&(M.normalMap.value=y.normalMap,i(y.normalMap,M.normalMapTransform),M.normalScale.value.copy(y.normalScale),y.side===ti&&M.normalScale.value.negate()),y.displacementMap&&(M.displacementMap.value=y.displacementMap,i(y.displacementMap,M.displacementMapTransform),M.displacementScale.value=y.displacementScale,M.displacementBias.value=y.displacementBias),y.emissiveMap&&(M.emissiveMap.value=y.emissiveMap,i(y.emissiveMap,M.emissiveMapTransform)),y.specularMap&&(M.specularMap.value=y.specularMap,i(y.specularMap,M.specularMapTransform)),y.alphaTest>0&&(M.alphaTest.value=y.alphaTest);const B=t.get(y),F=B.envMap,w=B.envMapRotation;F&&(M.envMap.value=F,M.envMapRotation.value.setFromMatrix4($3.makeRotationFromEuler(w)).transpose(),F.isCubeTexture&&F.isRenderTargetTexture===!1&&M.envMapRotation.value.premultiply(Xx),M.reflectivity.value=y.reflectivity,M.ior.value=y.ior,M.refractionRatio.value=y.refractionRatio),y.lightMap&&(M.lightMap.value=y.lightMap,M.lightMapIntensity.value=y.lightMapIntensity,i(y.lightMap,M.lightMapTransform)),y.aoMap&&(M.aoMap.value=y.aoMap,M.aoMapIntensity.value=y.aoMapIntensity,i(y.aoMap,M.aoMapTransform))}function f(M,y){M.diffuse.value.copy(y.color),M.opacity.value=y.opacity,y.map&&(M.map.value=y.map,i(y.map,M.mapTransform))}function p(M,y){M.dashSize.value=y.dashSize,M.totalSize.value=y.dashSize+y.gapSize,M.scale.value=y.scale}function m(M,y,B,F){M.diffuse.value.copy(y.color),M.opacity.value=y.opacity,M.size.value=y.size*B,M.scale.value=F*.5,y.map&&(M.map.value=y.map,i(y.map,M.uvTransform)),y.alphaMap&&(M.alphaMap.value=y.alphaMap,i(y.alphaMap,M.alphaMapTransform)),y.alphaTest>0&&(M.alphaTest.value=y.alphaTest)}function d(M,y){M.diffuse.value.copy(y.color),M.opacity.value=y.opacity,M.rotation.value=y.rotation,y.map&&(M.map.value=y.map,i(y.map,M.mapTransform)),y.alphaMap&&(M.alphaMap.value=y.alphaMap,i(y.alphaMap,M.alphaMapTransform)),y.alphaTest>0&&(M.alphaTest.value=y.alphaTest)}function _(M,y){M.specular.value.copy(y.specular),M.shininess.value=Math.max(y.shininess,1e-4)}function v(M,y){y.gradientMap&&(M.gradientMap.value=y.gradientMap)}function g(M,y){M.metalness.value=y.metalness,y.metalnessMap&&(M.metalnessMap.value=y.metalnessMap,i(y.metalnessMap,M.metalnessMapTransform)),M.roughness.value=y.roughness,y.roughnessMap&&(M.roughnessMap.value=y.roughnessMap,i(y.roughnessMap,M.roughnessMapTransform)),y.envMap&&(M.envMapIntensity.value=y.envMapIntensity)}function S(M,y,B){M.ior.value=y.ior,y.sheen>0&&(M.sheenColor.value.copy(y.sheenColor).multiplyScalar(y.sheen),M.sheenRoughness.value=y.sheenRoughness,y.sheenColorMap&&(M.sheenColorMap.value=y.sheenColorMap,i(y.sheenColorMap,M.sheenColorMapTransform)),y.sheenRoughnessMap&&(M.sheenRoughnessMap.value=y.sheenRoughnessMap,i(y.sheenRoughnessMap,M.sheenRoughnessMapTransform))),y.clearcoat>0&&(M.clearcoat.value=y.clearcoat,M.clearcoatRoughness.value=y.clearcoatRoughness,y.clearcoatMap&&(M.clearcoatMap.value=y.clearcoatMap,i(y.clearcoatMap,M.clearcoatMapTransform)),y.clearcoatRoughnessMap&&(M.clearcoatRoughnessMap.value=y.clearcoatRoughnessMap,i(y.clearcoatRoughnessMap,M.clearcoatRoughnessMapTransform)),y.clearcoatNormalMap&&(M.clearcoatNormalMap.value=y.clearcoatNormalMap,i(y.clearcoatNormalMap,M.clearcoatNormalMapTransform),M.clearcoatNormalScale.value.copy(y.clearcoatNormalScale),y.side===ti&&M.clearcoatNormalScale.value.negate())),y.dispersion>0&&(M.dispersion.value=y.dispersion),y.iridescence>0&&(M.iridescence.value=y.iridescence,M.iridescenceIOR.value=y.iridescenceIOR,M.iridescenceThicknessMinimum.value=y.iridescenceThicknessRange[0],M.iridescenceThicknessMaximum.value=y.iridescenceThicknessRange[1],y.iridescenceMap&&(M.iridescenceMap.value=y.iridescenceMap,i(y.iridescenceMap,M.iridescenceMapTransform)),y.iridescenceThicknessMap&&(M.iridescenceThicknessMap.value=y.iridescenceThicknessMap,i(y.iridescenceThicknessMap,M.iridescenceThicknessMapTransform))),y.transmission>0&&(M.transmission.value=y.transmission,M.transmissionSamplerMap.value=B.texture,M.transmissionSamplerSize.value.set(B.width,B.height),y.transmissionMap&&(M.transmissionMap.value=y.transmissionMap,i(y.transmissionMap,M.transmissionMapTransform)),M.thickness.value=y.thickness,y.thicknessMap&&(M.thicknessMap.value=y.thicknessMap,i(y.thicknessMap,M.thicknessMapTransform)),M.attenuationDistance.value=y.attenuationDistance,M.attenuationColor.value.copy(y.attenuationColor)),y.anisotropy>0&&(M.anisotropyVector.value.set(y.anisotropy*Math.cos(y.anisotropyRotation),y.anisotropy*Math.sin(y.anisotropyRotation)),y.anisotropyMap&&(M.anisotropyMap.value=y.anisotropyMap,i(y.anisotropyMap,M.anisotropyMapTransform))),M.specularIntensity.value=y.specularIntensity,M.specularColor.value.copy(y.specularColor),y.specularColorMap&&(M.specularColorMap.value=y.specularColorMap,i(y.specularColorMap,M.specularColorMapTransform)),y.specularIntensityMap&&(M.specularIntensityMap.value=y.specularIntensityMap,i(y.specularIntensityMap,M.specularIntensityMapTransform))}function E(M,y){y.matcap&&(M.matcap.value=y.matcap)}function A(M,y){const B=t.get(y).light;M.referencePosition.value.setFromMatrixPosition(B.matrixWorld),M.nearDistance.value=B.shadow.camera.near,M.farDistance.value=B.shadow.camera.far}return{refreshFogUniforms:s,refreshMaterialUniforms:l}}function eR(r,t,i,s){let l={},c={},f=[];const p=r.getParameter(r.MAX_UNIFORM_BUFFER_BINDINGS);function m(w,I){const O=I.program;s.uniformBlockBinding(w,O)}function d(w,I){let O=l[w.id];O===void 0&&(M(w),O=_(w),l[w.id]=O,w.addEventListener("dispose",B));const D=I.program;s.updateUBOMapping(w,D);const T=t.render.frame;c[w.id]!==T&&(g(w),c[w.id]=T)}function _(w){const I=v();w.__bindingPointIndex=I;const O=r.createBuffer(),D=w.__size,T=w.usage;return r.bindBuffer(r.UNIFORM_BUFFER,O),r.bufferData(r.UNIFORM_BUFFER,D,T),r.bindBuffer(r.UNIFORM_BUFFER,null),r.bindBufferBase(r.UNIFORM_BUFFER,I,O),O}function v(){for(let w=0;w<p;w++)if(f.indexOf(w)===-1)return f.push(w),w;return Ue("WebGLRenderer: Maximum number of simultaneously usable uniforms groups reached."),0}function g(w){const I=l[w.id],O=w.uniforms,D=w.__cache;r.bindBuffer(r.UNIFORM_BUFFER,I);for(let T=0,L=O.length;T<L;T++){const j=O[T];if(Array.isArray(j))for(let k=0,J=j.length;k<J;k++)S(j[k],T,k,D);else S(j,T,0,D)}r.bindBuffer(r.UNIFORM_BUFFER,null)}function S(w,I,O,D){if(A(w,I,O,D)===!0){const T=w.__offset,L=w.value;if(Array.isArray(L)){let j=0;for(let k=0;k<L.length;k++){const J=L[k],ut=y(J);E(J,w.__data,j),typeof J!="number"&&typeof J!="boolean"&&!J.isMatrix3&&!ArrayBuffer.isView(J)&&(j+=ut.storage/Float32Array.BYTES_PER_ELEMENT)}}else E(L,w.__data,0);r.bufferSubData(r.UNIFORM_BUFFER,T,w.__data)}}function E(w,I,O){typeof w=="number"||typeof w=="boolean"?I[0]=w:w.isMatrix3?(I[0]=w.elements[0],I[1]=w.elements[1],I[2]=w.elements[2],I[3]=0,I[4]=w.elements[3],I[5]=w.elements[4],I[6]=w.elements[5],I[7]=0,I[8]=w.elements[6],I[9]=w.elements[7],I[10]=w.elements[8],I[11]=0):ArrayBuffer.isView(w)?I.set(new w.constructor(w.buffer,w.byteOffset,I.length)):w.toArray(I,O)}function A(w,I,O,D){const T=w.value,L=I+"_"+O;if(D[L]===void 0)return typeof T=="number"||typeof T=="boolean"?D[L]=T:ArrayBuffer.isView(T)?D[L]=T.slice():D[L]=T.clone(),!0;{const j=D[L];if(typeof T=="number"||typeof T=="boolean"){if(j!==T)return D[L]=T,!0}else{if(ArrayBuffer.isView(T))return!0;if(j.equals(T)===!1)return j.copy(T),!0}}return!1}function M(w){const I=w.uniforms;let O=0;const D=16;for(let L=0,j=I.length;L<j;L++){const k=Array.isArray(I[L])?I[L]:[I[L]];for(let J=0,ut=k.length;J<ut;J++){const gt=k[J],$=Array.isArray(gt.value)?gt.value:[gt.value];for(let P=0,H=$.length;P<H;P++){const et=$[P],pt=y(et),Et=O%D,N=Et%pt.boundary,W=Et+N;O+=N,W!==0&&D-W<pt.storage&&(O+=D-W),gt.__data=new Float32Array(pt.storage/Float32Array.BYTES_PER_ELEMENT),gt.__offset=O,O+=pt.storage}}}const T=O%D;return T>0&&(O+=D-T),w.__size=O,w.__cache={},this}function y(w){const I={boundary:0,storage:0};return typeof w=="number"||typeof w=="boolean"?(I.boundary=4,I.storage=4):w.isVector2?(I.boundary=8,I.storage=8):w.isVector3||w.isColor?(I.boundary=16,I.storage=12):w.isVector4?(I.boundary=16,I.storage=16):w.isMatrix3?(I.boundary=48,I.storage=48):w.isMatrix4?(I.boundary=64,I.storage=64):w.isTexture?re("WebGLRenderer: Texture samplers can not be part of an uniforms group."):ArrayBuffer.isView(w)?(I.boundary=16,I.storage=w.byteLength):re("WebGLRenderer: Unsupported uniform value type.",w),I}function B(w){const I=w.target;I.removeEventListener("dispose",B);const O=f.indexOf(I.__bindingPointIndex);f.splice(O,1),r.deleteBuffer(l[I.id]),delete l[I.id],delete c[I.id]}function F(){for(const w in l)r.deleteBuffer(l[w]);f=[],l={},c={}}return{bind:m,update:d,dispose:F}}const nR=new Uint16Array([12469,15057,12620,14925,13266,14620,13807,14376,14323,13990,14545,13625,14713,13328,14840,12882,14931,12528,14996,12233,15039,11829,15066,11525,15080,11295,15085,10976,15082,10705,15073,10495,13880,14564,13898,14542,13977,14430,14158,14124,14393,13732,14556,13410,14702,12996,14814,12596,14891,12291,14937,11834,14957,11489,14958,11194,14943,10803,14921,10506,14893,10278,14858,9960,14484,14039,14487,14025,14499,13941,14524,13740,14574,13468,14654,13106,14743,12678,14818,12344,14867,11893,14889,11509,14893,11180,14881,10751,14852,10428,14812,10128,14765,9754,14712,9466,14764,13480,14764,13475,14766,13440,14766,13347,14769,13070,14786,12713,14816,12387,14844,11957,14860,11549,14868,11215,14855,10751,14825,10403,14782,10044,14729,9651,14666,9352,14599,9029,14967,12835,14966,12831,14963,12804,14954,12723,14936,12564,14917,12347,14900,11958,14886,11569,14878,11247,14859,10765,14828,10401,14784,10011,14727,9600,14660,9289,14586,8893,14508,8533,15111,12234,15110,12234,15104,12216,15092,12156,15067,12010,15028,11776,14981,11500,14942,11205,14902,10752,14861,10393,14812,9991,14752,9570,14682,9252,14603,8808,14519,8445,14431,8145,15209,11449,15208,11451,15202,11451,15190,11438,15163,11384,15117,11274,15055,10979,14994,10648,14932,10343,14871,9936,14803,9532,14729,9218,14645,8742,14556,8381,14461,8020,14365,7603,15273,10603,15272,10607,15267,10619,15256,10631,15231,10614,15182,10535,15118,10389,15042,10167,14963,9787,14883,9447,14800,9115,14710,8665,14615,8318,14514,7911,14411,7507,14279,7198,15314,9675,15313,9683,15309,9712,15298,9759,15277,9797,15229,9773,15166,9668,15084,9487,14995,9274,14898,8910,14800,8539,14697,8234,14590,7790,14479,7409,14367,7067,14178,6621,15337,8619,15337,8631,15333,8677,15325,8769,15305,8871,15264,8940,15202,8909,15119,8775,15022,8565,14916,8328,14804,8009,14688,7614,14569,7287,14448,6888,14321,6483,14088,6171,15350,7402,15350,7419,15347,7480,15340,7613,15322,7804,15287,7973,15229,8057,15148,8012,15046,7846,14933,7611,14810,7357,14682,7069,14552,6656,14421,6316,14251,5948,14007,5528,15356,5942,15356,5977,15353,6119,15348,6294,15332,6551,15302,6824,15249,7044,15171,7122,15070,7050,14949,6861,14818,6611,14679,6349,14538,6067,14398,5651,14189,5311,13935,4958,15359,4123,15359,4153,15356,4296,15353,4646,15338,5160,15311,5508,15263,5829,15188,6042,15088,6094,14966,6001,14826,5796,14678,5543,14527,5287,14377,4985,14133,4586,13869,4257,15360,1563,15360,1642,15358,2076,15354,2636,15341,3350,15317,4019,15273,4429,15203,4732,15105,4911,14981,4932,14836,4818,14679,4621,14517,4386,14359,4156,14083,3795,13808,3437,15360,122,15360,137,15358,285,15355,636,15344,1274,15322,2177,15281,2765,15215,3223,15120,3451,14995,3569,14846,3567,14681,3466,14511,3305,14344,3121,14037,2800,13753,2467,15360,0,15360,1,15359,21,15355,89,15346,253,15325,479,15287,796,15225,1148,15133,1492,15008,1749,14856,1882,14685,1886,14506,1783,14324,1608,13996,1398,13702,1183]);let Yi=null;function iR(){return Yi===null&&(Yi=new Rx(nR,16,16,js,Ca),Yi.name="DFG_LUT",Yi.minFilter=Gn,Yi.magFilter=Gn,Yi.wrapS=ba,Yi.wrapT=ba,Yi.generateMipmaps=!1,Yi.needsUpdate=!0),Yi}class aR{constructor(t={}){const{canvas:i=J1(),context:s=null,depth:l=!0,stencil:c=!1,alpha:f=!1,antialias:p=!1,premultipliedAlpha:m=!0,preserveDrawingBuffer:d=!1,powerPreference:_="default",failIfMajorPerformanceCaveat:v=!1,reversedDepthBuffer:g=!1,outputBufferType:S=gi}=t;this.isWebGLRenderer=!0;let E;if(s!==null){if(typeof WebGLRenderingContext<"u"&&s instanceof WebGLRenderingContext)throw new Error("THREE.WebGLRenderer: WebGL 1 is not supported since r163.");E=s.getContextAttributes().alpha}else E=f;const A=S,M=new Set([Op,Lp,Np]),y=new Set([gi,Ji,vl,xl,wp,Dp]),B=new Uint32Array(4),F=new Int32Array(4),w=new V;let I=null,O=null;const D=[],T=[];let L=null;this.domElement=i,this.debug={checkShaderErrors:!0,onShaderError:null},this.autoClear=!0,this.autoClearColor=!0,this.autoClearDepth=!0,this.autoClearStencil=!0,this.sortObjects=!0,this.clippingPlanes=[],this.localClippingEnabled=!1,this.toneMapping=Ki,this.toneMappingExposure=1,this.transmissionResolutionScale=1;const j=this;let k=!1,J=null,ut=null,gt=null,$=null;this._outputColorSpace=qn;let P=0,H=0,et=null,pt=-1,Et=null;const N=new cn,W=new cn;let vt=null;const Tt=new ue(0);let At=0,tt=i.width,yt=i.height,Mt=1,Nt=null,ae=null;const Qt=new cn(0,0,tt,yt),He=new cn(0,0,tt,yt);let pe=!1;const Se=new Vp;let Me=!1,Ut=!1;const Zt=new Fe,$t=new V,oe=new cn,Vt={background:null,fog:null,environment:null,overrideMaterial:null,isScene:!0};let Ce=!1;function me(){return et===null?Mt:1}let Y=s;function we(R,Z){return i.getContext(R,Z)}try{const R={alpha:!0,depth:l,stencil:c,antialias:p,premultipliedAlpha:m,preserveDrawingBuffer:d,powerPreference:_,failIfMajorPerformanceCaveat:v};if("setAttribute"in i&&i.setAttribute("data-engine",`three.js r${Ap}`),i.addEventListener("webglcontextlost",an,!1),i.addEventListener("webglcontextrestored",Ve,!1),i.addEventListener("webglcontextcreationerror",ei,!1),Y===null){const Z="webgl2";if(Y=we(Z,R),Y===null)throw we(Z)?new Error("THREE.WebGLRenderer: Error creating WebGL context with your selected attributes."):new Error("THREE.WebGLRenderer: Error creating WebGL context.")}}catch(R){throw Ue("WebGLRenderer: "+R.message),R}let Le,U,b,Q,rt,ht,Rt,Lt,ft,dt,wt,Ht,zt,Ot,te,ee,le,q,Ct,_t,Dt,Ft,bt;function jt(){Le=new i2(Y),Le.init(),Dt=new Z3(Y,Le),U=new jA(Y,Le,t,Dt),b=new q3(Y,Le),U.reversedDepthBuffer&&g&&b.buffers.depth.setReversed(!0),ut=Y.createFramebuffer(),gt=Y.createFramebuffer(),$=Y.createFramebuffer(),Q=new r2(Y),rt=new N3,ht=new Y3(Y,Le,b,rt,U,Dt,Q),Rt=new n2(j),Lt=new ub(Y),Ft=new YA(Y,Lt),ft=new a2(Y,Lt,Q,Ft),dt=new l2(Y,ft,Lt,Ft,Q),q=new o2(Y,U,ht),te=new KA(rt),wt=new U3(j,Rt,Le,U,Ft,te),Ht=new tR(j,rt),zt=new O3,Ot=new H3(Le),le=new qA(j,Rt,b,dt,E,m),ee=new W3(j,dt,U),bt=new eR(Y,Q,U,b),Ct=new ZA(Y,Le,Q),_t=new s2(Y,Le,Q),Q.programs=wt.programs,j.capabilities=U,j.extensions=Le,j.properties=rt,j.renderLists=zt,j.shadowMap=ee,j.state=b,j.info=Q}jt(),A!==gi&&(L=new u2(A,i.width,i.height,p,l,c));const Xt=new J3(j,Y);this.xr=Xt,this.getContext=function(){return Y},this.getContextAttributes=function(){return Y.getContextAttributes()},this.forceContextLoss=function(){const R=Le.get("WEBGL_lose_context");R&&R.loseContext()},this.forceContextRestore=function(){const R=Le.get("WEBGL_lose_context");R&&R.restoreContext()},this.getPixelRatio=function(){return Mt},this.setPixelRatio=function(R){R!==void 0&&(Mt=R,this.setSize(tt,yt,!1))},this.getSize=function(R){return R.set(tt,yt)},this.setSize=function(R,Z,ot=!0){if(Xt.isPresenting){re("WebGLRenderer: Can't change size while VR device is presenting.");return}tt=R,yt=Z,i.width=Math.floor(R*Mt),i.height=Math.floor(Z*Mt),ot===!0&&(i.style.width=R+"px",i.style.height=Z+"px"),L!==null&&L.setSize(i.width,i.height),this.setViewport(0,0,R,Z)},this.getDrawingBufferSize=function(R){return R.set(tt*Mt,yt*Mt).floor()},this.setDrawingBufferSize=function(R,Z,ot){tt=R,yt=Z,Mt=ot,i.width=Math.floor(R*ot),i.height=Math.floor(Z*ot),this.setViewport(0,0,R,Z)},this.setEffects=function(R){if(A===gi){Ue("WebGLRenderer: setEffects() requires outputBufferType set to HalfFloatType or FloatType.");return}if(R){for(let Z=0;Z<R.length;Z++)if(R[Z].isOutputPass===!0){re("WebGLRenderer: OutputPass is not needed in setEffects(). Tone mapping and color space conversion are applied automatically.");break}}L.setEffects(R||[])},this.getCurrentViewport=function(R){return R.copy(N)},this.getViewport=function(R){return R.copy(Qt)},this.setViewport=function(R,Z,ot,at){R.isVector4?Qt.set(R.x,R.y,R.z,R.w):Qt.set(R,Z,ot,at),b.viewport(N.copy(Qt).multiplyScalar(Mt).round())},this.getScissor=function(R){return R.copy(He)},this.setScissor=function(R,Z,ot,at){R.isVector4?He.set(R.x,R.y,R.z,R.w):He.set(R,Z,ot,at),b.scissor(W.copy(He).multiplyScalar(Mt).round())},this.getScissorTest=function(){return pe},this.setScissorTest=function(R){b.setScissorTest(pe=R)},this.setOpaqueSort=function(R){Nt=R},this.setTransparentSort=function(R){ae=R},this.getClearColor=function(R){return R.copy(le.getClearColor())},this.setClearColor=function(){le.setClearColor(...arguments)},this.getClearAlpha=function(){return le.getClearAlpha()},this.setClearAlpha=function(){le.setClearAlpha(...arguments)},this.clear=function(R=!0,Z=!0,ot=!0){let at=0;if(R){let st=!1;if(et!==null){const Bt=et.texture.format;st=M.has(Bt)}if(st){const Bt=et.texture.type,kt=y.has(Bt),Pt=le.getClearColor(),qt=le.getClearAlpha(),Wt=Pt.r,ne=Pt.g,ge=Pt.b;kt?(B[0]=Wt,B[1]=ne,B[2]=ge,B[3]=qt,Y.clearBufferuiv(Y.COLOR,0,B)):(F[0]=Wt,F[1]=ne,F[2]=ge,F[3]=qt,Y.clearBufferiv(Y.COLOR,0,F))}else at|=Y.COLOR_BUFFER_BIT}Z&&(at|=Y.DEPTH_BUFFER_BIT,this.state.buffers.depth.setMask(!0)),ot&&(at|=Y.STENCIL_BUFFER_BIT,this.state.buffers.stencil.setMask(4294967295)),at!==0&&Y.clear(at)},this.clearColor=function(){this.clear(!0,!1,!1)},this.clearDepth=function(){this.clear(!1,!0,!1)},this.clearStencil=function(){this.clear(!1,!1,!0)},this.setNodesHandler=function(R){R.setRenderer(this),J=R},this.dispose=function(){i.removeEventListener("webglcontextlost",an,!1),i.removeEventListener("webglcontextrestored",Ve,!1),i.removeEventListener("webglcontextcreationerror",ei,!1),le.dispose(),zt.dispose(),Ot.dispose(),rt.dispose(),Rt.dispose(),dt.dispose(),Ft.dispose(),bt.dispose(),wt.dispose(),Xt.dispose(),Xt.removeEventListener("sessionstart",mn),Xt.removeEventListener("sessionend",Un),Yn.stop()};function an(R){R.preventDefault(),Eu("WebGLRenderer: Context Lost."),k=!0}function Ve(){Eu("WebGLRenderer: Context Restored."),k=!1;const R=Q.autoReset,Z=ee.enabled,ot=ee.autoUpdate,at=ee.needsUpdate,st=ee.type;jt(),Q.autoReset=R,ee.enabled=Z,ee.autoUpdate=ot,ee.needsUpdate=at,ee.type=st}function ei(R){Ue("WebGLRenderer: A WebGL context could not be created. Reason: ",R.statusMessage)}function ni(R){const Z=R.target;Z.removeEventListener("dispose",ni),lo(Z)}function lo(R){co(R),rt.remove(R)}function co(R){const Z=rt.get(R).programs;Z!==void 0&&(Z.forEach(function(ot){wt.releaseProgram(ot)}),R.isShaderMaterial&&wt.releaseShaderCache(R))}this.renderBufferDirect=function(R,Z,ot,at,st,Bt){Z===null&&(Z=Vt);const kt=st.isMesh&&st.matrixWorld.determinantAffine()<0,Pt=Na(R,Z,ot,at,st);b.setMaterial(at,kt);let qt=ot.index,Wt=1;if(at.wireframe===!0){if(qt=ft.getWireframeAttribute(ot),qt===void 0)return;Wt=2}const ne=ot.drawRange,ge=ot.attributes.position;let Jt=ne.start*Wt,Pe=(ne.start+ne.count)*Wt;Bt!==null&&(Jt=Math.max(Jt,Bt.start*Wt),Pe=Math.min(Pe,(Bt.start+Bt.count)*Wt)),qt!==null?(Jt=Math.max(Jt,0),Pe=Math.min(Pe,qt.count)):ge!=null&&(Jt=Math.max(Jt,0),Pe=Math.min(Pe,ge.count));const sn=Pe-Jt;if(sn<0||sn===1/0)return;Ft.setup(st,at,Pt,ot,qt);let $e,ke=Ct;if(qt!==null&&($e=Lt.get(qt),ke=_t,ke.setIndex($e)),st.isMesh)at.wireframe===!0?(b.setLineWidth(at.wireframeLinewidth*me()),ke.setMode(Y.LINES)):ke.setMode(Y.TRIANGLES);else if(st.isLine){let Xe=at.linewidth;Xe===void 0&&(Xe=1),b.setLineWidth(Xe*me()),st.isLineSegments?ke.setMode(Y.LINES):st.isLineLoop?ke.setMode(Y.LINE_LOOP):ke.setMode(Y.LINE_STRIP)}else st.isPoints?ke.setMode(Y.POINTS):st.isSprite&&ke.setMode(Y.TRIANGLES);if(st.isBatchedMesh)if(Le.get("WEBGL_multi_draw"))ke.renderMultiDraw(st._multiDrawStarts,st._multiDrawCounts,st._multiDrawCount);else{const Xe=st._multiDrawStarts,Gt=st._multiDrawCounts,Bn=st._multiDrawCount,Ee=qt?Lt.get(qt).bytesPerElement:1,bn=rt.get(at).currentProgram.getUniforms();for(let ii=0;ii<Bn;ii++)bn.setValue(Y,"_gl_DrawID",ii),ke.render(Xe[ii]/Ee,Gt[ii])}else if(st.isInstancedMesh)ke.renderInstances(Jt,sn,st.count);else if(ot.isInstancedBufferGeometry){const Xe=ot._maxInstanceCount!==void 0?ot._maxInstanceCount:1/0,Gt=Math.min(ot.instanceCount,Xe);ke.renderInstances(Jt,sn,Gt)}else ke.render(Jt,sn)};function uo(R,Z,ot){R.transparent===!0&&R.side===Ci&&R.forceSinglePass===!1?(R.side=ti,R.needsUpdate=!0,Ua(R,Z,ot),R.side=hs,R.needsUpdate=!0,Ua(R,Z,ot),R.side=Ci):Ua(R,Z,ot)}this.compile=function(R,Z,ot=null){ot===null&&(ot=R),O=Ot.get(ot),O.init(Z),T.push(O),ot.traverseVisible(function(st){st.isLight&&st.layers.test(Z.layers)&&(O.pushLight(st),st.castShadow&&O.pushShadow(st))}),R!==ot&&R.traverseVisible(function(st){st.isLight&&st.layers.test(Z.layers)&&(O.pushLight(st),st.castShadow&&O.pushShadow(st))}),O.setupLights();const at=new Set;return R.traverse(function(st){if(!(st.isMesh||st.isPoints||st.isLine||st.isSprite))return;const Bt=st.material;if(Bt)if(Array.isArray(Bt))for(let kt=0;kt<Bt.length;kt++){const Pt=Bt[kt];uo(Pt,ot,st),at.add(Pt)}else uo(Bt,ot,st),at.add(Bt)}),O=T.pop(),at},this.compileAsync=function(R,Z,ot=null){const at=this.compile(R,Z,ot);return new Promise(st=>{function Bt(){if(at.forEach(function(kt){rt.get(kt).currentProgram.isReady()&&at.delete(kt)}),at.size===0){st(R);return}setTimeout(Bt,10)}Le.get("KHR_parallel_shader_compile")!==null?Bt():setTimeout(Bt,10)})};let $s=null;function Hi(R){$s&&$s(R)}function mn(){Yn.stop()}function Un(){Yn.start()}const Yn=new Bx;Yn.setAnimationLoop(Hi),typeof self<"u"&&Yn.setContext(self),this.setAnimationLoop=function(R){$s=R,Xt.setAnimationLoop(R),R===null?Yn.stop():Yn.start()},Xt.addEventListener("sessionstart",mn),Xt.addEventListener("sessionend",Un),this.render=function(R,Z){if(Z!==void 0&&Z.isCamera!==!0){Ue("WebGLRenderer.render: camera is not an instance of THREE.Camera.");return}if(k===!0)return;J!==null&&J.renderStart(R,Z);const ot=Xt.enabled===!0&&Xt.isPresenting===!0,at=L!==null&&(et===null||ot)&&L.begin(j,et);if(R.matrixWorldAutoUpdate===!0&&R.updateMatrixWorld(),Z.parent===null&&Z.matrixWorldAutoUpdate===!0&&Z.updateMatrixWorld(),Xt.enabled===!0&&Xt.isPresenting===!0&&(L===null||L.isCompositing()===!1)&&(Xt.cameraAutoUpdate===!0&&Xt.updateCamera(Z),Z=Xt.getCamera()),R.isScene===!0&&R.onBeforeRender(j,R,Z,et),O=Ot.get(R,T.length),O.init(Z),O.state.textureUnits=ht.getTextureUnits(),T.push(O),Zt.multiplyMatrices(Z.projectionMatrix,Z.matrixWorldInverse),Se.setFromProjectionMatrix(Zt,ji,Z.reversedDepth),Ut=this.localClippingEnabled,Me=te.init(this.clippingPlanes,Ut),I=zt.get(R,D.length),I.init(),D.push(I),Xt.enabled===!0&&Xt.isPresenting===!0){const kt=j.xr.getDepthSensingMesh();kt!==null&&gs(kt,Z,-1/0,j.sortObjects)}gs(R,Z,0,j.sortObjects),I.finish(),j.sortObjects===!0&&I.sort(Nt,ae,Z.reversedDepth),Ce=Xt.enabled===!1||Xt.isPresenting===!1||Xt.hasDepthSensing()===!1,Ce&&le.addToRenderList(I,R),this.info.render.frame++,this.info.autoReset===!0&&this.info.reset(),Me===!0&&te.beginShadows();const st=O.state.shadowsArray;if(ee.render(st,R,Z),Me===!0&&te.endShadows(),(at&&L.hasRenderPass())===!1){const kt=I.opaque,Pt=I.transmissive;if(O.setupLights(),Z.isArrayCamera){const qt=Z.cameras;if(Pt.length>0)for(let Wt=0,ne=qt.length;Wt<ne;Wt++){const ge=qt[Wt];El(kt,Pt,R,ge)}Ce&&le.render(R);for(let Wt=0,ne=qt.length;Wt<ne;Wt++){const ge=qt[Wt];Ml(I,R,ge,ge.viewport)}}else Pt.length>0&&El(kt,Pt,R,Z),Ce&&le.render(R),Ml(I,R,Z)}et!==null&&H===0&&(ht.updateMultisampleRenderTarget(et),ht.updateRenderTargetMipmap(et)),at&&L.end(j),R.isScene===!0&&R.onAfterRender(j,R,Z),Ft.resetDefaultState(),pt=-1,Et=null,T.pop(),T.length>0?(O=T[T.length-1],ht.setTextureUnits(O.state.textureUnits),Me===!0&&te.setGlobalState(j.clippingPlanes,O.state.camera)):O=null,D.pop(),D.length>0?I=D[D.length-1]:I=null,J!==null&&J.renderEnd()};function gs(R,Z,ot,at){if(R.visible===!1)return;if(R.layers.test(Z.layers)){if(R.isGroup)ot=R.renderOrder;else if(R.isLOD)R.autoUpdate===!0&&R.update(Z);else if(R.isLightProbeGrid)O.pushLightProbeGrid(R);else if(R.isLight)O.pushLight(R),R.castShadow&&O.pushShadow(R);else if(R.isSprite){if(!R.frustumCulled||Se.intersectsSprite(R)){at&&oe.setFromMatrixPosition(R.matrixWorld).applyMatrix4(Zt);const kt=dt.update(R),Pt=R.material;Pt.visible&&I.push(R,kt,Pt,ot,oe.z,null)}}else if((R.isMesh||R.isLine||R.isPoints)&&(!R.frustumCulled||Se.intersectsObject(R))){const kt=dt.update(R),Pt=R.material;if(at&&(R.boundingSphere!==void 0?(R.boundingSphere===null&&R.computeBoundingSphere(),oe.copy(R.boundingSphere.center)):(kt.boundingSphere===null&&kt.computeBoundingSphere(),oe.copy(kt.boundingSphere.center)),oe.applyMatrix4(R.matrixWorld).applyMatrix4(Zt)),Array.isArray(Pt)){const qt=kt.groups;for(let Wt=0,ne=qt.length;Wt<ne;Wt++){const ge=qt[Wt],Jt=Pt[ge.materialIndex];Jt&&Jt.visible&&I.push(R,kt,Jt,ot,oe.z,ge)}}else Pt.visible&&I.push(R,kt,Pt,ot,oe.z,null)}}const Bt=R.children;for(let kt=0,Pt=Bt.length;kt<Pt;kt++)gs(Bt[kt],Z,ot,at)}function Ml(R,Z,ot,at){const{opaque:st,transmissive:Bt,transparent:kt}=R;O.setupLightsView(ot),Me===!0&&te.setGlobalState(j.clippingPlanes,ot),at&&b.viewport(N.copy(at)),st.length>0&&_s(st,Z,ot),Bt.length>0&&_s(Bt,Z,ot),kt.length>0&&_s(kt,Z,ot),b.buffers.depth.setTest(!0),b.buffers.depth.setMask(!0),b.buffers.color.setMask(!0),b.setPolygonOffset(!1)}function El(R,Z,ot,at){if((ot.isScene===!0?ot.overrideMaterial:null)!==null)return;if(O.state.transmissionRenderTarget[at.id]===void 0){const Jt=Le.has("EXT_color_buffer_half_float")||Le.has("EXT_color_buffer_float");O.state.transmissionRenderTarget[at.id]=new Qi(1,1,{generateMipmaps:!0,type:Jt?Ca:gi,minFilter:Xs,samples:Math.max(4,U.samples),stencilBuffer:c,resolveDepthBuffer:!1,resolveStencilBuffer:!1,colorSpace:Ne.workingColorSpace})}const Bt=O.state.transmissionRenderTarget[at.id],kt=at.viewport||N;Bt.setSize(kt.z*j.transmissionResolutionScale,kt.w*j.transmissionResolutionScale);const Pt=j.getRenderTarget(),qt=j.getActiveCubeFace(),Wt=j.getActiveMipmapLevel();j.setRenderTarget(Bt),j.getClearColor(Tt),At=j.getClearAlpha(),At<1&&j.setClearColor(16777215,.5),j.clear(),Ce&&le.render(ot);const ne=j.toneMapping;j.toneMapping=Ki;const ge=at.viewport;if(at.viewport!==void 0&&(at.viewport=void 0),O.setupLightsView(at),Me===!0&&te.setGlobalState(j.clippingPlanes,at),_s(R,ot,at),ht.updateMultisampleRenderTarget(Bt),ht.updateRenderTargetMipmap(Bt),Le.has("WEBGL_multisampled_render_to_texture")===!1){let Jt=!1;for(let Pe=0,sn=Z.length;Pe<sn;Pe++){const $e=Z[Pe],{object:ke,geometry:Xe,material:Gt,group:Bn}=$e;if(Gt.side===Ci&&ke.layers.test(at.layers)){const Ee=Gt.side;Gt.side=ti,Gt.needsUpdate=!0,Da(ke,ot,at,Xe,Gt,Bn),Gt.side=Ee,Gt.needsUpdate=!0,Jt=!0}}Jt===!0&&(ht.updateMultisampleRenderTarget(Bt),ht.updateRenderTargetMipmap(Bt))}j.setRenderTarget(Pt,qt,Wt),j.setClearColor(Tt,At),ge!==void 0&&(at.viewport=ge),j.toneMapping=ne}function _s(R,Z,ot){const at=Z.isScene===!0?Z.overrideMaterial:null;for(let st=0,Bt=R.length;st<Bt;st++){const kt=R[st],{object:Pt,geometry:qt,group:Wt}=kt;let ne=kt.material;ne.allowOverride===!0&&at!==null&&(ne=at),Pt.layers.test(ot.layers)&&Da(Pt,Z,ot,qt,ne,Wt)}}function Da(R,Z,ot,at,st,Bt){R.onBeforeRender(j,Z,ot,at,st,Bt),R.modelViewMatrix.multiplyMatrices(ot.matrixWorldInverse,R.matrixWorld),R.normalMatrix.getNormalMatrix(R.modelViewMatrix),st.onBeforeRender(j,Z,ot,at,R,Bt),st.transparent===!0&&st.side===Ci&&st.forceSinglePass===!1?(st.side=ti,st.needsUpdate=!0,j.renderBufferDirect(ot,Z,at,st,R,Bt),st.side=hs,st.needsUpdate=!0,j.renderBufferDirect(ot,Z,at,st,R,Bt),st.side=Ci):j.renderBufferDirect(ot,Z,at,st,R,Bt),R.onAfterRender(j,Z,ot,at,st,Bt)}function Ua(R,Z,ot){Z.isScene!==!0&&(Z=Vt);const at=rt.get(R),st=O.state.lights,Bt=O.state.shadowsArray,kt=st.state.version,Pt=wt.getParameters(R,st.state,Bt,Z,ot,O.state.lightProbeGridArray),qt=wt.getProgramCacheKey(Pt);let Wt=at.programs;at.environment=R.isMeshStandardMaterial||R.isMeshLambertMaterial||R.isMeshPhongMaterial?Z.environment:null,at.fog=Z.fog;const ne=R.isMeshStandardMaterial||R.isMeshLambertMaterial&&!R.envMap||R.isMeshPhongMaterial&&!R.envMap;at.envMap=Rt.get(R.envMap||at.environment,ne),at.envMapRotation=at.environment!==null&&R.envMap===null?Z.environmentRotation:R.envMapRotation,Wt===void 0&&(R.addEventListener("dispose",ni),Wt=new Map,at.programs=Wt);let ge=Wt.get(qt);if(ge!==void 0){if(at.currentProgram===ge&&at.lightsStateVersion===kt)return ea(R,Pt),ge}else Pt.uniforms=wt.getUniforms(R),J!==null&&R.isNodeMaterial&&J.build(R,ot,Pt),R.onBeforeCompile(Pt,j),ge=wt.acquireProgram(Pt,qt),Wt.set(qt,ge),at.uniforms=Pt.uniforms;const Jt=at.uniforms;return(!R.isShaderMaterial&&!R.isRawShaderMaterial||R.clipping===!0)&&(Jt.clippingPlanes=te.uniform),ea(R,Pt),at.needsLights=bl(R),at.lightsStateVersion=kt,at.needsLights&&(Jt.ambientLightColor.value=st.state.ambient,Jt.lightProbe.value=st.state.probe,Jt.directionalLights.value=st.state.directional,Jt.directionalLightShadows.value=st.state.directionalShadow,Jt.spotLights.value=st.state.spot,Jt.spotLightShadows.value=st.state.spotShadow,Jt.rectAreaLights.value=st.state.rectArea,Jt.ltc_1.value=st.state.rectAreaLTC1,Jt.ltc_2.value=st.state.rectAreaLTC2,Jt.pointLights.value=st.state.point,Jt.pointLightShadows.value=st.state.pointShadow,Jt.hemisphereLights.value=st.state.hemi,Jt.directionalShadowMatrix.value=st.state.directionalShadowMatrix,Jt.spotLightMatrix.value=st.state.spotLightMatrix,Jt.spotLightMap.value=st.state.spotLightMap,Jt.pointShadowMatrix.value=st.state.pointShadowMatrix),at.lightProbeGrid=O.state.lightProbeGridArray.length>0,at.currentProgram=ge,at.uniformsList=null,ge}function ta(R){if(R.uniformsList===null){const Z=R.currentProgram.getUniforms();R.uniformsList=gu.seqWithValue(Z.seq,R.uniforms)}return R.uniformsList}function ea(R,Z){const ot=rt.get(R);ot.outputColorSpace=Z.outputColorSpace,ot.batching=Z.batching,ot.batchingColor=Z.batchingColor,ot.instancing=Z.instancing,ot.instancingColor=Z.instancingColor,ot.instancingMorph=Z.instancingMorph,ot.skinning=Z.skinning,ot.morphTargets=Z.morphTargets,ot.morphNormals=Z.morphNormals,ot.morphColors=Z.morphColors,ot.morphTargetsCount=Z.morphTargetsCount,ot.numClippingPlanes=Z.numClippingPlanes,ot.numIntersection=Z.numClipIntersection,ot.vertexAlphas=Z.vertexAlphas,ot.vertexTangents=Z.vertexTangents,ot.toneMapping=Z.toneMapping}function vs(R,Z){if(R.length===0)return null;if(R.length===1)return R[0].texture!==null?R[0]:null;w.setFromMatrixPosition(Z.matrixWorld);for(let ot=0,at=R.length;ot<at;ot++){const st=R[ot];if(st.texture!==null&&st.boundingBox.containsPoint(w))return st}return null}function Na(R,Z,ot,at,st){Z.isScene!==!0&&(Z=Vt),ht.resetTextureUnits();const Bt=Z.fog,kt=at.isMeshStandardMaterial||at.isMeshLambertMaterial||at.isMeshPhongMaterial?Z.environment:null,Pt=et===null?j.outputColorSpace:et.isXRRenderTarget===!0?et.texture.colorSpace:Ne.workingColorSpace,qt=at.isMeshStandardMaterial||at.isMeshLambertMaterial&&!at.envMap||at.isMeshPhongMaterial&&!at.envMap,Wt=Rt.get(at.envMap||kt,qt),ne=at.vertexColors===!0&&!!ot.attributes.color&&ot.attributes.color.itemSize===4,ge=!!ot.attributes.tangent&&(!!at.normalMap||at.anisotropy>0),Jt=!!ot.morphAttributes.position,Pe=!!ot.morphAttributes.normal,sn=!!ot.morphAttributes.color;let $e=Ki;at.toneMapped&&(et===null||et.isXRRenderTarget===!0)&&($e=j.toneMapping);const ke=ot.morphAttributes.position||ot.morphAttributes.normal||ot.morphAttributes.color,Xe=ke!==void 0?ke.length:0,Gt=rt.get(at),Bn=O.state.lights;if(Me===!0&&(Ut===!0||R!==Et)){const Ge=R===Et&&at.id===pt;te.setState(at,R,Ge)}let Ee=!1;at.version===Gt.__version?(Gt.needsLights&&Gt.lightsStateVersion!==Bn.state.version||Gt.outputColorSpace!==Pt||st.isBatchedMesh&&Gt.batching===!1||!st.isBatchedMesh&&Gt.batching===!0||st.isBatchedMesh&&Gt.batchingColor===!0&&st.colorTexture===null||st.isBatchedMesh&&Gt.batchingColor===!1&&st.colorTexture!==null||st.isInstancedMesh&&Gt.instancing===!1||!st.isInstancedMesh&&Gt.instancing===!0||st.isSkinnedMesh&&Gt.skinning===!1||!st.isSkinnedMesh&&Gt.skinning===!0||st.isInstancedMesh&&Gt.instancingColor===!0&&st.instanceColor===null||st.isInstancedMesh&&Gt.instancingColor===!1&&st.instanceColor!==null||st.isInstancedMesh&&Gt.instancingMorph===!0&&st.morphTexture===null||st.isInstancedMesh&&Gt.instancingMorph===!1&&st.morphTexture!==null||Gt.envMap!==Wt||at.fog===!0&&Gt.fog!==Bt||Gt.numClippingPlanes!==void 0&&(Gt.numClippingPlanes!==te.numPlanes||Gt.numIntersection!==te.numIntersection)||Gt.vertexAlphas!==ne||Gt.vertexTangents!==ge||Gt.morphTargets!==Jt||Gt.morphNormals!==Pe||Gt.morphColors!==sn||Gt.toneMapping!==$e||Gt.morphTargetsCount!==Xe||!!Gt.lightProbeGrid!=O.state.lightProbeGridArray.length>0)&&(Ee=!0):(Ee=!0,Gt.__version=at.version);let bn=Gt.currentProgram;Ee===!0&&(bn=Ua(at,Z,st),J&&at.isNodeMaterial&&J.onUpdateProgram(at,bn,Gt));let ii=!1,wi=!1,ai=!1;const We=bn.getUniforms(),rn=Gt.uniforms;if(b.useProgram(bn.program)&&(ii=!0,wi=!0,ai=!0),at.id!==pt&&(pt=at.id,wi=!0),Gt.needsLights){const Ge=vs(O.state.lightProbeGridArray,st);Gt.lightProbeGrid!==Ge&&(Gt.lightProbeGrid=Ge,wi=!0)}if(ii||Et!==R){b.buffers.depth.getReversed()&&R.reversedDepth!==!0&&(R._reversedDepth=!0,R.updateProjectionMatrix()),We.setValue(Y,"projectionMatrix",R.projectionMatrix),We.setValue(Y,"viewMatrix",R.matrixWorldInverse);const Gi=We.map.cameraPosition;Gi!==void 0&&Gi.setValue(Y,$t.setFromMatrixPosition(R.matrixWorld)),U.logarithmicDepthBuffer&&We.setValue(Y,"logDepthBufFC",2/(Math.log(R.far+1)/Math.LN2)),(at.isMeshPhongMaterial||at.isMeshToonMaterial||at.isMeshLambertMaterial||at.isMeshBasicMaterial||at.isMeshStandardMaterial||at.isShaderMaterial)&&We.setValue(Y,"isOrthographic",R.isOrthographicCamera===!0),Et!==R&&(Et=R,wi=!0,ai=!0)}if(Gt.needsLights&&(Bn.state.directionalShadowMap.length>0&&We.setValue(Y,"directionalShadowMap",Bn.state.directionalShadowMap,ht),Bn.state.spotShadowMap.length>0&&We.setValue(Y,"spotShadowMap",Bn.state.spotShadowMap,ht),Bn.state.pointShadowMap.length>0&&We.setValue(Y,"pointShadowMap",Bn.state.pointShadowMap,ht)),st.isSkinnedMesh){We.setOptional(Y,st,"bindMatrix"),We.setOptional(Y,st,"bindMatrixInverse");const Ge=st.skeleton;Ge&&(Ge.boneTexture===null&&Ge.computeBoneTexture(),We.setValue(Y,"boneTexture",Ge.boneTexture,ht))}st.isBatchedMesh&&(We.setOptional(Y,st,"batchingTexture"),We.setValue(Y,"batchingTexture",st._matricesTexture,ht),We.setOptional(Y,st,"batchingIdTexture"),We.setValue(Y,"batchingIdTexture",st._indirectTexture,ht),We.setOptional(Y,st,"batchingColorTexture"),st._colorsTexture!==null&&We.setValue(Y,"batchingColorTexture",st._colorsTexture,ht));const Di=ot.morphAttributes;if((Di.position!==void 0||Di.normal!==void 0||Di.color!==void 0)&&q.update(st,ot,bn),(wi||Gt.receiveShadow!==st.receiveShadow)&&(Gt.receiveShadow=st.receiveShadow,We.setValue(Y,"receiveShadow",st.receiveShadow)),(at.isMeshStandardMaterial||at.isMeshLambertMaterial||at.isMeshPhongMaterial)&&at.envMap===null&&Z.environment!==null&&(rn.envMapIntensity.value=Z.environmentIntensity),rn.dfgLUT!==void 0&&(rn.dfgLUT.value=iR()),wi){if(We.setValue(Y,"toneMappingExposure",j.toneMappingExposure),Gt.needsLights&&gn(rn,ai),Bt&&at.fog===!0&&Ht.refreshFogUniforms(rn,Bt),Ht.refreshMaterialUniforms(rn,at,Mt,yt,O.state.transmissionRenderTarget[R.id]),Gt.needsLights&&Gt.lightProbeGrid){const Ge=Gt.lightProbeGrid;rn.probesSH.value=Ge.texture,rn.probesMin.value.copy(Ge.boundingBox.min),rn.probesMax.value.copy(Ge.boundingBox.max),rn.probesResolution.value.copy(Ge.resolution)}gu.upload(Y,ta(Gt),rn,ht)}if(at.isShaderMaterial&&at.uniformsNeedUpdate===!0&&(gu.upload(Y,ta(Gt),rn,ht),at.uniformsNeedUpdate=!1),at.isSpriteMaterial&&We.setValue(Y,"center",st.center),We.setValue(Y,"modelViewMatrix",st.modelViewMatrix),We.setValue(Y,"normalMatrix",st.normalMatrix),We.setValue(Y,"modelMatrix",st.matrixWorld),at.uniformsGroups!==void 0){const Ge=at.uniformsGroups;for(let Gi=0,La=Ge.length;Gi<La;Gi++){const xs=Ge[Gi];bt.update(xs,bn),bt.bind(xs,bn)}}return bn}function gn(R,Z){R.ambientLightColor.needsUpdate=Z,R.lightProbe.needsUpdate=Z,R.directionalLights.needsUpdate=Z,R.directionalLightShadows.needsUpdate=Z,R.pointLights.needsUpdate=Z,R.pointLightShadows.needsUpdate=Z,R.spotLights.needsUpdate=Z,R.spotLightShadows.needsUpdate=Z,R.rectAreaLights.needsUpdate=Z,R.hemisphereLights.needsUpdate=Z}function bl(R){return R.isMeshLambertMaterial||R.isMeshToonMaterial||R.isMeshPhongMaterial||R.isMeshStandardMaterial||R.isShadowMaterial||R.isShaderMaterial&&R.lights===!0}this.getActiveCubeFace=function(){return P},this.getActiveMipmapLevel=function(){return H},this.getRenderTarget=function(){return et},this.setRenderTargetTextures=function(R,Z,ot){const at=rt.get(R);at.__autoAllocateDepthBuffer=R.resolveDepthBuffer===!1,at.__autoAllocateDepthBuffer===!1&&(at.__useRenderToTexture=!1),rt.get(R.texture).__webglTexture=Z,rt.get(R.depthTexture).__webglTexture=at.__autoAllocateDepthBuffer?void 0:ot,at.__hasExternalTextures=!0},this.setRenderTargetFramebuffer=function(R,Z){const ot=rt.get(R);ot.__webglFramebuffer=Z,ot.__useDefaultFramebuffer=Z===void 0},this.setRenderTarget=function(R,Z=0,ot=0){et=R,P=Z,H=ot;let at=null,st=!1,Bt=!1;if(R){const Pt=rt.get(R);if(Pt.__useDefaultFramebuffer!==void 0){b.bindFramebuffer(Y.FRAMEBUFFER,Pt.__webglFramebuffer),N.copy(R.viewport),W.copy(R.scissor),vt=R.scissorTest,b.viewport(N),b.scissor(W),b.setScissorTest(vt),pt=-1;return}else if(Pt.__webglFramebuffer===void 0)ht.setupRenderTarget(R);else if(Pt.__hasExternalTextures)ht.rebindTextures(R,rt.get(R.texture).__webglTexture,rt.get(R.depthTexture).__webglTexture);else if(R.depthBuffer){const ne=R.depthTexture;if(Pt.__boundDepthTexture!==ne){if(ne!==null&&rt.has(ne)&&(R.width!==ne.image.width||R.height!==ne.image.height))throw new Error("THREE.WebGLRenderer: Attached DepthTexture is initialized to the incorrect size.");ht.setupDepthRenderbuffer(R)}}const qt=R.texture;(qt.isData3DTexture||qt.isDataArrayTexture||qt.isCompressedArrayTexture)&&(Bt=!0);const Wt=rt.get(R).__webglFramebuffer;R.isWebGLCubeRenderTarget?(Array.isArray(Wt[Z])?at=Wt[Z][ot]:at=Wt[Z],st=!0):R.samples>0&&ht.useMultisampledRTT(R)===!1?at=rt.get(R).__webglMultisampledFramebuffer:Array.isArray(Wt)?at=Wt[ot]:at=Wt,N.copy(R.viewport),W.copy(R.scissor),vt=R.scissorTest}else N.copy(Qt).multiplyScalar(Mt).floor(),W.copy(He).multiplyScalar(Mt).floor(),vt=pe;if(ot!==0&&(at=ut),b.bindFramebuffer(Y.FRAMEBUFFER,at)&&b.drawBuffers(R,at),b.viewport(N),b.scissor(W),b.setScissorTest(vt),st){const Pt=rt.get(R.texture);Y.framebufferTexture2D(Y.FRAMEBUFFER,Y.COLOR_ATTACHMENT0,Y.TEXTURE_CUBE_MAP_POSITIVE_X+Z,Pt.__webglTexture,ot)}else if(Bt){const Pt=Z;for(let qt=0;qt<R.textures.length;qt++){const Wt=rt.get(R.textures[qt]);Y.framebufferTextureLayer(Y.FRAMEBUFFER,Y.COLOR_ATTACHMENT0+qt,Wt.__webglTexture,ot,Pt)}}else if(R!==null&&ot!==0){const Pt=rt.get(R.texture);Y.framebufferTexture2D(Y.FRAMEBUFFER,Y.COLOR_ATTACHMENT0,Y.TEXTURE_2D,Pt.__webglTexture,ot)}pt=-1},this.readRenderTargetPixels=function(R,Z,ot,at,st,Bt,kt,Pt=0){if(!(R&&R.isWebGLRenderTarget)){Ue("WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");return}let qt=rt.get(R).__webglFramebuffer;if(R.isWebGLCubeRenderTarget&&kt!==void 0&&(qt=qt[kt]),qt){b.bindFramebuffer(Y.FRAMEBUFFER,qt);try{const Wt=R.textures[Pt],ne=Wt.format,ge=Wt.type;if(R.textures.length>1&&Y.readBuffer(Y.COLOR_ATTACHMENT0+Pt),!U.textureFormatReadable(ne)){Ue("WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");return}if(!U.textureTypeReadable(ge)){Ue("WebGLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");return}Z>=0&&Z<=R.width-at&&ot>=0&&ot<=R.height-st&&Y.readPixels(Z,ot,at,st,Dt.convert(ne),Dt.convert(ge),Bt)}finally{const Wt=et!==null?rt.get(et).__webglFramebuffer:null;b.bindFramebuffer(Y.FRAMEBUFFER,Wt)}}},this.readRenderTargetPixelsAsync=async function(R,Z,ot,at,st,Bt,kt,Pt=0){if(!(R&&R.isWebGLRenderTarget))throw new Error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");let qt=rt.get(R).__webglFramebuffer;if(R.isWebGLCubeRenderTarget&&kt!==void 0&&(qt=qt[kt]),qt)if(Z>=0&&Z<=R.width-at&&ot>=0&&ot<=R.height-st){b.bindFramebuffer(Y.FRAMEBUFFER,qt);const Wt=R.textures[Pt],ne=Wt.format,ge=Wt.type;if(R.textures.length>1&&Y.readBuffer(Y.COLOR_ATTACHMENT0+Pt),!U.textureFormatReadable(ne))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in RGBA or implementation defined format.");if(!U.textureTypeReadable(ge))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in UnsignedByteType or implementation defined type.");const Jt=Y.createBuffer();Y.bindBuffer(Y.PIXEL_PACK_BUFFER,Jt),Y.bufferData(Y.PIXEL_PACK_BUFFER,Bt.byteLength,Y.STREAM_READ),Y.readPixels(Z,ot,at,st,Dt.convert(ne),Dt.convert(ge),0);const Pe=et!==null?rt.get(et).__webglFramebuffer:null;b.bindFramebuffer(Y.FRAMEBUFFER,Pe);const sn=Y.fenceSync(Y.SYNC_GPU_COMMANDS_COMPLETE,0);return Y.flush(),await $1(Y,sn,4),Y.bindBuffer(Y.PIXEL_PACK_BUFFER,Jt),Y.getBufferSubData(Y.PIXEL_PACK_BUFFER,0,Bt),Y.deleteBuffer(Jt),Y.deleteSync(sn),Bt}else throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: requested read bounds are out of range.")},this.copyFramebufferToTexture=function(R,Z=null,ot=0){const at=Math.pow(2,-ot),st=Math.floor(R.image.width*at),Bt=Math.floor(R.image.height*at),kt=Z!==null?Z.x:0,Pt=Z!==null?Z.y:0;ht.setTexture2D(R,0),Y.copyTexSubImage2D(Y.TEXTURE_2D,ot,0,0,kt,Pt,st,Bt),b.unbindTexture()},this.copyTextureToTexture=function(R,Z,ot=null,at=null,st=0,Bt=0){let kt,Pt,qt,Wt,ne,ge,Jt,Pe,sn;const $e=R.isCompressedTexture?R.mipmaps[Bt]:R.image;if(ot!==null)kt=ot.max.x-ot.min.x,Pt=ot.max.y-ot.min.y,qt=ot.isBox3?ot.max.z-ot.min.z:1,Wt=ot.min.x,ne=ot.min.y,ge=ot.isBox3?ot.min.z:0;else{const rn=Math.pow(2,-st);kt=Math.floor($e.width*rn),Pt=Math.floor($e.height*rn),R.isDataArrayTexture?qt=$e.depth:R.isData3DTexture?qt=Math.floor($e.depth*rn):qt=1,Wt=0,ne=0,ge=0}at!==null?(Jt=at.x,Pe=at.y,sn=at.z):(Jt=0,Pe=0,sn=0);const ke=Dt.convert(Z.format),Xe=Dt.convert(Z.type);let Gt;Z.isData3DTexture?(ht.setTexture3D(Z,0),Gt=Y.TEXTURE_3D):Z.isDataArrayTexture||Z.isCompressedArrayTexture?(ht.setTexture2DArray(Z,0),Gt=Y.TEXTURE_2D_ARRAY):(ht.setTexture2D(Z,0),Gt=Y.TEXTURE_2D),b.activeTexture(Y.TEXTURE0),b.pixelStorei(Y.UNPACK_FLIP_Y_WEBGL,Z.flipY),b.pixelStorei(Y.UNPACK_PREMULTIPLY_ALPHA_WEBGL,Z.premultiplyAlpha),b.pixelStorei(Y.UNPACK_ALIGNMENT,Z.unpackAlignment);const Bn=b.getParameter(Y.UNPACK_ROW_LENGTH),Ee=b.getParameter(Y.UNPACK_IMAGE_HEIGHT),bn=b.getParameter(Y.UNPACK_SKIP_PIXELS),ii=b.getParameter(Y.UNPACK_SKIP_ROWS),wi=b.getParameter(Y.UNPACK_SKIP_IMAGES);b.pixelStorei(Y.UNPACK_ROW_LENGTH,$e.width),b.pixelStorei(Y.UNPACK_IMAGE_HEIGHT,$e.height),b.pixelStorei(Y.UNPACK_SKIP_PIXELS,Wt),b.pixelStorei(Y.UNPACK_SKIP_ROWS,ne),b.pixelStorei(Y.UNPACK_SKIP_IMAGES,ge);const ai=R.isDataArrayTexture||R.isData3DTexture,We=Z.isDataArrayTexture||Z.isData3DTexture;if(R.isDepthTexture){const rn=rt.get(R),Di=rt.get(Z),Ge=rt.get(rn.__renderTarget),Gi=rt.get(Di.__renderTarget);b.bindFramebuffer(Y.READ_FRAMEBUFFER,Ge.__webglFramebuffer),b.bindFramebuffer(Y.DRAW_FRAMEBUFFER,Gi.__webglFramebuffer);for(let La=0;La<qt;La++)ai&&(Y.framebufferTextureLayer(Y.READ_FRAMEBUFFER,Y.COLOR_ATTACHMENT0,rt.get(R).__webglTexture,st,ge+La),Y.framebufferTextureLayer(Y.DRAW_FRAMEBUFFER,Y.COLOR_ATTACHMENT0,rt.get(Z).__webglTexture,Bt,sn+La)),Y.blitFramebuffer(Wt,ne,kt,Pt,Jt,Pe,kt,Pt,Y.DEPTH_BUFFER_BIT,Y.NEAREST);b.bindFramebuffer(Y.READ_FRAMEBUFFER,null),b.bindFramebuffer(Y.DRAW_FRAMEBUFFER,null)}else if(st!==0||R.isRenderTargetTexture||rt.has(R)){const rn=rt.get(R),Di=rt.get(Z);b.bindFramebuffer(Y.READ_FRAMEBUFFER,gt),b.bindFramebuffer(Y.DRAW_FRAMEBUFFER,$);for(let Ge=0;Ge<qt;Ge++)ai?Y.framebufferTextureLayer(Y.READ_FRAMEBUFFER,Y.COLOR_ATTACHMENT0,rn.__webglTexture,st,ge+Ge):Y.framebufferTexture2D(Y.READ_FRAMEBUFFER,Y.COLOR_ATTACHMENT0,Y.TEXTURE_2D,rn.__webglTexture,st),We?Y.framebufferTextureLayer(Y.DRAW_FRAMEBUFFER,Y.COLOR_ATTACHMENT0,Di.__webglTexture,Bt,sn+Ge):Y.framebufferTexture2D(Y.DRAW_FRAMEBUFFER,Y.COLOR_ATTACHMENT0,Y.TEXTURE_2D,Di.__webglTexture,Bt),st!==0?Y.blitFramebuffer(Wt,ne,kt,Pt,Jt,Pe,kt,Pt,Y.COLOR_BUFFER_BIT,Y.NEAREST):We?Y.copyTexSubImage3D(Gt,Bt,Jt,Pe,sn+Ge,Wt,ne,kt,Pt):Y.copyTexSubImage2D(Gt,Bt,Jt,Pe,Wt,ne,kt,Pt);b.bindFramebuffer(Y.READ_FRAMEBUFFER,null),b.bindFramebuffer(Y.DRAW_FRAMEBUFFER,null)}else We?R.isDataTexture||R.isData3DTexture?Y.texSubImage3D(Gt,Bt,Jt,Pe,sn,kt,Pt,qt,ke,Xe,$e.data):Z.isCompressedArrayTexture?Y.compressedTexSubImage3D(Gt,Bt,Jt,Pe,sn,kt,Pt,qt,ke,$e.data):Y.texSubImage3D(Gt,Bt,Jt,Pe,sn,kt,Pt,qt,ke,Xe,$e):R.isDataTexture?Y.texSubImage2D(Y.TEXTURE_2D,Bt,Jt,Pe,kt,Pt,ke,Xe,$e.data):R.isCompressedTexture?Y.compressedTexSubImage2D(Y.TEXTURE_2D,Bt,Jt,Pe,$e.width,$e.height,ke,$e.data):Y.texSubImage2D(Y.TEXTURE_2D,Bt,Jt,Pe,kt,Pt,ke,Xe,$e);b.pixelStorei(Y.UNPACK_ROW_LENGTH,Bn),b.pixelStorei(Y.UNPACK_IMAGE_HEIGHT,Ee),b.pixelStorei(Y.UNPACK_SKIP_PIXELS,bn),b.pixelStorei(Y.UNPACK_SKIP_ROWS,ii),b.pixelStorei(Y.UNPACK_SKIP_IMAGES,wi),Bt===0&&Z.generateMipmaps&&Y.generateMipmap(Gt),b.unbindTexture()},this.initRenderTarget=function(R){rt.get(R).__webglFramebuffer===void 0&&ht.setupRenderTarget(R)},this.initTexture=function(R){R.isCubeTexture?ht.setTextureCube(R,0):R.isData3DTexture?ht.setTexture3D(R,0):R.isDataArrayTexture||R.isCompressedArrayTexture?ht.setTexture2DArray(R,0):ht.setTexture2D(R,0),b.unbindTexture()},this.resetState=function(){P=0,H=0,et=null,b.reset(),Ft.reset()},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}get coordinateSystem(){return ji}get outputColorSpace(){return this._outputColorSpace}set outputColorSpace(t){this._outputColorSpace=t;const i=this.getContext();i.drawingBufferColorSpace=Ne._getDrawingBufferColorSpace(t),i.unpackColorSpace=Ne._getUnpackColorSpace()}}const tx={angle:.14,elevation:12.6,distance:22};function sR(r,t,i,s){const l=document.createElement("canvas");l.width=512,l.height=256;const c=l.getContext("2d");c.clearRect(0,0,l.width,l.height),c.fillStyle="rgba(5, 12, 20, .82)",c.roundRect(72,32,368,172,18),c.fill(),c.strokeStyle=i,c.lineWidth=4,c.stroke(),c.fillStyle="#ffffff",c.textAlign="center",c.font="700 62px Microsoft YaHei, sans-serif",c.fillText(r,256,112),c.fillStyle="#aebdd0",c.font="500 35px Microsoft YaHei, sans-serif",c.fillText(t,256,166);const f=8,p=7,m=260,d=(m-p*(f-1))/f,_=(l.width-m)/2,v=Math.max(1,Math.round(Hs.clamp(s,0,1)*f));for(let A=0;A<f;A+=1){const M=_+A*(d+p);c.globalAlpha=A<v?.56:.16,c.fillStyle=A<v?i:"#7890a4",c.beginPath(),c.roundRect(M,184,d,6,3),c.fill()}c.globalAlpha=1;const g=new Ux(l);g.colorSpace=qn;const S=new Gp({map:g,transparent:!0,depthTest:!1}),E=new Ax(S);return E.scale.set(1.08,.54,1),E.position.set(0,.3,.47),E.renderOrder=8,E}function rR(r,t,i){const s=document.createElement("canvas");s.width=1024,s.height=256;const l=s.getContext("2d");l.clearRect(0,0,s.width,s.height),l.fillStyle="rgba(4, 11, 18, .9)",l.roundRect(24,24,976,208,18),l.fill(),l.strokeStyle=r.color,l.lineWidth=5,l.stroke(),l.fillStyle=r.color,l.font="700 72px Microsoft YaHei, sans-serif",l.fillText(`${r.id} 区`,66,117),l.fillStyle="#f1f6fb",l.font="700 52px Microsoft YaHei, sans-serif",l.fillText(r.department,278,108),l.fillStyle="#8ba0b4",l.font="500 35px Microsoft YaHei, sans-serif",l.fillText(`8 个柜体  ·  ${t}/${i} 盒`,278,174);const c=new Ux(s);c.colorSpace=qn;const f=new Ax(new Gp({map:c,transparent:!0,depthTest:!1}));return f.scale.set(3.25,.81,1),f.renderOrder=9,f}function oR(r){const t={2:[{x:-4.43,z:0,labelPosition:"rear"},{x:4.37,z:0,labelPosition:"rear"}],3:[{x:-4.43,z:-2.18,labelPosition:"rear"},{x:4.37,z:-2.18,labelPosition:"rear"},{x:0,z:2.08,labelPosition:"front"}],4:[{x:-4.43,z:-2.58,labelPosition:"rear"},{x:4.37,z:-2.58,labelPosition:"rear"},{x:-4.43,z:2.18,labelPosition:"front"},{x:4.37,z:2.18,labelPosition:"front"}]},i=t[r.length]||t[4];return Object.fromEntries(r.map((s,l)=>[s.id,i[l]||i.at(-1)]))}function lR(r,t,i,s){const f=[];return t.forEach(p=>{const m=s[p.id],d=new ln(new ms(6.55,3.25),new io({color:p.color,transparent:!0,opacity:.055,depthWrite:!1,side:Ci}));d.rotation.x=-Math.PI/2,d.position.set(m.x,.026,m.z),r.add(d);const _=[new V(-6.55/2,0,-3.25/2),new V(6.55/2,0,-3.25/2),new V(6.55/2,0,3.25/2),new V(-6.55/2,0,3.25/2)],v=new vp(new hn().setFromPoints(_),new fs({color:p.color,transparent:!0,opacity:.82}));v.position.set(m.x,.055,m.z),r.add(v);const g=new hn().setFromPoints([new V(-6.55/2,.02,-3.25/2+.68),new V(-6.55/2,.02,-3.25/2),new V(-6.55/2+.68,.02,-3.25/2),new V(6.55/2-.68,.02,-3.25/2),new V(6.55/2,.02,-3.25/2),new V(6.55/2,.02,-3.25/2+.68),new V(6.55/2,.02,3.25/2-.68),new V(6.55/2,.02,3.25/2),new V(6.55/2-.68,.02,3.25/2),new V(-6.55/2+.68,.02,3.25/2),new V(-6.55/2,.02,3.25/2),new V(-6.55/2,.02,3.25/2-.68)]),S=new Du(g,new fs({color:p.color,transparent:!0,opacity:1}));S.position.set(m.x,.07,m.z),r.add(S);const E=i.filter(F=>F.zone===p.id),A=E.reduce((F,w)=>F+w.used,0),M=E.reduce((F,w)=>F+w.capacity,0),y=rR(p,A,M),B=m.labelPosition==="rear";B||y.scale.multiplyScalar(.9),y.position.set(m.x-6.55/2+1.65,B?2.5:.36,B?m.z-3.25/2+.08:m.z+3.25/2-.08),r.add(y),f.push({borderMaterial:v.material,bracketMaterial:S.material,labelMaterial:y.material,platformMaterial:d.material})}),f}function cR(r,t){const i=new qs;i.userData={id:r.id,zone:r.zone,interactive:!0};const s=new pi({color:1121065,transparent:!0,opacity:1,depthWrite:!1,metalness:.78,roughness:.4,emissive:new ue(t.color),emissiveIntensity:.04}),l=[];[[-.5625,.94,0,.055,1.8,.88,s],[.5625,.94,0,.055,1.8,.88,s],[0,1.8125,-.07,1.18,.055,.74,s],[0,.0675,-.07,1.18,.055,.74,s],[0,.94,-.4125,1.07,1.69,.055,s]].forEach(([W,vt,Tt,At,tt,yt,Mt])=>{const Nt=new ln(new Dn(At,tt,yt),Mt);Nt.position.set(W,vt,Tt),Nt.userData={id:r.id,interactive:!0},Nt.castShadow=!0,Nt.receiveShadow=!0,i.add(Nt),l.push(Nt)});const c=new Dn(.7,.28,.025),f=new ln(c,s);f.position.set(0,1.56,-.2),f.castShadow=!0,f.receiveShadow=!0,i.add(f),l.push(f);const p=new fs({color:t.color,transparent:!0,opacity:.95}),m=new Dn(1.18,1.8,.88),d=new kE(m);m.dispose();const _=new Cx(d,p);_.position.set(0,.94,0),i.add(_);const v=new pi({color:1121065,transparent:!0,opacity:1,depthWrite:!1,emissive:new ue(t.color),emissiveIntensity:.035,metalness:.78,roughness:.4}),g=new pi({color:11123144,transparent:!0,opacity:1,depthWrite:!1,metalness:.95,roughness:.16}),S=new fs({color:t.color,transparent:!0,opacity:0,depthWrite:!1}),E=[];[{hinge:-.59,center:.295,handle:.515},{hinge:.59,center:-.295,handle:-.515}].forEach(({hinge:W,center:vt,handle:Tt})=>{const At=new qs;At.position.set(W,0,.46);const tt=new Dn(.54,1.46,.035),yt=new ln(tt,v);yt.position.set(vt,.9,.02),yt.userData={id:r.id,interactive:!0},At.add(yt);const Mt=new vp(new hn().setFromPoints([new V(-.27,-.73,0),new V(.27,-.73,0),new V(.27,.73,0),new V(-.27,.73,0)]),S);Mt.position.set(vt,.9,.039),At.add(Mt);const Nt=new Dn(.045,.28,.04),ae=new ln(Nt,g);ae.position.set(Tt,.92,.05),At.add(ae),i.add(At),E.push(At)});const A=new pi({color:1582648,emissive:528668,emissiveIntensity:.2,transparent:!0,opacity:.94,metalness:.72,roughness:.36}),M=new io({color:t.color,transparent:!0,opacity:.64,blending:Ys,depthWrite:!1});for(let W=0;W<4;W+=1){const vt=new ln(new Dn(1.03,.024,.78),A);vt.position.set(0,.19+W*.31,0),i.add(vt);const Tt=new ln(new Dn(1.01,.016,.026),M);Tt.position.set(0,.19+W*.31,.402),i.add(Tt)}const y=new pi({color:15331057,metalness:.04,roughness:.62}),B=new pi({color:4685771,emissive:1059672,emissiveIntensity:.18,metalness:.08,roughness:.48}),F=new pi({color:5609180,emissive:1193055,emissiveIntensity:.16,roughness:.5}),w=new pi({color:12042954,emissive:6252913,emissiveIntensity:.5,metalness:.96,roughness:.12}),I=new pi({color:16251386,emissive:9410718,emissiveIntensity:.58,metalness:.08,roughness:.4}),O=new qs;O.visible=!1;const D=32,T=new ul(new Dn(.094,.18,.21),y,D),L=new ul(new Dn(.096,.075,.214),B,D),j=new ul(new Dn(.042,.052,.008),F,D),k=new ul(new Wp(.0175,.0055,10,20),w,D),J=new ul(new kp(.012,16),I,D),ut=new Fe;let gt=0;for(let W=0;W<4;W+=1)for(let vt=0;vt<8;vt+=1){const Tt=-.392+vt*.112,At=.202+W*.31,tt=.255;ut.makeTranslation(Tt,At+.165,tt),T.setMatrixAt(gt,ut),ut.makeTranslation(Tt,At+.0375,tt),L.setMatrixAt(gt,ut),ut.makeTranslation(Tt,At+.198,tt+.109),j.setMatrixAt(gt,ut),ut.makeTranslation(Tt,At+.038,tt+.109),J.setMatrixAt(gt,ut),ut.makeTranslation(Tt,At+.038,tt+.112),k.setMatrixAt(gt,ut),gt+=1}[T,L,j,J,k].forEach(W=>{W.instanceMatrix.needsUpdate=!0,W.castShadow=!0,O.add(W)}),i.add(O);const $=new io({color:t.color,transparent:!0,opacity:0,depthWrite:!1,side:Ci,blending:Ys}),P=new ln(new ms(1.78,1.78),$);P.rotation.x=-Math.PI/2,P.position.set(0,.036,.18),P.renderOrder=2,i.add(P);const H=new fs({color:t.color,transparent:!0,opacity:0,blending:Ys,depthWrite:!1}),et=new vp(new hn().setFromPoints([new V(-.89,0,-.89),new V(.89,0,-.89),new V(.89,0,.89),new V(-.89,0,.89)]),H);et.position.set(0,.043,.18),et.renderOrder=3,i.add(et);const pt=r.used/r.capacity,Et=sR(r.id,`${r.used}/${r.capacity}`,t.color,pt);Et.position.y=1.02,i.add(Et);const N=new zx(t.color,0,3.4,2);return N.position.set(0,.18,.22),i.add(N),i.userData.parts={archiveGroup:O,bodyMaterial:s,doorEdgeMaterial:S,doorMaterial:v,doorPivots:E,edgeMaterial:p,focusPad:P,focusPadBorder:et,focusPadBorderMaterial:H,focusPadMaterial:$,handleMaterial:g,labelMaterial:Et.material,shellPanels:l,shelfLipMaterial:M,shelfMaterial:A,glow:N,utilization:pt,focusProgress:0,visibility:1},i}function uR(r){const t=new ln(new ms(17,11.5),new pi({color:857891,metalness:.58,roughness:.64}));t.rotation.x=-Math.PI/2,t.receiveShadow=!0,r.add(t);const i=new lb(17,34,1796538,1454414);i.position.y=.015,i.material.transparent=!0,i.material.opacity=.32,r.add(i);const s=new pi({color:1713717,metalness:.75,roughness:.38});[[0,.75,-5.65,17.2,1.5,.18],[-8.55,.75,0,.18,1.5,11.5],[8.55,.75,0,.18,1.5,11.5]].forEach(([f,p,m,d,_,v])=>{const g=new ln(new Dn(d,_,v),s);g.position.set(f,p,m),g.castShadow=!0,r.add(g)});const l=new ln(new Dn(3.4,2.4,.28),new pi({color:2503748,metalness:.8,roughness:.3}));l.position.set(0,1.2,-5.52),r.add(l);const c=new ln(new Dn(2.7,1.95,.08),new QE({color:859953,transparent:!0,opacity:.75,metalness:.55,roughness:.3}));c.position.set(0,1.05,-5.31),r.add(c)}function fR(r){const t=[];return[-.36,0,.36].forEach((i,s)=>{const l=new WE([new V(i,.045,5.6),new V(i,.045,1.4),new V(i*.35,.045,-1.2),new V(i*.2,.045,-5.2)]),c=new hn().setFromPoints(l.getPoints(80)),f=new tb({color:s===1?5431295:2522367,dashSize:.26,gapSize:.21,transparent:!0,opacity:s===1?.78:.36}),p=new Du(c,f);p.computeLineDistances(),r.add(p),t.push(f)}),t}const hR=ce.forwardRef(function({autoRotate:t,cabinets:i,focusId:s,onHover:l,onSelect:c,selectedId:f,zones:p},m){const d=ce.useRef(null),_=ce.useRef(null),v=ce.useRef(f),g=ce.useRef(t),S=ce.useRef(s);return ce.useEffect(()=>{v.current=f},[f]),ce.useEffect(()=>{g.current=t},[t]),ce.useEffect(()=>{S.current=s},[s]),ce.useImperativeHandle(m,()=>({zoomIn(){_.current&&(_.current.view.distance=Math.max(16,_.current.view.distance-1.8))},zoomOut(){_.current&&(_.current.view.distance=Math.min(29,_.current.view.distance+1.8))},reset(){_.current&&Object.assign(_.current.view,tx)}})),ce.useEffect(()=>{const E=d.current;if(!E)return;const A=new DE;A.fog=new Hp(462874,.035);const M=new mi(38,1,.1,100),y=new aR({antialias:!0,alpha:!0,powerPreference:"high-performance"});y.setPixelRatio(Math.min(window.devicePixelRatio,1.8)),y.outputColorSpace=qn,y.toneMapping=Rp,y.toneMappingExposure=1.15,y.shadowMap.enabled=!0,y.shadowMap.type=gl,E.appendChild(y.domElement),A.add(new eb(8239615,462872,1.8));const B=new ab(12179711,3.8);B.position.set(-6,12,8),B.castShadow=!0,B.shadow.mapSize.set(2048,2048),A.add(B);const F=new zx(2068991,16,18,2);F.position.set(0,3.5,2.8),A.add(F),uR(A);const w=oR(p),I=lR(A,p,i,w),O=fR(A),D=new Map;i.forEach(Ut=>{const Zt=p.find(Y=>Y.id===Ut.zone),$t=Number(Ut.id.slice(-2))-1,oe=Math.floor($t/4),Vt=$t%4,Ce=w[Ut.zone],me=cR(Ut,Zt);me.position.set(Ce.x-2.22+Vt*1.48,0,Ce.z-.625+oe*1.25),A.add(me),D.set(Ut.id,me)});const T=new io({color:3324415,transparent:!0,opacity:.15,side:Ci,blending:Ys,depthWrite:!1}),L=new ln(new ms(16.4,.45),T);L.rotation.x=-Math.PI/2,L.position.set(0,.09,-5.1),A.add(L);const j=150,k=new Float32Array(j*3);for(let Ut=0;Ut<j;Ut+=1)k[Ut*3]=(Math.random()-.5)*15.8,k[Ut*3+1]=.08+Math.random()*.18,k[Ut*3+2]=(Math.random()-.5)*10.2;const J=new hn;J.setAttribute("position",new vi(k,3));const ut=new GE(J,new wx({color:3712767,size:.045,transparent:!0,opacity:.65,blending:Ys,depthWrite:!1}));A.add(ut);const gt=new ob,$=new fe,P={...tx},H=new V(0,.45,0),et=new V,pt=new V(0,.45,0);let Et=null,N,W=!1,vt=!1,Tt=!1,At=1,tt={x:0,y:0};const yt=(Ut=!1)=>{const Zt=S.current?D.get(S.current):null;if(Zt){const oe=Zt.position.x<0?1:-1;et.set(Zt.position.x+oe*2.8,2.8,Zt.position.z+6.2),pt.set(Zt.position.x,.91,Zt.position.z)}else{const oe=Math.sqrt(Math.max(P.distance**2-P.elevation**2,36));et.set(Math.sin(P.angle)*oe,P.elevation,Math.cos(P.angle)*oe),pt.set(0,.45,0)}const $t=Ut?1:Zt?.072:.055;M.position.lerp(et,$t),H.lerp(pt,$t),M.fov=Hs.lerp(M.fov,Zt?34:38,Ut?1:.07),M.updateProjectionMatrix(),M.lookAt(H)},Mt=()=>{const Ut=Math.max(E.clientWidth,1),Zt=Math.max(E.clientHeight,1);y.setSize(Ut,Zt,!1),M.aspect=Ut/Zt,M.updateProjectionMatrix(),yt(!Tt),Tt=!0},Nt=Ut=>{const Zt=y.domElement.getBoundingClientRect();return $.x=(Ut.clientX-Zt.left)/Zt.width*2-1,$.y=-((Ut.clientY-Zt.top)/Zt.height)*2+1,gt.setFromCamera($,M),gt.intersectObjects(A.children,!0).filter($t=>{var oe;return(oe=$t.object.userData)==null?void 0:oe.interactive})},ae=Ut=>{if(S.current){E.style.cursor="default";return}if(W){const oe=Ut.clientX-tt.x,Vt=Ut.clientY-tt.y;Math.abs(oe)+Math.abs(Vt)>2&&(vt=!0),P.angle-=oe*.006,P.elevation=Hs.clamp(P.elevation+Vt*.035,6.8,14),tt={x:Ut.clientX,y:Ut.clientY};return}const Zt=Nt(Ut)[0],$t=(Zt==null?void 0:Zt.object.userData.id)||null;$t!==Et&&(Et=$t,E.style.cursor=$t?"pointer":"grab",l==null||l($t))},Qt=Ut=>{var Zt,$t;S.current||(W=!0,vt=!1,tt={x:Ut.clientX,y:Ut.clientY},E.style.cursor="grabbing",($t=(Zt=y.domElement).setPointerCapture)==null||$t.call(Zt,Ut.pointerId))},He=Ut=>{var Zt,$t;if(S.current){W=!1,E.style.cursor="default";return}if(W=!1,E.style.cursor=Et?"pointer":"grab",($t=(Zt=y.domElement).releasePointerCapture)==null||$t.call(Zt,Ut.pointerId),!vt){const oe=Nt(Ut)[0],Vt=oe==null?void 0:oe.object.userData.id;Vt&&(c==null||c(Vt))}},pe=Ut=>{Ut.preventDefault(),!S.current&&(P.distance=Hs.clamp(P.distance+Ut.deltaY*.008,16,29))};y.domElement.addEventListener("pointermove",ae),y.domElement.addEventListener("pointerdown",Qt),y.domElement.addEventListener("pointerup",He),y.domElement.addEventListener("pointerleave",He),y.domElement.addEventListener("wheel",pe,{passive:!1});const Se=new ResizeObserver(Mt);Se.observe(E),Mt(),_.current={view:P,cabinetGroups:D};const Me=Ut=>{const Zt=Ut*.001;g.current&&!W&&!S.current&&(P.angle+=55e-5),yt(),L.position.z=-5.1+Zt*1.05%10.2,T.opacity=.11+Math.sin(Zt*4.5)*.045,ut.rotation.y=Zt*.015,O.forEach(($t,oe)=>{$t.dashOffset=-(Zt*(.25+oe*.06))}),At=Hs.lerp(At,S.current?.1:1,.075),I.forEach($t=>{$t.platformMaterial.opacity=.055*At,$t.borderMaterial.opacity=.82*At,$t.bracketMaterial.opacity=At,$t.labelMaterial.opacity=S.current?0:At}),D.forEach(($t,oe)=>{const Vt=$t.userData.parts,Ce=oe===v.current,me=oe===S.current,Y=S.current&&!me?0:1;$t.visible=!S.current||me,Vt.visibility=Hs.lerp(Vt.visibility,Y,.075),Vt.focusProgress=Hs.lerp(Vt.focusProgress,me?1:0,me?.07:.1),Vt.doorPivots[0].rotation.y=-1.54*Vt.focusProgress,Vt.doorPivots[1].rotation.y=1.54*Vt.focusProgress,Vt.archiveGroup.visible=me;const we=Math.max(0,1-Vt.focusProgress)*Vt.visibility;Vt.bodyMaterial.opacity=we,Vt.bodyMaterial.emissiveIntensity=Ce?me?.1:.14:.04,Vt.doorMaterial.opacity=we,Vt.handleMaterial.opacity=we,Vt.doorEdgeMaterial.opacity=.88*Vt.focusProgress*Vt.visibility,Vt.shellPanels.forEach(Q=>{Q.castShadow=we>.05}),Vt.shelfMaterial.opacity=.94*Vt.visibility,Vt.shelfLipMaterial.opacity=.64*Vt.visibility,Vt.labelMaterial.opacity=S.current?0:Vt.visibility;const Le=.6+Math.sin(Zt*4+$t.position.x)*.35;Vt.edgeMaterial.opacity=(Vt.utilization>.93?Le:.95)*Vt.visibility,Ce?($t.scale.setScalar(1),Vt.glow.intensity=me?2.4:.65+Math.sin(Zt*3.8)*.18):($t.scale.setScalar(1),Vt.glow.intensity=0);const U=(Math.sin(Zt*2.5)+1)/2,b=me?.96+U*.08:1;Vt.focusPadMaterial.opacity=me?.12+U*.13:0,Vt.focusPadBorderMaterial.opacity=me?.42+U*.48:0,Vt.focusPad.scale.setScalar(b),Vt.focusPadBorder.scale.setScalar(b)}),y.render(A,M),N=requestAnimationFrame(Me)};return N=requestAnimationFrame(Me),()=>{cancelAnimationFrame(N),Se.disconnect(),y.domElement.removeEventListener("pointermove",ae),y.domElement.removeEventListener("pointerdown",Qt),y.domElement.removeEventListener("pointerup",He),y.domElement.removeEventListener("pointerleave",He),y.domElement.removeEventListener("wheel",pe),A.traverse(Ut=>{var Zt,$t,oe,Vt,Ce,me,Y;($t=(Zt=Ut.geometry)==null?void 0:Zt.dispose)==null||$t.call(Zt),Array.isArray(Ut.material)?Ut.material.forEach(we=>we.dispose()):((Ce=(Vt=(oe=Ut.material)==null?void 0:oe.map)==null?void 0:Vt.dispose)==null||Ce.call(Vt),(Y=(me=Ut.material)==null?void 0:me.dispose)==null||Y.call(me))}),y.dispose(),y.domElement.remove(),_.current=null}},[i,l,c,p]),z.jsx("div",{className:"archive-scene",ref:d,role:"img","aria-label":`${p.length}分区三维档案柜数字孪生场景`})}),ex=[{title:"中建三局混凝土抗压强度检测报告",category:"建筑工程",retention:"长期"},{title:"中铁建工钢筋拉伸试验报告",category:"建筑工程",retention:"长期"},{title:"城市快速路桩基检测归档资料",category:"市政工程",retention:"永久"},{title:"中建八局水泥安定性检测报告",category:"材料检测",retention:"长期"},{title:"跨江大桥钢结构焊缝检测报告",category:"桥梁工程",retention:"永久"},{title:"轨道交通区间沉降监测月报",category:"监测工程",retention:"长期"},{title:"基坑支护结构位移监测报告",category:"基础工程",retention:"长期"},{title:"水利枢纽闸门荷载试验记录",category:"水利工程",retention:"永久"},{title:"预应力混凝土构件检验批资料",category:"构件检测",retention:"长期"},{title:"工程质量监督抽检汇总报告",category:"质量监督",retention:"永久"}],Cd=[{id:"room-1",name:"1号档案室 · 主档案室",zoneIds:["A","B","C","D"],manager:{name:"李一凡",department:"信息技术部",phone:"13013013012"}},{id:"room-2",name:"2号档案室 · 工程档案室",zoneIds:["A","B"],manager:{name:"李一凡",department:"信息技术部",phone:"13013013012"}},{id:"room-3",name:"3号档案室 · 综合档案室",zoneIds:["A","B","C"],manager:{name:"李一凡",department:"信息技术部",phone:"13013013012"}}],Ep=[{id:"A",department:"材料检测部",color:"#2f8cff",soft:"#112b4d"},{id:"B",department:"监测与结构检测部",color:"#ffac21",soft:"#3b2b10"},{id:"C",department:"路桥与水利检测部",color:"#27c6d1",soft:"#10343a"},{id:"D",department:"基础检测部",color:"#9a6bff",soft:"#2b1d48"}],dR={A:[43,50,57,34,41,48,55,32],B:[46,53,30,37,44,51,28,35],C:[49,56,33,40,47,54,31,38],D:[52,29,34,39,48,57,31,40]};function pR(r,t,i){return Array.from({length:6},(s,l)=>{const c=ex[(r*3+t*2+l)%ex.length],f=l%4+1,p=(t*7+l*3)%18+1;return{...c,id:`DA-2026-${i}${String(t+1).padStart(2,"0")}-${String(l+1).padStart(3,"0")}`,location:`第${f}层 · ${String(p).padStart(2,"0")}号盒`,archivedAt:`2026-${String(t%6+1).padStart(2,"0")}-${String(10+l*2).padStart(2,"0")}`,pages:64+(t*29+l*47)%236}})}const mR=Ep.flatMap(r=>dR[r.id].map((t,i)=>{const s=Ep.findIndex(l=>l.id===r.id);return{id:`${r.id}-${String(i+1).padStart(2,"0")}`,zone:r.id,capacity:60,used:t,files:pR(s,i,r.id)}})),gR=[{title:"中建三局混凝土抗压强度检测报告",cabinet:"A-01",category:"建筑工程"},{title:"中建三局八局水泥安定性检测报告",cabinet:"A-03",category:"建筑工程"}],_R=[{time:"14:31",text:"A-03 柜体使用率超过90%",type:"alert"},{time:"14:28",text:"C-02 柜体使用率超过90%",type:"alert"},{time:"14:24",text:"D-06 柜体使用率超过90%",type:"alert"},{time:"14:20",text:"环境巡检",type:"ok",status:"正常"},{time:"14:18",text:"系统巡检",type:"ok",status:"正常"}];function vR(){const[r,t]=ce.useState(new Date(2026,6,30,14,32,8));return ce.useEffect(()=>{const i=window.setInterval(()=>t(s=>new Date(s.getTime()+1e3)),1e3);return()=>window.clearInterval(i)},[]),r}function xR(r,t=900){const[i,s]=ce.useState(0);return ce.useEffect(()=>{let l;const c=performance.now(),f=p=>{const m=Math.min((p-c)/t,1),d=1-Math.pow(1-m,3);s(Math.round(r*d)),m<1&&(l=requestAnimationFrame(f))};return l=requestAnimationFrame(f),()=>cancelAnimationFrame(l)},[t,r]),i}async function yR(r){var s;if((s=navigator.clipboard)!=null&&s.writeText){await navigator.clipboard.writeText(r);return}const t=document.createElement("textarea");t.value=r,t.setAttribute("readonly",""),t.style.position="fixed",t.style.opacity="0",document.body.appendChild(t),t.select();const i=document.execCommand("copy");if(t.remove(),!i)throw new Error("Copy failed")}function Kr({icon:r,label:t,value:i,suffix:s,tone:l="blue"}){const c=xR(i);return z.jsxs("div",{className:`metric metric--${l}`,children:[z.jsx("span",{className:"metric__icon",children:z.jsx(r,{size:19,strokeWidth:1.8})}),z.jsxs("span",{className:"metric__copy",children:[z.jsx("span",{children:t}),z.jsxs("strong",{children:[c.toLocaleString(),z.jsx("small",{children:s})]})]})]})}function Wx({value:r,color:t,compact:i=!1}){return z.jsx("span",{className:`capacity ${i?"capacity--compact":""}`,children:z.jsx("span",{style:{width:`${r}%`,backgroundColor:t}})})}function SR({cabinets:r,onSelect:t,selectedId:i,zones:s}){const[l,c]=ce.useState("中建三局"),[f,p]=ce.useState("中建三局"),m=r.find(E=>E.id===i)||r[0],d=s.find(E=>E.id===m.zone),_=Math.round(m.used/m.capacity*100),v=new Set(r.map(E=>E.id)),g=f.trim()?gR.filter(E=>v.has(E.cabinet)&&E.title.includes(f.trim())):[],S=E=>{E.preventDefault(),p(l)};return z.jsxs("aside",{className:"side-rail side-rail--left","aria-label":"文件检索与柜体详情",children:[z.jsxs("section",{className:"rail-section rail-section--search",children:[z.jsxs("div",{className:"section-heading",children:[z.jsx("span",{children:"文件定位"}),z.jsx(kM,{size:15})]}),z.jsxs("form",{className:"search-form",onSubmit:S,children:[z.jsxs("label",{className:"search-input",children:[z.jsx(i1,{size:15}),z.jsx("input",{"aria-label":"输入文件或项目名称",value:l,onChange:E=>c(E.target.value)})]}),z.jsx("button",{type:"submit",children:"搜索"})]}),z.jsxs("p",{className:"result-count",children:["检索结果（",g.length," 条）"]}),z.jsxs("div",{className:"result-list",children:[g.map(E=>z.jsxs("button",{className:`result-row ${i===E.cabinet?"is-active":""}`,onClick:()=>t(E.cabinet),type:"button",children:[z.jsx("span",{className:"result-row__title",children:E.title}),z.jsxs("span",{className:"result-row__meta",children:[z.jsxs("span",{style:{color:(d==null?void 0:d.color)||"#3b8dff"},children:[z.jsx(_u,{size:12}),E.cabinet]}),z.jsx("span",{children:E.category}),z.jsx("em",{children:"在档"})]}),z.jsx(TM,{size:14})]},E.title)),!g.length&&z.jsx("div",{className:"empty-result",children:"未找到匹配档案，请更换关键词"})]})]}),z.jsxs("section",{className:"rail-section rail-section--detail",children:[z.jsxs("div",{className:"section-heading",children:[z.jsx("span",{children:"柜体详情"}),z.jsx(_M,{size:15})]}),z.jsxs("div",{className:"cabinet-heading",children:[z.jsxs("div",{children:[z.jsx("strong",{style:{color:d.color},children:m.id}),z.jsx("span",{children:d.department})]}),z.jsxs("span",{className:"online-state",children:[z.jsx("i",{}),"在线"]})]}),z.jsxs("div",{className:"detail-stats",children:[z.jsxs("div",{children:[z.jsx("span",{children:"容量"}),z.jsxs("strong",{children:[m.capacity,z.jsx("small",{children:"盒"})]})]}),z.jsxs("div",{children:[z.jsx("span",{children:"已用"}),z.jsxs("strong",{children:[m.used,z.jsx("small",{children:"盒"})]})]}),z.jsxs("div",{children:[z.jsx("span",{children:"使用率"}),z.jsxs("strong",{className:_>=85?"is-alert":"",children:[_,"%"]})]})]}),z.jsx(Wx,{value:_,color:_>=85?"#ff5664":d.color}),z.jsxs("div",{className:"file-list-heading",children:[z.jsx("span",{children:"柜内文件"}),z.jsx("small",{children:"最近更新"})]}),z.jsx("div",{className:"file-list",children:m.files.slice(0,2).map(E=>z.jsxs("button",{type:"button",children:[z.jsx("span",{className:"file-icon",children:z.jsx(ax,{size:15})}),z.jsxs("span",{children:[z.jsx("strong",{children:E.title}),z.jsxs("small",{children:[E.location," · ",E.category]})]}),z.jsx("em",{children:"在档"})]},E.id))})]})]})}function MR({percent:r,used:t,total:i}){return z.jsxs("div",{className:"ring-wrap",children:[z.jsx("div",{className:"utilization-ring",style:{"--progress":`${r*3.6}deg`},children:z.jsxs("div",{children:[z.jsxs("strong",{children:[r,"%"]}),z.jsxs("span",{children:[t.toLocaleString()," / ",i.toLocaleString()]})]})}),z.jsxs("div",{className:"ring-legend",children:[z.jsxs("span",{children:[z.jsx("i",{className:"is-used"})," 已用",z.jsxs("strong",{children:[t.toLocaleString()," 盒"]})]}),z.jsxs("span",{children:[z.jsx("i",{})," 剩余",z.jsxs("strong",{children:[(i-t).toLocaleString()," 盒"]})]})]})]})}function ER(){return z.jsx("div",{className:"environment-wave","aria-label":"环境指标近30分钟趋势",children:Array.from({length:30},(r,t)=>z.jsx("i",{style:{height:`${18+Math.sin(t*.75)*9+t%4*2}px`,animationDelay:`${t*-55}ms`}},t))})}function bR({activities:r,cabinets:t,total:i,used:s,zones:l}){const c=Math.round(s/i*100);return z.jsxs("aside",{className:"side-rail side-rail--right","aria-label":"档案室统计",children:[z.jsxs("section",{className:"rail-section rail-section--usage",children:[z.jsxs("div",{className:"section-heading",children:[z.jsx("span",{children:"总体利用率"}),z.jsx(Tp,{size:15})]}),z.jsx(MR,{percent:c,used:s,total:i}),z.jsx("div",{className:"zone-heading",children:"分区利用率"}),z.jsx("div",{className:"zone-bars",children:l.map(f=>{const p=t.filter(v=>v.zone===f.id),m=p.reduce((v,g)=>v+g.used,0),d=p.reduce((v,g)=>v+g.capacity,0),_=Math.round(m/d*100);return z.jsxs("div",{className:"zone-bar",children:[z.jsxs("div",{children:[z.jsxs("span",{style:{color:f.color},children:[f.id,"区 · ",f.department]}),z.jsxs("strong",{children:[_,"%"]})]}),z.jsx(Wx,{value:_,color:f.color,compact:!0})]},f.id)})})]}),z.jsxs("section",{className:"rail-section rail-section--environment",children:[z.jsxs("div",{className:"section-heading",children:[z.jsx("span",{children:"环境监测"}),z.jsx(ix,{size:15})]}),z.jsxs("div",{className:"environment-grid",children:[z.jsxs("div",{children:[z.jsx(c1,{size:18}),z.jsxs("span",{children:["温度",z.jsxs("strong",{children:["22.6",z.jsx("small",{children:"°C"})]})]})]}),z.jsxs("div",{children:[z.jsx(UM,{size:18}),z.jsxs("span",{children:["湿度",z.jsxs("strong",{children:["48",z.jsx("small",{children:"%RH"})]})]})]}),z.jsxs("div",{children:[z.jsx(GM,{size:18}),z.jsxs("span",{children:["空气质量",z.jsx("strong",{className:"air-quality",children:"优"})]})]})]}),z.jsx(ER,{})]}),z.jsxs("section",{className:"rail-section rail-section--activity",children:[z.jsxs("div",{className:"section-heading",children:[z.jsx("span",{children:"近期动态"}),z.jsx(RM,{size:15})]}),z.jsx("div",{className:"activity-list",children:r.map(f=>z.jsxs("div",{className:`activity-row activity-row--${f.type}`,children:[z.jsx("i",{}),z.jsx("time",{children:f.time}),z.jsx("span",{children:f.text}),f.status&&z.jsx("em",{children:f.status})]},`${f.time}-${f.text}`))})]})]})}function TR({cabinet:r,onClose:t,zone:i}){const s=Math.round(r.used/r.capacity*100);return z.jsxs("section",{className:"cabinet-inspection",style:{"--zone-color":i.color},"aria-label":`${r.id}柜体档案清单`,children:[z.jsx("div",{className:"inspection-accent"}),z.jsxs("div",{className:"inspection-heading",children:[z.jsx("button",{type:"button",onClick:t,title:"返回库区全景","aria-label":"返回库区全景",children:z.jsx(mM,{size:16})}),z.jsxs("div",{children:[z.jsxs("span",{children:["柜体近景 · ",i.id,"区"]}),z.jsx("strong",{children:r.id})]}),z.jsxs("em",{children:[z.jsx(WM,{size:13}),"柜门已解锁"]})]}),z.jsxs("div",{className:"inspection-summary",children:[z.jsxs("div",{children:[z.jsx(FM,{size:16}),z.jsx("span",{children:"4 层"})]}),z.jsxs("div",{children:[z.jsx(_u,{size:16}),z.jsxs("span",{children:[r.used," / ",r.capacity," 盒"]})]}),z.jsxs("div",{children:[z.jsx(Tp,{size:16}),z.jsxs("span",{children:[s,"%"]})]})]}),z.jsxs("div",{className:"inspection-list-heading",children:[z.jsx("span",{children:"柜内档案"}),z.jsxs("small",{children:["测试数据 · ",r.files.length," 份"]})]}),z.jsx("div",{className:"inspection-files",children:r.files.map((l,c)=>z.jsxs("button",{type:"button",children:[z.jsx("span",{className:"inspection-file-index",children:String(c+1).padStart(2,"0")}),z.jsxs("span",{className:"inspection-file-copy",children:[z.jsx("strong",{children:l.title}),z.jsxs("small",{children:[z.jsx(LM,{size:11}),l.id]})]}),z.jsxs("span",{className:"inspection-file-meta",children:[z.jsx("span",{children:l.location}),z.jsxs("small",{children:[z.jsx(SM,{size:10}),l.archivedAt]})]})]},l.id))}),z.jsxs("div",{className:"inspection-footer",children:[z.jsxs("span",{children:[z.jsx("i",{})," 镜头已锁定当前柜体"]}),z.jsxs("span",{children:[r.files.reduce((l,c)=>l+c.pages,0)," 页"]})]})]})}function AR({autoRotate:r,inspecting:t,onReset:i,sceneRef:s,setAutoRotate:l}){return z.jsxs("div",{className:"scene-toolbar","aria-label":"三维场景控制",children:[z.jsx("button",{className:r?"is-active":"","aria-label":t?"近景模式下暂停旋转":r?"暂停自动旋转":"开启自动旋转",disabled:t,onClick:()=>l(c=>!c),title:t?"近景模式下暂停旋转":r?"暂停自动旋转":"开启自动旋转",type:"button",children:r?z.jsx(jM,{size:15}):z.jsx(QM,{size:15})}),z.jsx("button",{"aria-label":"放大场景",disabled:t,onClick:()=>{var c;return(c=s.current)==null?void 0:c.zoomIn()},title:"放大场景",type:"button",children:z.jsx(g1,{size:15})}),z.jsx("button",{"aria-label":"缩小场景",disabled:t,onClick:()=>{var c;return(c=s.current)==null?void 0:c.zoomOut()},title:"缩小场景",type:"button",children:z.jsx(v1,{size:15})}),z.jsx("button",{"aria-label":"重置视角",onClick:i,title:"重置视角",type:"button",children:z.jsx(e1,{size:15})})]})}function RR({cabinets:r,focusId:t,onExitFocus:i,onSelect:s,selectedId:l,zones:c}){const f=ce.useRef(null),[p,m]=ce.useState(!0),[d,_]=ce.useState(null),v=r.find(A=>A.id===t),g=c.find(A=>A.id===(v==null?void 0:v.zone));ce.useEffect(()=>{t&&m(!1)},[t]);const S=ce.useCallback(A=>{m(!1),s(A)},[s]),E=ce.useCallback(()=>{var A;i(),(A=f.current)==null||A.reset()},[i]);return z.jsxs("main",{className:"scene-panel",children:[z.jsxs("div",{className:"scene-panel__header",children:[z.jsxs("div",{children:[z.jsx("span",{children:"数字孪生库区"}),z.jsx("small",{children:t?`正在查看 ${t} · 柜门已打开`:"点击柜体进入近景 · 拖拽调整视角 · 滚轮缩放"})]}),z.jsxs("div",{className:"scene-status",children:[d&&z.jsxs("span",{children:["当前指向 ",d]}),z.jsxs("span",{children:[z.jsx("i",{})," WebGL实时渲染"]})]}),z.jsx(AR,{sceneRef:f,autoRotate:p,inspecting:!!t,onReset:E,setAutoRotate:m})]}),z.jsxs("div",{className:`scene-stage ${t?"is-inspecting":""}`,children:[z.jsx(hR,{ref:f,autoRotate:p,cabinets:r,focusId:t,onHover:_,onSelect:S,selectedId:l,zones:c}),z.jsxs("div",{className:"scene-corners","aria-hidden":"true",children:[z.jsx("i",{}),z.jsx("i",{}),z.jsx("i",{}),z.jsx("i",{})]}),z.jsxs("div",{className:"scan-readout",children:[z.jsx(ix,{size:13}),t?"柜内扫描":"空间扫描",z.jsx("strong",{children:t?"已定位":"运行中"})]}),v&&z.jsx(TR,{cabinet:v,onClose:E,zone:g}),z.jsxs("div",{className:"scene-legend",children:[c.map(A=>z.jsxs("span",{children:[z.jsx("i",{style:{backgroundColor:A.color}}),A.id,"区 · ",A.department]},A.id)),z.jsxs("span",{children:[z.jsx(sx,{size:13})," 高占用柜体"]})]})]})]})}function CR(){const[r,t]=ce.useState("room-1"),[i,s]=ce.useState("A-01"),[l,c]=ce.useState(null),[f,p]=ce.useState("idle"),m=ce.useRef(null),d=vR(),_=ce.useMemo(()=>Cd.find(D=>D.id===r)||Cd[0],[r]),v=ce.useMemo(()=>Ep.filter(D=>_.zoneIds.includes(D.id)),[_]),g=ce.useMemo(()=>mR.filter(D=>_.zoneIds.includes(D.zone)),[_]),S=ce.useMemo(()=>_R.filter(D=>D.type!=="alert"||_.zoneIds.includes(D.text.charAt(0))),[_]),E=g.reduce((D,T)=>D+T.capacity,0),A=g.reduce((D,T)=>D+T.used,0),M=Math.round(A/E*100),y=g.filter(D=>D.used/D.capacity>.93).length,B=ce.useMemo(()=>new Intl.DateTimeFormat("zh-CN",{year:"numeric",month:"long",day:"numeric",weekday:"long"}).format(d),[d]),F=async()=>{var D,T,L;document.fullscreenElement?await((L=document.exitFullscreen)==null?void 0:L.call(document)):await((T=(D=document.documentElement).requestFullscreen)==null?void 0:T.call(D))},w=ce.useCallback(async()=>{window.clearTimeout(m.current);try{await yR(_.manager.phone),p("copied")}catch{p("failed")}m.current=window.setTimeout(()=>p("idle"),1800)},[_.manager.phone]);ce.useEffect(()=>()=>window.clearTimeout(m.current),[]);const I=ce.useCallback(D=>{s(D),c(D)},[]),O=ce.useCallback(()=>{c(null)},[]);return z.jsxs("div",{className:"dashboard-shell",children:[z.jsx("div",{className:"ambient-grid","aria-hidden":"true"}),z.jsxs("header",{className:"command-header",children:[z.jsxs("div",{className:"brand-block",children:[z.jsx("div",{className:"brand-mark",children:z.jsx(_u,{size:25})}),z.jsxs("div",{children:[z.jsx("h1",{children:"档案室管理大屏"}),z.jsxs("p",{children:[_.name," · 信息总部"]})]})]}),z.jsxs("div",{className:"top-metrics",children:[z.jsx(Kr,{icon:BM,label:"分区",value:v.length,suffix:"个"}),z.jsx(Kr,{icon:xM,label:"柜体",value:g.length,suffix:"个",tone:"cyan"}),z.jsx(Kr,{icon:_u,label:"总容量",value:E,suffix:"盒",tone:"cyan"}),z.jsx(Kr,{icon:ax,label:"已用",value:A,suffix:"盒",tone:"amber"}),z.jsx(Kr,{icon:Tp,label:"利用率",value:M,suffix:"%",tone:"amber"}),z.jsx(Kr,{icon:sx,label:"异常",value:y,suffix:"",tone:"coral"})]}),z.jsxs("div",{className:"system-tools",children:[z.jsxs("div",{className:"system-state",children:[z.jsxs("span",{children:[z.jsx("i",{})," 系统在线"]}),z.jsx("strong",{children:d.toLocaleTimeString("zh-CN",{hour12:!1})}),z.jsx("small",{children:B})]}),z.jsx("button",{type:"button",title:"进入全屏","aria-label":"进入全屏",onClick:F,children:z.jsx(YM,{size:17})}),z.jsx("button",{type:"button",title:"系统设置","aria-label":"系统设置",children:z.jsx(s1,{size:17})})]})]}),z.jsxs("nav",{className:"room-nav","aria-label":"档案室切换",children:[z.jsx("span",{children:"切换档案室"}),Cd.map(D=>z.jsx("button",{className:r===D.id?"is-active":"",onClick:()=>{t(D.id),s(`${D.zoneIds[0]}-01`),c(null)},type:"button",children:D.name},D.id)),z.jsxs("div",{className:"manager-info","aria-label":"档案室管理员信息",children:[z.jsxs("span",{className:"manager-info__role",children:[z.jsx(h1,{size:13}),"档案室管理员"]}),z.jsx("strong",{children:_.manager.name}),z.jsx("span",{children:_.manager.department}),z.jsxs("button",{"aria-label":`复制管理员电话 ${_.manager.phone}`,className:`manager-info__copy ${f==="idle"?"":`is-${f}`}`,onClick:w,title:f==="copied"?"电话已复制":f==="failed"?"复制失败":"复制管理员电话",type:"button",children:[f==="copied"?z.jsx(EM,{size:12}):z.jsx(wM,{size:12}),z.jsx("span",{"aria-live":"polite",children:f==="copied"?"已复制":f==="failed"?"复制失败":_.manager.phone})]})]}),z.jsxs("div",{className:"security-note",children:[z.jsx(o1,{size:14}),"今日巡检已完成"]})]}),z.jsxs("div",{className:"dashboard-grid",children:[z.jsx(SR,{cabinets:g,selectedId:i,onSelect:I,zones:v}),z.jsx(RR,{cabinets:g,focusId:l,onExitFocus:O,onSelect:I,selectedId:i,zones:v},r),z.jsx(bR,{activities:S,cabinets:g,used:A,total:E,zones:v})]}),z.jsxs("footer",{className:"status-footer",children:[z.jsxs("span",{children:[z.jsx("i",{})," 档案库环境稳定"]}),z.jsx("span",{children:"数据更新时间 14:32:08"}),z.jsxs("span",{children:[z.jsx(p1,{size:12})," 温湿度传感器 8 / 8 在线"]}),z.jsxs("span",{children:[z.jsx($M,{size:12})," 数字孪生引擎 v3.2"]})]})]})}sM.createRoot(document.getElementById("root")).render(z.jsx(JS.StrictMode,{children:z.jsx(CR,{})}));

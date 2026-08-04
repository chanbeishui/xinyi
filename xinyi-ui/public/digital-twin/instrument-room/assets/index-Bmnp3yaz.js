(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const l of document.querySelectorAll('link[rel="modulepreload"]'))r(l);new MutationObserver(l=>{for(const c of l)if(c.type==="childList")for(const f of c.addedNodes)f.tagName==="LINK"&&f.rel==="modulepreload"&&r(f)}).observe(document,{childList:!0,subtree:!0});function i(l){const c={};return l.integrity&&(c.integrity=l.integrity),l.referrerPolicy&&(c.referrerPolicy=l.referrerPolicy),l.crossOrigin==="use-credentials"?c.credentials="include":l.crossOrigin==="anonymous"?c.credentials="omit":c.credentials="same-origin",c}function r(l){if(l.ep)return;l.ep=!0;const c=i(l);fetch(l.href,c)}})();function LM(s){return s&&s.__esModule&&Object.prototype.hasOwnProperty.call(s,"default")?s.default:s}var Cd={exports:{}},Qo={};/**
 * @license React
 * react-jsx-runtime.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var F_;function OM(){if(F_)return Qo;F_=1;var s=Symbol.for("react.transitional.element"),t=Symbol.for("react.fragment");function i(r,l,c){var f=null;if(c!==void 0&&(f=""+c),l.key!==void 0&&(f=""+l.key),"key"in l){c={};for(var h in l)h!=="key"&&(c[h]=l[h])}else c=l;return l=c.ref,{$$typeof:s,type:r,key:f,ref:l!==void 0?l:null,props:c}}return Qo.Fragment=t,Qo.jsx=i,Qo.jsxs=i,Qo}var H_;function PM(){return H_||(H_=1,Cd.exports=OM()),Cd.exports}var H=PM(),wd={exports:{}},le={};/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var G_;function BM(){if(G_)return le;G_=1;var s=Symbol.for("react.transitional.element"),t=Symbol.for("react.portal"),i=Symbol.for("react.fragment"),r=Symbol.for("react.strict_mode"),l=Symbol.for("react.profiler"),c=Symbol.for("react.consumer"),f=Symbol.for("react.context"),h=Symbol.for("react.forward_ref"),m=Symbol.for("react.suspense"),p=Symbol.for("react.memo"),_=Symbol.for("react.lazy"),v=Symbol.for("react.activity"),g=Symbol.iterator;function x(L){return L===null||typeof L!="object"?null:(L=g&&L[g]||L["@@iterator"],typeof L=="function"?L:null)}var M={isMounted:function(){return!1},enqueueForceUpdate:function(){},enqueueReplaceState:function(){},enqueueSetState:function(){}},A=Object.assign,E={};function S(L,Y,yt){this.props=L,this.context=Y,this.refs=E,this.updater=yt||M}S.prototype.isReactComponent={},S.prototype.setState=function(L,Y){if(typeof L!="object"&&typeof L!="function"&&L!=null)throw Error("takes an object of state variables to update or a function which returns an object of state variables.");this.updater.enqueueSetState(this,L,Y,"setState")},S.prototype.forceUpdate=function(L){this.updater.enqueueForceUpdate(this,L,"forceUpdate")};function P(){}P.prototype=S.prototype;function O(L,Y,yt){this.props=L,this.context=Y,this.refs=E,this.updater=yt||M}var C=O.prototype=new P;C.constructor=O,A(C,S.prototype),C.isPureReactComponent=!0;var z=Array.isArray;function D(){}var U={H:null,A:null,T:null,S:null},b=Object.prototype.hasOwnProperty;function N(L,Y,yt){var At=yt.ref;return{$$typeof:s,type:L,key:Y,ref:At!==void 0?At:null,props:yt}}function G(L,Y){return N(L.type,Y,L.props)}function F(L){return typeof L=="object"&&L!==null&&L.$$typeof===s}function k(L){var Y={"=":"=0",":":"=2"};return"$"+L.replace(/[=:]/g,function(yt){return Y[yt]})}var K=/\/+/g;function tt(L,Y){return typeof L=="object"&&L!==null&&L.key!=null?k(""+L.key):Y.toString(36)}function $(L){switch(L.status){case"fulfilled":return L.value;case"rejected":throw L.reason;default:switch(typeof L.status=="string"?L.then(D,D):(L.status="pending",L.then(function(Y){L.status==="pending"&&(L.status="fulfilled",L.value=Y)},function(Y){L.status==="pending"&&(L.status="rejected",L.reason=Y)})),L.status){case"fulfilled":return L.value;case"rejected":throw L.reason}}throw L}function I(L,Y,yt,At,Dt){var it=typeof L;(it==="undefined"||it==="boolean")&&(L=null);var nt=!1;if(L===null)nt=!0;else switch(it){case"bigint":case"string":case"number":nt=!0;break;case"object":switch(L.$$typeof){case s:case t:nt=!0;break;case _:return nt=L._init,I(nt(L._payload),Y,yt,At,Dt)}}if(nt)return Dt=Dt(L),nt=At===""?"."+tt(L,0):At,z(Dt)?(yt="",nt!=null&&(yt=nt.replace(K,"$&/")+"/"),I(Dt,Y,yt,"",function(Ht){return Ht})):Dt!=null&&(F(Dt)&&(Dt=G(Dt,yt+(Dt.key==null||L&&L.key===Dt.key?"":(""+Dt.key).replace(K,"$&/")+"/")+nt)),Y.push(Dt)),1;nt=0;var ct=At===""?".":At+":";if(z(L))for(var bt=0;bt<L.length;bt++)At=L[bt],it=ct+tt(At,bt),nt+=I(At,Y,yt,it,Dt);else if(bt=x(L),typeof bt=="function")for(L=bt.call(L),bt=0;!(At=L.next()).done;)At=At.value,it=ct+tt(At,bt++),nt+=I(At,Y,yt,it,Dt);else if(it==="object"){if(typeof L.then=="function")return I($(L),Y,yt,At,Dt);throw Y=String(L),Error("Objects are not valid as a React child (found: "+(Y==="[object Object]"?"object with keys {"+Object.keys(L).join(", ")+"}":Y)+"). If you meant to render a collection of children, use an array instead.")}return nt}function X(L,Y,yt){if(L==null)return L;var At=[],Dt=0;return I(L,At,"","",function(it){return Y.call(yt,it,Dt++)}),At}function at(L){if(L._status===-1){var Y=L._result;Y=Y(),Y.then(function(yt){(L._status===0||L._status===-1)&&(L._status=1,L._result=yt)},function(yt){(L._status===0||L._status===-1)&&(L._status=2,L._result=yt)}),L._status===-1&&(L._status=0,L._result=Y)}if(L._status===1)return L._result.default;throw L._result}var _t=typeof reportError=="function"?reportError:function(L){if(typeof window=="object"&&typeof window.ErrorEvent=="function"){var Y=new window.ErrorEvent("error",{bubbles:!0,cancelable:!0,message:typeof L=="object"&&L!==null&&typeof L.message=="string"?String(L.message):String(L),error:L});if(!window.dispatchEvent(Y))return}else if(typeof process=="object"&&typeof process.emit=="function"){process.emit("uncaughtException",L);return}console.error(L)},vt={map:X,forEach:function(L,Y,yt){X(L,function(){Y.apply(this,arguments)},yt)},count:function(L){var Y=0;return X(L,function(){Y++}),Y},toArray:function(L){return X(L,function(Y){return Y})||[]},only:function(L){if(!F(L))throw Error("React.Children.only expected to receive a single React element child.");return L}};return le.Activity=v,le.Children=vt,le.Component=S,le.Fragment=i,le.Profiler=l,le.PureComponent=O,le.StrictMode=r,le.Suspense=m,le.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE=U,le.__COMPILER_RUNTIME={__proto__:null,c:function(L){return U.H.useMemoCache(L)}},le.cache=function(L){return function(){return L.apply(null,arguments)}},le.cacheSignal=function(){return null},le.cloneElement=function(L,Y,yt){if(L==null)throw Error("The argument must be a React element, but you passed "+L+".");var At=A({},L.props),Dt=L.key;if(Y!=null)for(it in Y.key!==void 0&&(Dt=""+Y.key),Y)!b.call(Y,it)||it==="key"||it==="__self"||it==="__source"||it==="ref"&&Y.ref===void 0||(At[it]=Y[it]);var it=arguments.length-2;if(it===1)At.children=yt;else if(1<it){for(var nt=Array(it),ct=0;ct<it;ct++)nt[ct]=arguments[ct+2];At.children=nt}return N(L.type,Dt,At)},le.createContext=function(L){return L={$$typeof:f,_currentValue:L,_currentValue2:L,_threadCount:0,Provider:null,Consumer:null},L.Provider=L,L.Consumer={$$typeof:c,_context:L},L},le.createElement=function(L,Y,yt){var At,Dt={},it=null;if(Y!=null)for(At in Y.key!==void 0&&(it=""+Y.key),Y)b.call(Y,At)&&At!=="key"&&At!=="__self"&&At!=="__source"&&(Dt[At]=Y[At]);var nt=arguments.length-2;if(nt===1)Dt.children=yt;else if(1<nt){for(var ct=Array(nt),bt=0;bt<nt;bt++)ct[bt]=arguments[bt+2];Dt.children=ct}if(L&&L.defaultProps)for(At in nt=L.defaultProps,nt)Dt[At]===void 0&&(Dt[At]=nt[At]);return N(L,it,Dt)},le.createRef=function(){return{current:null}},le.forwardRef=function(L){return{$$typeof:h,render:L}},le.isValidElement=F,le.lazy=function(L){return{$$typeof:_,_payload:{_status:-1,_result:L},_init:at}},le.memo=function(L,Y){return{$$typeof:p,type:L,compare:Y===void 0?null:Y}},le.startTransition=function(L){var Y=U.T,yt={};U.T=yt;try{var At=L(),Dt=U.S;Dt!==null&&Dt(yt,At),typeof At=="object"&&At!==null&&typeof At.then=="function"&&At.then(D,_t)}catch(it){_t(it)}finally{Y!==null&&yt.types!==null&&(Y.types=yt.types),U.T=Y}},le.unstable_useCacheRefresh=function(){return U.H.useCacheRefresh()},le.use=function(L){return U.H.use(L)},le.useActionState=function(L,Y,yt){return U.H.useActionState(L,Y,yt)},le.useCallback=function(L,Y){return U.H.useCallback(L,Y)},le.useContext=function(L){return U.H.useContext(L)},le.useDebugValue=function(){},le.useDeferredValue=function(L,Y){return U.H.useDeferredValue(L,Y)},le.useEffect=function(L,Y){return U.H.useEffect(L,Y)},le.useEffectEvent=function(L){return U.H.useEffectEvent(L)},le.useId=function(){return U.H.useId()},le.useImperativeHandle=function(L,Y,yt){return U.H.useImperativeHandle(L,Y,yt)},le.useInsertionEffect=function(L,Y){return U.H.useInsertionEffect(L,Y)},le.useLayoutEffect=function(L,Y){return U.H.useLayoutEffect(L,Y)},le.useMemo=function(L,Y){return U.H.useMemo(L,Y)},le.useOptimistic=function(L,Y){return U.H.useOptimistic(L,Y)},le.useReducer=function(L,Y,yt){return U.H.useReducer(L,Y,yt)},le.useRef=function(L){return U.H.useRef(L)},le.useState=function(L){return U.H.useState(L)},le.useSyncExternalStore=function(L,Y,yt){return U.H.useSyncExternalStore(L,Y,yt)},le.useTransition=function(){return U.H.useTransition()},le.version="19.2.0",le}var V_;function Op(){return V_||(V_=1,wd.exports=BM()),wd.exports}var rn=Op(),Dd={exports:{}},Jo={},Nd={exports:{}},Ud={};/**
 * @license React
 * scheduler.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var k_;function IM(){return k_||(k_=1,(function(s){function t(I,X){var at=I.length;I.push(X);t:for(;0<at;){var _t=at-1>>>1,vt=I[_t];if(0<l(vt,X))I[_t]=X,I[at]=vt,at=_t;else break t}}function i(I){return I.length===0?null:I[0]}function r(I){if(I.length===0)return null;var X=I[0],at=I.pop();if(at!==X){I[0]=at;t:for(var _t=0,vt=I.length,L=vt>>>1;_t<L;){var Y=2*(_t+1)-1,yt=I[Y],At=Y+1,Dt=I[At];if(0>l(yt,at))At<vt&&0>l(Dt,yt)?(I[_t]=Dt,I[At]=at,_t=At):(I[_t]=yt,I[Y]=at,_t=Y);else if(At<vt&&0>l(Dt,at))I[_t]=Dt,I[At]=at,_t=At;else break t}}return X}function l(I,X){var at=I.sortIndex-X.sortIndex;return at!==0?at:I.id-X.id}if(s.unstable_now=void 0,typeof performance=="object"&&typeof performance.now=="function"){var c=performance;s.unstable_now=function(){return c.now()}}else{var f=Date,h=f.now();s.unstable_now=function(){return f.now()-h}}var m=[],p=[],_=1,v=null,g=3,x=!1,M=!1,A=!1,E=!1,S=typeof setTimeout=="function"?setTimeout:null,P=typeof clearTimeout=="function"?clearTimeout:null,O=typeof setImmediate<"u"?setImmediate:null;function C(I){for(var X=i(p);X!==null;){if(X.callback===null)r(p);else if(X.startTime<=I)r(p),X.sortIndex=X.expirationTime,t(m,X);else break;X=i(p)}}function z(I){if(A=!1,C(I),!M)if(i(m)!==null)M=!0,D||(D=!0,k());else{var X=i(p);X!==null&&$(z,X.startTime-I)}}var D=!1,U=-1,b=5,N=-1;function G(){return E?!0:!(s.unstable_now()-N<b)}function F(){if(E=!1,D){var I=s.unstable_now();N=I;var X=!0;try{t:{M=!1,A&&(A=!1,P(U),U=-1),x=!0;var at=g;try{e:{for(C(I),v=i(m);v!==null&&!(v.expirationTime>I&&G());){var _t=v.callback;if(typeof _t=="function"){v.callback=null,g=v.priorityLevel;var vt=_t(v.expirationTime<=I);if(I=s.unstable_now(),typeof vt=="function"){v.callback=vt,C(I),X=!0;break e}v===i(m)&&r(m),C(I)}else r(m);v=i(m)}if(v!==null)X=!0;else{var L=i(p);L!==null&&$(z,L.startTime-I),X=!1}}break t}finally{v=null,g=at,x=!1}X=void 0}}finally{X?k():D=!1}}}var k;if(typeof O=="function")k=function(){O(F)};else if(typeof MessageChannel<"u"){var K=new MessageChannel,tt=K.port2;K.port1.onmessage=F,k=function(){tt.postMessage(null)}}else k=function(){S(F,0)};function $(I,X){U=S(function(){I(s.unstable_now())},X)}s.unstable_IdlePriority=5,s.unstable_ImmediatePriority=1,s.unstable_LowPriority=4,s.unstable_NormalPriority=3,s.unstable_Profiling=null,s.unstable_UserBlockingPriority=2,s.unstable_cancelCallback=function(I){I.callback=null},s.unstable_forceFrameRate=function(I){0>I||125<I?console.error("forceFrameRate takes a positive int between 0 and 125, forcing frame rates higher than 125 fps is not supported"):b=0<I?Math.floor(1e3/I):5},s.unstable_getCurrentPriorityLevel=function(){return g},s.unstable_next=function(I){switch(g){case 1:case 2:case 3:var X=3;break;default:X=g}var at=g;g=X;try{return I()}finally{g=at}},s.unstable_requestPaint=function(){E=!0},s.unstable_runWithPriority=function(I,X){switch(I){case 1:case 2:case 3:case 4:case 5:break;default:I=3}var at=g;g=I;try{return X()}finally{g=at}},s.unstable_scheduleCallback=function(I,X,at){var _t=s.unstable_now();switch(typeof at=="object"&&at!==null?(at=at.delay,at=typeof at=="number"&&0<at?_t+at:_t):at=_t,I){case 1:var vt=-1;break;case 2:vt=250;break;case 5:vt=1073741823;break;case 4:vt=1e4;break;default:vt=5e3}return vt=at+vt,I={id:_++,callback:X,priorityLevel:I,startTime:at,expirationTime:vt,sortIndex:-1},at>_t?(I.sortIndex=at,t(p,I),i(m)===null&&I===i(p)&&(A?(P(U),U=-1):A=!0,$(z,at-_t))):(I.sortIndex=vt,t(m,I),M||x||(M=!0,D||(D=!0,k()))),I},s.unstable_shouldYield=G,s.unstable_wrapCallback=function(I){var X=g;return function(){var at=g;g=X;try{return I.apply(this,arguments)}finally{g=at}}}})(Ud)),Ud}var X_;function zM(){return X_||(X_=1,Nd.exports=IM()),Nd.exports}var Ld={exports:{}},zn={};/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var q_;function FM(){if(q_)return zn;q_=1;var s=Op();function t(m){var p="https://react.dev/errors/"+m;if(1<arguments.length){p+="?args[]="+encodeURIComponent(arguments[1]);for(var _=2;_<arguments.length;_++)p+="&args[]="+encodeURIComponent(arguments[_])}return"Minified React error #"+m+"; visit "+p+" for the full message or use the non-minified dev environment for full errors and additional helpful warnings."}function i(){}var r={d:{f:i,r:function(){throw Error(t(522))},D:i,C:i,L:i,m:i,X:i,S:i,M:i},p:0,findDOMNode:null},l=Symbol.for("react.portal");function c(m,p,_){var v=3<arguments.length&&arguments[3]!==void 0?arguments[3]:null;return{$$typeof:l,key:v==null?null:""+v,children:m,containerInfo:p,implementation:_}}var f=s.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;function h(m,p){if(m==="font")return"";if(typeof p=="string")return p==="use-credentials"?p:""}return zn.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE=r,zn.createPortal=function(m,p){var _=2<arguments.length&&arguments[2]!==void 0?arguments[2]:null;if(!p||p.nodeType!==1&&p.nodeType!==9&&p.nodeType!==11)throw Error(t(299));return c(m,p,null,_)},zn.flushSync=function(m){var p=f.T,_=r.p;try{if(f.T=null,r.p=2,m)return m()}finally{f.T=p,r.p=_,r.d.f()}},zn.preconnect=function(m,p){typeof m=="string"&&(p?(p=p.crossOrigin,p=typeof p=="string"?p==="use-credentials"?p:"":void 0):p=null,r.d.C(m,p))},zn.prefetchDNS=function(m){typeof m=="string"&&r.d.D(m)},zn.preinit=function(m,p){if(typeof m=="string"&&p&&typeof p.as=="string"){var _=p.as,v=h(_,p.crossOrigin),g=typeof p.integrity=="string"?p.integrity:void 0,x=typeof p.fetchPriority=="string"?p.fetchPriority:void 0;_==="style"?r.d.S(m,typeof p.precedence=="string"?p.precedence:void 0,{crossOrigin:v,integrity:g,fetchPriority:x}):_==="script"&&r.d.X(m,{crossOrigin:v,integrity:g,fetchPriority:x,nonce:typeof p.nonce=="string"?p.nonce:void 0})}},zn.preinitModule=function(m,p){if(typeof m=="string")if(typeof p=="object"&&p!==null){if(p.as==null||p.as==="script"){var _=h(p.as,p.crossOrigin);r.d.M(m,{crossOrigin:_,integrity:typeof p.integrity=="string"?p.integrity:void 0,nonce:typeof p.nonce=="string"?p.nonce:void 0})}}else p==null&&r.d.M(m)},zn.preload=function(m,p){if(typeof m=="string"&&typeof p=="object"&&p!==null&&typeof p.as=="string"){var _=p.as,v=h(_,p.crossOrigin);r.d.L(m,_,{crossOrigin:v,integrity:typeof p.integrity=="string"?p.integrity:void 0,nonce:typeof p.nonce=="string"?p.nonce:void 0,type:typeof p.type=="string"?p.type:void 0,fetchPriority:typeof p.fetchPriority=="string"?p.fetchPriority:void 0,referrerPolicy:typeof p.referrerPolicy=="string"?p.referrerPolicy:void 0,imageSrcSet:typeof p.imageSrcSet=="string"?p.imageSrcSet:void 0,imageSizes:typeof p.imageSizes=="string"?p.imageSizes:void 0,media:typeof p.media=="string"?p.media:void 0})}},zn.preloadModule=function(m,p){if(typeof m=="string")if(p){var _=h(p.as,p.crossOrigin);r.d.m(m,{as:typeof p.as=="string"&&p.as!=="script"?p.as:void 0,crossOrigin:_,integrity:typeof p.integrity=="string"?p.integrity:void 0})}else r.d.m(m)},zn.requestFormReset=function(m){r.d.r(m)},zn.unstable_batchedUpdates=function(m,p){return m(p)},zn.useFormState=function(m,p,_){return f.H.useFormState(m,p,_)},zn.useFormStatus=function(){return f.H.useHostTransitionStatus()},zn.version="19.2.0",zn}var W_;function HM(){if(W_)return Ld.exports;W_=1;function s(){if(!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__>"u"||typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE!="function"))try{__REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(s)}catch(t){console.error(t)}}return s(),Ld.exports=FM(),Ld.exports}/**
 * @license React
 * react-dom-client.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */var Y_;function GM(){if(Y_)return Jo;Y_=1;var s=zM(),t=Op(),i=HM();function r(e){var n="https://react.dev/errors/"+e;if(1<arguments.length){n+="?args[]="+encodeURIComponent(arguments[1]);for(var a=2;a<arguments.length;a++)n+="&args[]="+encodeURIComponent(arguments[a])}return"Minified React error #"+e+"; visit "+n+" for the full message or use the non-minified dev environment for full errors and additional helpful warnings."}function l(e){return!(!e||e.nodeType!==1&&e.nodeType!==9&&e.nodeType!==11)}function c(e){var n=e,a=e;if(e.alternate)for(;n.return;)n=n.return;else{e=n;do n=e,(n.flags&4098)!==0&&(a=n.return),e=n.return;while(e)}return n.tag===3?a:null}function f(e){if(e.tag===13){var n=e.memoizedState;if(n===null&&(e=e.alternate,e!==null&&(n=e.memoizedState)),n!==null)return n.dehydrated}return null}function h(e){if(e.tag===31){var n=e.memoizedState;if(n===null&&(e=e.alternate,e!==null&&(n=e.memoizedState)),n!==null)return n.dehydrated}return null}function m(e){if(c(e)!==e)throw Error(r(188))}function p(e){var n=e.alternate;if(!n){if(n=c(e),n===null)throw Error(r(188));return n!==e?null:e}for(var a=e,o=n;;){var u=a.return;if(u===null)break;var d=u.alternate;if(d===null){if(o=u.return,o!==null){a=o;continue}break}if(u.child===d.child){for(d=u.child;d;){if(d===a)return m(u),e;if(d===o)return m(u),n;d=d.sibling}throw Error(r(188))}if(a.return!==o.return)a=u,o=d;else{for(var y=!1,w=u.child;w;){if(w===a){y=!0,a=u,o=d;break}if(w===o){y=!0,o=u,a=d;break}w=w.sibling}if(!y){for(w=d.child;w;){if(w===a){y=!0,a=d,o=u;break}if(w===o){y=!0,o=d,a=u;break}w=w.sibling}if(!y)throw Error(r(189))}}if(a.alternate!==o)throw Error(r(190))}if(a.tag!==3)throw Error(r(188));return a.stateNode.current===a?e:n}function _(e){var n=e.tag;if(n===5||n===26||n===27||n===6)return e;for(e=e.child;e!==null;){if(n=_(e),n!==null)return n;e=e.sibling}return null}var v=Object.assign,g=Symbol.for("react.element"),x=Symbol.for("react.transitional.element"),M=Symbol.for("react.portal"),A=Symbol.for("react.fragment"),E=Symbol.for("react.strict_mode"),S=Symbol.for("react.profiler"),P=Symbol.for("react.consumer"),O=Symbol.for("react.context"),C=Symbol.for("react.forward_ref"),z=Symbol.for("react.suspense"),D=Symbol.for("react.suspense_list"),U=Symbol.for("react.memo"),b=Symbol.for("react.lazy"),N=Symbol.for("react.activity"),G=Symbol.for("react.memo_cache_sentinel"),F=Symbol.iterator;function k(e){return e===null||typeof e!="object"?null:(e=F&&e[F]||e["@@iterator"],typeof e=="function"?e:null)}var K=Symbol.for("react.client.reference");function tt(e){if(e==null)return null;if(typeof e=="function")return e.$$typeof===K?null:e.displayName||e.name||null;if(typeof e=="string")return e;switch(e){case A:return"Fragment";case S:return"Profiler";case E:return"StrictMode";case z:return"Suspense";case D:return"SuspenseList";case N:return"Activity"}if(typeof e=="object")switch(e.$$typeof){case M:return"Portal";case O:return e.displayName||"Context";case P:return(e._context.displayName||"Context")+".Consumer";case C:var n=e.render;return e=e.displayName,e||(e=n.displayName||n.name||"",e=e!==""?"ForwardRef("+e+")":"ForwardRef"),e;case U:return n=e.displayName||null,n!==null?n:tt(e.type)||"Memo";case b:n=e._payload,e=e._init;try{return tt(e(n))}catch{}}return null}var $=Array.isArray,I=t.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,X=i.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,at={pending:!1,data:null,method:null,action:null},_t=[],vt=-1;function L(e){return{current:e}}function Y(e){0>vt||(e.current=_t[vt],_t[vt]=null,vt--)}function yt(e,n){vt++,_t[vt]=e.current,e.current=n}var At=L(null),Dt=L(null),it=L(null),nt=L(null);function ct(e,n){switch(yt(it,n),yt(Dt,e),yt(At,null),n.nodeType){case 9:case 11:e=(e=n.documentElement)&&(e=e.namespaceURI)?l_(e):0;break;default:if(e=n.tagName,n=n.namespaceURI)n=l_(n),e=c_(n,e);else switch(e){case"svg":e=1;break;case"math":e=2;break;default:e=0}}Y(At),yt(At,e)}function bt(){Y(At),Y(Dt),Y(it)}function Ht(e){e.memoizedState!==null&&yt(nt,e);var n=At.current,a=c_(n,e.type);n!==a&&(yt(Dt,e),yt(At,a))}function Bt(e){Dt.current===e&&(Y(At),Y(Dt)),nt.current===e&&(Y(nt),Yo._currentValue=at)}var he,ne;function ie(e){if(he===void 0)try{throw Error()}catch(a){var n=a.stack.trim().match(/\n( *(at )?)/);he=n&&n[1]||"",ne=-1<a.stack.indexOf(`
    at`)?" (<anonymous>)":-1<a.stack.indexOf("@")?"@unknown:0:0":""}return`
`+he+e+ne}var Xt=!1;function ae(e,n){if(!e||Xt)return"";Xt=!0;var a=Error.prepareStackTrace;Error.prepareStackTrace=void 0;try{var o={DetermineComponentFrameRoot:function(){try{if(n){var Et=function(){throw Error()};if(Object.defineProperty(Et.prototype,"props",{set:function(){throw Error()}}),typeof Reflect=="object"&&Reflect.construct){try{Reflect.construct(Et,[])}catch(ht){var dt=ht}Reflect.construct(e,[],Et)}else{try{Et.call()}catch(ht){dt=ht}e.call(Et.prototype)}}else{try{throw Error()}catch(ht){dt=ht}(Et=e())&&typeof Et.catch=="function"&&Et.catch(function(){})}}catch(ht){if(ht&&dt&&typeof ht.stack=="string")return[ht.stack,dt.stack]}return[null,null]}};o.DetermineComponentFrameRoot.displayName="DetermineComponentFrameRoot";var u=Object.getOwnPropertyDescriptor(o.DetermineComponentFrameRoot,"name");u&&u.configurable&&Object.defineProperty(o.DetermineComponentFrameRoot,"name",{value:"DetermineComponentFrameRoot"});var d=o.DetermineComponentFrameRoot(),y=d[0],w=d[1];if(y&&w){var V=y.split(`
`),st=w.split(`
`);for(u=o=0;o<V.length&&!V[o].includes("DetermineComponentFrameRoot");)o++;for(;u<st.length&&!st[u].includes("DetermineComponentFrameRoot");)u++;if(o===V.length||u===st.length)for(o=V.length-1,u=st.length-1;1<=o&&0<=u&&V[o]!==st[u];)u--;for(;1<=o&&0<=u;o--,u--)if(V[o]!==st[u]){if(o!==1||u!==1)do if(o--,u--,0>u||V[o]!==st[u]){var xt=`
`+V[o].replace(" at new "," at ");return e.displayName&&xt.includes("<anonymous>")&&(xt=xt.replace("<anonymous>",e.displayName)),xt}while(1<=o&&0<=u);break}}}finally{Xt=!1,Error.prepareStackTrace=a}return(a=e?e.displayName||e.name:"")?ie(a):""}function ge(e,n){switch(e.tag){case 26:case 27:case 5:return ie(e.type);case 16:return ie("Lazy");case 13:return e.child!==n&&n!==null?ie("Suspense Fallback"):ie("Suspense");case 19:return ie("SuspenseList");case 0:case 15:return ae(e.type,!1);case 11:return ae(e.type.render,!1);case 1:return ae(e.type,!0);case 31:return ie("Activity");default:return""}}function _e(e){try{var n="",a=null;do n+=ge(e,a),a=e,e=e.return;while(e);return n}catch(o){return`
Error generating stack: `+o.message+`
`+o.stack}}var Ce=Object.prototype.hasOwnProperty,Ke=s.unstable_scheduleCallback,Ue=s.unstable_cancelCallback,on=s.unstable_shouldYield,j=s.unstable_requestPaint,ke=s.unstable_now,Le=s.unstable_getCurrentPriorityLevel,B=s.unstable_ImmediatePriority,T=s.unstable_UserBlockingPriority,et=s.unstable_NormalPriority,ut=s.unstable_LowPriority,mt=s.unstable_IdlePriority,Rt=s.log,Ut=s.unstable_setDisableYieldValue,pt=null,gt=null;function wt(e){if(typeof Rt=="function"&&Ut(e),gt&&typeof gt.setStrictMode=="function")try{gt.setStrictMode(pt,e)}catch{}}var Gt=Math.clz32?Math.clz32:Jt,Pt=Math.log,Lt=Math.LN2;function Jt(e){return e>>>=0,e===0?32:31-(Pt(e)/Lt|0)|0}var $t=256,oe=262144,W=4194304;function Ct(e){var n=e&42;if(n!==0)return n;switch(e&-e){case 1:return 1;case 2:return 2;case 4:return 4;case 8:return 8;case 16:return 16;case 32:return 32;case 64:return 64;case 128:return 128;case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:return e&261888;case 262144:case 524288:case 1048576:case 2097152:return e&3932160;case 4194304:case 8388608:case 16777216:case 33554432:return e&62914560;case 67108864:return 67108864;case 134217728:return 134217728;case 268435456:return 268435456;case 536870912:return 536870912;case 1073741824:return 0;default:return e}}function St(e,n,a){var o=e.pendingLanes;if(o===0)return 0;var u=0,d=e.suspendedLanes,y=e.pingedLanes;e=e.warmLanes;var w=o&134217727;return w!==0?(o=w&~d,o!==0?u=Ct(o):(y&=w,y!==0?u=Ct(y):a||(a=w&~e,a!==0&&(u=Ct(a))))):(w=o&~d,w!==0?u=Ct(w):y!==0?u=Ct(y):a||(a=o&~e,a!==0&&(u=Ct(a)))),u===0?0:n!==0&&n!==u&&(n&d)===0&&(d=u&-u,a=n&-n,d>=a||d===32&&(a&4194048)!==0)?n:u}function Nt(e,n){return(e.pendingLanes&~(e.suspendedLanes&~e.pingedLanes)&n)===0}function Ft(e,n){switch(e){case 1:case 2:case 4:case 8:case 64:return n+250;case 16:case 32:case 128:case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:case 262144:case 524288:case 1048576:case 2097152:return n+5e3;case 4194304:case 8388608:case 16777216:case 33554432:return-1;case 67108864:case 134217728:case 268435456:case 536870912:case 1073741824:return-1;default:return-1}}function Tt(){var e=W;return W<<=1,(W&62914560)===0&&(W=4194304),e}function jt(e){for(var n=[],a=0;31>a;a++)n.push(e);return n}function qt(e,n){e.pendingLanes|=n,n!==268435456&&(e.suspendedLanes=0,e.pingedLanes=0,e.warmLanes=0)}function tn(e,n,a,o,u,d){var y=e.pendingLanes;e.pendingLanes=a,e.suspendedLanes=0,e.pingedLanes=0,e.warmLanes=0,e.expiredLanes&=a,e.entangledLanes&=a,e.errorRecoveryDisabledLanes&=a,e.shellSuspendCounter=0;var w=e.entanglements,V=e.expirationTimes,st=e.hiddenUpdates;for(a=y&~a;0<a;){var xt=31-Gt(a),Et=1<<xt;w[xt]=0,V[xt]=-1;var dt=st[xt];if(dt!==null)for(st[xt]=null,xt=0;xt<dt.length;xt++){var ht=dt[xt];ht!==null&&(ht.lane&=-536870913)}a&=~Et}o!==0&&Be(e,o,0),d!==0&&u===0&&e.tag!==0&&(e.suspendedLanes|=d&~(y&~n))}function Be(e,n,a){e.pendingLanes|=n,e.suspendedLanes&=~n;var o=31-Gt(n);e.entangledLanes|=n,e.entanglements[o]=e.entanglements[o]|1073741824|a&261930}function ti(e,n){var a=e.entangledLanes|=n;for(e=e.entanglements;a;){var o=31-Gt(a),u=1<<o;u&n|e[o]&n&&(e[o]|=n),a&=~u}}function ei(e,n){var a=n&-n;return a=(a&42)!==0?1:so(a),(a&(e.suspendedLanes|n))!==0?0:a}function so(e){switch(e){case 2:e=1;break;case 8:e=4;break;case 32:e=16;break;case 256:case 512:case 1024:case 2048:case 4096:case 8192:case 16384:case 32768:case 65536:case 131072:case 262144:case 524288:case 1048576:case 2097152:case 4194304:case 8388608:case 16777216:case 33554432:e=128;break;case 268435456:e=134217728;break;default:e=0}return e}function oo(e){return e&=-e,2<e?8<e?(e&134217727)!==0?32:268435456:8:2}function lo(){var e=X.p;return e!==0?e:(e=window.event,e===void 0?32:U_(e.type))}function ts(e,n){var a=X.p;try{return X.p=e,n()}finally{X.p=a}}var Bi=Math.random().toString(36).slice(2),pn="__reactFiber$"+Bi,Dn="__reactProps$"+Bi,qn="__reactContainer$"+Bi,gr="__reactEvents$"+Bi,xl="__reactListeners$"+Bi,Sl="__reactHandles$"+Bi,_r="__reactResources$"+Bi,Ua="__reactMarker$"+Bi;function La(e){delete e[pn],delete e[Dn],delete e[gr],delete e[xl],delete e[Sl]}function ea(e){var n=e[pn];if(n)return n;for(var a=e.parentNode;a;){if(n=a[qn]||a[pn]){if(a=n.alternate,n.child!==null||a!==null&&a.child!==null)for(e=g_(e);e!==null;){if(a=e[pn])return a;e=g_(e)}return n}e=a,a=e.parentNode}return null}function na(e){if(e=e[pn]||e[qn]){var n=e.tag;if(n===5||n===6||n===13||n===31||n===26||n===27||n===3)return e}return null}function vr(e){var n=e.tag;if(n===5||n===26||n===27||n===6)return e.stateNode;throw Error(r(33))}function Oa(e){var n=e[_r];return n||(n=e[_r]={hoistableStyles:new Map,hoistableScripts:new Map}),n}function mn(e){e[Ua]=!0}var yl=new Set,R={};function Z(e,n){ft(e,n),ft(e+"Capture",n)}function ft(e,n){for(R[e]=n,e=0;e<n.length;e++)yl.add(n[e])}var ot=RegExp("^[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*$"),lt={},It={};function kt(e){return Ce.call(It,e)?!0:Ce.call(lt,e)?!1:ot.test(e)?It[e]=!0:(lt[e]=!0,!1)}function Ot(e,n,a){if(kt(n))if(a===null)e.removeAttribute(n);else{switch(typeof a){case"undefined":case"function":case"symbol":e.removeAttribute(n);return;case"boolean":var o=n.toLowerCase().slice(0,5);if(o!=="data-"&&o!=="aria-"){e.removeAttribute(n);return}}e.setAttribute(n,""+a)}}function Yt(e,n,a){if(a===null)e.removeAttribute(n);else{switch(typeof a){case"undefined":case"function":case"symbol":case"boolean":e.removeAttribute(n);return}e.setAttribute(n,""+a)}}function Wt(e,n,a,o){if(o===null)e.removeAttribute(a);else{switch(typeof o){case"undefined":case"function":case"symbol":case"boolean":e.removeAttribute(a);return}e.setAttributeNS(n,a,""+o)}}function te(e){switch(typeof e){case"bigint":case"boolean":case"number":case"string":case"undefined":return e;case"object":return e;default:return""}}function ue(e){var n=e.type;return(e=e.nodeName)&&e.toLowerCase()==="input"&&(n==="checkbox"||n==="radio")}function Qt(e,n,a){var o=Object.getOwnPropertyDescriptor(e.constructor.prototype,n);if(!e.hasOwnProperty(n)&&typeof o<"u"&&typeof o.get=="function"&&typeof o.set=="function"){var u=o.get,d=o.set;return Object.defineProperty(e,n,{configurable:!0,get:function(){return u.call(this)},set:function(y){a=""+y,d.call(this,y)}}),Object.defineProperty(e,n,{enumerable:o.enumerable}),{getValue:function(){return a},setValue:function(y){a=""+y},stopTracking:function(){e._valueTracker=null,delete e[n]}}}}function we(e){if(!e._valueTracker){var n=ue(e)?"checked":"value";e._valueTracker=Qt(e,n,""+e[n])}}function en(e){if(!e)return!1;var n=e._valueTracker;if(!n)return!0;var a=n.getValue(),o="";return e&&(o=ue(e)?e.checked?"true":"false":e.value),e=o,e!==a?(n.setValue(e),!0):!1}function Ze(e){if(e=e||(typeof document<"u"?document:void 0),typeof e>"u")return null;try{return e.activeElement||e.body}catch{return e.body}}var Ie=/[\n"\\]/g;function ze(e){return e.replace(Ie,function(n){return"\\"+n.charCodeAt(0).toString(16)+" "})}function Vt(e,n,a,o,u,d,y,w){e.name="",y!=null&&typeof y!="function"&&typeof y!="symbol"&&typeof y!="boolean"?e.type=y:e.removeAttribute("type"),n!=null?y==="number"?(n===0&&e.value===""||e.value!=n)&&(e.value=""+te(n)):e.value!==""+te(n)&&(e.value=""+te(n)):y!=="submit"&&y!=="reset"||e.removeAttribute("value"),n!=null?ve(e,y,te(n)):a!=null?ve(e,y,te(a)):o!=null&&e.removeAttribute("value"),u==null&&d!=null&&(e.defaultChecked=!!d),u!=null&&(e.checked=u&&typeof u!="function"&&typeof u!="symbol"),w!=null&&typeof w!="function"&&typeof w!="symbol"&&typeof w!="boolean"?e.name=""+te(w):e.removeAttribute("name")}function In(e,n,a,o,u,d,y,w){if(d!=null&&typeof d!="function"&&typeof d!="symbol"&&typeof d!="boolean"&&(e.type=d),n!=null||a!=null){if(!(d!=="submit"&&d!=="reset"||n!=null)){we(e);return}a=a!=null?""+te(a):"",n=n!=null?""+te(n):a,w||n===e.value||(e.value=n),e.defaultValue=n}o=o??u,o=typeof o!="function"&&typeof o!="symbol"&&!!o,e.checked=w?e.checked:!!o,e.defaultChecked=!!o,y!=null&&typeof y!="function"&&typeof y!="symbol"&&typeof y!="boolean"&&(e.name=y),we(e)}function ve(e,n,a){n==="number"&&Ze(e.ownerDocument)===e||e.defaultValue===""+a||(e.defaultValue=""+a)}function yn(e,n,a,o){if(e=e.options,n){n={};for(var u=0;u<a.length;u++)n["$"+a[u]]=!0;for(a=0;a<e.length;a++)u=n.hasOwnProperty("$"+e[a].value),e[a].selected!==u&&(e[a].selected=u),u&&o&&(e[a].defaultSelected=!0)}else{for(a=""+te(a),n=null,u=0;u<e.length;u++){if(e[u].value===a){e[u].selected=!0,o&&(e[u].defaultSelected=!0);return}n!==null||e[u].disabled||(n=e[u])}n!==null&&(n.selected=!0)}}function ni(e,n,a){if(n!=null&&(n=""+te(n),n!==e.value&&(e.value=n),a==null)){e.defaultValue!==n&&(e.defaultValue=n);return}e.defaultValue=a!=null?""+te(a):""}function Ai(e,n,a,o){if(n==null){if(o!=null){if(a!=null)throw Error(r(92));if($(o)){if(1<o.length)throw Error(r(93));o=o[0]}a=o}a==null&&(a=""),n=a}a=te(n),e.defaultValue=a,o=e.textContent,o===a&&o!==""&&o!==null&&(e.value=o),we(e)}function ii(e,n){if(n){var a=e.firstChild;if(a&&a===e.lastChild&&a.nodeType===3){a.nodeValue=n;return}}e.textContent=n}var Fe=new Set("animationIterationCount aspectRatio borderImageOutset borderImageSlice borderImageWidth boxFlex boxFlexGroup boxOrdinalGroup columnCount columns flex flexGrow flexPositive flexShrink flexNegative flexOrder gridArea gridRow gridRowEnd gridRowSpan gridRowStart gridColumn gridColumnEnd gridColumnSpan gridColumnStart fontWeight lineClamp lineHeight opacity order orphans scale tabSize widows zIndex zoom fillOpacity floodOpacity stopOpacity strokeDasharray strokeDashoffset strokeMiterlimit strokeOpacity strokeWidth MozAnimationIterationCount MozBoxFlex MozBoxFlexGroup MozLineClamp msAnimationIterationCount msFlex msZoom msFlexGrow msFlexNegative msFlexOrder msFlexPositive msFlexShrink msGridColumn msGridColumnSpan msGridRow msGridRowSpan WebkitAnimationIterationCount WebkitBoxFlex WebKitBoxFlexGroup WebkitBoxOrdinalGroup WebkitColumnCount WebkitColumns WebkitFlex WebkitFlexGrow WebkitFlexPositive WebkitFlexShrink WebkitLineClamp".split(" "));function nn(e,n,a){var o=n.indexOf("--")===0;a==null||typeof a=="boolean"||a===""?o?e.setProperty(n,""):n==="float"?e.cssFloat="":e[n]="":o?e.setProperty(n,a):typeof a!="number"||a===0||Fe.has(n)?n==="float"?e.cssFloat=a:e[n]=(""+a).trim():e[n]=a+"px"}function Ri(e,n,a){if(n!=null&&typeof n!="object")throw Error(r(62));if(e=e.style,a!=null){for(var o in a)!a.hasOwnProperty(o)||n!=null&&n.hasOwnProperty(o)||(o.indexOf("--")===0?e.setProperty(o,""):o==="float"?e.cssFloat="":e[o]="");for(var u in n)o=n[u],n.hasOwnProperty(u)&&a[u]!==o&&nn(e,u,o)}else for(var d in n)n.hasOwnProperty(d)&&nn(e,d,n[d])}function Pe(e){if(e.indexOf("-")===-1)return!1;switch(e){case"annotation-xml":case"color-profile":case"font-face":case"font-face-src":case"font-face-uri":case"font-face-format":case"font-face-name":case"missing-glyph":return!1;default:return!0}}var Ii=new Map([["acceptCharset","accept-charset"],["htmlFor","for"],["httpEquiv","http-equiv"],["crossOrigin","crossorigin"],["accentHeight","accent-height"],["alignmentBaseline","alignment-baseline"],["arabicForm","arabic-form"],["baselineShift","baseline-shift"],["capHeight","cap-height"],["clipPath","clip-path"],["clipRule","clip-rule"],["colorInterpolation","color-interpolation"],["colorInterpolationFilters","color-interpolation-filters"],["colorProfile","color-profile"],["colorRendering","color-rendering"],["dominantBaseline","dominant-baseline"],["enableBackground","enable-background"],["fillOpacity","fill-opacity"],["fillRule","fill-rule"],["floodColor","flood-color"],["floodOpacity","flood-opacity"],["fontFamily","font-family"],["fontSize","font-size"],["fontSizeAdjust","font-size-adjust"],["fontStretch","font-stretch"],["fontStyle","font-style"],["fontVariant","font-variant"],["fontWeight","font-weight"],["glyphName","glyph-name"],["glyphOrientationHorizontal","glyph-orientation-horizontal"],["glyphOrientationVertical","glyph-orientation-vertical"],["horizAdvX","horiz-adv-x"],["horizOriginX","horiz-origin-x"],["imageRendering","image-rendering"],["letterSpacing","letter-spacing"],["lightingColor","lighting-color"],["markerEnd","marker-end"],["markerMid","marker-mid"],["markerStart","marker-start"],["overlinePosition","overline-position"],["overlineThickness","overline-thickness"],["paintOrder","paint-order"],["panose-1","panose-1"],["pointerEvents","pointer-events"],["renderingIntent","rendering-intent"],["shapeRendering","shape-rendering"],["stopColor","stop-color"],["stopOpacity","stop-opacity"],["strikethroughPosition","strikethrough-position"],["strikethroughThickness","strikethrough-thickness"],["strokeDasharray","stroke-dasharray"],["strokeDashoffset","stroke-dashoffset"],["strokeLinecap","stroke-linecap"],["strokeLinejoin","stroke-linejoin"],["strokeMiterlimit","stroke-miterlimit"],["strokeOpacity","stroke-opacity"],["strokeWidth","stroke-width"],["textAnchor","text-anchor"],["textDecoration","text-decoration"],["textRendering","text-rendering"],["transformOrigin","transform-origin"],["underlinePosition","underline-position"],["underlineThickness","underline-thickness"],["unicodeBidi","unicode-bidi"],["unicodeRange","unicode-range"],["unitsPerEm","units-per-em"],["vAlphabetic","v-alphabetic"],["vHanging","v-hanging"],["vIdeographic","v-ideographic"],["vMathematical","v-mathematical"],["vectorEffect","vector-effect"],["vertAdvY","vert-adv-y"],["vertOriginX","vert-origin-x"],["vertOriginY","vert-origin-y"],["wordSpacing","word-spacing"],["writingMode","writing-mode"],["xmlnsXlink","xmlns:xlink"],["xHeight","x-height"]]),Pa=/^[\u0000-\u001F ]*j[\r\n\t]*a[\r\n\t]*v[\r\n\t]*a[\r\n\t]*s[\r\n\t]*c[\r\n\t]*r[\r\n\t]*i[\r\n\t]*p[\r\n\t]*t[\r\n\t]*:/i;function xr(e){return Pa.test(""+e)?"javascript:throw new Error('React has blocked a javascript: URL as a security precaution.')":e}function ia(){}var bu=null;function Tu(e){return e=e.target||e.srcElement||window,e.correspondingUseElement&&(e=e.correspondingUseElement),e.nodeType===3?e.parentNode:e}var es=null,ns=null;function om(e){var n=na(e);if(n&&(e=n.stateNode)){var a=e[Dn]||null;t:switch(e=n.stateNode,n.type){case"input":if(Vt(e,a.value,a.defaultValue,a.defaultValue,a.checked,a.defaultChecked,a.type,a.name),n=a.name,a.type==="radio"&&n!=null){for(a=e;a.parentNode;)a=a.parentNode;for(a=a.querySelectorAll('input[name="'+ze(""+n)+'"][type="radio"]'),n=0;n<a.length;n++){var o=a[n];if(o!==e&&o.form===e.form){var u=o[Dn]||null;if(!u)throw Error(r(90));Vt(o,u.value,u.defaultValue,u.defaultValue,u.checked,u.defaultChecked,u.type,u.name)}}for(n=0;n<a.length;n++)o=a[n],o.form===e.form&&en(o)}break t;case"textarea":ni(e,a.value,a.defaultValue);break t;case"select":n=a.value,n!=null&&yn(e,!!a.multiple,n,!1)}}}var Au=!1;function lm(e,n,a){if(Au)return e(n,a);Au=!0;try{var o=e(n);return o}finally{if(Au=!1,(es!==null||ns!==null)&&(lc(),es&&(n=es,e=ns,ns=es=null,om(n),e)))for(n=0;n<e.length;n++)om(e[n])}}function co(e,n){var a=e.stateNode;if(a===null)return null;var o=a[Dn]||null;if(o===null)return null;a=o[n];t:switch(n){case"onClick":case"onClickCapture":case"onDoubleClick":case"onDoubleClickCapture":case"onMouseDown":case"onMouseDownCapture":case"onMouseMove":case"onMouseMoveCapture":case"onMouseUp":case"onMouseUpCapture":case"onMouseEnter":(o=!o.disabled)||(e=e.type,o=!(e==="button"||e==="input"||e==="select"||e==="textarea")),e=!o;break t;default:e=!1}if(e)return null;if(a&&typeof a!="function")throw Error(r(231,n,typeof a));return a}var aa=!(typeof window>"u"||typeof window.document>"u"||typeof window.document.createElement>"u"),Ru=!1;if(aa)try{var uo={};Object.defineProperty(uo,"passive",{get:function(){Ru=!0}}),window.addEventListener("test",uo,uo),window.removeEventListener("test",uo,uo)}catch{Ru=!1}var Ba=null,Cu=null,Ml=null;function cm(){if(Ml)return Ml;var e,n=Cu,a=n.length,o,u="value"in Ba?Ba.value:Ba.textContent,d=u.length;for(e=0;e<a&&n[e]===u[e];e++);var y=a-e;for(o=1;o<=y&&n[a-o]===u[d-o];o++);return Ml=u.slice(e,1<o?1-o:void 0)}function El(e){var n=e.keyCode;return"charCode"in e?(e=e.charCode,e===0&&n===13&&(e=13)):e=n,e===10&&(e=13),32<=e||e===13?e:0}function bl(){return!0}function um(){return!1}function Wn(e){function n(a,o,u,d,y){this._reactName=a,this._targetInst=u,this.type=o,this.nativeEvent=d,this.target=y,this.currentTarget=null;for(var w in e)e.hasOwnProperty(w)&&(a=e[w],this[w]=a?a(d):d[w]);return this.isDefaultPrevented=(d.defaultPrevented!=null?d.defaultPrevented:d.returnValue===!1)?bl:um,this.isPropagationStopped=um,this}return v(n.prototype,{preventDefault:function(){this.defaultPrevented=!0;var a=this.nativeEvent;a&&(a.preventDefault?a.preventDefault():typeof a.returnValue!="unknown"&&(a.returnValue=!1),this.isDefaultPrevented=bl)},stopPropagation:function(){var a=this.nativeEvent;a&&(a.stopPropagation?a.stopPropagation():typeof a.cancelBubble!="unknown"&&(a.cancelBubble=!0),this.isPropagationStopped=bl)},persist:function(){},isPersistent:bl}),n}var Sr={eventPhase:0,bubbles:0,cancelable:0,timeStamp:function(e){return e.timeStamp||Date.now()},defaultPrevented:0,isTrusted:0},Tl=Wn(Sr),fo=v({},Sr,{view:0,detail:0}),NS=Wn(fo),wu,Du,ho,Al=v({},fo,{screenX:0,screenY:0,clientX:0,clientY:0,pageX:0,pageY:0,ctrlKey:0,shiftKey:0,altKey:0,metaKey:0,getModifierState:Uu,button:0,buttons:0,relatedTarget:function(e){return e.relatedTarget===void 0?e.fromElement===e.srcElement?e.toElement:e.fromElement:e.relatedTarget},movementX:function(e){return"movementX"in e?e.movementX:(e!==ho&&(ho&&e.type==="mousemove"?(wu=e.screenX-ho.screenX,Du=e.screenY-ho.screenY):Du=wu=0,ho=e),wu)},movementY:function(e){return"movementY"in e?e.movementY:Du}}),fm=Wn(Al),US=v({},Al,{dataTransfer:0}),LS=Wn(US),OS=v({},fo,{relatedTarget:0}),Nu=Wn(OS),PS=v({},Sr,{animationName:0,elapsedTime:0,pseudoElement:0}),BS=Wn(PS),IS=v({},Sr,{clipboardData:function(e){return"clipboardData"in e?e.clipboardData:window.clipboardData}}),zS=Wn(IS),FS=v({},Sr,{data:0}),dm=Wn(FS),HS={Esc:"Escape",Spacebar:" ",Left:"ArrowLeft",Up:"ArrowUp",Right:"ArrowRight",Down:"ArrowDown",Del:"Delete",Win:"OS",Menu:"ContextMenu",Apps:"ContextMenu",Scroll:"ScrollLock",MozPrintableKey:"Unidentified"},GS={8:"Backspace",9:"Tab",12:"Clear",13:"Enter",16:"Shift",17:"Control",18:"Alt",19:"Pause",20:"CapsLock",27:"Escape",32:" ",33:"PageUp",34:"PageDown",35:"End",36:"Home",37:"ArrowLeft",38:"ArrowUp",39:"ArrowRight",40:"ArrowDown",45:"Insert",46:"Delete",112:"F1",113:"F2",114:"F3",115:"F4",116:"F5",117:"F6",118:"F7",119:"F8",120:"F9",121:"F10",122:"F11",123:"F12",144:"NumLock",145:"ScrollLock",224:"Meta"},VS={Alt:"altKey",Control:"ctrlKey",Meta:"metaKey",Shift:"shiftKey"};function kS(e){var n=this.nativeEvent;return n.getModifierState?n.getModifierState(e):(e=VS[e])?!!n[e]:!1}function Uu(){return kS}var XS=v({},fo,{key:function(e){if(e.key){var n=HS[e.key]||e.key;if(n!=="Unidentified")return n}return e.type==="keypress"?(e=El(e),e===13?"Enter":String.fromCharCode(e)):e.type==="keydown"||e.type==="keyup"?GS[e.keyCode]||"Unidentified":""},code:0,location:0,ctrlKey:0,shiftKey:0,altKey:0,metaKey:0,repeat:0,locale:0,getModifierState:Uu,charCode:function(e){return e.type==="keypress"?El(e):0},keyCode:function(e){return e.type==="keydown"||e.type==="keyup"?e.keyCode:0},which:function(e){return e.type==="keypress"?El(e):e.type==="keydown"||e.type==="keyup"?e.keyCode:0}}),qS=Wn(XS),WS=v({},Al,{pointerId:0,width:0,height:0,pressure:0,tangentialPressure:0,tiltX:0,tiltY:0,twist:0,pointerType:0,isPrimary:0}),hm=Wn(WS),YS=v({},fo,{touches:0,targetTouches:0,changedTouches:0,altKey:0,metaKey:0,ctrlKey:0,shiftKey:0,getModifierState:Uu}),ZS=Wn(YS),jS=v({},Sr,{propertyName:0,elapsedTime:0,pseudoElement:0}),KS=Wn(jS),QS=v({},Al,{deltaX:function(e){return"deltaX"in e?e.deltaX:"wheelDeltaX"in e?-e.wheelDeltaX:0},deltaY:function(e){return"deltaY"in e?e.deltaY:"wheelDeltaY"in e?-e.wheelDeltaY:"wheelDelta"in e?-e.wheelDelta:0},deltaZ:0,deltaMode:0}),JS=Wn(QS),$S=v({},Sr,{newState:0,oldState:0}),ty=Wn($S),ey=[9,13,27,32],Lu=aa&&"CompositionEvent"in window,po=null;aa&&"documentMode"in document&&(po=document.documentMode);var ny=aa&&"TextEvent"in window&&!po,pm=aa&&(!Lu||po&&8<po&&11>=po),mm=" ",gm=!1;function _m(e,n){switch(e){case"keyup":return ey.indexOf(n.keyCode)!==-1;case"keydown":return n.keyCode!==229;case"keypress":case"mousedown":case"focusout":return!0;default:return!1}}function vm(e){return e=e.detail,typeof e=="object"&&"data"in e?e.data:null}var is=!1;function iy(e,n){switch(e){case"compositionend":return vm(n);case"keypress":return n.which!==32?null:(gm=!0,mm);case"textInput":return e=n.data,e===mm&&gm?null:e;default:return null}}function ay(e,n){if(is)return e==="compositionend"||!Lu&&_m(e,n)?(e=cm(),Ml=Cu=Ba=null,is=!1,e):null;switch(e){case"paste":return null;case"keypress":if(!(n.ctrlKey||n.altKey||n.metaKey)||n.ctrlKey&&n.altKey){if(n.char&&1<n.char.length)return n.char;if(n.which)return String.fromCharCode(n.which)}return null;case"compositionend":return pm&&n.locale!=="ko"?null:n.data;default:return null}}var ry={color:!0,date:!0,datetime:!0,"datetime-local":!0,email:!0,month:!0,number:!0,password:!0,range:!0,search:!0,tel:!0,text:!0,time:!0,url:!0,week:!0};function xm(e){var n=e&&e.nodeName&&e.nodeName.toLowerCase();return n==="input"?!!ry[e.type]:n==="textarea"}function Sm(e,n,a,o){es?ns?ns.push(o):ns=[o]:es=o,n=mc(n,"onChange"),0<n.length&&(a=new Tl("onChange","change",null,a,o),e.push({event:a,listeners:n}))}var mo=null,go=null;function sy(e){n_(e,0)}function Rl(e){var n=vr(e);if(en(n))return e}function ym(e,n){if(e==="change")return n}var Mm=!1;if(aa){var Ou;if(aa){var Pu="oninput"in document;if(!Pu){var Em=document.createElement("div");Em.setAttribute("oninput","return;"),Pu=typeof Em.oninput=="function"}Ou=Pu}else Ou=!1;Mm=Ou&&(!document.documentMode||9<document.documentMode)}function bm(){mo&&(mo.detachEvent("onpropertychange",Tm),go=mo=null)}function Tm(e){if(e.propertyName==="value"&&Rl(go)){var n=[];Sm(n,go,e,Tu(e)),lm(sy,n)}}function oy(e,n,a){e==="focusin"?(bm(),mo=n,go=a,mo.attachEvent("onpropertychange",Tm)):e==="focusout"&&bm()}function ly(e){if(e==="selectionchange"||e==="keyup"||e==="keydown")return Rl(go)}function cy(e,n){if(e==="click")return Rl(n)}function uy(e,n){if(e==="input"||e==="change")return Rl(n)}function fy(e,n){return e===n&&(e!==0||1/e===1/n)||e!==e&&n!==n}var ai=typeof Object.is=="function"?Object.is:fy;function _o(e,n){if(ai(e,n))return!0;if(typeof e!="object"||e===null||typeof n!="object"||n===null)return!1;var a=Object.keys(e),o=Object.keys(n);if(a.length!==o.length)return!1;for(o=0;o<a.length;o++){var u=a[o];if(!Ce.call(n,u)||!ai(e[u],n[u]))return!1}return!0}function Am(e){for(;e&&e.firstChild;)e=e.firstChild;return e}function Rm(e,n){var a=Am(e);e=0;for(var o;a;){if(a.nodeType===3){if(o=e+a.textContent.length,e<=n&&o>=n)return{node:a,offset:n-e};e=o}t:{for(;a;){if(a.nextSibling){a=a.nextSibling;break t}a=a.parentNode}a=void 0}a=Am(a)}}function Cm(e,n){return e&&n?e===n?!0:e&&e.nodeType===3?!1:n&&n.nodeType===3?Cm(e,n.parentNode):"contains"in e?e.contains(n):e.compareDocumentPosition?!!(e.compareDocumentPosition(n)&16):!1:!1}function wm(e){e=e!=null&&e.ownerDocument!=null&&e.ownerDocument.defaultView!=null?e.ownerDocument.defaultView:window;for(var n=Ze(e.document);n instanceof e.HTMLIFrameElement;){try{var a=typeof n.contentWindow.location.href=="string"}catch{a=!1}if(a)e=n.contentWindow;else break;n=Ze(e.document)}return n}function Bu(e){var n=e&&e.nodeName&&e.nodeName.toLowerCase();return n&&(n==="input"&&(e.type==="text"||e.type==="search"||e.type==="tel"||e.type==="url"||e.type==="password")||n==="textarea"||e.contentEditable==="true")}var dy=aa&&"documentMode"in document&&11>=document.documentMode,as=null,Iu=null,vo=null,zu=!1;function Dm(e,n,a){var o=a.window===a?a.document:a.nodeType===9?a:a.ownerDocument;zu||as==null||as!==Ze(o)||(o=as,"selectionStart"in o&&Bu(o)?o={start:o.selectionStart,end:o.selectionEnd}:(o=(o.ownerDocument&&o.ownerDocument.defaultView||window).getSelection(),o={anchorNode:o.anchorNode,anchorOffset:o.anchorOffset,focusNode:o.focusNode,focusOffset:o.focusOffset}),vo&&_o(vo,o)||(vo=o,o=mc(Iu,"onSelect"),0<o.length&&(n=new Tl("onSelect","select",null,n,a),e.push({event:n,listeners:o}),n.target=as)))}function yr(e,n){var a={};return a[e.toLowerCase()]=n.toLowerCase(),a["Webkit"+e]="webkit"+n,a["Moz"+e]="moz"+n,a}var rs={animationend:yr("Animation","AnimationEnd"),animationiteration:yr("Animation","AnimationIteration"),animationstart:yr("Animation","AnimationStart"),transitionrun:yr("Transition","TransitionRun"),transitionstart:yr("Transition","TransitionStart"),transitioncancel:yr("Transition","TransitionCancel"),transitionend:yr("Transition","TransitionEnd")},Fu={},Nm={};aa&&(Nm=document.createElement("div").style,"AnimationEvent"in window||(delete rs.animationend.animation,delete rs.animationiteration.animation,delete rs.animationstart.animation),"TransitionEvent"in window||delete rs.transitionend.transition);function Mr(e){if(Fu[e])return Fu[e];if(!rs[e])return e;var n=rs[e],a;for(a in n)if(n.hasOwnProperty(a)&&a in Nm)return Fu[e]=n[a];return e}var Um=Mr("animationend"),Lm=Mr("animationiteration"),Om=Mr("animationstart"),hy=Mr("transitionrun"),py=Mr("transitionstart"),my=Mr("transitioncancel"),Pm=Mr("transitionend"),Bm=new Map,Hu="abort auxClick beforeToggle cancel canPlay canPlayThrough click close contextMenu copy cut drag dragEnd dragEnter dragExit dragLeave dragOver dragStart drop durationChange emptied encrypted ended error gotPointerCapture input invalid keyDown keyPress keyUp load loadedData loadedMetadata loadStart lostPointerCapture mouseDown mouseMove mouseOut mouseOver mouseUp paste pause play playing pointerCancel pointerDown pointerMove pointerOut pointerOver pointerUp progress rateChange reset resize seeked seeking stalled submit suspend timeUpdate touchCancel touchEnd touchStart volumeChange scroll toggle touchMove waiting wheel".split(" ");Hu.push("scrollEnd");function Ci(e,n){Bm.set(e,n),Z(n,[e])}var Cl=typeof reportError=="function"?reportError:function(e){if(typeof window=="object"&&typeof window.ErrorEvent=="function"){var n=new window.ErrorEvent("error",{bubbles:!0,cancelable:!0,message:typeof e=="object"&&e!==null&&typeof e.message=="string"?String(e.message):String(e),error:e});if(!window.dispatchEvent(n))return}else if(typeof process=="object"&&typeof process.emit=="function"){process.emit("uncaughtException",e);return}console.error(e)},mi=[],ss=0,Gu=0;function wl(){for(var e=ss,n=Gu=ss=0;n<e;){var a=mi[n];mi[n++]=null;var o=mi[n];mi[n++]=null;var u=mi[n];mi[n++]=null;var d=mi[n];if(mi[n++]=null,o!==null&&u!==null){var y=o.pending;y===null?u.next=u:(u.next=y.next,y.next=u),o.pending=u}d!==0&&Im(a,u,d)}}function Dl(e,n,a,o){mi[ss++]=e,mi[ss++]=n,mi[ss++]=a,mi[ss++]=o,Gu|=o,e.lanes|=o,e=e.alternate,e!==null&&(e.lanes|=o)}function Vu(e,n,a,o){return Dl(e,n,a,o),Nl(e)}function Er(e,n){return Dl(e,null,null,n),Nl(e)}function Im(e,n,a){e.lanes|=a;var o=e.alternate;o!==null&&(o.lanes|=a);for(var u=!1,d=e.return;d!==null;)d.childLanes|=a,o=d.alternate,o!==null&&(o.childLanes|=a),d.tag===22&&(e=d.stateNode,e===null||e._visibility&1||(u=!0)),e=d,d=d.return;return e.tag===3?(d=e.stateNode,u&&n!==null&&(u=31-Gt(a),e=d.hiddenUpdates,o=e[u],o===null?e[u]=[n]:o.push(n),n.lane=a|536870912),d):null}function Nl(e){if(50<Ho)throw Ho=0,Jf=null,Error(r(185));for(var n=e.return;n!==null;)e=n,n=e.return;return e.tag===3?e.stateNode:null}var os={};function gy(e,n,a,o){this.tag=e,this.key=a,this.sibling=this.child=this.return=this.stateNode=this.type=this.elementType=null,this.index=0,this.refCleanup=this.ref=null,this.pendingProps=n,this.dependencies=this.memoizedState=this.updateQueue=this.memoizedProps=null,this.mode=o,this.subtreeFlags=this.flags=0,this.deletions=null,this.childLanes=this.lanes=0,this.alternate=null}function ri(e,n,a,o){return new gy(e,n,a,o)}function ku(e){return e=e.prototype,!(!e||!e.isReactComponent)}function ra(e,n){var a=e.alternate;return a===null?(a=ri(e.tag,n,e.key,e.mode),a.elementType=e.elementType,a.type=e.type,a.stateNode=e.stateNode,a.alternate=e,e.alternate=a):(a.pendingProps=n,a.type=e.type,a.flags=0,a.subtreeFlags=0,a.deletions=null),a.flags=e.flags&65011712,a.childLanes=e.childLanes,a.lanes=e.lanes,a.child=e.child,a.memoizedProps=e.memoizedProps,a.memoizedState=e.memoizedState,a.updateQueue=e.updateQueue,n=e.dependencies,a.dependencies=n===null?null:{lanes:n.lanes,firstContext:n.firstContext},a.sibling=e.sibling,a.index=e.index,a.ref=e.ref,a.refCleanup=e.refCleanup,a}function zm(e,n){e.flags&=65011714;var a=e.alternate;return a===null?(e.childLanes=0,e.lanes=n,e.child=null,e.subtreeFlags=0,e.memoizedProps=null,e.memoizedState=null,e.updateQueue=null,e.dependencies=null,e.stateNode=null):(e.childLanes=a.childLanes,e.lanes=a.lanes,e.child=a.child,e.subtreeFlags=0,e.deletions=null,e.memoizedProps=a.memoizedProps,e.memoizedState=a.memoizedState,e.updateQueue=a.updateQueue,e.type=a.type,n=a.dependencies,e.dependencies=n===null?null:{lanes:n.lanes,firstContext:n.firstContext}),e}function Ul(e,n,a,o,u,d){var y=0;if(o=e,typeof e=="function")ku(e)&&(y=1);else if(typeof e=="string")y=yM(e,a,At.current)?26:e==="html"||e==="head"||e==="body"?27:5;else t:switch(e){case N:return e=ri(31,a,n,u),e.elementType=N,e.lanes=d,e;case A:return br(a.children,u,d,n);case E:y=8,u|=24;break;case S:return e=ri(12,a,n,u|2),e.elementType=S,e.lanes=d,e;case z:return e=ri(13,a,n,u),e.elementType=z,e.lanes=d,e;case D:return e=ri(19,a,n,u),e.elementType=D,e.lanes=d,e;default:if(typeof e=="object"&&e!==null)switch(e.$$typeof){case O:y=10;break t;case P:y=9;break t;case C:y=11;break t;case U:y=14;break t;case b:y=16,o=null;break t}y=29,a=Error(r(130,e===null?"null":typeof e,"")),o=null}return n=ri(y,a,n,u),n.elementType=e,n.type=o,n.lanes=d,n}function br(e,n,a,o){return e=ri(7,e,o,n),e.lanes=a,e}function Xu(e,n,a){return e=ri(6,e,null,n),e.lanes=a,e}function Fm(e){var n=ri(18,null,null,0);return n.stateNode=e,n}function qu(e,n,a){return n=ri(4,e.children!==null?e.children:[],e.key,n),n.lanes=a,n.stateNode={containerInfo:e.containerInfo,pendingChildren:null,implementation:e.implementation},n}var Hm=new WeakMap;function gi(e,n){if(typeof e=="object"&&e!==null){var a=Hm.get(e);return a!==void 0?a:(n={value:e,source:n,stack:_e(n)},Hm.set(e,n),n)}return{value:e,source:n,stack:_e(n)}}var ls=[],cs=0,Ll=null,xo=0,_i=[],vi=0,Ia=null,zi=1,Fi="";function sa(e,n){ls[cs++]=xo,ls[cs++]=Ll,Ll=e,xo=n}function Gm(e,n,a){_i[vi++]=zi,_i[vi++]=Fi,_i[vi++]=Ia,Ia=e;var o=zi;e=Fi;var u=32-Gt(o)-1;o&=~(1<<u),a+=1;var d=32-Gt(n)+u;if(30<d){var y=u-u%5;d=(o&(1<<y)-1).toString(32),o>>=y,u-=y,zi=1<<32-Gt(n)+u|a<<u|o,Fi=d+e}else zi=1<<d|a<<u|o,Fi=e}function Wu(e){e.return!==null&&(sa(e,1),Gm(e,1,0))}function Yu(e){for(;e===Ll;)Ll=ls[--cs],ls[cs]=null,xo=ls[--cs],ls[cs]=null;for(;e===Ia;)Ia=_i[--vi],_i[vi]=null,Fi=_i[--vi],_i[vi]=null,zi=_i[--vi],_i[vi]=null}function Vm(e,n){_i[vi++]=zi,_i[vi++]=Fi,_i[vi++]=Ia,zi=n.id,Fi=n.overflow,Ia=e}var Nn=null,Qe=null,be=!1,za=null,xi=!1,Zu=Error(r(519));function Fa(e){var n=Error(r(418,1<arguments.length&&arguments[1]!==void 0&&arguments[1]?"text":"HTML",""));throw So(gi(n,e)),Zu}function km(e){var n=e.stateNode,a=e.type,o=e.memoizedProps;switch(n[pn]=e,n[Dn]=o,a){case"dialog":Se("cancel",n),Se("close",n);break;case"iframe":case"object":case"embed":Se("load",n);break;case"video":case"audio":for(a=0;a<Vo.length;a++)Se(Vo[a],n);break;case"source":Se("error",n);break;case"img":case"image":case"link":Se("error",n),Se("load",n);break;case"details":Se("toggle",n);break;case"input":Se("invalid",n),In(n,o.value,o.defaultValue,o.checked,o.defaultChecked,o.type,o.name,!0);break;case"select":Se("invalid",n);break;case"textarea":Se("invalid",n),Ai(n,o.value,o.defaultValue,o.children)}a=o.children,typeof a!="string"&&typeof a!="number"&&typeof a!="bigint"||n.textContent===""+a||o.suppressHydrationWarning===!0||s_(n.textContent,a)?(o.popover!=null&&(Se("beforetoggle",n),Se("toggle",n)),o.onScroll!=null&&Se("scroll",n),o.onScrollEnd!=null&&Se("scrollend",n),o.onClick!=null&&(n.onclick=ia),n=!0):n=!1,n||Fa(e,!0)}function Xm(e){for(Nn=e.return;Nn;)switch(Nn.tag){case 5:case 31:case 13:xi=!1;return;case 27:case 3:xi=!0;return;default:Nn=Nn.return}}function us(e){if(e!==Nn)return!1;if(!be)return Xm(e),be=!0,!1;var n=e.tag,a;if((a=n!==3&&n!==27)&&((a=n===5)&&(a=e.type,a=!(a!=="form"&&a!=="button")||hd(e.type,e.memoizedProps)),a=!a),a&&Qe&&Fa(e),Xm(e),n===13){if(e=e.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(r(317));Qe=m_(e)}else if(n===31){if(e=e.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(r(317));Qe=m_(e)}else n===27?(n=Qe,$a(e.type)?(e=vd,vd=null,Qe=e):Qe=n):Qe=Nn?yi(e.stateNode.nextSibling):null;return!0}function Tr(){Qe=Nn=null,be=!1}function ju(){var e=za;return e!==null&&(Kn===null?Kn=e:Kn.push.apply(Kn,e),za=null),e}function So(e){za===null?za=[e]:za.push(e)}var Ku=L(null),Ar=null,oa=null;function Ha(e,n,a){yt(Ku,n._currentValue),n._currentValue=a}function la(e){e._currentValue=Ku.current,Y(Ku)}function Qu(e,n,a){for(;e!==null;){var o=e.alternate;if((e.childLanes&n)!==n?(e.childLanes|=n,o!==null&&(o.childLanes|=n)):o!==null&&(o.childLanes&n)!==n&&(o.childLanes|=n),e===a)break;e=e.return}}function Ju(e,n,a,o){var u=e.child;for(u!==null&&(u.return=e);u!==null;){var d=u.dependencies;if(d!==null){var y=u.child;d=d.firstContext;t:for(;d!==null;){var w=d;d=u;for(var V=0;V<n.length;V++)if(w.context===n[V]){d.lanes|=a,w=d.alternate,w!==null&&(w.lanes|=a),Qu(d.return,a,e),o||(y=null);break t}d=w.next}}else if(u.tag===18){if(y=u.return,y===null)throw Error(r(341));y.lanes|=a,d=y.alternate,d!==null&&(d.lanes|=a),Qu(y,a,e),y=null}else y=u.child;if(y!==null)y.return=u;else for(y=u;y!==null;){if(y===e){y=null;break}if(u=y.sibling,u!==null){u.return=y.return,y=u;break}y=y.return}u=y}}function fs(e,n,a,o){e=null;for(var u=n,d=!1;u!==null;){if(!d){if((u.flags&524288)!==0)d=!0;else if((u.flags&262144)!==0)break}if(u.tag===10){var y=u.alternate;if(y===null)throw Error(r(387));if(y=y.memoizedProps,y!==null){var w=u.type;ai(u.pendingProps.value,y.value)||(e!==null?e.push(w):e=[w])}}else if(u===nt.current){if(y=u.alternate,y===null)throw Error(r(387));y.memoizedState.memoizedState!==u.memoizedState.memoizedState&&(e!==null?e.push(Yo):e=[Yo])}u=u.return}e!==null&&Ju(n,e,a,o),n.flags|=262144}function Ol(e){for(e=e.firstContext;e!==null;){if(!ai(e.context._currentValue,e.memoizedValue))return!0;e=e.next}return!1}function Rr(e){Ar=e,oa=null,e=e.dependencies,e!==null&&(e.firstContext=null)}function Un(e){return qm(Ar,e)}function Pl(e,n){return Ar===null&&Rr(e),qm(e,n)}function qm(e,n){var a=n._currentValue;if(n={context:n,memoizedValue:a,next:null},oa===null){if(e===null)throw Error(r(308));oa=n,e.dependencies={lanes:0,firstContext:n},e.flags|=524288}else oa=oa.next=n;return a}var _y=typeof AbortController<"u"?AbortController:function(){var e=[],n=this.signal={aborted:!1,addEventListener:function(a,o){e.push(o)}};this.abort=function(){n.aborted=!0,e.forEach(function(a){return a()})}},vy=s.unstable_scheduleCallback,xy=s.unstable_NormalPriority,gn={$$typeof:O,Consumer:null,Provider:null,_currentValue:null,_currentValue2:null,_threadCount:0};function $u(){return{controller:new _y,data:new Map,refCount:0}}function yo(e){e.refCount--,e.refCount===0&&vy(xy,function(){e.controller.abort()})}var Mo=null,tf=0,ds=0,hs=null;function Sy(e,n){if(Mo===null){var a=Mo=[];tf=0,ds=ad(),hs={status:"pending",value:void 0,then:function(o){a.push(o)}}}return tf++,n.then(Wm,Wm),n}function Wm(){if(--tf===0&&Mo!==null){hs!==null&&(hs.status="fulfilled");var e=Mo;Mo=null,ds=0,hs=null;for(var n=0;n<e.length;n++)(0,e[n])()}}function yy(e,n){var a=[],o={status:"pending",value:null,reason:null,then:function(u){a.push(u)}};return e.then(function(){o.status="fulfilled",o.value=n;for(var u=0;u<a.length;u++)(0,a[u])(n)},function(u){for(o.status="rejected",o.reason=u,u=0;u<a.length;u++)(0,a[u])(void 0)}),o}var Ym=I.S;I.S=function(e,n){Dg=ke(),typeof n=="object"&&n!==null&&typeof n.then=="function"&&Sy(e,n),Ym!==null&&Ym(e,n)};var Cr=L(null);function ef(){var e=Cr.current;return e!==null?e:je.pooledCache}function Bl(e,n){n===null?yt(Cr,Cr.current):yt(Cr,n.pool)}function Zm(){var e=ef();return e===null?null:{parent:gn._currentValue,pool:e}}var ps=Error(r(460)),nf=Error(r(474)),Il=Error(r(542)),zl={then:function(){}};function jm(e){return e=e.status,e==="fulfilled"||e==="rejected"}function Km(e,n,a){switch(a=e[a],a===void 0?e.push(n):a!==n&&(n.then(ia,ia),n=a),n.status){case"fulfilled":return n.value;case"rejected":throw e=n.reason,Jm(e),e;default:if(typeof n.status=="string")n.then(ia,ia);else{if(e=je,e!==null&&100<e.shellSuspendCounter)throw Error(r(482));e=n,e.status="pending",e.then(function(o){if(n.status==="pending"){var u=n;u.status="fulfilled",u.value=o}},function(o){if(n.status==="pending"){var u=n;u.status="rejected",u.reason=o}})}switch(n.status){case"fulfilled":return n.value;case"rejected":throw e=n.reason,Jm(e),e}throw Dr=n,ps}}function wr(e){try{var n=e._init;return n(e._payload)}catch(a){throw a!==null&&typeof a=="object"&&typeof a.then=="function"?(Dr=a,ps):a}}var Dr=null;function Qm(){if(Dr===null)throw Error(r(459));var e=Dr;return Dr=null,e}function Jm(e){if(e===ps||e===Il)throw Error(r(483))}var ms=null,Eo=0;function Fl(e){var n=Eo;return Eo+=1,ms===null&&(ms=[]),Km(ms,e,n)}function bo(e,n){n=n.props.ref,e.ref=n!==void 0?n:null}function Hl(e,n){throw n.$$typeof===g?Error(r(525)):(e=Object.prototype.toString.call(n),Error(r(31,e==="[object Object]"?"object with keys {"+Object.keys(n).join(", ")+"}":e)))}function $m(e){function n(J,q){if(e){var rt=J.deletions;rt===null?(J.deletions=[q],J.flags|=16):rt.push(q)}}function a(J,q){if(!e)return null;for(;q!==null;)n(J,q),q=q.sibling;return null}function o(J){for(var q=new Map;J!==null;)J.key!==null?q.set(J.key,J):q.set(J.index,J),J=J.sibling;return q}function u(J,q){return J=ra(J,q),J.index=0,J.sibling=null,J}function d(J,q,rt){return J.index=rt,e?(rt=J.alternate,rt!==null?(rt=rt.index,rt<q?(J.flags|=67108866,q):rt):(J.flags|=67108866,q)):(J.flags|=1048576,q)}function y(J){return e&&J.alternate===null&&(J.flags|=67108866),J}function w(J,q,rt,Mt){return q===null||q.tag!==6?(q=Xu(rt,J.mode,Mt),q.return=J,q):(q=u(q,rt),q.return=J,q)}function V(J,q,rt,Mt){var ee=rt.type;return ee===A?xt(J,q,rt.props.children,Mt,rt.key):q!==null&&(q.elementType===ee||typeof ee=="object"&&ee!==null&&ee.$$typeof===b&&wr(ee)===q.type)?(q=u(q,rt.props),bo(q,rt),q.return=J,q):(q=Ul(rt.type,rt.key,rt.props,null,J.mode,Mt),bo(q,rt),q.return=J,q)}function st(J,q,rt,Mt){return q===null||q.tag!==4||q.stateNode.containerInfo!==rt.containerInfo||q.stateNode.implementation!==rt.implementation?(q=qu(rt,J.mode,Mt),q.return=J,q):(q=u(q,rt.children||[]),q.return=J,q)}function xt(J,q,rt,Mt,ee){return q===null||q.tag!==7?(q=br(rt,J.mode,Mt,ee),q.return=J,q):(q=u(q,rt),q.return=J,q)}function Et(J,q,rt){if(typeof q=="string"&&q!==""||typeof q=="number"||typeof q=="bigint")return q=Xu(""+q,J.mode,rt),q.return=J,q;if(typeof q=="object"&&q!==null){switch(q.$$typeof){case x:return rt=Ul(q.type,q.key,q.props,null,J.mode,rt),bo(rt,q),rt.return=J,rt;case M:return q=qu(q,J.mode,rt),q.return=J,q;case b:return q=wr(q),Et(J,q,rt)}if($(q)||k(q))return q=br(q,J.mode,rt,null),q.return=J,q;if(typeof q.then=="function")return Et(J,Fl(q),rt);if(q.$$typeof===O)return Et(J,Pl(J,q),rt);Hl(J,q)}return null}function dt(J,q,rt,Mt){var ee=q!==null?q.key:null;if(typeof rt=="string"&&rt!==""||typeof rt=="number"||typeof rt=="bigint")return ee!==null?null:w(J,q,""+rt,Mt);if(typeof rt=="object"&&rt!==null){switch(rt.$$typeof){case x:return rt.key===ee?V(J,q,rt,Mt):null;case M:return rt.key===ee?st(J,q,rt,Mt):null;case b:return rt=wr(rt),dt(J,q,rt,Mt)}if($(rt)||k(rt))return ee!==null?null:xt(J,q,rt,Mt,null);if(typeof rt.then=="function")return dt(J,q,Fl(rt),Mt);if(rt.$$typeof===O)return dt(J,q,Pl(J,rt),Mt);Hl(J,rt)}return null}function ht(J,q,rt,Mt,ee){if(typeof Mt=="string"&&Mt!==""||typeof Mt=="number"||typeof Mt=="bigint")return J=J.get(rt)||null,w(q,J,""+Mt,ee);if(typeof Mt=="object"&&Mt!==null){switch(Mt.$$typeof){case x:return J=J.get(Mt.key===null?rt:Mt.key)||null,V(q,J,Mt,ee);case M:return J=J.get(Mt.key===null?rt:Mt.key)||null,st(q,J,Mt,ee);case b:return Mt=wr(Mt),ht(J,q,rt,Mt,ee)}if($(Mt)||k(Mt))return J=J.get(rt)||null,xt(q,J,Mt,ee,null);if(typeof Mt.then=="function")return ht(J,q,rt,Fl(Mt),ee);if(Mt.$$typeof===O)return ht(J,q,rt,Pl(q,Mt),ee);Hl(q,Mt)}return null}function Zt(J,q,rt,Mt){for(var ee=null,De=null,Kt=q,de=q=0,Me=null;Kt!==null&&de<rt.length;de++){Kt.index>de?(Me=Kt,Kt=null):Me=Kt.sibling;var Ne=dt(J,Kt,rt[de],Mt);if(Ne===null){Kt===null&&(Kt=Me);break}e&&Kt&&Ne.alternate===null&&n(J,Kt),q=d(Ne,q,de),De===null?ee=Ne:De.sibling=Ne,De=Ne,Kt=Me}if(de===rt.length)return a(J,Kt),be&&sa(J,de),ee;if(Kt===null){for(;de<rt.length;de++)Kt=Et(J,rt[de],Mt),Kt!==null&&(q=d(Kt,q,de),De===null?ee=Kt:De.sibling=Kt,De=Kt);return be&&sa(J,de),ee}for(Kt=o(Kt);de<rt.length;de++)Me=ht(Kt,J,de,rt[de],Mt),Me!==null&&(e&&Me.alternate!==null&&Kt.delete(Me.key===null?de:Me.key),q=d(Me,q,de),De===null?ee=Me:De.sibling=Me,De=Me);return e&&Kt.forEach(function(ar){return n(J,ar)}),be&&sa(J,de),ee}function re(J,q,rt,Mt){if(rt==null)throw Error(r(151));for(var ee=null,De=null,Kt=q,de=q=0,Me=null,Ne=rt.next();Kt!==null&&!Ne.done;de++,Ne=rt.next()){Kt.index>de?(Me=Kt,Kt=null):Me=Kt.sibling;var ar=dt(J,Kt,Ne.value,Mt);if(ar===null){Kt===null&&(Kt=Me);break}e&&Kt&&ar.alternate===null&&n(J,Kt),q=d(ar,q,de),De===null?ee=ar:De.sibling=ar,De=ar,Kt=Me}if(Ne.done)return a(J,Kt),be&&sa(J,de),ee;if(Kt===null){for(;!Ne.done;de++,Ne=rt.next())Ne=Et(J,Ne.value,Mt),Ne!==null&&(q=d(Ne,q,de),De===null?ee=Ne:De.sibling=Ne,De=Ne);return be&&sa(J,de),ee}for(Kt=o(Kt);!Ne.done;de++,Ne=rt.next())Ne=ht(Kt,J,de,Ne.value,Mt),Ne!==null&&(e&&Ne.alternate!==null&&Kt.delete(Ne.key===null?de:Ne.key),q=d(Ne,q,de),De===null?ee=Ne:De.sibling=Ne,De=Ne);return e&&Kt.forEach(function(UM){return n(J,UM)}),be&&sa(J,de),ee}function We(J,q,rt,Mt){if(typeof rt=="object"&&rt!==null&&rt.type===A&&rt.key===null&&(rt=rt.props.children),typeof rt=="object"&&rt!==null){switch(rt.$$typeof){case x:t:{for(var ee=rt.key;q!==null;){if(q.key===ee){if(ee=rt.type,ee===A){if(q.tag===7){a(J,q.sibling),Mt=u(q,rt.props.children),Mt.return=J,J=Mt;break t}}else if(q.elementType===ee||typeof ee=="object"&&ee!==null&&ee.$$typeof===b&&wr(ee)===q.type){a(J,q.sibling),Mt=u(q,rt.props),bo(Mt,rt),Mt.return=J,J=Mt;break t}a(J,q);break}else n(J,q);q=q.sibling}rt.type===A?(Mt=br(rt.props.children,J.mode,Mt,rt.key),Mt.return=J,J=Mt):(Mt=Ul(rt.type,rt.key,rt.props,null,J.mode,Mt),bo(Mt,rt),Mt.return=J,J=Mt)}return y(J);case M:t:{for(ee=rt.key;q!==null;){if(q.key===ee)if(q.tag===4&&q.stateNode.containerInfo===rt.containerInfo&&q.stateNode.implementation===rt.implementation){a(J,q.sibling),Mt=u(q,rt.children||[]),Mt.return=J,J=Mt;break t}else{a(J,q);break}else n(J,q);q=q.sibling}Mt=qu(rt,J.mode,Mt),Mt.return=J,J=Mt}return y(J);case b:return rt=wr(rt),We(J,q,rt,Mt)}if($(rt))return Zt(J,q,rt,Mt);if(k(rt)){if(ee=k(rt),typeof ee!="function")throw Error(r(150));return rt=ee.call(rt),re(J,q,rt,Mt)}if(typeof rt.then=="function")return We(J,q,Fl(rt),Mt);if(rt.$$typeof===O)return We(J,q,Pl(J,rt),Mt);Hl(J,rt)}return typeof rt=="string"&&rt!==""||typeof rt=="number"||typeof rt=="bigint"?(rt=""+rt,q!==null&&q.tag===6?(a(J,q.sibling),Mt=u(q,rt),Mt.return=J,J=Mt):(a(J,q),Mt=Xu(rt,J.mode,Mt),Mt.return=J,J=Mt),y(J)):a(J,q)}return function(J,q,rt,Mt){try{Eo=0;var ee=We(J,q,rt,Mt);return ms=null,ee}catch(Kt){if(Kt===ps||Kt===Il)throw Kt;var De=ri(29,Kt,null,J.mode);return De.lanes=Mt,De.return=J,De}finally{}}}var Nr=$m(!0),t0=$m(!1),Ga=!1;function af(e){e.updateQueue={baseState:e.memoizedState,firstBaseUpdate:null,lastBaseUpdate:null,shared:{pending:null,lanes:0,hiddenCallbacks:null},callbacks:null}}function rf(e,n){e=e.updateQueue,n.updateQueue===e&&(n.updateQueue={baseState:e.baseState,firstBaseUpdate:e.firstBaseUpdate,lastBaseUpdate:e.lastBaseUpdate,shared:e.shared,callbacks:null})}function Va(e){return{lane:e,tag:0,payload:null,callback:null,next:null}}function ka(e,n,a){var o=e.updateQueue;if(o===null)return null;if(o=o.shared,(Oe&2)!==0){var u=o.pending;return u===null?n.next=n:(n.next=u.next,u.next=n),o.pending=n,n=Nl(e),Im(e,null,a),n}return Dl(e,o,n,a),Nl(e)}function To(e,n,a){if(n=n.updateQueue,n!==null&&(n=n.shared,(a&4194048)!==0)){var o=n.lanes;o&=e.pendingLanes,a|=o,n.lanes=a,ti(e,a)}}function sf(e,n){var a=e.updateQueue,o=e.alternate;if(o!==null&&(o=o.updateQueue,a===o)){var u=null,d=null;if(a=a.firstBaseUpdate,a!==null){do{var y={lane:a.lane,tag:a.tag,payload:a.payload,callback:null,next:null};d===null?u=d=y:d=d.next=y,a=a.next}while(a!==null);d===null?u=d=n:d=d.next=n}else u=d=n;a={baseState:o.baseState,firstBaseUpdate:u,lastBaseUpdate:d,shared:o.shared,callbacks:o.callbacks},e.updateQueue=a;return}e=a.lastBaseUpdate,e===null?a.firstBaseUpdate=n:e.next=n,a.lastBaseUpdate=n}var of=!1;function Ao(){if(of){var e=hs;if(e!==null)throw e}}function Ro(e,n,a,o){of=!1;var u=e.updateQueue;Ga=!1;var d=u.firstBaseUpdate,y=u.lastBaseUpdate,w=u.shared.pending;if(w!==null){u.shared.pending=null;var V=w,st=V.next;V.next=null,y===null?d=st:y.next=st,y=V;var xt=e.alternate;xt!==null&&(xt=xt.updateQueue,w=xt.lastBaseUpdate,w!==y&&(w===null?xt.firstBaseUpdate=st:w.next=st,xt.lastBaseUpdate=V))}if(d!==null){var Et=u.baseState;y=0,xt=st=V=null,w=d;do{var dt=w.lane&-536870913,ht=dt!==w.lane;if(ht?(ye&dt)===dt:(o&dt)===dt){dt!==0&&dt===ds&&(of=!0),xt!==null&&(xt=xt.next={lane:0,tag:w.tag,payload:w.payload,callback:null,next:null});t:{var Zt=e,re=w;dt=n;var We=a;switch(re.tag){case 1:if(Zt=re.payload,typeof Zt=="function"){Et=Zt.call(We,Et,dt);break t}Et=Zt;break t;case 3:Zt.flags=Zt.flags&-65537|128;case 0:if(Zt=re.payload,dt=typeof Zt=="function"?Zt.call(We,Et,dt):Zt,dt==null)break t;Et=v({},Et,dt);break t;case 2:Ga=!0}}dt=w.callback,dt!==null&&(e.flags|=64,ht&&(e.flags|=8192),ht=u.callbacks,ht===null?u.callbacks=[dt]:ht.push(dt))}else ht={lane:dt,tag:w.tag,payload:w.payload,callback:w.callback,next:null},xt===null?(st=xt=ht,V=Et):xt=xt.next=ht,y|=dt;if(w=w.next,w===null){if(w=u.shared.pending,w===null)break;ht=w,w=ht.next,ht.next=null,u.lastBaseUpdate=ht,u.shared.pending=null}}while(!0);xt===null&&(V=Et),u.baseState=V,u.firstBaseUpdate=st,u.lastBaseUpdate=xt,d===null&&(u.shared.lanes=0),Za|=y,e.lanes=y,e.memoizedState=Et}}function e0(e,n){if(typeof e!="function")throw Error(r(191,e));e.call(n)}function n0(e,n){var a=e.callbacks;if(a!==null)for(e.callbacks=null,e=0;e<a.length;e++)e0(a[e],n)}var gs=L(null),Gl=L(0);function i0(e,n){e=_a,yt(Gl,e),yt(gs,n),_a=e|n.baseLanes}function lf(){yt(Gl,_a),yt(gs,gs.current)}function cf(){_a=Gl.current,Y(gs),Y(Gl)}var si=L(null),Si=null;function Xa(e){var n=e.alternate;yt(un,un.current&1),yt(si,e),Si===null&&(n===null||gs.current!==null||n.memoizedState!==null)&&(Si=e)}function uf(e){yt(un,un.current),yt(si,e),Si===null&&(Si=e)}function a0(e){e.tag===22?(yt(un,un.current),yt(si,e),Si===null&&(Si=e)):qa()}function qa(){yt(un,un.current),yt(si,si.current)}function oi(e){Y(si),Si===e&&(Si=null),Y(un)}var un=L(0);function Vl(e){for(var n=e;n!==null;){if(n.tag===13){var a=n.memoizedState;if(a!==null&&(a=a.dehydrated,a===null||gd(a)||_d(a)))return n}else if(n.tag===19&&(n.memoizedProps.revealOrder==="forwards"||n.memoizedProps.revealOrder==="backwards"||n.memoizedProps.revealOrder==="unstable_legacy-backwards"||n.memoizedProps.revealOrder==="together")){if((n.flags&128)!==0)return n}else if(n.child!==null){n.child.return=n,n=n.child;continue}if(n===e)break;for(;n.sibling===null;){if(n.return===null||n.return===e)return null;n=n.return}n.sibling.return=n.return,n=n.sibling}return null}var ca=0,fe=null,Xe=null,_n=null,kl=!1,_s=!1,Ur=!1,Xl=0,Co=0,vs=null,My=0;function ln(){throw Error(r(321))}function ff(e,n){if(n===null)return!1;for(var a=0;a<n.length&&a<e.length;a++)if(!ai(e[a],n[a]))return!1;return!0}function df(e,n,a,o,u,d){return ca=d,fe=n,n.memoizedState=null,n.updateQueue=null,n.lanes=0,I.H=e===null||e.memoizedState===null?G0:Rf,Ur=!1,d=a(o,u),Ur=!1,_s&&(d=s0(n,a,o,u)),r0(e),d}function r0(e){I.H=No;var n=Xe!==null&&Xe.next!==null;if(ca=0,_n=Xe=fe=null,kl=!1,Co=0,vs=null,n)throw Error(r(300));e===null||vn||(e=e.dependencies,e!==null&&Ol(e)&&(vn=!0))}function s0(e,n,a,o){fe=e;var u=0;do{if(_s&&(vs=null),Co=0,_s=!1,25<=u)throw Error(r(301));if(u+=1,_n=Xe=null,e.updateQueue!=null){var d=e.updateQueue;d.lastEffect=null,d.events=null,d.stores=null,d.memoCache!=null&&(d.memoCache.index=0)}I.H=V0,d=n(a,o)}while(_s);return d}function Ey(){var e=I.H,n=e.useState()[0];return n=typeof n.then=="function"?wo(n):n,e=e.useState()[0],(Xe!==null?Xe.memoizedState:null)!==e&&(fe.flags|=1024),n}function hf(){var e=Xl!==0;return Xl=0,e}function pf(e,n,a){n.updateQueue=e.updateQueue,n.flags&=-2053,e.lanes&=~a}function mf(e){if(kl){for(e=e.memoizedState;e!==null;){var n=e.queue;n!==null&&(n.pending=null),e=e.next}kl=!1}ca=0,_n=Xe=fe=null,_s=!1,Co=Xl=0,vs=null}function Vn(){var e={memoizedState:null,baseState:null,baseQueue:null,queue:null,next:null};return _n===null?fe.memoizedState=_n=e:_n=_n.next=e,_n}function fn(){if(Xe===null){var e=fe.alternate;e=e!==null?e.memoizedState:null}else e=Xe.next;var n=_n===null?fe.memoizedState:_n.next;if(n!==null)_n=n,Xe=e;else{if(e===null)throw fe.alternate===null?Error(r(467)):Error(r(310));Xe=e,e={memoizedState:Xe.memoizedState,baseState:Xe.baseState,baseQueue:Xe.baseQueue,queue:Xe.queue,next:null},_n===null?fe.memoizedState=_n=e:_n=_n.next=e}return _n}function ql(){return{lastEffect:null,events:null,stores:null,memoCache:null}}function wo(e){var n=Co;return Co+=1,vs===null&&(vs=[]),e=Km(vs,e,n),n=fe,(_n===null?n.memoizedState:_n.next)===null&&(n=n.alternate,I.H=n===null||n.memoizedState===null?G0:Rf),e}function Wl(e){if(e!==null&&typeof e=="object"){if(typeof e.then=="function")return wo(e);if(e.$$typeof===O)return Un(e)}throw Error(r(438,String(e)))}function gf(e){var n=null,a=fe.updateQueue;if(a!==null&&(n=a.memoCache),n==null){var o=fe.alternate;o!==null&&(o=o.updateQueue,o!==null&&(o=o.memoCache,o!=null&&(n={data:o.data.map(function(u){return u.slice()}),index:0})))}if(n==null&&(n={data:[],index:0}),a===null&&(a=ql(),fe.updateQueue=a),a.memoCache=n,a=n.data[n.index],a===void 0)for(a=n.data[n.index]=Array(e),o=0;o<e;o++)a[o]=G;return n.index++,a}function ua(e,n){return typeof n=="function"?n(e):n}function Yl(e){var n=fn();return _f(n,Xe,e)}function _f(e,n,a){var o=e.queue;if(o===null)throw Error(r(311));o.lastRenderedReducer=a;var u=e.baseQueue,d=o.pending;if(d!==null){if(u!==null){var y=u.next;u.next=d.next,d.next=y}n.baseQueue=u=d,o.pending=null}if(d=e.baseState,u===null)e.memoizedState=d;else{n=u.next;var w=y=null,V=null,st=n,xt=!1;do{var Et=st.lane&-536870913;if(Et!==st.lane?(ye&Et)===Et:(ca&Et)===Et){var dt=st.revertLane;if(dt===0)V!==null&&(V=V.next={lane:0,revertLane:0,gesture:null,action:st.action,hasEagerState:st.hasEagerState,eagerState:st.eagerState,next:null}),Et===ds&&(xt=!0);else if((ca&dt)===dt){st=st.next,dt===ds&&(xt=!0);continue}else Et={lane:0,revertLane:st.revertLane,gesture:null,action:st.action,hasEagerState:st.hasEagerState,eagerState:st.eagerState,next:null},V===null?(w=V=Et,y=d):V=V.next=Et,fe.lanes|=dt,Za|=dt;Et=st.action,Ur&&a(d,Et),d=st.hasEagerState?st.eagerState:a(d,Et)}else dt={lane:Et,revertLane:st.revertLane,gesture:st.gesture,action:st.action,hasEagerState:st.hasEagerState,eagerState:st.eagerState,next:null},V===null?(w=V=dt,y=d):V=V.next=dt,fe.lanes|=Et,Za|=Et;st=st.next}while(st!==null&&st!==n);if(V===null?y=d:V.next=w,!ai(d,e.memoizedState)&&(vn=!0,xt&&(a=hs,a!==null)))throw a;e.memoizedState=d,e.baseState=y,e.baseQueue=V,o.lastRenderedState=d}return u===null&&(o.lanes=0),[e.memoizedState,o.dispatch]}function vf(e){var n=fn(),a=n.queue;if(a===null)throw Error(r(311));a.lastRenderedReducer=e;var o=a.dispatch,u=a.pending,d=n.memoizedState;if(u!==null){a.pending=null;var y=u=u.next;do d=e(d,y.action),y=y.next;while(y!==u);ai(d,n.memoizedState)||(vn=!0),n.memoizedState=d,n.baseQueue===null&&(n.baseState=d),a.lastRenderedState=d}return[d,o]}function o0(e,n,a){var o=fe,u=fn(),d=be;if(d){if(a===void 0)throw Error(r(407));a=a()}else a=n();var y=!ai((Xe||u).memoizedState,a);if(y&&(u.memoizedState=a,vn=!0),u=u.queue,yf(u0.bind(null,o,u,e),[e]),u.getSnapshot!==n||y||_n!==null&&_n.memoizedState.tag&1){if(o.flags|=2048,xs(9,{destroy:void 0},c0.bind(null,o,u,a,n),null),je===null)throw Error(r(349));d||(ca&127)!==0||l0(o,n,a)}return a}function l0(e,n,a){e.flags|=16384,e={getSnapshot:n,value:a},n=fe.updateQueue,n===null?(n=ql(),fe.updateQueue=n,n.stores=[e]):(a=n.stores,a===null?n.stores=[e]:a.push(e))}function c0(e,n,a,o){n.value=a,n.getSnapshot=o,f0(n)&&d0(e)}function u0(e,n,a){return a(function(){f0(n)&&d0(e)})}function f0(e){var n=e.getSnapshot;e=e.value;try{var a=n();return!ai(e,a)}catch{return!0}}function d0(e){var n=Er(e,2);n!==null&&Qn(n,e,2)}function xf(e){var n=Vn();if(typeof e=="function"){var a=e;if(e=a(),Ur){wt(!0);try{a()}finally{wt(!1)}}}return n.memoizedState=n.baseState=e,n.queue={pending:null,lanes:0,dispatch:null,lastRenderedReducer:ua,lastRenderedState:e},n}function h0(e,n,a,o){return e.baseState=a,_f(e,Xe,typeof o=="function"?o:ua)}function by(e,n,a,o,u){if(Kl(e))throw Error(r(485));if(e=n.action,e!==null){var d={payload:u,action:e,next:null,isTransition:!0,status:"pending",value:null,reason:null,listeners:[],then:function(y){d.listeners.push(y)}};I.T!==null?a(!0):d.isTransition=!1,o(d),a=n.pending,a===null?(d.next=n.pending=d,p0(n,d)):(d.next=a.next,n.pending=a.next=d)}}function p0(e,n){var a=n.action,o=n.payload,u=e.state;if(n.isTransition){var d=I.T,y={};I.T=y;try{var w=a(u,o),V=I.S;V!==null&&V(y,w),m0(e,n,w)}catch(st){Sf(e,n,st)}finally{d!==null&&y.types!==null&&(d.types=y.types),I.T=d}}else try{d=a(u,o),m0(e,n,d)}catch(st){Sf(e,n,st)}}function m0(e,n,a){a!==null&&typeof a=="object"&&typeof a.then=="function"?a.then(function(o){g0(e,n,o)},function(o){return Sf(e,n,o)}):g0(e,n,a)}function g0(e,n,a){n.status="fulfilled",n.value=a,_0(n),e.state=a,n=e.pending,n!==null&&(a=n.next,a===n?e.pending=null:(a=a.next,n.next=a,p0(e,a)))}function Sf(e,n,a){var o=e.pending;if(e.pending=null,o!==null){o=o.next;do n.status="rejected",n.reason=a,_0(n),n=n.next;while(n!==o)}e.action=null}function _0(e){e=e.listeners;for(var n=0;n<e.length;n++)(0,e[n])()}function v0(e,n){return n}function x0(e,n){if(be){var a=je.formState;if(a!==null){t:{var o=fe;if(be){if(Qe){e:{for(var u=Qe,d=xi;u.nodeType!==8;){if(!d){u=null;break e}if(u=yi(u.nextSibling),u===null){u=null;break e}}d=u.data,u=d==="F!"||d==="F"?u:null}if(u){Qe=yi(u.nextSibling),o=u.data==="F!";break t}}Fa(o)}o=!1}o&&(n=a[0])}}return a=Vn(),a.memoizedState=a.baseState=n,o={pending:null,lanes:0,dispatch:null,lastRenderedReducer:v0,lastRenderedState:n},a.queue=o,a=z0.bind(null,fe,o),o.dispatch=a,o=xf(!1),d=Af.bind(null,fe,!1,o.queue),o=Vn(),u={state:n,dispatch:null,action:e,pending:null},o.queue=u,a=by.bind(null,fe,u,d,a),u.dispatch=a,o.memoizedState=e,[n,a,!1]}function S0(e){var n=fn();return y0(n,Xe,e)}function y0(e,n,a){if(n=_f(e,n,v0)[0],e=Yl(ua)[0],typeof n=="object"&&n!==null&&typeof n.then=="function")try{var o=wo(n)}catch(y){throw y===ps?Il:y}else o=n;n=fn();var u=n.queue,d=u.dispatch;return a!==n.memoizedState&&(fe.flags|=2048,xs(9,{destroy:void 0},Ty.bind(null,u,a),null)),[o,d,e]}function Ty(e,n){e.action=n}function M0(e){var n=fn(),a=Xe;if(a!==null)return y0(n,a,e);fn(),n=n.memoizedState,a=fn();var o=a.queue.dispatch;return a.memoizedState=e,[n,o,!1]}function xs(e,n,a,o){return e={tag:e,create:a,deps:o,inst:n,next:null},n=fe.updateQueue,n===null&&(n=ql(),fe.updateQueue=n),a=n.lastEffect,a===null?n.lastEffect=e.next=e:(o=a.next,a.next=e,e.next=o,n.lastEffect=e),e}function E0(){return fn().memoizedState}function Zl(e,n,a,o){var u=Vn();fe.flags|=e,u.memoizedState=xs(1|n,{destroy:void 0},a,o===void 0?null:o)}function jl(e,n,a,o){var u=fn();o=o===void 0?null:o;var d=u.memoizedState.inst;Xe!==null&&o!==null&&ff(o,Xe.memoizedState.deps)?u.memoizedState=xs(n,d,a,o):(fe.flags|=e,u.memoizedState=xs(1|n,d,a,o))}function b0(e,n){Zl(8390656,8,e,n)}function yf(e,n){jl(2048,8,e,n)}function Ay(e){fe.flags|=4;var n=fe.updateQueue;if(n===null)n=ql(),fe.updateQueue=n,n.events=[e];else{var a=n.events;a===null?n.events=[e]:a.push(e)}}function T0(e){var n=fn().memoizedState;return Ay({ref:n,nextImpl:e}),function(){if((Oe&2)!==0)throw Error(r(440));return n.impl.apply(void 0,arguments)}}function A0(e,n){return jl(4,2,e,n)}function R0(e,n){return jl(4,4,e,n)}function C0(e,n){if(typeof n=="function"){e=e();var a=n(e);return function(){typeof a=="function"?a():n(null)}}if(n!=null)return e=e(),n.current=e,function(){n.current=null}}function w0(e,n,a){a=a!=null?a.concat([e]):null,jl(4,4,C0.bind(null,n,e),a)}function Mf(){}function D0(e,n){var a=fn();n=n===void 0?null:n;var o=a.memoizedState;return n!==null&&ff(n,o[1])?o[0]:(a.memoizedState=[e,n],e)}function N0(e,n){var a=fn();n=n===void 0?null:n;var o=a.memoizedState;if(n!==null&&ff(n,o[1]))return o[0];if(o=e(),Ur){wt(!0);try{e()}finally{wt(!1)}}return a.memoizedState=[o,n],o}function Ef(e,n,a){return a===void 0||(ca&1073741824)!==0&&(ye&261930)===0?e.memoizedState=n:(e.memoizedState=a,e=Ug(),fe.lanes|=e,Za|=e,a)}function U0(e,n,a,o){return ai(a,n)?a:gs.current!==null?(e=Ef(e,a,o),ai(e,n)||(vn=!0),e):(ca&42)===0||(ca&1073741824)!==0&&(ye&261930)===0?(vn=!0,e.memoizedState=a):(e=Ug(),fe.lanes|=e,Za|=e,n)}function L0(e,n,a,o,u){var d=X.p;X.p=d!==0&&8>d?d:8;var y=I.T,w={};I.T=w,Af(e,!1,n,a);try{var V=u(),st=I.S;if(st!==null&&st(w,V),V!==null&&typeof V=="object"&&typeof V.then=="function"){var xt=yy(V,o);Do(e,n,xt,ui(e))}else Do(e,n,o,ui(e))}catch(Et){Do(e,n,{then:function(){},status:"rejected",reason:Et},ui())}finally{X.p=d,y!==null&&w.types!==null&&(y.types=w.types),I.T=y}}function Ry(){}function bf(e,n,a,o){if(e.tag!==5)throw Error(r(476));var u=O0(e).queue;L0(e,u,n,at,a===null?Ry:function(){return P0(e),a(o)})}function O0(e){var n=e.memoizedState;if(n!==null)return n;n={memoizedState:at,baseState:at,baseQueue:null,queue:{pending:null,lanes:0,dispatch:null,lastRenderedReducer:ua,lastRenderedState:at},next:null};var a={};return n.next={memoizedState:a,baseState:a,baseQueue:null,queue:{pending:null,lanes:0,dispatch:null,lastRenderedReducer:ua,lastRenderedState:a},next:null},e.memoizedState=n,e=e.alternate,e!==null&&(e.memoizedState=n),n}function P0(e){var n=O0(e);n.next===null&&(n=e.alternate.memoizedState),Do(e,n.next.queue,{},ui())}function Tf(){return Un(Yo)}function B0(){return fn().memoizedState}function I0(){return fn().memoizedState}function Cy(e){for(var n=e.return;n!==null;){switch(n.tag){case 24:case 3:var a=ui();e=Va(a);var o=ka(n,e,a);o!==null&&(Qn(o,n,a),To(o,n,a)),n={cache:$u()},e.payload=n;return}n=n.return}}function wy(e,n,a){var o=ui();a={lane:o,revertLane:0,gesture:null,action:a,hasEagerState:!1,eagerState:null,next:null},Kl(e)?F0(n,a):(a=Vu(e,n,a,o),a!==null&&(Qn(a,e,o),H0(a,n,o)))}function z0(e,n,a){var o=ui();Do(e,n,a,o)}function Do(e,n,a,o){var u={lane:o,revertLane:0,gesture:null,action:a,hasEagerState:!1,eagerState:null,next:null};if(Kl(e))F0(n,u);else{var d=e.alternate;if(e.lanes===0&&(d===null||d.lanes===0)&&(d=n.lastRenderedReducer,d!==null))try{var y=n.lastRenderedState,w=d(y,a);if(u.hasEagerState=!0,u.eagerState=w,ai(w,y))return Dl(e,n,u,0),je===null&&wl(),!1}catch{}finally{}if(a=Vu(e,n,u,o),a!==null)return Qn(a,e,o),H0(a,n,o),!0}return!1}function Af(e,n,a,o){if(o={lane:2,revertLane:ad(),gesture:null,action:o,hasEagerState:!1,eagerState:null,next:null},Kl(e)){if(n)throw Error(r(479))}else n=Vu(e,a,o,2),n!==null&&Qn(n,e,2)}function Kl(e){var n=e.alternate;return e===fe||n!==null&&n===fe}function F0(e,n){_s=kl=!0;var a=e.pending;a===null?n.next=n:(n.next=a.next,a.next=n),e.pending=n}function H0(e,n,a){if((a&4194048)!==0){var o=n.lanes;o&=e.pendingLanes,a|=o,n.lanes=a,ti(e,a)}}var No={readContext:Un,use:Wl,useCallback:ln,useContext:ln,useEffect:ln,useImperativeHandle:ln,useLayoutEffect:ln,useInsertionEffect:ln,useMemo:ln,useReducer:ln,useRef:ln,useState:ln,useDebugValue:ln,useDeferredValue:ln,useTransition:ln,useSyncExternalStore:ln,useId:ln,useHostTransitionStatus:ln,useFormState:ln,useActionState:ln,useOptimistic:ln,useMemoCache:ln,useCacheRefresh:ln};No.useEffectEvent=ln;var G0={readContext:Un,use:Wl,useCallback:function(e,n){return Vn().memoizedState=[e,n===void 0?null:n],e},useContext:Un,useEffect:b0,useImperativeHandle:function(e,n,a){a=a!=null?a.concat([e]):null,Zl(4194308,4,C0.bind(null,n,e),a)},useLayoutEffect:function(e,n){return Zl(4194308,4,e,n)},useInsertionEffect:function(e,n){Zl(4,2,e,n)},useMemo:function(e,n){var a=Vn();n=n===void 0?null:n;var o=e();if(Ur){wt(!0);try{e()}finally{wt(!1)}}return a.memoizedState=[o,n],o},useReducer:function(e,n,a){var o=Vn();if(a!==void 0){var u=a(n);if(Ur){wt(!0);try{a(n)}finally{wt(!1)}}}else u=n;return o.memoizedState=o.baseState=u,e={pending:null,lanes:0,dispatch:null,lastRenderedReducer:e,lastRenderedState:u},o.queue=e,e=e.dispatch=wy.bind(null,fe,e),[o.memoizedState,e]},useRef:function(e){var n=Vn();return e={current:e},n.memoizedState=e},useState:function(e){e=xf(e);var n=e.queue,a=z0.bind(null,fe,n);return n.dispatch=a,[e.memoizedState,a]},useDebugValue:Mf,useDeferredValue:function(e,n){var a=Vn();return Ef(a,e,n)},useTransition:function(){var e=xf(!1);return e=L0.bind(null,fe,e.queue,!0,!1),Vn().memoizedState=e,[!1,e]},useSyncExternalStore:function(e,n,a){var o=fe,u=Vn();if(be){if(a===void 0)throw Error(r(407));a=a()}else{if(a=n(),je===null)throw Error(r(349));(ye&127)!==0||l0(o,n,a)}u.memoizedState=a;var d={value:a,getSnapshot:n};return u.queue=d,b0(u0.bind(null,o,d,e),[e]),o.flags|=2048,xs(9,{destroy:void 0},c0.bind(null,o,d,a,n),null),a},useId:function(){var e=Vn(),n=je.identifierPrefix;if(be){var a=Fi,o=zi;a=(o&~(1<<32-Gt(o)-1)).toString(32)+a,n="_"+n+"R_"+a,a=Xl++,0<a&&(n+="H"+a.toString(32)),n+="_"}else a=My++,n="_"+n+"r_"+a.toString(32)+"_";return e.memoizedState=n},useHostTransitionStatus:Tf,useFormState:x0,useActionState:x0,useOptimistic:function(e){var n=Vn();n.memoizedState=n.baseState=e;var a={pending:null,lanes:0,dispatch:null,lastRenderedReducer:null,lastRenderedState:null};return n.queue=a,n=Af.bind(null,fe,!0,a),a.dispatch=n,[e,n]},useMemoCache:gf,useCacheRefresh:function(){return Vn().memoizedState=Cy.bind(null,fe)},useEffectEvent:function(e){var n=Vn(),a={impl:e};return n.memoizedState=a,function(){if((Oe&2)!==0)throw Error(r(440));return a.impl.apply(void 0,arguments)}}},Rf={readContext:Un,use:Wl,useCallback:D0,useContext:Un,useEffect:yf,useImperativeHandle:w0,useInsertionEffect:A0,useLayoutEffect:R0,useMemo:N0,useReducer:Yl,useRef:E0,useState:function(){return Yl(ua)},useDebugValue:Mf,useDeferredValue:function(e,n){var a=fn();return U0(a,Xe.memoizedState,e,n)},useTransition:function(){var e=Yl(ua)[0],n=fn().memoizedState;return[typeof e=="boolean"?e:wo(e),n]},useSyncExternalStore:o0,useId:B0,useHostTransitionStatus:Tf,useFormState:S0,useActionState:S0,useOptimistic:function(e,n){var a=fn();return h0(a,Xe,e,n)},useMemoCache:gf,useCacheRefresh:I0};Rf.useEffectEvent=T0;var V0={readContext:Un,use:Wl,useCallback:D0,useContext:Un,useEffect:yf,useImperativeHandle:w0,useInsertionEffect:A0,useLayoutEffect:R0,useMemo:N0,useReducer:vf,useRef:E0,useState:function(){return vf(ua)},useDebugValue:Mf,useDeferredValue:function(e,n){var a=fn();return Xe===null?Ef(a,e,n):U0(a,Xe.memoizedState,e,n)},useTransition:function(){var e=vf(ua)[0],n=fn().memoizedState;return[typeof e=="boolean"?e:wo(e),n]},useSyncExternalStore:o0,useId:B0,useHostTransitionStatus:Tf,useFormState:M0,useActionState:M0,useOptimistic:function(e,n){var a=fn();return Xe!==null?h0(a,Xe,e,n):(a.baseState=e,[e,a.queue.dispatch])},useMemoCache:gf,useCacheRefresh:I0};V0.useEffectEvent=T0;function Cf(e,n,a,o){n=e.memoizedState,a=a(o,n),a=a==null?n:v({},n,a),e.memoizedState=a,e.lanes===0&&(e.updateQueue.baseState=a)}var wf={enqueueSetState:function(e,n,a){e=e._reactInternals;var o=ui(),u=Va(o);u.payload=n,a!=null&&(u.callback=a),n=ka(e,u,o),n!==null&&(Qn(n,e,o),To(n,e,o))},enqueueReplaceState:function(e,n,a){e=e._reactInternals;var o=ui(),u=Va(o);u.tag=1,u.payload=n,a!=null&&(u.callback=a),n=ka(e,u,o),n!==null&&(Qn(n,e,o),To(n,e,o))},enqueueForceUpdate:function(e,n){e=e._reactInternals;var a=ui(),o=Va(a);o.tag=2,n!=null&&(o.callback=n),n=ka(e,o,a),n!==null&&(Qn(n,e,a),To(n,e,a))}};function k0(e,n,a,o,u,d,y){return e=e.stateNode,typeof e.shouldComponentUpdate=="function"?e.shouldComponentUpdate(o,d,y):n.prototype&&n.prototype.isPureReactComponent?!_o(a,o)||!_o(u,d):!0}function X0(e,n,a,o){e=n.state,typeof n.componentWillReceiveProps=="function"&&n.componentWillReceiveProps(a,o),typeof n.UNSAFE_componentWillReceiveProps=="function"&&n.UNSAFE_componentWillReceiveProps(a,o),n.state!==e&&wf.enqueueReplaceState(n,n.state,null)}function Lr(e,n){var a=n;if("ref"in n){a={};for(var o in n)o!=="ref"&&(a[o]=n[o])}if(e=e.defaultProps){a===n&&(a=v({},a));for(var u in e)a[u]===void 0&&(a[u]=e[u])}return a}function q0(e){Cl(e)}function W0(e){console.error(e)}function Y0(e){Cl(e)}function Ql(e,n){try{var a=e.onUncaughtError;a(n.value,{componentStack:n.stack})}catch(o){setTimeout(function(){throw o})}}function Z0(e,n,a){try{var o=e.onCaughtError;o(a.value,{componentStack:a.stack,errorBoundary:n.tag===1?n.stateNode:null})}catch(u){setTimeout(function(){throw u})}}function Df(e,n,a){return a=Va(a),a.tag=3,a.payload={element:null},a.callback=function(){Ql(e,n)},a}function j0(e){return e=Va(e),e.tag=3,e}function K0(e,n,a,o){var u=a.type.getDerivedStateFromError;if(typeof u=="function"){var d=o.value;e.payload=function(){return u(d)},e.callback=function(){Z0(n,a,o)}}var y=a.stateNode;y!==null&&typeof y.componentDidCatch=="function"&&(e.callback=function(){Z0(n,a,o),typeof u!="function"&&(ja===null?ja=new Set([this]):ja.add(this));var w=o.stack;this.componentDidCatch(o.value,{componentStack:w!==null?w:""})})}function Dy(e,n,a,o,u){if(a.flags|=32768,o!==null&&typeof o=="object"&&typeof o.then=="function"){if(n=a.alternate,n!==null&&fs(n,a,u,!0),a=si.current,a!==null){switch(a.tag){case 31:case 13:return Si===null?cc():a.alternate===null&&cn===0&&(cn=3),a.flags&=-257,a.flags|=65536,a.lanes=u,o===zl?a.flags|=16384:(n=a.updateQueue,n===null?a.updateQueue=new Set([o]):n.add(o),ed(e,o,u)),!1;case 22:return a.flags|=65536,o===zl?a.flags|=16384:(n=a.updateQueue,n===null?(n={transitions:null,markerInstances:null,retryQueue:new Set([o])},a.updateQueue=n):(a=n.retryQueue,a===null?n.retryQueue=new Set([o]):a.add(o)),ed(e,o,u)),!1}throw Error(r(435,a.tag))}return ed(e,o,u),cc(),!1}if(be)return n=si.current,n!==null?((n.flags&65536)===0&&(n.flags|=256),n.flags|=65536,n.lanes=u,o!==Zu&&(e=Error(r(422),{cause:o}),So(gi(e,a)))):(o!==Zu&&(n=Error(r(423),{cause:o}),So(gi(n,a))),e=e.current.alternate,e.flags|=65536,u&=-u,e.lanes|=u,o=gi(o,a),u=Df(e.stateNode,o,u),sf(e,u),cn!==4&&(cn=2)),!1;var d=Error(r(520),{cause:o});if(d=gi(d,a),Fo===null?Fo=[d]:Fo.push(d),cn!==4&&(cn=2),n===null)return!0;o=gi(o,a),a=n;do{switch(a.tag){case 3:return a.flags|=65536,e=u&-u,a.lanes|=e,e=Df(a.stateNode,o,e),sf(a,e),!1;case 1:if(n=a.type,d=a.stateNode,(a.flags&128)===0&&(typeof n.getDerivedStateFromError=="function"||d!==null&&typeof d.componentDidCatch=="function"&&(ja===null||!ja.has(d))))return a.flags|=65536,u&=-u,a.lanes|=u,u=j0(u),K0(u,e,a,o),sf(a,u),!1}a=a.return}while(a!==null);return!1}var Nf=Error(r(461)),vn=!1;function Ln(e,n,a,o){n.child=e===null?t0(n,null,a,o):Nr(n,e.child,a,o)}function Q0(e,n,a,o,u){a=a.render;var d=n.ref;if("ref"in o){var y={};for(var w in o)w!=="ref"&&(y[w]=o[w])}else y=o;return Rr(n),o=df(e,n,a,y,d,u),w=hf(),e!==null&&!vn?(pf(e,n,u),fa(e,n,u)):(be&&w&&Wu(n),n.flags|=1,Ln(e,n,o,u),n.child)}function J0(e,n,a,o,u){if(e===null){var d=a.type;return typeof d=="function"&&!ku(d)&&d.defaultProps===void 0&&a.compare===null?(n.tag=15,n.type=d,$0(e,n,d,o,u)):(e=Ul(a.type,null,o,n,n.mode,u),e.ref=n.ref,e.return=n,n.child=e)}if(d=e.child,!Ff(e,u)){var y=d.memoizedProps;if(a=a.compare,a=a!==null?a:_o,a(y,o)&&e.ref===n.ref)return fa(e,n,u)}return n.flags|=1,e=ra(d,o),e.ref=n.ref,e.return=n,n.child=e}function $0(e,n,a,o,u){if(e!==null){var d=e.memoizedProps;if(_o(d,o)&&e.ref===n.ref)if(vn=!1,n.pendingProps=o=d,Ff(e,u))(e.flags&131072)!==0&&(vn=!0);else return n.lanes=e.lanes,fa(e,n,u)}return Uf(e,n,a,o,u)}function tg(e,n,a,o){var u=o.children,d=e!==null?e.memoizedState:null;if(e===null&&n.stateNode===null&&(n.stateNode={_visibility:1,_pendingMarkers:null,_retryCache:null,_transitions:null}),o.mode==="hidden"){if((n.flags&128)!==0){if(d=d!==null?d.baseLanes|a:a,e!==null){for(o=n.child=e.child,u=0;o!==null;)u=u|o.lanes|o.childLanes,o=o.sibling;o=u&~d}else o=0,n.child=null;return eg(e,n,d,a,o)}if((a&536870912)!==0)n.memoizedState={baseLanes:0,cachePool:null},e!==null&&Bl(n,d!==null?d.cachePool:null),d!==null?i0(n,d):lf(),a0(n);else return o=n.lanes=536870912,eg(e,n,d!==null?d.baseLanes|a:a,a,o)}else d!==null?(Bl(n,d.cachePool),i0(n,d),qa(),n.memoizedState=null):(e!==null&&Bl(n,null),lf(),qa());return Ln(e,n,u,a),n.child}function Uo(e,n){return e!==null&&e.tag===22||n.stateNode!==null||(n.stateNode={_visibility:1,_pendingMarkers:null,_retryCache:null,_transitions:null}),n.sibling}function eg(e,n,a,o,u){var d=ef();return d=d===null?null:{parent:gn._currentValue,pool:d},n.memoizedState={baseLanes:a,cachePool:d},e!==null&&Bl(n,null),lf(),a0(n),e!==null&&fs(e,n,o,!0),n.childLanes=u,null}function Jl(e,n){return n=tc({mode:n.mode,children:n.children},e.mode),n.ref=e.ref,e.child=n,n.return=e,n}function ng(e,n,a){return Nr(n,e.child,null,a),e=Jl(n,n.pendingProps),e.flags|=2,oi(n),n.memoizedState=null,e}function Ny(e,n,a){var o=n.pendingProps,u=(n.flags&128)!==0;if(n.flags&=-129,e===null){if(be){if(o.mode==="hidden")return e=Jl(n,o),n.lanes=536870912,Uo(null,e);if(uf(n),(e=Qe)?(e=p_(e,xi),e=e!==null&&e.data==="&"?e:null,e!==null&&(n.memoizedState={dehydrated:e,treeContext:Ia!==null?{id:zi,overflow:Fi}:null,retryLane:536870912,hydrationErrors:null},a=Fm(e),a.return=n,n.child=a,Nn=n,Qe=null)):e=null,e===null)throw Fa(n);return n.lanes=536870912,null}return Jl(n,o)}var d=e.memoizedState;if(d!==null){var y=d.dehydrated;if(uf(n),u)if(n.flags&256)n.flags&=-257,n=ng(e,n,a);else if(n.memoizedState!==null)n.child=e.child,n.flags|=128,n=null;else throw Error(r(558));else if(vn||fs(e,n,a,!1),u=(a&e.childLanes)!==0,vn||u){if(o=je,o!==null&&(y=ei(o,a),y!==0&&y!==d.retryLane))throw d.retryLane=y,Er(e,y),Qn(o,e,y),Nf;cc(),n=ng(e,n,a)}else e=d.treeContext,Qe=yi(y.nextSibling),Nn=n,be=!0,za=null,xi=!1,e!==null&&Vm(n,e),n=Jl(n,o),n.flags|=4096;return n}return e=ra(e.child,{mode:o.mode,children:o.children}),e.ref=n.ref,n.child=e,e.return=n,e}function $l(e,n){var a=n.ref;if(a===null)e!==null&&e.ref!==null&&(n.flags|=4194816);else{if(typeof a!="function"&&typeof a!="object")throw Error(r(284));(e===null||e.ref!==a)&&(n.flags|=4194816)}}function Uf(e,n,a,o,u){return Rr(n),a=df(e,n,a,o,void 0,u),o=hf(),e!==null&&!vn?(pf(e,n,u),fa(e,n,u)):(be&&o&&Wu(n),n.flags|=1,Ln(e,n,a,u),n.child)}function ig(e,n,a,o,u,d){return Rr(n),n.updateQueue=null,a=s0(n,o,a,u),r0(e),o=hf(),e!==null&&!vn?(pf(e,n,d),fa(e,n,d)):(be&&o&&Wu(n),n.flags|=1,Ln(e,n,a,d),n.child)}function ag(e,n,a,o,u){if(Rr(n),n.stateNode===null){var d=os,y=a.contextType;typeof y=="object"&&y!==null&&(d=Un(y)),d=new a(o,d),n.memoizedState=d.state!==null&&d.state!==void 0?d.state:null,d.updater=wf,n.stateNode=d,d._reactInternals=n,d=n.stateNode,d.props=o,d.state=n.memoizedState,d.refs={},af(n),y=a.contextType,d.context=typeof y=="object"&&y!==null?Un(y):os,d.state=n.memoizedState,y=a.getDerivedStateFromProps,typeof y=="function"&&(Cf(n,a,y,o),d.state=n.memoizedState),typeof a.getDerivedStateFromProps=="function"||typeof d.getSnapshotBeforeUpdate=="function"||typeof d.UNSAFE_componentWillMount!="function"&&typeof d.componentWillMount!="function"||(y=d.state,typeof d.componentWillMount=="function"&&d.componentWillMount(),typeof d.UNSAFE_componentWillMount=="function"&&d.UNSAFE_componentWillMount(),y!==d.state&&wf.enqueueReplaceState(d,d.state,null),Ro(n,o,d,u),Ao(),d.state=n.memoizedState),typeof d.componentDidMount=="function"&&(n.flags|=4194308),o=!0}else if(e===null){d=n.stateNode;var w=n.memoizedProps,V=Lr(a,w);d.props=V;var st=d.context,xt=a.contextType;y=os,typeof xt=="object"&&xt!==null&&(y=Un(xt));var Et=a.getDerivedStateFromProps;xt=typeof Et=="function"||typeof d.getSnapshotBeforeUpdate=="function",w=n.pendingProps!==w,xt||typeof d.UNSAFE_componentWillReceiveProps!="function"&&typeof d.componentWillReceiveProps!="function"||(w||st!==y)&&X0(n,d,o,y),Ga=!1;var dt=n.memoizedState;d.state=dt,Ro(n,o,d,u),Ao(),st=n.memoizedState,w||dt!==st||Ga?(typeof Et=="function"&&(Cf(n,a,Et,o),st=n.memoizedState),(V=Ga||k0(n,a,V,o,dt,st,y))?(xt||typeof d.UNSAFE_componentWillMount!="function"&&typeof d.componentWillMount!="function"||(typeof d.componentWillMount=="function"&&d.componentWillMount(),typeof d.UNSAFE_componentWillMount=="function"&&d.UNSAFE_componentWillMount()),typeof d.componentDidMount=="function"&&(n.flags|=4194308)):(typeof d.componentDidMount=="function"&&(n.flags|=4194308),n.memoizedProps=o,n.memoizedState=st),d.props=o,d.state=st,d.context=y,o=V):(typeof d.componentDidMount=="function"&&(n.flags|=4194308),o=!1)}else{d=n.stateNode,rf(e,n),y=n.memoizedProps,xt=Lr(a,y),d.props=xt,Et=n.pendingProps,dt=d.context,st=a.contextType,V=os,typeof st=="object"&&st!==null&&(V=Un(st)),w=a.getDerivedStateFromProps,(st=typeof w=="function"||typeof d.getSnapshotBeforeUpdate=="function")||typeof d.UNSAFE_componentWillReceiveProps!="function"&&typeof d.componentWillReceiveProps!="function"||(y!==Et||dt!==V)&&X0(n,d,o,V),Ga=!1,dt=n.memoizedState,d.state=dt,Ro(n,o,d,u),Ao();var ht=n.memoizedState;y!==Et||dt!==ht||Ga||e!==null&&e.dependencies!==null&&Ol(e.dependencies)?(typeof w=="function"&&(Cf(n,a,w,o),ht=n.memoizedState),(xt=Ga||k0(n,a,xt,o,dt,ht,V)||e!==null&&e.dependencies!==null&&Ol(e.dependencies))?(st||typeof d.UNSAFE_componentWillUpdate!="function"&&typeof d.componentWillUpdate!="function"||(typeof d.componentWillUpdate=="function"&&d.componentWillUpdate(o,ht,V),typeof d.UNSAFE_componentWillUpdate=="function"&&d.UNSAFE_componentWillUpdate(o,ht,V)),typeof d.componentDidUpdate=="function"&&(n.flags|=4),typeof d.getSnapshotBeforeUpdate=="function"&&(n.flags|=1024)):(typeof d.componentDidUpdate!="function"||y===e.memoizedProps&&dt===e.memoizedState||(n.flags|=4),typeof d.getSnapshotBeforeUpdate!="function"||y===e.memoizedProps&&dt===e.memoizedState||(n.flags|=1024),n.memoizedProps=o,n.memoizedState=ht),d.props=o,d.state=ht,d.context=V,o=xt):(typeof d.componentDidUpdate!="function"||y===e.memoizedProps&&dt===e.memoizedState||(n.flags|=4),typeof d.getSnapshotBeforeUpdate!="function"||y===e.memoizedProps&&dt===e.memoizedState||(n.flags|=1024),o=!1)}return d=o,$l(e,n),o=(n.flags&128)!==0,d||o?(d=n.stateNode,a=o&&typeof a.getDerivedStateFromError!="function"?null:d.render(),n.flags|=1,e!==null&&o?(n.child=Nr(n,e.child,null,u),n.child=Nr(n,null,a,u)):Ln(e,n,a,u),n.memoizedState=d.state,e=n.child):e=fa(e,n,u),e}function rg(e,n,a,o){return Tr(),n.flags|=256,Ln(e,n,a,o),n.child}var Lf={dehydrated:null,treeContext:null,retryLane:0,hydrationErrors:null};function Of(e){return{baseLanes:e,cachePool:Zm()}}function Pf(e,n,a){return e=e!==null?e.childLanes&~a:0,n&&(e|=ci),e}function sg(e,n,a){var o=n.pendingProps,u=!1,d=(n.flags&128)!==0,y;if((y=d)||(y=e!==null&&e.memoizedState===null?!1:(un.current&2)!==0),y&&(u=!0,n.flags&=-129),y=(n.flags&32)!==0,n.flags&=-33,e===null){if(be){if(u?Xa(n):qa(),(e=Qe)?(e=p_(e,xi),e=e!==null&&e.data!=="&"?e:null,e!==null&&(n.memoizedState={dehydrated:e,treeContext:Ia!==null?{id:zi,overflow:Fi}:null,retryLane:536870912,hydrationErrors:null},a=Fm(e),a.return=n,n.child=a,Nn=n,Qe=null)):e=null,e===null)throw Fa(n);return _d(e)?n.lanes=32:n.lanes=536870912,null}var w=o.children;return o=o.fallback,u?(qa(),u=n.mode,w=tc({mode:"hidden",children:w},u),o=br(o,u,a,null),w.return=n,o.return=n,w.sibling=o,n.child=w,o=n.child,o.memoizedState=Of(a),o.childLanes=Pf(e,y,a),n.memoizedState=Lf,Uo(null,o)):(Xa(n),Bf(n,w))}var V=e.memoizedState;if(V!==null&&(w=V.dehydrated,w!==null)){if(d)n.flags&256?(Xa(n),n.flags&=-257,n=If(e,n,a)):n.memoizedState!==null?(qa(),n.child=e.child,n.flags|=128,n=null):(qa(),w=o.fallback,u=n.mode,o=tc({mode:"visible",children:o.children},u),w=br(w,u,a,null),w.flags|=2,o.return=n,w.return=n,o.sibling=w,n.child=o,Nr(n,e.child,null,a),o=n.child,o.memoizedState=Of(a),o.childLanes=Pf(e,y,a),n.memoizedState=Lf,n=Uo(null,o));else if(Xa(n),_d(w)){if(y=w.nextSibling&&w.nextSibling.dataset,y)var st=y.dgst;y=st,o=Error(r(419)),o.stack="",o.digest=y,So({value:o,source:null,stack:null}),n=If(e,n,a)}else if(vn||fs(e,n,a,!1),y=(a&e.childLanes)!==0,vn||y){if(y=je,y!==null&&(o=ei(y,a),o!==0&&o!==V.retryLane))throw V.retryLane=o,Er(e,o),Qn(y,e,o),Nf;gd(w)||cc(),n=If(e,n,a)}else gd(w)?(n.flags|=192,n.child=e.child,n=null):(e=V.treeContext,Qe=yi(w.nextSibling),Nn=n,be=!0,za=null,xi=!1,e!==null&&Vm(n,e),n=Bf(n,o.children),n.flags|=4096);return n}return u?(qa(),w=o.fallback,u=n.mode,V=e.child,st=V.sibling,o=ra(V,{mode:"hidden",children:o.children}),o.subtreeFlags=V.subtreeFlags&65011712,st!==null?w=ra(st,w):(w=br(w,u,a,null),w.flags|=2),w.return=n,o.return=n,o.sibling=w,n.child=o,Uo(null,o),o=n.child,w=e.child.memoizedState,w===null?w=Of(a):(u=w.cachePool,u!==null?(V=gn._currentValue,u=u.parent!==V?{parent:V,pool:V}:u):u=Zm(),w={baseLanes:w.baseLanes|a,cachePool:u}),o.memoizedState=w,o.childLanes=Pf(e,y,a),n.memoizedState=Lf,Uo(e.child,o)):(Xa(n),a=e.child,e=a.sibling,a=ra(a,{mode:"visible",children:o.children}),a.return=n,a.sibling=null,e!==null&&(y=n.deletions,y===null?(n.deletions=[e],n.flags|=16):y.push(e)),n.child=a,n.memoizedState=null,a)}function Bf(e,n){return n=tc({mode:"visible",children:n},e.mode),n.return=e,e.child=n}function tc(e,n){return e=ri(22,e,null,n),e.lanes=0,e}function If(e,n,a){return Nr(n,e.child,null,a),e=Bf(n,n.pendingProps.children),e.flags|=2,n.memoizedState=null,e}function og(e,n,a){e.lanes|=n;var o=e.alternate;o!==null&&(o.lanes|=n),Qu(e.return,n,a)}function zf(e,n,a,o,u,d){var y=e.memoizedState;y===null?e.memoizedState={isBackwards:n,rendering:null,renderingStartTime:0,last:o,tail:a,tailMode:u,treeForkCount:d}:(y.isBackwards=n,y.rendering=null,y.renderingStartTime=0,y.last=o,y.tail=a,y.tailMode=u,y.treeForkCount=d)}function lg(e,n,a){var o=n.pendingProps,u=o.revealOrder,d=o.tail;o=o.children;var y=un.current,w=(y&2)!==0;if(w?(y=y&1|2,n.flags|=128):y&=1,yt(un,y),Ln(e,n,o,a),o=be?xo:0,!w&&e!==null&&(e.flags&128)!==0)t:for(e=n.child;e!==null;){if(e.tag===13)e.memoizedState!==null&&og(e,a,n);else if(e.tag===19)og(e,a,n);else if(e.child!==null){e.child.return=e,e=e.child;continue}if(e===n)break t;for(;e.sibling===null;){if(e.return===null||e.return===n)break t;e=e.return}e.sibling.return=e.return,e=e.sibling}switch(u){case"forwards":for(a=n.child,u=null;a!==null;)e=a.alternate,e!==null&&Vl(e)===null&&(u=a),a=a.sibling;a=u,a===null?(u=n.child,n.child=null):(u=a.sibling,a.sibling=null),zf(n,!1,u,a,d,o);break;case"backwards":case"unstable_legacy-backwards":for(a=null,u=n.child,n.child=null;u!==null;){if(e=u.alternate,e!==null&&Vl(e)===null){n.child=u;break}e=u.sibling,u.sibling=a,a=u,u=e}zf(n,!0,a,null,d,o);break;case"together":zf(n,!1,null,null,void 0,o);break;default:n.memoizedState=null}return n.child}function fa(e,n,a){if(e!==null&&(n.dependencies=e.dependencies),Za|=n.lanes,(a&n.childLanes)===0)if(e!==null){if(fs(e,n,a,!1),(a&n.childLanes)===0)return null}else return null;if(e!==null&&n.child!==e.child)throw Error(r(153));if(n.child!==null){for(e=n.child,a=ra(e,e.pendingProps),n.child=a,a.return=n;e.sibling!==null;)e=e.sibling,a=a.sibling=ra(e,e.pendingProps),a.return=n;a.sibling=null}return n.child}function Ff(e,n){return(e.lanes&n)!==0?!0:(e=e.dependencies,!!(e!==null&&Ol(e)))}function Uy(e,n,a){switch(n.tag){case 3:ct(n,n.stateNode.containerInfo),Ha(n,gn,e.memoizedState.cache),Tr();break;case 27:case 5:Ht(n);break;case 4:ct(n,n.stateNode.containerInfo);break;case 10:Ha(n,n.type,n.memoizedProps.value);break;case 31:if(n.memoizedState!==null)return n.flags|=128,uf(n),null;break;case 13:var o=n.memoizedState;if(o!==null)return o.dehydrated!==null?(Xa(n),n.flags|=128,null):(a&n.child.childLanes)!==0?sg(e,n,a):(Xa(n),e=fa(e,n,a),e!==null?e.sibling:null);Xa(n);break;case 19:var u=(e.flags&128)!==0;if(o=(a&n.childLanes)!==0,o||(fs(e,n,a,!1),o=(a&n.childLanes)!==0),u){if(o)return lg(e,n,a);n.flags|=128}if(u=n.memoizedState,u!==null&&(u.rendering=null,u.tail=null,u.lastEffect=null),yt(un,un.current),o)break;return null;case 22:return n.lanes=0,tg(e,n,a,n.pendingProps);case 24:Ha(n,gn,e.memoizedState.cache)}return fa(e,n,a)}function cg(e,n,a){if(e!==null)if(e.memoizedProps!==n.pendingProps)vn=!0;else{if(!Ff(e,a)&&(n.flags&128)===0)return vn=!1,Uy(e,n,a);vn=(e.flags&131072)!==0}else vn=!1,be&&(n.flags&1048576)!==0&&Gm(n,xo,n.index);switch(n.lanes=0,n.tag){case 16:t:{var o=n.pendingProps;if(e=wr(n.elementType),n.type=e,typeof e=="function")ku(e)?(o=Lr(e,o),n.tag=1,n=ag(null,n,e,o,a)):(n.tag=0,n=Uf(null,n,e,o,a));else{if(e!=null){var u=e.$$typeof;if(u===C){n.tag=11,n=Q0(null,n,e,o,a);break t}else if(u===U){n.tag=14,n=J0(null,n,e,o,a);break t}}throw n=tt(e)||e,Error(r(306,n,""))}}return n;case 0:return Uf(e,n,n.type,n.pendingProps,a);case 1:return o=n.type,u=Lr(o,n.pendingProps),ag(e,n,o,u,a);case 3:t:{if(ct(n,n.stateNode.containerInfo),e===null)throw Error(r(387));o=n.pendingProps;var d=n.memoizedState;u=d.element,rf(e,n),Ro(n,o,null,a);var y=n.memoizedState;if(o=y.cache,Ha(n,gn,o),o!==d.cache&&Ju(n,[gn],a,!0),Ao(),o=y.element,d.isDehydrated)if(d={element:o,isDehydrated:!1,cache:y.cache},n.updateQueue.baseState=d,n.memoizedState=d,n.flags&256){n=rg(e,n,o,a);break t}else if(o!==u){u=gi(Error(r(424)),n),So(u),n=rg(e,n,o,a);break t}else{switch(e=n.stateNode.containerInfo,e.nodeType){case 9:e=e.body;break;default:e=e.nodeName==="HTML"?e.ownerDocument.body:e}for(Qe=yi(e.firstChild),Nn=n,be=!0,za=null,xi=!0,a=t0(n,null,o,a),n.child=a;a;)a.flags=a.flags&-3|4096,a=a.sibling}else{if(Tr(),o===u){n=fa(e,n,a);break t}Ln(e,n,o,a)}n=n.child}return n;case 26:return $l(e,n),e===null?(a=S_(n.type,null,n.pendingProps,null))?n.memoizedState=a:be||(a=n.type,e=n.pendingProps,o=gc(it.current).createElement(a),o[pn]=n,o[Dn]=e,On(o,a,e),mn(o),n.stateNode=o):n.memoizedState=S_(n.type,e.memoizedProps,n.pendingProps,e.memoizedState),null;case 27:return Ht(n),e===null&&be&&(o=n.stateNode=__(n.type,n.pendingProps,it.current),Nn=n,xi=!0,u=Qe,$a(n.type)?(vd=u,Qe=yi(o.firstChild)):Qe=u),Ln(e,n,n.pendingProps.children,a),$l(e,n),e===null&&(n.flags|=4194304),n.child;case 5:return e===null&&be&&((u=o=Qe)&&(o=lM(o,n.type,n.pendingProps,xi),o!==null?(n.stateNode=o,Nn=n,Qe=yi(o.firstChild),xi=!1,u=!0):u=!1),u||Fa(n)),Ht(n),u=n.type,d=n.pendingProps,y=e!==null?e.memoizedProps:null,o=d.children,hd(u,d)?o=null:y!==null&&hd(u,y)&&(n.flags|=32),n.memoizedState!==null&&(u=df(e,n,Ey,null,null,a),Yo._currentValue=u),$l(e,n),Ln(e,n,o,a),n.child;case 6:return e===null&&be&&((e=a=Qe)&&(a=cM(a,n.pendingProps,xi),a!==null?(n.stateNode=a,Nn=n,Qe=null,e=!0):e=!1),e||Fa(n)),null;case 13:return sg(e,n,a);case 4:return ct(n,n.stateNode.containerInfo),o=n.pendingProps,e===null?n.child=Nr(n,null,o,a):Ln(e,n,o,a),n.child;case 11:return Q0(e,n,n.type,n.pendingProps,a);case 7:return Ln(e,n,n.pendingProps,a),n.child;case 8:return Ln(e,n,n.pendingProps.children,a),n.child;case 12:return Ln(e,n,n.pendingProps.children,a),n.child;case 10:return o=n.pendingProps,Ha(n,n.type,o.value),Ln(e,n,o.children,a),n.child;case 9:return u=n.type._context,o=n.pendingProps.children,Rr(n),u=Un(u),o=o(u),n.flags|=1,Ln(e,n,o,a),n.child;case 14:return J0(e,n,n.type,n.pendingProps,a);case 15:return $0(e,n,n.type,n.pendingProps,a);case 19:return lg(e,n,a);case 31:return Ny(e,n,a);case 22:return tg(e,n,a,n.pendingProps);case 24:return Rr(n),o=Un(gn),e===null?(u=ef(),u===null&&(u=je,d=$u(),u.pooledCache=d,d.refCount++,d!==null&&(u.pooledCacheLanes|=a),u=d),n.memoizedState={parent:o,cache:u},af(n),Ha(n,gn,u)):((e.lanes&a)!==0&&(rf(e,n),Ro(n,null,null,a),Ao()),u=e.memoizedState,d=n.memoizedState,u.parent!==o?(u={parent:o,cache:o},n.memoizedState=u,n.lanes===0&&(n.memoizedState=n.updateQueue.baseState=u),Ha(n,gn,o)):(o=d.cache,Ha(n,gn,o),o!==u.cache&&Ju(n,[gn],a,!0))),Ln(e,n,n.pendingProps.children,a),n.child;case 29:throw n.pendingProps}throw Error(r(156,n.tag))}function da(e){e.flags|=4}function Hf(e,n,a,o,u){if((n=(e.mode&32)!==0)&&(n=!1),n){if(e.flags|=16777216,(u&335544128)===u)if(e.stateNode.complete)e.flags|=8192;else if(Bg())e.flags|=8192;else throw Dr=zl,nf}else e.flags&=-16777217}function ug(e,n){if(n.type!=="stylesheet"||(n.state.loading&4)!==0)e.flags&=-16777217;else if(e.flags|=16777216,!T_(n))if(Bg())e.flags|=8192;else throw Dr=zl,nf}function ec(e,n){n!==null&&(e.flags|=4),e.flags&16384&&(n=e.tag!==22?Tt():536870912,e.lanes|=n,Es|=n)}function Lo(e,n){if(!be)switch(e.tailMode){case"hidden":n=e.tail;for(var a=null;n!==null;)n.alternate!==null&&(a=n),n=n.sibling;a===null?e.tail=null:a.sibling=null;break;case"collapsed":a=e.tail;for(var o=null;a!==null;)a.alternate!==null&&(o=a),a=a.sibling;o===null?n||e.tail===null?e.tail=null:e.tail.sibling=null:o.sibling=null}}function Je(e){var n=e.alternate!==null&&e.alternate.child===e.child,a=0,o=0;if(n)for(var u=e.child;u!==null;)a|=u.lanes|u.childLanes,o|=u.subtreeFlags&65011712,o|=u.flags&65011712,u.return=e,u=u.sibling;else for(u=e.child;u!==null;)a|=u.lanes|u.childLanes,o|=u.subtreeFlags,o|=u.flags,u.return=e,u=u.sibling;return e.subtreeFlags|=o,e.childLanes=a,n}function Ly(e,n,a){var o=n.pendingProps;switch(Yu(n),n.tag){case 16:case 15:case 0:case 11:case 7:case 8:case 12:case 9:case 14:return Je(n),null;case 1:return Je(n),null;case 3:return a=n.stateNode,o=null,e!==null&&(o=e.memoizedState.cache),n.memoizedState.cache!==o&&(n.flags|=2048),la(gn),bt(),a.pendingContext&&(a.context=a.pendingContext,a.pendingContext=null),(e===null||e.child===null)&&(us(n)?da(n):e===null||e.memoizedState.isDehydrated&&(n.flags&256)===0||(n.flags|=1024,ju())),Je(n),null;case 26:var u=n.type,d=n.memoizedState;return e===null?(da(n),d!==null?(Je(n),ug(n,d)):(Je(n),Hf(n,u,null,o,a))):d?d!==e.memoizedState?(da(n),Je(n),ug(n,d)):(Je(n),n.flags&=-16777217):(e=e.memoizedProps,e!==o&&da(n),Je(n),Hf(n,u,e,o,a)),null;case 27:if(Bt(n),a=it.current,u=n.type,e!==null&&n.stateNode!=null)e.memoizedProps!==o&&da(n);else{if(!o){if(n.stateNode===null)throw Error(r(166));return Je(n),null}e=At.current,us(n)?km(n):(e=__(u,o,a),n.stateNode=e,da(n))}return Je(n),null;case 5:if(Bt(n),u=n.type,e!==null&&n.stateNode!=null)e.memoizedProps!==o&&da(n);else{if(!o){if(n.stateNode===null)throw Error(r(166));return Je(n),null}if(d=At.current,us(n))km(n);else{var y=gc(it.current);switch(d){case 1:d=y.createElementNS("http://www.w3.org/2000/svg",u);break;case 2:d=y.createElementNS("http://www.w3.org/1998/Math/MathML",u);break;default:switch(u){case"svg":d=y.createElementNS("http://www.w3.org/2000/svg",u);break;case"math":d=y.createElementNS("http://www.w3.org/1998/Math/MathML",u);break;case"script":d=y.createElement("div"),d.innerHTML="<script><\/script>",d=d.removeChild(d.firstChild);break;case"select":d=typeof o.is=="string"?y.createElement("select",{is:o.is}):y.createElement("select"),o.multiple?d.multiple=!0:o.size&&(d.size=o.size);break;default:d=typeof o.is=="string"?y.createElement(u,{is:o.is}):y.createElement(u)}}d[pn]=n,d[Dn]=o;t:for(y=n.child;y!==null;){if(y.tag===5||y.tag===6)d.appendChild(y.stateNode);else if(y.tag!==4&&y.tag!==27&&y.child!==null){y.child.return=y,y=y.child;continue}if(y===n)break t;for(;y.sibling===null;){if(y.return===null||y.return===n)break t;y=y.return}y.sibling.return=y.return,y=y.sibling}n.stateNode=d;t:switch(On(d,u,o),u){case"button":case"input":case"select":case"textarea":o=!!o.autoFocus;break t;case"img":o=!0;break t;default:o=!1}o&&da(n)}}return Je(n),Hf(n,n.type,e===null?null:e.memoizedProps,n.pendingProps,a),null;case 6:if(e&&n.stateNode!=null)e.memoizedProps!==o&&da(n);else{if(typeof o!="string"&&n.stateNode===null)throw Error(r(166));if(e=it.current,us(n)){if(e=n.stateNode,a=n.memoizedProps,o=null,u=Nn,u!==null)switch(u.tag){case 27:case 5:o=u.memoizedProps}e[pn]=n,e=!!(e.nodeValue===a||o!==null&&o.suppressHydrationWarning===!0||s_(e.nodeValue,a)),e||Fa(n,!0)}else e=gc(e).createTextNode(o),e[pn]=n,n.stateNode=e}return Je(n),null;case 31:if(a=n.memoizedState,e===null||e.memoizedState!==null){if(o=us(n),a!==null){if(e===null){if(!o)throw Error(r(318));if(e=n.memoizedState,e=e!==null?e.dehydrated:null,!e)throw Error(r(557));e[pn]=n}else Tr(),(n.flags&128)===0&&(n.memoizedState=null),n.flags|=4;Je(n),e=!1}else a=ju(),e!==null&&e.memoizedState!==null&&(e.memoizedState.hydrationErrors=a),e=!0;if(!e)return n.flags&256?(oi(n),n):(oi(n),null);if((n.flags&128)!==0)throw Error(r(558))}return Je(n),null;case 13:if(o=n.memoizedState,e===null||e.memoizedState!==null&&e.memoizedState.dehydrated!==null){if(u=us(n),o!==null&&o.dehydrated!==null){if(e===null){if(!u)throw Error(r(318));if(u=n.memoizedState,u=u!==null?u.dehydrated:null,!u)throw Error(r(317));u[pn]=n}else Tr(),(n.flags&128)===0&&(n.memoizedState=null),n.flags|=4;Je(n),u=!1}else u=ju(),e!==null&&e.memoizedState!==null&&(e.memoizedState.hydrationErrors=u),u=!0;if(!u)return n.flags&256?(oi(n),n):(oi(n),null)}return oi(n),(n.flags&128)!==0?(n.lanes=a,n):(a=o!==null,e=e!==null&&e.memoizedState!==null,a&&(o=n.child,u=null,o.alternate!==null&&o.alternate.memoizedState!==null&&o.alternate.memoizedState.cachePool!==null&&(u=o.alternate.memoizedState.cachePool.pool),d=null,o.memoizedState!==null&&o.memoizedState.cachePool!==null&&(d=o.memoizedState.cachePool.pool),d!==u&&(o.flags|=2048)),a!==e&&a&&(n.child.flags|=8192),ec(n,n.updateQueue),Je(n),null);case 4:return bt(),e===null&&ld(n.stateNode.containerInfo),Je(n),null;case 10:return la(n.type),Je(n),null;case 19:if(Y(un),o=n.memoizedState,o===null)return Je(n),null;if(u=(n.flags&128)!==0,d=o.rendering,d===null)if(u)Lo(o,!1);else{if(cn!==0||e!==null&&(e.flags&128)!==0)for(e=n.child;e!==null;){if(d=Vl(e),d!==null){for(n.flags|=128,Lo(o,!1),e=d.updateQueue,n.updateQueue=e,ec(n,e),n.subtreeFlags=0,e=a,a=n.child;a!==null;)zm(a,e),a=a.sibling;return yt(un,un.current&1|2),be&&sa(n,o.treeForkCount),n.child}e=e.sibling}o.tail!==null&&ke()>sc&&(n.flags|=128,u=!0,Lo(o,!1),n.lanes=4194304)}else{if(!u)if(e=Vl(d),e!==null){if(n.flags|=128,u=!0,e=e.updateQueue,n.updateQueue=e,ec(n,e),Lo(o,!0),o.tail===null&&o.tailMode==="hidden"&&!d.alternate&&!be)return Je(n),null}else 2*ke()-o.renderingStartTime>sc&&a!==536870912&&(n.flags|=128,u=!0,Lo(o,!1),n.lanes=4194304);o.isBackwards?(d.sibling=n.child,n.child=d):(e=o.last,e!==null?e.sibling=d:n.child=d,o.last=d)}return o.tail!==null?(e=o.tail,o.rendering=e,o.tail=e.sibling,o.renderingStartTime=ke(),e.sibling=null,a=un.current,yt(un,u?a&1|2:a&1),be&&sa(n,o.treeForkCount),e):(Je(n),null);case 22:case 23:return oi(n),cf(),o=n.memoizedState!==null,e!==null?e.memoizedState!==null!==o&&(n.flags|=8192):o&&(n.flags|=8192),o?(a&536870912)!==0&&(n.flags&128)===0&&(Je(n),n.subtreeFlags&6&&(n.flags|=8192)):Je(n),a=n.updateQueue,a!==null&&ec(n,a.retryQueue),a=null,e!==null&&e.memoizedState!==null&&e.memoizedState.cachePool!==null&&(a=e.memoizedState.cachePool.pool),o=null,n.memoizedState!==null&&n.memoizedState.cachePool!==null&&(o=n.memoizedState.cachePool.pool),o!==a&&(n.flags|=2048),e!==null&&Y(Cr),null;case 24:return a=null,e!==null&&(a=e.memoizedState.cache),n.memoizedState.cache!==a&&(n.flags|=2048),la(gn),Je(n),null;case 25:return null;case 30:return null}throw Error(r(156,n.tag))}function Oy(e,n){switch(Yu(n),n.tag){case 1:return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 3:return la(gn),bt(),e=n.flags,(e&65536)!==0&&(e&128)===0?(n.flags=e&-65537|128,n):null;case 26:case 27:case 5:return Bt(n),null;case 31:if(n.memoizedState!==null){if(oi(n),n.alternate===null)throw Error(r(340));Tr()}return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 13:if(oi(n),e=n.memoizedState,e!==null&&e.dehydrated!==null){if(n.alternate===null)throw Error(r(340));Tr()}return e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 19:return Y(un),null;case 4:return bt(),null;case 10:return la(n.type),null;case 22:case 23:return oi(n),cf(),e!==null&&Y(Cr),e=n.flags,e&65536?(n.flags=e&-65537|128,n):null;case 24:return la(gn),null;case 25:return null;default:return null}}function fg(e,n){switch(Yu(n),n.tag){case 3:la(gn),bt();break;case 26:case 27:case 5:Bt(n);break;case 4:bt();break;case 31:n.memoizedState!==null&&oi(n);break;case 13:oi(n);break;case 19:Y(un);break;case 10:la(n.type);break;case 22:case 23:oi(n),cf(),e!==null&&Y(Cr);break;case 24:la(gn)}}function Oo(e,n){try{var a=n.updateQueue,o=a!==null?a.lastEffect:null;if(o!==null){var u=o.next;a=u;do{if((a.tag&e)===e){o=void 0;var d=a.create,y=a.inst;o=d(),y.destroy=o}a=a.next}while(a!==u)}}catch(w){Ge(n,n.return,w)}}function Wa(e,n,a){try{var o=n.updateQueue,u=o!==null?o.lastEffect:null;if(u!==null){var d=u.next;o=d;do{if((o.tag&e)===e){var y=o.inst,w=y.destroy;if(w!==void 0){y.destroy=void 0,u=n;var V=a,st=w;try{st()}catch(xt){Ge(u,V,xt)}}}o=o.next}while(o!==d)}}catch(xt){Ge(n,n.return,xt)}}function dg(e){var n=e.updateQueue;if(n!==null){var a=e.stateNode;try{n0(n,a)}catch(o){Ge(e,e.return,o)}}}function hg(e,n,a){a.props=Lr(e.type,e.memoizedProps),a.state=e.memoizedState;try{a.componentWillUnmount()}catch(o){Ge(e,n,o)}}function Po(e,n){try{var a=e.ref;if(a!==null){switch(e.tag){case 26:case 27:case 5:var o=e.stateNode;break;case 30:o=e.stateNode;break;default:o=e.stateNode}typeof a=="function"?e.refCleanup=a(o):a.current=o}}catch(u){Ge(e,n,u)}}function Hi(e,n){var a=e.ref,o=e.refCleanup;if(a!==null)if(typeof o=="function")try{o()}catch(u){Ge(e,n,u)}finally{e.refCleanup=null,e=e.alternate,e!=null&&(e.refCleanup=null)}else if(typeof a=="function")try{a(null)}catch(u){Ge(e,n,u)}else a.current=null}function pg(e){var n=e.type,a=e.memoizedProps,o=e.stateNode;try{t:switch(n){case"button":case"input":case"select":case"textarea":a.autoFocus&&o.focus();break t;case"img":a.src?o.src=a.src:a.srcSet&&(o.srcset=a.srcSet)}}catch(u){Ge(e,e.return,u)}}function Gf(e,n,a){try{var o=e.stateNode;nM(o,e.type,a,n),o[Dn]=n}catch(u){Ge(e,e.return,u)}}function mg(e){return e.tag===5||e.tag===3||e.tag===26||e.tag===27&&$a(e.type)||e.tag===4}function Vf(e){t:for(;;){for(;e.sibling===null;){if(e.return===null||mg(e.return))return null;e=e.return}for(e.sibling.return=e.return,e=e.sibling;e.tag!==5&&e.tag!==6&&e.tag!==18;){if(e.tag===27&&$a(e.type)||e.flags&2||e.child===null||e.tag===4)continue t;e.child.return=e,e=e.child}if(!(e.flags&2))return e.stateNode}}function kf(e,n,a){var o=e.tag;if(o===5||o===6)e=e.stateNode,n?(a.nodeType===9?a.body:a.nodeName==="HTML"?a.ownerDocument.body:a).insertBefore(e,n):(n=a.nodeType===9?a.body:a.nodeName==="HTML"?a.ownerDocument.body:a,n.appendChild(e),a=a._reactRootContainer,a!=null||n.onclick!==null||(n.onclick=ia));else if(o!==4&&(o===27&&$a(e.type)&&(a=e.stateNode,n=null),e=e.child,e!==null))for(kf(e,n,a),e=e.sibling;e!==null;)kf(e,n,a),e=e.sibling}function nc(e,n,a){var o=e.tag;if(o===5||o===6)e=e.stateNode,n?a.insertBefore(e,n):a.appendChild(e);else if(o!==4&&(o===27&&$a(e.type)&&(a=e.stateNode),e=e.child,e!==null))for(nc(e,n,a),e=e.sibling;e!==null;)nc(e,n,a),e=e.sibling}function gg(e){var n=e.stateNode,a=e.memoizedProps;try{for(var o=e.type,u=n.attributes;u.length;)n.removeAttributeNode(u[0]);On(n,o,a),n[pn]=e,n[Dn]=a}catch(d){Ge(e,e.return,d)}}var ha=!1,xn=!1,Xf=!1,_g=typeof WeakSet=="function"?WeakSet:Set,Rn=null;function Py(e,n){if(e=e.containerInfo,fd=Ec,e=wm(e),Bu(e)){if("selectionStart"in e)var a={start:e.selectionStart,end:e.selectionEnd};else t:{a=(a=e.ownerDocument)&&a.defaultView||window;var o=a.getSelection&&a.getSelection();if(o&&o.rangeCount!==0){a=o.anchorNode;var u=o.anchorOffset,d=o.focusNode;o=o.focusOffset;try{a.nodeType,d.nodeType}catch{a=null;break t}var y=0,w=-1,V=-1,st=0,xt=0,Et=e,dt=null;e:for(;;){for(var ht;Et!==a||u!==0&&Et.nodeType!==3||(w=y+u),Et!==d||o!==0&&Et.nodeType!==3||(V=y+o),Et.nodeType===3&&(y+=Et.nodeValue.length),(ht=Et.firstChild)!==null;)dt=Et,Et=ht;for(;;){if(Et===e)break e;if(dt===a&&++st===u&&(w=y),dt===d&&++xt===o&&(V=y),(ht=Et.nextSibling)!==null)break;Et=dt,dt=Et.parentNode}Et=ht}a=w===-1||V===-1?null:{start:w,end:V}}else a=null}a=a||{start:0,end:0}}else a=null;for(dd={focusedElem:e,selectionRange:a},Ec=!1,Rn=n;Rn!==null;)if(n=Rn,e=n.child,(n.subtreeFlags&1028)!==0&&e!==null)e.return=n,Rn=e;else for(;Rn!==null;){switch(n=Rn,d=n.alternate,e=n.flags,n.tag){case 0:if((e&4)!==0&&(e=n.updateQueue,e=e!==null?e.events:null,e!==null))for(a=0;a<e.length;a++)u=e[a],u.ref.impl=u.nextImpl;break;case 11:case 15:break;case 1:if((e&1024)!==0&&d!==null){e=void 0,a=n,u=d.memoizedProps,d=d.memoizedState,o=a.stateNode;try{var Zt=Lr(a.type,u);e=o.getSnapshotBeforeUpdate(Zt,d),o.__reactInternalSnapshotBeforeUpdate=e}catch(re){Ge(a,a.return,re)}}break;case 3:if((e&1024)!==0){if(e=n.stateNode.containerInfo,a=e.nodeType,a===9)md(e);else if(a===1)switch(e.nodeName){case"HEAD":case"HTML":case"BODY":md(e);break;default:e.textContent=""}}break;case 5:case 26:case 27:case 6:case 4:case 17:break;default:if((e&1024)!==0)throw Error(r(163))}if(e=n.sibling,e!==null){e.return=n.return,Rn=e;break}Rn=n.return}}function vg(e,n,a){var o=a.flags;switch(a.tag){case 0:case 11:case 15:ma(e,a),o&4&&Oo(5,a);break;case 1:if(ma(e,a),o&4)if(e=a.stateNode,n===null)try{e.componentDidMount()}catch(y){Ge(a,a.return,y)}else{var u=Lr(a.type,n.memoizedProps);n=n.memoizedState;try{e.componentDidUpdate(u,n,e.__reactInternalSnapshotBeforeUpdate)}catch(y){Ge(a,a.return,y)}}o&64&&dg(a),o&512&&Po(a,a.return);break;case 3:if(ma(e,a),o&64&&(e=a.updateQueue,e!==null)){if(n=null,a.child!==null)switch(a.child.tag){case 27:case 5:n=a.child.stateNode;break;case 1:n=a.child.stateNode}try{n0(e,n)}catch(y){Ge(a,a.return,y)}}break;case 27:n===null&&o&4&&gg(a);case 26:case 5:ma(e,a),n===null&&o&4&&pg(a),o&512&&Po(a,a.return);break;case 12:ma(e,a);break;case 31:ma(e,a),o&4&&yg(e,a);break;case 13:ma(e,a),o&4&&Mg(e,a),o&64&&(e=a.memoizedState,e!==null&&(e=e.dehydrated,e!==null&&(a=Xy.bind(null,a),uM(e,a))));break;case 22:if(o=a.memoizedState!==null||ha,!o){n=n!==null&&n.memoizedState!==null||xn,u=ha;var d=xn;ha=o,(xn=n)&&!d?ga(e,a,(a.subtreeFlags&8772)!==0):ma(e,a),ha=u,xn=d}break;case 30:break;default:ma(e,a)}}function xg(e){var n=e.alternate;n!==null&&(e.alternate=null,xg(n)),e.child=null,e.deletions=null,e.sibling=null,e.tag===5&&(n=e.stateNode,n!==null&&La(n)),e.stateNode=null,e.return=null,e.dependencies=null,e.memoizedProps=null,e.memoizedState=null,e.pendingProps=null,e.stateNode=null,e.updateQueue=null}var an=null,Yn=!1;function pa(e,n,a){for(a=a.child;a!==null;)Sg(e,n,a),a=a.sibling}function Sg(e,n,a){if(gt&&typeof gt.onCommitFiberUnmount=="function")try{gt.onCommitFiberUnmount(pt,a)}catch{}switch(a.tag){case 26:xn||Hi(a,n),pa(e,n,a),a.memoizedState?a.memoizedState.count--:a.stateNode&&(a=a.stateNode,a.parentNode.removeChild(a));break;case 27:xn||Hi(a,n);var o=an,u=Yn;$a(a.type)&&(an=a.stateNode,Yn=!1),pa(e,n,a),Xo(a.stateNode),an=o,Yn=u;break;case 5:xn||Hi(a,n);case 6:if(o=an,u=Yn,an=null,pa(e,n,a),an=o,Yn=u,an!==null)if(Yn)try{(an.nodeType===9?an.body:an.nodeName==="HTML"?an.ownerDocument.body:an).removeChild(a.stateNode)}catch(d){Ge(a,n,d)}else try{an.removeChild(a.stateNode)}catch(d){Ge(a,n,d)}break;case 18:an!==null&&(Yn?(e=an,d_(e.nodeType===9?e.body:e.nodeName==="HTML"?e.ownerDocument.body:e,a.stateNode),Ns(e)):d_(an,a.stateNode));break;case 4:o=an,u=Yn,an=a.stateNode.containerInfo,Yn=!0,pa(e,n,a),an=o,Yn=u;break;case 0:case 11:case 14:case 15:Wa(2,a,n),xn||Wa(4,a,n),pa(e,n,a);break;case 1:xn||(Hi(a,n),o=a.stateNode,typeof o.componentWillUnmount=="function"&&hg(a,n,o)),pa(e,n,a);break;case 21:pa(e,n,a);break;case 22:xn=(o=xn)||a.memoizedState!==null,pa(e,n,a),xn=o;break;default:pa(e,n,a)}}function yg(e,n){if(n.memoizedState===null&&(e=n.alternate,e!==null&&(e=e.memoizedState,e!==null))){e=e.dehydrated;try{Ns(e)}catch(a){Ge(n,n.return,a)}}}function Mg(e,n){if(n.memoizedState===null&&(e=n.alternate,e!==null&&(e=e.memoizedState,e!==null&&(e=e.dehydrated,e!==null))))try{Ns(e)}catch(a){Ge(n,n.return,a)}}function By(e){switch(e.tag){case 31:case 13:case 19:var n=e.stateNode;return n===null&&(n=e.stateNode=new _g),n;case 22:return e=e.stateNode,n=e._retryCache,n===null&&(n=e._retryCache=new _g),n;default:throw Error(r(435,e.tag))}}function ic(e,n){var a=By(e);n.forEach(function(o){if(!a.has(o)){a.add(o);var u=qy.bind(null,e,o);o.then(u,u)}})}function Zn(e,n){var a=n.deletions;if(a!==null)for(var o=0;o<a.length;o++){var u=a[o],d=e,y=n,w=y;t:for(;w!==null;){switch(w.tag){case 27:if($a(w.type)){an=w.stateNode,Yn=!1;break t}break;case 5:an=w.stateNode,Yn=!1;break t;case 3:case 4:an=w.stateNode.containerInfo,Yn=!0;break t}w=w.return}if(an===null)throw Error(r(160));Sg(d,y,u),an=null,Yn=!1,d=u.alternate,d!==null&&(d.return=null),u.return=null}if(n.subtreeFlags&13886)for(n=n.child;n!==null;)Eg(n,e),n=n.sibling}var wi=null;function Eg(e,n){var a=e.alternate,o=e.flags;switch(e.tag){case 0:case 11:case 14:case 15:Zn(n,e),jn(e),o&4&&(Wa(3,e,e.return),Oo(3,e),Wa(5,e,e.return));break;case 1:Zn(n,e),jn(e),o&512&&(xn||a===null||Hi(a,a.return)),o&64&&ha&&(e=e.updateQueue,e!==null&&(o=e.callbacks,o!==null&&(a=e.shared.hiddenCallbacks,e.shared.hiddenCallbacks=a===null?o:a.concat(o))));break;case 26:var u=wi;if(Zn(n,e),jn(e),o&512&&(xn||a===null||Hi(a,a.return)),o&4){var d=a!==null?a.memoizedState:null;if(o=e.memoizedState,a===null)if(o===null)if(e.stateNode===null){t:{o=e.type,a=e.memoizedProps,u=u.ownerDocument||u;e:switch(o){case"title":d=u.getElementsByTagName("title")[0],(!d||d[Ua]||d[pn]||d.namespaceURI==="http://www.w3.org/2000/svg"||d.hasAttribute("itemprop"))&&(d=u.createElement(o),u.head.insertBefore(d,u.querySelector("head > title"))),On(d,o,a),d[pn]=e,mn(d),o=d;break t;case"link":var y=E_("link","href",u).get(o+(a.href||""));if(y){for(var w=0;w<y.length;w++)if(d=y[w],d.getAttribute("href")===(a.href==null||a.href===""?null:a.href)&&d.getAttribute("rel")===(a.rel==null?null:a.rel)&&d.getAttribute("title")===(a.title==null?null:a.title)&&d.getAttribute("crossorigin")===(a.crossOrigin==null?null:a.crossOrigin)){y.splice(w,1);break e}}d=u.createElement(o),On(d,o,a),u.head.appendChild(d);break;case"meta":if(y=E_("meta","content",u).get(o+(a.content||""))){for(w=0;w<y.length;w++)if(d=y[w],d.getAttribute("content")===(a.content==null?null:""+a.content)&&d.getAttribute("name")===(a.name==null?null:a.name)&&d.getAttribute("property")===(a.property==null?null:a.property)&&d.getAttribute("http-equiv")===(a.httpEquiv==null?null:a.httpEquiv)&&d.getAttribute("charset")===(a.charSet==null?null:a.charSet)){y.splice(w,1);break e}}d=u.createElement(o),On(d,o,a),u.head.appendChild(d);break;default:throw Error(r(468,o))}d[pn]=e,mn(d),o=d}e.stateNode=o}else b_(u,e.type,e.stateNode);else e.stateNode=M_(u,o,e.memoizedProps);else d!==o?(d===null?a.stateNode!==null&&(a=a.stateNode,a.parentNode.removeChild(a)):d.count--,o===null?b_(u,e.type,e.stateNode):M_(u,o,e.memoizedProps)):o===null&&e.stateNode!==null&&Gf(e,e.memoizedProps,a.memoizedProps)}break;case 27:Zn(n,e),jn(e),o&512&&(xn||a===null||Hi(a,a.return)),a!==null&&o&4&&Gf(e,e.memoizedProps,a.memoizedProps);break;case 5:if(Zn(n,e),jn(e),o&512&&(xn||a===null||Hi(a,a.return)),e.flags&32){u=e.stateNode;try{ii(u,"")}catch(Zt){Ge(e,e.return,Zt)}}o&4&&e.stateNode!=null&&(u=e.memoizedProps,Gf(e,u,a!==null?a.memoizedProps:u)),o&1024&&(Xf=!0);break;case 6:if(Zn(n,e),jn(e),o&4){if(e.stateNode===null)throw Error(r(162));o=e.memoizedProps,a=e.stateNode;try{a.nodeValue=o}catch(Zt){Ge(e,e.return,Zt)}}break;case 3:if(xc=null,u=wi,wi=_c(n.containerInfo),Zn(n,e),wi=u,jn(e),o&4&&a!==null&&a.memoizedState.isDehydrated)try{Ns(n.containerInfo)}catch(Zt){Ge(e,e.return,Zt)}Xf&&(Xf=!1,bg(e));break;case 4:o=wi,wi=_c(e.stateNode.containerInfo),Zn(n,e),jn(e),wi=o;break;case 12:Zn(n,e),jn(e);break;case 31:Zn(n,e),jn(e),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,ic(e,o)));break;case 13:Zn(n,e),jn(e),e.child.flags&8192&&e.memoizedState!==null!=(a!==null&&a.memoizedState!==null)&&(rc=ke()),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,ic(e,o)));break;case 22:u=e.memoizedState!==null;var V=a!==null&&a.memoizedState!==null,st=ha,xt=xn;if(ha=st||u,xn=xt||V,Zn(n,e),xn=xt,ha=st,jn(e),o&8192)t:for(n=e.stateNode,n._visibility=u?n._visibility&-2:n._visibility|1,u&&(a===null||V||ha||xn||Or(e)),a=null,n=e;;){if(n.tag===5||n.tag===26){if(a===null){V=a=n;try{if(d=V.stateNode,u)y=d.style,typeof y.setProperty=="function"?y.setProperty("display","none","important"):y.display="none";else{w=V.stateNode;var Et=V.memoizedProps.style,dt=Et!=null&&Et.hasOwnProperty("display")?Et.display:null;w.style.display=dt==null||typeof dt=="boolean"?"":(""+dt).trim()}}catch(Zt){Ge(V,V.return,Zt)}}}else if(n.tag===6){if(a===null){V=n;try{V.stateNode.nodeValue=u?"":V.memoizedProps}catch(Zt){Ge(V,V.return,Zt)}}}else if(n.tag===18){if(a===null){V=n;try{var ht=V.stateNode;u?h_(ht,!0):h_(V.stateNode,!1)}catch(Zt){Ge(V,V.return,Zt)}}}else if((n.tag!==22&&n.tag!==23||n.memoizedState===null||n===e)&&n.child!==null){n.child.return=n,n=n.child;continue}if(n===e)break t;for(;n.sibling===null;){if(n.return===null||n.return===e)break t;a===n&&(a=null),n=n.return}a===n&&(a=null),n.sibling.return=n.return,n=n.sibling}o&4&&(o=e.updateQueue,o!==null&&(a=o.retryQueue,a!==null&&(o.retryQueue=null,ic(e,a))));break;case 19:Zn(n,e),jn(e),o&4&&(o=e.updateQueue,o!==null&&(e.updateQueue=null,ic(e,o)));break;case 30:break;case 21:break;default:Zn(n,e),jn(e)}}function jn(e){var n=e.flags;if(n&2){try{for(var a,o=e.return;o!==null;){if(mg(o)){a=o;break}o=o.return}if(a==null)throw Error(r(160));switch(a.tag){case 27:var u=a.stateNode,d=Vf(e);nc(e,d,u);break;case 5:var y=a.stateNode;a.flags&32&&(ii(y,""),a.flags&=-33);var w=Vf(e);nc(e,w,y);break;case 3:case 4:var V=a.stateNode.containerInfo,st=Vf(e);kf(e,st,V);break;default:throw Error(r(161))}}catch(xt){Ge(e,e.return,xt)}e.flags&=-3}n&4096&&(e.flags&=-4097)}function bg(e){if(e.subtreeFlags&1024)for(e=e.child;e!==null;){var n=e;bg(n),n.tag===5&&n.flags&1024&&n.stateNode.reset(),e=e.sibling}}function ma(e,n){if(n.subtreeFlags&8772)for(n=n.child;n!==null;)vg(e,n.alternate,n),n=n.sibling}function Or(e){for(e=e.child;e!==null;){var n=e;switch(n.tag){case 0:case 11:case 14:case 15:Wa(4,n,n.return),Or(n);break;case 1:Hi(n,n.return);var a=n.stateNode;typeof a.componentWillUnmount=="function"&&hg(n,n.return,a),Or(n);break;case 27:Xo(n.stateNode);case 26:case 5:Hi(n,n.return),Or(n);break;case 22:n.memoizedState===null&&Or(n);break;case 30:Or(n);break;default:Or(n)}e=e.sibling}}function ga(e,n,a){for(a=a&&(n.subtreeFlags&8772)!==0,n=n.child;n!==null;){var o=n.alternate,u=e,d=n,y=d.flags;switch(d.tag){case 0:case 11:case 15:ga(u,d,a),Oo(4,d);break;case 1:if(ga(u,d,a),o=d,u=o.stateNode,typeof u.componentDidMount=="function")try{u.componentDidMount()}catch(st){Ge(o,o.return,st)}if(o=d,u=o.updateQueue,u!==null){var w=o.stateNode;try{var V=u.shared.hiddenCallbacks;if(V!==null)for(u.shared.hiddenCallbacks=null,u=0;u<V.length;u++)e0(V[u],w)}catch(st){Ge(o,o.return,st)}}a&&y&64&&dg(d),Po(d,d.return);break;case 27:gg(d);case 26:case 5:ga(u,d,a),a&&o===null&&y&4&&pg(d),Po(d,d.return);break;case 12:ga(u,d,a);break;case 31:ga(u,d,a),a&&y&4&&yg(u,d);break;case 13:ga(u,d,a),a&&y&4&&Mg(u,d);break;case 22:d.memoizedState===null&&ga(u,d,a),Po(d,d.return);break;case 30:break;default:ga(u,d,a)}n=n.sibling}}function qf(e,n){var a=null;e!==null&&e.memoizedState!==null&&e.memoizedState.cachePool!==null&&(a=e.memoizedState.cachePool.pool),e=null,n.memoizedState!==null&&n.memoizedState.cachePool!==null&&(e=n.memoizedState.cachePool.pool),e!==a&&(e!=null&&e.refCount++,a!=null&&yo(a))}function Wf(e,n){e=null,n.alternate!==null&&(e=n.alternate.memoizedState.cache),n=n.memoizedState.cache,n!==e&&(n.refCount++,e!=null&&yo(e))}function Di(e,n,a,o){if(n.subtreeFlags&10256)for(n=n.child;n!==null;)Tg(e,n,a,o),n=n.sibling}function Tg(e,n,a,o){var u=n.flags;switch(n.tag){case 0:case 11:case 15:Di(e,n,a,o),u&2048&&Oo(9,n);break;case 1:Di(e,n,a,o);break;case 3:Di(e,n,a,o),u&2048&&(e=null,n.alternate!==null&&(e=n.alternate.memoizedState.cache),n=n.memoizedState.cache,n!==e&&(n.refCount++,e!=null&&yo(e)));break;case 12:if(u&2048){Di(e,n,a,o),e=n.stateNode;try{var d=n.memoizedProps,y=d.id,w=d.onPostCommit;typeof w=="function"&&w(y,n.alternate===null?"mount":"update",e.passiveEffectDuration,-0)}catch(V){Ge(n,n.return,V)}}else Di(e,n,a,o);break;case 31:Di(e,n,a,o);break;case 13:Di(e,n,a,o);break;case 23:break;case 22:d=n.stateNode,y=n.alternate,n.memoizedState!==null?d._visibility&2?Di(e,n,a,o):Bo(e,n):d._visibility&2?Di(e,n,a,o):(d._visibility|=2,Ss(e,n,a,o,(n.subtreeFlags&10256)!==0||!1)),u&2048&&qf(y,n);break;case 24:Di(e,n,a,o),u&2048&&Wf(n.alternate,n);break;default:Di(e,n,a,o)}}function Ss(e,n,a,o,u){for(u=u&&((n.subtreeFlags&10256)!==0||!1),n=n.child;n!==null;){var d=e,y=n,w=a,V=o,st=y.flags;switch(y.tag){case 0:case 11:case 15:Ss(d,y,w,V,u),Oo(8,y);break;case 23:break;case 22:var xt=y.stateNode;y.memoizedState!==null?xt._visibility&2?Ss(d,y,w,V,u):Bo(d,y):(xt._visibility|=2,Ss(d,y,w,V,u)),u&&st&2048&&qf(y.alternate,y);break;case 24:Ss(d,y,w,V,u),u&&st&2048&&Wf(y.alternate,y);break;default:Ss(d,y,w,V,u)}n=n.sibling}}function Bo(e,n){if(n.subtreeFlags&10256)for(n=n.child;n!==null;){var a=e,o=n,u=o.flags;switch(o.tag){case 22:Bo(a,o),u&2048&&qf(o.alternate,o);break;case 24:Bo(a,o),u&2048&&Wf(o.alternate,o);break;default:Bo(a,o)}n=n.sibling}}var Io=8192;function ys(e,n,a){if(e.subtreeFlags&Io)for(e=e.child;e!==null;)Ag(e,n,a),e=e.sibling}function Ag(e,n,a){switch(e.tag){case 26:ys(e,n,a),e.flags&Io&&e.memoizedState!==null&&MM(a,wi,e.memoizedState,e.memoizedProps);break;case 5:ys(e,n,a);break;case 3:case 4:var o=wi;wi=_c(e.stateNode.containerInfo),ys(e,n,a),wi=o;break;case 22:e.memoizedState===null&&(o=e.alternate,o!==null&&o.memoizedState!==null?(o=Io,Io=16777216,ys(e,n,a),Io=o):ys(e,n,a));break;default:ys(e,n,a)}}function Rg(e){var n=e.alternate;if(n!==null&&(e=n.child,e!==null)){n.child=null;do n=e.sibling,e.sibling=null,e=n;while(e!==null)}}function zo(e){var n=e.deletions;if((e.flags&16)!==0){if(n!==null)for(var a=0;a<n.length;a++){var o=n[a];Rn=o,wg(o,e)}Rg(e)}if(e.subtreeFlags&10256)for(e=e.child;e!==null;)Cg(e),e=e.sibling}function Cg(e){switch(e.tag){case 0:case 11:case 15:zo(e),e.flags&2048&&Wa(9,e,e.return);break;case 3:zo(e);break;case 12:zo(e);break;case 22:var n=e.stateNode;e.memoizedState!==null&&n._visibility&2&&(e.return===null||e.return.tag!==13)?(n._visibility&=-3,ac(e)):zo(e);break;default:zo(e)}}function ac(e){var n=e.deletions;if((e.flags&16)!==0){if(n!==null)for(var a=0;a<n.length;a++){var o=n[a];Rn=o,wg(o,e)}Rg(e)}for(e=e.child;e!==null;){switch(n=e,n.tag){case 0:case 11:case 15:Wa(8,n,n.return),ac(n);break;case 22:a=n.stateNode,a._visibility&2&&(a._visibility&=-3,ac(n));break;default:ac(n)}e=e.sibling}}function wg(e,n){for(;Rn!==null;){var a=Rn;switch(a.tag){case 0:case 11:case 15:Wa(8,a,n);break;case 23:case 22:if(a.memoizedState!==null&&a.memoizedState.cachePool!==null){var o=a.memoizedState.cachePool.pool;o!=null&&o.refCount++}break;case 24:yo(a.memoizedState.cache)}if(o=a.child,o!==null)o.return=a,Rn=o;else t:for(a=e;Rn!==null;){o=Rn;var u=o.sibling,d=o.return;if(xg(o),o===a){Rn=null;break t}if(u!==null){u.return=d,Rn=u;break t}Rn=d}}}var Iy={getCacheForType:function(e){var n=Un(gn),a=n.data.get(e);return a===void 0&&(a=e(),n.data.set(e,a)),a},cacheSignal:function(){return Un(gn).controller.signal}},zy=typeof WeakMap=="function"?WeakMap:Map,Oe=0,je=null,xe=null,ye=0,He=0,li=null,Ya=!1,Ms=!1,Yf=!1,_a=0,cn=0,Za=0,Pr=0,Zf=0,ci=0,Es=0,Fo=null,Kn=null,jf=!1,rc=0,Dg=0,sc=1/0,oc=null,ja=null,Mn=0,Ka=null,bs=null,va=0,Kf=0,Qf=null,Ng=null,Ho=0,Jf=null;function ui(){return(Oe&2)!==0&&ye!==0?ye&-ye:I.T!==null?ad():lo()}function Ug(){if(ci===0)if((ye&536870912)===0||be){var e=oe;oe<<=1,(oe&3932160)===0&&(oe=262144),ci=e}else ci=536870912;return e=si.current,e!==null&&(e.flags|=32),ci}function Qn(e,n,a){(e===je&&(He===2||He===9)||e.cancelPendingCommit!==null)&&(Ts(e,0),Qa(e,ye,ci,!1)),qt(e,a),((Oe&2)===0||e!==je)&&(e===je&&((Oe&2)===0&&(Pr|=a),cn===4&&Qa(e,ye,ci,!1)),Gi(e))}function Lg(e,n,a){if((Oe&6)!==0)throw Error(r(327));var o=!a&&(n&127)===0&&(n&e.expiredLanes)===0||Nt(e,n),u=o?Gy(e,n):td(e,n,!0),d=o;do{if(u===0){Ms&&!o&&Qa(e,n,0,!1);break}else{if(a=e.current.alternate,d&&!Fy(a)){u=td(e,n,!1),d=!1;continue}if(u===2){if(d=n,e.errorRecoveryDisabledLanes&d)var y=0;else y=e.pendingLanes&-536870913,y=y!==0?y:y&536870912?536870912:0;if(y!==0){n=y;t:{var w=e;u=Fo;var V=w.current.memoizedState.isDehydrated;if(V&&(Ts(w,y).flags|=256),y=td(w,y,!1),y!==2){if(Yf&&!V){w.errorRecoveryDisabledLanes|=d,Pr|=d,u=4;break t}d=Kn,Kn=u,d!==null&&(Kn===null?Kn=d:Kn.push.apply(Kn,d))}u=y}if(d=!1,u!==2)continue}}if(u===1){Ts(e,0),Qa(e,n,0,!0);break}t:{switch(o=e,d=u,d){case 0:case 1:throw Error(r(345));case 4:if((n&4194048)!==n)break;case 6:Qa(o,n,ci,!Ya);break t;case 2:Kn=null;break;case 3:case 5:break;default:throw Error(r(329))}if((n&62914560)===n&&(u=rc+300-ke(),10<u)){if(Qa(o,n,ci,!Ya),St(o,0,!0)!==0)break t;va=n,o.timeoutHandle=u_(Og.bind(null,o,a,Kn,oc,jf,n,ci,Pr,Es,Ya,d,"Throttled",-0,0),u);break t}Og(o,a,Kn,oc,jf,n,ci,Pr,Es,Ya,d,null,-0,0)}}break}while(!0);Gi(e)}function Og(e,n,a,o,u,d,y,w,V,st,xt,Et,dt,ht){if(e.timeoutHandle=-1,Et=n.subtreeFlags,Et&8192||(Et&16785408)===16785408){Et={stylesheets:null,count:0,imgCount:0,imgBytes:0,suspenseyImages:[],waitingForImages:!0,waitingForViewTransition:!1,unsuspend:ia},Ag(n,d,Et);var Zt=(d&62914560)===d?rc-ke():(d&4194048)===d?Dg-ke():0;if(Zt=EM(Et,Zt),Zt!==null){va=d,e.cancelPendingCommit=Zt(Vg.bind(null,e,n,d,a,o,u,y,w,V,xt,Et,null,dt,ht)),Qa(e,d,y,!st);return}}Vg(e,n,d,a,o,u,y,w,V)}function Fy(e){for(var n=e;;){var a=n.tag;if((a===0||a===11||a===15)&&n.flags&16384&&(a=n.updateQueue,a!==null&&(a=a.stores,a!==null)))for(var o=0;o<a.length;o++){var u=a[o],d=u.getSnapshot;u=u.value;try{if(!ai(d(),u))return!1}catch{return!1}}if(a=n.child,n.subtreeFlags&16384&&a!==null)a.return=n,n=a;else{if(n===e)break;for(;n.sibling===null;){if(n.return===null||n.return===e)return!0;n=n.return}n.sibling.return=n.return,n=n.sibling}}return!0}function Qa(e,n,a,o){n&=~Zf,n&=~Pr,e.suspendedLanes|=n,e.pingedLanes&=~n,o&&(e.warmLanes|=n),o=e.expirationTimes;for(var u=n;0<u;){var d=31-Gt(u),y=1<<d;o[d]=-1,u&=~y}a!==0&&Be(e,a,n)}function lc(){return(Oe&6)===0?(Go(0),!1):!0}function $f(){if(xe!==null){if(He===0)var e=xe.return;else e=xe,oa=Ar=null,mf(e),ms=null,Eo=0,e=xe;for(;e!==null;)fg(e.alternate,e),e=e.return;xe=null}}function Ts(e,n){var a=e.timeoutHandle;a!==-1&&(e.timeoutHandle=-1,rM(a)),a=e.cancelPendingCommit,a!==null&&(e.cancelPendingCommit=null,a()),va=0,$f(),je=e,xe=a=ra(e.current,null),ye=n,He=0,li=null,Ya=!1,Ms=Nt(e,n),Yf=!1,Es=ci=Zf=Pr=Za=cn=0,Kn=Fo=null,jf=!1,(n&8)!==0&&(n|=n&32);var o=e.entangledLanes;if(o!==0)for(e=e.entanglements,o&=n;0<o;){var u=31-Gt(o),d=1<<u;n|=e[u],o&=~d}return _a=n,wl(),a}function Pg(e,n){fe=null,I.H=No,n===ps||n===Il?(n=Qm(),He=3):n===nf?(n=Qm(),He=4):He=n===Nf?8:n!==null&&typeof n=="object"&&typeof n.then=="function"?6:1,li=n,xe===null&&(cn=1,Ql(e,gi(n,e.current)))}function Bg(){var e=si.current;return e===null?!0:(ye&4194048)===ye?Si===null:(ye&62914560)===ye||(ye&536870912)!==0?e===Si:!1}function Ig(){var e=I.H;return I.H=No,e===null?No:e}function zg(){var e=I.A;return I.A=Iy,e}function cc(){cn=4,Ya||(ye&4194048)!==ye&&si.current!==null||(Ms=!0),(Za&134217727)===0&&(Pr&134217727)===0||je===null||Qa(je,ye,ci,!1)}function td(e,n,a){var o=Oe;Oe|=2;var u=Ig(),d=zg();(je!==e||ye!==n)&&(oc=null,Ts(e,n)),n=!1;var y=cn;t:do try{if(He!==0&&xe!==null){var w=xe,V=li;switch(He){case 8:$f(),y=6;break t;case 3:case 2:case 9:case 6:si.current===null&&(n=!0);var st=He;if(He=0,li=null,As(e,w,V,st),a&&Ms){y=0;break t}break;default:st=He,He=0,li=null,As(e,w,V,st)}}Hy(),y=cn;break}catch(xt){Pg(e,xt)}while(!0);return n&&e.shellSuspendCounter++,oa=Ar=null,Oe=o,I.H=u,I.A=d,xe===null&&(je=null,ye=0,wl()),y}function Hy(){for(;xe!==null;)Fg(xe)}function Gy(e,n){var a=Oe;Oe|=2;var o=Ig(),u=zg();je!==e||ye!==n?(oc=null,sc=ke()+500,Ts(e,n)):Ms=Nt(e,n);t:do try{if(He!==0&&xe!==null){n=xe;var d=li;e:switch(He){case 1:He=0,li=null,As(e,n,d,1);break;case 2:case 9:if(jm(d)){He=0,li=null,Hg(n);break}n=function(){He!==2&&He!==9||je!==e||(He=7),Gi(e)},d.then(n,n);break t;case 3:He=7;break t;case 4:He=5;break t;case 7:jm(d)?(He=0,li=null,Hg(n)):(He=0,li=null,As(e,n,d,7));break;case 5:var y=null;switch(xe.tag){case 26:y=xe.memoizedState;case 5:case 27:var w=xe;if(y?T_(y):w.stateNode.complete){He=0,li=null;var V=w.sibling;if(V!==null)xe=V;else{var st=w.return;st!==null?(xe=st,uc(st)):xe=null}break e}}He=0,li=null,As(e,n,d,5);break;case 6:He=0,li=null,As(e,n,d,6);break;case 8:$f(),cn=6;break t;default:throw Error(r(462))}}Vy();break}catch(xt){Pg(e,xt)}while(!0);return oa=Ar=null,I.H=o,I.A=u,Oe=a,xe!==null?0:(je=null,ye=0,wl(),cn)}function Vy(){for(;xe!==null&&!on();)Fg(xe)}function Fg(e){var n=cg(e.alternate,e,_a);e.memoizedProps=e.pendingProps,n===null?uc(e):xe=n}function Hg(e){var n=e,a=n.alternate;switch(n.tag){case 15:case 0:n=ig(a,n,n.pendingProps,n.type,void 0,ye);break;case 11:n=ig(a,n,n.pendingProps,n.type.render,n.ref,ye);break;case 5:mf(n);default:fg(a,n),n=xe=zm(n,_a),n=cg(a,n,_a)}e.memoizedProps=e.pendingProps,n===null?uc(e):xe=n}function As(e,n,a,o){oa=Ar=null,mf(n),ms=null,Eo=0;var u=n.return;try{if(Dy(e,u,n,a,ye)){cn=1,Ql(e,gi(a,e.current)),xe=null;return}}catch(d){if(u!==null)throw xe=u,d;cn=1,Ql(e,gi(a,e.current)),xe=null;return}n.flags&32768?(be||o===1?e=!0:Ms||(ye&536870912)!==0?e=!1:(Ya=e=!0,(o===2||o===9||o===3||o===6)&&(o=si.current,o!==null&&o.tag===13&&(o.flags|=16384))),Gg(n,e)):uc(n)}function uc(e){var n=e;do{if((n.flags&32768)!==0){Gg(n,Ya);return}e=n.return;var a=Ly(n.alternate,n,_a);if(a!==null){xe=a;return}if(n=n.sibling,n!==null){xe=n;return}xe=n=e}while(n!==null);cn===0&&(cn=5)}function Gg(e,n){do{var a=Oy(e.alternate,e);if(a!==null){a.flags&=32767,xe=a;return}if(a=e.return,a!==null&&(a.flags|=32768,a.subtreeFlags=0,a.deletions=null),!n&&(e=e.sibling,e!==null)){xe=e;return}xe=e=a}while(e!==null);cn=6,xe=null}function Vg(e,n,a,o,u,d,y,w,V){e.cancelPendingCommit=null;do fc();while(Mn!==0);if((Oe&6)!==0)throw Error(r(327));if(n!==null){if(n===e.current)throw Error(r(177));if(d=n.lanes|n.childLanes,d|=Gu,tn(e,a,d,y,w,V),e===je&&(xe=je=null,ye=0),bs=n,Ka=e,va=a,Kf=d,Qf=u,Ng=o,(n.subtreeFlags&10256)!==0||(n.flags&10256)!==0?(e.callbackNode=null,e.callbackPriority=0,Wy(et,function(){return Yg(),null})):(e.callbackNode=null,e.callbackPriority=0),o=(n.flags&13878)!==0,(n.subtreeFlags&13878)!==0||o){o=I.T,I.T=null,u=X.p,X.p=2,y=Oe,Oe|=4;try{Py(e,n,a)}finally{Oe=y,X.p=u,I.T=o}}Mn=1,kg(),Xg(),qg()}}function kg(){if(Mn===1){Mn=0;var e=Ka,n=bs,a=(n.flags&13878)!==0;if((n.subtreeFlags&13878)!==0||a){a=I.T,I.T=null;var o=X.p;X.p=2;var u=Oe;Oe|=4;try{Eg(n,e);var d=dd,y=wm(e.containerInfo),w=d.focusedElem,V=d.selectionRange;if(y!==w&&w&&w.ownerDocument&&Cm(w.ownerDocument.documentElement,w)){if(V!==null&&Bu(w)){var st=V.start,xt=V.end;if(xt===void 0&&(xt=st),"selectionStart"in w)w.selectionStart=st,w.selectionEnd=Math.min(xt,w.value.length);else{var Et=w.ownerDocument||document,dt=Et&&Et.defaultView||window;if(dt.getSelection){var ht=dt.getSelection(),Zt=w.textContent.length,re=Math.min(V.start,Zt),We=V.end===void 0?re:Math.min(V.end,Zt);!ht.extend&&re>We&&(y=We,We=re,re=y);var J=Rm(w,re),q=Rm(w,We);if(J&&q&&(ht.rangeCount!==1||ht.anchorNode!==J.node||ht.anchorOffset!==J.offset||ht.focusNode!==q.node||ht.focusOffset!==q.offset)){var rt=Et.createRange();rt.setStart(J.node,J.offset),ht.removeAllRanges(),re>We?(ht.addRange(rt),ht.extend(q.node,q.offset)):(rt.setEnd(q.node,q.offset),ht.addRange(rt))}}}}for(Et=[],ht=w;ht=ht.parentNode;)ht.nodeType===1&&Et.push({element:ht,left:ht.scrollLeft,top:ht.scrollTop});for(typeof w.focus=="function"&&w.focus(),w=0;w<Et.length;w++){var Mt=Et[w];Mt.element.scrollLeft=Mt.left,Mt.element.scrollTop=Mt.top}}Ec=!!fd,dd=fd=null}finally{Oe=u,X.p=o,I.T=a}}e.current=n,Mn=2}}function Xg(){if(Mn===2){Mn=0;var e=Ka,n=bs,a=(n.flags&8772)!==0;if((n.subtreeFlags&8772)!==0||a){a=I.T,I.T=null;var o=X.p;X.p=2;var u=Oe;Oe|=4;try{vg(e,n.alternate,n)}finally{Oe=u,X.p=o,I.T=a}}Mn=3}}function qg(){if(Mn===4||Mn===3){Mn=0,j();var e=Ka,n=bs,a=va,o=Ng;(n.subtreeFlags&10256)!==0||(n.flags&10256)!==0?Mn=5:(Mn=0,bs=Ka=null,Wg(e,e.pendingLanes));var u=e.pendingLanes;if(u===0&&(ja=null),oo(a),n=n.stateNode,gt&&typeof gt.onCommitFiberRoot=="function")try{gt.onCommitFiberRoot(pt,n,void 0,(n.current.flags&128)===128)}catch{}if(o!==null){n=I.T,u=X.p,X.p=2,I.T=null;try{for(var d=e.onRecoverableError,y=0;y<o.length;y++){var w=o[y];d(w.value,{componentStack:w.stack})}}finally{I.T=n,X.p=u}}(va&3)!==0&&fc(),Gi(e),u=e.pendingLanes,(a&261930)!==0&&(u&42)!==0?e===Jf?Ho++:(Ho=0,Jf=e):Ho=0,Go(0)}}function Wg(e,n){(e.pooledCacheLanes&=n)===0&&(n=e.pooledCache,n!=null&&(e.pooledCache=null,yo(n)))}function fc(){return kg(),Xg(),qg(),Yg()}function Yg(){if(Mn!==5)return!1;var e=Ka,n=Kf;Kf=0;var a=oo(va),o=I.T,u=X.p;try{X.p=32>a?32:a,I.T=null,a=Qf,Qf=null;var d=Ka,y=va;if(Mn=0,bs=Ka=null,va=0,(Oe&6)!==0)throw Error(r(331));var w=Oe;if(Oe|=4,Cg(d.current),Tg(d,d.current,y,a),Oe=w,Go(0,!1),gt&&typeof gt.onPostCommitFiberRoot=="function")try{gt.onPostCommitFiberRoot(pt,d)}catch{}return!0}finally{X.p=u,I.T=o,Wg(e,n)}}function Zg(e,n,a){n=gi(a,n),n=Df(e.stateNode,n,2),e=ka(e,n,2),e!==null&&(qt(e,2),Gi(e))}function Ge(e,n,a){if(e.tag===3)Zg(e,e,a);else for(;n!==null;){if(n.tag===3){Zg(n,e,a);break}else if(n.tag===1){var o=n.stateNode;if(typeof n.type.getDerivedStateFromError=="function"||typeof o.componentDidCatch=="function"&&(ja===null||!ja.has(o))){e=gi(a,e),a=j0(2),o=ka(n,a,2),o!==null&&(K0(a,o,n,e),qt(o,2),Gi(o));break}}n=n.return}}function ed(e,n,a){var o=e.pingCache;if(o===null){o=e.pingCache=new zy;var u=new Set;o.set(n,u)}else u=o.get(n),u===void 0&&(u=new Set,o.set(n,u));u.has(a)||(Yf=!0,u.add(a),e=ky.bind(null,e,n,a),n.then(e,e))}function ky(e,n,a){var o=e.pingCache;o!==null&&o.delete(n),e.pingedLanes|=e.suspendedLanes&a,e.warmLanes&=~a,je===e&&(ye&a)===a&&(cn===4||cn===3&&(ye&62914560)===ye&&300>ke()-rc?(Oe&2)===0&&Ts(e,0):Zf|=a,Es===ye&&(Es=0)),Gi(e)}function jg(e,n){n===0&&(n=Tt()),e=Er(e,n),e!==null&&(qt(e,n),Gi(e))}function Xy(e){var n=e.memoizedState,a=0;n!==null&&(a=n.retryLane),jg(e,a)}function qy(e,n){var a=0;switch(e.tag){case 31:case 13:var o=e.stateNode,u=e.memoizedState;u!==null&&(a=u.retryLane);break;case 19:o=e.stateNode;break;case 22:o=e.stateNode._retryCache;break;default:throw Error(r(314))}o!==null&&o.delete(n),jg(e,a)}function Wy(e,n){return Ke(e,n)}var dc=null,Rs=null,nd=!1,hc=!1,id=!1,Ja=0;function Gi(e){e!==Rs&&e.next===null&&(Rs===null?dc=Rs=e:Rs=Rs.next=e),hc=!0,nd||(nd=!0,Zy())}function Go(e,n){if(!id&&hc){id=!0;do for(var a=!1,o=dc;o!==null;){if(e!==0){var u=o.pendingLanes;if(u===0)var d=0;else{var y=o.suspendedLanes,w=o.pingedLanes;d=(1<<31-Gt(42|e)+1)-1,d&=u&~(y&~w),d=d&201326741?d&201326741|1:d?d|2:0}d!==0&&(a=!0,$g(o,d))}else d=ye,d=St(o,o===je?d:0,o.cancelPendingCommit!==null||o.timeoutHandle!==-1),(d&3)===0||Nt(o,d)||(a=!0,$g(o,d));o=o.next}while(a);id=!1}}function Yy(){Kg()}function Kg(){hc=nd=!1;var e=0;Ja!==0&&aM()&&(e=Ja);for(var n=ke(),a=null,o=dc;o!==null;){var u=o.next,d=Qg(o,n);d===0?(o.next=null,a===null?dc=u:a.next=u,u===null&&(Rs=a)):(a=o,(e!==0||(d&3)!==0)&&(hc=!0)),o=u}Mn!==0&&Mn!==5||Go(e),Ja!==0&&(Ja=0)}function Qg(e,n){for(var a=e.suspendedLanes,o=e.pingedLanes,u=e.expirationTimes,d=e.pendingLanes&-62914561;0<d;){var y=31-Gt(d),w=1<<y,V=u[y];V===-1?((w&a)===0||(w&o)!==0)&&(u[y]=Ft(w,n)):V<=n&&(e.expiredLanes|=w),d&=~w}if(n=je,a=ye,a=St(e,e===n?a:0,e.cancelPendingCommit!==null||e.timeoutHandle!==-1),o=e.callbackNode,a===0||e===n&&(He===2||He===9)||e.cancelPendingCommit!==null)return o!==null&&o!==null&&Ue(o),e.callbackNode=null,e.callbackPriority=0;if((a&3)===0||Nt(e,a)){if(n=a&-a,n===e.callbackPriority)return n;switch(o!==null&&Ue(o),oo(a)){case 2:case 8:a=T;break;case 32:a=et;break;case 268435456:a=mt;break;default:a=et}return o=Jg.bind(null,e),a=Ke(a,o),e.callbackPriority=n,e.callbackNode=a,n}return o!==null&&o!==null&&Ue(o),e.callbackPriority=2,e.callbackNode=null,2}function Jg(e,n){if(Mn!==0&&Mn!==5)return e.callbackNode=null,e.callbackPriority=0,null;var a=e.callbackNode;if(fc()&&e.callbackNode!==a)return null;var o=ye;return o=St(e,e===je?o:0,e.cancelPendingCommit!==null||e.timeoutHandle!==-1),o===0?null:(Lg(e,o,n),Qg(e,ke()),e.callbackNode!=null&&e.callbackNode===a?Jg.bind(null,e):null)}function $g(e,n){if(fc())return null;Lg(e,n,!0)}function Zy(){sM(function(){(Oe&6)!==0?Ke(B,Yy):Kg()})}function ad(){if(Ja===0){var e=ds;e===0&&(e=$t,$t<<=1,($t&261888)===0&&($t=256)),Ja=e}return Ja}function t_(e){return e==null||typeof e=="symbol"||typeof e=="boolean"?null:typeof e=="function"?e:xr(""+e)}function e_(e,n){var a=n.ownerDocument.createElement("input");return a.name=n.name,a.value=n.value,e.id&&a.setAttribute("form",e.id),n.parentNode.insertBefore(a,n),e=new FormData(e),a.parentNode.removeChild(a),e}function jy(e,n,a,o,u){if(n==="submit"&&a&&a.stateNode===u){var d=t_((u[Dn]||null).action),y=o.submitter;y&&(n=(n=y[Dn]||null)?t_(n.formAction):y.getAttribute("formAction"),n!==null&&(d=n,y=null));var w=new Tl("action","action",null,o,u);e.push({event:w,listeners:[{instance:null,listener:function(){if(o.defaultPrevented){if(Ja!==0){var V=y?e_(u,y):new FormData(u);bf(a,{pending:!0,data:V,method:u.method,action:d},null,V)}}else typeof d=="function"&&(w.preventDefault(),V=y?e_(u,y):new FormData(u),bf(a,{pending:!0,data:V,method:u.method,action:d},d,V))},currentTarget:u}]})}}for(var rd=0;rd<Hu.length;rd++){var sd=Hu[rd],Ky=sd.toLowerCase(),Qy=sd[0].toUpperCase()+sd.slice(1);Ci(Ky,"on"+Qy)}Ci(Um,"onAnimationEnd"),Ci(Lm,"onAnimationIteration"),Ci(Om,"onAnimationStart"),Ci("dblclick","onDoubleClick"),Ci("focusin","onFocus"),Ci("focusout","onBlur"),Ci(hy,"onTransitionRun"),Ci(py,"onTransitionStart"),Ci(my,"onTransitionCancel"),Ci(Pm,"onTransitionEnd"),ft("onMouseEnter",["mouseout","mouseover"]),ft("onMouseLeave",["mouseout","mouseover"]),ft("onPointerEnter",["pointerout","pointerover"]),ft("onPointerLeave",["pointerout","pointerover"]),Z("onChange","change click focusin focusout input keydown keyup selectionchange".split(" ")),Z("onSelect","focusout contextmenu dragend focusin keydown keyup mousedown mouseup selectionchange".split(" ")),Z("onBeforeInput",["compositionend","keypress","textInput","paste"]),Z("onCompositionEnd","compositionend focusout keydown keypress keyup mousedown".split(" ")),Z("onCompositionStart","compositionstart focusout keydown keypress keyup mousedown".split(" ")),Z("onCompositionUpdate","compositionupdate focusout keydown keypress keyup mousedown".split(" "));var Vo="abort canplay canplaythrough durationchange emptied encrypted ended error loadeddata loadedmetadata loadstart pause play playing progress ratechange resize seeked seeking stalled suspend timeupdate volumechange waiting".split(" "),Jy=new Set("beforetoggle cancel close invalid load scroll scrollend toggle".split(" ").concat(Vo));function n_(e,n){n=(n&4)!==0;for(var a=0;a<e.length;a++){var o=e[a],u=o.event;o=o.listeners;t:{var d=void 0;if(n)for(var y=o.length-1;0<=y;y--){var w=o[y],V=w.instance,st=w.currentTarget;if(w=w.listener,V!==d&&u.isPropagationStopped())break t;d=w,u.currentTarget=st;try{d(u)}catch(xt){Cl(xt)}u.currentTarget=null,d=V}else for(y=0;y<o.length;y++){if(w=o[y],V=w.instance,st=w.currentTarget,w=w.listener,V!==d&&u.isPropagationStopped())break t;d=w,u.currentTarget=st;try{d(u)}catch(xt){Cl(xt)}u.currentTarget=null,d=V}}}}function Se(e,n){var a=n[gr];a===void 0&&(a=n[gr]=new Set);var o=e+"__bubble";a.has(o)||(i_(n,e,2,!1),a.add(o))}function od(e,n,a){var o=0;n&&(o|=4),i_(a,e,o,n)}var pc="_reactListening"+Math.random().toString(36).slice(2);function ld(e){if(!e[pc]){e[pc]=!0,yl.forEach(function(a){a!=="selectionchange"&&(Jy.has(a)||od(a,!1,e),od(a,!0,e))});var n=e.nodeType===9?e:e.ownerDocument;n===null||n[pc]||(n[pc]=!0,od("selectionchange",!1,n))}}function i_(e,n,a,o){switch(U_(n)){case 2:var u=AM;break;case 8:u=RM;break;default:u=Ed}a=u.bind(null,n,a,e),u=void 0,!Ru||n!=="touchstart"&&n!=="touchmove"&&n!=="wheel"||(u=!0),o?u!==void 0?e.addEventListener(n,a,{capture:!0,passive:u}):e.addEventListener(n,a,!0):u!==void 0?e.addEventListener(n,a,{passive:u}):e.addEventListener(n,a,!1)}function cd(e,n,a,o,u){var d=o;if((n&1)===0&&(n&2)===0&&o!==null)t:for(;;){if(o===null)return;var y=o.tag;if(y===3||y===4){var w=o.stateNode.containerInfo;if(w===u)break;if(y===4)for(y=o.return;y!==null;){var V=y.tag;if((V===3||V===4)&&y.stateNode.containerInfo===u)return;y=y.return}for(;w!==null;){if(y=ea(w),y===null)return;if(V=y.tag,V===5||V===6||V===26||V===27){o=d=y;continue t}w=w.parentNode}}o=o.return}lm(function(){var st=d,xt=Tu(a),Et=[];t:{var dt=Bm.get(e);if(dt!==void 0){var ht=Tl,Zt=e;switch(e){case"keypress":if(El(a)===0)break t;case"keydown":case"keyup":ht=qS;break;case"focusin":Zt="focus",ht=Nu;break;case"focusout":Zt="blur",ht=Nu;break;case"beforeblur":case"afterblur":ht=Nu;break;case"click":if(a.button===2)break t;case"auxclick":case"dblclick":case"mousedown":case"mousemove":case"mouseup":case"mouseout":case"mouseover":case"contextmenu":ht=fm;break;case"drag":case"dragend":case"dragenter":case"dragexit":case"dragleave":case"dragover":case"dragstart":case"drop":ht=LS;break;case"touchcancel":case"touchend":case"touchmove":case"touchstart":ht=ZS;break;case Um:case Lm:case Om:ht=BS;break;case Pm:ht=KS;break;case"scroll":case"scrollend":ht=NS;break;case"wheel":ht=JS;break;case"copy":case"cut":case"paste":ht=zS;break;case"gotpointercapture":case"lostpointercapture":case"pointercancel":case"pointerdown":case"pointermove":case"pointerout":case"pointerover":case"pointerup":ht=hm;break;case"toggle":case"beforetoggle":ht=ty}var re=(n&4)!==0,We=!re&&(e==="scroll"||e==="scrollend"),J=re?dt!==null?dt+"Capture":null:dt;re=[];for(var q=st,rt;q!==null;){var Mt=q;if(rt=Mt.stateNode,Mt=Mt.tag,Mt!==5&&Mt!==26&&Mt!==27||rt===null||J===null||(Mt=co(q,J),Mt!=null&&re.push(ko(q,Mt,rt))),We)break;q=q.return}0<re.length&&(dt=new ht(dt,Zt,null,a,xt),Et.push({event:dt,listeners:re}))}}if((n&7)===0){t:{if(dt=e==="mouseover"||e==="pointerover",ht=e==="mouseout"||e==="pointerout",dt&&a!==bu&&(Zt=a.relatedTarget||a.fromElement)&&(ea(Zt)||Zt[qn]))break t;if((ht||dt)&&(dt=xt.window===xt?xt:(dt=xt.ownerDocument)?dt.defaultView||dt.parentWindow:window,ht?(Zt=a.relatedTarget||a.toElement,ht=st,Zt=Zt?ea(Zt):null,Zt!==null&&(We=c(Zt),re=Zt.tag,Zt!==We||re!==5&&re!==27&&re!==6)&&(Zt=null)):(ht=null,Zt=st),ht!==Zt)){if(re=fm,Mt="onMouseLeave",J="onMouseEnter",q="mouse",(e==="pointerout"||e==="pointerover")&&(re=hm,Mt="onPointerLeave",J="onPointerEnter",q="pointer"),We=ht==null?dt:vr(ht),rt=Zt==null?dt:vr(Zt),dt=new re(Mt,q+"leave",ht,a,xt),dt.target=We,dt.relatedTarget=rt,Mt=null,ea(xt)===st&&(re=new re(J,q+"enter",Zt,a,xt),re.target=rt,re.relatedTarget=We,Mt=re),We=Mt,ht&&Zt)e:{for(re=$y,J=ht,q=Zt,rt=0,Mt=J;Mt;Mt=re(Mt))rt++;Mt=0;for(var ee=q;ee;ee=re(ee))Mt++;for(;0<rt-Mt;)J=re(J),rt--;for(;0<Mt-rt;)q=re(q),Mt--;for(;rt--;){if(J===q||q!==null&&J===q.alternate){re=J;break e}J=re(J),q=re(q)}re=null}else re=null;ht!==null&&a_(Et,dt,ht,re,!1),Zt!==null&&We!==null&&a_(Et,We,Zt,re,!0)}}t:{if(dt=st?vr(st):window,ht=dt.nodeName&&dt.nodeName.toLowerCase(),ht==="select"||ht==="input"&&dt.type==="file")var De=ym;else if(xm(dt))if(Mm)De=uy;else{De=ly;var Kt=oy}else ht=dt.nodeName,!ht||ht.toLowerCase()!=="input"||dt.type!=="checkbox"&&dt.type!=="radio"?st&&Pe(st.elementType)&&(De=ym):De=cy;if(De&&(De=De(e,st))){Sm(Et,De,a,xt);break t}Kt&&Kt(e,dt,st),e==="focusout"&&st&&dt.type==="number"&&st.memoizedProps.value!=null&&ve(dt,"number",dt.value)}switch(Kt=st?vr(st):window,e){case"focusin":(xm(Kt)||Kt.contentEditable==="true")&&(as=Kt,Iu=st,vo=null);break;case"focusout":vo=Iu=as=null;break;case"mousedown":zu=!0;break;case"contextmenu":case"mouseup":case"dragend":zu=!1,Dm(Et,a,xt);break;case"selectionchange":if(dy)break;case"keydown":case"keyup":Dm(Et,a,xt)}var de;if(Lu)t:{switch(e){case"compositionstart":var Me="onCompositionStart";break t;case"compositionend":Me="onCompositionEnd";break t;case"compositionupdate":Me="onCompositionUpdate";break t}Me=void 0}else is?_m(e,a)&&(Me="onCompositionEnd"):e==="keydown"&&a.keyCode===229&&(Me="onCompositionStart");Me&&(pm&&a.locale!=="ko"&&(is||Me!=="onCompositionStart"?Me==="onCompositionEnd"&&is&&(de=cm()):(Ba=xt,Cu="value"in Ba?Ba.value:Ba.textContent,is=!0)),Kt=mc(st,Me),0<Kt.length&&(Me=new dm(Me,e,null,a,xt),Et.push({event:Me,listeners:Kt}),de?Me.data=de:(de=vm(a),de!==null&&(Me.data=de)))),(de=ny?iy(e,a):ay(e,a))&&(Me=mc(st,"onBeforeInput"),0<Me.length&&(Kt=new dm("onBeforeInput","beforeinput",null,a,xt),Et.push({event:Kt,listeners:Me}),Kt.data=de)),jy(Et,e,st,a,xt)}n_(Et,n)})}function ko(e,n,a){return{instance:e,listener:n,currentTarget:a}}function mc(e,n){for(var a=n+"Capture",o=[];e!==null;){var u=e,d=u.stateNode;if(u=u.tag,u!==5&&u!==26&&u!==27||d===null||(u=co(e,a),u!=null&&o.unshift(ko(e,u,d)),u=co(e,n),u!=null&&o.push(ko(e,u,d))),e.tag===3)return o;e=e.return}return[]}function $y(e){if(e===null)return null;do e=e.return;while(e&&e.tag!==5&&e.tag!==27);return e||null}function a_(e,n,a,o,u){for(var d=n._reactName,y=[];a!==null&&a!==o;){var w=a,V=w.alternate,st=w.stateNode;if(w=w.tag,V!==null&&V===o)break;w!==5&&w!==26&&w!==27||st===null||(V=st,u?(st=co(a,d),st!=null&&y.unshift(ko(a,st,V))):u||(st=co(a,d),st!=null&&y.push(ko(a,st,V)))),a=a.return}y.length!==0&&e.push({event:n,listeners:y})}var tM=/\r\n?/g,eM=/\u0000|\uFFFD/g;function r_(e){return(typeof e=="string"?e:""+e).replace(tM,`
`).replace(eM,"")}function s_(e,n){return n=r_(n),r_(e)===n}function qe(e,n,a,o,u,d){switch(a){case"children":typeof o=="string"?n==="body"||n==="textarea"&&o===""||ii(e,o):(typeof o=="number"||typeof o=="bigint")&&n!=="body"&&ii(e,""+o);break;case"className":Yt(e,"class",o);break;case"tabIndex":Yt(e,"tabindex",o);break;case"dir":case"role":case"viewBox":case"width":case"height":Yt(e,a,o);break;case"style":Ri(e,o,d);break;case"data":if(n!=="object"){Yt(e,"data",o);break}case"src":case"href":if(o===""&&(n!=="a"||a!=="href")){e.removeAttribute(a);break}if(o==null||typeof o=="function"||typeof o=="symbol"||typeof o=="boolean"){e.removeAttribute(a);break}o=xr(""+o),e.setAttribute(a,o);break;case"action":case"formAction":if(typeof o=="function"){e.setAttribute(a,"javascript:throw new Error('A React form was unexpectedly submitted. If you called form.submit() manually, consider using form.requestSubmit() instead. If you\\'re trying to use event.stopPropagation() in a submit event handler, consider also calling event.preventDefault().')");break}else typeof d=="function"&&(a==="formAction"?(n!=="input"&&qe(e,n,"name",u.name,u,null),qe(e,n,"formEncType",u.formEncType,u,null),qe(e,n,"formMethod",u.formMethod,u,null),qe(e,n,"formTarget",u.formTarget,u,null)):(qe(e,n,"encType",u.encType,u,null),qe(e,n,"method",u.method,u,null),qe(e,n,"target",u.target,u,null)));if(o==null||typeof o=="symbol"||typeof o=="boolean"){e.removeAttribute(a);break}o=xr(""+o),e.setAttribute(a,o);break;case"onClick":o!=null&&(e.onclick=ia);break;case"onScroll":o!=null&&Se("scroll",e);break;case"onScrollEnd":o!=null&&Se("scrollend",e);break;case"dangerouslySetInnerHTML":if(o!=null){if(typeof o!="object"||!("__html"in o))throw Error(r(61));if(a=o.__html,a!=null){if(u.children!=null)throw Error(r(60));e.innerHTML=a}}break;case"multiple":e.multiple=o&&typeof o!="function"&&typeof o!="symbol";break;case"muted":e.muted=o&&typeof o!="function"&&typeof o!="symbol";break;case"suppressContentEditableWarning":case"suppressHydrationWarning":case"defaultValue":case"defaultChecked":case"innerHTML":case"ref":break;case"autoFocus":break;case"xlinkHref":if(o==null||typeof o=="function"||typeof o=="boolean"||typeof o=="symbol"){e.removeAttribute("xlink:href");break}a=xr(""+o),e.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href",a);break;case"contentEditable":case"spellCheck":case"draggable":case"value":case"autoReverse":case"externalResourcesRequired":case"focusable":case"preserveAlpha":o!=null&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,""+o):e.removeAttribute(a);break;case"inert":case"allowFullScreen":case"async":case"autoPlay":case"controls":case"default":case"defer":case"disabled":case"disablePictureInPicture":case"disableRemotePlayback":case"formNoValidate":case"hidden":case"loop":case"noModule":case"noValidate":case"open":case"playsInline":case"readOnly":case"required":case"reversed":case"scoped":case"seamless":case"itemScope":o&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,""):e.removeAttribute(a);break;case"capture":case"download":o===!0?e.setAttribute(a,""):o!==!1&&o!=null&&typeof o!="function"&&typeof o!="symbol"?e.setAttribute(a,o):e.removeAttribute(a);break;case"cols":case"rows":case"size":case"span":o!=null&&typeof o!="function"&&typeof o!="symbol"&&!isNaN(o)&&1<=o?e.setAttribute(a,o):e.removeAttribute(a);break;case"rowSpan":case"start":o==null||typeof o=="function"||typeof o=="symbol"||isNaN(o)?e.removeAttribute(a):e.setAttribute(a,o);break;case"popover":Se("beforetoggle",e),Se("toggle",e),Ot(e,"popover",o);break;case"xlinkActuate":Wt(e,"http://www.w3.org/1999/xlink","xlink:actuate",o);break;case"xlinkArcrole":Wt(e,"http://www.w3.org/1999/xlink","xlink:arcrole",o);break;case"xlinkRole":Wt(e,"http://www.w3.org/1999/xlink","xlink:role",o);break;case"xlinkShow":Wt(e,"http://www.w3.org/1999/xlink","xlink:show",o);break;case"xlinkTitle":Wt(e,"http://www.w3.org/1999/xlink","xlink:title",o);break;case"xlinkType":Wt(e,"http://www.w3.org/1999/xlink","xlink:type",o);break;case"xmlBase":Wt(e,"http://www.w3.org/XML/1998/namespace","xml:base",o);break;case"xmlLang":Wt(e,"http://www.w3.org/XML/1998/namespace","xml:lang",o);break;case"xmlSpace":Wt(e,"http://www.w3.org/XML/1998/namespace","xml:space",o);break;case"is":Ot(e,"is",o);break;case"innerText":case"textContent":break;default:(!(2<a.length)||a[0]!=="o"&&a[0]!=="O"||a[1]!=="n"&&a[1]!=="N")&&(a=Ii.get(a)||a,Ot(e,a,o))}}function ud(e,n,a,o,u,d){switch(a){case"style":Ri(e,o,d);break;case"dangerouslySetInnerHTML":if(o!=null){if(typeof o!="object"||!("__html"in o))throw Error(r(61));if(a=o.__html,a!=null){if(u.children!=null)throw Error(r(60));e.innerHTML=a}}break;case"children":typeof o=="string"?ii(e,o):(typeof o=="number"||typeof o=="bigint")&&ii(e,""+o);break;case"onScroll":o!=null&&Se("scroll",e);break;case"onScrollEnd":o!=null&&Se("scrollend",e);break;case"onClick":o!=null&&(e.onclick=ia);break;case"suppressContentEditableWarning":case"suppressHydrationWarning":case"innerHTML":case"ref":break;case"innerText":case"textContent":break;default:if(!R.hasOwnProperty(a))t:{if(a[0]==="o"&&a[1]==="n"&&(u=a.endsWith("Capture"),n=a.slice(2,u?a.length-7:void 0),d=e[Dn]||null,d=d!=null?d[a]:null,typeof d=="function"&&e.removeEventListener(n,d,u),typeof o=="function")){typeof d!="function"&&d!==null&&(a in e?e[a]=null:e.hasAttribute(a)&&e.removeAttribute(a)),e.addEventListener(n,o,u);break t}a in e?e[a]=o:o===!0?e.setAttribute(a,""):Ot(e,a,o)}}}function On(e,n,a){switch(n){case"div":case"span":case"svg":case"path":case"a":case"g":case"p":case"li":break;case"img":Se("error",e),Se("load",e);var o=!1,u=!1,d;for(d in a)if(a.hasOwnProperty(d)){var y=a[d];if(y!=null)switch(d){case"src":o=!0;break;case"srcSet":u=!0;break;case"children":case"dangerouslySetInnerHTML":throw Error(r(137,n));default:qe(e,n,d,y,a,null)}}u&&qe(e,n,"srcSet",a.srcSet,a,null),o&&qe(e,n,"src",a.src,a,null);return;case"input":Se("invalid",e);var w=d=y=u=null,V=null,st=null;for(o in a)if(a.hasOwnProperty(o)){var xt=a[o];if(xt!=null)switch(o){case"name":u=xt;break;case"type":y=xt;break;case"checked":V=xt;break;case"defaultChecked":st=xt;break;case"value":d=xt;break;case"defaultValue":w=xt;break;case"children":case"dangerouslySetInnerHTML":if(xt!=null)throw Error(r(137,n));break;default:qe(e,n,o,xt,a,null)}}In(e,d,w,V,st,y,u,!1);return;case"select":Se("invalid",e),o=y=d=null;for(u in a)if(a.hasOwnProperty(u)&&(w=a[u],w!=null))switch(u){case"value":d=w;break;case"defaultValue":y=w;break;case"multiple":o=w;default:qe(e,n,u,w,a,null)}n=d,a=y,e.multiple=!!o,n!=null?yn(e,!!o,n,!1):a!=null&&yn(e,!!o,a,!0);return;case"textarea":Se("invalid",e),d=u=o=null;for(y in a)if(a.hasOwnProperty(y)&&(w=a[y],w!=null))switch(y){case"value":o=w;break;case"defaultValue":u=w;break;case"children":d=w;break;case"dangerouslySetInnerHTML":if(w!=null)throw Error(r(91));break;default:qe(e,n,y,w,a,null)}Ai(e,o,u,d);return;case"option":for(V in a)if(a.hasOwnProperty(V)&&(o=a[V],o!=null))switch(V){case"selected":e.selected=o&&typeof o!="function"&&typeof o!="symbol";break;default:qe(e,n,V,o,a,null)}return;case"dialog":Se("beforetoggle",e),Se("toggle",e),Se("cancel",e),Se("close",e);break;case"iframe":case"object":Se("load",e);break;case"video":case"audio":for(o=0;o<Vo.length;o++)Se(Vo[o],e);break;case"image":Se("error",e),Se("load",e);break;case"details":Se("toggle",e);break;case"embed":case"source":case"link":Se("error",e),Se("load",e);case"area":case"base":case"br":case"col":case"hr":case"keygen":case"meta":case"param":case"track":case"wbr":case"menuitem":for(st in a)if(a.hasOwnProperty(st)&&(o=a[st],o!=null))switch(st){case"children":case"dangerouslySetInnerHTML":throw Error(r(137,n));default:qe(e,n,st,o,a,null)}return;default:if(Pe(n)){for(xt in a)a.hasOwnProperty(xt)&&(o=a[xt],o!==void 0&&ud(e,n,xt,o,a,void 0));return}}for(w in a)a.hasOwnProperty(w)&&(o=a[w],o!=null&&qe(e,n,w,o,a,null))}function nM(e,n,a,o){switch(n){case"div":case"span":case"svg":case"path":case"a":case"g":case"p":case"li":break;case"input":var u=null,d=null,y=null,w=null,V=null,st=null,xt=null;for(ht in a){var Et=a[ht];if(a.hasOwnProperty(ht)&&Et!=null)switch(ht){case"checked":break;case"value":break;case"defaultValue":V=Et;default:o.hasOwnProperty(ht)||qe(e,n,ht,null,o,Et)}}for(var dt in o){var ht=o[dt];if(Et=a[dt],o.hasOwnProperty(dt)&&(ht!=null||Et!=null))switch(dt){case"type":d=ht;break;case"name":u=ht;break;case"checked":st=ht;break;case"defaultChecked":xt=ht;break;case"value":y=ht;break;case"defaultValue":w=ht;break;case"children":case"dangerouslySetInnerHTML":if(ht!=null)throw Error(r(137,n));break;default:ht!==Et&&qe(e,n,dt,ht,o,Et)}}Vt(e,y,w,V,st,xt,d,u);return;case"select":ht=y=w=dt=null;for(d in a)if(V=a[d],a.hasOwnProperty(d)&&V!=null)switch(d){case"value":break;case"multiple":ht=V;default:o.hasOwnProperty(d)||qe(e,n,d,null,o,V)}for(u in o)if(d=o[u],V=a[u],o.hasOwnProperty(u)&&(d!=null||V!=null))switch(u){case"value":dt=d;break;case"defaultValue":w=d;break;case"multiple":y=d;default:d!==V&&qe(e,n,u,d,o,V)}n=w,a=y,o=ht,dt!=null?yn(e,!!a,dt,!1):!!o!=!!a&&(n!=null?yn(e,!!a,n,!0):yn(e,!!a,a?[]:"",!1));return;case"textarea":ht=dt=null;for(w in a)if(u=a[w],a.hasOwnProperty(w)&&u!=null&&!o.hasOwnProperty(w))switch(w){case"value":break;case"children":break;default:qe(e,n,w,null,o,u)}for(y in o)if(u=o[y],d=a[y],o.hasOwnProperty(y)&&(u!=null||d!=null))switch(y){case"value":dt=u;break;case"defaultValue":ht=u;break;case"children":break;case"dangerouslySetInnerHTML":if(u!=null)throw Error(r(91));break;default:u!==d&&qe(e,n,y,u,o,d)}ni(e,dt,ht);return;case"option":for(var Zt in a)if(dt=a[Zt],a.hasOwnProperty(Zt)&&dt!=null&&!o.hasOwnProperty(Zt))switch(Zt){case"selected":e.selected=!1;break;default:qe(e,n,Zt,null,o,dt)}for(V in o)if(dt=o[V],ht=a[V],o.hasOwnProperty(V)&&dt!==ht&&(dt!=null||ht!=null))switch(V){case"selected":e.selected=dt&&typeof dt!="function"&&typeof dt!="symbol";break;default:qe(e,n,V,dt,o,ht)}return;case"img":case"link":case"area":case"base":case"br":case"col":case"embed":case"hr":case"keygen":case"meta":case"param":case"source":case"track":case"wbr":case"menuitem":for(var re in a)dt=a[re],a.hasOwnProperty(re)&&dt!=null&&!o.hasOwnProperty(re)&&qe(e,n,re,null,o,dt);for(st in o)if(dt=o[st],ht=a[st],o.hasOwnProperty(st)&&dt!==ht&&(dt!=null||ht!=null))switch(st){case"children":case"dangerouslySetInnerHTML":if(dt!=null)throw Error(r(137,n));break;default:qe(e,n,st,dt,o,ht)}return;default:if(Pe(n)){for(var We in a)dt=a[We],a.hasOwnProperty(We)&&dt!==void 0&&!o.hasOwnProperty(We)&&ud(e,n,We,void 0,o,dt);for(xt in o)dt=o[xt],ht=a[xt],!o.hasOwnProperty(xt)||dt===ht||dt===void 0&&ht===void 0||ud(e,n,xt,dt,o,ht);return}}for(var J in a)dt=a[J],a.hasOwnProperty(J)&&dt!=null&&!o.hasOwnProperty(J)&&qe(e,n,J,null,o,dt);for(Et in o)dt=o[Et],ht=a[Et],!o.hasOwnProperty(Et)||dt===ht||dt==null&&ht==null||qe(e,n,Et,dt,o,ht)}function o_(e){switch(e){case"css":case"script":case"font":case"img":case"image":case"input":case"link":return!0;default:return!1}}function iM(){if(typeof performance.getEntriesByType=="function"){for(var e=0,n=0,a=performance.getEntriesByType("resource"),o=0;o<a.length;o++){var u=a[o],d=u.transferSize,y=u.initiatorType,w=u.duration;if(d&&w&&o_(y)){for(y=0,w=u.responseEnd,o+=1;o<a.length;o++){var V=a[o],st=V.startTime;if(st>w)break;var xt=V.transferSize,Et=V.initiatorType;xt&&o_(Et)&&(V=V.responseEnd,y+=xt*(V<w?1:(w-st)/(V-st)))}if(--o,n+=8*(d+y)/(u.duration/1e3),e++,10<e)break}}if(0<e)return n/e/1e6}return navigator.connection&&(e=navigator.connection.downlink,typeof e=="number")?e:5}var fd=null,dd=null;function gc(e){return e.nodeType===9?e:e.ownerDocument}function l_(e){switch(e){case"http://www.w3.org/2000/svg":return 1;case"http://www.w3.org/1998/Math/MathML":return 2;default:return 0}}function c_(e,n){if(e===0)switch(n){case"svg":return 1;case"math":return 2;default:return 0}return e===1&&n==="foreignObject"?0:e}function hd(e,n){return e==="textarea"||e==="noscript"||typeof n.children=="string"||typeof n.children=="number"||typeof n.children=="bigint"||typeof n.dangerouslySetInnerHTML=="object"&&n.dangerouslySetInnerHTML!==null&&n.dangerouslySetInnerHTML.__html!=null}var pd=null;function aM(){var e=window.event;return e&&e.type==="popstate"?e===pd?!1:(pd=e,!0):(pd=null,!1)}var u_=typeof setTimeout=="function"?setTimeout:void 0,rM=typeof clearTimeout=="function"?clearTimeout:void 0,f_=typeof Promise=="function"?Promise:void 0,sM=typeof queueMicrotask=="function"?queueMicrotask:typeof f_<"u"?function(e){return f_.resolve(null).then(e).catch(oM)}:u_;function oM(e){setTimeout(function(){throw e})}function $a(e){return e==="head"}function d_(e,n){var a=n,o=0;do{var u=a.nextSibling;if(e.removeChild(a),u&&u.nodeType===8)if(a=u.data,a==="/$"||a==="/&"){if(o===0){e.removeChild(u),Ns(n);return}o--}else if(a==="$"||a==="$?"||a==="$~"||a==="$!"||a==="&")o++;else if(a==="html")Xo(e.ownerDocument.documentElement);else if(a==="head"){a=e.ownerDocument.head,Xo(a);for(var d=a.firstChild;d;){var y=d.nextSibling,w=d.nodeName;d[Ua]||w==="SCRIPT"||w==="STYLE"||w==="LINK"&&d.rel.toLowerCase()==="stylesheet"||a.removeChild(d),d=y}}else a==="body"&&Xo(e.ownerDocument.body);a=u}while(a);Ns(n)}function h_(e,n){var a=e;e=0;do{var o=a.nextSibling;if(a.nodeType===1?n?(a._stashedDisplay=a.style.display,a.style.display="none"):(a.style.display=a._stashedDisplay||"",a.getAttribute("style")===""&&a.removeAttribute("style")):a.nodeType===3&&(n?(a._stashedText=a.nodeValue,a.nodeValue=""):a.nodeValue=a._stashedText||""),o&&o.nodeType===8)if(a=o.data,a==="/$"){if(e===0)break;e--}else a!=="$"&&a!=="$?"&&a!=="$~"&&a!=="$!"||e++;a=o}while(a)}function md(e){var n=e.firstChild;for(n&&n.nodeType===10&&(n=n.nextSibling);n;){var a=n;switch(n=n.nextSibling,a.nodeName){case"HTML":case"HEAD":case"BODY":md(a),La(a);continue;case"SCRIPT":case"STYLE":continue;case"LINK":if(a.rel.toLowerCase()==="stylesheet")continue}e.removeChild(a)}}function lM(e,n,a,o){for(;e.nodeType===1;){var u=a;if(e.nodeName.toLowerCase()!==n.toLowerCase()){if(!o&&(e.nodeName!=="INPUT"||e.type!=="hidden"))break}else if(o){if(!e[Ua])switch(n){case"meta":if(!e.hasAttribute("itemprop"))break;return e;case"link":if(d=e.getAttribute("rel"),d==="stylesheet"&&e.hasAttribute("data-precedence"))break;if(d!==u.rel||e.getAttribute("href")!==(u.href==null||u.href===""?null:u.href)||e.getAttribute("crossorigin")!==(u.crossOrigin==null?null:u.crossOrigin)||e.getAttribute("title")!==(u.title==null?null:u.title))break;return e;case"style":if(e.hasAttribute("data-precedence"))break;return e;case"script":if(d=e.getAttribute("src"),(d!==(u.src==null?null:u.src)||e.getAttribute("type")!==(u.type==null?null:u.type)||e.getAttribute("crossorigin")!==(u.crossOrigin==null?null:u.crossOrigin))&&d&&e.hasAttribute("async")&&!e.hasAttribute("itemprop"))break;return e;default:return e}}else if(n==="input"&&e.type==="hidden"){var d=u.name==null?null:""+u.name;if(u.type==="hidden"&&e.getAttribute("name")===d)return e}else return e;if(e=yi(e.nextSibling),e===null)break}return null}function cM(e,n,a){if(n==="")return null;for(;e.nodeType!==3;)if((e.nodeType!==1||e.nodeName!=="INPUT"||e.type!=="hidden")&&!a||(e=yi(e.nextSibling),e===null))return null;return e}function p_(e,n){for(;e.nodeType!==8;)if((e.nodeType!==1||e.nodeName!=="INPUT"||e.type!=="hidden")&&!n||(e=yi(e.nextSibling),e===null))return null;return e}function gd(e){return e.data==="$?"||e.data==="$~"}function _d(e){return e.data==="$!"||e.data==="$?"&&e.ownerDocument.readyState!=="loading"}function uM(e,n){var a=e.ownerDocument;if(e.data==="$~")e._reactRetry=n;else if(e.data!=="$?"||a.readyState!=="loading")n();else{var o=function(){n(),a.removeEventListener("DOMContentLoaded",o)};a.addEventListener("DOMContentLoaded",o),e._reactRetry=o}}function yi(e){for(;e!=null;e=e.nextSibling){var n=e.nodeType;if(n===1||n===3)break;if(n===8){if(n=e.data,n==="$"||n==="$!"||n==="$?"||n==="$~"||n==="&"||n==="F!"||n==="F")break;if(n==="/$"||n==="/&")return null}}return e}var vd=null;function m_(e){e=e.nextSibling;for(var n=0;e;){if(e.nodeType===8){var a=e.data;if(a==="/$"||a==="/&"){if(n===0)return yi(e.nextSibling);n--}else a!=="$"&&a!=="$!"&&a!=="$?"&&a!=="$~"&&a!=="&"||n++}e=e.nextSibling}return null}function g_(e){e=e.previousSibling;for(var n=0;e;){if(e.nodeType===8){var a=e.data;if(a==="$"||a==="$!"||a==="$?"||a==="$~"||a==="&"){if(n===0)return e;n--}else a!=="/$"&&a!=="/&"||n++}e=e.previousSibling}return null}function __(e,n,a){switch(n=gc(a),e){case"html":if(e=n.documentElement,!e)throw Error(r(452));return e;case"head":if(e=n.head,!e)throw Error(r(453));return e;case"body":if(e=n.body,!e)throw Error(r(454));return e;default:throw Error(r(451))}}function Xo(e){for(var n=e.attributes;n.length;)e.removeAttributeNode(n[0]);La(e)}var Mi=new Map,v_=new Set;function _c(e){return typeof e.getRootNode=="function"?e.getRootNode():e.nodeType===9?e:e.ownerDocument}var xa=X.d;X.d={f:fM,r:dM,D:hM,C:pM,L:mM,m:gM,X:vM,S:_M,M:xM};function fM(){var e=xa.f(),n=lc();return e||n}function dM(e){var n=na(e);n!==null&&n.tag===5&&n.type==="form"?P0(n):xa.r(e)}var Cs=typeof document>"u"?null:document;function x_(e,n,a){var o=Cs;if(o&&typeof n=="string"&&n){var u=ze(n);u='link[rel="'+e+'"][href="'+u+'"]',typeof a=="string"&&(u+='[crossorigin="'+a+'"]'),v_.has(u)||(v_.add(u),e={rel:e,crossOrigin:a,href:n},o.querySelector(u)===null&&(n=o.createElement("link"),On(n,"link",e),mn(n),o.head.appendChild(n)))}}function hM(e){xa.D(e),x_("dns-prefetch",e,null)}function pM(e,n){xa.C(e,n),x_("preconnect",e,n)}function mM(e,n,a){xa.L(e,n,a);var o=Cs;if(o&&e&&n){var u='link[rel="preload"][as="'+ze(n)+'"]';n==="image"&&a&&a.imageSrcSet?(u+='[imagesrcset="'+ze(a.imageSrcSet)+'"]',typeof a.imageSizes=="string"&&(u+='[imagesizes="'+ze(a.imageSizes)+'"]')):u+='[href="'+ze(e)+'"]';var d=u;switch(n){case"style":d=ws(e);break;case"script":d=Ds(e)}Mi.has(d)||(e=v({rel:"preload",href:n==="image"&&a&&a.imageSrcSet?void 0:e,as:n},a),Mi.set(d,e),o.querySelector(u)!==null||n==="style"&&o.querySelector(qo(d))||n==="script"&&o.querySelector(Wo(d))||(n=o.createElement("link"),On(n,"link",e),mn(n),o.head.appendChild(n)))}}function gM(e,n){xa.m(e,n);var a=Cs;if(a&&e){var o=n&&typeof n.as=="string"?n.as:"script",u='link[rel="modulepreload"][as="'+ze(o)+'"][href="'+ze(e)+'"]',d=u;switch(o){case"audioworklet":case"paintworklet":case"serviceworker":case"sharedworker":case"worker":case"script":d=Ds(e)}if(!Mi.has(d)&&(e=v({rel:"modulepreload",href:e},n),Mi.set(d,e),a.querySelector(u)===null)){switch(o){case"audioworklet":case"paintworklet":case"serviceworker":case"sharedworker":case"worker":case"script":if(a.querySelector(Wo(d)))return}o=a.createElement("link"),On(o,"link",e),mn(o),a.head.appendChild(o)}}}function _M(e,n,a){xa.S(e,n,a);var o=Cs;if(o&&e){var u=Oa(o).hoistableStyles,d=ws(e);n=n||"default";var y=u.get(d);if(!y){var w={loading:0,preload:null};if(y=o.querySelector(qo(d)))w.loading=5;else{e=v({rel:"stylesheet",href:e,"data-precedence":n},a),(a=Mi.get(d))&&xd(e,a);var V=y=o.createElement("link");mn(V),On(V,"link",e),V._p=new Promise(function(st,xt){V.onload=st,V.onerror=xt}),V.addEventListener("load",function(){w.loading|=1}),V.addEventListener("error",function(){w.loading|=2}),w.loading|=4,vc(y,n,o)}y={type:"stylesheet",instance:y,count:1,state:w},u.set(d,y)}}}function vM(e,n){xa.X(e,n);var a=Cs;if(a&&e){var o=Oa(a).hoistableScripts,u=Ds(e),d=o.get(u);d||(d=a.querySelector(Wo(u)),d||(e=v({src:e,async:!0},n),(n=Mi.get(u))&&Sd(e,n),d=a.createElement("script"),mn(d),On(d,"link",e),a.head.appendChild(d)),d={type:"script",instance:d,count:1,state:null},o.set(u,d))}}function xM(e,n){xa.M(e,n);var a=Cs;if(a&&e){var o=Oa(a).hoistableScripts,u=Ds(e),d=o.get(u);d||(d=a.querySelector(Wo(u)),d||(e=v({src:e,async:!0,type:"module"},n),(n=Mi.get(u))&&Sd(e,n),d=a.createElement("script"),mn(d),On(d,"link",e),a.head.appendChild(d)),d={type:"script",instance:d,count:1,state:null},o.set(u,d))}}function S_(e,n,a,o){var u=(u=it.current)?_c(u):null;if(!u)throw Error(r(446));switch(e){case"meta":case"title":return null;case"style":return typeof a.precedence=="string"&&typeof a.href=="string"?(n=ws(a.href),a=Oa(u).hoistableStyles,o=a.get(n),o||(o={type:"style",instance:null,count:0,state:null},a.set(n,o)),o):{type:"void",instance:null,count:0,state:null};case"link":if(a.rel==="stylesheet"&&typeof a.href=="string"&&typeof a.precedence=="string"){e=ws(a.href);var d=Oa(u).hoistableStyles,y=d.get(e);if(y||(u=u.ownerDocument||u,y={type:"stylesheet",instance:null,count:0,state:{loading:0,preload:null}},d.set(e,y),(d=u.querySelector(qo(e)))&&!d._p&&(y.instance=d,y.state.loading=5),Mi.has(e)||(a={rel:"preload",as:"style",href:a.href,crossOrigin:a.crossOrigin,integrity:a.integrity,media:a.media,hrefLang:a.hrefLang,referrerPolicy:a.referrerPolicy},Mi.set(e,a),d||SM(u,e,a,y.state))),n&&o===null)throw Error(r(528,""));return y}if(n&&o!==null)throw Error(r(529,""));return null;case"script":return n=a.async,a=a.src,typeof a=="string"&&n&&typeof n!="function"&&typeof n!="symbol"?(n=Ds(a),a=Oa(u).hoistableScripts,o=a.get(n),o||(o={type:"script",instance:null,count:0,state:null},a.set(n,o)),o):{type:"void",instance:null,count:0,state:null};default:throw Error(r(444,e))}}function ws(e){return'href="'+ze(e)+'"'}function qo(e){return'link[rel="stylesheet"]['+e+"]"}function y_(e){return v({},e,{"data-precedence":e.precedence,precedence:null})}function SM(e,n,a,o){e.querySelector('link[rel="preload"][as="style"]['+n+"]")?o.loading=1:(n=e.createElement("link"),o.preload=n,n.addEventListener("load",function(){return o.loading|=1}),n.addEventListener("error",function(){return o.loading|=2}),On(n,"link",a),mn(n),e.head.appendChild(n))}function Ds(e){return'[src="'+ze(e)+'"]'}function Wo(e){return"script[async]"+e}function M_(e,n,a){if(n.count++,n.instance===null)switch(n.type){case"style":var o=e.querySelector('style[data-href~="'+ze(a.href)+'"]');if(o)return n.instance=o,mn(o),o;var u=v({},a,{"data-href":a.href,"data-precedence":a.precedence,href:null,precedence:null});return o=(e.ownerDocument||e).createElement("style"),mn(o),On(o,"style",u),vc(o,a.precedence,e),n.instance=o;case"stylesheet":u=ws(a.href);var d=e.querySelector(qo(u));if(d)return n.state.loading|=4,n.instance=d,mn(d),d;o=y_(a),(u=Mi.get(u))&&xd(o,u),d=(e.ownerDocument||e).createElement("link"),mn(d);var y=d;return y._p=new Promise(function(w,V){y.onload=w,y.onerror=V}),On(d,"link",o),n.state.loading|=4,vc(d,a.precedence,e),n.instance=d;case"script":return d=Ds(a.src),(u=e.querySelector(Wo(d)))?(n.instance=u,mn(u),u):(o=a,(u=Mi.get(d))&&(o=v({},a),Sd(o,u)),e=e.ownerDocument||e,u=e.createElement("script"),mn(u),On(u,"link",o),e.head.appendChild(u),n.instance=u);case"void":return null;default:throw Error(r(443,n.type))}else n.type==="stylesheet"&&(n.state.loading&4)===0&&(o=n.instance,n.state.loading|=4,vc(o,a.precedence,e));return n.instance}function vc(e,n,a){for(var o=a.querySelectorAll('link[rel="stylesheet"][data-precedence],style[data-precedence]'),u=o.length?o[o.length-1]:null,d=u,y=0;y<o.length;y++){var w=o[y];if(w.dataset.precedence===n)d=w;else if(d!==u)break}d?d.parentNode.insertBefore(e,d.nextSibling):(n=a.nodeType===9?a.head:a,n.insertBefore(e,n.firstChild))}function xd(e,n){e.crossOrigin==null&&(e.crossOrigin=n.crossOrigin),e.referrerPolicy==null&&(e.referrerPolicy=n.referrerPolicy),e.title==null&&(e.title=n.title)}function Sd(e,n){e.crossOrigin==null&&(e.crossOrigin=n.crossOrigin),e.referrerPolicy==null&&(e.referrerPolicy=n.referrerPolicy),e.integrity==null&&(e.integrity=n.integrity)}var xc=null;function E_(e,n,a){if(xc===null){var o=new Map,u=xc=new Map;u.set(a,o)}else u=xc,o=u.get(a),o||(o=new Map,u.set(a,o));if(o.has(e))return o;for(o.set(e,null),a=a.getElementsByTagName(e),u=0;u<a.length;u++){var d=a[u];if(!(d[Ua]||d[pn]||e==="link"&&d.getAttribute("rel")==="stylesheet")&&d.namespaceURI!=="http://www.w3.org/2000/svg"){var y=d.getAttribute(n)||"";y=e+y;var w=o.get(y);w?w.push(d):o.set(y,[d])}}return o}function b_(e,n,a){e=e.ownerDocument||e,e.head.insertBefore(a,n==="title"?e.querySelector("head > title"):null)}function yM(e,n,a){if(a===1||n.itemProp!=null)return!1;switch(e){case"meta":case"title":return!0;case"style":if(typeof n.precedence!="string"||typeof n.href!="string"||n.href==="")break;return!0;case"link":if(typeof n.rel!="string"||typeof n.href!="string"||n.href===""||n.onLoad||n.onError)break;switch(n.rel){case"stylesheet":return e=n.disabled,typeof n.precedence=="string"&&e==null;default:return!0}case"script":if(n.async&&typeof n.async!="function"&&typeof n.async!="symbol"&&!n.onLoad&&!n.onError&&n.src&&typeof n.src=="string")return!0}return!1}function T_(e){return!(e.type==="stylesheet"&&(e.state.loading&3)===0)}function MM(e,n,a,o){if(a.type==="stylesheet"&&(typeof o.media!="string"||matchMedia(o.media).matches!==!1)&&(a.state.loading&4)===0){if(a.instance===null){var u=ws(o.href),d=n.querySelector(qo(u));if(d){n=d._p,n!==null&&typeof n=="object"&&typeof n.then=="function"&&(e.count++,e=Sc.bind(e),n.then(e,e)),a.state.loading|=4,a.instance=d,mn(d);return}d=n.ownerDocument||n,o=y_(o),(u=Mi.get(u))&&xd(o,u),d=d.createElement("link"),mn(d);var y=d;y._p=new Promise(function(w,V){y.onload=w,y.onerror=V}),On(d,"link",o),a.instance=d}e.stylesheets===null&&(e.stylesheets=new Map),e.stylesheets.set(a,n),(n=a.state.preload)&&(a.state.loading&3)===0&&(e.count++,a=Sc.bind(e),n.addEventListener("load",a),n.addEventListener("error",a))}}var yd=0;function EM(e,n){return e.stylesheets&&e.count===0&&Mc(e,e.stylesheets),0<e.count||0<e.imgCount?function(a){var o=setTimeout(function(){if(e.stylesheets&&Mc(e,e.stylesheets),e.unsuspend){var d=e.unsuspend;e.unsuspend=null,d()}},6e4+n);0<e.imgBytes&&yd===0&&(yd=62500*iM());var u=setTimeout(function(){if(e.waitingForImages=!1,e.count===0&&(e.stylesheets&&Mc(e,e.stylesheets),e.unsuspend)){var d=e.unsuspend;e.unsuspend=null,d()}},(e.imgBytes>yd?50:800)+n);return e.unsuspend=a,function(){e.unsuspend=null,clearTimeout(o),clearTimeout(u)}}:null}function Sc(){if(this.count--,this.count===0&&(this.imgCount===0||!this.waitingForImages)){if(this.stylesheets)Mc(this,this.stylesheets);else if(this.unsuspend){var e=this.unsuspend;this.unsuspend=null,e()}}}var yc=null;function Mc(e,n){e.stylesheets=null,e.unsuspend!==null&&(e.count++,yc=new Map,n.forEach(bM,e),yc=null,Sc.call(e))}function bM(e,n){if(!(n.state.loading&4)){var a=yc.get(e);if(a)var o=a.get(null);else{a=new Map,yc.set(e,a);for(var u=e.querySelectorAll("link[data-precedence],style[data-precedence]"),d=0;d<u.length;d++){var y=u[d];(y.nodeName==="LINK"||y.getAttribute("media")!=="not all")&&(a.set(y.dataset.precedence,y),o=y)}o&&a.set(null,o)}u=n.instance,y=u.getAttribute("data-precedence"),d=a.get(y)||o,d===o&&a.set(null,u),a.set(y,u),this.count++,o=Sc.bind(this),u.addEventListener("load",o),u.addEventListener("error",o),d?d.parentNode.insertBefore(u,d.nextSibling):(e=e.nodeType===9?e.head:e,e.insertBefore(u,e.firstChild)),n.state.loading|=4}}var Yo={$$typeof:O,Provider:null,Consumer:null,_currentValue:at,_currentValue2:at,_threadCount:0};function TM(e,n,a,o,u,d,y,w,V){this.tag=1,this.containerInfo=e,this.pingCache=this.current=this.pendingChildren=null,this.timeoutHandle=-1,this.callbackNode=this.next=this.pendingContext=this.context=this.cancelPendingCommit=null,this.callbackPriority=0,this.expirationTimes=jt(-1),this.entangledLanes=this.shellSuspendCounter=this.errorRecoveryDisabledLanes=this.expiredLanes=this.warmLanes=this.pingedLanes=this.suspendedLanes=this.pendingLanes=0,this.entanglements=jt(0),this.hiddenUpdates=jt(null),this.identifierPrefix=o,this.onUncaughtError=u,this.onCaughtError=d,this.onRecoverableError=y,this.pooledCache=null,this.pooledCacheLanes=0,this.formState=V,this.incompleteTransitions=new Map}function A_(e,n,a,o,u,d,y,w,V,st,xt,Et){return e=new TM(e,n,a,y,V,st,xt,Et,w),n=1,d===!0&&(n|=24),d=ri(3,null,null,n),e.current=d,d.stateNode=e,n=$u(),n.refCount++,e.pooledCache=n,n.refCount++,d.memoizedState={element:o,isDehydrated:a,cache:n},af(d),e}function R_(e){return e?(e=os,e):os}function C_(e,n,a,o,u,d){u=R_(u),o.context===null?o.context=u:o.pendingContext=u,o=Va(n),o.payload={element:a},d=d===void 0?null:d,d!==null&&(o.callback=d),a=ka(e,o,n),a!==null&&(Qn(a,e,n),To(a,e,n))}function w_(e,n){if(e=e.memoizedState,e!==null&&e.dehydrated!==null){var a=e.retryLane;e.retryLane=a!==0&&a<n?a:n}}function Md(e,n){w_(e,n),(e=e.alternate)&&w_(e,n)}function D_(e){if(e.tag===13||e.tag===31){var n=Er(e,67108864);n!==null&&Qn(n,e,67108864),Md(e,67108864)}}function N_(e){if(e.tag===13||e.tag===31){var n=ui();n=so(n);var a=Er(e,n);a!==null&&Qn(a,e,n),Md(e,n)}}var Ec=!0;function AM(e,n,a,o){var u=I.T;I.T=null;var d=X.p;try{X.p=2,Ed(e,n,a,o)}finally{X.p=d,I.T=u}}function RM(e,n,a,o){var u=I.T;I.T=null;var d=X.p;try{X.p=8,Ed(e,n,a,o)}finally{X.p=d,I.T=u}}function Ed(e,n,a,o){if(Ec){var u=bd(o);if(u===null)cd(e,n,o,bc,a),L_(e,o);else if(wM(u,e,n,a,o))o.stopPropagation();else if(L_(e,o),n&4&&-1<CM.indexOf(e)){for(;u!==null;){var d=na(u);if(d!==null)switch(d.tag){case 3:if(d=d.stateNode,d.current.memoizedState.isDehydrated){var y=Ct(d.pendingLanes);if(y!==0){var w=d;for(w.pendingLanes|=2,w.entangledLanes|=2;y;){var V=1<<31-Gt(y);w.entanglements[1]|=V,y&=~V}Gi(d),(Oe&6)===0&&(sc=ke()+500,Go(0))}}break;case 31:case 13:w=Er(d,2),w!==null&&Qn(w,d,2),lc(),Md(d,2)}if(d=bd(o),d===null&&cd(e,n,o,bc,a),d===u)break;u=d}u!==null&&o.stopPropagation()}else cd(e,n,o,null,a)}}function bd(e){return e=Tu(e),Td(e)}var bc=null;function Td(e){if(bc=null,e=ea(e),e!==null){var n=c(e);if(n===null)e=null;else{var a=n.tag;if(a===13){if(e=f(n),e!==null)return e;e=null}else if(a===31){if(e=h(n),e!==null)return e;e=null}else if(a===3){if(n.stateNode.current.memoizedState.isDehydrated)return n.tag===3?n.stateNode.containerInfo:null;e=null}else n!==e&&(e=null)}}return bc=e,null}function U_(e){switch(e){case"beforetoggle":case"cancel":case"click":case"close":case"contextmenu":case"copy":case"cut":case"auxclick":case"dblclick":case"dragend":case"dragstart":case"drop":case"focusin":case"focusout":case"input":case"invalid":case"keydown":case"keypress":case"keyup":case"mousedown":case"mouseup":case"paste":case"pause":case"play":case"pointercancel":case"pointerdown":case"pointerup":case"ratechange":case"reset":case"resize":case"seeked":case"submit":case"toggle":case"touchcancel":case"touchend":case"touchstart":case"volumechange":case"change":case"selectionchange":case"textInput":case"compositionstart":case"compositionend":case"compositionupdate":case"beforeblur":case"afterblur":case"beforeinput":case"blur":case"fullscreenchange":case"focus":case"hashchange":case"popstate":case"select":case"selectstart":return 2;case"drag":case"dragenter":case"dragexit":case"dragleave":case"dragover":case"mousemove":case"mouseout":case"mouseover":case"pointermove":case"pointerout":case"pointerover":case"scroll":case"touchmove":case"wheel":case"mouseenter":case"mouseleave":case"pointerenter":case"pointerleave":return 8;case"message":switch(Le()){case B:return 2;case T:return 8;case et:case ut:return 32;case mt:return 268435456;default:return 32}default:return 32}}var Ad=!1,tr=null,er=null,nr=null,Zo=new Map,jo=new Map,ir=[],CM="mousedown mouseup touchcancel touchend touchstart auxclick dblclick pointercancel pointerdown pointerup dragend dragstart drop compositionend compositionstart keydown keypress keyup input textInput copy cut paste click change contextmenu reset".split(" ");function L_(e,n){switch(e){case"focusin":case"focusout":tr=null;break;case"dragenter":case"dragleave":er=null;break;case"mouseover":case"mouseout":nr=null;break;case"pointerover":case"pointerout":Zo.delete(n.pointerId);break;case"gotpointercapture":case"lostpointercapture":jo.delete(n.pointerId)}}function Ko(e,n,a,o,u,d){return e===null||e.nativeEvent!==d?(e={blockedOn:n,domEventName:a,eventSystemFlags:o,nativeEvent:d,targetContainers:[u]},n!==null&&(n=na(n),n!==null&&D_(n)),e):(e.eventSystemFlags|=o,n=e.targetContainers,u!==null&&n.indexOf(u)===-1&&n.push(u),e)}function wM(e,n,a,o,u){switch(n){case"focusin":return tr=Ko(tr,e,n,a,o,u),!0;case"dragenter":return er=Ko(er,e,n,a,o,u),!0;case"mouseover":return nr=Ko(nr,e,n,a,o,u),!0;case"pointerover":var d=u.pointerId;return Zo.set(d,Ko(Zo.get(d)||null,e,n,a,o,u)),!0;case"gotpointercapture":return d=u.pointerId,jo.set(d,Ko(jo.get(d)||null,e,n,a,o,u)),!0}return!1}function O_(e){var n=ea(e.target);if(n!==null){var a=c(n);if(a!==null){if(n=a.tag,n===13){if(n=f(a),n!==null){e.blockedOn=n,ts(e.priority,function(){N_(a)});return}}else if(n===31){if(n=h(a),n!==null){e.blockedOn=n,ts(e.priority,function(){N_(a)});return}}else if(n===3&&a.stateNode.current.memoizedState.isDehydrated){e.blockedOn=a.tag===3?a.stateNode.containerInfo:null;return}}}e.blockedOn=null}function Tc(e){if(e.blockedOn!==null)return!1;for(var n=e.targetContainers;0<n.length;){var a=bd(e.nativeEvent);if(a===null){a=e.nativeEvent;var o=new a.constructor(a.type,a);bu=o,a.target.dispatchEvent(o),bu=null}else return n=na(a),n!==null&&D_(n),e.blockedOn=a,!1;n.shift()}return!0}function P_(e,n,a){Tc(e)&&a.delete(n)}function DM(){Ad=!1,tr!==null&&Tc(tr)&&(tr=null),er!==null&&Tc(er)&&(er=null),nr!==null&&Tc(nr)&&(nr=null),Zo.forEach(P_),jo.forEach(P_)}function Ac(e,n){e.blockedOn===n&&(e.blockedOn=null,Ad||(Ad=!0,s.unstable_scheduleCallback(s.unstable_NormalPriority,DM)))}var Rc=null;function B_(e){Rc!==e&&(Rc=e,s.unstable_scheduleCallback(s.unstable_NormalPriority,function(){Rc===e&&(Rc=null);for(var n=0;n<e.length;n+=3){var a=e[n],o=e[n+1],u=e[n+2];if(typeof o!="function"){if(Td(o||a)===null)continue;break}var d=na(a);d!==null&&(e.splice(n,3),n-=3,bf(d,{pending:!0,data:u,method:a.method,action:o},o,u))}}))}function Ns(e){function n(V){return Ac(V,e)}tr!==null&&Ac(tr,e),er!==null&&Ac(er,e),nr!==null&&Ac(nr,e),Zo.forEach(n),jo.forEach(n);for(var a=0;a<ir.length;a++){var o=ir[a];o.blockedOn===e&&(o.blockedOn=null)}for(;0<ir.length&&(a=ir[0],a.blockedOn===null);)O_(a),a.blockedOn===null&&ir.shift();if(a=(e.ownerDocument||e).$$reactFormReplay,a!=null)for(o=0;o<a.length;o+=3){var u=a[o],d=a[o+1],y=u[Dn]||null;if(typeof d=="function")y||B_(a);else if(y){var w=null;if(d&&d.hasAttribute("formAction")){if(u=d,y=d[Dn]||null)w=y.formAction;else if(Td(u)!==null)continue}else w=y.action;typeof w=="function"?a[o+1]=w:(a.splice(o,3),o-=3),B_(a)}}}function I_(){function e(d){d.canIntercept&&d.info==="react-transition"&&d.intercept({handler:function(){return new Promise(function(y){return u=y})},focusReset:"manual",scroll:"manual"})}function n(){u!==null&&(u(),u=null),o||setTimeout(a,20)}function a(){if(!o&&!navigation.transition){var d=navigation.currentEntry;d&&d.url!=null&&navigation.navigate(d.url,{state:d.getState(),info:"react-transition",history:"replace"})}}if(typeof navigation=="object"){var o=!1,u=null;return navigation.addEventListener("navigate",e),navigation.addEventListener("navigatesuccess",n),navigation.addEventListener("navigateerror",n),setTimeout(a,100),function(){o=!0,navigation.removeEventListener("navigate",e),navigation.removeEventListener("navigatesuccess",n),navigation.removeEventListener("navigateerror",n),u!==null&&(u(),u=null)}}}function Rd(e){this._internalRoot=e}Cc.prototype.render=Rd.prototype.render=function(e){var n=this._internalRoot;if(n===null)throw Error(r(409));var a=n.current,o=ui();C_(a,o,e,n,null,null)},Cc.prototype.unmount=Rd.prototype.unmount=function(){var e=this._internalRoot;if(e!==null){this._internalRoot=null;var n=e.containerInfo;C_(e.current,2,null,e,null,null),lc(),n[qn]=null}};function Cc(e){this._internalRoot=e}Cc.prototype.unstable_scheduleHydration=function(e){if(e){var n=lo();e={blockedOn:null,target:e,priority:n};for(var a=0;a<ir.length&&n!==0&&n<ir[a].priority;a++);ir.splice(a,0,e),a===0&&O_(e)}};var z_=t.version;if(z_!=="19.2.0")throw Error(r(527,z_,"19.2.0"));X.findDOMNode=function(e){var n=e._reactInternals;if(n===void 0)throw typeof e.render=="function"?Error(r(188)):(e=Object.keys(e).join(","),Error(r(268,e)));return e=p(n),e=e!==null?_(e):null,e=e===null?null:e.stateNode,e};var NM={bundleType:0,version:"19.2.0",rendererPackageName:"react-dom",currentDispatcherRef:I,reconcilerVersion:"19.2.0"};if(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__<"u"){var wc=__REACT_DEVTOOLS_GLOBAL_HOOK__;if(!wc.isDisabled&&wc.supportsFiber)try{pt=wc.inject(NM),gt=wc}catch{}}return Jo.createRoot=function(e,n){if(!l(e))throw Error(r(299));var a=!1,o="",u=q0,d=W0,y=Y0;return n!=null&&(n.unstable_strictMode===!0&&(a=!0),n.identifierPrefix!==void 0&&(o=n.identifierPrefix),n.onUncaughtError!==void 0&&(u=n.onUncaughtError),n.onCaughtError!==void 0&&(d=n.onCaughtError),n.onRecoverableError!==void 0&&(y=n.onRecoverableError)),n=A_(e,1,!1,null,null,a,o,null,u,d,y,I_),e[qn]=n.current,ld(e),new Rd(n)},Jo.hydrateRoot=function(e,n,a){if(!l(e))throw Error(r(299));var o=!1,u="",d=q0,y=W0,w=Y0,V=null;return a!=null&&(a.unstable_strictMode===!0&&(o=!0),a.identifierPrefix!==void 0&&(u=a.identifierPrefix),a.onUncaughtError!==void 0&&(d=a.onUncaughtError),a.onCaughtError!==void 0&&(y=a.onCaughtError),a.onRecoverableError!==void 0&&(w=a.onRecoverableError),a.formState!==void 0&&(V=a.formState)),n=A_(e,1,!0,n,a??null,o,u,V,d,y,w,I_),n.context=R_(null),a=n.current,o=ui(),o=so(o),u=Va(o),u.callback=null,ka(a,u,o),a=o,n.current.lanes=a,qt(n,a),Gi(n),e[qn]=n.current,ld(e),new Cc(n)},Jo.version="19.2.0",Jo}var Z_;function VM(){if(Z_)return Dd.exports;Z_=1;function s(){if(!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__>"u"||typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE!="function"))try{__REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(s)}catch(t){console.error(t)}}return s(),Dd.exports=GM(),Dd.exports}var kM=VM();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const Hx=(...s)=>s.filter((t,i,r)=>!!t&&t.trim()!==""&&r.indexOf(t)===i).join(" ").trim();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const XM=s=>s.replace(/([a-z0-9])([A-Z])/g,"$1-$2").toLowerCase();/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const qM=s=>s.replace(/^([A-Z])|[\s-_]+(\w)/g,(t,i,r)=>r?r.toUpperCase():i.toLowerCase());/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const j_=s=>{const t=qM(s);return t.charAt(0).toUpperCase()+t.slice(1)};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */var Od={xmlns:"http://www.w3.org/2000/svg",width:24,height:24,viewBox:"0 0 24 24",fill:"none",stroke:"currentColor",strokeWidth:2,strokeLinecap:"round",strokeLinejoin:"round"};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const WM=s=>{for(const t in s)if(t.startsWith("aria-")||t==="role"||t==="title")return!0;return!1},YM=rn.createContext({}),ZM=()=>rn.useContext(YM),jM=rn.forwardRef(({color:s,size:t,strokeWidth:i,absoluteStrokeWidth:r,className:l="",children:c,iconNode:f,...h},m)=>{const{size:p=24,strokeWidth:_=2,absoluteStrokeWidth:v=!1,color:g="currentColor",className:x=""}=ZM()??{},M=r??v?Number(i??_)*24/Number(t??p):i??_;return rn.createElement("svg",{ref:m,...Od,width:t??p??Od.width,height:t??p??Od.height,stroke:s??g,strokeWidth:M,className:Hx("lucide",x,l),...!c&&!WM(h)&&{"aria-hidden":"true"},...h},[...f.map(([A,E])=>rn.createElement(A,E)),...Array.isArray(c)?c:[c]])});/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const hn=(s,t)=>{const i=rn.forwardRef(({className:r,...l},c)=>rn.createElement(jM,{ref:c,iconNode:t,className:Hx(`lucide-${XM(j_(s))}`,`lucide-${s}`,r),...l}));return i.displayName=j_(s),i};/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const KM=[["path",{d:"M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2",key:"169zse"}]],QM=hn("activity",KM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const JM=[["path",{d:"m12 19-7-7 7-7",key:"1l729n"}],["path",{d:"M19 12H5",key:"x3x0zl"}]],Gx=hn("arrow-left",JM);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const $M=[["path",{d:"M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z",key:"hh9hay"}],["path",{d:"m3.3 7 8.7 5 8.7-5",key:"g66t2b"}],["path",{d:"M12 22V12",key:"d0xqtd"}]],t1=hn("box",$M);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const e1=[["path",{d:"M2.97 12.92A2 2 0 0 0 2 14.63v3.24a2 2 0 0 0 .97 1.71l3 1.8a2 2 0 0 0 2.06 0L12 19v-5.5l-5-3-4.03 2.42Z",key:"lc1i9w"}],["path",{d:"m7 16.5-4.74-2.85",key:"1o9zyk"}],["path",{d:"m7 16.5 5-3",key:"va8pkn"}],["path",{d:"M7 16.5v5.17",key:"jnp8gn"}],["path",{d:"M12 13.5V19l3.97 2.38a2 2 0 0 0 2.06 0l3-1.8a2 2 0 0 0 .97-1.71v-3.24a2 2 0 0 0-.97-1.71L17 10.5l-5 3Z",key:"8zsnat"}],["path",{d:"m17 16.5-5-3",key:"8arw3v"}],["path",{d:"m17 16.5 4.74-2.85",key:"8rfmw"}],["path",{d:"M17 16.5v5.17",key:"k6z78m"}],["path",{d:"M7.97 4.42A2 2 0 0 0 7 6.13v4.37l5 3 5-3V6.13a2 2 0 0 0-.97-1.71l-3-1.8a2 2 0 0 0-2.06 0l-3 1.8Z",key:"1xygjf"}],["path",{d:"M12 8 7.26 5.15",key:"1vbdud"}],["path",{d:"m12 8 4.74-2.85",key:"3rx089"}],["path",{d:"M12 13.5V8",key:"1io7kd"}]],K_=hn("boxes",e1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const n1=[["path",{d:"M10 12h4",key:"a56b0p"}],["path",{d:"M10 8h4",key:"1sr2af"}],["path",{d:"M14 21v-3a2 2 0 0 0-4 0v3",key:"1rgiei"}],["path",{d:"M6 10H4a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2",key:"secmi2"}],["path",{d:"M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16",key:"16ra0t"}]],i1=hn("building-2",n1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const a1=[["path",{d:"M16 14v2.2l1.6 1",key:"fo4ql5"}],["path",{d:"M16 2v3",key:"otl347"}],["path",{d:"M21 7.338V5a2 2 0 00-2-2H5a2 2 0 00-2 2v14a2 2 0 002 2h2.338",key:"7hb8p4"}],["path",{d:"M3 9h5.859",key:"numkqi"}],["path",{d:"M8 2v3",key:"1ioesn"}],["circle",{cx:"16",cy:"16",r:"6",key:"qoo3c4"}]],Vx=hn("calendar-clock",a1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const r1=[["path",{d:"M8 2v3",key:"1ioesn"}],["path",{d:"M16 2v3",key:"otl347"}],["rect",{x:"3",y:"3",width:"18",height:"18",rx:"2",key:"h1oib"}],["path",{d:"M3 9h18",key:"1pudct"}],["path",{d:"M8 13h.01",key:"1sbv64"}],["path",{d:"M12 13h.01",key:"y0uutt"}],["path",{d:"M16 13h.01",key:"wip0gl"}],["path",{d:"M8 17h.01",key:"p3bg7i"}],["path",{d:"M12 17h.01",key:"p32p05"}],["path",{d:"M16 17h.01",key:"ql8jdd"}]],kx=hn("calendar-days",r1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const s1=[["circle",{cx:"12",cy:"12",r:"10",key:"1mglay"}],["path",{d:"m9 12 2 2 4-4",key:"dzmm74"}]],o1=hn("circle-check",s1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const l1=[["circle",{cx:"12",cy:"12",r:"10",key:"1mglay"}],["path",{d:"M12 6v6h4",key:"135r8i"}]],Xx=hn("clock-3",l1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const c1=[["path",{d:"M12 20v2",key:"1lh1kg"}],["path",{d:"M12 2v2",key:"tus03m"}],["path",{d:"M17 20v2",key:"1rnc9c"}],["path",{d:"M17 2v2",key:"11trls"}],["path",{d:"M2 12h2",key:"1t8f8n"}],["path",{d:"M2 17h2",key:"7oei6x"}],["path",{d:"M2 7h2",key:"asdhe0"}],["path",{d:"M20 12h2",key:"1q8mjw"}],["path",{d:"M20 17h2",key:"1fpfkl"}],["path",{d:"M20 7h2",key:"1o8tra"}],["path",{d:"M7 20v2",key:"4gnj0m"}],["path",{d:"M7 2v2",key:"1i4yhu"}],["rect",{x:"4",y:"4",width:"16",height:"16",rx:"2",key:"1vbyd7"}],["rect",{x:"8",y:"8",width:"8",height:"8",rx:"1",key:"z9xiuo"}]],u1=hn("cpu",c1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const f1=[["path",{d:"M11 20H2",key:"nlcfvz"}],["path",{d:"M11 4.562v16.157a1 1 0 0 0 1.242.97L19 20V5.562a2 2 0 0 0-1.515-1.94l-4-1A2 2 0 0 0 11 4.561z",key:"au4z13"}],["path",{d:"M11 4H8a2 2 0 0 0-2 2v14",key:"74r1mk"}],["path",{d:"M14 12h.01",key:"1jfl7z"}],["path",{d:"M22 20h-3",key:"vhrsz"}]],d1=hn("door-open",f1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const h1=[["path",{d:"M15 3h6v6",key:"1q9fwt"}],["path",{d:"m21 3-7 7",key:"1l2asr"}],["path",{d:"m3 21 7-7",key:"tjx5ai"}],["path",{d:"M9 21H3v-6",key:"wtvkvv"}]],p1=hn("maximize-2",h1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const m1=[["rect",{width:"5",height:"5",x:"3",y:"3",rx:"1",key:"1tu5fj"}],["rect",{width:"5",height:"5",x:"16",y:"3",rx:"1",key:"1v8r4q"}],["rect",{width:"5",height:"5",x:"3",y:"16",rx:"1",key:"1x03jg"}],["path",{d:"M21 16h-3a2 2 0 0 0-2 2v3",key:"177gqh"}],["path",{d:"M21 21v.01",key:"ents32"}],["path",{d:"M12 7v3a2 2 0 0 1-2 2H7",key:"8crl2c"}],["path",{d:"M3 12h.01",key:"nlz23k"}],["path",{d:"M12 3h.01",key:"n36tog"}],["path",{d:"M12 16v.01",key:"133mhm"}],["path",{d:"M16 12h1",key:"1slzba"}],["path",{d:"M21 12v.01",key:"1lwtk9"}],["path",{d:"M12 21v-1",key:"1880an"}]],g1=hn("qr-code",m1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const _1=[["path",{d:"M16.247 7.761a6 6 0 0 1 0 8.478",key:"1fwjs5"}],["path",{d:"M19.075 4.933a10 10 0 0 1 0 14.134",key:"ehdyv1"}],["path",{d:"M4.925 19.067a10 10 0 0 1 0-14.134",key:"1q22gi"}],["path",{d:"M7.753 16.239a6 6 0 0 1 0-8.478",key:"r2q7qm"}],["circle",{cx:"12",cy:"12",r:"2",key:"1c9p78"}]],v1=hn("radio",_1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const x1=[["path",{d:"M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8",key:"1357e3"}],["path",{d:"M3 3v5h5",key:"1xhq8a"}],["path",{d:"M12 7v5l4 2",key:"1fdv2h"}]],qx=hn("rotate-ccw-clock",x1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const S1=[["path",{d:"m21 21-4.34-4.34",key:"14j7rj"}],["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}]],y1=hn("search",S1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const M1=[["path",{d:"M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915",key:"1i5ecw"}],["circle",{cx:"12",cy:"12",r:"3",key:"1v7zrd"}]],E1=hn("settings",M1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const b1=[["path",{d:"M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",key:"oel41y"}],["path",{d:"m9 12 2 2 4-4",key:"dzmm74"}]],T1=hn("shield-check",b1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const A1=[["circle",{cx:"12",cy:"8",r:"5",key:"1hypcn"}],["path",{d:"M20 21a8 8 0 0 0-16 0",key:"rfgkzh"}]],R1=hn("user-round",A1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const C1=[["path",{d:"M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.106-3.105c.32-.322.863-.22.983.218a6 6 0 0 1-8.259 7.057l-7.91 7.91a1 1 0 0 1-2.999-3l7.91-7.91a6 6 0 0 1 7.057-8.259c.438.12.54.662.219.984z",key:"1ngwbx"}]],w1=hn("wrench",C1);/**
 * @license lucide-react v1.28.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const D1=[["path",{d:"M18 6 6 18",key:"1bl5f8"}],["path",{d:"m6 6 12 12",key:"d8bk6v"}]],N1=hn("x",D1),kr={stored:{label:"存放中",color:"#45d483",shortLabel:"在柜"},checkedOut:{label:"检测外借",color:"#ff625f",shortLabel:"外借"},maintenance:{label:"检修中",color:"#f0b84b",shortLabel:"检修"}},U1={A:"正面柜墙",B:"左侧柜墙",C:"右侧柜墙"},L1={A:[[0,0],[1,0],[3,0],[5,0],[7,0],[8,0],[0,1],[2,1],[6,1],[8,1],[1,2],[3,2],[5,2],[7,2],[0,3],[4,3],[8,3],[4,4]],B:[[0,0],[2,0],[4,0],[6,0],[8,0],[1,1],[3,1],[5,1],[7,1],[0,2],[4,2],[8,2],[2,3],[6,3],[1,4],[3,4],[5,4],[7,4]],C:[[1,0],[3,0],[5,0],[7,0],[0,1],[2,1],[4,1],[6,1],[8,1],[1,2],[4,2],[7,2],[0,3],[3,3],[5,3],[8,3],[2,4],[6,4]]},Q_=[{equipmentName:"徕卡 LS15 数字水准仪",shortName:"LS15 水准仪",manufacturer:"Leica Geosystems",model:"LS15",range:"±15°",precision:"0.3 mm/km",department:"测绘检测部",custodian:"李一凡"},{equipmentName:"天宝 S9 高精度全站仪",shortName:"S9 全站仪",manufacturer:"Trimble Inc.",model:"S9",range:"1.5-5500 m",precision:"0.5″",department:"测绘检测部",custodian:"张伟"},{equipmentName:"喜利得 PS300 钢筋扫描仪",shortName:"PS300 扫描仪",manufacturer:"Hilti",model:"PS300",range:"0-200 mm",precision:"±1 mm",department:"结构检测部",custodian:"许文杰"},{equipmentName:"基桩动测仪 PIT-V",shortName:"PIT-V 动测仪",manufacturer:"武汉岩海工程技术有限公司",model:"PIT-V",range:"0-20 kHz",precision:"≤1.0%",department:"基桩检测部",custodian:"陈嘉豪"},{equipmentName:"多跨孔超声波循测仪 RSM-SY7(E)",shortName:"超声波循测仪",manufacturer:"武汉中岩科技股份有限公司",model:"RSM-SY7(E)",range:"0-500 μs",precision:"0.1 μs",department:"基桩检测部",custodian:"周成"},{equipmentName:"电子天平 TC30K-H",shortName:"电子天平",manufacturer:"常熟市双杰测试仪器厂",model:"TC30K-H",range:"0-30 kg",precision:"d=1 g",department:"材料检测部",custodian:"林晓敏"},{equipmentName:"蔡司 DiNi03 数字水准仪",shortName:"DiNi03 水准仪",manufacturer:"Carl Zeiss",model:"DiNi03",range:"1.5-100 m",precision:"0.3 mm/km",department:"测绘检测部",custodian:"王磊"},{equipmentName:"德图 885 红外热像仪",shortName:"红外热像仪",manufacturer:"Testo SE & Co. KGaA",model:"testo 885",range:"-30-650 ℃",precision:"±2 ℃",department:"结构检测部",custodian:"梁凯"},{equipmentName:"博势 GP8000 混凝土雷达",shortName:"GP8000 雷达",manufacturer:"Screening Eagle Technologies",model:"GP8000",range:"0-800 mm",precision:"±5 mm",department:"结构检测部",custodian:"赵璐"},{equipmentName:"福禄克 87V 数字万用表",shortName:"87V 万用表",manufacturer:"Fluke Corporation",model:"87V",range:"0-1000 V",precision:"±0.05%",department:"设备保障部",custodian:"吴迪"},{equipmentName:"智博联 ZBL-U520 超声检测仪",shortName:"U520 超声仪",manufacturer:"北京智博联科技股份有限公司",model:"ZBL-U520",range:"0-9999 μs",precision:"0.05 μs",department:"结构检测部",custodian:"黄莹"},{equipmentName:"海创高科 HT-225W 数显回弹仪",shortName:"数显回弹仪",manufacturer:"山东海创高科仪器有限公司",model:"HT-225W",range:"10-60 MPa",precision:"±0.5",department:"材料检测部",custodian:"高远"},{equipmentName:"天宝 R12i GNSS 接收机",shortName:"R12i 接收机",manufacturer:"Trimble Inc.",model:"R12i",range:"全星座",precision:"8 mm+1 ppm",department:"测绘检测部",custodian:"李强"},{equipmentName:"ISO 标准维卡仪",shortName:"标准维卡仪",manufacturer:"无锡建仪仪器机械有限公司",model:"VICAT-II",range:"0-70 mm",precision:"0.1 mm",department:"材料检测部",custodian:"孙媛"},{equipmentName:"智博联 ZBL-R630A 钢筋检测仪",shortName:"R630A 检测仪",manufacturer:"北京智博联科技股份有限公司",model:"ZBL-R630A",range:"6-50 mm",precision:"±1 mm",department:"结构检测部",custodian:"许文杰"},{equipmentName:"DJCK-2 裂缝宽度测量仪",shortName:"裂缝测量仪",manufacturer:"南京南大工程检测有限公司",model:"DJCK-2",range:"0-10 mm",precision:"0.01 mm",department:"结构检测部",custodian:"梁凯"},{equipmentName:"东华 DH3816N 静态应变系统",shortName:"静态应变系统",manufacturer:"江苏东华测试技术股份有限公司",model:"DH3816N",range:"±20000 με",precision:"±0.5%",department:"结构检测部",custodian:"赵璐"},{equipmentName:"徕卡 D510 激光测距仪",shortName:"D510 测距仪",manufacturer:"Leica Geosystems",model:"DISTO D510",range:"0.05-200 m",precision:"±1 mm",department:"测绘检测部",custodian:"王磊"}],J_=["广东科准计量检测有限公司","华南国家计量测试中心","广州市计量检测技术研究院"],$_=["stored","stored","checkedOut","stored","maintenance","stored","checkedOut","stored","stored"],tv=["2026-08-11","2026-08-26","2026-09-18","2026-10-09","2026-11-22","2027-01-15","2027-02-20","2027-03-04","2027-04-17","2027-05-08"];function O1(s,t,i){const r=String(10+i%18).padStart(2,"0"),l=t==="checkedOut"?"未归还":t==="maintenance"?"检修中":`2026-07-${r} 17:40`;return[{project:`JX-2026-${String(60+i).padStart(3,"0")} 在建项目检测`,department:s.department,user:s.custodian,checkout:`2026-07-${r} 08:20`,returned:l},{project:"城市基础设施质量抽检",department:s.department,user:s.custodian,checkout:"2026-06-18 08:40",returned:"2026-06-20 17:15"},{project:"年度设备比对试验",department:s.department,user:s.custodian,checkout:"2026-05-09 09:00",returned:"2026-05-09 16:50"}]}const Vi=Object.entries(L1).flatMap(([s,t],i)=>t.map(([r,l],c)=>{const f=i*t.length+c,h=Q_[(c+i*6)%Q_.length],m=$_[(f+i)%$_.length],p=`IC-${String(f+1).padStart(2,"0")}`,_=`${s}-${String(r+1).padStart(2,"0")}-${String(l+1).padStart(2,"0")}`,v=tv[f%tv.length];return{...h,id:p,bankId:s,cabinetGroup:U1[s],lockerNo:_,slotColumn:r,slotRow:l,status:m,managementNo:`JX-${String(1001+f)}`,serialNo:`JX26-${s}${String(2100+f)}`,verificationAgency:J_[f%J_.length],lastCalibration:`2026-${String(1+f%6).padStart(2,"0")}-${String(8+f%18).padStart(2,"0")}`,nextCalibration:v,history:O1(h,m,f)}})),P1=[{time:"15:22",text:"IC-39 已完成归还核验",status:"stored"},{time:"14:48",text:"IC-21 已转入检修流程",status:"maintenance"},{time:"13:16",text:"IC-07 已由测绘检测部领用",status:"checkedOut"},{time:"11:42",text:"IC-31 校准证书已归档",status:"stored"},{time:"10:08",text:"IC-45 完成设备点检",status:"stored"},{time:"09:36",text:"IC-14 已生成检定提醒",status:"maintenance"}],B1={A:"正面仪器柜",B:"左侧仪器柜",C:"右侧仪器柜"};function I1({instruments:s,selectedBankId:t}){const i=t?s.filter(c=>c.bankId===t):s,r=[...i].sort((c,f)=>c.nextCalibration.localeCompare(f.nextCalibration)).slice(0,5),l={stored:i.filter(c=>c.status==="stored").length,checkedOut:i.filter(c=>c.status==="checkedOut").length,maintenance:i.filter(c=>c.status==="maintenance").length};return H.jsxs("aside",{className:"detail-rail","aria-label":"仪器室运行状态",children:[H.jsxs("section",{className:"rail-section",children:[H.jsxs("div",{className:"section-title",children:[H.jsx(v1,{size:15}),H.jsx("h2",{children:t?"当前柜墙状态":"仪器室状态"})]}),H.jsxs("dl",{className:"monitor-list",children:[H.jsxs("div",{children:[H.jsx("dt",{children:"机房名称"}),H.jsx("dd",{children:"计量仪器室 A"})]}),t&&H.jsxs("div",{children:[H.jsx("dt",{children:"当前视角"}),H.jsx("dd",{children:B1[t]})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"柜墙数量"}),H.jsx("dd",{children:"3 面"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"单墙规格"}),H.jsx("dd",{children:"9 × 5 格"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"有效格口"}),H.jsx("dd",{children:"133 个"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"公司屏幕"}),H.jsx("dd",{children:"1 块"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"登记设备"}),H.jsxs("dd",{children:[i.length," 台"]})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"在柜设备"}),H.jsxs("dd",{className:"ok",children:[l.stored," 台"]})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"检测外借"}),H.jsxs("dd",{className:"alert",children:[l.checkedOut," 台"]})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"检修设备"}),H.jsxs("dd",{className:"warning",children:[l.maintenance," 台"]})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"环境温度"}),H.jsx("dd",{children:"23.6 ℃"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"相对湿度"}),H.jsx("dd",{children:"45 %RH"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"数据状态"}),H.jsx("dd",{className:"ok",children:"同步正常"})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"格口门传感"}),H.jsx("dd",{className:"muted",children:"未接入"})]})]})]}),H.jsxs("section",{className:"rail-section",children:[H.jsxs("div",{className:"section-title",children:[H.jsx(Vx,{size:15}),H.jsx("h2",{children:"近期检定计划"}),H.jsx("span",{children:r.length})]}),H.jsx("div",{className:"verification-list",children:r.map(c=>H.jsxs("div",{children:[H.jsx("span",{children:c.id}),H.jsx("strong",{children:c.shortName}),H.jsx("time",{children:c.nextCalibration})]},c.id))})]}),H.jsxs("section",{className:"rail-section rail-section--grow",children:[H.jsxs("div",{className:"section-title",children:[H.jsx(qx,{size:15}),H.jsx("h2",{children:"近期动态"})]}),H.jsx("div",{className:"activity-list",children:P1.map(c=>H.jsxs("div",{children:[H.jsx("time",{children:c.time}),H.jsx("i",{style:{backgroundColor:kr[c.status].color}}),H.jsx("span",{children:c.text})]},`${c.time}-${c.text}`))})]})]})}function z1(s){return H.jsx(I1,{instruments:s.instruments,selectedBankId:s.selectedBankId})}function F1({instrument:s,onClose:t}){return s?H.jsx("div",{className:"history-dialog-backdrop",onMouseDown:t,children:H.jsxs("section",{"aria-label":`${s.equipmentName} 使用履历`,"aria-modal":"true",className:"history-dialog",onMouseDown:i=>i.stopPropagation(),role:"dialog",children:[H.jsxs("header",{children:[H.jsxs("div",{children:[H.jsxs("span",{children:[s.id," · ",s.managementNo]}),H.jsx("h2",{children:s.equipmentName})]}),H.jsx("button",{onClick:t,type:"button","aria-label":"关闭设备履历",children:H.jsx(N1,{size:18})})]}),H.jsxs("div",{className:"history-dialog__summary",children:[H.jsxs("span",{children:[H.jsx(kx,{size:14})," 最近 90 天使用 ",s.history.length," 次"]}),H.jsxs("span",{children:[H.jsx(Xx,{size:14})," 当前记录已同步"]})]}),H.jsx("div",{className:"mobile-history",children:s.history.map((i,r)=>H.jsxs("article",{children:[H.jsx("span",{children:String(r+1).padStart(2,"0")}),H.jsxs("div",{children:[H.jsx("h3",{children:i.project}),H.jsxs("p",{children:[i.department," · ",i.user]}),H.jsxs("dl",{children:[H.jsxs("div",{children:[H.jsx("dt",{children:"领用"}),H.jsx("dd",{children:i.checkout})]}),H.jsxs("div",{children:[H.jsx("dt",{children:"归还"}),H.jsx("dd",{children:i.returned})]})]})]})]},i.project))})]})}):null}/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */const Pp="185",H1=0,ev=1,G1=2,ul=1,V1=2,ll=3,pr=0,Jn=1,Yi=2,Ca=0,js=1,Oi=2,nv=3,iv=4,k1=5,Gr=100,X1=101,q1=102,W1=103,Y1=104,Z1=200,j1=201,K1=202,Q1=203,Hh=204,Gh=205,J1=206,$1=207,tE=208,eE=209,nE=210,iE=211,aE=212,rE=213,sE=214,Vh=0,kh=1,Xh=2,to=3,qh=4,Wh=5,Yh=6,Zh=7,Wx=0,oE=1,lE=2,Ki=0,Yx=1,Zx=2,jx=3,Bp=4,Kx=5,Qx=6,Jx=7,$x=300,Wr=301,eo=302,Pd=303,Bd=304,Su=306,jh=1e3,Aa=1001,Kh=1002,Pn=1003,cE=1004,Dc=1005,wn=1006,Id=1007,Xr=1008,pi=1009,tS=1010,eS=1011,dl=1012,Ip=1013,$i=1014,Zi=1015,Da=1016,zp=1017,Fp=1018,hl=1020,nS=35902,iS=35899,aS=1021,rS=1022,Pi=1023,Na=1026,qr=1027,sS=1028,Hp=1029,Yr=1030,Gp=1031,Vp=1033,su=33776,ou=33777,lu=33778,cu=33779,Qh=35840,Jh=35841,$h=35842,tp=35843,ep=36196,np=37492,ip=37496,ap=37488,rp=37489,du=37490,sp=37491,op=37808,lp=37809,cp=37810,up=37811,fp=37812,dp=37813,hp=37814,pp=37815,mp=37816,gp=37817,_p=37818,vp=37819,xp=37820,Sp=37821,yp=36492,Mp=36494,Ep=36495,bp=36283,Tp=36284,hu=36285,Ap=36286,uE=3200,Rp=0,fE=1,dr="",Gn="srgb",pu="srgb-linear",mu="linear",Ve="srgb",Us=7680,av=519,dE=512,hE=513,pE=514,kp=515,mE=516,gE=517,Xp=518,_E=519,rv=35044,sv="300 es",ji=2e3,pl=2001;function vE(s){for(let t=s.length-1;t>=0;--t)if(s[t]>=65535)return!0;return!1}function ml(s){return document.createElementNS("http://www.w3.org/1999/xhtml",s)}function xE(){const s=ml("canvas");return s.style.display="block",s}const ov={};function lv(...s){const t="THREE."+s.shift();console.log(t,...s)}function oS(s){const t=s[0];if(typeof t=="string"&&t.startsWith("TSL:")){const i=s[1];i&&i.isStackTrace?s[0]+=" "+i.getLocation():s[1]='Stack trace not available. Enable "THREE.Node.captureStackTrace" to capture stack traces.'}return s}function se(...s){s=oS(s);const t="THREE."+s.shift();{const i=s[0];i&&i.isStackTrace?console.warn(i.getError(t)):console.warn(t,...s)}}function Re(...s){s=oS(s);const t="THREE."+s.shift();{const i=s[0];i&&i.isStackTrace?console.error(i.getError(t)):console.error(t,...s)}}function Ks(...s){const t=s.join(" ");t in ov||(ov[t]=!0,se(...s))}function SE(s,t,i){return new Promise(function(r,l){function c(){switch(s.clientWaitSync(t,s.SYNC_FLUSH_COMMANDS_BIT,0)){case s.WAIT_FAILED:l();break;case s.TIMEOUT_EXPIRED:setTimeout(c,i);break;default:r()}}setTimeout(c,i)})}const yE={[Vh]:kh,[Xh]:Yh,[qh]:Zh,[to]:Wh,[kh]:Vh,[Yh]:Xh,[Zh]:qh,[Wh]:to};class jr{addEventListener(t,i){this._listeners===void 0&&(this._listeners={});const r=this._listeners;r[t]===void 0&&(r[t]=[]),r[t].indexOf(i)===-1&&r[t].push(i)}hasEventListener(t,i){const r=this._listeners;return r===void 0?!1:r[t]!==void 0&&r[t].indexOf(i)!==-1}removeEventListener(t,i){const r=this._listeners;if(r===void 0)return;const l=r[t];if(l!==void 0){const c=l.indexOf(i);c!==-1&&l.splice(c,1)}}dispatchEvent(t){const i=this._listeners;if(i===void 0)return;const r=i[t.type];if(r!==void 0){t.target=this;const l=r.slice(0);for(let c=0,f=l.length;c<f;c++)l[c].call(this,t);t.target=null}}}const Fn=["00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f","10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f","20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f","30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f","40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f","50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f","60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f","70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f","80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f","90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f","a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af","b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf","c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf","d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df","e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef","f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"];let cv=1234567;const Qs=Math.PI/180,gl=180/Math.PI;function ao(){const s=Math.random()*4294967295|0,t=Math.random()*4294967295|0,i=Math.random()*4294967295|0,r=Math.random()*4294967295|0;return(Fn[s&255]+Fn[s>>8&255]+Fn[s>>16&255]+Fn[s>>24&255]+"-"+Fn[t&255]+Fn[t>>8&255]+"-"+Fn[t>>16&15|64]+Fn[t>>24&255]+"-"+Fn[i&63|128]+Fn[i>>8&255]+"-"+Fn[i>>16&255]+Fn[i>>24&255]+Fn[r&255]+Fn[r>>8&255]+Fn[r>>16&255]+Fn[r>>24&255]).toLowerCase()}function Ee(s,t,i){return Math.max(t,Math.min(i,s))}function qp(s,t){return(s%t+t)%t}function ME(s,t,i,r,l){return r+(s-t)*(l-r)/(i-t)}function EE(s,t,i){return s!==t?(i-s)/(t-s):0}function fl(s,t,i){return(1-i)*s+i*t}function bE(s,t,i,r){return fl(s,t,1-Math.exp(-i*r))}function TE(s,t=1){return t-Math.abs(qp(s,t*2)-t)}function AE(s,t,i){return s<=t?0:s>=i?1:(s=(s-t)/(i-t),s*s*(3-2*s))}function RE(s,t,i){return s<=t?0:s>=i?1:(s=(s-t)/(i-t),s*s*s*(s*(s*6-15)+10))}function CE(s,t){return s+Math.floor(Math.random()*(t-s+1))}function wE(s,t){return s+Math.random()*(t-s)}function DE(s){return s*(.5-Math.random())}function NE(s){s!==void 0&&(cv=s);let t=cv+=1831565813;return t=Math.imul(t^t>>>15,t|1),t^=t+Math.imul(t^t>>>7,t|61),((t^t>>>14)>>>0)/4294967296}function UE(s){return s*Qs}function LE(s){return s*gl}function OE(s){return(s&s-1)===0&&s!==0}function PE(s){return Math.pow(2,Math.ceil(Math.log(s)/Math.LN2))}function BE(s){return Math.pow(2,Math.floor(Math.log(s)/Math.LN2))}function IE(s,t,i,r,l){const c=Math.cos,f=Math.sin,h=c(i/2),m=f(i/2),p=c((t+r)/2),_=f((t+r)/2),v=c((t-r)/2),g=f((t-r)/2),x=c((r-t)/2),M=f((r-t)/2);switch(l){case"XYX":s.set(h*_,m*v,m*g,h*p);break;case"YZY":s.set(m*g,h*_,m*v,h*p);break;case"ZXZ":s.set(m*v,m*g,h*_,h*p);break;case"XZX":s.set(h*_,m*M,m*x,h*p);break;case"YXY":s.set(m*x,h*_,m*M,h*p);break;case"ZYZ":s.set(m*M,m*x,h*_,h*p);break;default:se("MathUtils: .setQuaternionFromProperEuler() encountered an unknown order: "+l)}}function Zs(s,t){switch(t.constructor){case Float32Array:return s;case Uint32Array:return s/4294967295;case Uint16Array:return s/65535;case Uint8Array:return s/255;case Int32Array:return Math.max(s/2147483647,-1);case Int16Array:return Math.max(s/32767,-1);case Int8Array:return Math.max(s/127,-1);default:throw new Error("THREE.MathUtils: Invalid component type.")}}function kn(s,t){switch(t.constructor){case Float32Array:return s;case Uint32Array:return Math.round(s*4294967295);case Uint16Array:return Math.round(s*65535);case Uint8Array:return Math.round(s*255);case Int32Array:return Math.round(s*2147483647);case Int16Array:return Math.round(s*32767);case Int8Array:return Math.round(s*127);default:throw new Error("THREE.MathUtils: Invalid component type.")}}const zd={DEG2RAD:Qs,RAD2DEG:gl,generateUUID:ao,clamp:Ee,euclideanModulo:qp,mapLinear:ME,inverseLerp:EE,lerp:fl,damp:bE,pingpong:TE,smoothstep:AE,smootherstep:RE,randInt:CE,randFloat:wE,randFloatSpread:DE,seededRandom:NE,degToRad:UE,radToDeg:LE,isPowerOfTwo:OE,ceilPowerOfTwo:PE,floorPowerOfTwo:BE,setQuaternionFromProperEuler:IE,normalize:kn,denormalize:Zs},nm=class nm{constructor(t=0,i=0){this.x=t,this.y=i}get width(){return this.x}set width(t){this.x=t}get height(){return this.y}set height(t){this.y=t}set(t,i){return this.x=t,this.y=i,this}setScalar(t){return this.x=t,this.y=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;default:throw new Error("THREE.Vector2: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;default:throw new Error("THREE.Vector2: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y)}copy(t){return this.x=t.x,this.y=t.y,this}add(t){return this.x+=t.x,this.y+=t.y,this}addScalar(t){return this.x+=t,this.y+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this}subScalar(t){return this.x-=t,this.y-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this}multiply(t){return this.x*=t.x,this.y*=t.y,this}multiplyScalar(t){return this.x*=t,this.y*=t,this}divide(t){return this.x/=t.x,this.y/=t.y,this}divideScalar(t){return this.multiplyScalar(1/t)}applyMatrix3(t){const i=this.x,r=this.y,l=t.elements;return this.x=l[0]*i+l[3]*r+l[6],this.y=l[1]*i+l[4]*r+l[7],this}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this}clamp(t,i){return this.x=Ee(this.x,t.x,i.x),this.y=Ee(this.y,t.y,i.y),this}clampScalar(t,i){return this.x=Ee(this.x,t,i),this.y=Ee(this.y,t,i),this}clampLength(t,i){const r=this.length();return this.divideScalar(r||1).multiplyScalar(Ee(r,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this}negate(){return this.x=-this.x,this.y=-this.y,this}dot(t){return this.x*t.x+this.y*t.y}cross(t){return this.x*t.y-this.y*t.x}lengthSq(){return this.x*this.x+this.y*this.y}length(){return Math.sqrt(this.x*this.x+this.y*this.y)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)}normalize(){return this.divideScalar(this.length()||1)}angle(){return Math.atan2(-this.y,-this.x)+Math.PI}angleTo(t){const i=Math.sqrt(this.lengthSq()*t.lengthSq());if(i===0)return Math.PI/2;const r=this.dot(t)/i;return Math.acos(Ee(r,-1,1))}distanceTo(t){return Math.sqrt(this.distanceToSquared(t))}distanceToSquared(t){const i=this.x-t.x,r=this.y-t.y;return i*i+r*r}manhattanDistanceTo(t){return Math.abs(this.x-t.x)+Math.abs(this.y-t.y)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this}lerpVectors(t,i,r){return this.x=t.x+(i.x-t.x)*r,this.y=t.y+(i.y-t.y)*r,this}equals(t){return t.x===this.x&&t.y===this.y}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this}rotateAround(t,i){const r=Math.cos(i),l=Math.sin(i),c=this.x-t.x,f=this.y-t.y;return this.x=c*r-f*l+t.x,this.y=c*l+f*r+t.y,this}random(){return this.x=Math.random(),this.y=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y}};nm.prototype.isVector2=!0;let Ae=nm;class Kr{constructor(t=0,i=0,r=0,l=1){this.isQuaternion=!0,this._x=t,this._y=i,this._z=r,this._w=l}static slerpFlat(t,i,r,l,c,f,h){let m=r[l+0],p=r[l+1],_=r[l+2],v=r[l+3],g=c[f+0],x=c[f+1],M=c[f+2],A=c[f+3];if(v!==A||m!==g||p!==x||_!==M){let E=m*g+p*x+_*M+v*A;E<0&&(g=-g,x=-x,M=-M,A=-A,E=-E);let S=1-h;if(E<.9995){const P=Math.acos(E),O=Math.sin(P);S=Math.sin(S*P)/O,h=Math.sin(h*P)/O,m=m*S+g*h,p=p*S+x*h,_=_*S+M*h,v=v*S+A*h}else{m=m*S+g*h,p=p*S+x*h,_=_*S+M*h,v=v*S+A*h;const P=1/Math.sqrt(m*m+p*p+_*_+v*v);m*=P,p*=P,_*=P,v*=P}}t[i]=m,t[i+1]=p,t[i+2]=_,t[i+3]=v}static multiplyQuaternionsFlat(t,i,r,l,c,f){const h=r[l],m=r[l+1],p=r[l+2],_=r[l+3],v=c[f],g=c[f+1],x=c[f+2],M=c[f+3];return t[i]=h*M+_*v+m*x-p*g,t[i+1]=m*M+_*g+p*v-h*x,t[i+2]=p*M+_*x+h*g-m*v,t[i+3]=_*M-h*v-m*g-p*x,t}get x(){return this._x}set x(t){this._x=t,this._onChangeCallback()}get y(){return this._y}set y(t){this._y=t,this._onChangeCallback()}get z(){return this._z}set z(t){this._z=t,this._onChangeCallback()}get w(){return this._w}set w(t){this._w=t,this._onChangeCallback()}set(t,i,r,l){return this._x=t,this._y=i,this._z=r,this._w=l,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._w)}copy(t){return this._x=t.x,this._y=t.y,this._z=t.z,this._w=t.w,this._onChangeCallback(),this}setFromEuler(t,i=!0){const r=t._x,l=t._y,c=t._z,f=t._order,h=Math.cos,m=Math.sin,p=h(r/2),_=h(l/2),v=h(c/2),g=m(r/2),x=m(l/2),M=m(c/2);switch(f){case"XYZ":this._x=g*_*v+p*x*M,this._y=p*x*v-g*_*M,this._z=p*_*M+g*x*v,this._w=p*_*v-g*x*M;break;case"YXZ":this._x=g*_*v+p*x*M,this._y=p*x*v-g*_*M,this._z=p*_*M-g*x*v,this._w=p*_*v+g*x*M;break;case"ZXY":this._x=g*_*v-p*x*M,this._y=p*x*v+g*_*M,this._z=p*_*M+g*x*v,this._w=p*_*v-g*x*M;break;case"ZYX":this._x=g*_*v-p*x*M,this._y=p*x*v+g*_*M,this._z=p*_*M-g*x*v,this._w=p*_*v+g*x*M;break;case"YZX":this._x=g*_*v+p*x*M,this._y=p*x*v+g*_*M,this._z=p*_*M-g*x*v,this._w=p*_*v-g*x*M;break;case"XZY":this._x=g*_*v-p*x*M,this._y=p*x*v-g*_*M,this._z=p*_*M+g*x*v,this._w=p*_*v+g*x*M;break;default:se("Quaternion: .setFromEuler() encountered an unknown order: "+f)}return i===!0&&this._onChangeCallback(),this}setFromAxisAngle(t,i){const r=i/2,l=Math.sin(r);return this._x=t.x*l,this._y=t.y*l,this._z=t.z*l,this._w=Math.cos(r),this._onChangeCallback(),this}setFromRotationMatrix(t){const i=t.elements,r=i[0],l=i[4],c=i[8],f=i[1],h=i[5],m=i[9],p=i[2],_=i[6],v=i[10],g=r+h+v;if(g>0){const x=.5/Math.sqrt(g+1);this._w=.25/x,this._x=(_-m)*x,this._y=(c-p)*x,this._z=(f-l)*x}else if(r>h&&r>v){const x=2*Math.sqrt(1+r-h-v);this._w=(_-m)/x,this._x=.25*x,this._y=(l+f)/x,this._z=(c+p)/x}else if(h>v){const x=2*Math.sqrt(1+h-r-v);this._w=(c-p)/x,this._x=(l+f)/x,this._y=.25*x,this._z=(m+_)/x}else{const x=2*Math.sqrt(1+v-r-h);this._w=(f-l)/x,this._x=(c+p)/x,this._y=(m+_)/x,this._z=.25*x}return this._onChangeCallback(),this}setFromUnitVectors(t,i){let r=t.dot(i)+1;return r<1e-8?(r=0,Math.abs(t.x)>Math.abs(t.z)?(this._x=-t.y,this._y=t.x,this._z=0,this._w=r):(this._x=0,this._y=-t.z,this._z=t.y,this._w=r)):(this._x=t.y*i.z-t.z*i.y,this._y=t.z*i.x-t.x*i.z,this._z=t.x*i.y-t.y*i.x,this._w=r),this.normalize()}angleTo(t){return 2*Math.acos(Math.abs(Ee(this.dot(t),-1,1)))}rotateTowards(t,i){const r=this.angleTo(t);if(r===0)return this;const l=Math.min(1,i/r);return this.slerp(t,l),this}identity(){return this.set(0,0,0,1)}invert(){return this.conjugate()}conjugate(){return this._x*=-1,this._y*=-1,this._z*=-1,this._onChangeCallback(),this}dot(t){return this._x*t._x+this._y*t._y+this._z*t._z+this._w*t._w}lengthSq(){return this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w}length(){return Math.sqrt(this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w)}normalize(){let t=this.length();return t===0?(this._x=0,this._y=0,this._z=0,this._w=1):(t=1/t,this._x=this._x*t,this._y=this._y*t,this._z=this._z*t,this._w=this._w*t),this._onChangeCallback(),this}multiply(t){return this.multiplyQuaternions(this,t)}premultiply(t){return this.multiplyQuaternions(t,this)}multiplyQuaternions(t,i){const r=t._x,l=t._y,c=t._z,f=t._w,h=i._x,m=i._y,p=i._z,_=i._w;return this._x=r*_+f*h+l*p-c*m,this._y=l*_+f*m+c*h-r*p,this._z=c*_+f*p+r*m-l*h,this._w=f*_-r*h-l*m-c*p,this._onChangeCallback(),this}slerp(t,i){let r=t._x,l=t._y,c=t._z,f=t._w,h=this.dot(t);h<0&&(r=-r,l=-l,c=-c,f=-f,h=-h);let m=1-i;if(h<.9995){const p=Math.acos(h),_=Math.sin(p);m=Math.sin(m*p)/_,i=Math.sin(i*p)/_,this._x=this._x*m+r*i,this._y=this._y*m+l*i,this._z=this._z*m+c*i,this._w=this._w*m+f*i,this._onChangeCallback()}else this._x=this._x*m+r*i,this._y=this._y*m+l*i,this._z=this._z*m+c*i,this._w=this._w*m+f*i,this.normalize();return this}slerpQuaternions(t,i,r){return this.copy(t).slerp(i,r)}random(){const t=2*Math.PI*Math.random(),i=2*Math.PI*Math.random(),r=Math.random(),l=Math.sqrt(1-r),c=Math.sqrt(r);return this.set(l*Math.sin(t),l*Math.cos(t),c*Math.sin(i),c*Math.cos(i))}equals(t){return t._x===this._x&&t._y===this._y&&t._z===this._z&&t._w===this._w}fromArray(t,i=0){return this._x=t[i],this._y=t[i+1],this._z=t[i+2],this._w=t[i+3],this._onChangeCallback(),this}toArray(t=[],i=0){return t[i]=this._x,t[i+1]=this._y,t[i+2]=this._z,t[i+3]=this._w,t}fromBufferAttribute(t,i){return this._x=t.getX(i),this._y=t.getY(i),this._z=t.getZ(i),this._w=t.getW(i),this._onChangeCallback(),this}toJSON(){return this.toArray()}_onChange(t){return this._onChangeCallback=t,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._w}}const im=class im{constructor(t=0,i=0,r=0){this.x=t,this.y=i,this.z=r}set(t,i,r){return r===void 0&&(r=this.z),this.x=t,this.y=i,this.z=r,this}setScalar(t){return this.x=t,this.y=t,this.z=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setZ(t){return this.z=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;case 2:this.z=i;break;default:throw new Error("THREE.Vector3: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:throw new Error("THREE.Vector3: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y,this.z)}copy(t){return this.x=t.x,this.y=t.y,this.z=t.z,this}add(t){return this.x+=t.x,this.y+=t.y,this.z+=t.z,this}addScalar(t){return this.x+=t,this.y+=t,this.z+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this.z=t.z+i.z,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this.z+=t.z*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this.z-=t.z,this}subScalar(t){return this.x-=t,this.y-=t,this.z-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this.z=t.z-i.z,this}multiply(t){return this.x*=t.x,this.y*=t.y,this.z*=t.z,this}multiplyScalar(t){return this.x*=t,this.y*=t,this.z*=t,this}multiplyVectors(t,i){return this.x=t.x*i.x,this.y=t.y*i.y,this.z=t.z*i.z,this}applyEuler(t){return this.applyQuaternion(uv.setFromEuler(t))}applyAxisAngle(t,i){return this.applyQuaternion(uv.setFromAxisAngle(t,i))}applyMatrix3(t){const i=this.x,r=this.y,l=this.z,c=t.elements;return this.x=c[0]*i+c[3]*r+c[6]*l,this.y=c[1]*i+c[4]*r+c[7]*l,this.z=c[2]*i+c[5]*r+c[8]*l,this}applyNormalMatrix(t){return this.applyMatrix3(t).normalize()}applyMatrix4(t){const i=this.x,r=this.y,l=this.z,c=t.elements,f=1/(c[3]*i+c[7]*r+c[11]*l+c[15]);return this.x=(c[0]*i+c[4]*r+c[8]*l+c[12])*f,this.y=(c[1]*i+c[5]*r+c[9]*l+c[13])*f,this.z=(c[2]*i+c[6]*r+c[10]*l+c[14])*f,this}applyQuaternion(t){const i=this.x,r=this.y,l=this.z,c=t.x,f=t.y,h=t.z,m=t.w,p=2*(f*l-h*r),_=2*(h*i-c*l),v=2*(c*r-f*i);return this.x=i+m*p+f*v-h*_,this.y=r+m*_+h*p-c*v,this.z=l+m*v+c*_-f*p,this}project(t){return this.applyMatrix4(t.matrixWorldInverse).applyMatrix4(t.projectionMatrix)}unproject(t){return this.applyMatrix4(t.projectionMatrixInverse).applyMatrix4(t.matrixWorld)}transformDirection(t){const i=this.x,r=this.y,l=this.z,c=t.elements;return this.x=c[0]*i+c[4]*r+c[8]*l,this.y=c[1]*i+c[5]*r+c[9]*l,this.z=c[2]*i+c[6]*r+c[10]*l,this.normalize()}divide(t){return this.x/=t.x,this.y/=t.y,this.z/=t.z,this}divideScalar(t){return this.multiplyScalar(1/t)}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this.z=Math.min(this.z,t.z),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this.z=Math.max(this.z,t.z),this}clamp(t,i){return this.x=Ee(this.x,t.x,i.x),this.y=Ee(this.y,t.y,i.y),this.z=Ee(this.z,t.z,i.z),this}clampScalar(t,i){return this.x=Ee(this.x,t,i),this.y=Ee(this.y,t,i),this.z=Ee(this.z,t,i),this}clampLength(t,i){const r=this.length();return this.divideScalar(r||1).multiplyScalar(Ee(r,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this}dot(t){return this.x*t.x+this.y*t.y+this.z*t.z}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)}normalize(){return this.divideScalar(this.length()||1)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this.z+=(t.z-this.z)*i,this}lerpVectors(t,i,r){return this.x=t.x+(i.x-t.x)*r,this.y=t.y+(i.y-t.y)*r,this.z=t.z+(i.z-t.z)*r,this}cross(t){return this.crossVectors(this,t)}crossVectors(t,i){const r=t.x,l=t.y,c=t.z,f=i.x,h=i.y,m=i.z;return this.x=l*m-c*h,this.y=c*f-r*m,this.z=r*h-l*f,this}projectOnVector(t){const i=t.lengthSq();if(i===0)return this.set(0,0,0);const r=t.dot(this)/i;return this.copy(t).multiplyScalar(r)}projectOnPlane(t){return Fd.copy(this).projectOnVector(t),this.sub(Fd)}reflect(t){return this.sub(Fd.copy(t).multiplyScalar(2*this.dot(t)))}angleTo(t){const i=Math.sqrt(this.lengthSq()*t.lengthSq());if(i===0)return Math.PI/2;const r=this.dot(t)/i;return Math.acos(Ee(r,-1,1))}distanceTo(t){return Math.sqrt(this.distanceToSquared(t))}distanceToSquared(t){const i=this.x-t.x,r=this.y-t.y,l=this.z-t.z;return i*i+r*r+l*l}manhattanDistanceTo(t){return Math.abs(this.x-t.x)+Math.abs(this.y-t.y)+Math.abs(this.z-t.z)}setFromSpherical(t){return this.setFromSphericalCoords(t.radius,t.phi,t.theta)}setFromSphericalCoords(t,i,r){const l=Math.sin(i)*t;return this.x=l*Math.sin(r),this.y=Math.cos(i)*t,this.z=l*Math.cos(r),this}setFromCylindrical(t){return this.setFromCylindricalCoords(t.radius,t.theta,t.y)}setFromCylindricalCoords(t,i,r){return this.x=t*Math.sin(i),this.y=r,this.z=t*Math.cos(i),this}setFromMatrixPosition(t){const i=t.elements;return this.x=i[12],this.y=i[13],this.z=i[14],this}setFromMatrixScale(t){const i=this.setFromMatrixColumn(t,0).length(),r=this.setFromMatrixColumn(t,1).length(),l=this.setFromMatrixColumn(t,2).length();return this.x=i,this.y=r,this.z=l,this}setFromMatrixColumn(t,i){return this.fromArray(t.elements,i*4)}setFromMatrix3Column(t,i){return this.fromArray(t.elements,i*3)}setFromEuler(t){return this.x=t._x,this.y=t._y,this.z=t._z,this}setFromColor(t){return this.x=t.r,this.y=t.g,this.z=t.b,this}equals(t){return t.x===this.x&&t.y===this.y&&t.z===this.z}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this.z=t[i+2],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t[i+2]=this.z,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this.z=t.getZ(i),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this}randomDirection(){const t=Math.random()*Math.PI*2,i=Math.random()*2-1,r=Math.sqrt(1-i*i);return this.x=r*Math.cos(t),this.y=i,this.z=r*Math.sin(t),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z}};im.prototype.isVector3=!0;let Q=im;const Fd=new Q,uv=new Kr,am=class am{constructor(t,i,r,l,c,f,h,m,p){this.elements=[1,0,0,0,1,0,0,0,1],t!==void 0&&this.set(t,i,r,l,c,f,h,m,p)}set(t,i,r,l,c,f,h,m,p){const _=this.elements;return _[0]=t,_[1]=l,_[2]=h,_[3]=i,_[4]=c,_[5]=m,_[6]=r,_[7]=f,_[8]=p,this}identity(){return this.set(1,0,0,0,1,0,0,0,1),this}copy(t){const i=this.elements,r=t.elements;return i[0]=r[0],i[1]=r[1],i[2]=r[2],i[3]=r[3],i[4]=r[4],i[5]=r[5],i[6]=r[6],i[7]=r[7],i[8]=r[8],this}extractBasis(t,i,r){return t.setFromMatrix3Column(this,0),i.setFromMatrix3Column(this,1),r.setFromMatrix3Column(this,2),this}setFromMatrix4(t){const i=t.elements;return this.set(i[0],i[4],i[8],i[1],i[5],i[9],i[2],i[6],i[10]),this}multiply(t){return this.multiplyMatrices(this,t)}premultiply(t){return this.multiplyMatrices(t,this)}multiplyMatrices(t,i){const r=t.elements,l=i.elements,c=this.elements,f=r[0],h=r[3],m=r[6],p=r[1],_=r[4],v=r[7],g=r[2],x=r[5],M=r[8],A=l[0],E=l[3],S=l[6],P=l[1],O=l[4],C=l[7],z=l[2],D=l[5],U=l[8];return c[0]=f*A+h*P+m*z,c[3]=f*E+h*O+m*D,c[6]=f*S+h*C+m*U,c[1]=p*A+_*P+v*z,c[4]=p*E+_*O+v*D,c[7]=p*S+_*C+v*U,c[2]=g*A+x*P+M*z,c[5]=g*E+x*O+M*D,c[8]=g*S+x*C+M*U,this}multiplyScalar(t){const i=this.elements;return i[0]*=t,i[3]*=t,i[6]*=t,i[1]*=t,i[4]*=t,i[7]*=t,i[2]*=t,i[5]*=t,i[8]*=t,this}determinant(){const t=this.elements,i=t[0],r=t[1],l=t[2],c=t[3],f=t[4],h=t[5],m=t[6],p=t[7],_=t[8];return i*f*_-i*h*p-r*c*_+r*h*m+l*c*p-l*f*m}invert(){const t=this.elements,i=t[0],r=t[1],l=t[2],c=t[3],f=t[4],h=t[5],m=t[6],p=t[7],_=t[8],v=_*f-h*p,g=h*m-_*c,x=p*c-f*m,M=i*v+r*g+l*x;if(M===0)return this.set(0,0,0,0,0,0,0,0,0);const A=1/M;return t[0]=v*A,t[1]=(l*p-_*r)*A,t[2]=(h*r-l*f)*A,t[3]=g*A,t[4]=(_*i-l*m)*A,t[5]=(l*c-h*i)*A,t[6]=x*A,t[7]=(r*m-p*i)*A,t[8]=(f*i-r*c)*A,this}transpose(){let t;const i=this.elements;return t=i[1],i[1]=i[3],i[3]=t,t=i[2],i[2]=i[6],i[6]=t,t=i[5],i[5]=i[7],i[7]=t,this}getNormalMatrix(t){return this.setFromMatrix4(t).invert().transpose()}transposeIntoArray(t){const i=this.elements;return t[0]=i[0],t[1]=i[3],t[2]=i[6],t[3]=i[1],t[4]=i[4],t[5]=i[7],t[6]=i[2],t[7]=i[5],t[8]=i[8],this}setUvTransform(t,i,r,l,c,f,h){const m=Math.cos(c),p=Math.sin(c);return this.set(r*m,r*p,-r*(m*f+p*h)+f+t,-l*p,l*m,-l*(-p*f+m*h)+h+i,0,0,1),this}scale(t,i){return Ks("Matrix3: .scale() is deprecated. Use .makeScale() instead."),this.premultiply(Hd.makeScale(t,i)),this}rotate(t){return Ks("Matrix3: .rotate() is deprecated. Use .makeRotation() instead."),this.premultiply(Hd.makeRotation(-t)),this}translate(t,i){return Ks("Matrix3: .translate() is deprecated. Use .makeTranslation() instead."),this.premultiply(Hd.makeTranslation(t,i)),this}makeTranslation(t,i){return t.isVector2?this.set(1,0,t.x,0,1,t.y,0,0,1):this.set(1,0,t,0,1,i,0,0,1),this}makeRotation(t){const i=Math.cos(t),r=Math.sin(t);return this.set(i,-r,0,r,i,0,0,0,1),this}makeScale(t,i){return this.set(t,0,0,0,i,0,0,0,1),this}equals(t){const i=this.elements,r=t.elements;for(let l=0;l<9;l++)if(i[l]!==r[l])return!1;return!0}fromArray(t,i=0){for(let r=0;r<9;r++)this.elements[r]=t[r+i];return this}toArray(t=[],i=0){const r=this.elements;return t[i]=r[0],t[i+1]=r[1],t[i+2]=r[2],t[i+3]=r[3],t[i+4]=r[4],t[i+5]=r[5],t[i+6]=r[6],t[i+7]=r[7],t[i+8]=r[8],t}clone(){return new this.constructor().fromArray(this.elements)}};am.prototype.isMatrix3=!0;let ce=am;const Hd=new ce,fv=new ce().set(.4123908,.3575843,.1804808,.212639,.7151687,.0721923,.0193308,.1191948,.9505322),dv=new ce().set(3.2409699,-1.5373832,-.4986108,-.9692436,1.8759675,.0415551,.0556301,-.203977,1.0569715);function zE(){const s={enabled:!0,workingColorSpace:pu,spaces:{},convert:function(l,c,f){return this.enabled===!1||c===f||!c||!f||(this.spaces[c].transfer===Ve&&(l.r=wa(l.r),l.g=wa(l.g),l.b=wa(l.b)),this.spaces[c].primaries!==this.spaces[f].primaries&&(l.applyMatrix3(this.spaces[c].toXYZ),l.applyMatrix3(this.spaces[f].fromXYZ)),this.spaces[f].transfer===Ve&&(l.r=Js(l.r),l.g=Js(l.g),l.b=Js(l.b))),l},workingToColorSpace:function(l,c){return this.convert(l,this.workingColorSpace,c)},colorSpaceToWorking:function(l,c){return this.convert(l,c,this.workingColorSpace)},getPrimaries:function(l){return this.spaces[l].primaries},getTransfer:function(l){return l===dr?mu:this.spaces[l].transfer},getToneMappingMode:function(l){return this.spaces[l].outputColorSpaceConfig.toneMappingMode||"standard"},getLuminanceCoefficients:function(l,c=this.workingColorSpace){return l.fromArray(this.spaces[c].luminanceCoefficients)},define:function(l){Object.assign(this.spaces,l)},_getMatrix:function(l,c,f){return l.copy(this.spaces[c].toXYZ).multiply(this.spaces[f].fromXYZ)},_getDrawingBufferColorSpace:function(l){return this.spaces[l].outputColorSpaceConfig.drawingBufferColorSpace},_getUnpackColorSpace:function(l=this.workingColorSpace){return this.spaces[l].workingColorSpaceConfig.unpackColorSpace},fromWorkingColorSpace:function(l,c){return Ks("ColorManagement: .fromWorkingColorSpace() has been renamed to .workingToColorSpace()."),s.workingToColorSpace(l,c)},toWorkingColorSpace:function(l,c){return Ks("ColorManagement: .toWorkingColorSpace() has been renamed to .colorSpaceToWorking()."),s.colorSpaceToWorking(l,c)}},t=[.64,.33,.3,.6,.15,.06],i=[.2126,.7152,.0722],r=[.3127,.329];return s.define({[pu]:{primaries:t,whitePoint:r,transfer:mu,toXYZ:fv,fromXYZ:dv,luminanceCoefficients:i,workingColorSpaceConfig:{unpackColorSpace:Gn},outputColorSpaceConfig:{drawingBufferColorSpace:Gn}},[Gn]:{primaries:t,whitePoint:r,transfer:Ve,toXYZ:fv,fromXYZ:dv,luminanceCoefficients:i,outputColorSpaceConfig:{drawingBufferColorSpace:Gn}}}),s}const Te=zE();function wa(s){return s<.04045?s*.0773993808:Math.pow(s*.9478672986+.0521327014,2.4)}function Js(s){return s<.0031308?s*12.92:1.055*Math.pow(s,.41666)-.055}let Ls;class FE{static getDataURL(t,i="image/png"){if(/^data:/i.test(t.src)||typeof HTMLCanvasElement>"u")return t.src;let r;if(t instanceof HTMLCanvasElement)r=t;else{Ls===void 0&&(Ls=ml("canvas")),Ls.width=t.width,Ls.height=t.height;const l=Ls.getContext("2d");t instanceof ImageData?l.putImageData(t,0,0):l.drawImage(t,0,0,t.width,t.height),r=Ls}return r.toDataURL(i)}static sRGBToLinear(t){if(typeof HTMLImageElement<"u"&&t instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&t instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&t instanceof ImageBitmap){const i=ml("canvas");i.width=t.width,i.height=t.height;const r=i.getContext("2d");r.drawImage(t,0,0,t.width,t.height);const l=r.getImageData(0,0,t.width,t.height),c=l.data;for(let f=0;f<c.length;f++)c[f]=wa(c[f]/255)*255;return r.putImageData(l,0,0),i}else if(t.data){const i=t.data.slice(0);for(let r=0;r<i.length;r++)i instanceof Uint8Array||i instanceof Uint8ClampedArray?i[r]=Math.floor(wa(i[r]/255)*255):i[r]=wa(i[r]);return{data:i,width:t.width,height:t.height}}else return se("ImageUtils.sRGBToLinear(): Unsupported image type. No color space conversion applied."),t}}let HE=0;class Wp{constructor(t=null){this.isSource=!0,Object.defineProperty(this,"id",{value:HE++}),this.uuid=ao(),this.data=t,this.dataReady=!0,this.version=0}getSize(t){const i=this.data;return typeof HTMLVideoElement<"u"&&i instanceof HTMLVideoElement?t.set(i.videoWidth,i.videoHeight,0):typeof VideoFrame<"u"&&i instanceof VideoFrame?t.set(i.displayWidth,i.displayHeight,0):i!==null?t.set(i.width,i.height,i.depth||0):t.set(0,0,0),t}set needsUpdate(t){t===!0&&this.version++}toJSON(t){const i=t===void 0||typeof t=="string";if(!i&&t.images[this.uuid]!==void 0)return t.images[this.uuid];const r={uuid:this.uuid,url:""},l=this.data;if(l!==null){let c;if(Array.isArray(l)){c=[];for(let f=0,h=l.length;f<h;f++)l[f].isDataTexture?c.push(Gd(l[f].image)):c.push(Gd(l[f]))}else c=Gd(l);r.url=c}return i||(t.images[this.uuid]=r),r}}function Gd(s){return typeof HTMLImageElement<"u"&&s instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&s instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&s instanceof ImageBitmap?FE.getDataURL(s):s.data?{data:Array.from(s.data),width:s.width,height:s.height,type:s.data.constructor.name}:(se("Texture: Unable to serialize Texture."),{})}let GE=0;const Vd=new Q;class Bn extends jr{constructor(t=Bn.DEFAULT_IMAGE,i=Bn.DEFAULT_MAPPING,r=Aa,l=Aa,c=wn,f=Xr,h=Pi,m=pi,p=Bn.DEFAULT_ANISOTROPY,_=dr){super(),this.isTexture=!0,Object.defineProperty(this,"id",{value:GE++}),this.uuid=ao(),this.name="",this.source=new Wp(t),this.mipmaps=[],this.mapping=i,this.channel=0,this.wrapS=r,this.wrapT=l,this.magFilter=c,this.minFilter=f,this.anisotropy=p,this.format=h,this.internalFormat=null,this.type=m,this.offset=new Ae(0,0),this.repeat=new Ae(1,1),this.center=new Ae(0,0),this.rotation=0,this.matrixAutoUpdate=!0,this.matrix=new ce,this.generateMipmaps=!0,this.premultiplyAlpha=!1,this.flipY=!0,this.unpackAlignment=4,this.colorSpace=_,this.userData={},this.updateRanges=[],this.version=0,this.onUpdate=null,this.renderTarget=null,this.isRenderTargetTexture=!1,this.isArrayTexture=!!(t&&t.depth&&t.depth>1),this.pmremVersion=0,this.normalized=!1}get width(){return this.source.getSize(Vd).x}get height(){return this.source.getSize(Vd).y}get depth(){return this.source.getSize(Vd).z}get image(){return this.source.data}set image(t){this.source.data=t}updateMatrix(){this.matrix.setUvTransform(this.offset.x,this.offset.y,this.repeat.x,this.repeat.y,this.rotation,this.center.x,this.center.y)}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}clone(){return new this.constructor().copy(this)}copy(t){return this.name=t.name,this.source=t.source,this.mipmaps=t.mipmaps.slice(0),this.mapping=t.mapping,this.channel=t.channel,this.wrapS=t.wrapS,this.wrapT=t.wrapT,this.magFilter=t.magFilter,this.minFilter=t.minFilter,this.anisotropy=t.anisotropy,this.format=t.format,this.internalFormat=t.internalFormat,this.type=t.type,this.normalized=t.normalized,this.offset.copy(t.offset),this.repeat.copy(t.repeat),this.center.copy(t.center),this.rotation=t.rotation,this.matrixAutoUpdate=t.matrixAutoUpdate,this.matrix.copy(t.matrix),this.generateMipmaps=t.generateMipmaps,this.premultiplyAlpha=t.premultiplyAlpha,this.flipY=t.flipY,this.unpackAlignment=t.unpackAlignment,this.colorSpace=t.colorSpace,this.renderTarget=t.renderTarget,this.isRenderTargetTexture=t.isRenderTargetTexture,this.isArrayTexture=t.isArrayTexture,this.userData=JSON.parse(JSON.stringify(t.userData)),this.needsUpdate=!0,this}setValues(t){for(const i in t){const r=t[i];if(r===void 0){se(`Texture.setValues(): parameter '${i}' has value of undefined.`);continue}const l=this[i];if(l===void 0){se(`Texture.setValues(): property '${i}' does not exist.`);continue}l&&r&&l.isVector2&&r.isVector2||l&&r&&l.isVector3&&r.isVector3||l&&r&&l.isMatrix3&&r.isMatrix3?l.copy(r):this[i]=r}}toJSON(t){const i=t===void 0||typeof t=="string";if(!i&&t.textures[this.uuid]!==void 0)return t.textures[this.uuid];const r={metadata:{version:4.7,type:"Texture",generator:"Texture.toJSON"},uuid:this.uuid,name:this.name,image:this.source.toJSON(t).uuid,mapping:this.mapping,channel:this.channel,repeat:[this.repeat.x,this.repeat.y],offset:[this.offset.x,this.offset.y],center:[this.center.x,this.center.y],rotation:this.rotation,wrap:[this.wrapS,this.wrapT],format:this.format,internalFormat:this.internalFormat,type:this.type,normalized:this.normalized,colorSpace:this.colorSpace,minFilter:this.minFilter,magFilter:this.magFilter,anisotropy:this.anisotropy,flipY:this.flipY,generateMipmaps:this.generateMipmaps,premultiplyAlpha:this.premultiplyAlpha,unpackAlignment:this.unpackAlignment};return Object.keys(this.userData).length>0&&(r.userData=this.userData),i||(t.textures[this.uuid]=r),r}dispose(){this.dispatchEvent({type:"dispose"})}transformUv(t){if(this.mapping!==$x)return t;if(t.applyMatrix3(this.matrix),t.x<0||t.x>1)switch(this.wrapS){case jh:t.x=t.x-Math.floor(t.x);break;case Aa:t.x=t.x<0?0:1;break;case Kh:Math.abs(Math.floor(t.x)%2)===1?t.x=Math.ceil(t.x)-t.x:t.x=t.x-Math.floor(t.x);break}if(t.y<0||t.y>1)switch(this.wrapT){case jh:t.y=t.y-Math.floor(t.y);break;case Aa:t.y=t.y<0?0:1;break;case Kh:Math.abs(Math.floor(t.y)%2)===1?t.y=Math.ceil(t.y)-t.y:t.y=t.y-Math.floor(t.y);break}return this.flipY&&(t.y=1-t.y),t}set needsUpdate(t){t===!0&&(this.version++,this.source.needsUpdate=!0)}set needsPMREMUpdate(t){t===!0&&this.pmremVersion++}}Bn.DEFAULT_IMAGE=null;Bn.DEFAULT_MAPPING=$x;Bn.DEFAULT_ANISOTROPY=1;const rm=class rm{constructor(t=0,i=0,r=0,l=1){this.x=t,this.y=i,this.z=r,this.w=l}get width(){return this.z}set width(t){this.z=t}get height(){return this.w}set height(t){this.w=t}set(t,i,r,l){return this.x=t,this.y=i,this.z=r,this.w=l,this}setScalar(t){return this.x=t,this.y=t,this.z=t,this.w=t,this}setX(t){return this.x=t,this}setY(t){return this.y=t,this}setZ(t){return this.z=t,this}setW(t){return this.w=t,this}setComponent(t,i){switch(t){case 0:this.x=i;break;case 1:this.y=i;break;case 2:this.z=i;break;case 3:this.w=i;break;default:throw new Error("THREE.Vector4: index is out of range: "+t)}return this}getComponent(t){switch(t){case 0:return this.x;case 1:return this.y;case 2:return this.z;case 3:return this.w;default:throw new Error("THREE.Vector4: index is out of range: "+t)}}clone(){return new this.constructor(this.x,this.y,this.z,this.w)}copy(t){return this.x=t.x,this.y=t.y,this.z=t.z,this.w=t.w!==void 0?t.w:1,this}add(t){return this.x+=t.x,this.y+=t.y,this.z+=t.z,this.w+=t.w,this}addScalar(t){return this.x+=t,this.y+=t,this.z+=t,this.w+=t,this}addVectors(t,i){return this.x=t.x+i.x,this.y=t.y+i.y,this.z=t.z+i.z,this.w=t.w+i.w,this}addScaledVector(t,i){return this.x+=t.x*i,this.y+=t.y*i,this.z+=t.z*i,this.w+=t.w*i,this}sub(t){return this.x-=t.x,this.y-=t.y,this.z-=t.z,this.w-=t.w,this}subScalar(t){return this.x-=t,this.y-=t,this.z-=t,this.w-=t,this}subVectors(t,i){return this.x=t.x-i.x,this.y=t.y-i.y,this.z=t.z-i.z,this.w=t.w-i.w,this}multiply(t){return this.x*=t.x,this.y*=t.y,this.z*=t.z,this.w*=t.w,this}multiplyScalar(t){return this.x*=t,this.y*=t,this.z*=t,this.w*=t,this}applyMatrix4(t){const i=this.x,r=this.y,l=this.z,c=this.w,f=t.elements;return this.x=f[0]*i+f[4]*r+f[8]*l+f[12]*c,this.y=f[1]*i+f[5]*r+f[9]*l+f[13]*c,this.z=f[2]*i+f[6]*r+f[10]*l+f[14]*c,this.w=f[3]*i+f[7]*r+f[11]*l+f[15]*c,this}divide(t){return this.x/=t.x,this.y/=t.y,this.z/=t.z,this.w/=t.w,this}divideScalar(t){return this.multiplyScalar(1/t)}setAxisAngleFromQuaternion(t){this.w=2*Math.acos(t.w);const i=Math.sqrt(1-t.w*t.w);return i<1e-4?(this.x=1,this.y=0,this.z=0):(this.x=t.x/i,this.y=t.y/i,this.z=t.z/i),this}setAxisAngleFromRotationMatrix(t){let i,r,l,c;const m=t.elements,p=m[0],_=m[4],v=m[8],g=m[1],x=m[5],M=m[9],A=m[2],E=m[6],S=m[10];if(Math.abs(_-g)<.01&&Math.abs(v-A)<.01&&Math.abs(M-E)<.01){if(Math.abs(_+g)<.1&&Math.abs(v+A)<.1&&Math.abs(M+E)<.1&&Math.abs(p+x+S-3)<.1)return this.set(1,0,0,0),this;i=Math.PI;const O=(p+1)/2,C=(x+1)/2,z=(S+1)/2,D=(_+g)/4,U=(v+A)/4,b=(M+E)/4;return O>C&&O>z?O<.01?(r=0,l=.707106781,c=.707106781):(r=Math.sqrt(O),l=D/r,c=U/r):C>z?C<.01?(r=.707106781,l=0,c=.707106781):(l=Math.sqrt(C),r=D/l,c=b/l):z<.01?(r=.707106781,l=.707106781,c=0):(c=Math.sqrt(z),r=U/c,l=b/c),this.set(r,l,c,i),this}let P=Math.sqrt((E-M)*(E-M)+(v-A)*(v-A)+(g-_)*(g-_));return Math.abs(P)<.001&&(P=1),this.x=(E-M)/P,this.y=(v-A)/P,this.z=(g-_)/P,this.w=Math.acos((p+x+S-1)/2),this}setFromMatrixPosition(t){const i=t.elements;return this.x=i[12],this.y=i[13],this.z=i[14],this.w=i[15],this}min(t){return this.x=Math.min(this.x,t.x),this.y=Math.min(this.y,t.y),this.z=Math.min(this.z,t.z),this.w=Math.min(this.w,t.w),this}max(t){return this.x=Math.max(this.x,t.x),this.y=Math.max(this.y,t.y),this.z=Math.max(this.z,t.z),this.w=Math.max(this.w,t.w),this}clamp(t,i){return this.x=Ee(this.x,t.x,i.x),this.y=Ee(this.y,t.y,i.y),this.z=Ee(this.z,t.z,i.z),this.w=Ee(this.w,t.w,i.w),this}clampScalar(t,i){return this.x=Ee(this.x,t,i),this.y=Ee(this.y,t,i),this.z=Ee(this.z,t,i),this.w=Ee(this.w,t,i),this}clampLength(t,i){const r=this.length();return this.divideScalar(r||1).multiplyScalar(Ee(r,t,i))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this.w=Math.floor(this.w),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this.w=Math.ceil(this.w),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this.w=Math.round(this.w),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this.w=Math.trunc(this.w),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this.w=-this.w,this}dot(t){return this.x*t.x+this.y*t.y+this.z*t.z+this.w*t.w}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)+Math.abs(this.w)}normalize(){return this.divideScalar(this.length()||1)}setLength(t){return this.normalize().multiplyScalar(t)}lerp(t,i){return this.x+=(t.x-this.x)*i,this.y+=(t.y-this.y)*i,this.z+=(t.z-this.z)*i,this.w+=(t.w-this.w)*i,this}lerpVectors(t,i,r){return this.x=t.x+(i.x-t.x)*r,this.y=t.y+(i.y-t.y)*r,this.z=t.z+(i.z-t.z)*r,this.w=t.w+(i.w-t.w)*r,this}equals(t){return t.x===this.x&&t.y===this.y&&t.z===this.z&&t.w===this.w}fromArray(t,i=0){return this.x=t[i],this.y=t[i+1],this.z=t[i+2],this.w=t[i+3],this}toArray(t=[],i=0){return t[i]=this.x,t[i+1]=this.y,t[i+2]=this.z,t[i+3]=this.w,t}fromBufferAttribute(t,i){return this.x=t.getX(i),this.y=t.getY(i),this.z=t.getZ(i),this.w=t.getW(i),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this.w=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z,yield this.w}};rm.prototype.isVector4=!0;let sn=rm;class VE extends jr{constructor(t=1,i=1,r={}){super(),r=Object.assign({generateMipmaps:!1,internalFormat:null,minFilter:wn,depthBuffer:!0,stencilBuffer:!1,resolveDepthBuffer:!0,resolveStencilBuffer:!0,depthTexture:null,samples:0,count:1,depth:1,multiview:!1,useArrayDepthTexture:!1},r),this.isRenderTarget=!0,this.width=t,this.height=i,this.depth=r.depth,this.scissor=new sn(0,0,t,i),this.scissorTest=!1,this.viewport=new sn(0,0,t,i),this.textures=[];const l={width:t,height:i,depth:r.depth},c=new Bn(l),f=r.count;for(let h=0;h<f;h++)this.textures[h]=c.clone(),this.textures[h].isRenderTargetTexture=!0,this.textures[h].renderTarget=this;this._setTextureOptions(r),this.depthBuffer=r.depthBuffer,this.stencilBuffer=r.stencilBuffer,this.resolveDepthBuffer=r.resolveDepthBuffer,this.resolveStencilBuffer=r.resolveStencilBuffer,this._depthTexture=null,this.depthTexture=r.depthTexture,this.samples=r.samples,this.multiview=r.multiview,this.useArrayDepthTexture=r.useArrayDepthTexture}_setTextureOptions(t={}){const i={minFilter:wn,generateMipmaps:!1,flipY:!1,internalFormat:null};t.mapping!==void 0&&(i.mapping=t.mapping),t.wrapS!==void 0&&(i.wrapS=t.wrapS),t.wrapT!==void 0&&(i.wrapT=t.wrapT),t.wrapR!==void 0&&(i.wrapR=t.wrapR),t.magFilter!==void 0&&(i.magFilter=t.magFilter),t.minFilter!==void 0&&(i.minFilter=t.minFilter),t.format!==void 0&&(i.format=t.format),t.type!==void 0&&(i.type=t.type),t.anisotropy!==void 0&&(i.anisotropy=t.anisotropy),t.colorSpace!==void 0&&(i.colorSpace=t.colorSpace),t.flipY!==void 0&&(i.flipY=t.flipY),t.generateMipmaps!==void 0&&(i.generateMipmaps=t.generateMipmaps),t.internalFormat!==void 0&&(i.internalFormat=t.internalFormat);for(let r=0;r<this.textures.length;r++)this.textures[r].setValues(i)}get texture(){return this.textures[0]}set texture(t){this.textures[0]=t}set depthTexture(t){this._depthTexture!==null&&(this._depthTexture.renderTarget=null),t!==null&&(t.renderTarget=this),this._depthTexture=t}get depthTexture(){return this._depthTexture}setSize(t,i,r=1){if(this.width!==t||this.height!==i||this.depth!==r){this.width=t,this.height=i,this.depth=r;for(let l=0,c=this.textures.length;l<c;l++)this.textures[l].image.width=t,this.textures[l].image.height=i,this.textures[l].image.depth=r,this.textures[l].isData3DTexture!==!0&&(this.textures[l].isArrayTexture=this.textures[l].image.depth>1);this.dispose()}this.viewport.set(0,0,t,i),this.scissor.set(0,0,t,i)}clone(){return new this.constructor().copy(this)}copy(t){this.width=t.width,this.height=t.height,this.depth=t.depth,this.scissor.copy(t.scissor),this.scissorTest=t.scissorTest,this.viewport.copy(t.viewport),this.textures.length=0;for(let i=0,r=t.textures.length;i<r;i++){this.textures[i]=t.textures[i].clone(),this.textures[i].isRenderTargetTexture=!0,this.textures[i].renderTarget=this;const l=Object.assign({},t.textures[i].image);this.textures[i].source=new Wp(l)}return this.depthBuffer=t.depthBuffer,this.stencilBuffer=t.stencilBuffer,this.resolveDepthBuffer=t.resolveDepthBuffer,this.resolveStencilBuffer=t.resolveStencilBuffer,t.depthTexture!==null&&(this.depthTexture=t.depthTexture.clone()),this.samples=t.samples,this.multiview=t.multiview,this.useArrayDepthTexture=t.useArrayDepthTexture,this}dispose(){this.dispatchEvent({type:"dispose"})}}class Qi extends VE{constructor(t=1,i=1,r={}){super(t,i,r),this.isWebGLRenderTarget=!0}}class lS extends Bn{constructor(t=null,i=1,r=1,l=1){super(null),this.isDataArrayTexture=!0,this.image={data:t,width:i,height:r,depth:l},this.magFilter=Pn,this.minFilter=Pn,this.wrapR=Aa,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1,this.layerUpdates=new Set}addLayerUpdate(t){this.layerUpdates.add(t)}clearLayerUpdates(){this.layerUpdates.clear()}}class kE extends Bn{constructor(t=null,i=1,r=1,l=1){super(null),this.isData3DTexture=!0,this.image={data:t,width:i,height:r,depth:l},this.magFilter=Pn,this.minFilter=Pn,this.wrapR=Aa,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}const xu=class xu{constructor(t,i,r,l,c,f,h,m,p,_,v,g,x,M,A,E){this.elements=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],t!==void 0&&this.set(t,i,r,l,c,f,h,m,p,_,v,g,x,M,A,E)}set(t,i,r,l,c,f,h,m,p,_,v,g,x,M,A,E){const S=this.elements;return S[0]=t,S[4]=i,S[8]=r,S[12]=l,S[1]=c,S[5]=f,S[9]=h,S[13]=m,S[2]=p,S[6]=_,S[10]=v,S[14]=g,S[3]=x,S[7]=M,S[11]=A,S[15]=E,this}identity(){return this.set(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1),this}clone(){return new xu().fromArray(this.elements)}copy(t){const i=this.elements,r=t.elements;return i[0]=r[0],i[1]=r[1],i[2]=r[2],i[3]=r[3],i[4]=r[4],i[5]=r[5],i[6]=r[6],i[7]=r[7],i[8]=r[8],i[9]=r[9],i[10]=r[10],i[11]=r[11],i[12]=r[12],i[13]=r[13],i[14]=r[14],i[15]=r[15],this}copyPosition(t){const i=this.elements,r=t.elements;return i[12]=r[12],i[13]=r[13],i[14]=r[14],this}setFromMatrix3(t){const i=t.elements;return this.set(i[0],i[3],i[6],0,i[1],i[4],i[7],0,i[2],i[5],i[8],0,0,0,0,1),this}extractBasis(t,i,r){return this.determinantAffine()===0?(t.set(1,0,0),i.set(0,1,0),r.set(0,0,1),this):(t.setFromMatrixColumn(this,0),i.setFromMatrixColumn(this,1),r.setFromMatrixColumn(this,2),this)}makeBasis(t,i,r){return this.set(t.x,i.x,r.x,0,t.y,i.y,r.y,0,t.z,i.z,r.z,0,0,0,0,1),this}extractRotation(t){if(t.determinantAffine()===0)return this.identity();const i=this.elements,r=t.elements,l=1/Os.setFromMatrixColumn(t,0).length(),c=1/Os.setFromMatrixColumn(t,1).length(),f=1/Os.setFromMatrixColumn(t,2).length();return i[0]=r[0]*l,i[1]=r[1]*l,i[2]=r[2]*l,i[3]=0,i[4]=r[4]*c,i[5]=r[5]*c,i[6]=r[6]*c,i[7]=0,i[8]=r[8]*f,i[9]=r[9]*f,i[10]=r[10]*f,i[11]=0,i[12]=0,i[13]=0,i[14]=0,i[15]=1,this}makeRotationFromEuler(t){const i=this.elements,r=t.x,l=t.y,c=t.z,f=Math.cos(r),h=Math.sin(r),m=Math.cos(l),p=Math.sin(l),_=Math.cos(c),v=Math.sin(c);if(t.order==="XYZ"){const g=f*_,x=f*v,M=h*_,A=h*v;i[0]=m*_,i[4]=-m*v,i[8]=p,i[1]=x+M*p,i[5]=g-A*p,i[9]=-h*m,i[2]=A-g*p,i[6]=M+x*p,i[10]=f*m}else if(t.order==="YXZ"){const g=m*_,x=m*v,M=p*_,A=p*v;i[0]=g+A*h,i[4]=M*h-x,i[8]=f*p,i[1]=f*v,i[5]=f*_,i[9]=-h,i[2]=x*h-M,i[6]=A+g*h,i[10]=f*m}else if(t.order==="ZXY"){const g=m*_,x=m*v,M=p*_,A=p*v;i[0]=g-A*h,i[4]=-f*v,i[8]=M+x*h,i[1]=x+M*h,i[5]=f*_,i[9]=A-g*h,i[2]=-f*p,i[6]=h,i[10]=f*m}else if(t.order==="ZYX"){const g=f*_,x=f*v,M=h*_,A=h*v;i[0]=m*_,i[4]=M*p-x,i[8]=g*p+A,i[1]=m*v,i[5]=A*p+g,i[9]=x*p-M,i[2]=-p,i[6]=h*m,i[10]=f*m}else if(t.order==="YZX"){const g=f*m,x=f*p,M=h*m,A=h*p;i[0]=m*_,i[4]=A-g*v,i[8]=M*v+x,i[1]=v,i[5]=f*_,i[9]=-h*_,i[2]=-p*_,i[6]=x*v+M,i[10]=g-A*v}else if(t.order==="XZY"){const g=f*m,x=f*p,M=h*m,A=h*p;i[0]=m*_,i[4]=-v,i[8]=p*_,i[1]=g*v+A,i[5]=f*_,i[9]=x*v-M,i[2]=M*v-x,i[6]=h*_,i[10]=A*v+g}return i[3]=0,i[7]=0,i[11]=0,i[12]=0,i[13]=0,i[14]=0,i[15]=1,this}makeRotationFromQuaternion(t){return this.compose(XE,t,qE)}lookAt(t,i,r){const l=this.elements;return fi.subVectors(t,i),fi.lengthSq()===0&&(fi.z=1),fi.normalize(),rr.crossVectors(r,fi),rr.lengthSq()===0&&(Math.abs(r.z)===1?fi.x+=1e-4:fi.z+=1e-4,fi.normalize(),rr.crossVectors(r,fi)),rr.normalize(),Nc.crossVectors(fi,rr),l[0]=rr.x,l[4]=Nc.x,l[8]=fi.x,l[1]=rr.y,l[5]=Nc.y,l[9]=fi.y,l[2]=rr.z,l[6]=Nc.z,l[10]=fi.z,this}multiply(t){return this.multiplyMatrices(this,t)}premultiply(t){return this.multiplyMatrices(t,this)}multiplyMatrices(t,i){const r=t.elements,l=i.elements,c=this.elements,f=r[0],h=r[4],m=r[8],p=r[12],_=r[1],v=r[5],g=r[9],x=r[13],M=r[2],A=r[6],E=r[10],S=r[14],P=r[3],O=r[7],C=r[11],z=r[15],D=l[0],U=l[4],b=l[8],N=l[12],G=l[1],F=l[5],k=l[9],K=l[13],tt=l[2],$=l[6],I=l[10],X=l[14],at=l[3],_t=l[7],vt=l[11],L=l[15];return c[0]=f*D+h*G+m*tt+p*at,c[4]=f*U+h*F+m*$+p*_t,c[8]=f*b+h*k+m*I+p*vt,c[12]=f*N+h*K+m*X+p*L,c[1]=_*D+v*G+g*tt+x*at,c[5]=_*U+v*F+g*$+x*_t,c[9]=_*b+v*k+g*I+x*vt,c[13]=_*N+v*K+g*X+x*L,c[2]=M*D+A*G+E*tt+S*at,c[6]=M*U+A*F+E*$+S*_t,c[10]=M*b+A*k+E*I+S*vt,c[14]=M*N+A*K+E*X+S*L,c[3]=P*D+O*G+C*tt+z*at,c[7]=P*U+O*F+C*$+z*_t,c[11]=P*b+O*k+C*I+z*vt,c[15]=P*N+O*K+C*X+z*L,this}multiplyScalar(t){const i=this.elements;return i[0]*=t,i[4]*=t,i[8]*=t,i[12]*=t,i[1]*=t,i[5]*=t,i[9]*=t,i[13]*=t,i[2]*=t,i[6]*=t,i[10]*=t,i[14]*=t,i[3]*=t,i[7]*=t,i[11]*=t,i[15]*=t,this}determinant(){const t=this.elements,i=t[0],r=t[4],l=t[8],c=t[12],f=t[1],h=t[5],m=t[9],p=t[13],_=t[2],v=t[6],g=t[10],x=t[14],M=t[3],A=t[7],E=t[11],S=t[15],P=m*x-p*g,O=h*x-p*v,C=h*g-m*v,z=f*x-p*_,D=f*g-m*_,U=f*v-h*_;return i*(A*P-E*O+S*C)-r*(M*P-E*z+S*D)+l*(M*O-A*z+S*U)-c*(M*C-A*D+E*U)}determinantAffine(){const t=this.elements,i=t[0],r=t[4],l=t[8],c=t[1],f=t[5],h=t[9],m=t[2],p=t[6],_=t[10];return i*(f*_-h*p)-r*(c*_-h*m)+l*(c*p-f*m)}transpose(){const t=this.elements;let i;return i=t[1],t[1]=t[4],t[4]=i,i=t[2],t[2]=t[8],t[8]=i,i=t[6],t[6]=t[9],t[9]=i,i=t[3],t[3]=t[12],t[12]=i,i=t[7],t[7]=t[13],t[13]=i,i=t[11],t[11]=t[14],t[14]=i,this}setPosition(t,i,r){const l=this.elements;return t.isVector3?(l[12]=t.x,l[13]=t.y,l[14]=t.z):(l[12]=t,l[13]=i,l[14]=r),this}invert(){const t=this.elements,i=t[0],r=t[1],l=t[2],c=t[3],f=t[4],h=t[5],m=t[6],p=t[7],_=t[8],v=t[9],g=t[10],x=t[11],M=t[12],A=t[13],E=t[14],S=t[15],P=i*h-r*f,O=i*m-l*f,C=i*p-c*f,z=r*m-l*h,D=r*p-c*h,U=l*p-c*m,b=_*A-v*M,N=_*E-g*M,G=_*S-x*M,F=v*E-g*A,k=v*S-x*A,K=g*S-x*E,tt=P*K-O*k+C*F+z*G-D*N+U*b;if(tt===0)return this.set(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);const $=1/tt;return t[0]=(h*K-m*k+p*F)*$,t[1]=(l*k-r*K-c*F)*$,t[2]=(A*U-E*D+S*z)*$,t[3]=(g*D-v*U-x*z)*$,t[4]=(m*G-f*K-p*N)*$,t[5]=(i*K-l*G+c*N)*$,t[6]=(E*C-M*U-S*O)*$,t[7]=(_*U-g*C+x*O)*$,t[8]=(f*k-h*G+p*b)*$,t[9]=(r*G-i*k-c*b)*$,t[10]=(M*D-A*C+S*P)*$,t[11]=(v*C-_*D-x*P)*$,t[12]=(h*N-f*F-m*b)*$,t[13]=(i*F-r*N+l*b)*$,t[14]=(A*O-M*z-E*P)*$,t[15]=(_*z-v*O+g*P)*$,this}scale(t){const i=this.elements,r=t.x,l=t.y,c=t.z;return i[0]*=r,i[4]*=l,i[8]*=c,i[1]*=r,i[5]*=l,i[9]*=c,i[2]*=r,i[6]*=l,i[10]*=c,i[3]*=r,i[7]*=l,i[11]*=c,this}getMaxScaleOnAxis(){const t=this.elements,i=t[0]*t[0]+t[1]*t[1]+t[2]*t[2],r=t[4]*t[4]+t[5]*t[5]+t[6]*t[6],l=t[8]*t[8]+t[9]*t[9]+t[10]*t[10];return Math.sqrt(Math.max(i,r,l))}makeTranslation(t,i,r){return t.isVector3?this.set(1,0,0,t.x,0,1,0,t.y,0,0,1,t.z,0,0,0,1):this.set(1,0,0,t,0,1,0,i,0,0,1,r,0,0,0,1),this}makeRotationX(t){const i=Math.cos(t),r=Math.sin(t);return this.set(1,0,0,0,0,i,-r,0,0,r,i,0,0,0,0,1),this}makeRotationY(t){const i=Math.cos(t),r=Math.sin(t);return this.set(i,0,r,0,0,1,0,0,-r,0,i,0,0,0,0,1),this}makeRotationZ(t){const i=Math.cos(t),r=Math.sin(t);return this.set(i,-r,0,0,r,i,0,0,0,0,1,0,0,0,0,1),this}makeRotationAxis(t,i){const r=Math.cos(i),l=Math.sin(i),c=1-r,f=t.x,h=t.y,m=t.z,p=c*f,_=c*h;return this.set(p*f+r,p*h-l*m,p*m+l*h,0,p*h+l*m,_*h+r,_*m-l*f,0,p*m-l*h,_*m+l*f,c*m*m+r,0,0,0,0,1),this}makeScale(t,i,r){return this.set(t,0,0,0,0,i,0,0,0,0,r,0,0,0,0,1),this}makeShear(t,i,r,l,c,f){return this.set(1,r,c,0,t,1,f,0,i,l,1,0,0,0,0,1),this}compose(t,i,r){const l=this.elements,c=i._x,f=i._y,h=i._z,m=i._w,p=c+c,_=f+f,v=h+h,g=c*p,x=c*_,M=c*v,A=f*_,E=f*v,S=h*v,P=m*p,O=m*_,C=m*v,z=r.x,D=r.y,U=r.z;return l[0]=(1-(A+S))*z,l[1]=(x+C)*z,l[2]=(M-O)*z,l[3]=0,l[4]=(x-C)*D,l[5]=(1-(g+S))*D,l[6]=(E+P)*D,l[7]=0,l[8]=(M+O)*U,l[9]=(E-P)*U,l[10]=(1-(g+A))*U,l[11]=0,l[12]=t.x,l[13]=t.y,l[14]=t.z,l[15]=1,this}decompose(t,i,r){const l=this.elements;t.x=l[12],t.y=l[13],t.z=l[14];const c=this.determinantAffine();if(c===0)return r.set(1,1,1),i.identity(),this;let f=Os.set(l[0],l[1],l[2]).length();const h=Os.set(l[4],l[5],l[6]).length(),m=Os.set(l[8],l[9],l[10]).length();c<0&&(f=-f),Ni.copy(this);const p=1/f,_=1/h,v=1/m;return Ni.elements[0]*=p,Ni.elements[1]*=p,Ni.elements[2]*=p,Ni.elements[4]*=_,Ni.elements[5]*=_,Ni.elements[6]*=_,Ni.elements[8]*=v,Ni.elements[9]*=v,Ni.elements[10]*=v,i.setFromRotationMatrix(Ni),r.x=f,r.y=h,r.z=m,this}makePerspective(t,i,r,l,c,f,h=ji,m=!1){const p=this.elements,_=2*c/(i-t),v=2*c/(r-l),g=(i+t)/(i-t),x=(r+l)/(r-l);let M,A;if(m)M=c/(f-c),A=f*c/(f-c);else if(h===ji)M=-(f+c)/(f-c),A=-2*f*c/(f-c);else if(h===pl)M=-f/(f-c),A=-f*c/(f-c);else throw new Error("THREE.Matrix4.makePerspective(): Invalid coordinate system: "+h);return p[0]=_,p[4]=0,p[8]=g,p[12]=0,p[1]=0,p[5]=v,p[9]=x,p[13]=0,p[2]=0,p[6]=0,p[10]=M,p[14]=A,p[3]=0,p[7]=0,p[11]=-1,p[15]=0,this}makeOrthographic(t,i,r,l,c,f,h=ji,m=!1){const p=this.elements,_=2/(i-t),v=2/(r-l),g=-(i+t)/(i-t),x=-(r+l)/(r-l);let M,A;if(m)M=1/(f-c),A=f/(f-c);else if(h===ji)M=-2/(f-c),A=-(f+c)/(f-c);else if(h===pl)M=-1/(f-c),A=-c/(f-c);else throw new Error("THREE.Matrix4.makeOrthographic(): Invalid coordinate system: "+h);return p[0]=_,p[4]=0,p[8]=0,p[12]=g,p[1]=0,p[5]=v,p[9]=0,p[13]=x,p[2]=0,p[6]=0,p[10]=M,p[14]=A,p[3]=0,p[7]=0,p[11]=0,p[15]=1,this}equals(t){const i=this.elements,r=t.elements;for(let l=0;l<16;l++)if(i[l]!==r[l])return!1;return!0}fromArray(t,i=0){for(let r=0;r<16;r++)this.elements[r]=t[r+i];return this}toArray(t=[],i=0){const r=this.elements;return t[i]=r[0],t[i+1]=r[1],t[i+2]=r[2],t[i+3]=r[3],t[i+4]=r[4],t[i+5]=r[5],t[i+6]=r[6],t[i+7]=r[7],t[i+8]=r[8],t[i+9]=r[9],t[i+10]=r[10],t[i+11]=r[11],t[i+12]=r[12],t[i+13]=r[13],t[i+14]=r[14],t[i+15]=r[15],t}};xu.prototype.isMatrix4=!0;let $e=xu;const Os=new Q,Ni=new $e,XE=new Q(0,0,0),qE=new Q(1,1,1),rr=new Q,Nc=new Q,fi=new Q,hv=new $e,pv=new Kr;class mr{constructor(t=0,i=0,r=0,l=mr.DEFAULT_ORDER){this.isEuler=!0,this._x=t,this._y=i,this._z=r,this._order=l}get x(){return this._x}set x(t){this._x=t,this._onChangeCallback()}get y(){return this._y}set y(t){this._y=t,this._onChangeCallback()}get z(){return this._z}set z(t){this._z=t,this._onChangeCallback()}get order(){return this._order}set order(t){this._order=t,this._onChangeCallback()}set(t,i,r,l=this._order){return this._x=t,this._y=i,this._z=r,this._order=l,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._order)}copy(t){return this._x=t._x,this._y=t._y,this._z=t._z,this._order=t._order,this._onChangeCallback(),this}setFromRotationMatrix(t,i=this._order,r=!0){const l=t.elements,c=l[0],f=l[4],h=l[8],m=l[1],p=l[5],_=l[9],v=l[2],g=l[6],x=l[10];switch(i){case"XYZ":this._y=Math.asin(Ee(h,-1,1)),Math.abs(h)<.9999999?(this._x=Math.atan2(-_,x),this._z=Math.atan2(-f,c)):(this._x=Math.atan2(g,p),this._z=0);break;case"YXZ":this._x=Math.asin(-Ee(_,-1,1)),Math.abs(_)<.9999999?(this._y=Math.atan2(h,x),this._z=Math.atan2(m,p)):(this._y=Math.atan2(-v,c),this._z=0);break;case"ZXY":this._x=Math.asin(Ee(g,-1,1)),Math.abs(g)<.9999999?(this._y=Math.atan2(-v,x),this._z=Math.atan2(-f,p)):(this._y=0,this._z=Math.atan2(m,c));break;case"ZYX":this._y=Math.asin(-Ee(v,-1,1)),Math.abs(v)<.9999999?(this._x=Math.atan2(g,x),this._z=Math.atan2(m,c)):(this._x=0,this._z=Math.atan2(-f,p));break;case"YZX":this._z=Math.asin(Ee(m,-1,1)),Math.abs(m)<.9999999?(this._x=Math.atan2(-_,p),this._y=Math.atan2(-v,c)):(this._x=0,this._y=Math.atan2(h,x));break;case"XZY":this._z=Math.asin(-Ee(f,-1,1)),Math.abs(f)<.9999999?(this._x=Math.atan2(g,p),this._y=Math.atan2(h,c)):(this._x=Math.atan2(-_,x),this._y=0);break;default:se("Euler: .setFromRotationMatrix() encountered an unknown order: "+i)}return this._order=i,r===!0&&this._onChangeCallback(),this}setFromQuaternion(t,i,r){return hv.makeRotationFromQuaternion(t),this.setFromRotationMatrix(hv,i,r)}setFromVector3(t,i=this._order){return this.set(t.x,t.y,t.z,i)}reorder(t){return pv.setFromEuler(this),this.setFromQuaternion(pv,t)}equals(t){return t._x===this._x&&t._y===this._y&&t._z===this._z&&t._order===this._order}fromArray(t){return this._x=t[0],this._y=t[1],this._z=t[2],t[3]!==void 0&&(this._order=t[3]),this._onChangeCallback(),this}toArray(t=[],i=0){return t[i]=this._x,t[i+1]=this._y,t[i+2]=this._z,t[i+3]=this._order,t}_onChange(t){return this._onChangeCallback=t,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._order}}mr.DEFAULT_ORDER="XYZ";class Yp{constructor(){this.mask=1}set(t){this.mask=(1<<t|0)>>>0}enable(t){this.mask|=1<<t|0}enableAll(){this.mask=-1}toggle(t){this.mask^=1<<t|0}disable(t){this.mask&=~(1<<t|0)}disableAll(){this.mask=0}test(t){return(this.mask&t.mask)!==0}isEnabled(t){return(this.mask&(1<<t|0))!==0}}let WE=0;const mv=new Q,Ps=new Kr,Sa=new $e,Uc=new Q,$o=new Q,YE=new Q,ZE=new Kr,gv=new Q(1,0,0),_v=new Q(0,1,0),vv=new Q(0,0,1),xv={type:"added"},jE={type:"removed"},Bs={type:"childadded",child:null},kd={type:"childremoved",child:null};class dn extends jr{constructor(){super(),this.isObject3D=!0,Object.defineProperty(this,"id",{value:WE++}),this.uuid=ao(),this.name="",this.type="Object3D",this.parent=null,this.children=[],this.up=dn.DEFAULT_UP.clone();const t=new Q,i=new mr,r=new Kr,l=new Q(1,1,1);function c(){r.setFromEuler(i,!1)}function f(){i.setFromQuaternion(r,void 0,!1)}i._onChange(c),r._onChange(f),Object.defineProperties(this,{position:{configurable:!0,enumerable:!0,value:t},rotation:{configurable:!0,enumerable:!0,value:i},quaternion:{configurable:!0,enumerable:!0,value:r},scale:{configurable:!0,enumerable:!0,value:l},modelViewMatrix:{value:new $e},normalMatrix:{value:new ce}}),this.matrix=new $e,this.matrixWorld=new $e,this.matrixAutoUpdate=dn.DEFAULT_MATRIX_AUTO_UPDATE,this.matrixWorldAutoUpdate=dn.DEFAULT_MATRIX_WORLD_AUTO_UPDATE,this.matrixWorldNeedsUpdate=!1,this.layers=new Yp,this.visible=!0,this.castShadow=!1,this.receiveShadow=!1,this.frustumCulled=!0,this.renderOrder=0,this.animations=[],this.customDepthMaterial=void 0,this.customDistanceMaterial=void 0,this.static=!1,this.userData={},this.pivot=null}onBeforeShadow(){}onAfterShadow(){}onBeforeRender(){}onAfterRender(){}applyMatrix4(t){this.matrixAutoUpdate&&this.updateMatrix(),this.matrix.premultiply(t),this.matrix.decompose(this.position,this.quaternion,this.scale)}applyQuaternion(t){return this.quaternion.premultiply(t),this}setRotationFromAxisAngle(t,i){this.quaternion.setFromAxisAngle(t,i)}setRotationFromEuler(t){this.quaternion.setFromEuler(t,!0)}setRotationFromMatrix(t){this.quaternion.setFromRotationMatrix(t)}setRotationFromQuaternion(t){this.quaternion.copy(t)}rotateOnAxis(t,i){return Ps.setFromAxisAngle(t,i),this.quaternion.multiply(Ps),this}rotateOnWorldAxis(t,i){return Ps.setFromAxisAngle(t,i),this.quaternion.premultiply(Ps),this}rotateX(t){return this.rotateOnAxis(gv,t)}rotateY(t){return this.rotateOnAxis(_v,t)}rotateZ(t){return this.rotateOnAxis(vv,t)}translateOnAxis(t,i){return mv.copy(t).applyQuaternion(this.quaternion),this.position.add(mv.multiplyScalar(i)),this}translateX(t){return this.translateOnAxis(gv,t)}translateY(t){return this.translateOnAxis(_v,t)}translateZ(t){return this.translateOnAxis(vv,t)}localToWorld(t){return this.updateWorldMatrix(!0,!1),t.applyMatrix4(this.matrixWorld)}worldToLocal(t){return this.updateWorldMatrix(!0,!1),t.applyMatrix4(Sa.copy(this.matrixWorld).invert())}lookAt(t,i,r){t.isVector3?Uc.copy(t):Uc.set(t,i,r);const l=this.parent;this.updateWorldMatrix(!0,!1),$o.setFromMatrixPosition(this.matrixWorld),this.isCamera||this.isLight?Sa.lookAt($o,Uc,this.up):Sa.lookAt(Uc,$o,this.up),this.quaternion.setFromRotationMatrix(Sa),l&&(Sa.extractRotation(l.matrixWorld),Ps.setFromRotationMatrix(Sa),this.quaternion.premultiply(Ps.invert()))}add(t){if(arguments.length>1){for(let i=0;i<arguments.length;i++)this.add(arguments[i]);return this}return t===this?(Re("Object3D.add: object can't be added as a child of itself.",t),this):(t&&t.isObject3D?(t.removeFromParent(),t.parent=this,this.children.push(t),t.dispatchEvent(xv),Bs.child=t,this.dispatchEvent(Bs),Bs.child=null):Re("Object3D.add: object not an instance of THREE.Object3D.",t),this)}remove(t){if(arguments.length>1){for(let r=0;r<arguments.length;r++)this.remove(arguments[r]);return this}const i=this.children.indexOf(t);return i!==-1&&(t.parent=null,this.children.splice(i,1),t.dispatchEvent(jE),kd.child=t,this.dispatchEvent(kd),kd.child=null),this}removeFromParent(){const t=this.parent;return t!==null&&t.remove(this),this}clear(){return this.remove(...this.children)}attach(t){return this.updateWorldMatrix(!0,!1),Sa.copy(this.matrixWorld).invert(),t.parent!==null&&(t.parent.updateWorldMatrix(!0,!1),Sa.multiply(t.parent.matrixWorld)),t.applyMatrix4(Sa),t.removeFromParent(),t.parent=this,this.children.push(t),t.updateWorldMatrix(!1,!0),t.dispatchEvent(xv),Bs.child=t,this.dispatchEvent(Bs),Bs.child=null,this}getObjectById(t){return this.getObjectByProperty("id",t)}getObjectByName(t){return this.getObjectByProperty("name",t)}getObjectByProperty(t,i){if(this[t]===i)return this;for(let r=0,l=this.children.length;r<l;r++){const f=this.children[r].getObjectByProperty(t,i);if(f!==void 0)return f}}getObjectsByProperty(t,i,r=[]){this[t]===i&&r.push(this);const l=this.children;for(let c=0,f=l.length;c<f;c++)l[c].getObjectsByProperty(t,i,r);return r}getWorldPosition(t){return this.updateWorldMatrix(!0,!1),t.setFromMatrixPosition(this.matrixWorld)}getWorldQuaternion(t){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose($o,t,YE),t}getWorldScale(t){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose($o,ZE,t),t}getWorldDirection(t){this.updateWorldMatrix(!0,!1);const i=this.matrixWorld.elements;return t.set(i[8],i[9],i[10]).normalize()}raycast(){}traverse(t){t(this);const i=this.children;for(let r=0,l=i.length;r<l;r++)i[r].traverse(t)}traverseVisible(t){if(this.visible===!1)return;t(this);const i=this.children;for(let r=0,l=i.length;r<l;r++)i[r].traverseVisible(t)}traverseAncestors(t){const i=this.parent;i!==null&&(t(i),i.traverseAncestors(t))}updateMatrix(){this.matrix.compose(this.position,this.quaternion,this.scale);const t=this.pivot;if(t!==null){const i=t.x,r=t.y,l=t.z,c=this.matrix.elements;c[12]+=i-c[0]*i-c[4]*r-c[8]*l,c[13]+=r-c[1]*i-c[5]*r-c[9]*l,c[14]+=l-c[2]*i-c[6]*r-c[10]*l}this.matrixWorldNeedsUpdate=!0}updateMatrixWorld(t){this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||t)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,t=!0);const i=this.children;for(let r=0,l=i.length;r<l;r++)i[r].updateMatrixWorld(t)}updateWorldMatrix(t,i,r=!1){const l=this.parent;if(t===!0&&l!==null&&l.updateWorldMatrix(!0,!1),this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||r)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,r=!0),i===!0){const c=this.children;for(let f=0,h=c.length;f<h;f++)c[f].updateWorldMatrix(!1,!0,r)}}toJSON(t){const i=t===void 0||typeof t=="string",r={};i&&(t={geometries:{},materials:{},textures:{},images:{},shapes:{},skeletons:{},animations:{},nodes:{}},r.metadata={version:4.7,type:"Object",generator:"Object3D.toJSON"});const l={};l.uuid=this.uuid,l.type=this.type,this.name!==""&&(l.name=this.name),this.castShadow===!0&&(l.castShadow=!0),this.receiveShadow===!0&&(l.receiveShadow=!0),this.visible===!1&&(l.visible=!1),this.frustumCulled===!1&&(l.frustumCulled=!1),this.renderOrder!==0&&(l.renderOrder=this.renderOrder),this.static!==!1&&(l.static=this.static),Object.keys(this.userData).length>0&&(l.userData=this.userData),l.layers=this.layers.mask,l.matrix=this.matrix.toArray(),l.up=this.up.toArray(),this.pivot!==null&&(l.pivot=this.pivot.toArray()),this.matrixAutoUpdate===!1&&(l.matrixAutoUpdate=!1),this.morphTargetDictionary!==void 0&&(l.morphTargetDictionary=Object.assign({},this.morphTargetDictionary)),this.morphTargetInfluences!==void 0&&(l.morphTargetInfluences=this.morphTargetInfluences.slice()),this.isInstancedMesh&&(l.type="InstancedMesh",l.count=this.count,l.instanceMatrix=this.instanceMatrix.toJSON(),this.instanceColor!==null&&(l.instanceColor=this.instanceColor.toJSON())),this.isBatchedMesh&&(l.type="BatchedMesh",l.perObjectFrustumCulled=this.perObjectFrustumCulled,l.sortObjects=this.sortObjects,l.drawRanges=this._drawRanges,l.reservedRanges=this._reservedRanges,l.geometryInfo=this._geometryInfo.map(h=>({...h,boundingBox:h.boundingBox?h.boundingBox.toJSON():void 0,boundingSphere:h.boundingSphere?h.boundingSphere.toJSON():void 0})),l.instanceInfo=this._instanceInfo.map(h=>({...h})),l.availableInstanceIds=this._availableInstanceIds.slice(),l.availableGeometryIds=this._availableGeometryIds.slice(),l.nextIndexStart=this._nextIndexStart,l.nextVertexStart=this._nextVertexStart,l.geometryCount=this._geometryCount,l.maxInstanceCount=this._maxInstanceCount,l.maxVertexCount=this._maxVertexCount,l.maxIndexCount=this._maxIndexCount,l.geometryInitialized=this._geometryInitialized,l.matricesTexture=this._matricesTexture.toJSON(t),l.indirectTexture=this._indirectTexture.toJSON(t),this._colorsTexture!==null&&(l.colorsTexture=this._colorsTexture.toJSON(t)),this.boundingSphere!==null&&(l.boundingSphere=this.boundingSphere.toJSON()),this.boundingBox!==null&&(l.boundingBox=this.boundingBox.toJSON()));function c(h,m){return h[m.uuid]===void 0&&(h[m.uuid]=m.toJSON(t)),m.uuid}if(this.isScene)this.background&&(this.background.isColor?l.background=this.background.toJSON():this.background.isTexture&&(l.background=this.background.toJSON(t).uuid)),this.environment&&this.environment.isTexture&&this.environment.isRenderTargetTexture!==!0&&(l.environment=this.environment.toJSON(t).uuid);else if(this.isMesh||this.isLine||this.isPoints){l.geometry=c(t.geometries,this.geometry);const h=this.geometry.parameters;if(h!==void 0&&h.shapes!==void 0){const m=h.shapes;if(Array.isArray(m))for(let p=0,_=m.length;p<_;p++){const v=m[p];c(t.shapes,v)}else c(t.shapes,m)}}if(this.isSkinnedMesh&&(l.bindMode=this.bindMode,l.bindMatrix=this.bindMatrix.toArray(),this.skeleton!==void 0&&(c(t.skeletons,this.skeleton),l.skeleton=this.skeleton.uuid)),this.material!==void 0)if(Array.isArray(this.material)){const h=[];for(let m=0,p=this.material.length;m<p;m++)h.push(c(t.materials,this.material[m]));l.material=h}else l.material=c(t.materials,this.material);if(this.children.length>0){l.children=[];for(let h=0;h<this.children.length;h++)l.children.push(this.children[h].toJSON(t).object)}if(this.animations.length>0){l.animations=[];for(let h=0;h<this.animations.length;h++){const m=this.animations[h];l.animations.push(c(t.animations,m))}}if(i){const h=f(t.geometries),m=f(t.materials),p=f(t.textures),_=f(t.images),v=f(t.shapes),g=f(t.skeletons),x=f(t.animations),M=f(t.nodes);h.length>0&&(r.geometries=h),m.length>0&&(r.materials=m),p.length>0&&(r.textures=p),_.length>0&&(r.images=_),v.length>0&&(r.shapes=v),g.length>0&&(r.skeletons=g),x.length>0&&(r.animations=x),M.length>0&&(r.nodes=M)}return r.object=l,r;function f(h){const m=[];for(const p in h){const _=h[p];delete _.metadata,m.push(_)}return m}}clone(t){return new this.constructor().copy(this,t)}copy(t,i=!0){if(this.name=t.name,this.up.copy(t.up),this.position.copy(t.position),this.rotation.order=t.rotation.order,this.quaternion.copy(t.quaternion),this.scale.copy(t.scale),this.pivot=t.pivot!==null?t.pivot.clone():null,this.matrix.copy(t.matrix),this.matrixWorld.copy(t.matrixWorld),this.matrixAutoUpdate=t.matrixAutoUpdate,this.matrixWorldAutoUpdate=t.matrixWorldAutoUpdate,this.matrixWorldNeedsUpdate=t.matrixWorldNeedsUpdate,this.layers.mask=t.layers.mask,this.visible=t.visible,this.castShadow=t.castShadow,this.receiveShadow=t.receiveShadow,this.frustumCulled=t.frustumCulled,this.renderOrder=t.renderOrder,this.static=t.static,this.animations=t.animations.slice(),this.userData=JSON.parse(JSON.stringify(t.userData)),i===!0)for(let r=0;r<t.children.length;r++){const l=t.children[r];this.add(l.clone())}return this}}dn.DEFAULT_UP=new Q(0,1,0);dn.DEFAULT_MATRIX_AUTO_UPDATE=!0;dn.DEFAULT_MATRIX_WORLD_AUTO_UPDATE=!0;class Ra extends dn{constructor(){super(),this.isGroup=!0,this.type="Group"}}const KE={type:"move"};class Xd{constructor(){this._targetRay=null,this._grip=null,this._hand=null}getHandSpace(){return this._hand===null&&(this._hand=new Ra,this._hand.matrixAutoUpdate=!1,this._hand.visible=!1,this._hand.joints={},this._hand.inputState={pinching:!1}),this._hand}getTargetRaySpace(){return this._targetRay===null&&(this._targetRay=new Ra,this._targetRay.matrixAutoUpdate=!1,this._targetRay.visible=!1,this._targetRay.hasLinearVelocity=!1,this._targetRay.linearVelocity=new Q,this._targetRay.hasAngularVelocity=!1,this._targetRay.angularVelocity=new Q),this._targetRay}getGripSpace(){return this._grip===null&&(this._grip=new Ra,this._grip.matrixAutoUpdate=!1,this._grip.visible=!1,this._grip.hasLinearVelocity=!1,this._grip.linearVelocity=new Q,this._grip.hasAngularVelocity=!1,this._grip.angularVelocity=new Q,this._grip.eventsEnabled=!1),this._grip}dispatchEvent(t){return this._targetRay!==null&&this._targetRay.dispatchEvent(t),this._grip!==null&&this._grip.dispatchEvent(t),this._hand!==null&&this._hand.dispatchEvent(t),this}connect(t){if(t&&t.hand){const i=this._hand;if(i)for(const r of t.hand.values())this._getHandJoint(i,r)}return this.dispatchEvent({type:"connected",data:t}),this}disconnect(t){return this.dispatchEvent({type:"disconnected",data:t}),this._targetRay!==null&&(this._targetRay.visible=!1),this._grip!==null&&(this._grip.visible=!1),this._hand!==null&&(this._hand.visible=!1),this}update(t,i,r){let l=null,c=null,f=null;const h=this._targetRay,m=this._grip,p=this._hand;if(t&&i.session.visibilityState!=="visible-blurred"){if(p&&t.hand){f=!0;for(const A of t.hand.values()){const E=i.getJointPose(A,r),S=this._getHandJoint(p,A);E!==null&&(S.matrix.fromArray(E.transform.matrix),S.matrix.decompose(S.position,S.rotation,S.scale),S.matrixWorldNeedsUpdate=!0,S.jointRadius=E.radius),S.visible=E!==null}const _=p.joints["index-finger-tip"],v=p.joints["thumb-tip"],g=_.position.distanceTo(v.position),x=.02,M=.005;p.inputState.pinching&&g>x+M?(p.inputState.pinching=!1,this.dispatchEvent({type:"pinchend",handedness:t.handedness,target:this})):!p.inputState.pinching&&g<=x-M&&(p.inputState.pinching=!0,this.dispatchEvent({type:"pinchstart",handedness:t.handedness,target:this}))}else m!==null&&t.gripSpace&&(c=i.getPose(t.gripSpace,r),c!==null&&(m.matrix.fromArray(c.transform.matrix),m.matrix.decompose(m.position,m.rotation,m.scale),m.matrixWorldNeedsUpdate=!0,c.linearVelocity?(m.hasLinearVelocity=!0,m.linearVelocity.copy(c.linearVelocity)):m.hasLinearVelocity=!1,c.angularVelocity?(m.hasAngularVelocity=!0,m.angularVelocity.copy(c.angularVelocity)):m.hasAngularVelocity=!1,m.eventsEnabled&&m.dispatchEvent({type:"gripUpdated",data:t,target:this})));h!==null&&(l=i.getPose(t.targetRaySpace,r),l===null&&c!==null&&(l=c),l!==null&&(h.matrix.fromArray(l.transform.matrix),h.matrix.decompose(h.position,h.rotation,h.scale),h.matrixWorldNeedsUpdate=!0,l.linearVelocity?(h.hasLinearVelocity=!0,h.linearVelocity.copy(l.linearVelocity)):h.hasLinearVelocity=!1,l.angularVelocity?(h.hasAngularVelocity=!0,h.angularVelocity.copy(l.angularVelocity)):h.hasAngularVelocity=!1,this.dispatchEvent(KE)))}return h!==null&&(h.visible=l!==null),m!==null&&(m.visible=c!==null),p!==null&&(p.visible=f!==null),this}_getHandJoint(t,i){if(t.joints[i.jointName]===void 0){const r=new Ra;r.matrixAutoUpdate=!1,r.visible=!1,t.joints[i.jointName]=r,t.add(r)}return t.joints[i.jointName]}}const cS={aliceblue:15792383,antiquewhite:16444375,aqua:65535,aquamarine:8388564,azure:15794175,beige:16119260,bisque:16770244,black:0,blanchedalmond:16772045,blue:255,blueviolet:9055202,brown:10824234,burlywood:14596231,cadetblue:6266528,chartreuse:8388352,chocolate:13789470,coral:16744272,cornflowerblue:6591981,cornsilk:16775388,crimson:14423100,cyan:65535,darkblue:139,darkcyan:35723,darkgoldenrod:12092939,darkgray:11119017,darkgreen:25600,darkgrey:11119017,darkkhaki:12433259,darkmagenta:9109643,darkolivegreen:5597999,darkorange:16747520,darkorchid:10040012,darkred:9109504,darksalmon:15308410,darkseagreen:9419919,darkslateblue:4734347,darkslategray:3100495,darkslategrey:3100495,darkturquoise:52945,darkviolet:9699539,deeppink:16716947,deepskyblue:49151,dimgray:6908265,dimgrey:6908265,dodgerblue:2003199,firebrick:11674146,floralwhite:16775920,forestgreen:2263842,fuchsia:16711935,gainsboro:14474460,ghostwhite:16316671,gold:16766720,goldenrod:14329120,gray:8421504,green:32768,greenyellow:11403055,grey:8421504,honeydew:15794160,hotpink:16738740,indianred:13458524,indigo:4915330,ivory:16777200,khaki:15787660,lavender:15132410,lavenderblush:16773365,lawngreen:8190976,lemonchiffon:16775885,lightblue:11393254,lightcoral:15761536,lightcyan:14745599,lightgoldenrodyellow:16448210,lightgray:13882323,lightgreen:9498256,lightgrey:13882323,lightpink:16758465,lightsalmon:16752762,lightseagreen:2142890,lightskyblue:8900346,lightslategray:7833753,lightslategrey:7833753,lightsteelblue:11584734,lightyellow:16777184,lime:65280,limegreen:3329330,linen:16445670,magenta:16711935,maroon:8388608,mediumaquamarine:6737322,mediumblue:205,mediumorchid:12211667,mediumpurple:9662683,mediumseagreen:3978097,mediumslateblue:8087790,mediumspringgreen:64154,mediumturquoise:4772300,mediumvioletred:13047173,midnightblue:1644912,mintcream:16121850,mistyrose:16770273,moccasin:16770229,navajowhite:16768685,navy:128,oldlace:16643558,olive:8421376,olivedrab:7048739,orange:16753920,orangered:16729344,orchid:14315734,palegoldenrod:15657130,palegreen:10025880,paleturquoise:11529966,palevioletred:14381203,papayawhip:16773077,peachpuff:16767673,peru:13468991,pink:16761035,plum:14524637,powderblue:11591910,purple:8388736,rebeccapurple:6697881,red:16711680,rosybrown:12357519,royalblue:4286945,saddlebrown:9127187,salmon:16416882,sandybrown:16032864,seagreen:3050327,seashell:16774638,sienna:10506797,silver:12632256,skyblue:8900331,slateblue:6970061,slategray:7372944,slategrey:7372944,snow:16775930,springgreen:65407,steelblue:4620980,tan:13808780,teal:32896,thistle:14204888,tomato:16737095,turquoise:4251856,violet:15631086,wheat:16113331,white:16777215,whitesmoke:16119285,yellow:16776960,yellowgreen:10145074},sr={h:0,s:0,l:0},Lc={h:0,s:0,l:0};function qd(s,t,i){return i<0&&(i+=1),i>1&&(i-=1),i<1/6?s+(t-s)*6*i:i<1/2?t:i<2/3?s+(t-s)*6*(2/3-i):s}class me{constructor(t,i,r){return this.isColor=!0,this.r=1,this.g=1,this.b=1,this.set(t,i,r)}set(t,i,r){if(i===void 0&&r===void 0){const l=t;l&&l.isColor?this.copy(l):typeof l=="number"?this.setHex(l):typeof l=="string"&&this.setStyle(l)}else this.setRGB(t,i,r);return this}setScalar(t){return this.r=t,this.g=t,this.b=t,this}setHex(t,i=Gn){return t=Math.floor(t),this.r=(t>>16&255)/255,this.g=(t>>8&255)/255,this.b=(t&255)/255,Te.colorSpaceToWorking(this,i),this}setRGB(t,i,r,l=Te.workingColorSpace){return this.r=t,this.g=i,this.b=r,Te.colorSpaceToWorking(this,l),this}setHSL(t,i,r,l=Te.workingColorSpace){if(t=qp(t,1),i=Ee(i,0,1),r=Ee(r,0,1),i===0)this.r=this.g=this.b=r;else{const c=r<=.5?r*(1+i):r+i-r*i,f=2*r-c;this.r=qd(f,c,t+1/3),this.g=qd(f,c,t),this.b=qd(f,c,t-1/3)}return Te.colorSpaceToWorking(this,l),this}setStyle(t,i=Gn){function r(c){c!==void 0&&parseFloat(c)<1&&se("Color: Alpha component of "+t+" will be ignored.")}let l;if(l=/^(\w+)\(([^\)]*)\)/.exec(t)){let c;const f=l[1],h=l[2];switch(f){case"rgb":case"rgba":if(c=/^\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(h))return r(c[4]),this.setRGB(Math.min(255,parseInt(c[1],10))/255,Math.min(255,parseInt(c[2],10))/255,Math.min(255,parseInt(c[3],10))/255,i);if(c=/^\s*(\d+)\%\s*,\s*(\d+)\%\s*,\s*(\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(h))return r(c[4]),this.setRGB(Math.min(100,parseInt(c[1],10))/100,Math.min(100,parseInt(c[2],10))/100,Math.min(100,parseInt(c[3],10))/100,i);break;case"hsl":case"hsla":if(c=/^\s*(\d*\.?\d+)\s*,\s*(\d*\.?\d+)\%\s*,\s*(\d*\.?\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(h))return r(c[4]),this.setHSL(parseFloat(c[1])/360,parseFloat(c[2])/100,parseFloat(c[3])/100,i);break;default:se("Color: Unknown color model "+t)}}else if(l=/^\#([A-Fa-f\d]+)$/.exec(t)){const c=l[1],f=c.length;if(f===3)return this.setRGB(parseInt(c.charAt(0),16)/15,parseInt(c.charAt(1),16)/15,parseInt(c.charAt(2),16)/15,i);if(f===6)return this.setHex(parseInt(c,16),i);se("Color: Invalid hex color "+t)}else if(t&&t.length>0)return this.setColorName(t,i);return this}setColorName(t,i=Gn){const r=cS[t.toLowerCase()];return r!==void 0?this.setHex(r,i):se("Color: Unknown color "+t),this}clone(){return new this.constructor(this.r,this.g,this.b)}copy(t){return this.r=t.r,this.g=t.g,this.b=t.b,this}copySRGBToLinear(t){return this.r=wa(t.r),this.g=wa(t.g),this.b=wa(t.b),this}copyLinearToSRGB(t){return this.r=Js(t.r),this.g=Js(t.g),this.b=Js(t.b),this}convertSRGBToLinear(){return this.copySRGBToLinear(this),this}convertLinearToSRGB(){return this.copyLinearToSRGB(this),this}getHex(t=Gn){return Te.workingToColorSpace(Hn.copy(this),t),Math.round(Ee(Hn.r*255,0,255))*65536+Math.round(Ee(Hn.g*255,0,255))*256+Math.round(Ee(Hn.b*255,0,255))}getHexString(t=Gn){return("000000"+this.getHex(t).toString(16)).slice(-6)}getHSL(t,i=Te.workingColorSpace){Te.workingToColorSpace(Hn.copy(this),i);const r=Hn.r,l=Hn.g,c=Hn.b,f=Math.max(r,l,c),h=Math.min(r,l,c);let m,p;const _=(h+f)/2;if(h===f)m=0,p=0;else{const v=f-h;switch(p=_<=.5?v/(f+h):v/(2-f-h),f){case r:m=(l-c)/v+(l<c?6:0);break;case l:m=(c-r)/v+2;break;case c:m=(r-l)/v+4;break}m/=6}return t.h=m,t.s=p,t.l=_,t}getRGB(t,i=Te.workingColorSpace){return Te.workingToColorSpace(Hn.copy(this),i),t.r=Hn.r,t.g=Hn.g,t.b=Hn.b,t}getStyle(t=Gn){Te.workingToColorSpace(Hn.copy(this),t);const i=Hn.r,r=Hn.g,l=Hn.b;return t!==Gn?`color(${t} ${i.toFixed(3)} ${r.toFixed(3)} ${l.toFixed(3)})`:`rgb(${Math.round(i*255)},${Math.round(r*255)},${Math.round(l*255)})`}offsetHSL(t,i,r){return this.getHSL(sr),this.setHSL(sr.h+t,sr.s+i,sr.l+r)}add(t){return this.r+=t.r,this.g+=t.g,this.b+=t.b,this}addColors(t,i){return this.r=t.r+i.r,this.g=t.g+i.g,this.b=t.b+i.b,this}addScalar(t){return this.r+=t,this.g+=t,this.b+=t,this}sub(t){return this.r=Math.max(0,this.r-t.r),this.g=Math.max(0,this.g-t.g),this.b=Math.max(0,this.b-t.b),this}multiply(t){return this.r*=t.r,this.g*=t.g,this.b*=t.b,this}multiplyScalar(t){return this.r*=t,this.g*=t,this.b*=t,this}lerp(t,i){return this.r+=(t.r-this.r)*i,this.g+=(t.g-this.g)*i,this.b+=(t.b-this.b)*i,this}lerpColors(t,i,r){return this.r=t.r+(i.r-t.r)*r,this.g=t.g+(i.g-t.g)*r,this.b=t.b+(i.b-t.b)*r,this}lerpHSL(t,i){this.getHSL(sr),t.getHSL(Lc);const r=fl(sr.h,Lc.h,i),l=fl(sr.s,Lc.s,i),c=fl(sr.l,Lc.l,i);return this.setHSL(r,l,c),this}setFromVector3(t){return this.r=t.x,this.g=t.y,this.b=t.z,this}applyMatrix3(t){const i=this.r,r=this.g,l=this.b,c=t.elements;return this.r=c[0]*i+c[3]*r+c[6]*l,this.g=c[1]*i+c[4]*r+c[7]*l,this.b=c[2]*i+c[5]*r+c[8]*l,this}equals(t){return t.r===this.r&&t.g===this.g&&t.b===this.b}fromArray(t,i=0){return this.r=t[i],this.g=t[i+1],this.b=t[i+2],this}toArray(t=[],i=0){return t[i]=this.r,t[i+1]=this.g,t[i+2]=this.b,t}fromBufferAttribute(t,i){return this.r=t.getX(i),this.g=t.getY(i),this.b=t.getZ(i),this}toJSON(){return this.getHex()}*[Symbol.iterator](){yield this.r,yield this.g,yield this.b}}const Hn=new me;me.NAMES=cS;class Zp{constructor(t,i=1,r=1e3){this.isFog=!0,this.name="",this.color=new me(t),this.near=i,this.far=r}clone(){return new Zp(this.color,this.near,this.far)}toJSON(){return{type:"Fog",name:this.name,color:this.color.getHex(),near:this.near,far:this.far}}}class QE extends dn{constructor(){super(),this.isScene=!0,this.type="Scene",this.background=null,this.environment=null,this.fog=null,this.backgroundBlurriness=0,this.backgroundIntensity=1,this.backgroundRotation=new mr,this.environmentIntensity=1,this.environmentRotation=new mr,this.overrideMaterial=null,typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}copy(t,i){return super.copy(t,i),t.background!==null&&(this.background=t.background.clone()),t.environment!==null&&(this.environment=t.environment.clone()),t.fog!==null&&(this.fog=t.fog.clone()),this.backgroundBlurriness=t.backgroundBlurriness,this.backgroundIntensity=t.backgroundIntensity,this.backgroundRotation.copy(t.backgroundRotation),this.environmentIntensity=t.environmentIntensity,this.environmentRotation.copy(t.environmentRotation),t.overrideMaterial!==null&&(this.overrideMaterial=t.overrideMaterial.clone()),this.matrixAutoUpdate=t.matrixAutoUpdate,this}toJSON(t){const i=super.toJSON(t);return this.fog!==null&&(i.object.fog=this.fog.toJSON()),this.backgroundBlurriness>0&&(i.object.backgroundBlurriness=this.backgroundBlurriness),this.backgroundIntensity!==1&&(i.object.backgroundIntensity=this.backgroundIntensity),i.object.backgroundRotation=this.backgroundRotation.toArray(),this.environmentIntensity!==1&&(i.object.environmentIntensity=this.environmentIntensity),i.object.environmentRotation=this.environmentRotation.toArray(),i}}const Ui=new Q,ya=new Q,Wd=new Q,Ma=new Q,Is=new Q,zs=new Q,Sv=new Q,Yd=new Q,Zd=new Q,jd=new Q,Kd=new sn,Qd=new sn,Jd=new sn;class bi{constructor(t=new Q,i=new Q,r=new Q){this.a=t,this.b=i,this.c=r}static getNormal(t,i,r,l){l.subVectors(r,i),Ui.subVectors(t,i),l.cross(Ui);const c=l.lengthSq();return c>0?l.multiplyScalar(1/Math.sqrt(c)):l.set(0,0,0)}static getBarycoord(t,i,r,l,c){Ui.subVectors(l,i),ya.subVectors(r,i),Wd.subVectors(t,i);const f=Ui.dot(Ui),h=Ui.dot(ya),m=Ui.dot(Wd),p=ya.dot(ya),_=ya.dot(Wd),v=f*p-h*h;if(v===0)return c.set(0,0,0),null;const g=1/v,x=(p*m-h*_)*g,M=(f*_-h*m)*g;return c.set(1-x-M,M,x)}static containsPoint(t,i,r,l){return this.getBarycoord(t,i,r,l,Ma)===null?!1:Ma.x>=0&&Ma.y>=0&&Ma.x+Ma.y<=1}static getInterpolation(t,i,r,l,c,f,h,m){return this.getBarycoord(t,i,r,l,Ma)===null?(m.x=0,m.y=0,"z"in m&&(m.z=0),"w"in m&&(m.w=0),null):(m.setScalar(0),m.addScaledVector(c,Ma.x),m.addScaledVector(f,Ma.y),m.addScaledVector(h,Ma.z),m)}static getInterpolatedAttribute(t,i,r,l,c,f){return Kd.setScalar(0),Qd.setScalar(0),Jd.setScalar(0),Kd.fromBufferAttribute(t,i),Qd.fromBufferAttribute(t,r),Jd.fromBufferAttribute(t,l),f.setScalar(0),f.addScaledVector(Kd,c.x),f.addScaledVector(Qd,c.y),f.addScaledVector(Jd,c.z),f}static isFrontFacing(t,i,r,l){return Ui.subVectors(r,i),ya.subVectors(t,i),Ui.cross(ya).dot(l)<0}set(t,i,r){return this.a.copy(t),this.b.copy(i),this.c.copy(r),this}setFromPointsAndIndices(t,i,r,l){return this.a.copy(t[i]),this.b.copy(t[r]),this.c.copy(t[l]),this}setFromAttributeAndIndices(t,i,r,l){return this.a.fromBufferAttribute(t,i),this.b.fromBufferAttribute(t,r),this.c.fromBufferAttribute(t,l),this}clone(){return new this.constructor().copy(this)}copy(t){return this.a.copy(t.a),this.b.copy(t.b),this.c.copy(t.c),this}getArea(){return Ui.subVectors(this.c,this.b),ya.subVectors(this.a,this.b),Ui.cross(ya).length()*.5}getMidpoint(t){return t.addVectors(this.a,this.b).add(this.c).multiplyScalar(1/3)}getNormal(t){return bi.getNormal(this.a,this.b,this.c,t)}getPlane(t){return t.setFromCoplanarPoints(this.a,this.b,this.c)}getBarycoord(t,i){return bi.getBarycoord(t,this.a,this.b,this.c,i)}getInterpolation(t,i,r,l,c){return bi.getInterpolation(t,this.a,this.b,this.c,i,r,l,c)}containsPoint(t){return bi.containsPoint(t,this.a,this.b,this.c)}isFrontFacing(t){return bi.isFrontFacing(this.a,this.b,this.c,t)}intersectsBox(t){return t.intersectsTriangle(this)}closestPointToPoint(t,i){const r=this.a,l=this.b,c=this.c;let f,h;Is.subVectors(l,r),zs.subVectors(c,r),Yd.subVectors(t,r);const m=Is.dot(Yd),p=zs.dot(Yd);if(m<=0&&p<=0)return i.copy(r);Zd.subVectors(t,l);const _=Is.dot(Zd),v=zs.dot(Zd);if(_>=0&&v<=_)return i.copy(l);const g=m*v-_*p;if(g<=0&&m>=0&&_<=0)return f=m/(m-_),i.copy(r).addScaledVector(Is,f);jd.subVectors(t,c);const x=Is.dot(jd),M=zs.dot(jd);if(M>=0&&x<=M)return i.copy(c);const A=x*p-m*M;if(A<=0&&p>=0&&M<=0)return h=p/(p-M),i.copy(r).addScaledVector(zs,h);const E=_*M-x*v;if(E<=0&&v-_>=0&&x-M>=0)return Sv.subVectors(c,l),h=(v-_)/(v-_+(x-M)),i.copy(l).addScaledVector(Sv,h);const S=1/(E+A+g);return f=A*S,h=g*S,i.copy(r).addScaledVector(Is,f).addScaledVector(zs,h)}equals(t){return t.a.equals(this.a)&&t.b.equals(this.b)&&t.c.equals(this.c)}}class _l{constructor(t=new Q(1/0,1/0,1/0),i=new Q(-1/0,-1/0,-1/0)){this.isBox3=!0,this.min=t,this.max=i}set(t,i){return this.min.copy(t),this.max.copy(i),this}setFromArray(t){this.makeEmpty();for(let i=0,r=t.length;i<r;i+=3)this.expandByPoint(Li.fromArray(t,i));return this}setFromBufferAttribute(t){this.makeEmpty();for(let i=0,r=t.count;i<r;i++)this.expandByPoint(Li.fromBufferAttribute(t,i));return this}setFromPoints(t){this.makeEmpty();for(let i=0,r=t.length;i<r;i++)this.expandByPoint(t[i]);return this}setFromCenterAndSize(t,i){const r=Li.copy(i).multiplyScalar(.5);return this.min.copy(t).sub(r),this.max.copy(t).add(r),this}setFromObject(t,i=!1){return this.makeEmpty(),this.expandByObject(t,i)}clone(){return new this.constructor().copy(this)}copy(t){return this.min.copy(t.min),this.max.copy(t.max),this}makeEmpty(){return this.min.x=this.min.y=this.min.z=1/0,this.max.x=this.max.y=this.max.z=-1/0,this}isEmpty(){return this.max.x<this.min.x||this.max.y<this.min.y||this.max.z<this.min.z}getCenter(t){return this.isEmpty()?t.set(0,0,0):t.addVectors(this.min,this.max).multiplyScalar(.5)}getSize(t){return this.isEmpty()?t.set(0,0,0):t.subVectors(this.max,this.min)}expandByPoint(t){return this.min.min(t),this.max.max(t),this}expandByVector(t){return this.min.sub(t),this.max.add(t),this}expandByScalar(t){return this.min.addScalar(-t),this.max.addScalar(t),this}expandByObject(t,i=!1){t.updateWorldMatrix(!1,!1);const r=t.geometry;if(r!==void 0){const c=r.getAttribute("position");if(i===!0&&c!==void 0&&t.isInstancedMesh!==!0)for(let f=0,h=c.count;f<h;f++)t.isMesh===!0?t.getVertexPosition(f,Li):Li.fromBufferAttribute(c,f),Li.applyMatrix4(t.matrixWorld),this.expandByPoint(Li);else t.boundingBox!==void 0?(t.boundingBox===null&&t.computeBoundingBox(),Oc.copy(t.boundingBox)):(r.boundingBox===null&&r.computeBoundingBox(),Oc.copy(r.boundingBox)),Oc.applyMatrix4(t.matrixWorld),this.union(Oc)}const l=t.children;for(let c=0,f=l.length;c<f;c++)this.expandByObject(l[c],i);return this}containsPoint(t){return t.x>=this.min.x&&t.x<=this.max.x&&t.y>=this.min.y&&t.y<=this.max.y&&t.z>=this.min.z&&t.z<=this.max.z}containsBox(t){return this.min.x<=t.min.x&&t.max.x<=this.max.x&&this.min.y<=t.min.y&&t.max.y<=this.max.y&&this.min.z<=t.min.z&&t.max.z<=this.max.z}getParameter(t,i){return i.set((t.x-this.min.x)/(this.max.x-this.min.x),(t.y-this.min.y)/(this.max.y-this.min.y),(t.z-this.min.z)/(this.max.z-this.min.z))}intersectsBox(t){return t.max.x>=this.min.x&&t.min.x<=this.max.x&&t.max.y>=this.min.y&&t.min.y<=this.max.y&&t.max.z>=this.min.z&&t.min.z<=this.max.z}intersectsSphere(t){return this.clampPoint(t.center,Li),Li.distanceToSquared(t.center)<=t.radius*t.radius}intersectsPlane(t){let i,r;return t.normal.x>0?(i=t.normal.x*this.min.x,r=t.normal.x*this.max.x):(i=t.normal.x*this.max.x,r=t.normal.x*this.min.x),t.normal.y>0?(i+=t.normal.y*this.min.y,r+=t.normal.y*this.max.y):(i+=t.normal.y*this.max.y,r+=t.normal.y*this.min.y),t.normal.z>0?(i+=t.normal.z*this.min.z,r+=t.normal.z*this.max.z):(i+=t.normal.z*this.max.z,r+=t.normal.z*this.min.z),i<=-t.constant&&r>=-t.constant}intersectsTriangle(t){if(this.isEmpty())return!1;this.getCenter(tl),Pc.subVectors(this.max,tl),Fs.subVectors(t.a,tl),Hs.subVectors(t.b,tl),Gs.subVectors(t.c,tl),or.subVectors(Hs,Fs),lr.subVectors(Gs,Hs),Br.subVectors(Fs,Gs);let i=[0,-or.z,or.y,0,-lr.z,lr.y,0,-Br.z,Br.y,or.z,0,-or.x,lr.z,0,-lr.x,Br.z,0,-Br.x,-or.y,or.x,0,-lr.y,lr.x,0,-Br.y,Br.x,0];return!$d(i,Fs,Hs,Gs,Pc)||(i=[1,0,0,0,1,0,0,0,1],!$d(i,Fs,Hs,Gs,Pc))?!1:(Bc.crossVectors(or,lr),i=[Bc.x,Bc.y,Bc.z],$d(i,Fs,Hs,Gs,Pc))}clampPoint(t,i){return i.copy(t).clamp(this.min,this.max)}distanceToPoint(t){return this.clampPoint(t,Li).distanceTo(t)}getBoundingSphere(t){return this.isEmpty()?t.makeEmpty():(this.getCenter(t.center),t.radius=this.getSize(Li).length()*.5),t}intersect(t){return this.min.max(t.min),this.max.min(t.max),this.isEmpty()&&this.makeEmpty(),this}union(t){return this.min.min(t.min),this.max.max(t.max),this}applyMatrix4(t){return this.isEmpty()?this:(Ea[0].set(this.min.x,this.min.y,this.min.z).applyMatrix4(t),Ea[1].set(this.min.x,this.min.y,this.max.z).applyMatrix4(t),Ea[2].set(this.min.x,this.max.y,this.min.z).applyMatrix4(t),Ea[3].set(this.min.x,this.max.y,this.max.z).applyMatrix4(t),Ea[4].set(this.max.x,this.min.y,this.min.z).applyMatrix4(t),Ea[5].set(this.max.x,this.min.y,this.max.z).applyMatrix4(t),Ea[6].set(this.max.x,this.max.y,this.min.z).applyMatrix4(t),Ea[7].set(this.max.x,this.max.y,this.max.z).applyMatrix4(t),this.setFromPoints(Ea),this)}translate(t){return this.min.add(t),this.max.add(t),this}equals(t){return t.min.equals(this.min)&&t.max.equals(this.max)}toJSON(){return{min:this.min.toArray(),max:this.max.toArray()}}fromJSON(t){return this.min.fromArray(t.min),this.max.fromArray(t.max),this}}const Ea=[new Q,new Q,new Q,new Q,new Q,new Q,new Q,new Q],Li=new Q,Oc=new _l,Fs=new Q,Hs=new Q,Gs=new Q,or=new Q,lr=new Q,Br=new Q,tl=new Q,Pc=new Q,Bc=new Q,Ir=new Q;function $d(s,t,i,r,l){for(let c=0,f=s.length-3;c<=f;c+=3){Ir.fromArray(s,c);const h=l.x*Math.abs(Ir.x)+l.y*Math.abs(Ir.y)+l.z*Math.abs(Ir.z),m=t.dot(Ir),p=i.dot(Ir),_=r.dot(Ir);if(Math.max(-Math.max(m,p,_),Math.min(m,p,_))>h)return!1}return!0}const Sn=new Q,Ic=new Ae;let JE=0;class Ti extends jr{constructor(t,i,r=!1){if(super(),Array.isArray(t))throw new TypeError("THREE.BufferAttribute: array should be a Typed Array.");this.isBufferAttribute=!0,Object.defineProperty(this,"id",{value:JE++}),this.name="",this.array=t,this.itemSize=i,this.count=t!==void 0?t.length/i:0,this.normalized=r,this.usage=rv,this.updateRanges=[],this.gpuType=Zi,this.version=0}onUploadCallback(){}set needsUpdate(t){t===!0&&this.version++}setUsage(t){return this.usage=t,this}addUpdateRange(t,i){this.updateRanges.push({start:t,count:i})}clearUpdateRanges(){this.updateRanges.length=0}copy(t){return this.name=t.name,this.array=new t.array.constructor(t.array),this.itemSize=t.itemSize,this.count=t.count,this.normalized=t.normalized,this.usage=t.usage,this.gpuType=t.gpuType,this}copyAt(t,i,r){t*=this.itemSize,r*=i.itemSize;for(let l=0,c=this.itemSize;l<c;l++)this.array[t+l]=i.array[r+l];return this}copyArray(t){return this.array.set(t),this}applyMatrix3(t){if(this.itemSize===2)for(let i=0,r=this.count;i<r;i++)Ic.fromBufferAttribute(this,i),Ic.applyMatrix3(t),this.setXY(i,Ic.x,Ic.y);else if(this.itemSize===3)for(let i=0,r=this.count;i<r;i++)Sn.fromBufferAttribute(this,i),Sn.applyMatrix3(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}applyMatrix4(t){for(let i=0,r=this.count;i<r;i++)Sn.fromBufferAttribute(this,i),Sn.applyMatrix4(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}applyNormalMatrix(t){for(let i=0,r=this.count;i<r;i++)Sn.fromBufferAttribute(this,i),Sn.applyNormalMatrix(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}transformDirection(t){for(let i=0,r=this.count;i<r;i++)Sn.fromBufferAttribute(this,i),Sn.transformDirection(t),this.setXYZ(i,Sn.x,Sn.y,Sn.z);return this}set(t,i=0){return this.array.set(t,i),this}getComponent(t,i){let r=this.array[t*this.itemSize+i];return this.normalized&&(r=Zs(r,this.array)),r}setComponent(t,i,r){return this.normalized&&(r=kn(r,this.array)),this.array[t*this.itemSize+i]=r,this}getX(t){let i=this.array[t*this.itemSize];return this.normalized&&(i=Zs(i,this.array)),i}setX(t,i){return this.normalized&&(i=kn(i,this.array)),this.array[t*this.itemSize]=i,this}getY(t){let i=this.array[t*this.itemSize+1];return this.normalized&&(i=Zs(i,this.array)),i}setY(t,i){return this.normalized&&(i=kn(i,this.array)),this.array[t*this.itemSize+1]=i,this}getZ(t){let i=this.array[t*this.itemSize+2];return this.normalized&&(i=Zs(i,this.array)),i}setZ(t,i){return this.normalized&&(i=kn(i,this.array)),this.array[t*this.itemSize+2]=i,this}getW(t){let i=this.array[t*this.itemSize+3];return this.normalized&&(i=Zs(i,this.array)),i}setW(t,i){return this.normalized&&(i=kn(i,this.array)),this.array[t*this.itemSize+3]=i,this}setXY(t,i,r){return t*=this.itemSize,this.normalized&&(i=kn(i,this.array),r=kn(r,this.array)),this.array[t+0]=i,this.array[t+1]=r,this}setXYZ(t,i,r,l){return t*=this.itemSize,this.normalized&&(i=kn(i,this.array),r=kn(r,this.array),l=kn(l,this.array)),this.array[t+0]=i,this.array[t+1]=r,this.array[t+2]=l,this}setXYZW(t,i,r,l,c){return t*=this.itemSize,this.normalized&&(i=kn(i,this.array),r=kn(r,this.array),l=kn(l,this.array),c=kn(c,this.array)),this.array[t+0]=i,this.array[t+1]=r,this.array[t+2]=l,this.array[t+3]=c,this}onUpload(t){return this.onUploadCallback=t,this}clone(){return new this.constructor(this.array,this.itemSize).copy(this)}toJSON(){const t={itemSize:this.itemSize,type:this.array.constructor.name,array:Array.from(this.array),normalized:this.normalized};return this.name!==""&&(t.name=this.name),this.usage!==rv&&(t.usage=this.usage),t}dispose(){this.dispatchEvent({type:"dispose"})}}class uS extends Ti{constructor(t,i,r){super(new Uint16Array(t),i,r)}}class fS extends Ti{constructor(t,i,r){super(new Uint32Array(t),i,r)}}class $n extends Ti{constructor(t,i,r){super(new Float32Array(t),i,r)}}const $E=new _l,el=new Q,th=new Q;class vl{constructor(t=new Q,i=-1){this.isSphere=!0,this.center=t,this.radius=i}set(t,i){return this.center.copy(t),this.radius=i,this}setFromPoints(t,i){const r=this.center;i!==void 0?r.copy(i):$E.setFromPoints(t).getCenter(r);let l=0;for(let c=0,f=t.length;c<f;c++)l=Math.max(l,r.distanceToSquared(t[c]));return this.radius=Math.sqrt(l),this}copy(t){return this.center.copy(t.center),this.radius=t.radius,this}isEmpty(){return this.radius<0}makeEmpty(){return this.center.set(0,0,0),this.radius=-1,this}containsPoint(t){return t.distanceToSquared(this.center)<=this.radius*this.radius}distanceToPoint(t){return t.distanceTo(this.center)-this.radius}intersectsSphere(t){const i=this.radius+t.radius;return t.center.distanceToSquared(this.center)<=i*i}intersectsBox(t){return t.intersectsSphere(this)}intersectsPlane(t){return Math.abs(t.distanceToPoint(this.center))<=this.radius}clampPoint(t,i){const r=this.center.distanceToSquared(t);return i.copy(t),r>this.radius*this.radius&&(i.sub(this.center).normalize(),i.multiplyScalar(this.radius).add(this.center)),i}getBoundingBox(t){return this.isEmpty()?(t.makeEmpty(),t):(t.set(this.center,this.center),t.expandByScalar(this.radius),t)}applyMatrix4(t){return this.center.applyMatrix4(t),this.radius=this.radius*t.getMaxScaleOnAxis(),this}translate(t){return this.center.add(t),this}expandByPoint(t){if(this.isEmpty())return this.center.copy(t),this.radius=0,this;el.subVectors(t,this.center);const i=el.lengthSq();if(i>this.radius*this.radius){const r=Math.sqrt(i),l=(r-this.radius)*.5;this.center.addScaledVector(el,l/r),this.radius+=l}return this}union(t){return t.isEmpty()?this:this.isEmpty()?(this.copy(t),this):(this.center.equals(t.center)===!0?this.radius=Math.max(this.radius,t.radius):(th.subVectors(t.center,this.center).setLength(t.radius),this.expandByPoint(el.copy(t.center).add(th)),this.expandByPoint(el.copy(t.center).sub(th))),this)}equals(t){return t.center.equals(this.center)&&t.radius===this.radius}clone(){return new this.constructor().copy(this)}toJSON(){return{radius:this.radius,center:this.center.toArray()}}fromJSON(t){return this.radius=t.radius,this.center.fromArray(t.center),this}}let tb=0;const Ei=new $e,eh=new dn,Vs=new Q,di=new _l,nl=new _l,Cn=new Q;class bn extends jr{constructor(){super(),this.isBufferGeometry=!0,Object.defineProperty(this,"id",{value:tb++}),this.uuid=ao(),this.name="",this.type="BufferGeometry",this.index=null,this.indirect=null,this.indirectOffset=0,this.attributes={},this.morphAttributes={},this.morphTargetsRelative=!1,this.groups=[],this.boundingBox=null,this.boundingSphere=null,this.drawRange={start:0,count:1/0},this.userData={},this._transformed=!1}getIndex(){return this.index}setIndex(t){return Array.isArray(t)?this.index=new(vE(t)?fS:uS)(t,1):this.index=t,this}setIndirect(t,i=0){return this.indirect=t,this.indirectOffset=i,this}getIndirect(){return this.indirect}getAttribute(t){return this.attributes[t]}setAttribute(t,i){return this.attributes[t]=i,this}deleteAttribute(t){return delete this.attributes[t],this}hasAttribute(t){return this.attributes[t]!==void 0}addGroup(t,i,r=0){this.groups.push({start:t,count:i,materialIndex:r})}clearGroups(){this.groups=[]}setDrawRange(t,i){this.drawRange.start=t,this.drawRange.count=i}applyMatrix4(t){const i=this.attributes.position;i!==void 0&&(i.applyMatrix4(t),i.needsUpdate=!0);const r=this.attributes.normal;if(r!==void 0){const c=new ce().getNormalMatrix(t);r.applyNormalMatrix(c),r.needsUpdate=!0}const l=this.attributes.tangent;return l!==void 0&&(l.transformDirection(t),l.needsUpdate=!0),this.boundingBox!==null&&this.computeBoundingBox(),this.boundingSphere!==null&&this.computeBoundingSphere(),this._transformed=!0,this}applyQuaternion(t){return Ei.makeRotationFromQuaternion(t),this.applyMatrix4(Ei),this}rotateX(t){return Ei.makeRotationX(t),this.applyMatrix4(Ei),this}rotateY(t){return Ei.makeRotationY(t),this.applyMatrix4(Ei),this}rotateZ(t){return Ei.makeRotationZ(t),this.applyMatrix4(Ei),this}translate(t,i,r){return Ei.makeTranslation(t,i,r),this.applyMatrix4(Ei),this}scale(t,i,r){return Ei.makeScale(t,i,r),this.applyMatrix4(Ei),this}lookAt(t){return eh.lookAt(t),eh.updateMatrix(),this.applyMatrix4(eh.matrix),this}center(){return this.computeBoundingBox(),this.boundingBox.getCenter(Vs).negate(),this.translate(Vs.x,Vs.y,Vs.z),this}setFromPoints(t){const i=this.getAttribute("position");if(i===void 0){const r=[];for(let l=0,c=t.length;l<c;l++){const f=t[l];r.push(f.x,f.y,f.z||0)}this.setAttribute("position",new $n(r,3))}else{const r=Math.min(t.length,i.count);for(let l=0;l<r;l++){const c=t[l];i.setXYZ(l,c.x,c.y,c.z||0)}t.length>i.count&&se("BufferGeometry: Buffer size too small for points data. Use .dispose() and create a new geometry."),i.needsUpdate=!0}return this}computeBoundingBox(){this.boundingBox===null&&(this.boundingBox=new _l);const t=this.attributes.position,i=this.morphAttributes.position;if(t&&t.isGLBufferAttribute){Re("BufferGeometry.computeBoundingBox(): GLBufferAttribute requires a manual bounding box.",this),this.boundingBox.set(new Q(-1/0,-1/0,-1/0),new Q(1/0,1/0,1/0));return}if(t!==void 0){if(this.boundingBox.setFromBufferAttribute(t),i)for(let r=0,l=i.length;r<l;r++){const c=i[r];di.setFromBufferAttribute(c),this.morphTargetsRelative?(Cn.addVectors(this.boundingBox.min,di.min),this.boundingBox.expandByPoint(Cn),Cn.addVectors(this.boundingBox.max,di.max),this.boundingBox.expandByPoint(Cn)):(this.boundingBox.expandByPoint(di.min),this.boundingBox.expandByPoint(di.max))}}else this.boundingBox.makeEmpty();(isNaN(this.boundingBox.min.x)||isNaN(this.boundingBox.min.y)||isNaN(this.boundingBox.min.z))&&Re('BufferGeometry.computeBoundingBox(): Computed min/max have NaN values. The "position" attribute is likely to have NaN values.',this)}computeBoundingSphere(){this.boundingSphere===null&&(this.boundingSphere=new vl);const t=this.attributes.position,i=this.morphAttributes.position;if(t&&t.isGLBufferAttribute){Re("BufferGeometry.computeBoundingSphere(): GLBufferAttribute requires a manual bounding sphere.",this),this.boundingSphere.set(new Q,1/0);return}if(t){const r=this.boundingSphere.center;if(di.setFromBufferAttribute(t),i)for(let c=0,f=i.length;c<f;c++){const h=i[c];nl.setFromBufferAttribute(h),this.morphTargetsRelative?(Cn.addVectors(di.min,nl.min),di.expandByPoint(Cn),Cn.addVectors(di.max,nl.max),di.expandByPoint(Cn)):(di.expandByPoint(nl.min),di.expandByPoint(nl.max))}di.getCenter(r);let l=0;for(let c=0,f=t.count;c<f;c++)Cn.fromBufferAttribute(t,c),l=Math.max(l,r.distanceToSquared(Cn));if(i)for(let c=0,f=i.length;c<f;c++){const h=i[c],m=this.morphTargetsRelative;for(let p=0,_=h.count;p<_;p++)Cn.fromBufferAttribute(h,p),m&&(Vs.fromBufferAttribute(t,p),Cn.add(Vs)),l=Math.max(l,r.distanceToSquared(Cn))}this.boundingSphere.radius=Math.sqrt(l),isNaN(this.boundingSphere.radius)&&Re('BufferGeometry.computeBoundingSphere(): Computed radius is NaN. The "position" attribute is likely to have NaN values.',this)}}computeTangents(){const t=this.index,i=this.attributes;if(t===null||i.position===void 0||i.normal===void 0||i.uv===void 0){Re("BufferGeometry: .computeTangents() failed. Missing required attributes (index, position, normal or uv)");return}const r=i.position,l=i.normal,c=i.uv;let f=this.getAttribute("tangent");(f===void 0||f.count!==r.count)&&(f=new Ti(new Float32Array(4*r.count),4),this.setAttribute("tangent",f));const h=[],m=[];for(let b=0;b<r.count;b++)h[b]=new Q,m[b]=new Q;const p=new Q,_=new Q,v=new Q,g=new Ae,x=new Ae,M=new Ae,A=new Q,E=new Q;function S(b,N,G){p.fromBufferAttribute(r,b),_.fromBufferAttribute(r,N),v.fromBufferAttribute(r,G),g.fromBufferAttribute(c,b),x.fromBufferAttribute(c,N),M.fromBufferAttribute(c,G),_.sub(p),v.sub(p),x.sub(g),M.sub(g);const F=1/(x.x*M.y-M.x*x.y);isFinite(F)&&(A.copy(_).multiplyScalar(M.y).addScaledVector(v,-x.y).multiplyScalar(F),E.copy(v).multiplyScalar(x.x).addScaledVector(_,-M.x).multiplyScalar(F),h[b].add(A),h[N].add(A),h[G].add(A),m[b].add(E),m[N].add(E),m[G].add(E))}let P=this.groups;P.length===0&&(P=[{start:0,count:t.count}]);for(let b=0,N=P.length;b<N;++b){const G=P[b],F=G.start,k=G.count;for(let K=F,tt=F+k;K<tt;K+=3)S(t.getX(K+0),t.getX(K+1),t.getX(K+2))}const O=new Q,C=new Q,z=new Q,D=new Q;function U(b){z.fromBufferAttribute(l,b),D.copy(z);const N=h[b];O.copy(N),O.sub(z.multiplyScalar(z.dot(N))).normalize(),C.crossVectors(D,N);const F=C.dot(m[b])<0?-1:1;f.setXYZW(b,O.x,O.y,O.z,F)}for(let b=0,N=P.length;b<N;++b){const G=P[b],F=G.start,k=G.count;for(let K=F,tt=F+k;K<tt;K+=3)U(t.getX(K+0)),U(t.getX(K+1)),U(t.getX(K+2))}this._transformed=!0}computeVertexNormals(){const t=this.index,i=this.getAttribute("position");if(i!==void 0){let r=this.getAttribute("normal");if(r===void 0||r.count!==i.count)r=new Ti(new Float32Array(i.count*3),3),this.setAttribute("normal",r);else for(let g=0,x=r.count;g<x;g++)r.setXYZ(g,0,0,0);const l=new Q,c=new Q,f=new Q,h=new Q,m=new Q,p=new Q,_=new Q,v=new Q;if(t)for(let g=0,x=t.count;g<x;g+=3){const M=t.getX(g+0),A=t.getX(g+1),E=t.getX(g+2);l.fromBufferAttribute(i,M),c.fromBufferAttribute(i,A),f.fromBufferAttribute(i,E),_.subVectors(f,c),v.subVectors(l,c),_.cross(v),h.fromBufferAttribute(r,M),m.fromBufferAttribute(r,A),p.fromBufferAttribute(r,E),h.add(_),m.add(_),p.add(_),r.setXYZ(M,h.x,h.y,h.z),r.setXYZ(A,m.x,m.y,m.z),r.setXYZ(E,p.x,p.y,p.z)}else for(let g=0,x=i.count;g<x;g+=3)l.fromBufferAttribute(i,g+0),c.fromBufferAttribute(i,g+1),f.fromBufferAttribute(i,g+2),_.subVectors(f,c),v.subVectors(l,c),_.cross(v),r.setXYZ(g+0,_.x,_.y,_.z),r.setXYZ(g+1,_.x,_.y,_.z),r.setXYZ(g+2,_.x,_.y,_.z);this.normalizeNormals(),r.needsUpdate=!0}}normalizeNormals(){const t=this.attributes.normal;for(let i=0,r=t.count;i<r;i++)Cn.fromBufferAttribute(t,i),Cn.normalize(),t.setXYZ(i,Cn.x,Cn.y,Cn.z)}toNonIndexed(){function t(h,m){const p=h.array,_=h.itemSize,v=h.normalized,g=new p.constructor(m.length*_);let x=0,M=0;for(let A=0,E=m.length;A<E;A++){h.isInterleavedBufferAttribute?x=m[A]*h.data.stride+h.offset:x=m[A]*_;for(let S=0;S<_;S++)g[M++]=p[x++]}return new Ti(g,_,v)}if(this.index===null)return se("BufferGeometry.toNonIndexed(): BufferGeometry is already non-indexed."),this;const i=new bn,r=this.index.array,l=this.attributes;for(const h in l){const m=l[h],p=t(m,r);i.setAttribute(h,p)}const c=this.morphAttributes;for(const h in c){const m=[],p=c[h];for(let _=0,v=p.length;_<v;_++){const g=p[_],x=t(g,r);m.push(x)}i.morphAttributes[h]=m}i.morphTargetsRelative=this.morphTargetsRelative;const f=this.groups;for(let h=0,m=f.length;h<m;h++){const p=f[h];i.addGroup(p.start,p.count,p.materialIndex)}return i}toJSON(){const t={metadata:{version:4.7,type:"BufferGeometry",generator:"BufferGeometry.toJSON"}};if(t.uuid=this.uuid,t.type=this.parameters!==void 0&&this._transformed===!0?"BufferGeometry":this.type,this.name!==""&&(t.name=this.name),Object.keys(this.userData).length>0&&(t.userData=this.userData),this.parameters!==void 0&&this._transformed!==!0){const m=this.parameters;for(const p in m)m[p]!==void 0&&(t[p]=m[p]);return t}t.data={attributes:{}};const i=this.index;i!==null&&(t.data.index={type:i.array.constructor.name,array:Array.prototype.slice.call(i.array)});const r=this.attributes;for(const m in r){const p=r[m];t.data.attributes[m]=p.toJSON(t.data)}const l={};let c=!1;for(const m in this.morphAttributes){const p=this.morphAttributes[m],_=[];for(let v=0,g=p.length;v<g;v++){const x=p[v];_.push(x.toJSON(t.data))}_.length>0&&(l[m]=_,c=!0)}c&&(t.data.morphAttributes=l,t.data.morphTargetsRelative=this.morphTargetsRelative);const f=this.groups;f.length>0&&(t.data.groups=JSON.parse(JSON.stringify(f)));const h=this.boundingSphere;return h!==null&&(t.data.boundingSphere=h.toJSON()),t}clone(){return new this.constructor().copy(this)}copy(t){this.index=null,this.attributes={},this.morphAttributes={},this.groups=[],this.boundingBox=null,this.boundingSphere=null;const i={};this.name=t.name;const r=t.index;r!==null&&this.setIndex(r.clone());const l=t.attributes;for(const p in l){const _=l[p];this.setAttribute(p,_.clone(i))}const c=t.morphAttributes;for(const p in c){const _=[],v=c[p];for(let g=0,x=v.length;g<x;g++)_.push(v[g].clone(i));this.morphAttributes[p]=_}this.morphTargetsRelative=t.morphTargetsRelative;const f=t.groups;for(let p=0,_=f.length;p<_;p++){const v=f[p];this.addGroup(v.start,v.count,v.materialIndex)}const h=t.boundingBox;h!==null&&(this.boundingBox=h.clone());const m=t.boundingSphere;return m!==null&&(this.boundingSphere=m.clone()),this.drawRange.start=t.drawRange.start,this.drawRange.count=t.drawRange.count,this.userData=t.userData,this._transformed=t._transformed,this}dispose(){this.dispatchEvent({type:"dispose"})}}let eb=0;class Qr extends jr{constructor(){super(),this.isMaterial=!0,Object.defineProperty(this,"id",{value:eb++}),this.uuid=ao(),this.name="",this.type="Material",this.blending=js,this.side=pr,this.vertexColors=!1,this.opacity=1,this.transparent=!1,this.alphaHash=!1,this.blendSrc=Hh,this.blendDst=Gh,this.blendEquation=Gr,this.blendSrcAlpha=null,this.blendDstAlpha=null,this.blendEquationAlpha=null,this.blendColor=new me(0,0,0),this.blendAlpha=0,this.depthFunc=to,this.depthTest=!0,this.depthWrite=!0,this.stencilWriteMask=255,this.stencilFunc=av,this.stencilRef=0,this.stencilFuncMask=255,this.stencilFail=Us,this.stencilZFail=Us,this.stencilZPass=Us,this.stencilWrite=!1,this.clippingPlanes=null,this.clipIntersection=!1,this.clipShadows=!1,this.shadowSide=null,this.colorWrite=!0,this.precision=null,this.polygonOffset=!1,this.polygonOffsetFactor=0,this.polygonOffsetUnits=0,this.dithering=!1,this.alphaToCoverage=!1,this.premultipliedAlpha=!1,this.forceSinglePass=!1,this.allowOverride=!0,this.visible=!0,this.toneMapped=!0,this.userData={},this.version=0,this._alphaTest=0}get alphaTest(){return this._alphaTest}set alphaTest(t){this._alphaTest>0!=t>0&&this.version++,this._alphaTest=t}onBeforeRender(){}onBeforeCompile(){}customProgramCacheKey(){return this.onBeforeCompile.toString()}setValues(t){if(t!==void 0)for(const i in t){const r=t[i];if(r===void 0){se(`Material: parameter '${i}' has value of undefined.`);continue}const l=this[i];if(l===void 0){se(`Material: '${i}' is not a property of THREE.${this.type}.`);continue}l&&l.isColor?l.set(r):l&&l.isVector2&&r&&r.isVector2||l&&l.isEuler&&r&&r.isEuler||l&&l.isVector3&&r&&r.isVector3?l.copy(r):this[i]=r}}toJSON(t){const i=t===void 0||typeof t=="string";i&&(t={textures:{},images:{}});const r={metadata:{version:4.7,type:"Material",generator:"Material.toJSON"}};r.uuid=this.uuid,r.type=this.type,this.name!==""&&(r.name=this.name),this.color&&this.color.isColor&&(r.color=this.color.getHex()),this.roughness!==void 0&&(r.roughness=this.roughness),this.metalness!==void 0&&(r.metalness=this.metalness),this.sheen!==void 0&&(r.sheen=this.sheen),this.sheenColor&&this.sheenColor.isColor&&(r.sheenColor=this.sheenColor.getHex()),this.sheenRoughness!==void 0&&(r.sheenRoughness=this.sheenRoughness),this.emissive&&this.emissive.isColor&&(r.emissive=this.emissive.getHex()),this.emissiveIntensity!==void 0&&this.emissiveIntensity!==1&&(r.emissiveIntensity=this.emissiveIntensity),this.specular&&this.specular.isColor&&(r.specular=this.specular.getHex()),this.specularIntensity!==void 0&&(r.specularIntensity=this.specularIntensity),this.specularColor&&this.specularColor.isColor&&(r.specularColor=this.specularColor.getHex()),this.shininess!==void 0&&(r.shininess=this.shininess),this.clearcoat!==void 0&&(r.clearcoat=this.clearcoat),this.clearcoatRoughness!==void 0&&(r.clearcoatRoughness=this.clearcoatRoughness),this.clearcoatMap&&this.clearcoatMap.isTexture&&(r.clearcoatMap=this.clearcoatMap.toJSON(t).uuid),this.clearcoatRoughnessMap&&this.clearcoatRoughnessMap.isTexture&&(r.clearcoatRoughnessMap=this.clearcoatRoughnessMap.toJSON(t).uuid),this.clearcoatNormalMap&&this.clearcoatNormalMap.isTexture&&(r.clearcoatNormalMap=this.clearcoatNormalMap.toJSON(t).uuid,r.clearcoatNormalScale=this.clearcoatNormalScale.toArray()),this.sheenColorMap&&this.sheenColorMap.isTexture&&(r.sheenColorMap=this.sheenColorMap.toJSON(t).uuid),this.sheenRoughnessMap&&this.sheenRoughnessMap.isTexture&&(r.sheenRoughnessMap=this.sheenRoughnessMap.toJSON(t).uuid),this.dispersion!==void 0&&(r.dispersion=this.dispersion),this.iridescence!==void 0&&(r.iridescence=this.iridescence),this.iridescenceIOR!==void 0&&(r.iridescenceIOR=this.iridescenceIOR),this.iridescenceThicknessRange!==void 0&&(r.iridescenceThicknessRange=this.iridescenceThicknessRange),this.iridescenceMap&&this.iridescenceMap.isTexture&&(r.iridescenceMap=this.iridescenceMap.toJSON(t).uuid),this.iridescenceThicknessMap&&this.iridescenceThicknessMap.isTexture&&(r.iridescenceThicknessMap=this.iridescenceThicknessMap.toJSON(t).uuid),this.anisotropy!==void 0&&(r.anisotropy=this.anisotropy),this.anisotropyRotation!==void 0&&(r.anisotropyRotation=this.anisotropyRotation),this.anisotropyMap&&this.anisotropyMap.isTexture&&(r.anisotropyMap=this.anisotropyMap.toJSON(t).uuid),this.map&&this.map.isTexture&&(r.map=this.map.toJSON(t).uuid),this.matcap&&this.matcap.isTexture&&(r.matcap=this.matcap.toJSON(t).uuid),this.alphaMap&&this.alphaMap.isTexture&&(r.alphaMap=this.alphaMap.toJSON(t).uuid),this.lightMap&&this.lightMap.isTexture&&(r.lightMap=this.lightMap.toJSON(t).uuid,r.lightMapIntensity=this.lightMapIntensity),this.aoMap&&this.aoMap.isTexture&&(r.aoMap=this.aoMap.toJSON(t).uuid,r.aoMapIntensity=this.aoMapIntensity),this.bumpMap&&this.bumpMap.isTexture&&(r.bumpMap=this.bumpMap.toJSON(t).uuid,r.bumpScale=this.bumpScale),this.normalMap&&this.normalMap.isTexture&&(r.normalMap=this.normalMap.toJSON(t).uuid,r.normalMapType=this.normalMapType,r.normalScale=this.normalScale.toArray()),this.displacementMap&&this.displacementMap.isTexture&&(r.displacementMap=this.displacementMap.toJSON(t).uuid,r.displacementScale=this.displacementScale,r.displacementBias=this.displacementBias),this.roughnessMap&&this.roughnessMap.isTexture&&(r.roughnessMap=this.roughnessMap.toJSON(t).uuid),this.metalnessMap&&this.metalnessMap.isTexture&&(r.metalnessMap=this.metalnessMap.toJSON(t).uuid),this.emissiveMap&&this.emissiveMap.isTexture&&(r.emissiveMap=this.emissiveMap.toJSON(t).uuid),this.specularMap&&this.specularMap.isTexture&&(r.specularMap=this.specularMap.toJSON(t).uuid),this.specularIntensityMap&&this.specularIntensityMap.isTexture&&(r.specularIntensityMap=this.specularIntensityMap.toJSON(t).uuid),this.specularColorMap&&this.specularColorMap.isTexture&&(r.specularColorMap=this.specularColorMap.toJSON(t).uuid),this.envMap&&this.envMap.isTexture&&(r.envMap=this.envMap.toJSON(t).uuid,this.combine!==void 0&&(r.combine=this.combine)),this.envMapRotation!==void 0&&(r.envMapRotation=this.envMapRotation.toArray()),this.envMapIntensity!==void 0&&(r.envMapIntensity=this.envMapIntensity),this.reflectivity!==void 0&&(r.reflectivity=this.reflectivity),this.refractionRatio!==void 0&&(r.refractionRatio=this.refractionRatio),this.gradientMap&&this.gradientMap.isTexture&&(r.gradientMap=this.gradientMap.toJSON(t).uuid),this.transmission!==void 0&&(r.transmission=this.transmission),this.transmissionMap&&this.transmissionMap.isTexture&&(r.transmissionMap=this.transmissionMap.toJSON(t).uuid),this.thickness!==void 0&&(r.thickness=this.thickness),this.thicknessMap&&this.thicknessMap.isTexture&&(r.thicknessMap=this.thicknessMap.toJSON(t).uuid),this.attenuationDistance!==void 0&&this.attenuationDistance!==1/0&&(r.attenuationDistance=this.attenuationDistance),this.attenuationColor!==void 0&&(r.attenuationColor=this.attenuationColor.getHex()),this.size!==void 0&&(r.size=this.size),this.shadowSide!==null&&(r.shadowSide=this.shadowSide),this.sizeAttenuation!==void 0&&(r.sizeAttenuation=this.sizeAttenuation),this.blending!==js&&(r.blending=this.blending),this.side!==pr&&(r.side=this.side),this.vertexColors===!0&&(r.vertexColors=!0),this.opacity<1&&(r.opacity=this.opacity),this.transparent===!0&&(r.transparent=!0),this.blendSrc!==Hh&&(r.blendSrc=this.blendSrc),this.blendDst!==Gh&&(r.blendDst=this.blendDst),this.blendEquation!==Gr&&(r.blendEquation=this.blendEquation),this.blendSrcAlpha!==null&&(r.blendSrcAlpha=this.blendSrcAlpha),this.blendDstAlpha!==null&&(r.blendDstAlpha=this.blendDstAlpha),this.blendEquationAlpha!==null&&(r.blendEquationAlpha=this.blendEquationAlpha),this.blendColor&&this.blendColor.isColor&&(r.blendColor=this.blendColor.getHex()),this.blendAlpha!==0&&(r.blendAlpha=this.blendAlpha),this.depthFunc!==to&&(r.depthFunc=this.depthFunc),this.depthTest===!1&&(r.depthTest=this.depthTest),this.depthWrite===!1&&(r.depthWrite=this.depthWrite),this.colorWrite===!1&&(r.colorWrite=this.colorWrite),this.stencilWriteMask!==255&&(r.stencilWriteMask=this.stencilWriteMask),this.stencilFunc!==av&&(r.stencilFunc=this.stencilFunc),this.stencilRef!==0&&(r.stencilRef=this.stencilRef),this.stencilFuncMask!==255&&(r.stencilFuncMask=this.stencilFuncMask),this.stencilFail!==Us&&(r.stencilFail=this.stencilFail),this.stencilZFail!==Us&&(r.stencilZFail=this.stencilZFail),this.stencilZPass!==Us&&(r.stencilZPass=this.stencilZPass),this.stencilWrite===!0&&(r.stencilWrite=this.stencilWrite),this.rotation!==void 0&&this.rotation!==0&&(r.rotation=this.rotation),this.polygonOffset===!0&&(r.polygonOffset=!0),this.polygonOffsetFactor!==0&&(r.polygonOffsetFactor=this.polygonOffsetFactor),this.polygonOffsetUnits!==0&&(r.polygonOffsetUnits=this.polygonOffsetUnits),this.linewidth!==void 0&&this.linewidth!==1&&(r.linewidth=this.linewidth),this.dashSize!==void 0&&(r.dashSize=this.dashSize),this.gapSize!==void 0&&(r.gapSize=this.gapSize),this.scale!==void 0&&(r.scale=this.scale),this.dithering===!0&&(r.dithering=!0),this.alphaTest>0&&(r.alphaTest=this.alphaTest),this.alphaHash===!0&&(r.alphaHash=!0),this.alphaToCoverage===!0&&(r.alphaToCoverage=!0),this.premultipliedAlpha===!0&&(r.premultipliedAlpha=!0),this.forceSinglePass===!0&&(r.forceSinglePass=!0),this.allowOverride===!1&&(r.allowOverride=!1),this.wireframe===!0&&(r.wireframe=!0),this.wireframeLinewidth>1&&(r.wireframeLinewidth=this.wireframeLinewidth),this.wireframeLinecap!=="round"&&(r.wireframeLinecap=this.wireframeLinecap),this.wireframeLinejoin!=="round"&&(r.wireframeLinejoin=this.wireframeLinejoin),this.flatShading===!0&&(r.flatShading=!0),this.visible===!1&&(r.visible=!1),this.toneMapped===!1&&(r.toneMapped=!1),this.fog===!1&&(r.fog=!1),Object.keys(this.userData).length>0&&(r.userData=this.userData);function l(c){const f=[];for(const h in c){const m=c[h];delete m.metadata,f.push(m)}return f}if(i){const c=l(t.textures),f=l(t.images);c.length>0&&(r.textures=c),f.length>0&&(r.images=f)}return r}fromJSON(t,i){if(t.uuid!==void 0&&(this.uuid=t.uuid),t.name!==void 0&&(this.name=t.name),t.color!==void 0&&this.color!==void 0&&this.color.setHex(t.color),t.roughness!==void 0&&(this.roughness=t.roughness),t.metalness!==void 0&&(this.metalness=t.metalness),t.sheen!==void 0&&(this.sheen=t.sheen),t.sheenColor!==void 0&&(this.sheenColor=new me().setHex(t.sheenColor)),t.sheenRoughness!==void 0&&(this.sheenRoughness=t.sheenRoughness),t.emissive!==void 0&&this.emissive!==void 0&&this.emissive.setHex(t.emissive),t.specular!==void 0&&this.specular!==void 0&&this.specular.setHex(t.specular),t.specularIntensity!==void 0&&(this.specularIntensity=t.specularIntensity),t.specularColor!==void 0&&this.specularColor!==void 0&&this.specularColor.setHex(t.specularColor),t.shininess!==void 0&&(this.shininess=t.shininess),t.clearcoat!==void 0&&(this.clearcoat=t.clearcoat),t.clearcoatRoughness!==void 0&&(this.clearcoatRoughness=t.clearcoatRoughness),t.dispersion!==void 0&&(this.dispersion=t.dispersion),t.iridescence!==void 0&&(this.iridescence=t.iridescence),t.iridescenceIOR!==void 0&&(this.iridescenceIOR=t.iridescenceIOR),t.iridescenceThicknessRange!==void 0&&(this.iridescenceThicknessRange=t.iridescenceThicknessRange),t.transmission!==void 0&&(this.transmission=t.transmission),t.thickness!==void 0&&(this.thickness=t.thickness),t.attenuationDistance!==void 0&&(this.attenuationDistance=t.attenuationDistance),t.attenuationColor!==void 0&&this.attenuationColor!==void 0&&this.attenuationColor.setHex(t.attenuationColor),t.anisotropy!==void 0&&(this.anisotropy=t.anisotropy),t.anisotropyRotation!==void 0&&(this.anisotropyRotation=t.anisotropyRotation),t.fog!==void 0&&(this.fog=t.fog),t.flatShading!==void 0&&(this.flatShading=t.flatShading),t.blending!==void 0&&(this.blending=t.blending),t.combine!==void 0&&(this.combine=t.combine),t.side!==void 0&&(this.side=t.side),t.shadowSide!==void 0&&(this.shadowSide=t.shadowSide),t.opacity!==void 0&&(this.opacity=t.opacity),t.transparent!==void 0&&(this.transparent=t.transparent),t.alphaTest!==void 0&&(this.alphaTest=t.alphaTest),t.alphaHash!==void 0&&(this.alphaHash=t.alphaHash),t.depthFunc!==void 0&&(this.depthFunc=t.depthFunc),t.depthTest!==void 0&&(this.depthTest=t.depthTest),t.depthWrite!==void 0&&(this.depthWrite=t.depthWrite),t.colorWrite!==void 0&&(this.colorWrite=t.colorWrite),t.blendSrc!==void 0&&(this.blendSrc=t.blendSrc),t.blendDst!==void 0&&(this.blendDst=t.blendDst),t.blendEquation!==void 0&&(this.blendEquation=t.blendEquation),t.blendSrcAlpha!==void 0&&(this.blendSrcAlpha=t.blendSrcAlpha),t.blendDstAlpha!==void 0&&(this.blendDstAlpha=t.blendDstAlpha),t.blendEquationAlpha!==void 0&&(this.blendEquationAlpha=t.blendEquationAlpha),t.blendColor!==void 0&&this.blendColor!==void 0&&this.blendColor.setHex(t.blendColor),t.blendAlpha!==void 0&&(this.blendAlpha=t.blendAlpha),t.stencilWriteMask!==void 0&&(this.stencilWriteMask=t.stencilWriteMask),t.stencilFunc!==void 0&&(this.stencilFunc=t.stencilFunc),t.stencilRef!==void 0&&(this.stencilRef=t.stencilRef),t.stencilFuncMask!==void 0&&(this.stencilFuncMask=t.stencilFuncMask),t.stencilFail!==void 0&&(this.stencilFail=t.stencilFail),t.stencilZFail!==void 0&&(this.stencilZFail=t.stencilZFail),t.stencilZPass!==void 0&&(this.stencilZPass=t.stencilZPass),t.stencilWrite!==void 0&&(this.stencilWrite=t.stencilWrite),t.wireframe!==void 0&&(this.wireframe=t.wireframe),t.wireframeLinewidth!==void 0&&(this.wireframeLinewidth=t.wireframeLinewidth),t.wireframeLinecap!==void 0&&(this.wireframeLinecap=t.wireframeLinecap),t.wireframeLinejoin!==void 0&&(this.wireframeLinejoin=t.wireframeLinejoin),t.rotation!==void 0&&(this.rotation=t.rotation),t.linewidth!==void 0&&(this.linewidth=t.linewidth),t.dashSize!==void 0&&(this.dashSize=t.dashSize),t.gapSize!==void 0&&(this.gapSize=t.gapSize),t.scale!==void 0&&(this.scale=t.scale),t.polygonOffset!==void 0&&(this.polygonOffset=t.polygonOffset),t.polygonOffsetFactor!==void 0&&(this.polygonOffsetFactor=t.polygonOffsetFactor),t.polygonOffsetUnits!==void 0&&(this.polygonOffsetUnits=t.polygonOffsetUnits),t.dithering!==void 0&&(this.dithering=t.dithering),t.alphaToCoverage!==void 0&&(this.alphaToCoverage=t.alphaToCoverage),t.premultipliedAlpha!==void 0&&(this.premultipliedAlpha=t.premultipliedAlpha),t.forceSinglePass!==void 0&&(this.forceSinglePass=t.forceSinglePass),t.allowOverride!==void 0&&(this.allowOverride=t.allowOverride),t.visible!==void 0&&(this.visible=t.visible),t.toneMapped!==void 0&&(this.toneMapped=t.toneMapped),t.userData!==void 0&&(this.userData=t.userData),t.vertexColors!==void 0&&(typeof t.vertexColors=="number"?this.vertexColors=t.vertexColors>0:this.vertexColors=t.vertexColors),t.size!==void 0&&(this.size=t.size),t.sizeAttenuation!==void 0&&(this.sizeAttenuation=t.sizeAttenuation),t.map!==void 0&&(this.map=i[t.map]||null),t.matcap!==void 0&&(this.matcap=i[t.matcap]||null),t.alphaMap!==void 0&&(this.alphaMap=i[t.alphaMap]||null),t.bumpMap!==void 0&&(this.bumpMap=i[t.bumpMap]||null),t.bumpScale!==void 0&&(this.bumpScale=t.bumpScale),t.normalMap!==void 0&&(this.normalMap=i[t.normalMap]||null),t.normalMapType!==void 0&&(this.normalMapType=t.normalMapType),t.normalScale!==void 0){let r=t.normalScale;Array.isArray(r)===!1&&(r=[r,r]),this.normalScale=new Ae().fromArray(r)}return t.displacementMap!==void 0&&(this.displacementMap=i[t.displacementMap]||null),t.displacementScale!==void 0&&(this.displacementScale=t.displacementScale),t.displacementBias!==void 0&&(this.displacementBias=t.displacementBias),t.roughnessMap!==void 0&&(this.roughnessMap=i[t.roughnessMap]||null),t.metalnessMap!==void 0&&(this.metalnessMap=i[t.metalnessMap]||null),t.emissiveMap!==void 0&&(this.emissiveMap=i[t.emissiveMap]||null),t.emissiveIntensity!==void 0&&(this.emissiveIntensity=t.emissiveIntensity),t.specularMap!==void 0&&(this.specularMap=i[t.specularMap]||null),t.specularIntensityMap!==void 0&&(this.specularIntensityMap=i[t.specularIntensityMap]||null),t.specularColorMap!==void 0&&(this.specularColorMap=i[t.specularColorMap]||null),t.envMap!==void 0&&(this.envMap=i[t.envMap]||null),t.envMapRotation!==void 0&&this.envMapRotation.fromArray(t.envMapRotation),t.envMapIntensity!==void 0&&(this.envMapIntensity=t.envMapIntensity),t.reflectivity!==void 0&&(this.reflectivity=t.reflectivity),t.refractionRatio!==void 0&&(this.refractionRatio=t.refractionRatio),t.lightMap!==void 0&&(this.lightMap=i[t.lightMap]||null),t.lightMapIntensity!==void 0&&(this.lightMapIntensity=t.lightMapIntensity),t.aoMap!==void 0&&(this.aoMap=i[t.aoMap]||null),t.aoMapIntensity!==void 0&&(this.aoMapIntensity=t.aoMapIntensity),t.gradientMap!==void 0&&(this.gradientMap=i[t.gradientMap]||null),t.clearcoatMap!==void 0&&(this.clearcoatMap=i[t.clearcoatMap]||null),t.clearcoatRoughnessMap!==void 0&&(this.clearcoatRoughnessMap=i[t.clearcoatRoughnessMap]||null),t.clearcoatNormalMap!==void 0&&(this.clearcoatNormalMap=i[t.clearcoatNormalMap]||null),t.clearcoatNormalScale!==void 0&&(this.clearcoatNormalScale=new Ae().fromArray(t.clearcoatNormalScale)),t.iridescenceMap!==void 0&&(this.iridescenceMap=i[t.iridescenceMap]||null),t.iridescenceThicknessMap!==void 0&&(this.iridescenceThicknessMap=i[t.iridescenceThicknessMap]||null),t.transmissionMap!==void 0&&(this.transmissionMap=i[t.transmissionMap]||null),t.thicknessMap!==void 0&&(this.thicknessMap=i[t.thicknessMap]||null),t.anisotropyMap!==void 0&&(this.anisotropyMap=i[t.anisotropyMap]||null),t.sheenColorMap!==void 0&&(this.sheenColorMap=i[t.sheenColorMap]||null),t.sheenRoughnessMap!==void 0&&(this.sheenRoughnessMap=i[t.sheenRoughnessMap]||null),this}clone(){return new this.constructor().copy(this)}copy(t){this.name=t.name,this.blending=t.blending,this.side=t.side,this.vertexColors=t.vertexColors,this.opacity=t.opacity,this.transparent=t.transparent,this.blendSrc=t.blendSrc,this.blendDst=t.blendDst,this.blendEquation=t.blendEquation,this.blendSrcAlpha=t.blendSrcAlpha,this.blendDstAlpha=t.blendDstAlpha,this.blendEquationAlpha=t.blendEquationAlpha,this.blendColor.copy(t.blendColor),this.blendAlpha=t.blendAlpha,this.depthFunc=t.depthFunc,this.depthTest=t.depthTest,this.depthWrite=t.depthWrite,this.stencilWriteMask=t.stencilWriteMask,this.stencilFunc=t.stencilFunc,this.stencilRef=t.stencilRef,this.stencilFuncMask=t.stencilFuncMask,this.stencilFail=t.stencilFail,this.stencilZFail=t.stencilZFail,this.stencilZPass=t.stencilZPass,this.stencilWrite=t.stencilWrite;const i=t.clippingPlanes;let r=null;if(i!==null){const l=i.length;r=new Array(l);for(let c=0;c!==l;++c)r[c]=i[c].clone()}return this.clippingPlanes=r,this.clipIntersection=t.clipIntersection,this.clipShadows=t.clipShadows,this.shadowSide=t.shadowSide,this.colorWrite=t.colorWrite,this.precision=t.precision,this.polygonOffset=t.polygonOffset,this.polygonOffsetFactor=t.polygonOffsetFactor,this.polygonOffsetUnits=t.polygonOffsetUnits,this.dithering=t.dithering,this.alphaTest=t.alphaTest,this.alphaHash=t.alphaHash,this.alphaToCoverage=t.alphaToCoverage,this.premultipliedAlpha=t.premultipliedAlpha,this.forceSinglePass=t.forceSinglePass,this.allowOverride=t.allowOverride,this.visible=t.visible,this.toneMapped=t.toneMapped,this.userData=JSON.parse(JSON.stringify(t.userData)),this}dispose(){this.dispatchEvent({type:"dispose"})}set needsUpdate(t){t===!0&&this.version++}}const ba=new Q,nh=new Q,zc=new Q,cr=new Q,ih=new Q,Fc=new Q,ah=new Q;class yu{constructor(t=new Q,i=new Q(0,0,-1)){this.origin=t,this.direction=i}set(t,i){return this.origin.copy(t),this.direction.copy(i),this}copy(t){return this.origin.copy(t.origin),this.direction.copy(t.direction),this}at(t,i){return i.copy(this.origin).addScaledVector(this.direction,t)}lookAt(t){return this.direction.copy(t).sub(this.origin).normalize(),this}recast(t){return this.origin.copy(this.at(t,ba)),this}closestPointToPoint(t,i){i.subVectors(t,this.origin);const r=i.dot(this.direction);return r<0?i.copy(this.origin):i.copy(this.origin).addScaledVector(this.direction,r)}distanceToPoint(t){return Math.sqrt(this.distanceSqToPoint(t))}distanceSqToPoint(t){const i=ba.subVectors(t,this.origin).dot(this.direction);return i<0?this.origin.distanceToSquared(t):(ba.copy(this.origin).addScaledVector(this.direction,i),ba.distanceToSquared(t))}distanceSqToSegment(t,i,r,l){nh.copy(t).add(i).multiplyScalar(.5),zc.copy(i).sub(t).normalize(),cr.copy(this.origin).sub(nh);const c=t.distanceTo(i)*.5,f=-this.direction.dot(zc),h=cr.dot(this.direction),m=-cr.dot(zc),p=cr.lengthSq(),_=Math.abs(1-f*f);let v,g,x,M;if(_>0)if(v=f*m-h,g=f*h-m,M=c*_,v>=0)if(g>=-M)if(g<=M){const A=1/_;v*=A,g*=A,x=v*(v+f*g+2*h)+g*(f*v+g+2*m)+p}else g=c,v=Math.max(0,-(f*g+h)),x=-v*v+g*(g+2*m)+p;else g=-c,v=Math.max(0,-(f*g+h)),x=-v*v+g*(g+2*m)+p;else g<=-M?(v=Math.max(0,-(-f*c+h)),g=v>0?-c:Math.min(Math.max(-c,-m),c),x=-v*v+g*(g+2*m)+p):g<=M?(v=0,g=Math.min(Math.max(-c,-m),c),x=g*(g+2*m)+p):(v=Math.max(0,-(f*c+h)),g=v>0?c:Math.min(Math.max(-c,-m),c),x=-v*v+g*(g+2*m)+p);else g=f>0?-c:c,v=Math.max(0,-(f*g+h)),x=-v*v+g*(g+2*m)+p;return r&&r.copy(this.origin).addScaledVector(this.direction,v),l&&l.copy(nh).addScaledVector(zc,g),x}intersectSphere(t,i){ba.subVectors(t.center,this.origin);const r=ba.dot(this.direction),l=ba.dot(ba)-r*r,c=t.radius*t.radius;if(l>c)return null;const f=Math.sqrt(c-l),h=r-f,m=r+f;return m<0?null:h<0?this.at(m,i):this.at(h,i)}intersectsSphere(t){return t.radius<0?!1:this.distanceSqToPoint(t.center)<=t.radius*t.radius}distanceToPlane(t){const i=t.normal.dot(this.direction);if(i===0)return t.distanceToPoint(this.origin)===0?0:null;const r=-(this.origin.dot(t.normal)+t.constant)/i;return r>=0?r:null}intersectPlane(t,i){const r=this.distanceToPlane(t);return r===null?null:this.at(r,i)}intersectsPlane(t){const i=t.distanceToPoint(this.origin);return i===0||t.normal.dot(this.direction)*i<0}intersectBox(t,i){let r,l,c,f,h,m;const p=1/this.direction.x,_=1/this.direction.y,v=1/this.direction.z,g=this.origin;return p>=0?(r=(t.min.x-g.x)*p,l=(t.max.x-g.x)*p):(r=(t.max.x-g.x)*p,l=(t.min.x-g.x)*p),_>=0?(c=(t.min.y-g.y)*_,f=(t.max.y-g.y)*_):(c=(t.max.y-g.y)*_,f=(t.min.y-g.y)*_),r>f||c>l||((c>r||isNaN(r))&&(r=c),(f<l||isNaN(l))&&(l=f),v>=0?(h=(t.min.z-g.z)*v,m=(t.max.z-g.z)*v):(h=(t.max.z-g.z)*v,m=(t.min.z-g.z)*v),r>m||h>l)||((h>r||r!==r)&&(r=h),(m<l||l!==l)&&(l=m),l<0)?null:this.at(r>=0?r:l,i)}intersectsBox(t){return this.intersectBox(t,ba)!==null}intersectTriangle(t,i,r,l,c){ih.subVectors(i,t),Fc.subVectors(r,t),ah.crossVectors(ih,Fc);let f=this.direction.dot(ah),h;if(f>0){if(l)return null;h=1}else if(f<0)h=-1,f=-f;else return null;cr.subVectors(this.origin,t);const m=h*this.direction.dot(Fc.crossVectors(cr,Fc));if(m<0)return null;const p=h*this.direction.dot(ih.cross(cr));if(p<0||m+p>f)return null;const _=-h*cr.dot(ah);return _<0?null:this.at(_/f,c)}applyMatrix4(t){return this.origin.applyMatrix4(t),this.direction.transformDirection(t),this}equals(t){return t.origin.equals(this.origin)&&t.direction.equals(this.direction)}clone(){return new this.constructor().copy(this)}}class Ta extends Qr{constructor(t){super(),this.isMeshBasicMaterial=!0,this.type="MeshBasicMaterial",this.color=new me(16777215),this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.specularMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new mr,this.combine=Wx,this.reflectivity=1,this.refractionRatio=.98,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.lightMap=t.lightMap,this.lightMapIntensity=t.lightMapIntensity,this.aoMap=t.aoMap,this.aoMapIntensity=t.aoMapIntensity,this.specularMap=t.specularMap,this.alphaMap=t.alphaMap,this.envMap=t.envMap,this.envMapRotation.copy(t.envMapRotation),this.combine=t.combine,this.reflectivity=t.reflectivity,this.refractionRatio=t.refractionRatio,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.wireframeLinecap=t.wireframeLinecap,this.wireframeLinejoin=t.wireframeLinejoin,this.fog=t.fog,this}}const yv=new $e,zr=new yu,Hc=new vl,Mv=new Q,Gc=new Q,Vc=new Q,kc=new Q,rh=new Q,Xc=new Q,Ev=new Q,qc=new Q;class Ye extends dn{constructor(t=new bn,i=new Ta){super(),this.isMesh=!0,this.type="Mesh",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.count=1,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),t.morphTargetInfluences!==void 0&&(this.morphTargetInfluences=t.morphTargetInfluences.slice()),t.morphTargetDictionary!==void 0&&(this.morphTargetDictionary=Object.assign({},t.morphTargetDictionary)),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}updateMorphTargets(){const i=this.geometry.morphAttributes,r=Object.keys(i);if(r.length>0){const l=i[r[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const h=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[h]=c}}}}getVertexPosition(t,i){const r=this.geometry,l=r.attributes.position,c=r.morphAttributes.position,f=r.morphTargetsRelative;i.fromBufferAttribute(l,t);const h=this.morphTargetInfluences;if(c&&h){Xc.set(0,0,0);for(let m=0,p=c.length;m<p;m++){const _=h[m],v=c[m];_!==0&&(rh.fromBufferAttribute(v,t),f?Xc.addScaledVector(rh,_):Xc.addScaledVector(rh.sub(i),_))}i.add(Xc)}return i}raycast(t,i){const r=this.geometry,l=this.material,c=this.matrixWorld;l!==void 0&&(r.boundingSphere===null&&r.computeBoundingSphere(),Hc.copy(r.boundingSphere),Hc.applyMatrix4(c),zr.copy(t.ray).recast(t.near),!(Hc.containsPoint(zr.origin)===!1&&(zr.intersectSphere(Hc,Mv)===null||zr.origin.distanceToSquared(Mv)>(t.far-t.near)**2))&&(yv.copy(c).invert(),zr.copy(t.ray).applyMatrix4(yv),!(r.boundingBox!==null&&zr.intersectsBox(r.boundingBox)===!1)&&this._computeIntersections(t,i,zr)))}_computeIntersections(t,i,r){let l;const c=this.geometry,f=this.material,h=c.index,m=c.attributes.position,p=c.attributes.uv,_=c.attributes.uv1,v=c.attributes.normal,g=c.groups,x=c.drawRange;if(h!==null)if(Array.isArray(f))for(let M=0,A=g.length;M<A;M++){const E=g[M],S=f[E.materialIndex],P=Math.max(E.start,x.start),O=Math.min(h.count,Math.min(E.start+E.count,x.start+x.count));for(let C=P,z=O;C<z;C+=3){const D=h.getX(C),U=h.getX(C+1),b=h.getX(C+2);l=Wc(this,S,t,r,p,_,v,D,U,b),l&&(l.faceIndex=Math.floor(C/3),l.face.materialIndex=E.materialIndex,i.push(l))}}else{const M=Math.max(0,x.start),A=Math.min(h.count,x.start+x.count);for(let E=M,S=A;E<S;E+=3){const P=h.getX(E),O=h.getX(E+1),C=h.getX(E+2);l=Wc(this,f,t,r,p,_,v,P,O,C),l&&(l.faceIndex=Math.floor(E/3),i.push(l))}}else if(m!==void 0)if(Array.isArray(f))for(let M=0,A=g.length;M<A;M++){const E=g[M],S=f[E.materialIndex],P=Math.max(E.start,x.start),O=Math.min(m.count,Math.min(E.start+E.count,x.start+x.count));for(let C=P,z=O;C<z;C+=3){const D=C,U=C+1,b=C+2;l=Wc(this,S,t,r,p,_,v,D,U,b),l&&(l.faceIndex=Math.floor(C/3),l.face.materialIndex=E.materialIndex,i.push(l))}}else{const M=Math.max(0,x.start),A=Math.min(m.count,x.start+x.count);for(let E=M,S=A;E<S;E+=3){const P=E,O=E+1,C=E+2;l=Wc(this,f,t,r,p,_,v,P,O,C),l&&(l.faceIndex=Math.floor(E/3),i.push(l))}}}}function nb(s,t,i,r,l,c,f,h){let m;if(t.side===Jn?m=r.intersectTriangle(f,c,l,!0,h):m=r.intersectTriangle(l,c,f,t.side===pr,h),m===null)return null;qc.copy(h),qc.applyMatrix4(s.matrixWorld);const p=i.ray.origin.distanceTo(qc);return p<i.near||p>i.far?null:{distance:p,point:qc.clone(),object:s}}function Wc(s,t,i,r,l,c,f,h,m,p){s.getVertexPosition(h,Gc),s.getVertexPosition(m,Vc),s.getVertexPosition(p,kc);const _=nb(s,t,i,r,Gc,Vc,kc,Ev);if(_){const v=new Q;bi.getBarycoord(Ev,Gc,Vc,kc,v),l&&(_.uv=bi.getInterpolatedAttribute(l,h,m,p,v,new Ae)),c&&(_.uv1=bi.getInterpolatedAttribute(c,h,m,p,v,new Ae)),f&&(_.normal=bi.getInterpolatedAttribute(f,h,m,p,v,new Q),_.normal.dot(r.direction)>0&&_.normal.multiplyScalar(-1));const g={a:h,b:m,c:p,normal:new Q,materialIndex:0};bi.getNormal(Gc,Vc,kc,g.normal),_.face=g,_.barycoord=v}return _}class ib extends Bn{constructor(t=null,i=1,r=1,l,c,f,h,m,p=Pn,_=Pn,v,g){super(null,f,h,m,p,_,l,c,v,g),this.isDataTexture=!0,this.image={data:t,width:i,height:r},this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}const sh=new Q,ab=new Q,rb=new ce;class Hr{constructor(t=new Q(1,0,0),i=0){this.isPlane=!0,this.normal=t,this.constant=i}set(t,i){return this.normal.copy(t),this.constant=i,this}setComponents(t,i,r,l){return this.normal.set(t,i,r),this.constant=l,this}setFromNormalAndCoplanarPoint(t,i){return this.normal.copy(t),this.constant=-i.dot(this.normal),this}setFromCoplanarPoints(t,i,r){const l=sh.subVectors(r,i).cross(ab.subVectors(t,i)).normalize();return this.setFromNormalAndCoplanarPoint(l,t),this}copy(t){return this.normal.copy(t.normal),this.constant=t.constant,this}normalize(){const t=1/this.normal.length();return this.normal.multiplyScalar(t),this.constant*=t,this}negate(){return this.constant*=-1,this.normal.negate(),this}distanceToPoint(t){return this.normal.dot(t)+this.constant}distanceToSphere(t){return this.distanceToPoint(t.center)-t.radius}projectPoint(t,i){return i.copy(t).addScaledVector(this.normal,-this.distanceToPoint(t))}intersectLine(t,i,r=!0){const l=t.delta(sh),c=this.normal.dot(l);if(c===0)return this.distanceToPoint(t.start)===0?i.copy(t.start):null;const f=-(t.start.dot(this.normal)+this.constant)/c;return r===!0&&(f<0||f>1)?null:i.copy(t.start).addScaledVector(l,f)}intersectsLine(t){const i=this.distanceToPoint(t.start),r=this.distanceToPoint(t.end);return i<0&&r>0||r<0&&i>0}intersectsBox(t){return t.intersectsPlane(this)}intersectsSphere(t){return t.intersectsPlane(this)}coplanarPoint(t){return t.copy(this.normal).multiplyScalar(-this.constant)}applyMatrix4(t,i){const r=i||rb.getNormalMatrix(t),l=this.coplanarPoint(sh).applyMatrix4(t),c=this.normal.applyMatrix3(r).normalize();return this.constant=-l.dot(c),this}translate(t){return this.constant-=t.dot(this.normal),this}equals(t){return t.normal.equals(this.normal)&&t.constant===this.constant}clone(){return new this.constructor().copy(this)}}const Fr=new vl,sb=new Ae(.5,.5),Yc=new Q;class jp{constructor(t=new Hr,i=new Hr,r=new Hr,l=new Hr,c=new Hr,f=new Hr){this.planes=[t,i,r,l,c,f]}set(t,i,r,l,c,f){const h=this.planes;return h[0].copy(t),h[1].copy(i),h[2].copy(r),h[3].copy(l),h[4].copy(c),h[5].copy(f),this}copy(t){const i=this.planes;for(let r=0;r<6;r++)i[r].copy(t.planes[r]);return this}setFromProjectionMatrix(t,i=ji,r=!1){const l=this.planes,c=t.elements,f=c[0],h=c[1],m=c[2],p=c[3],_=c[4],v=c[5],g=c[6],x=c[7],M=c[8],A=c[9],E=c[10],S=c[11],P=c[12],O=c[13],C=c[14],z=c[15];if(l[0].setComponents(p-f,x-_,S-M,z-P).normalize(),l[1].setComponents(p+f,x+_,S+M,z+P).normalize(),l[2].setComponents(p+h,x+v,S+A,z+O).normalize(),l[3].setComponents(p-h,x-v,S-A,z-O).normalize(),r)l[4].setComponents(m,g,E,C).normalize(),l[5].setComponents(p-m,x-g,S-E,z-C).normalize();else if(l[4].setComponents(p-m,x-g,S-E,z-C).normalize(),i===ji)l[5].setComponents(p+m,x+g,S+E,z+C).normalize();else if(i===pl)l[5].setComponents(m,g,E,C).normalize();else throw new Error("THREE.Frustum.setFromProjectionMatrix(): Invalid coordinate system: "+i);return this}intersectsObject(t){if(t.boundingSphere!==void 0)t.boundingSphere===null&&t.computeBoundingSphere(),Fr.copy(t.boundingSphere).applyMatrix4(t.matrixWorld);else{const i=t.geometry;i.boundingSphere===null&&i.computeBoundingSphere(),Fr.copy(i.boundingSphere).applyMatrix4(t.matrixWorld)}return this.intersectsSphere(Fr)}intersectsSprite(t){Fr.center.set(0,0,0);const i=sb.distanceTo(t.center);return Fr.radius=.7071067811865476+i,Fr.applyMatrix4(t.matrixWorld),this.intersectsSphere(Fr)}intersectsSphere(t){const i=this.planes,r=t.center,l=-t.radius;for(let c=0;c<6;c++)if(i[c].distanceToPoint(r)<l)return!1;return!0}intersectsBox(t){const i=this.planes;for(let r=0;r<6;r++){const l=i[r];if(Yc.x=l.normal.x>0?t.max.x:t.min.x,Yc.y=l.normal.y>0?t.max.y:t.min.y,Yc.z=l.normal.z>0?t.max.z:t.min.z,l.distanceToPoint(Yc)<0)return!1}return!0}containsPoint(t){const i=this.planes;for(let r=0;r<6;r++)if(i[r].distanceToPoint(t)<0)return!1;return!0}clone(){return new this.constructor().copy(this)}}class Zr extends Qr{constructor(t){super(),this.isLineBasicMaterial=!0,this.type="LineBasicMaterial",this.color=new me(16777215),this.map=null,this.linewidth=1,this.linecap="round",this.linejoin="round",this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.linewidth=t.linewidth,this.linecap=t.linecap,this.linejoin=t.linejoin,this.fog=t.fog,this}}const gu=new Q,_u=new Q,bv=new $e,il=new yu,Zc=new vl,oh=new Q,Tv=new Q;class Kp extends dn{constructor(t=new bn,i=new Zr){super(),this.isLine=!0,this.type="Line",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}computeLineDistances(){const t=this.geometry;if(t.index===null){const i=t.attributes.position,r=[0];for(let l=1,c=i.count;l<c;l++)gu.fromBufferAttribute(i,l-1),_u.fromBufferAttribute(i,l),r[l]=r[l-1],r[l]+=gu.distanceTo(_u);t.setAttribute("lineDistance",new $n(r,1))}else se("Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}raycast(t,i){const r=this.geometry,l=this.matrixWorld,c=t.params.Line.threshold,f=r.drawRange;if(r.boundingSphere===null&&r.computeBoundingSphere(),Zc.copy(r.boundingSphere),Zc.applyMatrix4(l),Zc.radius+=c,t.ray.intersectsSphere(Zc)===!1)return;bv.copy(l).invert(),il.copy(t.ray).applyMatrix4(bv);const h=c/((this.scale.x+this.scale.y+this.scale.z)/3),m=h*h,p=this.isLineSegments?2:1,_=r.index,g=r.attributes.position;if(_!==null){const x=Math.max(0,f.start),M=Math.min(_.count,f.start+f.count);for(let A=x,E=M-1;A<E;A+=p){const S=_.getX(A),P=_.getX(A+1),O=jc(this,t,il,m,S,P,A);O&&i.push(O)}if(this.isLineLoop){const A=_.getX(M-1),E=_.getX(x),S=jc(this,t,il,m,A,E,M-1);S&&i.push(S)}}else{const x=Math.max(0,f.start),M=Math.min(g.count,f.start+f.count);for(let A=x,E=M-1;A<E;A+=p){const S=jc(this,t,il,m,A,A+1,A);S&&i.push(S)}if(this.isLineLoop){const A=jc(this,t,il,m,M-1,x,M-1);A&&i.push(A)}}}updateMorphTargets(){const i=this.geometry.morphAttributes,r=Object.keys(i);if(r.length>0){const l=i[r[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const h=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[h]=c}}}}}function jc(s,t,i,r,l,c,f){const h=s.geometry.attributes.position;if(gu.fromBufferAttribute(h,l),_u.fromBufferAttribute(h,c),i.distanceSqToSegment(gu,_u,oh,Tv)>r)return;oh.applyMatrix4(s.matrixWorld);const p=t.ray.origin.distanceTo(oh);if(!(p<t.near||p>t.far))return{distance:p,point:Tv.clone().applyMatrix4(s.matrixWorld),index:f,face:null,faceIndex:null,barycoord:null,object:s}}const Av=new Q,Rv=new Q;class dS extends Kp{constructor(t,i){super(t,i),this.isLineSegments=!0,this.type="LineSegments"}computeLineDistances(){const t=this.geometry;if(t.index===null){const i=t.attributes.position,r=[];for(let l=0,c=i.count;l<c;l+=2)Av.fromBufferAttribute(i,l),Rv.fromBufferAttribute(i,l+1),r[l]=l===0?0:r[l-1],r[l+1]=r[l]+Av.distanceTo(Rv);t.setAttribute("lineDistance",new $n(r,1))}else se("LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}}class Cp extends Kp{constructor(t,i){super(t,i),this.isLineLoop=!0,this.type="LineLoop"}}class wp extends Qr{constructor(t){super(),this.isPointsMaterial=!0,this.type="PointsMaterial",this.color=new me(16777215),this.map=null,this.alphaMap=null,this.size=1,this.sizeAttenuation=!0,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.color.copy(t.color),this.map=t.map,this.alphaMap=t.alphaMap,this.size=t.size,this.sizeAttenuation=t.sizeAttenuation,this.fog=t.fog,this}}const Cv=new $e,Dp=new yu,Kc=new vl,Qc=new Q;class wv extends dn{constructor(t=new bn,i=new wp){super(),this.isPoints=!0,this.type="Points",this.geometry=t,this.material=i,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(t,i){return super.copy(t,i),this.material=Array.isArray(t.material)?t.material.slice():t.material,this.geometry=t.geometry,this}raycast(t,i){const r=this.geometry,l=this.matrixWorld,c=t.params.Points.threshold,f=r.drawRange;if(r.boundingSphere===null&&r.computeBoundingSphere(),Kc.copy(r.boundingSphere),Kc.applyMatrix4(l),Kc.radius+=c,t.ray.intersectsSphere(Kc)===!1)return;Cv.copy(l).invert(),Dp.copy(t.ray).applyMatrix4(Cv);const h=c/((this.scale.x+this.scale.y+this.scale.z)/3),m=h*h,p=r.index,v=r.attributes.position;if(p!==null){const g=Math.max(0,f.start),x=Math.min(p.count,f.start+f.count);for(let M=g,A=x;M<A;M++){const E=p.getX(M);Qc.fromBufferAttribute(v,E),Dv(Qc,E,m,l,t,i,this)}}else{const g=Math.max(0,f.start),x=Math.min(v.count,f.start+f.count);for(let M=g,A=x;M<A;M++)Qc.fromBufferAttribute(v,M),Dv(Qc,M,m,l,t,i,this)}}updateMorphTargets(){const i=this.geometry.morphAttributes,r=Object.keys(i);if(r.length>0){const l=i[r[0]];if(l!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let c=0,f=l.length;c<f;c++){const h=l[c].name||String(c);this.morphTargetInfluences.push(0),this.morphTargetDictionary[h]=c}}}}}function Dv(s,t,i,r,l,c,f){const h=Dp.distanceSqToPoint(s);if(h<i){const m=new Q;Dp.closestPointToPoint(s,m),m.applyMatrix4(r);const p=l.ray.origin.distanceTo(m);if(p<l.near||p>l.far)return;c.push({distance:p,distanceToRay:Math.sqrt(h),point:m,index:t,face:null,faceIndex:null,barycoord:null,object:f})}}class hS extends Bn{constructor(t=[],i=Wr,r,l,c,f,h,m,p,_){super(t,i,r,l,c,f,h,m,p,_),this.isCubeTexture=!0,this.flipY=!1}get images(){return this.image}set images(t){this.image=t}}class pS extends Bn{constructor(t,i,r,l,c,f,h,m,p){super(t,i,r,l,c,f,h,m,p),this.isCanvasTexture=!0,this.needsUpdate=!0}}class no extends Bn{constructor(t,i,r=$i,l,c,f,h=Pn,m=Pn,p,_=Na,v=1){if(_!==Na&&_!==qr)throw new Error("THREE.DepthTexture: format must be either THREE.DepthFormat or THREE.DepthStencilFormat");const g={width:t,height:i,depth:v};super(g,l,c,f,h,m,_,r,p),this.isDepthTexture=!0,this.flipY=!1,this.generateMipmaps=!1,this.compareFunction=null}copy(t){return super.copy(t),this.source=new Wp(Object.assign({},t.image)),this.compareFunction=t.compareFunction,this}toJSON(t){const i=super.toJSON(t);return this.compareFunction!==null&&(i.compareFunction=this.compareFunction),i}}class ob extends no{constructor(t,i=$i,r=Wr,l,c,f=Pn,h=Pn,m,p=Na){const _={width:t,height:t,depth:1},v=[_,_,_,_,_,_];super(t,t,i,r,l,c,f,h,m,p),this.image=v,this.isCubeDepthTexture=!0,this.isCubeTexture=!0}get images(){return this.image}set images(t){this.image=t}}class mS extends Bn{constructor(t=null){super(),this.sourceTexture=t,this.isExternalTexture=!0}copy(t){return super.copy(t),this.sourceTexture=t.sourceTexture,this}}class En extends bn{constructor(t=1,i=1,r=1,l=1,c=1,f=1){super(),this.type="BoxGeometry",this.parameters={width:t,height:i,depth:r,widthSegments:l,heightSegments:c,depthSegments:f};const h=this;l=Math.floor(l),c=Math.floor(c),f=Math.floor(f);const m=[],p=[],_=[],v=[];let g=0,x=0;M("z","y","x",-1,-1,r,i,t,f,c,0),M("z","y","x",1,-1,r,i,-t,f,c,1),M("x","z","y",1,1,t,r,i,l,f,2),M("x","z","y",1,-1,t,r,-i,l,f,3),M("x","y","z",1,-1,t,i,r,l,c,4),M("x","y","z",-1,-1,t,i,-r,l,c,5),this.setIndex(m),this.setAttribute("position",new $n(p,3)),this.setAttribute("normal",new $n(_,3)),this.setAttribute("uv",new $n(v,2));function M(A,E,S,P,O,C,z,D,U,b,N){const G=C/U,F=z/b,k=C/2,K=z/2,tt=D/2,$=U+1,I=b+1;let X=0,at=0;const _t=new Q;for(let vt=0;vt<I;vt++){const L=vt*F-K;for(let Y=0;Y<$;Y++){const yt=Y*G-k;_t[A]=yt*P,_t[E]=L*O,_t[S]=tt,p.push(_t.x,_t.y,_t.z),_t[A]=0,_t[E]=0,_t[S]=D>0?1:-1,_.push(_t.x,_t.y,_t.z),v.push(Y/U),v.push(1-vt/b),X+=1}}for(let vt=0;vt<b;vt++)for(let L=0;L<U;L++){const Y=g+L+$*vt,yt=g+L+$*(vt+1),At=g+(L+1)+$*(vt+1),Dt=g+(L+1)+$*vt;m.push(Y,yt,Dt),m.push(yt,At,Dt),at+=6}h.addGroup(x,at,N),x+=at,g+=X}}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new En(t.width,t.height,t.depth,t.widthSegments,t.heightSegments,t.depthSegments)}}const Jc=new Q,$c=new Q,lh=new Q,tu=new bi;class lb extends bn{constructor(t=null,i=1){if(super(),this.type="EdgesGeometry",this.parameters={geometry:t,thresholdAngle:i},t!==null){const l=Math.pow(10,4),c=Math.cos(Qs*i),f=t.getIndex(),h=t.getAttribute("position"),m=f?f.count:h.count,p=[0,0,0],_=["a","b","c"],v=new Array(3),g={},x=[];for(let M=0;M<m;M+=3){f?(p[0]=f.getX(M),p[1]=f.getX(M+1),p[2]=f.getX(M+2)):(p[0]=M,p[1]=M+1,p[2]=M+2);const{a:A,b:E,c:S}=tu;if(A.fromBufferAttribute(h,p[0]),E.fromBufferAttribute(h,p[1]),S.fromBufferAttribute(h,p[2]),tu.getNormal(lh),v[0]=`${Math.round(A.x*l)},${Math.round(A.y*l)},${Math.round(A.z*l)}`,v[1]=`${Math.round(E.x*l)},${Math.round(E.y*l)},${Math.round(E.z*l)}`,v[2]=`${Math.round(S.x*l)},${Math.round(S.y*l)},${Math.round(S.z*l)}`,!(v[0]===v[1]||v[1]===v[2]||v[2]===v[0]))for(let P=0;P<3;P++){const O=(P+1)%3,C=v[P],z=v[O],D=tu[_[P]],U=tu[_[O]],b=`${C}_${z}`,N=`${z}_${C}`;N in g&&g[N]?(lh.dot(g[N].normal)<=c&&(x.push(D.x,D.y,D.z),x.push(U.x,U.y,U.z)),g[N]=null):b in g||(g[b]={index0:p[P],index1:p[O],normal:lh.clone()})}}for(const M in g)if(g[M]){const{index0:A,index1:E}=g[M];Jc.fromBufferAttribute(h,A),$c.fromBufferAttribute(h,E),x.push(Jc.x,Jc.y,Jc.z),x.push($c.x,$c.y,$c.z)}this.setAttribute("position",new $n(x,3))}}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}}class Ji extends bn{constructor(t=1,i=1,r=1,l=1){super(),this.type="PlaneGeometry",this.parameters={width:t,height:i,widthSegments:r,heightSegments:l};const c=t/2,f=i/2,h=Math.floor(r),m=Math.floor(l),p=h+1,_=m+1,v=t/h,g=i/m,x=[],M=[],A=[],E=[];for(let S=0;S<_;S++){const P=S*g-f;for(let O=0;O<p;O++){const C=O*v-c;M.push(C,-P,0),A.push(0,0,1),E.push(O/h),E.push(1-S/m)}}for(let S=0;S<m;S++)for(let P=0;P<h;P++){const O=P+p*S,C=P+p*(S+1),z=P+1+p*(S+1),D=P+1+p*S;x.push(O,C,D),x.push(C,z,D)}this.setIndex(x),this.setAttribute("position",new $n(M,3)),this.setAttribute("normal",new $n(A,3)),this.setAttribute("uv",new $n(E,2))}copy(t){return super.copy(t),this.parameters=Object.assign({},t.parameters),this}static fromJSON(t){return new Ji(t.width,t.height,t.widthSegments,t.heightSegments)}}function io(s){const t={};for(const i in s){t[i]={};for(const r in s[i]){const l=s[i][r];if(Nv(l))l.isRenderTargetTexture?(se("UniformsUtils: Textures of render targets cannot be cloned via cloneUniforms() or mergeUniforms()."),t[i][r]=null):t[i][r]=l.clone();else if(Array.isArray(l))if(Nv(l[0])){const c=[];for(let f=0,h=l.length;f<h;f++)c[f]=l[f].clone();t[i][r]=c}else t[i][r]=l.slice();else t[i][r]=l}}return t}function Xn(s){const t={};for(let i=0;i<s.length;i++){const r=io(s[i]);for(const l in r)t[l]=r[l]}return t}function Nv(s){return s&&(s.isColor||s.isMatrix3||s.isMatrix4||s.isVector2||s.isVector3||s.isVector4||s.isTexture||s.isQuaternion)}function cb(s){const t=[];for(let i=0;i<s.length;i++)t.push(s[i].clone());return t}function gS(s){const t=s.getRenderTarget();return t===null?s.outputColorSpace:t.isXRRenderTarget===!0?t.texture.colorSpace:Te.workingColorSpace}const ub={clone:io,merge:Xn};var fb=`void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}`,db=`void main() {
	gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
}`;class ta extends Qr{constructor(t){super(),this.isShaderMaterial=!0,this.type="ShaderMaterial",this.defines={},this.uniforms={},this.uniformsGroups=[],this.vertexShader=fb,this.fragmentShader=db,this.linewidth=1,this.wireframe=!1,this.wireframeLinewidth=1,this.fog=!1,this.lights=!1,this.clipping=!1,this.forceSinglePass=!0,this.extensions={clipCullDistance:!1,multiDraw:!1},this.defaultAttributeValues={color:[1,1,1],uv:[0,0],uv1:[0,0]},this.index0AttributeName=void 0,this.uniformsNeedUpdate=!1,this.glslVersion=null,t!==void 0&&this.setValues(t)}copy(t){return super.copy(t),this.fragmentShader=t.fragmentShader,this.vertexShader=t.vertexShader,this.uniforms=io(t.uniforms),this.uniformsGroups=cb(t.uniformsGroups),this.defines=Object.assign({},t.defines),this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.fog=t.fog,this.lights=t.lights,this.clipping=t.clipping,this.extensions=Object.assign({},t.extensions),this.glslVersion=t.glslVersion,this.defaultAttributeValues=Object.assign({},t.defaultAttributeValues),this.index0AttributeName=t.index0AttributeName,this.uniformsNeedUpdate=t.uniformsNeedUpdate,this}toJSON(t){const i=super.toJSON(t);i.glslVersion=this.glslVersion,i.uniforms={};for(const l in this.uniforms){const f=this.uniforms[l].value;f&&f.isTexture?i.uniforms[l]={type:"t",value:f.toJSON(t).uuid}:f&&f.isColor?i.uniforms[l]={type:"c",value:f.getHex()}:f&&f.isVector2?i.uniforms[l]={type:"v2",value:f.toArray()}:f&&f.isVector3?i.uniforms[l]={type:"v3",value:f.toArray()}:f&&f.isVector4?i.uniforms[l]={type:"v4",value:f.toArray()}:f&&f.isMatrix3?i.uniforms[l]={type:"m3",value:f.toArray()}:f&&f.isMatrix4?i.uniforms[l]={type:"m4",value:f.toArray()}:i.uniforms[l]={value:f}}Object.keys(this.defines).length>0&&(i.defines=this.defines),i.vertexShader=this.vertexShader,i.fragmentShader=this.fragmentShader,i.lights=this.lights,i.clipping=this.clipping;const r={};for(const l in this.extensions)this.extensions[l]===!0&&(r[l]=!0);return Object.keys(r).length>0&&(i.extensions=r),i}fromJSON(t,i){if(super.fromJSON(t,i),t.uniforms!==void 0)for(const r in t.uniforms){const l=t.uniforms[r];switch(this.uniforms[r]={},l.type){case"t":this.uniforms[r].value=i[l.value]||null;break;case"c":this.uniforms[r].value=new me().setHex(l.value);break;case"v2":this.uniforms[r].value=new Ae().fromArray(l.value);break;case"v3":this.uniforms[r].value=new Q().fromArray(l.value);break;case"v4":this.uniforms[r].value=new sn().fromArray(l.value);break;case"m3":this.uniforms[r].value=new ce().fromArray(l.value);break;case"m4":this.uniforms[r].value=new $e().fromArray(l.value);break;default:this.uniforms[r].value=l.value}}if(t.defines!==void 0&&(this.defines=t.defines),t.vertexShader!==void 0&&(this.vertexShader=t.vertexShader),t.fragmentShader!==void 0&&(this.fragmentShader=t.fragmentShader),t.glslVersion!==void 0&&(this.glslVersion=t.glslVersion),t.extensions!==void 0)for(const r in t.extensions)this.extensions[r]=t.extensions[r];return t.lights!==void 0&&(this.lights=t.lights),t.clipping!==void 0&&(this.clipping=t.clipping),this}}class hb extends ta{constructor(t){super(t),this.isRawShaderMaterial=!0,this.type="RawShaderMaterial"}}class $s extends Qr{constructor(t){super(),this.isMeshStandardMaterial=!0,this.type="MeshStandardMaterial",this.defines={STANDARD:""},this.color=new me(16777215),this.roughness=1,this.metalness=0,this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.emissive=new me(0),this.emissiveIntensity=1,this.emissiveMap=null,this.bumpMap=null,this.bumpScale=1,this.normalMap=null,this.normalMapType=Rp,this.normalScale=new Ae(1,1),this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.roughnessMap=null,this.metalnessMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new mr,this.envMapIntensity=1,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.flatShading=!1,this.fog=!0,this.setValues(t)}copy(t){return super.copy(t),this.defines={STANDARD:""},this.color.copy(t.color),this.roughness=t.roughness,this.metalness=t.metalness,this.map=t.map,this.lightMap=t.lightMap,this.lightMapIntensity=t.lightMapIntensity,this.aoMap=t.aoMap,this.aoMapIntensity=t.aoMapIntensity,this.emissive.copy(t.emissive),this.emissiveMap=t.emissiveMap,this.emissiveIntensity=t.emissiveIntensity,this.bumpMap=t.bumpMap,this.bumpScale=t.bumpScale,this.normalMap=t.normalMap,this.normalMapType=t.normalMapType,this.normalScale.copy(t.normalScale),this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this.roughnessMap=t.roughnessMap,this.metalnessMap=t.metalnessMap,this.alphaMap=t.alphaMap,this.envMap=t.envMap,this.envMapRotation.copy(t.envMapRotation),this.envMapIntensity=t.envMapIntensity,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this.wireframeLinecap=t.wireframeLinecap,this.wireframeLinejoin=t.wireframeLinejoin,this.flatShading=t.flatShading,this.fog=t.fog,this}}class pb extends Qr{constructor(t){super(),this.isMeshDepthMaterial=!0,this.type="MeshDepthMaterial",this.depthPacking=uE,this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.wireframe=!1,this.wireframeLinewidth=1,this.setValues(t)}copy(t){return super.copy(t),this.depthPacking=t.depthPacking,this.map=t.map,this.alphaMap=t.alphaMap,this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this.wireframe=t.wireframe,this.wireframeLinewidth=t.wireframeLinewidth,this}}class mb extends Qr{constructor(t){super(),this.isMeshDistanceMaterial=!0,this.type="MeshDistanceMaterial",this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.setValues(t)}copy(t){return super.copy(t),this.map=t.map,this.alphaMap=t.alphaMap,this.displacementMap=t.displacementMap,this.displacementScale=t.displacementScale,this.displacementBias=t.displacementBias,this}}const ch={enabled:!1,files:{},add:function(s,t){this.enabled!==!1&&(Uv(s)||(this.files[s]=t))},get:function(s){if(this.enabled!==!1&&!Uv(s))return this.files[s]},remove:function(s){delete this.files[s]},clear:function(){this.files={}}};function Uv(s){try{const t=s.slice(s.indexOf(":")+1);return new URL(t).protocol==="blob:"}catch{return!1}}class gb{constructor(t,i,r){const l=this;let c=!1,f=0,h=0,m;const p=[];this.onStart=void 0,this.onLoad=t,this.onProgress=i,this.onError=r,this._abortController=null,this.itemStart=function(_){h++,c===!1&&l.onStart!==void 0&&l.onStart(_,f,h),c=!0},this.itemEnd=function(_){f++,l.onProgress!==void 0&&l.onProgress(_,f,h),f===h&&(c=!1,l.onLoad!==void 0&&l.onLoad())},this.itemError=function(_){l.onError!==void 0&&l.onError(_)},this.resolveURL=function(_){return _=_.normalize("NFC"),m?m(_):_},this.setURLModifier=function(_){return m=_,this},this.addHandler=function(_,v){return p.push(_,v),this},this.removeHandler=function(_){const v=p.indexOf(_);return v!==-1&&p.splice(v,2),this},this.getHandler=function(_){for(let v=0,g=p.length;v<g;v+=2){const x=p[v],M=p[v+1];if(x.global&&(x.lastIndex=0),x.test(_))return M}return null},this.abort=function(){return this.abortController.abort(),this._abortController=null,this}}get abortController(){return this._abortController||(this._abortController=new AbortController),this._abortController}}const _b=new gb;class Qp{constructor(t){this.manager=t!==void 0?t:_b,this.crossOrigin="anonymous",this.withCredentials=!1,this.path="",this.resourcePath="",this.requestHeader={},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}load(){}loadAsync(t,i){const r=this;return new Promise(function(l,c){r.load(t,l,i,c)})}parse(){}setCrossOrigin(t){return this.crossOrigin=t,this}setWithCredentials(t){return this.withCredentials=t,this}setPath(t){return this.path=t,this}setResourcePath(t){return this.resourcePath=t,this}setRequestHeader(t){return this.requestHeader=t,this}abort(){return this}}Qp.DEFAULT_MATERIAL_NAME="__DEFAULT";const ks=new WeakMap;class vb extends Qp{constructor(t){super(t)}load(t,i,r,l){this.path!==void 0&&(t=this.path+t),t=this.manager.resolveURL(t);const c=this,f=ch.get(`image:${t}`);if(f!==void 0){if(f.complete===!0)c.manager.itemStart(t),setTimeout(function(){i&&i(f),c.manager.itemEnd(t)},0);else{let v=ks.get(f);v===void 0&&(v=[],ks.set(f,v)),v.push({onLoad:i,onError:l})}return f}const h=ml("img");function m(){_(),i&&i(this);const v=ks.get(this)||[];for(let g=0;g<v.length;g++){const x=v[g];x.onLoad&&x.onLoad(this)}ks.delete(this),c.manager.itemEnd(t)}function p(v){_(),l&&l(v),ch.remove(`image:${t}`);const g=ks.get(this)||[];for(let x=0;x<g.length;x++){const M=g[x];M.onError&&M.onError(v)}ks.delete(this),c.manager.itemError(t),c.manager.itemEnd(t)}function _(){h.removeEventListener("load",m,!1),h.removeEventListener("error",p,!1)}return h.addEventListener("load",m,!1),h.addEventListener("error",p,!1),t.slice(0,5)!=="data:"&&this.crossOrigin!==void 0&&(h.crossOrigin=this.crossOrigin),ch.add(`image:${t}`,h),c.manager.itemStart(t),h.src=t,h}}class xb extends Qp{constructor(t){super(t)}load(t,i,r,l){const c=new Bn,f=new vb(this.manager);return f.setCrossOrigin(this.crossOrigin),f.setPath(this.path),f.load(t,function(h){c.image=h,c.needsUpdate=!0,i!==void 0&&i(c)},r,l),c}}class Jp extends dn{constructor(t,i=1){super(),this.isLight=!0,this.type="Light",this.color=new me(t),this.intensity=i}dispose(){this.dispatchEvent({type:"dispose"})}copy(t,i){return super.copy(t,i),this.color.copy(t.color),this.intensity=t.intensity,this}toJSON(t){const i=super.toJSON(t);return i.object.color=this.color.getHex(),i.object.intensity=this.intensity,i}}class Sb extends Jp{constructor(t,i,r){super(t,r),this.isHemisphereLight=!0,this.type="HemisphereLight",this.position.copy(dn.DEFAULT_UP),this.updateMatrix(),this.groundColor=new me(i)}copy(t,i){return super.copy(t,i),this.groundColor.copy(t.groundColor),this}toJSON(t){const i=super.toJSON(t);return i.object.groundColor=this.groundColor.getHex(),i}}const uh=new $e,Lv=new Q,Ov=new Q;class _S{constructor(t){this.camera=t,this.intensity=1,this.bias=0,this.biasNode=null,this.normalBias=0,this.radius=1,this.blurSamples=8,this.mapSize=new Ae(512,512),this.mapType=pi,this.map=null,this.mapPass=null,this.matrix=new $e,this.autoUpdate=!0,this.needsUpdate=!1,this._frustum=new jp,this._frameExtents=new Ae(1,1),this._viewportCount=1,this._viewports=[new sn(0,0,1,1)]}getViewportCount(){return this._viewportCount}getFrustum(){return this._frustum}updateMatrices(t){const i=this.camera,r=this.matrix;Lv.setFromMatrixPosition(t.matrixWorld),i.position.copy(Lv),Ov.setFromMatrixPosition(t.target.matrixWorld),i.lookAt(Ov),i.updateMatrixWorld(),uh.multiplyMatrices(i.projectionMatrix,i.matrixWorldInverse),this._frustum.setFromProjectionMatrix(uh,i.coordinateSystem,i.reversedDepth),i.coordinateSystem===pl||i.reversedDepth?r.set(.5,0,0,.5,0,.5,0,.5,0,0,1,0,0,0,0,1):r.set(.5,0,0,.5,0,.5,0,.5,0,0,.5,.5,0,0,0,1),r.multiply(uh)}getViewport(t){return this._viewports[t]}getFrameExtents(){return this._frameExtents}dispose(){this.map&&this.map.dispose(),this.mapPass&&this.mapPass.dispose()}copy(t){return this.camera=t.camera.clone(),this.intensity=t.intensity,this.bias=t.bias,this.radius=t.radius,this.autoUpdate=t.autoUpdate,this.needsUpdate=t.needsUpdate,this.normalBias=t.normalBias,this.blurSamples=t.blurSamples,this.mapSize.copy(t.mapSize),this.biasNode=t.biasNode,this}clone(){return new this.constructor().copy(this)}toJSON(){const t={};return this.intensity!==1&&(t.intensity=this.intensity),this.bias!==0&&(t.bias=this.bias),this.normalBias!==0&&(t.normalBias=this.normalBias),this.radius!==1&&(t.radius=this.radius),(this.mapSize.x!==512||this.mapSize.y!==512)&&(t.mapSize=this.mapSize.toArray()),t.camera=this.camera.toJSON(!1).object,delete t.camera.matrix,t}}const eu=new Q,nu=new Kr,ki=new Q;class vS extends dn{constructor(){super(),this.isCamera=!0,this.type="Camera",this.matrixWorldInverse=new $e,this.projectionMatrix=new $e,this.projectionMatrixInverse=new $e,this.coordinateSystem=ji,this._reversedDepth=!1}get reversedDepth(){return this._reversedDepth}copy(t,i){return super.copy(t,i),this.matrixWorldInverse.copy(t.matrixWorldInverse),this.projectionMatrix.copy(t.projectionMatrix),this.projectionMatrixInverse.copy(t.projectionMatrixInverse),this.coordinateSystem=t.coordinateSystem,this}getWorldDirection(t){return super.getWorldDirection(t).negate()}updateMatrixWorld(t){super.updateMatrixWorld(t),this.matrixWorld.decompose(eu,nu,ki),ki.x===1&&ki.y===1&&ki.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(eu,nu,ki.set(1,1,1)).invert()}updateWorldMatrix(t,i,r=!1){super.updateWorldMatrix(t,i,r),this.matrixWorld.decompose(eu,nu,ki),ki.x===1&&ki.y===1&&ki.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(eu,nu,ki.set(1,1,1)).invert()}clone(){return new this.constructor().copy(this)}}const ur=new Q,Pv=new Ae,Bv=new Ae;class hi extends vS{constructor(t=50,i=1,r=.1,l=2e3){super(),this.isPerspectiveCamera=!0,this.type="PerspectiveCamera",this.fov=t,this.zoom=1,this.near=r,this.far=l,this.focus=10,this.aspect=i,this.view=null,this.filmGauge=35,this.filmOffset=0,this.updateProjectionMatrix()}copy(t,i){return super.copy(t,i),this.fov=t.fov,this.zoom=t.zoom,this.near=t.near,this.far=t.far,this.focus=t.focus,this.aspect=t.aspect,this.view=t.view===null?null:Object.assign({},t.view),this.filmGauge=t.filmGauge,this.filmOffset=t.filmOffset,this}setFocalLength(t){const i=.5*this.getFilmHeight()/t;this.fov=gl*2*Math.atan(i),this.updateProjectionMatrix()}getFocalLength(){const t=Math.tan(Qs*.5*this.fov);return .5*this.getFilmHeight()/t}getEffectiveFOV(){return gl*2*Math.atan(Math.tan(Qs*.5*this.fov)/this.zoom)}getFilmWidth(){return this.filmGauge*Math.min(this.aspect,1)}getFilmHeight(){return this.filmGauge/Math.max(this.aspect,1)}getViewBounds(t,i,r){ur.set(-1,-1,.5).applyMatrix4(this.projectionMatrixInverse),i.set(ur.x,ur.y).multiplyScalar(-t/ur.z),ur.set(1,1,.5).applyMatrix4(this.projectionMatrixInverse),r.set(ur.x,ur.y).multiplyScalar(-t/ur.z)}getViewSize(t,i){return this.getViewBounds(t,Pv,Bv),i.subVectors(Bv,Pv)}setViewOffset(t,i,r,l,c,f){this.aspect=t/i,this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=t,this.view.fullHeight=i,this.view.offsetX=r,this.view.offsetY=l,this.view.width=c,this.view.height=f,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const t=this.near;let i=t*Math.tan(Qs*.5*this.fov)/this.zoom,r=2*i,l=this.aspect*r,c=-.5*l;const f=this.view;if(this.view!==null&&this.view.enabled){const m=f.fullWidth,p=f.fullHeight;c+=f.offsetX*l/m,i-=f.offsetY*r/p,l*=f.width/m,r*=f.height/p}const h=this.filmOffset;h!==0&&(c+=t*h/this.getFilmWidth()),this.projectionMatrix.makePerspective(c,c+l,i,i-r,t,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(t){const i=super.toJSON(t);return i.object.fov=this.fov,i.object.zoom=this.zoom,i.object.near=this.near,i.object.far=this.far,i.object.focus=this.focus,i.object.aspect=this.aspect,this.view!==null&&(i.object.view=Object.assign({},this.view)),i.object.filmGauge=this.filmGauge,i.object.filmOffset=this.filmOffset,i}}class yb extends _S{constructor(){super(new hi(90,1,.5,500)),this.isPointLightShadow=!0}}class uu extends Jp{constructor(t,i,r=0,l=2){super(t,i),this.isPointLight=!0,this.type="PointLight",this.distance=r,this.decay=l,this.shadow=new yb}get power(){return this.intensity*4*Math.PI}set power(t){this.intensity=t/(4*Math.PI)}dispose(){super.dispose(),this.shadow.dispose()}copy(t,i){return super.copy(t,i),this.distance=t.distance,this.decay=t.decay,this.shadow=t.shadow.clone(),this}toJSON(t){const i=super.toJSON(t);return i.object.distance=this.distance,i.object.decay=this.decay,i.object.shadow=this.shadow.toJSON(),i}}class $p extends vS{constructor(t=-1,i=1,r=1,l=-1,c=.1,f=2e3){super(),this.isOrthographicCamera=!0,this.type="OrthographicCamera",this.zoom=1,this.view=null,this.left=t,this.right=i,this.top=r,this.bottom=l,this.near=c,this.far=f,this.updateProjectionMatrix()}copy(t,i){return super.copy(t,i),this.left=t.left,this.right=t.right,this.top=t.top,this.bottom=t.bottom,this.near=t.near,this.far=t.far,this.zoom=t.zoom,this.view=t.view===null?null:Object.assign({},t.view),this}setViewOffset(t,i,r,l,c,f){this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=t,this.view.fullHeight=i,this.view.offsetX=r,this.view.offsetY=l,this.view.width=c,this.view.height=f,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const t=(this.right-this.left)/(2*this.zoom),i=(this.top-this.bottom)/(2*this.zoom),r=(this.right+this.left)/2,l=(this.top+this.bottom)/2;let c=r-t,f=r+t,h=l+i,m=l-i;if(this.view!==null&&this.view.enabled){const p=(this.right-this.left)/this.view.fullWidth/this.zoom,_=(this.top-this.bottom)/this.view.fullHeight/this.zoom;c+=p*this.view.offsetX,f=c+p*this.view.width,h-=_*this.view.offsetY,m=h-_*this.view.height}this.projectionMatrix.makeOrthographic(c,f,h,m,this.near,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(t){const i=super.toJSON(t);return i.object.zoom=this.zoom,i.object.left=this.left,i.object.right=this.right,i.object.top=this.top,i.object.bottom=this.bottom,i.object.near=this.near,i.object.far=this.far,this.view!==null&&(i.object.view=Object.assign({},this.view)),i}}class Mb extends _S{constructor(){super(new $p(-5,5,5,-5,.5,500)),this.isDirectionalLightShadow=!0}}class Eb extends Jp{constructor(t,i){super(t,i),this.isDirectionalLight=!0,this.type="DirectionalLight",this.position.copy(dn.DEFAULT_UP),this.updateMatrix(),this.target=new dn,this.shadow=new Mb}dispose(){super.dispose(),this.shadow.dispose()}copy(t){return super.copy(t),this.target=t.target.clone(),this.shadow=t.shadow.clone(),this}toJSON(t){const i=super.toJSON(t);return i.object.shadow=this.shadow.toJSON(),i.object.target=this.target.uuid,i}}const Xs=-90,qs=1;class bb extends dn{constructor(t,i,r){super(),this.type="CubeCamera",this.renderTarget=r,this.coordinateSystem=null,this.activeMipmapLevel=0;const l=new hi(Xs,qs,t,i);l.layers=this.layers,this.add(l);const c=new hi(Xs,qs,t,i);c.layers=this.layers,this.add(c);const f=new hi(Xs,qs,t,i);f.layers=this.layers,this.add(f);const h=new hi(Xs,qs,t,i);h.layers=this.layers,this.add(h);const m=new hi(Xs,qs,t,i);m.layers=this.layers,this.add(m);const p=new hi(Xs,qs,t,i);p.layers=this.layers,this.add(p)}updateCoordinateSystem(){const t=this.coordinateSystem,i=this.children.concat(),[r,l,c,f,h,m]=i;for(const p of i)this.remove(p);if(t===ji)r.up.set(0,1,0),r.lookAt(1,0,0),l.up.set(0,1,0),l.lookAt(-1,0,0),c.up.set(0,0,-1),c.lookAt(0,1,0),f.up.set(0,0,1),f.lookAt(0,-1,0),h.up.set(0,1,0),h.lookAt(0,0,1),m.up.set(0,1,0),m.lookAt(0,0,-1);else if(t===pl)r.up.set(0,-1,0),r.lookAt(-1,0,0),l.up.set(0,-1,0),l.lookAt(1,0,0),c.up.set(0,0,1),c.lookAt(0,1,0),f.up.set(0,0,-1),f.lookAt(0,-1,0),h.up.set(0,-1,0),h.lookAt(0,0,1),m.up.set(0,-1,0),m.lookAt(0,0,-1);else throw new Error("THREE.CubeCamera.updateCoordinateSystem(): Invalid coordinate system: "+t);for(const p of i)this.add(p),p.updateMatrixWorld()}update(t,i){this.parent===null&&this.updateMatrixWorld();const{renderTarget:r,activeMipmapLevel:l}=this;this.coordinateSystem!==t.coordinateSystem&&(this.coordinateSystem=t.coordinateSystem,this.updateCoordinateSystem());const[c,f,h,m,p,_]=this.children,v=t.getRenderTarget(),g=t.getActiveCubeFace(),x=t.getActiveMipmapLevel(),M=t.xr.enabled;t.xr.enabled=!1;const A=r.texture.generateMipmaps;r.texture.generateMipmaps=!1;let E=!1;t.isWebGLRenderer===!0?E=t.state.buffers.depth.getReversed():E=t.reversedDepthBuffer,t.setRenderTarget(r,0,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,c),t.setRenderTarget(r,1,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,f),t.setRenderTarget(r,2,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,h),t.setRenderTarget(r,3,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,m),t.setRenderTarget(r,4,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,p),r.texture.generateMipmaps=A,t.setRenderTarget(r,5,l),E&&t.autoClear===!1&&t.clearDepth(),t.render(i,_),t.setRenderTarget(v,g,x),t.xr.enabled=M,r.texture.needsPMREMUpdate=!0}}class Tb extends hi{constructor(t=[]){super(),this.isArrayCamera=!0,this.isMultiViewCamera=!1,this.cameras=t}}class Ab{constructor(){this._previousTime=0,this._currentTime=0,this._startTime=performance.now(),this._delta=0,this._elapsed=0,this._timescale=1,this._document=null,this._pageVisibilityHandler=null}connect(t){this._document=t,t.hidden!==void 0&&(this._pageVisibilityHandler=Rb.bind(this),t.addEventListener("visibilitychange",this._pageVisibilityHandler,!1))}disconnect(){this._pageVisibilityHandler!==null&&(this._document.removeEventListener("visibilitychange",this._pageVisibilityHandler),this._pageVisibilityHandler=null),this._document=null}getDelta(){return this._delta/1e3}getElapsed(){return this._elapsed/1e3}getTimescale(){return this._timescale}setTimescale(t){return this._timescale=t,this}reset(){return this._currentTime=performance.now()-this._startTime,this}dispose(){this.disconnect()}update(t){return this._pageVisibilityHandler!==null&&this._document.hidden===!0?this._delta=0:(this._previousTime=this._currentTime,this._currentTime=(t!==void 0?t:performance.now())-this._startTime,this._delta=(this._currentTime-this._previousTime)*this._timescale,this._elapsed+=this._delta),this}}function Rb(){this._document.hidden===!1&&this.reset()}const Iv=new $e;class Cb{constructor(t,i,r=0,l=1/0){this.ray=new yu(t,i),this.near=r,this.far=l,this.camera=null,this.layers=new Yp,this.params={Mesh:{},Line:{threshold:1},LOD:{},Points:{threshold:1},Sprite:{}}}set(t,i){this.ray.set(t,i)}setFromCamera(t,i){i.isPerspectiveCamera?(this.ray.origin.setFromMatrixPosition(i.matrixWorld),this.ray.direction.set(t.x,t.y,.5).unproject(i).sub(this.ray.origin).normalize(),this.camera=i):i.isOrthographicCamera?(this.ray.origin.set(t.x,t.y,i.projectionMatrix.elements[14]).unproject(i),this.ray.direction.set(0,0,-1).transformDirection(i.matrixWorld),this.camera=i):Re("Raycaster: Unsupported camera type: "+i.type)}setFromXRController(t){return Iv.identity().extractRotation(t.matrixWorld),this.ray.origin.setFromMatrixPosition(t.matrixWorld),this.ray.direction.set(0,0,-1).applyMatrix4(Iv),this}intersectObject(t,i=!0,r=[]){return Np(t,this,r,i),r.sort(zv),r}intersectObjects(t,i=!0,r=[]){for(let l=0,c=t.length;l<c;l++)Np(t[l],this,r,i);return r.sort(zv),r}}function zv(s,t){return s.distance-t.distance}function Np(s,t,i,r){let l=!0;if(s.layers.test(t.layers)&&s.raycast(t,i)===!1&&(l=!1),l===!0&&r===!0){const c=s.children;for(let f=0,h=c.length;f<h;f++)Np(c[f],t,i,!0)}}const sm=class sm{constructor(t,i,r,l){this.elements=[1,0,0,1],t!==void 0&&this.set(t,i,r,l)}identity(){return this.set(1,0,0,1),this}fromArray(t,i=0){for(let r=0;r<4;r++)this.elements[r]=t[r+i];return this}set(t,i,r,l){const c=this.elements;return c[0]=t,c[2]=i,c[1]=r,c[3]=l,this}};sm.prototype.isMatrix2=!0;let Fv=sm;class wb extends dS{constructor(t=10,i=10,r=4473924,l=8947848){r=new me(r),l=new me(l);const c=i/2,f=t/i,h=t/2,m=[],p=[];for(let g=0,x=0,M=-h;g<=i;g++,M+=f){m.push(-h,0,M,h,0,M),m.push(M,0,-h,M,0,h);const A=g===c?r:l;A.toArray(p,x),x+=3,A.toArray(p,x),x+=3,A.toArray(p,x),x+=3,A.toArray(p,x),x+=3}const _=new bn;_.setAttribute("position",new $n(m,3)),_.setAttribute("color",new $n(p,3));const v=new Zr({vertexColors:!0,toneMapped:!1});super(_,v),this.type="GridHelper"}dispose(){this.geometry.dispose(),this.material.dispose()}}function Hv(s,t,i,r){const l=Db(r);switch(i){case aS:return s*t;case sS:return s*t/l.components*l.byteLength;case Hp:return s*t/l.components*l.byteLength;case Yr:return s*t*2/l.components*l.byteLength;case Gp:return s*t*2/l.components*l.byteLength;case rS:return s*t*3/l.components*l.byteLength;case Pi:return s*t*4/l.components*l.byteLength;case Vp:return s*t*4/l.components*l.byteLength;case su:case ou:return Math.floor((s+3)/4)*Math.floor((t+3)/4)*8;case lu:case cu:return Math.floor((s+3)/4)*Math.floor((t+3)/4)*16;case Jh:case tp:return Math.max(s,16)*Math.max(t,8)/4;case Qh:case $h:return Math.max(s,8)*Math.max(t,8)/2;case ep:case np:case ap:case rp:return Math.floor((s+3)/4)*Math.floor((t+3)/4)*8;case ip:case du:case sp:return Math.floor((s+3)/4)*Math.floor((t+3)/4)*16;case op:return Math.floor((s+3)/4)*Math.floor((t+3)/4)*16;case lp:return Math.floor((s+4)/5)*Math.floor((t+3)/4)*16;case cp:return Math.floor((s+4)/5)*Math.floor((t+4)/5)*16;case up:return Math.floor((s+5)/6)*Math.floor((t+4)/5)*16;case fp:return Math.floor((s+5)/6)*Math.floor((t+5)/6)*16;case dp:return Math.floor((s+7)/8)*Math.floor((t+4)/5)*16;case hp:return Math.floor((s+7)/8)*Math.floor((t+5)/6)*16;case pp:return Math.floor((s+7)/8)*Math.floor((t+7)/8)*16;case mp:return Math.floor((s+9)/10)*Math.floor((t+4)/5)*16;case gp:return Math.floor((s+9)/10)*Math.floor((t+5)/6)*16;case _p:return Math.floor((s+9)/10)*Math.floor((t+7)/8)*16;case vp:return Math.floor((s+9)/10)*Math.floor((t+9)/10)*16;case xp:return Math.floor((s+11)/12)*Math.floor((t+9)/10)*16;case Sp:return Math.floor((s+11)/12)*Math.floor((t+11)/12)*16;case yp:case Mp:case Ep:return Math.ceil(s/4)*Math.ceil(t/4)*16;case bp:case Tp:return Math.ceil(s/4)*Math.ceil(t/4)*8;case hu:case Ap:return Math.ceil(s/4)*Math.ceil(t/4)*16}throw new Error(`Unable to determine texture byte length for ${i} format.`)}function Db(s){switch(s){case pi:case tS:return{byteLength:1,components:1};case dl:case eS:case Da:return{byteLength:2,components:1};case zp:case Fp:return{byteLength:2,components:4};case $i:case Ip:case Zi:return{byteLength:4,components:1};case nS:case iS:return{byteLength:4,components:3}}throw new Error(`THREE.TextureUtils: Unknown texture type ${s}.`)}typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("register",{detail:{revision:Pp}}));typeof window<"u"&&(window.__THREE__?se("WARNING: Multiple instances of Three.js being imported."):window.__THREE__=Pp);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */function xS(){let s=null,t=!1,i=null,r=null;function l(c,f){i(c,f),r=s.requestAnimationFrame(l)}return{start:function(){t!==!0&&i!==null&&s!==null&&(r=s.requestAnimationFrame(l),t=!0)},stop:function(){s!==null&&s.cancelAnimationFrame(r),t=!1},setAnimationLoop:function(c){i=c},setContext:function(c){s=c}}}function Nb(s){const t=new WeakMap;function i(h,m){const p=h.array,_=h.usage,v=p.byteLength,g=s.createBuffer();s.bindBuffer(m,g),s.bufferData(m,p,_),h.onUploadCallback();let x;if(p instanceof Float32Array)x=s.FLOAT;else if(typeof Float16Array<"u"&&p instanceof Float16Array)x=s.HALF_FLOAT;else if(p instanceof Uint16Array)h.isFloat16BufferAttribute?x=s.HALF_FLOAT:x=s.UNSIGNED_SHORT;else if(p instanceof Int16Array)x=s.SHORT;else if(p instanceof Uint32Array)x=s.UNSIGNED_INT;else if(p instanceof Int32Array)x=s.INT;else if(p instanceof Int8Array)x=s.BYTE;else if(p instanceof Uint8Array)x=s.UNSIGNED_BYTE;else if(p instanceof Uint8ClampedArray)x=s.UNSIGNED_BYTE;else throw new Error("THREE.WebGLAttributes: Unsupported buffer data format: "+p);return{buffer:g,type:x,bytesPerElement:p.BYTES_PER_ELEMENT,version:h.version,size:v}}function r(h,m,p){const _=m.array,v=m.updateRanges;if(s.bindBuffer(p,h),v.length===0)s.bufferSubData(p,0,_);else{v.sort((x,M)=>x.start-M.start);let g=0;for(let x=1;x<v.length;x++){const M=v[g],A=v[x];A.start<=M.start+M.count+1?M.count=Math.max(M.count,A.start+A.count-M.start):(++g,v[g]=A)}v.length=g+1;for(let x=0,M=v.length;x<M;x++){const A=v[x];s.bufferSubData(p,A.start*_.BYTES_PER_ELEMENT,_,A.start,A.count)}m.clearUpdateRanges()}m.onUploadCallback()}function l(h){return h.isInterleavedBufferAttribute&&(h=h.data),t.get(h)}function c(h){h.isInterleavedBufferAttribute&&(h=h.data);const m=t.get(h);m&&(s.deleteBuffer(m.buffer),t.delete(h))}function f(h,m){if(h.isInterleavedBufferAttribute&&(h=h.data),h.isGLBufferAttribute){const _=t.get(h);(!_||_.version<h.version)&&t.set(h,{buffer:h.buffer,type:h.type,bytesPerElement:h.elementSize,version:h.version});return}const p=t.get(h);if(p===void 0)t.set(h,i(h,m));else if(p.version<h.version){if(p.size!==h.array.byteLength)throw new Error("THREE.WebGLAttributes: The size of the buffer attribute's array buffer does not match the original size. Resizing buffer attributes is not supported.");r(p.buffer,h,m),p.version=h.version}}return{get:l,remove:c,update:f}}var Ub=`#ifdef USE_ALPHAHASH
	if ( diffuseColor.a < getAlphaHashThreshold( vPosition ) ) discard;
#endif`,Lb=`#ifdef USE_ALPHAHASH
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
#endif`,Ob=`#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, vAlphaMapUv ).g;
#endif`,Pb=`#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,Bb=`#ifdef USE_ALPHATEST
	#ifdef ALPHA_TO_COVERAGE
	diffuseColor.a = smoothstep( alphaTest, alphaTest + fwidth( diffuseColor.a ), diffuseColor.a );
	if ( diffuseColor.a == 0.0 ) discard;
	#else
	if ( diffuseColor.a < alphaTest ) discard;
	#endif
#endif`,Ib=`#ifdef USE_ALPHATEST
	uniform float alphaTest;
#endif`,zb=`#ifdef USE_AOMAP
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
#endif`,Fb=`#ifdef USE_AOMAP
	uniform sampler2D aoMap;
	uniform float aoMapIntensity;
#endif`,Hb=`#ifdef USE_BATCHING
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
#endif`,Gb=`#ifdef USE_BATCHING
	mat4 batchingMatrix = getBatchingMatrix( getIndirectIndex( gl_DrawID ) );
#endif`,Vb=`vec3 transformed = vec3( position );
#ifdef USE_ALPHAHASH
	vPosition = vec3( position );
#endif`,kb=`vec3 objectNormal = vec3( normal );
#ifdef USE_TANGENT
	vec3 objectTangent = vec3( tangent.xyz );
#endif`,Xb=`float G_BlinnPhong_Implicit( ) {
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
} // validated`,qb=`#ifdef USE_IRIDESCENCE
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
#endif`,Wb=`#ifdef USE_BUMPMAP
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
#endif`,Yb=`#if NUM_CLIPPING_PLANES > 0
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
#endif`,Zb=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
	uniform vec4 clippingPlanes[ NUM_CLIPPING_PLANES ];
#endif`,jb=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
#endif`,Kb=`#if NUM_CLIPPING_PLANES > 0
	vClipPosition = - mvPosition.xyz;
#endif`,Qb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	diffuseColor *= vColor;
#endif`,Jb=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#endif`,$b=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	varying vec4 vColor;
#endif`,tT=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
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
#endif`,eT=`#define PI 3.141592653589793
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
} // validated`,nT=`#ifdef ENVMAP_TYPE_CUBE_UV
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
#endif`,iT=`vec3 transformedNormal = objectNormal;
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
#endif`,aT=`#ifdef USE_DISPLACEMENTMAP
	uniform sampler2D displacementMap;
	uniform float displacementScale;
	uniform float displacementBias;
#endif`,rT=`#ifdef USE_DISPLACEMENTMAP
	transformed += normalize( objectNormal ) * ( texture2D( displacementMap, vDisplacementMapUv ).x * displacementScale + displacementBias );
#endif`,sT=`#ifdef USE_EMISSIVEMAP
	vec4 emissiveColor = texture2D( emissiveMap, vEmissiveMapUv );
	#ifdef DECODE_VIDEO_TEXTURE_EMISSIVE
		emissiveColor = sRGBTransferEOTF( emissiveColor );
	#endif
	totalEmissiveRadiance *= emissiveColor.rgb;
#endif`,oT=`#ifdef USE_EMISSIVEMAP
	uniform sampler2D emissiveMap;
#endif`,lT="gl_FragColor = linearToOutputTexel( gl_FragColor );",cT=`vec4 LinearTransferOETF( in vec4 value ) {
	return value;
}
vec4 sRGBTransferEOTF( in vec4 value ) {
	return vec4( mix( pow( value.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), value.rgb * 0.0773993808, vec3( lessThanEqual( value.rgb, vec3( 0.04045 ) ) ) ), value.a );
}
vec4 sRGBTransferOETF( in vec4 value ) {
	return vec4( mix( pow( value.rgb, vec3( 0.41666 ) ) * 1.055 - vec3( 0.055 ), value.rgb * 12.92, vec3( lessThanEqual( value.rgb, vec3( 0.0031308 ) ) ) ), value.a );
}`,uT=`#ifdef USE_ENVMAP
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
#endif`,fT=`#ifdef USE_ENVMAP
	uniform float envMapIntensity;
	uniform mat3 envMapRotation;
	#ifdef ENVMAP_TYPE_CUBE
		uniform samplerCube envMap;
	#else
		uniform sampler2D envMap;
	#endif
#endif`,dT=`#ifdef USE_ENVMAP
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
#endif`,hT=`#ifdef USE_ENVMAP
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		
		varying vec3 vWorldPosition;
	#else
		varying vec3 vReflect;
		uniform float refractionRatio;
	#endif
#endif`,pT=`#ifdef USE_ENVMAP
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
#endif`,mT=`#ifdef USE_FOG
	vFogDepth = - mvPosition.z;
#endif`,gT=`#ifdef USE_FOG
	varying float vFogDepth;
#endif`,_T=`#ifdef USE_FOG
	#ifdef FOG_EXP2
		float fogFactor = 1.0 - exp( - fogDensity * fogDensity * vFogDepth * vFogDepth );
	#else
		float fogFactor = smoothstep( fogNear, fogFar, vFogDepth );
	#endif
	gl_FragColor.rgb = mix( gl_FragColor.rgb, fogColor, fogFactor );
#endif`,vT=`#ifdef USE_FOG
	uniform vec3 fogColor;
	varying float vFogDepth;
	#ifdef FOG_EXP2
		uniform float fogDensity;
	#else
		uniform float fogNear;
		uniform float fogFar;
	#endif
#endif`,xT=`#ifdef USE_GRADIENTMAP
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
}`,ST=`#ifdef USE_LIGHTMAP
	uniform sampler2D lightMap;
	uniform float lightMapIntensity;
#endif`,yT=`LambertMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularStrength = specularStrength;`,MT=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Lambert`,ET=`uniform bool receiveShadow;
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
#include <lightprobes_pars_fragment>`,bT=`#ifdef USE_ENVMAP
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
#endif`,TT=`ToonMaterial material;
material.diffuseColor = diffuseColor.rgb;`,AT=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Toon`,RT=`BlinnPhongMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularColor = specular;
material.specularShininess = shininess;
material.specularStrength = specularStrength;`,CT=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_BlinnPhong`,wT=`PhysicalMaterial material;
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
#endif`,DT=`uniform sampler2D dfgLUT;
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
}`,NT=`
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
#endif`,UT=`#if defined( RE_IndirectDiffuse )
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
#endif`,LT=`#if defined( RE_IndirectDiffuse )
	#if defined( LAMBERT ) || defined( PHONG )
		irradiance += iblIrradiance;
	#endif
	RE_IndirectDiffuse( irradiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif
#if defined( RE_IndirectSpecular )
	RE_IndirectSpecular( radiance, iblIrradiance, clearcoatRadiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif`,OT=`#ifdef USE_LIGHT_PROBES_GRID
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
#endif`,PT=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	gl_FragDepth = vIsPerspective == 0.0 ? gl_FragCoord.z : log2( vFragDepth ) * logDepthBufFC * 0.5;
#endif`,BT=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	uniform float logDepthBufFC;
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,IT=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,zT=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	vFragDepth = 1.0 + gl_Position.w;
	vIsPerspective = float( isPerspectiveMatrix( projectionMatrix ) );
#endif`,FT=`#ifdef USE_MAP
	vec4 sampledDiffuseColor = texture2D( map, vMapUv );
	#ifdef DECODE_VIDEO_TEXTURE
		sampledDiffuseColor = sRGBTransferEOTF( sampledDiffuseColor );
	#endif
	diffuseColor *= sampledDiffuseColor;
#endif`,HT=`#ifdef USE_MAP
	uniform sampler2D map;
#endif`,GT=`#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
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
#endif`,VT=`#if defined( USE_POINTS_UV )
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
#endif`,kT=`float metalnessFactor = metalness;
#ifdef USE_METALNESSMAP
	vec4 texelMetalness = texture2D( metalnessMap, vMetalnessMapUv );
	metalnessFactor *= texelMetalness.b;
#endif`,XT=`#ifdef USE_METALNESSMAP
	uniform sampler2D metalnessMap;
#endif`,qT=`#ifdef USE_INSTANCING_MORPH
	float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	float morphTargetBaseInfluence = texelFetch( morphTexture, ivec2( 0, gl_InstanceID ), 0 ).r;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		morphTargetInfluences[i] =  texelFetch( morphTexture, ivec2( i + 1, gl_InstanceID ), 0 ).r;
	}
#endif`,WT=`#if defined( USE_MORPHCOLORS )
	vColor *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		#if defined( USE_COLOR_ALPHA )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ) * morphTargetInfluences[ i ];
		#elif defined( USE_COLOR )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ).rgb * morphTargetInfluences[ i ];
		#endif
	}
#endif`,YT=`#ifdef USE_MORPHNORMALS
	objectNormal *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) objectNormal += getMorph( gl_VertexID, i, 1 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,ZT=`#ifdef USE_MORPHTARGETS
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
#endif`,jT=`#ifdef USE_MORPHTARGETS
	transformed *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) transformed += getMorph( gl_VertexID, i, 0 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,KT=`float faceDirection = gl_FrontFacing ? 1.0 : - 1.0;
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
vec3 nonPerturbedNormal = normal;`,QT=`#ifdef USE_NORMALMAP_OBJECTSPACE
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
#endif`,JT=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,$T=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,tA=`#ifndef FLAT_SHADED
	vNormal = normalize( transformedNormal );
	#ifdef USE_TANGENT
		vTangent = normalize( transformedTangent );
		vBitangent = normalize( cross( vNormal, vTangent ) * tangent.w );
		#ifdef FLIP_SIDED
			vBitangent = - vBitangent;
		#endif
	#endif
#endif`,eA=`#ifdef USE_NORMALMAP
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
#endif`,nA=`#ifdef USE_CLEARCOAT
	vec3 clearcoatNormal = nonPerturbedNormal;
#endif`,iA=`#ifdef USE_CLEARCOAT_NORMALMAP
	vec3 clearcoatMapN = texture2D( clearcoatNormalMap, vClearcoatNormalMapUv ).xyz * 2.0 - 1.0;
	clearcoatMapN.xy *= clearcoatNormalScale;
	clearcoatNormal = normalize( tbn2 * clearcoatMapN );
#endif`,aA=`#ifdef USE_CLEARCOATMAP
	uniform sampler2D clearcoatMap;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform sampler2D clearcoatNormalMap;
	uniform vec2 clearcoatNormalScale;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform sampler2D clearcoatRoughnessMap;
#endif`,rA=`#ifdef USE_IRIDESCENCEMAP
	uniform sampler2D iridescenceMap;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform sampler2D iridescenceThicknessMap;
#endif`,sA=`#ifdef OPAQUE
diffuseColor.a = 1.0;
#endif
#ifdef USE_TRANSMISSION
diffuseColor.a *= material.transmissionAlpha;
#endif
gl_FragColor = vec4( outgoingLight, diffuseColor.a );`,oA=`vec3 packNormalToRGB( const in vec3 normal ) {
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
}`,lA=`#ifdef PREMULTIPLIED_ALPHA
	gl_FragColor.rgb *= gl_FragColor.a;
#endif`,cA=`vec4 mvPosition = vec4( transformed, 1.0 );
#ifdef USE_BATCHING
	mvPosition = batchingMatrix * mvPosition;
#endif
#ifdef USE_INSTANCING
	mvPosition = instanceMatrix * mvPosition;
#endif
mvPosition = modelViewMatrix * mvPosition;
gl_Position = projectionMatrix * mvPosition;`,uA=`#ifdef DITHERING
	gl_FragColor.rgb = dithering( gl_FragColor.rgb );
#endif`,fA=`#ifdef DITHERING
	vec3 dithering( vec3 color ) {
		float grid_position = rand( gl_FragCoord.xy );
		vec3 dither_shift_RGB = vec3( 0.25 / 255.0, -0.25 / 255.0, 0.25 / 255.0 );
		dither_shift_RGB = mix( 2.0 * dither_shift_RGB, -2.0 * dither_shift_RGB, grid_position );
		return color + dither_shift_RGB;
	}
#endif`,dA=`float roughnessFactor = roughness;
#ifdef USE_ROUGHNESSMAP
	vec4 texelRoughness = texture2D( roughnessMap, vRoughnessMapUv );
	roughnessFactor *= texelRoughness.g;
#endif`,hA=`#ifdef USE_ROUGHNESSMAP
	uniform sampler2D roughnessMap;
#endif`,pA=`#if NUM_SPOT_LIGHT_COORDS > 0
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
#endif`,mA=`#if NUM_SPOT_LIGHT_COORDS > 0
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
#endif`,gA=`#if ( defined( USE_SHADOWMAP ) && ( NUM_DIR_LIGHT_SHADOWS > 0 || NUM_POINT_LIGHT_SHADOWS > 0 ) ) || ( NUM_SPOT_LIGHT_COORDS > 0 )
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
#endif`,_A=`float getShadowMask() {
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
}`,vA=`#ifdef USE_SKINNING
	mat4 boneMatX = getBoneMatrix( skinIndex.x );
	mat4 boneMatY = getBoneMatrix( skinIndex.y );
	mat4 boneMatZ = getBoneMatrix( skinIndex.z );
	mat4 boneMatW = getBoneMatrix( skinIndex.w );
#endif`,xA=`#ifdef USE_SKINNING
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
#endif`,SA=`#ifdef USE_SKINNING
	vec4 skinVertex = bindMatrix * vec4( transformed, 1.0 );
	vec4 skinned = vec4( 0.0 );
	skinned += boneMatX * skinVertex * skinWeight.x;
	skinned += boneMatY * skinVertex * skinWeight.y;
	skinned += boneMatZ * skinVertex * skinWeight.z;
	skinned += boneMatW * skinVertex * skinWeight.w;
	transformed = ( bindMatrixInverse * skinned ).xyz;
#endif`,yA=`#ifdef USE_SKINNING
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
#endif`,MA=`float specularStrength;
#ifdef USE_SPECULARMAP
	vec4 texelSpecular = texture2D( specularMap, vSpecularMapUv );
	specularStrength = texelSpecular.r;
#else
	specularStrength = 1.0;
#endif`,EA=`#ifdef USE_SPECULARMAP
	uniform sampler2D specularMap;
#endif`,bA=`#if defined( TONE_MAPPING )
	gl_FragColor.rgb = toneMapping( gl_FragColor.rgb );
#endif`,TA=`#ifndef saturate
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
vec3 CustomToneMapping( vec3 color ) { return color; }`,AA=`#ifdef USE_TRANSMISSION
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
#endif`,RA=`#ifdef USE_TRANSMISSION
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
#endif`,CA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,wA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,DA=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,NA=`#if defined( USE_ENVMAP ) || defined( DISTANCE ) || defined ( USE_SHADOWMAP ) || defined ( USE_TRANSMISSION ) || NUM_SPOT_LIGHT_COORDS > 0
	vec4 worldPosition = vec4( transformed, 1.0 );
	#ifdef USE_BATCHING
		worldPosition = batchingMatrix * worldPosition;
	#endif
	#ifdef USE_INSTANCING
		worldPosition = instanceMatrix * worldPosition;
	#endif
	worldPosition = modelMatrix * worldPosition;
#endif`;const UA=`varying vec2 vUv;
uniform mat3 uvTransform;
void main() {
	vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	gl_Position = vec4( position.xy, 1.0, 1.0 );
}`,LA=`uniform sampler2D t2D;
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
}`,OA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,PA=`#ifdef ENVMAP_TYPE_CUBE
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
}`,BA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,IA=`uniform samplerCube tCube;
uniform float tFlip;
uniform float opacity;
varying vec3 vWorldDirection;
void main() {
	vec4 texColor = textureCube( tCube, vec3( tFlip * vWorldDirection.x, vWorldDirection.yz ) );
	gl_FragColor = texColor;
	gl_FragColor.a *= opacity;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,zA=`#include <common>
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
}`,FA=`#if DEPTH_PACKING == 3200
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
}`,HA=`#define DISTANCE
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
}`,GA=`#define DISTANCE
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
}`,VA=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
}`,kA=`uniform sampler2D tEquirect;
varying vec3 vWorldDirection;
#include <common>
void main() {
	vec3 direction = normalize( vWorldDirection );
	vec2 sampleUV = equirectUv( direction );
	gl_FragColor = texture2D( tEquirect, sampleUV );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,XA=`uniform float scale;
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
}`,qA=`uniform vec3 diffuse;
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
}`,WA=`#include <common>
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
}`,YA=`uniform vec3 diffuse;
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
}`,ZA=`#define LAMBERT
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
}`,jA=`#define LAMBERT
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
}`,KA=`#define MATCAP
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
}`,QA=`#define MATCAP
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
}`,JA=`#define NORMAL
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
}`,$A=`#define NORMAL
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
}`,t2=`#define PHONG
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
}`,e2=`#define PHONG
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
}`,n2=`#define STANDARD
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
}`,i2=`#define STANDARD
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
}`,a2=`#define TOON
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
}`,r2=`#define TOON
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
}`,s2=`uniform float size;
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
}`,o2=`uniform vec3 diffuse;
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
}`,l2=`#include <common>
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
}`,c2=`uniform vec3 color;
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
}`,u2=`uniform float rotation;
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
}`,f2=`uniform vec3 diffuse;
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
}`,pe={alphahash_fragment:Ub,alphahash_pars_fragment:Lb,alphamap_fragment:Ob,alphamap_pars_fragment:Pb,alphatest_fragment:Bb,alphatest_pars_fragment:Ib,aomap_fragment:zb,aomap_pars_fragment:Fb,batching_pars_vertex:Hb,batching_vertex:Gb,begin_vertex:Vb,beginnormal_vertex:kb,bsdfs:Xb,iridescence_fragment:qb,bumpmap_pars_fragment:Wb,clipping_planes_fragment:Yb,clipping_planes_pars_fragment:Zb,clipping_planes_pars_vertex:jb,clipping_planes_vertex:Kb,color_fragment:Qb,color_pars_fragment:Jb,color_pars_vertex:$b,color_vertex:tT,common:eT,cube_uv_reflection_fragment:nT,defaultnormal_vertex:iT,displacementmap_pars_vertex:aT,displacementmap_vertex:rT,emissivemap_fragment:sT,emissivemap_pars_fragment:oT,colorspace_fragment:lT,colorspace_pars_fragment:cT,envmap_fragment:uT,envmap_common_pars_fragment:fT,envmap_pars_fragment:dT,envmap_pars_vertex:hT,envmap_physical_pars_fragment:bT,envmap_vertex:pT,fog_vertex:mT,fog_pars_vertex:gT,fog_fragment:_T,fog_pars_fragment:vT,gradientmap_pars_fragment:xT,lightmap_pars_fragment:ST,lights_lambert_fragment:yT,lights_lambert_pars_fragment:MT,lights_pars_begin:ET,lights_toon_fragment:TT,lights_toon_pars_fragment:AT,lights_phong_fragment:RT,lights_phong_pars_fragment:CT,lights_physical_fragment:wT,lights_physical_pars_fragment:DT,lights_fragment_begin:NT,lights_fragment_maps:UT,lights_fragment_end:LT,lightprobes_pars_fragment:OT,logdepthbuf_fragment:PT,logdepthbuf_pars_fragment:BT,logdepthbuf_pars_vertex:IT,logdepthbuf_vertex:zT,map_fragment:FT,map_pars_fragment:HT,map_particle_fragment:GT,map_particle_pars_fragment:VT,metalnessmap_fragment:kT,metalnessmap_pars_fragment:XT,morphinstance_vertex:qT,morphcolor_vertex:WT,morphnormal_vertex:YT,morphtarget_pars_vertex:ZT,morphtarget_vertex:jT,normal_fragment_begin:KT,normal_fragment_maps:QT,normal_pars_fragment:JT,normal_pars_vertex:$T,normal_vertex:tA,normalmap_pars_fragment:eA,clearcoat_normal_fragment_begin:nA,clearcoat_normal_fragment_maps:iA,clearcoat_pars_fragment:aA,iridescence_pars_fragment:rA,opaque_fragment:sA,packing:oA,premultiplied_alpha_fragment:lA,project_vertex:cA,dithering_fragment:uA,dithering_pars_fragment:fA,roughnessmap_fragment:dA,roughnessmap_pars_fragment:hA,shadowmap_pars_fragment:pA,shadowmap_pars_vertex:mA,shadowmap_vertex:gA,shadowmask_pars_fragment:_A,skinbase_vertex:vA,skinning_pars_vertex:xA,skinning_vertex:SA,skinnormal_vertex:yA,specularmap_fragment:MA,specularmap_pars_fragment:EA,tonemapping_fragment:bA,tonemapping_pars_fragment:TA,transmission_fragment:AA,transmission_pars_fragment:RA,uv_pars_fragment:CA,uv_pars_vertex:wA,uv_vertex:DA,worldpos_vertex:NA,background_vert:UA,background_frag:LA,backgroundCube_vert:OA,backgroundCube_frag:PA,cube_vert:BA,cube_frag:IA,depth_vert:zA,depth_frag:FA,distance_vert:HA,distance_frag:GA,equirect_vert:VA,equirect_frag:kA,linedashed_vert:XA,linedashed_frag:qA,meshbasic_vert:WA,meshbasic_frag:YA,meshlambert_vert:ZA,meshlambert_frag:jA,meshmatcap_vert:KA,meshmatcap_frag:QA,meshnormal_vert:JA,meshnormal_frag:$A,meshphong_vert:t2,meshphong_frag:e2,meshphysical_vert:n2,meshphysical_frag:i2,meshtoon_vert:a2,meshtoon_frag:r2,points_vert:s2,points_frag:o2,shadow_vert:l2,shadow_frag:c2,sprite_vert:u2,sprite_frag:f2},zt={common:{diffuse:{value:new me(16777215)},opacity:{value:1},map:{value:null},mapTransform:{value:new ce},alphaMap:{value:null},alphaMapTransform:{value:new ce},alphaTest:{value:0}},specularmap:{specularMap:{value:null},specularMapTransform:{value:new ce}},envmap:{envMap:{value:null},envMapRotation:{value:new ce},reflectivity:{value:1},ior:{value:1.5},refractionRatio:{value:.98},dfgLUT:{value:null}},aomap:{aoMap:{value:null},aoMapIntensity:{value:1},aoMapTransform:{value:new ce}},lightmap:{lightMap:{value:null},lightMapIntensity:{value:1},lightMapTransform:{value:new ce}},bumpmap:{bumpMap:{value:null},bumpMapTransform:{value:new ce},bumpScale:{value:1}},normalmap:{normalMap:{value:null},normalMapTransform:{value:new ce},normalScale:{value:new Ae(1,1)}},displacementmap:{displacementMap:{value:null},displacementMapTransform:{value:new ce},displacementScale:{value:1},displacementBias:{value:0}},emissivemap:{emissiveMap:{value:null},emissiveMapTransform:{value:new ce}},metalnessmap:{metalnessMap:{value:null},metalnessMapTransform:{value:new ce}},roughnessmap:{roughnessMap:{value:null},roughnessMapTransform:{value:new ce}},gradientmap:{gradientMap:{value:null}},fog:{fogDensity:{value:25e-5},fogNear:{value:1},fogFar:{value:2e3},fogColor:{value:new me(16777215)}},lights:{ambientLightColor:{value:[]},lightProbe:{value:[]},directionalLights:{value:[],properties:{direction:{},color:{}}},directionalLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},directionalShadowMatrix:{value:[]},spotLights:{value:[],properties:{color:{},position:{},direction:{},distance:{},coneCos:{},penumbraCos:{},decay:{}}},spotLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},spotLightMap:{value:[]},spotLightMatrix:{value:[]},pointLights:{value:[],properties:{color:{},position:{},decay:{},distance:{}}},pointLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{},shadowCameraNear:{},shadowCameraFar:{}}},pointShadowMatrix:{value:[]},hemisphereLights:{value:[],properties:{direction:{},skyColor:{},groundColor:{}}},rectAreaLights:{value:[],properties:{color:{},position:{},width:{},height:{}}},ltc_1:{value:null},ltc_2:{value:null},probesSH:{value:null},probesMin:{value:new Q},probesMax:{value:new Q},probesResolution:{value:new Q}},points:{diffuse:{value:new me(16777215)},opacity:{value:1},size:{value:1},scale:{value:1},map:{value:null},alphaMap:{value:null},alphaMapTransform:{value:new ce},alphaTest:{value:0},uvTransform:{value:new ce}},sprite:{diffuse:{value:new me(16777215)},opacity:{value:1},center:{value:new Ae(.5,.5)},rotation:{value:0},map:{value:null},mapTransform:{value:new ce},alphaMap:{value:null},alphaMapTransform:{value:new ce},alphaTest:{value:0}}},Wi={basic:{uniforms:Xn([zt.common,zt.specularmap,zt.envmap,zt.aomap,zt.lightmap,zt.fog]),vertexShader:pe.meshbasic_vert,fragmentShader:pe.meshbasic_frag},lambert:{uniforms:Xn([zt.common,zt.specularmap,zt.envmap,zt.aomap,zt.lightmap,zt.emissivemap,zt.bumpmap,zt.normalmap,zt.displacementmap,zt.fog,zt.lights,{emissive:{value:new me(0)},envMapIntensity:{value:1}}]),vertexShader:pe.meshlambert_vert,fragmentShader:pe.meshlambert_frag},phong:{uniforms:Xn([zt.common,zt.specularmap,zt.envmap,zt.aomap,zt.lightmap,zt.emissivemap,zt.bumpmap,zt.normalmap,zt.displacementmap,zt.fog,zt.lights,{emissive:{value:new me(0)},specular:{value:new me(1118481)},shininess:{value:30},envMapIntensity:{value:1}}]),vertexShader:pe.meshphong_vert,fragmentShader:pe.meshphong_frag},standard:{uniforms:Xn([zt.common,zt.envmap,zt.aomap,zt.lightmap,zt.emissivemap,zt.bumpmap,zt.normalmap,zt.displacementmap,zt.roughnessmap,zt.metalnessmap,zt.fog,zt.lights,{emissive:{value:new me(0)},roughness:{value:1},metalness:{value:0},envMapIntensity:{value:1}}]),vertexShader:pe.meshphysical_vert,fragmentShader:pe.meshphysical_frag},toon:{uniforms:Xn([zt.common,zt.aomap,zt.lightmap,zt.emissivemap,zt.bumpmap,zt.normalmap,zt.displacementmap,zt.gradientmap,zt.fog,zt.lights,{emissive:{value:new me(0)}}]),vertexShader:pe.meshtoon_vert,fragmentShader:pe.meshtoon_frag},matcap:{uniforms:Xn([zt.common,zt.bumpmap,zt.normalmap,zt.displacementmap,zt.fog,{matcap:{value:null}}]),vertexShader:pe.meshmatcap_vert,fragmentShader:pe.meshmatcap_frag},points:{uniforms:Xn([zt.points,zt.fog]),vertexShader:pe.points_vert,fragmentShader:pe.points_frag},dashed:{uniforms:Xn([zt.common,zt.fog,{scale:{value:1},dashSize:{value:1},totalSize:{value:2}}]),vertexShader:pe.linedashed_vert,fragmentShader:pe.linedashed_frag},depth:{uniforms:Xn([zt.common,zt.displacementmap]),vertexShader:pe.depth_vert,fragmentShader:pe.depth_frag},normal:{uniforms:Xn([zt.common,zt.bumpmap,zt.normalmap,zt.displacementmap,{opacity:{value:1}}]),vertexShader:pe.meshnormal_vert,fragmentShader:pe.meshnormal_frag},sprite:{uniforms:Xn([zt.sprite,zt.fog]),vertexShader:pe.sprite_vert,fragmentShader:pe.sprite_frag},background:{uniforms:{uvTransform:{value:new ce},t2D:{value:null},backgroundIntensity:{value:1}},vertexShader:pe.background_vert,fragmentShader:pe.background_frag},backgroundCube:{uniforms:{envMap:{value:null},backgroundBlurriness:{value:0},backgroundIntensity:{value:1},backgroundRotation:{value:new ce}},vertexShader:pe.backgroundCube_vert,fragmentShader:pe.backgroundCube_frag},cube:{uniforms:{tCube:{value:null},tFlip:{value:-1},opacity:{value:1}},vertexShader:pe.cube_vert,fragmentShader:pe.cube_frag},equirect:{uniforms:{tEquirect:{value:null}},vertexShader:pe.equirect_vert,fragmentShader:pe.equirect_frag},distance:{uniforms:Xn([zt.common,zt.displacementmap,{referencePosition:{value:new Q},nearDistance:{value:1},farDistance:{value:1e3}}]),vertexShader:pe.distance_vert,fragmentShader:pe.distance_frag},shadow:{uniforms:Xn([zt.lights,zt.fog,{color:{value:new me(0)},opacity:{value:1}}]),vertexShader:pe.shadow_vert,fragmentShader:pe.shadow_frag}};Wi.physical={uniforms:Xn([Wi.standard.uniforms,{clearcoat:{value:0},clearcoatMap:{value:null},clearcoatMapTransform:{value:new ce},clearcoatNormalMap:{value:null},clearcoatNormalMapTransform:{value:new ce},clearcoatNormalScale:{value:new Ae(1,1)},clearcoatRoughness:{value:0},clearcoatRoughnessMap:{value:null},clearcoatRoughnessMapTransform:{value:new ce},dispersion:{value:0},iridescence:{value:0},iridescenceMap:{value:null},iridescenceMapTransform:{value:new ce},iridescenceIOR:{value:1.3},iridescenceThicknessMinimum:{value:100},iridescenceThicknessMaximum:{value:400},iridescenceThicknessMap:{value:null},iridescenceThicknessMapTransform:{value:new ce},sheen:{value:0},sheenColor:{value:new me(0)},sheenColorMap:{value:null},sheenColorMapTransform:{value:new ce},sheenRoughness:{value:1},sheenRoughnessMap:{value:null},sheenRoughnessMapTransform:{value:new ce},transmission:{value:0},transmissionMap:{value:null},transmissionMapTransform:{value:new ce},transmissionSamplerSize:{value:new Ae},transmissionSamplerMap:{value:null},thickness:{value:0},thicknessMap:{value:null},thicknessMapTransform:{value:new ce},attenuationDistance:{value:0},attenuationColor:{value:new me(0)},specularColor:{value:new me(1,1,1)},specularColorMap:{value:null},specularColorMapTransform:{value:new ce},specularIntensity:{value:1},specularIntensityMap:{value:null},specularIntensityMapTransform:{value:new ce},anisotropyVector:{value:new Ae},anisotropyMap:{value:null},anisotropyMapTransform:{value:new ce}}]),vertexShader:pe.meshphysical_vert,fragmentShader:pe.meshphysical_frag};const iu={r:0,b:0,g:0},d2=new $e,SS=new ce;SS.set(-1,0,0,0,1,0,0,0,1);function h2(s,t,i,r,l,c){const f=new me(0);let h=l===!0?0:1,m,p,_=null,v=0,g=null;function x(P){let O=P.isScene===!0?P.background:null;if(O&&O.isTexture){const C=P.backgroundBlurriness>0;O=t.get(O,C)}return O}function M(P){let O=!1;const C=x(P);C===null?E(f,h):C&&C.isColor&&(E(C,1),O=!0);const z=s.xr.getEnvironmentBlendMode();z==="additive"?i.buffers.color.setClear(0,0,0,1,c):z==="alpha-blend"&&i.buffers.color.setClear(0,0,0,0,c),(s.autoClear||O)&&(i.buffers.depth.setTest(!0),i.buffers.depth.setMask(!0),i.buffers.color.setMask(!0),s.clear(s.autoClearColor,s.autoClearDepth,s.autoClearStencil))}function A(P,O){const C=x(O);C&&(C.isCubeTexture||C.mapping===Su)?(p===void 0&&(p=new Ye(new En(1,1,1),new ta({name:"BackgroundCubeMaterial",uniforms:io(Wi.backgroundCube.uniforms),vertexShader:Wi.backgroundCube.vertexShader,fragmentShader:Wi.backgroundCube.fragmentShader,side:Jn,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),p.geometry.deleteAttribute("normal"),p.geometry.deleteAttribute("uv"),p.onBeforeRender=function(z,D,U){this.matrixWorld.copyPosition(U.matrixWorld)},Object.defineProperty(p.material,"envMap",{get:function(){return this.uniforms.envMap.value}}),r.update(p)),p.material.uniforms.envMap.value=C,p.material.uniforms.backgroundBlurriness.value=O.backgroundBlurriness,p.material.uniforms.backgroundIntensity.value=O.backgroundIntensity,p.material.uniforms.backgroundRotation.value.setFromMatrix4(d2.makeRotationFromEuler(O.backgroundRotation)).transpose(),C.isCubeTexture&&C.isRenderTargetTexture===!1&&p.material.uniforms.backgroundRotation.value.premultiply(SS),p.material.toneMapped=Te.getTransfer(C.colorSpace)!==Ve,(_!==C||v!==C.version||g!==s.toneMapping)&&(p.material.needsUpdate=!0,_=C,v=C.version,g=s.toneMapping),p.layers.enableAll(),P.unshift(p,p.geometry,p.material,0,0,null)):C&&C.isTexture&&(m===void 0&&(m=new Ye(new Ji(2,2),new ta({name:"BackgroundMaterial",uniforms:io(Wi.background.uniforms),vertexShader:Wi.background.vertexShader,fragmentShader:Wi.background.fragmentShader,side:pr,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),m.geometry.deleteAttribute("normal"),Object.defineProperty(m.material,"map",{get:function(){return this.uniforms.t2D.value}}),r.update(m)),m.material.uniforms.t2D.value=C,m.material.uniforms.backgroundIntensity.value=O.backgroundIntensity,m.material.toneMapped=Te.getTransfer(C.colorSpace)!==Ve,C.matrixAutoUpdate===!0&&C.updateMatrix(),m.material.uniforms.uvTransform.value.copy(C.matrix),(_!==C||v!==C.version||g!==s.toneMapping)&&(m.material.needsUpdate=!0,_=C,v=C.version,g=s.toneMapping),m.layers.enableAll(),P.unshift(m,m.geometry,m.material,0,0,null))}function E(P,O){P.getRGB(iu,gS(s)),i.buffers.color.setClear(iu.r,iu.g,iu.b,O,c)}function S(){p!==void 0&&(p.geometry.dispose(),p.material.dispose(),p=void 0),m!==void 0&&(m.geometry.dispose(),m.material.dispose(),m=void 0)}return{getClearColor:function(){return f},setClearColor:function(P,O=1){f.set(P),h=O,E(f,h)},getClearAlpha:function(){return h},setClearAlpha:function(P){h=P,E(f,h)},render:M,addToRenderList:A,dispose:S}}function p2(s,t){const i=s.getParameter(s.MAX_VERTEX_ATTRIBS),r={},l=g(null);let c=l,f=!1;function h(F,k,K,tt,$){let I=!1;const X=v(F,tt,K,k);c!==X&&(c=X,p(c.object)),I=x(F,tt,K,$),I&&M(F,tt,K,$),$!==null&&t.update($,s.ELEMENT_ARRAY_BUFFER),(I||f)&&(f=!1,C(F,k,K,tt),$!==null&&s.bindBuffer(s.ELEMENT_ARRAY_BUFFER,t.get($).buffer))}function m(){return s.createVertexArray()}function p(F){return s.bindVertexArray(F)}function _(F){return s.deleteVertexArray(F)}function v(F,k,K,tt){const $=tt.wireframe===!0;let I=r[k.id];I===void 0&&(I={},r[k.id]=I);const X=F.isInstancedMesh===!0?F.id:0;let at=I[X];at===void 0&&(at={},I[X]=at);let _t=at[K.id];_t===void 0&&(_t={},at[K.id]=_t);let vt=_t[$];return vt===void 0&&(vt=g(m()),_t[$]=vt),vt}function g(F){const k=[],K=[],tt=[];for(let $=0;$<i;$++)k[$]=0,K[$]=0,tt[$]=0;return{geometry:null,program:null,wireframe:!1,newAttributes:k,enabledAttributes:K,attributeDivisors:tt,object:F,attributes:{},index:null}}function x(F,k,K,tt){const $=c.attributes,I=k.attributes;let X=0;const at=K.getAttributes();for(const _t in at)if(at[_t].location>=0){const L=$[_t];let Y=I[_t];if(Y===void 0&&(_t==="instanceMatrix"&&F.instanceMatrix&&(Y=F.instanceMatrix),_t==="instanceColor"&&F.instanceColor&&(Y=F.instanceColor)),L===void 0||L.attribute!==Y||Y&&L.data!==Y.data)return!0;X++}return c.attributesNum!==X||c.index!==tt}function M(F,k,K,tt){const $={},I=k.attributes;let X=0;const at=K.getAttributes();for(const _t in at)if(at[_t].location>=0){let L=I[_t];L===void 0&&(_t==="instanceMatrix"&&F.instanceMatrix&&(L=F.instanceMatrix),_t==="instanceColor"&&F.instanceColor&&(L=F.instanceColor));const Y={};Y.attribute=L,L&&L.data&&(Y.data=L.data),$[_t]=Y,X++}c.attributes=$,c.attributesNum=X,c.index=tt}function A(){const F=c.newAttributes;for(let k=0,K=F.length;k<K;k++)F[k]=0}function E(F){S(F,0)}function S(F,k){const K=c.newAttributes,tt=c.enabledAttributes,$=c.attributeDivisors;K[F]=1,tt[F]===0&&(s.enableVertexAttribArray(F),tt[F]=1),$[F]!==k&&(s.vertexAttribDivisor(F,k),$[F]=k)}function P(){const F=c.newAttributes,k=c.enabledAttributes;for(let K=0,tt=k.length;K<tt;K++)k[K]!==F[K]&&(s.disableVertexAttribArray(K),k[K]=0)}function O(F,k,K,tt,$,I,X){X===!0?s.vertexAttribIPointer(F,k,K,$,I):s.vertexAttribPointer(F,k,K,tt,$,I)}function C(F,k,K,tt){A();const $=tt.attributes,I=K.getAttributes(),X=k.defaultAttributeValues;for(const at in I){const _t=I[at];if(_t.location>=0){let vt=$[at];if(vt===void 0&&(at==="instanceMatrix"&&F.instanceMatrix&&(vt=F.instanceMatrix),at==="instanceColor"&&F.instanceColor&&(vt=F.instanceColor)),vt!==void 0){const L=vt.normalized,Y=vt.itemSize,yt=t.get(vt);if(yt===void 0)continue;const At=yt.buffer,Dt=yt.type,it=yt.bytesPerElement,nt=Dt===s.INT||Dt===s.UNSIGNED_INT||vt.gpuType===Ip;if(vt.isInterleavedBufferAttribute){const ct=vt.data,bt=ct.stride,Ht=vt.offset;if(ct.isInstancedInterleavedBuffer){for(let Bt=0;Bt<_t.locationSize;Bt++)S(_t.location+Bt,ct.meshPerAttribute);F.isInstancedMesh!==!0&&tt._maxInstanceCount===void 0&&(tt._maxInstanceCount=ct.meshPerAttribute*ct.count)}else for(let Bt=0;Bt<_t.locationSize;Bt++)E(_t.location+Bt);s.bindBuffer(s.ARRAY_BUFFER,At);for(let Bt=0;Bt<_t.locationSize;Bt++)O(_t.location+Bt,Y/_t.locationSize,Dt,L,bt*it,(Ht+Y/_t.locationSize*Bt)*it,nt)}else{if(vt.isInstancedBufferAttribute){for(let ct=0;ct<_t.locationSize;ct++)S(_t.location+ct,vt.meshPerAttribute);F.isInstancedMesh!==!0&&tt._maxInstanceCount===void 0&&(tt._maxInstanceCount=vt.meshPerAttribute*vt.count)}else for(let ct=0;ct<_t.locationSize;ct++)E(_t.location+ct);s.bindBuffer(s.ARRAY_BUFFER,At);for(let ct=0;ct<_t.locationSize;ct++)O(_t.location+ct,Y/_t.locationSize,Dt,L,Y*it,Y/_t.locationSize*ct*it,nt)}}else if(X!==void 0){const L=X[at];if(L!==void 0)switch(L.length){case 2:s.vertexAttrib2fv(_t.location,L);break;case 3:s.vertexAttrib3fv(_t.location,L);break;case 4:s.vertexAttrib4fv(_t.location,L);break;default:s.vertexAttrib1fv(_t.location,L)}}}}P()}function z(){N();for(const F in r){const k=r[F];for(const K in k){const tt=k[K];for(const $ in tt){const I=tt[$];for(const X in I)_(I[X].object),delete I[X];delete tt[$]}}delete r[F]}}function D(F){if(r[F.id]===void 0)return;const k=r[F.id];for(const K in k){const tt=k[K];for(const $ in tt){const I=tt[$];for(const X in I)_(I[X].object),delete I[X];delete tt[$]}}delete r[F.id]}function U(F){for(const k in r){const K=r[k];for(const tt in K){const $=K[tt];if($[F.id]===void 0)continue;const I=$[F.id];for(const X in I)_(I[X].object),delete I[X];delete $[F.id]}}}function b(F){for(const k in r){const K=r[k],tt=F.isInstancedMesh===!0?F.id:0,$=K[tt];if($!==void 0){for(const I in $){const X=$[I];for(const at in X)_(X[at].object),delete X[at];delete $[I]}delete K[tt],Object.keys(K).length===0&&delete r[k]}}}function N(){G(),f=!0,c!==l&&(c=l,p(c.object))}function G(){l.geometry=null,l.program=null,l.wireframe=!1}return{setup:h,reset:N,resetDefaultState:G,dispose:z,releaseStatesOfGeometry:D,releaseStatesOfObject:b,releaseStatesOfProgram:U,initAttributes:A,enableAttribute:E,disableUnusedAttributes:P}}function m2(s,t,i){let r;function l(m){r=m}function c(m,p){s.drawArrays(r,m,p),i.update(p,r,1)}function f(m,p,_){_!==0&&(s.drawArraysInstanced(r,m,p,_),i.update(p,r,_))}function h(m,p,_){if(_===0)return;t.get("WEBGL_multi_draw").multiDrawArraysWEBGL(r,m,0,p,0,_);let g=0;for(let x=0;x<_;x++)g+=p[x];i.update(g,r,1)}this.setMode=l,this.render=c,this.renderInstances=f,this.renderMultiDraw=h}function g2(s,t,i,r){let l;function c(){if(l!==void 0)return l;if(t.has("EXT_texture_filter_anisotropic")===!0){const U=t.get("EXT_texture_filter_anisotropic");l=s.getParameter(U.MAX_TEXTURE_MAX_ANISOTROPY_EXT)}else l=0;return l}function f(U){return!(U!==Pi&&r.convert(U)!==s.getParameter(s.IMPLEMENTATION_COLOR_READ_FORMAT))}function h(U){const b=U===Da&&(t.has("EXT_color_buffer_half_float")||t.has("EXT_color_buffer_float"));return!(U!==pi&&r.convert(U)!==s.getParameter(s.IMPLEMENTATION_COLOR_READ_TYPE)&&U!==Zi&&!b)}function m(U){if(U==="highp"){if(s.getShaderPrecisionFormat(s.VERTEX_SHADER,s.HIGH_FLOAT).precision>0&&s.getShaderPrecisionFormat(s.FRAGMENT_SHADER,s.HIGH_FLOAT).precision>0)return"highp";U="mediump"}return U==="mediump"&&s.getShaderPrecisionFormat(s.VERTEX_SHADER,s.MEDIUM_FLOAT).precision>0&&s.getShaderPrecisionFormat(s.FRAGMENT_SHADER,s.MEDIUM_FLOAT).precision>0?"mediump":"lowp"}let p=i.precision!==void 0?i.precision:"highp";const _=m(p);_!==p&&(se("WebGLRenderer:",p,"not supported, using",_,"instead."),p=_);const v=i.logarithmicDepthBuffer===!0,g=i.reversedDepthBuffer===!0&&t.has("EXT_clip_control");i.reversedDepthBuffer===!0&&g===!1&&se("WebGLRenderer: Unable to use reversed depth buffer due to missing EXT_clip_control extension. Fallback to default depth buffer.");const x=s.getParameter(s.MAX_TEXTURE_IMAGE_UNITS),M=s.getParameter(s.MAX_VERTEX_TEXTURE_IMAGE_UNITS),A=s.getParameter(s.MAX_TEXTURE_SIZE),E=s.getParameter(s.MAX_CUBE_MAP_TEXTURE_SIZE),S=s.getParameter(s.MAX_VERTEX_ATTRIBS),P=s.getParameter(s.MAX_VERTEX_UNIFORM_VECTORS),O=s.getParameter(s.MAX_VARYING_VECTORS),C=s.getParameter(s.MAX_FRAGMENT_UNIFORM_VECTORS),z=s.getParameter(s.MAX_SAMPLES),D=s.getParameter(s.SAMPLES);return{isWebGL2:!0,getMaxAnisotropy:c,getMaxPrecision:m,textureFormatReadable:f,textureTypeReadable:h,precision:p,logarithmicDepthBuffer:v,reversedDepthBuffer:g,maxTextures:x,maxVertexTextures:M,maxTextureSize:A,maxCubemapSize:E,maxAttributes:S,maxVertexUniforms:P,maxVaryings:O,maxFragmentUniforms:C,maxSamples:z,samples:D}}function _2(s){const t=this;let i=null,r=0,l=!1,c=!1;const f=new Hr,h=new ce,m={value:null,needsUpdate:!1};this.uniform=m,this.numPlanes=0,this.numIntersection=0,this.init=function(v,g){const x=v.length!==0||g||r!==0||l;return l=g,r=v.length,x},this.beginShadows=function(){c=!0,_(null)},this.endShadows=function(){c=!1},this.setGlobalState=function(v,g){i=_(v,g,0)},this.setState=function(v,g,x){const M=v.clippingPlanes,A=v.clipIntersection,E=v.clipShadows,S=s.get(v);if(!l||M===null||M.length===0||c&&!E)c?_(null):p();else{const P=c?0:r,O=P*4;let C=S.clippingState||null;m.value=C,C=_(M,g,O,x);for(let z=0;z!==O;++z)C[z]=i[z];S.clippingState=C,this.numIntersection=A?this.numPlanes:0,this.numPlanes+=P}};function p(){m.value!==i&&(m.value=i,m.needsUpdate=r>0),t.numPlanes=r,t.numIntersection=0}function _(v,g,x,M){const A=v!==null?v.length:0;let E=null;if(A!==0){if(E=m.value,M!==!0||E===null){const S=x+A*4,P=g.matrixWorldInverse;h.getNormalMatrix(P),(E===null||E.length<S)&&(E=new Float32Array(S));for(let O=0,C=x;O!==A;++O,C+=4)f.copy(v[O]).applyMatrix4(P,h),f.normal.toArray(E,C),E[C+3]=f.constant}m.value=E,m.needsUpdate=!0}return t.numPlanes=A,t.numIntersection=0,E}}const hr=4,Gv=[.125,.215,.35,.446,.526,.582],Vr=20,v2=256,al=new $p,Vv=new me;let fh=null,dh=0,hh=0,ph=!1;const x2=new Q;class kv{constructor(t){this._renderer=t,this._pingPongRenderTarget=null,this._lodMax=0,this._cubeSize=0,this._sizeLods=[],this._sigmas=[],this._lodMeshes=[],this._backgroundBox=null,this._cubemapMaterial=null,this._equirectMaterial=null,this._blurMaterial=null,this._ggxMaterial=null}fromScene(t,i=0,r=.1,l=100,c={}){const{size:f=256,position:h=x2}=c;fh=this._renderer.getRenderTarget(),dh=this._renderer.getActiveCubeFace(),hh=this._renderer.getActiveMipmapLevel(),ph=this._renderer.xr.enabled,this._renderer.xr.enabled=!1,this._setSize(f);const m=this._allocateTargets();return m.depthBuffer=!0,this._sceneToCubeUV(t,r,l,m,h),i>0&&this._blur(m,0,0,i),this._applyPMREM(m),this._cleanup(m),m}fromEquirectangular(t,i=null){return this._fromTexture(t,i)}fromCubemap(t,i=null){return this._fromTexture(t,i)}compileCubemapShader(){this._cubemapMaterial===null&&(this._cubemapMaterial=Wv(),this._compileMaterial(this._cubemapMaterial))}compileEquirectangularShader(){this._equirectMaterial===null&&(this._equirectMaterial=qv(),this._compileMaterial(this._equirectMaterial))}dispose(){this._dispose(),this._cubemapMaterial!==null&&this._cubemapMaterial.dispose(),this._equirectMaterial!==null&&this._equirectMaterial.dispose(),this._backgroundBox!==null&&(this._backgroundBox.geometry.dispose(),this._backgroundBox.material.dispose())}_setSize(t){this._lodMax=Math.floor(Math.log2(t)),this._cubeSize=Math.pow(2,this._lodMax)}_dispose(){this._blurMaterial!==null&&this._blurMaterial.dispose(),this._ggxMaterial!==null&&this._ggxMaterial.dispose(),this._pingPongRenderTarget!==null&&this._pingPongRenderTarget.dispose();for(let t=0;t<this._lodMeshes.length;t++)this._lodMeshes[t].geometry.dispose()}_cleanup(t){this._renderer.setRenderTarget(fh,dh,hh),this._renderer.xr.enabled=ph,t.scissorTest=!1,Ws(t,0,0,t.width,t.height)}_fromTexture(t,i){t.mapping===Wr||t.mapping===eo?this._setSize(t.image.length===0?16:t.image[0].width||t.image[0].image.width):this._setSize(t.image.width/4),fh=this._renderer.getRenderTarget(),dh=this._renderer.getActiveCubeFace(),hh=this._renderer.getActiveMipmapLevel(),ph=this._renderer.xr.enabled,this._renderer.xr.enabled=!1;const r=i||this._allocateTargets();return this._textureToCubeUV(t,r),this._applyPMREM(r),this._cleanup(r),r}_allocateTargets(){const t=3*Math.max(this._cubeSize,112),i=4*this._cubeSize,r={magFilter:wn,minFilter:wn,generateMipmaps:!1,type:Da,format:Pi,colorSpace:pu,depthBuffer:!1},l=Xv(t,i,r);if(this._pingPongRenderTarget===null||this._pingPongRenderTarget.width!==t||this._pingPongRenderTarget.height!==i){this._pingPongRenderTarget!==null&&this._dispose(),this._pingPongRenderTarget=Xv(t,i,r);const{_lodMax:c}=this;({lodMeshes:this._lodMeshes,sizeLods:this._sizeLods,sigmas:this._sigmas}=S2(c)),this._blurMaterial=M2(c,t,i),this._ggxMaterial=y2(c,t,i)}return l}_compileMaterial(t){const i=new Ye(new bn,t);this._renderer.compile(i,al)}_sceneToCubeUV(t,i,r,l,c){const m=new hi(90,1,i,r),p=[1,-1,1,1,1,1],_=[1,1,1,-1,-1,-1],v=this._renderer,g=v.autoClear,x=v.toneMapping;v.getClearColor(Vv),v.toneMapping=Ki,v.autoClear=!1,v.state.buffers.depth.getReversed()&&(v.setRenderTarget(l),v.clearDepth(),v.setRenderTarget(null)),this._backgroundBox===null&&(this._backgroundBox=new Ye(new En,new Ta({name:"PMREM.Background",side:Jn,depthWrite:!1,depthTest:!1})));const A=this._backgroundBox,E=A.material;let S=!1;const P=t.background;P?P.isColor&&(E.color.copy(P),t.background=null,S=!0):(E.color.copy(Vv),S=!0);for(let O=0;O<6;O++){const C=O%3;C===0?(m.up.set(0,p[O],0),m.position.set(c.x,c.y,c.z),m.lookAt(c.x+_[O],c.y,c.z)):C===1?(m.up.set(0,0,p[O]),m.position.set(c.x,c.y,c.z),m.lookAt(c.x,c.y+_[O],c.z)):(m.up.set(0,p[O],0),m.position.set(c.x,c.y,c.z),m.lookAt(c.x,c.y,c.z+_[O]));const z=this._cubeSize;Ws(l,C*z,O>2?z:0,z,z),v.setRenderTarget(l),S&&v.render(A,m),v.render(t,m)}v.toneMapping=x,v.autoClear=g,t.background=P}_textureToCubeUV(t,i){const r=this._renderer,l=t.mapping===Wr||t.mapping===eo;l?(this._cubemapMaterial===null&&(this._cubemapMaterial=Wv()),this._cubemapMaterial.uniforms.flipEnvMap.value=t.isRenderTargetTexture===!1?-1:1):this._equirectMaterial===null&&(this._equirectMaterial=qv());const c=l?this._cubemapMaterial:this._equirectMaterial,f=this._lodMeshes[0];f.material=c;const h=c.uniforms;h.envMap.value=t;const m=this._cubeSize;Ws(i,0,0,3*m,2*m),r.setRenderTarget(i),r.render(f,al)}_applyPMREM(t){const i=this._renderer,r=i.autoClear;i.autoClear=!1;const l=this._lodMeshes.length;for(let c=1;c<l;c++)this._applyGGXFilter(t,c-1,c);i.autoClear=r}_applyGGXFilter(t,i,r){const l=this._renderer,c=this._pingPongRenderTarget,f=this._ggxMaterial,h=this._lodMeshes[r];h.material=f;const m=f.uniforms,p=r/(this._lodMeshes.length-1),_=i/(this._lodMeshes.length-1),v=Math.sqrt(p*p-_*_),g=0+p*1.25,x=v*g,{_lodMax:M}=this,A=this._sizeLods[r],E=3*A*(r>M-hr?r-M+hr:0),S=4*(this._cubeSize-A);m.envMap.value=t.texture,m.roughness.value=x,m.mipInt.value=M-i,Ws(c,E,S,3*A,2*A),l.setRenderTarget(c),l.render(h,al),m.envMap.value=c.texture,m.roughness.value=0,m.mipInt.value=M-r,Ws(t,E,S,3*A,2*A),l.setRenderTarget(t),l.render(h,al)}_blur(t,i,r,l,c){const f=this._pingPongRenderTarget;this._halfBlur(t,f,i,r,l,"latitudinal",c),this._halfBlur(f,t,r,r,l,"longitudinal",c)}_halfBlur(t,i,r,l,c,f,h){const m=this._renderer,p=this._blurMaterial;f!=="latitudinal"&&f!=="longitudinal"&&Re("blur direction must be either latitudinal or longitudinal!");const _=3,v=this._lodMeshes[l];v.material=p;const g=p.uniforms,x=this._sizeLods[r]-1,M=isFinite(c)?Math.PI/(2*x):2*Math.PI/(2*Vr-1),A=c/M,E=isFinite(c)?1+Math.floor(_*A):Vr;E>Vr&&se(`sigmaRadians, ${c}, is too large and will clip, as it requested ${E} samples when the maximum is set to ${Vr}`);const S=[];let P=0;for(let U=0;U<Vr;++U){const b=U/A,N=Math.exp(-b*b/2);S.push(N),U===0?P+=N:U<E&&(P+=2*N)}for(let U=0;U<S.length;U++)S[U]=S[U]/P;g.envMap.value=t.texture,g.samples.value=E,g.weights.value=S,g.latitudinal.value=f==="latitudinal",h&&(g.poleAxis.value=h);const{_lodMax:O}=this;g.dTheta.value=M,g.mipInt.value=O-r;const C=this._sizeLods[l],z=3*C*(l>O-hr?l-O+hr:0),D=4*(this._cubeSize-C);Ws(i,z,D,3*C,2*C),m.setRenderTarget(i),m.render(v,al)}}function S2(s){const t=[],i=[],r=[];let l=s;const c=s-hr+1+Gv.length;for(let f=0;f<c;f++){const h=Math.pow(2,l);t.push(h);let m=1/h;f>s-hr?m=Gv[f-s+hr-1]:f===0&&(m=0),i.push(m);const p=1/(h-2),_=-p,v=1+p,g=[_,_,v,_,v,v,_,_,v,v,_,v],x=6,M=6,A=3,E=2,S=1,P=new Float32Array(A*M*x),O=new Float32Array(E*M*x),C=new Float32Array(S*M*x);for(let D=0;D<x;D++){const U=D%3*2/3-1,b=D>2?0:-1,N=[U,b,0,U+2/3,b,0,U+2/3,b+1,0,U,b,0,U+2/3,b+1,0,U,b+1,0];P.set(N,A*M*D),O.set(g,E*M*D);const G=[D,D,D,D,D,D];C.set(G,S*M*D)}const z=new bn;z.setAttribute("position",new Ti(P,A)),z.setAttribute("uv",new Ti(O,E)),z.setAttribute("faceIndex",new Ti(C,S)),r.push(new Ye(z,null)),l>hr&&l--}return{lodMeshes:r,sizeLods:t,sigmas:i}}function Xv(s,t,i){const r=new Qi(s,t,i);return r.texture.mapping=Su,r.texture.name="PMREM.cubeUv",r.scissorTest=!0,r}function Ws(s,t,i,r,l){s.viewport.set(t,i,r,l),s.scissor.set(t,i,r,l)}function y2(s,t,i){return new ta({name:"PMREMGGXConvolution",defines:{GGX_SAMPLES:v2,CUBEUV_TEXEL_WIDTH:1/t,CUBEUV_TEXEL_HEIGHT:1/i,CUBEUV_MAX_MIP:`${s}.0`},uniforms:{envMap:{value:null},roughness:{value:0},mipInt:{value:0}},vertexShader:Mu(),fragmentShader:`

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
		`,blending:Ca,depthTest:!1,depthWrite:!1})}function M2(s,t,i){const r=new Float32Array(Vr),l=new Q(0,1,0);return new ta({name:"SphericalGaussianBlur",defines:{n:Vr,CUBEUV_TEXEL_WIDTH:1/t,CUBEUV_TEXEL_HEIGHT:1/i,CUBEUV_MAX_MIP:`${s}.0`},uniforms:{envMap:{value:null},samples:{value:1},weights:{value:r},latitudinal:{value:!1},dTheta:{value:0},mipInt:{value:0},poleAxis:{value:l}},vertexShader:Mu(),fragmentShader:`

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
		`,blending:Ca,depthTest:!1,depthWrite:!1})}function qv(){return new ta({name:"EquirectangularToCubeUV",uniforms:{envMap:{value:null}},vertexShader:Mu(),fragmentShader:`

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
		`,blending:Ca,depthTest:!1,depthWrite:!1})}function Wv(){return new ta({name:"CubemapToCubeUV",uniforms:{envMap:{value:null},flipEnvMap:{value:-1}},vertexShader:Mu(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			uniform float flipEnvMap;

			varying vec3 vOutputDirection;

			uniform samplerCube envMap;

			void main() {

				gl_FragColor = textureCube( envMap, vec3( flipEnvMap * vOutputDirection.x, vOutputDirection.yz ) );

			}
		`,blending:Ca,depthTest:!1,depthWrite:!1})}function Mu(){return`

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
	`}class yS extends Qi{constructor(t=1,i={}){super(t,t,i),this.isWebGLCubeRenderTarget=!0;const r={width:t,height:t,depth:1},l=[r,r,r,r,r,r];this.texture=new hS(l),this._setTextureOptions(i),this.texture.isRenderTargetTexture=!0}fromEquirectangularTexture(t,i){this.texture.type=i.type,this.texture.colorSpace=i.colorSpace,this.texture.generateMipmaps=i.generateMipmaps,this.texture.minFilter=i.minFilter,this.texture.magFilter=i.magFilter;const r={uniforms:{tEquirect:{value:null}},vertexShader:`

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
			`},l=new En(5,5,5),c=new ta({name:"CubemapFromEquirect",uniforms:io(r.uniforms),vertexShader:r.vertexShader,fragmentShader:r.fragmentShader,side:Jn,blending:Ca});c.uniforms.tEquirect.value=i;const f=new Ye(l,c),h=i.minFilter;return i.minFilter===Xr&&(i.minFilter=wn),new bb(1,10,this).update(t,f),i.minFilter=h,f.geometry.dispose(),f.material.dispose(),this}clear(t,i=!0,r=!0,l=!0){const c=t.getRenderTarget();for(let f=0;f<6;f++)t.setRenderTarget(this,f),t.clear(i,r,l);t.setRenderTarget(c)}}function E2(s){let t=new WeakMap,i=new WeakMap,r=null;function l(g,x=!1){return g==null?null:x?f(g):c(g)}function c(g){if(g&&g.isTexture){const x=g.mapping;if(x===Pd||x===Bd)if(t.has(g)){const M=t.get(g).texture;return h(M,g.mapping)}else{const M=g.image;if(M&&M.height>0){const A=new yS(M.height);return A.fromEquirectangularTexture(s,g),t.set(g,A),g.addEventListener("dispose",p),h(A.texture,g.mapping)}else return null}}return g}function f(g){if(g&&g.isTexture){const x=g.mapping,M=x===Pd||x===Bd,A=x===Wr||x===eo;if(M||A){let E=i.get(g);const S=E!==void 0?E.texture.pmremVersion:0;if(g.isRenderTargetTexture&&g.pmremVersion!==S)return r===null&&(r=new kv(s)),E=M?r.fromEquirectangular(g,E):r.fromCubemap(g,E),E.texture.pmremVersion=g.pmremVersion,i.set(g,E),E.texture;if(E!==void 0)return E.texture;{const P=g.image;return M&&P&&P.height>0||A&&P&&m(P)?(r===null&&(r=new kv(s)),E=M?r.fromEquirectangular(g):r.fromCubemap(g),E.texture.pmremVersion=g.pmremVersion,i.set(g,E),g.addEventListener("dispose",_),E.texture):null}}}return g}function h(g,x){return x===Pd?g.mapping=Wr:x===Bd&&(g.mapping=eo),g}function m(g){let x=0;const M=6;for(let A=0;A<M;A++)g[A]!==void 0&&x++;return x===M}function p(g){const x=g.target;x.removeEventListener("dispose",p);const M=t.get(x);M!==void 0&&(t.delete(x),M.dispose())}function _(g){const x=g.target;x.removeEventListener("dispose",_);const M=i.get(x);M!==void 0&&(i.delete(x),M.dispose())}function v(){t=new WeakMap,i=new WeakMap,r!==null&&(r.dispose(),r=null)}return{get:l,dispose:v}}function b2(s){const t={};function i(r){if(t[r]!==void 0)return t[r];const l=s.getExtension(r);return t[r]=l,l}return{has:function(r){return i(r)!==null},init:function(){i("EXT_color_buffer_float"),i("WEBGL_clip_cull_distance"),i("OES_texture_float_linear"),i("EXT_color_buffer_half_float"),i("WEBGL_multisampled_render_to_texture"),i("WEBGL_render_shared_exponent")},get:function(r){const l=i(r);return l===null&&Ks("WebGLRenderer: "+r+" extension not supported."),l}}}function T2(s,t,i,r){const l={},c=new WeakMap;function f(v){const g=v.target;g.index!==null&&t.remove(g.index);for(const M in g.attributes)t.remove(g.attributes[M]);g.removeEventListener("dispose",f),delete l[g.id];const x=c.get(g);x&&(t.remove(x),c.delete(g)),r.releaseStatesOfGeometry(g),g.isInstancedBufferGeometry===!0&&delete g._maxInstanceCount,i.memory.geometries--}function h(v,g){return l[g.id]===!0||(g.addEventListener("dispose",f),l[g.id]=!0,i.memory.geometries++),g}function m(v){const g=v.attributes;for(const x in g)t.update(g[x],s.ARRAY_BUFFER)}function p(v){const g=[],x=v.index,M=v.attributes.position;let A=0;if(M===void 0)return;if(x!==null){const P=x.array;A=x.version;for(let O=0,C=P.length;O<C;O+=3){const z=P[O+0],D=P[O+1],U=P[O+2];g.push(z,D,D,U,U,z)}}else{const P=M.array;A=M.version;for(let O=0,C=P.length/3-1;O<C;O+=3){const z=O+0,D=O+1,U=O+2;g.push(z,D,D,U,U,z)}}const E=new(M.count>=65535?fS:uS)(g,1);E.version=A;const S=c.get(v);S&&t.remove(S),c.set(v,E)}function _(v){const g=c.get(v);if(g){const x=v.index;x!==null&&g.version<x.version&&p(v)}else p(v);return c.get(v)}return{get:h,update:m,getWireframeAttribute:_}}function A2(s,t,i){let r;function l(v){r=v}let c,f;function h(v){c=v.type,f=v.bytesPerElement}function m(v,g){s.drawElements(r,g,c,v*f),i.update(g,r,1)}function p(v,g,x){x!==0&&(s.drawElementsInstanced(r,g,c,v*f,x),i.update(g,r,x))}function _(v,g,x){if(x===0)return;t.get("WEBGL_multi_draw").multiDrawElementsWEBGL(r,g,0,c,v,0,x);let A=0;for(let E=0;E<x;E++)A+=g[E];i.update(A,r,1)}this.setMode=l,this.setIndex=h,this.render=m,this.renderInstances=p,this.renderMultiDraw=_}function R2(s){const t={geometries:0,textures:0},i={frame:0,calls:0,triangles:0,points:0,lines:0};function r(c,f,h){switch(i.calls++,f){case s.TRIANGLES:i.triangles+=h*(c/3);break;case s.LINES:i.lines+=h*(c/2);break;case s.LINE_STRIP:i.lines+=h*(c-1);break;case s.LINE_LOOP:i.lines+=h*c;break;case s.POINTS:i.points+=h*c;break;default:Re("WebGLInfo: Unknown draw mode:",f);break}}function l(){i.calls=0,i.triangles=0,i.points=0,i.lines=0}return{memory:t,render:i,programs:null,autoReset:!0,reset:l,update:r}}function C2(s,t,i){const r=new WeakMap,l=new sn;function c(f,h,m){const p=f.morphTargetInfluences,_=h.morphAttributes.position||h.morphAttributes.normal||h.morphAttributes.color,v=_!==void 0?_.length:0;let g=r.get(h);if(g===void 0||g.count!==v){let N=function(){U.dispose(),r.delete(h),h.removeEventListener("dispose",N)};g!==void 0&&g.texture.dispose();const x=h.morphAttributes.position!==void 0,M=h.morphAttributes.normal!==void 0,A=h.morphAttributes.color!==void 0,E=h.morphAttributes.position||[],S=h.morphAttributes.normal||[],P=h.morphAttributes.color||[];let O=0;x===!0&&(O=1),M===!0&&(O=2),A===!0&&(O=3);let C=h.attributes.position.count*O,z=1;C>t.maxTextureSize&&(z=Math.ceil(C/t.maxTextureSize),C=t.maxTextureSize);const D=new Float32Array(C*z*4*v),U=new lS(D,C,z,v);U.type=Zi,U.needsUpdate=!0;const b=O*4;for(let G=0;G<v;G++){const F=E[G],k=S[G],K=P[G],tt=C*z*4*G;for(let $=0;$<F.count;$++){const I=$*b;x===!0&&(l.fromBufferAttribute(F,$),D[tt+I+0]=l.x,D[tt+I+1]=l.y,D[tt+I+2]=l.z,D[tt+I+3]=0),M===!0&&(l.fromBufferAttribute(k,$),D[tt+I+4]=l.x,D[tt+I+5]=l.y,D[tt+I+6]=l.z,D[tt+I+7]=0),A===!0&&(l.fromBufferAttribute(K,$),D[tt+I+8]=l.x,D[tt+I+9]=l.y,D[tt+I+10]=l.z,D[tt+I+11]=K.itemSize===4?l.w:1)}}g={count:v,texture:U,size:new Ae(C,z)},r.set(h,g),h.addEventListener("dispose",N)}if(f.isInstancedMesh===!0&&f.morphTexture!==null)m.getUniforms().setValue(s,"morphTexture",f.morphTexture,i);else{let x=0;for(let A=0;A<p.length;A++)x+=p[A];const M=h.morphTargetsRelative?1:1-x;m.getUniforms().setValue(s,"morphTargetBaseInfluence",M),m.getUniforms().setValue(s,"morphTargetInfluences",p)}m.getUniforms().setValue(s,"morphTargetsTexture",g.texture,i),m.getUniforms().setValue(s,"morphTargetsTextureSize",g.size)}return{update:c}}function w2(s,t,i,r,l){let c=new WeakMap;function f(p){const _=l.render.frame,v=p.geometry,g=t.get(p,v);if(c.get(g)!==_&&(t.update(g),c.set(g,_)),p.isInstancedMesh&&(p.hasEventListener("dispose",m)===!1&&p.addEventListener("dispose",m),c.get(p)!==_&&(i.update(p.instanceMatrix,s.ARRAY_BUFFER),p.instanceColor!==null&&i.update(p.instanceColor,s.ARRAY_BUFFER),c.set(p,_))),p.isSkinnedMesh){const x=p.skeleton;c.get(x)!==_&&(x.update(),c.set(x,_))}return g}function h(){c=new WeakMap}function m(p){const _=p.target;_.removeEventListener("dispose",m),r.releaseStatesOfObject(_),i.remove(_.instanceMatrix),_.instanceColor!==null&&i.remove(_.instanceColor)}return{update:f,dispose:h}}const D2={[Yx]:"LINEAR_TONE_MAPPING",[Zx]:"REINHARD_TONE_MAPPING",[jx]:"CINEON_TONE_MAPPING",[Bp]:"ACES_FILMIC_TONE_MAPPING",[Qx]:"AGX_TONE_MAPPING",[Jx]:"NEUTRAL_TONE_MAPPING",[Kx]:"CUSTOM_TONE_MAPPING"};function N2(s,t,i,r,l,c){const f=new Qi(t,i,{type:s,depthBuffer:l,stencilBuffer:c,samples:r?4:0,depthTexture:l?new no(t,i):void 0}),h=new Qi(t,i,{type:Da,depthBuffer:!1,stencilBuffer:!1}),m=new bn;m.setAttribute("position",new $n([-1,3,0,-1,-1,0,3,-1,0],3)),m.setAttribute("uv",new $n([0,2,0,0,2,0],2));const p=new hb({uniforms:{tDiffuse:{value:null}},vertexShader:`
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
			}`,depthTest:!1,depthWrite:!1}),_=new Ye(m,p),v=new $p(-1,1,1,-1,0,1);let g=null,x=null,M=!1,A,E=null,S=[],P=!1;this.setSize=function(O,C){f.setSize(O,C),h.setSize(O,C);for(let z=0;z<S.length;z++){const D=S[z];D.setSize&&D.setSize(O,C)}},this.setEffects=function(O){S=O,P=S.length>0&&S[0].isRenderPass===!0;const C=f.width,z=f.height;for(let D=0;D<S.length;D++){const U=S[D];U.setSize&&U.setSize(C,z)}},this.begin=function(O,C){if(M||O.toneMapping===Ki&&S.length===0)return!1;if(E=C,C!==null){const z=C.width,D=C.height;(f.width!==z||f.height!==D)&&this.setSize(z,D)}return P===!1&&O.setRenderTarget(f),A=O.toneMapping,O.toneMapping=Ki,!0},this.hasRenderPass=function(){return P},this.end=function(O,C){O.toneMapping=A,M=!0;let z=f,D=h;for(let U=0;U<S.length;U++){const b=S[U];if(b.enabled!==!1&&(b.render(O,D,z,C),b.needsSwap!==!1)){const N=z;z=D,D=N}}if(g!==O.outputColorSpace||x!==O.toneMapping){g=O.outputColorSpace,x=O.toneMapping,p.defines={},Te.getTransfer(g)===Ve&&(p.defines.SRGB_TRANSFER="");const U=D2[x];U&&(p.defines[U]=""),p.needsUpdate=!0}p.uniforms.tDiffuse.value=z.texture,O.setRenderTarget(E),O.render(_,v),E=null,M=!1},this.isCompositing=function(){return M},this.dispose=function(){f.depthTexture&&f.depthTexture.dispose(),f.dispose(),h.dispose(),m.dispose(),p.dispose()}}const MS=new Bn,Up=new no(1,1),ES=new lS,bS=new kE,TS=new hS,Yv=[],Zv=[],jv=new Float32Array(16),Kv=new Float32Array(9),Qv=new Float32Array(4);function ro(s,t,i){const r=s[0];if(r<=0||r>0)return s;const l=t*i;let c=Yv[l];if(c===void 0&&(c=new Float32Array(l),Yv[l]=c),t!==0){r.toArray(c,0);for(let f=1,h=0;f!==t;++f)h+=i,s[f].toArray(c,h)}return c}function Tn(s,t){if(s.length!==t.length)return!1;for(let i=0,r=s.length;i<r;i++)if(s[i]!==t[i])return!1;return!0}function An(s,t){for(let i=0,r=t.length;i<r;i++)s[i]=t[i]}function Eu(s,t){let i=Zv[t];i===void 0&&(i=new Int32Array(t),Zv[t]=i);for(let r=0;r!==t;++r)i[r]=s.allocateTextureUnit();return i}function U2(s,t){const i=this.cache;i[0]!==t&&(s.uniform1f(this.addr,t),i[0]=t)}function L2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(s.uniform2f(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(Tn(i,t))return;s.uniform2fv(this.addr,t),An(i,t)}}function O2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(s.uniform3f(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else if(t.r!==void 0)(i[0]!==t.r||i[1]!==t.g||i[2]!==t.b)&&(s.uniform3f(this.addr,t.r,t.g,t.b),i[0]=t.r,i[1]=t.g,i[2]=t.b);else{if(Tn(i,t))return;s.uniform3fv(this.addr,t),An(i,t)}}function P2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(s.uniform4f(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(Tn(i,t))return;s.uniform4fv(this.addr,t),An(i,t)}}function B2(s,t){const i=this.cache,r=t.elements;if(r===void 0){if(Tn(i,t))return;s.uniformMatrix2fv(this.addr,!1,t),An(i,t)}else{if(Tn(i,r))return;Qv.set(r),s.uniformMatrix2fv(this.addr,!1,Qv),An(i,r)}}function I2(s,t){const i=this.cache,r=t.elements;if(r===void 0){if(Tn(i,t))return;s.uniformMatrix3fv(this.addr,!1,t),An(i,t)}else{if(Tn(i,r))return;Kv.set(r),s.uniformMatrix3fv(this.addr,!1,Kv),An(i,r)}}function z2(s,t){const i=this.cache,r=t.elements;if(r===void 0){if(Tn(i,t))return;s.uniformMatrix4fv(this.addr,!1,t),An(i,t)}else{if(Tn(i,r))return;jv.set(r),s.uniformMatrix4fv(this.addr,!1,jv),An(i,r)}}function F2(s,t){const i=this.cache;i[0]!==t&&(s.uniform1i(this.addr,t),i[0]=t)}function H2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(s.uniform2i(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(Tn(i,t))return;s.uniform2iv(this.addr,t),An(i,t)}}function G2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(s.uniform3i(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else{if(Tn(i,t))return;s.uniform3iv(this.addr,t),An(i,t)}}function V2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(s.uniform4i(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(Tn(i,t))return;s.uniform4iv(this.addr,t),An(i,t)}}function k2(s,t){const i=this.cache;i[0]!==t&&(s.uniform1ui(this.addr,t),i[0]=t)}function X2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y)&&(s.uniform2ui(this.addr,t.x,t.y),i[0]=t.x,i[1]=t.y);else{if(Tn(i,t))return;s.uniform2uiv(this.addr,t),An(i,t)}}function q2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z)&&(s.uniform3ui(this.addr,t.x,t.y,t.z),i[0]=t.x,i[1]=t.y,i[2]=t.z);else{if(Tn(i,t))return;s.uniform3uiv(this.addr,t),An(i,t)}}function W2(s,t){const i=this.cache;if(t.x!==void 0)(i[0]!==t.x||i[1]!==t.y||i[2]!==t.z||i[3]!==t.w)&&(s.uniform4ui(this.addr,t.x,t.y,t.z,t.w),i[0]=t.x,i[1]=t.y,i[2]=t.z,i[3]=t.w);else{if(Tn(i,t))return;s.uniform4uiv(this.addr,t),An(i,t)}}function Y2(s,t,i){const r=this.cache,l=i.allocateTextureUnit();r[0]!==l&&(s.uniform1i(this.addr,l),r[0]=l);let c;this.type===s.SAMPLER_2D_SHADOW?(Up.compareFunction=i.isReversedDepthBuffer()?Xp:kp,c=Up):c=MS,i.setTexture2D(t||c,l)}function Z2(s,t,i){const r=this.cache,l=i.allocateTextureUnit();r[0]!==l&&(s.uniform1i(this.addr,l),r[0]=l),i.setTexture3D(t||bS,l)}function j2(s,t,i){const r=this.cache,l=i.allocateTextureUnit();r[0]!==l&&(s.uniform1i(this.addr,l),r[0]=l),i.setTextureCube(t||TS,l)}function K2(s,t,i){const r=this.cache,l=i.allocateTextureUnit();r[0]!==l&&(s.uniform1i(this.addr,l),r[0]=l),i.setTexture2DArray(t||ES,l)}function Q2(s){switch(s){case 5126:return U2;case 35664:return L2;case 35665:return O2;case 35666:return P2;case 35674:return B2;case 35675:return I2;case 35676:return z2;case 5124:case 35670:return F2;case 35667:case 35671:return H2;case 35668:case 35672:return G2;case 35669:case 35673:return V2;case 5125:return k2;case 36294:return X2;case 36295:return q2;case 36296:return W2;case 35678:case 36198:case 36298:case 36306:case 35682:return Y2;case 35679:case 36299:case 36307:return Z2;case 35680:case 36300:case 36308:case 36293:return j2;case 36289:case 36303:case 36311:case 36292:return K2}}function J2(s,t){s.uniform1fv(this.addr,t)}function $2(s,t){const i=ro(t,this.size,2);s.uniform2fv(this.addr,i)}function t3(s,t){const i=ro(t,this.size,3);s.uniform3fv(this.addr,i)}function e3(s,t){const i=ro(t,this.size,4);s.uniform4fv(this.addr,i)}function n3(s,t){const i=ro(t,this.size,4);s.uniformMatrix2fv(this.addr,!1,i)}function i3(s,t){const i=ro(t,this.size,9);s.uniformMatrix3fv(this.addr,!1,i)}function a3(s,t){const i=ro(t,this.size,16);s.uniformMatrix4fv(this.addr,!1,i)}function r3(s,t){s.uniform1iv(this.addr,t)}function s3(s,t){s.uniform2iv(this.addr,t)}function o3(s,t){s.uniform3iv(this.addr,t)}function l3(s,t){s.uniform4iv(this.addr,t)}function c3(s,t){s.uniform1uiv(this.addr,t)}function u3(s,t){s.uniform2uiv(this.addr,t)}function f3(s,t){s.uniform3uiv(this.addr,t)}function d3(s,t){s.uniform4uiv(this.addr,t)}function h3(s,t,i){const r=this.cache,l=t.length,c=Eu(i,l);Tn(r,c)||(s.uniform1iv(this.addr,c),An(r,c));let f;this.type===s.SAMPLER_2D_SHADOW?f=Up:f=MS;for(let h=0;h!==l;++h)i.setTexture2D(t[h]||f,c[h])}function p3(s,t,i){const r=this.cache,l=t.length,c=Eu(i,l);Tn(r,c)||(s.uniform1iv(this.addr,c),An(r,c));for(let f=0;f!==l;++f)i.setTexture3D(t[f]||bS,c[f])}function m3(s,t,i){const r=this.cache,l=t.length,c=Eu(i,l);Tn(r,c)||(s.uniform1iv(this.addr,c),An(r,c));for(let f=0;f!==l;++f)i.setTextureCube(t[f]||TS,c[f])}function g3(s,t,i){const r=this.cache,l=t.length,c=Eu(i,l);Tn(r,c)||(s.uniform1iv(this.addr,c),An(r,c));for(let f=0;f!==l;++f)i.setTexture2DArray(t[f]||ES,c[f])}function _3(s){switch(s){case 5126:return J2;case 35664:return $2;case 35665:return t3;case 35666:return e3;case 35674:return n3;case 35675:return i3;case 35676:return a3;case 5124:case 35670:return r3;case 35667:case 35671:return s3;case 35668:case 35672:return o3;case 35669:case 35673:return l3;case 5125:return c3;case 36294:return u3;case 36295:return f3;case 36296:return d3;case 35678:case 36198:case 36298:case 36306:case 35682:return h3;case 35679:case 36299:case 36307:return p3;case 35680:case 36300:case 36308:case 36293:return m3;case 36289:case 36303:case 36311:case 36292:return g3}}class v3{constructor(t,i,r){this.id=t,this.addr=r,this.cache=[],this.type=i.type,this.setValue=Q2(i.type)}}class x3{constructor(t,i,r){this.id=t,this.addr=r,this.cache=[],this.type=i.type,this.size=i.size,this.setValue=_3(i.type)}}class S3{constructor(t){this.id=t,this.seq=[],this.map={}}setValue(t,i,r){const l=this.seq;for(let c=0,f=l.length;c!==f;++c){const h=l[c];h.setValue(t,i[h.id],r)}}}const mh=/(\w+)(\])?(\[|\.)?/g;function Jv(s,t){s.seq.push(t),s.map[t.id]=t}function y3(s,t,i){const r=s.name,l=r.length;for(mh.lastIndex=0;;){const c=mh.exec(r),f=mh.lastIndex;let h=c[1];const m=c[2]==="]",p=c[3];if(m&&(h=h|0),p===void 0||p==="["&&f+2===l){Jv(i,p===void 0?new v3(h,s,t):new x3(h,s,t));break}else{let v=i.map[h];v===void 0&&(v=new S3(h),Jv(i,v)),i=v}}}class fu{constructor(t,i){this.seq=[],this.map={};const r=t.getProgramParameter(i,t.ACTIVE_UNIFORMS);for(let f=0;f<r;++f){const h=t.getActiveUniform(i,f),m=t.getUniformLocation(i,h.name);y3(h,m,this)}const l=[],c=[];for(const f of this.seq)f.type===t.SAMPLER_2D_SHADOW||f.type===t.SAMPLER_CUBE_SHADOW||f.type===t.SAMPLER_2D_ARRAY_SHADOW?l.push(f):c.push(f);l.length>0&&(this.seq=l.concat(c))}setValue(t,i,r,l){const c=this.map[i];c!==void 0&&c.setValue(t,r,l)}setOptional(t,i,r){const l=i[r];l!==void 0&&this.setValue(t,r,l)}static upload(t,i,r,l){for(let c=0,f=i.length;c!==f;++c){const h=i[c],m=r[h.id];m.needsUpdate!==!1&&h.setValue(t,m.value,l)}}static seqWithValue(t,i){const r=[];for(let l=0,c=t.length;l!==c;++l){const f=t[l];f.id in i&&r.push(f)}return r}}function $v(s,t,i){const r=s.createShader(t);return s.shaderSource(r,i),s.compileShader(r),r}const M3=37297;let E3=0;function b3(s,t){const i=s.split(`
`),r=[],l=Math.max(t-6,0),c=Math.min(t+6,i.length);for(let f=l;f<c;f++){const h=f+1;r.push(`${h===t?">":" "} ${h}: ${i[f]}`)}return r.join(`
`)}const tx=new ce;function T3(s){Te._getMatrix(tx,Te.workingColorSpace,s);const t=`mat3( ${tx.elements.map(i=>i.toFixed(4))} )`;switch(Te.getTransfer(s)){case mu:return[t,"LinearTransferOETF"];case Ve:return[t,"sRGBTransferOETF"];default:return se("WebGLProgram: Unsupported color space: ",s),[t,"LinearTransferOETF"]}}function ex(s,t,i){const r=s.getShaderParameter(t,s.COMPILE_STATUS),c=(s.getShaderInfoLog(t)||"").trim();if(r&&c==="")return"";const f=/ERROR: 0:(\d+)/.exec(c);if(f){const h=parseInt(f[1]);return i.toUpperCase()+`

`+c+`

`+b3(s.getShaderSource(t),h)}else return c}function A3(s,t){const i=T3(t);return[`vec4 ${s}( vec4 value ) {`,`	return ${i[1]}( vec4( value.rgb * ${i[0]}, value.a ) );`,"}"].join(`
`)}const R3={[Yx]:"Linear",[Zx]:"Reinhard",[jx]:"Cineon",[Bp]:"ACESFilmic",[Qx]:"AgX",[Jx]:"Neutral",[Kx]:"Custom"};function C3(s,t){const i=R3[t];return i===void 0?(se("WebGLProgram: Unsupported toneMapping:",t),"vec3 "+s+"( vec3 color ) { return LinearToneMapping( color ); }"):"vec3 "+s+"( vec3 color ) { return "+i+"ToneMapping( color ); }"}const au=new Q;function w3(){Te.getLuminanceCoefficients(au);const s=au.x.toFixed(4),t=au.y.toFixed(4),i=au.z.toFixed(4);return["float luminance( const in vec3 rgb ) {",`	const vec3 weights = vec3( ${s}, ${t}, ${i} );`,"	return dot( weights, rgb );","}"].join(`
`)}function D3(s){return[s.extensionClipCullDistance?"#extension GL_ANGLE_clip_cull_distance : require":"",s.extensionMultiDraw?"#extension GL_ANGLE_multi_draw : require":""].filter(cl).join(`
`)}function N3(s){const t=[];for(const i in s){const r=s[i];r!==!1&&t.push("#define "+i+" "+r)}return t.join(`
`)}function U3(s,t){const i={},r=s.getProgramParameter(t,s.ACTIVE_ATTRIBUTES);for(let l=0;l<r;l++){const c=s.getActiveAttrib(t,l),f=c.name;let h=1;c.type===s.FLOAT_MAT2&&(h=2),c.type===s.FLOAT_MAT3&&(h=3),c.type===s.FLOAT_MAT4&&(h=4),i[f]={type:c.type,location:s.getAttribLocation(t,f),locationSize:h}}return i}function cl(s){return s!==""}function nx(s,t){const i=t.numSpotLightShadows+t.numSpotLightMaps-t.numSpotLightShadowsWithMaps;return s.replace(/NUM_DIR_LIGHTS/g,t.numDirLights).replace(/NUM_SPOT_LIGHTS/g,t.numSpotLights).replace(/NUM_SPOT_LIGHT_MAPS/g,t.numSpotLightMaps).replace(/NUM_SPOT_LIGHT_COORDS/g,i).replace(/NUM_RECT_AREA_LIGHTS/g,t.numRectAreaLights).replace(/NUM_POINT_LIGHTS/g,t.numPointLights).replace(/NUM_HEMI_LIGHTS/g,t.numHemiLights).replace(/NUM_DIR_LIGHT_SHADOWS/g,t.numDirLightShadows).replace(/NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS/g,t.numSpotLightShadowsWithMaps).replace(/NUM_SPOT_LIGHT_SHADOWS/g,t.numSpotLightShadows).replace(/NUM_POINT_LIGHT_SHADOWS/g,t.numPointLightShadows)}function ix(s,t){return s.replace(/NUM_CLIPPING_PLANES/g,t.numClippingPlanes).replace(/UNION_CLIPPING_PLANES/g,t.numClippingPlanes-t.numClipIntersection)}const L3=/^[ \t]*#include +<([\w\d./]+)>/gm;function Lp(s){return s.replace(L3,P3)}const O3=new Map;function P3(s,t){let i=pe[t];if(i===void 0){const r=O3.get(t);if(r!==void 0)i=pe[r],se('WebGLRenderer: Shader chunk "%s" has been deprecated. Use "%s" instead.',t,r);else throw new Error("THREE.WebGLProgram: Can not resolve #include <"+t+">")}return Lp(i)}const B3=/#pragma unroll_loop_start\s+for\s*\(\s*int\s+i\s*=\s*(\d+)\s*;\s*i\s*<\s*(\d+)\s*;\s*i\s*\+\+\s*\)\s*{([\s\S]+?)}\s+#pragma unroll_loop_end/g;function ax(s){return s.replace(B3,I3)}function I3(s,t,i,r){let l="";for(let c=parseInt(t);c<parseInt(i);c++)l+=r.replace(/\[\s*i\s*\]/g,"[ "+c+" ]").replace(/UNROLLED_LOOP_INDEX/g,c);return l}function rx(s){let t=`precision ${s.precision} float;
	precision ${s.precision} int;
	precision ${s.precision} sampler2D;
	precision ${s.precision} samplerCube;
	precision ${s.precision} sampler3D;
	precision ${s.precision} sampler2DArray;
	precision ${s.precision} sampler2DShadow;
	precision ${s.precision} samplerCubeShadow;
	precision ${s.precision} sampler2DArrayShadow;
	precision ${s.precision} isampler2D;
	precision ${s.precision} isampler3D;
	precision ${s.precision} isamplerCube;
	precision ${s.precision} isampler2DArray;
	precision ${s.precision} usampler2D;
	precision ${s.precision} usampler3D;
	precision ${s.precision} usamplerCube;
	precision ${s.precision} usampler2DArray;
	`;return s.precision==="highp"?t+=`
#define HIGH_PRECISION`:s.precision==="mediump"?t+=`
#define MEDIUM_PRECISION`:s.precision==="lowp"&&(t+=`
#define LOW_PRECISION`),t}const z3={[ul]:"SHADOWMAP_TYPE_PCF",[ll]:"SHADOWMAP_TYPE_VSM"};function F3(s){return z3[s.shadowMapType]||"SHADOWMAP_TYPE_BASIC"}const H3={[Wr]:"ENVMAP_TYPE_CUBE",[eo]:"ENVMAP_TYPE_CUBE",[Su]:"ENVMAP_TYPE_CUBE_UV"};function G3(s){return s.envMap===!1?"ENVMAP_TYPE_CUBE":H3[s.envMapMode]||"ENVMAP_TYPE_CUBE"}const V3={[eo]:"ENVMAP_MODE_REFRACTION"};function k3(s){return s.envMap===!1?"ENVMAP_MODE_REFLECTION":V3[s.envMapMode]||"ENVMAP_MODE_REFLECTION"}const X3={[Wx]:"ENVMAP_BLENDING_MULTIPLY",[oE]:"ENVMAP_BLENDING_MIX",[lE]:"ENVMAP_BLENDING_ADD"};function q3(s){return s.envMap===!1?"ENVMAP_BLENDING_NONE":X3[s.combine]||"ENVMAP_BLENDING_NONE"}function W3(s){const t=s.envMapCubeUVHeight;if(t===null)return null;const i=Math.log2(t)-2,r=1/t;return{texelWidth:1/(3*Math.max(Math.pow(2,i),112)),texelHeight:r,maxMip:i}}function Y3(s,t,i,r){const l=s.getContext(),c=i.defines;let f=i.vertexShader,h=i.fragmentShader;const m=F3(i),p=G3(i),_=k3(i),v=q3(i),g=W3(i),x=D3(i),M=N3(c),A=l.createProgram();let E,S,P=i.glslVersion?"#version "+i.glslVersion+`
`:"";i.isRawShaderMaterial?(E=["#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,M].filter(cl).join(`
`),E.length>0&&(E+=`
`),S=["#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,M].filter(cl).join(`
`),S.length>0&&(S+=`
`)):(E=[rx(i),"#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,M,i.extensionClipCullDistance?"#define USE_CLIP_DISTANCE":"",i.batching?"#define USE_BATCHING":"",i.batchingColor?"#define USE_BATCHING_COLOR":"",i.instancing?"#define USE_INSTANCING":"",i.instancingColor?"#define USE_INSTANCING_COLOR":"",i.instancingMorph?"#define USE_INSTANCING_MORPH":"",i.useFog&&i.fog?"#define USE_FOG":"",i.useFog&&i.fogExp2?"#define FOG_EXP2":"",i.map?"#define USE_MAP":"",i.envMap?"#define USE_ENVMAP":"",i.envMap?"#define "+_:"",i.lightMap?"#define USE_LIGHTMAP":"",i.aoMap?"#define USE_AOMAP":"",i.bumpMap?"#define USE_BUMPMAP":"",i.normalMap?"#define USE_NORMALMAP":"",i.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",i.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",i.displacementMap?"#define USE_DISPLACEMENTMAP":"",i.emissiveMap?"#define USE_EMISSIVEMAP":"",i.anisotropy?"#define USE_ANISOTROPY":"",i.anisotropyMap?"#define USE_ANISOTROPYMAP":"",i.clearcoatMap?"#define USE_CLEARCOATMAP":"",i.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",i.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",i.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",i.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",i.specularMap?"#define USE_SPECULARMAP":"",i.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",i.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",i.roughnessMap?"#define USE_ROUGHNESSMAP":"",i.metalnessMap?"#define USE_METALNESSMAP":"",i.alphaMap?"#define USE_ALPHAMAP":"",i.alphaHash?"#define USE_ALPHAHASH":"",i.transmission?"#define USE_TRANSMISSION":"",i.transmissionMap?"#define USE_TRANSMISSIONMAP":"",i.thicknessMap?"#define USE_THICKNESSMAP":"",i.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",i.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",i.mapUv?"#define MAP_UV "+i.mapUv:"",i.alphaMapUv?"#define ALPHAMAP_UV "+i.alphaMapUv:"",i.lightMapUv?"#define LIGHTMAP_UV "+i.lightMapUv:"",i.aoMapUv?"#define AOMAP_UV "+i.aoMapUv:"",i.emissiveMapUv?"#define EMISSIVEMAP_UV "+i.emissiveMapUv:"",i.bumpMapUv?"#define BUMPMAP_UV "+i.bumpMapUv:"",i.normalMapUv?"#define NORMALMAP_UV "+i.normalMapUv:"",i.displacementMapUv?"#define DISPLACEMENTMAP_UV "+i.displacementMapUv:"",i.metalnessMapUv?"#define METALNESSMAP_UV "+i.metalnessMapUv:"",i.roughnessMapUv?"#define ROUGHNESSMAP_UV "+i.roughnessMapUv:"",i.anisotropyMapUv?"#define ANISOTROPYMAP_UV "+i.anisotropyMapUv:"",i.clearcoatMapUv?"#define CLEARCOATMAP_UV "+i.clearcoatMapUv:"",i.clearcoatNormalMapUv?"#define CLEARCOAT_NORMALMAP_UV "+i.clearcoatNormalMapUv:"",i.clearcoatRoughnessMapUv?"#define CLEARCOAT_ROUGHNESSMAP_UV "+i.clearcoatRoughnessMapUv:"",i.iridescenceMapUv?"#define IRIDESCENCEMAP_UV "+i.iridescenceMapUv:"",i.iridescenceThicknessMapUv?"#define IRIDESCENCE_THICKNESSMAP_UV "+i.iridescenceThicknessMapUv:"",i.sheenColorMapUv?"#define SHEEN_COLORMAP_UV "+i.sheenColorMapUv:"",i.sheenRoughnessMapUv?"#define SHEEN_ROUGHNESSMAP_UV "+i.sheenRoughnessMapUv:"",i.specularMapUv?"#define SPECULARMAP_UV "+i.specularMapUv:"",i.specularColorMapUv?"#define SPECULAR_COLORMAP_UV "+i.specularColorMapUv:"",i.specularIntensityMapUv?"#define SPECULAR_INTENSITYMAP_UV "+i.specularIntensityMapUv:"",i.transmissionMapUv?"#define TRANSMISSIONMAP_UV "+i.transmissionMapUv:"",i.thicknessMapUv?"#define THICKNESSMAP_UV "+i.thicknessMapUv:"",i.vertexTangents&&i.flatShading===!1?"#define USE_TANGENT":"",i.vertexNormals?"#define HAS_NORMAL":"",i.vertexColors?"#define USE_COLOR":"",i.vertexAlphas?"#define USE_COLOR_ALPHA":"",i.vertexUv1s?"#define USE_UV1":"",i.vertexUv2s?"#define USE_UV2":"",i.vertexUv3s?"#define USE_UV3":"",i.pointsUvs?"#define USE_POINTS_UV":"",i.flatShading?"#define FLAT_SHADED":"",i.skinning?"#define USE_SKINNING":"",i.morphTargets?"#define USE_MORPHTARGETS":"",i.morphNormals&&i.flatShading===!1?"#define USE_MORPHNORMALS":"",i.morphColors?"#define USE_MORPHCOLORS":"",i.morphTargetsCount>0?"#define MORPHTARGETS_TEXTURE_STRIDE "+i.morphTextureStride:"",i.morphTargetsCount>0?"#define MORPHTARGETS_COUNT "+i.morphTargetsCount:"",i.doubleSided?"#define DOUBLE_SIDED":"",i.flipSided?"#define FLIP_SIDED":"",i.shadowMapEnabled?"#define USE_SHADOWMAP":"",i.shadowMapEnabled?"#define "+m:"",i.sizeAttenuation?"#define USE_SIZEATTENUATION":"",i.numLightProbes>0?"#define USE_LIGHT_PROBES":"",i.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",i.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 modelMatrix;","uniform mat4 modelViewMatrix;","uniform mat4 projectionMatrix;","uniform mat4 viewMatrix;","uniform mat3 normalMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;","#ifdef USE_INSTANCING","	attribute mat4 instanceMatrix;","#endif","#ifdef USE_INSTANCING_COLOR","	attribute vec3 instanceColor;","#endif","#ifdef USE_INSTANCING_MORPH","	uniform sampler2D morphTexture;","#endif","attribute vec3 position;","attribute vec3 normal;","attribute vec2 uv;","#ifdef USE_UV1","	attribute vec2 uv1;","#endif","#ifdef USE_UV2","	attribute vec2 uv2;","#endif","#ifdef USE_UV3","	attribute vec2 uv3;","#endif","#ifdef USE_TANGENT","	attribute vec4 tangent;","#endif","#if defined( USE_COLOR_ALPHA )","	attribute vec4 color;","#elif defined( USE_COLOR )","	attribute vec3 color;","#endif","#ifdef USE_SKINNING","	attribute vec4 skinIndex;","	attribute vec4 skinWeight;","#endif",`
`].filter(cl).join(`
`),S=[rx(i),"#define SHADER_TYPE "+i.shaderType,"#define SHADER_NAME "+i.shaderName,M,i.useFog&&i.fog?"#define USE_FOG":"",i.useFog&&i.fogExp2?"#define FOG_EXP2":"",i.alphaToCoverage?"#define ALPHA_TO_COVERAGE":"",i.map?"#define USE_MAP":"",i.matcap?"#define USE_MATCAP":"",i.envMap?"#define USE_ENVMAP":"",i.envMap?"#define "+p:"",i.envMap?"#define "+_:"",i.envMap?"#define "+v:"",g?"#define CUBEUV_TEXEL_WIDTH "+g.texelWidth:"",g?"#define CUBEUV_TEXEL_HEIGHT "+g.texelHeight:"",g?"#define CUBEUV_MAX_MIP "+g.maxMip+".0":"",i.lightMap?"#define USE_LIGHTMAP":"",i.aoMap?"#define USE_AOMAP":"",i.bumpMap?"#define USE_BUMPMAP":"",i.normalMap?"#define USE_NORMALMAP":"",i.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",i.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",i.packedNormalMap?"#define USE_PACKED_NORMALMAP":"",i.emissiveMap?"#define USE_EMISSIVEMAP":"",i.anisotropy?"#define USE_ANISOTROPY":"",i.anisotropyMap?"#define USE_ANISOTROPYMAP":"",i.clearcoat?"#define USE_CLEARCOAT":"",i.clearcoatMap?"#define USE_CLEARCOATMAP":"",i.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",i.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",i.dispersion?"#define USE_DISPERSION":"",i.iridescence?"#define USE_IRIDESCENCE":"",i.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",i.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",i.specularMap?"#define USE_SPECULARMAP":"",i.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",i.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",i.roughnessMap?"#define USE_ROUGHNESSMAP":"",i.metalnessMap?"#define USE_METALNESSMAP":"",i.alphaMap?"#define USE_ALPHAMAP":"",i.alphaTest?"#define USE_ALPHATEST":"",i.alphaHash?"#define USE_ALPHAHASH":"",i.sheen?"#define USE_SHEEN":"",i.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",i.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",i.transmission?"#define USE_TRANSMISSION":"",i.transmissionMap?"#define USE_TRANSMISSIONMAP":"",i.thicknessMap?"#define USE_THICKNESSMAP":"",i.vertexTangents&&i.flatShading===!1?"#define USE_TANGENT":"",i.vertexColors||i.instancingColor?"#define USE_COLOR":"",i.vertexAlphas||i.batchingColor?"#define USE_COLOR_ALPHA":"",i.vertexUv1s?"#define USE_UV1":"",i.vertexUv2s?"#define USE_UV2":"",i.vertexUv3s?"#define USE_UV3":"",i.pointsUvs?"#define USE_POINTS_UV":"",i.gradientMap?"#define USE_GRADIENTMAP":"",i.flatShading?"#define FLAT_SHADED":"",i.doubleSided?"#define DOUBLE_SIDED":"",i.flipSided?"#define FLIP_SIDED":"",i.shadowMapEnabled?"#define USE_SHADOWMAP":"",i.shadowMapEnabled?"#define "+m:"",i.premultipliedAlpha?"#define PREMULTIPLIED_ALPHA":"",i.numLightProbes>0?"#define USE_LIGHT_PROBES":"",i.numLightProbeGrids>0?"#define USE_LIGHT_PROBES_GRID":"",i.decodeVideoTexture?"#define DECODE_VIDEO_TEXTURE":"",i.decodeVideoTextureEmissive?"#define DECODE_VIDEO_TEXTURE_EMISSIVE":"",i.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",i.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 viewMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;",i.toneMapping!==Ki?"#define TONE_MAPPING":"",i.toneMapping!==Ki?pe.tonemapping_pars_fragment:"",i.toneMapping!==Ki?C3("toneMapping",i.toneMapping):"",i.dithering?"#define DITHERING":"",i.opaque?"#define OPAQUE":"",pe.colorspace_pars_fragment,A3("linearToOutputTexel",i.outputColorSpace),w3(),i.useDepthPacking?"#define DEPTH_PACKING "+i.depthPacking:"",`
`].filter(cl).join(`
`)),f=Lp(f),f=nx(f,i),f=ix(f,i),h=Lp(h),h=nx(h,i),h=ix(h,i),f=ax(f),h=ax(h),i.isRawShaderMaterial!==!0&&(P=`#version 300 es
`,E=[x,"#define attribute in","#define varying out","#define texture2D texture"].join(`
`)+`
`+E,S=["#define varying in",i.glslVersion===sv?"":"layout(location = 0) out highp vec4 pc_fragColor;",i.glslVersion===sv?"":"#define gl_FragColor pc_fragColor","#define gl_FragDepthEXT gl_FragDepth","#define texture2D texture","#define textureCube texture","#define texture2DProj textureProj","#define texture2DLodEXT textureLod","#define texture2DProjLodEXT textureProjLod","#define textureCubeLodEXT textureLod","#define texture2DGradEXT textureGrad","#define texture2DProjGradEXT textureProjGrad","#define textureCubeGradEXT textureGrad"].join(`
`)+`
`+S);const O=P+E+f,C=P+S+h,z=$v(l,l.VERTEX_SHADER,O),D=$v(l,l.FRAGMENT_SHADER,C);l.attachShader(A,z),l.attachShader(A,D),i.index0AttributeName!==void 0?l.bindAttribLocation(A,0,i.index0AttributeName):i.hasPositionAttribute===!0&&l.bindAttribLocation(A,0,"position"),l.linkProgram(A);function U(F){if(s.debug.checkShaderErrors){const k=l.getProgramInfoLog(A)||"",K=l.getShaderInfoLog(z)||"",tt=l.getShaderInfoLog(D)||"",$=k.trim(),I=K.trim(),X=tt.trim();let at=!0,_t=!0;if(l.getProgramParameter(A,l.LINK_STATUS)===!1)if(at=!1,typeof s.debug.onShaderError=="function")s.debug.onShaderError(l,A,z,D);else{const vt=ex(l,z,"vertex"),L=ex(l,D,"fragment");Re("WebGLProgram: Shader Error "+l.getError()+" - VALIDATE_STATUS "+l.getProgramParameter(A,l.VALIDATE_STATUS)+`

Material Name: `+F.name+`
Material Type: `+F.type+`

Program Info Log: `+$+`
`+vt+`
`+L)}else $!==""?se("WebGLProgram: Program Info Log:",$):(I===""||X==="")&&(_t=!1);_t&&(F.diagnostics={runnable:at,programLog:$,vertexShader:{log:I,prefix:E},fragmentShader:{log:X,prefix:S}})}l.deleteShader(z),l.deleteShader(D),b=new fu(l,A),N=U3(l,A)}let b;this.getUniforms=function(){return b===void 0&&U(this),b};let N;this.getAttributes=function(){return N===void 0&&U(this),N};let G=i.rendererExtensionParallelShaderCompile===!1;return this.isReady=function(){return G===!1&&(G=l.getProgramParameter(A,M3)),G},this.destroy=function(){r.releaseStatesOfProgram(this),l.deleteProgram(A),this.program=void 0},this.type=i.shaderType,this.name=i.shaderName,this.id=E3++,this.cacheKey=t,this.usedTimes=1,this.program=A,this.vertexShader=z,this.fragmentShader=D,this}let Z3=0;class j3{constructor(){this.shaderCache=new Map,this.materialCache=new Map}update(t,i,r){const l=this._getShaderCacheForMaterial(t);return l.has(i)===!1&&(l.add(i),i.usedTimes++),l.has(r)===!1&&(l.add(r),r.usedTimes++),this}remove(t){const i=this.materialCache.get(t);for(const r of i)r.usedTimes--,r.usedTimes===0&&this.shaderCache.delete(r.code);return this.materialCache.delete(t),this}getVertexShaderStage(t){return this._getShaderStage(t.vertexShader)}getFragmentShaderStage(t){return this._getShaderStage(t.fragmentShader)}dispose(){this.shaderCache.clear(),this.materialCache.clear()}_getShaderCacheForMaterial(t){const i=this.materialCache;let r=i.get(t);return r===void 0&&(r=new Set,i.set(t,r)),r}_getShaderStage(t){const i=this.shaderCache;let r=i.get(t);return r===void 0&&(r=new K3(t),i.set(t,r)),r}}class K3{constructor(t){this.id=Z3++,this.code=t,this.usedTimes=0}}function Q3(s){return s===Yr||s===du||s===hu}function J3(s,t,i,r,l,c){const f=new Yp,h=new j3,m=new Set,p=[],_=new Map,v=r.logarithmicDepthBuffer;let g=r.precision;const x={MeshDepthMaterial:"depth",MeshDistanceMaterial:"distance",MeshNormalMaterial:"normal",MeshBasicMaterial:"basic",MeshLambertMaterial:"lambert",MeshPhongMaterial:"phong",MeshToonMaterial:"toon",MeshStandardMaterial:"physical",MeshPhysicalMaterial:"physical",MeshMatcapMaterial:"matcap",LineBasicMaterial:"basic",LineDashedMaterial:"dashed",PointsMaterial:"points",ShadowMaterial:"shadow",SpriteMaterial:"sprite"};function M(b){return m.add(b),b===0?"uv":`uv${b}`}function A(b,N,G,F,k,K){const tt=F.fog,$=k.geometry,I=b.isMeshStandardMaterial||b.isMeshLambertMaterial||b.isMeshPhongMaterial?F.environment:null,X=b.isMeshStandardMaterial||b.isMeshLambertMaterial&&!b.envMap||b.isMeshPhongMaterial&&!b.envMap,at=t.get(b.envMap||I,X),_t=at&&at.mapping===Su?at.image.height:null,vt=x[b.type];b.precision!==null&&(g=r.getMaxPrecision(b.precision),g!==b.precision&&se("WebGLProgram.getParameters:",b.precision,"not supported, using",g,"instead."));const L=$.morphAttributes.position||$.morphAttributes.normal||$.morphAttributes.color,Y=L!==void 0?L.length:0;let yt=0;$.morphAttributes.position!==void 0&&(yt=1),$.morphAttributes.normal!==void 0&&(yt=2),$.morphAttributes.color!==void 0&&(yt=3);let At,Dt,it,nt;if(vt){const qt=Wi[vt];At=qt.vertexShader,Dt=qt.fragmentShader}else{At=b.vertexShader,Dt=b.fragmentShader;const qt=h.getVertexShaderStage(b),tn=h.getFragmentShaderStage(b);h.update(b,qt,tn),it=qt.id,nt=tn.id}const ct=s.getRenderTarget(),bt=s.state.buffers.depth.getReversed(),Ht=k.isInstancedMesh===!0,Bt=k.isBatchedMesh===!0,he=!!b.map,ne=!!b.matcap,ie=!!at,Xt=!!b.aoMap,ae=!!b.lightMap,ge=!!b.bumpMap&&b.wireframe===!1,_e=!!b.normalMap,Ce=!!b.displacementMap,Ke=!!b.emissiveMap,Ue=!!b.metalnessMap,on=!!b.roughnessMap,j=b.anisotropy>0,ke=b.clearcoat>0,Le=b.dispersion>0,B=b.iridescence>0,T=b.sheen>0,et=b.transmission>0,ut=j&&!!b.anisotropyMap,mt=ke&&!!b.clearcoatMap,Rt=ke&&!!b.clearcoatNormalMap,Ut=ke&&!!b.clearcoatRoughnessMap,pt=B&&!!b.iridescenceMap,gt=B&&!!b.iridescenceThicknessMap,wt=T&&!!b.sheenColorMap,Gt=T&&!!b.sheenRoughnessMap,Pt=!!b.specularMap,Lt=!!b.specularColorMap,Jt=!!b.specularIntensityMap,$t=et&&!!b.transmissionMap,oe=et&&!!b.thicknessMap,W=!!b.gradientMap,Ct=!!b.alphaMap,St=b.alphaTest>0,Nt=!!b.alphaHash,Ft=!!b.extensions;let Tt=Ki;b.toneMapped&&(ct===null||ct.isXRRenderTarget===!0)&&(Tt=s.toneMapping);const jt={shaderID:vt,shaderType:b.type,shaderName:b.name,vertexShader:At,fragmentShader:Dt,defines:b.defines,customVertexShaderID:it,customFragmentShaderID:nt,isRawShaderMaterial:b.isRawShaderMaterial===!0,glslVersion:b.glslVersion,precision:g,batching:Bt,batchingColor:Bt&&k._colorsTexture!==null,instancing:Ht,instancingColor:Ht&&k.instanceColor!==null,instancingMorph:Ht&&k.morphTexture!==null,outputColorSpace:ct===null?s.outputColorSpace:ct.isXRRenderTarget===!0?ct.texture.colorSpace:Te.workingColorSpace,alphaToCoverage:!!b.alphaToCoverage,map:he,matcap:ne,envMap:ie,envMapMode:ie&&at.mapping,envMapCubeUVHeight:_t,aoMap:Xt,lightMap:ae,bumpMap:ge,normalMap:_e,displacementMap:Ce,emissiveMap:Ke,normalMapObjectSpace:_e&&b.normalMapType===fE,normalMapTangentSpace:_e&&b.normalMapType===Rp,packedNormalMap:_e&&b.normalMapType===Rp&&Q3(b.normalMap.format),metalnessMap:Ue,roughnessMap:on,anisotropy:j,anisotropyMap:ut,clearcoat:ke,clearcoatMap:mt,clearcoatNormalMap:Rt,clearcoatRoughnessMap:Ut,dispersion:Le,iridescence:B,iridescenceMap:pt,iridescenceThicknessMap:gt,sheen:T,sheenColorMap:wt,sheenRoughnessMap:Gt,specularMap:Pt,specularColorMap:Lt,specularIntensityMap:Jt,transmission:et,transmissionMap:$t,thicknessMap:oe,gradientMap:W,opaque:b.transparent===!1&&b.blending===js&&b.alphaToCoverage===!1,alphaMap:Ct,alphaTest:St,alphaHash:Nt,combine:b.combine,mapUv:he&&M(b.map.channel),aoMapUv:Xt&&M(b.aoMap.channel),lightMapUv:ae&&M(b.lightMap.channel),bumpMapUv:ge&&M(b.bumpMap.channel),normalMapUv:_e&&M(b.normalMap.channel),displacementMapUv:Ce&&M(b.displacementMap.channel),emissiveMapUv:Ke&&M(b.emissiveMap.channel),metalnessMapUv:Ue&&M(b.metalnessMap.channel),roughnessMapUv:on&&M(b.roughnessMap.channel),anisotropyMapUv:ut&&M(b.anisotropyMap.channel),clearcoatMapUv:mt&&M(b.clearcoatMap.channel),clearcoatNormalMapUv:Rt&&M(b.clearcoatNormalMap.channel),clearcoatRoughnessMapUv:Ut&&M(b.clearcoatRoughnessMap.channel),iridescenceMapUv:pt&&M(b.iridescenceMap.channel),iridescenceThicknessMapUv:gt&&M(b.iridescenceThicknessMap.channel),sheenColorMapUv:wt&&M(b.sheenColorMap.channel),sheenRoughnessMapUv:Gt&&M(b.sheenRoughnessMap.channel),specularMapUv:Pt&&M(b.specularMap.channel),specularColorMapUv:Lt&&M(b.specularColorMap.channel),specularIntensityMapUv:Jt&&M(b.specularIntensityMap.channel),transmissionMapUv:$t&&M(b.transmissionMap.channel),thicknessMapUv:oe&&M(b.thicknessMap.channel),alphaMapUv:Ct&&M(b.alphaMap.channel),vertexTangents:!!$.attributes.tangent&&(_e||j),vertexNormals:!!$.attributes.normal,vertexColors:b.vertexColors,vertexAlphas:b.vertexColors===!0&&!!$.attributes.color&&$.attributes.color.itemSize===4,pointsUvs:k.isPoints===!0&&!!$.attributes.uv&&(he||Ct),fog:!!tt,useFog:b.fog===!0,fogExp2:!!tt&&tt.isFogExp2,flatShading:b.wireframe===!1&&(b.flatShading===!0||$.attributes.normal===void 0&&_e===!1&&(b.isMeshLambertMaterial||b.isMeshPhongMaterial||b.isMeshStandardMaterial||b.isMeshPhysicalMaterial)),sizeAttenuation:b.sizeAttenuation===!0,logarithmicDepthBuffer:v,reversedDepthBuffer:bt,skinning:k.isSkinnedMesh===!0,hasPositionAttribute:$.attributes.position!==void 0,morphTargets:$.morphAttributes.position!==void 0,morphNormals:$.morphAttributes.normal!==void 0,morphColors:$.morphAttributes.color!==void 0,morphTargetsCount:Y,morphTextureStride:yt,numDirLights:N.directional.length,numPointLights:N.point.length,numSpotLights:N.spot.length,numSpotLightMaps:N.spotLightMap.length,numRectAreaLights:N.rectArea.length,numHemiLights:N.hemi.length,numDirLightShadows:N.directionalShadowMap.length,numPointLightShadows:N.pointShadowMap.length,numSpotLightShadows:N.spotShadowMap.length,numSpotLightShadowsWithMaps:N.numSpotLightShadowsWithMaps,numLightProbes:N.numLightProbes,numLightProbeGrids:K.length,numClippingPlanes:c.numPlanes,numClipIntersection:c.numIntersection,dithering:b.dithering,shadowMapEnabled:s.shadowMap.enabled&&G.length>0,shadowMapType:s.shadowMap.type,toneMapping:Tt,decodeVideoTexture:he&&b.map.isVideoTexture===!0&&Te.getTransfer(b.map.colorSpace)===Ve,decodeVideoTextureEmissive:Ke&&b.emissiveMap.isVideoTexture===!0&&Te.getTransfer(b.emissiveMap.colorSpace)===Ve,premultipliedAlpha:b.premultipliedAlpha,doubleSided:b.side===Yi,flipSided:b.side===Jn,useDepthPacking:b.depthPacking>=0,depthPacking:b.depthPacking||0,index0AttributeName:b.index0AttributeName,extensionClipCullDistance:Ft&&b.extensions.clipCullDistance===!0&&i.has("WEBGL_clip_cull_distance"),extensionMultiDraw:(Ft&&b.extensions.multiDraw===!0||Bt)&&i.has("WEBGL_multi_draw"),rendererExtensionParallelShaderCompile:i.has("KHR_parallel_shader_compile"),customProgramCacheKey:b.customProgramCacheKey()};return jt.vertexUv1s=m.has(1),jt.vertexUv2s=m.has(2),jt.vertexUv3s=m.has(3),m.clear(),jt}function E(b){const N=[];if(b.shaderID?N.push(b.shaderID):(N.push(b.customVertexShaderID),N.push(b.customFragmentShaderID)),b.defines!==void 0)for(const G in b.defines)N.push(G),N.push(b.defines[G]);return b.isRawShaderMaterial===!1&&(S(N,b),P(N,b),N.push(s.outputColorSpace)),N.push(b.customProgramCacheKey),N.join()}function S(b,N){b.push(N.precision),b.push(N.outputColorSpace),b.push(N.envMapMode),b.push(N.envMapCubeUVHeight),b.push(N.mapUv),b.push(N.alphaMapUv),b.push(N.lightMapUv),b.push(N.aoMapUv),b.push(N.bumpMapUv),b.push(N.normalMapUv),b.push(N.displacementMapUv),b.push(N.emissiveMapUv),b.push(N.metalnessMapUv),b.push(N.roughnessMapUv),b.push(N.anisotropyMapUv),b.push(N.clearcoatMapUv),b.push(N.clearcoatNormalMapUv),b.push(N.clearcoatRoughnessMapUv),b.push(N.iridescenceMapUv),b.push(N.iridescenceThicknessMapUv),b.push(N.sheenColorMapUv),b.push(N.sheenRoughnessMapUv),b.push(N.specularMapUv),b.push(N.specularColorMapUv),b.push(N.specularIntensityMapUv),b.push(N.transmissionMapUv),b.push(N.thicknessMapUv),b.push(N.combine),b.push(N.fogExp2),b.push(N.sizeAttenuation),b.push(N.morphTargetsCount),b.push(N.morphAttributeCount),b.push(N.numDirLights),b.push(N.numPointLights),b.push(N.numSpotLights),b.push(N.numSpotLightMaps),b.push(N.numHemiLights),b.push(N.numRectAreaLights),b.push(N.numDirLightShadows),b.push(N.numPointLightShadows),b.push(N.numSpotLightShadows),b.push(N.numSpotLightShadowsWithMaps),b.push(N.numLightProbes),b.push(N.shadowMapType),b.push(N.toneMapping),b.push(N.numClippingPlanes),b.push(N.numClipIntersection),b.push(N.depthPacking)}function P(b,N){f.disableAll(),N.instancing&&f.enable(0),N.instancingColor&&f.enable(1),N.instancingMorph&&f.enable(2),N.matcap&&f.enable(3),N.envMap&&f.enable(4),N.normalMapObjectSpace&&f.enable(5),N.normalMapTangentSpace&&f.enable(6),N.clearcoat&&f.enable(7),N.iridescence&&f.enable(8),N.alphaTest&&f.enable(9),N.vertexColors&&f.enable(10),N.vertexAlphas&&f.enable(11),N.vertexUv1s&&f.enable(12),N.vertexUv2s&&f.enable(13),N.vertexUv3s&&f.enable(14),N.vertexTangents&&f.enable(15),N.anisotropy&&f.enable(16),N.alphaHash&&f.enable(17),N.batching&&f.enable(18),N.dispersion&&f.enable(19),N.batchingColor&&f.enable(20),N.gradientMap&&f.enable(21),N.packedNormalMap&&f.enable(22),N.vertexNormals&&f.enable(23),b.push(f.mask),f.disableAll(),N.fog&&f.enable(0),N.useFog&&f.enable(1),N.flatShading&&f.enable(2),N.logarithmicDepthBuffer&&f.enable(3),N.reversedDepthBuffer&&f.enable(4),N.skinning&&f.enable(5),N.morphTargets&&f.enable(6),N.morphNormals&&f.enable(7),N.morphColors&&f.enable(8),N.premultipliedAlpha&&f.enable(9),N.shadowMapEnabled&&f.enable(10),N.doubleSided&&f.enable(11),N.flipSided&&f.enable(12),N.useDepthPacking&&f.enable(13),N.dithering&&f.enable(14),N.transmission&&f.enable(15),N.sheen&&f.enable(16),N.opaque&&f.enable(17),N.pointsUvs&&f.enable(18),N.decodeVideoTexture&&f.enable(19),N.decodeVideoTextureEmissive&&f.enable(20),N.alphaToCoverage&&f.enable(21),N.numLightProbeGrids>0&&f.enable(22),N.hasPositionAttribute&&f.enable(23),b.push(f.mask)}function O(b){const N=x[b.type];let G;if(N){const F=Wi[N];G=ub.clone(F.uniforms)}else G=b.uniforms;return G}function C(b,N){let G=_.get(N);return G!==void 0?++G.usedTimes:(G=new Y3(s,N,b,l),p.push(G),_.set(N,G)),G}function z(b){if(--b.usedTimes===0){const N=p.indexOf(b);p[N]=p[p.length-1],p.pop(),_.delete(b.cacheKey),b.destroy()}}function D(b){h.remove(b)}function U(){h.dispose()}return{getParameters:A,getProgramCacheKey:E,getUniforms:O,acquireProgram:C,releaseProgram:z,releaseShaderCache:D,programs:p,dispose:U}}function $3(){let s=new WeakMap;function t(f){return s.has(f)}function i(f){let h=s.get(f);return h===void 0&&(h={},s.set(f,h)),h}function r(f){s.delete(f)}function l(f,h,m){s.get(f)[h]=m}function c(){s=new WeakMap}return{has:t,get:i,remove:r,update:l,dispose:c}}function tR(s,t){return s.groupOrder!==t.groupOrder?s.groupOrder-t.groupOrder:s.renderOrder!==t.renderOrder?s.renderOrder-t.renderOrder:s.material.id!==t.material.id?s.material.id-t.material.id:s.materialVariant!==t.materialVariant?s.materialVariant-t.materialVariant:s.z!==t.z?s.z-t.z:s.id-t.id}function sx(s,t){return s.groupOrder!==t.groupOrder?s.groupOrder-t.groupOrder:s.renderOrder!==t.renderOrder?s.renderOrder-t.renderOrder:s.z!==t.z?t.z-s.z:s.id-t.id}function ox(){const s=[];let t=0;const i=[],r=[],l=[];function c(){t=0,i.length=0,r.length=0,l.length=0}function f(g){let x=0;return g.isInstancedMesh&&(x+=2),g.isSkinnedMesh&&(x+=1),x}function h(g,x,M,A,E,S){let P=s[t];return P===void 0?(P={id:g.id,object:g,geometry:x,material:M,materialVariant:f(g),groupOrder:A,renderOrder:g.renderOrder,z:E,group:S},s[t]=P):(P.id=g.id,P.object=g,P.geometry=x,P.material=M,P.materialVariant=f(g),P.groupOrder=A,P.renderOrder=g.renderOrder,P.z=E,P.group=S),t++,P}function m(g,x,M,A,E,S){const P=h(g,x,M,A,E,S);M.transmission>0?r.push(P):M.transparent===!0?l.push(P):i.push(P)}function p(g,x,M,A,E,S){const P=h(g,x,M,A,E,S);M.transmission>0?r.unshift(P):M.transparent===!0?l.unshift(P):i.unshift(P)}function _(g,x,M){i.length>1&&i.sort(g||tR),r.length>1&&r.sort(x||sx),l.length>1&&l.sort(x||sx),M&&(i.reverse(),r.reverse(),l.reverse())}function v(){for(let g=t,x=s.length;g<x;g++){const M=s[g];if(M.id===null)break;M.id=null,M.object=null,M.geometry=null,M.material=null,M.group=null}}return{opaque:i,transmissive:r,transparent:l,init:c,push:m,unshift:p,finish:v,sort:_}}function eR(){let s=new WeakMap;function t(r,l){const c=s.get(r);let f;return c===void 0?(f=new ox,s.set(r,[f])):l>=c.length?(f=new ox,c.push(f)):f=c[l],f}function i(){s=new WeakMap}return{get:t,dispose:i}}function nR(){const s={};return{get:function(t){if(s[t.id]!==void 0)return s[t.id];let i;switch(t.type){case"DirectionalLight":i={direction:new Q,color:new me};break;case"SpotLight":i={position:new Q,direction:new Q,color:new me,distance:0,coneCos:0,penumbraCos:0,decay:0};break;case"PointLight":i={position:new Q,color:new me,distance:0,decay:0};break;case"HemisphereLight":i={direction:new Q,skyColor:new me,groundColor:new me};break;case"RectAreaLight":i={color:new me,position:new Q,halfWidth:new Q,halfHeight:new Q};break}return s[t.id]=i,i}}}function iR(){const s={};return{get:function(t){if(s[t.id]!==void 0)return s[t.id];let i;switch(t.type){case"DirectionalLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new Ae};break;case"SpotLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new Ae};break;case"PointLight":i={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new Ae,shadowCameraNear:1,shadowCameraFar:1e3};break}return s[t.id]=i,i}}}let aR=0;function rR(s,t){return(t.castShadow?2:0)-(s.castShadow?2:0)+(t.map?1:0)-(s.map?1:0)}function sR(s){const t=new nR,i=iR(),r={version:0,hash:{directionalLength:-1,pointLength:-1,spotLength:-1,rectAreaLength:-1,hemiLength:-1,numDirectionalShadows:-1,numPointShadows:-1,numSpotShadows:-1,numSpotMaps:-1,numLightProbes:-1},ambient:[0,0,0],probe:[],directional:[],directionalShadow:[],directionalShadowMap:[],directionalShadowMatrix:[],spot:[],spotLightMap:[],spotShadow:[],spotShadowMap:[],spotLightMatrix:[],rectArea:[],rectAreaLTC1:null,rectAreaLTC2:null,point:[],pointShadow:[],pointShadowMap:[],pointShadowMatrix:[],hemi:[],numSpotLightShadowsWithMaps:0,numLightProbes:0};for(let p=0;p<9;p++)r.probe.push(new Q);const l=new Q,c=new $e,f=new $e;function h(p){let _=0,v=0,g=0;for(let N=0;N<9;N++)r.probe[N].set(0,0,0);let x=0,M=0,A=0,E=0,S=0,P=0,O=0,C=0,z=0,D=0,U=0;p.sort(rR);for(let N=0,G=p.length;N<G;N++){const F=p[N],k=F.color,K=F.intensity,tt=F.distance;let $=null;if(F.shadow&&F.shadow.map&&(F.shadow.map.texture.format===Yr?$=F.shadow.map.texture:$=F.shadow.map.depthTexture||F.shadow.map.texture),F.isAmbientLight)_+=k.r*K,v+=k.g*K,g+=k.b*K;else if(F.isLightProbe){for(let I=0;I<9;I++)r.probe[I].addScaledVector(F.sh.coefficients[I],K);U++}else if(F.isDirectionalLight){const I=t.get(F);if(I.color.copy(F.color).multiplyScalar(F.intensity),F.castShadow){const X=F.shadow,at=i.get(F);at.shadowIntensity=X.intensity,at.shadowBias=X.bias,at.shadowNormalBias=X.normalBias,at.shadowRadius=X.radius,at.shadowMapSize=X.mapSize,r.directionalShadow[x]=at,r.directionalShadowMap[x]=$,r.directionalShadowMatrix[x]=F.shadow.matrix,P++}r.directional[x]=I,x++}else if(F.isSpotLight){const I=t.get(F);I.position.setFromMatrixPosition(F.matrixWorld),I.color.copy(k).multiplyScalar(K),I.distance=tt,I.coneCos=Math.cos(F.angle),I.penumbraCos=Math.cos(F.angle*(1-F.penumbra)),I.decay=F.decay,r.spot[A]=I;const X=F.shadow;if(F.map&&(r.spotLightMap[z]=F.map,z++,X.updateMatrices(F),F.castShadow&&D++),r.spotLightMatrix[A]=X.matrix,F.castShadow){const at=i.get(F);at.shadowIntensity=X.intensity,at.shadowBias=X.bias,at.shadowNormalBias=X.normalBias,at.shadowRadius=X.radius,at.shadowMapSize=X.mapSize,r.spotShadow[A]=at,r.spotShadowMap[A]=$,C++}A++}else if(F.isRectAreaLight){const I=t.get(F);I.color.copy(k).multiplyScalar(K),I.halfWidth.set(F.width*.5,0,0),I.halfHeight.set(0,F.height*.5,0),r.rectArea[E]=I,E++}else if(F.isPointLight){const I=t.get(F);if(I.color.copy(F.color).multiplyScalar(F.intensity),I.distance=F.distance,I.decay=F.decay,F.castShadow){const X=F.shadow,at=i.get(F);at.shadowIntensity=X.intensity,at.shadowBias=X.bias,at.shadowNormalBias=X.normalBias,at.shadowRadius=X.radius,at.shadowMapSize=X.mapSize,at.shadowCameraNear=X.camera.near,at.shadowCameraFar=X.camera.far,r.pointShadow[M]=at,r.pointShadowMap[M]=$,r.pointShadowMatrix[M]=F.shadow.matrix,O++}r.point[M]=I,M++}else if(F.isHemisphereLight){const I=t.get(F);I.skyColor.copy(F.color).multiplyScalar(K),I.groundColor.copy(F.groundColor).multiplyScalar(K),r.hemi[S]=I,S++}}E>0&&(s.has("OES_texture_float_linear")===!0?(r.rectAreaLTC1=zt.LTC_FLOAT_1,r.rectAreaLTC2=zt.LTC_FLOAT_2):(r.rectAreaLTC1=zt.LTC_HALF_1,r.rectAreaLTC2=zt.LTC_HALF_2)),r.ambient[0]=_,r.ambient[1]=v,r.ambient[2]=g;const b=r.hash;(b.directionalLength!==x||b.pointLength!==M||b.spotLength!==A||b.rectAreaLength!==E||b.hemiLength!==S||b.numDirectionalShadows!==P||b.numPointShadows!==O||b.numSpotShadows!==C||b.numSpotMaps!==z||b.numLightProbes!==U)&&(r.directional.length=x,r.spot.length=A,r.rectArea.length=E,r.point.length=M,r.hemi.length=S,r.directionalShadow.length=P,r.directionalShadowMap.length=P,r.pointShadow.length=O,r.pointShadowMap.length=O,r.spotShadow.length=C,r.spotShadowMap.length=C,r.directionalShadowMatrix.length=P,r.pointShadowMatrix.length=O,r.spotLightMatrix.length=C+z-D,r.spotLightMap.length=z,r.numSpotLightShadowsWithMaps=D,r.numLightProbes=U,b.directionalLength=x,b.pointLength=M,b.spotLength=A,b.rectAreaLength=E,b.hemiLength=S,b.numDirectionalShadows=P,b.numPointShadows=O,b.numSpotShadows=C,b.numSpotMaps=z,b.numLightProbes=U,r.version=aR++)}function m(p,_){let v=0,g=0,x=0,M=0,A=0;const E=_.matrixWorldInverse;for(let S=0,P=p.length;S<P;S++){const O=p[S];if(O.isDirectionalLight){const C=r.directional[v];C.direction.setFromMatrixPosition(O.matrixWorld),l.setFromMatrixPosition(O.target.matrixWorld),C.direction.sub(l),C.direction.transformDirection(E),v++}else if(O.isSpotLight){const C=r.spot[x];C.position.setFromMatrixPosition(O.matrixWorld),C.position.applyMatrix4(E),C.direction.setFromMatrixPosition(O.matrixWorld),l.setFromMatrixPosition(O.target.matrixWorld),C.direction.sub(l),C.direction.transformDirection(E),x++}else if(O.isRectAreaLight){const C=r.rectArea[M];C.position.setFromMatrixPosition(O.matrixWorld),C.position.applyMatrix4(E),f.identity(),c.copy(O.matrixWorld),c.premultiply(E),f.extractRotation(c),C.halfWidth.set(O.width*.5,0,0),C.halfHeight.set(0,O.height*.5,0),C.halfWidth.applyMatrix4(f),C.halfHeight.applyMatrix4(f),M++}else if(O.isPointLight){const C=r.point[g];C.position.setFromMatrixPosition(O.matrixWorld),C.position.applyMatrix4(E),g++}else if(O.isHemisphereLight){const C=r.hemi[A];C.direction.setFromMatrixPosition(O.matrixWorld),C.direction.transformDirection(E),A++}}}return{setup:h,setupView:m,state:r}}function lx(s){const t=new sR(s),i=[],r=[],l=[];function c(g){v.camera=g,i.length=0,r.length=0,l.length=0}function f(g){i.push(g)}function h(g){r.push(g)}function m(g){l.push(g)}function p(){t.setup(i)}function _(g){t.setupView(i,g)}const v={lightsArray:i,shadowsArray:r,lightProbeGridArray:l,camera:null,lights:t,transmissionRenderTarget:{},textureUnits:0};return{init:c,state:v,setupLights:p,setupLightsView:_,pushLight:f,pushShadow:h,pushLightProbeGrid:m}}function oR(s){let t=new WeakMap;function i(l,c=0){const f=t.get(l);let h;return f===void 0?(h=new lx(s),t.set(l,[h])):c>=f.length?(h=new lx(s),f.push(h)):h=f[c],h}function r(){t=new WeakMap}return{get:i,dispose:r}}const lR=`void main() {
	gl_Position = vec4( position, 1.0 );
}`,cR=`uniform sampler2D shadow_pass;
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
}`,uR=[new Q(1,0,0),new Q(-1,0,0),new Q(0,1,0),new Q(0,-1,0),new Q(0,0,1),new Q(0,0,-1)],fR=[new Q(0,-1,0),new Q(0,-1,0),new Q(0,0,1),new Q(0,0,-1),new Q(0,-1,0),new Q(0,-1,0)],cx=new $e,rl=new Q,gh=new Q;function dR(s,t,i){let r=new jp;const l=new Ae,c=new Ae,f=new sn,h=new pb,m=new mb,p={},_=i.maxTextureSize,v={[pr]:Jn,[Jn]:pr,[Yi]:Yi},g=new ta({defines:{VSM_SAMPLES:8},uniforms:{shadow_pass:{value:null},resolution:{value:new Ae},radius:{value:4}},vertexShader:lR,fragmentShader:cR}),x=g.clone();x.defines.HORIZONTAL_PASS=1;const M=new bn;M.setAttribute("position",new Ti(new Float32Array([-1,-1,.5,3,-1,.5,-1,3,.5]),3));const A=new Ye(M,g),E=this;this.enabled=!1,this.autoUpdate=!0,this.needsUpdate=!1,this.type=ul;let S=this.type;this.render=function(D,U,b){if(E.enabled===!1||E.autoUpdate===!1&&E.needsUpdate===!1||D.length===0)return;this.type===V1&&(se("WebGLShadowMap: PCFSoftShadowMap has been deprecated. Using PCFShadowMap instead."),this.type=ul);const N=s.getRenderTarget(),G=s.getActiveCubeFace(),F=s.getActiveMipmapLevel(),k=s.state;k.setBlending(Ca),k.buffers.depth.getReversed()===!0?k.buffers.color.setClear(0,0,0,0):k.buffers.color.setClear(1,1,1,1),k.buffers.depth.setTest(!0),k.setScissorTest(!1);const K=S!==this.type;K&&U.traverse(function(tt){tt.material&&(Array.isArray(tt.material)?tt.material.forEach($=>$.needsUpdate=!0):tt.material.needsUpdate=!0)});for(let tt=0,$=D.length;tt<$;tt++){const I=D[tt],X=I.shadow;if(X===void 0){se("WebGLShadowMap:",I,"has no shadow.");continue}if(X.autoUpdate===!1&&X.needsUpdate===!1)continue;l.copy(X.mapSize);const at=X.getFrameExtents();l.multiply(at),c.copy(X.mapSize),(l.x>_||l.y>_)&&(l.x>_&&(c.x=Math.floor(_/at.x),l.x=c.x*at.x,X.mapSize.x=c.x),l.y>_&&(c.y=Math.floor(_/at.y),l.y=c.y*at.y,X.mapSize.y=c.y));const _t=s.state.buffers.depth.getReversed();if(X.camera._reversedDepth=_t,X.map===null||K===!0){if(X.map!==null&&(X.map.depthTexture!==null&&(X.map.depthTexture.dispose(),X.map.depthTexture=null),X.map.dispose()),this.type===ll){if(I.isPointLight){se("WebGLShadowMap: VSM shadow maps are not supported for PointLights. Use PCF or BasicShadowMap instead.");continue}X.map=new Qi(l.x,l.y,{format:Yr,type:Da,minFilter:wn,magFilter:wn,generateMipmaps:!1}),X.map.texture.name=I.name+".shadowMap",X.map.depthTexture=new no(l.x,l.y,Zi),X.map.depthTexture.name=I.name+".shadowMapDepth",X.map.depthTexture.format=Na,X.map.depthTexture.compareFunction=null,X.map.depthTexture.minFilter=Pn,X.map.depthTexture.magFilter=Pn}else I.isPointLight?(X.map=new yS(l.x),X.map.depthTexture=new ob(l.x,$i)):(X.map=new Qi(l.x,l.y),X.map.depthTexture=new no(l.x,l.y,$i)),X.map.depthTexture.name=I.name+".shadowMap",X.map.depthTexture.format=Na,this.type===ul?(X.map.depthTexture.compareFunction=_t?Xp:kp,X.map.depthTexture.minFilter=wn,X.map.depthTexture.magFilter=wn):(X.map.depthTexture.compareFunction=null,X.map.depthTexture.minFilter=Pn,X.map.depthTexture.magFilter=Pn);X.camera.updateProjectionMatrix()}const vt=X.map.isWebGLCubeRenderTarget?6:1;for(let L=0;L<vt;L++){if(X.map.isWebGLCubeRenderTarget)s.setRenderTarget(X.map,L),s.clear();else{L===0&&(s.setRenderTarget(X.map),s.clear());const Y=X.getViewport(L);f.set(c.x*Y.x,c.y*Y.y,c.x*Y.z,c.y*Y.w),k.viewport(f)}if(I.isPointLight){const Y=X.camera,yt=X.matrix,At=I.distance||Y.far;At!==Y.far&&(Y.far=At,Y.updateProjectionMatrix()),rl.setFromMatrixPosition(I.matrixWorld),Y.position.copy(rl),gh.copy(Y.position),gh.add(uR[L]),Y.up.copy(fR[L]),Y.lookAt(gh),Y.updateMatrixWorld(),yt.makeTranslation(-rl.x,-rl.y,-rl.z),cx.multiplyMatrices(Y.projectionMatrix,Y.matrixWorldInverse),X._frustum.setFromProjectionMatrix(cx,Y.coordinateSystem,Y.reversedDepth)}else X.updateMatrices(I);r=X.getFrustum(),C(U,b,X.camera,I,this.type)}X.isPointLightShadow!==!0&&this.type===ll&&P(X,b),X.needsUpdate=!1}S=this.type,E.needsUpdate=!1,s.setRenderTarget(N,G,F)};function P(D,U){const b=t.update(A);g.defines.VSM_SAMPLES!==D.blurSamples&&(g.defines.VSM_SAMPLES=D.blurSamples,x.defines.VSM_SAMPLES=D.blurSamples,g.needsUpdate=!0,x.needsUpdate=!0),D.mapPass===null&&(D.mapPass=new Qi(l.x,l.y,{format:Yr,type:Da})),g.uniforms.shadow_pass.value=D.map.depthTexture,g.uniforms.resolution.value=D.mapSize,g.uniforms.radius.value=D.radius,s.setRenderTarget(D.mapPass),s.clear(),s.renderBufferDirect(U,null,b,g,A,null),x.uniforms.shadow_pass.value=D.mapPass.texture,x.uniforms.resolution.value=D.mapSize,x.uniforms.radius.value=D.radius,s.setRenderTarget(D.map),s.clear(),s.renderBufferDirect(U,null,b,x,A,null)}function O(D,U,b,N){let G=null;const F=b.isPointLight===!0?D.customDistanceMaterial:D.customDepthMaterial;if(F!==void 0)G=F;else if(G=b.isPointLight===!0?m:h,s.localClippingEnabled&&U.clipShadows===!0&&Array.isArray(U.clippingPlanes)&&U.clippingPlanes.length!==0||U.displacementMap&&U.displacementScale!==0||U.alphaMap&&U.alphaTest>0||U.map&&U.alphaTest>0||U.alphaToCoverage===!0){const k=G.uuid,K=U.uuid;let tt=p[k];tt===void 0&&(tt={},p[k]=tt);let $=tt[K];$===void 0&&($=G.clone(),tt[K]=$,U.addEventListener("dispose",z)),G=$}if(G.visible=U.visible,G.wireframe=U.wireframe,N===ll?G.side=U.shadowSide!==null?U.shadowSide:U.side:G.side=U.shadowSide!==null?U.shadowSide:v[U.side],G.alphaMap=U.alphaMap,G.alphaTest=U.alphaToCoverage===!0?.5:U.alphaTest,G.map=U.map,G.clipShadows=U.clipShadows,G.clippingPlanes=U.clippingPlanes,G.clipIntersection=U.clipIntersection,G.displacementMap=U.displacementMap,G.displacementScale=U.displacementScale,G.displacementBias=U.displacementBias,G.wireframeLinewidth=U.wireframeLinewidth,G.linewidth=U.linewidth,b.isPointLight===!0&&G.isMeshDistanceMaterial===!0){const k=s.properties.get(G);k.light=b}return G}function C(D,U,b,N,G){if(D.visible===!1)return;if(D.layers.test(U.layers)&&(D.isMesh||D.isLine||D.isPoints)&&(D.castShadow||D.receiveShadow&&G===ll)&&(!D.frustumCulled||r.intersectsObject(D))){D.modelViewMatrix.multiplyMatrices(b.matrixWorldInverse,D.matrixWorld);const K=t.update(D),tt=D.material;if(Array.isArray(tt)){const $=K.groups;for(let I=0,X=$.length;I<X;I++){const at=$[I],_t=tt[at.materialIndex];if(_t&&_t.visible){const vt=O(D,_t,N,G);D.onBeforeShadow(s,D,U,b,K,vt,at),s.renderBufferDirect(b,null,K,vt,D,at),D.onAfterShadow(s,D,U,b,K,vt,at)}}}else if(tt.visible){const $=O(D,tt,N,G);D.onBeforeShadow(s,D,U,b,K,$,null),s.renderBufferDirect(b,null,K,$,D,null),D.onAfterShadow(s,D,U,b,K,$,null)}}const k=D.children;for(let K=0,tt=k.length;K<tt;K++)C(k[K],U,b,N,G)}function z(D){D.target.removeEventListener("dispose",z);for(const b in p){const N=p[b],G=D.target.uuid;G in N&&(N[G].dispose(),delete N[G])}}}function hR(s,t){function i(){let W=!1;const Ct=new sn;let St=null;const Nt=new sn(0,0,0,0);return{setMask:function(Ft){St!==Ft&&!W&&(s.colorMask(Ft,Ft,Ft,Ft),St=Ft)},setLocked:function(Ft){W=Ft},setClear:function(Ft,Tt,jt,qt,tn){tn===!0&&(Ft*=qt,Tt*=qt,jt*=qt),Ct.set(Ft,Tt,jt,qt),Nt.equals(Ct)===!1&&(s.clearColor(Ft,Tt,jt,qt),Nt.copy(Ct))},reset:function(){W=!1,St=null,Nt.set(-1,0,0,0)}}}function r(){let W=!1,Ct=!1,St=null,Nt=null,Ft=null;return{setReversed:function(Tt){if(Ct!==Tt){const jt=t.get("EXT_clip_control");Tt?jt.clipControlEXT(jt.LOWER_LEFT_EXT,jt.ZERO_TO_ONE_EXT):jt.clipControlEXT(jt.LOWER_LEFT_EXT,jt.NEGATIVE_ONE_TO_ONE_EXT),Ct=Tt;const qt=Ft;Ft=null,this.setClear(qt)}},getReversed:function(){return Ct},setTest:function(Tt){Tt?ct(s.DEPTH_TEST):bt(s.DEPTH_TEST)},setMask:function(Tt){St!==Tt&&!W&&(s.depthMask(Tt),St=Tt)},setFunc:function(Tt){if(Ct&&(Tt=yE[Tt]),Nt!==Tt){switch(Tt){case Vh:s.depthFunc(s.NEVER);break;case kh:s.depthFunc(s.ALWAYS);break;case Xh:s.depthFunc(s.LESS);break;case to:s.depthFunc(s.LEQUAL);break;case qh:s.depthFunc(s.EQUAL);break;case Wh:s.depthFunc(s.GEQUAL);break;case Yh:s.depthFunc(s.GREATER);break;case Zh:s.depthFunc(s.NOTEQUAL);break;default:s.depthFunc(s.LEQUAL)}Nt=Tt}},setLocked:function(Tt){W=Tt},setClear:function(Tt){Ft!==Tt&&(Ft=Tt,Ct&&(Tt=1-Tt),s.clearDepth(Tt))},reset:function(){W=!1,St=null,Nt=null,Ft=null,Ct=!1}}}function l(){let W=!1,Ct=null,St=null,Nt=null,Ft=null,Tt=null,jt=null,qt=null,tn=null;return{setTest:function(Be){W||(Be?ct(s.STENCIL_TEST):bt(s.STENCIL_TEST))},setMask:function(Be){Ct!==Be&&!W&&(s.stencilMask(Be),Ct=Be)},setFunc:function(Be,ti,ei){(St!==Be||Nt!==ti||Ft!==ei)&&(s.stencilFunc(Be,ti,ei),St=Be,Nt=ti,Ft=ei)},setOp:function(Be,ti,ei){(Tt!==Be||jt!==ti||qt!==ei)&&(s.stencilOp(Be,ti,ei),Tt=Be,jt=ti,qt=ei)},setLocked:function(Be){W=Be},setClear:function(Be){tn!==Be&&(s.clearStencil(Be),tn=Be)},reset:function(){W=!1,Ct=null,St=null,Nt=null,Ft=null,Tt=null,jt=null,qt=null,tn=null}}}const c=new i,f=new r,h=new l,m=new WeakMap,p=new WeakMap;let _={},v={},g={},x=new WeakMap,M=[],A=null,E=!1,S=null,P=null,O=null,C=null,z=null,D=null,U=null,b=new me(0,0,0),N=0,G=!1,F=null,k=null,K=null,tt=null,$=null;const I=s.getParameter(s.MAX_COMBINED_TEXTURE_IMAGE_UNITS);let X=!1,at=0;const _t=s.getParameter(s.VERSION);_t.indexOf("WebGL")!==-1?(at=parseFloat(/^WebGL (\d)/.exec(_t)[1]),X=at>=1):_t.indexOf("OpenGL ES")!==-1&&(at=parseFloat(/^OpenGL ES (\d)/.exec(_t)[1]),X=at>=2);let vt=null,L={};const Y=s.getParameter(s.SCISSOR_BOX),yt=s.getParameter(s.VIEWPORT),At=new sn().fromArray(Y),Dt=new sn().fromArray(yt);function it(W,Ct,St,Nt){const Ft=new Uint8Array(4),Tt=s.createTexture();s.bindTexture(W,Tt),s.texParameteri(W,s.TEXTURE_MIN_FILTER,s.NEAREST),s.texParameteri(W,s.TEXTURE_MAG_FILTER,s.NEAREST);for(let jt=0;jt<St;jt++)W===s.TEXTURE_3D||W===s.TEXTURE_2D_ARRAY?s.texImage3D(Ct,0,s.RGBA,1,1,Nt,0,s.RGBA,s.UNSIGNED_BYTE,Ft):s.texImage2D(Ct+jt,0,s.RGBA,1,1,0,s.RGBA,s.UNSIGNED_BYTE,Ft);return Tt}const nt={};nt[s.TEXTURE_2D]=it(s.TEXTURE_2D,s.TEXTURE_2D,1),nt[s.TEXTURE_CUBE_MAP]=it(s.TEXTURE_CUBE_MAP,s.TEXTURE_CUBE_MAP_POSITIVE_X,6),nt[s.TEXTURE_2D_ARRAY]=it(s.TEXTURE_2D_ARRAY,s.TEXTURE_2D_ARRAY,1,1),nt[s.TEXTURE_3D]=it(s.TEXTURE_3D,s.TEXTURE_3D,1,1),c.setClear(0,0,0,1),f.setClear(1),h.setClear(0),ct(s.DEPTH_TEST),f.setFunc(to),ge(!1),_e(ev),ct(s.CULL_FACE),Xt(Ca);function ct(W){_[W]!==!0&&(s.enable(W),_[W]=!0)}function bt(W){_[W]!==!1&&(s.disable(W),_[W]=!1)}function Ht(W,Ct){return g[W]!==Ct?(s.bindFramebuffer(W,Ct),g[W]=Ct,W===s.DRAW_FRAMEBUFFER&&(g[s.FRAMEBUFFER]=Ct),W===s.FRAMEBUFFER&&(g[s.DRAW_FRAMEBUFFER]=Ct),!0):!1}function Bt(W,Ct){let St=M,Nt=!1;if(W){St=x.get(Ct),St===void 0&&(St=[],x.set(Ct,St));const Ft=W.textures;if(St.length!==Ft.length||St[0]!==s.COLOR_ATTACHMENT0){for(let Tt=0,jt=Ft.length;Tt<jt;Tt++)St[Tt]=s.COLOR_ATTACHMENT0+Tt;St.length=Ft.length,Nt=!0}}else St[0]!==s.BACK&&(St[0]=s.BACK,Nt=!0);Nt&&s.drawBuffers(St)}function he(W){return A!==W?(s.useProgram(W),A=W,!0):!1}const ne={[Gr]:s.FUNC_ADD,[X1]:s.FUNC_SUBTRACT,[q1]:s.FUNC_REVERSE_SUBTRACT};ne[W1]=s.MIN,ne[Y1]=s.MAX;const ie={[Z1]:s.ZERO,[j1]:s.ONE,[K1]:s.SRC_COLOR,[Hh]:s.SRC_ALPHA,[nE]:s.SRC_ALPHA_SATURATE,[tE]:s.DST_COLOR,[J1]:s.DST_ALPHA,[Q1]:s.ONE_MINUS_SRC_COLOR,[Gh]:s.ONE_MINUS_SRC_ALPHA,[eE]:s.ONE_MINUS_DST_COLOR,[$1]:s.ONE_MINUS_DST_ALPHA,[iE]:s.CONSTANT_COLOR,[aE]:s.ONE_MINUS_CONSTANT_COLOR,[rE]:s.CONSTANT_ALPHA,[sE]:s.ONE_MINUS_CONSTANT_ALPHA};function Xt(W,Ct,St,Nt,Ft,Tt,jt,qt,tn,Be){if(W===Ca){E===!0&&(bt(s.BLEND),E=!1);return}if(E===!1&&(ct(s.BLEND),E=!0),W!==k1){if(W!==S||Be!==G){if((P!==Gr||z!==Gr)&&(s.blendEquation(s.FUNC_ADD),P=Gr,z=Gr),Be)switch(W){case js:s.blendFuncSeparate(s.ONE,s.ONE_MINUS_SRC_ALPHA,s.ONE,s.ONE_MINUS_SRC_ALPHA);break;case Oi:s.blendFunc(s.ONE,s.ONE);break;case nv:s.blendFuncSeparate(s.ZERO,s.ONE_MINUS_SRC_COLOR,s.ZERO,s.ONE);break;case iv:s.blendFuncSeparate(s.DST_COLOR,s.ONE_MINUS_SRC_ALPHA,s.ZERO,s.ONE);break;default:Re("WebGLState: Invalid blending: ",W);break}else switch(W){case js:s.blendFuncSeparate(s.SRC_ALPHA,s.ONE_MINUS_SRC_ALPHA,s.ONE,s.ONE_MINUS_SRC_ALPHA);break;case Oi:s.blendFuncSeparate(s.SRC_ALPHA,s.ONE,s.ONE,s.ONE);break;case nv:Re("WebGLState: SubtractiveBlending requires material.premultipliedAlpha = true");break;case iv:Re("WebGLState: MultiplyBlending requires material.premultipliedAlpha = true");break;default:Re("WebGLState: Invalid blending: ",W);break}O=null,C=null,D=null,U=null,b.set(0,0,0),N=0,S=W,G=Be}return}Ft=Ft||Ct,Tt=Tt||St,jt=jt||Nt,(Ct!==P||Ft!==z)&&(s.blendEquationSeparate(ne[Ct],ne[Ft]),P=Ct,z=Ft),(St!==O||Nt!==C||Tt!==D||jt!==U)&&(s.blendFuncSeparate(ie[St],ie[Nt],ie[Tt],ie[jt]),O=St,C=Nt,D=Tt,U=jt),(qt.equals(b)===!1||tn!==N)&&(s.blendColor(qt.r,qt.g,qt.b,tn),b.copy(qt),N=tn),S=W,G=!1}function ae(W,Ct){W.side===Yi?bt(s.CULL_FACE):ct(s.CULL_FACE);let St=W.side===Jn;Ct&&(St=!St),ge(St),W.blending===js&&W.transparent===!1?Xt(Ca):Xt(W.blending,W.blendEquation,W.blendSrc,W.blendDst,W.blendEquationAlpha,W.blendSrcAlpha,W.blendDstAlpha,W.blendColor,W.blendAlpha,W.premultipliedAlpha),f.setFunc(W.depthFunc),f.setTest(W.depthTest),f.setMask(W.depthWrite),c.setMask(W.colorWrite);const Nt=W.stencilWrite;h.setTest(Nt),Nt&&(h.setMask(W.stencilWriteMask),h.setFunc(W.stencilFunc,W.stencilRef,W.stencilFuncMask),h.setOp(W.stencilFail,W.stencilZFail,W.stencilZPass)),Ke(W.polygonOffset,W.polygonOffsetFactor,W.polygonOffsetUnits),W.alphaToCoverage===!0?ct(s.SAMPLE_ALPHA_TO_COVERAGE):bt(s.SAMPLE_ALPHA_TO_COVERAGE)}function ge(W){F!==W&&(W?s.frontFace(s.CW):s.frontFace(s.CCW),F=W)}function _e(W){W!==H1?(ct(s.CULL_FACE),W!==k&&(W===ev?s.cullFace(s.BACK):W===G1?s.cullFace(s.FRONT):s.cullFace(s.FRONT_AND_BACK))):bt(s.CULL_FACE),k=W}function Ce(W){W!==K&&(X&&s.lineWidth(W),K=W)}function Ke(W,Ct,St){W?(ct(s.POLYGON_OFFSET_FILL),(tt!==Ct||$!==St)&&(tt=Ct,$=St,f.getReversed()&&(Ct=-Ct),s.polygonOffset(Ct,St))):bt(s.POLYGON_OFFSET_FILL)}function Ue(W){W?ct(s.SCISSOR_TEST):bt(s.SCISSOR_TEST)}function on(W){W===void 0&&(W=s.TEXTURE0+I-1),vt!==W&&(s.activeTexture(W),vt=W)}function j(W,Ct,St){St===void 0&&(vt===null?St=s.TEXTURE0+I-1:St=vt);let Nt=L[St];Nt===void 0&&(Nt={type:void 0,texture:void 0},L[St]=Nt),(Nt.type!==W||Nt.texture!==Ct)&&(vt!==St&&(s.activeTexture(St),vt=St),s.bindTexture(W,Ct||nt[W]),Nt.type=W,Nt.texture=Ct)}function ke(){const W=L[vt];W!==void 0&&W.type!==void 0&&(s.bindTexture(W.type,null),W.type=void 0,W.texture=void 0)}function Le(){try{s.compressedTexImage2D(...arguments)}catch(W){Re("WebGLState:",W)}}function B(){try{s.compressedTexImage3D(...arguments)}catch(W){Re("WebGLState:",W)}}function T(){try{s.texSubImage2D(...arguments)}catch(W){Re("WebGLState:",W)}}function et(){try{s.texSubImage3D(...arguments)}catch(W){Re("WebGLState:",W)}}function ut(){try{s.compressedTexSubImage2D(...arguments)}catch(W){Re("WebGLState:",W)}}function mt(){try{s.compressedTexSubImage3D(...arguments)}catch(W){Re("WebGLState:",W)}}function Rt(){try{s.texStorage2D(...arguments)}catch(W){Re("WebGLState:",W)}}function Ut(){try{s.texStorage3D(...arguments)}catch(W){Re("WebGLState:",W)}}function pt(){try{s.texImage2D(...arguments)}catch(W){Re("WebGLState:",W)}}function gt(){try{s.texImage3D(...arguments)}catch(W){Re("WebGLState:",W)}}function wt(W){return v[W]!==void 0?v[W]:s.getParameter(W)}function Gt(W,Ct){v[W]!==Ct&&(s.pixelStorei(W,Ct),v[W]=Ct)}function Pt(W){At.equals(W)===!1&&(s.scissor(W.x,W.y,W.z,W.w),At.copy(W))}function Lt(W){Dt.equals(W)===!1&&(s.viewport(W.x,W.y,W.z,W.w),Dt.copy(W))}function Jt(W,Ct){let St=p.get(Ct);St===void 0&&(St=new WeakMap,p.set(Ct,St));let Nt=St.get(W);Nt===void 0&&(Nt=s.getUniformBlockIndex(Ct,W.name),St.set(W,Nt))}function $t(W,Ct){const Nt=p.get(Ct).get(W);m.get(Ct)!==Nt&&(s.uniformBlockBinding(Ct,Nt,W.__bindingPointIndex),m.set(Ct,Nt))}function oe(){s.disable(s.BLEND),s.disable(s.CULL_FACE),s.disable(s.DEPTH_TEST),s.disable(s.POLYGON_OFFSET_FILL),s.disable(s.SCISSOR_TEST),s.disable(s.STENCIL_TEST),s.disable(s.SAMPLE_ALPHA_TO_COVERAGE),s.blendEquation(s.FUNC_ADD),s.blendFunc(s.ONE,s.ZERO),s.blendFuncSeparate(s.ONE,s.ZERO,s.ONE,s.ZERO),s.blendColor(0,0,0,0),s.colorMask(!0,!0,!0,!0),s.clearColor(0,0,0,0),s.depthMask(!0),s.depthFunc(s.LESS),f.setReversed(!1),s.clearDepth(1),s.stencilMask(4294967295),s.stencilFunc(s.ALWAYS,0,4294967295),s.stencilOp(s.KEEP,s.KEEP,s.KEEP),s.clearStencil(0),s.cullFace(s.BACK),s.frontFace(s.CCW),s.polygonOffset(0,0),s.activeTexture(s.TEXTURE0),s.bindFramebuffer(s.FRAMEBUFFER,null),s.bindFramebuffer(s.DRAW_FRAMEBUFFER,null),s.bindFramebuffer(s.READ_FRAMEBUFFER,null),s.useProgram(null),s.lineWidth(1),s.scissor(0,0,s.canvas.width,s.canvas.height),s.viewport(0,0,s.canvas.width,s.canvas.height),s.pixelStorei(s.PACK_ALIGNMENT,4),s.pixelStorei(s.UNPACK_ALIGNMENT,4),s.pixelStorei(s.UNPACK_FLIP_Y_WEBGL,!1),s.pixelStorei(s.UNPACK_PREMULTIPLY_ALPHA_WEBGL,!1),s.pixelStorei(s.UNPACK_COLORSPACE_CONVERSION_WEBGL,s.BROWSER_DEFAULT_WEBGL),s.pixelStorei(s.PACK_ROW_LENGTH,0),s.pixelStorei(s.PACK_SKIP_PIXELS,0),s.pixelStorei(s.PACK_SKIP_ROWS,0),s.pixelStorei(s.UNPACK_ROW_LENGTH,0),s.pixelStorei(s.UNPACK_IMAGE_HEIGHT,0),s.pixelStorei(s.UNPACK_SKIP_PIXELS,0),s.pixelStorei(s.UNPACK_SKIP_ROWS,0),s.pixelStorei(s.UNPACK_SKIP_IMAGES,0),_={},v={},vt=null,L={},g={},x=new WeakMap,M=[],A=null,E=!1,S=null,P=null,O=null,C=null,z=null,D=null,U=null,b=new me(0,0,0),N=0,G=!1,F=null,k=null,K=null,tt=null,$=null,At.set(0,0,s.canvas.width,s.canvas.height),Dt.set(0,0,s.canvas.width,s.canvas.height),c.reset(),f.reset(),h.reset()}return{buffers:{color:c,depth:f,stencil:h},enable:ct,disable:bt,bindFramebuffer:Ht,drawBuffers:Bt,useProgram:he,setBlending:Xt,setMaterial:ae,setFlipSided:ge,setCullFace:_e,setLineWidth:Ce,setPolygonOffset:Ke,setScissorTest:Ue,activeTexture:on,bindTexture:j,unbindTexture:ke,compressedTexImage2D:Le,compressedTexImage3D:B,texImage2D:pt,texImage3D:gt,pixelStorei:Gt,getParameter:wt,updateUBOMapping:Jt,uniformBlockBinding:$t,texStorage2D:Rt,texStorage3D:Ut,texSubImage2D:T,texSubImage3D:et,compressedTexSubImage2D:ut,compressedTexSubImage3D:mt,scissor:Pt,viewport:Lt,reset:oe}}function pR(s,t,i,r,l,c,f){const h=t.has("WEBGL_multisampled_render_to_texture")?t.get("WEBGL_multisampled_render_to_texture"):null,m=typeof navigator>"u"?!1:/OculusBrowser/g.test(navigator.userAgent),p=new Ae,_=new WeakMap,v=new Set;let g;const x=new WeakMap;let M=!1;try{M=typeof OffscreenCanvas<"u"&&new OffscreenCanvas(1,1).getContext("2d")!==null}catch{}function A(B,T){return M?new OffscreenCanvas(B,T):ml("canvas")}function E(B,T,et){let ut=1;const mt=Le(B);if((mt.width>et||mt.height>et)&&(ut=et/Math.max(mt.width,mt.height)),ut<1)if(typeof HTMLImageElement<"u"&&B instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&B instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&B instanceof ImageBitmap||typeof VideoFrame<"u"&&B instanceof VideoFrame){const Rt=Math.floor(ut*mt.width),Ut=Math.floor(ut*mt.height);g===void 0&&(g=A(Rt,Ut));const pt=T?A(Rt,Ut):g;return pt.width=Rt,pt.height=Ut,pt.getContext("2d").drawImage(B,0,0,Rt,Ut),se("WebGLRenderer: Texture has been resized from ("+mt.width+"x"+mt.height+") to ("+Rt+"x"+Ut+")."),pt}else return"data"in B&&se("WebGLRenderer: Image in DataTexture is too big ("+mt.width+"x"+mt.height+")."),B;return B}function S(B){return B.generateMipmaps}function P(B){s.generateMipmap(B)}function O(B){return B.isWebGLCubeRenderTarget?s.TEXTURE_CUBE_MAP:B.isWebGL3DRenderTarget?s.TEXTURE_3D:B.isWebGLArrayRenderTarget||B.isCompressedArrayTexture?s.TEXTURE_2D_ARRAY:s.TEXTURE_2D}function C(B,T,et,ut,mt,Rt=!1){if(B!==null){if(s[B]!==void 0)return s[B];se("WebGLRenderer: Attempt to use non-existing WebGL internal format '"+B+"'")}let Ut;ut&&(Ut=t.get("EXT_texture_norm16"),Ut||se("WebGLRenderer: Unable to use normalized textures without EXT_texture_norm16 extension"));let pt=T;if(T===s.RED&&(et===s.FLOAT&&(pt=s.R32F),et===s.HALF_FLOAT&&(pt=s.R16F),et===s.UNSIGNED_BYTE&&(pt=s.R8),et===s.UNSIGNED_SHORT&&Ut&&(pt=Ut.R16_EXT),et===s.SHORT&&Ut&&(pt=Ut.R16_SNORM_EXT)),T===s.RED_INTEGER&&(et===s.UNSIGNED_BYTE&&(pt=s.R8UI),et===s.UNSIGNED_SHORT&&(pt=s.R16UI),et===s.UNSIGNED_INT&&(pt=s.R32UI),et===s.BYTE&&(pt=s.R8I),et===s.SHORT&&(pt=s.R16I),et===s.INT&&(pt=s.R32I)),T===s.RG&&(et===s.FLOAT&&(pt=s.RG32F),et===s.HALF_FLOAT&&(pt=s.RG16F),et===s.UNSIGNED_BYTE&&(pt=s.RG8),et===s.UNSIGNED_SHORT&&Ut&&(pt=Ut.RG16_EXT),et===s.SHORT&&Ut&&(pt=Ut.RG16_SNORM_EXT)),T===s.RG_INTEGER&&(et===s.UNSIGNED_BYTE&&(pt=s.RG8UI),et===s.UNSIGNED_SHORT&&(pt=s.RG16UI),et===s.UNSIGNED_INT&&(pt=s.RG32UI),et===s.BYTE&&(pt=s.RG8I),et===s.SHORT&&(pt=s.RG16I),et===s.INT&&(pt=s.RG32I)),T===s.RGB_INTEGER&&(et===s.UNSIGNED_BYTE&&(pt=s.RGB8UI),et===s.UNSIGNED_SHORT&&(pt=s.RGB16UI),et===s.UNSIGNED_INT&&(pt=s.RGB32UI),et===s.BYTE&&(pt=s.RGB8I),et===s.SHORT&&(pt=s.RGB16I),et===s.INT&&(pt=s.RGB32I)),T===s.RGBA_INTEGER&&(et===s.UNSIGNED_BYTE&&(pt=s.RGBA8UI),et===s.UNSIGNED_SHORT&&(pt=s.RGBA16UI),et===s.UNSIGNED_INT&&(pt=s.RGBA32UI),et===s.BYTE&&(pt=s.RGBA8I),et===s.SHORT&&(pt=s.RGBA16I),et===s.INT&&(pt=s.RGBA32I)),T===s.RGB&&(et===s.UNSIGNED_SHORT&&Ut&&(pt=Ut.RGB16_EXT),et===s.SHORT&&Ut&&(pt=Ut.RGB16_SNORM_EXT),et===s.UNSIGNED_INT_5_9_9_9_REV&&(pt=s.RGB9_E5),et===s.UNSIGNED_INT_10F_11F_11F_REV&&(pt=s.R11F_G11F_B10F)),T===s.RGBA){const gt=Rt?mu:Te.getTransfer(mt);et===s.FLOAT&&(pt=s.RGBA32F),et===s.HALF_FLOAT&&(pt=s.RGBA16F),et===s.UNSIGNED_BYTE&&(pt=gt===Ve?s.SRGB8_ALPHA8:s.RGBA8),et===s.UNSIGNED_SHORT&&Ut&&(pt=Ut.RGBA16_EXT),et===s.SHORT&&Ut&&(pt=Ut.RGBA16_SNORM_EXT),et===s.UNSIGNED_SHORT_4_4_4_4&&(pt=s.RGBA4),et===s.UNSIGNED_SHORT_5_5_5_1&&(pt=s.RGB5_A1)}return(pt===s.R16F||pt===s.R32F||pt===s.RG16F||pt===s.RG32F||pt===s.RGBA16F||pt===s.RGBA32F)&&t.get("EXT_color_buffer_float"),pt}function z(B,T){let et;return B?T===null||T===$i||T===hl?et=s.DEPTH24_STENCIL8:T===Zi?et=s.DEPTH32F_STENCIL8:T===dl&&(et=s.DEPTH24_STENCIL8,se("DepthTexture: 16 bit depth attachment is not supported with stencil. Using 24-bit attachment.")):T===null||T===$i||T===hl?et=s.DEPTH_COMPONENT24:T===Zi?et=s.DEPTH_COMPONENT32F:T===dl&&(et=s.DEPTH_COMPONENT16),et}function D(B,T){return S(B)===!0||B.isFramebufferTexture&&B.minFilter!==Pn&&B.minFilter!==wn?Math.log2(Math.max(T.width,T.height))+1:B.mipmaps!==void 0&&B.mipmaps.length>0?B.mipmaps.length:B.isCompressedTexture&&Array.isArray(B.image)?T.mipmaps.length:1}function U(B){const T=B.target;T.removeEventListener("dispose",U),N(T),T.isVideoTexture&&_.delete(T),T.isHTMLTexture&&v.delete(T)}function b(B){const T=B.target;T.removeEventListener("dispose",b),F(T)}function N(B){const T=r.get(B);if(T.__webglInit===void 0)return;const et=B.source,ut=x.get(et);if(ut){const mt=ut[T.__cacheKey];mt.usedTimes--,mt.usedTimes===0&&G(B),Object.keys(ut).length===0&&x.delete(et)}r.remove(B)}function G(B){const T=r.get(B);s.deleteTexture(T.__webglTexture);const et=B.source,ut=x.get(et);delete ut[T.__cacheKey],f.memory.textures--}function F(B){const T=r.get(B);if(B.depthTexture&&(B.depthTexture.dispose(),r.remove(B.depthTexture)),B.isWebGLCubeRenderTarget)for(let ut=0;ut<6;ut++){if(Array.isArray(T.__webglFramebuffer[ut]))for(let mt=0;mt<T.__webglFramebuffer[ut].length;mt++)s.deleteFramebuffer(T.__webglFramebuffer[ut][mt]);else s.deleteFramebuffer(T.__webglFramebuffer[ut]);T.__webglDepthbuffer&&s.deleteRenderbuffer(T.__webglDepthbuffer[ut])}else{if(Array.isArray(T.__webglFramebuffer))for(let ut=0;ut<T.__webglFramebuffer.length;ut++)s.deleteFramebuffer(T.__webglFramebuffer[ut]);else s.deleteFramebuffer(T.__webglFramebuffer);if(T.__webglDepthbuffer&&s.deleteRenderbuffer(T.__webglDepthbuffer),T.__webglMultisampledFramebuffer&&s.deleteFramebuffer(T.__webglMultisampledFramebuffer),T.__webglColorRenderbuffer)for(let ut=0;ut<T.__webglColorRenderbuffer.length;ut++)T.__webglColorRenderbuffer[ut]&&s.deleteRenderbuffer(T.__webglColorRenderbuffer[ut]);T.__webglDepthRenderbuffer&&s.deleteRenderbuffer(T.__webglDepthRenderbuffer)}const et=B.textures;for(let ut=0,mt=et.length;ut<mt;ut++){const Rt=r.get(et[ut]);Rt.__webglTexture&&(s.deleteTexture(Rt.__webglTexture),f.memory.textures--),r.remove(et[ut])}r.remove(B)}let k=0;function K(){k=0}function tt(){return k}function $(B){k=B}function I(){const B=k;return B>=l.maxTextures&&se("WebGLTextures: Trying to use "+B+" texture units while this GPU supports only "+l.maxTextures),k+=1,B}function X(B){const T=[];return T.push(B.wrapS),T.push(B.wrapT),T.push(B.wrapR||0),T.push(B.magFilter),T.push(B.minFilter),T.push(B.anisotropy),T.push(B.internalFormat),T.push(B.format),T.push(B.type),T.push(B.generateMipmaps),T.push(B.premultiplyAlpha),T.push(B.flipY),T.push(B.unpackAlignment),T.push(B.colorSpace),T.join()}function at(B,T){const et=r.get(B);if(B.isVideoTexture&&j(B),B.isRenderTargetTexture===!1&&B.isExternalTexture!==!0&&B.version>0&&et.__version!==B.version){const ut=B.image;if(ut===null)se("WebGLRenderer: Texture marked for update but no image data found.");else if(ut.complete===!1)se("WebGLRenderer: Texture marked for update but image is incomplete");else{bt(et,B,T);return}}else B.isExternalTexture&&(et.__webglTexture=B.sourceTexture?B.sourceTexture:null);i.bindTexture(s.TEXTURE_2D,et.__webglTexture,s.TEXTURE0+T)}function _t(B,T){const et=r.get(B);if(B.isRenderTargetTexture===!1&&B.version>0&&et.__version!==B.version){bt(et,B,T);return}else B.isExternalTexture&&(et.__webglTexture=B.sourceTexture?B.sourceTexture:null);i.bindTexture(s.TEXTURE_2D_ARRAY,et.__webglTexture,s.TEXTURE0+T)}function vt(B,T){const et=r.get(B);if(B.isRenderTargetTexture===!1&&B.version>0&&et.__version!==B.version){bt(et,B,T);return}i.bindTexture(s.TEXTURE_3D,et.__webglTexture,s.TEXTURE0+T)}function L(B,T){const et=r.get(B);if(B.isCubeDepthTexture!==!0&&B.version>0&&et.__version!==B.version){Ht(et,B,T);return}i.bindTexture(s.TEXTURE_CUBE_MAP,et.__webglTexture,s.TEXTURE0+T)}const Y={[jh]:s.REPEAT,[Aa]:s.CLAMP_TO_EDGE,[Kh]:s.MIRRORED_REPEAT},yt={[Pn]:s.NEAREST,[cE]:s.NEAREST_MIPMAP_NEAREST,[Dc]:s.NEAREST_MIPMAP_LINEAR,[wn]:s.LINEAR,[Id]:s.LINEAR_MIPMAP_NEAREST,[Xr]:s.LINEAR_MIPMAP_LINEAR},At={[dE]:s.NEVER,[_E]:s.ALWAYS,[hE]:s.LESS,[kp]:s.LEQUAL,[pE]:s.EQUAL,[Xp]:s.GEQUAL,[mE]:s.GREATER,[gE]:s.NOTEQUAL};function Dt(B,T){if(T.type===Zi&&t.has("OES_texture_float_linear")===!1&&(T.magFilter===wn||T.magFilter===Id||T.magFilter===Dc||T.magFilter===Xr||T.minFilter===wn||T.minFilter===Id||T.minFilter===Dc||T.minFilter===Xr)&&se("WebGLRenderer: Unable to use linear filtering with floating point textures. OES_texture_float_linear not supported on this device."),s.texParameteri(B,s.TEXTURE_WRAP_S,Y[T.wrapS]),s.texParameteri(B,s.TEXTURE_WRAP_T,Y[T.wrapT]),(B===s.TEXTURE_3D||B===s.TEXTURE_2D_ARRAY)&&s.texParameteri(B,s.TEXTURE_WRAP_R,Y[T.wrapR]),s.texParameteri(B,s.TEXTURE_MAG_FILTER,yt[T.magFilter]),s.texParameteri(B,s.TEXTURE_MIN_FILTER,yt[T.minFilter]),T.compareFunction&&(s.texParameteri(B,s.TEXTURE_COMPARE_MODE,s.COMPARE_REF_TO_TEXTURE),s.texParameteri(B,s.TEXTURE_COMPARE_FUNC,At[T.compareFunction])),t.has("EXT_texture_filter_anisotropic")===!0){if(T.magFilter===Pn||T.minFilter!==Dc&&T.minFilter!==Xr||T.type===Zi&&t.has("OES_texture_float_linear")===!1)return;if(T.anisotropy>1||r.get(T).__currentAnisotropy){const et=t.get("EXT_texture_filter_anisotropic");s.texParameterf(B,et.TEXTURE_MAX_ANISOTROPY_EXT,Math.min(T.anisotropy,l.getMaxAnisotropy())),r.get(T).__currentAnisotropy=T.anisotropy}}}function it(B,T){let et=!1;B.__webglInit===void 0&&(B.__webglInit=!0,T.addEventListener("dispose",U));const ut=T.source;let mt=x.get(ut);mt===void 0&&(mt={},x.set(ut,mt));const Rt=X(T);if(Rt!==B.__cacheKey){mt[Rt]===void 0&&(mt[Rt]={texture:s.createTexture(),usedTimes:0},f.memory.textures++,et=!0),mt[Rt].usedTimes++;const Ut=mt[B.__cacheKey];Ut!==void 0&&(mt[B.__cacheKey].usedTimes--,Ut.usedTimes===0&&G(T)),B.__cacheKey=Rt,B.__webglTexture=mt[Rt].texture}return et}function nt(B,T,et){return Math.floor(Math.floor(B/et)/T)}function ct(B,T,et,ut){const Rt=B.updateRanges;if(Rt.length===0)i.texSubImage2D(s.TEXTURE_2D,0,0,0,T.width,T.height,et,ut,T.data);else{Rt.sort((Gt,Pt)=>Gt.start-Pt.start);let Ut=0;for(let Gt=1;Gt<Rt.length;Gt++){const Pt=Rt[Ut],Lt=Rt[Gt],Jt=Pt.start+Pt.count,$t=nt(Lt.start,T.width,4),oe=nt(Pt.start,T.width,4);Lt.start<=Jt+1&&$t===oe&&nt(Lt.start+Lt.count-1,T.width,4)===$t?Pt.count=Math.max(Pt.count,Lt.start+Lt.count-Pt.start):(++Ut,Rt[Ut]=Lt)}Rt.length=Ut+1;const pt=i.getParameter(s.UNPACK_ROW_LENGTH),gt=i.getParameter(s.UNPACK_SKIP_PIXELS),wt=i.getParameter(s.UNPACK_SKIP_ROWS);i.pixelStorei(s.UNPACK_ROW_LENGTH,T.width);for(let Gt=0,Pt=Rt.length;Gt<Pt;Gt++){const Lt=Rt[Gt],Jt=Math.floor(Lt.start/4),$t=Math.ceil(Lt.count/4),oe=Jt%T.width,W=Math.floor(Jt/T.width),Ct=$t,St=1;i.pixelStorei(s.UNPACK_SKIP_PIXELS,oe),i.pixelStorei(s.UNPACK_SKIP_ROWS,W),i.texSubImage2D(s.TEXTURE_2D,0,oe,W,Ct,St,et,ut,T.data)}B.clearUpdateRanges(),i.pixelStorei(s.UNPACK_ROW_LENGTH,pt),i.pixelStorei(s.UNPACK_SKIP_PIXELS,gt),i.pixelStorei(s.UNPACK_SKIP_ROWS,wt)}}function bt(B,T,et){let ut=s.TEXTURE_2D;(T.isDataArrayTexture||T.isCompressedArrayTexture)&&(ut=s.TEXTURE_2D_ARRAY),T.isData3DTexture&&(ut=s.TEXTURE_3D);const mt=it(B,T),Rt=T.source;i.bindTexture(ut,B.__webglTexture,s.TEXTURE0+et);const Ut=r.get(Rt);if(Rt.version!==Ut.__version||mt===!0){if(i.activeTexture(s.TEXTURE0+et),(typeof ImageBitmap<"u"&&T.image instanceof ImageBitmap)===!1){const St=Te.getPrimaries(Te.workingColorSpace),Nt=T.colorSpace===dr?null:Te.getPrimaries(T.colorSpace),Ft=T.colorSpace===dr||St===Nt?s.NONE:s.BROWSER_DEFAULT_WEBGL;i.pixelStorei(s.UNPACK_FLIP_Y_WEBGL,T.flipY),i.pixelStorei(s.UNPACK_PREMULTIPLY_ALPHA_WEBGL,T.premultiplyAlpha),i.pixelStorei(s.UNPACK_COLORSPACE_CONVERSION_WEBGL,Ft)}i.pixelStorei(s.UNPACK_ALIGNMENT,T.unpackAlignment);let gt=E(T.image,!1,l.maxTextureSize);gt=ke(T,gt);const wt=c.convert(T.format,T.colorSpace),Gt=c.convert(T.type);let Pt=C(T.internalFormat,wt,Gt,T.normalized,T.colorSpace,T.isVideoTexture);Dt(ut,T);let Lt;const Jt=T.mipmaps,$t=T.isVideoTexture!==!0,oe=Ut.__version===void 0||mt===!0,W=Rt.dataReady,Ct=D(T,gt);if(T.isDepthTexture)Pt=z(T.format===qr,T.type),oe&&($t?i.texStorage2D(s.TEXTURE_2D,1,Pt,gt.width,gt.height):i.texImage2D(s.TEXTURE_2D,0,Pt,gt.width,gt.height,0,wt,Gt,null));else if(T.isDataTexture)if(Jt.length>0){$t&&oe&&i.texStorage2D(s.TEXTURE_2D,Ct,Pt,Jt[0].width,Jt[0].height);for(let St=0,Nt=Jt.length;St<Nt;St++)Lt=Jt[St],$t?W&&i.texSubImage2D(s.TEXTURE_2D,St,0,0,Lt.width,Lt.height,wt,Gt,Lt.data):i.texImage2D(s.TEXTURE_2D,St,Pt,Lt.width,Lt.height,0,wt,Gt,Lt.data);T.generateMipmaps=!1}else $t?(oe&&i.texStorage2D(s.TEXTURE_2D,Ct,Pt,gt.width,gt.height),W&&ct(T,gt,wt,Gt)):i.texImage2D(s.TEXTURE_2D,0,Pt,gt.width,gt.height,0,wt,Gt,gt.data);else if(T.isCompressedTexture)if(T.isCompressedArrayTexture){$t&&oe&&i.texStorage3D(s.TEXTURE_2D_ARRAY,Ct,Pt,Jt[0].width,Jt[0].height,gt.depth);for(let St=0,Nt=Jt.length;St<Nt;St++)if(Lt=Jt[St],T.format!==Pi)if(wt!==null)if($t){if(W)if(T.layerUpdates.size>0){const Ft=Hv(Lt.width,Lt.height,T.format,T.type);for(const Tt of T.layerUpdates){const jt=Lt.data.subarray(Tt*Ft/Lt.data.BYTES_PER_ELEMENT,(Tt+1)*Ft/Lt.data.BYTES_PER_ELEMENT);i.compressedTexSubImage3D(s.TEXTURE_2D_ARRAY,St,0,0,Tt,Lt.width,Lt.height,1,wt,jt)}T.clearLayerUpdates()}else i.compressedTexSubImage3D(s.TEXTURE_2D_ARRAY,St,0,0,0,Lt.width,Lt.height,gt.depth,wt,Lt.data)}else i.compressedTexImage3D(s.TEXTURE_2D_ARRAY,St,Pt,Lt.width,Lt.height,gt.depth,0,Lt.data,0,0);else se("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()");else $t?W&&i.texSubImage3D(s.TEXTURE_2D_ARRAY,St,0,0,0,Lt.width,Lt.height,gt.depth,wt,Gt,Lt.data):i.texImage3D(s.TEXTURE_2D_ARRAY,St,Pt,Lt.width,Lt.height,gt.depth,0,wt,Gt,Lt.data)}else{$t&&oe&&i.texStorage2D(s.TEXTURE_2D,Ct,Pt,Jt[0].width,Jt[0].height);for(let St=0,Nt=Jt.length;St<Nt;St++)Lt=Jt[St],T.format!==Pi?wt!==null?$t?W&&i.compressedTexSubImage2D(s.TEXTURE_2D,St,0,0,Lt.width,Lt.height,wt,Lt.data):i.compressedTexImage2D(s.TEXTURE_2D,St,Pt,Lt.width,Lt.height,0,Lt.data):se("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()"):$t?W&&i.texSubImage2D(s.TEXTURE_2D,St,0,0,Lt.width,Lt.height,wt,Gt,Lt.data):i.texImage2D(s.TEXTURE_2D,St,Pt,Lt.width,Lt.height,0,wt,Gt,Lt.data)}else if(T.isDataArrayTexture)if($t){if(oe&&i.texStorage3D(s.TEXTURE_2D_ARRAY,Ct,Pt,gt.width,gt.height,gt.depth),W)if(T.layerUpdates.size>0){const St=Hv(gt.width,gt.height,T.format,T.type);for(const Nt of T.layerUpdates){const Ft=gt.data.subarray(Nt*St/gt.data.BYTES_PER_ELEMENT,(Nt+1)*St/gt.data.BYTES_PER_ELEMENT);i.texSubImage3D(s.TEXTURE_2D_ARRAY,0,0,0,Nt,gt.width,gt.height,1,wt,Gt,Ft)}T.clearLayerUpdates()}else i.texSubImage3D(s.TEXTURE_2D_ARRAY,0,0,0,0,gt.width,gt.height,gt.depth,wt,Gt,gt.data)}else i.texImage3D(s.TEXTURE_2D_ARRAY,0,Pt,gt.width,gt.height,gt.depth,0,wt,Gt,gt.data);else if(T.isData3DTexture)$t?(oe&&i.texStorage3D(s.TEXTURE_3D,Ct,Pt,gt.width,gt.height,gt.depth),W&&i.texSubImage3D(s.TEXTURE_3D,0,0,0,0,gt.width,gt.height,gt.depth,wt,Gt,gt.data)):i.texImage3D(s.TEXTURE_3D,0,Pt,gt.width,gt.height,gt.depth,0,wt,Gt,gt.data);else if(T.isFramebufferTexture){if(oe)if($t)i.texStorage2D(s.TEXTURE_2D,Ct,Pt,gt.width,gt.height);else{let St=gt.width,Nt=gt.height;for(let Ft=0;Ft<Ct;Ft++)i.texImage2D(s.TEXTURE_2D,Ft,Pt,St,Nt,0,wt,Gt,null),St>>=1,Nt>>=1}}else if(T.isHTMLTexture){if("texElementImage2D"in s){const St=s.canvas;if(St.hasAttribute("layoutsubtree")||St.setAttribute("layoutsubtree","true"),gt.parentNode!==St){St.appendChild(gt),v.add(T),St.onpaint=Nt=>{const Ft=Nt.changedElements;for(const Tt of v)Ft.includes(Tt.image)&&(Tt.needsUpdate=!0)},St.requestPaint();return}if(s.texElementImage2D.length===3)s.texElementImage2D(s.TEXTURE_2D,s.RGBA8,gt);else{const Ft=s.RGBA,Tt=s.RGBA,jt=s.UNSIGNED_BYTE;s.texElementImage2D(s.TEXTURE_2D,0,Ft,Tt,jt,gt)}s.texParameteri(s.TEXTURE_2D,s.TEXTURE_MIN_FILTER,s.LINEAR),s.texParameteri(s.TEXTURE_2D,s.TEXTURE_WRAP_S,s.CLAMP_TO_EDGE),s.texParameteri(s.TEXTURE_2D,s.TEXTURE_WRAP_T,s.CLAMP_TO_EDGE)}}else if(Jt.length>0){if($t&&oe){const St=Le(Jt[0]);i.texStorage2D(s.TEXTURE_2D,Ct,Pt,St.width,St.height)}for(let St=0,Nt=Jt.length;St<Nt;St++)Lt=Jt[St],$t?W&&i.texSubImage2D(s.TEXTURE_2D,St,0,0,wt,Gt,Lt):i.texImage2D(s.TEXTURE_2D,St,Pt,wt,Gt,Lt);T.generateMipmaps=!1}else if($t){if(oe){const St=Le(gt);i.texStorage2D(s.TEXTURE_2D,Ct,Pt,St.width,St.height)}W&&i.texSubImage2D(s.TEXTURE_2D,0,0,0,wt,Gt,gt)}else i.texImage2D(s.TEXTURE_2D,0,Pt,wt,Gt,gt);S(T)&&P(ut),Ut.__version=Rt.version,T.onUpdate&&T.onUpdate(T)}B.__version=T.version}function Ht(B,T,et){if(T.image.length!==6)return;const ut=it(B,T),mt=T.source;i.bindTexture(s.TEXTURE_CUBE_MAP,B.__webglTexture,s.TEXTURE0+et);const Rt=r.get(mt);if(mt.version!==Rt.__version||ut===!0){i.activeTexture(s.TEXTURE0+et);const Ut=Te.getPrimaries(Te.workingColorSpace),pt=T.colorSpace===dr?null:Te.getPrimaries(T.colorSpace),gt=T.colorSpace===dr||Ut===pt?s.NONE:s.BROWSER_DEFAULT_WEBGL;i.pixelStorei(s.UNPACK_FLIP_Y_WEBGL,T.flipY),i.pixelStorei(s.UNPACK_PREMULTIPLY_ALPHA_WEBGL,T.premultiplyAlpha),i.pixelStorei(s.UNPACK_ALIGNMENT,T.unpackAlignment),i.pixelStorei(s.UNPACK_COLORSPACE_CONVERSION_WEBGL,gt);const wt=T.isCompressedTexture||T.image[0].isCompressedTexture,Gt=T.image[0]&&T.image[0].isDataTexture,Pt=[];for(let Tt=0;Tt<6;Tt++)!wt&&!Gt?Pt[Tt]=E(T.image[Tt],!0,l.maxCubemapSize):Pt[Tt]=Gt?T.image[Tt].image:T.image[Tt],Pt[Tt]=ke(T,Pt[Tt]);const Lt=Pt[0],Jt=c.convert(T.format,T.colorSpace),$t=c.convert(T.type),oe=C(T.internalFormat,Jt,$t,T.normalized,T.colorSpace),W=T.isVideoTexture!==!0,Ct=Rt.__version===void 0||ut===!0,St=mt.dataReady;let Nt=D(T,Lt);Dt(s.TEXTURE_CUBE_MAP,T);let Ft;if(wt){W&&Ct&&i.texStorage2D(s.TEXTURE_CUBE_MAP,Nt,oe,Lt.width,Lt.height);for(let Tt=0;Tt<6;Tt++){Ft=Pt[Tt].mipmaps;for(let jt=0;jt<Ft.length;jt++){const qt=Ft[jt];T.format!==Pi?Jt!==null?W?St&&i.compressedTexSubImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,0,0,qt.width,qt.height,Jt,qt.data):i.compressedTexImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,oe,qt.width,qt.height,0,qt.data):se("WebGLRenderer: Attempt to load unsupported compressed texture format in .setTextureCube()"):W?St&&i.texSubImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,0,0,qt.width,qt.height,Jt,$t,qt.data):i.texImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt,oe,qt.width,qt.height,0,Jt,$t,qt.data)}}}else{if(Ft=T.mipmaps,W&&Ct){Ft.length>0&&Nt++;const Tt=Le(Pt[0]);i.texStorage2D(s.TEXTURE_CUBE_MAP,Nt,oe,Tt.width,Tt.height)}for(let Tt=0;Tt<6;Tt++)if(Gt){W?St&&i.texSubImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,0,0,Pt[Tt].width,Pt[Tt].height,Jt,$t,Pt[Tt].data):i.texImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,oe,Pt[Tt].width,Pt[Tt].height,0,Jt,$t,Pt[Tt].data);for(let jt=0;jt<Ft.length;jt++){const tn=Ft[jt].image[Tt].image;W?St&&i.texSubImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,0,0,tn.width,tn.height,Jt,$t,tn.data):i.texImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,oe,tn.width,tn.height,0,Jt,$t,tn.data)}}else{W?St&&i.texSubImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,0,0,Jt,$t,Pt[Tt]):i.texImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,0,oe,Jt,$t,Pt[Tt]);for(let jt=0;jt<Ft.length;jt++){const qt=Ft[jt];W?St&&i.texSubImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,0,0,Jt,$t,qt.image[Tt]):i.texImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Tt,jt+1,oe,Jt,$t,qt.image[Tt])}}}S(T)&&P(s.TEXTURE_CUBE_MAP),Rt.__version=mt.version,T.onUpdate&&T.onUpdate(T)}B.__version=T.version}function Bt(B,T,et,ut,mt,Rt){const Ut=c.convert(et.format,et.colorSpace),pt=c.convert(et.type),gt=C(et.internalFormat,Ut,pt,et.normalized,et.colorSpace),wt=r.get(T),Gt=r.get(et);if(Gt.__renderTarget=T,!wt.__hasExternalTextures){const Pt=Math.max(1,T.width>>Rt),Lt=Math.max(1,T.height>>Rt);mt===s.TEXTURE_3D||mt===s.TEXTURE_2D_ARRAY?i.texImage3D(mt,Rt,gt,Pt,Lt,T.depth,0,Ut,pt,null):i.texImage2D(mt,Rt,gt,Pt,Lt,0,Ut,pt,null)}i.bindFramebuffer(s.FRAMEBUFFER,B),on(T)?h.framebufferTexture2DMultisampleEXT(s.FRAMEBUFFER,ut,mt,Gt.__webglTexture,0,Ue(T)):(mt===s.TEXTURE_2D||mt>=s.TEXTURE_CUBE_MAP_POSITIVE_X&&mt<=s.TEXTURE_CUBE_MAP_NEGATIVE_Z)&&s.framebufferTexture2D(s.FRAMEBUFFER,ut,mt,Gt.__webglTexture,Rt),i.bindFramebuffer(s.FRAMEBUFFER,null)}function he(B,T,et){if(s.bindRenderbuffer(s.RENDERBUFFER,B),T.depthBuffer){const ut=T.depthTexture,mt=ut&&ut.isDepthTexture?ut.type:null,Rt=z(T.stencilBuffer,mt),Ut=T.stencilBuffer?s.DEPTH_STENCIL_ATTACHMENT:s.DEPTH_ATTACHMENT;on(T)?h.renderbufferStorageMultisampleEXT(s.RENDERBUFFER,Ue(T),Rt,T.width,T.height):et?s.renderbufferStorageMultisample(s.RENDERBUFFER,Ue(T),Rt,T.width,T.height):s.renderbufferStorage(s.RENDERBUFFER,Rt,T.width,T.height),s.framebufferRenderbuffer(s.FRAMEBUFFER,Ut,s.RENDERBUFFER,B)}else{const ut=T.textures;for(let mt=0;mt<ut.length;mt++){const Rt=ut[mt],Ut=c.convert(Rt.format,Rt.colorSpace),pt=c.convert(Rt.type),gt=C(Rt.internalFormat,Ut,pt,Rt.normalized,Rt.colorSpace);on(T)?h.renderbufferStorageMultisampleEXT(s.RENDERBUFFER,Ue(T),gt,T.width,T.height):et?s.renderbufferStorageMultisample(s.RENDERBUFFER,Ue(T),gt,T.width,T.height):s.renderbufferStorage(s.RENDERBUFFER,gt,T.width,T.height)}}s.bindRenderbuffer(s.RENDERBUFFER,null)}function ne(B,T,et){const ut=T.isWebGLCubeRenderTarget===!0;if(i.bindFramebuffer(s.FRAMEBUFFER,B),!(T.depthTexture&&T.depthTexture.isDepthTexture))throw new Error("THREE.WebGLTextures: renderTarget.depthTexture must be an instance of THREE.DepthTexture.");const mt=r.get(T.depthTexture);if(mt.__renderTarget=T,(!mt.__webglTexture||T.depthTexture.image.width!==T.width||T.depthTexture.image.height!==T.height)&&(T.depthTexture.image.width=T.width,T.depthTexture.image.height=T.height,T.depthTexture.needsUpdate=!0),ut){if(mt.__webglInit===void 0&&(mt.__webglInit=!0,T.depthTexture.addEventListener("dispose",U)),mt.__webglTexture===void 0){mt.__webglTexture=s.createTexture(),i.bindTexture(s.TEXTURE_CUBE_MAP,mt.__webglTexture),Dt(s.TEXTURE_CUBE_MAP,T.depthTexture);const wt=c.convert(T.depthTexture.format),Gt=c.convert(T.depthTexture.type);let Pt;T.depthTexture.format===Na?Pt=s.DEPTH_COMPONENT24:T.depthTexture.format===qr&&(Pt=s.DEPTH24_STENCIL8);for(let Lt=0;Lt<6;Lt++)s.texImage2D(s.TEXTURE_CUBE_MAP_POSITIVE_X+Lt,0,Pt,T.width,T.height,0,wt,Gt,null)}}else at(T.depthTexture,0);const Rt=mt.__webglTexture,Ut=Ue(T),pt=ut?s.TEXTURE_CUBE_MAP_POSITIVE_X+et:s.TEXTURE_2D,gt=T.depthTexture.format===qr?s.DEPTH_STENCIL_ATTACHMENT:s.DEPTH_ATTACHMENT;if(T.depthTexture.format===Na)on(T)?h.framebufferTexture2DMultisampleEXT(s.FRAMEBUFFER,gt,pt,Rt,0,Ut):s.framebufferTexture2D(s.FRAMEBUFFER,gt,pt,Rt,0);else if(T.depthTexture.format===qr)on(T)?h.framebufferTexture2DMultisampleEXT(s.FRAMEBUFFER,gt,pt,Rt,0,Ut):s.framebufferTexture2D(s.FRAMEBUFFER,gt,pt,Rt,0);else throw new Error("THREE.WebGLTextures: Unknown depthTexture format.")}function ie(B){const T=r.get(B),et=B.isWebGLCubeRenderTarget===!0;if(T.__boundDepthTexture!==B.depthTexture){const ut=B.depthTexture;if(T.__depthDisposeCallback&&T.__depthDisposeCallback(),ut){const mt=()=>{delete T.__boundDepthTexture,delete T.__depthDisposeCallback,ut.removeEventListener("dispose",mt)};ut.addEventListener("dispose",mt),T.__depthDisposeCallback=mt}T.__boundDepthTexture=ut}if(B.depthTexture&&!T.__autoAllocateDepthBuffer)if(et)for(let ut=0;ut<6;ut++)ne(T.__webglFramebuffer[ut],B,ut);else{const ut=B.texture.mipmaps;ut&&ut.length>0?ne(T.__webglFramebuffer[0],B,0):ne(T.__webglFramebuffer,B,0)}else if(et){T.__webglDepthbuffer=[];for(let ut=0;ut<6;ut++)if(i.bindFramebuffer(s.FRAMEBUFFER,T.__webglFramebuffer[ut]),T.__webglDepthbuffer[ut]===void 0)T.__webglDepthbuffer[ut]=s.createRenderbuffer(),he(T.__webglDepthbuffer[ut],B,!1);else{const mt=B.stencilBuffer?s.DEPTH_STENCIL_ATTACHMENT:s.DEPTH_ATTACHMENT,Rt=T.__webglDepthbuffer[ut];s.bindRenderbuffer(s.RENDERBUFFER,Rt),s.framebufferRenderbuffer(s.FRAMEBUFFER,mt,s.RENDERBUFFER,Rt)}}else{const ut=B.texture.mipmaps;if(ut&&ut.length>0?i.bindFramebuffer(s.FRAMEBUFFER,T.__webglFramebuffer[0]):i.bindFramebuffer(s.FRAMEBUFFER,T.__webglFramebuffer),T.__webglDepthbuffer===void 0)T.__webglDepthbuffer=s.createRenderbuffer(),he(T.__webglDepthbuffer,B,!1);else{const mt=B.stencilBuffer?s.DEPTH_STENCIL_ATTACHMENT:s.DEPTH_ATTACHMENT,Rt=T.__webglDepthbuffer;s.bindRenderbuffer(s.RENDERBUFFER,Rt),s.framebufferRenderbuffer(s.FRAMEBUFFER,mt,s.RENDERBUFFER,Rt)}}i.bindFramebuffer(s.FRAMEBUFFER,null)}function Xt(B,T,et){const ut=r.get(B);T!==void 0&&Bt(ut.__webglFramebuffer,B,B.texture,s.COLOR_ATTACHMENT0,s.TEXTURE_2D,0),et!==void 0&&ie(B)}function ae(B){const T=B.texture,et=r.get(B),ut=r.get(T);B.addEventListener("dispose",b);const mt=B.textures,Rt=B.isWebGLCubeRenderTarget===!0,Ut=mt.length>1;if(Ut||(ut.__webglTexture===void 0&&(ut.__webglTexture=s.createTexture()),ut.__version=T.version,f.memory.textures++),Rt){et.__webglFramebuffer=[];for(let pt=0;pt<6;pt++)if(T.mipmaps&&T.mipmaps.length>0){et.__webglFramebuffer[pt]=[];for(let gt=0;gt<T.mipmaps.length;gt++)et.__webglFramebuffer[pt][gt]=s.createFramebuffer()}else et.__webglFramebuffer[pt]=s.createFramebuffer()}else{if(T.mipmaps&&T.mipmaps.length>0){et.__webglFramebuffer=[];for(let pt=0;pt<T.mipmaps.length;pt++)et.__webglFramebuffer[pt]=s.createFramebuffer()}else et.__webglFramebuffer=s.createFramebuffer();if(Ut)for(let pt=0,gt=mt.length;pt<gt;pt++){const wt=r.get(mt[pt]);wt.__webglTexture===void 0&&(wt.__webglTexture=s.createTexture(),f.memory.textures++)}if(B.samples>0&&on(B)===!1){et.__webglMultisampledFramebuffer=s.createFramebuffer(),et.__webglColorRenderbuffer=[],i.bindFramebuffer(s.FRAMEBUFFER,et.__webglMultisampledFramebuffer);for(let pt=0;pt<mt.length;pt++){const gt=mt[pt];et.__webglColorRenderbuffer[pt]=s.createRenderbuffer(),s.bindRenderbuffer(s.RENDERBUFFER,et.__webglColorRenderbuffer[pt]);const wt=c.convert(gt.format,gt.colorSpace),Gt=c.convert(gt.type),Pt=C(gt.internalFormat,wt,Gt,gt.normalized,gt.colorSpace,B.isXRRenderTarget===!0),Lt=Ue(B);s.renderbufferStorageMultisample(s.RENDERBUFFER,Lt,Pt,B.width,B.height),s.framebufferRenderbuffer(s.FRAMEBUFFER,s.COLOR_ATTACHMENT0+pt,s.RENDERBUFFER,et.__webglColorRenderbuffer[pt])}s.bindRenderbuffer(s.RENDERBUFFER,null),B.depthBuffer&&(et.__webglDepthRenderbuffer=s.createRenderbuffer(),he(et.__webglDepthRenderbuffer,B,!0)),i.bindFramebuffer(s.FRAMEBUFFER,null)}}if(Rt){i.bindTexture(s.TEXTURE_CUBE_MAP,ut.__webglTexture),Dt(s.TEXTURE_CUBE_MAP,T);for(let pt=0;pt<6;pt++)if(T.mipmaps&&T.mipmaps.length>0)for(let gt=0;gt<T.mipmaps.length;gt++)Bt(et.__webglFramebuffer[pt][gt],B,T,s.COLOR_ATTACHMENT0,s.TEXTURE_CUBE_MAP_POSITIVE_X+pt,gt);else Bt(et.__webglFramebuffer[pt],B,T,s.COLOR_ATTACHMENT0,s.TEXTURE_CUBE_MAP_POSITIVE_X+pt,0);S(T)&&P(s.TEXTURE_CUBE_MAP),i.unbindTexture()}else if(Ut){for(let pt=0,gt=mt.length;pt<gt;pt++){const wt=mt[pt],Gt=r.get(wt);let Pt=s.TEXTURE_2D;(B.isWebGL3DRenderTarget||B.isWebGLArrayRenderTarget)&&(Pt=B.isWebGL3DRenderTarget?s.TEXTURE_3D:s.TEXTURE_2D_ARRAY),i.bindTexture(Pt,Gt.__webglTexture),Dt(Pt,wt),Bt(et.__webglFramebuffer,B,wt,s.COLOR_ATTACHMENT0+pt,Pt,0),S(wt)&&P(Pt)}i.unbindTexture()}else{let pt=s.TEXTURE_2D;if((B.isWebGL3DRenderTarget||B.isWebGLArrayRenderTarget)&&(pt=B.isWebGL3DRenderTarget?s.TEXTURE_3D:s.TEXTURE_2D_ARRAY),i.bindTexture(pt,ut.__webglTexture),Dt(pt,T),T.mipmaps&&T.mipmaps.length>0)for(let gt=0;gt<T.mipmaps.length;gt++)Bt(et.__webglFramebuffer[gt],B,T,s.COLOR_ATTACHMENT0,pt,gt);else Bt(et.__webglFramebuffer,B,T,s.COLOR_ATTACHMENT0,pt,0);S(T)&&P(pt),i.unbindTexture()}B.depthBuffer&&ie(B)}function ge(B){const T=B.textures;for(let et=0,ut=T.length;et<ut;et++){const mt=T[et];if(S(mt)){const Rt=O(B),Ut=r.get(mt).__webglTexture;i.bindTexture(Rt,Ut),P(Rt),i.unbindTexture()}}}const _e=[],Ce=[];function Ke(B){if(B.samples>0){if(on(B)===!1){const T=B.textures,et=B.width,ut=B.height;let mt=s.COLOR_BUFFER_BIT;const Rt=B.stencilBuffer?s.DEPTH_STENCIL_ATTACHMENT:s.DEPTH_ATTACHMENT,Ut=r.get(B),pt=T.length>1;if(pt)for(let wt=0;wt<T.length;wt++)i.bindFramebuffer(s.FRAMEBUFFER,Ut.__webglMultisampledFramebuffer),s.framebufferRenderbuffer(s.FRAMEBUFFER,s.COLOR_ATTACHMENT0+wt,s.RENDERBUFFER,null),i.bindFramebuffer(s.FRAMEBUFFER,Ut.__webglFramebuffer),s.framebufferTexture2D(s.DRAW_FRAMEBUFFER,s.COLOR_ATTACHMENT0+wt,s.TEXTURE_2D,null,0);i.bindFramebuffer(s.READ_FRAMEBUFFER,Ut.__webglMultisampledFramebuffer);const gt=B.texture.mipmaps;gt&&gt.length>0?i.bindFramebuffer(s.DRAW_FRAMEBUFFER,Ut.__webglFramebuffer[0]):i.bindFramebuffer(s.DRAW_FRAMEBUFFER,Ut.__webglFramebuffer);for(let wt=0;wt<T.length;wt++){if(B.resolveDepthBuffer&&(B.depthBuffer&&(mt|=s.DEPTH_BUFFER_BIT),B.stencilBuffer&&B.resolveStencilBuffer&&(mt|=s.STENCIL_BUFFER_BIT)),pt){s.framebufferRenderbuffer(s.READ_FRAMEBUFFER,s.COLOR_ATTACHMENT0,s.RENDERBUFFER,Ut.__webglColorRenderbuffer[wt]);const Gt=r.get(T[wt]).__webglTexture;s.framebufferTexture2D(s.DRAW_FRAMEBUFFER,s.COLOR_ATTACHMENT0,s.TEXTURE_2D,Gt,0)}s.blitFramebuffer(0,0,et,ut,0,0,et,ut,mt,s.NEAREST),m===!0&&(_e.length=0,Ce.length=0,_e.push(s.COLOR_ATTACHMENT0+wt),B.depthBuffer&&B.resolveDepthBuffer===!1&&(_e.push(Rt),Ce.push(Rt),s.invalidateFramebuffer(s.DRAW_FRAMEBUFFER,Ce)),s.invalidateFramebuffer(s.READ_FRAMEBUFFER,_e))}if(i.bindFramebuffer(s.READ_FRAMEBUFFER,null),i.bindFramebuffer(s.DRAW_FRAMEBUFFER,null),pt)for(let wt=0;wt<T.length;wt++){i.bindFramebuffer(s.FRAMEBUFFER,Ut.__webglMultisampledFramebuffer),s.framebufferRenderbuffer(s.FRAMEBUFFER,s.COLOR_ATTACHMENT0+wt,s.RENDERBUFFER,Ut.__webglColorRenderbuffer[wt]);const Gt=r.get(T[wt]).__webglTexture;i.bindFramebuffer(s.FRAMEBUFFER,Ut.__webglFramebuffer),s.framebufferTexture2D(s.DRAW_FRAMEBUFFER,s.COLOR_ATTACHMENT0+wt,s.TEXTURE_2D,Gt,0)}i.bindFramebuffer(s.DRAW_FRAMEBUFFER,Ut.__webglMultisampledFramebuffer)}else if(B.depthBuffer&&B.resolveDepthBuffer===!1&&m){const T=B.stencilBuffer?s.DEPTH_STENCIL_ATTACHMENT:s.DEPTH_ATTACHMENT;s.invalidateFramebuffer(s.DRAW_FRAMEBUFFER,[T])}}}function Ue(B){return Math.min(l.maxSamples,B.samples)}function on(B){const T=r.get(B);return B.samples>0&&t.has("WEBGL_multisampled_render_to_texture")===!0&&T.__useRenderToTexture!==!1}function j(B){const T=f.render.frame;_.get(B)!==T&&(_.set(B,T),B.update())}function ke(B,T){const et=B.colorSpace,ut=B.format,mt=B.type;return B.isCompressedTexture===!0||B.isVideoTexture===!0||et!==pu&&et!==dr&&(Te.getTransfer(et)===Ve?(ut!==Pi||mt!==pi)&&se("WebGLTextures: sRGB encoded textures have to use RGBAFormat and UnsignedByteType."):Re("WebGLTextures: Unsupported texture color space:",et)),T}function Le(B){return typeof HTMLImageElement<"u"&&B instanceof HTMLImageElement?(p.width=B.naturalWidth||B.width,p.height=B.naturalHeight||B.height):typeof VideoFrame<"u"&&B instanceof VideoFrame?(p.width=B.displayWidth,p.height=B.displayHeight):(p.width=B.width,p.height=B.height),p}this.allocateTextureUnit=I,this.resetTextureUnits=K,this.getTextureUnits=tt,this.setTextureUnits=$,this.setTexture2D=at,this.setTexture2DArray=_t,this.setTexture3D=vt,this.setTextureCube=L,this.rebindTextures=Xt,this.setupRenderTarget=ae,this.updateRenderTargetMipmap=ge,this.updateMultisampleRenderTarget=Ke,this.setupDepthRenderbuffer=ie,this.setupFrameBufferTexture=Bt,this.useMultisampledRTT=on,this.isReversedDepthBuffer=function(){return i.buffers.depth.getReversed()}}function mR(s,t){function i(r,l=dr){let c;const f=Te.getTransfer(l);if(r===pi)return s.UNSIGNED_BYTE;if(r===zp)return s.UNSIGNED_SHORT_4_4_4_4;if(r===Fp)return s.UNSIGNED_SHORT_5_5_5_1;if(r===nS)return s.UNSIGNED_INT_5_9_9_9_REV;if(r===iS)return s.UNSIGNED_INT_10F_11F_11F_REV;if(r===tS)return s.BYTE;if(r===eS)return s.SHORT;if(r===dl)return s.UNSIGNED_SHORT;if(r===Ip)return s.INT;if(r===$i)return s.UNSIGNED_INT;if(r===Zi)return s.FLOAT;if(r===Da)return s.HALF_FLOAT;if(r===aS)return s.ALPHA;if(r===rS)return s.RGB;if(r===Pi)return s.RGBA;if(r===Na)return s.DEPTH_COMPONENT;if(r===qr)return s.DEPTH_STENCIL;if(r===sS)return s.RED;if(r===Hp)return s.RED_INTEGER;if(r===Yr)return s.RG;if(r===Gp)return s.RG_INTEGER;if(r===Vp)return s.RGBA_INTEGER;if(r===su||r===ou||r===lu||r===cu)if(f===Ve)if(c=t.get("WEBGL_compressed_texture_s3tc_srgb"),c!==null){if(r===su)return c.COMPRESSED_SRGB_S3TC_DXT1_EXT;if(r===ou)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;if(r===lu)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;if(r===cu)return c.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT}else return null;else if(c=t.get("WEBGL_compressed_texture_s3tc"),c!==null){if(r===su)return c.COMPRESSED_RGB_S3TC_DXT1_EXT;if(r===ou)return c.COMPRESSED_RGBA_S3TC_DXT1_EXT;if(r===lu)return c.COMPRESSED_RGBA_S3TC_DXT3_EXT;if(r===cu)return c.COMPRESSED_RGBA_S3TC_DXT5_EXT}else return null;if(r===Qh||r===Jh||r===$h||r===tp)if(c=t.get("WEBGL_compressed_texture_pvrtc"),c!==null){if(r===Qh)return c.COMPRESSED_RGB_PVRTC_4BPPV1_IMG;if(r===Jh)return c.COMPRESSED_RGB_PVRTC_2BPPV1_IMG;if(r===$h)return c.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;if(r===tp)return c.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG}else return null;if(r===ep||r===np||r===ip||r===ap||r===rp||r===du||r===sp)if(c=t.get("WEBGL_compressed_texture_etc"),c!==null){if(r===ep||r===np)return f===Ve?c.COMPRESSED_SRGB8_ETC2:c.COMPRESSED_RGB8_ETC2;if(r===ip)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:c.COMPRESSED_RGBA8_ETC2_EAC;if(r===ap)return c.COMPRESSED_R11_EAC;if(r===rp)return c.COMPRESSED_SIGNED_R11_EAC;if(r===du)return c.COMPRESSED_RG11_EAC;if(r===sp)return c.COMPRESSED_SIGNED_RG11_EAC}else return null;if(r===op||r===lp||r===cp||r===up||r===fp||r===dp||r===hp||r===pp||r===mp||r===gp||r===_p||r===vp||r===xp||r===Sp)if(c=t.get("WEBGL_compressed_texture_astc"),c!==null){if(r===op)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:c.COMPRESSED_RGBA_ASTC_4x4_KHR;if(r===lp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:c.COMPRESSED_RGBA_ASTC_5x4_KHR;if(r===cp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:c.COMPRESSED_RGBA_ASTC_5x5_KHR;if(r===up)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:c.COMPRESSED_RGBA_ASTC_6x5_KHR;if(r===fp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:c.COMPRESSED_RGBA_ASTC_6x6_KHR;if(r===dp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:c.COMPRESSED_RGBA_ASTC_8x5_KHR;if(r===hp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:c.COMPRESSED_RGBA_ASTC_8x6_KHR;if(r===pp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:c.COMPRESSED_RGBA_ASTC_8x8_KHR;if(r===mp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:c.COMPRESSED_RGBA_ASTC_10x5_KHR;if(r===gp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:c.COMPRESSED_RGBA_ASTC_10x6_KHR;if(r===_p)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:c.COMPRESSED_RGBA_ASTC_10x8_KHR;if(r===vp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:c.COMPRESSED_RGBA_ASTC_10x10_KHR;if(r===xp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:c.COMPRESSED_RGBA_ASTC_12x10_KHR;if(r===Sp)return f===Ve?c.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:c.COMPRESSED_RGBA_ASTC_12x12_KHR}else return null;if(r===yp||r===Mp||r===Ep)if(c=t.get("EXT_texture_compression_bptc"),c!==null){if(r===yp)return f===Ve?c.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT:c.COMPRESSED_RGBA_BPTC_UNORM_EXT;if(r===Mp)return c.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT;if(r===Ep)return c.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT}else return null;if(r===bp||r===Tp||r===hu||r===Ap)if(c=t.get("EXT_texture_compression_rgtc"),c!==null){if(r===bp)return c.COMPRESSED_RED_RGTC1_EXT;if(r===Tp)return c.COMPRESSED_SIGNED_RED_RGTC1_EXT;if(r===hu)return c.COMPRESSED_RED_GREEN_RGTC2_EXT;if(r===Ap)return c.COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT}else return null;return r===hl?s.UNSIGNED_INT_24_8:s[r]!==void 0?s[r]:null}return{convert:i}}const gR=`
void main() {

	gl_Position = vec4( position, 1.0 );

}`,_R=`
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

}`;class vR{constructor(){this.texture=null,this.mesh=null,this.depthNear=0,this.depthFar=0}init(t,i){if(this.texture===null){const r=new mS(t.texture);(t.depthNear!==i.depthNear||t.depthFar!==i.depthFar)&&(this.depthNear=t.depthNear,this.depthFar=t.depthFar),this.texture=r}}getMesh(t){if(this.texture!==null&&this.mesh===null){const i=t.cameras[0].viewport,r=new ta({vertexShader:gR,fragmentShader:_R,uniforms:{depthColor:{value:this.texture},depthWidth:{value:i.z},depthHeight:{value:i.w}}});this.mesh=new Ye(new Ji(20,20),r)}return this.mesh}reset(){this.texture=null,this.mesh=null}getDepthTexture(){return this.texture}}class xR extends jr{constructor(t,i){super();const r=this;let l=null,c=1,f=null,h="local-floor",m=1,p=null,_=null,v=null,g=null,x=null,M=null;const A=typeof XRWebGLBinding<"u",E=new vR,S={},P=i.getContextAttributes();let O=null,C=null;const z=[],D=[],U=new Ae;let b=null;const N=new hi;N.viewport=new sn;const G=new hi;G.viewport=new sn;const F=[N,G],k=new Tb;let K=null,tt=null;this.cameraAutoUpdate=!0,this.enabled=!1,this.isPresenting=!1,this.getController=function(it){let nt=z[it];return nt===void 0&&(nt=new Xd,z[it]=nt),nt.getTargetRaySpace()},this.getControllerGrip=function(it){let nt=z[it];return nt===void 0&&(nt=new Xd,z[it]=nt),nt.getGripSpace()},this.getHand=function(it){let nt=z[it];return nt===void 0&&(nt=new Xd,z[it]=nt),nt.getHandSpace()};function $(it){const nt=D.indexOf(it.inputSource);if(nt===-1)return;const ct=z[nt];ct!==void 0&&(ct.update(it.inputSource,it.frame,p||f),ct.dispatchEvent({type:it.type,data:it.inputSource}))}function I(){l.removeEventListener("select",$),l.removeEventListener("selectstart",$),l.removeEventListener("selectend",$),l.removeEventListener("squeeze",$),l.removeEventListener("squeezestart",$),l.removeEventListener("squeezeend",$),l.removeEventListener("end",I),l.removeEventListener("inputsourceschange",X);for(let it=0;it<z.length;it++){const nt=D[it];nt!==null&&(D[it]=null,z[it].disconnect(nt))}K=null,tt=null,E.reset();for(const it in S)delete S[it];t.setRenderTarget(O),x=null,g=null,v=null,l=null,C=null,Dt.stop(),r.isPresenting=!1,t.setPixelRatio(b),t.setSize(U.width,U.height,!1),r.dispatchEvent({type:"sessionend"})}this.setFramebufferScaleFactor=function(it){c=it,r.isPresenting===!0&&se("WebXRManager: Cannot change framebuffer scale while presenting.")},this.setReferenceSpaceType=function(it){h=it,r.isPresenting===!0&&se("WebXRManager: Cannot change reference space type while presenting.")},this.getReferenceSpace=function(){return p||f},this.setReferenceSpace=function(it){p=it},this.getBaseLayer=function(){return g!==null?g:x},this.getBinding=function(){return v===null&&A&&(v=new XRWebGLBinding(l,i)),v},this.getFrame=function(){return M},this.getSession=function(){return l},this.setSession=async function(it){if(l=it,l!==null){if(O=t.getRenderTarget(),l.addEventListener("select",$),l.addEventListener("selectstart",$),l.addEventListener("selectend",$),l.addEventListener("squeeze",$),l.addEventListener("squeezestart",$),l.addEventListener("squeezeend",$),l.addEventListener("end",I),l.addEventListener("inputsourceschange",X),P.xrCompatible!==!0&&await i.makeXRCompatible(),b=t.getPixelRatio(),t.getSize(U),A&&"createProjectionLayer"in XRWebGLBinding.prototype){let ct=null,bt=null,Ht=null;P.depth&&(Ht=P.stencil?i.DEPTH24_STENCIL8:i.DEPTH_COMPONENT24,ct=P.stencil?qr:Na,bt=P.stencil?hl:$i);const Bt={colorFormat:i.RGBA8,depthFormat:Ht,scaleFactor:c};v=this.getBinding(),g=v.createProjectionLayer(Bt),l.updateRenderState({layers:[g]}),t.setPixelRatio(1),t.setSize(g.textureWidth,g.textureHeight,!1),C=new Qi(g.textureWidth,g.textureHeight,{format:Pi,type:pi,depthTexture:new no(g.textureWidth,g.textureHeight,bt,void 0,void 0,void 0,void 0,void 0,void 0,ct),stencilBuffer:P.stencil,colorSpace:t.outputColorSpace,samples:P.antialias?4:0,resolveDepthBuffer:g.ignoreDepthValues===!1,resolveStencilBuffer:g.ignoreDepthValues===!1})}else{const ct={antialias:P.antialias,alpha:!0,depth:P.depth,stencil:P.stencil,framebufferScaleFactor:c};x=new XRWebGLLayer(l,i,ct),l.updateRenderState({baseLayer:x}),t.setPixelRatio(1),t.setSize(x.framebufferWidth,x.framebufferHeight,!1),C=new Qi(x.framebufferWidth,x.framebufferHeight,{format:Pi,type:pi,colorSpace:t.outputColorSpace,stencilBuffer:P.stencil,resolveDepthBuffer:x.ignoreDepthValues===!1,resolveStencilBuffer:x.ignoreDepthValues===!1})}C.isXRRenderTarget=!0,this.setFoveation(m),p=null,f=await l.requestReferenceSpace(h),Dt.setContext(l),Dt.start(),r.isPresenting=!0,r.dispatchEvent({type:"sessionstart"})}},this.getEnvironmentBlendMode=function(){if(l!==null)return l.environmentBlendMode},this.getDepthTexture=function(){return E.getDepthTexture()};function X(it){for(let nt=0;nt<it.removed.length;nt++){const ct=it.removed[nt],bt=D.indexOf(ct);bt>=0&&(D[bt]=null,z[bt].disconnect(ct))}for(let nt=0;nt<it.added.length;nt++){const ct=it.added[nt];let bt=D.indexOf(ct);if(bt===-1){for(let Bt=0;Bt<z.length;Bt++)if(Bt>=D.length){D.push(ct),bt=Bt;break}else if(D[Bt]===null){D[Bt]=ct,bt=Bt;break}if(bt===-1)break}const Ht=z[bt];Ht&&Ht.connect(ct)}}const at=new Q,_t=new Q;function vt(it,nt,ct){at.setFromMatrixPosition(nt.matrixWorld),_t.setFromMatrixPosition(ct.matrixWorld);const bt=at.distanceTo(_t),Ht=nt.projectionMatrix.elements,Bt=ct.projectionMatrix.elements,he=Ht[14]/(Ht[10]-1),ne=Ht[14]/(Ht[10]+1),ie=(Ht[9]+1)/Ht[5],Xt=(Ht[9]-1)/Ht[5],ae=(Ht[8]-1)/Ht[0],ge=(Bt[8]+1)/Bt[0],_e=he*ae,Ce=he*ge,Ke=bt/(-ae+ge),Ue=Ke*-ae;if(nt.matrixWorld.decompose(it.position,it.quaternion,it.scale),it.translateX(Ue),it.translateZ(Ke),it.matrixWorld.compose(it.position,it.quaternion,it.scale),it.matrixWorldInverse.copy(it.matrixWorld).invert(),Ht[10]===-1)it.projectionMatrix.copy(nt.projectionMatrix),it.projectionMatrixInverse.copy(nt.projectionMatrixInverse);else{const on=he+Ke,j=ne+Ke,ke=_e-Ue,Le=Ce+(bt-Ue),B=ie*ne/j*on,T=Xt*ne/j*on;it.projectionMatrix.makePerspective(ke,Le,B,T,on,j),it.projectionMatrixInverse.copy(it.projectionMatrix).invert()}}function L(it,nt){nt===null?it.matrixWorld.copy(it.matrix):it.matrixWorld.multiplyMatrices(nt.matrixWorld,it.matrix),it.matrixWorldInverse.copy(it.matrixWorld).invert()}this.updateCamera=function(it){if(l===null)return;let nt=it.near,ct=it.far;E.texture!==null&&(E.depthNear>0&&(nt=E.depthNear),E.depthFar>0&&(ct=E.depthFar)),k.near=G.near=N.near=nt,k.far=G.far=N.far=ct,(K!==k.near||tt!==k.far)&&(l.updateRenderState({depthNear:k.near,depthFar:k.far}),K=k.near,tt=k.far),k.layers.mask=it.layers.mask|6,N.layers.mask=k.layers.mask&-5,G.layers.mask=k.layers.mask&-3;const bt=it.parent,Ht=k.cameras;L(k,bt);for(let Bt=0;Bt<Ht.length;Bt++)L(Ht[Bt],bt);Ht.length===2?vt(k,N,G):k.projectionMatrix.copy(N.projectionMatrix),Y(it,k,bt)};function Y(it,nt,ct){ct===null?it.matrix.copy(nt.matrixWorld):(it.matrix.copy(ct.matrixWorld),it.matrix.invert(),it.matrix.multiply(nt.matrixWorld)),it.matrix.decompose(it.position,it.quaternion,it.scale),it.updateMatrixWorld(!0),it.projectionMatrix.copy(nt.projectionMatrix),it.projectionMatrixInverse.copy(nt.projectionMatrixInverse),it.isPerspectiveCamera&&(it.fov=gl*2*Math.atan(1/it.projectionMatrix.elements[5]),it.zoom=1)}this.getCamera=function(){return k},this.getFoveation=function(){if(!(g===null&&x===null))return m},this.setFoveation=function(it){m=it,g!==null&&(g.fixedFoveation=it),x!==null&&x.fixedFoveation!==void 0&&(x.fixedFoveation=it)},this.hasDepthSensing=function(){return E.texture!==null},this.getDepthSensingMesh=function(){return E.getMesh(k)},this.getCameraTexture=function(it){return S[it]};let yt=null;function At(it,nt){if(_=nt.getViewerPose(p||f),M=nt,_!==null){const ct=_.views;x!==null&&(t.setRenderTargetFramebuffer(C,x.framebuffer),t.setRenderTarget(C));let bt=!1;ct.length!==k.cameras.length&&(k.cameras.length=0,bt=!0);for(let ne=0;ne<ct.length;ne++){const ie=ct[ne];let Xt=null;if(x!==null)Xt=x.getViewport(ie);else{const ge=v.getViewSubImage(g,ie);Xt=ge.viewport,ne===0&&(t.setRenderTargetTextures(C,ge.colorTexture,ge.depthStencilTexture),t.setRenderTarget(C))}let ae=F[ne];ae===void 0&&(ae=new hi,ae.layers.enable(ne),ae.viewport=new sn,F[ne]=ae),ae.matrix.fromArray(ie.transform.matrix),ae.matrix.decompose(ae.position,ae.quaternion,ae.scale),ae.projectionMatrix.fromArray(ie.projectionMatrix),ae.projectionMatrixInverse.copy(ae.projectionMatrix).invert(),ae.viewport.set(Xt.x,Xt.y,Xt.width,Xt.height),ne===0&&(k.matrix.copy(ae.matrix),k.matrix.decompose(k.position,k.quaternion,k.scale)),bt===!0&&k.cameras.push(ae)}const Ht=l.enabledFeatures;if(Ht&&Ht.includes("depth-sensing")&&l.depthUsage=="gpu-optimized"&&A){v=r.getBinding();const ne=v.getDepthInformation(ct[0]);ne&&ne.isValid&&ne.texture&&E.init(ne,l.renderState)}if(Ht&&Ht.includes("camera-access")&&A){t.state.unbindTexture(),v=r.getBinding();for(let ne=0;ne<ct.length;ne++){const ie=ct[ne].camera;if(ie){let Xt=S[ie];Xt||(Xt=new mS,S[ie]=Xt);const ae=v.getCameraImage(ie);Xt.sourceTexture=ae}}}}for(let ct=0;ct<z.length;ct++){const bt=D[ct],Ht=z[ct];bt!==null&&Ht!==void 0&&Ht.update(bt,nt,p||f)}yt&&yt(it,nt),nt.detectedPlanes&&r.dispatchEvent({type:"planesdetected",data:nt}),M=null}const Dt=new xS;Dt.setAnimationLoop(At),this.setAnimationLoop=function(it){yt=it},this.dispose=function(){}}}const SR=new $e,AS=new ce;AS.set(-1,0,0,0,1,0,0,0,1);function yR(s,t){function i(E,S){E.matrixAutoUpdate===!0&&E.updateMatrix(),S.value.copy(E.matrix)}function r(E,S){S.color.getRGB(E.fogColor.value,gS(s)),S.isFog?(E.fogNear.value=S.near,E.fogFar.value=S.far):S.isFogExp2&&(E.fogDensity.value=S.density)}function l(E,S,P,O,C){S.isNodeMaterial?S.uniformsNeedUpdate=!1:S.isMeshBasicMaterial?c(E,S):S.isMeshLambertMaterial?(c(E,S),S.envMap&&(E.envMapIntensity.value=S.envMapIntensity)):S.isMeshToonMaterial?(c(E,S),v(E,S)):S.isMeshPhongMaterial?(c(E,S),_(E,S),S.envMap&&(E.envMapIntensity.value=S.envMapIntensity)):S.isMeshStandardMaterial?(c(E,S),g(E,S),S.isMeshPhysicalMaterial&&x(E,S,C)):S.isMeshMatcapMaterial?(c(E,S),M(E,S)):S.isMeshDepthMaterial?c(E,S):S.isMeshDistanceMaterial?(c(E,S),A(E,S)):S.isMeshNormalMaterial?c(E,S):S.isLineBasicMaterial?(f(E,S),S.isLineDashedMaterial&&h(E,S)):S.isPointsMaterial?m(E,S,P,O):S.isSpriteMaterial?p(E,S):S.isShadowMaterial?(E.color.value.copy(S.color),E.opacity.value=S.opacity):S.isShaderMaterial&&(S.uniformsNeedUpdate=!1)}function c(E,S){E.opacity.value=S.opacity,S.color&&E.diffuse.value.copy(S.color),S.emissive&&E.emissive.value.copy(S.emissive).multiplyScalar(S.emissiveIntensity),S.map&&(E.map.value=S.map,i(S.map,E.mapTransform)),S.alphaMap&&(E.alphaMap.value=S.alphaMap,i(S.alphaMap,E.alphaMapTransform)),S.bumpMap&&(E.bumpMap.value=S.bumpMap,i(S.bumpMap,E.bumpMapTransform),E.bumpScale.value=S.bumpScale,S.side===Jn&&(E.bumpScale.value*=-1)),S.normalMap&&(E.normalMap.value=S.normalMap,i(S.normalMap,E.normalMapTransform),E.normalScale.value.copy(S.normalScale),S.side===Jn&&E.normalScale.value.negate()),S.displacementMap&&(E.displacementMap.value=S.displacementMap,i(S.displacementMap,E.displacementMapTransform),E.displacementScale.value=S.displacementScale,E.displacementBias.value=S.displacementBias),S.emissiveMap&&(E.emissiveMap.value=S.emissiveMap,i(S.emissiveMap,E.emissiveMapTransform)),S.specularMap&&(E.specularMap.value=S.specularMap,i(S.specularMap,E.specularMapTransform)),S.alphaTest>0&&(E.alphaTest.value=S.alphaTest);const P=t.get(S),O=P.envMap,C=P.envMapRotation;O&&(E.envMap.value=O,E.envMapRotation.value.setFromMatrix4(SR.makeRotationFromEuler(C)).transpose(),O.isCubeTexture&&O.isRenderTargetTexture===!1&&E.envMapRotation.value.premultiply(AS),E.reflectivity.value=S.reflectivity,E.ior.value=S.ior,E.refractionRatio.value=S.refractionRatio),S.lightMap&&(E.lightMap.value=S.lightMap,E.lightMapIntensity.value=S.lightMapIntensity,i(S.lightMap,E.lightMapTransform)),S.aoMap&&(E.aoMap.value=S.aoMap,E.aoMapIntensity.value=S.aoMapIntensity,i(S.aoMap,E.aoMapTransform))}function f(E,S){E.diffuse.value.copy(S.color),E.opacity.value=S.opacity,S.map&&(E.map.value=S.map,i(S.map,E.mapTransform))}function h(E,S){E.dashSize.value=S.dashSize,E.totalSize.value=S.dashSize+S.gapSize,E.scale.value=S.scale}function m(E,S,P,O){E.diffuse.value.copy(S.color),E.opacity.value=S.opacity,E.size.value=S.size*P,E.scale.value=O*.5,S.map&&(E.map.value=S.map,i(S.map,E.uvTransform)),S.alphaMap&&(E.alphaMap.value=S.alphaMap,i(S.alphaMap,E.alphaMapTransform)),S.alphaTest>0&&(E.alphaTest.value=S.alphaTest)}function p(E,S){E.diffuse.value.copy(S.color),E.opacity.value=S.opacity,E.rotation.value=S.rotation,S.map&&(E.map.value=S.map,i(S.map,E.mapTransform)),S.alphaMap&&(E.alphaMap.value=S.alphaMap,i(S.alphaMap,E.alphaMapTransform)),S.alphaTest>0&&(E.alphaTest.value=S.alphaTest)}function _(E,S){E.specular.value.copy(S.specular),E.shininess.value=Math.max(S.shininess,1e-4)}function v(E,S){S.gradientMap&&(E.gradientMap.value=S.gradientMap)}function g(E,S){E.metalness.value=S.metalness,S.metalnessMap&&(E.metalnessMap.value=S.metalnessMap,i(S.metalnessMap,E.metalnessMapTransform)),E.roughness.value=S.roughness,S.roughnessMap&&(E.roughnessMap.value=S.roughnessMap,i(S.roughnessMap,E.roughnessMapTransform)),S.envMap&&(E.envMapIntensity.value=S.envMapIntensity)}function x(E,S,P){E.ior.value=S.ior,S.sheen>0&&(E.sheenColor.value.copy(S.sheenColor).multiplyScalar(S.sheen),E.sheenRoughness.value=S.sheenRoughness,S.sheenColorMap&&(E.sheenColorMap.value=S.sheenColorMap,i(S.sheenColorMap,E.sheenColorMapTransform)),S.sheenRoughnessMap&&(E.sheenRoughnessMap.value=S.sheenRoughnessMap,i(S.sheenRoughnessMap,E.sheenRoughnessMapTransform))),S.clearcoat>0&&(E.clearcoat.value=S.clearcoat,E.clearcoatRoughness.value=S.clearcoatRoughness,S.clearcoatMap&&(E.clearcoatMap.value=S.clearcoatMap,i(S.clearcoatMap,E.clearcoatMapTransform)),S.clearcoatRoughnessMap&&(E.clearcoatRoughnessMap.value=S.clearcoatRoughnessMap,i(S.clearcoatRoughnessMap,E.clearcoatRoughnessMapTransform)),S.clearcoatNormalMap&&(E.clearcoatNormalMap.value=S.clearcoatNormalMap,i(S.clearcoatNormalMap,E.clearcoatNormalMapTransform),E.clearcoatNormalScale.value.copy(S.clearcoatNormalScale),S.side===Jn&&E.clearcoatNormalScale.value.negate())),S.dispersion>0&&(E.dispersion.value=S.dispersion),S.iridescence>0&&(E.iridescence.value=S.iridescence,E.iridescenceIOR.value=S.iridescenceIOR,E.iridescenceThicknessMinimum.value=S.iridescenceThicknessRange[0],E.iridescenceThicknessMaximum.value=S.iridescenceThicknessRange[1],S.iridescenceMap&&(E.iridescenceMap.value=S.iridescenceMap,i(S.iridescenceMap,E.iridescenceMapTransform)),S.iridescenceThicknessMap&&(E.iridescenceThicknessMap.value=S.iridescenceThicknessMap,i(S.iridescenceThicknessMap,E.iridescenceThicknessMapTransform))),S.transmission>0&&(E.transmission.value=S.transmission,E.transmissionSamplerMap.value=P.texture,E.transmissionSamplerSize.value.set(P.width,P.height),S.transmissionMap&&(E.transmissionMap.value=S.transmissionMap,i(S.transmissionMap,E.transmissionMapTransform)),E.thickness.value=S.thickness,S.thicknessMap&&(E.thicknessMap.value=S.thicknessMap,i(S.thicknessMap,E.thicknessMapTransform)),E.attenuationDistance.value=S.attenuationDistance,E.attenuationColor.value.copy(S.attenuationColor)),S.anisotropy>0&&(E.anisotropyVector.value.set(S.anisotropy*Math.cos(S.anisotropyRotation),S.anisotropy*Math.sin(S.anisotropyRotation)),S.anisotropyMap&&(E.anisotropyMap.value=S.anisotropyMap,i(S.anisotropyMap,E.anisotropyMapTransform))),E.specularIntensity.value=S.specularIntensity,E.specularColor.value.copy(S.specularColor),S.specularColorMap&&(E.specularColorMap.value=S.specularColorMap,i(S.specularColorMap,E.specularColorMapTransform)),S.specularIntensityMap&&(E.specularIntensityMap.value=S.specularIntensityMap,i(S.specularIntensityMap,E.specularIntensityMapTransform))}function M(E,S){S.matcap&&(E.matcap.value=S.matcap)}function A(E,S){const P=t.get(S).light;E.referencePosition.value.setFromMatrixPosition(P.matrixWorld),E.nearDistance.value=P.shadow.camera.near,E.farDistance.value=P.shadow.camera.far}return{refreshFogUniforms:r,refreshMaterialUniforms:l}}function MR(s,t,i,r){let l={},c={},f=[];const h=s.getParameter(s.MAX_UNIFORM_BUFFER_BINDINGS);function m(C,z){const D=z.program;r.uniformBlockBinding(C,D)}function p(C,z){let D=l[C.id];D===void 0&&(E(C),D=_(C),l[C.id]=D,C.addEventListener("dispose",P));const U=z.program;r.updateUBOMapping(C,U);const b=t.render.frame;c[C.id]!==b&&(g(C),c[C.id]=b)}function _(C){const z=v();C.__bindingPointIndex=z;const D=s.createBuffer(),U=C.__size,b=C.usage;return s.bindBuffer(s.UNIFORM_BUFFER,D),s.bufferData(s.UNIFORM_BUFFER,U,b),s.bindBuffer(s.UNIFORM_BUFFER,null),s.bindBufferBase(s.UNIFORM_BUFFER,z,D),D}function v(){for(let C=0;C<h;C++)if(f.indexOf(C)===-1)return f.push(C),C;return Re("WebGLRenderer: Maximum number of simultaneously usable uniforms groups reached."),0}function g(C){const z=l[C.id],D=C.uniforms,U=C.__cache;s.bindBuffer(s.UNIFORM_BUFFER,z);for(let b=0,N=D.length;b<N;b++){const G=D[b];if(Array.isArray(G))for(let F=0,k=G.length;F<k;F++)x(G[F],b,F,U);else x(G,b,0,U)}s.bindBuffer(s.UNIFORM_BUFFER,null)}function x(C,z,D,U){if(A(C,z,D,U)===!0){const b=C.__offset,N=C.value;if(Array.isArray(N)){let G=0;for(let F=0;F<N.length;F++){const k=N[F],K=S(k);M(k,C.__data,G),typeof k!="number"&&typeof k!="boolean"&&!k.isMatrix3&&!ArrayBuffer.isView(k)&&(G+=K.storage/Float32Array.BYTES_PER_ELEMENT)}}else M(N,C.__data,0);s.bufferSubData(s.UNIFORM_BUFFER,b,C.__data)}}function M(C,z,D){typeof C=="number"||typeof C=="boolean"?z[0]=C:C.isMatrix3?(z[0]=C.elements[0],z[1]=C.elements[1],z[2]=C.elements[2],z[3]=0,z[4]=C.elements[3],z[5]=C.elements[4],z[6]=C.elements[5],z[7]=0,z[8]=C.elements[6],z[9]=C.elements[7],z[10]=C.elements[8],z[11]=0):ArrayBuffer.isView(C)?z.set(new C.constructor(C.buffer,C.byteOffset,z.length)):C.toArray(z,D)}function A(C,z,D,U){const b=C.value,N=z+"_"+D;if(U[N]===void 0)return typeof b=="number"||typeof b=="boolean"?U[N]=b:ArrayBuffer.isView(b)?U[N]=b.slice():U[N]=b.clone(),!0;{const G=U[N];if(typeof b=="number"||typeof b=="boolean"){if(G!==b)return U[N]=b,!0}else{if(ArrayBuffer.isView(b))return!0;if(G.equals(b)===!1)return G.copy(b),!0}}return!1}function E(C){const z=C.uniforms;let D=0;const U=16;for(let N=0,G=z.length;N<G;N++){const F=Array.isArray(z[N])?z[N]:[z[N]];for(let k=0,K=F.length;k<K;k++){const tt=F[k],$=Array.isArray(tt.value)?tt.value:[tt.value];for(let I=0,X=$.length;I<X;I++){const at=$[I],_t=S(at),vt=D%U,L=vt%_t.boundary,Y=vt+L;D+=L,Y!==0&&U-Y<_t.storage&&(D+=U-Y),tt.__data=new Float32Array(_t.storage/Float32Array.BYTES_PER_ELEMENT),tt.__offset=D,D+=_t.storage}}}const b=D%U;return b>0&&(D+=U-b),C.__size=D,C.__cache={},this}function S(C){const z={boundary:0,storage:0};return typeof C=="number"||typeof C=="boolean"?(z.boundary=4,z.storage=4):C.isVector2?(z.boundary=8,z.storage=8):C.isVector3||C.isColor?(z.boundary=16,z.storage=12):C.isVector4?(z.boundary=16,z.storage=16):C.isMatrix3?(z.boundary=48,z.storage=48):C.isMatrix4?(z.boundary=64,z.storage=64):C.isTexture?se("WebGLRenderer: Texture samplers can not be part of an uniforms group."):ArrayBuffer.isView(C)?(z.boundary=16,z.storage=C.byteLength):se("WebGLRenderer: Unsupported uniform value type.",C),z}function P(C){const z=C.target;z.removeEventListener("dispose",P);const D=f.indexOf(z.__bindingPointIndex);f.splice(D,1),s.deleteBuffer(l[z.id]),delete l[z.id],delete c[z.id]}function O(){for(const C in l)s.deleteBuffer(l[C]);f=[],l={},c={}}return{bind:m,update:p,dispose:O}}const ER=new Uint16Array([12469,15057,12620,14925,13266,14620,13807,14376,14323,13990,14545,13625,14713,13328,14840,12882,14931,12528,14996,12233,15039,11829,15066,11525,15080,11295,15085,10976,15082,10705,15073,10495,13880,14564,13898,14542,13977,14430,14158,14124,14393,13732,14556,13410,14702,12996,14814,12596,14891,12291,14937,11834,14957,11489,14958,11194,14943,10803,14921,10506,14893,10278,14858,9960,14484,14039,14487,14025,14499,13941,14524,13740,14574,13468,14654,13106,14743,12678,14818,12344,14867,11893,14889,11509,14893,11180,14881,10751,14852,10428,14812,10128,14765,9754,14712,9466,14764,13480,14764,13475,14766,13440,14766,13347,14769,13070,14786,12713,14816,12387,14844,11957,14860,11549,14868,11215,14855,10751,14825,10403,14782,10044,14729,9651,14666,9352,14599,9029,14967,12835,14966,12831,14963,12804,14954,12723,14936,12564,14917,12347,14900,11958,14886,11569,14878,11247,14859,10765,14828,10401,14784,10011,14727,9600,14660,9289,14586,8893,14508,8533,15111,12234,15110,12234,15104,12216,15092,12156,15067,12010,15028,11776,14981,11500,14942,11205,14902,10752,14861,10393,14812,9991,14752,9570,14682,9252,14603,8808,14519,8445,14431,8145,15209,11449,15208,11451,15202,11451,15190,11438,15163,11384,15117,11274,15055,10979,14994,10648,14932,10343,14871,9936,14803,9532,14729,9218,14645,8742,14556,8381,14461,8020,14365,7603,15273,10603,15272,10607,15267,10619,15256,10631,15231,10614,15182,10535,15118,10389,15042,10167,14963,9787,14883,9447,14800,9115,14710,8665,14615,8318,14514,7911,14411,7507,14279,7198,15314,9675,15313,9683,15309,9712,15298,9759,15277,9797,15229,9773,15166,9668,15084,9487,14995,9274,14898,8910,14800,8539,14697,8234,14590,7790,14479,7409,14367,7067,14178,6621,15337,8619,15337,8631,15333,8677,15325,8769,15305,8871,15264,8940,15202,8909,15119,8775,15022,8565,14916,8328,14804,8009,14688,7614,14569,7287,14448,6888,14321,6483,14088,6171,15350,7402,15350,7419,15347,7480,15340,7613,15322,7804,15287,7973,15229,8057,15148,8012,15046,7846,14933,7611,14810,7357,14682,7069,14552,6656,14421,6316,14251,5948,14007,5528,15356,5942,15356,5977,15353,6119,15348,6294,15332,6551,15302,6824,15249,7044,15171,7122,15070,7050,14949,6861,14818,6611,14679,6349,14538,6067,14398,5651,14189,5311,13935,4958,15359,4123,15359,4153,15356,4296,15353,4646,15338,5160,15311,5508,15263,5829,15188,6042,15088,6094,14966,6001,14826,5796,14678,5543,14527,5287,14377,4985,14133,4586,13869,4257,15360,1563,15360,1642,15358,2076,15354,2636,15341,3350,15317,4019,15273,4429,15203,4732,15105,4911,14981,4932,14836,4818,14679,4621,14517,4386,14359,4156,14083,3795,13808,3437,15360,122,15360,137,15358,285,15355,636,15344,1274,15322,2177,15281,2765,15215,3223,15120,3451,14995,3569,14846,3567,14681,3466,14511,3305,14344,3121,14037,2800,13753,2467,15360,0,15360,1,15359,21,15355,89,15346,253,15325,479,15287,796,15225,1148,15133,1492,15008,1749,14856,1882,14685,1886,14506,1783,14324,1608,13996,1398,13702,1183]);let Xi=null;function bR(){return Xi===null&&(Xi=new ib(ER,16,16,Yr,Da),Xi.name="DFG_LUT",Xi.minFilter=wn,Xi.magFilter=wn,Xi.wrapS=Aa,Xi.wrapT=Aa,Xi.generateMipmaps=!1,Xi.needsUpdate=!0),Xi}class TR{constructor(t={}){const{canvas:i=xE(),context:r=null,depth:l=!0,stencil:c=!1,alpha:f=!1,antialias:h=!1,premultipliedAlpha:m=!0,preserveDrawingBuffer:p=!1,powerPreference:_="default",failIfMajorPerformanceCaveat:v=!1,reversedDepthBuffer:g=!1,outputBufferType:x=pi}=t;this.isWebGLRenderer=!0;let M;if(r!==null){if(typeof WebGLRenderingContext<"u"&&r instanceof WebGLRenderingContext)throw new Error("THREE.WebGLRenderer: WebGL 1 is not supported since r163.");M=r.getContextAttributes().alpha}else M=f;const A=x,E=new Set([Vp,Gp,Hp]),S=new Set([pi,$i,dl,hl,zp,Fp]),P=new Uint32Array(4),O=new Int32Array(4),C=new Q;let z=null,D=null;const U=[],b=[];let N=null;this.domElement=i,this.debug={checkShaderErrors:!0,onShaderError:null},this.autoClear=!0,this.autoClearColor=!0,this.autoClearDepth=!0,this.autoClearStencil=!0,this.sortObjects=!0,this.clippingPlanes=[],this.localClippingEnabled=!1,this.toneMapping=Ki,this.toneMappingExposure=1,this.transmissionResolutionScale=1;const G=this;let F=!1,k=null,K=null,tt=null,$=null;this._outputColorSpace=Gn;let I=0,X=0,at=null,_t=-1,vt=null;const L=new sn,Y=new sn;let yt=null;const At=new me(0);let Dt=0,it=i.width,nt=i.height,ct=1,bt=null,Ht=null;const Bt=new sn(0,0,it,nt),he=new sn(0,0,it,nt);let ne=!1;const ie=new jp;let Xt=!1,ae=!1;const ge=new $e,_e=new Q,Ce=new sn,Ke={background:null,fog:null,environment:null,overrideMaterial:null,isScene:!0};let Ue=!1;function on(){return at===null?ct:1}let j=r;function ke(R,Z){return i.getContext(R,Z)}try{const R={alpha:!0,depth:l,stencil:c,antialias:h,premultipliedAlpha:m,preserveDrawingBuffer:p,powerPreference:_,failIfMajorPerformanceCaveat:v};if("setAttribute"in i&&i.setAttribute("data-engine",`three.js r${Pp}`),i.addEventListener("webglcontextlost",tn,!1),i.addEventListener("webglcontextrestored",Be,!1),i.addEventListener("webglcontextcreationerror",ti,!1),j===null){const Z="webgl2";if(j=ke(Z,R),j===null)throw ke(Z)?new Error("THREE.WebGLRenderer: Error creating WebGL context with your selected attributes."):new Error("THREE.WebGLRenderer: Error creating WebGL context.")}}catch(R){throw Re("WebGLRenderer: "+R.message),R}let Le,B,T,et,ut,mt,Rt,Ut,pt,gt,wt,Gt,Pt,Lt,Jt,$t,oe,W,Ct,St,Nt,Ft,Tt;function jt(){Le=new b2(j),Le.init(),Nt=new mR(j,Le),B=new g2(j,Le,t,Nt),T=new hR(j,Le),B.reversedDepthBuffer&&g&&T.buffers.depth.setReversed(!0),K=j.createFramebuffer(),tt=j.createFramebuffer(),$=j.createFramebuffer(),et=new R2(j),ut=new $3,mt=new pR(j,Le,T,ut,B,Nt,et),Rt=new E2(G),Ut=new Nb(j),Ft=new p2(j,Ut),pt=new T2(j,Ut,et,Ft),gt=new w2(j,pt,Ut,Ft,et),W=new C2(j,B,mt),Jt=new _2(ut),wt=new J3(G,Rt,Le,B,Ft,Jt),Gt=new yR(G,ut),Pt=new eR,Lt=new oR(Le),oe=new h2(G,Rt,T,gt,M,m),$t=new dR(G,gt,B),Tt=new MR(j,et,B,T),Ct=new m2(j,Le,et),St=new A2(j,Le,et),et.programs=wt.programs,G.capabilities=B,G.extensions=Le,G.properties=ut,G.renderLists=Pt,G.shadowMap=$t,G.state=T,G.info=et}jt(),A!==pi&&(N=new N2(A,i.width,i.height,h,l,c));const qt=new xR(G,j);this.xr=qt,this.getContext=function(){return j},this.getContextAttributes=function(){return j.getContextAttributes()},this.forceContextLoss=function(){const R=Le.get("WEBGL_lose_context");R&&R.loseContext()},this.forceContextRestore=function(){const R=Le.get("WEBGL_lose_context");R&&R.restoreContext()},this.getPixelRatio=function(){return ct},this.setPixelRatio=function(R){R!==void 0&&(ct=R,this.setSize(it,nt,!1))},this.getSize=function(R){return R.set(it,nt)},this.setSize=function(R,Z,ft=!0){if(qt.isPresenting){se("WebGLRenderer: Can't change size while VR device is presenting.");return}it=R,nt=Z,i.width=Math.floor(R*ct),i.height=Math.floor(Z*ct),ft===!0&&(i.style.width=R+"px",i.style.height=Z+"px"),N!==null&&N.setSize(i.width,i.height),this.setViewport(0,0,R,Z)},this.getDrawingBufferSize=function(R){return R.set(it*ct,nt*ct).floor()},this.setDrawingBufferSize=function(R,Z,ft){it=R,nt=Z,ct=ft,i.width=Math.floor(R*ft),i.height=Math.floor(Z*ft),this.setViewport(0,0,R,Z)},this.setEffects=function(R){if(A===pi){Re("WebGLRenderer: setEffects() requires outputBufferType set to HalfFloatType or FloatType.");return}if(R){for(let Z=0;Z<R.length;Z++)if(R[Z].isOutputPass===!0){se("WebGLRenderer: OutputPass is not needed in setEffects(). Tone mapping and color space conversion are applied automatically.");break}}N.setEffects(R||[])},this.getCurrentViewport=function(R){return R.copy(L)},this.getViewport=function(R){return R.copy(Bt)},this.setViewport=function(R,Z,ft,ot){R.isVector4?Bt.set(R.x,R.y,R.z,R.w):Bt.set(R,Z,ft,ot),T.viewport(L.copy(Bt).multiplyScalar(ct).round())},this.getScissor=function(R){return R.copy(he)},this.setScissor=function(R,Z,ft,ot){R.isVector4?he.set(R.x,R.y,R.z,R.w):he.set(R,Z,ft,ot),T.scissor(Y.copy(he).multiplyScalar(ct).round())},this.getScissorTest=function(){return ne},this.setScissorTest=function(R){T.setScissorTest(ne=R)},this.setOpaqueSort=function(R){bt=R},this.setTransparentSort=function(R){Ht=R},this.getClearColor=function(R){return R.copy(oe.getClearColor())},this.setClearColor=function(){oe.setClearColor(...arguments)},this.getClearAlpha=function(){return oe.getClearAlpha()},this.setClearAlpha=function(){oe.setClearAlpha(...arguments)},this.clear=function(R=!0,Z=!0,ft=!0){let ot=0;if(R){let lt=!1;if(at!==null){const It=at.texture.format;lt=E.has(It)}if(lt){const It=at.texture.type,kt=S.has(It),Ot=oe.getClearColor(),Yt=oe.getClearAlpha(),Wt=Ot.r,te=Ot.g,ue=Ot.b;kt?(P[0]=Wt,P[1]=te,P[2]=ue,P[3]=Yt,j.clearBufferuiv(j.COLOR,0,P)):(O[0]=Wt,O[1]=te,O[2]=ue,O[3]=Yt,j.clearBufferiv(j.COLOR,0,O))}else ot|=j.COLOR_BUFFER_BIT}Z&&(ot|=j.DEPTH_BUFFER_BIT,this.state.buffers.depth.setMask(!0)),ft&&(ot|=j.STENCIL_BUFFER_BIT,this.state.buffers.stencil.setMask(4294967295)),ot!==0&&j.clear(ot)},this.clearColor=function(){this.clear(!0,!1,!1)},this.clearDepth=function(){this.clear(!1,!0,!1)},this.clearStencil=function(){this.clear(!1,!1,!0)},this.setNodesHandler=function(R){R.setRenderer(this),k=R},this.dispose=function(){i.removeEventListener("webglcontextlost",tn,!1),i.removeEventListener("webglcontextrestored",Be,!1),i.removeEventListener("webglcontextcreationerror",ti,!1),oe.dispose(),Pt.dispose(),Lt.dispose(),ut.dispose(),Rt.dispose(),gt.dispose(),Ft.dispose(),Tt.dispose(),wt.dispose(),qt.dispose(),qt.removeEventListener("sessionstart",pn),qt.removeEventListener("sessionend",Dn),qn.stop()};function tn(R){R.preventDefault(),lv("WebGLRenderer: Context Lost."),F=!0}function Be(){lv("WebGLRenderer: Context Restored."),F=!1;const R=et.autoReset,Z=$t.enabled,ft=$t.autoUpdate,ot=$t.needsUpdate,lt=$t.type;jt(),et.autoReset=R,$t.enabled=Z,$t.autoUpdate=ft,$t.needsUpdate=ot,$t.type=lt}function ti(R){Re("WebGLRenderer: A WebGL context could not be created. Reason: ",R.statusMessage)}function ei(R){const Z=R.target;Z.removeEventListener("dispose",ei),so(Z)}function so(R){oo(R),ut.remove(R)}function oo(R){const Z=ut.get(R).programs;Z!==void 0&&(Z.forEach(function(ft){wt.releaseProgram(ft)}),R.isShaderMaterial&&wt.releaseShaderCache(R))}this.renderBufferDirect=function(R,Z,ft,ot,lt,It){Z===null&&(Z=Ke);const kt=lt.isMesh&&lt.matrixWorld.determinantAffine()<0,Ot=Oa(R,Z,ft,ot,lt);T.setMaterial(ot,kt);let Yt=ft.index,Wt=1;if(ot.wireframe===!0){if(Yt=pt.getWireframeAttribute(ft),Yt===void 0)return;Wt=2}const te=ft.drawRange,ue=ft.attributes.position;let Qt=te.start*Wt,we=(te.start+te.count)*Wt;It!==null&&(Qt=Math.max(Qt,It.start*Wt),we=Math.min(we,(It.start+It.count)*Wt)),Yt!==null?(Qt=Math.max(Qt,0),we=Math.min(we,Yt.count)):ue!=null&&(Qt=Math.max(Qt,0),we=Math.min(we,ue.count));const en=we-Qt;if(en<0||en===1/0)return;Ft.setup(lt,ot,Ot,ft,Yt);let Ze,Ie=Ct;if(Yt!==null&&(Ze=Ut.get(Yt),Ie=St,Ie.setIndex(Ze)),lt.isMesh)ot.wireframe===!0?(T.setLineWidth(ot.wireframeLinewidth*on()),Ie.setMode(j.LINES)):Ie.setMode(j.TRIANGLES);else if(lt.isLine){let ze=ot.linewidth;ze===void 0&&(ze=1),T.setLineWidth(ze*on()),lt.isLineSegments?Ie.setMode(j.LINES):lt.isLineLoop?Ie.setMode(j.LINE_LOOP):Ie.setMode(j.LINE_STRIP)}else lt.isPoints?Ie.setMode(j.POINTS):lt.isSprite&&Ie.setMode(j.TRIANGLES);if(lt.isBatchedMesh)if(Le.get("WEBGL_multi_draw"))Ie.renderMultiDraw(lt._multiDrawStarts,lt._multiDrawCounts,lt._multiDrawCount);else{const ze=lt._multiDrawStarts,Vt=lt._multiDrawCounts,In=lt._multiDrawCount,ve=Yt?Ut.get(Yt).bytesPerElement:1,yn=ut.get(ot).currentProgram.getUniforms();for(let ni=0;ni<In;ni++)yn.setValue(j,"_gl_DrawID",ni),Ie.render(ze[ni]/ve,Vt[ni])}else if(lt.isInstancedMesh)Ie.renderInstances(Qt,en,lt.count);else if(ft.isInstancedBufferGeometry){const ze=ft._maxInstanceCount!==void 0?ft._maxInstanceCount:1/0,Vt=Math.min(ft.instanceCount,ze);Ie.renderInstances(Qt,en,Vt)}else Ie.render(Qt,en)};function lo(R,Z,ft){R.transparent===!0&&R.side===Yi&&R.forceSinglePass===!1?(R.side=Jn,R.needsUpdate=!0,La(R,Z,ft),R.side=pr,R.needsUpdate=!0,La(R,Z,ft),R.side=Yi):La(R,Z,ft)}this.compile=function(R,Z,ft=null){ft===null&&(ft=R),D=Lt.get(ft),D.init(Z),b.push(D),ft.traverseVisible(function(lt){lt.isLight&&lt.layers.test(Z.layers)&&(D.pushLight(lt),lt.castShadow&&D.pushShadow(lt))}),R!==ft&&R.traverseVisible(function(lt){lt.isLight&&lt.layers.test(Z.layers)&&(D.pushLight(lt),lt.castShadow&&D.pushShadow(lt))}),D.setupLights();const ot=new Set;return R.traverse(function(lt){if(!(lt.isMesh||lt.isPoints||lt.isLine||lt.isSprite))return;const It=lt.material;if(It)if(Array.isArray(It))for(let kt=0;kt<It.length;kt++){const Ot=It[kt];lo(Ot,ft,lt),ot.add(Ot)}else lo(It,ft,lt),ot.add(It)}),D=b.pop(),ot},this.compileAsync=function(R,Z,ft=null){const ot=this.compile(R,Z,ft);return new Promise(lt=>{function It(){if(ot.forEach(function(kt){ut.get(kt).currentProgram.isReady()&&ot.delete(kt)}),ot.size===0){lt(R);return}setTimeout(It,10)}Le.get("KHR_parallel_shader_compile")!==null?It():setTimeout(It,10)})};let ts=null;function Bi(R){ts&&ts(R)}function pn(){qn.stop()}function Dn(){qn.start()}const qn=new xS;qn.setAnimationLoop(Bi),typeof self<"u"&&qn.setContext(self),this.setAnimationLoop=function(R){ts=R,qt.setAnimationLoop(R),R===null?qn.stop():qn.start()},qt.addEventListener("sessionstart",pn),qt.addEventListener("sessionend",Dn),this.render=function(R,Z){if(Z!==void 0&&Z.isCamera!==!0){Re("WebGLRenderer.render: camera is not an instance of THREE.Camera.");return}if(F===!0)return;k!==null&&k.renderStart(R,Z);const ft=qt.enabled===!0&&qt.isPresenting===!0,ot=N!==null&&(at===null||ft)&&N.begin(G,at);if(R.matrixWorldAutoUpdate===!0&&R.updateMatrixWorld(),Z.parent===null&&Z.matrixWorldAutoUpdate===!0&&Z.updateMatrixWorld(),qt.enabled===!0&&qt.isPresenting===!0&&(N===null||N.isCompositing()===!1)&&(qt.cameraAutoUpdate===!0&&qt.updateCamera(Z),Z=qt.getCamera()),R.isScene===!0&&R.onBeforeRender(G,R,Z,at),D=Lt.get(R,b.length),D.init(Z),D.state.textureUnits=mt.getTextureUnits(),b.push(D),ge.multiplyMatrices(Z.projectionMatrix,Z.matrixWorldInverse),ie.setFromProjectionMatrix(ge,ji,Z.reversedDepth),ae=this.localClippingEnabled,Xt=Jt.init(this.clippingPlanes,ae),z=Pt.get(R,U.length),z.init(),U.push(z),qt.enabled===!0&&qt.isPresenting===!0){const kt=G.xr.getDepthSensingMesh();kt!==null&&gr(kt,Z,-1/0,G.sortObjects)}gr(R,Z,0,G.sortObjects),z.finish(),G.sortObjects===!0&&z.sort(bt,Ht,Z.reversedDepth),Ue=qt.enabled===!1||qt.isPresenting===!1||qt.hasDepthSensing()===!1,Ue&&oe.addToRenderList(z,R),this.info.render.frame++,this.info.autoReset===!0&&this.info.reset(),Xt===!0&&Jt.beginShadows();const lt=D.state.shadowsArray;if($t.render(lt,R,Z),Xt===!0&&Jt.endShadows(),(ot&&N.hasRenderPass())===!1){const kt=z.opaque,Ot=z.transmissive;if(D.setupLights(),Z.isArrayCamera){const Yt=Z.cameras;if(Ot.length>0)for(let Wt=0,te=Yt.length;Wt<te;Wt++){const ue=Yt[Wt];Sl(kt,Ot,R,ue)}Ue&&oe.render(R);for(let Wt=0,te=Yt.length;Wt<te;Wt++){const ue=Yt[Wt];xl(z,R,ue,ue.viewport)}}else Ot.length>0&&Sl(kt,Ot,R,Z),Ue&&oe.render(R),xl(z,R,Z)}at!==null&&X===0&&(mt.updateMultisampleRenderTarget(at),mt.updateRenderTargetMipmap(at)),ot&&N.end(G),R.isScene===!0&&R.onAfterRender(G,R,Z),Ft.resetDefaultState(),_t=-1,vt=null,b.pop(),b.length>0?(D=b[b.length-1],mt.setTextureUnits(D.state.textureUnits),Xt===!0&&Jt.setGlobalState(G.clippingPlanes,D.state.camera)):D=null,U.pop(),U.length>0?z=U[U.length-1]:z=null,k!==null&&k.renderEnd()};function gr(R,Z,ft,ot){if(R.visible===!1)return;if(R.layers.test(Z.layers)){if(R.isGroup)ft=R.renderOrder;else if(R.isLOD)R.autoUpdate===!0&&R.update(Z);else if(R.isLightProbeGrid)D.pushLightProbeGrid(R);else if(R.isLight)D.pushLight(R),R.castShadow&&D.pushShadow(R);else if(R.isSprite){if(!R.frustumCulled||ie.intersectsSprite(R)){ot&&Ce.setFromMatrixPosition(R.matrixWorld).applyMatrix4(ge);const kt=gt.update(R),Ot=R.material;Ot.visible&&z.push(R,kt,Ot,ft,Ce.z,null)}}else if((R.isMesh||R.isLine||R.isPoints)&&(!R.frustumCulled||ie.intersectsObject(R))){const kt=gt.update(R),Ot=R.material;if(ot&&(R.boundingSphere!==void 0?(R.boundingSphere===null&&R.computeBoundingSphere(),Ce.copy(R.boundingSphere.center)):(kt.boundingSphere===null&&kt.computeBoundingSphere(),Ce.copy(kt.boundingSphere.center)),Ce.applyMatrix4(R.matrixWorld).applyMatrix4(ge)),Array.isArray(Ot)){const Yt=kt.groups;for(let Wt=0,te=Yt.length;Wt<te;Wt++){const ue=Yt[Wt],Qt=Ot[ue.materialIndex];Qt&&Qt.visible&&z.push(R,kt,Qt,ft,Ce.z,ue)}}else Ot.visible&&z.push(R,kt,Ot,ft,Ce.z,null)}}const It=R.children;for(let kt=0,Ot=It.length;kt<Ot;kt++)gr(It[kt],Z,ft,ot)}function xl(R,Z,ft,ot){const{opaque:lt,transmissive:It,transparent:kt}=R;D.setupLightsView(ft),Xt===!0&&Jt.setGlobalState(G.clippingPlanes,ft),ot&&T.viewport(L.copy(ot)),lt.length>0&&_r(lt,Z,ft),It.length>0&&_r(It,Z,ft),kt.length>0&&_r(kt,Z,ft),T.buffers.depth.setTest(!0),T.buffers.depth.setMask(!0),T.buffers.color.setMask(!0),T.setPolygonOffset(!1)}function Sl(R,Z,ft,ot){if((ft.isScene===!0?ft.overrideMaterial:null)!==null)return;if(D.state.transmissionRenderTarget[ot.id]===void 0){const Qt=Le.has("EXT_color_buffer_half_float")||Le.has("EXT_color_buffer_float");D.state.transmissionRenderTarget[ot.id]=new Qi(1,1,{generateMipmaps:!0,type:Qt?Da:pi,minFilter:Xr,samples:Math.max(4,B.samples),stencilBuffer:c,resolveDepthBuffer:!1,resolveStencilBuffer:!1,colorSpace:Te.workingColorSpace})}const It=D.state.transmissionRenderTarget[ot.id],kt=ot.viewport||L;It.setSize(kt.z*G.transmissionResolutionScale,kt.w*G.transmissionResolutionScale);const Ot=G.getRenderTarget(),Yt=G.getActiveCubeFace(),Wt=G.getActiveMipmapLevel();G.setRenderTarget(It),G.getClearColor(At),Dt=G.getClearAlpha(),Dt<1&&G.setClearColor(16777215,.5),G.clear(),Ue&&oe.render(ft);const te=G.toneMapping;G.toneMapping=Ki;const ue=ot.viewport;if(ot.viewport!==void 0&&(ot.viewport=void 0),D.setupLightsView(ot),Xt===!0&&Jt.setGlobalState(G.clippingPlanes,ot),_r(R,ft,ot),mt.updateMultisampleRenderTarget(It),mt.updateRenderTargetMipmap(It),Le.has("WEBGL_multisampled_render_to_texture")===!1){let Qt=!1;for(let we=0,en=Z.length;we<en;we++){const Ze=Z[we],{object:Ie,geometry:ze,material:Vt,group:In}=Ze;if(Vt.side===Yi&&Ie.layers.test(ot.layers)){const ve=Vt.side;Vt.side=Jn,Vt.needsUpdate=!0,Ua(Ie,ft,ot,ze,Vt,In),Vt.side=ve,Vt.needsUpdate=!0,Qt=!0}}Qt===!0&&(mt.updateMultisampleRenderTarget(It),mt.updateRenderTargetMipmap(It))}G.setRenderTarget(Ot,Yt,Wt),G.setClearColor(At,Dt),ue!==void 0&&(ot.viewport=ue),G.toneMapping=te}function _r(R,Z,ft){const ot=Z.isScene===!0?Z.overrideMaterial:null;for(let lt=0,It=R.length;lt<It;lt++){const kt=R[lt],{object:Ot,geometry:Yt,group:Wt}=kt;let te=kt.material;te.allowOverride===!0&&ot!==null&&(te=ot),Ot.layers.test(ft.layers)&&Ua(Ot,Z,ft,Yt,te,Wt)}}function Ua(R,Z,ft,ot,lt,It){R.onBeforeRender(G,Z,ft,ot,lt,It),R.modelViewMatrix.multiplyMatrices(ft.matrixWorldInverse,R.matrixWorld),R.normalMatrix.getNormalMatrix(R.modelViewMatrix),lt.onBeforeRender(G,Z,ft,ot,R,It),lt.transparent===!0&&lt.side===Yi&&lt.forceSinglePass===!1?(lt.side=Jn,lt.needsUpdate=!0,G.renderBufferDirect(ft,Z,ot,lt,R,It),lt.side=pr,lt.needsUpdate=!0,G.renderBufferDirect(ft,Z,ot,lt,R,It),lt.side=Yi):G.renderBufferDirect(ft,Z,ot,lt,R,It),R.onAfterRender(G,Z,ft,ot,lt,It)}function La(R,Z,ft){Z.isScene!==!0&&(Z=Ke);const ot=ut.get(R),lt=D.state.lights,It=D.state.shadowsArray,kt=lt.state.version,Ot=wt.getParameters(R,lt.state,It,Z,ft,D.state.lightProbeGridArray),Yt=wt.getProgramCacheKey(Ot);let Wt=ot.programs;ot.environment=R.isMeshStandardMaterial||R.isMeshLambertMaterial||R.isMeshPhongMaterial?Z.environment:null,ot.fog=Z.fog;const te=R.isMeshStandardMaterial||R.isMeshLambertMaterial&&!R.envMap||R.isMeshPhongMaterial&&!R.envMap;ot.envMap=Rt.get(R.envMap||ot.environment,te),ot.envMapRotation=ot.environment!==null&&R.envMap===null?Z.environmentRotation:R.envMapRotation,Wt===void 0&&(R.addEventListener("dispose",ei),Wt=new Map,ot.programs=Wt);let ue=Wt.get(Yt);if(ue!==void 0){if(ot.currentProgram===ue&&ot.lightsStateVersion===kt)return na(R,Ot),ue}else Ot.uniforms=wt.getUniforms(R),k!==null&&R.isNodeMaterial&&k.build(R,ft,Ot),R.onBeforeCompile(Ot,G),ue=wt.acquireProgram(Ot,Yt),Wt.set(Yt,ue),ot.uniforms=Ot.uniforms;const Qt=ot.uniforms;return(!R.isShaderMaterial&&!R.isRawShaderMaterial||R.clipping===!0)&&(Qt.clippingPlanes=Jt.uniform),na(R,Ot),ot.needsLights=yl(R),ot.lightsStateVersion=kt,ot.needsLights&&(Qt.ambientLightColor.value=lt.state.ambient,Qt.lightProbe.value=lt.state.probe,Qt.directionalLights.value=lt.state.directional,Qt.directionalLightShadows.value=lt.state.directionalShadow,Qt.spotLights.value=lt.state.spot,Qt.spotLightShadows.value=lt.state.spotShadow,Qt.rectAreaLights.value=lt.state.rectArea,Qt.ltc_1.value=lt.state.rectAreaLTC1,Qt.ltc_2.value=lt.state.rectAreaLTC2,Qt.pointLights.value=lt.state.point,Qt.pointLightShadows.value=lt.state.pointShadow,Qt.hemisphereLights.value=lt.state.hemi,Qt.directionalShadowMatrix.value=lt.state.directionalShadowMatrix,Qt.spotLightMatrix.value=lt.state.spotLightMatrix,Qt.spotLightMap.value=lt.state.spotLightMap,Qt.pointShadowMatrix.value=lt.state.pointShadowMatrix),ot.lightProbeGrid=D.state.lightProbeGridArray.length>0,ot.currentProgram=ue,ot.uniformsList=null,ue}function ea(R){if(R.uniformsList===null){const Z=R.currentProgram.getUniforms();R.uniformsList=fu.seqWithValue(Z.seq,R.uniforms)}return R.uniformsList}function na(R,Z){const ft=ut.get(R);ft.outputColorSpace=Z.outputColorSpace,ft.batching=Z.batching,ft.batchingColor=Z.batchingColor,ft.instancing=Z.instancing,ft.instancingColor=Z.instancingColor,ft.instancingMorph=Z.instancingMorph,ft.skinning=Z.skinning,ft.morphTargets=Z.morphTargets,ft.morphNormals=Z.morphNormals,ft.morphColors=Z.morphColors,ft.morphTargetsCount=Z.morphTargetsCount,ft.numClippingPlanes=Z.numClippingPlanes,ft.numIntersection=Z.numClipIntersection,ft.vertexAlphas=Z.vertexAlphas,ft.vertexTangents=Z.vertexTangents,ft.toneMapping=Z.toneMapping}function vr(R,Z){if(R.length===0)return null;if(R.length===1)return R[0].texture!==null?R[0]:null;C.setFromMatrixPosition(Z.matrixWorld);for(let ft=0,ot=R.length;ft<ot;ft++){const lt=R[ft];if(lt.texture!==null&&lt.boundingBox.containsPoint(C))return lt}return null}function Oa(R,Z,ft,ot,lt){Z.isScene!==!0&&(Z=Ke),mt.resetTextureUnits();const It=Z.fog,kt=ot.isMeshStandardMaterial||ot.isMeshLambertMaterial||ot.isMeshPhongMaterial?Z.environment:null,Ot=at===null?G.outputColorSpace:at.isXRRenderTarget===!0?at.texture.colorSpace:Te.workingColorSpace,Yt=ot.isMeshStandardMaterial||ot.isMeshLambertMaterial&&!ot.envMap||ot.isMeshPhongMaterial&&!ot.envMap,Wt=Rt.get(ot.envMap||kt,Yt),te=ot.vertexColors===!0&&!!ft.attributes.color&&ft.attributes.color.itemSize===4,ue=!!ft.attributes.tangent&&(!!ot.normalMap||ot.anisotropy>0),Qt=!!ft.morphAttributes.position,we=!!ft.morphAttributes.normal,en=!!ft.morphAttributes.color;let Ze=Ki;ot.toneMapped&&(at===null||at.isXRRenderTarget===!0)&&(Ze=G.toneMapping);const Ie=ft.morphAttributes.position||ft.morphAttributes.normal||ft.morphAttributes.color,ze=Ie!==void 0?Ie.length:0,Vt=ut.get(ot),In=D.state.lights;if(Xt===!0&&(ae===!0||R!==vt)){const Pe=R===vt&&ot.id===_t;Jt.setState(ot,R,Pe)}let ve=!1;ot.version===Vt.__version?(Vt.needsLights&&Vt.lightsStateVersion!==In.state.version||Vt.outputColorSpace!==Ot||lt.isBatchedMesh&&Vt.batching===!1||!lt.isBatchedMesh&&Vt.batching===!0||lt.isBatchedMesh&&Vt.batchingColor===!0&&lt.colorTexture===null||lt.isBatchedMesh&&Vt.batchingColor===!1&&lt.colorTexture!==null||lt.isInstancedMesh&&Vt.instancing===!1||!lt.isInstancedMesh&&Vt.instancing===!0||lt.isSkinnedMesh&&Vt.skinning===!1||!lt.isSkinnedMesh&&Vt.skinning===!0||lt.isInstancedMesh&&Vt.instancingColor===!0&&lt.instanceColor===null||lt.isInstancedMesh&&Vt.instancingColor===!1&&lt.instanceColor!==null||lt.isInstancedMesh&&Vt.instancingMorph===!0&&lt.morphTexture===null||lt.isInstancedMesh&&Vt.instancingMorph===!1&&lt.morphTexture!==null||Vt.envMap!==Wt||ot.fog===!0&&Vt.fog!==It||Vt.numClippingPlanes!==void 0&&(Vt.numClippingPlanes!==Jt.numPlanes||Vt.numIntersection!==Jt.numIntersection)||Vt.vertexAlphas!==te||Vt.vertexTangents!==ue||Vt.morphTargets!==Qt||Vt.morphNormals!==we||Vt.morphColors!==en||Vt.toneMapping!==Ze||Vt.morphTargetsCount!==ze||!!Vt.lightProbeGrid!=D.state.lightProbeGridArray.length>0)&&(ve=!0):(ve=!0,Vt.__version=ot.version);let yn=Vt.currentProgram;ve===!0&&(yn=La(ot,Z,lt),k&&ot.isNodeMaterial&&k.onUpdateProgram(ot,yn,Vt));let ni=!1,Ai=!1,ii=!1;const Fe=yn.getUniforms(),nn=Vt.uniforms;if(T.useProgram(yn.program)&&(ni=!0,Ai=!0,ii=!0),ot.id!==_t&&(_t=ot.id,Ai=!0),Vt.needsLights){const Pe=vr(D.state.lightProbeGridArray,lt);Vt.lightProbeGrid!==Pe&&(Vt.lightProbeGrid=Pe,Ai=!0)}if(ni||vt!==R){T.buffers.depth.getReversed()&&R.reversedDepth!==!0&&(R._reversedDepth=!0,R.updateProjectionMatrix()),Fe.setValue(j,"projectionMatrix",R.projectionMatrix),Fe.setValue(j,"viewMatrix",R.matrixWorldInverse);const Ii=Fe.map.cameraPosition;Ii!==void 0&&Ii.setValue(j,_e.setFromMatrixPosition(R.matrixWorld)),B.logarithmicDepthBuffer&&Fe.setValue(j,"logDepthBufFC",2/(Math.log(R.far+1)/Math.LN2)),(ot.isMeshPhongMaterial||ot.isMeshToonMaterial||ot.isMeshLambertMaterial||ot.isMeshBasicMaterial||ot.isMeshStandardMaterial||ot.isShaderMaterial)&&Fe.setValue(j,"isOrthographic",R.isOrthographicCamera===!0),vt!==R&&(vt=R,Ai=!0,ii=!0)}if(Vt.needsLights&&(In.state.directionalShadowMap.length>0&&Fe.setValue(j,"directionalShadowMap",In.state.directionalShadowMap,mt),In.state.spotShadowMap.length>0&&Fe.setValue(j,"spotShadowMap",In.state.spotShadowMap,mt),In.state.pointShadowMap.length>0&&Fe.setValue(j,"pointShadowMap",In.state.pointShadowMap,mt)),lt.isSkinnedMesh){Fe.setOptional(j,lt,"bindMatrix"),Fe.setOptional(j,lt,"bindMatrixInverse");const Pe=lt.skeleton;Pe&&(Pe.boneTexture===null&&Pe.computeBoneTexture(),Fe.setValue(j,"boneTexture",Pe.boneTexture,mt))}lt.isBatchedMesh&&(Fe.setOptional(j,lt,"batchingTexture"),Fe.setValue(j,"batchingTexture",lt._matricesTexture,mt),Fe.setOptional(j,lt,"batchingIdTexture"),Fe.setValue(j,"batchingIdTexture",lt._indirectTexture,mt),Fe.setOptional(j,lt,"batchingColorTexture"),lt._colorsTexture!==null&&Fe.setValue(j,"batchingColorTexture",lt._colorsTexture,mt));const Ri=ft.morphAttributes;if((Ri.position!==void 0||Ri.normal!==void 0||Ri.color!==void 0)&&W.update(lt,ft,yn),(Ai||Vt.receiveShadow!==lt.receiveShadow)&&(Vt.receiveShadow=lt.receiveShadow,Fe.setValue(j,"receiveShadow",lt.receiveShadow)),(ot.isMeshStandardMaterial||ot.isMeshLambertMaterial||ot.isMeshPhongMaterial)&&ot.envMap===null&&Z.environment!==null&&(nn.envMapIntensity.value=Z.environmentIntensity),nn.dfgLUT!==void 0&&(nn.dfgLUT.value=bR()),Ai){if(Fe.setValue(j,"toneMappingExposure",G.toneMappingExposure),Vt.needsLights&&mn(nn,ii),It&&ot.fog===!0&&Gt.refreshFogUniforms(nn,It),Gt.refreshMaterialUniforms(nn,ot,ct,nt,D.state.transmissionRenderTarget[R.id]),Vt.needsLights&&Vt.lightProbeGrid){const Pe=Vt.lightProbeGrid;nn.probesSH.value=Pe.texture,nn.probesMin.value.copy(Pe.boundingBox.min),nn.probesMax.value.copy(Pe.boundingBox.max),nn.probesResolution.value.copy(Pe.resolution)}fu.upload(j,ea(Vt),nn,mt)}if(ot.isShaderMaterial&&ot.uniformsNeedUpdate===!0&&(fu.upload(j,ea(Vt),nn,mt),ot.uniformsNeedUpdate=!1),ot.isSpriteMaterial&&Fe.setValue(j,"center",lt.center),Fe.setValue(j,"modelViewMatrix",lt.modelViewMatrix),Fe.setValue(j,"normalMatrix",lt.normalMatrix),Fe.setValue(j,"modelMatrix",lt.matrixWorld),ot.uniformsGroups!==void 0){const Pe=ot.uniformsGroups;for(let Ii=0,Pa=Pe.length;Ii<Pa;Ii++){const xr=Pe[Ii];Tt.update(xr,yn),Tt.bind(xr,yn)}}return yn}function mn(R,Z){R.ambientLightColor.needsUpdate=Z,R.lightProbe.needsUpdate=Z,R.directionalLights.needsUpdate=Z,R.directionalLightShadows.needsUpdate=Z,R.pointLights.needsUpdate=Z,R.pointLightShadows.needsUpdate=Z,R.spotLights.needsUpdate=Z,R.spotLightShadows.needsUpdate=Z,R.rectAreaLights.needsUpdate=Z,R.hemisphereLights.needsUpdate=Z}function yl(R){return R.isMeshLambertMaterial||R.isMeshToonMaterial||R.isMeshPhongMaterial||R.isMeshStandardMaterial||R.isShadowMaterial||R.isShaderMaterial&&R.lights===!0}this.getActiveCubeFace=function(){return I},this.getActiveMipmapLevel=function(){return X},this.getRenderTarget=function(){return at},this.setRenderTargetTextures=function(R,Z,ft){const ot=ut.get(R);ot.__autoAllocateDepthBuffer=R.resolveDepthBuffer===!1,ot.__autoAllocateDepthBuffer===!1&&(ot.__useRenderToTexture=!1),ut.get(R.texture).__webglTexture=Z,ut.get(R.depthTexture).__webglTexture=ot.__autoAllocateDepthBuffer?void 0:ft,ot.__hasExternalTextures=!0},this.setRenderTargetFramebuffer=function(R,Z){const ft=ut.get(R);ft.__webglFramebuffer=Z,ft.__useDefaultFramebuffer=Z===void 0},this.setRenderTarget=function(R,Z=0,ft=0){at=R,I=Z,X=ft;let ot=null,lt=!1,It=!1;if(R){const Ot=ut.get(R);if(Ot.__useDefaultFramebuffer!==void 0){T.bindFramebuffer(j.FRAMEBUFFER,Ot.__webglFramebuffer),L.copy(R.viewport),Y.copy(R.scissor),yt=R.scissorTest,T.viewport(L),T.scissor(Y),T.setScissorTest(yt),_t=-1;return}else if(Ot.__webglFramebuffer===void 0)mt.setupRenderTarget(R);else if(Ot.__hasExternalTextures)mt.rebindTextures(R,ut.get(R.texture).__webglTexture,ut.get(R.depthTexture).__webglTexture);else if(R.depthBuffer){const te=R.depthTexture;if(Ot.__boundDepthTexture!==te){if(te!==null&&ut.has(te)&&(R.width!==te.image.width||R.height!==te.image.height))throw new Error("THREE.WebGLRenderer: Attached DepthTexture is initialized to the incorrect size.");mt.setupDepthRenderbuffer(R)}}const Yt=R.texture;(Yt.isData3DTexture||Yt.isDataArrayTexture||Yt.isCompressedArrayTexture)&&(It=!0);const Wt=ut.get(R).__webglFramebuffer;R.isWebGLCubeRenderTarget?(Array.isArray(Wt[Z])?ot=Wt[Z][ft]:ot=Wt[Z],lt=!0):R.samples>0&&mt.useMultisampledRTT(R)===!1?ot=ut.get(R).__webglMultisampledFramebuffer:Array.isArray(Wt)?ot=Wt[ft]:ot=Wt,L.copy(R.viewport),Y.copy(R.scissor),yt=R.scissorTest}else L.copy(Bt).multiplyScalar(ct).floor(),Y.copy(he).multiplyScalar(ct).floor(),yt=ne;if(ft!==0&&(ot=K),T.bindFramebuffer(j.FRAMEBUFFER,ot)&&T.drawBuffers(R,ot),T.viewport(L),T.scissor(Y),T.setScissorTest(yt),lt){const Ot=ut.get(R.texture);j.framebufferTexture2D(j.FRAMEBUFFER,j.COLOR_ATTACHMENT0,j.TEXTURE_CUBE_MAP_POSITIVE_X+Z,Ot.__webglTexture,ft)}else if(It){const Ot=Z;for(let Yt=0;Yt<R.textures.length;Yt++){const Wt=ut.get(R.textures[Yt]);j.framebufferTextureLayer(j.FRAMEBUFFER,j.COLOR_ATTACHMENT0+Yt,Wt.__webglTexture,ft,Ot)}}else if(R!==null&&ft!==0){const Ot=ut.get(R.texture);j.framebufferTexture2D(j.FRAMEBUFFER,j.COLOR_ATTACHMENT0,j.TEXTURE_2D,Ot.__webglTexture,ft)}_t=-1},this.readRenderTargetPixels=function(R,Z,ft,ot,lt,It,kt,Ot=0){if(!(R&&R.isWebGLRenderTarget)){Re("WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");return}let Yt=ut.get(R).__webglFramebuffer;if(R.isWebGLCubeRenderTarget&&kt!==void 0&&(Yt=Yt[kt]),Yt){T.bindFramebuffer(j.FRAMEBUFFER,Yt);try{const Wt=R.textures[Ot],te=Wt.format,ue=Wt.type;if(R.textures.length>1&&j.readBuffer(j.COLOR_ATTACHMENT0+Ot),!B.textureFormatReadable(te)){Re("WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");return}if(!B.textureTypeReadable(ue)){Re("WebGLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");return}Z>=0&&Z<=R.width-ot&&ft>=0&&ft<=R.height-lt&&j.readPixels(Z,ft,ot,lt,Nt.convert(te),Nt.convert(ue),It)}finally{const Wt=at!==null?ut.get(at).__webglFramebuffer:null;T.bindFramebuffer(j.FRAMEBUFFER,Wt)}}},this.readRenderTargetPixelsAsync=async function(R,Z,ft,ot,lt,It,kt,Ot=0){if(!(R&&R.isWebGLRenderTarget))throw new Error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");let Yt=ut.get(R).__webglFramebuffer;if(R.isWebGLCubeRenderTarget&&kt!==void 0&&(Yt=Yt[kt]),Yt)if(Z>=0&&Z<=R.width-ot&&ft>=0&&ft<=R.height-lt){T.bindFramebuffer(j.FRAMEBUFFER,Yt);const Wt=R.textures[Ot],te=Wt.format,ue=Wt.type;if(R.textures.length>1&&j.readBuffer(j.COLOR_ATTACHMENT0+Ot),!B.textureFormatReadable(te))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in RGBA or implementation defined format.");if(!B.textureTypeReadable(ue))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in UnsignedByteType or implementation defined type.");const Qt=j.createBuffer();j.bindBuffer(j.PIXEL_PACK_BUFFER,Qt),j.bufferData(j.PIXEL_PACK_BUFFER,It.byteLength,j.STREAM_READ),j.readPixels(Z,ft,ot,lt,Nt.convert(te),Nt.convert(ue),0);const we=at!==null?ut.get(at).__webglFramebuffer:null;T.bindFramebuffer(j.FRAMEBUFFER,we);const en=j.fenceSync(j.SYNC_GPU_COMMANDS_COMPLETE,0);return j.flush(),await SE(j,en,4),j.bindBuffer(j.PIXEL_PACK_BUFFER,Qt),j.getBufferSubData(j.PIXEL_PACK_BUFFER,0,It),j.deleteBuffer(Qt),j.deleteSync(en),It}else throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: requested read bounds are out of range.")},this.copyFramebufferToTexture=function(R,Z=null,ft=0){const ot=Math.pow(2,-ft),lt=Math.floor(R.image.width*ot),It=Math.floor(R.image.height*ot),kt=Z!==null?Z.x:0,Ot=Z!==null?Z.y:0;mt.setTexture2D(R,0),j.copyTexSubImage2D(j.TEXTURE_2D,ft,0,0,kt,Ot,lt,It),T.unbindTexture()},this.copyTextureToTexture=function(R,Z,ft=null,ot=null,lt=0,It=0){let kt,Ot,Yt,Wt,te,ue,Qt,we,en;const Ze=R.isCompressedTexture?R.mipmaps[It]:R.image;if(ft!==null)kt=ft.max.x-ft.min.x,Ot=ft.max.y-ft.min.y,Yt=ft.isBox3?ft.max.z-ft.min.z:1,Wt=ft.min.x,te=ft.min.y,ue=ft.isBox3?ft.min.z:0;else{const nn=Math.pow(2,-lt);kt=Math.floor(Ze.width*nn),Ot=Math.floor(Ze.height*nn),R.isDataArrayTexture?Yt=Ze.depth:R.isData3DTexture?Yt=Math.floor(Ze.depth*nn):Yt=1,Wt=0,te=0,ue=0}ot!==null?(Qt=ot.x,we=ot.y,en=ot.z):(Qt=0,we=0,en=0);const Ie=Nt.convert(Z.format),ze=Nt.convert(Z.type);let Vt;Z.isData3DTexture?(mt.setTexture3D(Z,0),Vt=j.TEXTURE_3D):Z.isDataArrayTexture||Z.isCompressedArrayTexture?(mt.setTexture2DArray(Z,0),Vt=j.TEXTURE_2D_ARRAY):(mt.setTexture2D(Z,0),Vt=j.TEXTURE_2D),T.activeTexture(j.TEXTURE0),T.pixelStorei(j.UNPACK_FLIP_Y_WEBGL,Z.flipY),T.pixelStorei(j.UNPACK_PREMULTIPLY_ALPHA_WEBGL,Z.premultiplyAlpha),T.pixelStorei(j.UNPACK_ALIGNMENT,Z.unpackAlignment);const In=T.getParameter(j.UNPACK_ROW_LENGTH),ve=T.getParameter(j.UNPACK_IMAGE_HEIGHT),yn=T.getParameter(j.UNPACK_SKIP_PIXELS),ni=T.getParameter(j.UNPACK_SKIP_ROWS),Ai=T.getParameter(j.UNPACK_SKIP_IMAGES);T.pixelStorei(j.UNPACK_ROW_LENGTH,Ze.width),T.pixelStorei(j.UNPACK_IMAGE_HEIGHT,Ze.height),T.pixelStorei(j.UNPACK_SKIP_PIXELS,Wt),T.pixelStorei(j.UNPACK_SKIP_ROWS,te),T.pixelStorei(j.UNPACK_SKIP_IMAGES,ue);const ii=R.isDataArrayTexture||R.isData3DTexture,Fe=Z.isDataArrayTexture||Z.isData3DTexture;if(R.isDepthTexture){const nn=ut.get(R),Ri=ut.get(Z),Pe=ut.get(nn.__renderTarget),Ii=ut.get(Ri.__renderTarget);T.bindFramebuffer(j.READ_FRAMEBUFFER,Pe.__webglFramebuffer),T.bindFramebuffer(j.DRAW_FRAMEBUFFER,Ii.__webglFramebuffer);for(let Pa=0;Pa<Yt;Pa++)ii&&(j.framebufferTextureLayer(j.READ_FRAMEBUFFER,j.COLOR_ATTACHMENT0,ut.get(R).__webglTexture,lt,ue+Pa),j.framebufferTextureLayer(j.DRAW_FRAMEBUFFER,j.COLOR_ATTACHMENT0,ut.get(Z).__webglTexture,It,en+Pa)),j.blitFramebuffer(Wt,te,kt,Ot,Qt,we,kt,Ot,j.DEPTH_BUFFER_BIT,j.NEAREST);T.bindFramebuffer(j.READ_FRAMEBUFFER,null),T.bindFramebuffer(j.DRAW_FRAMEBUFFER,null)}else if(lt!==0||R.isRenderTargetTexture||ut.has(R)){const nn=ut.get(R),Ri=ut.get(Z);T.bindFramebuffer(j.READ_FRAMEBUFFER,tt),T.bindFramebuffer(j.DRAW_FRAMEBUFFER,$);for(let Pe=0;Pe<Yt;Pe++)ii?j.framebufferTextureLayer(j.READ_FRAMEBUFFER,j.COLOR_ATTACHMENT0,nn.__webglTexture,lt,ue+Pe):j.framebufferTexture2D(j.READ_FRAMEBUFFER,j.COLOR_ATTACHMENT0,j.TEXTURE_2D,nn.__webglTexture,lt),Fe?j.framebufferTextureLayer(j.DRAW_FRAMEBUFFER,j.COLOR_ATTACHMENT0,Ri.__webglTexture,It,en+Pe):j.framebufferTexture2D(j.DRAW_FRAMEBUFFER,j.COLOR_ATTACHMENT0,j.TEXTURE_2D,Ri.__webglTexture,It),lt!==0?j.blitFramebuffer(Wt,te,kt,Ot,Qt,we,kt,Ot,j.COLOR_BUFFER_BIT,j.NEAREST):Fe?j.copyTexSubImage3D(Vt,It,Qt,we,en+Pe,Wt,te,kt,Ot):j.copyTexSubImage2D(Vt,It,Qt,we,Wt,te,kt,Ot);T.bindFramebuffer(j.READ_FRAMEBUFFER,null),T.bindFramebuffer(j.DRAW_FRAMEBUFFER,null)}else Fe?R.isDataTexture||R.isData3DTexture?j.texSubImage3D(Vt,It,Qt,we,en,kt,Ot,Yt,Ie,ze,Ze.data):Z.isCompressedArrayTexture?j.compressedTexSubImage3D(Vt,It,Qt,we,en,kt,Ot,Yt,Ie,Ze.data):j.texSubImage3D(Vt,It,Qt,we,en,kt,Ot,Yt,Ie,ze,Ze):R.isDataTexture?j.texSubImage2D(j.TEXTURE_2D,It,Qt,we,kt,Ot,Ie,ze,Ze.data):R.isCompressedTexture?j.compressedTexSubImage2D(j.TEXTURE_2D,It,Qt,we,Ze.width,Ze.height,Ie,Ze.data):j.texSubImage2D(j.TEXTURE_2D,It,Qt,we,kt,Ot,Ie,ze,Ze);T.pixelStorei(j.UNPACK_ROW_LENGTH,In),T.pixelStorei(j.UNPACK_IMAGE_HEIGHT,ve),T.pixelStorei(j.UNPACK_SKIP_PIXELS,yn),T.pixelStorei(j.UNPACK_SKIP_ROWS,ni),T.pixelStorei(j.UNPACK_SKIP_IMAGES,Ai),It===0&&Z.generateMipmaps&&j.generateMipmap(Vt),T.unbindTexture()},this.initRenderTarget=function(R){ut.get(R).__webglFramebuffer===void 0&&mt.setupRenderTarget(R)},this.initTexture=function(R){R.isCubeTexture?mt.setTextureCube(R,0):R.isData3DTexture?mt.setTexture3D(R,0):R.isDataArrayTexture||R.isCompressedArrayTexture?mt.setTexture2DArray(R,0):mt.setTexture2D(R,0),T.unbindTexture()},this.resetState=function(){I=0,X=0,at=null,T.reset(),Ft.reset()},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}get coordinateSystem(){return ji}get outputColorSpace(){return this._outputColorSpace}set outputColorSpace(t){this._outputColorSpace=t;const i=this.getContext();i.drawingBufferColorSpace=Te._getDrawingBufferColorSpace(t),i.unpackColorSpace=Te._getUnpackColorSpace()}}const AR=""+new URL("jixin-guokong-logo-C3h33Xyg.gif",import.meta.url).href,RR={columns:9,rows:5,cellWidth:.56,cellHeight:.48,gap:.045,depth:.54},CR=[{id:"A",position:[0,0,-4.18],rotationY:0,accent:3185151,shell:596522,door:794932,screenSpan:{column:4,rowStart:1,rowEnd:2}},{id:"B",position:[-5.5,0,-1.08],rotationY:Math.PI/2,accent:2934967,shell:663585,door:862507},{id:"C",position:[5.5,0,-1.08],rotationY:-Math.PI/2,accent:9991423,shell:1512235,door:2169147}];function wR(s){return Math.max(0,Math.min(1,s))}function DR(s){const t=wR(s);return t*t*(3-2*t)}function ux({accent:s="#3099ff",detail:t,muted:i=!1,statusColor:r,title:l}){const c=document.createElement("canvas");c.width=512,c.height=176;const f=c.getContext("2d");f.clearRect(0,0,c.width,c.height),f.fillStyle=i?"rgba(5, 12, 20, .72)":"rgba(5, 14, 24, .94)",f.fillRect(5,5,502,166),r&&(f.fillStyle=r,f.fillRect(16,14,480,12)),f.strokeStyle=i?"rgba(90, 116, 138, .28)":s,f.lineWidth=i?2:4,f.strokeRect(7,7,498,162),f.textAlign="center",f.fillStyle=i?"#698096":"#e3f2ff",f.font="700 45px Microsoft YaHei, sans-serif",f.fillText(l,256,t?78:104),t&&(f.fillStyle=i?"#51677a":r||"#7fa9c8",f.font="500 26px Microsoft YaHei, sans-serif",f.fillText(t,256,128));const h=new pS(c);return h.colorSpace=Gn,h.anisotropy=4,h}function NR(){const s=document.createElement("canvas");s.width=640,s.height=1024;const t=s.getContext("2d");t.fillStyle="#07182a",t.fillRect(0,0,s.width,s.height),t.strokeStyle="#4bb0ff",t.lineWidth=7,t.strokeRect(18,18,604,988),t.strokeStyle="rgba(83, 184, 255, .28)",t.lineWidth=2;for(let r=88;r<940;r+=74)t.beginPath(),t.moveTo(44,r),t.lineTo(596,r),t.stroke();t.fillStyle="#5db9ff",t.font="600 30px Segoe UI, sans-serif",t.textAlign="center",t.fillText("JIXIN GUOKONG",320,192),t.fillStyle="#eef8ff",t.font="700 86px Microsoft YaHei, sans-serif",t.fillText("集信国控",320,302),t.fillStyle="#7296b8",t.font="500 27px Microsoft YaHei, sans-serif",t.fillText("仪器设备智慧管理中心",320,774),t.fillStyle="#4f7392",t.font="500 22px Segoe UI, sans-serif",t.fillText("INSTRUMENT DIGITAL TWIN",320,832);const i=new pS(s);return i.colorSpace=Gn,i.anisotropy=4,i}function vu(s,t,i,r=1){return new Ye(new Ji(t,i),new Ta({map:s,opacity:r,toneMapped:!1,transparent:!0}))}function tm({color:s,depth:t,height:i,opacity:r=.72,width:l}){const c=new En(l,i,t),f=new lb(c);c.dispose();const h=new Zr({blending:Oi,color:s,opacity:r,transparent:!0});return{edges:new dS(f,h),material:h}}function UR({accent:s,cellHeight:t,cellWidth:i,depth:r,panelMaterial:l}){const c=new Ra,f=r-.11,h=.025,m=new Ye(new En(i-.065,t-.065,h),l);m.position.z=-f/2,c.add(m),[-1,1].forEach(v=>{const g=new Ye(new En(h,t-.065,f),l);g.position.set(v*(i/2-.042),0,-.015),c.add(g)}),[-1,1].forEach(v=>{const g=new Ye(new En(i-.065,h,f),l);g.position.set(0,v*(t/2-.042),-.015),c.add(g)});const{edges:p}=tm({color:s,depth:f,height:t-.055,opacity:.2,width:i-.055});p.position.z=-.015,c.add(p);const _=new Zr({color:s,opacity:.1,transparent:!0});return[-.18,0,.18].forEach(v=>{const g=new Kp(new bn().setFromPoints([new Q(-i*.34,v*t,-f/2+.018),new Q(i*.34,v*t,-f/2+.018)]),_);c.add(g)}),c}function fx({accent:s,cellHeight:t,cellWidth:i,doorMaterial:r,lineOpacity:l=.72}){const c=new Ra,f=new Ye(new En(i,t,.032),r);c.add(f);const{edges:h}=tm({color:s,depth:.036,height:t,opacity:l,width:i});h.position.z=.003,c.add(h);const m=new Zr({color:s,opacity:l*.3,transparent:!0}),p=new Cp(new bn().setFromPoints([new Q(-i*.39,-t*.34,0),new Q(i*.39,-t*.34,0),new Q(i*.39,t*.34,0),new Q(-i*.39,t*.34,0)]),m);return p.position.z=.02,c.add(p),c}function LR({accent:s,cellHeight:t,cellWidth:i,doorZ:r}){const l=new Ra,c=new $s({color:662576,emissive:935039,emissiveIntensity:.42,metalness:.58,roughness:.25}),f=new Ye(new En(i,t,.07),c);l.add(f);const h=vu(NR(),i-.07,t-.07);h.position.z=.041,l.add(h);const m=new xb().load(AR);m.colorSpace=Gn,m.generateMipmaps=!1,m.minFilter=wn,m.magFilter=wn;const p=i*.58,_=p/1.8175,v=vu(m,p,_);v.position.set(0,.02,.092),l.add(v);const g=new Ta({blending:Oi,color:s,depthWrite:!1,opacity:.2,transparent:!0}),x=new Ye(new Ji(i-.09,.018),g);x.position.z=.047,l.add(x);const M=new uu(s,.18,1.55,2);return M.position.set(0,0,.32),l.add(M),l.position.z=r,{group:l,parts:{cellHeight:t,glow:M,scan:x,scanMaterial:g}}}function OR(s,t,i){const r=s.screenSpan;return!!(r&&t===r.column&&i>=r.rowStart&&i<=r.rowEnd)}function PR(s,t){const i=new Ra;i.position.fromArray(s.position),i.rotation.y=s.rotationY;const{cellHeight:r,cellWidth:l,columns:c,depth:f,gap:h,rows:m}=RR,p=c*l+(c+1)*h,_=m*r+(m+1)*h,v=.12,g=.12,x=v+_+g,M=f/2+.035,A=`#${s.accent.toString(16).padStart(6,"0")}`,E=new $s({color:s.shell,depthWrite:!1,emissive:s.accent,emissiveIntensity:.035,metalness:.72,opacity:.94,roughness:.4,transparent:!0}),S=new $s({color:132877,depthWrite:!1,emissive:s.accent,emissiveIntensity:.025,metalness:.48,opacity:.93,roughness:.7,transparent:!0}),P=new $s({color:s.door,depthWrite:!1,emissive:s.accent,emissiveIntensity:.035,metalness:.7,opacity:.95,roughness:.42,transparent:!0}),O=new Ye(new En(p+.16,x,.045),E);O.position.set(0,x/2,-f/2),i.add(O),[-1,1].forEach(vt=>{const L=new Ye(new En(.05,x,f),E);L.position.set(vt*(p/2+.05),x/2,0),i.add(L)});const C=new Ye(new En(p+.16,v,f+.05),E);C.position.set(0,v/2,0),i.add(C);const z=new Ye(new En(p+.16,g,f+.05),E);z.position.set(0,x-g/2,0),i.add(z);const D=tm({color:s.accent,depth:f+.08,height:x,opacity:.58,width:p+.18});D.edges.position.set(0,x/2,0),i.add(D.edges);const U=new dn;U.position.set(0,v+_/2,M),i.add(U);const b=vt=>v+h+(m-vt-.5)*(r+h),N=[];let G=null;for(let vt=0;vt<m;vt+=1)for(let L=0;L<c;L+=1){const Y=-p/2+h+l/2+L*(l+h),yt=b(vt);if(OR(s,L,vt)){if(vt===s.screenSpan.rowStart){const _e=s.screenSpan.rowEnd-s.screenSpan.rowStart+1,Ce=_e*r+(_e-1)*h-.02,Ke=(b(s.screenSpan.rowStart)+b(s.screenSpan.rowEnd))/2,Ue=LR({accent:s.accent,cellHeight:Ce,cellWidth:l-.02,doorZ:M});Ue.group.position.x=Y,Ue.group.position.y=Ke,i.add(Ue.group),G=Ue.parts}continue}const At=t.get(`${s.id}-${L}-${vt}`),Dt=UR({accent:s.accent,cellHeight:r-.025,cellWidth:l-.025,depth:f,panelMaterial:S});Dt.position.set(Y,yt,0),i.add(Dt);const it=l-.02,nt=r-.02,ct=(At==null?void 0:At.lockerNo)||`${s.id}-${String(L+1).padStart(2,"0")}-${String(vt+1).padStart(2,"0")}`;if(!At){const _e=fx({accent:s.accent,cellHeight:nt,cellWidth:it,doorMaterial:P,lineOpacity:.38});_e.position.set(Y,yt,M),i.add(_e);const Ce=vu(ux({accent:A,muted:!0,title:ct}),it*.52,nt*.17,.75);Ce.position.set(0,-nt*.23,.025),_e.add(Ce);continue}const bt=new Ra;bt.position.set(Y-it/2,yt,M),i.add(bt);const Ht=fx({accent:s.accent,cellHeight:nt,cellWidth:it,doorMaterial:P,lineOpacity:.76});Ht.position.x=it/2,bt.add(Ht);const Bt=vu(ux({accent:A,title:ct,detail:`${At.id} · ${kr[At.status].shortLabel}`,statusColor:kr[At.status].color}),it*.58,nt*.22);Bt.position.set(it/2,-nt*.22,.026),bt.add(Bt);const he=new Zr({blending:Oi,color:kr[At.status].color,depthTest:!1,depthWrite:!1,opacity:0,transparent:!0}),ne=new Cp(new bn().setFromPoints([new Q(-it/2-.025,-nt/2-.025,0),new Q(it/2+.025,-nt/2-.025,0),new Q(it/2+.025,nt/2+.025,0),new Q(-it/2-.025,nt/2+.025,0)]),he);ne.position.set(Y,yt,M+.055),i.add(ne);const ie=new Ta({blending:Oi,color:kr[At.status].color,depthWrite:!1,opacity:0,transparent:!0}),Xt=new Ye(new Ji(it*.72,nt*.68),ie);Xt.position.set(Y,yt,-f/2+.035),i.add(Xt);const ae=new dn;ae.position.set(Y,yt,M),i.add(ae);const ge=new Ye(new En(l,r,.15),new Ta({depthWrite:!1,opacity:0,transparent:!0}));ge.position.set(Y,yt,M+.018),ge.userData={bankId:s.id,id:At.id,lockerNo:At.lockerNo,type:"locker"},i.add(ge),N.push({doorPivot:bt,hitbox:ge,instrument:At,interiorMaterial:ie,markerMaterial:he,openProgress:0,slotAnchor:ae})}const F=new Ye(new En(p+.24,x+.08,f+.18),new Ta({depthWrite:!1,opacity:0,transparent:!0}));F.position.set(0,x/2,0),F.userData={bankId:s.id,id:s.id,type:"bank"},i.add(F);const k=new Ta({blending:Oi,color:s.accent,depthWrite:!1,opacity:0,side:Yi,transparent:!0}),K=new Ye(new Ji(p+.65,f+.95),k);K.rotation.x=-Math.PI/2,K.position.set(0,.017,.22),i.add(K);const tt=new Zr({blending:Oi,color:s.accent,depthWrite:!1,opacity:.24,transparent:!0}),$=p/2+.32,I=f/2+.46,X=new Cp(new bn().setFromPoints([new Q(-$,0,-I),new Q($,0,-I),new Q($,0,I),new Q(-$,0,I)]),tt);X.position.set(0,.022,.22),i.add(X);const at=new Ta({blending:Oi,color:s.accent,depthTest:!1,depthWrite:!1,opacity:.04,transparent:!0}),_t=new Ye(new Ji(p-.08,.022),at);return _t.position.set(0,v+h,M+.07),i.add(_t),i.traverse(vt=>{vt.isMesh&&vt!==F&&!vt.userData.type&&(vt.castShadow=!0,vt.receiveShadow=!0)}),{group:i,parts:{bankHeight:x,bankHitbox:F,edgeMaterial:D.material,focusAnchor:U,gridHeight:_,group:i,instrumentParts:N,padBorderMaterial:tt,padMaterial:k,scanLine:_t,scanMaterial:at,screenParts:G}}}function BR(s){const i=new Float32Array(270),r=new Float32Array(90);for(let g=0;g<90;g+=1){const x=g*37%90/90;r[g]=x,i[g*3]=g*53%110/10-5.5,i[g*3+1]=.08+g*19%34/10,i[g*3+2]=g*71%88/10-3.9}const l=new bn;l.setAttribute("position",new Ti(i,3));const c=new wp({blending:Oi,color:4892895,depthWrite:!1,opacity:.34,size:.022,sizeAttenuation:!0,transparent:!0}),f=new wv(l,c);s.add(f);const h=24,m=new Float32Array(h*3),p=new wp({blending:Oi,color:5690606,depthWrite:!1,opacity:.72,size:.045,sizeAttenuation:!0,transparent:!0}),_=new bn;_.setAttribute("position",new Ti(m,3));const v=new wv(_,p);return s.add(v),{update(g){const x=l.getAttribute("position");for(let A=0;A<90;A+=1){const E=.08+(r[A]*3.8+g*.16+A*.013)%3.8;x.setY(A,E)}x.needsUpdate=!0,f.rotation.y=Math.sin(g*.08)*.06,c.opacity=.28+Math.sin(g*.7)*.05;const M=_.getAttribute("position");for(let A=0;A<h;A+=1){const E=A%3,S=(g*.16+A/h)%1;E===0?M.setXYZ(A,-2.2+S*4.4,.055,3.1-S*6.8):E===1?M.setXYZ(A,-4.6+S*4.2,.055,1.7-S*3.5):M.setXYZ(A,4.6-S*4.2,.055,1.7-S*3.5)}M.needsUpdate=!0,p.opacity=.58+Math.sin(g*1.7)*.12}}}function IR(s){const t=new Ye(new Ji(14,11),new $s({color:529183,metalness:.62,roughness:.61}));t.rotation.x=-Math.PI/2,t.receiveShadow=!0,s.add(t);const i=new wb(14,36,1793167,1059912);i.position.y=.012,i.material.opacity=.38,i.material.transparent=!0,s.add(i);const r=new $s({color:397340,emissive:465194,emissiveIntensity:.15,metalness:.65,roughness:.47}),l=new Ye(new En(12.5,4.9,.12),r);l.position.set(0,2.45,-4.5),s.add(l),[-5.8,5.8].forEach(p=>{const _=new Ye(new En(.12,4.9,7.8),r);_.position.set(p,2.45,-.62),s.add(_)}),s.add(new Sb(9223655,132620,1.08));const c=new Eb(12770548,1.62);c.position.set(2.5,7,6),c.castShadow=!0,c.shadow.mapSize.set(2048,2048),c.shadow.camera.left=-7,c.shadow.camera.right=7,c.shadow.camera.top=7,c.shadow.camera.bottom=-7,s.add(c);const f=new uu(2460927,.66,11,2);f.position.set(0,2.1,2.7),s.add(f);const h=new uu(2934967,.34,8,2);h.position.set(-4.2,1.7,.5),s.add(h);const m=new uu(9991423,.3,8,2);m.position.set(4.2,1.7,.5),s.add(m)}function zR({instruments:s,onBankClick:t,onHover:i,onLockerClick:r,openId:l,selectedBankId:c,selectedId:f}){const h=rn.useRef(null),m=rn.useRef({onBankClick:t,onHover:i,onLockerClick:r,openId:l,selectedBankId:c,selectedId:f});return m.current={onBankClick:t,onHover:i,onLockerClick:r,openId:l,selectedBankId:c,selectedId:f},rn.useEffect(()=>{const p=h.current;if(!p)return;const _=new QE;_.background=new me(397340),_.fog=new Zp(397340,11,24),IR(_);const v=BR(_),g=new hi(39,1,.1,80);g.position.set(.55,5.35,11.2);const x=new Q(0,1.4,-.9);g.lookAt(x);const M=new TR({antialias:!0,powerPreference:"high-performance"});M.setPixelRatio(Math.min(window.devicePixelRatio,1.75)),M.shadowMap.enabled=!0,M.shadowMap.type=ul,M.outputColorSpace=Gn,M.toneMapping=Bp,M.toneMappingExposure=1.18,p.appendChild(M.domElement);const A=new Map(s.map(nt=>[`${nt.bankId}-${nt.slotColumn}-${nt.slotRow}`,nt])),E=new Map,S=new Map,P=[],O=new Map;CR.forEach(nt=>{const ct=PR(nt,A);_.add(ct.group),E.set(nt.id,ct.parts),P.push(ct.parts.bankHitbox),O.set(nt.id,ct.parts.instrumentParts.map(bt=>bt.hitbox)),ct.parts.instrumentParts.forEach(bt=>{S.set(bt.instrument.id,bt)})});const C=new Cb,z=new Ae,D={active:!1,moved:!1,x:0,y:0};let U=.05,b=11.2,N;const G=new Ab;G.connect(document);const F=new Q,k=new Q,K=new Q,tt=new Q,$=new Kr,I=new Q,X=new Q,at=()=>{const nt=Math.max(1,p.clientWidth),ct=Math.max(1,p.clientHeight);M.setSize(nt,ct,!1),g.aspect=nt/ct,g.updateProjectionMatrix()},_t=new ResizeObserver(at);_t.observe(p),at();const vt=nt=>{const ct=M.domElement.getBoundingClientRect();z.x=(nt.clientX-ct.left)/ct.width*2-1,z.y=-((nt.clientY-ct.top)/ct.height)*2+1,C.setFromCamera(z,g);const bt=m.current.selectedBankId;if(!bt)return C.intersectObjects(P,!1)[0];const Ht=O.get(bt)||[],Bt=P.filter(he=>he.userData.bankId!==bt);return C.intersectObjects([...Ht,...Bt],!1)[0]},L=nt=>{var ct,bt;D.active=!0,D.moved=!1,D.x=nt.clientX,D.y=nt.clientY,(bt=(ct=M.domElement).setPointerCapture)==null||bt.call(ct,nt.pointerId)},Y=nt=>{var bt,Ht;if(D.active){const Bt=nt.clientX-D.x,he=nt.clientY-D.y;Math.abs(Bt)+Math.abs(he)>4&&(D.moved=!0),m.current.selectedBankId||(U-=Bt*.0032),D.x=nt.clientX,D.y=nt.clientY;return}const ct=vt(nt);M.domElement.style.cursor=ct?"pointer":"grab",(Ht=(bt=m.current).onHover)==null||Ht.call(bt,(ct==null?void 0:ct.object.userData)||null)},yt=nt=>{var ct,bt,Ht,Bt,he,ne;if(D.active){if(D.active=!1,!D.moved){const ie=vt(nt);(ie==null?void 0:ie.object.userData.type)==="bank"?(bt=(ct=m.current).onBankClick)==null||bt.call(ct,ie.object.userData.bankId):(ie==null?void 0:ie.object.userData.type)==="locker"&&((Bt=(Ht=m.current).onLockerClick)==null||Bt.call(Ht,ie.object.userData.id))}(ne=(he=M.domElement).releasePointerCapture)==null||ne.call(he,nt.pointerId)}},At=()=>{var nt,ct;D.active=!1,(ct=(nt=m.current).onHover)==null||ct.call(nt,null)},Dt=nt=>{m.current.selectedBankId||(nt.preventDefault(),b=zd.clamp(b+nt.deltaY*.008,9.8,13.4))};M.domElement.addEventListener("pointerdown",L),M.domElement.addEventListener("pointermove",Y),M.domElement.addEventListener("pointerup",yt),M.domElement.addEventListener("pointerleave",At),M.domElement.addEventListener("wheel",Dt,{passive:!1});const it=nt=>{G.update(nt);const ct=Math.min(G.getDelta(),.05),bt=G.getElapsed(),{openId:Ht,selectedBankId:Bt,selectedId:he}=m.current,ne=1-Math.exp(-ct*5.2);let ie=39;if(Bt){const Xt=E.get(Bt);Xt.focusAnchor.getWorldPosition(K),Xt.focusAnchor.getWorldQuaternion($),I.set(0,0,1).applyQuaternion($).normalize(),X.set(1,0,0).applyQuaternion($).normalize(),Ht&&S.has(Ht)?(S.get(Ht).slotAnchor.getWorldPosition(tt),F.copy(tt).addScaledVector(I,2.38).addScaledVector(X,.12),F.y=tt.y+.18,k.copy(tt).addScaledVector(X,.33),ie=33):(F.copy(K).addScaledVector(I,6.72),F.y=K.y+.66,k.copy(K),ie=38)}else F.set(.5+Math.sin(U)*b,5.35,-.9+Math.cos(U)*b),k.set(0,1.4,-.9);g.position.lerp(F,ne),x.lerp(k,ne),g.fov=zd.lerp(g.fov,ie,ne),g.updateProjectionMatrix(),g.lookAt(x),v.update(bt),E.forEach((Xt,ae)=>{const ge=ae===Bt,_e=!Ht||ge,Ce=.5+Math.sin(bt*2.2+ae.charCodeAt(0))*.12;if(Xt.group.visible=_e,Xt.edgeMaterial.opacity=Bt?ge?.86+Ce*.08:.2:.5+Ce*.08,Xt.padMaterial.opacity=ge?.045+Ce*.04:0,Xt.padBorderMaterial.opacity=ge?.66:.18,Xt.scanMaterial.opacity=ge?.11+Ce*.08:.025,Xt.scanLine.position.y=.16+(bt*.42+ae.charCodeAt(0)*.13)%1*(Xt.gridHeight-.05),Xt.screenParts){const Ke=bt*.24%1;Xt.screenParts.scan.position.y=-Xt.screenParts.cellHeight*.42+Ke*Xt.screenParts.cellHeight*.84,Xt.screenParts.scanMaterial.opacity=.14+Math.sin(bt*2.4)*.05,Xt.screenParts.glow.intensity=.15+Math.sin(bt*1.8)*.035}}),S.forEach((Xt,ae)=>{const ge=ae===he,_e=ae===Ht;Xt.openProgress=zd.lerp(Xt.openProgress,_e?1:0,1-Math.exp(-ct*(_e?4.1:5.3))),Xt.doorPivot.rotation.y=-1.72*DR(Xt.openProgress),Xt.markerMaterial.opacity=ge?.42+Math.sin(bt*2.6)*.12:0,Xt.interiorMaterial.opacity=_e?.055+Math.sin(bt*2.2)*.018:0}),M.render(_,g),N=requestAnimationFrame(it)};return it(),()=>{cancelAnimationFrame(N),G.dispose(),_t.disconnect(),M.domElement.removeEventListener("pointerdown",L),M.domElement.removeEventListener("pointermove",Y),M.domElement.removeEventListener("pointerup",yt),M.domElement.removeEventListener("pointerleave",At),M.domElement.removeEventListener("wheel",Dt),_.traverse(nt=>{var ct,bt,Ht,Bt,he,ne,ie;(bt=(ct=nt.geometry)==null?void 0:ct.dispose)==null||bt.call(ct),Array.isArray(nt.material)?nt.material.forEach(Xt=>{var ae,ge,_e;(ge=(ae=Xt.map)==null?void 0:ae.dispose)==null||ge.call(ae),(_e=Xt.dispose)==null||_e.call(Xt)}):((he=(Bt=(Ht=nt.material)==null?void 0:Ht.map)==null?void 0:Bt.dispose)==null||he.call(Bt),(ie=(ne=nt.material)==null?void 0:ne.dispose)==null||ie.call(ne))}),M.dispose(),M.domElement.remove()}},[s]),H.jsx("div",{className:"instrument-scene",ref:h,role:"img","aria-label":"三面九列五行仪器柜墙数字孪生场景"})}var Ys={},_h,dx;function FR(){return dx||(dx=1,_h=function(){return typeof Promise=="function"&&Promise.prototype&&Promise.prototype.then}),_h}var vh={},fr={},hx;function Jr(){if(hx)return fr;hx=1;let s;const t=[0,26,44,70,100,134,172,196,242,292,346,404,466,532,581,655,733,815,901,991,1085,1156,1258,1364,1474,1588,1706,1828,1921,2051,2185,2323,2465,2611,2761,2876,3034,3196,3362,3532,3706];return fr.getSymbolSize=function(r){if(!r)throw new Error('"version" cannot be null or undefined');if(r<1||r>40)throw new Error('"version" should be in range from 1 to 40');return r*4+17},fr.getSymbolTotalCodewords=function(r){return t[r]},fr.getBCHDigit=function(i){let r=0;for(;i!==0;)r++,i>>>=1;return r},fr.setToSJISFunction=function(r){if(typeof r!="function")throw new Error('"toSJISFunc" is not a valid function.');s=r},fr.isKanjiModeEnabled=function(){return typeof s<"u"},fr.toSJIS=function(r){return s(r)},fr}var xh={},px;function em(){return px||(px=1,(function(s){s.L={bit:1},s.M={bit:0},s.Q={bit:3},s.H={bit:2};function t(i){if(typeof i!="string")throw new Error("Param is not a string");switch(i.toLowerCase()){case"l":case"low":return s.L;case"m":case"medium":return s.M;case"q":case"quartile":return s.Q;case"h":case"high":return s.H;default:throw new Error("Unknown EC Level: "+i)}}s.isValid=function(r){return r&&typeof r.bit<"u"&&r.bit>=0&&r.bit<4},s.from=function(r,l){if(s.isValid(r))return r;try{return t(r)}catch{return l}}})(xh)),xh}var Sh,mx;function HR(){if(mx)return Sh;mx=1;function s(){this.buffer=[],this.length=0}return s.prototype={get:function(t){const i=Math.floor(t/8);return(this.buffer[i]>>>7-t%8&1)===1},put:function(t,i){for(let r=0;r<i;r++)this.putBit((t>>>i-r-1&1)===1)},getLengthInBits:function(){return this.length},putBit:function(t){const i=Math.floor(this.length/8);this.buffer.length<=i&&this.buffer.push(0),t&&(this.buffer[i]|=128>>>this.length%8),this.length++}},Sh=s,Sh}var yh,gx;function GR(){if(gx)return yh;gx=1;function s(t){if(!t||t<1)throw new Error("BitMatrix size must be defined and greater than 0");this.size=t,this.data=new Uint8Array(t*t),this.reservedBit=new Uint8Array(t*t)}return s.prototype.set=function(t,i,r,l){const c=t*this.size+i;this.data[c]=r,l&&(this.reservedBit[c]=!0)},s.prototype.get=function(t,i){return this.data[t*this.size+i]},s.prototype.xor=function(t,i,r){this.data[t*this.size+i]^=r},s.prototype.isReserved=function(t,i){return this.reservedBit[t*this.size+i]},yh=s,yh}var Mh={},_x;function VR(){return _x||(_x=1,(function(s){const t=Jr().getSymbolSize;s.getRowColCoords=function(r){if(r===1)return[];const l=Math.floor(r/7)+2,c=t(r),f=c===145?26:Math.ceil((c-13)/(2*l-2))*2,h=[c-7];for(let m=1;m<l-1;m++)h[m]=h[m-1]-f;return h.push(6),h.reverse()},s.getPositions=function(r){const l=[],c=s.getRowColCoords(r),f=c.length;for(let h=0;h<f;h++)for(let m=0;m<f;m++)h===0&&m===0||h===0&&m===f-1||h===f-1&&m===0||l.push([c[h],c[m]]);return l}})(Mh)),Mh}var Eh={},vx;function kR(){if(vx)return Eh;vx=1;const s=Jr().getSymbolSize,t=7;return Eh.getPositions=function(r){const l=s(r);return[[0,0],[l-t,0],[0,l-t]]},Eh}var bh={},xx;function XR(){return xx||(xx=1,(function(s){s.Patterns={PATTERN000:0,PATTERN001:1,PATTERN010:2,PATTERN011:3,PATTERN100:4,PATTERN101:5,PATTERN110:6,PATTERN111:7};const t={N1:3,N2:3,N3:40,N4:10};s.isValid=function(l){return l!=null&&l!==""&&!isNaN(l)&&l>=0&&l<=7},s.from=function(l){return s.isValid(l)?parseInt(l,10):void 0},s.getPenaltyN1=function(l){const c=l.size;let f=0,h=0,m=0,p=null,_=null;for(let v=0;v<c;v++){h=m=0,p=_=null;for(let g=0;g<c;g++){let x=l.get(v,g);x===p?h++:(h>=5&&(f+=t.N1+(h-5)),p=x,h=1),x=l.get(g,v),x===_?m++:(m>=5&&(f+=t.N1+(m-5)),_=x,m=1)}h>=5&&(f+=t.N1+(h-5)),m>=5&&(f+=t.N1+(m-5))}return f},s.getPenaltyN2=function(l){const c=l.size;let f=0;for(let h=0;h<c-1;h++)for(let m=0;m<c-1;m++){const p=l.get(h,m)+l.get(h,m+1)+l.get(h+1,m)+l.get(h+1,m+1);(p===4||p===0)&&f++}return f*t.N2},s.getPenaltyN3=function(l){const c=l.size;let f=0,h=0,m=0;for(let p=0;p<c;p++){h=m=0;for(let _=0;_<c;_++)h=h<<1&2047|l.get(p,_),_>=10&&(h===1488||h===93)&&f++,m=m<<1&2047|l.get(_,p),_>=10&&(m===1488||m===93)&&f++}return f*t.N3},s.getPenaltyN4=function(l){let c=0;const f=l.data.length;for(let m=0;m<f;m++)c+=l.data[m];return Math.abs(Math.ceil(c*100/f/5)-10)*t.N4};function i(r,l,c){switch(r){case s.Patterns.PATTERN000:return(l+c)%2===0;case s.Patterns.PATTERN001:return l%2===0;case s.Patterns.PATTERN010:return c%3===0;case s.Patterns.PATTERN011:return(l+c)%3===0;case s.Patterns.PATTERN100:return(Math.floor(l/2)+Math.floor(c/3))%2===0;case s.Patterns.PATTERN101:return l*c%2+l*c%3===0;case s.Patterns.PATTERN110:return(l*c%2+l*c%3)%2===0;case s.Patterns.PATTERN111:return(l*c%3+(l+c)%2)%2===0;default:throw new Error("bad maskPattern:"+r)}}s.applyMask=function(l,c){const f=c.size;for(let h=0;h<f;h++)for(let m=0;m<f;m++)c.isReserved(m,h)||c.xor(m,h,i(l,m,h))},s.getBestMask=function(l,c){const f=Object.keys(s.Patterns).length;let h=0,m=1/0;for(let p=0;p<f;p++){c(p),s.applyMask(p,l);const _=s.getPenaltyN1(l)+s.getPenaltyN2(l)+s.getPenaltyN3(l)+s.getPenaltyN4(l);s.applyMask(p,l),_<m&&(m=_,h=p)}return h}})(bh)),bh}var ru={},Sx;function RS(){if(Sx)return ru;Sx=1;const s=em(),t=[1,1,1,1,1,1,1,1,1,1,2,2,1,2,2,4,1,2,4,4,2,4,4,4,2,4,6,5,2,4,6,6,2,5,8,8,4,5,8,8,4,5,8,11,4,8,10,11,4,9,12,16,4,9,16,16,6,10,12,18,6,10,17,16,6,11,16,19,6,13,18,21,7,14,21,25,8,16,20,25,8,17,23,25,9,17,23,34,9,18,25,30,10,20,27,32,12,21,29,35,12,23,34,37,12,25,34,40,13,26,35,42,14,28,38,45,15,29,40,48,16,31,43,51,17,33,45,54,18,35,48,57,19,37,51,60,19,38,53,63,20,40,56,66,21,43,59,70,22,45,62,74,24,47,65,77,25,49,68,81],i=[7,10,13,17,10,16,22,28,15,26,36,44,20,36,52,64,26,48,72,88,36,64,96,112,40,72,108,130,48,88,132,156,60,110,160,192,72,130,192,224,80,150,224,264,96,176,260,308,104,198,288,352,120,216,320,384,132,240,360,432,144,280,408,480,168,308,448,532,180,338,504,588,196,364,546,650,224,416,600,700,224,442,644,750,252,476,690,816,270,504,750,900,300,560,810,960,312,588,870,1050,336,644,952,1110,360,700,1020,1200,390,728,1050,1260,420,784,1140,1350,450,812,1200,1440,480,868,1290,1530,510,924,1350,1620,540,980,1440,1710,570,1036,1530,1800,570,1064,1590,1890,600,1120,1680,1980,630,1204,1770,2100,660,1260,1860,2220,720,1316,1950,2310,750,1372,2040,2430];return ru.getBlocksCount=function(l,c){switch(c){case s.L:return t[(l-1)*4+0];case s.M:return t[(l-1)*4+1];case s.Q:return t[(l-1)*4+2];case s.H:return t[(l-1)*4+3];default:return}},ru.getTotalCodewordsCount=function(l,c){switch(c){case s.L:return i[(l-1)*4+0];case s.M:return i[(l-1)*4+1];case s.Q:return i[(l-1)*4+2];case s.H:return i[(l-1)*4+3];default:return}},ru}var Th={},sl={},yx;function qR(){if(yx)return sl;yx=1;const s=new Uint8Array(512),t=new Uint8Array(256);return(function(){let r=1;for(let l=0;l<255;l++)s[l]=r,t[r]=l,r<<=1,r&256&&(r^=285);for(let l=255;l<512;l++)s[l]=s[l-255]})(),sl.log=function(r){if(r<1)throw new Error("log("+r+")");return t[r]},sl.exp=function(r){return s[r]},sl.mul=function(r,l){return r===0||l===0?0:s[t[r]+t[l]]},sl}var Mx;function WR(){return Mx||(Mx=1,(function(s){const t=qR();s.mul=function(r,l){const c=new Uint8Array(r.length+l.length-1);for(let f=0;f<r.length;f++)for(let h=0;h<l.length;h++)c[f+h]^=t.mul(r[f],l[h]);return c},s.mod=function(r,l){let c=new Uint8Array(r);for(;c.length-l.length>=0;){const f=c[0];for(let m=0;m<l.length;m++)c[m]^=t.mul(l[m],f);let h=0;for(;h<c.length&&c[h]===0;)h++;c=c.slice(h)}return c},s.generateECPolynomial=function(r){let l=new Uint8Array([1]);for(let c=0;c<r;c++)l=s.mul(l,new Uint8Array([1,t.exp(c)]));return l}})(Th)),Th}var Ah,Ex;function YR(){if(Ex)return Ah;Ex=1;const s=WR();function t(i){this.genPoly=void 0,this.degree=i,this.degree&&this.initialize(this.degree)}return t.prototype.initialize=function(r){this.degree=r,this.genPoly=s.generateECPolynomial(this.degree)},t.prototype.encode=function(r){if(!this.genPoly)throw new Error("Encoder not initialized");const l=new Uint8Array(r.length+this.degree);l.set(r);const c=s.mod(l,this.genPoly),f=this.degree-c.length;if(f>0){const h=new Uint8Array(this.degree);return h.set(c,f),h}return c},Ah=t,Ah}var Rh={},Ch={},wh={},bx;function CS(){return bx||(bx=1,wh.isValid=function(t){return!isNaN(t)&&t>=1&&t<=40}),wh}var qi={},Tx;function wS(){if(Tx)return qi;Tx=1;const s="[0-9]+",t="[A-Z $%*+\\-./:]+";let i="(?:[u3000-u303F]|[u3040-u309F]|[u30A0-u30FF]|[uFF00-uFFEF]|[u4E00-u9FAF]|[u2605-u2606]|[u2190-u2195]|u203B|[u2010u2015u2018u2019u2025u2026u201Cu201Du2225u2260]|[u0391-u0451]|[u00A7u00A8u00B1u00B4u00D7u00F7])+";i=i.replace(/u/g,"\\u");const r="(?:(?![A-Z0-9 $%*+\\-./:]|"+i+`)(?:.|[\r
]))+`;qi.KANJI=new RegExp(i,"g"),qi.BYTE_KANJI=new RegExp("[^A-Z0-9 $%*+\\-./:]+","g"),qi.BYTE=new RegExp(r,"g"),qi.NUMERIC=new RegExp(s,"g"),qi.ALPHANUMERIC=new RegExp(t,"g");const l=new RegExp("^"+i+"$"),c=new RegExp("^"+s+"$"),f=new RegExp("^[A-Z0-9 $%*+\\-./:]+$");return qi.testKanji=function(m){return l.test(m)},qi.testNumeric=function(m){return c.test(m)},qi.testAlphanumeric=function(m){return f.test(m)},qi}var Ax;function $r(){return Ax||(Ax=1,(function(s){const t=CS(),i=wS();s.NUMERIC={id:"Numeric",bit:1,ccBits:[10,12,14]},s.ALPHANUMERIC={id:"Alphanumeric",bit:2,ccBits:[9,11,13]},s.BYTE={id:"Byte",bit:4,ccBits:[8,16,16]},s.KANJI={id:"Kanji",bit:8,ccBits:[8,10,12]},s.MIXED={bit:-1},s.getCharCountIndicator=function(c,f){if(!c.ccBits)throw new Error("Invalid mode: "+c);if(!t.isValid(f))throw new Error("Invalid version: "+f);return f>=1&&f<10?c.ccBits[0]:f<27?c.ccBits[1]:c.ccBits[2]},s.getBestModeForData=function(c){return i.testNumeric(c)?s.NUMERIC:i.testAlphanumeric(c)?s.ALPHANUMERIC:i.testKanji(c)?s.KANJI:s.BYTE},s.toString=function(c){if(c&&c.id)return c.id;throw new Error("Invalid mode")},s.isValid=function(c){return c&&c.bit&&c.ccBits};function r(l){if(typeof l!="string")throw new Error("Param is not a string");switch(l.toLowerCase()){case"numeric":return s.NUMERIC;case"alphanumeric":return s.ALPHANUMERIC;case"kanji":return s.KANJI;case"byte":return s.BYTE;default:throw new Error("Unknown mode: "+l)}}s.from=function(c,f){if(s.isValid(c))return c;try{return r(c)}catch{return f}}})(Ch)),Ch}var Rx;function ZR(){return Rx||(Rx=1,(function(s){const t=Jr(),i=RS(),r=em(),l=$r(),c=CS(),f=7973,h=t.getBCHDigit(f);function m(g,x,M){for(let A=1;A<=40;A++)if(x<=s.getCapacity(A,M,g))return A}function p(g,x){return l.getCharCountIndicator(g,x)+4}function _(g,x){let M=0;return g.forEach(function(A){const E=p(A.mode,x);M+=E+A.getBitsLength()}),M}function v(g,x){for(let M=1;M<=40;M++)if(_(g,M)<=s.getCapacity(M,x,l.MIXED))return M}s.from=function(x,M){return c.isValid(x)?parseInt(x,10):M},s.getCapacity=function(x,M,A){if(!c.isValid(x))throw new Error("Invalid QR Code version");typeof A>"u"&&(A=l.BYTE);const E=t.getSymbolTotalCodewords(x),S=i.getTotalCodewordsCount(x,M),P=(E-S)*8;if(A===l.MIXED)return P;const O=P-p(A,x);switch(A){case l.NUMERIC:return Math.floor(O/10*3);case l.ALPHANUMERIC:return Math.floor(O/11*2);case l.KANJI:return Math.floor(O/13);case l.BYTE:default:return Math.floor(O/8)}},s.getBestVersionForData=function(x,M){let A;const E=r.from(M,r.M);if(Array.isArray(x)){if(x.length>1)return v(x,E);if(x.length===0)return 1;A=x[0]}else A=x;return m(A.mode,A.getLength(),E)},s.getEncodedBits=function(x){if(!c.isValid(x)||x<7)throw new Error("Invalid QR Code version");let M=x<<12;for(;t.getBCHDigit(M)-h>=0;)M^=f<<t.getBCHDigit(M)-h;return x<<12|M}})(Rh)),Rh}var Dh={},Cx;function jR(){if(Cx)return Dh;Cx=1;const s=Jr(),t=1335,i=21522,r=s.getBCHDigit(t);return Dh.getEncodedBits=function(c,f){const h=c.bit<<3|f;let m=h<<10;for(;s.getBCHDigit(m)-r>=0;)m^=t<<s.getBCHDigit(m)-r;return(h<<10|m)^i},Dh}var Nh={},Uh,wx;function KR(){if(wx)return Uh;wx=1;const s=$r();function t(i){this.mode=s.NUMERIC,this.data=i.toString()}return t.getBitsLength=function(r){return 10*Math.floor(r/3)+(r%3?r%3*3+1:0)},t.prototype.getLength=function(){return this.data.length},t.prototype.getBitsLength=function(){return t.getBitsLength(this.data.length)},t.prototype.write=function(r){let l,c,f;for(l=0;l+3<=this.data.length;l+=3)c=this.data.substr(l,3),f=parseInt(c,10),r.put(f,10);const h=this.data.length-l;h>0&&(c=this.data.substr(l),f=parseInt(c,10),r.put(f,h*3+1))},Uh=t,Uh}var Lh,Dx;function QR(){if(Dx)return Lh;Dx=1;const s=$r(),t=["0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ","$","%","*","+","-",".","/",":"];function i(r){this.mode=s.ALPHANUMERIC,this.data=r}return i.getBitsLength=function(l){return 11*Math.floor(l/2)+6*(l%2)},i.prototype.getLength=function(){return this.data.length},i.prototype.getBitsLength=function(){return i.getBitsLength(this.data.length)},i.prototype.write=function(l){let c;for(c=0;c+2<=this.data.length;c+=2){let f=t.indexOf(this.data[c])*45;f+=t.indexOf(this.data[c+1]),l.put(f,11)}this.data.length%2&&l.put(t.indexOf(this.data[c]),6)},Lh=i,Lh}var Oh,Nx;function JR(){if(Nx)return Oh;Nx=1;const s=$r();function t(i){this.mode=s.BYTE,typeof i=="string"?this.data=new TextEncoder().encode(i):this.data=new Uint8Array(i)}return t.getBitsLength=function(r){return r*8},t.prototype.getLength=function(){return this.data.length},t.prototype.getBitsLength=function(){return t.getBitsLength(this.data.length)},t.prototype.write=function(i){for(let r=0,l=this.data.length;r<l;r++)i.put(this.data[r],8)},Oh=t,Oh}var Ph,Ux;function $R(){if(Ux)return Ph;Ux=1;const s=$r(),t=Jr();function i(r){this.mode=s.KANJI,this.data=r}return i.getBitsLength=function(l){return l*13},i.prototype.getLength=function(){return this.data.length},i.prototype.getBitsLength=function(){return i.getBitsLength(this.data.length)},i.prototype.write=function(r){let l;for(l=0;l<this.data.length;l++){let c=t.toSJIS(this.data[l]);if(c>=33088&&c<=40956)c-=33088;else if(c>=57408&&c<=60351)c-=49472;else throw new Error("Invalid SJIS character: "+this.data[l]+`
Make sure your charset is UTF-8`);c=(c>>>8&255)*192+(c&255),r.put(c,13)}},Ph=i,Ph}var Bh={exports:{}},Lx;function tC(){return Lx||(Lx=1,(function(s){var t={single_source_shortest_paths:function(i,r,l){var c={},f={};f[r]=0;var h=t.PriorityQueue.make();h.push(r,0);for(var m,p,_,v,g,x,M,A,E;!h.empty();){m=h.pop(),p=m.value,v=m.cost,g=i[p]||{};for(_ in g)g.hasOwnProperty(_)&&(x=g[_],M=v+x,A=f[_],E=typeof f[_]>"u",(E||A>M)&&(f[_]=M,h.push(_,M),c[_]=p))}if(typeof l<"u"&&typeof f[l]>"u"){var S=["Could not find a path from ",r," to ",l,"."].join("");throw new Error(S)}return c},extract_shortest_path_from_predecessor_list:function(i,r){for(var l=[],c=r;c;)l.push(c),i[c],c=i[c];return l.reverse(),l},find_path:function(i,r,l){var c=t.single_source_shortest_paths(i,r,l);return t.extract_shortest_path_from_predecessor_list(c,l)},PriorityQueue:{make:function(i){var r=t.PriorityQueue,l={},c;i=i||{};for(c in r)r.hasOwnProperty(c)&&(l[c]=r[c]);return l.queue=[],l.sorter=i.sorter||r.default_sorter,l},default_sorter:function(i,r){return i.cost-r.cost},push:function(i,r){var l={value:i,cost:r};this.queue.push(l),this.queue.sort(this.sorter)},pop:function(){return this.queue.shift()},empty:function(){return this.queue.length===0}}};s.exports=t})(Bh)),Bh.exports}var Ox;function eC(){return Ox||(Ox=1,(function(s){const t=$r(),i=KR(),r=QR(),l=JR(),c=$R(),f=wS(),h=Jr(),m=tC();function p(S){return unescape(encodeURIComponent(S)).length}function _(S,P,O){const C=[];let z;for(;(z=S.exec(O))!==null;)C.push({data:z[0],index:z.index,mode:P,length:z[0].length});return C}function v(S){const P=_(f.NUMERIC,t.NUMERIC,S),O=_(f.ALPHANUMERIC,t.ALPHANUMERIC,S);let C,z;return h.isKanjiModeEnabled()?(C=_(f.BYTE,t.BYTE,S),z=_(f.KANJI,t.KANJI,S)):(C=_(f.BYTE_KANJI,t.BYTE,S),z=[]),P.concat(O,C,z).sort(function(U,b){return U.index-b.index}).map(function(U){return{data:U.data,mode:U.mode,length:U.length}})}function g(S,P){switch(P){case t.NUMERIC:return i.getBitsLength(S);case t.ALPHANUMERIC:return r.getBitsLength(S);case t.KANJI:return c.getBitsLength(S);case t.BYTE:return l.getBitsLength(S)}}function x(S){return S.reduce(function(P,O){const C=P.length-1>=0?P[P.length-1]:null;return C&&C.mode===O.mode?(P[P.length-1].data+=O.data,P):(P.push(O),P)},[])}function M(S){const P=[];for(let O=0;O<S.length;O++){const C=S[O];switch(C.mode){case t.NUMERIC:P.push([C,{data:C.data,mode:t.ALPHANUMERIC,length:C.length},{data:C.data,mode:t.BYTE,length:C.length}]);break;case t.ALPHANUMERIC:P.push([C,{data:C.data,mode:t.BYTE,length:C.length}]);break;case t.KANJI:P.push([C,{data:C.data,mode:t.BYTE,length:p(C.data)}]);break;case t.BYTE:P.push([{data:C.data,mode:t.BYTE,length:p(C.data)}])}}return P}function A(S,P){const O={},C={start:{}};let z=["start"];for(let D=0;D<S.length;D++){const U=S[D],b=[];for(let N=0;N<U.length;N++){const G=U[N],F=""+D+N;b.push(F),O[F]={node:G,lastCount:0},C[F]={};for(let k=0;k<z.length;k++){const K=z[k];O[K]&&O[K].node.mode===G.mode?(C[K][F]=g(O[K].lastCount+G.length,G.mode)-g(O[K].lastCount,G.mode),O[K].lastCount+=G.length):(O[K]&&(O[K].lastCount=G.length),C[K][F]=g(G.length,G.mode)+4+t.getCharCountIndicator(G.mode,P))}}z=b}for(let D=0;D<z.length;D++)C[z[D]].end=0;return{map:C,table:O}}function E(S,P){let O;const C=t.getBestModeForData(S);if(O=t.from(P,C),O!==t.BYTE&&O.bit<C.bit)throw new Error('"'+S+'" cannot be encoded with mode '+t.toString(O)+`.
 Suggested mode is: `+t.toString(C));switch(O===t.KANJI&&!h.isKanjiModeEnabled()&&(O=t.BYTE),O){case t.NUMERIC:return new i(S);case t.ALPHANUMERIC:return new r(S);case t.KANJI:return new c(S);case t.BYTE:return new l(S)}}s.fromArray=function(P){return P.reduce(function(O,C){return typeof C=="string"?O.push(E(C,null)):C.data&&O.push(E(C.data,C.mode)),O},[])},s.fromString=function(P,O){const C=v(P,h.isKanjiModeEnabled()),z=M(C),D=A(z,O),U=m.find_path(D.map,"start","end"),b=[];for(let N=1;N<U.length-1;N++)b.push(D.table[U[N]].node);return s.fromArray(x(b))},s.rawSplit=function(P){return s.fromArray(v(P,h.isKanjiModeEnabled()))}})(Nh)),Nh}var Px;function nC(){if(Px)return vh;Px=1;const s=Jr(),t=em(),i=HR(),r=GR(),l=VR(),c=kR(),f=XR(),h=RS(),m=YR(),p=ZR(),_=jR(),v=$r(),g=eC();function x(D,U){const b=D.size,N=c.getPositions(U);for(let G=0;G<N.length;G++){const F=N[G][0],k=N[G][1];for(let K=-1;K<=7;K++)if(!(F+K<=-1||b<=F+K))for(let tt=-1;tt<=7;tt++)k+tt<=-1||b<=k+tt||(K>=0&&K<=6&&(tt===0||tt===6)||tt>=0&&tt<=6&&(K===0||K===6)||K>=2&&K<=4&&tt>=2&&tt<=4?D.set(F+K,k+tt,!0,!0):D.set(F+K,k+tt,!1,!0))}}function M(D){const U=D.size;for(let b=8;b<U-8;b++){const N=b%2===0;D.set(b,6,N,!0),D.set(6,b,N,!0)}}function A(D,U){const b=l.getPositions(U);for(let N=0;N<b.length;N++){const G=b[N][0],F=b[N][1];for(let k=-2;k<=2;k++)for(let K=-2;K<=2;K++)k===-2||k===2||K===-2||K===2||k===0&&K===0?D.set(G+k,F+K,!0,!0):D.set(G+k,F+K,!1,!0)}}function E(D,U){const b=D.size,N=p.getEncodedBits(U);let G,F,k;for(let K=0;K<18;K++)G=Math.floor(K/3),F=K%3+b-8-3,k=(N>>K&1)===1,D.set(G,F,k,!0),D.set(F,G,k,!0)}function S(D,U,b){const N=D.size,G=_.getEncodedBits(U,b);let F,k;for(F=0;F<15;F++)k=(G>>F&1)===1,F<6?D.set(F,8,k,!0):F<8?D.set(F+1,8,k,!0):D.set(N-15+F,8,k,!0),F<8?D.set(8,N-F-1,k,!0):F<9?D.set(8,15-F-1+1,k,!0):D.set(8,15-F-1,k,!0);D.set(N-8,8,1,!0)}function P(D,U){const b=D.size;let N=-1,G=b-1,F=7,k=0;for(let K=b-1;K>0;K-=2)for(K===6&&K--;;){for(let tt=0;tt<2;tt++)if(!D.isReserved(G,K-tt)){let $=!1;k<U.length&&($=(U[k]>>>F&1)===1),D.set(G,K-tt,$),F--,F===-1&&(k++,F=7)}if(G+=N,G<0||b<=G){G-=N,N=-N;break}}}function O(D,U,b){const N=new i;b.forEach(function(tt){N.put(tt.mode.bit,4),N.put(tt.getLength(),v.getCharCountIndicator(tt.mode,D)),tt.write(N)});const G=s.getSymbolTotalCodewords(D),F=h.getTotalCodewordsCount(D,U),k=(G-F)*8;for(N.getLengthInBits()+4<=k&&N.put(0,4);N.getLengthInBits()%8!==0;)N.putBit(0);const K=(k-N.getLengthInBits())/8;for(let tt=0;tt<K;tt++)N.put(tt%2?17:236,8);return C(N,D,U)}function C(D,U,b){const N=s.getSymbolTotalCodewords(U),G=h.getTotalCodewordsCount(U,b),F=N-G,k=h.getBlocksCount(U,b),K=N%k,tt=k-K,$=Math.floor(N/k),I=Math.floor(F/k),X=I+1,at=$-I,_t=new m(at);let vt=0;const L=new Array(k),Y=new Array(k);let yt=0;const At=new Uint8Array(D.buffer);for(let bt=0;bt<k;bt++){const Ht=bt<tt?I:X;L[bt]=At.slice(vt,vt+Ht),Y[bt]=_t.encode(L[bt]),vt+=Ht,yt=Math.max(yt,Ht)}const Dt=new Uint8Array(N);let it=0,nt,ct;for(nt=0;nt<yt;nt++)for(ct=0;ct<k;ct++)nt<L[ct].length&&(Dt[it++]=L[ct][nt]);for(nt=0;nt<at;nt++)for(ct=0;ct<k;ct++)Dt[it++]=Y[ct][nt];return Dt}function z(D,U,b,N){let G;if(Array.isArray(D))G=g.fromArray(D);else if(typeof D=="string"){let $=U;if(!$){const I=g.rawSplit(D);$=p.getBestVersionForData(I,b)}G=g.fromString(D,$||40)}else throw new Error("Invalid data");const F=p.getBestVersionForData(G,b);if(!F)throw new Error("The amount of data is too big to be stored in a QR Code");if(!U)U=F;else if(U<F)throw new Error(`
The chosen QR Code version cannot contain this amount of data.
Minimum version required to store current data is: `+F+`.
`);const k=O(U,b,G),K=s.getSymbolSize(U),tt=new r(K);return x(tt,U),M(tt),A(tt,U),S(tt,b,0),U>=7&&E(tt,U),P(tt,k),isNaN(N)&&(N=f.getBestMask(tt,S.bind(null,tt,b))),f.applyMask(N,tt),S(tt,b,N),{modules:tt,version:U,errorCorrectionLevel:b,maskPattern:N,segments:G}}return vh.create=function(U,b){if(typeof U>"u"||U==="")throw new Error("No input text");let N=t.M,G,F;return typeof b<"u"&&(N=t.from(b.errorCorrectionLevel,t.M),G=p.from(b.version),F=f.from(b.maskPattern),b.toSJISFunc&&s.setToSJISFunction(b.toSJISFunc)),z(U,G,N,F)},vh}var Ih={},zh={},Bx;function DS(){return Bx||(Bx=1,(function(s){function t(i){if(typeof i=="number"&&(i=i.toString()),typeof i!="string")throw new Error("Color should be defined as hex string");let r=i.slice().replace("#","").split("");if(r.length<3||r.length===5||r.length>8)throw new Error("Invalid hex color: "+i);(r.length===3||r.length===4)&&(r=Array.prototype.concat.apply([],r.map(function(c){return[c,c]}))),r.length===6&&r.push("F","F");const l=parseInt(r.join(""),16);return{r:l>>24&255,g:l>>16&255,b:l>>8&255,a:l&255,hex:"#"+r.slice(0,6).join("")}}s.getOptions=function(r){r||(r={}),r.color||(r.color={});const l=typeof r.margin>"u"||r.margin===null||r.margin<0?4:r.margin,c=r.width&&r.width>=21?r.width:void 0,f=r.scale||4;return{width:c,scale:c?4:f,margin:l,color:{dark:t(r.color.dark||"#000000ff"),light:t(r.color.light||"#ffffffff")},type:r.type,rendererOpts:r.rendererOpts||{}}},s.getScale=function(r,l){return l.width&&l.width>=r+l.margin*2?l.width/(r+l.margin*2):l.scale},s.getImageWidth=function(r,l){const c=s.getScale(r,l);return Math.floor((r+l.margin*2)*c)},s.qrToImageData=function(r,l,c){const f=l.modules.size,h=l.modules.data,m=s.getScale(f,c),p=Math.floor((f+c.margin*2)*m),_=c.margin*m,v=[c.color.light,c.color.dark];for(let g=0;g<p;g++)for(let x=0;x<p;x++){let M=(g*p+x)*4,A=c.color.light;if(g>=_&&x>=_&&g<p-_&&x<p-_){const E=Math.floor((g-_)/m),S=Math.floor((x-_)/m);A=v[h[E*f+S]?1:0]}r[M++]=A.r,r[M++]=A.g,r[M++]=A.b,r[M]=A.a}}})(zh)),zh}var Ix;function iC(){return Ix||(Ix=1,(function(s){const t=DS();function i(l,c,f){l.clearRect(0,0,c.width,c.height),c.style||(c.style={}),c.height=f,c.width=f,c.style.height=f+"px",c.style.width=f+"px"}function r(){try{return document.createElement("canvas")}catch{throw new Error("You need to specify a canvas element")}}s.render=function(c,f,h){let m=h,p=f;typeof m>"u"&&(!f||!f.getContext)&&(m=f,f=void 0),f||(p=r()),m=t.getOptions(m);const _=t.getImageWidth(c.modules.size,m),v=p.getContext("2d"),g=v.createImageData(_,_);return t.qrToImageData(g.data,c,m),i(v,p,_),v.putImageData(g,0,0),p},s.renderToDataURL=function(c,f,h){let m=h;typeof m>"u"&&(!f||!f.getContext)&&(m=f,f=void 0),m||(m={});const p=s.render(c,f,m),_=m.type||"image/png",v=m.rendererOpts||{};return p.toDataURL(_,v.quality)}})(Ih)),Ih}var Fh={},zx;function aC(){if(zx)return Fh;zx=1;const s=DS();function t(l,c){const f=l.a/255,h=c+'="'+l.hex+'"';return f<1?h+" "+c+'-opacity="'+f.toFixed(2).slice(1)+'"':h}function i(l,c,f){let h=l+c;return typeof f<"u"&&(h+=" "+f),h}function r(l,c,f){let h="",m=0,p=!1,_=0;for(let v=0;v<l.length;v++){const g=Math.floor(v%c),x=Math.floor(v/c);!g&&!p&&(p=!0),l[v]?(_++,v>0&&g>0&&l[v-1]||(h+=p?i("M",g+f,.5+x+f):i("m",m,0),m=0,p=!1),g+1<c&&l[v+1]||(h+=i("h",_),_=0)):m++}return h}return Fh.render=function(c,f,h){const m=s.getOptions(f),p=c.modules.size,_=c.modules.data,v=p+m.margin*2,g=m.color.light.a?"<path "+t(m.color.light,"fill")+' d="M0 0h'+v+"v"+v+'H0z"/>':"",x="<path "+t(m.color.dark,"stroke")+' d="'+r(_,p,m.margin)+'"/>',M='viewBox="0 0 '+v+" "+v+'"',E='<svg xmlns="http://www.w3.org/2000/svg" '+(m.width?'width="'+m.width+'" height="'+m.width+'" ':"")+M+' shape-rendering="crispEdges">'+g+x+`</svg>
`;return typeof h=="function"&&h(null,E),E},Fh}var Fx;function rC(){if(Fx)return Ys;Fx=1;const s=FR(),t=nC(),i=iC(),r=aC();function l(c,f,h,m,p){const _=[].slice.call(arguments,1),v=_.length,g=typeof _[v-1]=="function";if(!g&&!s())throw new Error("Callback required as last argument");if(g){if(v<2)throw new Error("Too few arguments provided");v===2?(p=h,h=f,f=m=void 0):v===3&&(f.getContext&&typeof p>"u"?(p=m,m=void 0):(p=m,m=h,h=f,f=void 0))}else{if(v<1)throw new Error("Too few arguments provided");return v===1?(h=f,f=m=void 0):v===2&&!f.getContext&&(m=h,h=f,f=void 0),new Promise(function(x,M){try{const A=t.create(h,m);x(c(A,f,m))}catch(A){M(A)}})}try{const x=t.create(h,m);p(null,c(x,f,m))}catch(x){p(x)}}return Ys.create=t.create,Ys.toCanvas=l.bind(null,i.render),Ys.toDataURL=l.bind(null,i.renderToDataURL),Ys.toString=l.bind(null,function(c,f,h){return r.render(c,h)}),Ys}var sC=rC();const oC=LM(sC);function lC({instrument:s,onOpen:t}){const[i,r]=rn.useState("");return rn.useEffect(()=>{let l=!0;const c=`https://instrument-twin.example/history/${s.id}`;return oC.toDataURL(c,{errorCorrectionLevel:"M",margin:1,width:220,color:{dark:"#101719",light:"#f4f7f5"}}).then(f=>{l&&r(f)}),()=>{l=!1}},[s.id]),H.jsx("button",{className:"qr-code",onClick:t,type:"button","aria-label":`查看 ${s.equipmentName} 使用履历`,children:i?H.jsx("img",{src:i,alt:`${s.id} 设备履历二维码`}):H.jsx("span",{className:"qr-code__loading",children:"生成中"})})}function cC(){const[s,t]=rn.useState(()=>new Date);return rn.useEffect(()=>{const i=window.setInterval(()=>t(new Date),1e3);return()=>window.clearInterval(i)},[]),s}function ol({icon:s,label:t,tone:i,value:r}){return H.jsxs("div",{className:`top-metric top-metric--${i}`,children:[H.jsx(s,{size:17}),H.jsx("span",{children:t}),H.jsx("strong",{children:r})]})}function uC({filteredInstruments:s,onQueryChange:t,onSelect:i,onStatusChange:r,query:l,selectedBankId:c,selectedId:f,statusFilter:h}){const m=[["all","全部"],["stored","存放"],["checkedOut","外借"],["maintenance","检修"]];return H.jsxs("aside",{className:"equipment-rail","aria-label":"仪器柜格口列表",children:[H.jsxs("div",{className:"rail-search",children:[H.jsx(y1,{size:15}),H.jsx("input",{"aria-label":"搜索设备名称或编号",onChange:p=>t(p.target.value),placeholder:"搜索设备名称或编号",value:l})]}),H.jsx("div",{className:"status-filter","aria-label":"设备状态筛选",children:m.map(([p,_])=>H.jsx("button",{className:h===p?"is-active":"",onClick:()=>r(p),type:"button",children:_},p))}),H.jsxs("div",{className:"equipment-list-heading",children:[H.jsx("span",{children:"格口与设备"}),H.jsx("strong",{children:s.length})]}),H.jsx("div",{className:"equipment-list",children:s.map((p,_)=>{const v=kr[p.status];return H.jsxs("button",{className:`equipment-row ${f===p.id?"is-selected":c===p.bankId?"is-bank-selected":""}`,onClick:()=>i(p.id),type:"button",children:[H.jsx("span",{className:"equipment-row__index",children:String(_+1).padStart(2,"0")}),H.jsx(t1,{size:17}),H.jsxs("span",{className:"equipment-row__main",children:[H.jsxs("strong",{children:[p.lockerNo," · ",p.id]}),H.jsx("small",{children:p.equipmentName})]}),H.jsxs("span",{className:"equipment-row__status",style:{"--status-color":v.color},children:[H.jsx("i",{}),v.shortLabel]})]},p.id)})}),H.jsxs("div",{className:"status-legend",children:[H.jsx("span",{children:"柜面状态标识"}),H.jsxs("div",{children:[H.jsx("i",{className:"red"})," 检测外借"]}),H.jsxs("div",{children:[H.jsx("i",{className:"amber"})," 检修中"]}),H.jsxs("div",{children:[H.jsx("i",{className:"green"})," 存放中"]}),H.jsx("small",{children:"铭牌色带由设备业务台账驱动"})]})]})}const fC=[["生产厂家","manufacturer"],["管理编号","managementNo"],["仪器编号","serialNo"],["规格型号","model"],["量程","range"],["精度","precision"],["最近检定","lastCalibration"],["下次检定","nextCalibration"],["检定机构","verificationAgency"]];function dC({instrument:s,onBack:t,onOpenHistory:i}){const r=kr[s.status];return H.jsxs("section",{className:"instrument-inspection",style:{"--status-color":r.color},"aria-label":`${s.lockerNo}格口设备资料`,children:[H.jsx("div",{className:"inspection-accent"}),H.jsxs("div",{className:"inspection-heading",children:[H.jsx("button",{type:"button",onClick:t,title:"返回整面柜墙","aria-label":"返回整面柜墙",children:H.jsx(Gx,{size:16})}),H.jsxs("div",{children:[H.jsxs("span",{children:["格口近景 · ",s.lockerNo]}),H.jsx("strong",{children:s.equipmentName})]}),H.jsxs("em",{children:[H.jsx("i",{}),r.label]})]}),H.jsxs("div",{className:"inspection-summary",children:[H.jsxs("div",{children:[H.jsx(u1,{size:15}),H.jsx("span",{children:s.model})]}),H.jsxs("div",{children:[H.jsx(i1,{size:15}),H.jsx("span",{children:s.department})]}),H.jsxs("div",{children:[H.jsx(R1,{size:15}),H.jsx("span",{children:s.custodian})]})]}),H.jsxs("div",{className:"inspection-list-heading",children:[H.jsxs("span",{children:[H.jsx(T1,{size:13})," 设备铭牌"]}),H.jsx("small",{children:s.id})]}),H.jsx("dl",{className:"inspection-nameplate",children:fC.map(([l,c])=>H.jsxs("div",{children:[H.jsx("dt",{children:l}),H.jsx("dd",{children:s[c]})]},c))}),H.jsxs("div",{className:"inspection-list-heading",children:[H.jsxs("span",{children:[H.jsx(qx,{size:13})," 最近使用记录"]}),H.jsx("small",{children:"测试数据"})]}),H.jsx("div",{className:"inspection-history",children:s.history.slice(0,3).map((l,c)=>H.jsxs("button",{onClick:i,type:"button",children:[H.jsx("span",{children:String(c+1).padStart(2,"0")}),H.jsxs("div",{children:[H.jsx("strong",{children:l.project}),H.jsxs("small",{children:[l.department," · ",l.user]})]}),H.jsxs("time",{children:[H.jsx(kx,{size:10}),l.checkout.slice(0,10)]})]},`${l.project}-${l.checkout}`))}),H.jsx("div",{className:"inspection-spacer","aria-hidden":"true"}),H.jsxs("div",{className:"inspection-footer",children:[H.jsx(lC,{instrument:s,onOpen:i}),H.jsxs("div",{children:[H.jsxs("span",{children:[H.jsx(g1,{size:13})," 设备履历二维码"]}),H.jsx("strong",{children:s.managementNo}),H.jsx("small",{children:"点击查看完整使用记录"})]})]})]})}function hC(){const s=cC(),[t,i]=rn.useState(""),[r,l]=rn.useState("all"),[c,f]=rn.useState(null),[h,m]=rn.useState(null),[p,_]=rn.useState(null),[v,g]=rn.useState(null),[x,M]=rn.useState(null),A=Vi.find(U=>U.id===h),E=rn.useMemo(()=>({stored:Vi.filter(U=>U.status==="stored").length,checkedOut:Vi.filter(U=>U.status==="checkedOut").length,maintenance:Vi.filter(U=>U.status==="maintenance").length,dueSoon:Vi.filter(U=>U.nextCalibration<="2026-09-01").length}),[]),S=rn.useMemo(()=>{const U=t.trim().toLowerCase();return Vi.filter(b=>{const N=r==="all"||b.status===r,G=!U||b.id.toLowerCase().includes(U)||b.lockerNo.toLowerCase().includes(U)||b.cabinetGroup.toLowerCase().includes(U)||b.equipmentName.toLowerCase().includes(U)||b.managementNo.toLowerCase().includes(U)||b.model.toLowerCase().includes(U);return N&&G})},[t,r]),P=U=>{const b=Vi.find(N=>N.id===U);b&&(f(b.bankId),m(U),_(U),g(null))},O=U=>{f(U),m(null),_(null),g(null)},C=U=>{P(U)},z=()=>{if(p){_(null),m(null),g(null);return}c&&(f(null),m(null),g(null))},D=async()=>{var U,b,N;if(document.fullscreenElement){await((U=document.exitFullscreen)==null?void 0:U.call(document));return}await((N=(b=document.documentElement).requestFullscreen)==null?void 0:N.call(b))};return H.jsxs("div",{className:"app-shell",children:[H.jsxs("header",{className:"command-header",children:[H.jsxs("div",{className:"brand",children:[H.jsx("div",{className:"brand-mark",children:H.jsx(K_,{size:24})}),H.jsxs("div",{children:[H.jsx("h1",{children:"仪器柜数字孪生系统"}),H.jsx("span",{children:"设备室管理员：李一凡 · 部门：信息技术部 · 电话：13012313382"})]})]}),H.jsxs("div",{className:"top-metrics",children:[H.jsx(ol,{icon:K_,label:"设备总数",tone:"steel",value:Vi.length}),H.jsx(ol,{icon:o1,label:"存放中",tone:"green",value:E.stored}),H.jsx(ol,{icon:d1,label:"检测外借",tone:"red",value:E.checkedOut}),H.jsx(ol,{icon:w1,label:"检修中",tone:"amber",value:E.maintenance}),H.jsx(ol,{icon:Vx,label:"近期检定",tone:"cyan",value:E.dueSoon})]}),H.jsxs("div",{className:"header-tools",children:[H.jsxs("div",{className:"clock",children:[H.jsx(Xx,{size:14}),H.jsx("strong",{children:s.toLocaleTimeString("zh-CN",{hour12:!1})}),H.jsx("span",{children:s.toLocaleDateString("zh-CN",{year:"numeric",month:"2-digit",day:"2-digit"})})]}),H.jsx("button",{onClick:D,type:"button","aria-label":"进入全屏",title:"进入全屏",children:H.jsx(p1,{size:17})}),H.jsx("button",{type:"button","aria-label":"系统设置",title:"系统设置",children:H.jsx(E1,{size:17})})]})]}),H.jsxs("div",{className:"workspace",children:[H.jsx(uC,{filteredInstruments:S,onQueryChange:i,onSelect:P,onStatusChange:l,query:t,selectedBankId:c,selectedId:h,statusFilter:r}),H.jsxs("main",{className:"scene-workspace",children:[H.jsxs("div",{className:"scene-header",children:[H.jsxs("div",{children:[H.jsx("span",{children:"智能仪器柜组"}),H.jsx("small",{children:p&&A?`${A.lockerNo} · 设备资料已定位`:c?"整面柜墙已聚焦 · 点击设备格口查看资料":"3 面柜墙 · 133 个有效格口 · 1 块纵向中控屏"})]}),H.jsxs("div",{className:"scene-header__status",children:[v&&H.jsxs("span",{children:["当前指向"," ",v.type==="bank"?"仪器柜墙":v.lockerNo]}),H.jsx("i",{}),"WebGL 实时渲染"]})]}),H.jsxs("div",{className:"scene-stage",children:[H.jsx(zR,{instruments:Vi,onBankClick:O,onHover:g,onLockerClick:C,openId:p,selectedBankId:c,selectedId:h}),H.jsxs("div",{className:"scene-brackets","aria-hidden":"true",children:[H.jsx("i",{}),H.jsx("i",{}),H.jsx("i",{}),H.jsx("i",{})]}),H.jsx("div",{className:"scene-scanline","aria-hidden":"true"}),H.jsxs("div",{className:"sync-indicator",children:[H.jsx(QM,{size:13}),H.jsx("span",{children:p?"格口数据扫描":"业务状态同步"}),H.jsx("strong",{children:p?"已定位":"正常"})]}),c&&!p&&H.jsx("button",{className:"scene-back-button",onClick:z,type:"button","aria-label":"返回仪器室总览",title:"返回仪器室总览",children:H.jsx(Gx,{size:16})}),p&&A&&H.jsx(dC,{instrument:A,onBack:z,onOpenHistory:()=>M(A)})]})]}),H.jsx(z1,{instruments:Vi,selectedBankId:c})]}),H.jsxs("footer",{className:"system-footer",children:[H.jsxs("span",{children:[H.jsx("i",{className:"ok"})," 系统状态：正常"]}),H.jsxs("span",{children:[H.jsx("i",{className:"ok"})," 数据连接：演示数据"]}),H.jsx("span",{children:"三维场景刷新：60 FPS"}),H.jsx("span",{children:"状态刷新间隔：10 秒"}),H.jsx("span",{children:"当前用户：设备管理员"}),H.jsx("span",{children:"版本：v1.0.0"})]}),H.jsx(F1,{instrument:x,onClose:()=>M(null)})]})}kM.createRoot(document.getElementById("root")).render(H.jsx(rn.StrictMode,{children:H.jsx(hC,{})}));

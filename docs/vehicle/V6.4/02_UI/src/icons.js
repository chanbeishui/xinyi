/* 原生 SVG 图标库 —— 全部矢量，任意倍率导出不失真 */
var ICONS = {
  /* 系统导航 */
  'back':'<path d="M15.5 4.5L8 12l7.5 7.5"/>',
  'chev':'<path d="M9.5 5.5L16 12l-6.5 6.5"/>',
  'chev-d':'<path d="M6 9.5l6 6 6-6"/>',
  'close':'<path d="M6 6l12 12M18 6L6 18"/>',
  'search':'<circle cx="11" cy="11" r="7"/><path d="M16.5 16.5L21 21"/>',
  'more':'<circle cx="5" cy="12" r="1.4"/><circle cx="12" cy="12" r="1.4"/><circle cx="19" cy="12" r="1.4"/>',
  'add':'<path d="M12 5v14M5 12h14"/>',
  'check':'<path d="M4.5 12.5l5 5 10-11"/>',
  'filter':'<path d="M4 6h16M7 12h10M10 18h4"/>',
  'refresh':'<path d="M20 11a8 8 0 0 0-13.7-5.3L3 9"/><path d="M3 4v5h5"/><path d="M4 13a8 8 0 0 0 13.7 5.3L21 15"/><path d="M21 20v-5h-5"/>',
  'share':'<path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7"/><path d="M16 6l-4-4-4 4"/><path d="M12 2v13"/>',
  'download':'<path d="M12 3v12"/><path d="M7 11l5 5 5-5"/><path d="M4 19h16"/>',
  'edit':'<path d="M4 20h4l10-10a2.5 2.5 0 0 0-3.5-3.5L4.5 16.5 4 20z"/>',
  'delete':'<path d="M4 7h16"/><path d="M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/><path d="M6.5 7l1 13a1 1 0 0 0 1 1h7a1 1 0 0 0 1-1l1-13"/>',
  'eye':'<path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z"/><circle cx="12" cy="12" r="3"/>',
  'lock':'<rect x="4.5" y="10.5" width="15" height="10" rx="2"/><path d="M8 10.5V8a4 4 0 0 1 8 0v2.5"/>',
  'bell':'<path d="M18 9a6 6 0 0 0-12 0c0 5-2 6-2 6h16s-2-1-2-6z"/><path d="M13.7 20a2 2 0 0 1-3.4 0"/>',
  'setting':'<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.6 1.6 0 0 0-2.7 1.1v.3a2 2 0 1 1-4 0v-.2a1.6 1.6 0 0 0-1-1.5 1.6 1.6 0 0 0-1.8.4l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.6 1.6 0 0 0-1.1-2.7H3a2 2 0 1 1 0-4h.2a1.6 1.6 0 0 0 1.5-1 1.6 1.6 0 0 0-.4-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.6 1.6 0 0 0 1.8.3H9a1.6 1.6 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.2a1.6 1.6 0 0 0 2.7 1.1l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.6 1.6 0 0 0-.3 1.8V9a1.6 1.6 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.2a1.6 1.6 0 0 0-1.4 1z"/>',
  'help':'<circle cx="12" cy="12" r="9"/><path d="M9.2 9.2a3 3 0 0 1 5.8 1c0 2-3 2.5-3 4.3"/><circle cx="12" cy="17.5" r=".9" fill="currentColor" stroke="none"/>',
  /* 提示 */
  'info':'<circle cx="12" cy="12" r="9"/><path d="M12 11v5.5"/><circle cx="12" cy="7.8" r=".9" fill="currentColor" stroke="none"/>',
  'warn':'<path d="M10.6 4.2L2.9 17.5a2 2 0 0 0 1.7 3h14.8a2 2 0 0 0 1.7-3L13.4 4.2a2 2 0 0 0-2.8 0z"/><path d="M12 9.5v4"/><circle cx="12" cy="17" r=".9" fill="currentColor" stroke="none"/>',
  'err':'<circle cx="12" cy="12" r="9"/><path d="M15 9l-6 6M9 9l6 6"/>',
  'empty':'<circle cx="12" cy="12" r="9"/><path d="M8.5 14.5s1.3-1.5 3.5-1.5 3.5 1.5 3.5 1.5"/><path d="M9 9.5h.01M15 9.5h.01"/>',
  /* 状态栏 */
  'signal':'<rect x="2.5" y="14" width="3" height="6" rx="1" fill="currentColor" stroke="none"/><rect x="7" y="11" width="3" height="9" rx="1" fill="currentColor" stroke="none"/><rect x="11.5" y="7.5" width="3" height="12.5" rx="1" fill="currentColor" stroke="none"/><rect x="16" y="4.5" width="3" height="15.5" rx="1" fill="currentColor" opacity=".35" stroke="none"/>',
  'wifi':'<path d="M2.5 8.5a15 15 0 0 1 19 0"/><path d="M6 12.2a10 10 0 0 1 12 0"/><path d="M9.4 15.8a5 5 0 0 1 5.2 0"/><circle cx="12" cy="19" r="1.1" fill="currentColor" stroke="none"/>',
  'battery':'<rect x="2" y="7.5" width="18" height="9" rx="2.2"/><rect x="4.5" y="9.8" width="13" height="4.4" rx="1" fill="currentColor" stroke="none" opacity=".85"/><path d="M21.5 10.5v3"/>',
  /* 业务 */
  'car':'<path d="M4.5 15.5h15"/><path d="M5.5 15.5V11l2-4.2h9l2 4.2v4.5"/><path d="M4.5 15.5V19h15v-3.5"/><path d="M7.5 11h9"/><circle cx="8" cy="17.6" r="1.2"/><circle cx="16" cy="17.6" r="1.2"/>',
  'key':'<circle cx="8" cy="12" r="3.6"/><path d="M11.5 12H20"/><path d="M17 12v3.5M19.5 12v2.5"/>',
  'calendar':'<rect x="3.5" y="5" width="17" height="16" rx="2.5"/><path d="M3.5 9.5h17"/><path d="M8 3v3M16 3v3"/>',
  'clock':'<circle cx="12" cy="12" r="9"/><path d="M12 6.8V12l3.4 2.2"/>',
  'pin':'<path d="M12 21s6.5-6 6.5-10.4A6.5 6.5 0 0 0 5.5 10.6C5.5 15 12 21 12 21z"/><circle cx="12" cy="10.4" r="2.4"/>',
  'phone':'<path d="M6.5 3.5h3l1.5 4-2 1.4a12 12 0 0 0 6.1 6.1l1.4-2 4 1.5v3a2 2 0 0 1-2.2 2A17.5 17.5 0 0 1 4.5 5.7 2 2 0 0 1 6.5 3.5z"/>',
  'user':'<circle cx="12" cy="8" r="3.8"/><path d="M4.5 20.5a7.5 7.5 0 0 1 15 0"/>',
  'users':'<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M16 4.6a3.5 3.5 0 0 1 0 6.8"/><path d="M17.5 14.2A6.5 6.5 0 0 1 21.5 20"/>',
  'doc':'<path d="M6 3.5h8l4.5 4.5V20a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4.5a1 1 0 0 1 1-1z"/><path d="M14 3.5V8h4.5"/><path d="M8.5 13h7M8.5 16.5h5"/>',
  'file':'<path d="M13.5 3.5H7a1 1 0 0 0-1 1v15a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V8z"/><path d="M13.5 3.5V8H18"/>',
  'camera':'<path d="M4 8.5h3l1.6-2.5h6.8L17 8.5h3a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1z"/><circle cx="12" cy="13.5" r="3.4"/>',
  'image':'<rect x="3.5" y="4.5" width="17" height="15" rx="2.5"/><circle cx="8.5" cy="9.5" r="1.7"/><path d="M4 17.5l5-4.6 3.8 3.4 3.2-3 4 3.6"/>',
  'shield':'<path d="M12 3.2l7 2.6v6c0 4.3-3 7.6-7 9-4-1.4-7-4.7-7-9v-6z"/><path d="M9 12l2 2 4-4"/>',
  'money':'<rect x="2.5" y="6" width="19" height="12" rx="2.5"/><circle cx="12" cy="12" r="2.6"/><path d="M6 12h.01M18 12h.01M12 6.5h.01M12 17.5h.01"/>',
  'card':'<rect x="2.5" y="5" width="19" height="14" rx="2.5"/><path d="M2.5 9.5h19"/><path d="M6.5 14.5h4"/>',
  'wallet':'<path d="M3.5 7.5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-14a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2z"/><path d="M3.5 7.5V6.4A1.7 1.7 0 0 1 5.2 4.5h11"/><circle cx="16.5" cy="13.5" r="1.3"/>',
  'gift':'<rect x="3.5" y="9" width="17" height="11" rx="2"/><path d="M3.5 13.5h17"/><path d="M12 9v11"/><path d="M12 9S10.5 4 8 4a2.3 2.3 0 0 0 0 5M12 9s1.5-5 4-5a2.3 2.3 0 0 1 0 5"/>',
  'chart':'<path d="M4 20V9.5M10 20V4.5M16 20v-7M22 20H2"/>',
  'history':'<path d="M3.5 12a8.5 8.5 0 1 0 2.6-6.1L3 9"/><path d="M3 4v5h5"/><path d="M12 7.5V12l3 2"/>',
  'home':'<path d="M4 10.5L12 4l8 6.5V20a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z"/><path d="M9.5 21v-6h5v6"/>',
  'order':'<rect x="4" y="3.5" width="16" height="17" rx="2.5"/><path d="M8 8.5h8M8 12.5h8M8 16.5h4"/>',
  'tool':'<path d="M14.5 6.5a3.5 3.5 0 0 1 4.5 4.5l-9 9a2.5 2.5 0 0 1-3.5-3.5z"/><path d="M14.5 6.5L18 3.2A5.5 5.5 0 0 0 20.8 9l-3.3 1.5z"/>',
  'alert':'<path d="M12 3.2l9 15.8H3z"/><path d="M12 9v4.5"/><circle cx="12" cy="16.5" r=".9" fill="currentColor" stroke="none"/>',
  'police':'<path d="M4.5 11.5v5h15v-5z"/><path d="M7.5 6.5h9l3 5H4.5z"/><path d="M8 11.5v3M16 11.5v3"/><path d="M6 20h12"/>',
  'rescue':'<circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="3.6"/><path d="M12 3v2.5M12 18.5V21M3 12h2.5M18.5 12H21M5.6 5.6l1.8 1.8M16.6 16.6l1.8 1.8M18.4 5.6l-1.8 1.8M7.4 16.6l-1.8 1.8"/>',
  'support':'<circle cx="12" cy="12" r="9"/><path d="M8.5 14.5h7"/><path d="M12 14.5v3.5"/><circle cx="12" cy="9.5" r="2.4"/>',
  'flash':'<path d="M13 3L5 13.5h6L10.5 21l8-10.5h-6z"/>',
  'flag':'<path d="M5.5 21V4"/><path d="M5.5 4.8h11l-2 3.6 2 3.6h-11"/>',
  'steering':'<circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="3.2"/><path d="M12 6.8v2.4M6.9 13.5H4.5M17.1 13.5h2.4M12 21v-2"/>',
  'dash':'<circle cx="12" cy="12" r="9"/><path d="M12 12l3.6-2.8"/><path d="M6.5 15.5a6 6 0 0 1 11 0"/>',
  'percent':'<circle cx="7.5" cy="7.5" r="2.6"/><circle cx="16.5" cy="16.5" r="2.6"/><path d="M19 5L5 19"/>',
  'truck':'<path d="M2.5 6.5h11v9h-11z"/><path d="M13.5 10h4l4 3.5v2h-8z"/><circle cx="7" cy="18" r="1.8"/><circle cx="17.5" cy="18" r="1.8"/>',
  'building':'<rect x="4" y="3.5" width="16" height="17" rx="2"/><path d="M8.5 8h2M13.5 8h2M8.5 12h2M13.5 12h2"/><path d="M10.5 20.5v-4h3v4"/>',
  'idcard':'<rect x="2.5" y="5.5" width="19" height="13" rx="2.5"/><rect x="5.5" y="9" width="4.6" height="6" rx="1"/><path d="M13 9.5h6M13 13h4"/>',
  'msg':'<path d="M20.5 12a8 8 0 0 1-11.6 7.1L4 20.5l1.4-4.9A8 8 0 1 1 20.5 12z"/>',
  'outlet':'<rect x="3.5" y="2.5" width="17" height="19" rx="2.5"/><path d="M9 15.5h6"/><path d="M12 11.5v4"/>',
  'scan':'<path d="M3.5 8.5V6a2 2 0 0 1 2-2h2.5M15.5 4H18a2 2 0 0 1 2 2v2.5M20.5 15.5V18a2 2 0 0 1-2 2h-2.5M8.5 20H6a2 2 0 0 1-2-2v-2.5"/><path d="M3.5 12h17"/>',
  'qr':'<rect x="3.5" y="3.5" width="6" height="6" rx="1.5"/><rect x="14.5" y="3.5" width="6" height="6" rx="1.5"/><rect x="3.5" y="14.5" width="6" height="6" rx="1.5"/><path d="M14.5 14.5h3v3h-3zM20.5 14.5v.01M14.5 20.5h.01M17.5 20.5h.01M20.5 20.5h.01M20.5 17.5h.01"/>',
  'copy':'<rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/>',
  'travel':'<path d="M2.5 12h17"/><path d="M5 12a7 7 0 0 0 14 0 7 7 0 0 0-14 0z"/><circle cx="12" cy="12" r="2.5"/>',
  /* 微信胶囊右侧「退出」圆点 */
  'exit':'<circle cx="12" cy="12" r="7.4"/><circle cx="12" cy="12" r="2.4" fill="currentColor" stroke="none"/>',
  'capsule-dot':'<circle cx="5" cy="12" r="1.6" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.6" fill="currentColor" stroke="none"/><circle cx="19" cy="12" r="1.6" fill="currentColor" stroke="none"/>'
};

function ico(name, size){
  var d = ICONS[name];
  if(!d){ d = ICONS.empty; }
  var s = size || 22;
  var w = s < 12 ? 2.4 : (s < 16 ? 2.1 : 1.8);
  return '<svg width="'+s+'" height="'+s+'" viewBox="0 0 24 24" fill="none" stroke="currentColor" '+
    'stroke-width="'+w+'" stroke-linecap="round" stroke-linejoin="round" style="flex:0 0 auto">'+d+'</svg>';
}
function icoc(name, size, color){
  return '<span style="color:'+(color||'currentColor')+';display:inline-flex">'+ico(name,size)+'</span>';
}

/* CREAZZY SPACE 1.1 — enhancement layer; no dependencies */
(() => {
  const $ = id => document.getElementById(id);
  const log = (action, detail = '') => {
    try {
      const list = JSON.parse(localStorage.getItem('creazzy-space-activity') || '[]');
      list.unshift({ at: new Date().toISOString(), action, detail: String(detail) });
      localStorage.setItem('creazzy-space-activity', JSON.stringify(list.slice(0, 100)));
    } catch {}
  };
  const download = (name, data, type) => {
    const url = URL.createObjectURL(new Blob([data], { type }));
    const a = document.createElement('a');
    a.href = url; a.download = name; a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1200);
  };
  $('csvExport')?.addEventListener('click', () => {
    const rows = [['id','name','app','category','url','note','active','createdAt'],
      ...items.map(x => [x.id,x.name,x.app,x.category,x.url,x.note,x.active ? 'true' : 'false',new Date(x.createdAt || Date.now()).toISOString()])];
    const csv = '\\uFEFF' + rows.map(row => row.map(v => '"' + String(v ?? '').replace(/"/g, '""') + '"').join(',')).join('\\r\\n');
    download('creazzy-space.csv', csv, 'text/csv;charset=utf-8');
    log('تصدير CSV', items.length + ' مساحة'); toast('تم تصدير جدول CSV');
  });
  $('launchActive')?.addEventListener('click', () => {
    const urls = items.filter(x => x.active && /^https?:\\/\\//i.test(x.url || ''));
    if (!urls.length) return toast('لا توجد روابط صالحة لمساحات نشطة');
    if (!confirm('سيتم فتح ' + urls.length + ' رابطاً في علامات تبويب جديدة. متابعة؟')) return;
    urls.forEach((x, i) => setTimeout(() => window.open(x.url, '_blank', 'noopener,noreferrer'), i * 220));
    log('فتح الروابط النشطة', urls.length + ' رابط');
    toast('تم طلب فتح الروابط؛ قد يمنع المتصفح بعض النوافذ المنبثقة');
  });
  $('activityBtn')?.addEventListener('click', () => {
    try {
      const list = JSON.parse(localStorage.getItem('creazzy-space-activity') || '[]');
      alert(list.length ? list.slice(0, 30).map(x => new Date(x.at).toLocaleString('ar-EG') + ' — ' + x.action + ' — ' + x.detail).join('\\n') : 'لا يوجد نشاط مسجل بعد.');
    } catch { alert('تعذر قراءة سجل النشاط'); }
  });
  const template = async (name, app, category, note, url = '') => {
    const item = { id: 'space-' + crypto.randomUUID(), name, app, category, note, url, active: true, createdAt: Date.now() };
    items.push(item); await persist(); log('إنشاء قالب', name); render(); toast('تم إنشاء القالب: ' + name);
  };
  $('templateAI')?.addEventListener('click', () => template('AI Team','مجموعة أدوات الذكاء الاصطناعي','ذكاء اصطناعي','مساحة لفصل روابط وأدوات العمل','https://chatgpt.com/'));
  $('templateSocial')?.addEventListener('click', () => template('Social Campaigns','إدارة الحملات والمحتوى','اجتماعي','مساحة لتنظيم الحملات وروابط المنصات'));
  $('templateStudy')?.addEventListener('click', () => template('Study Desk','تعلم ومراجع','عمل وإنتاجية','مساحة للدراسة والملاحظات والمصادر'));
  $('templateFiles')?.addEventListener('click', () => template('File Archive','أرشيف ملفات ومستندات','ملفات ومستندات','قائمة تنظيمية للأرشيف والمراجع'));
  // Add a clone action to every card without modifying the core renderer.
  const addCloneButtons = () => {
    document.querySelectorAll('.instance').forEach(card => {
      const actions = card.querySelector('.inst-actions');
      if (!actions || actions.querySelector('[data-clone-space]')) return;
      const edit = actions.querySelector('button[onclick*="editItem"]');
      if (!edit) return;
      const idMatch = edit.getAttribute('onclick')?.match(/editItem\\('([^']+)'\\)/);
      if (!idMatch) return;
      const clone = document.createElement('button');
      clone.className = 'btn ghost'; clone.textContent = 'نسخ'; clone.dataset.cloneSpace = '1';
      clone.addEventListener('click', async () => {
        const source = items.find(x => x.id === idMatch[1]); if (!source) return;
        const copy = { ...source, id: 'space-' + crypto.randomUUID(), name: (source.name + ' — نسخة').slice(0, 70), createdAt: Date.now() };
        items.push(copy); await persist(); log('نسخ مساحة', copy.name); render(); toast('تم نسخ إعدادات المساحة');
      });
      actions.insertBefore(clone, edit.nextSibling);
    });
  };
  new MutationObserver(addCloneButtons).observe($('instances'), { childList: true, subtree: true });
  addCloneButtons();
  document.addEventListener('keydown', e => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') { e.preventDefault(); $('search')?.focus(); }
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'n') { e.preventDefault(); $('addMain')?.click(); }
    if (e.key === 'Escape') $('cancel')?.click();
  });
})();
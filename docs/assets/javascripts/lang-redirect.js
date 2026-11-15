// Redirects to the page in the preferred language.
// The preferred language is the one the viewer last chose in the language selector,
// or, if never chosen, the browser's language (Japanese -> /ja/, anything else -> English).
(function () {
  var KEY = "ecuacion-docs-lang";
  var LANGS = ["en", "ja"];
  var DEFAULT = "en";

  function stored() {
    try {
      return localStorage.getItem(KEY);
    } catch (e) {
      return null;
    }
  }

  function store(lang) {
    try {
      localStorage.setItem(KEY, lang);
    } catch (e) {
      // ignore (private mode, blocked storage)
    }
  }

  function browserLang() {
    var list = navigator.languages || [navigator.language || ""];
    for (var i = 0; i < list.length; i++) {
      var code = String(list[i]).toLowerCase().split("-")[0];
      if (LANGS.indexOf(code) >= 0) return code;
    }
    return DEFAULT;
  }

  // Remember the language explicitly chosen in the Material language selector.
  document.addEventListener("click", function (e) {
    var a = e.target.closest && e.target.closest("a[hreflang]");
    if (a && LANGS.indexOf(a.getAttribute("hreflang")) >= 0) {
      store(a.getAttribute("hreflang"));
    }
  });

  // __md_scope is the site root URL, defined by Material for MkDocs.
  if (typeof __md_scope === "undefined") return;
  var root = __md_scope.pathname;
  var path = location.pathname;
  if (path.indexOf(root) !== 0) return;

  var rest = path.substring(root.length);
  var current = DEFAULT;
  for (var i = 0; i < LANGS.length; i++) {
    if (LANGS[i] !== DEFAULT && (rest === LANGS[i] || rest.indexOf(LANGS[i] + "/") === 0)) {
      current = LANGS[i];
      rest = rest.substring(LANGS[i].length).replace(/^\//, "");
    }
  }

  var wanted = stored() || browserLang();
  if (wanted === current) return;

  var target = root + (wanted === DEFAULT ? "" : wanted + "/") + rest;
  location.replace(target + location.search + location.hash);
})();

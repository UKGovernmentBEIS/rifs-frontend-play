(function() {
    var helpers = document.getElementsByClassName("helptext");
    for (var i in helpers) {
        var helper = helpers[i];
        var input = document.getElementById(helper.getAttribute('data-for')),
            rule = getRule(helper.getAttribute('data-rule')),
            config = JSON.parse(helper.getAttribute('data-ruleconfig') || "{}");

        if (!input || !rule) continue;

        input.addEventListener("keyup", makeCallback(input, helper, rule, config))
    }

    function makeCallback(input, helper, rule, config) {
        return function() {
            var output = rule(input.value, config);
            helper.innerHTML = output;
        }
    }

    function getRule(ruleName) {
        switch (ruleName) {
            case "WordCountRule": return function(value, config) {
                var trimmed = value.replace(/^\s+|\s+$/gm,'');
                if(!trimmed) return "" + config.maxWords + " " + (config.maxWords == 1 ? "word" : "words") + " maximum"
                var w = trimmed.split(/\s+/).length;
                if (w <= config.maxWords) return "Words remaining: " + (config.maxWords - w);
                return "" + (w - config.maxWords) + " " + (w - config.maxWords == 1 ? "word" : "words") + " over limit"
            };
            default: return null;
        }
    }
})();
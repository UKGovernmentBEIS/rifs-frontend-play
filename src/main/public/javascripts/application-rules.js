(function() {
    var rules = {
        "WordCountRule": function(value, config) {
             var trimmed = value.replace(/^\s+|\s+$/gm,'');
             if(!trimmed) {return "" + config.maxWords + " " + (config.maxWords === 1 ? "word" : "words") + " maximum";}
             var w = trimmed.split(/\s+/).length;
             if (w <= config.maxWords) {return "Words remaining: " + (config.maxWords - w);}
             return "" + (w - config.maxWords) + " " + (w - config.maxWords === 1 ? "word" : "words") + " over limit";
        }
    }

    function addInputListener(input, helper, rule, config) {
        input.addEventListener("keyup", function() {
            var output = rule(input.value, config);
            helper.innerHTML = output;
        })
    }

    function rifsHelperText() {
        if(!document.getElementsByClassName || !document.body.addEventListener) {return;}

        var helpers = document.getElementsByClassName("helptext");
        if (helpers.length == 0) return;
        for (var i = 0; i < helpers.length; i++) {
            var helper = helpers[i];
            var input = document.getElementById(helper.getAttribute("data-for")),
                rule = rules[helper.getAttribute("data-rule")] || null,
                config = JSON.parse(helper.getAttribute("data-ruleconfig") || "{}");

            if (!input || !rule) {continue;}
            addInputListener(input, helper, rule, config);
        }
    }

    window.rifsHelperText = rifsHelperText;

    rifsHelperText();
}());
(function() {
    "use strict";

    Array.prototype.clean = function(deleteValue) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == deleteValue) {
                this.splice(i, 1);
                i--;
            }
        }
        return this;
    };

    if(!document.getElementsByClassName || !document.body.addEventListener) {return;}

    var rules = {
        WordCount: function WordCount(value, config) {
             var w = value.split(/\s+/).clean("").length;
             var words = function(count) {return count === 1 ? "word" : "words";};

             return w === 0  ? "" + config.maxWords + " " + words(config.maxWords) + " maximum"
                  : w <= config.maxWords ? "Words remaining: " + (config.maxWords - w)
                  : "" + (w - config.maxWords) + " " + words(w - config.maxWords) + " over limit";
        }
    };

    function addInputListener(input, helper, rule, config) {
        if (!input || !rule) {return;}

        input.addEventListener("keyup", function() {
            var output = rule(input.value, config);
            helper.innerHTML = output;
        });
    }

    function rifsHelperText() {
        var helpers = document.getElementsByClassName("js__hint");
        for (var i = 0; i < helpers.length; i++) {
            var helper = helpers[i];
            var input = document.getElementById(helper.getAttribute("data-for")),
                rule = rules[helper.getAttribute("data-rule")] || null,
                config = JSON.parse(helper.getAttribute("data-ruleconfig") || "{}");

            addInputListener(input, helper, rule, config);
        }
    }

    window.rifsHelperText = rifsHelperText;

    rifsHelperText();
}());

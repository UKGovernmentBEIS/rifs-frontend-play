(function() {
    'use strict';

    Array.prototype.clean = function(deleteValue) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] === deleteValue) {
                this.splice(i, 1);
                i--;
            }
        }
        return this;
    };

    if(!document.getElementsByClassName || !document.body.addEventListener) {return;}

    var rules = {
        WordCount: function WordCount(value, config) {
             var w = value.split(/\s+/).clean('').length;
             var words = function(count) {return count === 1 ? 'word' : 'words';};

             return w === 0  ? '' + config.maxWords + ' ' + words(config.maxWords) + ' maximum'
                  : w <= config.maxWords ? 'Words remaining: ' + (config.maxWords - w)
                  : '' + (w - config.maxWords) + ' ' + words(w - config.maxWords) + ' over limit';
        }
    };

    function addInputListener(input, helper, rule, config) {
        // TODO: get this working in IE8
        if (!input || !rule) {return;}

        input.addEventListener('keyup', function() {
            var output = rule(input.value, config);
            helper.innerHTML = output;
        });
    }

    function rifsHelperText() {
        var helpers = document.getElementsByClassName('js__hint');
        for (var i = 0; i < helpers.length; i++) {
            var helper = helpers[i];
            var input = document.getElementById(helper.getAttribute('data-for')),
                rule = rules[helper.getAttribute('data-rule')] || null,
                config = JSON.parse(helper.getAttribute('data-ruleconfig') || '{}');

            addInputListener(input, helper, rule, config);
        }
    }

    window.rifsHelperText = rifsHelperText;

    rifsHelperText();

}());

$(document).ready(function () {
    jQuery.fx.off = true;
    var GOVUK = window.GOVUK || {};

    // Don't enhance the selection buttons on IE8 as it can't handle the javascript.
    if (navigator.appVersion.indexOf('MSIE 8.') === -1) {
        var selectionButtons = new GOVUK.SelectionButtons($('.block-label input[type="radio"], .block-label input[type="checkbox"]'));
    }

    // Turn the tabs on if the correct structures exist in the page
    var e = $('section.more');
    e.find('.js-tabs').length && e.tabs();

    // $('details').details();

    $('.js-hide-on-load').hide();

    // Trigger Show/Hide events
    $('.js-show').click(function(){
       var selector = $(this).attr('data-for');
       var el = $(selector);
       el.show();
    });

    $('.js-hide').click(function(){
        var selector = $(this).attr('data-for');
        var el = $(selector);
        el.hide();
    });
});
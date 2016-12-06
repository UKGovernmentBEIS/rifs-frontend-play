function triggerEvent(element, eventName) {
  var event; // The custom event that will be created

  if (document.createEvent) {
    event = document.createEvent('HTMLEvents');
    event.initEvent(eventName, true, true);
  } else {
    event = document.createEventObject();
    event.eventType = eventName;
  }

  event.eventName = eventName;

  if (document.createEvent) {
    element.dispatchEvent(event);
  } else {
    element.fireEvent('on' + event.eventType, event);
  }
}

function testWordCountHelper(message, value) {
    QUnit.test('message for ' + value + ': ' + message, function(assert) {
      document.getElementById('qunit-fixture').innerHTML = '<textarea id="test"></textarea><span class="js__hint" id="testhelptext" data-for="test" data-rule="WordCount" data-ruleconfig=\'{"maxWords":50}\'></span>';
      window.rifsHelperText();

      document.getElementById('test').value = value;
      triggerEvent(document.getElementById('test'), 'keyup');
      assert.equal(document.getElementById('testhelptext').innerHTML, message);
    });
}

function repeat(word, times) {
    var rtn = '';
    for (var i =0; i < times; i++) {rtn += word;}
    return rtn;
}




// Boundary tests
testWordCountHelper('50 words maximum', '');
testWordCountHelper('Words remaining: 46', 'These are four words');
testWordCountHelper('Words remaining: 1', repeat('word ', 49));
testWordCountHelper('Words remaining: 0', repeat('word ', 50));
testWordCountHelper('1 word over limit', repeat('word ', 51));
testWordCountHelper('10 words over limit', repeat('word ', 60));

//whitespace tolerance
testWordCountHelper('Words remaining: 46', ' These are four words');
testWordCountHelper('Words remaining: 46', 'These are four words  ');
testWordCountHelper('Words remaining: 46', '\nThese are four words.....&hellip;  ');
testWordCountHelper('Words remaining: 46', 'These are four words.....still');

//UTF
testWordCountHelper('Words remaining: 46', 'These are föur wörds');

// GF-1418
testWordCountHelper('Words remaining: 37', 'one two \nthree four\n       five       six\r\n\n\n\n\nseven\r\n\n\neight 0 ten [] "" thirteen');

// TODO: we need to test the application-rules.js file more cleanly: it has several dependencies at present.
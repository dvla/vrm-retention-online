require.config({
    paths: {
        'jquery': '../lib/jquery/jquery'
    }
});

require(["on-load-conditional-display", "on-event-change-display", "main"],function(onLoadConditionalDisplay, onEventChangeDisplay, main) {
    // Passing Parameters to Require.js module
    // https://stackoverflow.com/questions/17382291/passing-parameters-to-require-js-module
    var isKeeperEmailDisplayedOnLoad = $('#supply-email_true').is(':checked'); // Read the radio button value.
    onLoadConditionalDisplay.foo('keeper-email-wrapper', isKeeperEmailDisplayedOnLoad);
    onEventChangeDisplay.foo('supply-email_true', 'keeper-email-wrapper', true);
    onEventChangeDisplay.foo('supply-email_false', 'keeper-email-wrapper', false);
});
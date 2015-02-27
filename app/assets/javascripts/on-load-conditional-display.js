require.config({
    paths: {
        'jquery': 'lib/jquery/jquery-1.9.1',
        'jquery-migrate': 'lib/jquery/jquery-migrate-1.2.1.min'
    }
});

define(["jquery", "jquery-migrate"], function($) {
    return {
        foo: function(id, isDisplayed) {
            var element = $('#' + id);
            if (isDisplayed) {
                element.show();
            } else {
                element.hide();
            }
        }
    }
});
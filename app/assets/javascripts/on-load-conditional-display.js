define(["jquery"], function($) {
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
define(["jquery"], function($) {
    return {
        /*
         * Display a field that fires when the link or button is clicked.
         *
         * ids - The ids of the links/buttons you want to track.
         */
        foo: function(idOfClickable, idOfDisplayable, isDisplayed) {
            $('#' + idOfClickable).click(function() {
                var elementOfDisplayable = $('#' + idOfDisplayable );
                /*console.debug("onEventChangeDisplay addEventListener so on event change id: " + idOfDisplayable + " isDisplayed: " + isDisplayed);*/
                if (isDisplayed) {
                    elementOfDisplayable.show();
                } else {
                    elementOfDisplayable.hide();
                }
            });
        }
    }
});
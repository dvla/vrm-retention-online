// Define the dependency to page-init in common
define(['jquery', 'jquery-migrate', "page-init"], function($, jqueryMigrate, pageInit) {

    return {
        init: function() {
            // Call initAll on the pageInit object to run all the common js in vehicles-presentation-common
            pageInit.initAll();

            // If JS enabled hide summary details
            $('.details').hide();

            // Summary details toggle
            $('.summary').on('click', function() {
                $(this).siblings().toggle();
                $(this).toggleClass('active');
            });
        }
    }
});

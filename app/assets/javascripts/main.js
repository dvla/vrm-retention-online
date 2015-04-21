require.config({
    paths: {
        'jquery' : '../lib/jquery/jquery',
        'jquery-migrate': '../lib/jquery-migrate/jquery-migrate',
        'header-footer-only': '../lib/vehicles-presentation-common/javascripts/header-footer-only',
        'global-helpers': '../lib/vehicles-presentation-common/javascripts/global-helpers',
        'page-init': '../lib/vehicles-presentation-common/javascripts/page-init',
        'details-polyfill': 'details-polyfill',
        'retention-page-init': 'retention-page-init'
    },
    // Make jquery-migrate depend on the loading of jquery
    "shim": {
        'jquery-migrate': ['jquery']
    }
});

require(["retention-page-init"], function(retentionPageInit) {
    $(function() {
        retentionPageInit.init();
    });
});
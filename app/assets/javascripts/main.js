require(['config'], function() {
    require(["retention-page-init"], function(retentionPageInit) {
        $(function() {
            retentionPageInit.init();
        });
    });
});
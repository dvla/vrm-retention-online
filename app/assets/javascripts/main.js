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

/*
require.config({
    paths: {
        'jquery': '../lib/jquery/jquery',
        'header-footer-only': 'header-footer-only',
        'details-polyfill': 'details-polyfill'
    }
});

require(["jquery", "header-footer-only", "form-checked-selection", "details-polyfill"],function($) {

    // ALREADY IN COMMON
    var IE10 = (navigator.userAgent.match(/(MSIE 10.0)/g) ? true : false);
    if (IE10) {
        $('html').addClass('ie10');
    }

    $(function() {

        // ALREADY IN COMMON
        // Enabling loading class/js animation on submit's CTAs
        $('button[type="submit"]').on('click', function(e) {
            var runTimes;

            if ( $(this).hasClass("disabled") ) {
                return false;
            }

            $(this).html('Loading').addClass('loading-action disabled');
            runTimes = 0;
            setInterval(function() {
                if ( runTimes < 3 ){
                    $('button[type="submit"]').append('.');
                    runTimes++;
                } else {
                    runTimes = 0;
                    $('button[type="submit"]').html('Loading');
                }
            }, 1000);
        });

        // MOVED TO RETENTION INIT
        // If JS enabled hide summary details
        $('.details').hide();

        // MOVED TO RETENTION INIT
        // Summary details toggle
        $('.summary').on('click', function() {
            $(this).siblings().toggle();
            $(this).toggleClass('active');
        });

        // THIS IS IN COMMON
        // Disabled clicking on disabled buttons
        $('.button-not-implemented').click(function() {
            return false;
        });

        // THIS IS IN COMMON
        // Print button
        $('.print-button').click(function() {
            window.print();
            return false;
        });

        // THIS IS IN COMMON
        // smooth scroll
        $('a[href^="#"]').bind('click.smoothscroll', function (e) {
            e.preventDefault();
            var target = this.hash,
                $target = $(target);
            $('html, body').animate({
                scrollTop: $(target).offset().top - 40
            }, 750, 'swing', function () {
                window.location.hash = target;
            });
        });

        // THIS IS IN COMMON
        function updateCountdown() {
            var remaining = 500 - $('#feedback-form textarea').val().length;
            $('.character-countdown').text(remaining + ' characters remaining.');
        }

        $(document).ready(function($) {

            // IE 9- maxlenght on input textarea
            var txts = document.getElementsByTagName('TEXTAREA')
            for(var i = 0, l = txts.length; i < l; i++) {
                if(/^[0-9]+$/.test(txts[i].getAttribute("maxlength"))) {
                    var func = function() {
                        var len = parseInt(this.getAttribute("maxlength"), 10);

                        if(this.value.length > len) {
                            this.value = this.value.substr(0, len);
                            return false;
                        }
                    }
                    txts[i].onkeyup = func;
                    txts[i].onblur = func;
                }
            }
            // Update Countdown on input textarea
            $('#feedback-form textarea').change(updateCountdown);
            $('#feedback-form textarea').keyup(updateCountdown);
        });

    });

    // THIS IS IN COMMON GLOBAL HELPERS
    function areCookiesEnabled(){
        var cookieEnabled = (navigator.cookieEnabled) ? true : false;

        if (typeof navigator.cookieEnabled == "undefined" && !cookieEnabled)
        {
            document.cookie="testcookie";
            cookieEnabled = (document.cookie.indexOf("testcookie") != -1) ? true : false;
        }
        return (cookieEnabled);
    }

    // THIS IS IN COMMON GLOBAL HELPERS
    function opt(v){
        if (typeof v == 'undefined') return [];
        else return[v];
    }
});
*/
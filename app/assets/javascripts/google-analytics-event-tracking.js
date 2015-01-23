function getInputValue(inputId) {
    var inp = document.getElementById(inputId);
    if (!inp) {
        return;
    }
    addListener(inp, 'blur', function (e) {
        var from = getGaCookie() + location.href + ' - ' + this.id;
        var to = this.value;
        /*console.log("getInputValue blur from: " + from + ", to: " + to);*/
        sendAnalyticsEvent('blur', from, to);
    });
}

function trackClick(inputId) {
    var element = document.getElementById(inputId);
    if (element) {
        if (element.addEventListener) {
            /* addEventListener is a W3 standard that is implemented in the majority of other browsers (FF, Webkit, Opera, IE9+) */
            element.addEventListener('click', function (e) {
                /*console.log("trackClick addEventListener send event: " + location.href + ", id: " + inputId + ", onClick");*/
                ga('send', 'event', location.href, inputId, 'onClick');
            });
        } else if (element.attachEvent) {
            /* attachEvent can only be used on older trident rendering engines ( IE5+ IE5-8*) */
            element.attachEvent('click', function (e) {
                /*console.log("trackClick attachEvent send event: " + location.href + ", id: " + inputId + ", onClick");*/
                ga('send', 'event', location.href, inputId, 'onClick');
            });
        } else {
            console.error("element does not support addEventListener or attachEvent");
            return false;
        }
    } else {
        console.error("element id: " + inputId + " not found on page");
        return false;
    }
}
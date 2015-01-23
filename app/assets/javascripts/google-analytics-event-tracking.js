var pageStart = new Date();

function addListener(element, type, callback) {
    /*console.log(element);*/
    if (element.addEventListener) {
        /*console.log("addEventListener");*/
        element.addEventListener(type, callback);
    } else if (element.attachEvent) {
        /*console.log("attachEvent");*/
        element.attachEvent('on' + type, callback);
    }
}

function getGaCookie() {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        if (/ga/.test(cookies[i].split("=")[0]) === true) {
            return cookies[i].split("=")[1] + " - ";
        }
    }
    return "";
}

function sendAnalyticsEvent(action, from, to) {
    dateVal = new Date() - pageStart;
    ga('send', 'event', action, from, to, dateVal);
    /*console.log("sent to google action: " + action + ", from: " + from + ", to: " + to + ", dateVal: " + dateVal);*/
    pageStart = new Date();
}

function loopByTag(tag, action, type) {
    var list = document.getElementsByTagName(tag);
    for (i = 0; i < list.length; i++) {
        if (!type || list[i].type == type) {
            addListener(list[i], action, function (e) {
                var from = location.href;
                var actionName = null;
                var to = null;
                if (tag == "input") {
                    actionName = this.id || e.srcElement.id;
                    to = getGaCookie() + document.forms[0].action;
                    sendAnalyticsEvent(from, actionName, to);
                } else {
                    actionName = this.innerText || this.textContent || e.srcElement.innerText;
                    to = getGaCookie() + this.href || e.srcElement.href;
                    sendAnalyticsEvent(from, actionName, to);
                }
            });
        }
    }
}

function getInputValue(inputId) {
    var inp = document.getElementById(inputId);
    if (!inp) {
        return;
    }
    addListener(inp, 'blur', function (e) {
        var from = getGaCookie() + location.href + ' - ' + this.id;
        var to = this.value;
        sendAnalyticsEvent('blur', from, to);
    });
}
loopByTag('a', 'click');
loopByTag('button', 'click');
loopByTag('input', 'click', 'submit');
loopByTag('input', 'click', 'button');
loopByTag('input', 'click', 'checkbox');
/*
loopByTag('input','select','text');
loopByTag('input','blur','text');
*/
/* On on blur, if field value != "" then sendAnalyticsEvent  */


/*The line below would post the value of any textbox with id "postcode" to google analytics when it loses focus (regardless of validation)*/
/*getInputValue('postcode');*/
/*
* inputId - the id of the html field you want to track.
*
* event - A String that specifies the name of the event.
* Note: Do not use the "on" prefix. For example, use "click" instead of "onclick".
* For a list of all HTML DOM events, look at our complete HTML DOM Event Object Reference http://www.w3schools.com/jsref/dom_obj_event.asp.
*/
function track(inputId, event) {
    var element = document.getElementById(inputId);
    if (element) {
        if (element.addEventListener) {
            /* addEventListener is a W3 standard that is implemented in the majority of other browsers (FF, Webkit, Opera, IE9+) */
            element.addEventListener(event, function (e) {
                console.log("trackClick addEventListener send event: " + location.href + ", id: " + inputId + ", event " + event);
                ga('send', 'event', location.href, inputId, event);
            });
        } else if (element.attachEvent) {
            /* attachEvent can only be used on older trident rendering engines ( IE5+ IE5-8*) */
            element.attachEvent(event, function (e) {
                /*console.log("trackClick attachEvent send event: " + location.href + ", id: " + inputId + ", event " + event);*/
                ga('send', 'event', location.href, inputId, event);
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
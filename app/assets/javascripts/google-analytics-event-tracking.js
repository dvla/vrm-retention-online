var pageStart = new Date();

function addListener(element, type, callback) {
    if (element.addEventListener) element.addEventListener(type, callback);
    else if (element.attachEvent) element.attachEvent('on' + type, callback);
}

function getGaCookie() {
    var cookies = document.cookie.split(';')
    for(var i=0;i<cookies.length;i++) {
        if(/ga/.test(cookies[i].split("=")[0])==true){
            return cookies[i].split("=")[1] + " - ";
        }
    }
    return "";
}

function sendAnalyticsEvent(action,from,to) {
    dateVal = new Date() - pageStart;
    ga('send','event',action,from,to,dateVal);
    console.log("sent to google action: " + action + ", from: " + from + ", to: " + to + ", dateVal: " + dateVal);
    pageStart = new Date();
}

function loopByTag(tag,action,type) {
    var list = document.getElementsByTagName(tag);
    for (i=0;i<list.length;i++){
        if(!type||list[i].type==type) {
            addListener(list[i], action, function(e) {
                var from = location.href;
                if(tag=="input"){
                    var actionName = this.value||e.srcElement.value;
                    var to = getGaCookie() + document.forms[0].action;
                } else {
                    var actionName = this.innerText||this.textContent||e.srcElement.innerText;
                    var to = getGaCookie() + this.href||e.srcElement.href;
                }
                sendAnalyticsEvent(from,actionName,to);
            })
        }
    }
}

function getInputValue(inputId) {
    var inp = document.getElementById(inputId);
    if(!inp){return}
    addListener(inp,'blur',function(e){
        var from = getGaCookie() + location.href + ' - ' + this.id;
        var to = this.value
        sendAnalyticsEvent('blur',from,to);
    })
}

function virtualPageView() {
    var spans = document.getElementsByTagName('span');
    for(i=0;i<spans.length;i++) {
        if(/nino-help/gim.test(spans[i].outerHTML)){
            addListener(spans[i],'click',function(e){
                var from =  + location.href + ' - ' + this.id;
                var to = getGaCookie() + "/nino-help";
                ga('send', 'pageview', 'nino-help');
            })
        }
        if(/dln-help/gim.test(spans[i].outerHTML)){
            addListener(spans[i],'click',function(e){
                var from =  + location.href + ' - ' + this.id;
                var to = getGaCookie() + "/dln-help";
                ga('send', 'pageview', 'dln-help');
            })
        }
    }
}

loopByTag('a','click');
loopByTag('button','click');
loopByTag('input','click','submit');
loopByTag('input','click','button');
virtualPageView();
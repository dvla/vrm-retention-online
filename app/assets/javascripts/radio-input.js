//radio button selection, active class
var radiosWrap = $('div.radio');
var radioLabel = radiosWrap.find('.form-radio.selectable');
var radio = radioLabel.children('input:radio');

radio.each(function() {
  $(this).on('change', function() {
    if($('input:radio:checked').length > 0){
      $('input:radio').parent('label').removeClass('selected');
      $(this).parent('label').addClass("selected");
    } else if (!$('input:radio:checked').length > 0){
        $(this).parent('label').removeClass("selected");
    }
  });
});
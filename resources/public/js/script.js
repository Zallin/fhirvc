$(function() {
    $(document).foundation()

    $('.tree li > p').click(function (e){
	e.preventDefault()
	var childUl = $(this).parent().children('ul')
	var newVal = childUl.css('display') == 'none' ? 'block' : 'none';
	childUl.css('display', newVal);
    });
});

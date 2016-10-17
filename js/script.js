$('form').submit(function (e){
    e.preventDefault()
    newUrl = $('select option:selected').attr('value')
    window.location.href = location.host + newUrl;
});

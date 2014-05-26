$(document).ready(function(){
    $('input[name=schemaSource]:radio').change(function() {
        checkFileSelect();
    });
    
    $('input[name=schemaCustomization]:radio').change(function() {
        checkCustomizationSelect();
    });
    
    $('input[name=guidelinesSource]:radio').change(function() {
        checkGuidelinesSelect();
    });
    
    checkFileSelect();
    checkCustomizationSelect();
    checkGuidelinesSelect();
});

function checkFileSelect() {
    
    var mei2013 = $("#schemaCustomizationMEI2013")[0];
	var mei2012 = $("#schemaCustomizationMEI2012")[0];
	var branch = $("#schemaCustomizationBranch")[0];
			
	if($('#mei2013')[0].checked) {
		mei2013.checked = 'checked';
		branch.disabled = 'disabled';
		mei2012.disabled = 'disabled';
		mei2013.disabled = '';
		
	}else if($('#mei2012')[0].checked) {
		mei2012.checked = 'checked';
		branch.disabled = 'disabled';
		mei2013.disabled = 'disabled';
		mei2012.disabled = '';
		
	}else if($('#dev')[0].checked) {
		branch.checked = 'checked';
		mei2013.disabled = 'disabled';
		mei2012.disabled = 'disabled';
		branch.disabled = '';
		
	}else if($('#local')[0].checked) {
		mei2013.disabled = '';
		mei2012.disabled = '';
		branch.disabled = '';
	}
    
    
    if($('#local')[0].checked)
        $('#localFileSelect').show();
    else
        $('#localFileSelect').hide();
}

function checkCustomizationSelect() {
    if($('#schemaCustomizationLocal')[0].checked)
        $('#localCustomizationSelect').show();
    else
        $('#localCustomizationSelect').hide();
}

function checkGuidelinesSelect() {
    if($('#guidelines_local')[0].checked)
        $('#localGuidelinesSelect').show();
    else
        $('#localGuidelinesSelect').hide();
}
var profileArray = Array();
var profileParam = '';
/*
	LicenseStates defined in License.js must already exist.
	Values added to profileArray must match a profile name specified in a profile
	processing instruction in the source xml file.
*/
if(typeof(LicenseStates) != 'undefined')
{
	if(LicenseStates)
	{
		if(LicenseStates.remotemgmt == true)
			profileArray.push('zcm');
		
		if(LicenseStates.asset == true)
			profileArray.push('zam');
		
		if(LicenseStates.patch == true)
			profileArray.push('zpm');
		
		if(LicenseStates.zesm == true)
			profileArray.push('zesm');
    
		if(LicenseStates.fde == true)
			profileArray.push('zfde');
      
		if(LicenseStates.zmm == true)
			profileArray.push('zmm');       			
	}
}

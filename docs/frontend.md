# Frontend developer documentation

If you are a frontend developer you may find this guide useful.

## GOVUK resources

This application should be designed to pass a GDS service assessment 
but it is not expected to be hosted on .gov.uk.   The header and footer, for example,  
do not carry the Crown logo.

* [The GDS service manual](https://www.gov.uk/service-manual/developers)
* [GDS Elements](https://www.gov.uk/service-manual/design/using-the-govuk-template-frontend-toolkit-and-elements) - 
a collection of [SASS](http://sass-lang.com/guide) repositories and a page template
* [Design patterns](https://www.gov.uk/service-manual/user-centred-design/resources/patterns/index.html)
* [Progressive enhancement](https://www.gov.uk/service-manual/technology/using-progressive-enhancement)
* [Targeted browsers](https://www.gov.uk/service-manual/technology/designing-for-different-browsers-and-devices)
* [Accessibility](https://www.gov.uk/service-manual/helping-people-to-use-your-service/making-your-service-accessible-an-introduction)

### Additional accessibility resources
* [Paypal Automated Accessibility Testing Tool](https://github.com/paypal/AATT) 
* [WAVE plugin for Chrome](https://chrome.google.com/webstore/detail/wave-evaluation-tool/jbbplnpkjmmeebjpijfedlgcdilocofh)
* [Screen reader poll from RNIB](https://www.rnib.org.uk/webaim-screen-reader-survey-5-what-are-trends)
* [Tink's blog](http://tink.uk/)

## UX Prototypes

The UX language is efficient and simple.  This is the result of much thought and synthesis from the UX team, and 
considerable research with the users.  

The UX team have provided high fidelity mock-ups of the screens and interactions.  

* [Appplicant Journey](http://37w3jk.axshare.com/#g=1&p=start_page)
* [Portfolio Manager Journey](http://3skvvc.axshare.com/#g=1&p=start_page)

## Hosting

View the [The demonstration site](https://rifs-demo.herokuapp.com)


## Tools

You may want to consider using [IntelliJ Ultimate](https://www.jetbrains.com/idea/) with the Scala plugin.

[Virtualbox](https://www.virtualbox.org/) and [Microsoft's virtual machine images](https://developer.microsoft.com/en-us/microsoft-edge/tools/vms/)
allow various flavours of IE to be run on the desktop.

If you use macOS it's worth finding out how to use [VoiceOver](https://www.apple.com/voiceover/info/guide/).

On Windows you can use [NVDS](http://www.nvaccess.org/) and [JAWS](http://www.freedomscientific.com/Products/Blindness/JAWS).


## Code

The sass for the site is located here: [stylesheets](https://github.com/UKGovernmentBEIS/rifs-frontend-play/tree/master/src/main/assets/stylesheets) 

Logically the application sass depends on GDS Elements which depends on GDS Frontend Toolkit, but the SBT sass plugin 
was unable to set the sass load-path.  The following folder structure is the work-around, and it's important to note 
that because of this the vendor dependencies are actually checked into the source code. 


Folder explanation:

* `colours` - the colours folder from the GDS Frontend Toolkit
* `design-patterns` - also from the GDS Frontend Toolkit
* `elements` - files from GDS Elements SASS repo
* `static` - files taken from [AlphaGov static repo](https://github.com/alphagov/static)
* The files in the `stylesheets` folder itself are from the GDS Frontend Toolkit
 
The [application.scss](https://github.com/UKGovernmentBEIS/rifs-frontend-play/blob/master/src/main/assets/stylesheets/application.scss) file
pulls everything needed in.  







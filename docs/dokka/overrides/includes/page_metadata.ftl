<#macro display>
	<title>${pageName}</title>
	<@template_cmd name="pathToRoot">
		<link href="${pathToRoot}images/logo-icon.svg" rel="icon" type="image/svg">
	</@template_cmd>

    <@template_cmd name="projectName">
		<!-- Privacy-friendly analytics by Plausible -->
		<script async src="https://p.opensavvy.dev/script.js"></script>
		<script>
			window.plausible = window.plausible || function () {
				(plausible.q = plausible.q || []).push(arguments)
			}, plausible.init = plausible.init || function (i) {
				plausible.o = i || {}
			};
			plausible.init({
				endpoint: 'https://p.opensavvy.dev/event',
				customProperties: function (eventname) {
					return {
						host: window.location.host,
						project: '${projectName}',
						area: 'dokka',
					}
				},
				transformRequest: function (request) {
					request.u = request.u.replace(/.*\/jobs\/[0-9]+\/artifacts\/[^\/]*/g, ''); // Remove review app URL prefix
					return request;
				},
			})
		</script>
    </@template_cmd>
</#macro>


def call(state, recipients = null , stage = null, extra_body = null) {
    def causes = currentBuild.rawBuild.getCauses()
    def cause = null
    if (!causes.isEmpty()) {
        cause = causes[0].getShortDescription()
    }
    log = currentBuild.rawBuild.getLog(40).join('\n')
    causes = null
    def body = """
                    <p>$env.JOB_NAME - Release # $currentBuild.displayName ran on $env.NODE_NAME and ended with $currentBuild.result .
                    </p>
                    <p><b>Build trigger</b>: $cause</p>
                    <p>Check console output at: <a href="$env.BUILD_URL">$env.BUILD_URL</a></p>
                """
    if (extra_body) {
        body += extra_body
    }
    /*if  (state != 'SUCCESS') {
        body += """
            <p><b>Failed on stage</b>: $stage</p>
            <h2>Last lines of output:</h2>
            <pre>$log</pre>
        """
    }*/
    switch (state) {
        case 'FAILURE':
            body += """
            <p><b>Failed on stage</b>: $stage</p>
            <h2>Last lines of output:</h2>
            <pre>$log</pre>
            """
            break
        case 'UNSTABLE':
            body += """
            <p><b>Unstable on stage</b>: $stage</p>
            """
            break
        default:
            break
    }
    if (!recipients) {
        recipients = emailextrecipients([[$class: 'UpstreamComitterRecipientProvider'],
                                         [$class: 'FailingTestSuspectsRecipientProvider'],
                                         [$class: 'FirstFailingBuildSuspectsRecipientProvider'],
                                         [$class: 'CulpritsRecipientProvider'],
                                         [$class: 'DevelopersRecipientProvider'],
                                         [$class: 'RequesterRecipientProvider']])
    }
    emailext attachLog: true, body: body ,
            compressLog: true,
            mimeType: 'text/html',
            //subject: "$env.JOB_NAME $env.BUILD_NUMBER: $currentBuild.result",
            subject: "${JOB_NAME.split( '/' )[-1]} - Release # $currentBuild.displayName : $currentBuild.result",

            to: recipients
}




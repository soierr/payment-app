mvn release:prepare -B -Dproject.rel.play:payment-app=1.0 -Dproject.dev.play:payment-app=1.1-SNAPSHOT

mvn release:prepare -B -DdryRun=true
mvn release:prepare -B
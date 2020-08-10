
{{- define "gateway.host" -}}
{{- printf "%s.%s.%s.%s" .Chart.Name .Release.Name .Release.Namespace (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- end -}}

{{- define "gateway.host.govuk" -}}
{{- if eq .Release.Name "production" -}}
{{- printf "%s.%s" .Chart.Name .Values.global.domainGovUK -}}
{{- else -}}
{{- printf "%s.%s.%s" .Chart.Name .Release.Name .Values.global.domainGovUK -}}
{{- end -}}
{{- end -}}

{{- define "gateway.entityID" -}}
{{- printf "https://%s%s" (include "gateway.host.govuk" .) .Values.gateway.metadataPath -}}
{{- end -}}

{{- define "stubConnector.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s.%s.%s.%s" "stub-connector" .Release.Name .Release.Namespace (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s" .Values.stubConnector.host -}}
{{- end -}}
{{- end -}}

{{- define "stubConnector.host.govuk" -}}
{{- printf "%s.%s.%s" "stub-connector" .Release.Name .Values.global.domainGovUK -}}
{{- end -}}

{{- define "connector.metadata.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s" (include "stubConnector.host.govuk" .) | trimSuffix "-" -}}
{{- else -}}
{{ printf "%s" (required "connector.metadata.fqdn or stubConnector.enabled required" .Values.connector.metadata.fqdn) }}
{{- end -}}
{{- end -}}

{{- define "connector.metadata.url" -}}
{{ printf "%s://%s%s" .Values.connector.metadata.scheme (include "connector.metadata.host" .) .Values.connector.metadata.path }}
{{- end -}}

{{- define "connector.metadata.assertionConsumerService.url" -}}
{{ printf "%s://%s%s" .Values.connector.metadata.scheme (include "connector.metadata.host" .) .Values.connector.metadata.assertionConsumerServicePath }}
{{- end -}}

{{- define "connector.entityID" -}}
{{- if .Values.stubConnector.enabled -}}
{{ include "connector.metadata.url" . }}
{{- else -}}
{{ printf "%s" (required "connector.entityID or stubConnector.enabled required" .Values.connector.entityID) }}
{{- end -}}
{{- end -}}

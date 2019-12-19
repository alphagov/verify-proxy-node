
{{- define "gateway.host" -}}
{{- printf "%s.%s.%s.%s" .Chart.Name "eidas" .Release.Name (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- end -}}

{{- define "metatron.host" -}}
{{- printf "%s.%s.%s" "metatron.eidas" .Release.Name (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- end -}}

{{- define "gateway.entityID" -}}
{{- printf "https://%s%s" (include "gateway.host" .) .Values.gateway.metadataPath -}}
{{- end -}}

{{- define "stubConnector.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s.%s.%s" "stub-connector.eidas" .Release.Name (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s" .Values.stubConnector.host -}}
{{- end -}}
{{- end -}}

{{- define "connector.metadata.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s" (include "stubConnector.host" .) | trimSuffix "-" -}}
{{- else -}}
{{ printf "%s" (required "connector.metadata.fqdn or stubConnector.enabled required" .Values.connector.metadata.fqdn) }}
{{- end -}}
{{- end -}}

{{- define "connector.metadata.url" -}}
{{ printf "%s://%s%s" .Values.connector.metadata.scheme (include "connector.metadata.host" .) .Values.connector.metadata.path }}
{{- end -}}

{{- define "connector.entityID" -}}
{{- if .Values.stubConnector.enabled -}}
{{ include "connector.metadata.url" . }}
{{- else -}}
{{ printf "%s" (required "connector.entityID or stubConnector.enabled required" .Values.connector.entityID) }}
{{- end -}}
{{- end -}}

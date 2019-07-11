
{{- define "gateway.host" -}}
{{- printf "%s-%s.%s" .Release.Name .Chart.Name (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- end -}}

{{- define "connector.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s-%s.%s" .Release.Name "connector" (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s" .Values.connector.host -}}
{{- end -}}
{{- end -}}

{{- define "metadata.host" -}}
{{- printf "%s-%s.%s" .Release.Name "metadata" (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- end -}}

{{- define "connector.entityID" -}}
{{- if .Values.stubConnector.enabled -}}
https://{{ include "connector.host" . }}
{{- else -}}
{{ printf "%s" (required "connector.entityID or stubConnector.enabled required" .Values.connector.entityID) }}
{{- end -}}
{{- end -}}

{{- define "connector.metadata.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s-%s.%s" .Release.Name "connector-metadata" (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- else -}}
{{ printf "%s" (required "connector.metadata.fqdn or stubConnector.enabled required" .Values.connector.metadata.fqdn) }}
{{- end -}}
{{- end -}}

{{- define "connector.metadata.url" -}}
{{ printf "%s://%s%s" .Values.connector.metadata.scheme (include "connector.metadata.host" .) .Values.connector.metadata.path }}
{{- end -}}

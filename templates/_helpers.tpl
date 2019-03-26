
{{- define "gateway.host" -}}
{{- printf "%s-%s.%s" .Release.Name .Chart.Name (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- end -}}

{{- define "connector.host" -}}
{{- if .Values.stubConnector.enabled -}}
{{- printf "%s-%s.%s" .Release.Name "connector" (required "global.cluster.domain is required" .Values.global.cluster.domain) | trimSuffix "-" -}}
{{- else -}}
{{- required "connector.host or stubConnector.enabled required" .Values.connector.host -}}
{{- end -}}
{{- end -}}

{{- define "connector.entityID" -}}
{{- if .Values.stubConnector.enabled -}}
https://{{ include "connector.host" . }}
{{- else -}}
{{ printf "%s" (required "connector.entityID or stubConnector.enabled required" .Values.connector.entityID) }}
{{- end -}}
{{- end -}}

{{- define "connector.metadataURL" -}}
{{- if .Values.stubConnector.enabled -}}
http://{{ .Release.Name }}-connector-metadata/metadata.xml
{{- else -}}
{{ printf "%s" (required "connector.metadataURL or stubConnector.enabled required" .Values.connector.metadataURL) }}
{{- end -}}
{{- end -}}

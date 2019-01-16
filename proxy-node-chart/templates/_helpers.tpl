{{/* vim: set filetype=mustache: */}}
{{/*
Build a docker image name from an app
*/}}
{{- define "docker_image" -}}
{{- $name := index . 0 -}}
{{- $data := index . 1 -}}
{{- index $data "Values" "global" $name "image" -}}
{{- end -}}

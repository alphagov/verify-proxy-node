{{/* vim: set filetype=mustache: */}}
{{/*
Build a docker image name from an app
*/}}
{{- define "docker_image" -}}
{{- $name := index . 0 -}}
{{- $data := index . 1 -}}
{{- $repo := index $data "Values" "global" $name "repository" -}}
{{- $tag := index $data "Values" "global" $name "tag" -}}
{{- printf "%s:%s" $repo $tag -}}
{{- end -}}

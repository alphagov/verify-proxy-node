#!/usr/bin/env bash


follow=false

ref_rebuild=false
ref_recompile=false

while [ ! $# -eq 0 ]
do
	case "$1" in
		--follow)
      follow=true
			;;
		--ref_rebuild)
      ref_rebuild=true
			;;
		--ref_recompile)
      ref_recompile=true
			;;
		*)
      echo "Usage $0 [--follow --ref_rebuild --ref_recompile]"
			exit 1
			;;
	esac
	shift
done

reference_scripts_dir="../../verify-eidas-reference-1.4/scripts"

if [ "$ref_recompile" = true ]
then
  ref_rebuild=true
  "$reference_scripts_dir"/compile.sh
fi

if [ "$ref_rebuild" = true ]
then
  "$reference_scripts_dir"/build_docker_image.sh
fi


"$reference_scripts_dir"/copy_metadata.sh
cp Dockerfile.stub-idp ../../ida-stub-idp
cp scripts/start-stub-idp.sh ../../ida-stub-idp/start-stub-idp.sh
rm -r ../../ida-stub-idp/notification_resources || true
cp -r ../stub-idp/resources/local ../../ida-stub-idp/notification_resources
docker-compose up --build -d

if [ "$follow" = true ]
then
  docker-compose logs -f
fi


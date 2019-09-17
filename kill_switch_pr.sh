#!/bin/bash

branch_name=kill-switch

files_to_comment="
    chart/templates/gateway-ingress-virtual-service.yaml
    proxy-node-acceptance-tests/features/proxy_node.feature
"

echo "Creating kill switch branch"
git checkout master
git pull origin master

commit_hash=$(git rev-parse HEAD)

git branch -D $branch_name
git checkout -b $branch_name

for file in $files_to_comment
do
    echo "Commenting ${file}"
    sed -e 's/^\([^#].*\)/# \1/g' -i '' $file 
    git add $file
done

git commit -m "BAU: Creating kill switch at $(date) for commit ${commit_hash}"

git push origin $branch_name -f

echo "Pushing branch."

# https://www.gnu.org/software/make/manual/make.html#Automatic-Variables

# requires: pandoc 1.16 or higher

# https://github.com/freebroccolo/docker-haskell/blob/a396f0d9b35cabeb60920abd87a8b2612530cb1b/7.10/Dockerfile
# https://github.com/jagregory/pandoc-docker/blob/master/Dockerfile

# docker images
# docker run --rm -t -i --entrypoint="/bin/bash" pandoc
# docker run --rm -e "userid=$(id -u):$(id -g)" -t -i --entrypoint="/bin/bash" pandoc

# https://github.com/docker/docker/issues/3206
# http://stackoverflow.com/questions/27925006/using-host-environment-variables-with-dockerfile

docker-image-cleanup:
	docker rmi -f $(docker images | grep "<none>" | awk "{print \$3}")

index.html: header.md body.md
	touch $@ && docker run --rm -e "userid=$$(id -u):$$(id -g)" -t -v `pwd`:/source jagregory/pandoc --standalone -S --highlight-style pygments --toc -c pandoc.css -f markdown+link_attributes -t html5 --self-contained -o $@ $^


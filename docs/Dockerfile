FROM python:3.14.0-alpine3.21

RUN pip install --no-cache-dir zensical==0.0.11

COPY ./ /docs

# see README for why this is necessary
EXPOSE 8000/tcp

WORKDIR /docs

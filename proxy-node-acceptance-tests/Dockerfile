FROM ruby:2.6.5 as bundler

COPY Gemfile Gemfile
COPY Gemfile.lock Gemfile.lock

RUN gem install bundler
RUN bundle install


FROM ruby:2.6.5-slim

COPY Gemfile Gemfile
COPY Gemfile.lock Gemfile.lock
COPY --from=bundler /usr/local/bundle/ /usr/local/bundle/

COPY features /features

ENTRYPOINT bundle exec cucumber --color --strict --tags "not @ignore" features/acceptance/

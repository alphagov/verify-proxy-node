FROM ruby:2.6.5

COPY Gemfile Gemfile
COPY Gemfile.lock Gemfile.lock

RUN gem install bundler
RUN bundle install

COPY features /features

ENTRYPOINT bundle exec cucumber --color --strict --tags "not @ignore" features/acceptance/
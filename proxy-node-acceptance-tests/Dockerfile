FROM ruby:2.5.3

COPY Gemfile Gemfile
COPY Gemfile.lock Gemfile.lock

RUN bundle install

COPY features /features

ENTRYPOINT ["bundle", "exec", "cucumber", "--strict"]

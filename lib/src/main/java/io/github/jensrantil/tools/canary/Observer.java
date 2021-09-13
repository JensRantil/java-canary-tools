package io.github.jensrantil.tools.canary;

public interface Observer {
    class EmptyObserver implements Observer {

        @Override
        public void registerFailure() {
            // deliberately left empty.
        }

        @Override
        public void registerSuccess() {
            // deliberately left empty.
        }

        @Override
        public Summary getSummary() {
            return new Summary(0, 0);
        }
    }

    Observer EMPTY_OBSERVER = new EmptyObserver();

    void registerFailure();

    void registerSuccess();

    Summary getSummary();
}

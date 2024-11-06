def call() {
    sh """
        . ${env.VENV_NAME}/bin/activate
        pip install flake8 pylint
        flake8 . --max-line-length=120 --exclude=${env.VENV_NAME} || true
        pylint --exit-zero *.py || true
    """
}
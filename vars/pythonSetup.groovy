def call() {
    sh """
        ${env.PYTHON_VERSION} -m venv ${env.VENV_NAME}
        . ${env.VENV_NAME}/bin/activate
        pip install --upgrade pip
        pip install Flask==2.0.1 Werkzeug==2.0.1
        pip install pytest==7.4.0 pytest-cov==4.1.0
        pip install -r requirements.txt
    """
}
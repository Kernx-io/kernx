from setuptools import setup, find_packages

setup(
    name="kernx",
    version="1.0.0",
    packages=find_packages(),
    install_requires=[
        "requests>=2.25.0",
    ],
    description="Python Client for the Kernx Deterministic Kernel",
    author="Kernx",
    python_requires='>=3.8',
)
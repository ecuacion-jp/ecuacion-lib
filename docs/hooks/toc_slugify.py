"""MkDocs hook: keep non-ASCII (Japanese) characters in heading anchors.

This is the same as writing the following in mkdocs.yml, but avoids the Python-specific
YAML tag that IDE YAML language servers report as "Unresolved tag":

    - toc:
        slugify: !!python/object/apply:pymdownx.slugs.slugify
          kwds:
            case: lower
"""

from pymdownx.slugs import slugify


def on_config(config, **kwargs):
    config["mdx_configs"].setdefault("toc", {})["slugify"] = slugify(case="lower")
    return config

package com.zane.smapiinstaller.dto;

import java.util.List;

/**
 * @author Zane
 */
public class ModUpdateCheckResponseDto {
    private String id;
    private UpdateInfo suggestedUpdate;
    private List<String> errors;
    private Metadata metadata;


    public static class UpdateInfo {
        private String version;
        private String url;

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public String getVersion() {
            return this.version;
        }

        @SuppressWarnings("all")
        public String getUrl() {
            return this.url;
        }

        @SuppressWarnings("all")
        public void setVersion(final String version) {
            this.version = version;
        }

        @SuppressWarnings("all")
        public void setUrl(final String url) {
            this.url = url;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ModUpdateCheckResponseDto.UpdateInfo)) return false;
            final ModUpdateCheckResponseDto.UpdateInfo other = (ModUpdateCheckResponseDto.UpdateInfo) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$version = this.getVersion();
            final Object other$version = other.getVersion();
            if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
            final Object this$url = this.getUrl();
            final Object other$url = other.getUrl();
            if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof ModUpdateCheckResponseDto.UpdateInfo;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $version = this.getVersion();
            result = result * PRIME + ($version == null ? 43 : $version.hashCode());
            final Object $url = this.getUrl();
            result = result * PRIME + ($url == null ? 43 : $url.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "ModUpdateCheckResponseDto.UpdateInfo(version=" + this.getVersion() + ", url=" + this.getUrl() + ")";
        }

        @SuppressWarnings("all")
        public UpdateInfo() {
        }

        @SuppressWarnings("all")
        public UpdateInfo(final String version, final String url) {
            this.version = version;
            this.url = url;
        }
        //</editor-fold>
    }


    public static class Metadata {
        private Main main;


        public static class Main {
            private String version;
            private String url;

            //<editor-fold defaultstate="collapsed" desc="delombok">
            @SuppressWarnings("all")
            public Main() {
            }

            @SuppressWarnings("all")
            public String getVersion() {
                return this.version;
            }

            @SuppressWarnings("all")
            public String getUrl() {
                return this.url;
            }

            @SuppressWarnings("all")
            public void setVersion(final String version) {
                this.version = version;
            }

            @SuppressWarnings("all")
            public void setUrl(final String url) {
                this.url = url;
            }

            @Override
            @SuppressWarnings("all")
            public boolean equals(final Object o) {
                if (o == this) return true;
                if (!(o instanceof ModUpdateCheckResponseDto.Metadata.Main)) return false;
                final ModUpdateCheckResponseDto.Metadata.Main other = (ModUpdateCheckResponseDto.Metadata.Main) o;
                if (!other.canEqual((Object) this)) return false;
                final Object this$version = this.getVersion();
                final Object other$version = other.getVersion();
                if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
                final Object this$url = this.getUrl();
                final Object other$url = other.getUrl();
                if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
                return true;
            }

            @SuppressWarnings("all")
            protected boolean canEqual(final Object other) {
                return other instanceof ModUpdateCheckResponseDto.Metadata.Main;
            }

            @Override
            @SuppressWarnings("all")
            public int hashCode() {
                final int PRIME = 59;
                int result = 1;
                final Object $version = this.getVersion();
                result = result * PRIME + ($version == null ? 43 : $version.hashCode());
                final Object $url = this.getUrl();
                result = result * PRIME + ($url == null ? 43 : $url.hashCode());
                return result;
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "ModUpdateCheckResponseDto.Metadata.Main(version=" + this.getVersion() + ", url=" + this.getUrl() + ")";
            }
            //</editor-fold>
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public Metadata() {
        }

        @SuppressWarnings("all")
        public Main getMain() {
            return this.main;
        }

        @SuppressWarnings("all")
        public void setMain(final Main main) {
            this.main = main;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ModUpdateCheckResponseDto.Metadata)) return false;
            final ModUpdateCheckResponseDto.Metadata other = (ModUpdateCheckResponseDto.Metadata) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$main = this.getMain();
            final Object other$main = other.getMain();
            if (this$main == null ? other$main != null : !this$main.equals(other$main)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof ModUpdateCheckResponseDto.Metadata;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $main = this.getMain();
            result = result * PRIME + ($main == null ? 43 : $main.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "ModUpdateCheckResponseDto.Metadata(main=" + this.getMain() + ")";
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public ModUpdateCheckResponseDto() {
    }

    @SuppressWarnings("all")
    public String getId() {
        return this.id;
    }

    @SuppressWarnings("all")
    public UpdateInfo getSuggestedUpdate() {
        return this.suggestedUpdate;
    }

    @SuppressWarnings("all")
    public List<String> getErrors() {
        return this.errors;
    }

    @SuppressWarnings("all")
    public Metadata getMetadata() {
        return this.metadata;
    }

    @SuppressWarnings("all")
    public void setId(final String id) {
        this.id = id;
    }

    @SuppressWarnings("all")
    public void setSuggestedUpdate(final UpdateInfo suggestedUpdate) {
        this.suggestedUpdate = suggestedUpdate;
    }

    @SuppressWarnings("all")
    public void setErrors(final List<String> errors) {
        this.errors = errors;
    }

    @SuppressWarnings("all")
    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ModUpdateCheckResponseDto)) return false;
        final ModUpdateCheckResponseDto other = (ModUpdateCheckResponseDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$suggestedUpdate = this.getSuggestedUpdate();
        final Object other$suggestedUpdate = other.getSuggestedUpdate();
        if (this$suggestedUpdate == null ? other$suggestedUpdate != null : !this$suggestedUpdate.equals(other$suggestedUpdate)) return false;
        final Object this$errors = this.getErrors();
        final Object other$errors = other.getErrors();
        if (this$errors == null ? other$errors != null : !this$errors.equals(other$errors)) return false;
        final Object this$metadata = this.getMetadata();
        final Object other$metadata = other.getMetadata();
        if (this$metadata == null ? other$metadata != null : !this$metadata.equals(other$metadata)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof ModUpdateCheckResponseDto;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $suggestedUpdate = this.getSuggestedUpdate();
        result = result * PRIME + ($suggestedUpdate == null ? 43 : $suggestedUpdate.hashCode());
        final Object $errors = this.getErrors();
        result = result * PRIME + ($errors == null ? 43 : $errors.hashCode());
        final Object $metadata = this.getMetadata();
        result = result * PRIME + ($metadata == null ? 43 : $metadata.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "ModUpdateCheckResponseDto(id=" + this.getId() + ", suggestedUpdate=" + this.getSuggestedUpdate() + ", errors=" + this.getErrors() + ", metadata=" + this.getMetadata() + ")";
    }
    //</editor-fold>
}
